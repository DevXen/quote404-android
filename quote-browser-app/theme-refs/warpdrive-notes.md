# Warpdrive.se — theme reference notes

## Source capture
- Wayback Machine capture of `http://warpdrive.se/`, timestamp **20101006050354**
  (Oct 6, 2010). Stylesheet recovered from capture **20100818130957**.
- The homepage itself shows the latest quote, so no separate quote page was needed.
- The PNGs in this folder are **reconstructed renders** of the archived HTML/CSS
  (rendered with WeasyPrint from the genuine archived files), not native browser
  screenshots. Colors and layout are faithful to the archived stylesheet.

## Background
- Page background is light gray (`#E7E7E7`). Content sits in a centered,
  white/off-white framed panel with rounded corners and a thin gray border.

## Branding / header
- No big graphical logo. The top of the content frame shows a welcome line
  **"Välkommen till warpdrive!"** in light gray italic, with the subtitle
  "Den Svenska Chatcitatdatabasen" (The Swedish Chat-quote Database) beneath it
  in small gray text.
- A search box ("Sök" button) sits at the top right of the frame.

## Navigation
- Pale-gray tabbed navigation bar with cyan-blue links (`#0099CC`), items
  separated by pipes:
  Hem | Senaste | Bläddra | <0 Slumpa >0 | Topp/Botten | Bubblare | Favoriter |
  Skräp | Lägg till  …  Logga in | Registrera
  (Swedish: Home, Latest, Browse, Random, Top/Bottom, Bubblers, Favorites,
  Trash, Add … Log in, Register).
- The same nav repeats at the bottom of the content frame.

## Quote headers
- Quote info line above the quote text: cyan-blue **#number** link (`#0099CC`),
  e.g. "#60529", followed by small gray action links — "Begär omprövning av
  citatet" (request review), "+ -35 -" voting, "Favoriter 0", "Visa Kommentarer
  4", "Mer info".
- Info text is approximately `#777777` gray.

## Cards / body
- Quotes are plain text blocks inside the white content frame — no heavy card
  chrome. Quote text is dark gray/black proportional sans, with IRC-style
  `<nick>` lines.
- Below the quote: a "Senast inloggade:" (recently logged in) line listing
  nicks as cyan-blue links.
- Footer line: "warpdrive.se : Välkommen till ... - Tailsweep Analytics".

## Fonts
- Trebuchet MS, Verdana, Arial, Helvetica, sans-serif throughout.

## Approximate palette
- Light gray `#E7E7E7` (page background)
- White / off-white `#FAFAFA`-ish (content frame)
- Pale gray `#E2E2E2` (tabs, boxes, borders)
- Cyan-blue `#0099CC` (links, quote numbers)
- Gray `#777777`-ish (quote meta text)
