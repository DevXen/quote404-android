# Site layouts — single-quote page reference for the v1.53 "mini version of the site" redesign

Research goal: per-site single-quote page structure (page background, header bar,
quote header, quote body, footer/info bar, extras) so each QuoteBrowser database
theme can render a faithful mini version of its original site.

## Sources & caveats

- **Bash.org**: app's existing Bash theme (built to match the real bash.org site:
  white bg, black text, orange #FF9900); `BashOrgFetch.kt` parsing of real
  bash.org quote pages (`<p class="qt">` blocks, `#N ...` header first line);
  Wayback homepage text fetched via web search (captures 2003/2006). The v1.53
  site-mimicking Bash chrome in `DbSiteCards.kt` (orange bars, white bordered
  body, "Score: N").
- **qdb.us**: the user's supplied qdb.us screenshot (navy header/navigation,
  light gray content rows); v1.51/v1.53 implementation details in
  `DbSiteCards.kt` (`#10479 +(53)-` quote line, gold/amber bar, "Rendered in…"
  footer); single-quote URL pattern confirmed by `quote-scrape/scrape_qdb.py`:
  `https://web.archive.org/web/20200123054707id_/http://qdb.us/{number}`.
- **The other 8 sites**: `theme-refs/*-notes.md` (WeasyPrint reconstructions of
  archived HTML/CSS for QDB.MIT.edu, QuoteIRC, BurntElectrons, Warpdrive;
  user screenshots for WQDB, xkcdb, furcqdb, I-Rox).
- **CrapShack**: no public site; layout follows Clint's spec (Bash-theme layout
  clone, black bg, dark gray text, light blue headers).
- **Terminal fetch note (2026-09-15)**: `browser.open` on the Wayback
  single-quote URLs failed terminally and could not be retried; details below
  that could not be confirmed from a fresh archived-page fetch are grounded in
  the artifacts above and otherwise marked **UNKNOWN** rather than guessed.

---

## 1. Bash.org — single-quote page

- **Page background:** white. Content column centered on white; quote text black.
- **Header bar / branding:** top header carries the site wordmark — nav line
  "QDB / Admin" above the title "Quote Database Home". Branding area is text,
  left-aligned; no heavy graphical logo in the classic layout.
- **Nav:** plain link row — `Home / Latest / Browse / Random >0 / Top 100 -200 /
  Add Quote / ModApp / Search` (per 2006 capture).
- **Quote header:** inside the quote block (`<p class="qt">`), the FIRST line is
  the quote header: `#N ...` followed by the score and the vote/flag links —
  `+` and `-` voting links and the `[X]` link (submits the quote for
  removal/review). Quote numbers render in orange (#FF9900 — the app's dedicated
  quote-number color slot for the true Bash theme). Exact score format on the
  single-quote page: **UNKNOWN** (app renders the site-mimicking "Score: N"
  label; do not copy verbatim as a site fact).
- **Quote body:** monospace typewriter font, black on white, `<br>`-separated
  IRC log lines. No card chrome on the real site — the body is plain page text.
- **Footer / info bar:** bottom line shows the real database counts, e.g.
  "19140 quotes approved; 8006 quotes pending", then "© QDB 1999-2006, All
  Rights Reserved."
- **Extras:** voting is popup-based (later captures changed voting to a popup
  instead of navigating away); quotes are moderated, hence the ModApp/moderator
  roster; single-quote URL form is `http://bash.org/?{number}` (confirmed by
  `BashOrgFetch.kt`, which fetches this form live).
- **Queued tags:** approved quotes only on the main archive; submissions sit in
  a moderation queue (ModApp). No visible "Queued" tag on approved single-quote
  pages.

---

## 2. qdb.us — single-quote page

- **Page background:** light gray content rows on a darker chrome; per the
  user's screenshot, the site's top-level chrome is **navy** (header and
  navigation bars).
