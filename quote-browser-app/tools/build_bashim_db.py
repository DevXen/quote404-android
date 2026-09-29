#!/usr/bin/env python3
"""Build the app's bundled bash.im SQLite database.

Reads ~/workspace/translation-work/bashim/dataset.jsonl (one JSON object per
line: {"id": int, "date": ISO, "rating": int, "text": str} — the 81,497-quote
set, Russian originals) and writes
app/src/main/assets/databases/bash_im.db with:

  TABLE quotes(number INTEGER NOT NULL PRIMARY KEY, rating INTEGER,
               text TEXT NOT NULL, url TEXT NOT NULL,
               maturity INTEGER NOT NULL DEFAULT 0,
               speakers INTEGER NOT NULL DEFAULT 0,
               spicy INTEGER NOT NULL DEFAULT 0)
  VIRTUAL TABLE quotes_fts USING fts5(text, content='quotes', content_rowid='number')
      -- external-content FTS5 index, pre-populated, plus the standard
      insert/delete/update triggers.

Schema intentionally matches warpdrive_se.db (the other curated-translation
database) exactly, including the v1.65 content columns. text holds the
Russian ORIGINAL; the curated English lives in the translation seed asset
(translation_seeds/bashim_ru_en.jsonl) and is seeded into translations.db
at runtime like warpdrive's.

url is the canonical per-quote URL, confirmed from three independent
sources (Telegram channel posts, the bash-new npm README, RSS <link>
elements): https://bash.im/quote/NNNNNN

speakers = distinct <nick> speaker tokens at line starts (the app's <nick>
recognition convention). maturity defaults 0 for every row: no ground-truth
maturity ratings exist for this corpus, so nothing is invented; the v1.89
Clean mode handles display-level language rewriting.
"""

import json
import os
import re
import sqlite3
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.normpath(os.path.join(
    HERE, "..", "..", "translation-work", "bashim", "dataset.jsonl"))
DST_DIR = os.path.normpath(os.path.join(
    HERE, "..", "app", "src", "main", "assets", "databases"))
DST = os.path.join(DST_DIR, "bash_im.db")
# v1.88.2: genuine per-quote publication dates for "On this day".
# One line per quote: "<id> <YYYY-MM-DD>", sorted by id. These are the
# corpus's real posting timestamps (dropped from the DB itself so the
# Room-validated schema stays untouched); DatabaseManager reads this
# sidecar asset directly.
DATES_DST = os.path.normpath(os.path.join(
    HERE, "..", "app", "src", "main", "assets", "bashim_posted_dates.txt"))

EXPECTED_ROWS = 81497
NICK_RE = re.compile(r"^<([^<>\n]{1,32})>")


def count_speakers(text: str) -> int:
    nicks = set()
    for line in text.split("\n"):
        m = NICK_RE.match(line.strip())
        if m:
            nicks.add(m.group(1))
    return len(nicks)


