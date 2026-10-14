package com.quotebrowser.app.ui

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.ui.components.LocalNoveltyStripNicks
import com.quotebrowser.app.ui.components.LocalTranslatedText
import com.quotebrowser.app.ui.components.NoveltyThemeCard
import com.quotebrowser.app.ui.theme.LocalTextScale
import com.quotebrowser.app.ui.theme.QuoteBrowserTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * v1.88.24: share/export rendering for the ten novelty quote-card themes
 * (DOS, macOS, Macintosh, iMessage, Google Messages, BSOD, Linux, mIRC,
 * Terminal, Green Monochrome).
 *
 * The export renders the SAME [NoveltyThemeCard] composables the app
 * shows on screen — composed into an off-screen [ComposeView] attached
 * invisibly to the host Activity's window, then drawn into a bitmap.
 * Single source of truth: screen and export can never drift, and the
 * full text pipeline (translations, kid-safe masking, quote cleaning,
 * nick colors, "Break between quotes" spacing) flows through untouched
 * because it IS the on-screen card.
 *
 * The finished export keeps the standing export contract: the slim
 * Quote404 logo plate across the top (same geometry as the site-card
 * exports), an optional quiet URL line, and nothing else — the card
 * itself is pixel-identical to the on-screen presentation.
 */

/** The fourteen novelty styles, in picker order. */
val NOVELTY_THEME_STYLES = listOf(
    SiteStyle.DOS,
    SiteStyle.MACOS,
    SiteStyle.MACINTOSH,
    SiteStyle.IMESSAGE,
    SiteStyle.GMESSAGES,
    SiteStyle.BSOD,
    SiteStyle.LINUX,
    SiteStyle.MIRC,
    SiteStyle.TERMINAL,
    SiteStyle.GREEN_MONO,
    // v1.88.25 textmode additions.
    SiteStyle.ANSI,
    SiteStyle.ASCII,
    SiteStyle.MATRIX,
    SiteStyle.GREEN_TERM,
    // v1.88.27 chat additions.
    SiteStyle.MESSENGER_LIGHT,
    SiteStyle.MESSENGER_DARK,
    SiteStyle.AIM,
    SiteStyle.ICQ,
    SiteStyle.MSN_MESSENGER,
    // v1.88.38: Clint's personal Friends database theme.
    SiteStyle.FRIENDS
)

/** Tile-preview render width — square tiles, crisp at tile scale, cheap to compose. */
const val NOVELTY_TILE_PREVIEW_W = 420

/** True for the twenty v1.88.23/v1.88.25/v1.88.27/v1.88.28/v1.88.38 novelty styles (bespoke Compose cards). */
fun SiteStyle.isNoveltyTheme(): Boolean = this in NOVELTY_THEME_STYLES

/**
 * Representative (background, foreground, glyph) for a novelty theme's
 * picker tile.
 */
fun noveltyTileSwatch(style: SiteStyle): Triple<Long, Long, String> = when (style) {
    SiteStyle.DOS -> Triple(0xFF000000, 0xFFFFFFFF, "C:\\>")
    SiteStyle.MACOS -> Triple(0xFFF5F5F7, 0xFF1D1D1F, "Ag")
    SiteStyle.MACINTOSH -> Triple(0xFFFFFFFF, 0xFF000000, "Ag")
    SiteStyle.IMESSAGE -> Triple(0xFF0A84FF, 0xFFFFFFFF, "Ag")
    SiteStyle.GMESSAGES -> Triple(0xFFD9E2FF, 0xFF041E49, "Ag")
    SiteStyle.BSOD -> Triple(0xFF0000AA, 0xFFFFFFFF, ":(")
    SiteStyle.LINUX -> Triple(0xFF000000, 0xFF33FF66, "\$_")
    SiteStyle.MIRC -> Triple(0xFFFFFFFF, 0xFF000080, "#")
    SiteStyle.TERMINAL -> Triple(0xFF0D1117, 0xFFE6EDF3, ">_")
    SiteStyle.GREEN_MONO -> Triple(0xFF000000, 0xFF33FF33, "Ag")
    SiteStyle.ANSI -> Triple(0xFF000000, 0xFF55FFFF, "▓▒")
    SiteStyle.ASCII -> Triple(0xFF0C0C0C, 0xFFCCCCCC, "+-+")
    SiteStyle.MATRIX -> Triple(0xFF000000, 0xFF00FF41, "Mx")
    SiteStyle.GREEN_TERM -> Triple(0xFF000000, 0xFF33FF33, ">_")
    SiteStyle.MESSENGER_LIGHT -> Triple(0xFF0084FF, 0xFFFFFFFF, "Ag")
    SiteStyle.MESSENGER_DARK -> Triple(0xFF1C1C1E, 0xFF0084FF, "Ag")
    SiteStyle.AIM -> Triple(0xFF1E4E79, 0xFFE60000, "Ag")
    SiteStyle.ICQ -> Triple(0xFFCDE8F7, 0xFF0066CC, "Ag")
    SiteStyle.MSN_MESSENGER -> Triple(0xFF0B7DDA, 0xFFFFFFFF, "Ag")
    // v1.88.38: Friends — white wall card, classic Facebook blue glyph.
    // (The Sunset Hangout palette stays on the FriendsCard itself when
    // that preset is the resolved structure theme.)
    SiteStyle.FRIENDS -> Triple(0xFF3B5998, 0xFFFFFFFF, "✦")
    else -> Triple(0xFF222222, 0xFFFFFFFF, "Ag")
}

