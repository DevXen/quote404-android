package com.quotebrowser.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.quotebrowser.app.R
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.PersonalRatings
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.quoteBackgroundForDb
import com.quotebrowser.app.data.ResolvedDbTheme
import com.quotebrowser.app.data.resolveDbTheme
import com.quotebrowser.app.data.siteStyleFor
import com.quotebrowser.app.ui.theme.LocalDevXenCustomBg
import androidx.compose.ui.graphics.asAndroidBitmap
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.data.siteStyleMonospace
import com.quotebrowser.app.ui.components.LocalBlackOutsideQuotes
import com.quotebrowser.app.ui.components.LocalDbThemeSnapshots
import com.quotebrowser.app.ui.components.LocalDbTotals
import com.quotebrowser.app.ui.components.LocalQdbCounts
import com.quotebrowser.app.ui.components.LocalServedDates
import com.quotebrowser.app.ui.components.siteHeaderTitle
import com.quotebrowser.app.ui.components.formatServedDate
import com.quotebrowser.app.ui.components.siteBarColor
import com.quotebrowser.app.ui.components.siteBodyBackgroundColor
import com.quotebrowser.app.ui.components.siteInnerBorderColor
import com.quotebrowser.app.ui.components.siteNumberLabel
import com.quotebrowser.app.ui.components.siteOnBarColor
import com.quotebrowser.app.ui.components.siteRatingLabel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/** A designed background style for shared quote images. */
/** v1.88.32: style families for the share picker's collapsible sections. */
enum class StyleCategory { DESIGNED, TXTRACT }

enum class QuoteCardStyle(
    val title: String,
    val bgTop: Long,
    val bgBottom: Long,
    val textColor: Long,
    val accent: Long,
    val category: StyleCategory = StyleCategory.DESIGNED
) {
    /**
     * v1.61.5: Clint's Quote404 artwork as the card background (dimmed
     * with a scrim so the text reads). The gradient colors are the
     * fallback when the artwork can't load. Listed first: it's the
     * default export look.
     */
    QUOTE404("Quote404", 0xFF060A12, 0xFF020306, 0xFFFFFFFF, 0xFF0090FF),
    /**
     * v1.61: Clint's DevXen artwork as the card background (dimmed with a
     * scrim so the text reads). The gradient colors are the fallback when
     * the artwork can't load.
     */
    DEVXEN("DevXen", 0xFF0A0A18, 0xFF050508, 0xFFFFFFFF, 0xFF087AFF),
    MIDNIGHT("Midnight", 0xFF23233A, 0xFF0E0E1A, 0xFFFFFFFF, 0xFF90CAF9),
    SUNSET("Sunset", 0xFFFF9A56, 0xFFC63D5E, 0xFFFFFFFF, 0xFFFFE0B2),
    OCEAN("Ocean", 0xFF0F3443, 0xFF08232E, 0xFFFFFFFF, 0xFF7FD8F7),
    FOREST("Forest", 0xFF2D6A4F, 0xFF081C15, 0xFFFFFFFF, 0xFFB7E4C7),
    PAPER("Paper", 0xFFFFFDF5, 0xFFF1E4C3, 0xFF2B2118, 0xFFB8860B),
    MONO("Mono", 0xFF2B2B2B, 0xFF000000, 0xFFF5F5F5, 0xFFBDBDBD),
    // v1.88.32: ported from the SMS Xtract app's HTML export themes
    // (Clint: more themes in the share picker). Each gets a signature
    // drawn detail in renderQuoteCard's background section.
    /** Newsprint: aged paper, serif body, double-rule accents. */
    NEWSPAPER(
        "Newspaper", 0xFFF7F3E8, 0xFFEFE8D6, 0xFF1C1A16, 0xFF9E2B25,
        StyleCategory.TXTRACT
    ),
    /** Green-on-black BBS terminal with scanlines. */
    TERMINAL(
        "Terminal", 0xFF0A0F0A, 0xFF060A06, 0xFF33FF66, 0xFF33FF66,
        StyleCategory.TXTRACT
    ),
    /** Pulp comic: yellow stock, halftone dots, heavy frame. */
    COMIC_BOOK(
        "Comic Book", 0xFFF2C230, 0xFFE0A92A, 0xFF111111, 0xFFD62828,
        StyleCategory.TXTRACT
    ),
    /** Screenplay page: white stock, Courier body, thin frame. */
    MOVIE_SCRIPT(
        "Movie Script", 0xFFFDFDF8, 0xFFF2F0E6, 0xFF111111, 0xFF555555,
        StyleCategory.TXTRACT
    ),
    /** Manila dossier: folder tab + CONFIDENTIAL stamp. */
    CASE_FILE(
        "Case File", 0xFFC8A96A, 0xFFB08D55, 0xFF2A2118, 0xFFA31621,
        StyleCategory.TXTRACT
    ),
    /** Aged telegram form: dashed border, serif body. */
    TELEGRAM(
        "Telegram", 0xFFE8D9A0, 0xFFDCC98C, 0xFF2B2416, 0xFF6B5B3E,
        StyleCategory.TXTRACT
    ),
    /** Typed letter: warm gray stock, red margin line. */
    TYPEWRITER(
        "Typewriter", 0xFFD9D2C0, 0xFFCBC3AC, 0xFF1A1A1A, 0xFF333333,
        StyleCategory.TXTRACT
    ),
    /** Black metal: near-black, blood-red accents. */
    HEAVY_METAL(
        "Heavy Metal", 0xFF0B0B0D, 0xFF050506, 0xFFE8E8E8, 0xFF8F1018,
        StyleCategory.TXTRACT
    ),
    // v1.88.32: the rest of the SMS Xtract theme ports (24 total) —
    // skipping the 14 that duplicate existing Quote404 novelty themes
    // (Bsod, Dos, Aim, Ansi, Ascii, Matrix, GreenTerminal,
    // MessengerLight/Dark, Icq, Mirc, Msn, Ios, GoogleMessages).
    /** Clean light theme, blue thread-header rule. */
    MODERN(
        "Modern", 0xFFF4F6F8, 0xFFE8ECF1, 0xFF1A1D21, 0xFF1E88E5,
        StyleCategory.TXTRACT
    ),
    /** One UI: white, blue chat bubbles. */
    SAMSUNG_MESSAGES(
        "Samsung Messages", 0xFFFFFFFF, 0xFFF0F0F4, 0xFF1A1A1C, 0xFF0381FE,
        StyleCategory.TXTRACT
    ),
    /** Classic Nokia-blue header band. */
    NOKIA(
        "Nokia", 0xFFDFE8F2, 0xFFC7D6E8, 0xFF0A0A0A, 0xFF124191,
        StyleCategory.TXTRACT
    ),
    /** BlackBerry dark with a dotted keyboard row. */
    BLACKBERRY(
        "BlackBerry", 0xFF1A1A1A, 0xFF0A0A0A, 0xFFF2F2F2, 0xFF9AA3A8,
        StyleCategory.TXTRACT
    ),
    /** 1999 monochrome CRT: greenish-gray, scanlines. */
    MONO1999(
        "Mono 1999", 0xFF8A9455, 0xFF7A844A, 0xFF141A05, 0xFF141A05,
        StyleCategory.TXTRACT
    ),
    /** Windows 95: gray window, blue title bar, close button. */
    WIN95(
        "Windows 95", 0xFFC6C6C6, 0xFFA8A8A8, 0xFF000000, 0xFF000080,
        StyleCategory.TXTRACT
    ),
    /** Pager LCD in a dark bezel. */
    PAGER(
        "Pager", 0xFF75866F, 0xFF67765F, 0xFF2A332A, 0xFF2A332A,
        StyleCategory.TXTRACT
    ),
    /** IRC client: dark, status bar along the bottom. */
    IRC(
        "IRC", 0xFF11161D, 0xFF0B0E13, 0xFFC9D4DC, 0xFF58A6FF,
        StyleCategory.TXTRACT
    ),
    /** Game Boy DMG: green screen, gray bezel. */
    GAMEBOY(
        "Game Boy", 0xFF9BBC0F, 0xFF8BAC0F, 0xFF0F380F, 0xFF0F380F,
        StyleCategory.TXTRACT
    ),
    /** Ransom note: paper pinned with masking tape. */
    RANSOM(
        "Ransom Note", 0xFFF2ECDA, 0xFFE7DCC0, 0xFF222222, 0xFFB03A2E,
        StyleCategory.TXTRACT
    ),
    /** NES: dark with the console's red/blue/yellow stripe. */
    NES(
        "NES", 0xFF161616, 0xFF0A0A0A, 0xFFE8E8E8, 0xFFE60012,
        StyleCategory.TXTRACT
    ),
    /** SNES: deep purple with controller button dots. */
    SNES(
        "SNES", 0xFF1E1E36, 0xFF141428, 0xFFE8E8F0, 0xFF8B5CF6,
        StyleCategory.TXTRACT
    ),
    /** Sega: black with the blue stripe band. */
    SEGA(
        "Sega", 0xFF101418, 0xFF0A0A0A, 0xFFF2F2F2, 0xFF1E90FF,
        StyleCategory.TXTRACT
    ),
    /** Atari 2600 woodgrain. */
    ATARI(
        "Atari", 0xFF6E4520, 0xFF3A2410, 0xFFF2E8D8, 0xFFE86A17,
        StyleCategory.TXTRACT
    ),
    /** Wild-west wanted poster: aged paper, WANTED masthead. */
    WANTED(
        "Wanted", 0xFFEADCB4, 0xFFD9C9A3, 0xFF3A2C1A, 0xFF6B4F2A,
        StyleCategory.TXTRACT
    ),
    /** Chalkboard: dark board, wooden frame, chalk dust. */
    CHALKBOARD(
        "Chalkboard", 0xFF32383A, 0xFF222627, 0xFFF4F1E8, 0xFFFFE9A8,
        StyleCategory.TXTRACT
    )
}

private const val CARD_W = 1080
private const val CARD_H = 1350
private const val CARD_MARGIN = 90

/**
 * v1.61.7: display name of the database behind a site style ("bash.org"),
 * used for source attribution on shared cards.
 */
fun siteDisplayName(siteStyle: SiteStyle): String = when (siteStyle) {
    SiteStyle.CLASSIC_QDB -> "qdb.us"
    SiteStyle.QDB_MIT -> "QDB.MIT.edu"
    SiteStyle.QUOTEIRC -> "QuoteIRC.com"
    SiteStyle.BURNT_ELECTRONS -> "Quotes.BurntElectrons.org"
    SiteStyle.WARPDRIVE -> "warpdrive.se"
    SiteStyle.BASHIM -> "bash.im"
    SiteStyle.WQDB -> "WormsQuoteDatabase"
    SiteStyle.XKCDB -> "xkcdb.com"
    SiteStyle.FURCQDB -> "Mandaliet.com / furcqdb"
    SiteStyle.IROX -> "I-Rox.com"
    SiteStyle.BASH -> "bash.org"
    SiteStyle.CRAPSHACK -> "#CrapXen CrapShack"
    // v1.88.23 novelty themes.
    SiteStyle.DOS -> "DOS"
    SiteStyle.MACOS -> "macOS"
    SiteStyle.MACINTOSH -> "Macintosh"
    SiteStyle.IMESSAGE -> "iMessage"
    SiteStyle.GMESSAGES -> "Google Messages"
    SiteStyle.BSOD -> "BSOD"
    SiteStyle.LINUX -> "Linux"
    SiteStyle.MIRC -> "mIRC"
    SiteStyle.TERMINAL -> "Terminal"
    SiteStyle.GREEN_MONO -> "Green Monochrome"
    SiteStyle.ANSI -> "ANSI"
    SiteStyle.ASCII -> "ASCII"
    SiteStyle.MATRIX -> "Matrix"
    SiteStyle.GREEN_TERM -> "Green Terminal"
    SiteStyle.MESSENGER_LIGHT -> "Messenger"
    SiteStyle.MESSENGER_DARK -> "Messenger Dark"
    SiteStyle.AIM -> "AIM"
    SiteStyle.ICQ -> "ICQ"
    SiteStyle.MSN_MESSENGER -> "MSN Messenger"
    // v1.88.38: Clint's personal Friends database.
    SiteStyle.FRIENDS -> "Friends"
    SiteStyle.MODERN_CARD -> "Quote404"
}

/**
 * Renders a quote as a designed share card: gradient background, wrapped
 * quote text, optional quote number / rating / URL, and a small watermark.
 */
/**
 * v1.88.32: one signature drawn detail per SMS Xtract-ported theme,
 * painted over the gradient background (behind the quote text) so each
 * theme reads as its own thing instead of a flat gradient.
 *
 * v1.88.33: the Quote404 logo now sits at the very top of the card, so
 * theme top-chrome (title bars, bands, stripes) is drawn shifted down by
 * [chromeDy] — the y just below the logo block. Full-bleed textures
 * (scanlines, woodgrain, halftone) stay full-bleed. [textRect] is the
 * quote text area (margins CARD_MARGIN, textTop..textTop+height),
 * computed before the bitmap exists — a theme may draw a surface
 * (checkerboard frame, phone screen, caption box, window client area)
 * framing it. [quoteNumber] feeds details like the case-file "CASE #".
 */
