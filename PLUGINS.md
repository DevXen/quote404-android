# Making a Quote404 plugin

Quote404 plugins are **data packs** — themes, quotes, export styles, read-aloud
voices, and page layouts. They contain no executable code, which is what makes
them safe to share. Anyone can make one; when it's accepted into this repo's
catalog, every Quote404 app (v1.89.4+) offers it to users on startup and in
the Plugins screen, with your name on it as the author.

## The five types

| type          | entry file(s)        | what it does                                  |
|---------------|----------------------|-----------------------------------------------|
| `theme-pack`  | `themes.json`        | adds color themes                             |
| `quote-pack`  | `quotes.json`        | adds a quote database                         |
| `export-style`| `styles.json`        | adds HTML export styles                       |
| `voice-pack`  | `voices.json`        | adds read-aloud (TTS) voices                  |
| `layout-pack` | `layout.json` + `style.css` | adds reading layouts for quote pages |

**Start from a sample.** Working examples of every type live in
`quote-browser-app/app/src/main/assets/sample-plugins/` (the
`com.quotebrowser.sample.*` folders). Copy the one closest to your type and
edit it — the samples are the canonical format reference.

## plugin.json (required, exact rules)

Every plugin folder has a `plugin.json` at its root:

```json
{
  "id": "com.yourname.mypack",
  "name": "My Pack",
  "author": "Your Name",
  "version": "1.0.0",
  "apiVersion": 1,
  "type": "theme-pack",
  "entry": "themes.json",
  "description": "What it does, in a sentence or two.",
  "permissions": [],
  "signature": ""
}
```

Rules the app enforces:

- **`apiVersion` is required** and must be `1` (the current plugin API).
  A manifest without it is rejected ("manifest is missing apiVersion").
- `id` must be unique and match your plugin's folder/zip name. Use a
  reverse-domain style id with **your** name in it, not `quotebrowser`.
- `type` must be one of the five types above; `entry` must name the real
  entry file in the pack.
- `version` is dotted numbers (`1.0.0`, `1.10.0` > `1.9.0`). Bump it when
  you update your plugin — the app offers updates by comparing versions.
- Declare the permissions your type needs (see the samples: export styles
  use `["export"]`, voice packs use `["tts"]`, most others use `[]`).

## Packaging

Zip the **contents** of your plugin folder (not the folder itself) so
`plugin.json` sits at the root of the zip:

```
mypack.zip
├── plugin.json
└── themes.json        (or your type's entry file[s])
```

## Submitting yours

1. Fork this repository.
2. Add your plugin's **source folder** under `community-plugins/<your-id>/`
   (the unpacked files, like the samples).
3. Open a pull request. Say what the plugin does and which type it is.

On acceptance, the maintainer publishes your zip and a catalog entry
(name, author, type, version, description, size, SHA-256) to the
[`plugins` release](../../releases/tag/plugins). From then on the app
offers it to everyone: on startup as a "new plugin" (users choose what to
install — nothing is installed without a tap), in Settings →
Check for new databases & plugins, and in the Plugins screen's
"Available on GitHub" section.

Updates work the same way: PR an updated folder with a higher `version`,
and users with your plugin installed get offered the update.

## Content rules

- Quote packs: only quotes you have the right to share (public domain or
  your own). Credit the author in each entry, as the samples do.
- No offensive surprises: the description must honestly describe the pack.
- Keep packs small and self-contained; no external downloads.
