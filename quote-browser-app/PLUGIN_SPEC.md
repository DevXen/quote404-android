# QuoteBrowser Plugin Spec — v1 (Plugin API 1)

> v1.85 adds the `layout-pack` type (quote layouts). Still Plugin API 1 —
> no manifest changes; layout packs are data-only like theme packs.
>
> v1.87 adds the `export-style` and `voice-pack` loaders, one-tap pack
> updates (`updateUrl`), and per-plugin settings pages (`settings`
> schema). Still Plugin API 1 — all additive; older packs keep working.

QuoteBrowser 1.83+ supports **data packs**: third-party content bundles that
the app discovers, validates, and loads at startup. The architecture covers
**seven** pack types; v1 loads five of them (`quote-pack`, `theme-pack`,
`layout-pack`, `export-style`, `voice-pack`), and the other two are
reserved as known types so they can be added without redesigning the
manifest or the loader.

> **Safety model (v1):** packs are *data only*. A pack is a folder of JSON
> files — no code is ever executed from a pack. A malformed pack is rejected
> with a note on the Plugins screen; it can never crash the app or block
> startup. Because packs cannot run code in v1, the declared-permission
> system below is validated and user-approved at install time now, and
> becomes the enforcement gate the moment a pack type can *do* anything.

## Pack types

| Type | v1 status | What it is | Declared permissions (typical) |
|------|-----------|-----------|--------------------------------|
| `quote-pack` | ✅ loads | A quote database | none |
| `theme-pack` | ✅ loads | One or more UI themes | none |
| `layout-pack` | ✅ loads (v1.85+) | A quote layout (arrangement + CSS) | none |
| `export-style` | ✅ loads (v1.87+) | Extra CSS themes for HTML export | `export`, `storage` |
| `voice-pack` | ✅ loads (v1.87+) | Extra read-aloud voices | `tts`, `storage` |
| `game-pack` | reserved | Quote games (quiz, memory, …) | `quotes.read`, `storage` (plus `audio` if it makes sound) |
| `video-background` | reserved | Animated background for video exports | `export`, `storage` |

A pack whose `type` is known but not loadable by the app version is
refused with *"pack type X needs a newer QuoteBrowser"* — a clear message,
never a crash. This is the forward-compatibility contract: an old app plus
a new pack type degrades to a sentence, and a new app plus an old pack
keeps working (unknown manifest fields are always ignored).

### What each reserved type will need (design, not v1)

- **game-pack:** reads the quote corpus via a declared `quotes.read`
  capability (text, numbers, ratings — never write access). Games get
  their own screen entry point in a later API.
- **video-background:** supplies an animated background to the video
  export path, rendered through the same shared renderer as built-in
  backgrounds; gated by the `export` permission.

## Pack layout

A pack is a folder:

```
my-pack/
  plugin.json      # the manifest (required)
  quotes.json      # quote-pack data (required for type quote-pack)
  themes.json      # theme-pack data (required for type theme-pack)
```

## Installing a pack

- **In the app (recommended):** Settings → Plugins → *Import pack (.zip)*,
  then pick a `.zip` whose top level (or single top-level folder) contains
  `plugin.json`. The app validates the pack, then shows an **install
  approval screen**: name, author, version, type, the permissions it asks
  for, and its SHA-256 checksum. Nothing is installed until you approve.
- **Manual:** copy the pack folder into the app's private `plugins/`
  directory. (On most devices the easy route is the zip import above.)

## Lifecycle

- **Enable / disable:** toggle per pack on the Plugins screen. A disabled
  pack is ignored everywhere, as if it were not installed.
- **Update (v1.87+):** a pack whose manifest declares `updateUrl` (an
  `http(s)` URL to a newer pack `.zip`) gets a *Check for updates* button
  on the Plugins screen. The app downloads the zip, validates it exactly
  like a manual import, and — only when the downloaded pack has the **same
  id** and a **newer version** (dotted-version comparison, so `1.2.10` >
  `1.2.9`) — offers a one-tap *Update*. Applying replaces the pack folder
  and rescans; the enabled flag survives (it is keyed by pack id). For
  quote packs the cached database handle is evicted first, like
  uninstall. If the update declares permissions the installed pack did
  not, the new permissions are shown before you apply — a pack can never
  silently broaden what it may do. Packs without an `updateUrl` update
  the old way: replace the pack folder (or re-import the zip) with a
  higher `version`.
- **Uninstall:** remove per pack on the Plugins screen. Deletes the pack
  folder, drops its cached database and preferences, and rescans — the
  pack is gone everywhere.

