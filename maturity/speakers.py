#!/usr/bin/env python3
"""Cast-size counter for Quote404 quote databases.

Counts distinct speaker nicks per quote using the same patterns as
speakerNamesIn() in ui/NickColors.kt (the voice/color pipeline), so the
"Cast Size" filter agrees with what the app sees:
  <nick> mentions, Nick: labels, [nick], nick>, {nick}, -nick-, (nick)

Adds/updates a `speakers` INTEGER column (distinct nick count, 0 = none
detected, e.g. prose quotes). The app buckets it:
  0 -> none/prose, 1 -> Solo, 2 -> Duo, 3 -> Trio, 4 -> Quartet, 5+ -> Group

Usage:
    python3 speakers.py <dbfile>...     # adds speakers column, counts
    python3 speakers.py --check <dbfile> # prints distribution only
"""
import re
import sqlite3
import sys

# Mirrors NickChars in ui/NickColors.kt: A-Za-z0-9_@\-\|\\[]{}^`
NC = r"A-Za-z0-9_@\-\|\\\[\]{}^`"

NICK_MENTION = re.compile(r"<[ \t]*([~&%+]?)([" + NC + r"]+)[ \t]*>")
COLON_SPEAKER = re.compile(
    r"(?m)^([A-Za-z][" + NC + r"]{0,28}(?: [" + NC + r"]{1,28}){0,2}"
    r"|[A-Za-z](?: [" + NC + r"]){1,10})\s*:{1,2}[ \t]"
)
BRACKET_SPEAKER = re.compile(
    r"(?m)^\[[ \t]*([~&%+]?[" + NC + r"]{1,39})[ \t]*\](?::?[ \t]|$)"
)
GT_SPEAKER = re.compile(r"(?m)^([A-Za-z][" + NC + r"]{0,28})>[ \t]")
CURLY_SPEAKER = re.compile(r"(?m)^\{([~&%+]?[" + NC + r"]{1,39})\}[ \t]")
DASH_SPEAKER = re.compile(r"(?m)^-([A-Za-z][" + NC + r"]{1,28})-[ \t]")
PAREN_SPEAKER = re.compile(r"(?m)^\([ \t]*([~&%+]?@?[" + NC + r"]{1,39})[ \t]*\)")


def speaker_names(text: str):
    """Distinct speaker nicks, mirroring speakerNamesIn()."""
    names = set()
    if not text:
        return names
    for m in NICK_MENTION.finditer(text):
        names.add(m.group(2).lower())
    for m in COLON_SPEAKER.finditer(text):
        names.add(m.group(1).lower())
    for m in BRACKET_SPEAKER.finditer(text):
        names.add(m.group(1).lower())
    for rx in (GT_SPEAKER, CURLY_SPEAKER, DASH_SPEAKER, PAREN_SPEAKER):
        for m in rx.finditer(text):
            names.add(m.group(1).lower())
    return names


def bucket(n: int) -> str:
    if n <= 0:
        return "none"
    if n == 1:
        return "solo"
    if n == 2:
        return "duo"
    if n == 3:
        return "trio"
    if n == 4:
        return "quartet"
    return "group5"


def count_db(path, verbose=True):
    con = sqlite3.connect(path)
    cols = [r[1] for r in con.execute("PRAGMA table_info(quotes)")]
    if "speakers" not in cols:
        con.execute("ALTER TABLE quotes ADD COLUMN speakers INTEGER NOT NULL DEFAULT 0")
    rows = con.execute("SELECT number, text FROM quotes").fetchall()
    from collections import Counter
    dist = Counter()
    updates = []
    for number, text in rows:
        n = len(speaker_names(text or ""))
        dist[bucket(n)] += 1
        updates.append((n, number))
    con.executemany("UPDATE quotes SET speakers=? WHERE number=?", updates)
    con.commit()
    con.close()
    if verbose:
        print(f"{path}: {len(rows)} quotes -> " +
              ", ".join(f"{k}={dist[k]}" for k in
                         ("solo", "duo", "trio", "quartet", "group5", "none")
                         if dist[k]))
    return dist


def main():
    args = sys.argv[1:]
    check_only = args[:1] == ["--check"]
    if check_only:
        args = args[1:]
    for path in args:
        if check_only:
            con = sqlite3.connect(path)
            cols = [r[1] for r in con.execute("PRAGMA table_info(quotes)")]
            if "speakers" not in cols:
                print(f"{path}: no speakers column")
            else:
                from collections import Counter
                dist = Counter()
                for (n,) in con.execute("SELECT speakers FROM quotes"):
                    dist[bucket(n)] += 1
                print(f"{path}: " + ", ".join(f"{k}={dist[k]}" for k in
                      ("solo", "duo", "trio", "quartet", "group5", "none") if dist[k]))
            con.close()
        else:
            count_db(path)


if __name__ == "__main__":
    main()
