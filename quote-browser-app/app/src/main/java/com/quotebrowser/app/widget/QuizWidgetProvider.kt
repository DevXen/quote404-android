package com.quotebrowser.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.quotebrowser.app.MainActivity
import com.quotebrowser.app.R
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.applyDisplayBreaks
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.redactNicksForQuiz
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.worker.QuizWidgetWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Home-screen widget showing the daily "Who said it?" quiz question: the
 * day's quote with nicks redacted, plus the four nick options. Tapping the
 * widget opens the app into the Who Said It game. The question is
 * date-seeded, so every widget instance shows the same question all day;
 * [QuizWidgetWorker] refreshes it daily.
 */
class QuizWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.quotebrowser.app.widget.QUIZ_REFRESH"
        private const val QUIZ_WORK = "quiz_widget_refresh"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        ensureDailyRefresh(context)
        appWidgetIds.forEach { refreshWidget(context, appWidgetManager, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                ?: mgr.getAppWidgetIds(ComponentName(context, javaClass))
            ids.forEach { refreshWidget(context, mgr, it) }
        }
    }

    /**
     * Arms the daily refresh worker once (KEEP: re-placing the widget
     * doesn't reschedule). Lives in the provider so no app-level wiring
     * is needed.
     */
    private fun ensureDailyRefresh(context: Context) {
        try {
            val request = PeriodicWorkRequestBuilder<QuizWidgetWorker>(1, TimeUnit.DAYS)
                .addTag(QUIZ_WORK)
                .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    QUIZ_WORK,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        } catch (e: Exception) {
            // Widget still works; it just won't auto-refresh daily.
        }
    }

    private fun refreshWidget(
        context: Context,
        mgr: AppWidgetManager,
        appWidgetId: Int
    ) {
        val pending = goAsync()
        scope.launch {
            try {
                mgr.updateAppWidget(appWidgetId, buildViews(context, appWidgetId))
            } catch (e: Exception) {
                // Leave the previous widget content in place on failure.
            } finally {
                pending.finish()
            }
        }
    }

    private data class QuizQuestion(
        val dbId: String,
        val quote: Quote,
        val dbLabel: String,
        val redacted: String,
        val options: List<String>
    )

    /**
     * The day's who-said-it question, seeded by the date: identical on
     * every widget instance until midnight. Honors "Never show again"
     * and the content filter.
     */
    private suspend fun pickDailyQuestion(manager: DatabaseManager): QuizQuestion? =
        withContext(Dispatchers.IO) {
            try {
                val today = LocalDate.now()
                val rng = Random(today.toEpochDay())
                val top = manager.topNicks(60).map { it.first }
                if (top.size < 4) return@withContext null
                val configured = manager.randomSourceIds.first()
                val all = manager.allDbIds()
                val ids = (configured ?: all.toSet()).ifEmpty { all.toSet() }.toList()
                if (ids.isEmpty()) return@withContext null
                repeat(30) {
                    val id = ids[rng.nextInt(ids.size)]
                    val quotes = try {
                        manager.repoFor(id).randomQuotes(20)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val pick = quotes.shuffled(rng).firstOrNull { q ->
                        extractNicks(q.text).isNotEmpty() &&
                            !manager.isQuoteHiddenFromLists(id, q.number, q.text)
                    } ?: return@repeat
                    val answer = extractNicks(pick.text).first()
                    val distractors = top
                        .filter { t -> !t.equals(answer, ignoreCase = true) }
                        .shuffled(rng)
                        .take(3)
                    if (distractors.size < 3) return@repeat
                    val display = applyDisplayBreaks(
                        pick.text,
                        manager.ircLineBreaks.value,
                        manager.dialogueLineBreaks.value
                    )
                    return@withContext QuizQuestion(
                        dbId = id,
                        quote = pick,
                        dbLabel = manager.shortLabel(id),
                        redacted = redactNicksForQuiz(display, answer).first,
                        options = (distractors + answer).shuffled(rng)
                    )
                }
                null
            } catch (e: Exception) {
                null
            }
        }

    private suspend fun buildViews(context: Context, appWidgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_quiz)
        val manager = DatabaseManager.getInstance(context.applicationContext)
        val question = pickDailyQuestion(manager)
        if (question == null) {
            views.setTextViewText(R.id.quiz_text, "No quiz available today")
            views.setTextViewText(R.id.quiz_options, "")
            views.setTextViewText(R.id.quiz_meta, "")
        } else {
            views.setTextViewText(R.id.quiz_text, question.redacted)
            views.setTextViewText(
                R.id.quiz_options,
                question.options.mapIndexed { i, nick ->
                    "${'A' + i}. $nick"
                }.joinToString("\n")
            )
            val quizMetaText =
                "Who said it?  ·  #${question.quote.number}  ·  ${question.dbLabel}"
            // v1.59: render the question's quote as its database's
            // site-theme card; fall back to the plain styling when the
            // database has no site theme or resolution fails.
            val cardColors = try {
                resolveWidgetCardColors(
                    manager.widgetThemeSnapshots(),
                    question.dbId
                )
            } catch (e: Exception) {
                null
            }
            if (cardColors != null) {
                views.applyWidgetCardColors(
                    R.id.quiz_root,
                    R.id.quiz_text,
                    R.id.quiz_meta,
                    cardColors,
                    R.id.quiz_options
                )
                views.setTextViewText(
                    R.id.quiz_meta,
                    withNumberHighlight(
                        quizMetaText,
                        question.quote.number,
                        cardColors.numberColor
                    )
                )
            } else {
                views.setTextViewText(R.id.quiz_meta, quizMetaText)
            }
        }
        // Tap anywhere: open the app into the Who Said It game.
        val open = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_WHO_SAID
        }
        views.setOnClickPendingIntent(
            R.id.quiz_root,
            PendingIntent.getActivity(
                context,
                appWidgetId,
                open,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        return views
    }
}
