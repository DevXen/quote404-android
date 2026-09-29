# Quote404 for Android — source archive

**Quote404** (package `com.quotebrowser.app`, formerly **QuoteBrowser**) is an
offline-first Android quote browser: bundled quote databases (bash.org,
qdb.us, bash.im + curated English translations, CrapShack, and others),
browse/search/random, categories, favorites, themes, quote-card PNG/video
export with watermark, TTS read-aloud, widgets, and on-device Room databases.

This repo is a **disaster-recovery archive**: everything needed to rebuild the
app from scratch on a fresh machine. Keep it private.

## Layout

| Path | What it is |
|---|---|
| `quote-browser-app/` | The app itself: `app/src/main` (Kotlin, resources, `AndroidManifest.xml`, bundled `.db` assets), Gradle wrapper files (reference only) |
| `apkbuild/` | The **manual build pipeline** (see below): `build.sh`, `merge.py`, `package.sh`, `gen_r.py`, `resolve.py`, `deps.json`, pinned Kotlin toolchain jars in `tools/`, `debug.keystore` |
| `maturity/` | Maturity-classifier tooling (`classify.py`, `speakers.py`) used when preparing quote data |
| `translation-work/` | Translation work files (bash.im Russian→English chunks, warpdrive data) consumed by `quote-browser-app/tools/build_bashim_db.py` |
| `CHANGELOG.md` | Version history (reconstructed from build logs — not git history) |

## Toolchain — read this first

This project does **NOT use Gradle**. It is built by a hand-rolled pipeline
driving `aapt2`, `kotlinc`, and `D8` directly. The pipeline scripts hardcode
`~/workspace/...` paths, so check this repo out to `~/workspace/` (keeping the
`apkbuild/` and `quote-browser-app/` directory names) and the scripts work as-is.

**Prerequisites (not in this repo — install/provide on the build machine):**

- `~/workspace/tools/jdk17` — JDK 17 (kotlinc, javac, D8 all run on it)
- `~/workspace/android-sdk` — Android SDK with **build-tools 34.0.0** and
  **platform android-36** (`android.jar`)
- Python 3 (for `merge.py`, `resolve.py`, the `tools/*.py` DB builders)
- `sqlite3` CLI (for verifying/repairing bundled database assets)
- Network access to Maven Central / Google Maven (one-time, for `resolve.py`)

## Fetching dependencies

```bash
cd ~/workspace/apkbuild
python3 resolve.py        # downloads AARs/JARs from Maven into libs/, writes deps.json
```

`deps.json` pins the resolved dependency list. `libs/` is regenerable and is
**not** committed. The pinned Kotlin toolchain (`kotlin-compiler-embeddable`,
`kotlin-stdlib`, `compose-compiler`, `kapt`, `trove4j` — 1.9.24 / 1.5.14) **is**
committed under `apkbuild/tools/` because the build scripts reference those
exact jars.

## Build order (order matters!)

```bash
cd ~/workspace/apkbuild
python3 merge.py   # 1. merge manifests (see below)
./build.sh         # 2. aapt2 compile+link, kotlinc, kapt
./package.sh       # 3. javac, D8, zipalign, sign  -> build/apk/app.apk
```

### 1. `merge.py` — BEFORE `build.sh`, always

`merge.py` merges `quote-browser-app/app/src/main/AndroidManifest.xml` with the
library manifests from the resolved AARs into `build/merged/AndroidManifest.xml`,
which `aapt2 link` then consumes. **Run it before every build**, and re-run it
after *any* manifest edit (version bump, permission, component) — otherwise the
APK ships a stale manifest. It also:

- skips `androidx.startup.InitializationProvider` and the ML Kit
  `MlKitInitProvider` (both crash hand-built APKs at launch; ML Kit is
  initialized lazily via `MlKit.initialize(context)` instead),
- requires `android:exported` on every `intent-filter` component (Android 12+;
  the manual link step won't fail without it, but the device refuses install).

### 2. `build.sh` — compile

aapt2 compiles resources, links `base.apk`, `gen_r.py` emits per-package
`R.java`, then kotlinc compiles in two phases (kapt first, then full). It uses
`set -o pipefail` and tees full logs to `build/logs/kotlinc-kapt.log` and
`build/logs/kotlinc-full.log` — **grep those for `error:`** and confirm the new
classes exist in `build/classes` before trusting a build.

### 3. `package.sh` — dex, align, sign

javac → **D8** → dex into the APK → zipalign → sign → verify. Output:
`build/apk/app.apk`. Rename the deliverable per the standing convention:
`Quote404-vX.Y.Z.apk`.

## Version numbering

The version lives in **TWO** places and both must be bumped together:

1. `quote-browser-app/app/build.gradle` (`versionCode` / `versionName`)
2. `quote-browser-app/app/src/main/AndroidManifest.xml`
   (`android:versionCode` / `android:versionName`)

`merge.py` takes the version from the **manifest**. Bumping only one ships an
APK with the old version in its badging.

## Signing

`package.sh` signs with `apkbuild/debug.keystore` — the **standard Android
debug key** (`CN=Android Debug, O=Android, C=US`, password `android`). It is
committed here deliberately: every shipped Quote404 APK is signed with this
exact key (cert SHA-256
`7F:E4:A0:09:39:96:E9:D1:B8:93:B6:12:2F:E7:DC:3A:9A:16:F0:28:FD:2D:5C:55:F8:3E:E7:F4:2E:95:6C:4D`), and **Android will refuse to install an
update signed with a different key**. If you lose this file, existing installs
can't be upgraded in place — users would have to uninstall/reinstall (losing
their on-device data).

