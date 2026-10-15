package com.quotebrowser.app.widget

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.RemoteViews
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DbThemeSnapshots
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.data.quoteBackgroundForDb
import com.quotebrowser.app.data.resolveDbTheme
import com.quotebrowser.app.data.siteStyleFor
import com.quotebrowser.app.ui.components.siteBarColor
import com.quotebrowser.app.ui.components.siteOnBarColor
import com.quotebrowser.app.ui.theme.contentOn
import com.quotebrowser.app.ui.theme.effectivePreset
import kotlinx.coroutines.flow.first
import kotlin.math.pow

/**
 * v1.59 card-styled widgets: the quote-displaying home-screen widgets
 * (random quote, quote sizes, daily quiz) render the quote as its
 * database's site-theme card — the same site colors the in-app site
 * cards use ("like the real site").
 *
 * RemoteViews cannot use Compose and cannot draw rounded corners or
 * borders, so the site card is approximated with solid color bands:
 *
 * - widget root: the site's header-bar color ([siteBarColor] — the site's
 *   signature accent), acting as the header/foot strip band.
 * - quote body: the effective per-database "Quote background"
 *   ([quoteBackgroundForDb], which honors the explicit per-database
 *   override) with a contrasting body text color — the same rule the
 *   in-app site cards use ([contentOn]).
 * - meta line: the bar's content color ([siteOnBarColor]), with the
 *   #number picked out in the site link color (the palette theme's
 *   number slot), falling back to the on-bar color when the link color
 *   would not read on the bar (e.g. furcqdb's orange on orange).
 *
 * When the database's theming resolves bypassed (master override,
 * per-database themes off, unknown database, Modern layout) or
 * resolution throws, null is returned and the caller keeps the
 * existing plain widget styling.
 */
data class WidgetCardColors(
    val rootBackground: Int,
    val bodyBackground: Int,
    val bodyText: Int,
    val metaText: Int,
    val numberColor: Int
)

/** Builds the theme snapshot the pure per-database resolution needs. */
suspend fun DatabaseManager.widgetThemeSnapshots(): DbThemeSnapshots =
    DbThemeSnapshots(
        globalThemeId = themeId.first(),
        perDbThemesEnabled = perDbThemesEnabled.first(),
        masterOverride = masterThemeOverride.first(),
        configs = dbThemeConfigs.first(),
        customThemes = customThemes.first(),
        builtInOverrides = builtInThemeOverrides.first(),
        classicLayoutEnabled = classicLayoutOption.first(),
        // v1.89.5: themes carried inside database files.
        builtInDbThemes = dbBuiltInThemes.first(),
        builtInDbCards = dbBuiltInCards.first(),
        // v1.89.6: rating formats / vote styles carried alongside.
        builtInDbRatingFormats = dbBuiltInRatingFormats.first(),
        builtInDbVoteStyles = dbBuiltInVoteStyles.first()
    )

/** The site-card colors for one database's widget card, or null for plain styling. */
fun resolveWidgetCardColors(
    snap: DbThemeSnapshots,
    dbId: String?
): WidgetCardColors? {
    return try {
        val resolved = resolveDbTheme(snap, dbId)
        if (resolved.bypassed) return null
        val style = siteStyleFor(resolved.structureThemeId)
        if (style == SiteStyle.MODERN_CARD) return null
        val bar = siteBarColor(style)
        if (bar.alpha == 0f) return null
        val bodyBg = quoteBackgroundForDb(resolved, dbId, snap)
        val onBar = siteOnBarColor(style)
        val link: Color = effectivePreset(resolved.paletteThemeId, snap.builtInOverrides)
            ?.let { Color(it.number) } ?: onBar
        val number = if (readableOn(link, bar)) link else onBar
        WidgetCardColors(
            rootBackground = bar.toArgb(),
            bodyBackground = bodyBg.toArgb(),
            bodyText = contentOn(bodyBg).toArgb(),
            metaText = onBar.toArgb(),
            numberColor = number.toArgb()
        )
    } catch (e: Exception) {
        null
    }
}

/** Applies [colors] to the standard widget card views; extra ids get the on-bar text color. */
fun RemoteViews.applyWidgetCardColors(
    rootId: Int,
    textId: Int,
    metaId: Int,
    colors: WidgetCardColors,
    vararg extraTextIds: Int
) {
    setInt(rootId, "setBackgroundColor", colors.rootBackground)
    setInt(textId, "setBackgroundColor", colors.bodyBackground)
    setTextColor(textId, colors.bodyText)
    setTextColor(metaId, colors.metaText)
    extraTextIds.forEach { setTextColor(it, colors.metaText) }
}

/**
 * Highlights the "#number" token in a meta line: bold, in the site link
 * color. Spans survive RemoteViews marshalling (the widget already sends
 * Html spans the same way).
 */
fun withNumberHighlight(meta: String, number: Int, numberColor: Int): CharSequence {
    val s = SpannableString(meta)
    val token = "#$number"
    var i = s.indexOf(token)
    while (i >= 0) {
        s.setSpan(
            ForegroundColorSpan(numberColor),
            i, i + token.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        s.setSpan(
            StyleSpan(Typeface.BOLD),
            i, i + token.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        i = s.indexOf(token, i + token.length)
    }
    return s
}

/** WCAG-style contrast check; ~3:1 is enough for large/bold text. */
private fun readableOn(fg: Color, bg: Color): Boolean {
    fun lum(c: Color): Double {
        fun t(v: Double) =
            if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        return 0.2126 * t(c.red.toDouble()) +
            0.7152 * t(c.green.toDouble()) +
            0.0722 * t(c.blue.toDouble())
    }
    val l1 = lum(fg)
    val l2 = lum(bg)
    val ratio = (maxOf(l1, l2) + 0.05) / (minOf(l1, l2) + 0.05)
    return ratio >= 3.0
}