/**
 * The composition-local inputs the on-screen card reads, snapshotted
 * from the ambient composition so the export matches the screen: text
 * scale, quote font, kid-safe, display transforms, the quote's
 * translation, quote-clean options, and the nick palette.
 */
data class NoveltyExportLocals(
    val textScale: Float = 1f,
    val fontFamily: FontFamily? = null,
    val kidSafe: Boolean = false,
    val transforms: DisplayTransforms = DisplayTransforms(),
    val translatedText: String? = null,
    val quoteClean: QuoteCleanOptions = QuoteCleanOptions(),
    val nickPalette: NickPalette? = null
)

/** Snapshots the ambient composition locals for [renderNoveltyQuoteCard]. */
@Composable
fun rememberNoveltyExportLocals(): NoveltyExportLocals = NoveltyExportLocals(
    textScale = LocalTextScale.current,
    fontFamily = LocalQuoteFontFamily.current,
    kidSafe = LocalKidSafeEnabled.current,
    transforms = LocalDisplayTransforms.current,
    translatedText = LocalTranslatedText.current,
    quoteClean = LocalQuoteClean.current,
    nickPalette = LocalNickPalette.current
)

fun Context.findActivity(): ComponentActivity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is ComponentActivity) return c
        c = c.baseContext
    }
    return null
}

/** Export bitmap width, matching the other card exporters (1080px). */
private const val NOVELTY_EXPORT_W = 1080

/**
 * Renders a novelty-theme quote card exactly as it appears on screen:
 * the real [NoveltyThemeCard] composable is composed off-screen into an
 * invisible [ComposeView] on the host Activity's window and drawn to a
 * bitmap, then the slim Quote404 logo plate is added on top (standing
 * watermark requirement) with an optional quiet URL line at the bottom.
 *
 * Must be called from a coroutine scope; the view work runs on the Main
 * dispatcher internally. Throws when there is no host Activity or the
 * card doesn't compose within the timeout — callers surface that as a
 * render failure like the other exporters.
 *
 * v1.88.31: [includeHeader] — false skips the slim Quote404 logo plate
 * so the image is just the card (the share dialog's "Quote404 header"
 * toggle).
 */