def main() -> None:
    if not os.path.exists(SRC):
        print(f"input not found: {SRC}", file=sys.stderr)
        sys.exit(1)
    os.makedirs(DST_DIR, exist_ok=True)
    if os.path.exists(DST):
        os.remove(DST)

    con = sqlite3.connect(DST)
    cur = con.cursor()
    cur.execute(
        """CREATE TABLE quotes(
               number INTEGER NOT NULL PRIMARY KEY,
               rating INTEGER,
               text   TEXT    NOT NULL,
               url    TEXT    NOT NULL,
               status TEXT    NOT NULL DEFAULT 'approved',
               maturity INTEGER NOT NULL DEFAULT 0,
               speakers INTEGER NOT NULL DEFAULT 0,
               spicy INTEGER NOT NULL DEFAULT 0
           )"""
    )

    batch = []
    count = 0
    seen = set()
    dupes = 0
    # v1.88.2: genuine publication dates for the "On this day" sidecar.
    dates = {}
    date_re = re.compile(r"^\d{4}-\d{2}-\d{2}$")
    with open(SRC, encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            o = json.loads(line)
            num = int(o["id"])
            if num in seen:
                dupes += 1
                continue
            seen.add(num)
            r = o.get("rating")
            text = str(o["text"])
            iso = str(o.get("date") or "")
            ymd = iso[:10]
            if not date_re.match(ymd):
                print(f"bad date for id {num}: {iso!r}", file=sys.stderr)
                sys.exit(1)
            dates[num] = ymd
            batch.append(
                (num, int(r) if r is not None else None, text,
                 f"https://bash.im/quote/{num}", "approved", 0,
                 count_speakers(text), 0)
            )
            if len(batch) >= 2000:
                cur.executemany(
                    "INSERT INTO quotes(number, rating, text, url, status,"
                    " maturity, speakers, spicy) VALUES (?,?,?,?,?,?,?,?)",
                    batch
                )
                count += len(batch)
                batch = []
    if batch:
        cur.executemany(
            "INSERT INTO quotes(number, rating, text, url, status,"
            " maturity, speakers, spicy) VALUES (?,?,?,?,?,?,?,?)", batch
        )
        count += len(batch)

    cur.execute(
        "CREATE VIRTUAL TABLE quotes_fts USING fts5(text, content='quotes',"
        " content_rowid='number')"
    )
    cur.execute("INSERT INTO quotes_fts(rowid, text) SELECT number, text FROM quotes")
    cur.execute(
        """CREATE TRIGGER quotes_ai AFTER INSERT ON quotes BEGIN
               INSERT INTO quotes_fts(rowid, text) VALUES (new.number, new.text);
           END"""
    )
    cur.execute(
        """CREATE TRIGGER quotes_ad AFTER DELETE ON quotes BEGIN
               INSERT INTO quotes_fts(quotes_fts, rowid, text)
               VALUES ('delete', old.number, old.text);
           END"""
    )
    cur.execute(
        """CREATE TRIGGER quotes_au AFTER UPDATE ON quotes BEGIN
               INSERT INTO quotes_fts(quotes_fts, rowid, text)
               VALUES ('delete', old.number, old.text);
               INSERT INTO quotes_fts(rowid, text) VALUES (new.number, new.text);
           END"""
    )
    con.commit()

    # v1.88.1: stamp the Room schema version so createFromAsset never
    # needs a migration path for this file.
    cur.execute("PRAGMA user_version = 3")
    con.commit()

    # Verification.
    n_quotes = cur.execute("SELECT COUNT(*) FROM quotes").fetchone()[0]
    n_fts = cur.execute("SELECT COUNT(*) FROM quotes_fts").fetchone()[0]
    n_null = cur.execute(
        "SELECT COUNT(*) FROM quotes WHERE number IS NULL OR text IS NULL"
    ).fetchone()[0]
    n_bad_status = cur.execute(
        "SELECT COUNT(*) FROM quotes WHERE status IS NULL OR status != 'approved'"
    ).fetchone()[0]
    n_bad_url = cur.execute(
        "SELECT COUNT(*) FROM quotes WHERE url != 'https://bash.im/quote/' || number"
    ).fetchone()[0]
    cols = [r[1] for r in cur.execute("PRAGMA table_info(quotes)").fetchall()]
    sample = cur.execute(
        "SELECT q.number FROM quotes q JOIN quotes_fts f ON q.number = f.rowid"
        " WHERE quotes_fts MATCH 'юникс' LIMIT 3"
    ).fetchall()
    first = cur.execute(
        "SELECT number, rating, substr(text,1,40), url, maturity, speakers, spicy"
        " FROM quotes WHERE number = 1"
    ).fetchone()
    print(f"dataset rows read      : {count} (dupes skipped: {dupes})")
    print(f"quotes table rows      : {n_quotes}")
    print(f"quotes_fts rows        : {n_fts}")
    print(f"rows with null key/text: {n_null}")
    print(f"rows with bad url      : {n_bad_url}")
    print(f"rows with bad status   : {n_bad_status}")
    print(f"columns                : {cols}")
    print(f"FTS sanity (MATCH 'юникс'): {[r[0] for r in sample]}")
    print(f"row #1                 : {first}")
    print(f"PRAGMA table_info      : {cur.execute('PRAGMA table_info(quotes)').fetchall()}")
    assert n_quotes == count and n_quotes == EXPECTED_ROWS, "row count mismatch!"
    assert n_fts == n_quotes, "FTS index incomplete!"
    assert n_null == 0, "null keys present!"
    assert n_bad_url == 0, "bad urls present!"
    assert n_bad_status == 0, "bad status values present!"
    assert "status" in cols, "status column missing!"
    assert dupes == 0, "duplicate ids present!"
    assert sample, "FTS MATCH returned nothing!"
    con.close()
    print(f"wrote {DST}")

    # v1.88.2: write the genuine posted-dates sidecar asset.
    assert len(dates) == EXPECTED_ROWS, "date sidecar row count mismatch!"
    with open(DATES_DST, "w", encoding="utf-8") as fh:
        for num in sorted(dates):
            fh.write(f"{num} {dates[num]}\n")
    # Verify what we wrote.
    back = {}
    with open(DATES_DST, encoding="utf-8") as fh:
        for line in fh:
            parts = line.split()
            assert len(parts) == 2, f"malformed sidecar line: {line!r}"
            back[int(parts[0])] = parts[1]
    assert back == dates, "sidecar round-trip mismatch!"
    import datetime
    today_mmdd = datetime.date.today().strftime("%m-%d")
    n_today = sum(1 for d in dates.values() if d[5:] == today_mmdd)
    print(f"wrote {DATES_DST} ({len(back)} dates; {n_today} fall on {today_mmdd})")


if __name__ == "__main__":
    main()
