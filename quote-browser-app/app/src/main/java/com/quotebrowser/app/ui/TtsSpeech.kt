package com.quotebrowser.app.ui

import com.quotebrowser.app.data.plugins.PluginManager

/**
 * Builds the text actually spoken by "Read aloud".
 *
 * IRC-style quotes are turned into natural dialogue instead of reading
 * punctuation literally:
 * - `<TheMaxx> hello`            -> "TheMaxx says: hello"
 * - a later `<Bob> hi`           -> "Bob replies: hi"
 * - `* TheMaxx waves` (an action)-> "TheMaxx waves"
 * - plain lines are read as-is.
 *
 * Nicks are never spelled out symbol-by-symbol: "<", ">", "{Dev\Xen}" style
 * decorations are stripped, so "{Dev\Xen}" is spoken as "Dev Xen".
 */
private val ircControlCodes =
    Regex("[\u0002\u000F\u0016\u001D\u001F]|\u0003[0-9]{0,2}(,[0-9]{1,2})?")

private val nickLinePattern = Regex("^<([^<>\n]{1,40})>\\s?(.*)$")

/**
 * Full-line IRC action / notice ("* nick does x", "*wrapped*", "** kicked",
 * "*** quit"): the whole line is one action turn. Inline *emphasis* never
 * matches (the "*" must start the line).
 */
private val actionLinePattern = Regex("^\\*[ \\t]*\\S.*$")

/** "Nick: message" speaker line, incl. multi-word "Kalka Wrendi:" (see NickColors.kt). */
private val colonSpeakerLinePattern =
    Regex("^([A-Za-z][$NickChars]{0,28}(?: [$NickChars]{1,28}){0,2}|[A-Za-z](?: [$NickChars]){1,10})\\s*:[ \\t]+(.*)$")

/** "[nick]" / "[@nick]" / "[X]:" speaker line (quoteirc/wqdb/crapshack/irox style). */
private val bracketSpeakerLinePattern =
    Regex("^\\[([^<>\\[\\]\\n:]{1,40})\\]:?[ \\t]*(.*)$")

/** "nick>" speaker line (bash_org/crapshack/irox/wqdb/xkcdb/quoteirc style). */
private val gtSpeakerLinePattern =
    Regex("^([A-Za-z][$NickChars]{0,28})>[ \\t]+(.*)$")

/** "{nick}" speaker line (crapshack "{Dev\Xen}", irox "{@Melly}"). */
private val curlySpeakerLinePattern =
    Regex("^\\{([~&%+]?[$NickChars]{1,39})\\}[ \\t]+(.*)$")

/** "-nick-" speaker line (qdb_us "-ambrness- ..."). */
private val dashSpeakerLinePattern =
    Regex("^-([A-Za-z][$NickChars]{1,28})-[ \\t]+(.*)$")

/**
 * "nick |" / "+nick |" / "@nick |" speaker line ("kirch | hello",
 * "+janitor | ..."). Group 1 is the nick, group 2 the message. The
 * negative lookahead keeps irox "join | nick | (...)" / "signoff | ..."
 * notices on the narrator voice (see [noticeLinePattern]).
 */
private val pipeSpeakerLinePattern =
    Regex("^(?!(?i)(?:join|signoff)[ \\t]*\\|)([~&%+]?@?[${PipeNickChars}]{1,39})[ \\t]*\\|[ \\t]*(.*)$")

/**
 * IRC join/part/quit/topic notices without a leading "*":
 * "--- dave joined the chat room.", "---- sex has quit irc (Ping
 * timeout) ----", "--- Topic for #celebs-n-models is ...", irox
 * "join | nick | (...)". One narrator turn, like *action* lines.
 */
private val noticeLinePattern = Regex(
    "^(?:-{3,}[ \\t].*(?:join|part|quit|left[ \\t]|topic).*|(?:join|signoff)[ \\t]*\\|.*)$",
    RegexOption.IGNORE_CASE
)

