package com.quotebrowser.app.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.quotebrowser.app.data.DisplayPrefs

/**
 * QuoteClean: shared quote-text normalization + colorization rules (v1.47).
 *
 * One pipeline feeds display coloring, the HTML/widget renderer, and the
 * read-aloud (TTS) segmentation, so speech and display agree on who is
 * speaking. Everything is render-time only: stored quote text is never
 * mutated, so the Settings toggles below are fully reversible and apply
 * automatically to existing databases and newly imported quotes alike.
 *
 * Formats cataloged from a full audit of the 11 bundled databases:
 * - Speakers: `<nick>`, `< nick >`, `<@nick>`, `<nick|away>`, `nick:`,
 *   `nick :`, multi-word `Kalka Wrendi:`, `[nick]`, `[@nick]`, `[X]:`,
 *   `nick>`, `{nick}`, `-nick-`, `(@nick)`, `nick1 | nick2:`
 * - Mangled data: literal U+FFFD replacement chars from old scrapes are
 *   stripped at display time (stored text untouched)
 * - Actions/notices: `* nick does x`, `*wrapped*`, `** kicked`, `*** quit`
 * - Line separators: `\` (IRC convention) and `/` (crapshack convention)
 * - Timestamps: `[17:05]`, `[02:16:02]`, `[01:25pm]`, at line starts and
 *   pasted mid-line after ` : `
 */

/** Per-user QuoteClean switches, provided app-wide via [LocalQuoteClean]. */
data class QuoteCleanOptions(
    val colorNames: Boolean = true,
    val colorActions: Boolean = true,
    val normalizeBreaks: Boolean = true,
    val showTimestamps: Boolean = false,
    /**
     * v1.86: per-category color controls. Each category has an on/off
     * toggle plus an optional custom color (hex, null = theme default).
     * - message: the words a speaker says (after any recognized label).
     * - other: lines that are neither speech nor action (narration,
     *   notices, joins/parts, system lines, anything else).
     * - name/action/timestamp colors override the theme color when set.
     */
    val colorMessage: Boolean = true,
    val nameColorHex: String? = null,
    val messageColorHex: String? = null,
    val actionColorHex: String? = null,
    val timestampColorHex: String? = null,
    val colorOther: Boolean = true,
    val otherColorHex: String? = null
)

val LocalQuoteClean = compositionLocalOf { QuoteCleanOptions() }

// ---------------------------------------------------------------------------
// Line-break normalization
// ---------------------------------------------------------------------------

/**
 * Normalizes the `/` line-separator convention used by some archives
 * (notably crapshack) into real line breaks. Conservative on purpose:
 * a split only happens where the text after the `/` looks like the start
 * of a new speaker/action/status turn, so ordinary slashes stay intact:
 * `17 / f?`, `find / -name`, `MSN / facebook`, poetry lines, `cd /`.
 */
fun normalizeSlashBreaks(text: String): String {
    var out = text
    // " / " followed by a speaker marker: <nick>, *action, {nick}, [nick], `nick`, @nick
    out = SlashBreakSpacedRegex.replace(out, "\n")
    // " / " (or "/") followed by a nick-ish token ending in > or :  e.g. KaRaOkE^KiNg> kewl
    out = SlashBreakNickRegex.replace(out, "\n")
    // Tight "word/<nick>" join, e.g. snowy?/<Allianora> (but never after ":" -> keeps http://)
    out = SlashBreakTightRegex.replace(out, "\n")
    return out
}

private val SlashBreakSpacedRegex = Regex("\\s+/\\s+(?=[<*{\\[`@])")
private val SlashBreakNickRegex = Regex("\\s+/\\s*(?=[$NickChars]+[>:])")
// ^ nick token is lookahead-only: the speaker marker/name is preserved,
// only the separator becomes a line break.
private val SlashBreakTightRegex = Regex("(?<=[^:\\s])/(?=<)")

// ---------------------------------------------------------------------------
// Replacement-character stripping
// ---------------------------------------------------------------------------

/**
 * Old scrapes left literal U+FFFD replacement characters in stored quote
 * text (e.g. bash.org #138542 renders "�( cAz )�: sup otter"). Stripped
 * at display time only — stored text is never touched.
 */
fun stripReplacementChars(text: String): String = text.replace("\uFFFD", "")

// ---------------------------------------------------------------------------
// Timestamp stripping
// ---------------------------------------------------------------------------

/**
 * Removes IRC log timestamp prefixes such as `[17:05]`, `[02:16:02]`,
 * and `[01:25pm]`. Applied at line starts and to timestamps pasted
 * mid-line after ` : ` (e.g. `=P : [01:49] <w...`). Other bracketed
 * text (`[SJR]`, `[T]`, `[about diagnosing STDs]`) is never touched.
 */
fun stripTimestamps(text: String): String =
    TimestampRegex.replace(text, "")

internal val TimestampRegex =
    Regex("(?m)(?:^|(?<=[\\s:]))\\[\\d{1,2}:\\d{2}(?::\\d{2})?(?:[aApP][mM])?\\][ \\t]*")

