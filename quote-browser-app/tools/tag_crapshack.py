#!/usr/bin/env python3
"""Heuristic category tagging for the #CrapXen CrapShack quote set.

Reads tools/crapshack_quotes.jsonl, applies keyword rules per category,
and writes tools/crapshack_tags.json in the same shape as the app's
category_seed.json: {"categories": [...], "tags": [[quote_number, cat_idx], ...]}.

The "Racist" and "Ironic" categories are deliberately skipped entirely.
Every quote is guaranteed at least one tag (fallback: "Phone & chat",
since the whole set is IRC chat logs).
"""

import json
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
JSONL = os.path.join(HERE, "crapshack_quotes.jsonl")
DST = os.path.join(HERE, "crapshack_tags.json")

CATEGORIES = [
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
    "Funny",
    "Short quotes",
    "Long quotes",
]

KEYWORDS = {
    "Computer": [
        r"computer", r"\bpc\b", r"cpu", r"\bram\b", r"hdd", r"hard ?drive",
        r"motherboard", r"keyboard", r"monitor", r"printer", r"laptop",
        r"hardware", r"software", r"reboot", r"crash(?:ed|ing)?", r"format c",
        r"megahertz", r"gigahertz", r"floppy", r"cd-?rom", r"usb", r"server",
        r"\bvirus\b", r"firewall", r"bandwidth",
    ],
    "Programming": [
        r"\bcode\b", r"coding", r"programmer", r"\bbug\b", r"\bbugs\b",
        r"compil", r"script", r"\bperl\b", r"\bpython\b", r"\bjava\b", r"\bhtml\b",
        r"\bphp\b", r"\bsql\b", r"function", r"variable", r"syntax", r"hacker",
        r"crack", r"debug", r"algorithm", r"regex", r"source ?code",
    ],
    "Linux": [
        r"linux", r"ubuntu", r"debian", r"red ?hat", r"kernel", r"penguin",
        r"distro", r"gnome", r"\bkde\b", r"slackware", r"mandrake", r"suse",
        r"\btux\b",
    ],
    "Windows": [
        r"windows", r"microsoft", r"bill gates", r"win ?9[58]", r"win ?xp",
        r"win ?2k", r"blue ?screen", r"bsod", r"internet explorer",
    ],
    "Apple": [
        r"apple", r"macintosh", r"\bmac\b", r"imac", r"ipod", r"steve jobs",
        r"mac ?os",
    ],
    "Internet": [
        r"internet", r"web ?site", r"www\.", r"http", r"e-?mail", r"\birc\b",
        r"chat ?room", r"dial-?up", r"modem", r"download", r"upload",
        r"web ?page", r"google", r"napster", r"\baol\b", r"online",
        r"cyberspace", r"dot ?com",
    ],
    "Gaming": [
        r"\bgame\b", r"\bgames\b", r"gaming", r"gamer", r"nintendo",
        r"playstation", r"\bps2\b", r"xbox", r"sega", r"\bdoom\b", r"quake",
        r"counter-?strike", r"mario", r"zelda", r"pokemon", r"diablo",
        r"warcraft", r"starcraft", r"tetris", r"atari", r"console", r"joystick",
        r"frag", r"\brpg\b", r"\bfps\b", r"lan party", r"gameboy", r"halo",
    ],
    "Phone & chat": [
        r"phone", r"cell ?phone", r"mobile", r"call me", r"text me",
        r"\bsms\b", r"paged", r"pager", r"/msg", r"pm me", r"\baim\b",
        r"\bicq\b", r"\bmsn\b", r"yahoo messenger", r"\bbrb\b", r"\bafk\b",
        r"away message", r"voicemail", r"telephone", r"nokia",
    ],
    "Sexual": [
        r"\bsex\b", r"sexual", r"porn", r"masturbat", r"orgasm",
        r"blow ?job", r"hand ?job", r"\btits?\b", r"boobs?", r"pussy",
        r"\bdick\b", r"\bcock\b", r"penis", r"vagina", r"\bvag\b", r"horny",
        r"\bnudes?\b", r"\bnaked\b", r"condom", r"viagra", r"clit", r"nipple",
        r"boner", r"hentai", r"get laid", r"cyber ?sex",
    ],
    "Dark humor": [
        r"\bdead\b", r"death", r"\bdie\b", r"\bdies\b", r"\bdied\b", r"dying",
        r"\bkill\b", r"killed", r"killer", r"murder", r"suicid", r"\brape\b",
        r"rapist", r"corpse", r"coffin", r"funeral", r"\bgrave\b", r"torture",
        r"\bbomb\b", r"terrorist", r"9/11", r"bin laden", r"autopsy",
        r"decapitat", r"hang(?:ed|ing|s)? him", r"electrocute",
    ],
    "Drugs & alcohol": [
        r"\bbeer\b", r"\bdrunk\b", r"\bweed\b", r"\bpot\b", r"\bjoint\b",
        r"stoned", r"getting high", r"got high", r"was high", r"\bcoke\b",
        r"cocaine", r"heroin", r"\bdrugs\b", r"alcohol", r"whiskey", r"vodka",
        r"tequila", r"\brum\b", r"\bwine\b", r"bong", r"blunt", r"\blsd\b",
        r"ecstasy", r"wasted", r"hung ?over", r"liquor", r"\bbrew\b",
        r"shots?\b",
    ],
    "Work": [
        r"\bwork\b", r"working", r"\bjob\b", r"\bboss\b", r"office", r"fired",
        r"paycheck", r"co-?worker", r"meeting", r"\bshift\b", r"overtime",
        r"unemployed", r"interview", r"career",
    ],
    "School": [
        r"school", r"teacher", r"homework", r"\bclass\b", r"college",
        r"university", r"\bexam\b", r"principal", r"student", r"detention",
        r"\bgrade\b", r"diploma", r"professor", r"\bprom\b", r"recess",
        r"semester",
    ],
    "Relationships": [
        r"\blove\b", r"girlfriend", r"boyfriend", r"\bwife\b", r"husband",
        r"\bmarry\b", r"married", r"marriage", r"divorce", r"dating",
        r"\bcrush\b", r"break ?up", r"fianc", r"anniversary", r"valentine",
        r"cheating", r"dumped",
    ],
    "Religion": [
        r"\bgod\b", r"jesus", r"christ", r"bible", r"church", r"\bpray\b",
        r"heaven", r"muslim", r"christian", r"jewish", r"\bjew\b", r"priest",
        r"\bnun\b", r"allah", r"buddha", r"religion", r"\bamen\b",
        r"\bsins?\b", r"gospel", r"\bholy\b", r"atheist",
    ],
    "Animals": [
        r"\bdog\b", r"\bcat\b", r"\bbird\b", r"\bfish\b", r"horse", r"\bcow\b",
        r"\bpig\b", r"whale", r"shark", r"spider", r"snake", r"monkey",
        r"bunny", r"rabbit", r"hamster", r"\bpet\b", r"\bzoo\b", r"\brat\b",
        r"frog", r"\bduck\b", r"chicken", r"turkey", r"\bbear\b", r"\bwolf\b",
        r"squirrel", r"kitten", r"puppy",
    ],
    "Movies & TV": [
        r"movie", r"\bfilm\b", r"\btv\b", r"television", r"episode",
        r"star ?wars", r"star ?trek", r"simpsons", r"south ?park", r"\bmatrix\b",
        r"lord of the rings", r"disney", r"hollywood", r"actor", r"actress",
        r"\bdvd\b", r"\bvhs\b", r"cinema", r"sitcom", r"cartoon", r"anime",
    ],
    "Music": [
        r"music", r"\bsong\b", r"\bband\b", r"metallica", r"blink ?182",
        r"iron ?maiden", r"guitar", r"drums", r"\bbass\b", r"concert",
        r"album", r"\bmp3\b", r"\bsing\b", r"singing", r"lyrics", r"\brock\b",
        r"punk", r"\brap\b", r"hip ?hop", r"\bdj\b", r"karaoke", r"sepultura",
        r"slayer", r"megadeth", r"pantera",
    ],
    "Sports": [
        r"\bsport\b", r"football", r"baseball", r"basketball", r"soccer",
        r"hockey", r"tennis", r"golf", r"nascar", r"olympics", r"touchdown",
        r"home ?run", r"\bcoach\b", r"referee", r"\bespn\b", r"wrestling",
        r"\bwwe\b",
    ],
    "Food": [
        r"\bfood\b", r"\beat\b", r"eating", r"pizza", r"burger", r"mcdonald",
        r"taco", r"sandwich", r"breakfast", r"lunch", r"dinner", r"cookie",
        r"\bcake\b", r"ice ?cream", r"chocolate", r"coffee", r"hungry",
        r"restaurant", r"\bcook\b", r"cooking", r"recipe", r"bacon", r"cheese",
    ],
    "Funny": [
        r"\blol\b", r"lmao", r"\brofl\b", r"haha", r"hehe", r":\)", r":-\)",
        r":d", r"=\)", r"\bxd\b", r"\bfunny\b", r"hilarious", r"\bjoke\b",
        r"lmfao", r"rotfl",
    ],
}

