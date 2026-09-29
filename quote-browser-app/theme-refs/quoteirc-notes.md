# QuoteIRC.com — theme reference notes

## Source capture
- Wayback Machine capture of `http://www.quoteirc.com/`, timestamp **20120101013439**
  (Jan 1, 2012). Stylesheet `css/cssq.css` recovered from capture **20110130073719**;
  the site logo image `images/ircq-logo.jpg` recovered from capture **20110130073843**.
- The PNGs in this folder are **reconstructed renders** of the archived HTML/CSS
  (rendered with WeasyPrint from the genuine archived files, logo composited from
  the archived image), not native browser screenshots. Colors and layout are
  faithful to the archived stylesheet.

## Background
- Page background is white; content sits in a centered container on white.

## Branding / header
- White header. Left side: the site logo — two overlapping speech bubbles
  (red `#A52B2B`-ish and orange/yellow) next to the wordmark **"IRC Quotes"**
  ("IRC" in gray, "Quotes" in brick red) with the tagline **"You quote we vote!"**
  underneath in small gray text.
- Right side of the header: small stats text — "Approved quotes: 608",
  "Pending quotes: 13" — plus a bookmark/share button.
- Below the header sits a dark gray gradient navigation bar (gray `#606060`-ish
  gradient) with light/white uppercase links: HOME, ADD QUOTE, SEARCH, etc.
- Note: the archived page also carries a large third-party top banner ad
  ("Daily Mission" game ad). That ad is **not** part of the site theme and should
  not be reproduced.

## Navigation
- Dark gray gradient bar with white/light uppercase links (Home, Add Quote,
  Search, and section links). Right side of the page has a sidebar with a
  "REGISTER TODAY!" panel.

## Quote headers
- One-line header per quote: brick-red **#number** link (`#A52B2B`), then
  "Posted by: *nick*" / "Posted on: *date*" in dark gray/black, and
  "Origin: *network* » *#channel*" with the network/channel in brick red.
- Right side of the header: "Quote rank: N" with small red down-vote and green
  up-vote icon buttons.
- Header row background is white/light with a thin divider; hover states shift
  links to blue (`#2D75AC`).

## Cards / body
- Quote bodies are white with light-gray dividers; alternating content areas use
  very light gray (`#F2F2F1` / `#FAFAFA`) and pale blue (`#EFF3F7` / `#F7FBFF`).
- Pale yellow accent (`#FEEDBA`) used for highlight panels.
- Quote text is dark gray/black, plain proportional sans.

## Fonts
- Verdana, Arial, Helvetica, sans-serif throughout.

## Approximate palette
- Brick red `#A52B2B` (links, quote numbers, logo "Quotes")
- Blue `#2D75AC` (link hover)
- Dark gray `#606060`-ish (nav bar gradient)
- Light gray `#DFDFDD` / `#D8D7D3` (borders, dividers)
- Pale yellow `#FEEDBA` (accent panels)
- Pale blue `#EFF3F7` (alt content areas)
- White `#FFFFFF` (page, header, quote bodies)