// ---------------------------------------------------------------------------
// Full display pipeline
// ---------------------------------------------------------------------------

/**
 * The complete display-text pipeline: line endings are normalized first
 * (v1.88.35: scraped quotes can carry \n\r\n / lone-\r sequences and
 * Android's framework TextView — e.g. home-screen widgets fed via
 * Html.fromHtml — renders a lone \r as a missing-glyph box instead of a
 * line break; paragraphizeBody only normalized \r when "Break between
 * quotes" wasn't Off), then U+FFFD replacement characters from old scrapes
 * are stripped, then existing IRC `\` / dialogue breaks, then QuoteClean
 * `/` normalization (when enabled), then timestamp stripping (unless the
 * user chose to keep timestamps), and finally the "Break between quotes"
 * paragraph separation (v1.88.22: blank lines between turns per the
 * DisplayPrefs setting; Off leaves newlines untouched apart from the
 * \r normalization, since \r is never legitimate visible content).
 */
fun cleanDisplayText(
    text: String,
    irc: Boolean,
    dialogue: Boolean,
    opts: QuoteCleanOptions
): String {
    var out = applyDisplayBreaks(stripReplacementChars(normalizeLineEndings(text)), irc, dialogue)
    if (opts.normalizeBreaks) out = normalizeSlashBreaks(out)
    if (!opts.showTimestamps) out = stripTimestamps(out)
    return paragraphizeBody(out)
}

/**
 * Collapses \r\n and lone \r to \n. Display-only; stored text is never
 * touched. (v1.88.35 fix for widget tofu diamonds on quotes with \n\r\n
 * sequences in their stored text.)
 */
fun normalizeLineEndings(text: String): String =
    text.replace("\r\n", "\n").replace('\r', '\n')

/**
 * v1.88.22 "mind breaks": display-only paragraph separation between quote
 * lines, driven by the "Break between quotes" setting (DisplayPrefs).
 * Off = the text is returned byte-identical (no normalization at all —
 * the original dense look); Small/Medium/Large = every non-blank line
 * becomes its own paragraph separated by exactly 1/2/3 blank lines, so
 * multi-turn quotes (IRC/chat logs) read as distinct turns. Leading and
 * trailing blank lines are dropped; internal runs of blank lines collapse
 * to exactly the setting's count. Stored quote text is never touched —
 * this is render-time only, like the rest of QuoteClean. Reads the setting
 * itself so the single choke point in cleanDisplayText/applyDisplayBreaks
 * covers every surface (Compose cards, detail view, widgets, exports).
 */
fun paragraphizeBody(text: String): String {
    val blanks = DisplayPrefs.blanksFor(DisplayPrefs.breakBetweenQuotes.value)
    if (blanks <= 0) return text
    val sep = "\n".repeat(blanks + 1)
    val paras = text
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')
        .filter { it.isNotBlank() }
    return paras.joinToString(sep)
}

/**
 * The same normalization used for read-aloud segmentation, so TTS hears
 * exactly what the eye sees: timestamps never spoken, `/` and `\`
 * separators become turn boundaries, and the same speaker/action
 * detection decides who is speaking.
 */
fun cleanSpokenText(text: String, dialogueBreaks: Boolean): String {
    var out = stripTimestamps(text)
    out = applyIrcLineBreaks(out)
    out = normalizeSlashBreaks(out)
    if (dialogueBreaks) out = applyDialogueLineBreaks(out)
    return out
}

// ---------------------------------------------------------------------------
// Contrast enforcement (Quote of the Day card)
// ---------------------------------------------------------------------------

/** Minimum WCAG contrast ratio enforced for nick/action colors on the QotD card. */
private const val QotdMinContrast = 3.0

private fun contrastRatio(a: Color, b: Color): Double {
    val l1 = a.luminance().toDouble()
    val l2 = b.luminance().toDouble()
    val (hi, lo) = if (l1 >= l2) l1 to l2 else l2 to l1
    return (hi + 0.05) / (lo + 0.05)
}

/**
 * Shifts [color] toward white on dark backgrounds, or toward black on
 * light backgrounds, until it reaches [minRatio] contrast against [bg]
 * (or 24 blend steps pass). Dark nick/action colors unreadable on light
 * cards darken; light ones unreadable on dark cards lighten; hues stay
 * recognizable. (v1.55: previously always shifted toward white, which
 * only worked on dark cards like the blue Quote-of-the-Day card.)
 */
fun ensureContrastOn(color: Color, bg: Color, minRatio: Double = QotdMinContrast): Color {
    val bgLight = bg.red * 0.299f + bg.green * 0.587f + bg.blue * 0.114f >= 0.5f
    val target = if (bgLight) 0f else 1f
    var c = color
    repeat(24) {
        if (contrastRatio(c, bg) >= minRatio) return c
        c = Color(
            red = c.red + (target - c.red) * 0.18f,
            green = c.green + (target - c.green) * 0.18f,
            blue = c.blue + (target - c.blue) * 0.18f,
            alpha = c.alpha
        )
    }
    return c
}

