package com.quotebrowser.app.data

/**
 * Content maturity ladder, stored per quote in the `maturity` column
 * (0-4) of every quote database. Rated by an offline heuristic
 * classifier (see workspace/maturity/classify.py); FML's own /spicy
 * curation overrides the heuristic for FML stories.
 *
 * Named filters map to ranges on the ladder:
 * - Kid Safe: level 0 only
 * - Clean Only: levels 0-1
 * - PG-13: levels 0-2
 * - Safe for Work: levels 0-2, plus the Spicy flag screened out
 * - Rated R: levels 0-3
 * - NC-17: levels 0-4 (everything)
 * - NSFW only: levels 3-4
 * - Spicy: the explicit `spicy` flag (FML-/spicy-style sexual content)
 */
enum class MaturityLevel(
    val label: String,
    val description: String,
    val maxLevel: Int?,
    val minLevel: Int?,
    val useSpicyFlag: Boolean = false,
    /**
     * v1.65: when true, the spicy flag is screened out as well. This is
     * what distinguishes Safe for Work from PG-13: both cap at level 2,
     * but Safe for Work additionally excludes Spicy-flagged quotes.
     */
    val excludeSpicy: Boolean = false
) {
    EVERYTHING(
        "Everything",
        "No maturity filtering; every quote is eligible.",
        null, null
    ),
    KID_SAFE(
        "Kid Safe",
        "Only quotes rated Kid Safe: no profanity, no adult themes.",
        0, null
    ),
    CLEAN_ONLY(
        "Clean Only",
        "Kid Safe plus Clean quotes: essentially profanity-free.",
        1, null
    ),
    PG_13(
        "PG-13",
        "Up to PG-13: mild language and innuendo are OK; nothing explicit.",
        2, null
    ),
    SAFE_FOR_WORK(
        "Safe for Work",
        "The PG-13 range, but anything flagged Spicy is screened out.",
        2, null, excludeSpicy = true
    ),
    RATED_R(
        "Rated R",
        "Up to Rated R: strong language and adult themes are OK.",
        3, null
    ),
    NC_17(
        "NC-17",
        "Up to NC-17: everything short of the Spicy-only view.",
        4, null
    ),
    NSFW_ONLY(
        "NSFW only",
        "Only Rated R and NC-17 quotes.",
        null, 3
    ),
    SPICY(
        "Spicy",
        "Only quotes flagged Spicy (FML-style sexual content).",
        null, null, useSpicyFlag = true
    );

    /** SQL predicate over the quotes table (interpolation is safe: fixed enum values). */
    fun sqlWhere(): String {
        val base = when {
            useSpicyFlag -> "spicy = 1"
            maxLevel != null && minLevel != null -> "maturity BETWEEN $minLevel AND $maxLevel"
            maxLevel != null -> "maturity <= $maxLevel"
            minLevel != null -> "maturity >= $minLevel"
            else -> "1 = 1"
        }
        return if (excludeSpicy) "($base AND spicy = 0)" else base
    }

    companion object {
        fun fromStored(name: String): MaturityLevel =
            values().firstOrNull { it.name == name } ?: EVERYTHING
    }

    /** Kotlin-side test mirroring [sqlWhere] (for post-filtering). */
    fun matches(maturity: Int, spicy: Int): Boolean {
        val base = when {
            useSpicyFlag -> spicy == 1
            maxLevel != null && minLevel != null -> maturity in minLevel..maxLevel
            maxLevel != null -> maturity <= maxLevel
            minLevel != null -> maturity >= minLevel
            else -> true
        }
        return base && (!excludeSpicy || spicy == 0)
    }
}

/**
 * Cast size: distinct speaker nicks per quote, stored in the `speakers`
 * column. Counted with the same speaker patterns the nick-coloring and
 * voice-assignment pipeline uses (see ui/NickColors.kt speakerNamesIn):
 * <nick> mentions, Nick: labels, [nick], nick>, {nick}, -nick-, (nick).
 * Quotes with no detectable speakers (prose) carry 0.
 */
enum class CastSize(val label: String) {
    SOLO("Single person"),
    MULTIPLE("Multiple people"),
    DUO("Exactly 2"),
    TRIO("Exactly 3"),
    QUARTET("Exactly 4"),
    FIVE("Exactly 5");

    /** SQL predicate over the quotes table (interpolation is safe: fixed enum values). */
    fun sqlWhere(): String = when (this) {
        SOLO -> "speakers = 1"
        MULTIPLE -> "speakers >= 2"
        DUO -> "speakers = 2"
        TRIO -> "speakers = 3"
        QUARTET -> "speakers = 4"
        FIVE -> "speakers = 5"
    }

    /** Kotlin-side bucket test (for post-filtering merged results). */
    fun matches(speakers: Int): Boolean = when (this) {
        SOLO -> speakers == 1
        MULTIPLE -> speakers >= 2
        DUO -> speakers == 2
        TRIO -> speakers == 3
        QUARTET -> speakers == 4
        FIVE -> speakers == 5
    }

    companion object {
        fun fromStored(name: String?): CastSize? =
            values().firstOrNull { it.name == name }
    }
}
