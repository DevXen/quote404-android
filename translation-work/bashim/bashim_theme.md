# bash.im — site theme spec (QuoteBrowser v1.88)

Research: 2026-09-19 → 2026-09-20. Status: **FINAL**.
Structural facts are confirmed from multiple sources. Exact archived CSS was
**not recoverable**: web.archive.org is unreachable from this environment
(CDX API + snapshot URLs fail), the two Pikabu screenshot posts have dead
slugs, image search surfaced no site screenshots, and no press/GitHub/npm
source carries the stylesheet. The palette below is therefore a **labeled
reconstruction** — each value carries a confidence note and its reasoning.
Treat values as starting points for the app theme, not pixel-truth.

## 1. Identity (confirmed)

- Official name: **«bash.im — Цитатник Рунета»** (ru.wikipedia.org/wiki/Bash.im).
- Display in app header: **bash.im** (never append "Quotes").
- Canonical quote URL: `https://bash.im/quote/NNNNNN` — confirmed by three
  independent sources: official Telegram channel posts
  (`https://bash.im/quote/460114`), the `bash-new` npm package README
  (`https://bash.im/quote/442394`), and RSS `<link>` elements
  (`https://bash.im/quote/451983`, `<title>Цитата #451983</title>`).
- Also served `/random`.
- Lineage: `bash.org.ru` (2004) → `bash.im` (2012-03-01) → dark
  2022-02-24 («НЕТ ВОЙНЕ» plug) → closed 2022; `bash.org.рф` spring 2022.

## 2. Target era

The **2019-03-07 major redesign** ("cardinally different from the previous
one", Neolurk «Башорг») through the 2022 shutdown — contemporary with the
dataset (2004-08-30 → ~2020-11).

Design history (Neolurk):
- 2007: black-and-white logo → **color logo**; quote blocks got a distinct
  non-white/shaded treatment.
- 2009-11-30: voting `+`/`-` → words «смешно»/«не смешно».
- 2011-12-11: web-2.0 restyle; voting reverted to **`+` / `-`** (clicking
  showed a 3.5-smiley easter egg); mobile version in testing; AJAX voting
  since 2008-04-30.
- 2019-03-07: major minimalist redesign (this spec's target).

## 3. Layout (confirmed)

Per Wikipedia's Bash.im article:
- Main page showed the 100 most recent approved quotes.
- Above **every** quote: the **quote number/identifier**, **voting controls**,
  and the **current rating** — grouped together in the quote header area.
- Text ads resembled quote blocks but differed mainly by text color.
- Site sections: `Цитатник`, `Бездна` (Abyss), `Комиксы`; sister projects
  `ithappens.me`, `zadolba.li` cross-linked in nav.

App mapping (per Clint's standing header rule for header-bar styles):
- Header bar: database name **bash.im** left, **#NNNNNN** right.
- Vote controls (+ / −, rating) in a right-aligned row just under the header.
- Quote body below, preserving line breaks exactly.

## 4. Palette — reconstructed (see confidence notes)

> UNVERIFIED RECONSTRUCTION. Directional facts are solid (light stock theme —
> every dark variant, "Night Glass" / "DarkBash" / "yet another dark style",
> was a third-party userstyle; 2019 redesign was minimalist), but no hex value
> below was read from the site's CSS.

| Element | Value | Confidence | Reasoning |
|---|---|---|---|
| Page background | `#FFFFFF` | Medium | Light stock theme; 2019 redesign stark/minimalist |
| Quote block surface | `#FFFFFF`, quotes separated by whitespace + hairline dividers (no heavy card chrome) | Medium | Minimalist 2019 language; 2007-era shaded blocks were flattened by the redesign |
| Primary text | `#333333` | Medium | Standard dark-gray body text of the era |
| Secondary/meta text (nav, dates) | `#8A8A8A` | Low–medium | Muted gray convention |
| Hairline dividers | `#E5E5E5` | Low–medium | Light neutral hairlines |
| Links (quote numbers, nav) | blue, restrained (`#2B6CB0` placeholder) | Low | Hue unverified; keep a muted blue, not bright |
| Vote `+` / `−` | `#8A8A8A` text links | Medium | Text voting since Dec 2011; small gray links fit the minimal design |
| Rating number | `#333333` | Low–medium | Displayed with the vote controls in the quote header |
| Header bar | white, hairline bottom border `#E5E5E5` | Medium | Minimalist; no heavy brand bar |
| Logo | lowercase **bash.im** color wordmark (color logo since 2007) | Medium (existence) / Low (colors) | Do not invent logo colors; render wordmark in dark `#222222` if the original art is unavailable |

Typography: system sans-serif stack (the 2019 design used a clean
sans-serif); quote text regular weight, quote number slightly emphasized.
No verified webfont.

## 5. App implementation notes

- Site theme affects **quote cards only**; the global app theme is never
  locked by it (standing rule).
- The **single-quote detail view stays in lockstep** with the site theme:
  same header (bash.im left, #NNNNNN right), same vote row, same card.
- Quote number links to `https://bash.im/quote/<id>` (matches DB `url`).
- Tapping a quote shows only that quote (no "More like this").
- Speaker nicks (`<nick>` format) keep the app's standard speaker
  colorization/voices; the card surface itself stays white per this theme.

## 6. Sources & dead ends

- ru.wikipedia.org/wiki/Bash.im — title, quote anatomy, sections, closure.
- Neolurk «Башорг» — design-era timeline, voting history, logo history.
- userstyles.org/styles/browse/bash — style names proving light stock theme;
  "bash.im - the gray, the wide and the smooth" (2012) targets the pre-2019
  design; its CSS sits behind a JS toggle (not extractable).
- Dead ends: web.archive.org (blocked), Pikabu redesign/mirror posts (dead
  slugs), image-search (no site screenshots), npm `bash-new` (structure
  only), GitHub code search (nothing).
- If archived CSS ever becomes reachable, re-verify §4 against a 2019–2021
  snapshot before treating values as truth.