## Declared permissions

Every pack declares what it may touch in a `permissions` array. The app
validates every entry against the known vocabulary and rejects packs that
declare unknown permissions. The approval screen shows the list at install
time, and **a pack can never silently exceed its declarations** — future
capability APIs (corpus read for games, the export pipeline for export
styles / video backgrounds, TTS for voices) check the declarations before
granting access.

| Permission | Grants |
|------------|--------|
| `quotes.read` | Read access to the quote corpus (text, numbers, ratings) — for games and future types. Never write access. |
| `storage` | Files inside the pack's own private sandbox directory. |
| `network` | Making network requests. |
| `tts` | Text-to-speech / audio playback (voices). |
| `audio` | Audio playback (games). |
| `export` | Hooking the export pipeline / shared renderer (export styles, video backgrounds). |

v1's data packs declare `"permissions": []` — they contribute content the
app reads itself, so they need no capabilities.

## plugin.json — manifest reference

```json
{
  "id": "com.example.quotes.stoicism",
  "name": "Stoic Wisdom",
  "version": "1.0.0",
  "author": "Jane Doe",
  "apiVersion": 1,
  "type": "quote-pack",
  "entry": "quotes.json",
  "description": "Two hundred stoic quotes, cleaned and numbered.",
  "permissions": [],
  "settings": {
    "settings": [
      { "key": "dark_mode", "type": "toggle", "title": "Dark variant",
        "description": "Prefer the dark style.", "default": true },
      { "key": "accent", "type": "choice", "title": "Accent",
        "options": ["Cyan", "Amber"], "default": "Cyan" },
      { "key": "footer", "type": "text", "title": "Footer text", "default": "" }
    ]
  },
  "updateUrl": "https://example.com/packs/stoicism.zip",
  "signature": "",
  "checksum": ""
}
```

| Field         | Required | Notes |
|---------------|----------|-------|
| `id`          | yes | Reverse-domain, e.g. `com.example.quotes.stoicism`. Must match `^[A-Za-z0-9_]+(\.[A-Za-z0-9_]+)+$` (at least one dot). Must be unique across installed packs. |
| `name`        | yes | Display name shown in the database list / theme picker. |
| `version`     | yes | Free-form version string, e.g. `1.0.0`. |
| `author`      | yes | Pack author — the pack's identity — shown on the Plugins screen. Required on every pack. |
| `apiVersion`  | yes | Integer. v1 packs use `1`. If a pack declares a newer apiVersion than the app supports, the app refuses it with "requires a newer QuoteBrowser"; an older apiVersion keeps loading on newer apps. |
| `type`        | yes | One of the seven known types: `quote-pack`, `theme-pack`, `layout-pack`, `export-style`, `game-pack`, `voice-pack`, `video-background`. Unknown types are rejected; known-but-not-yet-loadable types are refused with "needs a newer QuoteBrowser". |
| `entry`       | yes | File name of the pack's data file inside the pack folder (`quotes.json` / `themes.json`). Must not contain `..` or be an absolute path. |
| `description` | no  | Short blurb shown on the Plugins screen. |
| `permissions` | no  | Array of declared permissions (see above). Unknown permission strings reject the pack. Defaults to `[]`. |
| `settings`    | no  | Per-plugin settings schema (v1.87+) — an object with a `settings` array; each entry has `key`, `type` (`toggle`/`text`/`choice`), `title`, optional `description`, `default`, and `options` (required for `choice`). The app renders a settings page per pack (toggle → switch, text → field, choice → dropdown). See below. |
| `updateUrl`   | no  | One-tap update source (v1.87+): an `http://` or `https://` URL to a newer pack `.zip`. Anything else is ignored. Enables *Check for updates* on the Plugins screen. |
| `signature`   | no  | **Reserved for real pack signing** (a later plugin API). Leave `""` for now. |
| `checksum`    | no  | **Reserved.** The app computes and verifies the entry file's SHA-256 itself at install/scan time; the field stays reserved for a future signed-checksum workflow. |

### Validation

A pack is rejected (listed on the Plugins screen with the reason, never
crashing) when: `plugin.json` is missing or not valid JSON; any required
field is missing/blank; `id` is malformed or collides with another pack;
`apiVersion` is newer than the app supports; `type` is unknown (or known
but not loadable by this app version); a declared permission is unknown;
the `entry` file is missing or escapes the pack folder.

