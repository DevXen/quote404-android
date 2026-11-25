package com.quotebrowser.app.ui

import androidx.compose.runtime.compositionLocalOf

/**
 * v1.89.10 Clean mode (Clint, green-lit 2026-09-19 as the eighth feature):
 * a single global display-only toggle (default OFF) that swaps profanity
 * for milder substitute words at render time — fuck → fudge/frick/flip,
 * shit → shoot/shucks/crud, and so on. Stored text is never altered; the
 * swap happens in the display pipeline only, like the Kid-safe mask.
 *
 * Clean mode vs Kid-safe: the mask keeps the word's shape (f***); Clean
 * mode replaces the word outright with a silly-but-clean substitute. The
 * substitute for a given word is picked deterministically from the quote
 * text itself, so the same quote always renders the same way (no flicker
 * between recompositions) while different quotes get different picks.
 * If both toggles are on, substitution runs first and the mask then
 * catches anything the substitution list doesn't cover.
 */
val LocalCleanModeEnabled = compositionLocalOf { false }

/** Profanity → milder substitutes. Keys are lowercase; matching is
 *  word-boundary, case-insensitive, and the substitute adopts the matched
 *  word's casing (lower / Capitalized / ALL CAPS). */
private val CLEAN_SUBSTITUTES: Map<String, List<String>> = mapOf(
    "fuck" to listOf("fudge", "frick", "flip"),
    "fucking" to listOf("fudging", "fricking", "flipping"),
    "fuckin" to listOf("frickin", "flippin"),
    "fucked" to listOf("fudged", "fricked", "flipped"),
    "fucker" to listOf("fudger", "fricker", "goofball"),
    "fuckers" to listOf("fudgers", "frickers", "goofballs"),
    "fucks" to listOf("fudges", "fricks"),
    "motherfucker" to listOf("motherlover", "mother fudger"),
    "motherfuckers" to listOf("motherlovers", "mother fudgers"),
    "shit" to listOf("shoot", "shucks", "crud"),
    "shits" to listOf("shoots", "cruds"),
    "shitty" to listOf("crummy", "lousy"),
    "bullshit" to listOf("baloney", "hogwash", "nonsense"),
    "bitch" to listOf("witch", "brat"),
    "bitches" to listOf("witches", "brats"),
    "bitchy" to listOf("cranky", "witchy"),
    "asshole" to listOf("jerk", "goofball"),
    "assholes" to listOf("jerks", "goofballs"),
    "ass" to listOf("butt"),
    "arse" to listOf("butt"),
    "jackass" to listOf("goofball"),
    "dumbass" to listOf("goofball"),
    "dickhead" to listOf("bonehead", "knucklehead"),
    "dickheads" to listOf("boneheads", "knuckleheads"),
    "dick" to listOf("dingus", "weenie"),
    "dicks" to listOf("dinguses", "weenies"),
    "cock" to listOf("rooster"),
    "cocks" to listOf("roosters"),
    "cunt" to listOf("meanie"),
    "cunts" to listOf("meanies"),
    "whore" to listOf("floozy"),
    "whores" to listOf("floozies"),
    "slut" to listOf("floozy"),
    "sluts" to listOf("floozies"),
    "bastard" to listOf("rascal", "scoundrel"),
    "bastards" to listOf("rascals", "scoundrels"),
    "damn" to listOf("darn", "dang"),
    "dammit" to listOf("dagnabbit", "gosh darn it"),
    "goddamn" to listOf("gosh darn", "doggone"),
    "goddammit" to listOf("dagnabbit", "gosh darn it"),
    "hell" to listOf("heck"),
    "piss" to listOf("pee"),
    "pissed" to listOf("ticked", "annoyed"),
    "pissing" to listOf("peeing"),
    "pussy" to listOf("wimp", "scaredy-cat"),
    "tits" to listOf("bosom"),
    "titties" to listOf("bosom"),
    "boobs" to listOf("bosom"),
    "wank" to listOf("fiddle"),
    "wanker" to listOf("wally", "plonker"),
    "wankers" to listOf("wallies", "plonkers"),
    "wanking" to listOf("fiddling"),
    "bollocks" to listOf("baloney", "rubbish"),
    "bugger" to listOf("blighter", "rascal"),
    "buggered" to listOf("wrecked", "bushed"),
    "prick" to listOf("jerk"),
    "pricks" to listOf("jerks"),
    "twat" to listOf("twit"),
    "nigga" to listOf("dude", "buddy"),
    "nigger" to listOf("dude", "buddy"),
    "fag" to listOf("fool"),
    "faggot" to listOf("fool", "jerk"),
    "faggots" to listOf("fools", "jerks"),
    "retard" to listOf("goofball"),
    "retarded" to listOf("goofy", "silly"),
    "douche" to listOf("doofus"),
    "douchebag" to listOf("doofus", "goofball"),
    "crap" to listOf("crud", "crumbs"),
    "crappy" to listOf("crummy")
)

private val CleanModeRegex: Regex by lazy {
    // Longest keys first so e.g. "fucking" wins over "fuck" at the same spot.
    val alt = CLEAN_SUBSTITUTES.keys
        .sortedByDescending { it.length }
        .joinToString("|") { Regex.escape(it) }
    Regex("\\b($alt)\\b", RegexOption.IGNORE_CASE)
}

/** Adopts the matched word's casing for the substitute. */
private fun matchCase(original: String, substitute: String): String = when {
    original.length > 1 && original.all { !it.isLetter() || it.isUpperCase() } ->
        substitute.uppercase()
    original.firstOrNull()?.isUpperCase() == true ->
        substitute.replaceFirstChar { it.uppercase() }
    else -> substitute
}

/**
 * Swaps profanity in [text] for milder substitutes, display-only. The pick
 * per occurrence is deterministic in (text, word, occurrence index), so a
 * quote renders identically on every recomposition but picks vary between
 * quotes. Unlike the Kid-safe mask this can change text length — it is
 * applied before span annotation, exactly where the mask is applied.
 */
fun applyCleanMode(text: String): String {
    if (text.isEmpty()) return text
    val textSeed = text.hashCode()
    var occurrence = 0
    return CleanModeRegex.replace(text) { m ->
        val word = m.value
        val options = CLEAN_SUBSTITUTES[word.lowercase()] ?: return@replace word
        val pick = options[
            kotlin.math.abs(textSeed * 31 + occurrence * 131 + word.lowercase().hashCode()) % options.size
        ]
        occurrence++
        matchCase(word, pick)
    }
}