/** Bare third-person action line: "Rhoslin fistpumps." */
private val bareActionLinePattern =
    Regex("^([$NickChars]{0,39}[A-Za-z][$NickChars]{0,39})[ \\t]+(\\S.*)$")

private val nickSymbolPattern = Regex("[^A-Za-z0-9 ]+")

private val whitespacePattern = Regex("\\s+")

/** Same " \" -> line break rule the quote display uses. */
private val ircLineSeparatorRegex = Regex("\\s+\\\\\\s*")

/** Applies the IRC line-separator rule so TTS reads what the screen shows. */
fun applyIrcLineBreaks(text: String): String =
    ircLineSeparatorRegex.replace(text, "\n")

/**
 * Breaks single-line dialogue into turns: a <nick> (or {nick}) that
 * follows quoted text — or a closing bracket/paren — starts its own
 * line. Only spaces/tabs may sit between the trigger and the nick, so
 * quotes that already span lines are untouched.
 */
private val dialogueBreakRegex =
    Regex("([\"\"''\u201C\u201D\\]\\)])[ \t]*(?=(<[A-Za-z0-9_@\\-\\\\|\\[\\]{}^`]+>|\\{[A-Za-z0-9_@\\-\\\\|\\[\\]{}^`]+\\}))")

fun applyDialogueLineBreaks(text: String): String =
    dialogueBreakRegex.replace(text, "$1\n")

/**
 * Display line breaks: IRC " \" separators and/or dialogue-turn breaks,
 * then the "Break between quotes" paragraph separation (v1.88.22, driven
 * by the DisplayPrefs setting). Display-only — the TTS speech path never
 * calls this; it uses [applyIrcLineBreaks]/[applyDialogueLineBreaks]
 * directly.
 */
fun applyDisplayBreaks(text: String, irc: Boolean, dialogue: Boolean): String {
    var out = text
    if (irc) out = applyIrcLineBreaks(out)
    if (dialogue) out = applyDialogueLineBreaks(out)
    return paragraphizeBody(out)
}

/** Strips every non-alphanumeric symbol from a nick so TTS says the name. */
fun cleanSpokenNick(rawNick: String): String {
    val stripped = ircControlCodes.replace(rawNick, "")
    return whitespacePattern
        .replace(nickSymbolPattern.replace(stripped, " "), " ")
        .trim()
        .ifEmpty { "someone" }
}

/**
 * One read-aloud turn: the speaker nick (null for narration lines), the
 * text to speak, and whether it is an *action* line. Action lines are read
 * in the dedicated narrator voice, never in a speaker's voice.
 */
data class SpokenTurn(
    val nick: String?,
    val text: String,
    val isAction: Boolean = false
)

/**
 * All installed TTS voices in a stable (name-sorted) order.
 */
fun installedVoices(tts: android.speech.tts.TextToSpeech): List<android.speech.tts.Voice> =
    try {
        tts.voices?.toList()?.sortedBy { it.name }.orEmpty()
    } catch (e: Exception) {
        emptyList()
    }

/**
 * A stable installed TTS voice for a speaker nick: the same person always
 * gets the same voice across sessions. Returns null for narration (nick ==
 * null/blank) so the system default voice is used. [overrideName], when it
 * names one of the installed voices, wins over the hash assignment (the
 * per-nick override picked on the nick's page).
 */
/**
 * Sets the TTS voice without crashing: the framework's setVoice() throws a
 * NullPointerException when passed null (it dereferences the voice
 * internally), so a null voice (narration turn, no installed voices, or an
 * engine with no default) falls back to the engine's default voice, and if
 * that is unavailable the call is skipped entirely.
 */
fun applyVoiceOrDefault(
    tts: android.speech.tts.TextToSpeech,
    voice: android.speech.tts.Voice?
) {
    val v = voice ?: runCatching { tts.defaultVoice }.getOrNull() ?: return
    tts.voice = v
}

