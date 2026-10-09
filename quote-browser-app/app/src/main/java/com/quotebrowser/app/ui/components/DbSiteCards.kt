package com.quotebrowser.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.R
import com.quotebrowser.app.data.DB_THEME_IDS
import com.quotebrowser.app.data.DbThemeSnapshots
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.ResolvedDbTheme
import com.quotebrowser.app.data.SiteLayout
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.data.siteStyleFor
import com.quotebrowser.app.data.siteStyleMonospace
import com.quotebrowser.app.ui.LocalQuoteBackground
import com.quotebrowser.app.ui.openWebLink
import com.quotebrowser.app.ui.theme.contentOn
import java.util.Locale

/**
 * v1.51 per-database theming runtime.
 *
 * [LocalDbThemeSnapshots] is provided once at the activity root; cards
 * resolve their own database's theme purely (no I/O in composition).
 * [LocalVoteControlsEnabled] gates the offline +/- thumbs controls, and
 * [LocalQueuedTagsEnabled] gates the "Queued" tag on unapproved quotes.
 */
val LocalDbThemeSnapshots = compositionLocalOf { DbThemeSnapshots.BYPASSED }
val LocalVoteControlsEnabled = compositionLocalOf { true }
val LocalQueuedTagsEnabled = compositionLocalOf { true }

/**
 * furcqdb's bright-blue title color ("FURCADIA QUOTES!" masthead); v1.57
 * also uses it for the thin borders delineating the card's header and
 * footer sections (border-only, no filled bars — the all-black
 * header/footer stays authentic).
 */
private val FurcqdbTitleBlue = Color(0xFF3333FF)

/**
 * Site header art for a structure theme id, or null when the theme has no
 * art. Art only ever renders when the card is inside that site theme's own
 * Site-layout presentation (gated by the caller per the art rule); the
 * drawable shown is always the active site theme's own.
 */
fun siteHeaderArt(structureThemeId: String?): Int? = when (structureThemeId) {
    // xkcdb: the small xkcd-style header figures go in the quote card's top
    // bar (never the green donation box).
    "db_xkcdb" -> R.drawable.db_header_xkcdb_strip
    "db_quoteirc" -> R.drawable.db_logo_quoteirc
    "db_wqdb" -> R.drawable.db_logo_wqdb
    "db_qdb_mit" -> R.drawable.db_header_qdbmit
    "db_burntelectrons" -> R.drawable.db_header_burntelectrons
    "db_warpdrive" -> R.drawable.db_header_warpdrive
    "db_bashim" -> R.drawable.db_header_bashim
    "db_furcqdb" -> R.drawable.db_header_furcqdb
    "db_irox" -> R.drawable.db_header_irox
    else -> null
}

/**
 * Whether this card may show its site theme's header art: per-DB themes on,
 * no master override, the DB's own theming enabled, Site layout, and the
 * card is rendering a real database site theme (not Classic/Modern).
 */
fun siteArtAllowed(resolved: ResolvedDbTheme): Boolean =
    !resolved.bypassed &&
        resolved.layout == SiteLayout.SITE &&
        resolved.structureThemeId in DB_THEME_IDS

/**
 * "Queued" tag shown on quotes whose status is 'queued' (submitted to the
 * site but not yet approved for the main archive). Tapping explains what
 * that means.
 */
