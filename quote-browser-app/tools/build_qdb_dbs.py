#!/usr/bin/env python3
"""Build bundled SQLite databases from the 2011 QDB quote-collection 7z files.

Each archive (already extracted to ~/workspace/qdb_extract/all/<name>/) holds
one <number>.txt per quote. Output: app/src/main/assets/databases/<file>.db
with the app's standard schema:

  TABLE quotes(number INTEGER NOT NULL PRIMARY KEY, rating INTEGER,
               text TEXT NOT NULL, url TEXT NOT NULL)
  VIRTUAL TABLE quotes_fts USING fts5(text, content='quotes',
      content_rowid='number')  -- pre-populated, plus insert/delete/update
      triggers, matching make_db.py.

These collections carry no ratings, so rating is NULL for every quote.
"""

import os
import re
import sqlite3
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
SRC_ROOT = os.path.expanduser("~/workspace/qdb_extract/all")
DST_DIR = os.path.normpath(os.path.join(HERE, "..", "app", "src", "main", "assets", "databases"))

# extracted dir name -> (db file name, label, collection date)
DBS = {
    "I-Rox.com": ("irox_com.db", "I-Rox.com", "2011-04-07"),
    "Mandaliet.com-furcqdb-": ("mandaliet_furcqdb.db", "Mandaliet furcqdb", "2011-04-06"),
    "QDB.MIT.edu": ("qdb_mit_edu.db", "QDB.MIT.edu", "2011-04-05"),
    "QDB.us": ("qdb_us.db", "QDB.us", "2011-04-03"),
    "QuoteIRC.com": ("quoteirc_com.db", "QuoteIRC.com", "2011-04-04"),
    "Quotes.BurntElectrons.org": ("burntelectrons_org.db", "BurntElectrons.org", "2011-04-06"),
    "WQDB.org": ("wqdb_org.db", "WQDB.org", "2011-04-05"),
    "Warpdrive.se": ("warpdrive_se.db", "Warpdrive.se", "2011-04-05"),
    "xkcdb.com": ("xkcdb_com.db", "xkcdb.com", "2011-04-05"),
}

NUM_RE = re.compile(r"^(\d+)\.txt$")


def build_one(src_dir: str, dst: str) -> int:
    if os.path.exists(dst):
        os.remove(dst)
    con = sqlite3.connect(dst)
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
    skipped = 0
    for name in os.listdir(src_dir):
        m = NUM_RE.match(name)
        if not m:
            skipped += 1
            continue
        number = int(m.group(1))
        with open(os.path.join(src_dir, name), encoding="utf-8", errors="replace") as fh:
            text = fh.read().strip()
        if not text:
            skipped += 1
            continue
        batch.append((number, None, text, ""))
        if len(batch) >= 2000:
            cur.executemany(
                "INSERT OR IGNORE INTO quotes(number, rating, text, url) VALUES (?,?,?,?)",
                batch,
            )
            count += len(batch)
            batch = []
    if batch:
        cur.executemany(
            "INSERT OR IGNORE INTO quotes(number, rating, text, url) VALUES (?,?,?,?)", batch
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
    # sanity: FTS row count matches
    fts_n = cur.execute("SELECT COUNT(*) FROM quotes_fts").fetchone()[0]
    real_n = cur.execute("SELECT COUNT(*) FROM quotes").fetchone()[0]
    assert fts_n == real_n, f"FTS mismatch in {dst}: {fts_n} vs {real_n}"
    con.close()
    if skipped:
        print(f"  skipped {skipped} non-quote/empty files", file=sys.stderr)
    return real_n


def main() -> None:
    os.makedirs(DST_DIR, exist_ok=True)
    total = 0
    for src_name, (db_file, label, date) in DBS.items():
        src_dir = os.path.join(SRC_ROOT, src_name)
        if not os.path.isdir(src_dir):
            print(f"MISSING source dir: {src_dir}", file=sys.stderr)
            sys.exit(1)
        dst = os.path.join(DST_DIR, db_file)
        n = build_one(src_dir, dst)
        size = os.path.getsize(dst)
        print(f"{db_file}: {n} quotes, {size} bytes  [{label} / {date}]")
        total += n
    print(f"TOTAL: {total} quotes across {len(DBS)} databases")


if __name__ == "__main__":
    main()