private fun drawTxtractCardDetails(
    canvas: Canvas,
    cardStyle: QuoteCardStyle,
    cardH: Float,
    chromeDy: Float,
    textRect: RectF,
    quoteNumber: Int
) {
    val w = CARD_W.toFloat()
    // v1.88.33: three layers —
    // 1. full-bleed textures (absolute coords, no translate),
    // 2. top chrome in translated space (local y 0 = chromeDy),
    // 3. text-area surfaces in absolute coords (textRect).
    // The caller draws the logo above chromeDy, the quote text inside
    // textRect, the number/rating top-right, and the footer at the
    // bottom — none of that is drawn here. Never draw past cardH
    // (the footer occupies the last 68f).

    // ---------- 1. full-bleed textures ----------
    when (cardStyle) {
        QuoteCardStyle.TERMINAL -> {
            // CRT scanlines.
            val scan = Paint().apply {
                color = 0xFF000000.toInt()
                strokeWidth = 2f
                alpha = 40
            }
            var y = 0f
            while (y < cardH) {
                canvas.drawLine(0f, y, w, y, scan)
                y += 10f
            }
        }
        QuoteCardStyle.MONO1999 -> {
            // Old monochrome CRT scanlines.
            val scan = Paint().apply {
                color = 0xFF000000.toInt()
                strokeWidth = 2f
                alpha = 28
            }
            var y = 0f
            while (y < cardH) {
                canvas.drawLine(0f, y, w, y, scan)
                y += 12f
            }
        }
        QuoteCardStyle.ATARI -> {
            // Woodgrain bands.
            val grain = Paint().apply { strokeWidth = 12f }
            val tones = listOf(
                0xFF6E4520, 0xFF7A4E24, 0xFF5A3A1A, 0xFF8A5A2B
            )
            var y = 6f
            var i = 0
            while (y < cardH) {
                grain.color = tones[i % tones.size].toInt()
                grain.alpha = 90
                canvas.drawLine(0f, y, w, y, grain)
                y += 26f
                i++
            }
        }
        QuoteCardStyle.TYPEWRITER -> {
            // Typed stationery: blue ruled lines, faint red margin line,
            // and faint ink speckles over the whole card.
            val rules = Paint().apply {
                color = 0xFF3B6BD6.toInt()
                strokeWidth = 2f
                alpha = 90
            }
            var y = 150f
            while (y < cardH) {
                canvas.drawLine(0f, y, w, y, rules)
                y += 60f
            }
            val margin = Paint().apply {
                color = 0xFFE07060.toInt()
                strokeWidth = 3f
                alpha = 70
            }
            canvas.drawLine(150f, 0f, 150f, cardH, margin)
            val dot = Paint().apply {
                color = 0xFF000000.toInt()
                alpha = 18
            }
            var i = 0
            while (i < 420) {
                val sx = (i * 113f) % w
                val sy = (i * 167f) % cardH
                canvas.drawCircle(sx, sy, 1.2f, dot)
                i++
            }
        }
        QuoteCardStyle.HEAVY_METAL -> {
            // Blood-red diagonal pinstripe mesh behind everything.
            val stripe = Paint().apply {
                color = 0xFF8F1018.toInt()
                strokeWidth = 3f
                alpha = 46
            }
            var x = -cardH
            while (x < w + cardH) {
                canvas.drawLine(x, 0f, x + cardH, cardH, stripe)
                x += 22f
            }
        }
        QuoteCardStyle.COMIC_BOOK -> {
            // Halftone dots (dark, per the theme) + heavy ink frame.
            val dot = Paint().apply {
                color = 0xFF000000.toInt()
                alpha = 36
            }
            var y = 44f
            while (y < cardH - 30f) {
                var x = 44f
                while (x < w - 30f) {
                    canvas.drawCircle(x, y, 5f, dot)
                    x += 56f
                }
                y += 56f
            }
            val frame = Paint().apply {
                color = 0xFF111111.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 14f
            }
            canvas.drawRect(28f, 28f, w - 28f, cardH - 28f, frame)
        }
        QuoteCardStyle.MOVIE_SCRIPT -> {
            // Thin screenplay-page frame.
            val frame = Paint().apply {
                color = 0xFF555555.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
                alpha = 150
            }
            canvas.drawRect(30f, 30f, w - 30f, cardH - 30f, frame)
        }
        QuoteCardStyle.CASE_FILE -> {
            // Red EXHIBIT stamp, rotated 8 degrees, top-right — the theme's
            // signature. (Layer 1: positioned relative to chromeDy so it
            // clears the logo block.)
            val cfx = w - 230f
            val cfy = chromeDy + 70f
            canvas.save()
            canvas.rotate(8f, cfx, cfy)
            val cflabel = "EXHIBIT #$quoteNumber"
            val stamp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFA01C1C.toInt()
                textSize = 50f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                alpha = 217
            }
            val cfw = stamp.measureText(cflabel)
            val edge = Paint().apply {
                color = 0xFFA01C1C.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 5f
                alpha = 217
            }
            canvas.drawRect(cfx - cfw / 2f - 24f, cfy - 50f, cfx + cfw / 2f + 24f, cfy + 20f, edge)
            canvas.drawText(cflabel, cfx, cfy, stamp)
            canvas.restore()
        }
        QuoteCardStyle.TELEGRAM -> {
            // Dashed telegram-form border framing the card below the logo.
            val border = Paint().apply {
                color = 0xFF2B2416.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 5f
                alpha = 200
                pathEffect = android.graphics.DashPathEffect(
                    floatArrayOf(28f, 18f), 0f
                )
            }
            canvas.drawRect(30f, chromeDy - 28f, w - 30f, cardH - 80f, border)
        }
        QuoteCardStyle.WANTED -> {
            // Poster frame.
            val frame = Paint().apply {
                color = 0xFF3A2C1A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 8f
                alpha = 200
            }
            canvas.drawRect(30f, 30f, w - 30f, cardH - 30f, frame)
            val inner = Paint().apply {
                color = 0xFF3A2C1A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
                alpha = 160
            }
            canvas.drawRect(48f, 48f, w - 48f, cardH - 48f, inner)
        }
        QuoteCardStyle.IRC -> {
            // Client status bar along the bottom (above the footer).
            val bar = Paint().apply { color = 0xFF161D26.toInt() }
            canvas.drawRect(0f, cardH - 64f, w, cardH, bar)
            val status = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF6E7B8A.toInt()
                textSize = 34f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            canvas.drawText(
                "*** Connected to irc.quote404.net", 36f, cardH - 20f, status
            )
        }
        QuoteCardStyle.CHALKBOARD -> {
            // Wooden frame + chalk dust smudges.
            val wood = Paint().apply {
                color = 0xFF8A6F4D.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 22f
            }
            canvas.drawRect(22f, 22f, w - 22f, cardH - 22f, wood)
            val dust = Paint().apply {
                color = 0xFFFFFFFF.toInt()
                alpha = 18
            }
            listOf(
                Triple(200f, 300f, 46f), Triple(760f, 220f, 60f),
                Triple(420f, 900f, 52f), Triple(880f, 1100f, 44f),
                Triple(150f, 1150f, 56f)
            ).forEach { (x, y, r) -> canvas.drawCircle(x, y, r, dust) }
        }
        else -> { /* No full-bleed texture for this style. */ }
    }

    // ---------- 2. top chrome (local coords; y=0 is chromeDy) ----------
    canvas.save()
    canvas.translate(0f, chromeDy)
    when (cardStyle) {
        QuoteCardStyle.NEWSPAPER -> {
            // Front-page masthead "The Quote404 Times" + VOL/edition/price
            // rule + per-story kicker/byline, after the v0.7.9 rework.
            // (Canvas has no blackletter face; serif faux-bold carries the
            // nameplate.)
            val name = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1C1A16.toInt()
                textSize = 60f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("The Quote404 Times", w / 2f, 66f, name)
            val vol = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1C1A16.toInt()
                textSize = 24f
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
                alpha = 220
            }
            canvas.drawText(
                "VOL. 1 \u2014 NO. 1 \u00B7 SMS BACKUP EDITION \u00B7 PRICE: FREE",
                w / 2f, 102f, vol
            )
            val thick = Paint().apply {
                color = 0xFF1C1A16.toInt()
                strokeWidth = 5f
                alpha = 200
            }
            val thin = Paint().apply {
                color = 0xFF1C1A16.toInt()
                strokeWidth = 2f
                alpha = 200
            }
            canvas.drawLine(48f, 118f, w - 48f, 118f, thick)
            canvas.drawLine(48f, 126f, w - 48f, 126f, thin)
            val kicker = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1C1A16.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
                alpha = 180
            }
            kicker.letterSpacing = 0.12f
            canvas.drawText("STORY \u00B7 QUOTE #$quoteNumber", w / 2f, 160f, kicker)
            val byline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1C1A16.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
                alpha = 190
            }
            byline.textSkewX = -0.2f
            canvas.drawText("By Quote404 \u00B7 Daily Edition", w / 2f, 190f, byline)
        }
        QuoteCardStyle.COMIC_BOOK -> {
            // Masthead logo badge + issue strip, after the theme's header
            // (the old POW! starburst was retired in v0.7.10).
            val cx = w / 2f
            val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 52f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val lw = logoPaint.measureText("QUOTE404")
            canvas.save()
            canvas.rotate(-2f, cx, 44f)
            val shadow = Paint().apply { color = 0xFF111111.toInt() }
            canvas.drawRect(cx - lw / 2f - 20f, 18f, cx + lw / 2f + 32f, 82f, shadow)
            val badge = Paint().apply { color = 0xFFD42A1E.toInt() }
            canvas.drawRect(cx - lw / 2f - 26f, 12f, cx + lw / 2f + 26f, 76f, badge)
            val edge = Paint().apply {
                color = 0xFF111111.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 5f
            }
            canvas.drawRect(cx - lw / 2f - 26f, 12f, cx + lw / 2f + 26f, 76f, edge)
            canvas.drawText("QUOTE404", cx, 58f, logoPaint)
            canvas.restore()
            val issue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF2C230.toInt()
                textSize = 28f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            val issueText = "ISSUE #$quoteNumber \u00B7 QUOTE ARCHIVE"
            val iw = issue.measureText(issueText)
            canvas.save()
            canvas.rotate(1f, cx, 108f)
            val strip = Paint().apply { color = 0xFF111111.toInt() }
            canvas.drawRect(cx - iw / 2f - 18f, 88f, cx + iw / 2f + 18f, 128f, strip)
            canvas.drawText(issueText, cx, 117f, issue)
            canvas.restore()
        }
        QuoteCardStyle.MOVIE_SCRIPT -> {
            // Title page: underlined centered title, "written in text
            // messages", "Draft" line — after the theme's masthead.
            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF111111.toInt()
                textSize = 44f
                typeface = android.graphics.Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
            }
            title.isUnderlineText = true
            canvas.drawText("QUOTE404", w / 2f, 60f, title)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF111111.toInt()
                textSize = 28f
                typeface = android.graphics.Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("written in text messages", w / 2f, 112f, sub)
            canvas.drawText("Draft \u2014 Quote #$quoteNumber", w / 2f, 152f, sub)
        }
        QuoteCardStyle.CASE_FILE -> {
            // Manila folder tab + typed header + subject line, after the theme.
            val tab = Paint().apply { color = 0xFFA9884E.toInt() }
            val tabEdge = Paint().apply {
                color = 0xFF2A2118.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(RectF(60f, 6f, 460f, 92f), 14f, 14f, tab)
            canvas.drawRoundRect(RectF(60f, 6f, 460f, 92f), 14f, 14f, tabEdge)
            val tabTx = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2A2118.toInt()
                textSize = 34f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
            }
            canvas.drawText("CASE NO. Q-$quoteNumber", 90f, 60f, tabTx)
            val head = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2A2118.toInt()
                textSize = 44f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
            }
            head.letterSpacing = 0.08f
            canvas.drawText("SMS EVIDENCE FILE", 90f, 140f, head)
            val subj = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2A2118.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            canvas.drawText("SUBJECT: QUOTE404 \u00B7 1 EXHIBIT LOGGED", 90f, 174f, subj)
        }
        QuoteCardStyle.TELEGRAM -> {
            // Western Message Service brand + TELEGRAM masthead + received
            // line, after the theme's header.
            val brand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2B2416.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
            }
            brand.letterSpacing = 0.3f
            canvas.drawText("\u2726 WESTERN MESSAGE SERVICE \u2726", w / 2f, 34f, brand)
            val head = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2B2416.toInt()
                textSize = 54f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
            }
            head.letterSpacing = 0.22f
            canvas.drawText("TELEGRAM", w / 2f, 96f, head)
            val meta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2B2416.toInt()
                textSize = 24f
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
                alpha = 220
            }
            meta.letterSpacing = 0.06f
            canvas.drawText("QUOTE #$quoteNumber \u00B7 DAILY EDITION STOP", w / 2f, 132f, meta)
            val thick = Paint().apply {
                color = 0xFF2B2416.toInt()
                strokeWidth = 4f
                alpha = 200
            }
            val thin = Paint().apply {
                color = 0xFF2B2416.toInt()
                strokeWidth = 1.5f
                alpha = 200
            }
            canvas.drawLine(90f, 150f, w - 90f, 150f, thick)
            canvas.drawLine(90f, 158f, w - 90f, 158f, thin)
        }
        QuoteCardStyle.TYPEWRITER -> {
            // Typed correspondence masthead: mono title over "SENT VIA
            // POSTAL SERVICE" and a stamped "ARCHIVED" mark.
            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1A.toInt()
                textSize = 48f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SMS XTRACT", w / 2f, 62f, title)
            val via = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1A.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.3f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SENT VIA POSTAL SERVICE", w / 2f, 100f, via)
            val stamp = Paint().apply {
                color = 0xFF8F1018.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
                alpha = 200
            }
            canvas.save()
            canvas.translate(w - 130f, 60f)
            canvas.rotate(12f)
            canvas.drawRect(-78f, -26f, 78f, 26f, stamp)
            val stampTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF8F1018.toInt()
                textSize = 32f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.14f
                textAlign = Paint.Align.CENTER
                alpha = 200
            }
            canvas.drawText("ARCHIVED", 0f, 12f, stampTxt)
            canvas.restore()
        }
        QuoteCardStyle.HEAVY_METAL -> {
            // Tour-poster masthead: jagged metal "SMS XTRACT", the tour
            // sub-line, lightning bolts at the edges.
            val chrome = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, 110f,
                    0xFFF2F2F2.toInt(), 0xFF6B6B6B.toInt(),
                    Shader.TileMode.CLAMP
                )
            }
            val metal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = chrome.shader
                textSize = 88f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                letterSpacing = 0.1f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SMS XTRACT", w / 2f, 78f, metal)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFF2A2A.toInt()
                textSize = 30f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.3f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("* WORLD TOUR *", w / 2f, 116f, sub)
            val bolt = Paint().apply { color = 0xFFFFD400.toInt() }
            fun boltPath(bx: Float, flip: Boolean): android.graphics.Path {
                val p = android.graphics.Path()
                val s = if (flip) -1f else 1f
                p.moveTo(bx, 20f)
                p.lineTo(bx + 26f * s, 20f)
                p.lineTo(bx + 8f * s, 70f)
                p.lineTo(bx + 26f * s, 70f)
                p.lineTo(bx, 130f)
                p.lineTo(bx + 4f * s, 78f)
                p.lineTo(bx - 14f * s, 78f)
                p.close()
                return p
            }
            canvas.drawPath(boltPath(36f, false), bolt)
            canvas.drawPath(boltPath(w - 36f, true), bolt)
        }
        QuoteCardStyle.MODERN -> {
            // Chat header: blue pill with white "Messages" and a
            // subtle avatar dot.
            val headBg = Paint().apply { color = 0xFF1E88E5.toInt() }
            canvas.drawRoundRect(RectF(0f, 0f, w, 76f), 0f, 0f, headBg)
            val avatar = Paint().apply { color = 0xFFFFFFFF.toInt() }
            canvas.drawCircle(56f, 38f, 20f, avatar)
            val head = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 36f
                isFakeBoldText = true
            }
            canvas.drawText("Messages", 92f, 51f, head)
        }
        QuoteCardStyle.SAMSUNG_MESSAGES -> {
            // One UI chat header: "Messages", the meta line, and the
            // outlined "Enter message" pill.
            val head = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1C.toInt()
                textSize = 56f
                isFakeBoldText = true
            }
            canvas.drawText("Messages", 90f, 62f, head)
            val meta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF6B6B6B.toInt()
                textSize = 24f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("RCS", w - 90f, 56f, meta)
            val pill = Paint().apply {
                color = 0xFF6B6B6B.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
                alpha = 150
            }
            canvas.drawRoundRect(RectF(90f, 78f, w - 90f, 128f), 25f, 25f, pill)
            val hint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF9A9A9A.toInt()
                textSize = 26f
            }
            canvas.drawText("Enter message", 122f, 113f, hint)
        }
        QuoteCardStyle.NOKIA -> {
            // Classic header: status line (time, signal bars, battery),
            // blue "NOKIA" band, and the Options/Select/Back bar.
            val status = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1A.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            canvas.drawText("12:00", 24f, 32f, status)
            val bar1 = Paint().apply { color = 0xFF1A1A1A.toInt() }
            for (k in 0..3) {
                canvas.drawRect(
                    w - 200f + k * 22f, 40f - (k + 1) * 8f,
                    w - 186f + k * 22f, 40f, bar1
                )
            }
            canvas.drawRect(w - 100f, 12f, w - 40f, 36f, Paint().apply {
                color = 0xFF1A1A1A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            })
            canvas.drawRect(w - 94f, 16f, w - 58f, 32f, bar1)
            val band = Paint().apply { color = 0xFF124191.toInt() }
            canvas.drawRect(0f, 44f, w, 100f, band)
            val nokia = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 52f
                isFakeBoldText = true
                letterSpacing = 0.12f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("NOKIA", w / 2f, 90f, nokia)
            val osb = Paint().apply { color = 0xFFE8E8E8.toInt() }
            canvas.drawRect(0f, 100f, w, 140f, osb)
            val osbTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1A.toInt()
                textSize = 24f
                isFakeBoldText = true
            }
            canvas.drawText("Options", 24f, 130f, osbTxt)
            val sel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1A.toInt()
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Select", w / 2f, 130f, sel)
            val back = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1A.toInt()
                textSize = 24f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Back", w - 24f, 130f, back)
        }
        QuoteCardStyle.BLACKBERRY -> {
            // BlackBerry Messenger header: the blue-dot BBM mark,
            // "BlackBerry Messenger", and the blue 4px rule.
            val mark = Paint().apply { color = 0xFF2A7FFF.toInt() }
            val dotR = 10f
            val markCx = 90f
            val markCy = 56f
            for (r in 0..2) {
                for (c in 0..2) {
                    canvas.drawCircle(
                        markCx + c * (dotR * 2.4f) - dotR * 2.4f,
                        markCy + r * (dotR * 2.4f) - dotR * 2.4f,
                        dotR, mark
                    )
                }
            }
            val word = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 44f
                isFakeBoldText = true
            }
            canvas.drawText("BlackBerry Messenger", 160f, 70f, word)
            val blue = Paint().apply {
                color = 0xFF2A7FFF.toInt()
                strokeWidth = 4f
            }
            canvas.drawLine(0f, 112f, w, 112f, blue)
        }
        QuoteCardStyle.WIN95 -> {
            // Blue title bar with a raised gray close button.
            val bar = Paint().apply {
                shader = LinearGradient(
                    40f, 0f, w - 40f, 0f,
                    0xFF000080.toInt(), 0xFF1084D0.toInt(),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(40f, 0f, w - 40f, 88f, bar)
            val hi = Paint().apply {
                color = 0xFFDFDFDF.toInt()
                strokeWidth = 4f
            }
            val lo = Paint().apply {
                color = 0xFF808080.toInt()
                strokeWidth = 4f
            }
            canvas.drawLine(40f, 2f, w - 40f, 2f, hi)
            canvas.drawLine(42f, 0f, 42f, 88f, hi)
            canvas.drawLine(40f, 88f, w - 40f, 88f, lo)
            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 40f
                isFakeBoldText = true
            }
            canvas.drawText("Quote404.exe", 64f, 58f, title)
            val btn = Paint().apply { color = 0xFFC6C6C6.toInt() }
            canvas.drawRect(w - 140f, 14f, w - 56f, 74f, btn)
            val btnHi = Paint().apply {
                color = 0xFFDFDFDF.toInt()
                strokeWidth = 3f
            }
            canvas.drawLine(w - 140f, 15f, w - 56f, 15f, btnHi)
            canvas.drawLine(w - 139f, 14f, w - 139f, 74f, btnHi)
            val xMark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF000000.toInt()
                textSize = 40f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("\u00D7", w - 98f, 60f, xMark)
        }
        QuoteCardStyle.PAGER -> {
            // PT-900 header: "SMS ● XTRACT PT-900" on one line,
            // "▶ 1 NEW MSG ◀" below it.
            val header = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3A443A.toInt()
                textSize = 44f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.12f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SMS \u25CF XTRACT PT-900", w / 2f, 56f, header)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3A443A.toInt()
                textSize = 34f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("\u25B6 1 NEW MSG \u25C0", w / 2f, 104f, sub)
        }
        QuoteCardStyle.MONO1999 -> {
            // Amber CRT masthead: dark panel, "SMS XTRACT" and the
            // "* SMS ARCHIVE *" banner, amber rule, status line.
            val panel = Paint().apply { color = 0xFF1A1408.toInt() }
            canvas.drawRect(0f, 0f, w, 110f, panel)
            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFB000.toInt()
                textSize = 44f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.2f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SMS XTRACT", w / 2f, 44f, title)
            val banner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFB000.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.3f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("* SMS ARCHIVE *", w / 2f, 72f, banner)
            val rule = Paint().apply {
                color = 0xFFFFB000.toInt()
                strokeWidth = 3f
            }
            canvas.drawLine(0f, 90f, w, 90f, rule)
            val status = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFB000.toInt()
                textSize = 20f
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.14f
            }
            canvas.drawText("80x25", 24f, 110f, status)
            val ok = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF7FFF7F.toInt()
                textSize = 20f
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.14f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("READY", w - 24f, 110f, ok)
        }
        QuoteCardStyle.RANSOM -> {
            // Ransom-note masthead: 8deg tilted "DO NOT DELETE" tape,
            // cutout-letter "SMS XTRACT" over the "we have your" sub-line.
            canvas.save()
            canvas.translate(w / 2f, 78f)
            canvas.rotate(-8f)
            val tape = Paint().apply { color = 0xFFE8D8A0.toInt() }
            canvas.drawRect(-250f, -40f, 250f, 8f, tape)
            val tapeTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF8A2A2A.toInt()
                textSize = 38f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.12f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("DO NOT DELETE", 0f, -6f, tapeTxt)
            canvas.restore()
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF5A5A5A.toInt()
                textSize = 24f
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.2f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("we have your", w / 2f, 148f, sub)
            fun rowW(letter: Paint, word: String): Float {
                var tw = 0f
                for (ch in word) tw += letter.measureText(ch.toString())
                return tw
            }
            val cuts = listOf(
                0xFFE23B3B.toInt(), 0xFF3B5BDB.toInt(), 0xFF3B9E3B.toInt(),
                0xFFE88B3B.toInt(), 0xFF9B3BE8.toInt(), 0xFF3BA8B8.toInt()
            )
            var cy = 0
            val mastY = 196f
            fun tile(ch: Char, cw: Float): Float {
                val rot = ((cy * 37 + ch.code) % 15) - 7f
                val letter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = cuts[cy % cuts.size]
                    textSize = 52f
                    isFakeBoldText = true
                    typeface = android.graphics.Typeface.SERIF
                    textAlign = Paint.Align.CENTER
                }
                val x = w / 2f - rowW(letter, "SMS XTRACT") / 2f + cw +
                    letter.measureText(ch.toString()) / 2f
                canvas.save()
                canvas.translate(x, mastY)
                canvas.rotate(rot)
                canvas.drawRect(-24f, -44f, 24f, 16f, Paint().apply {
                    color = 0xFFCFCFCF.toInt()
                })
                canvas.drawText(ch.toString(), 0f, 4f, letter)
                canvas.restore()
                cy++
                return cw + letter.measureText(ch.toString())
            }
            var xw = 0f
            for (ch in "SMS XTRACT") xw = tile(ch, xw)
        }
        QuoteCardStyle.IRC -> {
            // #quote404 topic bar.
            val bar = Paint().apply { color = 0xFF161B22.toInt() }
            canvas.drawRect(0f, 0f, w, 64f, bar)
            val edge = Paint().apply {
                color = 0xFF2D333B.toInt()
                strokeWidth = 2f
            }
            canvas.drawLine(0f, 64f, w, 64f, edge)
            val chan = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF79C0FF.toInt()
                textSize = 36f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
            }
            canvas.drawText("#quote404", 36f, 44f, chan)
            val topic = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF8B949E.toInt()
                textSize = 30f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            canvas.drawText(
                "| daily quotes archive",
                36f + chan.measureText("#quote404") + 24f, 44f, topic
            )
        }
        QuoteCardStyle.GAMEBOY -> {
            // Red power LED + DOT MATRIX strip.
            val led = Paint().apply { color = 0xFFE60012.toInt() }
            canvas.drawCircle(64f, 30f, 9f, led)
            val pwr = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF0F380F.toInt()
                textSize = 20f
                alpha = 170
            }
            canvas.drawText("POWER", 84f, 37f, pwr)
            val dm = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF0F380F.toInt()
                textSize = 28f
                alpha = 170
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("DOT MATRIX WITH STEREO SOUND", w / 2f, 40f, dm)
        }
        QuoteCardStyle.NES -> {
            // v0.7.5 masthead: blinking-yellow "- PRESS START -" over a
            // 4px blue rule (tx-nes-press / tx-page-head border).
            val press = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFE66D.toInt()
                textSize = 40f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("- PRESS START -", w / 2f, 64f, press)
            val rule = Paint().apply {
                color = 0xFF3B5BDB.toInt()
                strokeWidth = 4f
            }
            canvas.drawLine(0f, 96f, w, 96f, rule)
        }
        QuoteCardStyle.SNES -> {
            // Mode-7 header: purple sky bands, gold 6px rule, the
            // "16-BIT MESSAGE ADVENTURE" tagline.
            val sky1 = Paint().apply { color = 0xFF0F0C29.toInt() }
            val sky2 = Paint().apply { color = 0xFF302B63.toInt() }
            val sky3 = Paint().apply { color = 0xFF5B4B8A.toInt() }
            val sky4 = Paint().apply { color = 0xFFC86DD7.toInt() }
            canvas.drawRect(0f, 0f, w, 24f, sky1)
            canvas.drawRect(0f, 24f, w, 40f, sky2)
            canvas.drawRect(0f, 40f, w, 50f, sky3)
            canvas.drawRect(0f, 50f, w, 56f, sky4)
            val gold = Paint().apply { color = 0xFFF9D423.toInt() }
            canvas.drawRect(0f, 56f, w, 62f, gold)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF9D423.toInt()
                textSize = 34f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.22f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("16-BIT MESSAGE ADVENTURE", w / 2f, 104f, sub)
        }
        QuoteCardStyle.SEGA -> {
            // Red/black/white stripe band + blast-processing tagline.
            val red = Paint().apply { color = 0xFFE60012.toInt() }
            val black = Paint().apply { color = 0xFF0A0A0A.toInt() }
            val white = Paint().apply { color = 0xFFF2F2F2.toInt() }
            var x = 0f
            while (x < w) {
                canvas.drawRect(x, 0f, x + 28f, 16f, red)
                canvas.drawRect(x + 28f, 0f, x + 42f, 16f, black)
                canvas.drawRect(x + 42f, 0f, x + 70f, 16f, white)
                canvas.drawRect(x + 70f, 0f, x + 84f, 16f, black)
                x += 84f
            }
            val tag = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFE60012.toInt()
                textSize = 34f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.14f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("16-BIT BLAST PROCESSING", w / 2f, 66f, tag)
        }
        QuoteCardStyle.ATARI -> {
            // Wood panel header: dark rails, striped brown panel, cream
            // title, then the 16px rainbow stripe.
            val dark = Paint().apply { color = 0xFF3A2410.toInt() }
            canvas.drawRect(0f, 0f, w, 6f, dark)
            val tones = listOf(
                0xFF8A5A2B.toInt(), 0xFF7A4E24.toInt(),
                0xFF96683A.toInt(), 0xFF6E4520.toInt()
            )
            var x = 0f
            var i = 0
            while (x < w) {
                val p = Paint().apply { color = tones[i % tones.size] }
                canvas.drawRect(x, 6f, x + 26f, 104f, p)
                x += 26f
                i++
            }
            val titleShadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3A2410.toInt()
                textSize = 52f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SMS XTRACT", w / 2f + 3f, 61f, titleShadow)
            val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF5EEDA.toInt()
                textSize = 52f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SMS XTRACT", w / 2f, 58f, title)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFE9B8.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.24f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("VIDEO MESSAGE SYSTEM", w / 2f, 92f, sub)
            canvas.drawRect(0f, 104f, w, 110f, dark)
            val rainbow = listOf(
                0xFFE23B3B.toInt(), 0xFFE88B3B.toInt(), 0xFFE8D93B.toInt(),
                0xFF3BE855.toInt(), 0xFF3B9DE8.toInt(), 0xFF9B3BE8.toInt()
            )
            val segW = w / 6f
            rainbow.forEachIndexed { k, c ->
                val p = Paint().apply { color = c }
                canvas.drawRect(k * segW, 110f, (k + 1) * segW, 126f, p)
            }
        }
        QuoteCardStyle.WANTED -> {
            // Wanted-poster masthead: serif WANTED, star rule, the crime,
            // and the honest REWARD line.
            val mast = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3A2C1A.toInt()
                textSize = 76f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                letterSpacing = 0.18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("WANTED", w / 2f, 84f, mast)
            val stars = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF6B4F2A.toInt()
                textSize = 30f
                letterSpacing = 0.3f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("\u2722 \u2722 \u2722", w / 2f, 116f, stars)
            val sub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3A2C1A.toInt()
                textSize = 30f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                letterSpacing = 0.08f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "FOR THE CRIME OF EXCELLENT CONVERSATION", w / 2f, 150f, sub
            )
            val reward = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3A2C1A.toInt()
                textSize = 36f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                letterSpacing = 0.1f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("REWARD: 1 REPLY", w / 2f, 190f, reward)
        }
        QuoteCardStyle.CHALKBOARD -> {
            // Chalk "~ SMS Xtract ~" header + wavy squiggle underline.
            val chalk = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFF4F1E8.toInt()
                textSize = 48f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
                alpha = 230
            }
            canvas.drawText("~ SMS Xtract ~", w / 2f, 64f, chalk)
            val squig = Paint().apply {
                color = 0xFFF4F1E8.toInt()
                strokeWidth = 4f
                alpha = 200
                style = Paint.Style.STROKE
            }
            val path = android.graphics.Path()
            var sx = w / 2f - 210f
            path.moveTo(sx, 88f)
            while (sx < w / 2f + 210f) {
                path.quadTo(sx + 15f, 78f, sx + 30f, 88f)
                sx += 30f
            }
            canvas.drawPath(path, squig)
        }
        QuoteCardStyle.TERMINAL -> {
            // ASCII-art masthead + dump prompt lines, after the theme's header.
            val art = listOf(
                " _____  __  _______                __",
                "|_   _|/ /_/ ___/________ ______/ /_",
                "  |/ / // / /__/ ___/ __ `/ ___/ __/",
                "  |/ / // / /__/ /  / /_/ / /__/ /_",
                " /_/ \\_, /\\___/_/   \\__,_/\\___/\\__/",
                "     /___/            SMS BACKUP v1.0"
            )
            val green = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF33FF66.toInt()
                textSize = 22f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            var artW = 0f
            for (line in art) artW = maxOf(artW, green.measureText(line))
            val ax = (w - artW) / 2f
            var ay = 32f
            for (line in art) {
                canvas.drawText(line, ax, ay, green)
                ay += 26f
            }
            val prompt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1F7A3D.toInt()
                textSize = 24f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            val edition = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF7DFF9E.toInt()
                textSize = 24f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            val gt = prompt.measureText("> ")
            canvas.drawText("> ", ax, ay + 16f, prompt)
            canvas.drawText("dump --quote #$quoteNumber --format card", ax + gt, ay + 16f, edition)
            canvas.drawText("> ", ax, ay + 50f, prompt)
            canvas.drawText("1 quote archived_", ax + gt, ay + 50f, edition)
        }
        else -> { /* This style has no top chrome. */ }
    }
    canvas.restore()

    // ---------- 3. text-area surfaces (absolute coords) ----------
    when (cardStyle) {
        QuoteCardStyle.SEGA -> {
            // Black/white checkerboard band (18px cells) around the quote.
            val band = 12f
            val cell = 18f
            val l = textRect.left - 30f
            val t = textRect.top - 30f
            val r = textRect.right + 30f
            val b = textRect.bottom + 30f
            val white = Paint().apply { color = 0xFFF2F2F2.toInt() }
            canvas.drawRect(l, t, r, t + band, white)
            canvas.drawRect(l, b - band, r, b, white)
            canvas.drawRect(l, t, l + band, b, white)
            canvas.drawRect(r - band, t, r, b, white)
            val black = Paint().apply { color = 0xFF0A0A0A.toInt() }
            var i = 0
            var x = l
            while (x < r) {
                val x2 = minOf(x + cell, r)
                if (i % 2 == 0) {
                    canvas.drawRect(x, t, x2, t + band, black)
                    canvas.drawRect(x, b - band, x2, b, black)
                }
                x += cell
                i++
            }
            var j = 1
            var y = t
            while (y < b) {
                val y2 = minOf(y + cell, b)
                if (j % 2 == 0) {
                    canvas.drawRect(l, y, l + band, y2, black)
                    canvas.drawRect(r - band, y, r, y2, black)
                }
                y += cell
                j++
            }
            // v0.7.10: dimmed diagonal red speed lines behind the text
            // (5% wash, clipped to the inner panel).
            val inner = RectF(l + band, t + band, r - band, b - band)
            canvas.save()
            canvas.clipRect(inner)
            val speed = Paint().apply {
                color = 0xFFE60012.toInt()
                strokeWidth = 6f
                alpha = 13
            }
            var sx = l - (b - t)
            while (sx < r + (b - t)) {
                canvas.drawLine(sx, b, sx + (b - t), t, speed)
                sx += 24f
            }
            canvas.restore()
        }
        QuoteCardStyle.WIN95 -> {
            // Full window below the title bar: body, sunken client area
            // behind the quote, status bar just above the footer.
            val bodyT = chromeDy + 88f
            val bodyB = cardH - 76f
            val gray = Paint().apply { color = 0xFFC0C0C0.toInt() }
            canvas.drawRect(40f, bodyT, w - 40f, bodyB, gray)
            val hi = Paint().apply {
                color = 0xFFDFDFDF.toInt()
                strokeWidth = 4f
            }
            val lo = Paint().apply {
                color = 0xFF808080.toInt()
                strokeWidth = 4f
            }
            canvas.drawLine(40f, bodyT, 40f, bodyB, hi)
            canvas.drawLine(w - 40f, bodyT, w - 40f, bodyB, lo)
            canvas.drawLine(40f, bodyB, w - 40f, bodyB, lo)
            // Sunken white client area behind the quote text.
            val client = RectF(
                textRect.left - 24f, textRect.top - 24f,
                textRect.right + 24f, textRect.bottom + 24f
            )
            val white = Paint().apply { color = 0xFFFFFFFF.toInt() }
            canvas.drawRect(client, white)
            val sunk = Paint().apply {
                color = 0xFF808080.toInt()
                strokeWidth = 5f
            }
            canvas.drawLine(client.left, client.top, client.right, client.top, sunk)
            canvas.drawLine(client.left, client.top, client.left, client.bottom, sunk)
            // Status bar strip just above the footer.
            val stat = RectF(56f, bodyB - 48f, w - 56f, bodyB - 12f)
            canvas.drawRect(stat, white)
            canvas.drawLine(stat.left, stat.top, stat.right, stat.top, sunk)
            canvas.drawLine(stat.left, stat.top, stat.left, stat.bottom, sunk)
            val ready = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF000000.toInt()
                textSize = 28f
            }
            canvas.drawText("Ready", stat.left + 16f, stat.bottom - 12f, ready)
        }
        QuoteCardStyle.NOKIA -> {
            // Correct Series-40-ish LCD: #B7C9DE background, gray plastic
            // frame with a deep shadow, Options/Select/Back softkeys below.
            val screen = RectF(
                textRect.left - 34f, textRect.top - 34f,
                textRect.right + 34f, textRect.bottom + 34f
            )
            val shadow = Paint().apply { color = 0xFF1A1A1A.toInt() }
            canvas.drawRoundRect(
                RectF(
                    screen.left + 6f, screen.top + 10f,
                    screen.right + 6f, screen.bottom + 10f
                ),
                18f, 18f, shadow
            )
            val frame = Paint().apply { color = 0xFF6B6B6B.toInt() }
            canvas.drawRoundRect(
                RectF(
                    screen.left - 12f, screen.top - 12f,
                    screen.right + 12f, screen.bottom + 12f
                ),
                18f, 18f, frame
            )
            val lcd = Paint().apply { color = 0xFFB7C9DE.toInt() }
            canvas.drawRoundRect(screen, 8f, 8f, lcd)
            val keys = Paint().apply { color = 0xFF0A2A5E.toInt() }
            val keyT = screen.bottom + 12f
            canvas.drawRoundRect(
                RectF(screen.left, keyT, screen.right, keyT + 56f),
                12f, 12f, keys
            )
            val keyTx = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 28f
                isFakeBoldText = true
            }
            canvas.drawText("Options", screen.left + 24f, keyT + 38f, keyTx)
            val sel = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 28f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Select", screen.centerX(), keyT + 38f, sel)
            val back = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 28f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Back", screen.right - 24f, keyT + 38f, back)
        }
        QuoteCardStyle.GAMEBOY -> {
            // Light-DMG game screen: #C6C6BD shell, rounded rect screen,
            // green 2px inner ring, clipped scanlines.
            val shell = RectF(
                36f, textRect.top - 44f, w - 36f, textRect.bottom + 64f
            )
            val shellFill = Paint().apply { color = 0xFFC6C6BD.toInt() }
            val shellEdge = Paint().apply {
                color = 0xFF8A8A7E.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
            canvas.drawRoundRect(shell, 36f, 36f, shellFill)
            canvas.drawRoundRect(shell, 36f, 36f, shellEdge)
            val screenR = RectF(
                textRect.left - 28f, textRect.top - 20f,
                textRect.right + 28f, textRect.bottom + 28f
            )
            val scr = Paint().apply { color = 0xFF9BBC0F.toInt() }
            canvas.drawRoundRect(screenR, 12f, 12f, scr)
            val ring = Paint().apply {
                color = 0xFF306230.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(
                RectF(
                    screenR.left + 6f, screenR.top + 6f,
                    screenR.right - 6f, screenR.bottom - 6f
                ),
                8f, 8f, ring
            )
            canvas.save()
            canvas.clipRect(screenR)
            val scan = Paint().apply {
                color = 0xFF0F380F.toInt()
                strokeWidth = 2f
                alpha = 40
            }
            var y = screenR.top
            while (y < screenR.bottom) {
                canvas.drawLine(screenR.left, y, screenR.right, y, scan)
                y += 4f
            }
            canvas.restore()
        }
        QuoteCardStyle.COMIC_BOOK -> {
            // White speech-balloon surface behind the quote.
            val box = RectF(
                textRect.left - 32f, textRect.top - 32f,
                textRect.right + 32f, textRect.bottom + 32f
            )
            val shadow = Paint().apply { color = 0xFF111111.toInt() }
            canvas.drawRoundRect(
                RectF(box.left + 12f, box.top + 12f, box.right + 12f, box.bottom + 12f),
                18f, 18f, shadow
            )
            val paper = Paint().apply { color = 0xFFFFFFFF.toInt() }
            val ink = Paint().apply {
                color = 0xFF111111.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 8f
            }
            canvas.drawRoundRect(box, 18f, 18f, paper)
            canvas.drawRoundRect(box, 18f, 18f, ink)
            // Yellow caption box with the red star, pinned on the balloon's
            // top edge — the v0.7.10 layout (caption above/outside the balloon).
            val starP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFD42A1E.toInt()
                textSize = 24f
                isFakeBoldText = true
            }
            val capP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF111111.toInt()
                textSize = 24f
                isFakeBoldText = true
            }
            val capRest = "QUOTE #$quoteNumber"
            val capW = starP.measureText("\u2605 ") + capP.measureText(capRest)
            val ccx = w / 2f
            val ccy = textRect.top - 36f
            canvas.save()
            canvas.rotate(-1f, ccx, ccy)
            val capBox = RectF(ccx - capW / 2f - 20f, ccy - 30f, ccx + capW / 2f + 20f, ccy + 10f)
            val capSh = Paint().apply { color = 0xFF111111.toInt() }
            canvas.drawRect(
                RectF(capBox.left + 4f, capBox.top + 4f, capBox.right + 4f, capBox.bottom + 4f),
                capSh
            )
            val capFill = Paint().apply { color = 0xFFFFE14D.toInt() }
            canvas.drawRect(capBox, capFill)
            val capEdge = Paint().apply {
                color = 0xFF111111.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRect(capBox, capEdge)
            val tx0 = capBox.left + 20f
            canvas.drawText("\u2605 ", tx0, ccy, starP)
            canvas.drawText(capRest, tx0 + starP.measureText("\u2605 "), ccy, capP)
            canvas.restore()
        }
        QuoteCardStyle.SAMSUNG_MESSAGES -> {
            // One UI chat window: white 44f-radius panel with a hairline
            // edge plus the blue "Message" pill at the bottom.
            val win = RectF(
                textRect.left - 36f, textRect.top - 36f,
                textRect.right + 36f, textRect.bottom + 96f
            )
            val sh = Paint().apply {
                color = 0xFF000000.toInt()
                alpha = 24
            }
            canvas.drawRoundRect(
                RectF(win.left, win.top + 10f, win.right, win.bottom + 10f),
                44f, 44f, sh
            )
            val white = Paint().apply { color = 0xFFFFFFFF.toInt() }
            val edge = Paint().apply {
                color = 0xFFE5E5EA.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(win, 44f, 44f, white)
            canvas.drawRoundRect(win, 44f, 44f, edge)
            val pill = Paint().apply { color = 0xFF0381FE.toInt() }
            canvas.drawRoundRect(
                RectF(win.left + 40f, win.bottom - 76f, win.right - 40f, win.bottom - 24f),
                26f, 26f, pill
            )
            val pillTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 30f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "Message", win.centerX(), win.bottom - 38f, pillTxt
            )
        }
        QuoteCardStyle.BLACKBERRY -> {
            // Blue BBM bubble dressing: blue double-chevrons and the
            // "✓✓ Read" mark under the quote.
            val blue = Paint().apply { color = 0xFF2A7FFF.toInt() }
            val chev = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2A7FFF.toInt()
                textSize = 44f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                alpha = 170
            }
            canvas.drawText("\u00BB", textRect.left + 30f, textRect.top - 20f, chev)
            canvas.drawText("\u00AB", textRect.right - 30f, textRect.bottom + 60f, chev)
            val read = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2A7FFF.toInt()
                textSize = 26f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(
                "\u2713\u2713 Read",
                textRect.right + 30f, textRect.bottom + 60f, read
            )
        }
        QuoteCardStyle.RANSOM -> {
            // Paper sheet pinned by masking-tape strips at its top corners.
            val sheet = RectF(
                textRect.left - 24f, textRect.top - 24f,
                textRect.right + 24f, textRect.bottom + 24f
            )
            val sh = Paint().apply {
                color = 0xFF000000.toInt()
                alpha = 30
            }
            canvas.drawRect(
                sheet.left + 6f, sheet.top + 8f,
                sheet.right + 6f, sheet.bottom + 8f, sh
            )
            val paper = Paint().apply { color = 0xFFFBF8EC.toInt() }
            canvas.drawRect(sheet, paper)
            val tape = Paint().apply {
                color = 0xFFD8CBA0.toInt()
                alpha = 190
            }
            canvas.save()
            canvas.rotate(-14f, sheet.left + 75f, sheet.top)
            canvas.drawRect(
                sheet.left, sheet.top - 20f,
                sheet.left + 150f, sheet.top + 16f, tape
            )
            canvas.restore()
            canvas.save()
            canvas.rotate(14f, sheet.right - 75f, sheet.top)
            canvas.drawRect(
                sheet.right - 150f, sheet.top - 20f,
                sheet.right, sheet.top + 16f, tape
            )
            canvas.restore()
        }
        QuoteCardStyle.TERMINAL -> {
            // One log-line header over the quote: the theme renders every
            // message as "[time] <who> body".
            val log = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2EA85C.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "[archived] <quote404> quote #$quoteNumber",
                w / 2f, textRect.top - 6f, log
            )
        }
        QuoteCardStyle.MOVIE_SCRIPT -> {
            // Centered uppercase character cue over the dialogue block,
            // after the theme's cue-per-message layout.
            val cue = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF111111.toInt()
                textSize = 30f
                typeface = android.graphics.Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("QUOTE #$quoteNumber", w / 2f, textRect.top - 8f, cue)
        }
        QuoteCardStyle.CASE_FILE -> {
            // Evidence sheet behind the quote + the evidence-tag row on
            // top, after the theme's .tx-msg cards and .tx-evrow labels.
            val sheet = RectF(
                textRect.left - 28f, textRect.top - 28f,
                textRect.right + 28f, textRect.bottom + 28f
            )
            val card = Paint().apply { color = 0xFFE8D9B0.toInt() }
            canvas.drawRect(sheet, card)
            val edge = Paint().apply {
                color = 0xFF2A2118.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRect(sheet, edge)
            val tagTx = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2A2118.toInt()
                textSize = 22f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
            }
            val chip = Paint().apply {
                color = 0xFF000000.toInt()
                alpha = 20
            }
            val ty = textRect.top - 8f
            var tx = textRect.left
            for (t in listOf("ITEM #$quoteNumber", "FROM: QUOTE404")) {
                val tw = tagTx.measureText(t)
                canvas.drawRect(tx - 10f, ty - 26f, tx + tw + 10f, ty + 8f, chip)
                canvas.drawText(t, tx, ty, tagTx)
                tx += tw + 36f
            }
        }
        QuoteCardStyle.TELEGRAM -> {
            // Centered dispatch meta above the message and the signature
            // STOP below, after the theme's .tx-meta lines and STOP-joined
            // sentences.
            val meta = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2B2416.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
            }
            meta.letterSpacing = 0.12f
            canvas.drawText(
                "QUOTE404 \u00B7 DISPATCH #$quoteNumber",
                w / 2f, textRect.top - 8f, meta
            )
            val stop = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF2B2416.toInt()
                textSize = 28f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                textAlign = Paint.Align.CENTER
                alpha = 200
            }
            stop.letterSpacing = 0.3f
            canvas.drawText("STOP", w / 2f, textRect.bottom + 44f, stop)
        }
        QuoteCardStyle.MONO1999 -> {
            // Old amber CRT: dark screen box over the quote, fat bezel,
            // dense dark scanlines on top of it.
            val screen = RectF(
                textRect.left - 24f, textRect.top - 24f,
                textRect.right + 24f, textRect.bottom + 24f
            )
            val bezel = Paint().apply { color = 0xFF2A2118.toInt() }
            canvas.drawRoundRect(
                RectF(
                    screen.left - 16f, screen.top - 16f,
                    screen.right + 16f, screen.bottom + 16f
                ),
                14f, 14f, bezel
            )
            val fill = Paint().apply { color = 0xFF1A1408.toInt() }
            canvas.drawRect(screen, fill)
            canvas.save()
            canvas.clipRect(screen)
            val scan = Paint().apply {
                color = 0xFF000000.toInt()
                strokeWidth = 2f
                alpha = 90
            }
            var y = screen.top
            while (y < screen.bottom) {
                canvas.drawLine(screen.left, y, screen.right, y, scan)
                y += 4f
            }
            canvas.restore()
        }
        QuoteCardStyle.PAGER -> {
            // Per-message pager LCD: two-tone green screen shell with
            // dark bevel and faint LCD lines behind the quote.
            val lcdR = RectF(
                textRect.left - 26f, textRect.top - 26f,
                textRect.right + 26f, textRect.bottom + 26f
            )
            val shell = Paint().apply { color = 0xFF2A332A.toInt() }
            canvas.drawRoundRect(
                RectF(
                    lcdR.left - 12f, lcdR.top - 12f,
                    lcdR.right + 12f, lcdR.bottom + 12f
                ),
                16f, 16f, shell
            )
            val edge = Paint().apply {
                color = 0xFF3C443C.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(lcdR, 10f, 10f, edge)
            val grad = LinearGradient(
                lcdR.left, lcdR.top, lcdR.left, lcdR.bottom,
                0xFF9CAE91.toInt(), 0xFF7C8F76.toInt(),
                Shader.TileMode.CLAMP
            )
            val lcd = Paint().apply { shader = grad }
            canvas.drawRoundRect(lcdR, 10f, 10f, lcd)
            canvas.save()
            canvas.clipRect(lcdR)
            val rows = Paint().apply {
                color = 0xFF263026.toInt()
                strokeWidth = 2f
                alpha = 30
            }
            var y = lcdR.top + 8f
            while (y < lcdR.bottom) {
                canvas.drawLine(lcdR.left, y, lcdR.right, y, rows)
                y += 12f
            }
            canvas.restore()
        }
        QuoteCardStyle.NES -> {
            // RPG dialog box: #101010 panel, 6px double gray border,
            // yellow blinking-cursor ▼ bottom-right.
            val box = RectF(
                textRect.left - 30f, textRect.top - 30f,
                textRect.right + 30f, textRect.bottom + 30f
            )
            val fill = Paint().apply { color = 0xFF101010.toInt() }
            canvas.drawRect(box, fill)
            val outer = Paint().apply {
                color = 0xFF9AA0A6.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 6f
            }
            canvas.drawRect(box, outer)
            val inner = Paint().apply {
                color = 0xFF9AA0A6.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRect(
                RectF(
                    box.left + 14f, box.top + 14f,
                    box.right - 14f, box.bottom - 14f
                ),
                inner
            )
            val cursor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFE66D.toInt()
                textSize = 42f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("\u25BC", box.right - 22f, box.bottom - 18f, cursor)
        }
        QuoteCardStyle.SNES -> {
            // 16-bit JRPG dialog: blue box, chunky purple border, hard
            // #0f0c29 drop shadow.
            val box = RectF(
                textRect.left - 32f, textRect.top - 32f,
                textRect.right + 32f, textRect.bottom + 32f
            )
            val shadow = Paint().apply { color = 0xFF0F0C29.toInt() }
            canvas.drawRoundRect(
                RectF(box.left, box.top + 8f, box.right, box.bottom + 8f),
                12f, 12f, shadow
            )
            val fill = Paint().apply { color = 0xFF23233F.toInt() }
            canvas.drawRoundRect(box, 12f, 12f, fill)
            val edge = Paint().apply {
                color = 0xFF5B4B8A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(box, 12f, 12f, edge)
        }
        QuoteCardStyle.ATARI -> {
            // Chunky low-res CRT screen: black panel, gray border,
            // heavy scanlines over it (tx-atari-screen::after).
            val screen = RectF(
                textRect.left - 30f, textRect.top - 30f,
                textRect.right + 30f, textRect.bottom + 30f
            )
            val fill = Paint().apply { color = 0xFF101010.toInt() }
            canvas.drawRect(screen, fill)
            val edge = Paint().apply {
                color = 0xFF6B6B6B.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRect(screen, edge)
            canvas.save()
            canvas.clipRect(screen)
            val scan = Paint().apply {
                color = 0xFF000000.toInt()
                strokeWidth = 2f
                alpha = 100
            }
            var y = screen.top
            while (y < screen.bottom) {
                canvas.drawLine(screen.left, y, screen.right, y, scan)
                y += 4f
            }
            canvas.restore()
        }
        QuoteCardStyle.WANTED -> {
            // Mini wanted poster framing the quote: aged-paper panel,
            // 4px double border, "REWARD: 1 REPLY" footer.
            val poster = RectF(
                textRect.left - 30f, textRect.top - 30f,
                textRect.right + 30f, textRect.bottom + 70f
            )
            val paper = Paint().apply { color = 0xFFEFE0BA.toInt() }
            canvas.drawRect(poster, paper)
            val outer = Paint().apply {
                color = 0xFF3A2C1A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRect(poster, outer)
            val inner = Paint().apply {
                color = 0xFF3A2C1A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRect(
                RectF(
                    poster.left + 10f, poster.top + 10f,
                    poster.right - 10f, poster.bottom - 10f
                ),
                inner
            )
            val hair = Paint().apply {
                color = 0xFF3A2C1A.toInt()
                strokeWidth = 2f
                alpha = 200
            }
            canvas.drawLine(
                poster.left + 24f, poster.bottom - 56f,
                poster.right - 24f, poster.bottom - 56f, hair
            )
            val reward = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF3A2C1A.toInt()
                textSize = 32f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.SERIF
                letterSpacing = 0.1f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("REWARD: 1 REPLY", poster.centerX(), poster.bottom - 20f, reward)
        }
        QuoteCardStyle.CHALKBOARD -> {
            // Per-message chalk dressing: chalk-dot squiggle + wooden
            // tray with a chalk stub and eraser.
            val trayY = textRect.bottom + 30f
            val dot = Paint().apply {
                color = 0xFFF4F1E8.toInt()
                alpha = 200
            }
            var dx = textRect.left - 30f
            while (dx < textRect.right + 30f) {
                canvas.drawCircle(dx, trayY - 12f, 2.2f, dot)
                dx += 18f
            }
            val wood = Paint().apply {
                color = 0xFF8A6F4D.toInt()
                strokeWidth = 3f
                alpha = 230
            }
            canvas.drawLine(
                textRect.left - 30f, trayY, textRect.right + 30f, trayY, wood
            )
            val stub = Paint().apply { color = 0xFFF4F1E8.toInt() }
            canvas.save()
            canvas.rotate(-2f, textRect.left + 20f, trayY + 10f)
            canvas.drawRoundRect(
                RectF(
                    textRect.left + 4f, trayY + 6f,
                    textRect.left + 38f, trayY + 14f
                ),
                2f, 2f, stub
            )
            canvas.restore()
            val eraser = Paint().apply { color = 0xFF5A4632.toInt() }
            canvas.drawRoundRect(
                RectF(
                    textRect.right - 50f, trayY + 5f,
                    textRect.right - 4f, trayY + 17f
                ),
                3f, 3f, eraser
            )
        }
        QuoteCardStyle.TYPEWRITER -> {
            // Letter sheet under the quote: cream paper, red margin line,
            // and the typed signoff.
            val sheet = RectF(
                textRect.left - 28f, textRect.top - 28f,
                textRect.right + 28f, textRect.bottom + 96f
            )
            val sh = Paint().apply {
                color = 0xFF000000.toInt()
                alpha = 30
            }
            canvas.drawRect(
                sheet.left + 6f, sheet.top + 8f,
                sheet.right + 6f, sheet.bottom + 8f, sh
            )
            val paper = Paint().apply { color = 0xFFF6F1E3.toInt() }
            canvas.drawRect(sheet, paper)
            val margin = Paint().apply {
                color = 0xFFE07060.toInt()
                strokeWidth = 3f
                alpha = 70
            }
            canvas.drawLine(
                sheet.left + 70f, sheet.top, sheet.left + 70f, sheet.bottom, margin
            )
            val sign = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1A1A1A.toInt()
                textSize = 30f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            canvas.drawText("- SMS XTRACT", sheet.left + 40f, sheet.bottom - 28f, sign)
        }
        QuoteCardStyle.HEAVY_METAL -> {
            // Setlist card: black 18f-radius card, red edge, white
            // "SMS XTRACT", the "SETLIST" band and numbered songs.
            val card = RectF(
                textRect.left - 32f, textRect.top - 32f,
                textRect.right + 32f, textRect.bottom + 32f
            )
            val black = Paint().apply { color = 0xFF0A0A0A.toInt() }
            canvas.drawRoundRect(card, 18f, 18f, black)
            val edge = Paint().apply {
                color = 0xFFFF2A2A.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawRoundRect(card, 18f, 18f, edge)
            val band = Paint().apply { color = 0xFF8F1018.toInt() }
            canvas.drawRect(card.left, card.top, card.right, card.top + 54f, band)
            val bandTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = 32f
                isFakeBoldText = true
                typeface = android.graphics.Typeface.MONOSPACE
                letterSpacing = 0.2f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SETLIST", card.centerX(), card.top + 39f, bandTxt)
            val song = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFE8E8E8.toInt()
                textSize = 26f
                typeface = android.graphics.Typeface.MONOSPACE
            }
            val songs = listOf(
                "1. INTRO / FEEDBACK",
                "2. VOLUME TO ELEVEN",
                "3. ENCORE"
            )
            var sy = textRect.bottom + 70f
            for (s in songs) {
                canvas.drawText(s, card.left + 32f, sy, song)
                sy += 38f
            }
        }
        QuoteCardStyle.MODERN -> {
            // Blue chat window: white 44f-radius panel, hairline edge,
            // and the incoming blue chat bubble dressing.
            val win = RectF(
                textRect.left - 36f, textRect.top - 36f,
                textRect.right + 36f, textRect.bottom + 36f
            )
            val sh = Paint().apply {
                color = 0xFF000000.toInt()
                alpha = 24
            }
            canvas.drawRoundRect(
                RectF(win.left, win.top + 8f, win.right, win.bottom + 8f),
                44f, 44f, sh
            )
            val white = Paint().apply { color = 0xFFFFFFFF.toInt() }
            val edge = Paint().apply {
                color = 0xFFE5E5EA.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(win, 44f, 44f, white)
            canvas.drawRoundRect(win, 44f, 44f, edge)
            val bubble = Paint().apply { color = 0xFF1E88E5.toInt() }
            canvas.drawRoundRect(
                RectF(win.left + 40f, win.bottom - 84f, win.left + 320f, win.bottom - 24f),
                30f, 30f, bubble
            )
        }
        else -> { /* No text-area surface for this style. */ }
    }
}

/**
 * v1.88.33: how tall a TXTRACT theme's top chrome is (the part that
 * sits between the logo block and the quote text). Implemented alongside
 * the faithful theme ports in drawTxtractCardDetails.
 */
private fun txtractChromeH(style: QuoteCardStyle): Float = when (style) {
    QuoteCardStyle.NEWSPAPER -> 200f
    QuoteCardStyle.TERMINAL -> 250f
    QuoteCardStyle.COMIC_BOOK -> 170f
    QuoteCardStyle.MOVIE_SCRIPT -> 170f
    QuoteCardStyle.CASE_FILE -> 190f
    QuoteCardStyle.TELEGRAM -> 170f
    QuoteCardStyle.MODERN -> 76f
    QuoteCardStyle.TYPEWRITER -> 140f
    QuoteCardStyle.HEAVY_METAL -> 150f
    QuoteCardStyle.SAMSUNG_MESSAGES -> 100f
    QuoteCardStyle.NOKIA -> 120f
    QuoteCardStyle.BLACKBERRY -> 112f
    QuoteCardStyle.WIN95 -> 120f
    QuoteCardStyle.MONO1999 -> 120f
    QuoteCardStyle.RANSOM -> 224f
    QuoteCardStyle.PAGER -> 140f
    QuoteCardStyle.IRC -> 64f
    QuoteCardStyle.GAMEBOY -> 60f
    QuoteCardStyle.NES -> 120f
    QuoteCardStyle.SNES -> 130f
    QuoteCardStyle.SEGA -> 96f
    QuoteCardStyle.ATARI -> 140f
    QuoteCardStyle.WANTED -> 200f
    QuoteCardStyle.CHALKBOARD -> 110f
    else -> 0f
}

fun renderQuoteCard(
    quote: Quote,
    style: QuoteCardStyle,
    includeNumber: Boolean,
    includeRating: Boolean = false,
    includeUrl: Boolean = false,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    /** v1.61: optional full-bleed artwork background (DevXen style). */
    artwork: Bitmap? = null,
    /**
     * v1.61.7: the database this quote came from ("Bash.org"), drawn as
     * the source line in the footer.
     */
    sourceLabel: String? = null,
    /**
     * v1.61.8: Clint's Quote404 logo, drawn as a faint centered watermark
     * over the finished card. Null = no watermark.
     *
     * v1.61.15: the logo is drawn large, right on the background
     * ([Canvas.drawQuote404LogoOnArt]) — no separate strip floating
     * above the card. v1.61.16: it sits just above the quote text (one
     * centered group), and the baked-in tagline is redrawn as vector
     * text so it reads. Null = no logo.
     */
    watermark: Bitmap? = null,
    /**
     * v1.61.11: nick colorization for the share card, mirroring the
     * on-screen card (same palette, contrast-adjusted). The share dialog's
     * "Colorize nicknames" checkbox drives [colorizeNicks].
     */
    colorizeNicks: Boolean = false,
    nickPalette: NickPalette? = null,
    /**
     * v1.61.11: drew the black Quote404 logo strip across the top of the
     * card. v1.61.15: the logo now sits right on the background instead
     * (see below) — false when the caller brands the image itself
     * (multi-quote strips put one plate at the top of the whole image).
     */
    includeLogo: Boolean = true
): Bitmap {
    // v1.61.16: branded header — the quote number top-right with the
    // rating under it.
    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.accent.toInt()
        textSize = 64f
        isFakeBoldText = true
    }
    var top = 170f
    // (Header text is drawn after the bitmap exists — see below.)
    if (includeNumber) top += 110f
    if (includeRating && quote.rating != null) top += 96f
    // v1.61.15: the accent rule (the little blue line above the quote)
    // is gone — Clint asked it removed.

    // Wrapped quote text (IRC " \" separators and dialogue turns become
    // line breaks, like in-app). Built BEFORE the bitmap so a long quote
    // can grow the canvas instead of being cut off (v1.61.20).
    val cardText = applyDisplayBreaks(quote.text, ircLineBreaks, dialogueLineBreaks)
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.textColor.toInt()
        textSize = 52f
        // v1.88.32: the SMS Xtract ports carry their own voice —
        // terminals/typewriters/screenplays in monospace, newsprint and
        // telegrams in serif.
        typeface = when (style) {
            QuoteCardStyle.TERMINAL, QuoteCardStyle.MOVIE_SCRIPT,
            QuoteCardStyle.CASE_FILE, QuoteCardStyle.TYPEWRITER,
            QuoteCardStyle.IRC, QuoteCardStyle.PAGER,
            QuoteCardStyle.MONO1999, QuoteCardStyle.GAMEBOY ->
                android.graphics.Typeface.MONOSPACE
            QuoteCardStyle.NEWSPAPER, QuoteCardStyle.TELEGRAM,
            QuoteCardStyle.WANTED, QuoteCardStyle.RANSOM ->
                android.graphics.Typeface.SERIF
            else -> null
        }
    }
    // v1.61.11: optional nick colorization, mirroring the on-screen card
    // (same palette, contrast-adjusted to the card background).
    val bgForNicks = if (artwork != null) Color.Black
    else Color(style.bgBottom.toInt())
    val spannedText: CharSequence = if (colorizeNicks && nickPalette != null) {
        SpannableString(cardText).apply {
            for (s in nickExportSpans(cardText, nickPalette, bgForNicks)) {
                setSpan(
                    ForegroundColorSpan(s.argb), s.start, s.endExcl,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    } else {
        cardText
    }
    val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(
            spannedText, 0, spannedText.length, textPaint, CARD_W - CARD_MARGIN * 2
        ).build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(
            spannedText, textPaint, CARD_W - CARD_MARGIN * 2,
            android.text.Layout.Alignment.ALIGN_NORMAL,
            1.35f, 0f, false
        )
    }
    // v1.61.20: the logo block sits just above the quote, top-anchored
    // right under the header (Clint: the centered group left too much
    // black above the logo). The canvas grows for long quotes so the
    // text is never cut off — the background/artwork extends with it.
    //
    // v1.88.33: SMS Xtract-ported themes put the Quote404 logo FIRST at
    // the very top of the image (Clint: it was rendering below the
    // theme's top chrome — Win95 title bar, Sega stripes at y=0 with the
    // logo at y~200 — which looked wrong), then the theme chrome, then
    // the quote text. Designed styles keep the established layout.
    val logoBlockH =
        if (includeLogo && watermark != null && !watermark.isRecycled)
            quote404LogoBlockH(CARD_W.toFloat()) else 0f
    val groupGap = if (logoBlockH > 0f) 56f else 0f
    val contentTop = top + 30f
    val isTxtract = style.category == StyleCategory.TXTRACT
    // v1.88.33: narrower logo on TXTRACT cards so the quote number stays
    // clear of it in the top-right corner.
    val txtractLogoFrac = 0.6f
    val txtractLogoBlockH =
        if (isTxtract && includeLogo && watermark != null && !watermark.isRecycled)
            quote404LogoBlockH(CARD_W.toFloat(), txtractLogoFrac) else 0f
    val logoTopPad = 24f
    // The quote text rect is known before the bitmap exists — TXTRACT
    // themes may draw a surface (checkerboard frame, phone screen,
    // caption box, window client area) framing it.
    val chromeDy: Float
    val txtractTextRect: RectF
    val cardH: Int
    val groupTop: Float
    if (isTxtract) {
        val logoBottomT = logoTopPad + txtractLogoBlockH
        chromeDy = logoBottomT + 48f
        val textTopT = chromeDy + txtractChromeH(style) + 30f
        cardH = maxOf(CARD_H, (textTopT + layout.height + 260f).toInt())
        txtractTextRect = RectF(
            CARD_MARGIN.toFloat(), textTopT,
            (CARD_W - CARD_MARGIN).toFloat(), textTopT + layout.height
        )
        groupTop = logoTopPad
    } else {
        chromeDy = 0f
        txtractTextRect = RectF()
        val needed = logoBlockH + groupGap + layout.height
        cardH = maxOf(CARD_H, (contentTop + needed + 260f).toInt())
        groupTop = contentTop
    }

    val bitmap = Bitmap.createBitmap(
        CARD_W, cardH + quote404FooterH().toInt(), Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)

    if (artwork != null) {
        // v1.61: center-crop the artwork to the card, then a black scrim
        // so the quote text stays legible over it. v1.61.20: crops to the
        // grown canvas for long quotes.
        val scale = maxOf(
            CARD_W / artwork.width.toFloat(),
            cardH / artwork.height.toFloat()
        )
        val dw = artwork.width * scale
        val dh = artwork.height * scale
        val dx = (CARD_W - dw) / 2f
        val dy = (cardH - dh) / 2f
        canvas.drawBitmap(
            artwork, null,
            RectF(dx, dy, dx + dw, dy + dh),
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        canvas.drawRect(
            0f, 0f, CARD_W.toFloat(), cardH.toFloat(),
            // v1.61.17: lightened (was 140) so more of Clint's art shows
            // through — the text panel below still carries readability.
            Paint().apply { color = android.graphics.Color.argb(115, 0, 0, 0) }
        )
    } else {
        // Gradient background.
        val bg = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, cardH.toFloat(),
                style.bgTop.toInt(), style.bgBottom.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, CARD_W.toFloat(), cardH.toFloat(), bg)
        // v1.88.32: the SMS Xtract ports get one signature drawn detail
        // each (behind the quote text) so they're more than a flat
        // gradient.
        drawTxtractCardDetails(
            canvas, style, cardH.toFloat(), chromeDy, txtractTextRect, quote.number
        )
    }

    // Header text (drawn now that the canvas exists).
    // v1.88.33: on TXTRACT cards the number/rating move to the top-right
    // corner, clear of the centered logo (which now sits at the very
    // top); designed cards keep the established header.
    if (isTxtract) {
        if (includeNumber) {
            val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = style.accent.toInt()
                textSize = 52f
                isFakeBoldText = true
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(
                "#${quote.number}", CARD_W - 40f, logoTopPad + 72f, numPaint
            )
        }
        if (includeRating && quote.rating != null) {
            val ratingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = style.accent.toInt()
                textSize = 40f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(
                "Rating ${quote.rating}", CARD_W - 40f, logoTopPad + 126f, ratingPaint
            )
        }
    } else {
        var headerY = 170f
        if (includeNumber) {
            val numText = "#${quote.number}"
            canvas.drawText(
                numText,
                CARD_W - CARD_MARGIN - accentPaint.measureText(numText),
                headerY, accentPaint
            )
            headerY += 110f
        }
        if (includeRating && quote.rating != null) {
            val ratingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = style.accent.toInt()
                textSize = 48f
            }
            val ratingText = "Rating ${quote.rating}"
            canvas.drawText(
                ratingText,
                CARD_W - CARD_MARGIN - ratingPaint.measureText(ratingText),
                headerY, ratingPaint
            )
        }
    }
    // v1.88.33: TXTRACT cards draw the logo at the very top, then theme
    // chrome, then the text rect computed above.
    val textTop: Float
    if (isTxtract) {
        if (txtractLogoBlockH > 0f) {
            canvas.drawQuote404LogoOnArt(watermark, logoTopPad, txtractLogoFrac)
        }
        textTop = txtractTextRect.top
    } else {
        val logoBottom =
            if (logoBlockH > 0f) canvas.drawQuote404LogoOnArt(watermark, groupTop)
            else groupTop
        textTop = logoBottom + groupGap
    }
    // v1.61.11: on artwork backgrounds a dark panel sits behind the text
    // block so the quote stays readable over bright art (the old
    // full-card scrim wasn't enough where the logo glows). v1.61.17:
    // lightened (was 175) so more of the art shows through — white text
    // still reads fine over it.
    if (artwork != null) {
        canvas.drawRoundRect(
            RectF(
                CARD_MARGIN.toFloat() - 28f, textTop - 34f,
                (CARD_W - CARD_MARGIN).toFloat() + 28f,
                textTop + layout.height + 34f
            ),
            28f, 28f,
            Paint().apply {
                color = android.graphics.Color.argb(140, 0, 0, 0)
            }
        )
    }
    canvas.save()
    canvas.translate(CARD_MARGIN.toFloat(), textTop)
    layout.draw(canvas)
    canvas.restore()

    // Optional URL footer above the watermark.
    if (includeUrl && quote.url.isNotBlank()) {
        val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.textColor.toInt()
            textSize = 34f
            alpha = 220
        }
        val maxW = (CARD_W - CARD_MARGIN * 2).toFloat()
        var display = quote.url.trim()
        while (urlPaint.measureText(display) > maxW && display.length > 12) {
            display = display.dropLast(1)
        }
        if (display.length < quote.url.trim().length) display += "\u2026"
        canvas.drawText(
            display,
            CARD_MARGIN.toFloat(),
            (cardH - 170).toFloat(),
            urlPaint
        )
    }

    // v1.61.7: footer — the source database bottom-left. (The Quote404
    // wordmark moved into the logo strip at the top in v1.61.11.)
    val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.accent.toInt()
        textSize = 36f
        alpha = 200
    }
    if (!sourceLabel.isNullOrBlank()) {
        canvas.drawText(
            sourceLabel,
            CARD_MARGIN.toFloat(),
            (cardH - 90).toFloat(),
            markPaint
        )
    }
    // v1.88.32: the slim Quote404 site footer under the card — same
    // black as the top logo plate, www.Quote404.com bottom-right.
    canvas.drawQuote404Footer(cardH.toFloat(), CARD_W.toFloat())
    return bitmap
}

/** Renders the card, then shares the PNG. Call from a coroutine scope. */
/**
 * v1.61.16: writes [bitmap] as a PNG into the share cache and fires the
 * system share sheet for it. The bitmap is recycled after writing.
 * Throws on failure — callers report it.
 */
suspend fun shareBitmap(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
    chooserTitle: String
) {
    val uri = withContext(Dispatchers.IO) {
        try {
            val dir = File(context.cacheDir, "quote_cards").apply { mkdirs() }
            val file = File(dir, fileName)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context, "com.quotebrowser.app.fileprovider", file
            )
        } finally {
            bitmap.recycle()
        }
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle))
}

