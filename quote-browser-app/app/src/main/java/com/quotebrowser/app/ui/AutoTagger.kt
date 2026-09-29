package com.quotebrowser.app.ui

/**
 * Heuristic keyword auto-tagger for My Quotes additions and imports.
 *
 * This is the runtime Kotlin port of the archive's build-time tagger
 * (tools/tag_crapshack.py): the same category list and keyword patterns,
 * applied to new quote text so My Quotes gets the same category coverage
 * as the bundled archives without any manual tagging.
 */
object AutoTagger {

    /** Category names in tag order; "Short quotes"/"Long quotes" are computed. */
    private val CATEGORY_ORDER = listOf(
        "Computer",
        "Programming",
        "Linux",
        "Windows",
        "Apple",
        "Internet",
        "Gaming",
        "Phone & chat",
        "Sexual",
        "Dark humor",
        "Drugs & alcohol",
        "Work",
        "School",
        "Relationships",
        "Religion",
        "Animals",
        "Movies & TV",
        "Music",
        "Sports",
        "Food",
        "Funny"
    )

    private val KEYWORDS: Map<String, List<Regex>> = mapOf(
        "Computer" to listOf(
            "computer", "\\bpc\\b", "cpu", "\\bram\\b", "hdd", "hard ?drive",
            "motherboard", "keyboard", "monitor", "printer", "laptop",
            "hardware", "software", "reboot", "crash(?:ed|ing)?", "format c",
            "megahertz", "gigahertz", "floppy", "cd-?rom", "usb", "server",
            "\\bvirus\\b", "firewall", "bandwidth"
        ),
        "Programming" to listOf(
            "\\bcode\\b", "coding", "programmer", "\\bbug\\b", "\\bbugs\\b",
            "compil", "script", "\\bperl\\b", "\\bpython\\b", "\\bjava\\b", "\\bhtml\\b",
            "\\bphp\\b", "\\bsql\\b", "function", "variable", "syntax", "hacker",
            "crack", "debug", "algorithm", "regex", "source ?code"
        ),
        "Linux" to listOf(
            "linux", "ubuntu", "debian", "red ?hat", "kernel", "penguin",
            "distro", "gnome", "\\bkde\\b", "slackware", "mandrake", "suse",
            "\\btux\\b"
        ),
        "Windows" to listOf(
            "windows", "microsoft", "bill gates", "win ?9[58]", "win ?xp",
            "win ?2k", "blue ?screen", "bsod", "internet explorer"
        ),
        "Apple" to listOf(
            "apple", "macintosh", "\\bmac\\b", "imac", "ipod", "steve jobs",
            "mac ?os"
        ),
        "Internet" to listOf(
            "internet", "web ?site", "www\\.", "http", "e-?mail", "\\birc\\b",
            "chat ?room", "dial-?up", "modem", "download", "upload",
            "web ?page", "google", "napster", "\\baol\\b", "online",
            "cyberspace", "dot ?com"
        ),
        "Gaming" to listOf(
            "\\bgame\\b", "\\bgames\\b", "gaming", "gamer", "nintendo",
            "playstation", "\\bps2\\b", "xbox", "sega", "\\bdoom\\b", "quake",
            "counter-?strike", "mario", "zelda", "pokemon", "diablo",
            "warcraft", "starcraft", "tetris", "atari", "console", "joystick",
            "frag", "\\brpg\\b", "\\bfps\\b", "lan party", "gameboy", "halo"
        ),
        "Phone & chat" to listOf(
            "phone", "cell ?phone", "mobile", "call me", "text me",
            "\\bsms\\b", "paged", "pager", "/msg", "pm me", "\\baim\\b",
            "\\bicq\\b", "\\bmsn\\b", "yahoo messenger", "\\bbrb\\b", "\\bafk\\b",
            "away message", "voicemail", "telephone", "nokia"
        ),
        "Sexual" to listOf(
            "\\bsex\\b", "sexual", "porn", "masturbat", "orgasm",
            "blow ?job", "hand ?job", "\\btits?\\b", "boobs?", "pussy",
            "\\bdick\\b", "\\bcock\\b", "penis", "vagina", "\\bvag\\b", "horny",
            "\\bnudes?\\b", "\\bnaked\\b", "condom", "viagra", "clit", "nipple",
            "boner", "hentai", "get laid", "cyber ?sex"
        ),
        "Dark humor" to listOf(
            "\\bdead\\b", "death", "\\bdie\\b", "\\bdies\\b", "\\bdied\\b", "dying",
            "\\bkill\\b", "killed", "killer", "murder", "suicid", "\\brape\\b",
            "rapist", "corpse", "coffin", "funeral", "\\bgrave\\b", "torture",
            "\\bbomb\\b", "terrorist", "9/11", "bin laden", "autopsy",
            "decapitat", "hang(?:ed|ing|s)? him", "electrocute"
        ),
        "Drugs & alcohol" to listOf(
            "\\bbeer\\b", "\\bdrunk\\b", "\\bweed\\b", "\\bpot\\b", "\\bjoint\\b",
            "stoned", "getting high", "got high", "was high", "\\bcoke\\b",
            "cocaine", "heroin", "\\bdrugs\\b", "alcohol", "whiskey", "vodka",
            "tequila", "\\brum\\b", "\\bwine\\b", "bong", "blunt", "\\blsd\\b",
            "ecstasy", "wasted", "hung ?over", "liquor", "\\bbrew\\b",
            "shots?\\b"
        ),
        "Work" to listOf(
            "\\bwork\\b", "working", "\\bjob\\b", "\\bboss\\b", "office", "fired",
            "paycheck", "co-?worker", "meeting", "\\bshift\\b", "overtime",
            "unemployed", "interview", "career"
        ),
        "School" to listOf(
            "school", "teacher", "homework", "\\bclass\\b", "college",
            "university", "\\bexam\\b", "principal", "student", "detention",
            "\\bgrade\\b", "diploma", "professor", "\\bprom\\b", "recess",
            "semester"
        ),
        "Relationships" to listOf(
            "\\blove\\b", "girlfriend", "boyfriend", "\\bwife\\b", "husband",
            "\\bmarry\\b", "married", "marriage", "divorce", "dating",
            "\\bcrush\\b", "break ?up", "fianc", "anniversary", "valentine",
            "cheating", "dumped"
        ),
        "Religion" to listOf(
            "\\bgod\\b", "jesus", "christ", "bible", "church", "\\bpray\\b",
            "heaven", "muslim", "christian", "jewish", "\\bjew\\b", "priest",
            "\\bnun\\b", "allah", "buddha", "religion", "\\bamen\\b",
            "\\bsins?\\b", "gospel", "\\bholy\\b", "atheist"
        ),
        "Animals" to listOf(
            "\\bdog\\b", "\\bcat\\b", "\\bbird\\b", "\\bfish\\b", "horse", "\\bcow\\b",
            "\\bpig\\b", "whale", "shark", "spider", "snake", "monkey",
            "bunny", "rabbit", "hamster", "\\bpet\\b", "\\bzoo\\b", "\\brat\\b",
            "frog", "\\bduck\\b", "chicken", "turkey", "\\bbear\\b", "\\bwolf\\b",
            "squirrel", "kitten", "puppy"
        ),
        "Movies & TV" to listOf(
            "movie", "\\bfilm\\b", "\\btv\\b", "television", "episode",
            "star ?wars", "star ?trek", "simpsons", "south ?park", "\\bmatrix\\b",
            "lord of the rings", "disney", "hollywood", "actor", "actress",
            "\\bdvd\\b", "\\bvhs\\b", "cinema", "sitcom", "cartoon", "anime"
        ),
        "Music" to listOf(
            "music", "\\bsong\\b", "\\bband\\b", "metallica", "blink ?182",
            "iron ?maiden", "guitar", "drums", "\\bbass\\b", "concert",
            "album", "\\bmp3\\b", "\\bsing\\b", "singing", "lyrics", "\\brock\\b",
            "punk", "\\brap\\b", "hip ?hop", "\\bdj\\b", "karaoke", "sepultura",
            "slayer", "megadeth", "pantera"
        ),
        "Sports" to listOf(
            "\\bsport\\b", "football", "baseball", "basketball", "soccer",
            "hockey", "tennis", "golf", "nascar", "olympics", "touchdown",
            "home ?run", "\\bcoach\\b", "referee", "\\bespn\\b", "wrestling",
            "\\bwwe\\b"
        ),
        "Food" to listOf(
            "\\bfood\\b", "\\beat\\b", "eating", "pizza", "burger", "mcdonald",
            "taco", "sandwich", "breakfast", "lunch", "dinner", "cookie",
            "\\bcake\\b", "ice ?cream", "chocolate", "coffee", "hungry",
            "restaurant", "\\bcook\\b", "cooking", "recipe", "bacon", "cheese"
        ),
        "Funny" to listOf(
            "\\blol\\b", "lmao", "\\brofl\\b", "haha", "hehe", ":\\)", ":-\\)",
            ":d", "=\\)", "\\bxd\\b", "\\bfunny\\b", "hilarious", "\\bjoke\\b",
            "lmfao", "rotfl"
        )
    ).mapValues { (_, pats) -> pats.map { Regex(it, RegexOption.IGNORE_CASE) } }

    /**
     * Category names for [text]: keyword matches in category order, plus
     * "Short quotes" (< 120 chars) and "Long quotes" (> 400 chars). When
     * nothing matches, falls back to "Phone & chat" like the archive
     * tagger (IRC chat logs), guaranteeing at least one tag.
     */
    fun autoTagCategories(text: String): List<String> {
        val tags = CATEGORY_ORDER.filter { cat ->
            KEYWORDS[cat]?.any { rx -> rx.containsMatchIn(text) } == true
        }.toMutableList()
        if (text.length < 120) tags.add("Short quotes")
        if (text.length > 400) tags.add("Long quotes")
        if (tags.isEmpty()) tags.add("Phone & chat")
        return tags
    }
}
