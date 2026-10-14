# Adding a Quote404 database

Quote databases are **not** bundled in the app (since v1.89.0). They are
hosted as assets on this repo's
[`databases` release](../../releases/tag/databases) with a `manifest.json`
catalog. The app reads the catalog: new databases appear as installable (and
are offered on startup) with **no app update**.

## File layout

- The catalog key / on-device name is stable and plain: `fml.db`.
- The **download asset is dated**: `fml-2026-09-21.db` — the date is the
  day the data was **last scraped** (YYYY-MM-DD). The manifest entry's `url`
  points at the dated asset while its key stays `fml.db`, so re-scrapes
  update the same installed database instead of spawning duplicates.
- In git, the database sources live in `quote-browser-app/databases/`
  under the plain (undated) name.

## Database format

SQLite, mirroring the existing databases exactly:

- `quotes(number INTEGER PK, rating INTEGER, text TEXT, url TEXT,
  maturity INTEGER DEFAULT 0, speakers INTEGER DEFAULT 0,
  spicy INTEGER DEFAULT 0, status TEXT DEFAULT 'approved')`
- `quotes_fts` — FTS5 external-content index on `text`
  (`content='quotes', content_rowid='number'`) with the standard
  `quotes_ai/ad/au` triggers.
- `categories` + `quote_categories` tables (may be empty).
- **`db_info(key, value)`** — provenance inside the file. Required keys:
  `name`, `source`, `source_url`, **`last_scraped`** (YYYY-MM-DD, the date
  the data was last scraped), `stories` (row count). The same date also
  goes in the manifest entry and the download filename (above) — the app
  shows it in the database info.
- Run `PRAGMA integrity_check` and confirm `quotes_fts` count == `quotes`
  count before publishing.

## Built-in site theme (`db_info.theme`)

A database can carry its own default theme — the source site's look — as a
`theme` row in `db_info` holding a JSON spec:

```json
{"name": "FML", "dark": false, "background": "#EEF2F7", "surface": "#FFFFFF",
 "primary": "#2F6DB3", "onBackground": "#1F2937", "onSurface": "#1F2937",
 "number": "#E07B39", "divider": "#C9D6E4", "quoteBackground": "#FFFFFF",
 "card": "standard"}
```

Colors are `#RRGGBB`; `card` names a built-in card structure (`standard`
today). Since v1.89.5 the app reads this on install/update and uses it as
the database's default theme, unless the user has picked a different theme
for that database. Users can also add their own themes to a database
in-app, and load ("Add to my themes") any database theme — built-in or
their own — into their main theme list, where it survives removing the
database. Parsing is lenient: missing or invalid keys fall back to the
current global theme's values, so a partial spec is safe.

## Manifest entry

```json
"fml.db": {
  "revision": 1,
  "url": "fml-2026-09-21.db",
  "sha256": "<sha256 of the .db file bytes>",
  "size": 31703040,
  "count": 62105,
  "label": "FML",
  "date": "09.21.2026",
  "contentSha256": "<content hash, see below>"
}
```

- `date` is MM.DD.YYYY of the last scrape — same date as the filename and
  `db_info.last_scraped`.
- `contentSha256`: SHA-256 over the quotes rows `ORDER BY number`, each
  row contributing the UTF-8 string
  `"<number>\n<rating or empty>\n<text>\n<url>\n"`. The app uses it to
  adopt on-device copies without re-downloading and to detect content
  changes.
- `revision`: start new databases at 1. For an updated database, upload
  the new dated asset, bump `revision`, and refresh `url`, `sha256`,
  `size`, `count`, `date`, and `contentSha256`. Installed apps download
  the new file and **merge** it (user category tables live inside the
  on-device files and are preserved).

## Publishing

1. Build/verify the .db (above).
2. Upload the dated asset + the updated `manifest.json` to the
   `databases` release (replace `manifest.json`).
3. Commit the .db source (plain name) under `quote-browser-app/databases/`.
4. Verify the live manifest downloads anonymously and lists the new entry.