/**
 * v1.61.16: renders the designed share card bitmap (artwork decoded and
 * recycled here). The share sheet and the share preview both render
 * through this so the preview shows exactly what gets shared.
 */
suspend fun renderQuoteCardForShare(
    context: Context,
    quote: Quote,
    style: QuoteCardStyle,
    includeNumber: Boolean = true,
    includeRating: Boolean = false,
    includeUrl: Boolean = false,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    devXenCustomArt: Bitmap? = null,
    sourceLabel: String? = null,
    colorizeNicks: Boolean = false,
    nickPalette: NickPalette? = null,
    /**
     * v1.88.31: false skips the Quote404 logo block so the image is just
     * the card; the header is always drawn on share/export images.
     */
    includeHeader: Boolean = true
): Bitmap = withContext(Dispatchers.IO) {
    // v1.61: DevXen cards render over Clint's theme artwork.
    // v1.61.3: the user's own picture replaces the bundled art.
    // v1.61.5: Quote404 cards render over Clint's new Quote404 art.
    val art = if (style == QuoteCardStyle.DEVXEN) {
        devXenCustomArt ?: try {
            BitmapFactory.decodeResource(context.resources, R.drawable.devxen_bg)
        } catch (e: Exception) { null }
    } else if (style == QuoteCardStyle.QUOTE404) {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.quote404_bg)
        } catch (e: Exception) { null }
    } else null
    try {
        // v1.61.11: Clint's Quote404 logo for the top branding strip.
        val watermark = quote404WatermarkLogo(context)
        renderQuoteCard(
            quote, style,
            includeNumber = includeNumber,
            includeRating = includeRating,
            includeUrl = includeUrl,
            ircLineBreaks = ircLineBreaks,
            dialogueLineBreaks = dialogueLineBreaks,
            artwork = art,
            sourceLabel = sourceLabel,
            watermark = watermark,
            colorizeNicks = colorizeNicks,
            nickPalette = nickPalette,
            includeLogo = includeHeader
        )
    } finally {
        // v1.61.3: only recycle artwork we decoded ourselves — the
        // custom background bitmap is shared app-wide.
        if (devXenCustomArt == null) art?.recycle()
    }
}

