package com.quotebrowser.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.Html
import android.widget.RemoteViews
import com.quotebrowser.app.R
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.NickPalette
import com.quotebrowser.app.ui.QuoteCleanOptions
import com.quotebrowser.app.ui.nickQuoteHtml
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.theme.resolveNickColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Home-screen widget showing a single random quote (text + number) drawn
 * from the global random sources. Every draw is filtered through
 * [DatabaseManager.isQuoteHiddenFromLists], so content filter, "Never show
 * again", hidden quotes, and muted nicks are honored — never falling back
 * to an unchecked draw that could leak a hidden quote onto the home screen.
 *
 * Tapping the widget sends a broadcast back to this provider, which draws
 * a new quote and updates the widget instance that was tapped.
 */
class RandomQuoteWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.quotebrowser.app.widget.RANDOM_QUOTE_REFRESH"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
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

    private data class SourcedPick(
        val dbId: String,
        val label: String,
        val quote: Quote?
    )

    /**
     * One random quote from the configured random sources, filtered the
     * same way as drawPair() in QuoteBattleScreen: a batch is drawn and
     * every quote rejected by the content filter / muted nicks / hidden
     * list is discarded before picking. Returns null when no source has
     * anything drawable.
     */
    private suspend fun pickQuote(
        manager: DatabaseManager,
        appWidgetId: Int
    ): SourcedPick? {
        return try {
            val all = manager.allDbIds()
            val selected = manager.randomSourceIds.value
            val ids = (if (selected == null) all else all.filter { it in selected })
                .ifEmpty { all }
            if (ids.isEmpty()) return null
            val lastShown = manager.widgetLastQuote(appWidgetId)
            repeat(10) {
                val dbId = ids.random()
                val pick = try {
                    manager.repoFor(dbId).randomQuotes(12)
                        .filter { q ->
                            !manager.isQuoteHiddenFromLists(dbId, q.number, q.text)
                        }
                        .shuffled()
                } catch (e: Exception) {
                    emptyList()
                }
                // Skip the quote currently on the widget so a tap refresh
                // never appears to do nothing.
                val quote = pick.firstOrNull { q ->
                    "$dbId:${q.number}" != lastShown
                } ?: pick.firstOrNull()
                if (quote != null) {
                    manager.setWidgetLastQuote(appWidgetId, dbId, quote.number)
                    return SourcedPick(dbId, manager.shortLabel(dbId), quote)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun buildViews(context: Context, appWidgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_random_quote)
        val manager = DatabaseManager.getInstance(context.applicationContext)
        val sourced = pickQuote(manager, appWidgetId)
        if (sourced?.quote == null) {
            views.setTextViewText(R.id.random_text, "No quotes available")
            views.setTextViewText(R.id.random_meta, "")
        } else {
            val quote = sourced.quote
            // Colorize nicks with the same palette and settings as the
            // in-app quote rendering.
            val resolved = resolveNickColors(
                manager.themeId.value,
                manager.customThemes.value
            )
            val palette = NickPalette(
                enabled = manager.nickColoringEnabled.value,
                brackets = resolved.brackets,
                names = resolved.names,
                bracketPalette = resolved.bracketPalette,
                action = resolved.action,
                actionOverridesName = resolved.actionOverridesName,
                colorMessageWithSpeaker = resolved.colorMessageWithSpeaker,
                rules = manager.nickRules.value,
                ircLineBreaks = manager.ircLineBreaks.value,
                dialogueLineBreaks = manager.dialogueLineBreaks.value
            )
            val html = nickQuoteHtml(
                quote.text,
                palette,
                QuoteCleanOptions(
                    colorNames = manager.qcColorNames.value,
                    colorActions = manager.qcColorActions.value,
                    normalizeBreaks = manager.qcNormalizeBreaks.value,
                    showTimestamps = manager.qcShowTimestamps.value,
                    colorMessage = manager.qcColorMessage.value,
                    nameColorHex = manager.qcNameColor.value,
                    messageColorHex = manager.qcMessageColor.value,
                    actionColorHex = manager.qcActionColor.value,
                    timestampColorHex = manager.qcTimestampColor.value,
                    colorOther = manager.qcColorOther.value,
                    otherColorHex = manager.qcOtherColor.value
                )
            )
            views.setTextViewText(
                R.id.random_text,
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            )
            // v1.59: render the quote as its database's site-theme card;
            // fall back to the plain styling when resolution fails.
            val cardColors = try {
                resolveWidgetCardColors(manager.widgetThemeSnapshots(), sourced.dbId)
            } catch (e: Exception) {
                null
            }
            val metaText = "#${quote.number}  ·  ${sourced.label}"
            if (cardColors != null) {
                views.applyWidgetCardColors(
                    R.id.random_root,
                    R.id.random_text,
                    R.id.random_meta,
                    cardColors
                )
                views.setTextViewText(
                    R.id.random_meta,
                    withNumberHighlight(metaText, quote.number, cardColors.numberColor)
                )
            } else {
                views.setTextViewText(R.id.random_meta, metaText)
            }
        }
        // Tap anywhere: draw a new random quote and update this widget.
        val refresh = Intent(context, javaClass).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        views.setOnClickPendingIntent(
            R.id.random_root,
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