suspend fun renderNoveltyQuoteCard(
    context: Context,
    quote: Quote,
    siteStyle: SiteStyle,
    includeNumber: Boolean = true,
    includeRating: Boolean = true,
    includeUrl: Boolean = false,
    colorizeNicks: Boolean = false,
    locals: NoveltyExportLocals = NoveltyExportLocals(),
    dbId: String? = null,
    includeHeader: Boolean = true,
    /**
     * v1.88.32: "remove nicks from message text" for the AIM/ICQ/MSN chat
     * themes — strips the redundant speaker prefix from each turn's body.
     */
    stripNicks: Boolean = false,
    /**
     * v1.89.2: the structure theme id the card resolved from on screen,
     * threaded so dual-palette cards (Friends sunset vs wall) render the
     * same palette in the export as on screen.
     */
    structureThemeId: String? = null
): Bitmap {
    require(siteStyle.isNoveltyTheme()) { "Not a novelty theme: $siteStyle" }
    val watermark = if (includeHeader) quote404WatermarkLogo(context) else null
    val cardBitmap = renderNoveltyCardBitmap(
        context, quote, siteStyle,
        widthPx = NOVELTY_EXPORT_W,
        includeNumber = includeNumber,
        includeRating = includeRating,
        colorizeNicks = colorizeNicks,
        locals = locals,
        dbId = dbId,
        stripNicks = stripNicks,
        structureThemeId = structureThemeId
    )
    try {
        // Same slim logo plate geometry as the site-card exports
        // (widthFrac 0.78, vPad 10, no vector tagline).
        val logoPlateH = if (watermark != null) quote404LogoPlateH(
            watermark, NOVELTY_EXPORT_W.toFloat(),
            widthFrac = 0.78f, vPad = 10f, drawTagline = false
        ) else 0f
        val urlH = if (includeUrl && quote.url.isNotBlank()) 76f else 0f
        // v1.88.32: the slim Quote404 site footer under the card (same
        // black as the top logo plate, www.Quote404.com bottom-right).
        val qFooterH = quote404FooterH()
        val totalH =
            (logoPlateH + cardBitmap.height + urlH + qFooterH).toInt().coerceAtLeast(1)
        val out = Bitmap.createBitmap(NOVELTY_EXPORT_W, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(0xFF000000.toInt())
        if (watermark != null) {
            canvas.drawQuote404LogoPlate(
                watermark, widthFrac = 0.78f, vPad = 10f,
                plateH = logoPlateH, drawTagline = false
            )
        }
        canvas.drawBitmap(cardBitmap, 0f, logoPlateH, null)
        if (urlH > 0f) {
            val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF9A9A9A.toInt()
                textSize = 32f
            }
            canvas.drawText(
                quote.url, 36f,
                logoPlateH + cardBitmap.height + 52f, urlPaint
            )
        }
        canvas.drawQuote404Footer(
            logoPlateH + cardBitmap.height + urlH,
            NOVELTY_EXPORT_W.toFloat(), qFooterH
        )
        return out
    } finally {
        cardBitmap.recycle()
    }
}

/**
 * v1.88.25: renders just the novelty-theme card (no logo plate, no URL
 * line) at [widthPx] wide — the raw [NoveltyThemeCard] off-screen
 * capture. The full export ([renderNoveltyQuoteCard]) builds on this;
 * the share/export picker tiles use it at [NOVELTY_TILE_PREVIEW_W] so
 * every tile shows the real theme instead of a generic swatch.
 *
 * Must be called from a coroutine scope; the view work runs on the Main
 * dispatcher internally. Throws when there is no host Activity or the
 * card doesn't compose within the timeout.
 */
suspend fun renderNoveltyCardBitmap(
    context: Context,
    quote: Quote,
    siteStyle: SiteStyle,
    widthPx: Int = NOVELTY_EXPORT_W,
    includeNumber: Boolean = true,
    includeRating: Boolean = true,
    colorizeNicks: Boolean = false,
    locals: NoveltyExportLocals = NoveltyExportLocals(),
    dbId: String? = null,
    /** v1.88.32: "remove nicks from message text" for the chat themes. */
    stripNicks: Boolean = false,
    /** v1.89.2: see renderNoveltyQuoteCard's structureThemeId. */
    structureThemeId: String? = null
): Bitmap {
    require(siteStyle.isNoveltyTheme()) { "Not a novelty theme: $siteStyle" }
    val activity = context.findActivity()
        ?: throw IllegalStateException("Novelty theme export needs an Activity")
    return withContext(Dispatchers.Main) {
        captureNoveltyCard(
            activity, quote, siteStyle,
            includeNumber, includeRating, colorizeNicks, locals, dbId,
            stripNicks,
            widthPx = widthPx,
            structureThemeId = structureThemeId
        )
    }
}