**Integrity:** every pack carries its author identity and the app records
the SHA-256 checksum of the entry file at install and scan time (shown on
the Plugins screen). If the entry file changes without a version bump, the
app detects the checksum drift and rebuilds. Real cryptographic signing
uses the reserved `signature` field in a later API.

## quote-pack — quotes.json

The entry file is either a JSON **array** of quote objects, or an object
with a `"quotes"` array. Each quote:

```json
{
  "quotes": [
    { "number": 1, "text": "The obstacle is the way.", "rating": 12, "url": "", "status": "approved" },
    { "number": 2, "text": "Amor fati.", "rating": null }
  ]
}
```

| Field    | Required | Notes |
|----------|----------|-------|
| `number` | yes | Integer quote number, unique within the pack. Displayed as the quote's #number. |
| `text`   | yes | The quote text. May contain newlines. |
| `rating` | no  | Integer score, or null/omitted for unrated. |
| `url`    | no  | Source URL shown as "view original" when present. |
| `status` | no  | `approved` (default) or `queued`. |

Bad entries (missing number/text, duplicate numbers) are skipped and
counted; the rest of the pack still loads.

**How it appears in the app:** the pack becomes a real database in the
database switcher under its `name`, with database id `plugin:<id>`. It
works in Browse, Search (including all-databases search), Random/Shuffle,
Favorites, History, and quote-of-the-day exactly like a built-in database.
Per-database theme settings apply to it.

**Updating a pack:** replace the pack folder (or re-import the zip) with a
higher `version`. The app rebuilds the pack's quote database when the
manifest version or entry file changes.

## theme-pack — themes.json

```json
{
  "themes": [
    {
      "key": "neon-nights",
      "name": "Neon Nights",
      "dark": true,
      "background": "#050510",
      "surface": "#101020",
      "primary": "#00E5FF",
      "onBackground": "#FFFFFF",
      "onSurface": "#FFFFFF",
      "number": "#00E5FF",
      "rating": "#90CAF9",
      "ratingText": "#000000",
      "quoteBackground": "#050510",
      "divider": "#2E4A66",
      "structure": "classic_qdb"
    }
  ]
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `key` | yes | Unique within the pack; `^[A-Za-z0-9_-]+$`. Theme id becomes `plugin:<pack-id>:<key>`. |
| `name` | yes | Display name in the theme picker. |
| `dark` | no | `true`/`false` (default `true`). Tells the app whether this is a dark theme. |
| `background`, `surface`, `primary`, `onBackground`, `onSurface` | yes | Base palette. |
| `number`, `rating`, `ratingText`, `quoteBackground`, `divider` | no | Accent slots; sensible defaults are derived from `primary`/theme darkness when omitted. |
| `nickBrackets`, `nickAction` | no | IRC nick styling colors; omit for defaults. |
| `structure` | no | Which existing site presentation the theme mimics for quote cards when used as a database theme: one of `classic_qdb`, `bashorg`, `db_crapshack`, `db_qdb_us`, `db_qdb_mit`, `db_quoteirc`, `db_burntelectrons`, `db_warpdrive`, `db_wqdb`, `db_xkcdb`, `db_furcqdb`, `db_irox`. Default: `classic_qdb`. (v1 packs reuse the app's built-in card art; fully custom card layouts arrive with a later plugin API.) |

Colors are `"#RRGGBB"` or `"RRGGBB"` (8-digit `AARRGGBB` also accepted).
A theme with any invalid color is skipped; the rest of the pack still loads.

**How it appears in the app:** each theme shows up in Settings → Themes
(and the per-database theme picker) and can be chosen as the global app
theme or a single database's palette theme.

## layout-pack — layout.json (v1.85+)

A layout pack arranges a quote's *elements* — the theme still owns colors
and fonts. One layout drives both the on-screen card and the HTML export.

Pack contents:

```
my-layout-pack/
  plugin.json     # type: "layout-pack", entry: "layout.json"
  layout.json     # the layout definition (below)
  style.css       # optional: overrides the "css" field of layout.json
