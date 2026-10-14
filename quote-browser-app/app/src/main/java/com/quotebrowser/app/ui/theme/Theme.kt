package com.quotebrowser.app.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import com.quotebrowser.app.ui.DefaultNickNameColors
import com.quotebrowser.app.ui.MAX_NICK_NAME_COLORS
import com.quotebrowser.app.ui.hexToColorOrNull

data class ThemePreset(
    val id: String,
    val name: String,
    val dark: Boolean,
    val background: Long,
    val surface: Long,
    val primary: Long,
    val onBackground: Long,
    val onSurface: Long,
    /** Background of the #quote-number badge; defaults to the accent color. */
    val number: Long = primary,
    /** Background of the rating badge; defaults to light blue. */
    val rating: Long = 0xFF90CAF9,
    /** Rating number text; defaults to black/white auto-contrast on [rating]. */
    val ratingText: Long =
        if (contentOn(Color(rating)) == Color.White) 0xFFFFFFFF else 0xFF000000,
    /** Default color of the <> around IRC-style <nick> mentions; 0 = none. */
    val nickBrackets: Long = 0xFF7FC4FF,
    /**
     * Default colors for nick names without a personal rule (up to 10).
     * Nicks are assigned one deterministically; empty = names not colored.
     */
    val nickNames: List<Long> = DefaultNickNameColors,
    /**
     * Default colors for the <> brackets around <nick> mentions, paired
     * 1:1 with [nickNames]: the nick that gets name color *i* gets bracket
     * color *i*. Each is a lighter/darker tint of its name color so the two
     * differ but go together. Empty = fall back to [nickBrackets].
     */
    val nickBracketPalette: List<Long> =
        nickNames.map { defaultBracketTint(it, dark) },
    /** Color of whole IRC action lines ("* nick does something"); 0 = none. */
    val nickAction: Long = 0xFF4CAF50,
    /**
     * When true, the action-line color covers the nick too; when false the
     * nick keeps its personal/default name color inside action lines.
     */
    val actionOverridesName: Boolean = true,
    /**
     * When true, a speaker's message text ("<nick> words", "Nick: words")
     * is colored with their name color (or bracket color when the name
     * has none). Off by default.
     */
    val colorMessageWithSpeaker: Boolean = false,
    /**
     * Color of the dividers separating quote headers/bodies and list rows;
     * 0 = none (invisible). Defaults to a subtle neutral per theme darkness.
     */
    val divider: Long = if (dark) 0xFF494949 else 0xFFCAC4D0,
    /**
     * v1.55: the surface directly behind the quote body text on the quote
     * cards, per database theme. Defaults to black; editable in the theme
     * editor ("Quote background") and resettable to this default.
     */
    val quoteBackground: Long = 0xFF000000
)

/**
 * DevXen theme background-art placement (Settings → Themes, only when the
 * DevXen theme is active). Default: the art sits behind the quote lists.
 */
enum class DevXenBgMode(val prefValue: String, val label: String) {
    BEHIND_QUOTES("behind", "Behind quotes"),
    ON_EACH_QUOTE("card", "On each quote"),
    COLORS_ONLY("colors", "Colors only");

    companion object {
        fun fromPref(value: String?): DevXenBgMode =
            entries.firstOrNull { it.prefValue == value } ?: BEHIND_QUOTES
    }
}

/**
 * The DevXen background-art mode, non-null only when DevXen is the
 * effective theme. Provided at the app root in MainActivity.
 */
val LocalDevXenBgMode = compositionLocalOf<DevXenBgMode?> { null }

/**
 * v1.61.3: the user's own DevXen background picture (null = use the
 * bundled DevXen artwork). Loaded once at the app root in MainActivity
 * and provided here so every DevXen background surface — lists, quote
 * cards, share cards — uses the same picture.
 */
val LocalDevXenCustomBg = compositionLocalOf<androidx.compose.ui.graphics.ImageBitmap?> { null }

/**
 * Clint's DevXen artwork, dimmed with a black scrim so light quote text
 * stays legible over it. Draws into the given [modifier]'s bounds.
 */
