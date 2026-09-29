package com.quotebrowser.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.SiteStyle
import com.quotebrowser.app.ui.theme.ResolvedNickTheme
import com.quotebrowser.app.ui.theme.defaultBracketTint
import kotlinx.coroutines.flow.StateFlow

/**
 * v1.63: nick color themes. Each theme is a palette of name colors — one
 * color per person, applied to their name, their brackets, and (optionally)
 * what they say. Nicks without a personal rule get a deterministic pick
 * from the theme's palette, so the same person keeps the same color in
 * every quote.
 *
 * Preset ids are stable strings; custom themes use "custom:<uuid>".
 * The special id [NICK_THEME_APP] means "follow the app theme" (the
 * pre-v1.63 behavior where nick colors came from the selected app theme).
 */
data class NickColorTheme(
    val id: String,
    val name: String,
    /** ARGB ints, up to [MAX_NICK_NAME_COLORS]. */
    val nameColors: List<Int>,
    /** ARGB int or null = the default action-line green. */
    val actionColor: Int? = null,
    val isCustom: Boolean = false
)

/** The default nick color theme: Clint's Quote404 logo colors. */
const val NICK_THEME_DEFAULT = "logo"

/** "Follow the app theme" — the pre-v1.63 nick color behavior. */
const val NICK_THEME_APP = "app"

/** Maximum colors in a custom nick color theme. */
const val MAX_NICK_THEME_COLORS = 10

/** Built-in nick color themes. */
val NickColorThemePresets: List<NickColorTheme> = listOf(
    NickColorTheme(
        id = "logo",
        name = "Quote404",
        nameColors = listOf(
            0xFF00D4FF.toInt(), 0xFF0090FF.toInt(), 0xFF33CCFF.toInt(), 0xFF66E0FF.toInt(), 0xFF0080FF.toInt(),
            0xFF00A8FF.toInt(), 0xFF4DC9FF.toInt(), 0xFF1E90FF.toInt(), 0xFF00E5FF.toInt(), 0xFF7FDBFF.toInt()
        ),
        actionColor = 0xFF4CAF50.toInt()
    ),
    NickColorTheme(
        id = "rainbow",
        name = "Rainbow",
        nameColors = listOf(
            0xFFFF3B30.toInt(), 0xFFFF9500.toInt(), 0xFFFFCC00.toInt(), 0xFF34C759.toInt(), 0xFF00C7BE.toInt(),
            0xFF32ADE6.toInt(), 0xFF0A84FF.toInt(), 0xFFBF5AF2.toInt(), 0xFFFF2D55.toInt(), 0xFFFF6482.toInt()
        ),
        actionColor = 0xFF4CAF50.toInt()
    ),
    NickColorTheme(
        id = "sunset",
        name = "Sunset",
        nameColors = listOf(
            0xFFFF6B35.toInt(), 0xFFFF8C42.toInt(), 0xFFFFA552.toInt(), 0xFFFFC53D.toInt(), 0xFFFF4E50.toInt(),
            0xFFF857A6.toInt(), 0xFFDD2C6B.toInt(), 0xFF9D4EDD.toInt(), 0xFF7B2FF7.toInt(), 0xFFFFB3BA.toInt()
        ),
        actionColor = 0xFFFFA552.toInt()
    ),
    NickColorTheme(
        id = "ocean",
        name = "Ocean",
        nameColors = listOf(
            0xFF0077B6.toInt(), 0xFF0096C7.toInt(), 0xFF00B4D8.toInt(), 0xFF48CAE4.toInt(), 0xFF90E0EF.toInt(),
            0xFFADE8F4.toInt(), 0xFF2EC4B6.toInt(), 0xFF023E8A.toInt(), 0xFF3A86FF.toInt(), 0xFF06D6A0.toInt()
        ),
        actionColor = 0xFF2EC4B6.toInt()
    ),
    NickColorTheme(
        id = "neon",
        name = "Neon Nights",
        nameColors = listOf(
            0xFFFF00FF.toInt(), 0xFF00FF00.toInt(), 0xFF00FFFF.toInt(), 0xFFFFFF00.toInt(), 0xFFFF007F.toInt(),
            0xFF7F00FF.toInt(), 0xFF00FF7F.toInt(), 0xFFFF7F00.toInt(), 0xFF7FFF00.toInt(), 0xFF007FFF.toInt()
        ),
        actionColor = 0xFF00FF7F.toInt()
    ),
    NickColorTheme(
        id = "pastel",
        name = "Pastel",
        nameColors = listOf(
            0xFFFFB3BA.toInt(), 0xFFFFDFBA.toInt(), 0xFFFFFFBA.toInt(), 0xFFBAFFC9.toInt(), 0xFFBAE1FF.toInt(),
            0xFFD3B5E5.toInt(), 0xFFFFC6FF.toInt(), 0xFFBDE0FE.toInt(), 0xFFFFAFCC.toInt(), 0xFFC8F7C5.toInt()
        ),
        actionColor = 0xFF95D5B2.toInt()
    ),
    NickColorTheme(
        id = "forest",
        name = "Forest",
        nameColors = listOf(
            0xFF2D6A4F.toInt(), 0xFF40916C.toInt(), 0xFF52B788.toInt(), 0xFF74C69D.toInt(), 0xFF95D5B2.toInt(),
            0xFFB7E4C7.toInt(), 0xFFD8F3DC.toInt(), 0xFF1B4332.toInt(), 0xFFA3B18A.toInt(), 0xFF588157.toInt()
        ),
        actionColor = 0xFF95D5B2.toInt()
    ),
    NickColorTheme(
        id = "ember",
        name = "Ember",
        nameColors = listOf(
            0xFFFFBA08.toInt(), 0xFFFAA307.toInt(), 0xFFF48C06.toInt(), 0xFFE85D04.toInt(), 0xFFDC2F02.toInt(),
            0xFFD00000.toInt(), 0xFF9D0208.toInt(), 0xFFFFD500.toInt(), 0xFFFF6D00.toInt(), 0xFFFF9E00.toInt()
        ),
        actionColor = 0xFFFFBA08.toInt()
    )
)

