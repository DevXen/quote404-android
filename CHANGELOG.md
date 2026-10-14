# Quote404 (Android) — Changelog

> **Reconstructed from build logs and contemporaneous notes — not git history.**
> This project predates version control; the entries below were rebuilt from
> `apkbuild` build/package logs, APK file dates, and dated working notes
> (`~/memory/2026-09-*.md`). Only details confirmed by those sources are
> listed. Dozens of intermediate builds shipped between the milestones below;
> the surviving APKs are the ground truth for what each version contained.

## v1.89.3 — 2026-10-13 (versionCode 179)

- **Fixed the two broken sample plugins (Clint's screenshot):** the
  Sample Export Styles and Sample Voice Pack manifests were missing the
  `apiVersion` field the plugin loader requires, so they showed as red
  "Unknown type" error cards. Both now carry `"apiVersion": 1`.
- **Five new bundled sample plugins** (one per type): Arcade After Dark
  theme pack (CRT Terminal + Synthwave Sunset), Poster & Print export
  styles (Movie Poster + Clean Print), Dramatic Voices (Trailer
  Narrator, Chipmunk, Midnight Radio — on-device TTS presets), Stoic
  Mornings quote pack (25 public-domain quotes: Marcus Aurelius,
  Seneca, Epictetus), Big Print layout pack (large-type high-contrast).

## v1.89.2 — 2026-10-13 (versionCode 178)

- **Export search results (Clint):** Search gains an "Export results"
  button that captures the FULL matching set (every match in on-screen
  order, not just the scrolled window) and opens Export on a new
  "Search results" scope — every format and option works (TXT/JSON/XML/
  CSV/PNG/PDF/EPUB/ANKI/MP4/M4A/HTML, one file or per-quote, strip
  usernames, etc.). Exports are titled from the query ("Search - …").
  Picking the scope manually with nothing captured shows an explanatory
  empty state instead of exporting the wrong thing.
- **Friends theme shuffle (Clint):** the "Sunset Hangout" palette
  (formerly the Friends DB theme, id db_friends) is now a general
  preset in the Themes screen (Dark group). The Friends database gets a
  new default theme, "Friends — Wall" (db_friends_wall): a light,
  nostalgic early-Facebook wall-post look (page #ECEFF5, white cards,
  classic FB blue #3B5998), keeping the chat-bubble cards — Clint's
  lines now ride a blue gradient. List, detail, share and exports all
  render the active palette identically; a per-DB override back to
  Sunset Hangout restores the plum/sunset card everywhere.

## v1.89.1 — 2026-10-13 (versionCode 177)

- **Load .db files directly (Clint):** Settings → Databases and the
  first-run picker now have "Load a .db file". A file whose name matches
  an online database (e.g. a bash_im.db copied from a PC) installs AS
  that database — works fully offline; if it's already installed the
  file merges in (user category tables preserved). A file matching the
  official copy exactly is stamped current; a different build installs
  too and the startup check can refresh it later. Any other valid quote
  database is added as your own, like an import.
- **Fixes the v1.89.0 launch crash:** that build's dex contained no R
  classes at all (NoClassDefFoundError androidx/work/R$bool at startup).
  Root cause: package.sh ran javac over the generated R sources with no
  -encoding flag — under a non-UTF-8 locale javac failed on UTF-8
  characters and the failure was swallowed by a pipe, so packaging
  continued without the R classes. package.sh now passes
  `-encoding UTF-8` and aborts if javac fails; releases are additionally
  verified by a dex inventory (R-class definition count matches the
  v1.88.40 baseline exactly: 1,776).

## v1.89.0 — 2026-10-13 (versionCode 176) — SHIPPED BROKEN, superseded same day by v1.89.1

> The v1.89.0 APK crashed at launch on device (missing R classes — see
> v1.89.1 above). The feature work below is real and carried into
> v1.89.1; don't install the v1.89.0 APK.


- **Databases are now downloads, not bundled (Clint):** the 13 quote
  databases no longer ship inside the APK. They are hosted as assets on
  this repo's `databases` release with a manifest.json (revision, sha256,
  content hash, size, quote count, label, date per database).
  - First run shows a picker: choose which databases to install, with
    sizes and quote counts; "Skip for now" is allowed.
  - Every startup checks installed databases against the manifest and
    silently downloads + merges updates (user category tables live inside
    the .db files, so updates always merge, never replace).
  - Settings → Databases: install, update, re-download, or uninstall any
    database; uninstalling the current one switches away first.
  - New databases need only a file + manifest entry on the release — no
    app update (the catalog is fully manifest-driven).
  - Upgrades keep everything: on-device database files stay in place and
    are adopted via a content-hash check (SHA-256 over number/rating/
    text/url), so nothing re-downloads unless its content changed.
  - Your own databases live in the same screen: create your own, import
    your own chat/text files into them, or import a .db file.
  - The .db sources moved in-repo from app assets to `databases/` (still
    the upload source for the release). APK is ~53MB smaller (50.8MB).
  - The repo is now public so the app can download anonymously.

## v1.88.40 — 2026-10-12 (versionCode 175)

- **Friends conversations separated (Clint's Fold7 screenshots of #10/#112):**
  the transcripts were stored as single lines with inline `<Nick>` markers,
  so the Friends chat card rendered whole conversations as one bubble.
  friends.db is now normalized — every turn on its own line (116 quotes),
  Clint's turns labeled `<Clint>` instead of `<Me>` (resolved per post
  author: in Jordyn/Tiffany-authored posts "Me" was the author), nick
  variants unified (Jordyn Sommer → Jordyn, Tiff/TIFFany → Tiffany),
  `*...*` stage directions as their own lines. The FriendsCard also splits
  inline turns itself as a safety net for un-normalized copies.
  BUNDLED_DATA_REVISION 15 → 16.

## v1.88.39 — 2026-10-08 (versionCode 174)

- **Friends database ships built-in:** `friends.db` (176 quotes — 145
  Jordyn, 31 Tiffany, Jordyn/Tiffany categories, PG-13 maturity on the
  racier ones) is now a bundled asset, so it appears in the database
  picker on install with the Friends theme auto-applied — no import step.
  Users who already imported Friends.db will see two entries and can
  remove the imported copy.

## v1.88.38 — 2026-10-08 (versionCode 173)

- **New "Friends" theme:** bespoke chat-bubble card presentation for
  Clint's personal Friends database (Jordyn & Tiffany banter). Sunset-
  hangout palette — deep plum night, hot pink → orange → amber gradient
  bubbles for his lines, warm purple bubbles with amber nick labels for
  theirs, narration as centered status lines. Auto-applies to the imported
  `Friends` database via DB_SITE_THEMES; also pickable in the theme list.

## v1.88.37 — 2026-10-06 (versionCode 172)

- **Runaway-fling fix:** every scrolling list now caps fling velocity at
  12,000 px/s before the platform fling animation runs. Touch-sensor noise
  on finger lift-off could make the velocity tracker report 20,000+ px/s
  from a near-stationary finger, teleporting the list up or down;
  deliberate flicks live well under the ceiling, so fast scrolling still
  feels fast. Applied to all 54 LazyColumn/LazyVerticalGrid call sites.

## v1.88.36 — 2026-10-01 (versionCode 171)

- **New nick format:** `nick1 | nick2:` compound speaker lines (bash.org
  #448982 style, e.g. `CwD | Asbestos: calculus is ...`) are now recognized
  everywhere nicks are: both names get their own nick colors and stay
  tappable on cards, in the widget, and in exports; the message is attributed
  to the colon-side speaker; quiz redaction and "strip usernames" (copy /
  export) handle the new label too.
- **My Databases:** new section (Settings → "My databases", next to
  My Quotes) where any number of named personal databases can be created.
  Each database gets full quote management — add, edit, delete, duplicate
  checking, auto-tagging — plus rename/delete for the database itself. User
  databases appear in the database picker, so Random, search, Quote of the
  Day, and exports cover them like any bundled database.

## v1.88.35 — 2026-10-01 (versionCode 170)

- **Carriage-return / tofu-box fix:** 47 quotes in the bundled `qdb_us.db`
  contain carriage returns (stored `\n\r\n` sequences). The widget/Compose
  text pipeline passed raw `\r` through to Android's `TextView`, which
  rendered each as a missing-glyph diamond. `cleanDisplayText()` now
  normalizes `\r\n` → `\n` and lone `\r` → `\n` before display (display-only;
  stored database text untouched).
- Note: v1.88.35 shipped and was superseded by v1.88.36 within the hour; no
  separate source snapshot was retained for it. Its signed APK is attached to
  the v1.88.36 release below.

## v1.88.34 — 2026-09-29 (versionCode 169)

Latest shipped version at archive time. (Per build notes, v1.88.33 was built
and verified same-day; v1.88.34 supersedes it.)

## v1.88.25 — 2026-09-22 (versionCode 160)

**Rename:** deliverables renamed from `QuoteBrowser-vX.Y.Z.apk` to
`Quote404-vX.Y.Z.apk` (standing convention from this version onward).
v1.88.26 (code 161) followed with an ASCII-frame right-border alignment
correction; v1.88.28 (code 163) added AIM/ICQ/MSN Messenger novelty
quote-card themes; v1.88.31 (code 166) added an on-by-default "Quote404
header" toggle in the Share-as-image dialog.

## v1.88 — 2026-09-20 (versionCode 135)

**Bash.im integration drop:** bundled `bash_im.db` (81,497 quotes, FTS
indexed, canonical `https://bash.im/quote/<id>` links), curated English
translations by default with original Russian available, reconstructed
minimalist bash.im site styling. Shipped after the v1.88–v1.88.8 launch-crash
saga (pre-packaged Room schema validation vs. stale on-device copies —
resolved by the background schema-heal + per-database lock documented in the
README).

## v1.75 — 2026-09-19 (versionCode 119)

- **Cached Gemini explanations:** successful "Explain this quote" results are
  saved on-device (`filesDir/quote_explanations.json`, keyed
  `"$dbId|$number"`); reopening reads locally instead of calling Gemini again.
  Purple AutoAwesome icon on cards with a saved explanation; Search gained a
  "Has explanation" filter.
- **PNG Export/Share parity:** single-quote PNG export reuses the same
  `QuoteCardStyleDialog` renderers as Share (Quote theme, Quote404, Midnight,
  DevXen, …) with true preview before saving.

## v1.72 — 2026-09-18 (versionCode 102)

- Global search across databases.
- v1.72.3: **removed** the "More like this" similar-quote popup on quote tap —
  Clint reversed his earlier approval; tapping a quote now just shows the quote.

## v1.68 — 2026-09-18 (versionCode 98)

- Random screen header rework: wide blue **Shuffle quotes** button + labeled
  **Options** pull-down (replacing the bare ⋮ icon).
- Browse/Random/Search top-bar categories control became a labeled pull-down
  ("All categories" / selected name / "N categories").

## v1.59 — 2026-09-17 (versionCode 63)

- Reorganized Settings (grouped/collapsible sections, pinned + recently
  changed).
- Quote-database verification/repair tool; Browse jump-to-quote-number with
  nonfatal missing-quote feedback; favorites as a daily-notification source;
  previous/next quote-detail swipes; Bash.org canonical priority for
  cross-database duplicates; site-themed quote widget; live theme preview;
  per-database theme picker in the database switcher; Help → What's new.

## v1.40 — 2026-09-15 (versionCode 41)

- Spaced-bracket `< nick>` mention rendering.
- (v1.39: launch-crash fix; v1.38 era: ML Kit init-provider crash fix —
  `MlKitInitProvider` now skipped by `merge.py`, lazy init instead.)

## v1.27 — 2026-09-14 (versionCode 28)

- Content filter one-tap suggested word sets (Profanity, Sexual content,
  Religious exclamations, Racial slurs, Other slurs) with bulk add.
- Multi-select bulk delete on the blocked-words list.
- Redesigned endless deck (vertical flick gestures, large centered type,
  page-number pill, haptics).

## v1.1 — 2026-09-14

Earliest surviving build artifact. (Feature detail for the earliest builds was
not preserved in the notes used for this reconstruction.)
