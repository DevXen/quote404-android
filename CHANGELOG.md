# Quote404 (Android) — Changelog

> **Reconstructed from build logs and contemporaneous notes — not git history.**
> This project predates version control; the entries below were rebuilt from
> `apkbuild` build/package logs, APK file dates, and dated working notes
> (`~/memory/2026-09-*.md`). Only details confirmed by those sources are
> listed. Dozens of intermediate builds shipped between the milestones below;
> the surviving APKs are the ground truth for what each version contained.

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