/** Default action-line color when a theme doesn't set one. */
private const val DEFAULT_THEME_ACTION = -11751600 /* 0xFF4CAF50 */

/**
 * Resolves the active nick color theme into the [ResolvedNickTheme] the
 * quote renderer consumes. Each nick's name, brackets, and (when enabled)
 * message text share that nick's palette color.
 *
 * @param themeId the selected theme id, [NICK_THEME_APP], or a custom id.
 * @param customThemes the user's own themes.
 * @param colorMessageWithSpeaker the dedicated "color what they say"
 *   toggle (off by default); wins over any theme-level flag.
 * @param legacy the app-theme resolution used when [themeId] is
 *   [NICK_THEME_APP] or the id is unknown.
 */
fun resolveNickThemePalette(
    themeId: String,
    customThemes: List<NickColorTheme>,
    colorMessageWithSpeaker: Boolean,
    legacy: ResolvedNickTheme
): ResolvedNickTheme {
    if (themeId == NICK_THEME_APP) {
        return legacy.copy(colorMessageWithSpeaker = colorMessageWithSpeaker)
    }
    val theme = NickColorThemePresets.firstOrNull { it.id == themeId }
        ?: customThemes.firstOrNull { it.id == themeId }
        ?: return legacy.copy(colorMessageWithSpeaker = colorMessageWithSpeaker)
    val names = theme.nameColors
        .take(MAX_NICK_NAME_COLORS)
        .map { Color(it) }
    if (names.isEmpty()) {
        return legacy.copy(colorMessageWithSpeaker = colorMessageWithSpeaker)
    }
    // Per-nick bracket colors, paired 1:1 with the name colors (tinted
    // toward white for dark quote backgrounds; the per-card
    // contrast-adjust pass handles light backgrounds).
    val bracketPalette = names.map {
        Color(defaultBracketTint(it.toArgb().toLong() and 0xFFFFFFFFL, true))
    }
    return ResolvedNickTheme(
        // No single <> color: every nick gets its paired bracket tint.
        brackets = null,
        names = names,
        bracketPalette = bracketPalette,
        action = Color(
            (theme.actionColor ?: DEFAULT_THEME_ACTION).toLong() and 0xFFFFFFFFL
        ),
        actionOverridesName = true,
        colorMessageWithSpeaker = colorMessageWithSpeaker
    )
}

