package com.quotebrowser.app.ui.screens

/**
 * Curated starter sets for the content filter's blocked-words list.
 * The user adds a whole set with one tap and can delete words (or sets)
 * individually afterwards. Everything is lowercase; matching is
 * whole-word and case-insensitive.
 */
data class SuggestedWordSet(
    val id: String,
    val title: String,
    val blurb: String,
    val words: List<String>
)

val SUGGESTED_WORD_SETS: List<SuggestedWordSet> = listOf(
    SuggestedWordSet(
        id = "profanity",
        title = "Profanity",
        blurb = "Common swear words and intensifiers.",
        words = listOf(
            "fuck", "fucking", "fucked", "fucker", "fuckers",
            "motherfucker", "motherfuckers",
            "shit", "shite",
            "bitch", "bitches",
            "bastard", "bastards",
            "asshole", "assholes", "arsehole",
            "dick", "dickhead", "dickheads",
            "cock", "cocksucker",
            "cunt", "twat",
            "wanker", "wankers", "bollocks",
            "piss", "pissed",
            "whore", "whores",
            "slut", "sluts"
        )
    ),
    SuggestedWordSet(
        id = "sexual",
        title = "Sexual content",
        blurb = "Explicit sexual terms.",
        words = listOf(
            "porn", "porno", "pornography", "xxx",
            "masturbate", "masturbating", "masturbation",
            "orgasm", "orgasms",
            "blowjob", "blowjobs", "handjob",
            "anal", "horny",
            "erotic", "erotica",
            "dildo", "dildos", "vibrator",
            "escort", "escorts",
            "prostitute", "prostitutes", "prostitution",
            "brothel",
            "stripper", "strippers",
            "orgy", "fetish", "bdsm",
            "cum"
        )
    ),
    SuggestedWordSet(
        id = "religious",
        title = "Religious exclamations",
        blurb = "Profanity with religious overtones.",
        words = listOf(
            "goddamn", "goddammit", "goddam",
            "hell", "damn"
        )
    ),
    SuggestedWordSet(
        id = "racial-slurs",
        title = "Racial slurs",
        blurb = "Ethnic and racial slurs.",
        words = listOf(
            "nigger", "niggers", "nigga", "niggas",
            "chink", "chinks",
            "spic", "spics",
            "kike", "kikes",
            "wetback", "wetbacks",
            "raghead", "ragheads",
            "gook", "gooks",
            "coon", "coons",
            "beaner", "beaners",
            "honky", "honkies",
            "cracker", "crackers",
            "towelhead", "towelheads"
        )
    ),
    SuggestedWordSet(
        id = "other-slurs",
        title = "Other slurs",
        blurb = "Homophobic, transphobic, and ableist slurs.",
        words = listOf(
            "faggot", "faggots", "fag", "fags",
            "dyke", "dykes",
            "tranny", "trannies",
            "shemale", "shemales",
            "retard", "retarded", "retards",
            "hebe"
        )
    )
)