fun voiceForNick(
    tts: android.speech.tts.TextToSpeech,
    nick: String?,
    overrideName: String? = null,
    /** v1.62: when false, only explicit overrides resolve; else default voice. */
    autoAssign: Boolean = true
): android.speech.tts.Voice? {
    if (nick.isNullOrBlank()) return null
    val voices = installedVoices(tts)
    if (voices.isEmpty()) return null
    if (!overrideName.isNullOrBlank()) {
        // Manual per-nick overrides win and may use any language.
        voices.firstOrNull { it.name == overrideName }?.let { return it }
    }
    if (!autoAssign) return null
    // v1.55: automatic assignment prefers voices matching the TTS engine's
    // default voice language (so e.g. an English engine doesn't assign a
    // French voice), falling back to the device locale language, then all
    // installed voices.
    val engineLang = runCatching { tts.defaultVoice?.locale?.language }.getOrNull()
    val deviceLang = java.util.Locale.getDefault().language
    val pool = when {
        engineLang != null && voices.any { it.locale.language == engineLang } ->
            voices.filter { it.locale.language == engineLang }
        voices.any { it.locale.language == deviceLang } ->
            voices.filter { it.locale.language == deviceLang }
        else -> voices
    }
    // v1.63: gender-aware pick. A nick that reads female (male) prefers
    // installed voices whose names carry that gender hint, then voices
    // with no gender hint, then the whole pool — the same nick still
    // always lands on the same voice.
    val want = guessNickGender(nick)
    val ordered = when (want) {
        NickGender.UNKNOWN -> pool
        else -> (pool.filter { guessVoiceGender(it.name) == want } +
                pool.filter { guessVoiceGender(it.name) == NickGender.UNKNOWN } +
                pool).distinct()
    }
    return ordered[Math.floorMod(nick.lowercase().hashCode(), ordered.size)]
}

/**
 * True when the "says"/"replies" speaker announcements can be dropped:
 * speaker voices are on and the quote has at least two distinct speakers,
 * so the per-speaker voice (or the pitch/rate fallback below, when the
 * engine exposes fewer than two distinct voices) identifies who is
 * talking.
 */
fun dropSpeakerAnnouncements(nicks: List<String?>): Boolean =
    nicks.filter { !it.isNullOrBlank() }.distinct().size >= 2

/**
 * Deterministic pitch/rate variation for a speaker nick, used as a
 * fallback when the TTS engine cannot give every speaker a distinct
 * installed voice: speakers stay distinguishable by ear. Returns
 * (pitch, speechRate); neutral (1, 1) for narration (null nick).
 */
fun speakerPitchRate(nick: String?): Pair<Float, Float> {
    if (nick.isNullOrBlank()) return 1.0f to 1.0f
    val h = Math.floorMod(nick.lowercase().hashCode(), 1000)
    val pitch = 0.85f + (h % 7) * 0.05f // 0.85 .. 1.15
    val rate = 0.95f + ((h / 7) % 5) * 0.025f // 0.95 .. 1.05
    return pitch to rate
}

/**
 * True when the per-speaker pitch/rate fallback is needed for [nicks]:
 * speaker voices are on with 2+ speakers, but the engine cannot give
 * each speaker a distinct installed voice.
 */
fun needsPitchRateFallback(
    tts: android.speech.tts.TextToSpeech,
    nicks: List<String?>,
    overrideNameFor: (String) -> String? = { null },
    /** v1.62: with auto-assign off everyone shares the default voice by design. */
    autoAssign: Boolean = true
): Boolean {
    if (!autoAssign) return false
    val speakers = nicks.mapNotNull { it?.takeIf { n -> n.isNotBlank() } }.distinct()
    if (speakers.size < 2) return false
    val voices = installedVoices(tts)
    if (voices.size < 2) return true
    val assigned = speakers.mapTo(mutableSetOf()) {
        voiceForNick(tts, it, overrideNameFor(it), autoAssign)
    }
    return assigned.size < speakers.size
}