- **Header bar / branding:** page-level navy header/navigation bars with the
  site navigation. Per-quote: a **gold/amber (#D4A017) bar** carrying a bold
  serif **"QDB"** wordmark on the left (dark brown #3E2723 text) and
  **"Quote #N"** on the right.
- **Nav:** site-wide navigation under/beside the navy header (exact labels on
  the single-quote page: UNKNOWN — homepage listing uses standard QDB sections).
- **Quote header / quote line:** the site's signature single-quote line is
  `#N +(score)-` with the score in **green when positive** (#2E7D32) and
  **red when negative** (#C62828); `+`/`-` vote controls sit inline on the
  same line (e.g. `#10479 +(53)-`). Rating lives on this quote line, not in
  the header bar.
- **Quote body:** monospace typewriter font, dark on light.
- **Footer / info bar:** bottom bar shows "Rendered in X.XXX seconds" on the
  left and the real database counts on the right — "N quotes approved; M
  quotes pending" (e.g. 14,355 approved / 1,218 pending in the current app DB).
- **Extras:** the site's famous **1337/leet text view** toggle (plus
  upside-down and alternating-case views in the app); [X]/flag behavior same
  QDB-engine family as bash.org (resubmit for review). Single-quote URL:
  `http://qdb.us/{number}` (Wayback ts `20200123054707`).
- **Queued tags:** the archive has a pending/submissions queue (1,218 pending
  quotes scraped from 77 archived `/queue` pages); queued quotes are stored
  with status='queued' and rating NULL — the app shows the "Queued" tag with
  the explanatory popup.

---

## 3. QDB.MIT.edu — single-quote page

Source: Wayback capture `20120220124500`; stylesheets "Chirpy! Default".

- **Page background:** white / very light; centered content column.
- **Header bar / branding:** rounded **maroon (#993333)** bar spanning the
  content width; site title **"qdb.mit.edu"** in gray (#999999) italic text,
  left-aligned inside the bar; subtle lighter edge highlight (pill look). No
  image logo.
- **Nav:** horizontal row directly under the bar, slash-separated:
  `/ Home / Browse / Random / Top / Bottom / Search / Tags / Stats / Submit /`
  (dark/plain links).
- **Quote header:** quote-number links **purple (#4D0099)**; upvote link green
  (#009900), downvote link dark red (#990000); quote header rows take a pale
  orange hover/highlight (#FFE9C9 / #F0D0A0 tones). Per-quote header layout
  (number/score placement): UNKNOWN — reconstructed notes cover the
  homepage/list look.
- **Quote body:** nearly-white bodies (#FFFCEC / #FFF3F3 tints) with thin
  borders; quote text is proportional sans (Verdana/Arial/Helvetica), NOT
  monospace. Homepage "Sample Quotes" box pale orange (#FFE9C9).
- **Footer / info bar:** "Powered by Chirpy! v0.4" credit and an "EDUbash
  Consortium" badge image; a bordered disclaimer box above the footer.
- **Extras:** "MIT's Funny Quotes" section headings; bold purple
  "SUBMIT YOUR FUNNY QUOTES!!" link in the left column.
- **Queued tags:** none observed.

---

## 4. QuoteIRC.com — single-quote page

Source: Wayback capture `20120101013439`; stylesheet `css/cssq.css`.

- **Page background:** white; centered container.
- **Header bar / branding:** white header. Left: speech-bubble logo (red
  #A52B2B-ish + orange/yellow overlapping bubbles) next to the wordmark
  **"IRC Quotes"** ("IRC" gray, "Quotes" brick red) with tagline **"You quote
  we vote!"** in small gray underneath. Right: small stats text ("Approved
  quotes: 608", "Pending quotes: 13") plus a bookmark/share button.
- **Nav:** dark gray gradient bar (#606060-ish) with white/light UPPERCASE
  links: HOME, ADD QUOTE, SEARCH, etc. A "REGISTER TODAY!" sidebar panel sits
  on the right of the page (not part of the quote card).
- **Quote header:** one line: brick-red **#number** link (#A52B2B), then
  "Posted by: *nick*" / "Posted on: *date*" in dark gray/black, and
  "Origin: *network* » *#channel*" (network/channel in brick red). Right
  side: "Quote rank: N" with small red down-vote and green up-vote icon
  buttons. Link hover shifts to blue (#2D75AC).
- **Quote body:** white with light-gray dividers; alternating content areas in
  very light gray (#F2F2F1 / #FAFAFA) and pale blue (#EFF3F7 / #F7FBFF); dark
  gray/black proportional sans text (Verdana/Arial).
- **Footer / info bar:** pale yellow (#FEEDBA) accent panels used for
  highlight boxes; standard footer text below (exact footer wording: UNKNOWN).
- **Extras:** the header's approved/pending stats line; sidebar panels are
  page chrome, not quote chrome.
- **Queued tags:** pending quotes counted in the header stats ("Pending
  quotes: 13"); no per-quote Queued tag on approved single-quote pages.

---

## 5. Quotes.BurntElectrons.org — single-quote page

Source: Wayback capture `20110903204331`; base Mash QDB CSS + red theme CSS.

- **Page background:** light gray; content panels layer white over it.
- **Header bar / branding:** wide **red gradient** header bar (deep red, darker
  at edges, rounded bottom corners, soft drop shadow) with the title **"the
  irc.mozilla.org qdb"** in large white lowercase text, left-aligned.
- **Nav:** BLACK uppercase links directly under the title inside the red
  header: HOME BROWSE RANDOM TOP BOTTOM SEARCH QUEUE TAGS STATS SUBMIT LOG IN.
  Right side of the header: small white site-stats/login text.
- **Quote header:** quote-number links red (#990000); visited darken to
  #660000; hover brightens to #CC0000; small gray submitted-by/date meta lines.
  Per-quote header arrangement (score/vote placement): UNKNOWN.
- **Quote body:** white beveled cards with thin gray borders and slightly
  rounded corners; quote text in **monospace** (Consolas/Monaco/monospace),
  dark gray/black on white — classic IRC-log look.
- **Footer / info bar:** footer carries the standard Mash QDB credits line
  (exact wording: UNKNOWN).
- **Extras:** the site offers several alternate color themes; the archive
  shows the red default. QUEUE nav item leads to the moderation queue.
- **Queued tags:** none on approved quote pages (queue is a separate section).

---

## 6. Warpdrive.se — single-quote page

Source: Wayback capture `20101006050354` (homepage shows the latest quote;
  no separate single-quote page needed).

- **Page background:** light gray (#E7E7E7). Content sits in a centered
  white/off-white framed panel with rounded corners and a thin gray border.
- **Header bar / branding:** no graphical logo. Top of the frame: welcome line
  **"Välkommen till warpdrive!"** in light gray italic, subtitle "Den Svenska
  Chatcitatdatabasen" beneath in small gray. Search box with "Sök" button at
  top right of the frame.
- **Nav:** pale-gray tabbed bar with cyan-blue (#0099CC) links, pipe-separated:
  `Hem | Senaste | Bläddra | <0 Slumpa >0 | Topp/Botten | Bubblare |
  Favoriter | Skräp | Lägg till … Logga in | Registrera` — repeated at the
  bottom of the content frame.
- **Quote header:** info line above the quote text: cyan-blue **#number** link
  (#0099CC), then small gray (~#777777) action links: "Begär omprövning av
  citatet" (request review), "+ -35 -" voting, "Favoriter 0",
  "Visa Kommentarer 4", "Mer info".
- **Quote body:** plain text blocks inside the white frame — no heavy card
  chrome; dark gray/black proportional sans (Trebuchet MS/Verdana/Arial) with
  IRC-style `<nick>` lines.
- **Footer / info bar:** "Senast inloggade:" (recently logged in) nick line
  below the quote, then footer line "warpdrive.se : Välkommen till ... -
  Tailsweep Analytics".
- **Extras:** Swedish-language UI throughout; comment counts and "more info"
  per quote.
- **Queued tags:** none observed (the "Bubblare"/"Skräp" sections are separate).

---

## 7. WQDB.org (WormsQuoteDatabase) — single-quote page

Source: user's screenshot (wqdb-original.png); Clint approved using the logo.

- **Page background:** white page; quote rows alternate white and light gray.
- **Header bar / branding:** white header with the pink Worms-game worm mascot
  on the left and the **"WormsQuoteDatabase"** wordmark in gray-gradient text
  (wqdb-logo.png cropped from the screenshot; Clint approved).
- **Nav:** dark navy blue bar (~#003366) with white links: Home | Latest |
  Top Rated | Bottom Rated | Browse | Random | Search | Add Quote; Login /
  Register on the right with icons. Content heading ("Browse") with sort line
  "Down [-] | [+] Up | Listing quotes from 1 to 30".
- **Quote header:** blue **#number** link, "- Rating N -" flanked by
  thumbs-down / thumbs-up / flag icons (vote controls are icon buttons).
- **Quote body:** dark text on the alternating row background; plain
  proportional font; rows alternate white / light-gray backgrounds.
- **Footer / info bar:** standard footer below the quote list (exact wording:
  UNKNOWN).
- **Extras:** the rating bar with thumbs + flag is the site's signature
  element; Login/Register icons in the nav.
- **Queued tags:** none observed.

---

## 8. xkcdb.com — single-quote page

Source: user's screenshot (xkcdb-original.png); Clint excluded the green
donation box ("Eww").

- **Page background:** light lavender/periwinkle gray (~#DCDCE6). Content
  area: white.
- **Header bar / branding:** gray "XKCDB" wordmark, xkcd-style stick figures
  (xkcdb-header-figures.png), tagline: "That's what SHE said. We have logs."
- **Nav:** dark blue links: About | Submit | New | Top (Recent) | Random >0;
  search box + "Go" button; orange RSS icon, banana icon, "$" icon on the
  right. Pager line: "← ← 1 of 137 → →" (centered above quotes).
- **Quote header:** blue **#number**, green up-arrow, (+up/-down) vote counts,
  red down-arrow — e.g. `#11194 ↑ (+858/-873) ↓`.
- **Quote body:** monospace font, black on white; no card chrome on the real
  site (plain page text).
- **Footer / info bar:** footer line below the quotes (exact wording: UNKNOWN).
- **Extras:** the xkcd-style header figures are header chrome (the app places
  them in the quote card's top bar per Clint's request); the green
  "Hi! Just so you know…" donation box must NOT be reproduced.
- **Queued tags:** none observed.

---

## 9. furcqdb / Mandaliet.com (Furcadia Quotes!) — single-quote page

Source: user's screenshot (furcqdb-original.png).

- **Page background:** black.
- **Header bar / branding:** **"FURCADIA QUOTES!"** in bright blue,
  right-aligned; no image logo.
- **Nav:** black bar with thin orange border; orange/peach links: Home /
  Show quotes: Latest (positive only), Highest scoring, Random / Search /
  Links / Submit a quote. "Next 50 ->" pager bar in the same bordered style,
  right-aligned.
- **Quote header:** white text "Quote #", quote number in orange,
  "Current score: N" with orange `+` / `-` vote links, then gray
  "Total votes: N  Submitted: YYYY-MM-DD HH:MM:SS" (real submitted timestamps
  per quote).
- **Quote body:** tan/beige card (~#D8CFC0) with thin orange border; dark text;
  plain (non-monospace) font.
- **Footer / info bar:** footer line below the pager (exact wording: UNKNOWN).
- **Extras:** the "positive only" latest filter; per-quote submitted
  timestamps are a distinctive element worth showing.
- **Queued tags:** none observed.

---

## 10. I-Rox.com — single-quote page

Source: user's screenshot (irox-original.png).

- **Page background:** white / very light gray.
- **Header bar / branding:** huge orange **"o·w·n·e·d"** wordmark (dotted
  separators) on the right, with a mock dictionary definition of "owned"
  beneath it ("1. a. To have or possess as property: owns a chain of
  restaurants...").
- **Nav:** orange/rust links: news / latest / random / browse / top 100 / add /
  search / store / garden gnomes / uk mobiles / dvorak / login. Old Google
  search box below the nav. Breadcrumb: "-> o·w·n·e·d > latest >" in orange.
- **Quote header:** "#4003 - 0 + [x]" in orange/rust — number, score,
  vote links, and the [x] flag element, all on the header line.
- **Quote body:** very light gray boxes with thin gray borders; dark text,
  plain font. Layout has a narrow empty left sidebar (light gray box) with
  quote cards on the right.
- **Footer / info bar:** footer line below the quotes (exact wording: UNKNOWN).
- **Extras:** the quirky store/gnome nav links are page chrome (not needed in
  a quote card); Google search box is page chrome.
- **Queued tags:** none observed.

---

## CrapShack (not a real site — layout per Clint's spec)

- No archived site exists (quotes come from Clint's local `crap.txt` IRC dump).
- Per Clint's 2026-09-15 directive: clone the app's Bash.org theme layout and
  structure, but with a **black page background**, **dark gray letters** for
  quote/body text, and **light blue headers** (quote headers / number bars).
  v1.53 maps this to deep-blue (#4AA3FF) header/footer bars with a dark
  (#141414) bordered body.

---

## Quick palette + font cheat sheet (for the redesign build)

| Site | Bar/header | Page bg | Body bg | Number | Body font | Rating display |
|---|---|---|---|---|---|---|
| bash.org | orange #FF9900 | white | white, bordered | orange #FF9900 | monospace | score + `+ - [X]` links (exact format UNKNOWN) |
| qdb.us | gold/amber #D4A017, brown text #3E2723; page navy header | light gray rows | light | bold `#N` + green/red score | monospace | `#N +(53)-`, inline `+`/`-` |
| QDB.MIT.edu | maroon #993333, gray #999999 title | white | #FFFCEC, thin border | purple #4D0099 | sans (Verdana) | up green #009900 / down dark red #990000 |
| QuoteIRC | white header, speech-bubble logo; dark gray nav gradient | white | white / #F2F2F1 / #EFF3F7 alt | brick red #A52B2B | sans (Verdana) | "Quote rank: N" + red-down/green-up icon buttons |
| BurntElectrons | red gradient, white lowercase title | light gray | white beveled card | red #990000 | monospace | layout UNKNOWN |
| warpdrive.se | welcome line (no logo); pale-gray tabs | gray #E7E7E7 | white frame, no card chrome | cyan #0099CC | sans (Trebuchet) | `+ -35 -` inline gray links |
| wqdb.org | white header + worm mascot; navy #003366 nav | white | alternating white / light gray | blue | sans | "- Rating N -" + thumbs/flag icons |
| xkcdb.com | lavender #DCDCE6 page, gray wordmark + figures | lavender #DCDCE6 | white | blue | monospace | `↑ (+up/-down) ↓` green/red arrows |
| furcqdb | black page, bright blue title right | black | tan #D8CFC0, thin orange border | orange | sans | "Current score: N" + orange `+`/`-`; gray "Submitted: YYYY-MM-DD HH:MM:SS" |
| I-Rox.com | orange "o·w·n·e·d" wordmark | white/light gray | very light gray, thin gray border | orange/rust | sans | `#N - 0 + [x]` orange |
| CrapShack | light blue headers | black | dark | light blue | monospace | (Bash clone) |