suspend fun renderAndShareQuoteCard(
    context: Context,
    quote: Quote,
    style: QuoteCardStyle,
    includeNumber: Boolean = true,
    includeRating: Boolean = false,
    includeUrl: Boolean = false,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    // v1.61.3: the user's own DevXen background picture (null = bundled art).
    devXenCustomArt: Bitmap? = null,
    /** v1.61.7: the source database label for the card footer. */
    sourceLabel: String? = null,
    /** v1.61.11: nick colorization for the share card. */
    colorizeNicks: Boolean = false,
    nickPalette: NickPalette? = null
) {
    try {
        val rendered = renderQuoteCardForShare(
            context, quote, style,
            includeNumber = includeNumber,
            includeRating = includeRating,
            includeUrl = includeUrl,
            ircLineBreaks = ircLineBreaks,
            dialogueLineBreaks = dialogueLineBreaks,
            devXenCustomArt = devXenCustomArt,
            sourceLabel = sourceLabel,
            colorizeNicks = colorizeNicks,
            nickPalette = nickPalette
        )
        shareBitmap(
            context, rendered,
            "quote-${quote.number}-${style.name.lowercase()}.png",
            "Share quote card"
        )
    } catch (e: Exception) {
        // v1.61: never report cancellation as a render failure.
        if (e is CancellationException) throw e
        Toast.makeText(context, "Couldn't render card", Toast.LENGTH_SHORT).show()
    }
}

