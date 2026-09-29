# Quote Browser

A native Android app (Kotlin + Jetpack Compose + Room) for browsing and
searching archived quote databases. Ships with the **bash.org archive**
(21,042 quotes) and can import additional quote databases.

## Features

- **Browse** — paged quote list, sortable by quote number or rating
  (ascending/descending), plus a minimum-rating filter (All, >0, >50, >100)
- **Quote of the Day** — a deterministic pick (same quote all day) shown on a
  card at the top of Browse; optional daily 9:00am notification, toggled with
  the bell icon (asks for the Android 13+ notification permission when enabled)
- **Search** — full-text search over quote text (FTS5), or enter digits to
  jump to an exact quote number
- **Random** — random quote tab plus a dice button in the top bar
- **Quote detail** — full text, rating, permalink; **Favorite**, **Copy**
  (clipboard), and **Share** (system chooser) actions
- **Favorites** — persisted per database; export the list to a `.txt` file
- **History** — previously viewed quotes (last 100, newest first), with Clear
  and `.txt` export
- **Top 100 / Bottom 100** — highest- and lowest-rated quotes
- **Long-press any quote row** — quick-action sheet with Copy, Share,
  Favorite toggle, and Open
- **Home-screen widget** — shows a random quote; tap the widget for a new
  random quote, tap the text to open that quote in the app
- **Multiple databases** — switch between bundled databases from the top-bar
  dropdown; import your own `.db` files via the Storage Access Framework
  picker. Favorites and history are stored per database and survive switching.

## Build

Requirements: JDK 17, Android SDK with platform 34 and build-tools.

```bash
./gradlew assembleDebug
# APK lands in app/build/outputs/apk/debug/app-debug.apk
```

Or open the project in Android Studio and press Run.

Pinned toolchain: AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00,
compileSdk/targetSdk 34, minSdk 26, Gradle 8.7 (wrapper included).

Notes:

- The app uses `androidx.compose.material:material-icons-extended` (the full
  icon set) because several referenced icons are not in the core set. If APK
  size matters, consider enabling R8/minify in the release build type.
- The daily Quote-of-the-Day reminder uses WorkManager
  (`androidx.work:work-runtime-ktx`) and needs the `POST_NOTIFICATIONS`
  runtime permission on Android 13+; the app requests it only when the user
  enables the reminder via the bell icon on the Quote-of-the-Day card.

## Databases

### Bundled

Bundled databases are discovered at runtime from
`app/src/main/assets/databases/*.db` — dropping another `.db` file into that
folder is enough to add it to the in-app switcher (label is derived from the
file name, e.g. `qdb_us.db` → "Qdb Us"). No code changes needed.

The bundled `bash_org.db` was generated from the scraped archive with:

```bash
python3 tools/make_db.py
```

which reads `../bash-archive/bashorg_merged.jsonl` (one JSON object per line:
`{"number": int, "rating": int|null, "text": str, "url": str}` — the complete
21,096-quote set; `rating` is null for 59 deleted quotes) and writes the
SQLite file, including a pre-populated FTS5 index (`quotes_fts`,
external-content on `quotes`) so the app never builds the index at runtime.

### Import schema

An importable database is any SQLite `.db` file containing a `quotes` table
with exactly these columns:

| column | type    | constraints |
|--------|---------|-------------|
| number | INTEGER | PRIMARY KEY (NOT NULL) |
| rating | INTEGER | nullable (deleted quotes have no rating) |
| text   | TEXT    | NOT NULL    |
| url    | TEXT    | NOT NULL    |

Extra tables are fine (the FTS5 index `quotes_fts` and its shadow tables are
ignored by Room), but only the `quotes` table is read. Imported files are
validated on selection and copied into the app's internal storage.

## Project layout

```
app/src/main/
  assets/databases/bash_org.db      # bundled quote database (+ FTS5 index)
  java/com/quotebrowser/app/
    MainActivity.kt                 # single activity, hosts Compose UI
    QuoteApp.kt                     # Application; owns DatabaseManager
    data/
      Quote.kt                      # @Entity matching the quotes table
      QuoteDao.kt                   # paging, search (FTS5 MATCH), top/bottom 100, random, QotD offset
      QuoteDatabase.kt              # Room wrapper openable for any quotes-schema .db
      MetaDatabase.kt               # favorites / history / imported-dbs (meta.db)
      DatabaseManager.kt            # discovers assets, opens/caches DBs, SAF import,
                                    # QotD notification preference + WorkManager scheduling
      QuoteRepository.kt            # per-database data access + favorites/history + quoteOfTheDay
    worker/
      QotdWorker.kt                 # daily 9am Quote-of-the-Day notification (taps open the quote)
    widget/
      QuoteWidgetProvider.kt        # home-screen widget: random quote, tap to refresh / open
    ui/
      AppNav.kt                     # NavHost, top bar (DB switcher, dice, overflow), bottom bar,
                                    # widget/notification deep links
      ViewModels.kt                 # Main/Browse/Search/Random/Detail/Favorites/History/TopBottom/Qotd
      ViewModelFactory.kt
      ShareUtils.kt                 # copy-to-clipboard + share-intent + .txt export helpers
      components/Components.kt      # QuoteRow (long-press), QuoteDetailCard, RatingBadge, DbSwitcher,
                                    # EmptyState, QuoteActionSheetHost (quick-action bottom sheet)
      screens/                      # one file per screen (Browse hosts the QotD card + rating filter)
      theme/Theme.kt                # Material3 light/dark theme
  res/
    layout/widget_quote.xml         # home-screen widget layout
    xml/quote_widget_info.xml       # widget provider info
    drawable/widget_background.xml  # widget card background
    drawable/ic_stat_quote.xml      # notification small icon
    values/strings.xml              # widget description
    ...                             # launcher icon + base theme
tools/make_db.py                    # builds app/src/main/assets/databases/bash_org.db
```
