#!/usr/bin/env python3
"""Maturity classifier for Quote404 quote databases.

Assigns every quote a maturity level on a single ladder:

    0 KID_SAFE  no profanity, no adult/dark themes at all
    1 CLEAN     clean language; may touch dark/morbid themes (no profanity)
    2 PG13      mild/moderate profanity, crude humor, innuendo-free-ish
    3 R         hard profanity, sexual references, drugs, slurs
    4 NC17      explicit sexual content

Named filters in the app map to ranges on this ladder:
    Kid Safe    -> level 0 only
    Clean Only  -> levels 0-1
    PG-13       -> levels 0-2
    Safe For Work -> levels 0-2 (nothing explicit)
    Rated R     -> levels 0-3
    NC-17       -> levels 0-4 (everything)
    NSFW        -> levels 3-4 only (the spicy stuff)

"Spicy rules": FML's own /spicy curation is authoritative for FML stories
(spicy stories get max(classifier, R)). For every other database the
keyword rules below approximate the same line: content the FML editors
would file under /spicy lands at R or NC17.

Usage:
    python3 classify.py <dbfile>...     # adds maturity column, classifies
    python3 classify.py --check <dbfile> # prints distribution only
    python3 classify.py --rate "text"    # rates one string (debug)
"""
import re
import sqlite3
import sys

# ----------------------------------------------------------------------------
# Keyword lists. Word-boundary matched, case-insensitive.
# Ordered from spiciest level down; first (highest) match wins.
# ----------------------------------------------------------------------------

# L4: explicit sexual content — the NC-17 line.
NC17 = [
    r"blow\s?jobs?", r"hand\s?jobs?", r"tit\s?jobs?", r"rim\s?jobs?",
    r"gave me head", r"give me head",
    r"cunnilingus", r"fellatio", r"deep\s?throat",
    r"masturbat\w*", r"orgasms?", r"ejaculat\w*", r"creampies?",
    r"dildos?", r"vibrators?", r"fleshlights?",
    r"porn\w*", r"hentai", r"\bxxx\b",
    r"bukkake", r"gangbangs?", r"orgys?", r"\bthreesomes?\b", r"foursomes?",
    r"bdsm", r"pegging", r"queef\w*",
    r"anal\s+sex", r"vaginal\s+sex", r"oral\s+sex",
    r"screwing like rabbits",
    r"cum\s+(shot|dump|stain|on|in)", r"\bcum\b",
    r"facial\b.{0,20}(cum|shot)",
    r"puss(?:y|ies).{0,30}(lick|eat|finger)",
    r"dicks?.{0,30}(suck|throat|mouth)",
    r"only\s?fans", r"cam\s?girls?",
]

# L3: sexual situations, hard profanity, drugs, slurs — R.
# Split: R_SEX drives the explicit `spicy` flag (FML-/spicy-style sexual
# content); R_REST is hard profanity/slurs/drugs (R maturity, not spicy).
R_SEX = [
    # sexual situations / acts (FML-spicy vocabulary)
    r"hav(?:e|ing) sex", r"had sex",
    r"sleep(?:ing)? with (him|her|them|me)", r"slept with",
    r"hook(?:ed)? up", r"hooking up",
    r"make love", r"getting laid", r"got laid", r"get laid",
    r"one.night stands?", r"friends with benefits",
    r"cheat(?:ed|ing)? on", r"\baffair\b", r"mistress", r"cuckold\w*",
    r"exhibitionists?", r"\bnaked\b", r"in bed with",
    r"lose (my|his|her|their) virginity", r"take (my|his|her) virginity",
    r"deflower\w*",
    r"swap (girlfriends?|wives|husbands?|partners)",
    r"took (him|her|them) home",
    r"foreplays?", r"\blube\b", r"condoms?",
    r"knocked up", r"baby daddy", r"who the father is",
    r"herpes", r"chlamydia", r"\bSTDs?\b", r"\bSTIs?\b", r"the clap",
    r"\bshagging\b", r"\bbonking\b", r"\bscrewing\b", r"\bbanging\b",
    r"horny", r"aroused?", r"erections?", r"boners?", r"boobs?",
    r"nipples?", r"\bpenis\b", r"\bvagina\b",
    r"strippers?", r"prostitut\w*", r"escorts?",
    r"\bkinks?\b", r"kinky", r"fetish\w*",
    r"wet dreams?", r"cybersex", r"sexting",
]

R_REST = [
    # hard profanity
    r"fuck\w*", r"shit\w*", r"\bcunts?\b", r"\bcocks?\b", r"\bdicks?\b",
    r"puss(?:y|ies)", r"\btits?\b", r"titties",
    r"whores?", r"sluts?", r"bitches?", r"assholes?",
    r"motherfuck\w*",
    # slurs
    r"nigg\w*", r"faggots?", r"\bfags?\b", r"retards?", r"chinks?",
    r"wetbacks?", r"kikes?",
    # drugs
    r"cocaine", r"\bcoke\b.{0,10}(snort|line|bump)", r"heroin", r"\bmeth\b",
    r"marijuana", r"\bweed\b", r"bongs?", r"\bblunts?\b", r"\bLSD\b",
    r"ecstasy", r"\bmolly\b", r"overdos\w*",
    r"snort\w+ coke", r"shoot\w+ (up|heroin)",
]

