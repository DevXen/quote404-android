package com.quotebrowser.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.quotebrowser.app.ui.theme.defaultBracketTint

/**
 * One per-person nick color rule, as stored in prefs (JSON).
 * Hex strings are RRGGBB; null means "use the theme default" (automatic
 * name color for names), [NICK_COLOR_NONE] means "explicitly uncolored".
 */
data class NickColorRule(
    val nick: String,
    val bracketsHex: String?,
    val nameHex: String?
)

/** Per-person rule value meaning "explicitly uncolored" (vs null = theme default). */
const val NICK_COLOR_NONE = ""

/** Maximum number of per-person nick color rules. */
const val MAX_NICK_RULES = 10

/** Maximum number of default name colors in a theme's palette. */
const val MAX_NICK_NAME_COLORS = 10

/**
 * Resolved nick palette handed to quote text rendering, built per theme:
 * - [brackets]: the theme's <> color (null = brackets not colored).
 * - [names]: the theme's default name-color palette, up to
 *   [MAX_NICK_NAME_COLORS] entries. Nicks without a personal rule get one
 *   of these deterministically (empty = names not colored).
 * - [action]: the theme's color for whole IRC action lines
 *   ("* nick does something"; null = not colored).
 * - [actionOverridesName]: when true the action color covers the nick too;
 *   when false the nick keeps its personal/default name color in actions.
 * - [rules]: the user's per-person overrides (matched case-insensitively).
 *   A rule always wins — anywhere that name talks, including action lines
 *   when the action color doesn't override it.
 * - [ircLineBreaks]: turns " \" separators into real line breaks.
 * - [dialogueLineBreaks]: breaks single-line dialogue into turns — a new
 *   <nick> that follows quoted text starts its own line.
 */
data class NickPalette(
    val enabled: Boolean,
    val brackets: Color?,
    val names: List<Color>,
    /**
     * Per-nick bracket colors, paired 1:1 with [names]: the nick that gets
     * name color *i* gets bracket color *i*. Empty = [brackets] is used for
     * every nick.
     */
    val bracketPalette: List<Color> = emptyList(),
    val action: Color?,
    val actionOverridesName: Boolean = true,
    /**
     * When true, a speaker's message text ("<nick> words", "Nick: words")
     * takes the speaker's name color, or the bracket color when the name
     * has none. Off by default.
     */
    val colorMessageWithSpeaker: Boolean = false,
    val rules: List<NickColorRule>,
    val ircLineBreaks: Boolean = true,
    val dialogueLineBreaks: Boolean = true
)

val LocalNickPalette = compositionLocalOf {
    NickPalette(
        enabled = true,
        brackets = Color(0xFF7FC4FF),
        names = DefaultNickNameColors.map { Color(it) },
        bracketPalette = DefaultNickNameColors.map {
            Color(defaultBracketTint(it, true))
        },
        action = Color(0xFF4CAF50),
        actionOverridesName = true,
        rules = emptyList(),
        ircLineBreaks = true,
        dialogueLineBreaks = true
    )
}

/**
 * The effective quote-body background behind quote text, or null when the
 * surface is unknown. Provided per card (QuoteRow); when present and nick
 * colorization is on, the palette is contrast-adjusted against it (v1.55)
 * so nick/bracket/action colors stay readable on the quote background.
 */
val LocalQuoteBackground = compositionLocalOf<Color?> { null }

/**
 * Default name-color palette: 10 distinct colors. Nicks without a personal
 * rule are assigned one deterministically, so the same person keeps the
 * same color in every quote while different people differ.
 */
val DefaultNickNameColors = listOf(
    0xFFEF9A9A, 0xFFF48FB1, 0xFFCE93D8, 0xFFBA68C8,
    0xFF7986CB, 0xFF90CAF9, 0xFF4DD0E1, 0xFF80CBC4,
    0xFFA5D6A7, 0xFFFFCC80
)

/**
 * Deterministic palette pick for [nick]; null when the palette is empty
 * (names not colored).
 *
 * Nicks that hash onto the same palette slot get a deterministic hue
 * rotation, so two different people never render in one identical color
 * while the same nick always keeps its color in every quote.
 */
fun defaultNameColor(palette: List<Color>, nick: String): Color? =
    spreadPick(palette, nick)

/**
 * Default bracket color for [nick]: the paired entry from [bracketPalette]
 * (same deterministic slot as the name color), or [fallback] — the theme's
 * single <> color — when the palette is empty. [nameCount] is the name
 * palette's size; indexing by it keeps the pair aligned even when the two
 * palettes have different lengths.
 */
fun defaultBracketColor(
    bracketPalette: List<Color>,
    fallback: Color?,
    nick: String,
    nameCount: Int = 0
): Color? {
    if (bracketPalette.isEmpty()) return fallback
    val size = if (nameCount > 0) nameCount else bracketPalette.size
    val base = bracketPalette[Math.floorMod(spreadIndex(size, nick), bracketPalette.size)]
    return spreadRotate(base, nick)
}

/** Shared deterministic pick behind [defaultNameColor]/[defaultBracketColor]. */
private fun spreadPick(palette: List<Color>, nick: String): Color? {
    if (palette.isEmpty()) return null
    return spreadRotate(palette[spreadIndex(palette.size, nick)], nick)
}

/** Deterministic palette slot for a nick. */
private fun spreadIndex(size: Int, nick: String): Int {
    if (size <= 0) return 0
    return Math.floorMod(nick.lowercase().hashCode(), size)
}

