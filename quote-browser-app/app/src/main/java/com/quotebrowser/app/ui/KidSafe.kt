package com.quotebrowser.app.ui

import androidx.compose.runtime.compositionLocalOf

/**
 * v1.53 Kid-safe mask: a single global display-only toggle (default OFF)
 * that masks profanity across every quote. Stored text is never altered —
 * masking happens in the display pipeline only, like the text transforms.
 *
 * Masking is 1:1 per character (first letter + asterisks, e.g. "f***") so
 * annotated span offsets stay aligned.
 */
val LocalKidSafeEnabled = compositionLocalOf { false }

/** Built-in profanity list. Word-boundary matched, case-insensitive. */
private val KID_SAFE_WORDS = listOf(
    "fuck", "fucking", "fucker", "fucked", "fucks",
    "shit", "shitty", "shits", "bullshit",
    "bitch", "bitches", "bitchy",
    "asshole", "assholes", "dickhead", "dickheads",
    "cunt", "cunts",
    "whore", "whores", "slut", "sluts",
    "bastard", "bastards",
    "damn", "goddamn", "goddammit",
    "piss", "pissed", "pissing",
    "cock", "cocks", "dick", "dicks", "pussy",
    "tits", "titties", "boobs",
    "wank", "wanker", "wanking",
    "bollocks", "bugger", "buggered",
    "prick", "pricks", "twat",
    "nigga", "nigger",
    "fag", "faggot", "faggots",
    "retard", "retarded",
    "crap", "crappy"
)

private val KidSafeRegex: Regex by lazy {
    val alt = KID_SAFE_WORDS.joinToString("|") { Regex.escape(it) }
    Regex("\\b($alt)\\b", RegexOption.IGNORE_CASE)
}

/**
 * Masks profanity display-only: keeps the first letter, replaces the rest
 * with '*'. Output length always equals input length so span offsets in
 * the annotated pipeline stay valid.
 */
fun applyKidSafeMask(text: String): String {
    if (text.isEmpty()) return text
    return KidSafeRegex.replace(text) { m ->
        val w = m.value
        if (w.length <= 1) "*"
        else w[0] + "*".repeat(w.length - 1)
    }
}