COMPILED = {
    cat: [re.compile(p, re.IGNORECASE) for p in pats]
    for cat, pats in KEYWORDS.items()
}


def tag(text: str) -> list:
    tags = []
    for cat in CATEGORIES:
        if cat in ("Short quotes", "Long quotes"):
            continue
        if any(rx.search(text) for rx in COMPILED[cat]):
            tags.append(cat)
    if len(text) < 120:
        tags.append("Short quotes")
    if len(text) > 400:
        tags.append("Long quotes")
    if not tags:
        tags.append("Phone & chat")  # fallback: the whole set is IRC chat logs
    return tags


def main() -> None:
    quotes = []
    with open(JSONL, encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if line:
                quotes.append(json.loads(line))

    cat_index = {c: i for i, c in enumerate(CATEGORIES)}
    tags = []  # [quote_number, category_index]
    counts = {c: 0 for c in CATEGORIES}
    untagged_before_fallback = 0
    for q in quotes:
        qtags = tag(q["text"])
        if qtags == ["Phone & chat"] and not any(
            rx.search(q["text"]) for rx in COMPILED["Phone & chat"]
        ):
            untagged_before_fallback += 1
        for c in qtags:
            tags.append([q["number"], cat_index[c]])
            counts[c] += 1

    with open(DST, "w", encoding="utf-8") as out:
        json.dump({"categories": CATEGORIES, "tags": tags}, out)

    print(f"quotes tagged: {len(quotes)}")
    print(f"total tag rows: {len(tags)}")
    print(f"quotes that needed the fallback tag: {untagged_before_fallback}")
    print("per-category counts:")
    for c in CATEGORIES:
        print(f"  {c:15s} {counts[c]:5d}")
    print(f"wrote {DST}")


if __name__ == "__main__":
    main()