/** Deterministic hue rotation so different nicks don't collide. */
private fun spreadRotate(base: Color, nick: String): Color {
    val hash = nick.lowercase().hashCode()
    val spread = Math.floorMod(hash / 97, 6)
    if (spread == 0) return base
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(base.toArgb(), hsv)
    hsv[0] = (hsv[0] + spread * 47f) % 360f
    hsv[2] = (hsv[2] + (if (spread % 2 == 0) 0.08f else -0.08f)).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/** IRC nickname alphabet shared by mentions and action lines. */
internal const val NickChars = "A-Za-z0-9_@\\-\\\\|\\[\\]{}^`"

/**
 * Matches IRC-style <nick> mentions, including channel mode prefixes
 * (<+voiced>, <%halfop>, <&admin>, <~owner>). Group 1 is the optional
 * prefix, group 2 the bare nick. The character class covers the IRC
 * nickname alphabet; things like "<-=-=" or "<3" don't match.
 * Some archives (e.g. xkcdb.com) write a space inside the brackets
 * ("< nick>"); the optional padding is allowed but is not part of the
 * nick groups, so coloring and tap-to-search still target the bare nick.
 */
private val NickMentionRegex = Regex("<[ \\t]*([~&%+]?)([$NickChars]+)[ \\t]*>")

/**
 * Matches I-Rox-style ?nick? speaker lines ("?DizMatt? velvetpants?",
 * "?babyharpseal? one of the peanuts has crashed"). Group 1 is the bare
 * nick. The ^ line-start anchor (with (?m)) keeps mid-line "?..?" asides
 * and file metadata ("?4.57mb?") from matching — only the speaker
 * position is colorized, like ParenSpeakerRegex. The ? marks take the
 * bracket color and the name its personal/default name color, tappable.
 */
private val QuestionNickRegex =
    Regex("(?m)^\\?[ \\t]*([~&%+]?[$NickChars]{1,39})[ \\t]*\\?")

/** Matches whole IRC action / notice lines: "* nick does x", "*wrapped*",
 * "** kicked", "*** quit". Inline *emphasis* is not an action. */
private val ActionLineRegex = Regex("(?m)^\\*[ \\t]*\\S.*$")

/**
 * Leading nick of an action line matched by [ActionLineRegex], when it has
 * one ("* nick does x" -> nick). Group 2 is the bare nick.
 */
private val ActionNickRegex = Regex("^\\*[ \\t]*([~&%+]?)([$NickChars]+)")

/**
 * Matches whole CrapShack-style "+ nick" / "+ {nick}" action lines:
 * "+ {Dev\Xen} night shirin..", "+ Avian_Assassin -kick ...". The "+"
 * must be followed by whitespace and a nick (brace-wrapped or plain,
 * with an optional channel mode prefix), so IRC voice-prefixed speaker
 * lines ("+Marik: hi"), "+<nick>" mentions and "+1"-style lines never
 * match. Like [ActionLineRegex] the match covers the whole line.
 */
private val PlusActionLineRegex =
    Regex("(?m)^\\+[ \\t]+(?:\\{([~&%+]?[$NickChars]{1,39})\\}|([~&%+]?)([$NickChars]+)).*$")

/**
 * Leading nick of a "+" action-line match (from [PlusActionLineRegex]),
 * as a (range, nick) pair relative to [lineStart]. The braces of
 * "{nick}" are not part of the nick range, mirroring [actionNickRange].
 */
private fun plusActionNickRange(line: String, lineStart: Int): Pair<IntRange, String>? {
    val m = PlusActionLineRegex.find(line) ?: return null
    val g = m.groups[1] ?: m.groups[3] ?: return null
    val s = lineStart + g.range.first
    return (s..s + g.value.length - 1) to g.value
}

/**
 * Matches "Nick: message" speaker lines (furcdb/QDB/mandaliet style
 * dialogue): "allabouttrust: okay", "Stranger: Hey", multi-word
 * "Kalka Wrendi:" / "Professor Yaffe:" (up to 3 words), spaced-out
 * "n e r d b 5:", and "Ninjakossan : hahaha" (space before the colon).
 * One or two colons are allowed ("herblackwings29:: I'm bored").
 * The name must start with a letter and the colon(s) must be followed by a
 * space/tab, so URLs ("http://...") and timestamps don't match.
 */
private val ColonSpeakerRegex =
    Regex("(?m)^([A-Za-z][$NickChars]{0,28}(?: [$NickChars]{1,28}){0,2}|[A-Za-z](?: [$NickChars]){1,10})\\s*:{1,2}[ \\t]")

/**
 * Matches "[nick]" / "[@nick]" / "[X]:" speaker lines (quoteirc, wqdb,
 * crapshack, irox style). The brackets must hold a plain nick (no
 * spaces, no colons), so timestamps ("[17:05]", "[01:25pm]") and notes
 * ("[about diagnosing STDs]") never match; padding spaces just inside
 * the brackets are allowed ("[ Elim-away ]") but are not part of the
 * nick group. Group 1 is the bare nick.
 */
private val BracketSpeakerRegex =
    Regex("(?m)^\\[[ \\t]*([~&%+]?[$NickChars]{1,39})[ \\t]*\\](?::?[ \\t]|$)")

/**
 * Matches "nick>" speaker lines (bash_org, crapshack, irox, wqdb, xkcdb,
 * quoteirc, warpdrive style): "Marik> o yea", "Bug> (That really...)".
 * The name must start with a letter and ">" must be followed by a
 * space/tab, so stray "a>b" comparisons don't match. Group 1 is the nick.
 */
private val GtSpeakerRegex =
    Regex("(?m)^([A-Za-z][$NickChars]{0,28})>[ \\t]")

/**
 * Matches "{nick}" speaker lines (crapshack "{Dev\Xen}", irox "{@Melly}"
 * / "{+Marik}"). Group 1 is the bare nick.
 */
private val CurlySpeakerRegex =
    Regex("(?m)^\\{([~&%+]?[$NickChars]{1,39})\\}[ \\t]")

/**
 * Matches "-nick-" speaker lines (qdb_us "-ambrness- ..."). Group 1 is
 * the bare nick.
 */
private val DashSpeakerRegex =
    Regex("(?m)^-([A-Za-z][$NickChars]{1,28})-[ \\t]")

/**
 * Matches "(@nick)" / "(nick)" speaker lines (bash.org "(@Bargeld) i went up to
 * this...", "(StanLee) I'm tired of working today."). The @ prefix is optional;
 * the ^ line-start anchor (with (?m)) keeps mid-line parenthetical asides like
 * "(laughing)" or "(That really...)" from matching — only the speaker position
 * is colorized. Group 1 is the nick (including the @ when present, consistent
 * with <@nick>, where the @ also takes the name color). Optional channel-mode
 * status chars ([~&%+]?) are allowed before the @.
 */
private val ParenSpeakerRegex =
    Regex("(?m)^\\([ \\t]*([~&%+]?@?[$NickChars]{1,39})[ \\t]*\\)")

/**
 * The IRC nickname alphabet minus the pipe, for the "nick |" speaker
 * format below — the separator must not be swallowable as a nick char.
 */
internal val PipeNickChars: String = NickChars.replace("|", "")

/**
 * Matches "nick |" / "+nick |" / "@nick |" speaker lines ("kirch | hello",
 * "+janitor | ...", "@kirch | ..."). Group 1 is the bare nick (including
 * the @ when present, consistent with (@nick)). The ^ line-start anchor
 * (with (?m)) keeps mid-line "a | b" prose from matching. A negative
 * lookahead keeps irox "join | nick | (...)" / "signoff | ..." notices
 * from matching — those are system notices, colored as action lines.
 */
private val PipeSpeakerRegex =
    Regex("(?m)^(?!(?i)(?:join|signoff)[ \\t]*\\|)([~&%+]?@?[${PipeNickChars}]{1,39})[ \\t]*\\|(?:[ \\t]|$)")

/**
 * Matches "nick1 | nick2:" compound speaker lines (bash.org #448982 style:
 * "CwD | Asbestos: calculus is ..."). Group 1 is the first nick, group 2
 * the second (the colon-side speaker). Line-start anchored like the other
 * speaker formats so mid-line "a | b: c" prose never matches. The second
 * nick follows the ColonSpeakerRegex shape (letter-start, up to two
 * internal spaces).
 */
private val PipePairSpeakerRegex =
    Regex("(?m)^([~&%+]?@?[$PipeNickChars]{1,39})[ \\t]*\\|[ \\t]*([A-Za-z][$NickChars]{0,28}(?: [$NickChars]{1,28}){0,2})\\s*:{1,2}[ \\t]")

/**
 * IRC join/part/quit/topic notices without a leading "*":
 * "--- dave joined the chat room.", "---- sex has quit irc (Ping
 * timeout) ----", "--- Topic for #celebs-n-models is ...", irox
 * "join | nick | (...)" / "signoff | nick | (Quit) | ...". Colored and
 * read as action lines (they're system notices, like "*** quit"). The
 * keyword requirement keeps ordinary "---" prose separators untouched.
 */
private val NoticeLineRegex = Regex(
    "(?m)^(?:-{3,}[ \\t].*(?:join|part|quit|left[ \\t]|topic).*|(?:join|signoff)[ \\t]*\\|.*)$",
    RegexOption.IGNORE_CASE
)

/**
 * Leading nick of an action-line match (from [ActionLineRegex]), as a
 * (range, nick) pair relative to [lineStart], or null when the action
 * line has no leading nick ("*** quit", "*wrapped*").
 */
private fun actionNickRange(line: String, lineStart: Int): Pair<IntRange, String>? {
    val nm = ActionNickRegex.find(line) ?: return null
    val g = nm.groups[2] ?: return null
    val s = lineStart + g.range.first
    return (s..s + g.value.length - 1) to g.value
}

/**
 * Matches a bare leading name plus the rest of the line:
 * "Rhoslin fistpumps." Only treated as an action line when the name is a
 * speaker seen elsewhere in the same quote (see [bareActionMatches]), so
 * ordinary narration is never miscolored.
 */
private val BareActionLineRegex =
    Regex("(?m)^([$NickChars]{0,39}[A-Za-z][$NickChars]{0,39})[ \\t]+\\S.*$")

/** Speaker names known in [text]: <nick> mentions, "Nick:"/"[nick]:" labels. */
private fun speakerNamesIn(text: String): Set<String> {
    val names = mutableSetOf<String>()
    for (m in NickMentionRegex.findAll(text)) names += m.groupValues[2].lowercase()
    for (m in ColonSpeakerRegex.findAll(text)) names += m.groupValues[1].lowercase()
    // v1.88.36: "nick1 | nick2:" compound speaker lines: both nicks are known.
    for (m in PipePairSpeakerRegex.findAll(text)) {
        names += m.groupValues[1].lowercase()
        names += m.groupValues[2].lowercase()
    }
    for (m in BracketSpeakerRegex.findAll(text)) names += m.groupValues[1].lowercase()
    for (regex in listOf(GtSpeakerRegex, CurlySpeakerRegex, DashSpeakerRegex, ParenSpeakerRegex, PipeSpeakerRegex))
        for (m in regex.findAll(text)) names += m.groupValues[1].lowercase()
    return names
}

/**
 * Third-person action lines without any marker ("Rhoslin fistpumps."):
 * the leading name must be a speaker seen elsewhere in the same quote.
 */
private fun bareActionMatches(text: String): List<MatchResult> {
    val speakers = speakerNamesIn(text)
    if (speakers.isEmpty()) return emptyList()
    return BareActionLineRegex.findAll(text)
        .filter { it.groupValues[1].lowercase() in speakers }
        .toList()
}

/** One nick replacement inside quiz redaction: the nick's character range. */
private data class NickRepl(val start: Int, val endExcl: Int, val nick: String)

/**
 * Every nick occurrence in [displayText] (<nick> mentions, "Nick:"/"[nick]"
 * labels, "* nick" / "+ nick" action nicks, bare action-line names), with overlaps
 * resolved earliest-first. Shared by [redactNicksForQuiz] and
 * [redactedSpansForQuiz].
 */
private fun nickReplacements(displayText: String): List<NickRepl> {
    val repls = mutableListOf<NickRepl>()
    for (m in NickMentionRegex.findAll(displayText)) {
        val g = m.groups[2] ?: continue
        repls += NickRepl(g.range.first, g.range.last + 1, g.value)
    }
    // v1.57: I-Rox ?nick? speaker lines redact like <nick> mentions.
    for (m in QuestionNickRegex.findAll(displayText)) {
        val g = m.groups[1] ?: continue
        repls += NickRepl(g.range.first, g.range.last + 1, g.value)
    }
    for (m in ColonSpeakerRegex.findAll(displayText)) {
        val g = m.groups[1] ?: continue
        repls += NickRepl(g.range.first, g.range.last + 1, g.value)
    }
    // v1.88.36: "nick1 | nick2:" compound speaker lines: redact both nicks.
    for (m in PipePairSpeakerRegex.findAll(displayText)) {
        for (gi in 1..2) {
            val g = m.groups[gi] ?: continue
            repls += NickRepl(g.range.first, g.range.last + 1, g.value)
        }
    }
    for (m in BracketSpeakerRegex.findAll(displayText)) {
        val g = m.groups[1] ?: continue
        repls += NickRepl(g.range.first, g.range.last + 1, g.value)
    }
    for (regex in listOf(GtSpeakerRegex, CurlySpeakerRegex, DashSpeakerRegex, ParenSpeakerRegex, PipeSpeakerRegex)) {
        for (m in regex.findAll(displayText)) {
            val g = m.groups[1] ?: continue
            repls += NickRepl(g.range.first, g.range.last + 1, g.value)
        }
    }
    for (m in ActionLineRegex.findAll(displayText)) {
        val hit = actionNickRange(m.value, m.range.first) ?: continue
        val (range, nick) = hit
        repls += NickRepl(range.first, range.last + 1, nick)
    }
    // v1.55: "+ nick" / "+ {nick}" action nicks redact like "* nick" ones.
    for (m in PlusActionLineRegex.findAll(displayText)) {
        val hit = plusActionNickRange(m.value, m.range.first) ?: continue
        val (range, nick) = hit
        repls += NickRepl(range.first, range.last + 1, nick)
    }
    for (m in bareActionMatches(displayText)) {
        val g = m.groups[1] ?: continue
        repls += NickRepl(g.range.first, g.range.last + 1, g.value)
    }
    // Earliest match wins on overlap.
    val kept = mutableListOf<NickRepl>()
    var lastEnd = -1
    for (r in repls.sortedBy { it.start }) {
        if (r.start >= lastEnd) {
            kept += r
            lastEnd = r.endExcl
        }
    }
    return kept
}

/** End of the line containing [from] (exclusive); the newline itself excluded. */
private fun lineEndOf(text: String, from: Int): Int {
    val nl = text.indexOf('\n', from)
    return if (nl == -1) text.length else nl
}

/**
 * v1.86: one spoken-message range — the rest of the line after a
 * recognized speaker label. [bracketColor] is the format's bracket color
 * (non-null only for the bracketed `<nick>` / `?nick?` formats), used as
 * the last-resort message color.
 */
private data class MessageRange(
    val start: Int,
    val end: Int,
    val nick: String,
    val bracketColor: Color?
)

/**
 * v1.86: every spoken-message range in [displayText], across all
 * recognized speaker-label formats. Backs the user-level "Color message
 * text" control (independent of the "Color speaker names" toggle).
 */
private fun speakerMessageRanges(
    displayText: String,
    palette: NickPalette,
    ruleMap: Map<String, NickColorRule>
): List<MessageRange> {
    val out = ArrayList<MessageRange>()
    fun bracketFor(nick: String): Color? {
        val rule = ruleMap[nick.lowercase()]
        return resolveRuleColor(
            rule?.bracketsHex,
            defaultBracketColor(
                palette.bracketPalette, palette.brackets,
                nick, palette.names.size
            )
        )
    }
    fun emit(m: MatchResult, nick: String, bracketColor: Color?) {
        val s = m.range.last + 1
        val e = lineEndOf(displayText, s)
        if (s < e && displayText.substring(s, e).isNotBlank()) {
            out += MessageRange(s, e, nick, bracketColor)
        }
    }
    if (displayText.contains('<')) {
        for (m in NickMentionRegex.findAll(displayText)) {
            emit(m, m.groupValues[2], bracketFor(m.groupValues[2]))
        }
    }
    if (displayText.contains('?')) {
        for (m in QuestionNickRegex.findAll(displayText)) {
            emit(m, m.groupValues[1], bracketFor(m.groupValues[1]))
        }
    }
    if (displayText.contains('[')) {
        for (m in BracketSpeakerRegex.findAll(displayText)) {
            val g = m.groups[1] ?: continue
            emit(m, g.value, bracketFor(g.value))
        }
    }
    for (m in ColonSpeakerRegex.findAll(displayText)) {
        val g = m.groups[1] ?: continue
        emit(m, g.value, null)
    }
    // v1.88.36: "nick1 | nick2:" compound speaker lines: the message
    // belongs to the colon-side (second) nick.
    for (m in PipePairSpeakerRegex.findAll(displayText)) {
        val g2 = m.groups[2] ?: continue
        emit(m, g2.value, null)
    }
    for (regex in listOf(
        GtSpeakerRegex, CurlySpeakerRegex, DashSpeakerRegex,
        ParenSpeakerRegex, PipeSpeakerRegex
    )) {
        for (m in regex.findAll(displayText)) {
            val g = m.groups[1] ?: continue
            emit(m, g.value, null)
        }
    }
    return out
}

/**
 * v1.86: true when [line] (a single line starting at [lineStart] in the
 * full text) contains a recognized speaker label, action, or notice —
 * i.e. it is NOT an "other" line. [bareActionStarts] holds the full-text
 * offsets of bare third-person action line starts.
 */
private fun lineHasSpeakerOrAction(
    line: String,
    lineStart: Int,
    bareActionStarts: Set<Int>
): Boolean {
    if (lineStart in bareActionStarts) return true
    return NickMentionRegex.containsMatchIn(line)
        || QuestionNickRegex.containsMatchIn(line)
        || BracketSpeakerRegex.containsMatchIn(line)
        || ColonSpeakerRegex.containsMatchIn(line)
        || GtSpeakerRegex.containsMatchIn(line)
        || CurlySpeakerRegex.containsMatchIn(line)
        || DashSpeakerRegex.containsMatchIn(line)
        || ParenSpeakerRegex.containsMatchIn(line)
        || PipeSpeakerRegex.containsMatchIn(line)
        || PipePairSpeakerRegex.containsMatchIn(line)
        || ActionLineRegex.containsMatchIn(line)
        || PlusActionLineRegex.containsMatchIn(line)
        || NoticeLineRegex.containsMatchIn(line)
}

/** Character range of the line holding the first mention of the answer. */
private fun answerLineRange(redacted: String, answerPos: Int?): Pair<Int, Int>? =
    answerPos?.let { p ->
        val prevNl = if (p == 0) -1 else redacted.lastIndexOf('\n', p - 1)
        val lineStart = if (prevNl == -1) 0 else prevNl + 1
        lineStart to lineEndOf(redacted, p)
    }

/**
 * "Who said it?" question text: every nick in [displayText] (<nick>
 * mentions, "Nick:" labels, "* nick" action nicks, bare action-line
 * names) is replaced with "•••" — deliberately uncolored, so per-nick
 * colors can't leak the answer. Returns the redacted text plus the
 * (start, end-exclusive) character range of the line holding the first
 * mention of [answer] (for highlighting), or null when the answer has
 * no mention. [displayText] must already have display breaks applied.
 */
fun redactNicksForQuiz(displayText: String, answer: String): Pair<String, Pair<Int, Int>?> {
    val (redacted, _, line) = redactedSpansForQuiz(displayText, answer)
    return redacted to line
}

/**
 * One "•••" replacement in quiz-redacted text, colored by speaker:
 * [start]/[endExcl] is the character range of the "•••" in the redacted
 * string and [color] identifies the speaker. The name itself stays
 * unreadable — only the color distinguishes speakers.
 */
data class RedactedNickSpan(val start: Int, val endExcl: Int, val color: Color)

/** Fallback palette for quiz redaction when no theme palette is supplied. */
fun defaultQuizNickPalette(): NickPalette = NickPalette(
    enabled = true,
    brackets = null,
    names = DefaultNickNameColors.map { Color(it) },
    action = null,
    rules = emptyList()
)

/**
 * Like [redactNicksForQuiz], but every "•••" replacement is additionally
 * assigned a speaker color, returned as [RedactedNickSpan]s. The same nick
 * (case-insensitive) always gets the same color within the call; different
 * nicks get different colors, drawn from [palette]'s default name palette
 * (the 10 default nick name colors in the default theme). Explicit per-nick
 * color rules in [palette] win, resolved exactly like quote text coloring
 * (see [resolveRuleColor]/[defaultNameColor]). Names remain unreadable —
 * only the color identifies the speaker, so players can see when one
 * person says multiple things in a quote.
 *
 * Returns (redacted text, spans, answer line range).
 */
fun redactedSpansForQuiz(
    displayText: String,
    answer: String,
    palette: NickPalette = defaultQuizNickPalette()
): Triple<String, List<RedactedNickSpan>, Pair<Int, Int>?> {
    val kept = nickReplacements(displayText)
    val ruleMap = palette.rules.associateBy { it.nick.lowercase() }
    val names = palette.names.ifEmpty { DefaultNickNameColors.map { Color(it) } }
    // Assign one color per distinct nick in first-appearance order: the
    // same nick (case-insensitive) keeps its color; every other nick gets
    // a color not yet taken.
    val usedColors = mutableSetOf<Color>()
    val colorForNick = mutableMapOf<String, Color>()
    for (nick in kept.map { it.nick }.distinctBy { it.lowercase() }) {
        val key = nick.lowercase()
        val ruleColor = resolveRuleColor(ruleMap[key]?.nameHex, null)
        val color = when {
            ruleColor != null -> ruleColor
            else -> {
                var slot = Math.floorMod(key.hashCode(), names.size)
                var tries = 0
                while (names[slot] in usedColors && tries < names.size) {
                    slot = (slot + 1) % names.size
                    tries++
                }
                val base = names[slot]
                // Hue-rotated like the regular name coloring so colliding
                // nicks stay visually distinct.
                var rotated = spreadRotate(base, nick)
                var rt = 0
                while (rotated in usedColors && rt < 8) {
                    rotated = spreadRotate(base, "$nick#$rt")
                    rt++
                }
                rotated
            }
        }
        colorForNick[key] = color
        usedColors += color
    }
    val out = StringBuilder(displayText.length)
    val spans = mutableListOf<RedactedNickSpan>()
    var pos = 0
    var answerPos: Int? = null
    for (r in kept) {
        if (answerPos == null && r.nick.equals(answer, ignoreCase = true)) {
            answerPos = out.length
        }
        out.append(displayText, pos, r.start)
        val spanStart = out.length
        out.append("•••")
        spans += RedactedNickSpan(
            spanStart,
            out.length,
            colorForNick.getValue(r.nick.lowercase())
        )
        pos = r.endExcl
    }
    out.append(displayText, pos, displayText.length)
    val redacted = out.toString()
    return Triple(redacted, spans, answerLineRange(redacted, answerPos))
}

/**
 * Distinct nicks mentioned in [text] (<nick> mentions, "* nick" and bare
 * action lines, "Nick:"/"[nick]:" dialogue labels), in order of first
 * appearance.
 */
fun extractNicks(text: String): List<String> {
    val hits = mutableListOf<Pair<Int, String>>()
    for (m in NickMentionRegex.findAll(text)) hits.add(m.range.first to m.groupValues[2])
    // v1.57: I-Rox ?nick? speakers count as mentioned nicks too.
    for (m in QuestionNickRegex.findAll(text)) hits.add(m.range.first to m.groupValues[1])
    for (m in ActionLineRegex.findAll(text)) {
        val hit = actionNickRange(m.value, m.range.first)
        if (hit != null) hits.add(m.range.first to hit.second)
    }
    for (m in ColonSpeakerRegex.findAll(text)) hits.add(m.range.first to m.groupValues[1])
    // v1.88.36: "nick1 | nick2:" compound speaker lines: both nicks count.
    for (m in PipePairSpeakerRegex.findAll(text)) {
        hits.add(m.range.first to m.groupValues[1])
        hits.add(m.range.first to m.groupValues[2])
    }
    for (m in BracketSpeakerRegex.findAll(text)) hits.add(m.range.first to m.groupValues[1])
    for (regex in listOf(GtSpeakerRegex, CurlySpeakerRegex, DashSpeakerRegex, ParenSpeakerRegex, PipeSpeakerRegex))
        for (m in regex.findAll(text)) hits.add(m.range.first to m.groupValues[1])
    for (m in bareActionMatches(text)) hits.add(m.range.first to m.groupValues[1])
    return hits.sortedBy { it.first }.map { it.second }.distinct()
}

/** "RRGGBB" (or "#RRGGBB") -> Color, or null when unparseable. */
fun hexToColorOrNull(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return try {
        Color(hex.trim().removePrefix("#").toLong(16) or 0xFF000000)
    } catch (e: Exception) {
        null
    }
}

/**
 * Resolves a per-person rule color: null -> [default] (theme default /
 * automatic name color), [NICK_COLOR_NONE] -> null (explicitly uncolored),
 * otherwise the parsed hex color.
 */
fun resolveRuleColor(ruleHex: String?, default: Color?): Color? =
    when {
        ruleHex == NICK_COLOR_NONE -> null
        ruleHex != null -> hexToColorOrNull(ruleHex)
        else -> default
    }

/**
 * Builds the quote text with IRC coloring applied:
 * - `<nick>` / `<@nick>`: the <> get the bracket color, the name its
 *   personal rule color or a deterministic pick from the theme's default
 *   name palette.
 * - `Nick:` / `Nick :` / `Kalka Wrendi:` dialogue labels, `[nick]`
 *   bracket labels, `(@nick)` paren labels, and `?nick?` I-Rox speaker
 *   lines: the name gets the same name coloring and is tappable
 *   like <nick> names.
 * - `* action` lines (`* nick does x`, `*wrapped*`, `** kicked`,
 *   `*** quit`), `+` action lines (`+ nick does x`, `+ {nick} does x`)
 *   and bare third-person action lines ("Nick does
 *   something", with Nick a speaker elsewhere in the quote): the whole
 *   line gets the theme's action color. When the theme's
 *   action-overrides-name flag is off, the nick itself keeps its
 *   personal/default name color instead.
 * Brackets, names, and actions are each colored separately, and any of them
 * can be uncolored (null bracket/action color, empty name palette, or the
 * Quote text toggles in Settings). When coloring is disabled the plain
 * text is returned. QuoteClean normalization applies first: IRC " \"
 * separators become real line breaks when [NickPalette.ircLineBreaks] is
 * on, "/" separators when the QuoteClean "normalize line breaks" toggle
 * is on, single-line dialogue turns break per speaker when
 * [NickPalette.dialogueLineBreaks] is on, and timestamps are stripped
 * unless the user keeps them.
 */
@Composable
fun rememberNickAnnotatedText(text: String, style: TextStyle): AnnotatedString {
    val rawPalette = LocalNickPalette.current
    val clean = LocalQuoteClean.current
    val transforms = LocalDisplayTransforms.current
    val kidSafe = LocalKidSafeEnabled.current
    // v1.55: contrast-adjust the nick/bracket/action colors against the
    // effective quote-body background so they stay readable (e.g. pale
    // colors on a white card darken, dark colors on a black card
    // lighten). Hues stay recognizable; only lightness shifts. Gated by
    // the colorization toggle like everything else below.
    val quoteBg = LocalQuoteBackground.current
    val palette = remember(rawPalette, quoteBg) {
        if (quoteBg != null && rawPalette.enabled) rawPalette.contrastAdjusted(quoteBg)
        else rawPalette
    }
    // v1.53 Kid-safe mask: display-only, applied before cleaning/coloring.
    // Masking is 1:1 per character so nick offsets stay aligned.
    val maskedText = if (kidSafe) applyKidSafeMask(text) else text
    // Split first so nick offsets line up with the displayed text.
    val displayText =
        cleanDisplayText(maskedText, palette.ircLineBreaks, palette.dialogueLineBreaks, clean)
    val annotated = remember(displayText, palette, clean) {
        buildAnnotatedString {
            append(displayText)
            if (!palette.enabled) return@buildAnnotatedString
            val ruleMap = palette.rules.associateBy { it.nick.lowercase() }
            // v1.86: per-category color overrides. A chosen custom hex color
            // (contrast-adjusted against the quote background like the
            // theme colors) wins over the theme default for its category.
            val adjust: (Color?) -> Color? = { c ->
                if (c != null && quoteBg != null) ensureContrastOn(c, quoteBg) else c
            }
            val nameOverride = adjust(clean.nameColorHex?.let(::hexToColorOrNull))
            val messageOverride = adjust(clean.messageColorHex?.let(::hexToColorOrNull))
            val actionOverride = adjust(clean.actionColorHex?.let(::hexToColorOrNull))
            val timestampColor = adjust(clean.timestampColorHex?.let(::hexToColorOrNull))
            val otherColor = adjust(clean.otherColorHex?.let(::hexToColorOrNull))
            val effAction = actionOverride ?: palette.action
            fun effNameDefault(nick: String): Color? =
                nameOverride ?: defaultNameColor(palette.names, nick)
            // Action lines first: whole line in the action color. Unless the
            // theme says the action color covers the nick, the nick span
            // below overrides just the name portion.
            if (clean.colorActions) {
                for (m in ActionLineRegex.findAll(displayText)) {
                    if (effAction != null) {
                        addStyle(
                            SpanStyle(color = effAction),
                            m.range.first, m.range.last + 1
                        )
                    }
                    if (!palette.actionOverridesName) {
                        val hit = actionNickRange(m.value, m.range.first)
                            ?: continue
                        val (nickRange, nick) = hit
                        val nameColor = resolveRuleColor(
                            ruleMap[nick.lowercase()]?.nameHex,
                            effNameDefault(nick)
                        )
                        if (nameColor != null) {
                            addStyle(
                                SpanStyle(color = nameColor),
                                nickRange.first, nickRange.last + 1
                            )
                        }
                    }
                }
                // v1.55: CrapShack-style "+ nick" / "+ {nick}" action
                // lines: same action color (and nick override) as "*"
                // lines, under the same "Color action lines" toggle.
                for (m in PlusActionLineRegex.findAll(displayText)) {
                    if (effAction != null) {
                        addStyle(
                            SpanStyle(color = effAction),
                            m.range.first, m.range.last + 1
                        )
                    }
                    if (!palette.actionOverridesName) {
                        val hit = plusActionNickRange(m.value, m.range.first)
                            ?: continue
                        val (nickRange, nick) = hit
                        val nameColor = resolveRuleColor(
                            ruleMap[nick.lowercase()]?.nameHex,
                            effNameDefault(nick)
                        )
                        if (nameColor != null) {
                            addStyle(
                                SpanStyle(color = nameColor),
                                nickRange.first, nickRange.last + 1
                            )
                        }
                    }
                }
                for (m in bareActionMatches(displayText)) {
                    val nickGroup = m.groups[1] ?: continue
                    val nick = nickGroup.value
                    val rule = ruleMap[nick.lowercase()]
                    if (effAction != null) {
                        addStyle(
                            SpanStyle(color = effAction),
                            m.range.first, m.range.last + 1
                        )
                    }
                    if (!palette.actionOverridesName) {
                        val nameColor = resolveRuleColor(
                            rule?.nameHex,
                            effNameDefault(nick)
                        )
                        if (nameColor != null) {
                            addStyle(
                                SpanStyle(color = nameColor),
                                nickGroup.range.first, nickGroup.range.last + 1
                            )
                        }
                    }
                }
                // IRC join/part/quit/topic notices ("--- dave joined the
                // chat room."): whole line in the action color, like
                // "*** quit".
                for (m in NoticeLineRegex.findAll(displayText)) {
                    if (effAction != null) {
                        addStyle(
                            SpanStyle(color = effAction),
                            m.range.first, m.range.last + 1
                        )
                    }
                }
            }
            if (clean.colorNames) {
            if (displayText.contains('<')) {
                for (m in NickMentionRegex.findAll(displayText)) {
                val nick = m.groupValues[2]
                val rule = ruleMap[nick.lowercase()]
                val bracketColor = resolveRuleColor(
                rule?.bracketsHex,
                defaultBracketColor(palette.bracketPalette, palette.brackets, nick, palette.names.size)
            )
                val nameColor = resolveRuleColor(
                    rule?.nameHex,
                    effNameDefault(nick)
                )
                // '<' and '>' share the bracket color; the name gets its own.
                if (bracketColor != null) {
                    addStyle(
                        SpanStyle(color = bracketColor),
                        m.range.first, m.range.first + 1
                    )
                    addStyle(
                        SpanStyle(color = bracketColor),
                        m.range.last, m.range.last + 1
                    )
                }
                if (nameColor != null && m.range.last > m.range.first + 1) {
                    // Tag the name so it is tappable (tap -> the nick's
                    // page). Explicit range: push/pop would capture an
                    // empty range here since no text is appended between
                    // them. The whole <nick> match (brackets included) is
                    // tappable so taps on the first letter land.
                    addStringAnnotation("nick", nick, m.range.first, m.range.last + 1)
                    addStyle(
                        SpanStyle(color = nameColor),
                        m.range.first + 1,
                        m.range.last
                    )
                }
                // Optional: the speaker's message text takes the name
                // color, or the bracket color when the name has none.
                if (palette.colorMessageWithSpeaker) {
                    val msgColor = nameColor ?: bracketColor
                    if (msgColor != null) {
                        val msgStart = m.range.last + 1
                        val msgEnd = lineEndOf(displayText, msgStart)
                        if (msgStart < msgEnd &&
                            displayText.substring(msgStart, msgEnd).isNotBlank()
                        ) {
                            addStyle(SpanStyle(color = msgColor), msgStart, msgEnd)
                        }
                    }
                }
                }
            }
            // "?nick?" I-Rox speaker lines: the ? marks take the bracket
            // color, the name its personal/default name color, tappable.
            if (displayText.contains('?')) {
                for (m in QuestionNickRegex.findAll(displayText)) {
                val nick = m.groupValues[1]
                val rule = ruleMap[nick.lowercase()]
                val bracketColor = resolveRuleColor(
                rule?.bracketsHex,
                defaultBracketColor(palette.bracketPalette, palette.brackets, nick, palette.names.size)
            )
                val nameColor = resolveRuleColor(
                    rule?.nameHex,
                    effNameDefault(nick)
                )
                // '?' marks share the bracket color; the name gets its own.
                if (bracketColor != null) {
                    addStyle(
                        SpanStyle(color = bracketColor),
                        m.range.first, m.range.first + 1
                    )
                    addStyle(
                        SpanStyle(color = bracketColor),
                        m.range.last, m.range.last + 1
                    )
                }
                if (nameColor != null && m.range.last > m.range.first + 1) {
                    // Tag the name so it is tappable (tap -> the nick's
                    // page). Explicit range: push/pop would capture an
                    // empty range here since no text is appended between
                    // them. The whole <nick> match (brackets included) is
                    // tappable so taps on the first letter land.
                    addStringAnnotation("nick", nick, m.range.first, m.range.last + 1)
                    addStyle(
                        SpanStyle(color = nameColor),
                        m.range.first + 1,
                        m.range.last
                    )
                }
                // Optional: the speaker's message text takes the name
                // color, or the bracket color when the name has none.
                if (palette.colorMessageWithSpeaker) {
                    val msgColor = nameColor ?: bracketColor
                    if (msgColor != null) {
                        val msgStart = m.range.last + 1
                        val msgEnd = lineEndOf(displayText, msgStart)
                        if (msgStart < msgEnd &&
                            displayText.substring(msgStart, msgEnd).isNotBlank()
                        ) {
                            addStyle(SpanStyle(color = msgColor), msgStart, msgEnd)
                        }
                    }
                }
                }
            }
            // "[nick]" / "[@nick]" / "[X]:" labels: brackets get the bracket
            // color, the name its personal/default name color, tappable.
            if (displayText.contains('[')) {
                for (m in BracketSpeakerRegex.findAll(displayText)) {
                    val nickGroup = m.groups[1] ?: continue
                    val nick = nickGroup.value
                    val rule = ruleMap[nick.lowercase()]
                    val bracketColor = resolveRuleColor(
                        rule?.bracketsHex,
                        defaultBracketColor(
                            palette.bracketPalette, palette.brackets,
                            nick, palette.names.size
                        )
                    )
                    val nameColor = resolveRuleColor(
                        rule?.nameHex,
                        effNameDefault(nick)
                    )
                    if (bracketColor != null) {
                        addStyle(
                            SpanStyle(color = bracketColor),
                            m.range.first, m.range.first + 1
                        )
                        addStyle(
                            SpanStyle(color = bracketColor),
                            m.range.last, m.range.last + 1
                        )
                    }
                    if (nameColor != null) {
                        // Explicit range (see <nick> above): push/pop with
                        // no append in between yields an empty annotation.
                        addStringAnnotation(
                            "nick", nick,
                            nickGroup.range.first, nickGroup.range.last + 1
                        )
                        addStyle(
                            SpanStyle(color = nameColor),
                            nickGroup.range.first, nickGroup.range.last + 1
                        )
                    }
                    // Optional: the speaker's message text takes the name color.
                    if (palette.colorMessageWithSpeaker && nameColor != null) {
                        val msgStart = m.range.last + 1
                        val msgEnd = lineEndOf(displayText, msgStart)
                        if (msgStart < msgEnd &&
                            displayText.substring(msgStart, msgEnd).isNotBlank()
                        ) {
                            addStyle(SpanStyle(color = nameColor), msgStart, msgEnd)
                        }
                    }
                }
            }
            // "Nick:" dialogue labels: the name gets its personal/default
            // name color and is tappable like <nick> names.
            for (m in ColonSpeakerRegex.findAll(displayText)) {
                val nickGroup = m.groups[1] ?: continue
                val nick = nickGroup.value
                val rule = ruleMap[nick.lowercase()]
                val nameColor = resolveRuleColor(
                    rule?.nameHex,
                    effNameDefault(nick)
                )
                if (nameColor != null) {
                    // Explicit range (see <nick> above): push/pop with
                    // no append in between yields an empty annotation.
                    addStringAnnotation(
                        "nick", nick,
                        nickGroup.range.first, nickGroup.range.last + 1
                    )
                    addStyle(
                        SpanStyle(color = nameColor),
                        nickGroup.range.first,
                        nickGroup.range.last + 1
                    )
                }
                // Optional: the speaker's message text takes the name color.
                if (palette.colorMessageWithSpeaker && nameColor != null) {
                    val msgStart = m.range.last + 1
                    val msgEnd = lineEndOf(displayText, msgStart)
                    if (msgStart < msgEnd &&
                        displayText.substring(msgStart, msgEnd).isNotBlank()
                    ) {
                        addStyle(SpanStyle(color = nameColor), msgStart, msgEnd)
                    }
                }
            }
            // v1.88.36: "nick1 | nick2:" compound speaker lines (bash.org
            // #448982): both nicks get their name color and stay tappable;
            // the message takes the colon-side (second) nick's color.
            for (m in PipePairSpeakerRegex.findAll(displayText)) {
                var secondColor: Color? = null
                for (gi in 1..2) {
                    val nickGroup = m.groups[gi] ?: continue
                    val nick = nickGroup.value
                    val rule = ruleMap[nick.lowercase()]
                    val nameColor = resolveRuleColor(
                        rule?.nameHex,
                        effNameDefault(nick)
                    )
                    if (gi == 2) secondColor = nameColor
                    if (nameColor != null) {
                        // Explicit range (see <nick> above): push/pop with
                        // no append in between yields an empty annotation.
                        addStringAnnotation(
                            "nick", nick,
                            nickGroup.range.first,
                            nickGroup.range.last + 1
                        )
                        addStyle(
                            SpanStyle(color = nameColor),
                            nickGroup.range.first,
                            nickGroup.range.last + 1
                        )
                    }
                }
                // Optional: the speaker's message text takes the name color.
                if (palette.colorMessageWithSpeaker && secondColor != null) {
                    val msgStart = m.range.last + 1
                    val msgEnd = lineEndOf(displayText, msgStart)
                    if (msgStart < msgEnd &&
                        displayText.substring(msgStart, msgEnd).isNotBlank()
                    ) {
                        addStyle(SpanStyle(color = secondColor), msgStart, msgEnd)
                    }
                }
            }
            // "nick>" / "{nick}" / "-nick-" / "(@nick)" speaker labels: the name gets
            // its personal/default name color and is tappable like "Nick:".
            for (regex in listOf(GtSpeakerRegex, CurlySpeakerRegex, DashSpeakerRegex, ParenSpeakerRegex, PipeSpeakerRegex)) {
                for (m in regex.findAll(displayText)) {
                    val nickGroup = m.groups[1] ?: continue
                    val nick = nickGroup.value
                    val rule = ruleMap[nick.lowercase()]
                    val nameColor = resolveRuleColor(
                        rule?.nameHex,
                        effNameDefault(nick)
                    )
                    if (nameColor != null) {
                        // Explicit range (see <nick> above): push/pop with
                        // no append in between yields an empty annotation.
                        addStringAnnotation(
                            "nick", nick,
                            nickGroup.range.first,
                            nickGroup.range.last + 1
                        )
                        addStyle(
                            SpanStyle(color = nameColor),
                            nickGroup.range.first,
                            nickGroup.range.last + 1
                        )
                    }
                    // Optional: the speaker's message text takes the name color.
                    if (palette.colorMessageWithSpeaker && nameColor != null) {
                        val msgStart = m.range.last + 1
                        val msgEnd = lineEndOf(displayText, msgStart)
                        if (msgStart < msgEnd &&
                            displayText.substring(msgStart, msgEnd).isNotBlank()
                        ) {
                            addStyle(SpanStyle(color = nameColor), msgStart, msgEnd)
                        }
                    }
                }
            }
            } // clean.colorNames
            // v1.86: user-level "Color message text" — the words each
            // speaker says, colored independently of the "Color speaker
            // names" toggle. A chosen custom color wins; otherwise the
            // message takes the speaker's (possibly overridden) name
            // color, or the bracket color for <nick> / ?nick? when the
            // name has none. Applied after the theme-level pass so the
            // user's choice wins on overlap.
            if (clean.colorMessage) {
                for (r in speakerMessageRanges(displayText, palette, ruleMap)) {
                    val c = messageOverride
                        ?: resolveRuleColor(
                            ruleMap[r.nick.lowercase()]?.nameHex,
                            effNameDefault(r.nick)
                        )
                        ?: r.bracketColor
                    if (c != null) addStyle(SpanStyle(color = c), r.start, r.end)
                }
            }
            // v1.86: "Other lines" — anything that is not speech or an
            // action (narration, notices, join/part lines, system lines).
            // Whole-line color, applied before timestamps and names so
            // the more specific spans win.
            if (clean.colorOther && otherColor != null) {
                val bareStarts =
                    bareActionMatches(displayText).map { it.range.first }.toSet()
                var pos = 0
                while (pos < displayText.length) {
                    val end = lineEndOf(displayText, pos)
                    if (end > pos) {
                        val line = displayText.substring(pos, end)
                        if (line.isNotBlank() &&
                            !lineHasSpeakerOrAction(line, pos, bareStarts)
                        ) {
                            addStyle(SpanStyle(color = otherColor), pos, end)
                        }
                    }
                    if (end >= displayText.length) break
                    pos = end + 1
                }
            }
            // v1.86: timestamp prefixes ([17:05]) take the chosen
            // timestamp color. Timestamps only survive into displayText
            // when "Show timestamps" is on, so no extra gate is needed.
            if (timestampColor != null) {
                for (m in TimestampRegex.findAll(displayText)) {
                    val end = m.range.first + m.value.trimEnd().length
                    if (end > m.range.first) {
                        addStyle(
                            SpanStyle(color = timestampColor),
                            m.range.first, end
                        )
                    }
                }
            }
        }
    }
    // v1.51 display-only transforms (1337/upside-down/alternating case):
    // applied last, preserving every span/annotation by remapping offsets.
    return remember(annotated, transforms) {
        annotated.withDisplayTransforms(transforms)
    }
}

/**
 * Applies [DisplayTransforms] to an [AnnotatedString] while keeping every
 * span and string annotation aligned. All three transforms are 1:1 per
 * character; upside-down additionally reverses the whole string, so span
 * ranges are mirrored ([s,e) -> [n-e,n-s)).
 */
fun AnnotatedString.withDisplayTransforms(t: DisplayTransforms): AnnotatedString {
    if (!t.leet && !t.upsideDown && !t.alternatingCase) return this
    val newText = applyDisplayTransforms(text, t)
    // Safety: the transforms are defined 1:1, but if a mapping ever
    // changes length, drop the spans rather than misalign them.
    if (newText.length != text.length) return AnnotatedString(newText)
    val n = newText.length
    fun remap(s: Int, e: Int): Pair<Int, Int> =
        if (t.upsideDown) (n - e).coerceIn(0, n) to (n - s).coerceIn(0, n)
        else s to e
    return buildAnnotatedString {
        append(newText)
        this@withDisplayTransforms.spanStyles.forEach { r ->
            val (ns, ne) = remap(r.start, r.end)
            if (ns < ne) addStyle(r.item, ns, ne)
        }
        this@withDisplayTransforms.paragraphStyles.forEach { r ->
            val (ns, ne) = remap(r.start, r.end)
            if (ns < ne) addStyle(r.item, ns, ne)
        }
        this@withDisplayTransforms.getStringAnnotations(0, n).forEach { r ->
            val (ns, ne) = remap(r.start, r.end)
            if (ns <= ne) addStringAnnotation(r.tag, r.item, ns, ne)
        }
    }
}

/** Compose Color -> "#RRGGBB" for HTML font tags. */
private fun Color.toHtmlHex(): String = "#%06X".format(0xFFFFFF and toArgb())

private fun String.escapeHtml(): String {
    val out = StringBuilder(length)
    for (ch in this) {
        when (ch) {
            '&' -> out.append("&amp;")
            '<' -> out.append("&lt;")
            '>' -> out.append("&gt;")
            '"' -> out.append("&quot;")
            '\n' -> out.append("<br/>")
            else -> out.append(ch)
        }
    }
    return out.toString()
}

/**
 * v1.86: semantic kind of a quote-text span, for the standalone HTML
 * export's CSS classes. The widget path ([nickQuoteHtml]) ignores the
 * kind; the HTML export renders each kind with its `qb-…` class so custom
 * layout CSS can target the categories (speaker names, spoken message
 * text, action lines, timestamps, other lines).
 */
enum class QuoteSpanKind(val cssClass: String) {
    NAME("qb-said-name"),
    BRACKET("qb-said-bracket"),
    MESSAGE("qb-said-text"),
    ACTION("qb-action-line"),
    TIMESTAMP("qb-timestamp"),
    OTHER("qb-other-line")
}

/** One classified color span: [start, end) (exclusive end) painted [hex] as [kind]. */
private data class KindSpan(
    val start: Int,
    val end: Int,
    val hex: String,
    val kind: QuoteSpanKind
)

/**
 * Renders classified [spans] over [displayText] as HTML. Spans apply in
 * order; later spans win on overlap. Adjacent runs sharing both color
 * and kind merge into one element. With [withClasses] every colored run
 * also carries its semantic `qb-…` CSS class (standalone HTML export);
 * otherwise plain `<font color>` tags are used (widget path).
 */
private fun renderSpansToHtml(
    displayText: String,
    spans: List<KindSpan>,
    withClasses: Boolean
): String {
    val colors = arrayOfNulls<String>(displayText.length)
    val kinds = arrayOfNulls<QuoteSpanKind>(displayText.length)
    for (s in spans) {
        val from = s.start.coerceIn(0, displayText.length)
        val to = s.end.coerceIn(0, displayText.length)
        for (i in from until to) {
            colors[i] = s.hex
            kinds[i] = s.kind
        }
    }
    val out = StringBuilder(displayText.length * 2)
    var i = 0
    while (i < displayText.length) {
        val c = colors[i]
        val k = kinds[i]
        var j = i
        while (j < displayText.length && colors[j] == c && kinds[j] == k) j++
        val chunk = displayText.substring(i, j).escapeHtml()
        out.append(
            when {
                c == null -> chunk
                withClasses && k != null ->
                    "<span class=\"${k.cssClass}\" style=\"color:$c\">$chunk</span>"
                else -> "<font color=\"$c\">$chunk</font>"
            }
        )
        i = j
    }
    return out.toString()
}

private fun collectQuoteSpans(
    displayText: String,
    palette: NickPalette,
    opts: QuoteCleanOptions
): List<KindSpan> {
    // Spans in application order; later spans win on overlap, matching the
    // Compose version (action spans are added before nick-mention spans).
    val spans = mutableListOf<KindSpan>()
    val ruleMap = palette.rules.associateBy { it.nick.lowercase() }
    // v1.86: per-category color overrides. A chosen custom hex wins over
    // the theme color for its category (validated; blank = unset).
    fun optHex(h: String?): String? =
        h?.takeIf { it.isNotBlank() }?.let { hexToColorOrNull(it)?.toHtmlHex() }
    val nameHex = optHex(opts.nameColorHex)
    val messageHex = optHex(opts.messageColorHex)
    val actionHex = optHex(opts.actionColorHex)
    val timestampHex = optHex(opts.timestampColorHex)
    val otherHex = optHex(opts.otherColorHex)
    val effActionHex = actionHex ?: palette.action?.toHtmlHex()
    fun effNameDefaultHtml(nick: String): Color? =
        nameHex?.let(::hexToColorOrNull) ?: defaultNameColor(palette.names, nick)
    if (opts.colorActions) {
        for (m in ActionLineRegex.findAll(displayText)) {
            if (effActionHex != null) {
                spans += KindSpan(m.range.first, m.range.last + 1, effActionHex, QuoteSpanKind.ACTION)
            }
            if (!palette.actionOverridesName) {
                val hit = actionNickRange(m.value, m.range.first) ?: continue
                val (nickRange, nick) = hit
                val nameColor = resolveRuleColor(
                    ruleMap[nick.lowercase()]?.nameHex,
                    effNameDefaultHtml(nick)
                )
                if (nameColor != null) {
                    spans += KindSpan(
                        nickRange.first,
                        nickRange.last + 1,
                        nameColor.toHtmlHex()
                    , QuoteSpanKind.NAME)
                }
            }
        }
        for (m in bareActionMatches(displayText)) {
            val nickGroup = m.groups[1] ?: continue
            val nick = nickGroup.value
            val rule = ruleMap[nick.lowercase()]
            if (effActionHex != null) {
                spans += KindSpan(m.range.first, m.range.last + 1, effActionHex, QuoteSpanKind.ACTION)
            }
            if (!palette.actionOverridesName) {
                val nameColor = resolveRuleColor(
                    rule?.nameHex,
                    effNameDefaultHtml(nick)
                )
                if (nameColor != null) {
                    spans += KindSpan(
                        nickGroup.range.first,
                        nickGroup.range.last + 1,
                        nameColor.toHtmlHex()
                    , QuoteSpanKind.NAME)
                }
            }
        }
        // IRC join/part/quit/topic notices: whole line in the action
        // color, like "*** quit".
        for (m in NoticeLineRegex.findAll(displayText)) {
            if (effActionHex != null) {
                spans += KindSpan(m.range.first, m.range.last + 1, effActionHex, QuoteSpanKind.ACTION)
            }
        }
    }
    if (opts.colorNames) {
    if (displayText.contains('<')) {
        for (m in NickMentionRegex.findAll(displayText)) {
            val nick = m.groupValues[2]
            val rule = ruleMap[nick.lowercase()]
            val bracketColor = resolveRuleColor(
                rule?.bracketsHex,
                defaultBracketColor(palette.bracketPalette, palette.brackets, nick, palette.names.size)
            )
            val nameColor = resolveRuleColor(
                rule?.nameHex,
                effNameDefaultHtml(nick)
            )
            // '<' and '>' share the bracket color; the name gets its own.
            if (bracketColor != null) {
                val hex = bracketColor.toHtmlHex()
                spans += KindSpan(m.range.first, m.range.first + 1, hex, QuoteSpanKind.BRACKET)
                spans += KindSpan(m.range.last, m.range.last + 1, hex, QuoteSpanKind.BRACKET)
            }
            if (nameColor != null && m.range.last > m.range.first + 1) {
                spans += KindSpan(m.range.first + 1, m.range.last, nameColor.toHtmlHex(), QuoteSpanKind.NAME)
            }
            // Optional: the speaker's message text takes the name color,
            // or the bracket color when the name has none.
            if (palette.colorMessageWithSpeaker) {
                val msgColor = nameColor ?: bracketColor
                if (msgColor != null) {
                    val msgStart = m.range.last + 1
                    val msgEnd = lineEndOf(displayText, msgStart)
                    if (msgStart < msgEnd &&
                        displayText.substring(msgStart, msgEnd).isNotBlank()
                    ) {
                        spans += KindSpan(msgStart, msgEnd, msgColor.toHtmlHex(), QuoteSpanKind.MESSAGE)
                    }
                }
            }
        }
    }
    // "?nick?" I-Rox speaker lines: the ? marks take the bracket
    // color, the name its personal/default name color.
    if (displayText.contains('?')) {
        for (m in QuestionNickRegex.findAll(displayText)) {
            val nick = m.groupValues[1]
            val rule = ruleMap[nick.lowercase()]
            val bracketColor = resolveRuleColor(
                rule?.bracketsHex,
                defaultBracketColor(palette.bracketPalette, palette.brackets, nick, palette.names.size)
            )
            val nameColor = resolveRuleColor(
                rule?.nameHex,
                effNameDefaultHtml(nick)
            )
            // '?' marks share the bracket color; the name gets its own.
            if (bracketColor != null) {
                val hex = bracketColor.toHtmlHex()
                spans += KindSpan(m.range.first, m.range.first + 1, hex, QuoteSpanKind.BRACKET)
                spans += KindSpan(m.range.last, m.range.last + 1, hex, QuoteSpanKind.BRACKET)
            }
            if (nameColor != null && m.range.last > m.range.first + 1) {
                spans += KindSpan(m.range.first + 1, m.range.last, nameColor.toHtmlHex(), QuoteSpanKind.NAME)
            }
            // Optional: the speaker's message text takes the name color,
            // or the bracket color when the name has none.
            if (palette.colorMessageWithSpeaker) {
                val msgColor = nameColor ?: bracketColor
                if (msgColor != null) {
                    val msgStart = m.range.last + 1
                    val msgEnd = lineEndOf(displayText, msgStart)
                    if (msgStart < msgEnd &&
                        displayText.substring(msgStart, msgEnd).isNotBlank()
                    ) {
                        spans += KindSpan(msgStart, msgEnd, msgColor.toHtmlHex(), QuoteSpanKind.MESSAGE)
                    }
                }
            }
        }
    }
    // "[nick]" / "[@nick]" / "[X]:" labels.
    if (displayText.contains('[')) {
        for (m in BracketSpeakerRegex.findAll(displayText)) {
            val nickGroup = m.groups[1] ?: continue
            val nick = nickGroup.value
            val rule = ruleMap[nick.lowercase()]
            val bracketColor = resolveRuleColor(
                rule?.bracketsHex,
                defaultBracketColor(
                    palette.bracketPalette, palette.brackets,
                    nick, palette.names.size
                )
            )
            val nameColor = resolveRuleColor(
                rule?.nameHex,
                effNameDefaultHtml(nick)
            )
            if (bracketColor != null) {
                val hex = bracketColor.toHtmlHex()
                spans += KindSpan(m.range.first, m.range.first + 1, hex, QuoteSpanKind.BRACKET)
                spans += KindSpan(m.range.last, m.range.last + 1, hex, QuoteSpanKind.BRACKET)
            }
            if (nameColor != null) {
                spans += KindSpan(
                    nickGroup.range.first,
                    nickGroup.range.last + 1,
                    nameColor.toHtmlHex()
                , QuoteSpanKind.NAME)
            }
            // Optional: the speaker's message text takes the name color.
            if (palette.colorMessageWithSpeaker && nameColor != null) {
                val msgStart = m.range.last + 1
                val msgEnd = lineEndOf(displayText, msgStart)
                if (msgStart < msgEnd &&
                    displayText.substring(msgStart, msgEnd).isNotBlank()
                ) {
                    spans += KindSpan(msgStart, msgEnd, nameColor.toHtmlHex(), QuoteSpanKind.MESSAGE)
                }
            }
        }
    }
    // "Nick:" dialogue labels: the name gets its personal/default name color.
    for (m in ColonSpeakerRegex.findAll(displayText)) {
        val nickGroup = m.groups[1] ?: continue
        val nick = nickGroup.value
        val rule = ruleMap[nick.lowercase()]
        val nameColor = resolveRuleColor(
            rule?.nameHex,
            effNameDefaultHtml(nick)
        )
        if (nameColor != null) {
            spans += KindSpan(
                nickGroup.range.first,
                nickGroup.range.last + 1,
                nameColor.toHtmlHex()
            , QuoteSpanKind.NAME)
            // Optional: the speaker's message text takes the name color.
            if (palette.colorMessageWithSpeaker) {
                val msgStart = m.range.last + 1
                val msgEnd = lineEndOf(displayText, msgStart)
                if (msgStart < msgEnd &&
                    displayText.substring(msgStart, msgEnd).isNotBlank()
                ) {
                    spans += KindSpan(msgStart, msgEnd, nameColor.toHtmlHex(), QuoteSpanKind.MESSAGE)
                }
            }
        }
    }
    // v1.88.36: "nick1 | nick2:" compound speaker lines: both nicks get
    // NAME spans; the message takes the colon-side (second) nick's color.
    for (m in PipePairSpeakerRegex.findAll(displayText)) {
        for (gi in 1..2) {
            val nickGroup = m.groups[gi] ?: continue
            val nick = nickGroup.value
            val rule = ruleMap[nick.lowercase()]
            val nameColor = resolveRuleColor(
                rule?.nameHex,
                effNameDefaultHtml(nick)
            )
            if (nameColor != null) {
                spans += KindSpan(
                    nickGroup.range.first,
                    nickGroup.range.last + 1,
                    nameColor.toHtmlHex()
                , QuoteSpanKind.NAME)
                // Optional: the speaker's message text takes the name color.
                if (gi == 2 && palette.colorMessageWithSpeaker) {
                    val msgStart = m.range.last + 1
                    val msgEnd = lineEndOf(displayText, msgStart)
                    if (msgStart < msgEnd &&
                        displayText.substring(msgStart, msgEnd).isNotBlank()
                    ) {
                        spans += KindSpan(msgStart, msgEnd, nameColor.toHtmlHex(), QuoteSpanKind.MESSAGE)
                    }
                }
            }
        }
    }
    // "nick>" / "{nick}" / "-nick-" / "(@nick)" speaker labels: same treatment as
    // "Nick:" labels.
    for (regex in listOf(GtSpeakerRegex, CurlySpeakerRegex, DashSpeakerRegex, ParenSpeakerRegex, PipeSpeakerRegex)) {
        for (m in regex.findAll(displayText)) {
            val nickGroup = m.groups[1] ?: continue
            val nick = nickGroup.value
            val rule = ruleMap[nick.lowercase()]
            val nameColor = resolveRuleColor(
                rule?.nameHex,
                effNameDefaultHtml(nick)
            )
            if (nameColor != null) {
                spans += KindSpan(
                    nickGroup.range.first,
                    nickGroup.range.last + 1,
                    nameColor.toHtmlHex()
                , QuoteSpanKind.NAME)
                // Optional: the speaker's message text takes the name color.
                if (palette.colorMessageWithSpeaker) {
                    val msgStart = m.range.last + 1
                    val msgEnd = lineEndOf(displayText, msgStart)
                    if (msgStart < msgEnd &&
                        displayText.substring(msgStart, msgEnd).isNotBlank()
                    ) {
                        spans += KindSpan(msgStart, msgEnd, nameColor.toHtmlHex(), QuoteSpanKind.MESSAGE)
                    }
                }
            }
        }
    }
    } // opts.colorNames
    // v1.86: user-level "Color message text" — the words each speaker
    // says. A chosen custom color wins; otherwise the message takes the
    // speaker's (possibly overridden) name color, or the bracket color
    // for <nick> / ?nick? when the name has none. Applied after the
    // theme-level pass so the user's choice wins on overlap.
    if (opts.colorMessage) {
        for (r in speakerMessageRanges(displayText, palette, ruleMap)) {
            val c = messageHex
                ?: resolveRuleColor(
                    ruleMap[r.nick.lowercase()]?.nameHex,
                    effNameDefaultHtml(r.nick)
                )?.toHtmlHex()
                ?: r.bracketColor?.toHtmlHex()
            if (c != null) spans += KindSpan(r.start, r.end, c, QuoteSpanKind.MESSAGE)
        }
    }
    // v1.86: "Other lines" — anything that is not speech or an action.
    // Applied before timestamps and names so more specific spans win.
    if (opts.colorOther && otherHex != null) {
        val bareStarts =
            bareActionMatches(displayText).map { it.range.first }.toSet()
        var pos = 0
        while (pos < displayText.length) {
            val end = lineEndOf(displayText, pos)
            if (end > pos) {
                val line = displayText.substring(pos, end)
                if (line.isNotBlank() &&
                    !lineHasSpeakerOrAction(line, pos, bareStarts)
                ) {
                    spans += KindSpan(pos, end, otherHex, QuoteSpanKind.OTHER)
                }
            }
            if (end >= displayText.length) break
            pos = end + 1
        }
    }
    // v1.86: timestamp prefixes ([17:05]) take the chosen timestamp
    // color. Timestamps only survive into displayText when "Show
    // timestamps" is on, so no extra gate is needed.
    if (timestampHex != null) {
        for (m in TimestampRegex.findAll(displayText)) {
            val end = m.range.first + m.value.trimEnd().length
            if (end > m.range.first) {
                spans += KindSpan(m.range.first, end, timestampHex, QuoteSpanKind.TIMESTAMP)
            }
        }
    }
    return spans
}

/**
 * Widget-friendly version of [rememberNickAnnotatedText]: returns HTML with
 * `<font color>` spans and `<br/>` line breaks, honoring the same palette
 * rules (bracket color, name colors, "Nick:"/"[nick]:" labels, action
 * lines, per-person rules, and the IRC line-separator setting) plus the
 * QuoteClean [opts] (name/action coloring, "/" normalization, timestamps).
 * Render with Html.fromHtml().
 */
fun nickQuoteHtml(
    text: String,
    palette: NickPalette,
    opts: QuoteCleanOptions = QuoteCleanOptions()
): String {
    // Split first so nick offsets line up with the displayed text.
    val displayText =
        cleanDisplayText(text, palette.ircLineBreaks, palette.dialogueLineBreaks, opts)
    if (!palette.enabled) return displayText.escapeHtml()
    // Spans in application order; later spans win on overlap, matching the
    // Compose version (action spans are added before nick-mention spans).
    return renderSpansToHtml(
        displayText,
        collectQuoteSpans(displayText, palette, opts),
        withClasses = false
    )
}

/**
 * v1.86: standalone-HTML-export version of [nickQuoteHtml]. Same
 * classification and colors, but every colored run also carries a
 * semantic CSS class (`qb-said-name`, `qb-said-bracket`, `qb-said-text`,
 * `qb-action-line`, `qb-timestamp`, `qb-other-line`) so custom layout CSS
 * can target the categories. Colors stay inline so the page looks right
 * even without custom CSS.
 */
fun quoteTextHtmlWithClasses(
    text: String,
    palette: NickPalette,
    opts: QuoteCleanOptions = QuoteCleanOptions()
): String {
    // Split first so nick offsets line up with the displayed text.
    val displayText =
        cleanDisplayText(text, palette.ircLineBreaks, palette.dialogueLineBreaks, opts)
    if (!palette.enabled) return displayText.escapeHtml()
    return renderSpansToHtml(
        displayText,
        collectQuoteSpans(displayText, palette, opts),
        withClasses = true
    )
}

/** One nick color span for the bitmap export: [start, endExcl) painted [argb]. */
data class ExportNickSpan(val start: Int, val endExcl: Int, val argb: Int)

/**
 * Non-composable nick colors for the site-card bitmap export, mirroring
 * [rememberNickAnnotatedText]'s name/bracket coloring. Scans the exact
 * [displayText] being laid out (so ranges always line up), resolves each
 * nick's color from [palette] (per-nick rules win, then the palette's
 * deterministic name color), contrast-adjusted against [background] like
 * the on-screen card. Empty when coloring is off.
 */
fun nickExportSpans(
    displayText: String,
    palette: NickPalette,
    background: Color
): List<ExportNickSpan> {
    if (!palette.enabled || displayText.isEmpty()) return emptyList()
    val adj = palette.contrastAdjusted(background)
    val ruleMap = adj.rules.associateBy { it.nick.lowercase() }
    val spans = mutableListOf<ExportNickSpan>()
    val covered = BooleanArray(displayText.length)
    /** Claims [start, endExcl) if no span has taken any of it yet. */
    fun claim(start: Int, endExcl: Int): Boolean {
        val s = start.coerceIn(0, covered.size)
        val e = endExcl.coerceIn(0, covered.size)
        if (s >= e) return false
        for (i in s until e) if (covered[i]) return false
        for (i in s until e) covered[i] = true
        return true
    }
    if (displayText.contains('<')) {
        for (m in NickMentionRegex.findAll(displayText)) {
            val nick = m.groupValues[2]
            if (nick.isEmpty()) continue
            val rule = ruleMap[nick.lowercase()]
            val bracketArgb = resolveRuleColor(
                rule?.bracketsHex,
                defaultBracketColor(adj.bracketPalette, adj.brackets, nick, adj.names.size)
            )?.toArgb()
            val nameArgb = resolveRuleColor(
                rule?.nameHex,
                defaultNameColor(adj.names, nick)
            )?.toArgb()
            // '<' and '>' share the bracket color; the name gets its own.
            if (bracketArgb != null && claim(m.range.first, m.range.first + 1))
                spans += ExportNickSpan(m.range.first, m.range.first + 1, bracketArgb)
            if (bracketArgb != null && claim(m.range.last, m.range.last + 1))
                spans += ExportNickSpan(m.range.last, m.range.last + 1, bracketArgb)
            val g = m.groups[2] ?: continue
            if (nameArgb != null && claim(g.range.first, g.range.last + 1))
                spans += ExportNickSpan(g.range.first, g.range.last + 1, nameArgb)
        }
    }
    // "[nick]" / "[@nick]" / "[X]:" speaker labels (bash.org, quoteirc,
    // wqdb...): brackets take the bracket color, the name its personal /
    // default name color — mirroring rememberNickAnnotatedText. v1.61.11:
    // this was the missing piece — bash.org quotes use [nick], so exports
    // never colorized them.
    if (displayText.contains('[')) {
        for (m in BracketSpeakerRegex.findAll(displayText)) {
            val g = m.groups[1] ?: continue
            val nick = g.value
            if (nick.isEmpty()) continue
            val rule = ruleMap[nick.lowercase()]
            val bracketArgb = resolveRuleColor(
                rule?.bracketsHex,
                defaultBracketColor(adj.bracketPalette, adj.brackets, nick, adj.names.size)
            )?.toArgb()
            val nameArgb = resolveRuleColor(
                rule?.nameHex,
                defaultNameColor(adj.names, nick)
            )?.toArgb()
            // '[' and ']' share the bracket color; the name gets its own.
            if (bracketArgb != null && claim(m.range.first, m.range.first + 1))
                spans += ExportNickSpan(m.range.first, m.range.first + 1, bracketArgb)
            if (bracketArgb != null && claim(m.range.last, m.range.last + 1))
                spans += ExportNickSpan(m.range.last, m.range.last + 1, bracketArgb)
            if (nameArgb != null && claim(g.range.first, g.range.last + 1))
                spans += ExportNickSpan(g.range.first, g.range.last + 1, nameArgb)
        }
    }
    // "Nick:" / "Nick :" speaker labels take the name color.
    for (m in ColonSpeakerRegex.findAll(displayText)) {
        val g = m.groups[1] ?: continue
        val nick = g.value
        if (nick.isEmpty()) continue
        val nameArgb = resolveRuleColor(
            ruleMap[nick.lowercase()]?.nameHex,
            defaultNameColor(adj.names, nick)
        )?.toArgb() ?: continue
        if (claim(g.range.first, g.range.last + 1))
            spans += ExportNickSpan(g.range.first, g.range.last + 1, nameArgb)
    }
    // "nick |" / "+nick |" / "@nick |" speaker labels take the name color.
    for (m in PipeSpeakerRegex.findAll(displayText)) {
        val g = m.groups[1] ?: continue
        val nick = g.value
        if (nick.isEmpty()) continue
        val nameArgb = resolveRuleColor(
            ruleMap[nick.lowercase()]?.nameHex,
            defaultNameColor(adj.names, nick)
        )?.toArgb() ?: continue
        if (claim(g.range.first, g.range.last + 1))
            spans += ExportNickSpan(g.range.first, g.range.last + 1, nameArgb)
    }
    return spans.sortedBy { it.start }
}