```

`layout.json`:

```json
{
  "id": "broadsheet",
  "name": "Broadsheet",
  "author": "Jane Doe",
  "version": 1,
  "description": "Dense newspaper-style card.",
  "blocks": [
    { "type": "siteHeader", "align": "spread" },
    { "type": "quoteText", "align": "justify" },
    { "type": "author", "align": "end", "prefix": "— " },
    { "type": "divider" },
    { "type": "meta", "show": ["date", "categories"] },
    { "type": "rating", "align": "end" }
  ],
  "css": ".qb-quote { ... }",
  "htmlTemplate": ""
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `id` | yes | `^[A-Za-z0-9_-]+$` (max 64 chars). Layout id becomes `plugin:<pack-id>:<id>`. |
| `name` | yes | Display name in the layout picker. |
| `author` | no | Shown in the picker detail. (The manifest author is still required.) |
| `version` | no | Layout schema version, default 1. |
| `description` | no | One-liner shown in the picker. |
| `blocks` | yes | 1–40 blocks, in top-to-bottom order. |
| `css` | no | CSS for the HTML export (one class per block, see below). A sibling `style.css` file overrides this field. |
| `htmlTemplate` | no | Custom per-quote HTML using `{{tokens}}` (below). Blank = HTML is generated from `blocks`. |

Block types: `siteHeader`, `quoteText`, `author`, `rating`, `meta`,
`actions`, `divider`, `spacer`. Per-block options:

| Option | Values | Notes |
|--------|--------|-------|
| `align` | `start`, `center`, `end`, `spread`, `justify` | `spread` = first/last pushed apart (header); `justify` = text only. |
| `textScale` | 0.5–3.0 | Relative text size multiplier. |
| `paddingDp` | 0–64 | Vertical padding around the block (on-screen). |
| `show` | list of strings | `meta`: `date`, `categories`, `url`. `actions`: `share`, `copy`, `favorite`, `tts`. |
| `prefix` | string | Prepended text, e.g. `"— "` for the author block. |

Validation: unknown block type, bad `id`, empty `blocks`, or malformed
JSON rejects the pack with a message on the Plugins screen — never a
crash. `textScale`/`paddingDp` are clamped; unknown `align` values are
rejected.

**HTML export classes** (one per block): `qb-site-header` (with
`qb-site-name`, `qb-quote-number`), `qb-quote-text`, `qb-author`,
`qb-rating`, `qb-meta`, `qb-source-url`, `qb-divider`, `qb-spacer`,
plus `qb-branding` and the page's `qb-page-title`. Each quote is wrapped
in `<article class="qb-quote qb-layout-<id>">`.

**Custom HTML templates:** when `htmlTemplate` is non-blank it replaces
block-generated HTML for that layout. Arrange these tokens however you
want; style with your own CSS:

`{{quote_text}}` `{{author}}` `{{quote_number}}` `{{site_name}}`
`{{source_url}}` `{{date}}` `{{rating}}` `{{categories}}`

Tokens are HTML-escaped on export; newlines in `{{quote_text}}` become
`<br>`. Unknown tokens are left as-is so typos stay visible.

**How it appears in the app:** the layout shows up in Settings → Quote
layouts (and the per-database override picker) next to the six premades,
and becomes available to the HTML export. No permissions are needed —
a layout pack is arrangement + CSS only, and no code ever runs from a
pack.

## export-style — styles.json (v1.87+)

An export-style pack contributes extra CSS themes for HTML export. The
entry file is an object with a `"styles"` array:

```json
{
  "styles": [
    {
      "key": "terminal",
      "name": "Terminal",
      "description": "Green-on-black terminal look.",
      "css": "body { background: #0a0f0a; color: #c8e6c8; }\n.qb-said-name { color: #7fff9f; font-weight: bold; }"
    }
  ]
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `key` | yes | Unique within the pack; `^[A-Za-z0-9_-]+$`. Style id becomes `plugin:<pack-id>:<key>`. |
| `name` | yes | Display name in the export-style picker. |
| `css` | yes | CSS layered **on top of** the active quote layout's CSS at export time (max 512 KB per style). |
| `description` | no | One-liner shown on the Plugins screen. |

Bad entries (blank key/name/css, duplicate keys, oversized CSS) are
skipped; a pack with no valid styles is rejected with a note. Target the
semantic classes the HTML export emits: `.qb-said-name` (speaker names),
`.qb-said-text` (spoken words), `.qb-action-line` (`* action` lines),
`.qb-timestamp` (`[HH:MM]` prefixes), `.qb-other-line` (narration/system
lines), plus the layout classes (`.qb-quote`, `.qb-quote-text`,
`.qb-page-title`, …).

**How it appears in the app:** every style shows up in Export → HTML →
*Export style*, next to *Default (active quote layout)*. The chosen CSS
applies to all three HTML export paths (scope export, per-category
export, export tray).

## voice-pack — voices.json (v1.87+)

A voice pack contributes extra read-aloud voices. The entry file is an
object with a `"voices"` array:

```json
{
  "voices": [
    {
      "key": "storyteller",
      "name": "Storyteller",
      "engine": "gemini",
      "baseVoice": "Aoede",
      "gender": "female",
      "description": "A warm cloud voice."
    },
    {
      "key": "chipmunk",
      "name": "Chipmunk",
      "engine": "builtin",
      "pitch": 1.6,
      "rate": 1.15,
      "description": "A high-pitched built-in preset."
    }
  ]
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `key` | yes | Unique within the pack; `^[A-Za-z0-9_-]+$`. Voice id becomes `plugin:<pack-id>:<key>`. |
| `name` | yes | Display name in the voice pickers. |
| `engine` | no | `gemini` (default) or `builtin`. |
| `baseVoice` | for `gemini` | A real Gemini prebuilt voice name (`Zephyr`, `Kore`, `Fenrir`, …) that this voice aliases. Unknown names reject the entry. |
| `pitch`, `rate` | no | Built-in engine preset: pitch/rate multipliers applied to the device TTS voice (0.25–4.0, clamped; default 1.0). Also applied to `gemini` voices as metadata. |
| `gender` | no | `male` or `female` (anything else is ignored). |
| `description` | no | One-liner shown on the Plugins screen. |

Bad entries are skipped; a pack with no valid voices is rejected with a
note. **Gemini pack voices alias a real cloud voice** — the app resolves
the id to its `baseVoice` before every synthesis request, so pack ids
never reach the API. Disabling or uninstalling the pack removes its
voices everywhere; stored overrides pointing at a vanished voice fall
back to Automatic.

**How it appears in the app:** pack voices join the Gemini and built-in
lists in Settings → Read aloud (default voice, narrator voice, and the
per-speaker picker). Built-in presets apply their pitch/rate to the
device TTS voice and keep working with read-along speed and playlists.

## settings schema — per-plugin settings (v1.87+)

A pack of any type may declare a `settings` object in its manifest:

```json
"settings": {
  "settings": [
    { "key": "serif_headings", "type": "toggle", "title": "Serif headings",
      "description": "Use serif fonts for headings.", "default": true },
    { "key": "footer_text", "type": "text", "title": "Footer text",
      "description": "Custom footer line.", "default": "" },
    { "key": "accent", "type": "choice", "title": "Accent color",
      "options": ["Cyan", "Amber", "Lime"], "default": "Cyan" }
  ]
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `key` | yes | `^[A-Za-z0-9_-]+$`, unique within the pack. |
| `type` | yes | `toggle` (→ switch), `text` (→ text field), `choice` (→ dropdown). |
| `title` | yes | Label shown in the settings page. |
| `description` | no | Helper text under the control. |
| `default` | no | Stored default as a string (`true`/`false` for toggles; for `choice` it must be one of `options`, else the first option wins). |
| `options` | for `choice` | The allowed values. A `choice` with no options is skipped. |

Malformed entries are skipped; a pack with no valid settings simply
shows no Settings button. Values persist per pack in the app's private
preferences and are rendered on a dedicated settings page per pack
(Plugins screen → *Settings*).

## Sample packs

The app ships tiny sample packs (auto-installed on first launch after
upgrading) that double as authoring examples:

- `com.quotebrowser.sample.wisdom` — quote-pack with 8 quotes.
- `com.quotebrowser.sample.neon` — theme-pack with 1 theme.
- `com.quotebrowser.sample.broadsheet` — layout-pack with 1 layout (v1.85+).
- `com.quotebrowser.sample.exportstyle` — export-style pack with 2 styles
  (Terminal, Parchment) plus a 3-control settings demo (v1.87+).
- `com.quotebrowser.sample.voicepack` — voice-pack with 2 cloud voices
  and 1 built-in pitch/rate preset (v1.87+).

Their source lives in the app's `assets/sample-plugins/` folder.

## Versioning & compatibility

- `PLUGIN_API_VERSION = 1` in v1.83.
- Minor app updates never break v1 packs: unknown manifest fields are
  ignored, unknown pack `type`s are rejected gracefully (not crashes), and
  older apiVersions keep loading on newer apps.
- Author identity is required on every pack; the app verifies entry-file
  checksums itself at install/scan time. Real cryptographic signing will
  use the reserved `signature`/`checksum` fields; unsigned packs keep
  working.

## Roadmap (not in v1)

Real pack signing (`signature` verification), and the loaders for the two
remaining reserved pack types (`game-pack`, `video-background`) — the
manifest's `type` field, permission vocabulary, and apiVersion gating
already reserve room for all of these, so none of it needs a redesign.
