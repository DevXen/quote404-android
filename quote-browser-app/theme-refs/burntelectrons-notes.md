# Quotes.BurntElectrons.org — theme reference notes

## Source capture
- Wayback Machine capture of `http://quotes.burntelectrons.org/`, timestamp
  **20110903204331** (Sep 3, 2011). Site title: **"The irc.mozilla.org QDB"**.
- Stylesheets: base Mash QDB CSS from capture **20070311020417** and the
  **red** theme CSS from capture **20110721054346** (the archived default theme;
  the site offered several alternate color themes, but red is what the archive shows).
- The PNGs in this folder are **reconstructed renders** of the archived HTML/CSS
  (rendered with WeasyPrint from the genuine archived files), not native browser
  screenshots. Colors and layout are faithful to the archived red-theme stylesheet.

## Background
- Page background is light gray; content panels layer white over it with subtle
  beveled/rounded edges (classic "Mash" QDB look).

## Branding / header
- Wide red gradient header bar (deep red, darker at edges) with the title
  **"the irc.mozilla.org qdb"** in large white lowercase text, left-aligned.
- The header has rounded bottom corners and a soft drop shadow separating it
  from the page.

## Navigation
- Black uppercase nav links directly under the title inside the red header:
  HOME BROWSE RANDOM TOP BOTTOM SEARCH QUEUE TAGS STATS SUBMIT LOG IN.
- Right side of the header shows small white text: site stats / login info.

## Quote headers
- Quote number links are red (`#990000`); visited links darken to `#660000`;
  hover brightens to `#CC0000`.
- Quote meta lines (submitted-by info, dates) are small gray text.

## Cards / body
- Quote cards are white panels on the light-gray page, with thin gray borders
  and slightly beveled/rounded corners.
- Quote text itself is rendered in a **monospace** font (Consolas/Monaco/monospace),
  dark gray/black on white — the classic IRC-log look.

## Fonts
- UI: Trebuchet MS, Verdana, Arial, Helvetica, sans-serif.
- Quote text: Consolas, Monaco, monospace.

## Approximate palette
- Red `#990000` (links, header accents)
- Dark red `#660000` (visited links)
- Bright red `#CC0000` (link hover)
- Light gray page background (white `#FFFFFF` content panels over it)
- White quote cards with thin gray borders