/**
 * Applies one speaker turn's voice: the nick's stable installed voice (or
 * their chosen override when set), plus the deterministic pitch/rate
 * variation when [fallbackPitchRate] is true. Narration turns (nick ==
 * null) restore the engine default voice and neutral pitch/rate.
 * Returns the speech-rate multiplier applied, so callers that layer
 * their own speed on top (read-along) can multiply instead of
 * overwriting a voice-pack preset.
 */
fun applySpeakerVoice(
    tts: android.speech.tts.TextToSpeech,
    nick: String?,
    overrideName: String? = null,
    fallbackPitchRate: Boolean = false,
    /** v1.62: when false, only explicit overrides resolve; else default voice. */
    autoAssign: Boolean = true
): Float {
    // v1.87: voice-pack presets for the built-in engine apply their
    // pitch/rate to the default voice. An explicit pack choice always
    // wins over the deterministic fallback.
    val packParams = overrideName?.let { PluginManager.resolveBuiltinVoicePitchRate(it) }
    if (packParams != null) {
        applyVoiceOrDefault(tts, null)
        tts.setPitch(packParams.first)
        tts.setSpeechRate(packParams.second)
        return packParams.second
    }
    applyVoiceOrDefault(tts, voiceForNick(tts, nick, overrideName, autoAssign))
    return if (fallbackPitchRate && !nick.isNullOrBlank()) {
        val (pitch, rate) = speakerPitchRate(nick)
        tts.setPitch(pitch)
        tts.setSpeechRate(rate)
        rate
    } else {
        tts.setPitch(1.0f)
        tts.setSpeechRate(1.0f)
        1.0f
    }
}

/**
 * Applies the narrator voice for *action* line turns: the user's chosen
 * narrator voice when it names an installed voice, otherwise the engine
 * default voice with a slightly lowered pitch so narration stays audibly
 * distinct from every speaker voice.
 * Returns the speech-rate multiplier applied, so callers that layer
 * their own speed on top (read-along) can multiply instead of
 * overwriting a voice-pack preset.
 */
fun applyNarratorVoice(
    tts: android.speech.tts.TextToSpeech,
    narratorVoiceName: String? = null
): Float {
    // v1.87: voice-pack narrator presets apply their pitch/rate to the
    // default voice.
    val packParams = narratorVoiceName?.let { PluginManager.resolveBuiltinVoicePitchRate(it) }
    if (packParams != null) {
        applyVoiceOrDefault(tts, null)
        tts.setPitch(packParams.first)
        tts.setSpeechRate(packParams.second)
        return packParams.second
    }
    val voice = if (!narratorVoiceName.isNullOrBlank())
        installedVoices(tts).firstOrNull { it.name == narratorVoiceName }
    else null
    applyVoiceOrDefault(tts, voice)
    tts.setPitch(0.92f)
    tts.setSpeechRate(1.0f)
    return 1.0f
}

/**
 * The single-voice text actually spoken by "Read aloud": the quote's
 * speaker turns (with "says"/"replies" announcements) joined into one
 * string, after the same QuoteClean normalization the display uses
 * (timestamps stripped, "/" and "\" separators split into turns).
 */
fun buildSpokenQuoteText(raw: String, dialogueBreaks: Boolean = true): String =
    buildSpokenTurns(raw, dialogueBreaks, announceSpeakers = true)
        .joinToString(" ") { it.text }

/**
 * Breaks a quote into speaker turns for per-nick TTS voices.
 * Each turn carries the speaker nick (null for narration and *action*
 * lines) and whether it is an action line; action lines are read in the
 * dedicated narrator voice, never in a speaker's voice.
 * When [announceSpeakers] is false the "nick says"/"nick replies" lead-in
 * is dropped (the voice change identifies the speaker).
 *
 * The text is normalized exactly like the display pipeline
 * ([cleanSpokenText]): timestamps are stripped and "/" + "\" separators
 * become turn boundaries, so speech and display agree on who is speaking.
 */