@Composable
fun DevXenDimmedBackground(
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.6f
) {
    Box(modifier) {
        // v1.61.3: the user's own picture replaces the bundled DevXen
        // artwork everywhere this background is drawn.
        val customBg = LocalDevXenCustomBg.current
        if (customBg != null) {
            Image(
                bitmap = customBg,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Image(
                painter = painterResource(id = com.quotebrowser.app.R.drawable.devxen_bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
        )
    }
}

/**
 * Whether the Quote404 theme is the effective global theme. Provided at
 * the app root in MainActivity; when true the app draws Clint's Quote404
 * artwork behind the lists and tints quote cards to match.
 */
val LocalQuote404Theme = compositionLocalOf { false }

/**
 * Clint's Quote404 artwork, dimmed with a black scrim so light quote text
 * stays legible over it. Draws into the given [modifier]'s bounds.
 */
@Composable
fun Quote404DimmedBackground(
    modifier: Modifier = Modifier,
    scrimAlpha: Float = 0.6f
) {
    Box(modifier) {
        Image(
            painter = painterResource(id = com.quotebrowser.app.R.drawable.quote404_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
        )
    }
}

/** Black or white, whichever reads better on the given background color. */
fun contentOn(bg: Color): Color {
    val luminance = bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f
    return if (luminance > 0.5f) Color.Black else Color.White
}

/**
 * Default bracket color paired with a name color: the same hue family,
 * shifted lighter (dark themes) or darker (light themes) so brackets and
 * names read as a pair but are never the same color.
 */
fun defaultBracketTint(nameColor: Long, dark: Boolean): Long {
    val base = Color(nameColor)
    val target = if (dark) Color.White else Color.Black
    val ratio = if (dark) 0.45f else 0.30f
    val c = Color(
        red = base.red + (target.red - base.red) * ratio,
        green = base.green + (target.green - base.green) * ratio,
        blue = base.blue + (target.blue - base.blue) * ratio
    )
    return c.toArgb().toLong() and 0xFFFFFFFFL
}

/**
 * Bracket-palette hexes ("RRGGBB") for the theme editor: the explicitly
 * saved palette wins; "-" ([bracketsSingle]) means empty (the single <>
 * color is used); otherwise tints of the name colors are generated.
 */
fun defaultBracketHexes(
    saved: List<String>,
    nameHexes: List<String>,
    bracketsSingle: Boolean,
    bgHex: String
): List<String> = when {
    bracketsSingle -> emptyList()
    saved.isNotEmpty() -> saved
    else -> {
        val bg = hexToColorOrNull(bgHex) ?: Color.Black
        val dark = bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f < 0.5f
        nameHexes.mapNotNull { hexToColorOrNull(it) }.map {
            val argb = defaultBracketTint(it.toArgb().toLong() and 0xFFFFFFFFL, dark)
            "%06X".format(argb and 0xFFFFFFL)
        }
    }
}

/**
 * "FFFFFF" or "000000", whichever reads better on the given RRGGBB hex.
 * Used as the default rating-text color when a theme doesn't set one.
 */
fun contrastHex(bgHex: String): String {
    val bg = try {
        Color(("FF${bgHex.trim().removePrefix("#")}").toLong(16))
    } catch (e: Exception) {
        return "000000"
    }
    return if (contentOn(bg) == Color.White) "FFFFFF" else "000000"
}

/**
 * Subtle divider default ("494949"/"CAC4D0" RRGGBB) following the given
 * background hex's darkness. Used when a theme doesn't set a divider color.
 */
fun defaultDividerHex(bgHex: String): String {
    val bg = try {
        Color(("FF${bgHex.trim().removePrefix("#")}").toLong(16))
    } catch (e: Exception) {
        return "494949"
    }
    val luminance = bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f
    return if (luminance < 0.5f) "494949" else "CAC4D0"
}

/** Mixes [a] toward [b] by [ratio] (0 = all a, 1 = all b). */
private fun blend(a: Color, b: Color, ratio: Float): Color {
    val r = ratio.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * r,
        green = a.green + (b.green - a.green) * r,
        blue = a.blue + (b.blue - a.blue) * r,
        alpha = 1f
    )
}

/** A user-created theme: a display name plus color slots.
 *
 * The colors string is
 * "bg,surface,primary,onBg,onSurface[,number[,rating[,ratingText[,nickBrackets[,legacyNickName[,nickAction[,nickNames]]]]]]]]"
 * hex (with or without #/0x). Position 9 (legacyNickName) is the old single
 * default name color, kept only to migrate older themes; the editor always
 * writes it blank. nickNames is ";"-separated (up to 10). The nickBrackets
 * and nickAction slots accept "none" to leave them uncolored; blank means
 * the default (light blue brackets, green actions).
 */
data class CustomTheme(
    val name: String,
    val colors: String
)

/** Theme id for the custom theme at [index] in the user's saved list. */
fun customThemeId(index: Int): String = "custom:$index"

/**
 * Global quote-text size multiplier (0.8–1.5), provided at the activity root
 * from the user's setting. Quote bodies multiply their font size by this.
 */
val LocalTextScale = compositionLocalOf { 1f }

/**
 * Compact quote-list density (tighter padding, smaller text), provided at the
 * activity root from the user's setting.
 */
val LocalCompactMode = compositionLocalOf { false }

/** Returns [base] with its font size multiplied by the current [LocalTextScale]. */
@Composable
fun scaledTextStyle(base: TextStyle): TextStyle {
    val scale = LocalTextScale.current
    return remember(base, scale) {
        if (base.fontSize != TextUnit.Unspecified) base.copy(fontSize = base.fontSize * scale)
        else base
    }
}

val ThemePresets = listOf(
    ThemePreset("system", "System default", true, 0, 0, 0, 0, 0), // special-cased
    ThemePreset("dynamic", "Material You", true, 0, 0, 0, 0, 0), // special-cased
    // Quote404: Clint's new app branding (v1.61.5). Electric neon blue on
    // pure black, silver-blue linework; his artwork sits behind the lists
    // and on Quote404 share cards. This is the default theme.
    ThemePreset(
        "quote404", "Quote404", true,
        background = 0xFF000000, surface = 0xFF0A0E14, primary = 0xFF0090FF,
        onBackground = 0xFFFFFFFF, onSurface = 0xFFFFFFFF, number = 0xFF0090FF,
        divider = 0xFF2E4A66
    ),
    ThemePreset(
        "midnight", "Midnight Blue", true,
        background = 0xFF000000, surface = 0xFF000000, primary = 0xFF008CFF,
        onBackground = 0xFFFFFFFF, onSurface = 0xFFFFFFFF
    ),
    // DevXen: Clint's own artwork as an optional background (Settings →
    // Themes picks the placement). Electric blue accent, pure black page,
    // silver-gray linework for dividers/secondary text.
    ThemePreset(
        "devxen", "DevXen", true,
        background = 0xFF000000, surface = 0xFF101010, primary = 0xFF087AFF,
        onBackground = 0xFFFFFFFF, onSurface = 0xFFFFFFFF, number = 0xFF087AFF,
        divider = 0xFF7A7A7A
    ),
    ThemePreset(
        "bashorg", "Bash.org", false,
        background = 0xFFFFFFFF, surface = 0xFFF5F5F5, primary = 0xFFC08100,
        onBackground = 0xFF000000, onSurface = 0xFF000000, number = 0xFFC08100,
        quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "bashorg_alt", "Bash.org Alt", true,
        background = 0xFF000000, surface = 0xFF1A1A1A, primary = 0xFFFF9900,
        onBackground = 0xFFFFFFFF, onSurface = 0xFFF2F2F2, number = 0xFFFF9900
    ),
    // v1.51 database site themes: mini versions of each quote database's
    // original site. All are normal global themes and all are directly
    // editable (per-preset color overrides); the hardcoded values below are
    // the never-lost defaults.
    ThemePreset(
        "db_qdb_us", "qdb.us (QDB Classic)", false,
        background = 0xFFFFFFFF, surface = 0xFFFFFFFF, primary = 0xFF8098B0,
        onBackground = 0xFF000000, onSurface = 0xFF000000, number = 0xFF8098B0,
        quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "db_qdb_mit", "QDB.MIT.edu", false,
        background = 0xFFFFFFFF, surface = 0xFFFFFCEC, primary = 0xFF993333,
        onBackground = 0xFF000000, onSurface = 0xFF1A1A1A, number = 0xFF4D0099,
        rating = 0xFFFFE9C9, quoteBackground = 0xFFFFFCEC
    ),
    ThemePreset(
        "db_quoteirc", "QuoteIRC.com", false,
        background = 0xFFFFFFFF, surface = 0xFFFAFAFA, primary = 0xFFA52B2B,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFFA52B2B,
        rating = 0xFFFEEDBA, quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "db_burntelectrons", "BurntElectrons", false,
        background = 0xFFE8E8E8, surface = 0xFFFFFFFF, primary = 0xFF990000,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFF990000,
        quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "db_warpdrive", "Warpdrive.se", false,
        background = 0xFFE7E7E7, surface = 0xFFFAFAFA, primary = 0xFF0099CC,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFF0099CC,
        quoteBackground = 0xFFFAFAFA
    ),
    // v1.88: bash.im (Цитатник Рунета) — the 2019-2021 minimalist light
    // theme, reconstructed (see translation-work/bashim/bashim_theme.md):
    // white page, white quote surface, dark-gray body text, muted blue
    // links, hairline dividers. The archived CSS was not recoverable, so
    // these are labeled reconstructions, not pixel-truth.
    ThemePreset(
        "db_bashim", "bash.im", false,
        background = 0xFFFFFFFF, surface = 0xFFFFFFFF, primary = 0xFF2B6CB0,
        onBackground = 0xFF333333, onSurface = 0xFF333333, number = 0xFF2B6CB0,
        divider = 0xFFE5E5E5, quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "db_wqdb", "WQDB.org", false,
        background = 0xFFFFFFFF, surface = 0xFFF2F2F2, primary = 0xFF003366,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFF1E5AA8,
        quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "db_xkcdb", "xkcdb.com", false,
        background = 0xFFDCDCE6, surface = 0xFFFFFFFF, primary = 0xFF2D5FA8,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFF2D5FA8,
        quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "db_furcqdb", "furcqdb (Furcadia)", true,
        background = 0xFF000000, surface = 0xFFD8CFC0, primary = 0xFFFF8C00,
        onBackground = 0xFFE8E8E8, onSurface = 0xFF1A1A1A, number = 0xFFFF8C00,
        quoteBackground = 0xFFD6C9B4
    ),
    ThemePreset(
        "db_irox", "I-Rox.com", false,
        background = 0xFFF5F5F5, surface = 0xFFEFEFEF, primary = 0xFFCC5500,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFFCC5500,
        quoteBackground = 0xFFF7F7F7
    ),
    ThemePreset(
        "db_crapshack", "CrapShack", true,
        background = 0xFF000000, surface = 0xFF141414, primary = 0xFFFF9900,
        onBackground = 0xFF9A9A9A, onSurface = 0xFF9A9A9A, number = 0xFF4AA3FF,
        quoteBackground = 0xFF141414
    ),
    // v1.88.38: "Sunset Hangout" — deep plum night, hot pink + amber.
    // Built for Clint's Friends database; v1.89.2: Clint liked it enough
    // to keep it as a general preset (id stays "db_friends" so stored
    // settings don't break), while the Friends database itself defaults
    // to the new "Friends — Wall" theme below.
    ThemePreset(
        "db_friends", "Sunset Hangout", true,
        background = 0xFF1E1033, surface = 0xFF2A1846, primary = 0xFFFF4D8D,
        onBackground = 0xFFFFF6EC, onSurface = 0xFFFFF6EC, number = 0xFFFFB020,
        divider = 0xFF4A3568, quoteBackground = 0xFF2A1846
    ),
    // v1.89.2: "Friends — Wall" — the Friends database's new default:
    // nostalgic early-Facebook wall-post era (2010–2013). Classic
    // Facebook blue #3B5998 over a pale blue-gray paper page, white
    // cards, era body-text gray. Light theme.
    ThemePreset(
        "db_friends_wall", "Friends — Wall", false,
        background = 0xFFECEFF5, surface = 0xFFFFFFFF, primary = 0xFF3B5998,
        onBackground = 0xFF333333, onSurface = 0xFF333333, number = 0xFF365899,
        divider = 0xFFD3D9E6, quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "dark", "Dark", true,
        background = 0xFF121212, surface = 0xFF1E1E1E, primary = 0xFFBB86FC,
        onBackground = 0xFFE0E0E0, onSurface = 0xFFE0E0E0
    ),
    ThemePreset(
        "light", "Light", false,
        background = 0xFFFFFBFE, surface = 0xFFF7F2FA, primary = 0xFF6750A4,
        onBackground = 0xFF1C1B1F, onSurface = 0xFF1C1B1F
    ),
    ThemePreset(
        "amoled", "AMOLED Black", true,
        background = 0xFF000000, surface = 0xFF0A0A0A, primary = 0xFFBB86FC,
        onBackground = 0xFFFFFFFF, onSurface = 0xFFE0E0E0
    ),
    ThemePreset(
        "dracula", "Dracula", true,
        background = 0xFF282A36, surface = 0xFF343746, primary = 0xFFBD93F9,
        onBackground = 0xFFF8F8F2, onSurface = 0xFFF8F8F2
    ),
    ThemePreset(
        "nord", "Nord", true,
        background = 0xFF2E3440, surface = 0xFF3B4252, primary = 0xFF88C0D0,
        onBackground = 0xFFECEFF4, onSurface = 0xFFE5E9F0
    ),
    ThemePreset(
        "solarized", "Solarized Dark", true,
        background = 0xFF002B36, surface = 0xFF073642, primary = 0xFF268BD2,
        onBackground = 0xFF839496, onSurface = 0xFF93A1A1
    ),
    ThemePreset(
        "forest", "Forest", true,
        background = 0xFF1A2E1A, surface = 0xFF243824, primary = 0xFF7CB342,
        onBackground = 0xFFE8F5E9, onSurface = 0xFFE8F5E9
    ),
    ThemePreset(
        "sunset", "Sunset", false,
        background = 0xFFFFF3E0, surface = 0xFFFFE0B2, primary = 0xFFE65100,
        onBackground = 0xFF3E2723, onSurface = 0xFF3E2723
    ),
    // v1.88.23 novelty card themes: full custom card presentations (see
    // ui/components/NoveltyThemes.kt). Each also works as a plain palette
    // (global theme or per-database Modern layout); with the Site layout
    // the database's cards render the bespoke presentation.
    ThemePreset(
        "theme_dos", "DOS", true,
        background = 0xFF000000, surface = 0xFF0A0A0A, primary = 0xFFE8E8E8,
        onBackground = 0xFFF5F5F5, onSurface = 0xFFF5F5F5, number = 0xFFE8E8E8,
        quoteBackground = 0xFF000000
    ),
    ThemePreset(
        "theme_macos", "macOS", false,
        background = 0xFFF5F5F7, surface = 0xFFFFFFFF, primary = 0xFF007AFF,
        onBackground = 0xFF1D1D1F, onSurface = 0xFF1D1D1F, number = 0xFF007AFF,
        quoteBackground = 0xFFF5F5F7
    ),
    ThemePreset(
        "theme_macintosh", "Macintosh", false,
        background = 0xFFFFFFFF, surface = 0xFFFFFFFF, primary = 0xFF000000,
        onBackground = 0xFF000000, onSurface = 0xFF000000, number = 0xFF000000,
        divider = 0xFF000000, quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "theme_imessage", "iMessage", false,
        background = 0xFFFFFFFF, surface = 0xFFF2F2F7, primary = 0xFF0A84FF,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFF0A84FF,
        quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "theme_gmessages", "Google Messages", false,
        background = 0xFFFFFFFF, surface = 0xFFFDFBFF, primary = 0xFF0B57D0,
        onBackground = 0xFF1A1C1E, onSurface = 0xFF1A1C1E, number = 0xFF0B57D0,
        quoteBackground = 0xFFFFFFFF
    ),
    ThemePreset(
        "theme_bsod", "BSOD", true,
        background = 0xFF0000AA, surface = 0xFF0000AA, primary = 0xFFFFFFFF,
        onBackground = 0xFFFFFFFF, onSurface = 0xFFFFFFFF, number = 0xFFFFFFFF,
        divider = 0L, quoteBackground = 0xFF0000AA
    ),
    ThemePreset(
        "theme_linux", "Linux", true,
        background = 0xFF000000, surface = 0xFF0A0A0A, primary = 0xFF33FF66,
        onBackground = 0xFF33FF66, onSurface = 0xFF33FF66, number = 0xFF33FF66,
        quoteBackground = 0xFF000000
    ),
    // mIRC: nicks cycle the classic mIRC color palette (blue, red,
    // green, purple, brown, orange, teal, pink...) via the normal
    // deterministic assignment.
    ThemePreset(
        "theme_mirc", "mIRC", false,
        background = 0xFFFFFFFF, surface = 0xFFFFFFFF, primary = 0xFF000080,
        onBackground = 0xFF1A1A1A, onSurface = 0xFF1A1A1A, number = 0xFF000080,
        quoteBackground = 0xFFFFFFFF,
        nickNames = listOf(
            0xFF0000CC, 0xFFCC0000, 0xFF009300, 0xFF7F007F, 0xFF7F4A00,
            0xFFFC7F00, 0xFF009393, 0xFFFF00CC, 0xFF555555, 0xFF000000
        )
    ),
    ThemePreset(
        "theme_terminal", "Terminal", true,
        background = 0xFF0D1117, surface = 0xFF161B22, primary = 0xFFE6EDF3,
        onBackground = 0xFFE6EDF3, onSurface = 0xFFE6EDF3, number = 0xFFE6EDF3,
        quoteBackground = 0xFF0D1117
    ),
    // Green monochrome: P1 phosphor — nick colors stay in the green family.
    ThemePreset(
        "theme_greenmono", "Green Monochrome", true,
        background = 0xFF000000, surface = 0xFF000000, primary = 0xFF33FF33,
        onBackground = 0xFF33FF33, onSurface = 0xFF33FF33, number = 0xFF33FF33,
        quoteBackground = 0xFF000000,
        nickNames = listOf(
            0xFF33FF33, 0xFF66FF66, 0xFF00CC00, 0xFF99FF99, 0xFF009900,
            0xFF33FF33, 0xFF66FF66, 0xFF00CC00, 0xFF99FF99, 0xFF009900
        )
    ),
    // v1.88.25 textmode novelty card themes: full custom card
    // presentations (see ui/components/NoveltyThemes.kt).
    // ANSI: BBS ANSI-art — nicks cycle the bright ANSI palette.
    ThemePreset(
        "theme_ansi", "ANSI", true,
        background = 0xFF000000, surface = 0xFF0A0A0A, primary = 0xFF55FFFF,
        onBackground = 0xFFFFFFFF, onSurface = 0xFFFFFFFF, number = 0xFFFFFF55,
        quoteBackground = 0xFF000000,
        nickNames = listOf(
            0xFFFF5555, 0xFF55FF55, 0xFFFFFF55, 0xFF5555FF, 0xFFFF55FF,
            0xFF55FFFF, 0xFFFFFFFF, 0xFFFF5555, 0xFF55FF55, 0xFFFFFF55
        )
    ),
    // ASCII: classic .nfo — strictly monochrome, nicks in gray shades.
    ThemePreset(
        "theme_ascii", "ASCII", true,
        background = 0xFF0C0C0C, surface = 0xFF111111, primary = 0xFFCCCCCC,
        onBackground = 0xFFCCCCCC, onSurface = 0xFFCCCCCC, number = 0xFFCCCCCC,
        quoteBackground = 0xFF0C0C0C,
        nickNames = listOf(
            0xFFCCCCCC, 0xFFAAAAAA, 0xFF999999, 0xFFBBBBBB, 0xFF888888,
            0xFFCCCCCC, 0xFFAAAAAA, 0xFF999999, 0xFFBBBBBB, 0xFF888888
        )
    ),
    // Matrix: digital rain — nick colors stay in the green family.
    ThemePreset(
        "theme_matrix", "Matrix", true,
        background = 0xFF000000, surface = 0xFF001100, primary = 0xFF00FF41,
        onBackground = 0xFF00FF41, onSurface = 0xFF00FF41, number = 0xFF00FF41,
        quoteBackground = 0xFF000000,
        nickNames = listOf(
            0xFF00FF41, 0xFF33FF66, 0xFF00CC33, 0xFF99FF99, 0xFF008F11,
            0xFF00FF41, 0xFF33FF66, 0xFF00CC33, 0xFF99FF99, 0xFF008F11
        )
    ),
    // Green Terminal: emulator window in green — nick colors stay green.
    ThemePreset(
        "theme_greenterm", "Green Terminal", true,
        background = 0xFF000000, surface = 0xFF0A0A0A, primary = 0xFF33FF33,
        onBackground = 0xFF33FF33, onSurface = 0xFF33FF33, number = 0xFF33FF33,
        quoteBackground = 0xFF000000,
        nickNames = listOf(
            0xFF33FF33, 0xFF66FF66, 0xFF00CC00, 0xFF99FF99, 0xFF009900,
            0xFF33FF33, 0xFF66FF66, 0xFF00CC00, 0xFF99FF99, 0xFF009900
        )
    ),
)

/**
 * v1.88.23: labeled picker groups so the theme list stays compact.
 * Alphabetical within each group; plugin and custom themes keep their
 * own existing sections outside these groups.
 */
data class ThemeGroup(val label: String, val presets: List<ThemePreset>)

fun themeGroups(): List<ThemeGroup> {
    val byId = ThemePresets.associateBy { it.id }
    fun group(label: String, vararg ids: String): ThemeGroup =
        ThemeGroup(label, ids.mapNotNull { byId[it] })
    return listOf(
        group("App", "devxen", "dynamic", "quote404", "system"),
        group("Chat", "theme_gmessages", "theme_imessage", "theme_mirc"),
        group(
            "Terminals", "theme_dos", "theme_greenmono", "theme_greenterm",
            "theme_linux", "theme_terminal"
        ),
        // v1.88.25: textmode novelty themes.
        group("Textmode", "theme_ansi", "theme_ascii", "theme_matrix"),
        group("System", "theme_bsod", "theme_macintosh", "theme_macos"),
        group(
            "Quote databases", "db_bashim", "bashorg", "db_burntelectrons",
            "db_crapshack", "db_friends_wall", "db_furcqdb", "db_irox", "db_qdb_mit",
            "db_qdb_us", "db_quoteirc", "db_warpdrive", "db_wqdb", "db_xkcdb"
        ),
        group(
            "Dark", "amoled", "dark", "dracula", "forest", "midnight",
            "nord", "solarized", "db_friends"
        ),
        group("Light", "light", "sunset")
    )
}

/** v1.88.23: one entry of a grouped theme picker. */
sealed interface ThemeOption {
    data class Header(val label: String) : ThemeOption
    data class Item(val id: String, val name: String) : ThemeOption
}

/**
 * v1.88.23: grouped options for the per-database theme pickers: the
 * "Site default" entry first, then the labeled preset groups
 * (alphabetical within each group), then custom and plugin themes.
 * Both the Settings dropdown and the quick theme dialog render [Header]
 * entries as disabled section labels.
 */
fun groupedDbThemeOptions(
    siteDefaultLabel: String,
    customThemes: List<CustomTheme>,
    pluginThemeOptions: List<Pair<String, String>>
): List<ThemeOption> = buildList {
    add(ThemeOption.Item("", siteDefaultLabel))
    themeGroups().forEach { group ->
        add(ThemeOption.Header(group.label))
        group.presets.forEach { add(ThemeOption.Item(it.id, it.name)) }
    }
    if (customThemes.isNotEmpty()) {
        add(ThemeOption.Header("My themes"))
        customThemes.forEachIndexed { i, t ->
            add(ThemeOption.Item(customThemeId(i), t.name))
        }
    }
    if (pluginThemeOptions.isNotEmpty()) {
        add(ThemeOption.Header("Plugin themes"))
        pluginThemeOptions.forEach { (id, name) ->
            add(ThemeOption.Item(id, name))
        }
    }
}

fun ThemePreset.toColorScheme(): ColorScheme {
    val primaryColor = Color(primary)
    val bgColor = Color(background)
    val ratingColor = Color(rating)
    val onPrimaryColor = contentOn(primaryColor)
    // Muted step of the accent toward the background. Material's baseline
    // scheme leaves onPrimary/primaryContainer/secondary at purple when only
    // primary is overridden, which is why switches and checkboxes kept their
    // purple thumbs on custom themes — deriving them here keeps every
    // primary-colored widget inside the theme's own color family.
    val containerColor = blend(primaryColor, bgColor, 0.55f)
    val onContainerColor = contentOn(containerColor)
    return if (dark) {
        darkColorScheme(
            background = bgColor,
            surface = Color(surface),
            surfaceVariant = Color(surface),
            // The "Card surface" slot drives every container tone: quote
            // cards use surfaceContainerHigh, and leaving the baseline
            // grays in place made the slot look broken.
            surfaceContainerLowest = Color(surface),
            surfaceContainerLow = Color(surface),
            surfaceContainer = Color(surface),
            surfaceContainerHigh = Color(surface),
            surfaceContainerHighest = Color(surface),
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = containerColor,
            onPrimaryContainer = onContainerColor,
            secondary = primaryColor,
            onSecondary = onPrimaryColor,
            secondaryContainer = containerColor,
            onSecondaryContainer = onContainerColor,
            onBackground = Color(onBackground),
            onSurface = Color(onSurface),
            tertiary = Color(number),
            tertiaryContainer = ratingColor,
            onTertiaryContainer = Color(ratingText),
            outlineVariant = if (divider == 0L) Color.Transparent else Color(divider)
        )
    } else {
        lightColorScheme(
            background = bgColor,
            surface = Color(surface),
            surfaceVariant = Color(surface),
            // The "Card surface" slot drives every container tone: quote
            // cards use surfaceContainerHigh, and leaving the baseline
            // grays in place made the slot look broken.
            surfaceContainerLowest = Color(surface),
            surfaceContainerLow = Color(surface),
            surfaceContainer = Color(surface),
            surfaceContainerHigh = Color(surface),
            surfaceContainerHighest = Color(surface),
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = containerColor,
            onPrimaryContainer = onContainerColor,
            secondary = primaryColor,
            onSecondary = onPrimaryColor,
            secondaryContainer = containerColor,
            onSecondaryContainer = onContainerColor,
            onBackground = Color(onBackground),
            onSurface = Color(onSurface),
            tertiary = Color(number),
            tertiaryContainer = ratingColor,
            onTertiaryContainer = Color(ratingText),
            outlineVariant = if (divider == 0L) Color.Transparent else Color(divider)
        )
    }
}

/** Default rating-badge background (light blue) used when a theme has no rating slot. */
const val DEFAULT_RATING_HEX = "90CAF9"

/**
 * Applies a built-in preset color override (custom-theme CSV format) to a
 * [ThemePreset], returning the effective preset. Null/blank override =
 * the hardcoded default. This is how directly-edited built-in themes
 * resolve everywhere: the hardcoded [ThemePresets] are never mutated.
 */
fun ThemePreset.withOverride(colorsCsv: String?): ThemePreset {
    if (colorsCsv.isNullOrBlank()) return this
    val slots = try {
        parseCustomThemeSlots(colorsCsv)
    } catch (e: Exception) {
        return this
    }
    val fixed = slots.fixed
    fun hexAt(i: Int, fallback: Long): Long {
        val raw = fixed.getOrNull(i).orEmpty()
        if (raw.isBlank() || raw.equals("none", ignoreCase = true)) return fallback
        return try {
            raw.removePrefix("#").toLong(16) or 0xFF000000L
        } catch (e: Exception) {
            fallback
        }
    }
    fun longOrZero(i: Int): Long {
        val raw = fixed.getOrNull(i).orEmpty()
        if (raw.equals("none", ignoreCase = true)) return 0L
        return hexAt(i, 0L)
    }
    val bg = hexAt(0, background)
    val dark = run {
        val c = Color(bg)
        c.red * 0.299f + c.green * 0.587f + c.blue * 0.114f < 0.5f
    }
    val newNames = if (slots.namesDisabled) emptyList()
    else slots.nameColors.mapNotNull {
        try { it.removePrefix("#").toLong(16) or 0xFF000000L } catch (e: Exception) { null }
    }.ifEmpty { nickNames }
    // Divider lives at CSV position 14 ("none" = invisible); blank keeps
    // the preset's divider.
    val dividerRaw = try {
        colorsCsv.split(",").getOrNull(14)?.trim().orEmpty()
    } catch (e: Exception) { "" }
    val newDivider = when {
        dividerRaw.equals("none", ignoreCase = true) -> 0L
        dividerRaw.isNotBlank() -> try {
            dividerRaw.removePrefix("#").toLong(16) or 0xFF000000L
        } catch (e: Exception) { divider }
        else -> divider
    }
    // v1.55: "Quote background" lives at CSV position 16 (appended, so
    // older CSVs keep their positions); blank keeps the preset default.
    val quoteBgRaw = try {
        colorsCsv.split(",").getOrNull(16)?.trim().orEmpty()
    } catch (e: Exception) { "" }
    val newQuoteBackground = when {
        quoteBgRaw.isNotBlank() -> try {
            quoteBgRaw.removePrefix("#").toLong(16) or 0xFF000000L
        } catch (e: Exception) { quoteBackground }
        else -> quoteBackground
    }
    return copy(
        dark = dark,
        background = bg,
        surface = hexAt(1, surface),
        primary = hexAt(2, primary),
        onBackground = hexAt(3, onBackground),
        onSurface = hexAt(4, onSurface),
        number = hexAt(5, number),
        rating = hexAt(6, rating),
        ratingText = hexAt(7, ratingText),
        nickBrackets = longOrZero(8),
        nickNames = newNames,
        nickBracketPalette = when {
            slots.bracketsSingle -> emptyList()
            slots.bracketColors.isNotEmpty() -> slots.bracketColors.mapNotNull {
                try { it.removePrefix("#").toLong(16) or 0xFF000000L } catch (e: Exception) { null }
            }
            // No explicit bracket palette: tint the (possibly overridden)
            // name colors, mirroring the data-class default.
            else -> newNames.map { defaultBracketTint(it, dark) }
        },
        nickAction = longOrZero(9),
        actionOverridesName = slots.actionOverridesName,
        colorMessageWithSpeaker = slots.colorMessageWithSpeaker,
        divider = newDivider,
        quoteBackground = newQuoteBackground
    )
}

/**
 * The effective preset for [themeId]: the hardcoded preset with any
 * stored built-in override applied. Null for special ids
 * (system/dynamic/custom) which resolve elsewhere.
 */
fun effectivePreset(
    themeId: String,
    builtInOverrides: Map<String, String> = emptyMap()
): ThemePreset? =
    if (themeId.startsWith(com.quotebrowser.app.data.plugins.PluginManager.DB_ID_PREFIX)) {
        // v1.83: plugin theme packs register their presets with the
        // PluginManager; pack data is read-only so no per-preset overrides.
        com.quotebrowser.app.data.plugins.PluginManager.themePresets[themeId]
    } else {
        ThemePresets.find { it.id == themeId }
            ?.withOverride(builtInOverrides[themeId])
    }

/**
 * v1.55: the "Quote background" color for [themeId] — the surface behind
 * the quote body text on quote cards. Built-in themes read the preset
 * (with overrides); custom themes read CSV position 16; anything unknown
 * falls back to the black default.
 */
fun quoteBackgroundFor(
    themeId: String,
    customThemes: List<CustomTheme>,
    builtInOverrides: Map<String, String> = emptyMap()
): Color {
    if (themeId.startsWith("custom:")) {
        val idx = themeId.removePrefix("custom:").toIntOrNull()
        val hex = idx?.let { customThemes.getOrNull(it)?.colors }
            ?.split(",")?.getOrNull(16)?.trim().orEmpty()
        if (hex.isNotBlank()) {
            try {
                return Color(hex.removePrefix("#").toLong(16) or 0xFF000000)
            } catch (e: Exception) { /* fall through to default */ }
        }
        return Color.Black
    }
    return Color(effectivePreset(themeId, builtInOverrides)?.quoteBackground ?: 0xFF000000)
}

/** Default nick-bracket color (light blue) used when a theme has no nick slot. */
const val DEFAULT_NICK_BRACKETS_HEX = "7FC4FF"

/** Default action-line color (green) used when a theme has no action slot. */
const val DEFAULT_NICK_ACTION_HEX = "4CAF50"

/**
 * Parses "bg,surface,primary,onBg,onSurface[,number[,rating[,ratingText[,nickBrackets[,nickName]]]]]"
 * hex (with or without #/0x). A blank nickName slot means "no color".
 */
fun parseCustomTheme(colors: String?): ColorScheme? {
    if (colors.isNullOrBlank()) return null
    val parts = colors.split(",").map { it.trim().removePrefix("#").removePrefix("0x") }
    // v1.55: widened to 17 — the editor saves 16 parts (0-15) and the
    // "Quote background" slot appends position 16.
    if (parts.size !in 5..17) return null
    return try {
        val bg = Color(parts[0].toLong(16) or 0xFF000000)
        val surface = Color(parts[1].toLong(16) or 0xFF000000)
        val primary = Color(parts[2].toLong(16) or 0xFF000000)
        val onBg = Color(parts[3].toLong(16) or 0xFF000000)
        val onSurface = Color(parts[4].toLong(16) or 0xFF000000)
        // Older 5-slot themes have no number color: fall back to the accent.
        val number = if (parts.size >= 6) Color(parts[5].toLong(16) or 0xFF000000)
        else primary
        // Themes without a rating slot get the light-blue default.
        val rating = if (parts.size >= 7) Color(parts[6].toLong(16) or 0xFF000000)
        else Color(DEFAULT_RATING_HEX.toLong(16) or 0xFF000000)
        // Themes without a rating-text slot get auto-contrast on the rating bg.
        val ratingText = if (parts.size >= 8 && parts[7].isNotBlank())
            Color(parts[7].toLong(16) or 0xFF000000)
        else contentOn(rating)
        val onPrimaryColor = contentOn(primary)
        val containerColor = blend(primary, bg, 0.55f)
        val onContainerColor = contentOn(containerColor)
        // Assume dark if background is dark
        val isDark = (bg.red * 0.299 + bg.green * 0.587 + bg.blue * 0.114) < 0.5
        // Divider lives at CSV position 14 ("none" = invisible); older
        // themes without it get the subtle per-darkness default.
        val dividerRaw = parts.getOrNull(14)?.trim().orEmpty()
        val dividerColor = when {
            dividerRaw.equals("none", ignoreCase = true) -> Color.Transparent
            dividerRaw.isNotBlank() -> try {
                Color(dividerRaw.toLong(16) or 0xFF000000)
            } catch (e: Exception) {
                defaultDividerColor(isDark)
            }
            else -> defaultDividerColor(isDark)
        }
        if (isDark) {
            darkColorScheme(
                background = bg, surface = surface, surfaceVariant = surface,
                // The "Card surface" slot drives every container tone: quote
                // cards use surfaceContainerHigh, and leaving the baseline
                // grays in place made the slot look broken.
                surfaceContainerLowest = surface,
                surfaceContainerLow = surface,
                surfaceContainer = surface,
                surfaceContainerHigh = surface,
                surfaceContainerHighest = surface,
                primary = primary, onPrimary = onPrimaryColor,
                primaryContainer = containerColor, onPrimaryContainer = onContainerColor,
                secondary = primary, onSecondary = onPrimaryColor,
                secondaryContainer = containerColor, onSecondaryContainer = onContainerColor,
                onBackground = onBg, onSurface = onSurface,
                tertiary = number,
                tertiaryContainer = rating, onTertiaryContainer = ratingText,
                outlineVariant = dividerColor
            )
        } else {
            lightColorScheme(
                background = bg, surface = surface, surfaceVariant = surface,
                // The "Card surface" slot drives every container tone: quote
                // cards use surfaceContainerHigh, and leaving the baseline
                // grays in place made the slot look broken.
                surfaceContainerLowest = surface,
                surfaceContainerLow = surface,
                surfaceContainer = surface,
                surfaceContainerHigh = surface,
                surfaceContainerHighest = surface,
                primary = primary, onPrimary = onPrimaryColor,
                primaryContainer = containerColor, onPrimaryContainer = onContainerColor,
                secondary = primary, onSecondary = onPrimaryColor,
                secondaryContainer = containerColor, onSecondaryContainer = onContainerColor,
                onBackground = onBg, onSurface = onSurface,
                tertiary = number,
                tertiaryContainer = rating, onTertiaryContainer = ratingText,
                outlineVariant = dividerColor
            )
        }
    } catch (e: Exception) {
        null
    }
}

/** Subtle divider default for custom themes that predate the divider slot. */
fun defaultDividerColor(dark: Boolean): Color =
    if (dark) Color(0xFF494949) else Color(0xFFCAC4D0)

/**
 * The [ColorScheme] for a theme id, shared by the root theme and the
 * per-card per-database theming (v1.51): cards in All-databases mode wrap
 * themselves in the resolved palette's scheme via this function.
 */
@Composable
fun themeColorScheme(
    themeId: String,
    customThemes: List<CustomTheme> = emptyList(),
    builtInOverrides: Map<String, String> = emptyMap(),
    darkTheme: Boolean = isSystemInDarkTheme()
): ColorScheme = when {
    themeId == "dynamic" -> {
        // Material You: wallpaper/system dynamic colors (Android 12+).
        // Falls back to the plain system theme on older devices.
        val context = LocalContext.current
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        } else {
            if (darkTheme) darkColorScheme() else lightColorScheme()
        }
    }
    themeId == "system" -> {
        // The rating badge uses tertiaryContainer: pin it to the
        // light-blue rating default instead of Material's purple.
        val rating = Color(DEFAULT_RATING_HEX.toLong(16) or 0xFF000000)
        if (darkTheme) darkColorScheme(
            tertiaryContainer = rating,
            onTertiaryContainer = contentOn(rating)
        ) else lightColorScheme(
            tertiaryContainer = rating,
            onTertiaryContainer = contentOn(rating)
        )
    }
    themeId.startsWith("custom:") -> {
        val index = themeId.removePrefix("custom:").toIntOrNull()
        val colors = index?.let { customThemes.getOrNull(it)?.colors }
        parseCustomTheme(colors) ?: if (darkTheme) darkColorScheme() else lightColorScheme()
    }
    else -> effectivePreset(themeId, builtInOverrides)?.toColorScheme()
        ?: if (darkTheme) darkColorScheme() else lightColorScheme()
}

@Composable
fun QuoteBrowserTheme(
    themeId: String = "quote404",
    customThemes: List<CustomTheme> = emptyList(),
    /** Built-in preset color overrides: presetId -> custom-theme CSV. */
    builtInOverrides: Map<String, String> = emptyMap(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = themeColorScheme(themeId, customThemes, builtInOverrides, darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * A custom theme's colors string split into its 10 fixed slots
 * (bg, surface, primary, onBg, onSurface, number, rating, ratingText,
 * nickBrackets, nickAction) plus the ";"-separated default name-color
 * palette and the action-overrides-name flag. Older themes stored a single
 * default name color at CSV position 9; that value seeds the palette when
 * no palette was saved. A lone "-" at CSV position 11 disables name
 * coloring entirely (names stay uncolored, brackets keep their color).
 * CSV position 13 holds the ";"-separated default bracket-color palette
 * (paired 1:1 with the name palette); "-" there disables per-nick bracket
 * colors so the single <> color slot is used, and a missing position 13
 * falls back to tints of the name palette.
 */
data class CustomThemeSlots(
    val fixed: List<String>,
    val nameColors: List<String>,
    val actionOverridesName: Boolean,
    val namesDisabled: Boolean = false,
    val bracketColors: List<String> = emptyList(),
    val bracketsSingle: Boolean = false,
    val colorMessageWithSpeaker: Boolean = false
)

fun parseCustomThemeSlots(colors: String): CustomThemeSlots {
    val parts = colors.split(",").map { it.trim().removePrefix("#").removePrefix("0x") }
    // Fixed slots 0-8 map straight across; the action color lives at CSV
    // position 10 (position 9 is the legacy single name color).
    val fixed = List(10) { i ->
        when (i) {
            in 0..8 -> parts.getOrNull(i).orEmpty()
            9 -> parts.getOrNull(10).orEmpty()
            else -> ""
        }
    }
    val rawPalette = parts.getOrNull(11).orEmpty()
    // "-" at CSV position 11 = name coloring disabled entirely.
    val namesDisabled = rawPalette == "-"
    val palette = rawPalette
        .split(";")
        .map { it.trim().removePrefix("#").removePrefix("0x") }
        .filter { it.isNotBlank() }
        .take(MAX_NICK_NAME_COLORS)
    val legacy = parts.getOrNull(9).orEmpty()
    val names = when {
        namesDisabled -> emptyList()
        palette.isNotEmpty() -> palette
        legacy.isNotBlank() -> listOf(legacy)
        else -> emptyList()
    }
    // CSV position 12: "1"/"true" = action color covers the nick in action
    // lines, "0"/"false" = the nick keeps its name color. Missing = true.
    val overrides = when (parts.getOrNull(12)?.lowercase()) {
        "0", "false", "no", "off" -> false
        else -> true
    }
    // CSV position 15: "1"/"true" = color a speaker's message text with
    // their name (or bracket) color. Missing = false (off by default).
    // Position 14 is the divider color (see the save side).
    val colorMessage = when (parts.getOrNull(15)?.lowercase()) {
        "1", "true", "yes", "on" -> true
        else -> false
    }
    // CSV position 13: ";"-joined default bracket palette. "-" = no
    // per-nick bracket colors (use the single <> slot); missing/blank =
    // leave empty here so callers can fall back to tints of the names.
    val rawBrackets = parts.getOrNull(13)?.trim().orEmpty()
    val bracketsSingle = rawBrackets == "-"
    val bracketPalette = rawBrackets
        .split(";")
        .map { it.trim().removePrefix("#").removePrefix("0x") }
        .filter { it.isNotBlank() }
        .take(MAX_NICK_NAME_COLORS)
    return CustomThemeSlots(fixed, names, overrides, namesDisabled, bracketPalette, bracketsSingle, colorMessage)
}

/**
 * The active theme's nick colors: the <> bracket color (null = not
 * colored), the default name-color palette (empty = names not colored),
 * the action-line color (null = not colored), and whether the action color
 * covers the nick inside action lines.
 */
data class ResolvedNickTheme(
    val brackets: Color?,
    val names: List<Color>,
    /** Per-nick bracket colors paired 1:1 with [names]; empty = use [brackets]. */
    val bracketPalette: List<Color>,
    val action: Color?,
    val actionOverridesName: Boolean,
    val colorMessageWithSpeaker: Boolean
)

/**
 * Parses one nullable nick color slot: blank/missing -> the theme default,
 * "none" -> uncolored, otherwise the hex value.
 */
private fun parseNickSlot(part: String?, defaultHex: String): Color? {
    val p = part?.trim().orEmpty()
    if (p.isBlank()) return hexToColorOrNull(defaultHex)
    if (p.equals("none", ignoreCase = true)) return null
    return hexToColorOrNull(p) ?: hexToColorOrNull(defaultHex)
}

/**
 * Resolves the active theme's nick colors: the <> bracket color, the
 * default name-color palette, and the action-line color.
 */
fun resolveNickColors(
    themeId: String,
    customThemes: List<CustomTheme>,
    builtInOverrides: Map<String, String> = emptyMap()
): ResolvedNickTheme {
    val defaultBrackets = Color(DEFAULT_NICK_BRACKETS_HEX.toLong(16) or 0xFF000000)
    val defaultAction = Color(DEFAULT_NICK_ACTION_HEX.toLong(16) or 0xFF000000)
    val defaultNames = DefaultNickNameColors.map { Color(it) }
    if (themeId.startsWith("custom:")) {
        val index = themeId.removePrefix("custom:").toIntOrNull()
        val colors = index?.let { customThemes.getOrNull(it)?.colors }
        val parts = colors?.split(",")
        if (parts != null && parts.size in 5..16 && !colors.isNullOrBlank()) {
            val slots = parseCustomThemeSlots(colors)
            val fixed = slots.fixed
            val nameHexes = slots.nameColors
            val names = when {
                slots.namesDisabled -> emptyList()
                nameHexes.isNotEmpty() -> nameHexes.mapNotNull { hexToColorOrNull(it) }
                else -> defaultNames
            }
            // Bracket palette: explicit CSV position 13 wins; "-" disables
            // per-nick brackets (single <> slot used); missing falls back
            // to tints of the theme's name colors.
            val bracketPalette = when {
                slots.bracketsSingle -> emptyList()
                slots.bracketColors.isNotEmpty() ->
                    slots.bracketColors.mapNotNull { hexToColorOrNull(it) }
                else -> {
                    val bg = hexToColorOrNull(fixed.getOrNull(0)) ?: Color.Black
                    val dark = bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f < 0.5f
                    names.map {
                        Color(defaultBracketTint(it.toArgb().toLong() and 0xFFFFFFFFL, dark))
                    }
                }
            }
            return ResolvedNickTheme(
                brackets = parseNickSlot(fixed[8], DEFAULT_NICK_BRACKETS_HEX),
                names = names,
                bracketPalette = bracketPalette,
                action = parseNickSlot(fixed[9], DEFAULT_NICK_ACTION_HEX),
                actionOverridesName = slots.actionOverridesName,
                colorMessageWithSpeaker = slots.colorMessageWithSpeaker
            )
        }
        return ResolvedNickTheme(
            defaultBrackets, defaultNames,
            DefaultNickNameColors.map { Color(defaultBracketTint(it, true)) },
            defaultAction, true, false
        )
    }
    val preset = effectivePreset(themeId, builtInOverrides)
        ?: return ResolvedNickTheme(
            defaultBrackets, defaultNames,
            DefaultNickNameColors.map { Color(defaultBracketTint(it, true)) },
            defaultAction, true, false
        )
    return ResolvedNickTheme(
        brackets = if (preset.nickBrackets == 0L) null else Color(preset.nickBrackets),
        names = preset.nickNames.map { Color(it) },
        bracketPalette = preset.nickBracketPalette.map { Color(it) },
        action = if (preset.nickAction == 0L) null else Color(preset.nickAction),
        actionOverridesName = preset.actionOverridesName,
        colorMessageWithSpeaker = preset.colorMessageWithSpeaker
    )
}