/**
 * v1.88.32: per-card-theme nick palette overrides. Stored value "default"
 * (or a missing key) means "Use global"; anything else is a nick color
 * theme id (a preset id or a custom theme id).
 */
const val THEME_NICK_USE_GLOBAL = "default"

/** Stable key for the "Site theme" card in the share picker. */
const val CARD_THEME_KEY_SITE = "site"

/** Stable per-theme key for a [QuoteCardStyle] card. */
fun QuoteCardStyle.cardThemeKey(): String = "style:$name"

/** Stable per-theme key for a novelty [SiteStyle] card. */
fun SiteStyle.noveltyCardThemeKey(): String = "novelty:$name"

/**
 * v1.88.32: resolves the effective nick palette for one card theme: the
 * theme's override when set, otherwise the global/share palette. Mirrors
 * [resolveNickThemePalette] but falls back to [global] (instead of the
 * legacy app-theme resolution) when the override id is unknown — e.g. a
 * custom palette that was deleted after being assigned.
 *
 * The caller passes the effective palette into the renderers' [NickPalette]
 * params, so the existing contrast-against-background adjustment keeps
 * working unchanged.
 */
fun resolveThemeNickPaletteOverride(
    overrideId: String?,
    global: NickPalette,
    customThemes: List<NickColorTheme>,
    colorMessageWithSpeaker: Boolean
): NickPalette {
    if (overrideId.isNullOrBlank() || overrideId == THEME_NICK_USE_GLOBAL) return global
    val theme = NickColorThemePresets.firstOrNull { it.id == overrideId }
        ?: customThemes.firstOrNull { it.id == overrideId }
        ?: return global
    val names = theme.nameColors.take(MAX_NICK_NAME_COLORS).map { Color(it) }
    if (names.isEmpty()) return global
    val bracketPalette = names.map {
        Color(defaultBracketTint(it.toArgb().toLong() and 0xFFFFFFFFL, true))
    }
    return global.copy(
        brackets = null,
        names = names,
        bracketPalette = bracketPalette,
        action = Color(
            (theme.actionColor ?: 0xFF4CAF50.toInt()).toLong() and 0xFFFFFFFFL
        ),
        actionOverridesName = true,
        colorMessageWithSpeaker = colorMessageWithSpeaker
    )
}

/**
 * v1.88.32: the per-card-theme nick palette override store, provided once
 * at the root so the share picker (which has no ViewModel parameter) can
 * read and write overrides without signature changes at every call site.
 */
class ThemeNickPaletteStore(
    val overrides: StateFlow<Map<String, String>>,
    val customThemes: StateFlow<List<NickColorTheme>>,
    val colorMessageWithSpeaker: StateFlow<Boolean>,
    val globalThemeId: StateFlow<String>,
    val setOverride: (themeKey: String, nickThemeId: String) -> Unit,
    /**
     * v1.88.32: "remove nicks from message text" — set of card theme keys
     * (novelty:AIM etc.) where the speaker prefix is stripped from chat
     * turn bodies. Missing = OFF (default).
     */
    val nickStrip: StateFlow<Set<String>>,
    val setNickStrip: (themeKey: String, strip: Boolean) -> Unit
)

val LocalThemeNickPaletteStore = compositionLocalOf<ThemeNickPaletteStore?> { null }

/** Row of color dots previewing a nick color theme's palette. */
@Composable
fun NickThemeDots(colors: List<Int>, max: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.take(max).forEach { argb ->
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
        }
    }
}
