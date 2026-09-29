#!/usr/bin/env python3
"""Build the bundled #CrapXen CrapShack SQLite database for QuoteBrowser.

Reads:
  tools/crapshack_quotes.jsonl  ({"number","rating","text","url"} per line)
  tools/crapshack_tags.json     ({"categories":[...], "tags":[[number,cat_idx],...]})
Writes:
  app/src/main/assets/databases/crapshack_quotes.db

Schema matches the app's Room @Entity Quote plus the FTS5 external-content
index (pre-populated, with triggers copied verbatim from tools/make_db.py)
and the categories / quote_categories tables (SQL copied verbatim from
CategoryStore.ensureTables()). No category_seed_version table is created.
"""

import json
import os
import sqlite3
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
JSONL = os.path.join(HERE, "crapshack_quotes.jsonl")
TAGS = os.path.join(HERE, "crapshack_tags.json")
DST_DIR = os.path.normpath(
    os.path.join(HERE, "..", "app", "src", "main", "assets", "databases")
)
DST = os.path.join(DST_DIR, "crapshack_quotes.db")


def main() -> None:
    quotes = []
    with open(JSONL, encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if line:
                quotes.append(json.loads(line))
    with open(TAGS, encoding="utf-8") as fh:
        tag_data = json.load(fh)

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
    batch = [
        (q["number"], q["rating"], q["text"], q["url"]) for q in quotes
    ]
    cur.executemany(
        "INSERT INTO quotes(number, rating, text, url) VALUES (?,?,?,?)", batch
    )

    # FTS5 external-content index + triggers (verbatim from tools/make_db.py).
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

    # Categories (verbatim from CategoryStore.ensureTables()).
    cur.execute(
        "CREATE TABLE IF NOT EXISTS categories ("
        "id INTEGER PRIMARY KEY AUTOINCREMENT, "
        "name TEXT NOT NULL UNIQUE)"
    )
    cur.execute(
        "CREATE TABLE IF NOT EXISTS quote_categories ("
        "quote_number INTEGER NOT NULL, "
        "category_id INTEGER NOT NULL, "
        "PRIMARY KEY (quote_number, category_id))"
    )
    cur.execute(
        "CREATE INDEX IF NOT EXISTS idx_qc_category "
        "ON quote_categories(category_id)"
    )
    cur.execute(
        "CREATE INDEX IF NOT EXISTS idx_qc_quote "
        "ON quote_categories(quote_number)"
    )

    categories = tag_data["categories"]
    name_to_id = {}
    for name in categories:
        cur.execute("INSERT INTO categories (name) VALUES (?)", (name,))
        name_to_id[name] = cur.lastrowid
    tag_rows = [
        (number, name_to_id[categories[cat_idx]])
        for number, cat_idx in tag_data["tags"]
    ]
    cur.executemany(
        "INSERT OR IGNORE INTO quote_categories (quote_number, category_id)"
        " VALUES (?, ?)",
        tag_rows,
    )
    con.commit()

    # Verification.
    n_jsonl = len(quotes)
    n_quotes = cur.execute("SELECT COUNT(*) FROM quotes").fetchone()[0]
    n_fts = cur.execute("SELECT COUNT(*) FROM quotes_fts").fetchone()[0]
    n_cats = cur.execute("SELECT COUNT(*) FROM categories").fetchone()[0]
    n_qc = cur.execute("SELECT COUNT(*) FROM quote_categories").fetchone()[0]
    n_null = cur.execute(
        "SELECT COUNT(*) FROM quotes WHERE number IS NULL OR text IS NULL"
    ).fetchone()[0]
    sample = cur.execute(
        "SELECT q.number FROM quotes q JOIN quotes_fts f ON q.number = f.rowid"
        " WHERE quotes_fts MATCH 'crap' LIMIT 3"
    ).fetchall()
    integrity = cur.execute("PRAGMA integrity_check").fetchone()[0]
    seed_tbl = cur.execute(
        "SELECT COUNT(*) FROM sqlite_master WHERE type='table'"
        " AND name='category_seed_version'"
    ).fetchone()[0]
    triggers = cur.execute(
        "SELECT name FROM sqlite_master WHERE type='trigger' ORDER BY name"
    ).fetchall()
    per_cat = cur.execute(
        "SELECT c.name, COUNT(qc.quote_number) FROM categories c"
        " LEFT JOIN quote_categories qc ON qc.category_id = c.id"
        " GROUP BY c.id ORDER BY c.name"
    ).fetchall()
    con.close()

    print(f"jsonl rows           : {n_jsonl}")
    print(f"quotes table rows    : {n_quotes}")
    print(f"quotes_fts rows      : {n_fts}")
    print(f"categories           : {n_cats}")
    print(f"quote_categories rows: {n_qc}")
    print(f"rows with null key/text: {n_null}")
    print(f"FTS sanity (MATCH 'crap'): {[r[0] for r in sample]}")
    print(f"integrity_check      : {integrity}")
    print(f"category_seed_version table present: {bool(seed_tbl)} (must be False)")
    print(f"triggers             : {[t[0] for t in triggers]}")
    print("per-category tag counts in db:")
    for name, cnt in per_cat:
        print(f"  {name:15s} {cnt:5d}")

    assert n_quotes == n_jsonl, "quotes count mismatch!"
    assert n_fts == n_quotes, "FTS index incomplete!"
    assert n_null == 0, "null key/text rows!"
    assert integrity == "ok", "integrity_check failed!"
    assert sample, "FTS MATCH 'crap' returned no rows!"
    print(f"wrote {DST}")


if __name__ == "__main__":
    main()
