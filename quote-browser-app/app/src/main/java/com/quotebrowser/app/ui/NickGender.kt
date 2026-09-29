package com.quotebrowser.app.ui

/**
 * v1.63: best-effort nick gender guess, used by automatic speaker-voice
 * assignment so a nick that reads female gets a feminine voice and a nick
 * that reads male gets a masculine voice. Pure heuristic — anything
 * ambiguous returns [UNKNOWN] and the caller falls back to the existing
 * deterministic assignment. Manual per-nick voice overrides always win.
 */
enum class NickGender { MALE, FEMALE, UNKNOWN }

private val femaleMarkers = listOf(
    "girl", "gal", "lady", "ladies", "queen", "princess", "miss", "mrs",
    "chick", "woman", "women", "female", "babe", "sista", "sister",
    "goddess", "diva", "madam", "dame", "lass", "grrl", "gurl", "xx_",
    "_xx", "xoxo", "mami", "mama", "mommy", "wife", "wifey", "bride"
)

private val maleMarkers = listOf(
    "boy", "boys", "guy", "dude", "king", "prince", "mister", "sir",
    "male", "bro", "bros", "fella", "fellas", "bloke", "chap", "god",
    "daddy", "papa", "husband", "groom", "stud", "homie", "bruh"
)

/**
 * Common first names, checked against the nick's first token after
 * decorations and trailing digits are stripped. Deliberately
 * conservative: only names that are overwhelmingly one gender in
 * English-speaking IRC/quote culture.
 */
private val femaleNames = setOf(
    "sarah", "jessica", "ashley", "emily", "samantha", "jennifer", "amanda",
    "melissa", "stephanie", "nicole", "elizabeth", "lauren", "megan",
    "hannah", "rachel", "kayla", "amber", "brittany", "danielle", "natalie",
    "katherine", "kate", "katie", "victoria", "olivia", "emma", "sophia",
    "ava", "mia", "chloe", "lily", "zoe", "ruby", "ella", "grace", "anna",
    "mary", "lisa", "michelle", "kimberly", "amy", "angela", "helen",
    "laura", "kelly", "christina", "tiffany", "diana", "julia", "maria",
    "susan", "karen", "nancy", "betty", "dorothy", "sandra", "donna",
    "carol", "ruth", "sharon", "cynthia", "deborah", "jess", "jenny",
    "kristen", "allison", "courtney", "erin", "morgan", "taylor", "alexis",
    "brianna", "kelsey", "sydney", "alyssa", "haley", "madison", "paige",
    "rebecca", "vanessa", "maggie", "lucy", "daisy", "rosie", "poppy",
    "willow", "ivy", "hazel", "violet", "scarlett", "stella", "luna",
    "aurora", "maya", "nina", "tara", "lena", "mia", "ella", "aria",
    "eva", "isabella", "charlotte", "amelia", "harper", "evelyn",
    "abigail", "emilia", "elena", "clara", "celine", "gwen", "fiona",
    "ginger", "holly", "iris", "jade", "june", "kitty", "lola", "molly",
    "penny", "piper", "sadie", "sasha", "tessa", "wendy", "yuki", "sakura"
)

private val maleNames = setOf(
    "james", "john", "robert", "michael", "william", "david", "joseph",
    "thomas", "charles", "daniel", "matthew", "anthony", "mark", "donald",
    "steven", "paul", "andrew", "joshua", "kevin", "brian", "george",
    "edward", "ronald", "jason", "jeffrey", "ryan", "jacob", "nicholas",
    "gary", "eric", "jonathan", "stephen", "larry", "scott", "justin",
    "brandon", "frank", "gregory", "raymond", "alexander", "patrick",
    "jack", "dennis", "jerry", "tyler", "aaron", "nathan", "henry",
    "adam", "douglas", "peter", "kyle", "ethan", "walter", "noah",
    "jeremy", "carl", "keith", "roger", "gerald", "harold", "sean",
    "austin", "carl", "arthur", "lawrence", "dylan", "bryan", "gabriel",
    "owen", "caleb", "liam", "lucas", "mason", "logan", "oliver",
    "elijah", "connor", "cody", "dustin", "luke", "max", "leo", "finn",
    "miles", "nolan", "oscar", "parker", "quinn", "riley", "ryder",
    "silas", "theo", "victor", "wyatt", "xander", "zane", "marcus",
    "felix", "hugo", "ivan", "jude", "kai", "leon", "milo", "nico",
    "otis", "rex", "sam", "tom", "ben", "joe", "jim", "bob", "bill",
    "fred", "steve", "dave", "mike", "chris", "alex", "tony", "nick"
)

/**
 * Strips common nick decorations (xX..Xx, leading/trailing _-|[]{} etc.,
 * trailing digits) and returns the core token most likely to be a name.
 */
private fun coreNickToken(nick: String): String {
    var s = nick.lowercase().trim()
    // "xXSarahXx" / "xx_sarah_xx" wrappers.
    s = s.replace(Regex("^x+[_\\- ]?"), "").replace(Regex("[_\\- ]?x+$"), "")
    // Leading mode chars and bracket decorations.
    s = s.trim { it in "_-|[]{}()<>=~^`" || it.isWhitespace() }
    // Trailing digits/underscores ("sarah92", "mike___").
    s = s.replace(Regex("[0-9_]+$"), "")
    s = s.trim { it in "_-|[]{}()<>=~^`" || it.isWhitespace() }
    // First token only ("sarah connor" -> "sarah").
    s = s.split(Regex("[ _\\-./|\\\\]+")).firstOrNull().orEmpty()
    // Strip a leading @/~ (irc status prefixes that survived cleaning).
    s = s.trimStart('@', '~', '+', '%', '&')
    return s
}

/** Best-effort gender guess for a speaker nick. */
fun guessNickGender(rawNick: String?): NickGender {
    if (rawNick.isNullOrBlank()) return NickGender.UNKNOWN
    val lowered = rawNick.lowercase()
    // Explicit gender markers win over name lists. Female markers are
    // checked first because several contain male substrings ("woman"
    // contains "man").
    if (femaleMarkers.any { it in lowered }) return NickGender.FEMALE
    if (maleMarkers.any { it in lowered }) return NickGender.MALE
    val token = coreNickToken(rawNick)
    if (token.length >= 2) {
        if (token in femaleNames) return NickGender.FEMALE
        if (token in maleNames) return NickGender.MALE
    }
    return NickGender.UNKNOWN
}

/**
 * Best-effort gender guess for an installed (built-in) TTS voice from its
 * name, e.g. "Google UK English Female" or "Samsung US English Male".
 * UNKNOWN when the name carries no gender hint.
 */
fun guessVoiceGender(voiceName: String?): NickGender {
    if (voiceName.isNullOrBlank()) return NickGender.UNKNOWN
    val n = voiceName.lowercase()
    val femaleHints = listOf(
        "female", "woman", "women", "girl", "feminine", "zira", "samantha",
        "aria", "jenny", "sonia", "emma", "olivia", "ava", "mia"
    )
    val maleHints = listOf(
        "male", "man ", "man-", "man_", "boy", "masculine", "david",
        "daniel", "guy", "ryan", "george", "james", "mark"
    )
    // Whole-word-ish matching: "woman" must not match inside "womanly"... keep
    // it simple — check female hints first like the nick markers.
    if (femaleHints.any { it in n }) return NickGender.FEMALE
    if (maleHints.any { it in n }) return NickGender.MALE
    return NickGender.UNKNOWN
}