/** Renders a shareable reading-stats card (1080x1350 PNG). */
fun renderStatsCard(
    totalSeen: Int,
    streak: Int,
    stats: List<DbReadingStat>,
    style: QuoteCardStyle = QuoteCardStyle.MIDNIGHT,
    /** Quotes read per local day ("YYYY-MM-DD" -> count); draws the heatmap. */
    heat: Map<String, Int> = emptyMap(),
    /**
     * v1.61.8: Clint's Quote404 logo. v1.61.15: drawn large, right on the
     * card background ([Canvas.drawQuote404LogoOnArt]) — no floating
     * strip. Null = no logo.
     */
    watermark: Bitmap? = null
): Bitmap {
    val bitmap = Bitmap.createBitmap(CARD_W, CARD_H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bg = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, CARD_H.toFloat(),
            style.bgTop.toInt(), style.bgBottom.toInt(),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, CARD_W.toFloat(), CARD_H.toFloat(), bg)

    // v1.61.15: the Quote404 logo right on the background, large.
    val logoBottom = canvas.drawQuote404LogoOnArt(watermark)

    var top = logoBottom + 80f
    val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.accent.toInt()
        textSize = 64f
        isFakeBoldText = true
    }
    canvas.drawText("READING STATS", CARD_MARGIN.toFloat(), top, accentPaint)
    top += 28f
    canvas.drawRect(
        CARD_MARGIN.toFloat(), top,
        (CARD_MARGIN + 120).toFloat(), top + 12f,
        Paint().apply { color = style.accent.toInt() }
    )
    top += 150f

    // Headline tiles.
    val bigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.textColor.toInt()
        textSize = 130f
        isFakeBoldText = true
    }
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.textColor.toInt()
        textSize = 40f
        alpha = 210
    }
    val half = CARD_W / 2f
    canvas.drawText(totalSeen.toString(), CARD_MARGIN.toFloat(), top, bigPaint)
    canvas.drawText("quotes read", CARD_MARGIN.toFloat(), top + 60f, labelPaint)
    canvas.drawText(streak.toString(), half, top, bigPaint)
    canvas.drawText("day streak", half, top + 60f, labelPaint)
    top += 200f

    // Reading-activity heatmap (last 18 weeks).
    if (heat.isNotEmpty()) {
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.accent.toInt()
            textSize = 44f
            isFakeBoldText = true
        }
        canvas.drawText("READING ACTIVITY", CARD_MARGIN.toFloat(), top, sectionPaint)
        top += 72f
        val weeks = 18
        val today = java.time.LocalDate.now()
        val gridStart = today.minusDays((weeks * 7 - 1).toLong())
        val cell = 36f
        val step = cell + 10f
        val maxCount = (heat.values.maxOrNull() ?: 0).coerceAtLeast(1)
        val emptyPaint = Paint().apply {
            color = style.textColor.toInt()
            alpha = 36
        }
        val fillPaint = Paint().apply { color = style.accent.toInt() }
        for (col in 0 until weeks) {
            for (row in 0 until 7) {
                val date = gridStart.plusDays((col * 7 + row).toLong())
                if (date.isAfter(today)) break
                val count = heat[date.toString()] ?: 0
                val x = CARD_MARGIN + col * step
                val y = top + row * step
                if (count == 0) {
                    canvas.drawRoundRect(x, y, x + cell, y + cell, 8f, 8f, emptyPaint)
                } else {
                    fillPaint.alpha =
                        (110 + 145 * count / maxCount).coerceIn(0, 255)
                    canvas.drawRoundRect(x, y, x + cell, y + cell, 8f, 8f, fillPaint)
                }
            }
        }
        top += 7 * step + 48f
    }

    // Per-database progress rows.
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.textColor.toInt()
        textSize = 46f
    }
    val countPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.accent.toInt()
        textSize = 46f
        isFakeBoldText = true
        textAlign = Paint.Align.RIGHT
    }
    val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.textColor.toInt()
        alpha = 40
    }
    val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = style.accent.toInt()
    }
    for (s in stats.take(6)) {
        if (top > CARD_H - 280) break
        var label = s.label
        while (namePaint.measureText(label) > CARD_W - CARD_MARGIN * 2 - 300 &&
            label.length > 8
        ) {
            label = label.dropLast(1)
        }
        if (label.length < s.label.length) label += "\u2026"
        canvas.drawText(label, CARD_MARGIN.toFloat(), top, namePaint)
        canvas.drawText(
            "${s.seen}/${s.total}",
            (CARD_W - CARD_MARGIN).toFloat(), top, countPaint
        )
        top += 26f
        val frac =
            if (s.total > 0) (s.seen.toFloat() / s.total).coerceIn(0f, 1f) else 0f
        val barW = (CARD_W - CARD_MARGIN * 2).toFloat()
        canvas.drawRoundRect(
            CARD_MARGIN.toFloat(), top,
            CARD_MARGIN + barW, top + 22f,
            11f, 11f, trackPaint
        )
        if (frac > 0f) {
            canvas.drawRoundRect(
                CARD_MARGIN.toFloat(), top,
                CARD_MARGIN + barW * frac, top + 22f,
                11f, 11f, barPaint
            )
        }
        top += 96f
    }

    // v1.61.7: footer — the Quote404 wordmark moved into the logo strip
    // at the top in v1.61.11.
    return bitmap
}