For Play Store / public release distribution, re-sign with the DevXen release
keystore (`devxen-release.keystore`, password in Clint's Secure Vault /
`~/workspace/homebrew/keys/` — names only, never committed).

## Secrets inventory (for a fresh setup)

There are **no build-time secrets**. Verified by audit 2026-09-29:

- No API keys, tokens, or passwords anywhere in source (the only
  key-like strings found were Swedish words in translation data — false
  positives).
- The Gemini/translation API key is **entered by the user in the app's
  Settings at runtime** and passed as a request header — it is never baked
  into a build.
- `debug.keystore` uses the well-known `android`/`android` debug credentials.
- The release keystore + password stay out of git (see `.gitignore`).

## Bundled database assets

`quote-browser-app/app/src/main/assets/databases/*.db` are pre-packaged Room
databases shipped inside the APK. Room validates their schema on first open,
so **every asset must carry every column the entity requires** (currently
`status TEXT NOT NULL DEFAULT 'approved'`, plus `maturity`/`spicy`/`speakers`).
After adding/replacing any bundled DB, verify:

```bash
for db in quote-browser-app/app/src/main/assets/databases/*.db; do
  sqlite3 "$db" "SELECT CASE WHEN EXISTS(SELECT 1 FROM pragma_table_info('quotes')
    WHERE name='status') THEN 'OK' ELSE 'MISSING' END;"
done
```

Repair with `ALTER TABLE quotes ADD COLUMN status TEXT NOT NULL DEFAULT 'approved'`
(back up first; row counts must be unchanged). Also bump
`BUNDLED_DATA_REVISION` whenever bundled assets change, or the in-app refresh
skips them as already-current.

Adding a new Room `@Database`/`@Dao`/`@Entity` source file? It must also be
added to the hardcoded `ROOM_SRCS` list in `build.sh`, or kapt never generates
its `_Impl` and the app crashes at launch.

## Build lessons that will bite you (from experience)

- **D8 stays at build-tools 34.0.0 (R8 8.2.2).** It tolerates the ~2048
  duplicate classes across dependency jars (first-wins); newer D8 hard-fails.
- **livedata-core swap:** R8 8.2.2 NPE-crashes on
  `androidx.lifecycle:lifecycle-livedata-core` **2.8.3** class files. After
  resolving deps, replace
  `build/aar/androidx.lifecycle_lifecycle-livedata-core/classes.jar` with the
  **2.4.0** classes.jar (API-compatible; the app never uses LiveData directly).
- **D8 duplicates:** `package.sh` keeps a `DROPMD5` list of dependency jars to
  exclude by content hash (guava 29.0 vs 31.1, old stdlib, etc.). If D8 fails
  with "defined multiple times", add the offender's md5 there.
- **Startup schema heal:** the app heals stale pre-packaged DB schemas on a
  background worker *before* the bundled-data refresh thread starts, serialized
  through a per-database JVM lock — don't "simplify" this; the lock race it
  fixes crashed every launch of v1.88.3.
- **Material3 1.2.1:** `rememberPullToRefreshState()` takes **no** `onRefresh`
  parameter.
- **Large scratch work goes under `~/workspace/`, never `/tmp`** (`/tmp` is a
  small tmpfs and fills silently).
- Keep `~/AGENTS.md`'s "QuoteBrowser APK manual build" section in sync with any
  pipeline change — it's the living build log.

## Rebuilding the bundled databases (optional)

The `.db` assets are committed as built, so a normal rebuild doesn't touch
them. If quote data ever needs regenerating from raw sources:

- `quote-browser-app/tools/build_bashim_db.py` — builds `bash_im.db` from
  `translation-work/bashim/dataset.jsonl`
- `quote-browser-app/tools/build_qdb_dbs.py`, `make_db.py` — other corpora
- `maturity/classify.py`, `maturity/speakers.py` — maturity classification

## What was deliberately excluded from this repo

- `apkbuild/build/` — all intermediate/compiled output (regenerable)
- `apkbuild/libs/` — resolved Maven artifacts, 128 MB (regenerate: `python3 apkbuild/resolve.py`)
- `apkbuild/verify-1.74.6/` — one-off v1.74.6 verification artifacts
- `quote-browser-app/asset-backups/` — stale v1.65 asset copies (the current
  `app/src/main/assets/` is canonical)
- `*.log`, `__pycache__/`, `*.apk` — build junk; APKs ship as GitHub Release
  attachments instead
- Release keystore + passwords — Secure Vault only, never git

## Releases

Versioned APKs are attached to GitHub Releases (see the Releases page), not
committed to git history. The in-repo source is always the latest shipped
version (`1.88.34` / code 169 at archive time).