/**
 * Composes [NoveltyThemeCard] into an invisible [ComposeView] attached
 * to the Activity's decor view and draws it into a bitmap at
 * [widthPx] px wide. The card gets no header actions (share /
 * favorite / overflow are on-screen affordances, not export content)
 * and no list padding — full-bleed like the other card exporters.
 */
private suspend fun captureNoveltyCard(
    activity: ComponentActivity,
    quote: Quote,
    siteStyle: SiteStyle,
    includeNumber: Boolean,
    includeRating: Boolean,
    colorizeNicks: Boolean,
    locals: NoveltyExportLocals,
    dbId: String?,
    stripNicks: Boolean,
    widthPx: Int,
    // v1.89.2: see renderNoveltyQuoteCard's structureThemeId.
    structureThemeId: String? = null
): Bitmap = withContext(Dispatchers.Main) {
    val composeView = ComposeView(activity).apply {
        visibility = View.INVISIBLE
    }
    // The Activity is the lifecycle / saved-state / ViewModel owner, so
    // the off-screen composition behaves like any on-screen one
    // (collectAsState, rememberSaveable, etc. all work).
    composeView.setViewTreeLifecycleOwner(activity)
    composeView.setViewTreeSavedStateRegistryOwner(activity)
    composeView.setViewTreeViewModelStoreOwner(activity)
    val palette = locals.nickPalette?.copy(enabled = colorizeNicks)
    composeView.setContent {
        QuoteBrowserTheme {
            CompositionLocalProvider(
                LocalTextScale provides locals.textScale,
                LocalQuoteFontFamily provides locals.fontFamily,
                LocalKidSafeEnabled provides locals.kidSafe,
                LocalDisplayTransforms provides locals.transforms,
                LocalTranslatedText provides locals.translatedText,
                LocalQuoteClean provides locals.quoteClean,
                // v1.88.32: "remove nicks from message text" for the
                // AIM/ICQ/MSN chat themes (the off-screen composition has
                // no per-theme store, so it's threaded explicitly).
                LocalNoveltyStripNicks provides stripNicks
            ) {
                if (palette != null) {
                    CompositionLocalProvider(LocalNickPalette provides palette) {
                        NoveltyCardExportContent(
                            siteStyle, quote, includeNumber, includeRating, dbId,
                            structureThemeId
                        )
                    }
                } else {
                    NoveltyCardExportContent(
                        siteStyle, quote, includeNumber, includeRating, dbId,
                        structureThemeId
                    )
                }
            }
        }
    }
    val decor = activity.window.decorView as? ViewGroup
        ?: throw IllegalStateException("Novelty theme export: no decor view")
    decor.addView(
        composeView,
        FrameLayout.LayoutParams(widthPx, FrameLayout.LayoutParams.WRAP_CONTENT)
    )
    try {
        // Wait until the compose content has actually been measured —
        // the first layout pass can run before the first composition
        // lands, which would capture an empty view.
        withTimeout(15_000) {
            while (!composeView.isLaidOut || composeView.height == 0) delay(32)
        }
        // Pin the width to the requested size and re-measure/re-layout so
        // the capture uses the final geometry, then draw it.
        val wSpec = MeasureSpec.makeMeasureSpec(widthPx, MeasureSpec.EXACTLY)
        val hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        composeView.measure(wSpec, hSpec)
        composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
        val bmp = Bitmap.createBitmap(
            composeView.measuredWidth, composeView.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        composeView.draw(Canvas(bmp))
        bmp
    } finally {
        // Detaching disposes the composition (DisposeOnDetachedFromWindow).
        decor.removeView(composeView)
    }
}

@Composable
private fun NoveltyCardExportContent(
    siteStyle: SiteStyle,
    quote: Quote,
    includeNumber: Boolean,
    includeRating: Boolean,
    dbId: String?,
    // v1.89.2: see renderNoveltyQuoteCard's structureThemeId.
    structureThemeId: String? = null
) {
    NoveltyThemeCard(
        style = siteStyle,
        quote = quote,
        showNumber = includeNumber,
        showRating = includeRating,
        quoteBackground = Color.Transparent,
        // No header actions in an export — they're interactive chrome.
        headerActions = {},
        dbId = dbId,
        structureThemeId = structureThemeId
    )
}