# L2: mild/moderate profanity, crude humor — PG-13.
PG13 = [
    r"\bdamn\b", r"goddamn\w*", r"\bhell\b", r"\bcrap\w*\b",
    r"\bass\b", r"asses", r"asshats?",
    r"piss\w*", r"\bfart\w*\b", r"\bpoop\b", r"\bturd\b", r"douches?",
    r"bastards?", r"\bsucks?\b", r"sucky",
    r"\bbooze\b", r"\bdrunk\b", r"hungover", r"\bpuke\b", r"\bbarf\b",
    r"\bsexy\b", r"make out", r"making out",
    r"underwear", r"\bbra\b", r"panties",
    r"\bboner\b",
]

# L1: dark/morbid themes with clean language — CLEAN (not for kids, but no
# profanity or sexual content). These only matter when nothing above matched.
DARK = [
    r"\bkill\w*\b", r"murder\w*", r"suicid\w*", r"\bdie\b", r"\bdied\b",
    r"\bdead\b", r"\bdeath\b", r"\bdying\b",
    r"\bblood\b", r"\bgore\b", r"corpses?", r"\bgrave\b", r"funerals?",
    r"\bguns?\b", r"shootings?", r"stab\w*", r"\bbombs?\b", r"torture",
    r"decapitat\w*", r"dismember\w*",
]

LEVELS = [
    (4, NC17),
    (3, R_SEX + R_REST),
    (2, PG13),
    (1, DARK),
]

COMPILED = [
    (lvl, [re.compile(p, re.IGNORECASE) for p in pats]) for lvl, pats in LEVELS
]

# Spicy detection: explicit sexual content (FML-/spicy-style). NC-17
# patterns plus the R sexual-situations vocabulary. Hard profanity, slurs,
# and drugs alone do NOT set the flag.
SPICY_COMPILED = [re.compile(p, re.IGNORECASE) for p in NC17 + R_SEX]

NAMES = {0: "KID_SAFE", 1: "CLEAN", 2: "PG13", 3: "R", 4: "NC17"}


def rate(text: str):
    """Returns (level, matched_pattern_or_None). Highest level wins."""
    if not text:
        return 0, None
    for lvl, regexes in COMPILED:
        for rx in regexes:
            m = rx.search(text)
            if m:
                # Require word boundaries where the pattern doesn't set them:
                # patterns already include \b where needed.
                return lvl, rx.pattern
    return 0, None


def is_spicy(text: str) -> bool:
    """True when the text carries FML-/spicy-style sexual content."""
    if not text:
        return False
    return any(rx.search(text) for rx in SPICY_COMPILED)


def classify_db(path, verbose=True):
    con = sqlite3.connect(path)
    cols = [r[1] for r in con.execute("PRAGMA table_info(quotes)")]
    if "maturity" not in cols:
        con.execute("ALTER TABLE quotes ADD COLUMN maturity INTEGER NOT NULL DEFAULT 0")
    if "spicy" not in cols:
        con.execute("ALTER TABLE quotes ADD COLUMN spicy INTEGER NOT NULL DEFAULT 0")
    rows = con.execute("SELECT number, text FROM quotes").fetchall()
    dist = {i: 0 for i in range(5)}
    spicy_n = 0
    updates = []
    for number, text in rows:
        lvl, _ = rate(text or "")
        sp = 1 if is_spicy(text or "") else 0
        dist[lvl] += 1
        spicy_n += sp
        updates.append((lvl, sp, number))
    con.executemany("UPDATE quotes SET maturity=?, spicy=? WHERE number=?", updates)
    con.commit()
    con.close()
    if verbose:
        print(f"{path}: {len(rows)} quotes -> " +
              ", ".join(f"{NAMES[i]}={dist[i]}" for i in range(5)) +
              f", spicy={spicy_n}")
    return dist


def main():
    args = sys.argv[1:]
    if args[:1] == ["--rate"]:
        lvl, pat = rate(" ".join(args[1:]))
        print(f"{NAMES[lvl]} ({lvl})" + (f" via {pat}" if pat else ""))
        return
    check_only = args[:1] == ["--check"]
    if check_only:
        args = args[1:]
    for path in args:
        if check_only:
            con = sqlite3.connect(path)
            cols = [r[1] for r in con.execute("PRAGMA table_info(quotes)")]
            if "maturity" not in cols:
                print(f"{path}: no maturity column")
            else:
                dist = con.execute(
                    "SELECT maturity, COUNT(*) FROM quotes GROUP BY maturity"
                ).fetchall()
                spicy = con.execute(
                    "SELECT COUNT(*) FROM quotes WHERE spicy=1"
                ).fetchone()[0] if "spicy" in cols else 0
                print(f"{path}: " + ", ".join(
                    f"{NAMES.get(m, m)}={c}" for m, c in sorted(dist)) +
                    f", spicy={spicy}")
            con.close()
        else:
            classify_db(path)


if __name__ == "__main__":
    main()
