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
import com.quotebrowser.app.R
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.worker.StreakWidgetWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Home-screen widget showing the current reading streak (consecutive days
 * with at least one quote read) plus the longest streak ever. Reuses the
 * same streak computation as the Stats screen
 * ([DatabaseManager.readingStreakDays] / [DatabaseManager.longestReadingStreakDays]).
 *
 * updatePeriodMillis is 0; refreshes on APPWIDGET_UPDATE, on tap, and once
 * a day via [StreakWidgetWorker] so the day counter rolls over at midnight.
 */
class StreakWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.quotebrowser.app.widget.STREAK_REFRESH"
        private const val STREAK_WORK = "streak_widget_refresh"
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
            val request = PeriodicWorkRequestBuilder<StreakWidgetWorker>(1, TimeUnit.DAYS)
                .addTag(STREAK_WORK)
                .build()
            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    STREAK_WORK,
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

    private suspend fun buildViews(context: Context, appWidgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_streak)
        val manager = DatabaseManager.getInstance(context.applicationContext)
        val current = try {
            manager.readingStreakDays()
        } catch (e: Exception) {
            0
        }
        val longest = try {
            manager.longestReadingStreakDays()
        } catch (e: Exception) {
            0
        }
        views.setTextViewText(R.id.streak_days, current.toString())
        views.setTextViewText(
            R.id.streak_unit,
            if (current == 1) "day" else "days"
        )
        views.setTextViewText(
            R.id.streak_best,
            "Best: $longest " + if (longest == 1) "day" else "days"
        )
        // Tap anywhere: refresh the widget.
        val refresh = Intent(context, javaClass).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        views.setOnClickPendingIntent(
            R.id.streak_root,
            PendingIntent.getBroadcast(
                context,
                appWidgetId,
                refresh,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        return views
    }
}