@Composable
fun QueuedTag(modifier: Modifier = Modifier) {
    var showInfo by remember { mutableStateOf(false) }
    AssistChip(
        onClick = { showInfo = true },
        label = { Text("Queued") },
        modifier = modifier
    )
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Queued quote") },
            text = {
                Text(
                    "This quote was submitted to the site but hasn't been " +
                        "approved for the main archive yet. It may show up " +
                        "in the archive later, or never be approved."
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// v1.54 site-fidelity rework: every site style renders a faithful mini
// version of its original single-quote page (researched from archived
// captures and Clint's screenshots in theme-refs/), not a generic
// colored bar. qdb.us keeps its v1.53 blue-era presentation untouched;
// CrapShack keeps its Clint-approved deep-blue bars.
// ---------------------------------------------------------------------------

/**
 * qdb.us approved/pending quote counts for the site footer's real counts
 * ("14,355 quotes approved; 1,218 quotes pending"). Provided once at the
 * app root.
 */
val LocalQdbCounts = compositionLocalOf { 0 to 0 }

/**
 * v1.55.1: total quote counts per database id ("%,d quotes" in the site
 * footers). Provided once at the app root.
 */
val LocalDbTotals = compositionLocalOf { mapOf<String, Int>() }

/**
 * v1.58: per-quote capture dates ("YYYYMMDD") keyed "dbId/number",
 * currently only populated for qdb.us (sidecar asset). Provided once at
 * the app root; cards look up their own "dbId/number" key, so quote
 * numbers from other databases can never collide.
 */
val LocalServedDates = compositionLocalOf { mapOf<String, String>() }

/** v1.58: "YYYYMMDD" -> "Aug 31, 2011" for the quiet footer date line. */
fun formatServedDate(yyyymmdd: String): String {
    return try {
        val parsed = java.time.LocalDate.parse(
            yyyymmdd,
            java.time.format.DateTimeFormatter.BASIC_ISO_DATE
        )
        parsed.format(
            java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", java.util.Locale.US)
        )
    } catch (e: Exception) {
        yyyymmdd
    }
}

/**
 * v1.53 main-theme option: black page background outside the bordered site
 * quotes. Defaults OFF so site themes match their original sites (qdb.us
 * renders on white like its screenshot); when ON it applies no matter
 * which global theme (including a database site theme) is selected.
 * Provided once at the app root.
 */
val LocalBlackOutsideQuotes = compositionLocalOf { false }

/**
 * v1.57: true = single-database list or single-quote detail (the card may
 * paint its authentic page background outside its own card, e.g. qdb.us
 * white); false = mixed-database list (the card must NOT paint any page
 * color outside its own rounded card — the global page background shows
 * through). The explicit black-outside-quotes user setting
 * ([LocalBlackOutsideQuotes]) still applies in both modes.
 */
val LocalSingleDatabaseMode = compositionLocalOf { true }

/** The site's signature accent color (public for share cards / tiles). */
fun siteBarColor(style: SiteStyle): Color = when (style) {
    SiteStyle.CLASSIC_QDB -> Color(0xFF7A9CC6) // qdb.us blue-era header bar (v1.61.10: a touch bluer)
    SiteStyle.QDB_MIT -> Color(0xFF993333) // QDB.MIT.edu maroon header bar
    SiteStyle.QUOTEIRC -> Color(0xFFA52B2B) // QuoteIRC brick red
    SiteStyle.BURNT_ELECTRONS -> Color(0xFF990000) // burnt red accent
    SiteStyle.WARPDRIVE -> Color(0xFF0099CC) // warpdrive cyan links
    SiteStyle.BASHIM -> Color(0xFF2B6CB0) // bash.im muted blue links
    SiteStyle.WQDB -> Color(0xFF003366) // WQDB navy nav bar
    SiteStyle.XKCDB -> Color(0xFFD7DCE5) // xkcdb pale header
    SiteStyle.FURCQDB -> Color(0xFFFF8C00) // furcqdb orange
    SiteStyle.IROX -> Color(0xFFE8721C) // I-Rox orange wordmark
    SiteStyle.BASH -> Color(0xFFCD8C00) // bash.org gold (v1.61.15: brightened a touch — the old dark gold read too dark on exports)
    SiteStyle.CRAPSHACK -> Color(0xFF4AA3FF) // CrapShack deepened blue
    // v1.88.23 novelty themes.
    SiteStyle.DOS -> Color(0xFF1A1A1A)
    SiteStyle.MACOS -> Color(0xFFE8E8ED)
    SiteStyle.MACINTOSH -> Color(0xFF000000)
    SiteStyle.IMESSAGE -> Color(0xFF0A84FF)
    SiteStyle.GMESSAGES -> Color(0xFF0B57D0)
    SiteStyle.BSOD -> Color(0xFF0000AA)
    SiteStyle.LINUX -> Color(0xFF1A1A1A)
    SiteStyle.MIRC -> Color(0xFF000080)
    SiteStyle.TERMINAL -> Color(0xFF2D2D2D)
    SiteStyle.GREEN_MONO -> Color(0xFF0A0A0A)
    SiteStyle.ANSI -> Color(0xFFAA00AA) // BBS ANSI bright magenta
    SiteStyle.ASCII -> Color(0xFF808080) // textmode gray
    SiteStyle.MATRIX -> Color(0xFF00FF00) // matrix green
    SiteStyle.GREEN_TERM -> Color(0xFF003300) // dark terminal green
    SiteStyle.MESSENGER_LIGHT -> Color(0xFF0084FF) // Messenger blue
    SiteStyle.MESSENGER_DARK -> Color(0xFF0084FF) // Messenger blue
    SiteStyle.AIM -> Color(0xFF0058E6) // AIM title-bar blue
    SiteStyle.ICQ -> Color(0xFF4A9EDE) // ICQ light blue
    SiteStyle.MSN_MESSENGER -> Color(0xFF0B7DDA) // MSN blue
    // v1.88.38: Friends deep plum.
    SiteStyle.FRIENDS -> Color(0xFF1E1033)
    SiteStyle.MODERN_CARD -> Color.Transparent
}

/** Content color on the site bar. */
fun siteOnBarColor(style: SiteStyle): Color = when (style) {
    SiteStyle.CLASSIC_QDB -> Color.Black // black on qdb.us blue bar
    SiteStyle.BASH -> Color.Black // black on bash.org gold bar
    SiteStyle.XKCDB -> Color(0xFF1A237E) // dark blue on pale
    else -> Color.White
}

/** Quote-body background per site (public for the site-styled share card). */
fun siteBodyBackgroundColor(style: SiteStyle, listIndex: Int? = null): Color = when (style) {
    // QDB.MIT.edu: nearly-white quote bodies (#FFFCEC).
    SiteStyle.QDB_MIT -> Color(0xFFFFFCEC)
    // WQDB: alternating white / light-gray quote rows.
    SiteStyle.WQDB ->
        if (listIndex != null && listIndex % 2 == 1) Color(0xFFF0F0F0)
        else Color.White
    // furcqdb: tan/beige quote cards.
    SiteStyle.FURCQDB -> Color(0xFFD6C9B4)
    // I-Rox: very light gray cards.
    SiteStyle.IROX -> Color(0xFFF7F7F7)
    // CrapShack: dark card on the dark theme.
    SiteStyle.CRAPSHACK -> Color(0xFF141414)
    // v1.88.23 novelty themes.
    SiteStyle.DOS -> Color(0xFF000000)
    SiteStyle.MACOS -> Color(0xFFF5F5F7)
    SiteStyle.MACINTOSH -> Color.White
    SiteStyle.IMESSAGE -> Color.White
    SiteStyle.GMESSAGES -> Color.White
    SiteStyle.BSOD -> Color(0xFF0000AA)
    SiteStyle.LINUX -> Color(0xFF000000)
    SiteStyle.MIRC -> Color.White
    SiteStyle.TERMINAL -> Color(0xFF0D1117)
    SiteStyle.GREEN_MONO -> Color(0xFF000000)
    else -> Color.White
}

/** Quote-box inner border per site: qdb.us uses a light gray border
 * (screenshot-sampled); others tint toward their bar color. */
fun siteInnerBorderColor(style: SiteStyle, bar: Color): Color = when (style) {
    SiteStyle.CLASSIC_QDB -> Color(0xFFD4D4D4)
    else -> bar.copy(alpha = 0.65f)
}

/** The quote-number label per site (left side of the header bar). */
fun siteNumberLabel(style: SiteStyle, number: Int): String = when (style) {
    SiteStyle.CLASSIC_QDB -> "Quote #$number"
    SiteStyle.QUOTEIRC -> "Quote #$number"
    SiteStyle.FURCQDB -> "Quote #$number"
    else -> "#$number"
}

/** The rating display per site (right side of the header bar). */
fun siteRatingLabel(style: SiteStyle, rating: Int?): String? {
    // v1.77: the score always shows — 0 when the quote has no rating.
    val score = rating ?: 0
    return when (style) {
        SiteStyle.CLASSIC_QDB -> null // rating lives on the quote line
        SiteStyle.QDB_MIT -> "($score)"
        SiteStyle.QUOTEIRC -> "$score pts"
        SiteStyle.BURNT_ELECTRONS -> "[$score]"
        SiteStyle.WARPDRIVE -> "rating: $score"
        SiteStyle.BASHIM -> "$score" // bash.im showed the plain rating number
        SiteStyle.WQDB -> "- Rating $score -"
        SiteStyle.XKCDB -> "(+$score/-0)"
        SiteStyle.FURCQDB -> "Rating: $score"
        SiteStyle.IROX -> "$score"
        SiteStyle.BASH -> "Score: $score"
        SiteStyle.CRAPSHACK -> "$score pts"
        // v1.88.23 novelty themes.
        SiteStyle.DOS -> "[$score]"
        SiteStyle.MACOS -> "$score"
        SiteStyle.MACINTOSH -> "$score"
        SiteStyle.IMESSAGE -> "$score pts"
        SiteStyle.GMESSAGES -> "$score"
        SiteStyle.BSOD -> "STOP:$score"
        SiteStyle.LINUX -> "[$score]"
        SiteStyle.MIRC -> "$score"
        SiteStyle.TERMINAL -> "$score"
        SiteStyle.GREEN_MONO -> "$score"
        SiteStyle.ANSI -> "[$score]"
        SiteStyle.ASCII -> "[$score]"
        SiteStyle.MATRIX -> "[$score]"
        SiteStyle.GREEN_TERM -> "[$score]"
        SiteStyle.MESSENGER_LIGHT -> "$score"
        SiteStyle.MESSENGER_DARK -> "$score"
        SiteStyle.AIM -> "$score"
        SiteStyle.ICQ -> "$score"
        SiteStyle.MSN_MESSENGER -> "$score"
        // v1.88.38: Friends shows the plain score.
        SiteStyle.FRIENDS -> "$score"
        SiteStyle.MODERN_CARD -> null
    }
}

/**
 * v1.61.11: the full site title for the header bar (TLD on, the default).
 * These match what the headers showed before the hide-TLD option existed.
 */
fun siteFullTitle(style: SiteStyle): String = when (style) {
    SiteStyle.CLASSIC_QDB -> "qdb.us"
    SiteStyle.CRAPSHACK -> "CrapShack"
    SiteStyle.QDB_MIT -> "QDB.MIT.edu"
    SiteStyle.QUOTEIRC -> "QuoteIRC"
    SiteStyle.BURNT_ELECTRONS -> "BurntElectrons"
    SiteStyle.WARPDRIVE -> "warpdrive.se"
    SiteStyle.BASHIM -> "bash.im"
    SiteStyle.WQDB -> "WQDB.org"
    SiteStyle.XKCDB -> "xkcdb"
    SiteStyle.FURCQDB -> "furcqdb"
    SiteStyle.IROX -> "I-Rox"
    SiteStyle.BASH -> "Bash.org"
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
    SiteStyle.MODERN_CARD -> ""
}

/**
 * v1.61.11: the short site title for the header bar when the per-database
 * "hide TLD" option is on ("Bash" instead of "Bash.org").
 */
fun siteShortTitle(style: SiteStyle): String = when (style) {
    SiteStyle.CLASSIC_QDB -> "qdb"
    SiteStyle.CRAPSHACK -> "CrapShack"
    SiteStyle.QDB_MIT -> "QDB.MIT"
    SiteStyle.QUOTEIRC -> "QuoteIRC"
    SiteStyle.BURNT_ELECTRONS -> "BurntElectrons"
    SiteStyle.WARPDRIVE -> "Warpdrive"
    SiteStyle.BASHIM -> "bash.im" // no TLD to hide
    SiteStyle.WQDB -> "WQDB"
    SiteStyle.XKCDB -> "xkcdb"
    SiteStyle.FURCQDB -> "furcqdb"
    SiteStyle.IROX -> "I-Rox"
    SiteStyle.BASH -> "Bash"
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
    // v1.88.38: Clint's personal Friends database (no TLD to hide).
    SiteStyle.FRIENDS -> "Friends"
    SiteStyle.MODERN_CARD -> ""
}

/** v1.61.11: header-bar title honoring the per-database hide-TLD option. */
fun siteHeaderTitle(style: SiteStyle, hideTld: Boolean): String =
    if (hideTld) siteShortTitle(style) else siteFullTitle(style)

// ---------------------------------------------------------------------------
// Legacy presentations (kept verbatim): qdb.us blue-era and CrapShack.
// ---------------------------------------------------------------------------

/**
 * v1.88.2: tap the database name in a card header to jump to Browse
 * filtered to that database. Returns a plain Modifier when there is no
 * database to filter to (or no navigation host, e.g. bitmap exports and
 * previews) — the name then renders as inert text. Taps anywhere else on
 * the card still open just that quote; the name tap never does.
 */
@Composable
private fun dbNameTapModifier(dbId: String?): Modifier {
    val browseDb = LocalBrowseDatabase.current
    return if (dbId != null && browseDb != null) {
        Modifier.clickable { browseDb(dbId) }
    } else {
        Modifier
    }
}

/**
 * The legacy site-colored header bar: site name on the left, quote number
 * on the right, and the optional centered site logo in the middle.
 * v1.61.10: no decorative borders on the title/number cells and no
 * " Quotes" suffix — just the site name ("qdb.us", "CrapShack").
 * Kept for qdb.us (CLASSIC_QDB, blue era) and CrapShack.
 */
@Composable
private fun SiteHeaderBar(
    style: SiteStyle,
    quote: Quote,
    showNumber: Boolean,
    showQueuedTag: Boolean,
    centeredLogoArt: Int?,
    /** v1.61.11: per-database "hide TLD" header option. */
    hideTldInHeader: Boolean = false,
    /** v1.88.2: database id for the tappable header name (null = inert). */
    dbId: String? = null,
) {
    // v1.61.10: header bar — database name left, quote number right.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (style) {
                // qdb.us blue era: italic bold serif name left, Arial-bold
                // "Quote #N" right, black on the blue bar.
                SiteStyle.CLASSIC_QDB -> {
                    Text(
                        text = siteHeaderTitle(style, hideTldInHeader),
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        modifier = dbNameTapModifier(dbId)
                    )
                    Spacer(Modifier.weight(1f))
                    if (showNumber) {
                        Text(
                            text = "Quote #${quote.number}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                    }
                }
                else -> {
                    // CrapShack: database name left, quote number right.
                    Text(
                        text = siteHeaderTitle(style, hideTldInHeader),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        modifier = dbNameTapModifier(dbId)
                    )
                    Spacer(Modifier.weight(1f))
                    if (showNumber) {
                        Text(
                            text = "#${quote.number}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
            if (showQueuedTag) {
                QueuedTag()
            }
        }
        // Optional centered site logo in the header bar (per-database setting,
        // default off). Never in a corner: centered or not at all.
        if (centeredLogoArt != null) {
            Image(
                painter = painterResource(id = centeredLogoArt),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(30.dp)
            )
        }
    }
}

/**
 * v1.61.10: the clickable header controls sit in their own row just under
 * the header bar — never inside it.
 * v1.61.11: new order — the rating + vote controls sit at the far LEFT of
 * the row; the flag, share, favorite, and menu (⋯) buttons sit at the far
 * RIGHT (menu last). qdb.us and bash.org keep their specific vote spots
 * (the quote line / the header bar), so they get no vote controls here —
 * but the flag joins the right group on every theme.
 */
@Composable
private fun SiteHeaderControls(
    style: SiteStyle,
    quote: Quote,
    /** Tint for the controls, contrasting with the card body they sit on. */
    tint: Color,
    showRating: Boolean,
    /** v1.53 report/flag affordance; null hides it. */
    onFlagClick: (() -> Unit)? = null,
    voteControls: @Composable RowScope.(upTint: Color, downTint: Color) -> Unit,
    headerActions: @Composable RowScope.() -> Unit = {},
    /** v1.77: Favorites pin toggle; null hides the pin. */
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Far left: rating + vote controls — except qdb.us and bash.org,
        // which keep their specific spots.
        if (style != SiteStyle.CLASSIC_QDB && style != SiteStyle.BASH) {
            val ratingLabel = siteRatingLabel(style, quote.rating)
            if (showRating && ratingLabel != null) {
                Text(
                    text = ratingLabel,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint
                )
            }
            voteControls(tint, tint)
        }
        Spacer(Modifier.weight(1f))
        // Far right: flag, then share / favorite / menu via headerActions
        // (MoreFavoriteButtons renders share, favorite, menu — in that
        // order — since v1.61.11).
        // v1.53 report/flag: the flag affordance, on every theme.
        // v1.77: the Favorites pin sits next to the flag.
        if (onTogglePin != null) {
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (isPinned) "Unpin from top" else "Pin to top",
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (onFlagClick != null) {
            IconButton(
                onClick = onFlagClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Filled.Flag,
                    contentDescription = "Flag quote",
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        headerActions()
    }
}

/**
 * qdb.us quote line, styled like the original site: `#100874 (509/695)`
 * with the number as a blue link, the score as plain (up/down) counts,
 * and working local "-" / "+" vote buttons inline.
 */
@Composable
private fun QdbQuoteLine(
    quote: Quote,
    showRating: Boolean,
    showQueuedTag: Boolean,
    /** v1.53 report/flag affordance; null hides it. */
    onFlagClick: (() -> Unit)? = null,
    voteControls: @Composable RowScope.(upTint: Color, downTint: Color) -> Unit
) {
    // Blue era: quote number in the blue link color, score in plain black
    // like the site's "(509/695)".
    // v1.61.10: matches the header bar (a touch bluer than before).
    val blue = Color(0xFF7A9CC6)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "#${quote.number}",
            fontWeight = FontWeight.Bold,
            color = blue
        )
        if (showRating) {
            // v1.77: the score always shows — 0 when the quote has no rating.
            val r = quote.rating ?: 0
            Text(
                text = if (r >= 0) "(+$r)" else "($r)",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        voteControls(blue, blue)
        if (onFlagClick != null) {
            IconButton(
                onClick = onFlagClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Filled.Flag,
                    contentDescription = "Flag quote",
                    tint = blue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        if (showQueuedTag) {
            QueuedTag()
        }
    }
}

/**
 * The legacy site-colored footer bar: site-colored bar with the site's own
 * info line. qdb.us shows real render timing and the real database
 * total quote count (approved + queued). The footer never shows the
 * individual quote number (it lives in the header / vote lines), never
 * shows pending/queued counts, and never shows the database/site name.
 */
/**
 * v1.56 footer stats: the quote load/render time plus the database's
 * total quote count ("0.0005   21,096 quotes"). The footer never shows the
 * individual quote number (it lives in the header / vote lines), never
 * shows pending/queued counts, and never shows the database/site name.
 * v1.58: a quiet per-quote capture-date line ("captured Aug 31, 2011")
 * after the load time, where the sidecar data exists.
 */
@Composable
private fun RowScope.footerStats(
    loadSecs: Double?,
    dbTotal: Int?,
    color: Color,
    servedDate: String? = null
) {
    val labelStyle = MaterialTheme.typography.labelSmall
    if (loadSecs != null) {
        Text(
            text = "%.4f".format(Locale.US, loadSecs),
            style = labelStyle,
            color = color
        )
    }
    if (servedDate != null) {
        Text(
            text = "captured " + formatServedDate(servedDate),
            style = labelStyle,
            color = color
        )
    }
    if (dbTotal != null) {
        Spacer(Modifier.weight(1f))
        Text(
            text = "%,d quotes".format(Locale.US, dbTotal),
            style = labelStyle,
            color = color
        )
    }
}

@Composable
private fun SiteCardFooter(
    style: SiteStyle,
    quote: Quote,
    onBar: Color,
    loadSecs: Double?,
    dbTotal: Int?,
    servedDate: String? = null
) {
    val labelStyle = MaterialTheme.typography.labelSmall
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (style) {
            SiteStyle.CLASSIC_QDB -> {
                // v1.55.1: blue-era footer — load time left, database
                // total right. No quote number, no pending counts.
                footerStats(loadSecs, dbTotal, onBar, servedDate)
            }
            SiteStyle.CRAPSHACK -> {
                // v1.56: no site branding in the footer — load time left,
                // database total right, like every other site card.
                footerStats(loadSecs, dbTotal, onBar, servedDate)
            }
            else -> {
                Text(
                    text = style.name,
                    style = labelStyle,
                    color = onBar
                )
                footerStats(loadSecs, dbTotal, onBar, servedDate)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// v1.54 mini-site presentations: each quote card mimics its original
// single-quote page — masthead, quote info line, body, footer — per the
// archived captures / screenshots in theme-refs/.
// ---------------------------------------------------------------------------

/** Small flag affordance for the mini-site quote headers. */
@Composable
private fun RowScope.flagIconButton(
    tint: Color,
    onFlagClick: (() -> Unit)?,
    /** v1.77: hide the flag (bash.org — the [X] link does the same thing). */
    hideFlag: Boolean = false,
    isPinned: Boolean = false,
    /** v1.77: Favorites pin toggle; sits next to the flag (or in its spot). */
    onTogglePin: (() -> Unit)? = null
) {
    if (onTogglePin != null) {
        IconButton(
            onClick = onTogglePin,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                contentDescription = if (isPinned) "Unpin from top" else "Pin to top",
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
    if (onFlagClick != null && !hideFlag) {
        IconButton(
            onClick = onFlagClick,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Filled.Flag,
                contentDescription = "Flag quote",
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun bashTightLink(label: String, onClick: () -> Unit, color: Color) {
    Text(
        text = label,
        fontWeight = FontWeight.Bold,
        color = color,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 6.dp)
    )
}

/**
 * Per-site masthead: the branding strip at the top of the mini page.
 * v1.61.10: every database now brands itself through the unified header
 * bar (site name left, quote number right, optional centered logo), so
 * only the styles with extra identity above the bar keep a masthead:
 * warpdrive.se's welcome line and bash.org's gold bar.
 */
@Composable
private fun SiteMasthead(
    style: SiteStyle,
    centeredLogoArt: Int?,
    quote: Quote,
    showNumber: Boolean,
    /** v1.61.11: per-database "hide TLD" header option. */
    hideTldInHeader: Boolean = false,
    /** v1.88.2: database id for the tappable masthead name (null = inert). */
    dbId: String? = null
) {
    when (style) {
        // warpdrive.se: the site's own welcome line (no graphical logo).
        SiteStyle.WARPDRIVE -> {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "V\u00e4lkommen till warpdrive!",
                    color = Color(0xFF9A9A9A),
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Den Svenska Chatcitatdatabasen",
                    color = Color(0xFF9A9A9A),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        // bash.org: the site's own dark-gold top bar — italic
        // wordmark left, black on gold (#CD8C00,
        // sampled from the real site's header). v1.55.1: "Quote #N" back
        // at the right (v1.54 position). v1.61.10: just the site name, no
        // "Quotes" suffix — like every other database header.
        // v1.61.11: the hide-TLD option shortens it to "Bash".
        SiteStyle.BASH -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFCD8C00))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = siteHeaderTitle(style, hideTldInHeader),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = dbNameTapModifier(dbId)
                )
                Spacer(Modifier.weight(1f))
                if (showNumber) {
                    Text(
                        text = "Quote #${quote.number}",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        else -> {
            // Every other database brands itself through the unified
            // header bar (name left, number right, optional centered
            // logo) — no separate masthead.
        }
    }
}

/**
 * v1.61.10: the unified site-card header. Every database theme uses the
 * same professional layout: a site-colored bar with the site name on the
 * left, the quote number on the right (and the optional centered logo in
 * the middle), then a right-aligned controls row just under the bar with
 * the rating display, vote controls, flag, and the overflow/share/favorite
 * actions.
 */
@Composable
private fun UnifiedSiteHeader(
    /** Outer spacing around the whole header (for pill-style bars). */
    modifier: Modifier = Modifier,
    siteName: String,
    /** The quote-number text for the right side, or null to hide it. */
    numberText: String?,
    showQueuedTag: Boolean,
    centeredLogoArt: Int?,
    /** Background drawn behind the bar (flat color, gradient, pill...). */
    barBackground: Modifier,
    barContentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    nameColor: Color,
    numberColor: Color,
    nameStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    numberStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    /** v1.88: optional tap action on the quote number (bash.im links to the quote's page). */
    onNumberClick: (() -> Unit)? = null,
    /** v1.88.2: database id for the tappable site name (null = inert). */
    dbId: String? = null,
    /** Right-aligned controls row just under the bar. */
    controls: @Composable RowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(barBackground)
                .padding(barContentPadding)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = siteName,
                    color = nameColor,
                    style = nameStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = dbNameTapModifier(dbId)
                )
                Spacer(Modifier.weight(1f))
                if (numberText != null) {
                    Text(
                        text = numberText,
                        color = numberColor,
                        style = numberStyle,
                        maxLines = 1,
                        modifier = if (onNumberClick != null) {
                            Modifier.clickable(onClick = onNumberClick)
                        } else {
                            Modifier
                        }
                    )
                }
                if (showQueuedTag) {
                    Spacer(Modifier.width(8.dp))
                    QueuedTag()
                }
            }
            // Optional centered site logo (per-database setting, default
            // off). Never in a corner: centered or not at all.
            if (centeredLogoArt != null) {
                Image(
                    painter = painterResource(id = centeredLogoArt),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .height(30.dp)
                )
            }
        }
        // The controls row sits just under the header bar — never inside
        // it — right-aligned.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            controls()
        }
    }
}

/**
 * The per-quote header: every database theme now uses the unified header
 * (site name left, quote number right, controls right-aligned just under
 * the bar). bash.org keeps its iconic single-line quote header under the
 * gold bar. Vote controls stay wired to the app's personal rating.
 */
@Composable
private fun SiteQuoteHeader(
    style: SiteStyle,
    quote: Quote,
    showNumber: Boolean,
    showRating: Boolean,
    showQueuedTag: Boolean,
    /** v1.61.10: optional centered site logo for the unified header bar. */
    centeredLogoArt: Int? = null,
    hideTldInHeader: Boolean = false,
    onFlagClick: (() -> Unit)?,
    voteControls: @Composable RowScope.(upTint: Color, downTint: Color) -> Unit,
    /** v1.54: bash.org renders overflow + favorite at the top right. */
    headerActions: @Composable RowScope.() -> Unit = {},
    /** v1.77: Favorites pin toggle; null hides the pin. */
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null,
    /**
     * v1.77: bash.org's vote links always show (authentic site chrome), so
     * it gets the ungated rate handler + local rating directly.
     */
    bashMyRating: Int = 0,
    bashOnRate: ((Int) -> Unit)? = null,
    /** v1.88.2: database id for the tappable header site name (null = inert). */
    dbId: String? = null
) {
    val labelMedium = MaterialTheme.typography.labelMedium
    val labelSmall = MaterialTheme.typography.labelSmall
    when (style) {
        // QDB.MIT.edu (Chirpy!): the site's maroon pill bar carries the
        // wordmark left and the quote number right; the row just under it
        // has the green/red vote links and (rating) at the far left, then
        // the flag and the share/favorite/menu actions at the far right.
        SiteStyle.QDB_MIT -> {
            UnifiedSiteHeader(
                dbId = dbId,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "#${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFA94444),
                            Color(0xFF993333),
                            Color(0xFF7E2B2B)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                ),
                barContentPadding = PaddingValues(vertical = 8.dp, horizontal = 18.dp),
                nameColor = Color(0xFF999999),
                numberColor = Color(0xFFDDDDDD),
                nameStyle = MaterialTheme.typography.titleMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium
                ),
                controls = {
                    if (showRating) {
                        voteControls(Color(0xFF009900), Color(0xFF990000))
                        Text(
                            text = "(${quote.rating ?: 0})",
                            color = Color(0xFF666666),
                            style = labelMedium
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    flagIconButton(Color(0xFF4D0099), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // QuoteIRC: brick-red bar with the site name left and "Quote #N"
        // right; the row just under it has the rank and vote links at the
        // far left, then the flag and the share/favorite/menu actions at
        // the far right.
        SiteStyle.QUOTEIRC -> {
            UnifiedSiteHeader(
                dbId = dbId,
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "Quote #${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(Color(0xFFA52B2B)),
                nameColor = Color.White,
                numberColor = Color.White,
                controls = {
                    if (showRating) {
                        Text(
                            text = "Quote rank: ${quote.rating ?: 0}",
                            color = Color(0xFF444444),
                            style = labelMedium
                        )
                    }
                    voteControls(Color(0xFF2E9E44), Color(0xFFC62828))
                    Spacer(Modifier.weight(1f))
                    flagIconButton(Color(0xFFA52B2B), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // BurntElectrons: the site's red gradient bar with the name left
        // and the quote number right; the row just under it has the
        // (rating) and "Up!"/"Down" vote links at the far left, then the
        // flag and the share/favorite/menu actions at the far right.
        SiteStyle.BURNT_ELECTRONS -> {
            UnifiedSiteHeader(
                dbId = dbId,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "#${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF7A0000),
                            Color(0xFFCC2222),
                            Color(0xFF990000),
                            Color(0xFF7A0000)
                        )
                    ),
                    RoundedCornerShape(
                        topStart = 6.dp, topEnd = 6.dp,
                        bottomStart = 28.dp, bottomEnd = 28.dp
                    )
                ),
                barContentPadding = PaddingValues(
                    start = 16.dp, top = 12.dp, bottom = 16.dp, end = 12.dp
                ),
                nameColor = Color.White,
                numberColor = Color.White,
                nameStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                controls = {
                    if (showRating) {
                        Text(
                            text = "(${quote.rating ?: 0})",
                            color = Color(0xFF777777),
                            fontFamily = FontFamily.Monospace,
                            style = labelMedium
                        )
                    }
                    voteControls(Color(0xFF990000), Color(0xFF990000))
                    Spacer(Modifier.weight(1f))
                    // v1.61.11: the report affordance is a flag icon on
                    // every theme (was a "Report" text link here).
                    flagIconButton(Color(0xFF990000), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // warpdrive.se: cyan bar with the site name left and the quote
        // number right; the row just under it has the site's "+ score -"
        // text vote links at the far left, then the flag and the
        // share/favorite/menu actions at the far right.
        SiteStyle.WARPDRIVE -> {
            UnifiedSiteHeader(
                dbId = dbId,
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "#${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(Color(0xFF0099CC)),
                nameColor = Color.White,
                numberColor = Color.White,
                controls = {
                    if (showRating) {
                        voteControls(Color(0xFF0099CC), Color(0xFF0099CC))
                    }
                    Spacer(Modifier.weight(1f))
                    // v1.61.11: the report affordance is a flag icon on
                    // every theme (was the "Begär omprövning" text link).
                    flagIconButton(Color(0xFF0099CC), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // bash.im: white bar with a hairline bottom border (the 2019
        // minimalist redesign — no heavy brand bar); the site name left
        // in dark, the #number right in muted blue. The row just under
        // the bar has the site's "+ score −" gray text vote links at the
        // far left, then the flag and the share/favorite/menu actions at
        // the far right.
        SiteStyle.BASHIM -> {
            val context = LocalContext.current
            UnifiedSiteHeader(
                dbId = dbId,
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "#${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(Color.White).drawBehind {
                    val stroke = 1.dp.toPx()
                    drawLine(
                        Color(0xFFE5E5E5),
                        Offset(0f, size.height - stroke / 2),
                        Offset(size.width, size.height - stroke / 2),
                        stroke
                    )
                },
                nameColor = Color(0xFF222222),
                numberColor = Color(0xFF2B6CB0),
                // v1.88: the number opens the quote's canonical bash.im page.
                onNumberClick = { openWebLink(context, quote.url) },
                controls = {
                    if (showRating) {
                        voteControls(Color(0xFF8A8A8A), Color(0xFF8A8A8A))
                    }
                    Spacer(Modifier.weight(1f))
                    // v1.61.11: the report affordance is a flag icon on
                    // every theme.
                    flagIconButton(Color(0xFF8A8A8A), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // WQDB: navy bar with the site name left and the quote number
        // right; the row just under it has the "- Rating N -" line and
        // vote thumbs at the far left, then the flag and the
        // share/favorite/menu actions at the far right.
        SiteStyle.WQDB -> {
            UnifiedSiteHeader(
                dbId = dbId,
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "#${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(Color(0xFF003366)),
                nameColor = Color.White,
                numberColor = Color.White,
                controls = {
                    if (showRating) {
                        Text(
                            text = buildAnnotatedString {
                                append("- Rating ")
                                withStyle(
                                    style = SpanStyle(fontWeight = FontWeight.Bold)
                                ) {
                                    append("${quote.rating ?: 0}")
                                }
                                append(" -")
                            },
                            color = Color(0xFF333333),
                            style = labelMedium
                        )
                    }
                    voteControls(Color(0xFF757575), Color(0xFF757575))
                    Spacer(Modifier.weight(1f))
                    flagIconButton(Color(0xFF757575), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // xkcdb: pale bar with the site name left and the quote number
        // right; the row just under it has the vote links at the far left,
        // the site's xkcd-style header figures in the middle, and the
        // flag plus share/favorite/menu actions at the far right.
        SiteStyle.XKCDB -> {
            UnifiedSiteHeader(
                dbId = dbId,
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "#${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(Color(0xFFD7DCE5)),
                nameColor = Color(0xFF1A237E),
                numberColor = Color(0xFF1A237E),
                controls = {
                    if (showRating) {
                        voteControls(Color(0xFF2E9E44), Color(0xFFD32F2F))
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(30.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.db_header_xkcdb_strip),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    flagIconButton(Color(0xFF2020DD), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // furcqdb: orange bar with the site name left and "Quote #N"
        // right; the row just under it has the "Current score: N" line
        // with the orange vote links at the far left, then the flag and
        // the share/favorite/menu actions at the far right.
        SiteStyle.FURCQDB -> {
            UnifiedSiteHeader(
                dbId = dbId,
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "Quote #${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(Color(0xFFFF8C00)),
                nameColor = Color.Black,
                numberColor = Color.Black,
                controls = {
                    val orange = Color(0xFFFFA726)
                    if (showRating && quote.rating != null) {
                        Text(
                            text = "Current score:",
                            style = labelSmall,
                            color = Color(0xFFCCCCCC)
                        )
                        Text(
                            text = "${quote.rating}",
                            style = labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        // Tight vote links, snug against the score like the
                        // site's "Current score: 2 + -" line.
                        voteControls(orange, orange)
                    }
                    Spacer(Modifier.weight(1f))
                    flagIconButton(orange, onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // I-Rox: orange bar with the site name left and the quote number
        // right; the row just under it has the site's own "-"/"+ " text
        // vote links at the far left, then the flag and the
        // share/favorite/menu actions at the far right.
        SiteStyle.IROX -> {
            UnifiedSiteHeader(
                dbId = dbId,
                siteName = siteHeaderTitle(style, hideTldInHeader),
                numberText = if (showNumber) "#${quote.number}" else null,
                showQueuedTag = showQueuedTag,
                centeredLogoArt = centeredLogoArt,
                barBackground = Modifier.background(Color(0xFFE8721C)),
                nameColor = Color.White,
                numberColor = Color.White,
                controls = {
                    if (showRating) {
                        voteControls(Color(0xFFE8721C), Color(0xFFE8721C))
                    }
                    Spacer(Modifier.weight(1f))
                    // v1.61.11: the report affordance is a flag icon on
                    // every theme (was the site's "[x]" text link).
                    flagIconButton(Color(0xFFE8721C), onFlagClick, isPinned = isPinned, onTogglePin = onTogglePin)
                    headerActions()
                }
            )
        }
        // bash.org: the site's single-line quote header — v1.88.34: element
        // order corrected to match the real bash.org layout per Clint:
        // #151227 - (11117) + [X]. The "-" and "+" vote links and the
        // green parenthesized score always show (v1.77 behavior kept);
        // only the ORDER was wrong. Air between each element, like the
        // real site. The [X] text link stays as the flag affordance (the
        // flag icon stays hidden; the Favorites pin sits in its spot).
        // The share/favorite/menu actions sit at the far right of the
        // row, just under the gold header bar.
        SiteStyle.BASH -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val gold = Color(0xFFCD8C00)
                if (showNumber) {
                    Text(
                        text = "#${quote.number}",
                        color = gold,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                    Spacer(Modifier.width(4.dp))
                }
                // v1.88.34: bash.org order — downvote link, score, upvote
                // link. Links only render when there's a rate handler.
                if (bashOnRate != null) {
                    bashTightLink(
                        "-",
                        onClick = { bashOnRate?.invoke(if (bashMyRating == -1) 0 else -1) },
                        color = if (bashMyRating == -1) gold else gold.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = "(${quote.rating ?: 0})",
                    color = Color(0xFF3D7A3D),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (bashOnRate != null) {
                    Spacer(Modifier.width(4.dp))
                    bashTightLink(
                        "+",
                        onClick = { bashOnRate?.invoke(if (bashMyRating == 1) 0 else 1) },
                        color = if (bashMyRating == 1) gold else gold.copy(alpha = 0.6f)
                    )
                }
                if (onFlagClick != null) {
                    Spacer(Modifier.width(4.dp))
                    bashTightLink(
                        "[X]",
                        onClick = onFlagClick,
                        color = gold
                    )
                }
                Spacer(Modifier.weight(1f))
                // v1.77: the flag icon is gone on bash.org — [X] flags the
                // quote. The Favorites pin sits in the flag's old spot.
                flagIconButton(gold, onFlagClick, hideFlag = true, isPinned = isPinned, onTogglePin = onTogglePin)
                headerActions()
                if (showQueuedTag) QueuedTag()
            }
        }
        else -> {
            // CLASSIC_QDB / CRAPSHACK / MODERN_CARD never reach the mini
            // header; they keep their legacy presentations.
        }
    }
}

/**
 * The quote body container per site: thin bordered boxes on every site
 * theme except Bash.org (borderless, like the original site).
 *
 * v1.55: the box background is the per-database "Quote background" theme
 * slot, and the body text is wrapped in a contrasting content color so it
 * always reads (dark text on light cards like furcqdb/qdb.us/xkcdb, light
 * text on dark cards).
 */
@Composable
private fun SiteBodyBox(
    style: SiteStyle,
    quoteBackground: Color,
    body: @Composable () -> Unit
) {
    // The body text always sits on the quote background (directly on the
    // card for chromeless styles, inside the box for boxed styles), so one
    // contrasting content color covers every branch.
    val bodyContentColor = contentOn(quoteBackground)
    @Composable
    fun contrastingBody() {
        CompositionLocalProvider(LocalContentColor provides bodyContentColor) {
            // The nick colorizer also keys off the quote background.
            CompositionLocalProvider(LocalQuoteBackground provides quoteBackground) {
                body()
            }
        }
    }
    when (style) {
        // QDB.MIT.edu: nearly-white body with a thin border.
        SiteStyle.QDB_MIT -> Box(
            Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(quoteBackground, RoundedCornerShape(2.dp))
                .border(BorderStroke(1.dp, Color(0xFFE0CFA8)), RoundedCornerShape(2.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) { contrastingBody() }
        // BurntElectrons: white beveled card, thin gray border.
        SiteStyle.BURNT_ELECTRONS -> Box(
            Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .background(quoteBackground, RoundedCornerShape(4.dp))
                .border(BorderStroke(1.dp, Color(0xFFBBBBBB)), RoundedCornerShape(4.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) { contrastingBody() }
        // furcqdb: tan card with a thin orange-brown border, indented
        // from the left like the original.
        SiteStyle.FURCQDB -> Box(
            Modifier
                .padding(start = 28.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                .background(quoteBackground, RoundedCornerShape(2.dp))
                .border(BorderStroke(1.dp, Color(0xFFB07A3F)), RoundedCornerShape(2.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) { contrastingBody() }
        // I-Rox: body text indented inside the light-gray card, with the
        // thin bordered body like the other site themes (v1.61.10).
        SiteStyle.IROX -> Box(
            Modifier
                .padding(start = 28.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                .border(
                    BorderStroke(1.dp, siteInnerBorderColor(style, siteBarColor(style))),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) { contrastingBody() }
        // v1.61.10: every site theme gets the thin bordered quote body —
        // except Bash.org, which stays borderless like the original site.
        else -> {
            val bordered = style != SiteStyle.BASH
            Box(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .then(
                        if (bordered) Modifier.border(
                            BorderStroke(1.dp, siteInnerBorderColor(style, siteBarColor(style))),
                            RoundedCornerShape(4.dp)
                        ) else Modifier
                    )
                    .then(
                        if (bordered) Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        else Modifier
                    )
            ) { contrastingBody() }
        }
    }
}

/**
 * The mini-site footer: each site's own closing line, carrying the quote
 * load time and the database's total quote count.
 *
 * v1.55.1: the footer never shows the individual quote number (it lives in
 * the header / vote lines, v1.54 positions) and never shows pending/queued
 * counts. furcqdb's quote line ("Quote #N Current score: N + -") is back in
 * the header; its footer carries load time + total like the others.
 */
/**
 * v1.54 mini-site footer: the quote database's total quote count.
 *
 * v1.55.1: the footer never shows the individual quote number (it lives in
 * the header / vote lines, v1.54 positions) and never shows pending/queued
 * counts.
 *
 * v1.61.18: every footer is a bar-colored strip like the header (Clint: the
 * white/uncolored footers looked unfinished next to the colored headers),
 * and the bottom-left text is gone — no site version label, no render
 * time. Only the database total stays, right-aligned in the on-bar color.
 * QuoteIRC keeps its authentic copyright footer.
 */
@Composable
private fun SiteMiniFooter(
    style: SiteStyle,
    quote: Quote,
    showNumber: Boolean = true,
    showRating: Boolean = true,
    voteControls: @Composable RowScope.(upTint: Color, downTint: Color) -> Unit = { _, _ -> },
    loadSecs: Double? = null,
    dbTotal: Int? = null
) {
    val labelStyle = MaterialTheme.typography.labelSmall
    val bar = siteBarColor(style)
    val onBar = siteOnBarColor(style)
    when (style) {
        SiteStyle.QUOTEIRC -> {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF606060))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // v1.54: the site's own footer wording.
                Text(
                    text = "Copyrighted \u00a9 2011 QuoteIRC \u2014 All content posted " +
                        "by QI Users falls under the public domain license.",
                    style = labelStyle,
                    color = Color.White
                )
            }
            // v1.61.18: stats bar under the credit bar — bar-colored like
            // the header now, total only (no render time).
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bar)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                footerTotalOnly(dbTotal, onBar, labelStyle)
            }
        }
        // bash.org: the site's own dark-gold footer bar — database total
        // right, black on gold. The card ends here: no white action strip
        // below (overflow + favorite moved to the top right).
        SiteStyle.BASH -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFCD8C00))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                footerTotalOnly(dbTotal, Color.Black, labelStyle)
            }
        }
        else -> {
            // v1.61.18: bar-colored footer like the header; total only.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(bar)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                footerTotalOnly(dbTotal, onBar, labelStyle)
            }
        }
    }
}

/**
 * v1.61.18: the mini-site footer total, right-aligned — the only thing
 * left in the footer after the bottom-left text (site version + render
 * time) was removed.
 */
@Composable
private fun RowScope.footerTotalOnly(
    dbTotal: Int?,
    color: Color,
    labelStyle: androidx.compose.ui.text.TextStyle
) {
    if (dbTotal != null) {
        Spacer(Modifier.weight(1f))
        Text(
            text = "%,d quotes".format(Locale.US, dbTotal),
            style = labelStyle,
            color = color
        )
    }
}

/** Card container (shape, border, background) for a mini-site style. */
private fun miniCardContainer(
    style: SiteStyle,
    quoteBackground: Color
): Triple<androidx.compose.ui.graphics.Shape, BorderStroke?, Color> = when (style) {
    // v1.55: the Card container is the quote-background slot wherever the
    // quote text sits directly on the card; styles with a separate body
    // box (MIT, BurntElectrons, furcqdb) keep their site page color here
    // and the box itself uses the slot (see SiteBodyBox).
    SiteStyle.QDB_MIT -> Triple(
        RoundedCornerShape(8.dp), BorderStroke(1.dp, Color(0xFFE0D0B8)), Color.White
    )
    SiteStyle.QUOTEIRC -> Triple(
        RoundedCornerShape(4.dp), BorderStroke(1.dp, Color(0xFFDFDFDD)), quoteBackground
    )
    SiteStyle.BURNT_ELECTRONS -> Triple(
        RoundedCornerShape(6.dp), BorderStroke(1.dp, Color(0xFFBBBBBB)), Color.White
    )
    SiteStyle.WARPDRIVE -> Triple(
        RoundedCornerShape(12.dp), BorderStroke(1.dp, Color(0xFFB0BEC5)), quoteBackground
    )
    // WQDB / xkcdb / bash.org / bash.im: chromeless rows on the quote
    // background. (WQDB odd-row darkening is applied by the caller.)
    SiteStyle.WQDB -> Triple(RoundedCornerShape(0.dp), null, quoteBackground)
    SiteStyle.XKCDB -> Triple(RoundedCornerShape(0.dp), null, quoteBackground)
    SiteStyle.BASH -> Triple(RoundedCornerShape(0.dp), null, quoteBackground)
    SiteStyle.BASHIM -> Triple(RoundedCornerShape(0.dp), null, quoteBackground)
    // furcqdb: the black page; the tan quote box carries the chrome.
    SiteStyle.FURCQDB -> Triple(RoundedCornerShape(0.dp), null, Color.Black)
    SiteStyle.IROX -> Triple(
        RoundedCornerShape(2.dp), BorderStroke(1.dp, Color(0xFFD8D8D8)), quoteBackground
    )
    else -> Triple(RoundedCornerShape(8.dp), BorderStroke(1.dp, quoteBackground), quoteBackground)
}

/**
 * One quote card rendered as a mini version of its database's original
 * site: the site's own masthead, quote info line, body treatment, and
 * footer. qdb.us keeps its blue-era presentation and CrapShack its
 * approved deep-blue bars; everything else is rebuilt per-site in v1.54.
 *
 * The palette ([MaterialTheme] color scheme) and nick colors are applied
 * by the caller; this composable only owns the site chrome.
 *
 * @param body the quote body (already styled, e.g. monospace where the
 * site uses it).
 * @param actions the interactive row (overflow, favorite): rating thumbs
 * live in the themed header via [voteControls], never duplicated here.
 * @param voteControls the working offline +/- controls, rendered in the
 * themed header (up/down tints follow the site's own vote colors).
 */
@Composable
fun SiteQuoteCard(
    style: SiteStyle,
    quote: Quote,
    /** Centered header logo drawable res, or null for no logo (default). */
    centeredLogoArt: Int? = null,
    /** v1.61.11: per-database "hide TLD" header title option. */
    hideTldInHeader: Boolean = false,
    showNumber: Boolean,
    showRating: Boolean,
    showQueuedTag: Boolean,
    /** WQDB alternating row shades: even/odd list index, null = even. */
    listIndex: Int?,
    /** v1.53 report/flag: header flag affordance; null hides it. */
    onFlagClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    /** v1.54: bash.org renders overflow + favorite at the top right. */
    headerActions: @Composable RowScope.() -> Unit = {},
    voteControls: @Composable RowScope.(upTint: Color, downTint: Color) -> Unit = { _, _ -> },
    /**
     * v1.55.1: the per-database "Quote background" theme slot — the surface
     * behind the quote body text. Site themes use their authentic body
     * colors as the built-in default (e.g. furcqdb tan, qdb.us white);
     * the body text color is derived via [contentOn] so it always
     * contrasts (dark text on light cards, light text on dark cards).
     */
    quoteBackground: Color = Color.Black,
    /**
     * v1.55.1: id of the database this quote came from, used to look up
     * the database's total quote count for the footer ("21,096 quotes").
     */
    dbId: String? = null,
    /** v1.77: Favorites pin toggle; null hides the pin. */
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null,
    /**
     * v1.77: bash.org's vote links always show (authentic site chrome), so
     * it gets the ungated rate handler + local rating directly.
     */
    bashMyRating: Int = 0,
    bashOnRate: ((Int) -> Unit)? = null
) {
    // Black page background outside the bordered quotes is a main-theme
    // option (default ON) — an explicit user setting, so it applies in
    // both modes. v1.57: qdb.us renders on a white page like the original
    // site's screenshot only in single-database mode (single-DB list or
    // single-quote detail); in a mixed-database list the card must not
    // paint white past its own rounded card — the global page background
    // shows through instead.
    val outside = when {
        LocalBlackOutsideQuotes.current -> Modifier.background(Color.Black)
        style == SiteStyle.CLASSIC_QDB && LocalSingleDatabaseMode.current ->
            Modifier.background(Color.White)
        else -> Modifier
    }
    // v1.55.1: per-quote load/render timing for the footer ("how long the
    // quote took to load"): from first composition to post-compose, stable
    // across recompositions.
    val t0 = remember(quote) { System.nanoTime() }
    var loadSecs by remember(quote) { mutableStateOf<Double?>(null) }
    SideEffect {
        if (loadSecs == null) loadSecs = (System.nanoTime() - t0) / 1_000_000_000.0
    }
    // v1.55.1: this quote's database total for the footer.
    val dbTotal = LocalDbTotals.current[dbId]
    // v1.58: this quote's capture date for the quiet footer line, where
    // available. The key is scoped by database id, so other databases'
    // quote numbers can never pick up qdb.us dates.
    val servedDate = dbId?.let { LocalServedDates.current["$it/${quote.number}"] }
    Column(modifier = modifier.then(outside).fillMaxWidth()) {
        when (style) {
            // qdb.us blue era: unchanged v1.53 presentation.
            SiteStyle.CLASSIC_QDB -> {
                val bar = siteBarColor(style)
                val onBar = siteOnBarColor(style)
                // v1.55: the quote body surface is the per-database "Quote
                // background" theme slot (built-in default: white, like
                // the original site).
                val bodyBg = quoteBackground
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color(0xFFD4D4D4)),
                    colors = CardDefaults.cardColors(containerColor = bodyBg)
                ) {
                    Column {
                        // Site-colored header bar.
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(bar)
                        ) {
                            SiteHeaderBar(
                                style = style,
                                quote = quote,
                                showNumber = showNumber,
                                showQueuedTag = showQueuedTag,
                                centeredLogoArt = centeredLogoArt,
                                hideTldInHeader = hideTldInHeader,
                                dbId = dbId
                            )
                        }
                        // v1.61.10: the header controls sit just under the
                        // bar, on the card body — never inside the bar.
                        SiteHeaderControls(
                            style = style,
                            quote = quote,
                            tint = contentOn(bodyBg),
                            showRating = showRating,
                            onFlagClick = onFlagClick,
                            voteControls = voteControls,
                            headerActions = headerActions,
                            isPinned = isPinned,
                            onTogglePin = onTogglePin
                        )
                        // qdb.us quote line: #N +(score)- with the +/- controls.
                        QdbQuoteLine(
                            quote = quote,
                            showRating = showRating,
                            showQueuedTag = showQueuedTag,
                            onFlagClick = onFlagClick,
                            voteControls = voteControls
                        )
                        // Bordered quote body. v1.55: body text uses a
                        // contrasting color for the quote background
                        // (dark text on light cards).
                        Box(
                            Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .border(
                                    BorderStroke(1.dp, siteInnerBorderColor(style, bar)),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            CompositionLocalProvider(
                                LocalContentColor provides contentOn(bodyBg)
                            ) {
                                body()
                            }
                        }
                        // Site-colored footer bar.
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(bar)
                        ) {
                            SiteCardFooter(style = style, quote = quote, onBar = onBar, loadSecs = loadSecs, dbTotal = dbTotal, servedDate = servedDate)
                        }
                        // v1.55: bottom action strip removed — overflow and
                        // favorite live at the top right under the header.
                    }
                }
            }
            // CrapShack: Clint-approved deep-blue bars (legacy path).
            SiteStyle.CRAPSHACK -> {
                val bar = siteBarColor(style)
                val onBar = siteOnBarColor(style)
                // v1.55: the quote body surface is the per-database "Quote
                // background" theme slot (built-in default: #141414, like
                // the original site).
                val bodyBg = quoteBackground
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, bar.copy(alpha = 0.65f)),
                    colors = CardDefaults.cardColors(containerColor = bodyBg)
                ) {
                    Column {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(bar)
                        ) {
                            SiteHeaderBar(
                                style = style,
                                quote = quote,
                                showNumber = showNumber,
                                showQueuedTag = showQueuedTag,
                                centeredLogoArt = centeredLogoArt,
                                hideTldInHeader = hideTldInHeader,
                                dbId = dbId
                            )
                        }
                        // v1.61.10: the header controls sit just under the
                        // bar, on the card body — never inside the bar.
                        SiteHeaderControls(
                            style = style,
                            quote = quote,
                            tint = contentOn(bodyBg),
                            showRating = showRating,
                            onFlagClick = onFlagClick,
                            voteControls = voteControls,
                            headerActions = headerActions,
                            isPinned = isPinned,
                            onTogglePin = onTogglePin
                        )
                        // v1.61.10: bordered quote body, like the other
                        // site themes (Bash.org is the only borderless one).
                        Box(
                            Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .border(
                                    BorderStroke(1.dp, siteInnerBorderColor(style, bar)),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            // v1.55: body text contrasts with the quote
                            // background (light text on the dark card).
                            CompositionLocalProvider(
                                LocalContentColor provides contentOn(bodyBg)
                            ) {
                                body()
                            }
                        }
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .background(bar)
                        ) {
                            SiteCardFooter(style = style, quote = quote, onBar = onBar, loadSecs = loadSecs, dbTotal = dbTotal, servedDate = servedDate)
                        }
                        // v1.55: bottom action strip removed — overflow and
                        // favorite live at the top right under the header.
                    }
                }
            }
            // v1.88.23 novelty card themes: bespoke presentations in
            // NoveltyThemes.kt (one arm per family member, dispatched by
            // the theme id through siteStyleFor).
            SiteStyle.DOS, SiteStyle.MACOS, SiteStyle.MACINTOSH,
            SiteStyle.IMESSAGE, SiteStyle.GMESSAGES, SiteStyle.BSOD,
            SiteStyle.LINUX, SiteStyle.MIRC, SiteStyle.TERMINAL,
            SiteStyle.GREEN_MONO, SiteStyle.ANSI, SiteStyle.ASCII,
            SiteStyle.MATRIX, SiteStyle.GREEN_TERM,
            SiteStyle.MESSENGER_LIGHT, SiteStyle.MESSENGER_DARK,
            SiteStyle.AIM, SiteStyle.ICQ, SiteStyle.MSN_MESSENGER,
            // v1.88.38: Clint's personal Friends database theme.
            SiteStyle.FRIENDS -> {
                NoveltyThemeCard(
                    style = style,
                    quote = quote,
                    showNumber = showNumber,
                    showRating = showRating,
                    quoteBackground = quoteBackground,
                    headerActions = headerActions,
                    dbId = dbId,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
            // v1.54 mini-site presentations.
            else -> {
                // v1.55: the quote body surface is the per-database "Quote
                // background" theme slot. WQDB keeps its alternating rows
                // by darkening the slot slightly on odd rows.
                val qb = quoteBackground
                val (shape, border, containerBase) = miniCardContainer(style, qb)
                val container = if (style == SiteStyle.WQDB && (listIndex ?: 0) % 2 == 1) {
                    val f = 0.96f
                    Color(qb.red * f, qb.green * f, qb.blue * f, qb.alpha)
                } else containerBase
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = shape,
                    border = border,
                    colors = CardDefaults.cardColors(containerColor = container)
                ) {
                    Column {
                        // v1.55: quote numbers live in the bottom themed
                        // footer bar, not the masthead.
                        // v1.58: furcqdb gets ONE continuous thin bright-blue
                        // frame around the whole card (header + body +
                        // footer) so the lines run down both sides — this
                        // replaces the v1.57 separate header/footer boxes.
                        // Border-only, no filled bars; the all-black
                        // header/footer stays authentic per the original
                        // site.
                        val headerSection: @Composable () -> Unit = {
                            Column {
                                SiteMasthead(
                                    style,
                                    centeredLogoArt,
                                    quote,
                                    showNumber,
                                    hideTldInHeader = hideTldInHeader,
                                    dbId = dbId
                                )
                                SiteQuoteHeader(
                                    style = style,
                                    quote = quote,
                                    showNumber = showNumber,
                                    showRating = showRating,
                                    showQueuedTag = showQueuedTag,
                                    centeredLogoArt = centeredLogoArt,
                                    hideTldInHeader = hideTldInHeader,
                                    onFlagClick = onFlagClick,
                                    voteControls = voteControls,
                                    headerActions = headerActions,
                                    isPinned = isPinned,
                                    onTogglePin = onTogglePin,
                                    bashMyRating = bashMyRating,
                                    bashOnRate = bashOnRate,
                                    dbId = dbId
                                )
                            }
                        }
                        val footerSection: @Composable () -> Unit = {
                            SiteMiniFooter(
                                style = style,
                                quote = quote,
                                showNumber = showNumber,
                                showRating = showRating,
                                voteControls = voteControls,
                                loadSecs = loadSecs,
                                dbTotal = dbTotal
                            )
                        }
                        if (style == SiteStyle.FURCQDB) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.dp, FurcqdbTitleBlue))
                            ) {
                                Column {
                                    headerSection()
                                    SiteBodyBox(style, qb) { body() }
                                    footerSection()
                                }
                            }
                        } else {
                            headerSection()
                            SiteBodyBox(style, qb) { body() }
                            footerSection()
                        }
                        // v1.55: no bottom action strip on any site theme —
                        // overflow and favorite live at the top right under
                        // the header bar.
                    }
                }
            }
        }
    }
}

/** The body font for a site style: monospace where the site uses it. */
fun siteBodyMonospace(style: SiteStyle): Boolean = siteStyleMonospace(style)