fun buildSpokenTurns(
    raw: String,
    dialogueBreaks: Boolean = true,
    announceSpeakers: Boolean = true
): List<SpokenTurn> {
    val turns = mutableListOf<SpokenTurn>()
    var announced = false
    val text = cleanSpokenText(raw, dialogueBreaks)
    val lines = text.split("\n").map { ircControlCodes.replace(it, "").trim() }
    // Speakers known in this quote: <nick>, "[nick]" and "Nick:" labels.
    val speakers = mutableSetOf<String>()
    for (line in lines) {
        if (line.isEmpty()) continue
        nickLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        bracketSpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        colonSpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        gtSpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        curlySpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        dashSpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        pipeSpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
    }
    for (line in lines) {
        if (line.isEmpty()) continue
        val nickMatch = nickLinePattern.matchEntire(line)
        if (nickMatch != null) {
            val nick = cleanSpokenNick(nickMatch.groupValues[1])
            val message = nickMatch.groupValues[2].trim()
            val spoken = if (announceSpeakers) {
                val verb = if (!announced) "says" else "replies"
                announced = true
                buildString {
                    append(nick).append(' ').append(verb)
                    if (message.isNotEmpty()) append(": ").append(message)
                }
            } else {
                if (message.isNotEmpty()) message else nick
            }
            turns += SpokenTurn(nick, spoken)
            continue
        }
        if (actionLinePattern.matches(line)) {
            // Full-line *action* / **notice / ***notice: one action turn,
            // read by the narrator voice. Strip the marker asterisks (and
            // one trailing "*" for "*wrapped*" style); never read them.
            var spoken = line.trimStart().trimStart('*').trim()
            if (spoken.endsWith("*") && spoken.length > 1) {
                spoken = spoken.dropLast(1).trim()
            }
            if (spoken.isNotEmpty()) {
                turns += SpokenTurn(null, spoken, isAction = true)
            }
            continue
        }
        val bracketMatch = bracketSpeakerLinePattern.matchEntire(line)
        if (bracketMatch != null) {
            val nick = cleanSpokenNick(bracketMatch.groupValues[1])
            val message = bracketMatch.groupValues[2].trim()
            val spoken = if (announceSpeakers) {
                val verb = if (!announced) "says" else "replies"
                announced = true
                buildString {
                    append(nick).append(' ').append(verb)
                    if (message.isNotEmpty()) append(": ").append(message)
                }
            } else {
                if (message.isNotEmpty()) message else nick
            }
            turns += SpokenTurn(nick, spoken)
            continue
        }
        val colonMatch = colonSpeakerLinePattern.matchEntire(line)
        if (colonMatch != null) {
            val nick = cleanSpokenNick(colonMatch.groupValues[1])
            val message = colonMatch.groupValues[2].trim()
            val spoken = if (announceSpeakers) {
                val verb = if (!announced) "says" else "replies"
                announced = true
                if (message.isNotEmpty()) "$nick $verb: $message" else "$nick $verb"
            } else {
                if (message.isNotEmpty()) message else nick
            }
            turns += SpokenTurn(nick, spoken)
            continue
        }
        var matchedPlainSpeaker = false
        for (pattern in listOf(
            gtSpeakerLinePattern,
            curlySpeakerLinePattern,
            dashSpeakerLinePattern,
            pipeSpeakerLinePattern
        )) {
            val pm = pattern.matchEntire(line)
            if (pm != null) {
                val nick = cleanSpokenNick(pm.groupValues[1])
                val message = pm.groupValues[2].trim()
                val spoken = if (announceSpeakers) {
                    val verb = if (!announced) "says" else "replies"
                    announced = true
                    if (message.isNotEmpty()) "$nick $verb: $message" else "$nick $verb"
                } else {
                    if (message.isNotEmpty()) message else nick
                }
                turns += SpokenTurn(nick, spoken)
                matchedPlainSpeaker = true
                break
            }
        }
        if (matchedPlainSpeaker) continue
        if (noticeLinePattern.matches(line)) {
            // "--- dave joined the chat room." / "---- sex has quit irc
            // ----" / "join | nick | (...)": one narrator turn; strip the
            // dashes and read pipes as pauses.
            var spoken = line.trim().trim { it == '-' }.trim()
            spoken = spoken.replace("|", ",")
            spoken = whitespacePattern.replace(spoken, " ").trim()
            if (spoken.isNotEmpty()) {
                turns += SpokenTurn(null, spoken, isAction = true)
            }
            continue
        }
        val bareMatch = bareActionLinePattern.matchEntire(line)
        if (bareMatch != null &&
            cleanSpokenNick(bareMatch.groupValues[1]).lowercase() in speakers
        ) {
            // Third-person action ("Rhoslin fistpumps."): read as-is in
            // the narrator voice, matching its action-line coloring.
            turns += SpokenTurn(null, line, isAction = true)
            continue
        }
        turns += SpokenTurn(null, line)
    }
    return turns
}

