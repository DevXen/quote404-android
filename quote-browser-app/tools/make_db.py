#!/usr/bin/env python3
"""Build the app's bundled SQLite database from the merged quote archive.

Reads ~/workspace/bash-archive/bashorg_merged.jsonl (one JSON object per line:
{"number": int, "rating": int|null, "text": str, "url": str}) — the complete
21,096-quote set (archive.org dump text + scraped ratings; 59 deleted quotes
have rating null) — and writes app/src/main/assets/databases/bash_org.db with:

  TABLE quotes(number INTEGER NOT NULL PRIMARY KEY, rating INTEGER,
               text TEXT NOT NULL, url TEXT NOT NULL)
  VIRTUAL TABLE quotes_fts USING fts5(text, content='quotes', content_rowid='number')
      -- external-content FTS5 index, pre-populated so the app never builds it
      at runtime, plus the standard insert/delete/update triggers.

The table definition intentionally matches the Room @Entity Quote in the app
(explicit NOT NULL on the PK column keeps Room's schema validation happy).
Rating is NULLABLE: 59 deleted quotes carry no rating data.
"""

import json
import os
import sqlite3
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.normpath(os.path.join(HERE, "..", "..", "bash-archive", "bashorg_merged.jsonl"))
DST_DIR = os.path.normpath(os.path.join(HERE, "..", "app", "src", "main", "assets", "databases"))
DST = os.path.join(DST_DIR, "bash_org.db")


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
               url    TEXT    NOT NULL
           )"""
    )

    batch = []
    count = 0
    with open(SRC, encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line:
                continue
            o = json.loads(line)
            r = o.get("rating")
            batch.append(
                (int(o["number"]), int(r) if r is not None else None,
                 str(o["text"]), str(o["url"]))
            )
            if len(batch) >= 2000:
                cur.executemany(
                    "INSERT INTO quotes(number, rating, text, url) VALUES (?,?,?,?)", batch
                )
                count += len(batch)
                batch = []
    if batch:
        cur.executemany("INSERT INTO quotes(number, rating, text, url) VALUES (?,?,?,?)", batch)
        count += len(batch)

    # FTS5 external-content index, pre-populated.
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

    # Verification.
    n_quotes = cur.execute("SELECT COUNT(*) FROM quotes").fetchone()[0]
    n_fts = cur.execute("SELECT COUNT(*) FROM quotes_fts").fetchone()[0]
    n_null = cur.execute(
        "SELECT COUNT(*) FROM quotes WHERE number IS NULL OR text IS NULL"
    ).fetchone()[0]
    sample = cur.execute(
        "SELECT q.number FROM quotes q JOIN quotes_fts f ON q.number = f.rowid"
        " WHERE quotes_fts MATCH 'linux' LIMIT 3"
    ).fetchall()
    n_null_rating = cur.execute(
        "SELECT COUNT(*) FROM quotes WHERE rating IS NULL"
    ).fetchone()[0]
    print(f"quotes.jsonl rows read : {count}")
    print(f"quotes table rows      : {n_quotes}")
    print(f"quotes_fts rows        : {n_fts}")
    print(f"rows with null key/text: {n_null}")
    print(f"rows with null rating  : {n_null_rating} (deleted quotes, expected 59)")
    print(f"FTS sanity (MATCH 'linux'): {[r[0] for r in sample]}")
    print(f"PRAGMA table_info(quotes): {cur.execute('PRAGMA table_info(quotes)').fetchall()}")
    assert n_quotes == count and n_quotes == 21096, "row count mismatch!"
    assert n_fts == n_quotes, "FTS index incomplete!"
    assert n_null == 0, "null keys present!"
    assert n_null_rating == 59, "unexpected null-rating count!"
    assert sample, "FTS MATCH returned nothing!"
    con.close()
    print(f"wrote {DST}")


if __name__ == "__main__":
    main()