/** Returns a copy of this palette whose nick/bracket/action colors all meet [QotdMinContrast] on [bg]. */
fun NickPalette.contrastAdjusted(bg: Color): NickPalette = copy(
    names = names.map { ensureContrastOn(it, bg) },
    brackets = brackets?.let { ensureContrastOn(it, bg) },
    bracketPalette = bracketPalette.map { ensureContrastOn(it, bg) },
    action = action?.let { ensureContrastOn(it, bg) }
)

// ---------------------------------------------------------------------------
// Display-only text transformations (v1.51)
// ---------------------------------------------------------------------------

/**
 * Display-only novelty transformation mode. v1.52: a single mutually
 * exclusive selector (replacing v1.51's three independent stacking
 * toggles, which shipped defaulting ON and rendered quote text
 * unreadable). Runs at the very end of the display pipeline (around
 * QuoteClean, never touching stored text) and is never applied to TTS.
 * Defaults to OFF.
 */
enum class TransformMode(val label: String) {
    OFF("Off"),
    LEET("1337"),
    UPSIDE_DOWN("Upside-down"),
    ALTERNATING_CASE("aLtErNaTiNg")
}

data class DisplayTransforms(val mode: TransformMode = TransformMode.OFF) {
    val leet: Boolean get() = mode == TransformMode.LEET
    val upsideDown: Boolean get() = mode == TransformMode.UPSIDE_DOWN
    val alternatingCase: Boolean get() = mode == TransformMode.ALTERNATING_CASE
}

val LocalDisplayTransforms = compositionLocalOf { DisplayTransforms() }

private val LeetMap = mapOf(
    'a' to '4', 'A' to '4', 'b' to '8', 'B' to '8', 'e' to '3', 'E' to '3',
    'g' to '6', 'G' to '6', 'i' to '1', 'I' to '1', 'l' to '1', 'L' to '1',
    'o' to '0', 'O' to '0', 's' to '5', 'S' to '5', 't' to '7', 'T' to '7',
    'z' to '2', 'Z' to '2'
)

/** Classic 1337 speak: a→4, e→3, o→0, … */
fun leetChar(c: Char): Char = LeetMap[c] ?: c

private val UpsideDownMap = mapOf(
    'a' to 'ɐ', 'b' to 'q', 'c' to 'ɔ', 'd' to 'p', 'e' to 'ǝ',
    'f' to 'ɟ', 'g' to 'ƃ', 'h' to 'ɥ', 'i' to 'ı', 'j' to 'ɾ',
    'k' to 'ʞ', 'l' to 'l', 'm' to 'ɯ', 'n' to 'u', 'o' to 'o',
    'p' to 'd', 'q' to 'b', 'r' to 'ɹ', 's' to 's', 't' to 'ʇ',
    'u' to 'n', 'v' to 'ʌ', 'w' to 'ʍ', 'x' to 'x', 'y' to 'ʎ', 'z' to 'z',
    'A' to '∀', 'B' to 'ᗺ', 'C' to 'Ɔ', 'D' to 'ᗡ', 'E' to 'Ǝ',
    'F' to 'Ⅎ', 'G' to '⅁', 'H' to 'H', 'I' to 'I', 'J' to 'ᒿ',
    'K' to 'ꓘ', 'L' to '⅂', 'M' to 'W', 'N' to 'N', 'O' to 'O',
    'P' to 'Ԁ', 'Q' to 'Ό', 'R' to 'ᴚ', 'S' to 'S', 'T' to '⊥',
    'U' to '∩', 'V' to 'Λ', 'W' to 'M', 'X' to 'X', 'Y' to '⅄', 'Z' to 'Z',
    '0' to '0', '1' to 'Ɩ', '2' to 'ᄅ', '3' to 'Ɛ', '4' to 'ᔭ',
    '5' to 'ϛ', '6' to '9', '7' to 'ㄥ', '8' to '8', '9' to '6',
    '.' to '˙', ',' to '\'', '\'' to ',', '"' to '„', '?' to '¿',
    '!' to '¡', '(' to ')', ')' to '(', '[' to ']', ']' to '[',
    '{' to '}', '}' to '{', '<' to '>', '>' to '<', '_' to '‾'
)

fun upsideDownChar(c: Char): Char = UpsideDownMap[c] ?: c

/**
 * Applies the enabled display transforms to plain text: 1337 speak,
 * upside-down Unicode (plus full string reversal), and aLtErNaTiNg case.
 * All mappings are 1:1 per character so annotated spans stay aligned.
 */
fun applyDisplayTransforms(text: String, t: DisplayTransforms): String {
    if (!t.leet && !t.upsideDown && !t.alternatingCase) return text
    val sb = StringBuilder(text.length)
    var upper = true
    for (c in text) {
        var ch = c
        if (t.leet) ch = leetChar(ch)
        if (t.upsideDown) ch = upsideDownChar(ch)
        if (t.alternatingCase && ch.isLetter()) {
            ch = if (upper) ch.uppercaseChar() else ch.lowercaseChar()
            upper = !upper
        }
        sb.append(ch)
    }
    return if (t.upsideDown) sb.reverse().toString() else sb.toString()
}