/** Renders the stats card, then shares the PNG. Call from a coroutine scope. */
suspend fun renderAndShareStatsCard(
    context: Context,
    totalSeen: Int,
    streak: Int,
    stats: List<DbReadingStat>,
    heat: Map<String, Int> = emptyMap()
) {
    try {
        val bitmap = withContext(Dispatchers.IO) {
            // v1.61.8: Clint's Quote404 logo watermark for the export.
            val watermark = quote404WatermarkLogo(context)
            val rendered = renderStatsCard(totalSeen, streak, stats, heat = heat, watermark = watermark)
            val dir = File(context.cacheDir, "quote_cards").apply { mkdirs() }
            val file = File(dir, "reading-stats.png")
            file.outputStream().use { out ->
                rendered.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            rendered.recycle()
            FileProvider.getUriForFile(
                context, "com.quotebrowser.app.fileprovider", file
            )
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, bitmap)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share reading stats"))
    } catch (e: Exception) {
        // v1.61: never report cancellation as a render failure.
        if (e is CancellationException) throw e
        Toast.makeText(context, "Couldn't render card", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Maximum quotes in one image strip: one card is 1080x1350, so 40 cards is
 * ~233 MB as a single ARGB bitmap. Capped to keep sizes sane.
 */
const val MAX_QUOTE_STRIP_QUOTES = 40
private const val STRIP_GAP_PX = 24

/**
 * Renders multiple quotes as one tall PNG strip (1080px wide): each quote
 * is rendered through the single-card [renderQuoteCard] path and the cards
 * are stacked vertically with a small divider gap between them.
 */
fun renderQuoteStrip(
    quotes: List<Quote>,
    style: QuoteCardStyle = QuoteCardStyle.MIDNIGHT,
    includeNumber: Boolean = true,
    includeRating: Boolean = false,
    includeUrl: Boolean = false,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    /**
     * v1.61.8: Clint's Quote404 logo, drawn as a faint centered watermark
     * on each card of the strip. Null = no watermark.
     */
    watermark: Bitmap? = null
): Bitmap {
    val items = quotes.take(MAX_QUOTE_STRIP_QUOTES)
    require(items.isNotEmpty()) { "renderQuoteStrip needs at least one quote" }
    // v1.61.15: one slim Quote404 logo plate tops the whole strip image,
    // flush against the first card — no floating strip.
    val logoPlateH = quote404LogoPlateH(watermark, CARD_W.toFloat())
    val totalH =
        (logoPlateH + items.size * CARD_H + (items.size - 1) * STRIP_GAP_PX).toInt()
    val bitmap = Bitmap.createBitmap(CARD_W, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawQuote404LogoPlate(watermark, plateH = logoPlateH)
    val gapPaint = Paint().apply { color = style.bgBottom.toInt() }
    val rulePaint = Paint().apply {
        color = style.accent.toInt()
        alpha = 160
    }
    var top = logoPlateH.toInt()
    items.forEachIndexed { index, quote ->
        // Each card is drawn then recycled immediately so peak memory is
        // the strip plus a single card.
        val card = renderQuoteCard(
            quote, style,
            includeNumber = includeNumber,
            includeRating = includeRating,
            includeUrl = includeUrl,
            ircLineBreaks = ircLineBreaks,
            dialogueLineBreaks = dialogueLineBreaks,
            watermark = watermark,
            // The strip image carries one logo plate at its own top.
            includeLogo = false
        )
        canvas.drawBitmap(card, 0f, top.toFloat(), null)
        card.recycle()
        top += CARD_H
        if (index < items.lastIndex) {
            canvas.drawRect(
                0f, top.toFloat(),
                CARD_W.toFloat(), (top + STRIP_GAP_PX).toFloat(),
                gapPaint
            )
            val cy = top + STRIP_GAP_PX / 2f
            canvas.drawRect(
                CARD_MARGIN.toFloat(), cy - 2f,
                (CARD_MARGIN + 120).toFloat(), cy + 2f,
                rulePaint
            )
            top += STRIP_GAP_PX
        }
    }
    return bitmap
}

/** Renders the strip, then shares the PNG. Call from a coroutine scope. */
suspend fun renderAndShareQuoteStrip(
    context: Context,
    quotes: List<Quote>,
    style: QuoteCardStyle = QuoteCardStyle.MIDNIGHT,
    includeNumber: Boolean = true,
    includeRating: Boolean = false,
    includeUrl: Boolean = false,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true
) {
    try {
        if (quotes.isEmpty()) {
            Toast.makeText(context, "No quotes to share", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = withContext(Dispatchers.IO) {
            // v1.61.8: Clint's Quote404 logo watermark for the export.
            val watermark = quote404WatermarkLogo(context)
            val rendered = renderQuoteStrip(
                quotes, style,
                includeNumber = includeNumber,
                includeRating = includeRating,
                includeUrl = includeUrl,
                ircLineBreaks = ircLineBreaks,
                dialogueLineBreaks = dialogueLineBreaks,
                watermark = watermark
            )
            val dir = File(context.cacheDir, "quote_cards").apply { mkdirs() }
            val file = File(dir, "quote-strip.png")
            file.outputStream().use { out ->
                rendered.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            rendered.recycle()
            FileProvider.getUriForFile(
                context, "com.quotebrowser.app.fileprovider", file
            )
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share quote strip"))
    } catch (e: Exception) {
        // v1.61: never report cancellation as a render failure.
        if (e is CancellationException) throw e
        Toast.makeText(context, "Couldn't render strip", Toast.LENGTH_SHORT).show()
    }
}

/** Feature gate for site-styled share cards (Settings → Features). */
val LocalSiteShareCardsEnabled = compositionLocalOf { true }

/**
 * v1.61.4: the "Quote theme" picker tile preview — a miniature of the
 * actual site-styled share card (site-colored header bar with the quote
 * number, a snippet of the real quote text, site-colored footer bar), so
 * the tile unmistakably shows what the export will look like.
 */
@Composable
private fun QuoteThemeTilePreview(
    siteStyle: SiteStyle,
    quote: Quote,
    /** v1.61.5: the effective per-database quote background, like the export. */
    bodyBg: Color,
    /** v1.61.5: the tile shows the number, rating, and web address exactly as the export will. */
    includeUrl: Boolean,
    colorizeNicks: Boolean,
    nickPalette: NickPalette,
    /** v1.61.11: the per-database "hide TLD" header option. */
    hideTldInHeader: Boolean = false
) {
    val bar = siteBarColor(siteStyle)
    val onBar = siteOnBarColor(siteStyle)
    val monospace = siteStyleMonospace(siteStyle)
    val darkBody = bodyBg.red * 0.299f + bodyBg.green * 0.587f +
        bodyBg.blue * 0.114f <= 0.5f
    val bodyTextColor = if (darkBody) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    // v1.61.5: the snippet colorizes nicks like the export when the toggle
    // is on (spans computed on the exact displayed snippet).
    val snippet = quote.text.take(90)
    val snippetAnnotated = remember(snippet, colorizeNicks, nickPalette, bodyBg) {
        buildAnnotatedString {
            append(snippet)
            if (colorizeNicks) {
                for (s in nickExportSpans(snippet, nickPalette, bodyBg)) {
                    addStyle(
                        SpanStyle(color = Color(s.argb)),
                        s.start.coerceIn(0, snippet.length),
                        s.endExcl.coerceIn(0, snippet.length)
                    )
                }
            }
        }
    }
    // v1.61.5: bash.org's vote line, like the export (gold number link,
    // green score).
    val voteAnnotated = remember(quote.number, quote.rating) {
        buildAnnotatedString {
            pushStyle(SpanStyle(
                color = Color(0xFFCD8C00),
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            ))
            append("#${quote.number}")
            pop()
            if (quote.rating != null) {
                pushStyle(SpanStyle(color = Color(0xFFCD8C00), fontWeight = FontWeight.Bold))
                append(" +")
                pop()
                pushStyle(SpanStyle(color = Color(0xFF3D7A3D)))
                append("(${quote.rating})")
                pop()
                pushStyle(SpanStyle(color = Color(0xFFCD8C00), fontWeight = FontWeight.Bold))
                append("-")
                pop()
            }
            // v1.61.11: the flag icon replaced "[X]" on the export.
            pushStyle(SpanStyle(color = Color(0xFFCD8C00), fontWeight = FontWeight.Bold))
            append(" \u2691\uFE0E")
            pop()
        }
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .aspectRatio(0.8f)
            .fillMaxWidth(0.28f)
            .background(Color.Black)
    ) {
        // v1.61.11: mini logo strip, like the export's top branding bar.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Quote404",
                color = Color(0xFF4DA6FF),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        // Header bar, like the export: the site title left, the quote
        // number right.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bar)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // v1.61.11: the real site title, like the export (the
                // Quote404 branding moved into the strip above).
                Text(
                    text = siteHeaderTitle(siteStyle, hideTldInHeader),
                    color = onBar,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontStyle = if (siteStyle == SiteStyle.BASH) FontStyle.Italic
                    else FontStyle.Normal,
                    maxLines = 1
                )
                if (siteStyle == SiteStyle.CLASSIC_QDB ||
                    siteStyle == SiteStyle.BASH
                ) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "Quote #${quote.number}",
                        color = onBar,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
        // Vote line, like the export.
        if (siteStyle == SiteStyle.BASH) {
            Text(
                text = voteAnnotated,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        // Web address, like the export's quiet URL line.
        if (includeUrl && quote.url.isNotBlank()) {
            Text(
                text = quote.url,
                color = Color(0xFF9A9A9A),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
        // Quote body snippet in the site body colors.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(4.dp)
                .background(bodyBg)
                .border(1.dp, siteInnerBorderColor(siteStyle, bar))
                .padding(4.dp)
        ) {
            Text(
                text = snippetAnnotated,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = if (monospace) FontFamily.Monospace
                    else FontFamily.Default,
                    // v1.61.5: readable on the effective background, same
                    // luminance rule as the export.
                    color = bodyTextColor
                )
            )
        }
        // Footer bar with the quote number, like the export.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bar)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = siteNumberLabel(siteStyle, quote.number),
                color = onBar,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

/**
 * v1.88.32: collapsible labeled section for the share style picker.
 * Sections start expanded; tapping the header row toggles the arrow.
 */
@Composable
private fun CollapsibleSection(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector =
                    if (expanded) Icons.Filled.ExpandLess
                    else Icons.Filled.ExpandMore,
                contentDescription =
                    if (expanded) "Collapse $title" else "Expand $title"
            )
        }
        if (expanded) content()
    }
}

/**
 * v1.88.32: one tile in the share picker's style sections — the tile
 * art plus its label; a tap previews the true export before sharing.
 */
@Composable
private fun ShareStyleTile(
    label: String,
    onPick: () -> Unit,
    // v1.88.32: per-theme nick palette override — a small palette button
    // on each tile opens the override picker for that card theme.
    // NOTE: tileArt is last so call sites can use the trailing-lambda
    // syntax for it while still passing onPaletteClick as a named arg.
    onPaletteClick: (() -> Unit)? = null,
    tileArt: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onPick)
    ) {
        tileArt()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (onPaletteClick != null) {
                IconButton(
                    onClick = onPaletteClick,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.Palette,
                        contentDescription = "Nick colors for $label",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * v1.88.32: chat themes with per-turn headers (AIM, ICQ, MSN) — the only
 * card themes eligible for "remove nicks from message text".
 */
private val NICK_STRIP_THEME_KEYS: Set<String> = setOf(
    SiteStyle.AIM,
    SiteStyle.ICQ,
    SiteStyle.MSN_MESSENGER
).map { it.noveltyCardThemeKey() }.toSet()

/**
 * v1.88.32: per-card-theme nick palette override picker. Offers "Use
 * global" (the default — every theme starts here) or any existing nick
 * color preset / custom palette, reusing the v1.63 preset system instead
 * of rebuilding a color editor.
 */
@Composable
private fun ThemeNickPaletteDialog(
    themeKey: String,
    themeLabel: String,
    onDismiss: () -> Unit
) {
    val store = LocalThemeNickPaletteStore.current
    val overrides by (store?.overrides
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, String>()) })
        .collectAsState()
    val customs by (store?.customThemes
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyList<NickColorTheme>()) })
        .collectAsState()
    val globalThemeId by (store?.globalThemeId
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(NICK_THEME_DEFAULT) })
        .collectAsState()
    // v1.88.32: "remove nicks from message text" per chat theme.
    val nickStripSet by (store?.nickStrip
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptySet<String>()) })
        .collectAsState()
    val current = overrides[themeKey]
    val options = remember(customs) { NickColorThemePresets + customs }
    val globalName = when {
        globalThemeId == NICK_THEME_APP -> "App theme (classic)"
        else -> options.firstOrNull { it.id == globalThemeId }?.name ?: "Quote404"
    }
    val globalColors =
        options.firstOrNull { it.id == globalThemeId }?.nameColors.orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nick colors — $themeLabel") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Give this card style its own nick color palette, " +
                        "or follow the global nick color theme.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // v1.88.32: "remove nicks from message text" — only for
                // the chat themes with per-turn headers (AIM, ICQ, MSN).
                // Off by default; strips the speaker prefix from each
                // message body while every turn keeps its own block,
                // header, and alternating color.
                if (themeKey in NICK_STRIP_THEME_KEYS) {
                    val stripOn = nickStripSet.contains(themeKey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                store?.setNickStrip?.invoke(themeKey, !stripOn)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Remove nicks from message text",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Strip the speaker prefix from each message — " +
                                    "the header already shows who said it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = stripOn,
                            onCheckedChange = {
                                store?.setNickStrip?.invoke(themeKey, it)
                            }
                        )
                    }
                }
                NickPaletteOptionRow(
                    selected = current == null,
                    title = "Use global ($globalName)",
                    colors = globalColors,
                    onClick = {
                        store?.setOverride?.invoke(themeKey, THEME_NICK_USE_GLOBAL)
                        onDismiss()
                    }
                )
                options.forEach { theme ->
                    NickPaletteOptionRow(
                        selected = current == theme.id,
                        title = theme.name + if (theme.isCustom) " (yours)" else "",
                        colors = theme.nameColors,
                        onClick = {
                            store?.setOverride?.invoke(themeKey, theme.id)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun NickPaletteOptionRow(
    selected: Boolean,
    title: String,
    colors: List<Int>,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (colors.isNotEmpty()) {
            NickThemeDots(colors = colors, max = 6)
        }
    }
}

/** Style picker shown before sharing a quote as a designed image. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuoteCardStyleDialog(
    quote: Quote,
    includeNumber: Boolean,
    includeRating: Boolean,
    includeUrl: Boolean,
    ircLineBreaks: Boolean,
    dialogueLineBreaks: Boolean = true,
    onDismiss: () -> Unit,
    dbId: String? = null,
    /**
     * v1.61: the share work must run in a scope that outlives this dialog —
     * the dialog's own rememberCoroutineScope() is cancelled the moment
     * onDismiss() disposes the picker, which used to cancel every render
     * mid-flight and report "Couldn't render card".
     */
    shareScope: CoroutineScope,
    /**
     * v1.75: export mode — when set, the preview's confirm button hands
     * the rendered bitmap + suggested filename to this callback instead
     * of opening the share sheet, so Export can offer the exact same
     * designed styles as Share. Ownership of the bitmap transfers to the
     * caller (it must recycle it).
     */
    onExportBitmap: ((Bitmap, String) -> Unit)? = null
) {
    val context = LocalContext.current
    val siteCardsOn = LocalSiteShareCardsEnabled.current
    val outsideBlack = LocalBlackOutsideQuotes.current
    // v1.55.1: database total for the shared site card footer.
    val shareDbTotal = LocalDbTotals.current[dbId]
    // v1.58: capture date for the shared site card footer line.
    val shareServedDate = dbId?.let { LocalServedDates.current["$it/${quote.number}"] }
    // v1.89.6: the rating format the quote's database carries in its
    // theme spec, when it declares one.
    val shareRatingFormat = dbId?.let {
        LocalDbThemeSnapshots.current.builtInDbRatingFormats[it]
    }
    // v1.53: offer the database's site theme as a card style alternative.
    // v1.61.11: the resolved theme is hoisted so the share renderers can
    // also read the per-database "hide TLD" header option.
    val shareResolved: ResolvedDbTheme? = if (siteCardsOn && dbId != null) {
        val snap = LocalDbThemeSnapshots.current
        resolveDbTheme(snap, dbId)
    } else {
        null
    }
    val siteStyle = if (shareResolved != null && !shareResolved.bypassed) {
        siteStyleFor(shareResolved.structureThemeId)
    } else {
        SiteStyle.MODERN_CARD
    }
    // v1.61.3: the user's own DevXen background picture, captured here in
    // composition so the click handlers below can use it (CompositionLocal
    // reads are not valid inside click callbacks).
    val devXenShareArt = LocalDevXenCustomBg.current?.asAndroidBitmap()
    // v1.61.5: the effective per-database "Quote background" behind the
    // quote body — the site theme's authentic color by default, the user's
    // explicit pick when set (Settings → database themes). The Quote theme
    // card and its preview both use it, like the on-screen site card.
    val shareQuoteBackground = if (dbId != null) {
        val snap = LocalDbThemeSnapshots.current
        quoteBackgroundForDb(resolveDbTheme(snap, dbId), dbId, snap)
    } else {
        siteBodyBackgroundColor(siteStyle)
    }
    // v1.61.5: nick colorization for the Quote theme card, "with the
    // option to do colorizations or not" — defaults to the global nick
    // coloring setting, like the on-screen site card.
    val shareNickPalette = LocalNickPalette.current
    var colorizeNicks by remember { mutableStateOf(shareNickPalette.enabled) }
    // v1.88.32: per-card-theme nick palette overrides. The store is
    // provided at the root (no ViewModel param on this dialog); every
    // theme starts on "Use global" until Clint picks an override.
    val nickStore = LocalThemeNickPaletteStore.current
    val themeNickOverrides by (nickStore?.overrides
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyMap<String, String>()) })
        .collectAsState()
    val customNickThemes by (nickStore?.customThemes
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptyList<NickColorTheme>()) })
        .collectAsState()
    val nickColorMessage by (nickStore?.colorMessageWithSpeaker
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(false) })
        .collectAsState()
    val globalNickThemeId by (nickStore?.globalThemeId
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(NICK_THEME_DEFAULT) })
        .collectAsState()
    // v1.88.32: "remove nicks from message text" per chat theme.
    val nickStripSet by (nickStore?.nickStrip
        ?: remember { kotlinx.coroutines.flow.MutableStateFlow(emptySet<String>()) })
        .collectAsState()
    // Effective palette for one card theme: its override, else global.
    fun paletteForTheme(themeKey: String): NickPalette =
        resolveThemeNickPaletteOverride(
            overrideId = themeNickOverrides[themeKey],
            global = shareNickPalette,
            customThemes = customNickThemes,
            colorMessageWithSpeaker = nickColorMessage
        )
    // (themeKey, label) for the override picker, or null when closed.
    var paletteDialogFor by remember { mutableStateOf<Pair<String, String>?>(null) }
    // v1.88.24: ambient composition locals for the novelty-theme export
    // path, so the exported card matches the on-screen one (text scale,
    // quote font, kid-safe, translation, nick palette…).
    val noveltyLocals = rememberNoveltyExportLocals()
    // v1.88.25: true miniature previews for the novelty theme tiles —
    // each theme's real card rendered once at tile width into a bitmap,
    // so the picker shows what the theme actually looks like instead of
    // a generic swatch. Rendered once per dialog open (re-rendered when
    // the nick-colorize toggle changes), recycled when the dialog closes.
    // v1.88.30: tiles render the ACTUAL quote being shared (not a fixed
    // sample) and show the WHOLE card scaled down — no cropping, so the
    // quote is never cut off. Renders run concurrently: sequential
    // renders left most tiles stuck on the "Ag" placeholder for seconds.
    val noveltyTileBitmaps = remember { mutableStateMapOf<SiteStyle, Bitmap>() }
    LaunchedEffect(quote, colorizeNicks, nickStripSet, themeNickOverrides) {
        NOVELTY_THEME_STYLES.map { style ->
            async {
                try {
                    val bmp = renderNoveltyCardBitmap(
                        context, quote, style,
                        widthPx = NOVELTY_TILE_PREVIEW_W,
                        includeNumber = true,
                        includeRating = false,
                        colorizeNicks = colorizeNicks,
                        // v1.88.32: tiles match the theme's palette
                        // override and the "remove nicks" toggle.
                        locals = noveltyLocals.copy(
                            nickPalette = paletteForTheme(
                                style.noveltyCardThemeKey()
                            )
                        ),
                        dbId = dbId,
                        stripNicks = nickStripSet.contains(
                            style.noveltyCardThemeKey()
                        ),
                        // v1.89.2: the Friends tile matches the quote's
                        // database palette (sunset vs wall).
                        structureThemeId = shareResolved?.structureThemeId
                    )
                    val old = noveltyTileBitmaps.put(style, bmp)
                    old?.recycle()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    // Tile keeps its swatch placeholder; the tap path still
                    // renders the true export on demand.
                }
            }
        }.awaitAll()
    }
    DisposableEffect(Unit) {
        onDispose {
            noveltyTileBitmaps.values.forEach { it.recycle() }
            noveltyTileBitmaps.clear()
        }
    }
    // v1.88.33: true miniature previews for the "From SMS Xtract" tiles
    // too (Clint: the gradient + "Ag" placeholders didn't show what the
    // theme looks like). Each style's real card renders via
    // renderQuoteCardForShare, then is downscaled to tile width with the
    // full-size bitmap recycled inside the worker. Renders run in chunks
    // of ~6 to bound transient memory (a full card is ~6MB).
    val txtractTileBitmaps = remember { mutableStateMapOf<QuoteCardStyle, Bitmap>() }
    val txtractStyles = remember {
        QuoteCardStyle.entries
            .filter { it.category == StyleCategory.TXTRACT }
            .sortedBy { it.title }
    }
    // v1.88.33: tiles sorted alphabetically within each section; the
    // default (Quote404) stays pinned first in Designed.
    val designedStyles = remember {
        QuoteCardStyle.entries
            .filter { it.category == StyleCategory.DESIGNED }
            .sortedWith(compareBy({ it != QuoteCardStyle.QUOTE404 }, { it.title }))
    }
    val noveltyStylesSorted = remember {
        NOVELTY_THEME_STYLES.sortedBy { siteDisplayName(it) }
    }
    LaunchedEffect(quote, colorizeNicks, themeNickOverrides) {
        txtractStyles.chunked(6).forEach { chunk ->
            chunk.map { style ->
                async {
                    try {
                        val full = renderQuoteCardForShare(
                            context, quote, style,
                            includeNumber = includeNumber,
                            includeRating = includeRating,
                            includeUrl = includeUrl,
                            ircLineBreaks = ircLineBreaks,
                            dialogueLineBreaks = dialogueLineBreaks,
                            devXenCustomArt = devXenShareArt,
                            sourceLabel = when {
                                dbId == DatabaseManager.MY_QUOTES_ID ->
                                    DatabaseManager.MY_QUOTES_LABEL
                                siteStyle != SiteStyle.MODERN_CARD ->
                                    siteDisplayName(siteStyle)
                                else -> null
                            },
                            colorizeNicks = colorizeNicks,
                            // v1.88.33: tiles reflect the theme's palette
                            // override, like the novelty tiles do.
                            nickPalette = paletteForTheme(style.cardThemeKey()),
                        )
                        val tileW = 300
                        val tileH = (tileW * full.height / full.width)
                            .coerceAtLeast(1)
                        val small = Bitmap.createScaledBitmap(
                            full, tileW, tileH, true
                        )
                        full.recycle()
                        val old = txtractTileBitmaps.put(style, small)
                        old?.recycle()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Tile keeps its gradient placeholder; the tap
                        // path still renders the true export on demand.
                    }
                }
            }.awaitAll()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            txtractTileBitmaps.values.forEach { it.recycle() }
            txtractTileBitmaps.clear()
        }
    }
    // v1.61.16: share preview — tapping a style tile renders the true
    // export bitmap first and shows it (Share / Back) so the card can be
    // checked before it's sent to anyone. The preview renders through
    // the same ForShare functions the share sheet uses, so what you see
    // is exactly what gets shared.
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBusy by remember { mutableStateOf(false) }
    var previewFileName by remember { mutableStateOf("") }
    // Guards a render that finishes after the preview was closed or
    // replaced: a stale bitmap is recycled, never shown.
    var previewGen by remember { mutableStateOf(0) }
    fun closePreview() {
        previewGen++
        previewBitmap?.recycle()
        previewBitmap = null
        previewBusy = false
    }
    fun showPreview(fileName: String, render: suspend () -> Bitmap) {
        previewGen++
        val gen = previewGen
        previewFileName = fileName
        previewBitmap = null
        previewBusy = true
        shareScope.launch {
            try {
                val bmp = render()
                if (gen == previewGen) {
                    previewBitmap = bmp
                } else {
                    bmp.recycle()
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Toast.makeText(context, "Couldn't render card", Toast.LENGTH_SHORT).show()
            } finally {
                if (gen == previewGen) previewBusy = false
            }
        }
    }
    // Dismissing the picker also drops any open preview.
    val dismissPicker = {
        closePreview()
        onDismiss()
    }
    AlertDialog(
        onDismissRequest = dismissPicker,
        // v1.75: export mode relabels the picker.
        title = { Text(if (onExportBitmap != null) "Export as image" else "Share as image") },
        text = {
            // v1.61: the style list scrolls — on narrow screens only a few
            // tiles fit and the rest were unreachable.
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pick a card style:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // v1.61.5: colorize nicknames like the on-screen card — or
                // leave them plain. v1.61.11: designed cards colorize nicks
                // too now, so the toggle always shows.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { colorizeNicks = !colorizeNicks }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = colorizeNicks,
                        onCheckedChange = { colorizeNicks = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Colorize nicknames",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                // v1.88.32: style tiles grouped into collapsible sections —
                // Site theme (this database's card), Designed, From SMS
                // Xtract, Novelty. Sections start expanded.
                if (siteStyle != SiteStyle.MODERN_CARD) {
                    CollapsibleSection("Site theme") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // v1.61.4: the quote's database site theme —
                            // a mini version of the actual site card, not
                            // a generic icon.
                            ShareStyleTile(
                                label = "Quote theme",
                                onPick = {
                                    // v1.61.16: render the true export first
                                    // and preview it — Share / Back in the
                                    // preview dialog — instead of sharing
                                    // blind. v1.88.24: novelty themes render
                                    // the real on-screen composables
                                    // off-screen, so the preview IS the card.
                                    showPreview("quote-${quote.number}-site.png") {
                                        if (siteStyle.isNoveltyTheme()) {
                                            renderNoveltyQuoteCard(
                                                context, quote, siteStyle,
                                                // The theme card always carries
                                                // the quote's number and rating —
                                                // they're part of the site
                                                // theme's look.
                                                includeNumber = true,
                                                includeRating = true,
                                                includeUrl = includeUrl,
                                                colorizeNicks = colorizeNicks,
                                                // v1.88.32: this card theme's
                                                // nick palette override, if any.
                                                locals = noveltyLocals.copy(
                                                    nickPalette = paletteForTheme(
                                                        CARD_THEME_KEY_SITE
                                                    )
                                                ),
                                                // v1.88.32: "remove nicks from
                                                // message text" for the chat
                                                // themes (keyed by the theme's
                                                // own novelty key).
                                                stripNicks = nickStripSet.contains(
                                                    siteStyle.noveltyCardThemeKey()
                                                ),
                                                dbId = dbId,
                                                // v1.89.2: dual-palette cards
                                                // (Friends) match the screen.
                                                structureThemeId =
                                                    shareResolved?.structureThemeId,
                                            )
                                        } else {
                                            renderSiteQuoteCardForShare(
                                                context, quote, siteStyle,
                                                // The theme card always carries the
                                                // quote's number and rating —
                                                // they're part of the site theme's
                                                // look.
                                                includeNumber = true,
                                                includeRating = true,
                                                includeUrl = includeUrl,
                                                ircLineBreaks = ircLineBreaks,
                                                dialogueLineBreaks = dialogueLineBreaks,
                                                outsideBlack = outsideBlack,
                                                dbTotal = shareDbTotal,
                                                servedDate = shareServedDate,
                                                quoteBackgroundArgb = shareQuoteBackground.toArgb(),
                                                colorizeNicks = colorizeNicks,
                                                // v1.88.32: this card theme's
                                                // nick palette override, if any.
                                                nickPalette = paletteForTheme(
                                                    CARD_THEME_KEY_SITE
                                                ),
                                                // v1.61.11: the per-database "hide
                                                // TLD" header option, so the export
                                                // title matches the on-screen card.
                                                hideTldInHeader = shareResolved?.hideTldInHeader
                                                    ?: false,
                                                // v1.89.6: the rating format
                                                // the database carries, so
                                                // the shared card's rating
                                                // text matches the screen.
                                                ratingFormat = shareRatingFormat,
                                            )
                                        }
                                    }
                                },
                                // v1.88.32: per-theme nick palette override.
                                onPaletteClick = {
                                    paletteDialogFor =
                                        CARD_THEME_KEY_SITE to "Quote theme"
                                }
                            ) {
                                QuoteThemeTilePreview(
                                    siteStyle, quote, shareQuoteBackground,
                                    includeUrl = includeUrl,
                                    colorizeNicks = colorizeNicks,
                                    nickPalette = paletteForTheme(CARD_THEME_KEY_SITE),
                                    hideTldInHeader = shareResolved?.hideTldInHeader
                                        ?: false
                                )
                            }
                        }
                    }
                }
                CollapsibleSection("Designed") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // v1.88.33: sorted alphabetically, Quote404 pinned
                        // first (designedStyles).
                        designedStyles
                            .forEach { style ->
                                ShareStyleTile(
                                    label = style.title,
                                    onPick = {
                                        // v1.61.16: preview the true export before
                                        // sharing — same as the Quote theme tile.
                                        showPreview("quote-${quote.number}-${style.name.lowercase()}.png") {
                                            renderQuoteCardForShare(
                                                context, quote, style,
                                                includeNumber = includeNumber,
                                                includeRating = includeRating,
                                                includeUrl = includeUrl,
                                                ircLineBreaks = ircLineBreaks,
                                                dialogueLineBreaks = dialogueLineBreaks,
                                                // v1.61.3: the user's own DevXen
                                                // background picture, if they set one.
                                                devXenCustomArt = devXenShareArt,
                                                // v1.61.7: the source database in the
                                                // card footer.
                                                sourceLabel = when {
                                                    dbId == DatabaseManager.MY_QUOTES_ID ->
                                                        DatabaseManager.MY_QUOTES_LABEL
                                                    siteStyle != SiteStyle.MODERN_CARD ->
                                                        siteDisplayName(siteStyle)
                                                    else -> null
                                                },
                                                // v1.61.11: nick colorization works
                                                // on designed cards too now.
                                                // v1.88.32: this card theme's
                                                // nick palette override, if any.
                                                colorizeNicks = colorizeNicks,
                                                nickPalette = paletteForTheme(
                                                    style.cardThemeKey()
                                                ),
                                            )
                                        }
                                    },
                                    // v1.88.32: per-theme nick palette override.
                                    onPaletteClick = {
                                        paletteDialogFor =
                                            style.cardThemeKey() to style.title
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .aspectRatio(0.8f)
                                            .fillMaxWidth(0.28f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (style == QuoteCardStyle.DEVXEN || style == QuoteCardStyle.QUOTE404) {
                                            // v1.61: the tile shows Clint's DevXen
                                            // artwork, dimmed like the real card.
                                            // v1.61.3: the user's own picture replaces
                                            // the bundled art here too.
                                            // v1.61.5: the Quote404 tile shows his
                                            // new Quote404 artwork.
                                            val artRes = if (style == QuoteCardStyle.QUOTE404)
                                                R.drawable.quote404_bg else R.drawable.devxen_bg
                                            val customTileBg = LocalDevXenCustomBg.current
                                            if (customTileBg != null && style == QuoteCardStyle.DEVXEN) {
                                                Image(
                                                    bitmap = customTileBg,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.matchParentSize()
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = artRes),
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.matchParentSize()
                                                )
                                            }
                                            Box(
                                                Modifier
                                                    .matchParentSize()
                                                    .background(Color.Black.copy(alpha = 0.55f))
                                            )
                                        } else {
                                            Box(
                                                Modifier
                                                    .matchParentSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color(style.bgTop),
                                                                Color(style.bgBottom)
                                                            )
                                                        )
                                                    )
                                            )
                                        }
                                        Text(
                                            text = "Ag",
                                            color = Color(style.textColor),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                            }
                    }
                }
                CollapsibleSection("From SMS Xtract") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // v1.88.33: sorted alphabetically (txtractStyles).
                        txtractStyles
                            .forEach { style ->
                                ShareStyleTile(
                                    label = style.title,
                                    onPick = {
                                        // v1.61.16: preview the true export before
                                        // sharing — same as the Quote theme tile.
                                        showPreview("quote-${quote.number}-${style.name.lowercase()}.png") {
                                            renderQuoteCardForShare(
                                                context, quote, style,
                                                includeNumber = includeNumber,
                                                includeRating = includeRating,
                                                includeUrl = includeUrl,
                                                ircLineBreaks = ircLineBreaks,
                                                dialogueLineBreaks = dialogueLineBreaks,
                                                // v1.61.3: the user's own DevXen
                                                // background picture, if they set one.
                                                devXenCustomArt = devXenShareArt,
                                                // v1.61.7: the source database in the
                                                // card footer.
                                                sourceLabel = when {
                                                    dbId == DatabaseManager.MY_QUOTES_ID ->
                                                        DatabaseManager.MY_QUOTES_LABEL
                                                    siteStyle != SiteStyle.MODERN_CARD ->
                                                        siteDisplayName(siteStyle)
                                                    else -> null
                                                },
                                                // v1.61.11: nick colorization works
                                                // on designed cards too now.
                                                // v1.88.32: this card theme's
                                                // nick palette override, if any.
                                                colorizeNicks = colorizeNicks,
                                                nickPalette = paletteForTheme(
                                                    style.cardThemeKey()
                                                ),
                                            )
                                        }
                                    },
                                    // v1.88.32: per-theme nick palette override.
                                    onPaletteClick = {
                                        paletteDialogFor =
                                            style.cardThemeKey() to style.title
                                    }
                                ) {
                                    // v1.88.33: the tile shows the theme's
                                    // true card scaled down (rendered once at
                                    // dialog open), with the gradient swatch
                                    // as the loading fallback.
                                    val tileBmp = txtractTileBitmaps[style]
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .aspectRatio(0.74f)
                                            .fillMaxWidth(0.28f),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (tileBmp != null) {
                                            Image(
                                                bitmap = tileBmp.asImageBitmap(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(4.dp)
                                            )
                                        } else {
                                            Box(
                                                Modifier
                                                    .matchParentSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color(style.bgTop),
                                                                Color(style.bgBottom)
                                                            )
                                                        )
                                                    )
                                            )
                                            Text(
                                                text = "Ag",
                                                color = Color(style.textColor),
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
                CollapsibleSection("Novelty") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // v1.88.33: sorted alphabetically by display name.
                        noveltyStylesSorted.forEach { noveltyStyle ->
                            // v1.88.25: the tile shows a true miniature of the
                            // theme's real card (rendered once at dialog open),
                            // falling back to the swatch glyph while it loads.
                            val (tileBg, tileFg, tileGlyph) = noveltyTileSwatch(noveltyStyle)
                            val tileBmp = noveltyTileBitmaps[noveltyStyle]
                            ShareStyleTile(
                                label = siteDisplayName(noveltyStyle),
                                onPick = {
                                    showPreview(
                                        "quote-${quote.number}-${
                                            noveltyStyle.name.lowercase()
                                        }.png"
                                    ) {
                                        renderNoveltyQuoteCard(
                                            context, quote, noveltyStyle,
                                            includeNumber = includeNumber,
                                            includeRating = includeRating,
                                            includeUrl = includeUrl,
                                            colorizeNicks = colorizeNicks,
                                            // v1.88.32: "remove nicks from
                                            // message text" for the chat themes.
                                            stripNicks = nickStripSet.contains(
                                                noveltyStyle.noveltyCardThemeKey()
                                            ),
                                            // v1.88.32: this card theme's
                                            // nick palette override, if any.
                                            locals = noveltyLocals.copy(
                                                nickPalette = paletteForTheme(
                                                    noveltyStyle.noveltyCardThemeKey()
                                                )
                                            ),
                                            dbId = dbId,
                                        )
                                    }
                                },
                                // v1.88.32: per-theme nick palette override.
                                onPaletteClick = {
                                    paletteDialogFor =
                                        noveltyStyle.noveltyCardThemeKey() to
                                            siteDisplayName(noveltyStyle)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .aspectRatio(1f)
                                        .fillMaxWidth(0.28f)
                                        .background(Color(tileBg)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (tileBmp != null) {
                                        Image(
                                            bitmap = tileBmp.asImageBitmap(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp)
                                        )
                                    } else {
                                        Text(
                                            text = tileGlyph,
                                            color = Color(tileFg),
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = dismissPicker) { Text("Cancel") }
        }
    )
    // v1.88.32: per-card-theme nick palette override picker.
    paletteDialogFor?.let { (themeKey, themeLabel) ->
        ThemeNickPaletteDialog(
            themeKey = themeKey,
            themeLabel = themeLabel,
            onDismiss = { paletteDialogFor = null }
        )
    }
    // v1.61.16: the true export preview — the actual rendered bitmap, so
    // the card can be checked before it's sent to anyone. Share hands the
    // rendered bitmap to the share sheet (no second render); Back returns
    // to the style picker.
    if (previewBusy || previewBitmap != null) {
        AlertDialog(
            onDismissRequest = { closePreview() },
            title = { Text("Preview") },
            text = {
                if (previewBusy) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    previewBitmap?.let { bmp ->
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Share card preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val bmp = previewBitmap
                        val name = previewFileName
                        previewBitmap = null
                        previewBusy = false
                        previewGen++
                        if (onExportBitmap != null) {
                            // v1.75 export mode: hand the rendered bitmap to
                            // the caller instead of sharing it.
                            if (bmp != null) onExportBitmap(bmp, name)
                            onDismiss()
                        } else {
                            shareScope.launch {
                                try {
                                    if (bmp != null) {
                                        shareBitmap(
                                            context, bmp, name,
                                            "Share quote card"
                                        )
                                    }
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                    Toast.makeText(
                                        context, "Couldn't share card",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            onDismiss()
                        }
                    },
                    enabled = previewBitmap != null
                ) { Text(if (onExportBitmap != null) "Save" else "Share") }
            },
            dismissButton = {
                TextButton(onClick = { closePreview() }) { Text("Back") }
            }
        )
    }
}

/**
 * Rate-as-you-read: a compact, low-key 5-star tap row rendered under the
 * quote text on quote cards. Filled stars show the user's rating;
 * tapping star N sets the rating to N, and tapping the currently-selected
 * star clears it back to unrated. Each star consumes its own click so
 * card gestures (single/double-tap, swipe) are unaffected.
 */
@Composable
fun PersonalRatingStars(
    store: PersonalRatings,
    dbId: String,
    number: Int,
    modifier: Modifier = Modifier
) {
    var stars by remember(dbId, number) { mutableStateOf(store.ratingFor(dbId, number)) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (n in 1..5) {
            val selected = n <= stars
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        val new = if (n == stars) 0 else n
                        store.setRating(dbId, number, new)
                        stars = new
                    }
            ) {
                Icon(
                    imageVector = if (selected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (n == stars) "Clear personal rating"
                    else "Rate $n star" + if (n > 1) "s" else "",
                    tint = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * v1.53 site-styled share card: renders the quote as a mini version of its
 * database's original site — site-colored header bar (quote number +
 * rating), bordered quote body, site-colored footer bar.
 */
fun renderSiteQuoteCard(
    quote: Quote,
    siteStyle: SiteStyle,
    includeNumber: Boolean = true,
    includeRating: Boolean = true,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    /** Mirrors the on-screen "black background outside quotes" option. */
    outsideBlack: Boolean = false,
    /**
     * v1.55.1: the database's total quote count for the footer
     * ("21,096 quotes"). Never shows pending/queued counts.
     */
    dbTotal: Int? = null,
    /**
     * v1.58: per-quote capture date ("YYYYMMDD") for the quiet footer
     * line, matching the on-screen site card. Null = no date line.
     */
    servedDate: String? = null,
    /**
     * v1.61.5: the effective per-database "Quote background" theme slot
     * (Settings → database themes), honoring the user's pick. Null keeps
     * the site theme's built-in body color.
     */
    quoteBackgroundArgb: Int? = null,
    /** v1.61.5: draw the quote's web address as a quiet line under the header. */
    includeUrl: Boolean = false,
    /**
     * v1.61.5: colorize nicknames in the body like the on-screen site
     * card, using [nickPalette]. The picker and share dialog expose this
     * as a toggle ("with the option to do colorizations or not").
     */
    colorizeNicks: Boolean = false,
    nickPalette: NickPalette? = null,
    /**
     * v1.61.8: Clint's Quote404 logo. v1.61.15: drawn in the slim logo
     * plate flush against the header — large enough to read. Null = no
     * plate.
     */
    watermark: Bitmap? = null,
    /**
     * v1.61.11: the per-database "hide TLD in header" option, so the
     * export title matches the on-screen quote card.
     */
    hideTldInHeader: Boolean = false,
    /**
     * v1.89.6: the rating format carried by the quote's database, when
     * it declares one; null keeps the built-in per-style format (the
     * exported rating text matches the on-screen card either way).
     */
    ratingFormat: String? = null
): Bitmap {
    // v1.61.15: slim Quote404 logo plate flush against the header — no
    // floating strip — with the logo large enough to read. The card goes
    // full-bleed like the on-screen site card: no page margins.
    // v1.61.19: no vector tagline (the wordmark crop already shows the
    // tagline baked into the logo art — the second line was a duplicate),
    // logo a touch bigger, and a slimmer plate so the black area is smaller.
    // v1.74.7: bigger again (0.78) and slimmer (10dp pad) — Clint: "way
    // too much black area on top" and "the logo at the top needs to be a
    // bit bigger". The tighter asset crop carries most of the change.
    val logoPlateH = quote404LogoPlateH(
        watermark, CARD_W.toFloat(),
        widthFrac = 0.78f, vPad = 10f, drawTagline = false
    )
    val bar = siteBarColor(siteStyle).toArgb()
    val onBar = siteOnBarColor(siteStyle).toArgb()
    val bodyBg = quoteBackgroundArgb ?: siteBodyBackgroundColor(siteStyle).toArgb()
    val monospace = siteStyleMonospace(siteStyle)
    // Body text: dark on light backgrounds, light on dark ones — from the
    // effective background's luminance, same rule as contentOn().
    val bgR = android.graphics.Color.red(bodyBg) / 255f
    val bgG = android.graphics.Color.green(bodyBg) / 255f
    val bgB = android.graphics.Color.blue(bodyBg) / 255f
    val darkBody = quoteBackgroundArgb?.let {
        (bgR * 0.299f + bgG * 0.587f + bgB * 0.114f) <= 0.5f
    } ?: (siteStyle == SiteStyle.CRAPSHACK)
    val bodyText = if (darkBody) 0xFFE0E0E0.toInt() else 0xFF1A1A1A.toInt()

    // v1.61.13: full-bleed like the on-screen site card — no page
    // margins. Bash.org is borderless like the original site; every
    // other style gets its thin border just inside the card edges,
    // mirroring the on-screen body box.
    val borderless = siteStyle == SiteStyle.BASH
    val boxInsetX = if (borderless) 0f else 30f
    // v1.61.15: the header bar was 180px tall — nearly twice the
    // on-screen card's header — so it read as "extended". 110px matches
    // the on-screen proportions; the Quote404 logo strip sits above it.
    val headerH = 110f
    val footerH = 110f
    val bodyPad = 44f

    val cardText = applyDisplayBreaks(quote.text, ircLineBreaks, dialogueLineBreaks)
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bodyText
        textSize = 46f
        typeface = if (monospace) android.graphics.Typeface.MONOSPACE
        else android.graphics.Typeface.DEFAULT
    }
    // v1.61.5: optional nick colorization, mirroring the on-screen site
    // card (same palette, contrast-adjusted to the body background).
    val spannedText: CharSequence = if (colorizeNicks && nickPalette != null) {
        SpannableString(cardText).apply {
            for (s in nickExportSpans(cardText, nickPalette, Color(bodyBg))) {
                setSpan(
                    ForegroundColorSpan(s.argb), s.start, s.endExcl,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    } else {
        cardText
    }
    val textW = (CARD_W - boxInsetX * 2 - bodyPad * 2).toInt()
    val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(spannedText, 0, spannedText.length, textPaint, textW).build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(
            spannedText, textPaint, textW,
            android.text.Layout.Alignment.ALIGN_NORMAL, 1.3f, 0f, false
        )
    }
    val bodyH = layout.height + bodyPad * 2
    // v1.61.5: strips between the header bar and the body — bash.org's
    // "#N +(score)- [X]" vote line, qdb.us's quote line, and the optional
    // web-address line. Measured before totalH (the qdb.us line used to
    // overflow the bitmap and clip the footer).
    val bashVoteLineH =
        if (siteStyle == SiteStyle.BASH &&
            (includeNumber || (includeRating && quote.rating != null))
        ) 90f else 0f
    val classicQuoteLineH =
        if (siteStyle == SiteStyle.CLASSIC_QDB && includeRating && quote.rating != null)
            90f else 0f
    val urlLineH = if (includeUrl && quote.url.isNotBlank()) 76f else 0f
    val stripsH = bashVoteLineH + classicQuoteLineH + urlLineH
    // v1.88.32: the slim Quote404 site footer under the card (same
    // black as the top logo plate, www.Quote404.com bottom-right).
    val qFooterH = quote404FooterH()
    val totalH = (logoPlateH + headerH + stripsH + bodyH + footerH + qFooterH).toInt()

    val bitmap = Bitmap.createBitmap(CARD_W, totalH, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    // Page base color FIRST: drawColor fills the whole canvas, so it has
    // to go down before the logo plate. v1.61.16: it used to run after
    // the plate and painted black right over the logo — which is why the
    // site-theme exports never showed it.
    val pageColor = when {
        outsideBlack -> 0xFF000000
        siteStyle == SiteStyle.CLASSIC_QDB -> 0xFFFFFFFF
        else -> 0xFF000000
    }
    canvas.drawColor(pageColor.toInt())
    // v1.61.15: the Quote404 logo plate across the top, flush against
    // the header; everything else shifts down by its height.
    // v1.61.19: bigger logo, no duplicate tagline, slimmer plate.
    // v1.74.7: matches the layout call above (0.78 / 10dp).
    canvas.drawQuote404LogoPlate(
        watermark, widthFrac = 0.78f, vPad = 10f,
        plateH = logoPlateH, drawTagline = false
    )
    canvas.save()
    canvas.translate(0f, logoPlateH)
    // Page base color under the full-bleed card (no margins show, but
    // the base keeps overdraw clean). outsideBlack is kept for API
    // compatibility — with the full-bleed layout there is no "outside"
    // left to paint.

    val barPaint = Paint().apply { color = bar }
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = onBar
        textSize = 56f
        isFakeBoldText = true
        typeface = when (siteStyle) {
            SiteStyle.CLASSIC_QDB ->
                android.graphics.Typeface.create(
                    android.graphics.Typeface.SERIF,
                    android.graphics.Typeface.BOLD_ITALIC
                )
            // v1.61.5: bash.org's italic "QDB" wordmark, like the site.
            SiteStyle.BASH ->
                android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD_ITALIC
                )
            else -> android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = onBar
        textSize = 44f
    }

    // v1.61.13: header bar full-bleed — the site's own title left (like
    // the on-screen quote card), the quote number right. The Quote404
    // branding lives in the logo strip above.
    canvas.drawRect(0f, 0f, CARD_W.toFloat(), headerH, barPaint)
    // v1.61.11: the real site name, honoring the per-database "hide TLD"
    // header option — "Bash.org", not "QDB".
    // v1.61.15: baselines recentered for the shorter 110px header.
    val headerTitle = siteHeaderTitle(siteStyle, hideTldInHeader)
    canvas.drawText(headerTitle, 36f, 75f, headerPaint)
    val headerRight = when (siteStyle) {
        // v1.61.5: bash.org's header pairs the site name with
        // "Quote #N" at the right, like the on-screen site card (the
        // score moves to the vote line below).
        SiteStyle.CLASSIC_QDB, SiteStyle.BASH ->
            if (includeNumber) "Quote #${quote.number}" else null
        else -> buildString {
            if (includeNumber) append(siteNumberLabel(siteStyle, quote.number))
            val rl = siteRatingLabel(siteStyle, quote.rating, ratingFormat)
            if (includeRating && rl != null) {
                if (isNotEmpty()) append("  ")
                append(rl)
            }
        }.ifEmpty { null }
    }
    if (headerRight != null) {
        canvas.drawText(
            headerRight, CARD_W - 36f - subPaint.measureText(headerRight),
            71f, subPaint
        )
    }

    // qdb.us quote line: #N (up/down) with the number in the blue link
    // color and the score in plain black, like the original site.
    // v1.61.13: full-bleed on the body background, like the on-screen card.
    var bodyTop = headerH
    val stripsBgPaint = Paint().apply { color = bodyBg }
    if (siteStyle == SiteStyle.CLASSIC_QDB && includeRating && quote.rating != null) {
        val r = quote.rating
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF8098B0.toInt()
            textSize = 44f
            isFakeBoldText = true
        }
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF000000.toInt()
            textSize = 44f
            isFakeBoldText = true
        }
        val num = "#${quote.number} "
        val score = if (r >= 0) "(+$r)" else "($r)"
        canvas.drawRect(0f, bodyTop, CARD_W.toFloat(), bodyTop + 90f, stripsBgPaint)
        canvas.drawText(num, 36f, bodyTop + 60f, linePaint)
        canvas.drawText(score, 36f + linePaint.measureText(num), bodyTop + 60f, scorePaint)
        bodyTop += 90f
    }
    // v1.61.5: bash.org's iconic vote line, like the on-screen site card
    // and the original site — number in site gold (underlined link),
    // vote links hugging the green parenthesized score.
    // v1.61.11: the flag icon replaces "[X]" — reporting is a flag tap
    // on the on-screen card now.
    if (bashVoteLineH > 0f) {
        val gold = 0xFFCD8C00.toInt()
        // v1.61.13: the vote line sits on the body background full-bleed,
        // like the on-screen card — not on the page color.
        canvas.drawRect(0f, bodyTop, CARD_W.toFloat(), bodyTop + bashVoteLineH, stripsBgPaint)
        val votePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold
            textSize = 44f
            isFakeBoldText = true
        }
        val numPaint = Paint(votePaint).apply { setUnderlineText(true) }
        val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF3D7A3D.toInt()
            textSize = 44f
        }
        var vx = 36f
        val vy = bodyTop + 60f
        fun drawSeg(text: String, paint: Paint) {
            canvas.drawText(text, vx, vy, paint)
            vx += paint.measureText(text)
        }
        if (includeNumber) drawSeg("#${quote.number}", numPaint)
        if (includeRating && quote.rating != null) {
            drawSeg(" +", votePaint)
            drawSeg("(${quote.rating})", scorePaint)
            drawSeg("-", votePaint)
        }
        // U+2691 BLACK FLAG with U+FE0E to keep the text (non-emoji)
        // presentation — a gold flag glyph for reporting.
        drawSeg(" \u2691\uFE0E", votePaint)
        bodyTop += bashVoteLineH
    }
    // v1.61.5: the quote's web address as a quiet line under the header.
    if (urlLineH > 0f) {
        val urlPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF9A9A9A.toInt()
            textSize = 32f
        }
        canvas.drawRect(0f, bodyTop, CARD_W.toFloat(), bodyTop + urlLineH, stripsBgPaint)
        canvas.drawText(quote.url, 36f, bodyTop + 52f, urlPaint)
        bodyTop += urlLineH
    }

    // Quote body: full-bleed on the body background. The thin border
    // (every style except Bash.org, like the on-screen card) sits just
    // inside the card edges.
    val bodyBgPaint = Paint().apply { color = bodyBg }
    canvas.drawRect(0f, bodyTop, CARD_W.toFloat(), bodyTop + bodyH, bodyBgPaint)
    if (!borderless) {
        val borderPaint = Paint().apply {
            color = if (siteStyle == SiteStyle.CLASSIC_QDB) 0xFFD4D4D4.toInt() else bar
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(
            boxInsetX, bodyTop + 14f,
            CARD_W - boxInsetX, bodyTop + bodyH - 14f,
            borderPaint
        )
    }
    canvas.save()
    canvas.translate(boxInsetX + bodyPad, bodyTop + bodyPad)
    layout.draw(canvas)
    canvas.restore()

    // Footer bar, full-bleed.
    val footerTop = bodyTop + bodyH
    canvas.drawRect(0f, footerTop, CARD_W.toFloat(), footerTop + footerH, barPaint)
    val footPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = onBar
        textSize = 36f
    }
    // v1.61.7: the source database is always named in the footer — with
    // the total count when we have it, on its own when we don't.
    // v1.61.18: no render-time text on the left (Clint: the bottom-left
    // text goes) — the footer is just the database total, right-aligned,
    // like the on-screen site card.
    val siteName = siteDisplayName(siteStyle)
    if (dbTotal != null) {
        val right = String.format(Locale.US, "%,d quotes", dbTotal)
        canvas.drawText(
            right, CARD_W - 36f - footPaint.measureText(right),
            footerTop + 70f, footPaint
        )
    } else {
        canvas.drawText(siteName, 36f, footerTop + 70f, footPaint)
    }
    // v1.61.11: the logo strip at the top replaces the centered
    // watermark. Undo the content translate before returning.
    canvas.restore()
    // v1.88.32: the slim Quote404 site footer under the card — same
    // black as the top logo plate, www.Quote404.com bottom-right.
    canvas.drawQuote404Footer(totalH - qFooterH, CARD_W.toFloat(), qFooterH)
    return bitmap
}

/**
 * v1.61.16: renders the site-styled share card bitmap. The share sheet
 * and the share preview both render through this so the preview shows
 * exactly what gets shared. Call from a coroutine scope.
 */
suspend fun renderSiteQuoteCardForShare(
    context: Context,
    quote: Quote,
    siteStyle: SiteStyle,
    includeNumber: Boolean = true,
    includeRating: Boolean = true,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    outsideBlack: Boolean = false,
    /** v1.55.1: database total for the footer ("21,096 quotes"). */
    dbTotal: Int? = null,
    /** v1.58: per-quote capture date ("YYYYMMDD") for the footer line. */
    servedDate: String? = null,
    /**
     * v1.61.5: the effective per-database "Quote background" behind the
     * quote body, honoring the user's pick. Null keeps the site theme's
     * built-in body color.
     */
    quoteBackgroundArgb: Int? = null,
    /** v1.61.5: draw the quote's web address as a quiet line under the header. */
    includeUrl: Boolean = false,
    /**
     * v1.61.5: colorize nicknames in the body like the on-screen site
     * card ("with the option to do colorizations or not").
     */
    colorizeNicks: Boolean = false,
    nickPalette: NickPalette? = null,
    /** v1.61.11: the per-database "hide TLD" header option. */
    hideTldInHeader: Boolean = false,
    /**
     * v1.88.24: ambient composition locals for the novelty-theme export
     * path (translations, kid-safe, nick palette…). Null falls back to
     * defaults.
     */
    noveltyLocals: NoveltyExportLocals? = null,
    /**
     * v1.88.31: false skips the slim Quote404 logo plate so the image is
     * just the card; the header is always drawn on share/export images.
     */
    includeHeader: Boolean = true,
    /**
     * v1.89.2: the structure theme id the card resolved from, threaded to
     * the novelty renderer so dual-palette cards (Friends sunset vs wall)
     * match the on-screen card.
     */
    structureThemeId: String? = null,
    /**
     * v1.89.6: the rating format carried by the quote's database, when
     * it declares one; forwarded to the canvas renderer so the exported
     * rating text matches the on-screen card.
     */
    ratingFormat: String? = null
): Bitmap = withContext(Dispatchers.IO) {
    // v1.88.24: novelty themes render the real on-screen composables
    // off-screen to a bitmap — single source of truth, so the export is
    // pixel-identical to the card (bubbles, pinstripes, BSOD…).
    if (siteStyle.isNoveltyTheme()) {
        return@withContext renderNoveltyQuoteCard(
            context, quote, siteStyle,
            includeNumber = includeNumber,
            includeRating = includeRating,
            includeUrl = includeUrl,
            colorizeNicks = colorizeNicks,
            locals = noveltyLocals?.copy(nickPalette = nickPalette)
                ?: NoveltyExportLocals(nickPalette = nickPalette),
            dbId = null,
            includeHeader = includeHeader,
            structureThemeId = structureThemeId
        )
    }
    // v1.61.15: Clint's Quote404 logo for the top branding plate.
    // v1.88.31: null skips the plate (share dialog toggle).
    val watermark = if (includeHeader) quote404WatermarkLogo(context) else null
    renderSiteQuoteCard(
        quote, siteStyle,
        includeNumber = includeNumber,
        includeRating = includeRating,
        ircLineBreaks = ircLineBreaks,
        dialogueLineBreaks = dialogueLineBreaks,
        outsideBlack = outsideBlack,
        dbTotal = dbTotal,
        servedDate = servedDate,
        quoteBackgroundArgb = quoteBackgroundArgb,
        includeUrl = includeUrl,
        colorizeNicks = colorizeNicks,
        nickPalette = nickPalette,
        watermark = watermark,
        hideTldInHeader = hideTldInHeader,
        ratingFormat = ratingFormat
    )
}

/** Renders the site-styled card, then shares the PNG. Call from a coroutine scope. */
suspend fun renderAndShareSiteQuoteCard(
    context: Context,
    quote: Quote,
    siteStyle: SiteStyle,
    includeNumber: Boolean = true,
    includeRating: Boolean = true,
    ircLineBreaks: Boolean = true,
    dialogueLineBreaks: Boolean = true,
    outsideBlack: Boolean = false,
    /** v1.55.1: database total for the footer ("21,096 quotes"). */
    dbTotal: Int? = null,
    /** v1.58: per-quote capture date ("YYYYMMDD") for the footer line. */
    servedDate: String? = null,
    /**
     * v1.61.5: the effective per-database "Quote background" behind the
     * quote body, honoring the user's pick. Null keeps the site theme's
     * built-in body color.
     */
    quoteBackgroundArgb: Int? = null,
    /** v1.61.5: draw the quote's web address as a quiet line under the header. */
    includeUrl: Boolean = false,
    /**
     * v1.61.5: colorize nicknames in the body like the on-screen site
     * card ("with the option to do colorizations or not").
     */
    colorizeNicks: Boolean = false,
    nickPalette: NickPalette? = null,
    /** v1.61.11: the per-database "hide TLD" header option. */
    hideTldInHeader: Boolean = false,
    /**
     * v1.88.24: ambient composition locals for the novelty-theme export
     * path. Null falls back to defaults.
     */
    noveltyLocals: NoveltyExportLocals? = null,
    /**
     * v1.89.6: the rating format carried by the quote's database, when
     * it declares one; forwarded so the shared card's rating text
     * matches the on-screen card.
     */
    ratingFormat: String? = null
) {
    try {
        val rendered = renderSiteQuoteCardForShare(
            context, quote, siteStyle,
            includeNumber = includeNumber,
            includeRating = includeRating,
            ircLineBreaks = ircLineBreaks,
            dialogueLineBreaks = dialogueLineBreaks,
            outsideBlack = outsideBlack,
            dbTotal = dbTotal,
            servedDate = servedDate,
            quoteBackgroundArgb = quoteBackgroundArgb,
            includeUrl = includeUrl,
            colorizeNicks = colorizeNicks,
            nickPalette = nickPalette,
            hideTldInHeader = hideTldInHeader,
            noveltyLocals = noveltyLocals,
            ratingFormat = ratingFormat
        )
        shareBitmap(
            context, rendered,
            "quote-${quote.number}-site.png",
            "Share site quote card"
        )
    } catch (e: Exception) {
        // v1.61: never report cancellation as a render failure.
        if (e is CancellationException) throw e
        Toast.makeText(context, "Couldn't render site card", Toast.LENGTH_SHORT).show()
    }
}
