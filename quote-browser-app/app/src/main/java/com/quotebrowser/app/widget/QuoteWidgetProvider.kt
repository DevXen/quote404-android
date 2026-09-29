package com.quotebrowser.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.Html
import android.widget.RemoteViews
import com.quotebrowser.app.MainActivity
import com.quotebrowser.app.R
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.MaturityLevel
import com.quotebrowser.app.ui.NickPalette
import com.quotebrowser.app.ui.nickQuoteHtml
import com.quotebrowser.app.ui.QuoteCleanOptions
import com.quotebrowser.app.ui.categoryStoreFor
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.theme.resolveNickColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * One widget refresh result: which database the quote came from, its label,
 * the quote itself (null when nothing could be drawn), the active category
 * filter, and whether every draw was rejected by the content filter.
 */
private data class WidgetPick(
    val dbId: String,
    val label: String,
    val quote: com.quotebrowser.app.data.Quote?,
    val category: String?,
    val allHidden: Boolean
)

/**
 * Home-screen widget showing a random quote from the currently selected
 * database. Tapping the widget loads a new random quote; tapping the quote
 * text opens that quote in the app.
 */
open class QuoteWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_REFRESH = "com.quotebrowser.app.widget.REFRESH"
    }

    /** Layout rendered by this widget size; subclasses override for variants. */
    protected open val layoutRes: Int = R.layout.widget_quote

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { refreshWidget(context, appWidgetManager, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val manager = DatabaseManager.getInstance(context.applicationContext)
        appWidgetIds.forEach { manager.clearWidgetConfig(it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                ?: mgr.getAppWidgetIds(
                    ComponentName(context, javaClass)
                )
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

    /**
     * One random quote for the widget. Honors the per-widget database +
     * category configuration when this widget instance has one, otherwise
     * falls back to the global random sources / widget category settings.
     * When "hide read quotes" is on, retries a few times to avoid quotes
     * the user has already opened.
     *
     * Returns (quote, category label, allHiddenByFilter): when every draw
     * was rejected by the content filter, quote is null and allHiddenByFilter
     * is true so the widget renders a placeholder instead of leaking one.
     */
    private suspend fun pickWidgetQuote(
        manager: DatabaseManager,
        dbId: String,
        appWidgetId: Int
    ): Triple<com.quotebrowser.app.data.Quote?, String?, Boolean> {
        val cfg = manager.widgetConfig(appWidgetId)
        // The caller draws dbId from the widget's configured database set;
        // the category (by name) is matched in whichever database was drawn.
        val filterName = cfg?.category ?: manager.widgetCategory.first()
        val repo = manager.repoFor(dbId)
        val categoryIds = filterName?.let {
            try {
                manager.categoryStoreFor(dbId).findByName(it)?.id
                    ?.let { id -> setOf(id) }
            } catch (e: Exception) {
                null
            }
        } ?: emptySet()
        suspend fun draw(): com.quotebrowser.app.data.Quote? =
            if (categoryIds.isEmpty()) repo.randomQuote()
            else repo.randomQuotes(10, categoryIds).randomOrNull()
        val filtering = manager.hideSeen.value || manager.contentFilterEnabled.value ||
            manager.hiddenQuoteKeys.value.isNotEmpty() ||
            manager.maturityCeiling.value != MaturityLevel.EVERYTHING
        if (!filtering) return Triple(draw(), filterName, false)
        val seen =
            if (manager.hideSeen.value) manager.seenNumbers(dbId) else emptySet()
        var filteredOut = false
        repeat(10) {
            val q = draw() ?: return Triple(null, filterName, false)
            // v1.65: the global maturity ceiling applies to widget quotes.
            val filterOk =
                !manager.isQuoteHiddenFromLists(dbId, q.number, q.text) &&
                    manager.isMaturityAllowed(q)
            if (!filterOk) filteredOut = true
            if (q.number !in seen && filterOk) {
                return Triple(q, filterName, false)
            }
        }
        // Every draw was already seen or hidden by the content filter: never
        // fall back to an unchecked random draw, which could leak a hidden
        // quote onto the home screen.
        return Triple(null, filterName, filteredOut)
    }

    private suspend fun buildViews(context: Context, appWidgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, layoutRes)
        val manager = DatabaseManager.getInstance(context.applicationContext)
        val cfg = manager.widgetConfig(appWidgetId)

        suspend fun drawOnce(): WidgetPick? {
            return try {
                val validDbIds = manager.allDbIds()
                // Per-widget database choice: null = all databases.
                // No config yet = follow the global widget settings.
                val pool = if (cfg == null) {
                    manager.randomSourceIds.first()?.toList()
                        ?: listOf(manager.defaultDbId())
                } else {
                    val wanted = cfg.dbIds
                    (if (wanted == null) validDbIds
                    else validDbIds.filter { it in wanted })
                        .ifEmpty { validDbIds }
                }
                if (pool.isEmpty()) null
                else {
                    val id = pool.random()
                    val (quote, category, allHidden) = pickWidgetQuote(manager, id, appWidgetId)
                    WidgetPick(id, manager.shortLabel(id), quote, category, allHidden)
                }
            } catch (e: Exception) {
                null
            }
        }

        // Draw until the quote differs from the one currently shown, so a
        // tap always feels like it loads something new.
        var sourced = drawOnce()
        var attempts = 0
        while (attempts < 3) {
            val s = sourced ?: break
            val q = s.quote ?: break
            if ("${s.dbId}:${q.number}" != manager.widgetLastQuote(appWidgetId)) break
            sourced = drawOnce()
            attempts++
        }
        sourced?.quote?.let { manager.setWidgetLastQuote(appWidgetId, sourced.dbId, it.number) }

        if (sourced != null) {
            val (dbId, label, quote, widgetCategory, allHidden) = sourced
            if (quote == null && allHidden) {
                views.setTextViewText(
                    R.id.widget_text,
                    "All quotes hidden by content filter"
                )
                views.setTextViewText(
                    R.id.widget_meta,
                    label + (widgetCategory?.let { "  ·  $it" } ?: "")
                )
            } else if (quote == null) {
                views.setTextViewText(R.id.widget_text, "No quotes available")
                views.setTextViewText(R.id.widget_meta, "")
            } else {
                // Colorize nicks and split IRC " \" line separators, using the
                // same palette and settings as the in-app quote rendering.
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
                    R.id.widget_text,
                    Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
                )
                // v1.59: render the quote as its database's site-theme
                // card; fall back to the plain styling when the database
                // has no site theme or resolution fails.
                val cardColors = try {
                    resolveWidgetCardColors(manager.widgetThemeSnapshots(), dbId)
                } catch (e: Exception) {
                    null
                }
                val metaText =
                    "#${quote.number}  ·  rating ${quote.rating?.toString() ?: "—"}  ·  $label" +
                        (widgetCategory?.let { "  ·  $it" } ?: "")
                if (cardColors != null) {
                    views.applyWidgetCardColors(
                        R.id.widget_root,
                        R.id.widget_text,
                        R.id.widget_meta,
                        cardColors
                    )
                    views.setTextViewText(
                        R.id.widget_meta,
                        withNumberHighlight(metaText, quote.number, cardColors.numberColor)
                    )
                } else {
                    views.setTextViewText(R.id.widget_meta, metaText)
                }
                val open = Intent(context, MainActivity::class.java).apply {
                    action = MainActivity.ACTION_OPEN_QUOTE
                    putExtra(MainActivity.EXTRA_QUOTE_NUMBER, quote.number)
                    putExtra(MainActivity.EXTRA_DB_ID, dbId)
                }
                views.setOnClickPendingIntent(
                    R.id.widget_text,
                    PendingIntent.getActivity(
                        context,
                        quote.number,
                        open,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
        } else {
            views.setTextViewText(R.id.widget_text, "No quotes available")
            views.setTextViewText(R.id.widget_meta, "")
        }

        // Tap anywhere else on the widget for a new random quote.
        val refresh = Intent(context, javaClass).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
        }
        views.setOnClickPendingIntent(
            R.id.widget_root,
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