/**
 * One read-along display turn: a display-text line with its speaker nick
 * (null for narration), whether it is an action or notice line (read in the
 * narrator voice), the line's char offset inside the display text, and the
 * raw line text (spoken as-is so word offsets line up with highlighting).
 */
data class DisplayTurn(
    val nick: String?,
    val isAction: Boolean,
    val lineStart: Int,
    val text: String
)

/**
 * Classifies one trimmed display line exactly like [buildSpokenTurns]
 * does: returns (speaker nick or null, isActionLine). Shared so read-along
 * per-turn voices match what Read aloud uses.
 */
internal fun classifyDisplayLine(
    line: String,
    speakers: Set<String>
): Pair<String?, Boolean> {
    nickLinePattern.matchEntire(line)?.let {
        return cleanSpokenNick(it.groupValues[1]) to false
    }
    if (actionLinePattern.matches(line)) return null to true
    bracketSpeakerLinePattern.matchEntire(line)?.let {
        return cleanSpokenNick(it.groupValues[1]) to false
    }
    colonSpeakerLinePattern.matchEntire(line)?.let {
        return cleanSpokenNick(it.groupValues[1]) to false
    }
    for (pattern in listOf(
        gtSpeakerLinePattern,
        curlySpeakerLinePattern,
        dashSpeakerLinePattern,
        pipeSpeakerLinePattern
    )) {
        pattern.matchEntire(line)?.let {
            return cleanSpokenNick(it.groupValues[1]) to false
        }
    }
    if (noticeLinePattern.matches(line)) return null to true
    val bareMatch = bareActionLinePattern.matchEntire(line)
    if (bareMatch != null &&
        cleanSpokenNick(bareMatch.groupValues[1]).lowercase() in speakers
    ) return null to true
    return null to false
}

/**
 * Splits already-display-normalized [displayText] into per-line turns for
 * read-along's per-speaker voices. Empty lines are skipped (nothing to
 * speak); offsets are char offsets into [displayText] so word-range
 * callbacks map back to the highlight spans.
 */
fun displayTurns(displayText: String): List<DisplayTurn> {
    if (displayText.isEmpty()) return emptyList()
    val rawLines = displayText.split("\n")
    // First pass: speakers known in this quote (for bare third-person
    // action lines), mirroring buildSpokenTurns.
    val speakers = mutableSetOf<String>()
    for (raw in rawLines) {
        val line = raw.trim()
        if (line.isEmpty()) continue
        nickLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        bracketSpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        colonSpeakerLinePattern.matchEntire(line)?.let {
            speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
        }
        for (pattern in listOf(
            gtSpeakerLinePattern,
            curlySpeakerLinePattern,
            dashSpeakerLinePattern,
            pipeSpeakerLinePattern
        )) {
            pattern.matchEntire(line)?.let {
                speakers += cleanSpokenNick(it.groupValues[1]).lowercase()
            }
        }
    }
    val turns = mutableListOf<DisplayTurn>()
    var start = 0
    for (raw in rawLines) {
        val lineStart = start
        start += raw.length + 1 // +1 for the split-off "\n"
        val line = raw.trim()
        if (line.isEmpty()) continue
        val (nick, isAction) = classifyDisplayLine(line, speakers)
        turns += DisplayTurn(nick, isAction, lineStart, raw)
    }
    return turns
}
