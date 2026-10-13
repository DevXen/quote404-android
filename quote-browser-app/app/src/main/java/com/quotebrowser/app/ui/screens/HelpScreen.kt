package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

private data class ChangelogEntry(val version: String, val items: List<String>)

private val CHANGELOG = listOf(
    ChangelogEntry(
        "1.89.1", listOf(
            "Load .db files directly from the phone: Settings → Databases (and the first-run setup) has a \"Load a .db file\" button — pick a database file you've saved on the phone and, if its name matches one of the online databases, it installs as that database, no download needed (works offline too). Any other valid quote database is added as your own, just like importing before"
        )
    ),
    ChangelogEntry(
        "1.89.0", listOf(
            "The quote databases are downloads now, not part of the app — the app itself is about 50 MB smaller. On first run you pick which databases to install; afterwards they check for updates on every startup and update themselves in the background. Manage them any time in Settings → Databases: install, update, re-download, or remove one (removing also removes the favorites and categories stored inside it)",
            "Your own databases live in the same place: Settings → Databases also covers My Databases, importing a chat or text file into a database you pick (or a brand-new one), and importing a .db file — everything personal is in the one screen"
        )
    ),
    ChangelogEntry(
        "1.88.40", listOf(
            "Friends database fix: conversations are now separated into individual chat bubbles — one bubble per person per turn — and Clint's own lines are labeled with his name instead of <Me>"
        )
    ),
    ChangelogEntry(
        "1.88.39", listOf(
            "The Friends database now ships inside the app — no import needed. It appears in the database picker on its own with the Friends theme already applied. If you imported Friends.db earlier you'll see two entries; remove the imported copy from the database manager"
        )
    ),
    ChangelogEntry(
        "1.88.38", listOf(
            "New Friends theme: sunset chat bubbles on deep plum — your own sunset-gradient bubbles, amber nick labels for everyone else, auto-applied to the Friends database"
        )
    ),
    ChangelogEntry(
        "1.88.37", listOf(
            "Scrolling fix: lists no longer teleport on an accidental fling — a speed ceiling now caps runaway flings from touch-sensor noise on lift-off, while deliberate fast flicks still scroll just as fast as before"
        )
    ),
    ChangelogEntry(
        "1.88.36", listOf(
            "My Databases: create as many of your own quote databases as you like — Settings → My Quotes card → \"My databases\", then tap + to name a new one. Each database gets the full add/edit/delete quote management, shows up in the database switcher, and is included in Random and Quote of the Day. Rename or delete a database from its ⋮ menu",
            "Nick colors: the \"nick1 | nick2:\" compound speaker format (bash.org #448982 style, e.g. \"CwD | Asbestos: calculus is ...\") is now recognized — both nicks get their own colors and stay tappable, and the \"strip usernames\" copy/export option strips the whole \"nick1 | nick2:\" prefix"
        )
    ),
    ChangelogEntry(
        "1.88.33", listOf(
            "The Quote404 logo now sits at the very top of every SMS Xtract theme card — the theme's own chrome (Windows 95 title bar, Sega stripes, Nokia band, mastheads) starts below it instead of above it, and the quote number moved to the top-right corner",
            "The 24 SMS Xtract themes were rebuilt to actually read as their themes: Sega gets its red/white/black stripes, 16-BIT BLAST PROCESSING tagline, and checkerboard versus frame; Windows 95 is a full window with raised 3D border, blue title bar, and status bar; Nokia has its blue band and phone-screen inset; Game Boy has the gray bezel, DOT MATRIX strip, and power LED — and the equivalent treatment for the rest",
            "Share picker: the \"From SMS Xtract\" tiles now show true miniatures of each theme's real card (like the novelty tiles do) instead of gradient placeholders, and tiles are sorted alphabetically within each section with the default pinned first"
        )
    ),
    ChangelogEntry(
        "1.88.32", listOf(
            "Quote404 logo header is now always on for shared/exported images — no more toggle — and every exported card gets a small black footer strip with www.Quote404.com in the bottom-right corner",
            "24 themes ported from SMS Xtract into the share picker's new \"From SMS Xtract\" section (Newspaper, Terminal, Comic Book, Movie Script, Case File, Telegram, Typewriter, Heavy Metal, plus Modern, Samsung Messages, Nokia, BlackBerry, Mono 1999, Windows 95, Pager, IRC, Game Boy, Ransom Note, NES, SNES, Sega, Atari, Wanted, and Chalkboard) — the share picker is now organized into collapsible Site theme / Designed / From SMS Xtract / Novelty sections",
            "Per-theme nick colors: every style tile in the share picker has a small palette button that gives that card style its own nick color palette (or keeps following the global nick color theme)",
            "AIM, ICQ, and MSN chat cards: per-message headers now show just the nick — the fake (0:mm) timestamps are gone, and turns without a nick get no header instead of an invented \"Friend\" name. Each theme also has a \"Remove nicks from message text\" toggle (same palette button) that strips the redundant speaker prefix from each message while keeping every turn's own block, header, and color",
            "Favorites cards now have a share button that opens the full share picker",
            "ASCII novelty theme: the title row is built with a real row layout now, so the frame lines up on every screen"
        )
    ),
    ChangelogEntry(
        "1.88.31", listOf(
            "Share-as-image picker: new \"Quote404 header\" toggle — turn it off and the shared image is just the card, without the Quote404 logo banner across the top. The preview always shows exactly what gets shared"
        )
    ),
    ChangelogEntry(
        "1.88.30", listOf(
            "Share-as-image picker: novelty theme tiles now show the whole card scaled down so the quote is never cut off, and all 19 tiles render up front instead of some getting stuck on the placeholder"
        )
    ),
    ChangelogEntry(
        "1.88.29", listOf(
            "Share-as-image style picker: novelty theme tiles now render the actual quote you're sharing in each theme — no more sample-quote placeholders — and the tiles are compact squares instead of long strips, so the picker is much shorter to scroll"
        )
    ),
    ChangelogEntry(
        "1.88.28", listOf(
            "Three new novelty themes: AIM (classic AOL Instant Messenger window — blue gradient title bar, red and blue nicks, Warn/Block/Add Buddy/Talk/Get Info row, big Send button), ICQ (light-blue client with the flower logo, timestamped blue/orange nicks, avatar panel, emoticon input), and MSN Messenger (butterfly logo, “says:” lines, display pictures on both sides, Invite/Send a file/Video/Games toolbar) — 19 novelty themes total"
        )
    ),
    ChangelogEntry(
        "1.88.27", listOf(
            "Two new novelty themes: Messenger and Messenger Dark — Facebook Messenger chat cards with the signature blue-to-purple-to-pink gradient outgoing bubbles, gray incoming bubbles, and an “Active now” header, in light and dark mode — 16 novelty themes total"
        )
    ),
    ChangelogEntry(
        "1.88.26", listOf(
            "ASCII theme fix: the box frame's right border now lines up with the corners — the title line was drawn in a bolder weight than the frame, which shifted its metrics"
        )
    ),
    ChangelogEntry(
        "1.88.25", listOf(
            "Four new novelty themes join the lineup — ANSI (BBS ANSI-art with bright multi-colored text and block-element frames), ASCII (classic .nfo textmode: light-gray monospace in an ASCII box, no color), Matrix (faint green katakana rain behind bright green text), and Green Terminal (emulator window with title bar and block cursor) — 14 novelty themes total",
            "Share/Export style picker: novelty theme tiles now show true miniature previews of each theme's real card — DOS, macOS traffic lights, Macintosh pinstripes, iMessage bubbles, BSOD, mIRC, ANSI colors, ASCII box frames, Matrix rain, Green Terminal, and the rest — instead of generic swatches, so you can see what you're picking",
            "Tiles render once when the picker opens and follow the “Colorize nicknames” toggle; tapping a tile still shows the full true-export preview before sharing or saving"
        )
    ),
    ChangelogEntry(
        "1.88.24", listOf(
            "Share/export images now render novelty themes exactly as they look on screen: iMessage and Google Messages chat bubbles, the Macintosh pinstripe title bar, macOS traffic lights, the BSOD :(, mIRC timestamps and nick colors, terminal title bars and cursors, DOS and Linux prompts, and the green phosphor glow",
            "The Share/Export image style picker now lists all ten novelty themes (DOS, macOS, Macintosh, iMessage, Google Messages, BSOD, Linux, mIRC, Terminal, Green Monochrome) alongside “Quote theme” and the designed styles — pick one, preview the true export, then share or save",
            "Exports keep the slim Quote404 logo plate, your “Break between quotes” spacing, translations, kid-safe masking, and nick colors"
        )
    ),
    ChangelogEntry(
        "1.88.23", listOf(
            "New quote-card themes: DOS, macOS, Macintosh, iMessage, Google Messages, BSOD, Linux, mIRC, Terminal, and Green Monochrome — each with its own full card presentation (chat bubbles, terminal prompts, BSOD, window chrome). Pick one per database (Database themes → Site layout) or as the global theme; “Break between quotes”, translations, and nick colors keep working inside all of them",
            "Theme picker, organized: built-in themes are now grouped into labeled collapsible sections — App, Chat, Terminals, System, Quote databases, Dark, Light — alphabetical within each group, so the list stays compact",
            "Per-database theme pickers (Settings and the quick theme dialog) group themes into labeled sections too"
        )
    ),
    ChangelogEntry(
        "1.88.22", listOf(
            "“Break between quotes” now does what it says: it controls the blank lines between the turns of a quote (Settings → Appearance → Display; Off / Small / Medium / Large). Off shows quotes exactly as stored; Small, Medium, and Large add 1, 2, or 3 blank lines between turns. The old extra space between quote cards is gone"
        )
    ),
    ChangelogEntry(
        "1.88.19", listOf(
            "New: “Break between quotes” — extra breathing room between quote cards on list screens (Settings → Appearance → Display; Off / Small / Medium / Large, off by default)",
            "Random: pressing Enter/Done in the Count box now commits the count and shuffles with it, then dismisses the keyboard so the quote list stays visible",
            "Help: the old always-visible Diagnostics card is now its own collapsed section — with Share and the new Save (to Downloads) for the startup log, the last crash report, per-database open timings for the latest launch, and app/device info",
            "Startup log: now a ring buffer (last 500 lines) written on a background thread, so logging can't slow startup or grow forever"
        )
    ),
    ChangelogEntry(
        "1.88.4", listOf(
            "Hotfix: if v1.88.2's broken databases left a bad copy on your device, opening that database could still crash with “Pre-packaged database has an invalid schema” even on v1.88.3. The app now repairs the on-device copy's schema itself right before opening it — no reinstall or data clearing needed, and your categories are preserved"
        )
    ),
    ChangelogEntry(
        "1.88.3", listOf(
            "Hotfix: opening a bundled database for the first time could crash with “Pre-packaged database has an invalid schema” — ten of the bundled databases were still missing the status column the app requires (the v1.88 bash.im fix only covered bash.im). All bundled databases now ship with the correct schema, so first opens validate cleanly"
        )
    ),
    ChangelogEntry(
        "1.88.2", listOf(
            "Tap a database's name on any quote card header to jump to Browse filtered to that database — the name is tappable on every site theme and the modern card; tapping anywhere else on the card still opens just that quote",
            "“On this day” now means what it says: quotes genuinely posted on today's date in past years, from bash.im's real posting timestamps. The old deterministic “deep cuts” randoms are gone, and the qdb.us Wayback captures moved to their own clearly-labeled section — those are archive dates, not posting dates",
            "Fixed “Dismiss and retry app” getting stuck on an empty crash viewer: dismissing now restarts the app in a fresh process, so a cleared report can't bounce the app back to the crash screen with nothing to show"
        )
    ),
    ChangelogEntry(
        "1.88.1", listOf(
            "Hotfix: v1.88 crashed on launch with a database error — the new bash.im database shipped without the status column the app requires, so it failed validation on open. The database is rebuilt with the correct schema; the app opens normally again"
        )
    ),
    ChangelogEntry(
        "1.88", listOf(
            "New database: bash.im — the Цитатник Рунета archive: all 81,497 quotes from 2004–2020, with fresh idiom-aware English translations (English stays the default; the original/translated toggle is unchanged). The minimalist white site theme matches the site's 2019 redesign, and each quote links to its canonical bash.im/quote page",
            "Quote numbers on bash.im cards link straight to the quote's page on bash.im"
        )
    ),
    ChangelogEntry(
        "1.87.2", listOf(
            "Fixed the white page behind quotes opened from notifications: “Black background outside quotes” is now on by default and actually paints the whole page black (it used to leave the site's own page color, like qdb.us white, showing around the card)"
        )
    ),
    ChangelogEntry(
        "1.87.1", listOf(
            "Fixed a startup crash loop: if a previous run left a crash report behind, scheduled background jobs could crash the app before it opened. The app now recovers instead of looping"
        )
    ),
    ChangelogEntry(
        "1.87", listOf(
            "Export-style plugins: HTML export themes now install as packs — pick one in Export → HTML → Export style (next to “Default (active quote layout)”). The style's CSS layers on top of your active quote layout for every HTML export (scope, per-category, and the export tray)",
            "One-tap plugin updates: packs that declare an update address in their manifest now show a “Check for updates” button on the Plugins screen — one tap downloads, validates, and installs the newer version. If the update asks for new permissions, they're shown before you apply; the pack's on/off state survives the update",
            "Voice packs: extra read-aloud voices now install as packs — they join the Gemini and built-in voice lists in Settings → Read aloud (default voice, narrator voice, and the per-speaker picker). Gemini pack voices alias real cloud voices; built-in pack voices are pitch/rate presets for your device's text-to-speech",
            "Per-plugin settings: packs that ship their own options now get a “Settings” button on the Plugins screen with their own settings page (switches, text fields, and dropdowns)",
            "warpdrive.se retranslated: all 6,025 quotes now use fresh idiom-aware English translations that replace the old machine translations (English stays the default; the original/translated toggle is unchanged)",
            "Repaired Swedish originals: the 5,859 warpdrive.se quotes whose Swedish text was corrupted with � characters now show the repaired Swedish",
            "Faster startup: the bundled-database refresh now runs in the background instead of blocking the first frame — the app opens immediately on upgrade and the refreshed data lands a moment later"
        )
    ),
    ChangelogEntry(
        "1.86.1", listOf(
            "Hotfix: v1.86 could never get past a black screen on launch — a bad reference in the new per-category color code sent the app into an instant stack overflow the moment the first quote card tried to render. Fixed; the app opens normally again"
        )
    ),
    ChangelogEntry(
        "1.86", listOf(
            "Quote text colors are now per category: Settings → Quote text has separate on/off switches and color pickers for speaker names, message text (the words someone says), action lines, timestamps, and other lines (narration, notices, joins/parts — anything that isn't speech or an action). No hex codes to type — every color is a visual picker",
            "HTML export keeps your colors: exported pages now color quote text by category (speaker names, messages, actions, timestamps, other lines) with CSS classes custom layouts can retarget — .qb-said-name, .qb-said-text, .qb-action-line, .qb-timestamp, .qb-other-line",
            "The export queue is now the Export Tray: stage quotes with “Add to export tray”, reorder them with the up/down arrows (exports follow tray order), and share the whole tray as text right from the tray screen. The tray keeps your quotes across restarts until you clear it",
            "Speaker voices are on for everyone: the per-speaker read-aloud voices switch on automatically once for existing installs (new installs already had them). You can still turn them off in Settings"
        )
    ),
    ChangelogEntry(
        "1.85", listOf(
            "Quote layouts: quotes now have a layout (arrangement of the card's elements) separate from the theme (colors and fonts). Six premades ship with the app — Classic (exactly the old card, the default), Minimal, Poster, Index Card, Newspaper, and Chat — and picking one changes the cards everywhere in the app",
            "Settings → Quote layouts: pick the default layout, override it per database, and open the custom HTML/CSS editor",
            "Custom styles: write your own per-quote HTML with {{tokens}} ({{quote_text}}, {{author}}, {{quote_number}}, {{site_name}}, {{source_url}}, {{date}}, {{rating}}, {{categories}}), style it with your own CSS, preview it live, and save it — it joins the layout picker",
            "HTML export: new \"Web page (.html)\" export format. The exported page uses your active layout, and the export dialog offers toggles for the web address link, rating, date, categories, and site branding",
            "Layout plugin packs: a new pack type — a layout.json plus optional style.css — installs through the same Plugins flow and its layouts appear in the picker. Layout packs need no permissions",
            "Read aloud now gives every speaker their own voice by default: each distinct nickname is assigned a voice deterministically — the same nickname always gets the same voice — and the voices switch at every turn. Turn the \"Speaker voices\" switch off to go back to one narrator",
            "Read-along follows the same voices on multi-speaker quotes: each turn is read in its speaker's voice while the word highlighting tracks normally",
            "New \"nick |\" speaker format: lines like \"kirch | hello\", \"+janitor | ...\", and \"@kirch | ...\" are now recognized everywhere nicknames are — card coloring, export, search, and read-aloud voices"
        )
    ),
    ChangelogEntry(
        "1.84", listOf(
            "The search field and Count box are rebuilt on BasicTextField with exact vertical centering — the Material text field kept shaving the tops of the letters at every size, so the fields now draw their own pills and center the text themselves"
        )
    ),
    ChangelogEntry(
        "1.83", listOf(
            "Plugin system: third-party quote packs and theme packs now load as first-class citizens — quote packs appear as real databases in the switcher (Browse, Search, Random, Favorites, History all work), theme packs appear in the theme picker and per-database themes",
            "New Plugins screen (Settings → Plugins): install packs from a .zip with an install-time approval (name, author, version, type, permissions, checksum), enable/disable packs, and uninstall them cleanly",
            "The plugin architecture covers six pack types: quote packs and theme packs load now; export styles, game packs, voice packs, and video backgrounds are reserved so they slot in later without redesigning anything",
            "Packs are data only — no code ever runs from a pack — and a malformed pack is skipped with a note instead of crashing the app. Two sample packs are bundled to show how packs work",
            "How to install a pack: Settings → Plugins → Import pack (.zip), then approve what it declares. Pack authors: see PLUGIN_SPEC.md for the manifest format"
        )
    ),
    ChangelogEntry(
        "1.82", listOf(
            "The search field and Count box text is a step smaller (12.sp) so it renders fully inside the compact pills — the tops of the letters were still getting shaved at the old size"
        )
    ),
    ChangelogEntry(
        "1.81", listOf(
            "The search field and the Random page's Count box now use smaller text that fits inside the compact pills — the text was clipped and unreadable at the old size",
            "The search row and the Random page's control row are tighter (48.dp rows, less padding) so they sit closer to the top bar and take up less space"
        )
    ),
    ChangelogEntry(
        "1.80", listOf(
            "The search row and the Random page's Count / Shuffle / Options row are now the same compact pill size as the top bar's database / Categories pills — 40.dp controls centered in the 56.dp row"
        )
    ),
    ChangelogEntry(
        "1.79", listOf(
            "Fixed: the top bar's database / Categories pills were vertically squished, with their labels spilling out of the pill — the top bar was rebuilt so the pills always render at their full height with readable labels (the v1.77 compact bar didn't leave them enough room)"
        )
    ),
    ChangelogEntry(
        "1.78", listOf(
            "\"Explain this quote\" is now \"Explain / Roast / Rhyme\" — the same dialog offers a savage-but-playful roast of the quote and a rhyming-couplet song-verse rewrite, alongside the plain-English explanation",
            "Explanations are still saved on your device and re-open instantly; roasts and rhymes are fresh every time — never cached",
            "New: \"My Quote DNA\" — a fun taste profile computed on your device from your favorites and votes, with a shareable card (Favorites screen, flask icon)",
            "New: \"Read along\" mode — karaoke-style reading that highlights each word of the quote as it is spoken, with play/pause, stop, and 0.75x / 1x / 1.25x speed (any quote menu, next to \"Read aloud\", and on the quote detail screen)",
            "New: Quote alarm — at a time you choose each day, your phone reads a random favorite quote aloud, even with the app closed (Settings → Quote alarm). No favorites yet? It reads a random quote instead"
        )
    ),
    ChangelogEntry(
        "1.77", listOf(
            "Top bar: the database / Categories pill row is now the same compact height as the search row, with readable text throughout",
            "Fixed: the Search button and the search field are now exactly the same height on the Search tab (the button used to sit shorter than the field)",
            "The database pill is never cut off — it always shows its full name, and the Categories pill is the one that shrinks instead",
            "Fixed: searching with \\\"All databases\\\" selected now shows every result with its real database's site theme (recent results used the wrong or no theme)",
            "Fixed: Favorites and History in the \\\"All databases\\\" view now show quotes from every database, each with its true source theme — tapping one opens the right quote",
            "Clearing History in the \\\"All databases\\\" view now clears every database's history, not just the first one",
            "Favorites: the pin button moved into the quote card header (next to the flag icon), so cards are full-width again — the flag icon is gone on Bash.org cards because [X] does the same thing",
            "Favorites: the thumbs up / down vote buttons now work on favorite quotes too",
            "Bash.org cards: the + / - vote links now always show, sitting just left of the quote number, and the score always shows (0 when a quote has no rating yet)",
            "Every database's card now shows its score as 0 instead of hiding it when a quote has no rating yet"
        )
    ),
    ChangelogEntry(
        "1.76", listOf(
            "Fixed: the search bar's \\\"Search\\\" text was cramped and clipped, and the bar rendered shorter than the Random page's Count / Shuffle / Options row — both rows are now the same height with fully readable text",
            "Fixed: the Categories pill in the top bar could be squeezed into a clipped \\\"Catego\\\" — the database pill now gives up width first, so Categories always shows in full"
        )
    ),
    ChangelogEntry(
        "1.75", listOf(
            "Saved explanations: \"Explain this quote\" results are now cached on your device — re-opening an explanation is instant and free",
            "Quote cards show a purple explanation icon when a quote has a saved explanation — tap it to view",
            "Search: new \"Has explanation\" filter lists only quotes with a saved explanation, like \"Has audio\" does for recordings",
            "Export: the PNG \"Designed image…\" button offers the exact same card styles as Share (Quote theme, Quote404, Midnight, DevXen…), with preview before saving"
        )
    ),
    ChangelogEntry(
        "1.74.10", listOf(
            "Explain quote: fixed the \"Gemini said no (HTTP 404)\" error — Google retired the gemini-2.0-flash model; the app now uses the current gemini-3.6-flash model for explanations and translations"
        )
    ),
    ChangelogEntry(
        "1.74.9", listOf(
            "Audio export: single-quote M4A export now uses the Gemini voices already saved for the quote (per-nick voices), with options to announce nicknames (\"X says:\" / \"Y replies:\") and include the quote number or not",
            "Favorites: quote cards now show the correct database site themes (fixed \"All databases\" mode showing the default theme)",
            "Quote cards: the audio icon now only shows when a real saved recording exists on the device (validated, not just listed in the manifest)"
        )
    ),
    ChangelogEntry(
        "1.74.8", listOf(
            "Saved audio: fixed a bug where a quote's recorded audio wasn't saved correctly — a quote only shows as having audio when the actual recording file is really on the device",
            "Saved audio: broken or missing recordings are now cleaned up instead of pretending a quote has audio when it doesn't",
            "Search bar is back to its compact size, and the Random page's Count / Shuffle / Options row matches it"
        )
    ),
    ChangelogEntry(
        "1.74.7", listOf(
            "Quote card menus: new Read aloud option — plays the quote through the audio player without opening the detail screen",
            "Single-quote Export: MP4 and M4A exports are back with real rendering — MP4 gets Narrate, spoken-word highlighting, and animated background choice; M4A exports real narrated audio",
            "Single-quote Export: PNG export restored with the database's site background, TLD toggle, and the Colorize nicknames option",
            "Search filters: \"Has voice\" is now \"Has audio\" so it's clear it means a saved recording",
            "Random: the Count box now shows the number properly, and the search bars' buttons match the field height",
            "Logo watermark plate: tighter crop, less empty space, bigger logo"
        )
    ),
    ChangelogEntry(
        "1.74.6", listOf(
            "Fixed: the Cast size filter could still come up empty on some devices — the one-time database refresh now reruns and retries properly instead of silently giving up",
            "Fixed: narrated audio export could write a tiny broken .m4a when text-to-speech produced no audio — it now fails with the real reason instead, and no empty ghost files are left behind"
        )
    ),
    ChangelogEntry(
        "1.74.5", listOf(
            "Regex search: flip the Regex chip in the search filters and your query becomes a regular expression (case-insensitive) — try ^never.*again\$, <[^>]+>, or (cat|dog)",
            "Narrated audio export: export any quotes as an .m4a audiobook read aloud with your device's default text-to-speech voice"
        )
    ),
    ChangelogEntry(
        "1.74.1", listOf(
            "Random: the Options menu now has its own Maturity and Cast size filters — Shuffle finally listens to them (each pick re-rolls)",
            "Random: the Count box shows the number properly instead of hiding it behind the label"
        )
    ),
    ChangelogEntry(
        "1.74", listOf(
            "Fixed: the Maturity and Cast size filters did nothing on some devices — the app now backfills the content ratings into existing databases on first launch",
            "Search filters: the Maturity and Cast size pills now wrap instead of clipping off-screen, and the doubled-up labels are gone",
            "Quote pings grew up: new Notification filters section with its own maturity ceiling (follow the app's or pick your own)",
            "Work hours mode — set your days and hours (overnight shifts work), and pings stay Safe for Work while you're on the clock",
            "Ping moods and categories: only get pinged with the kinds of quotes you pick",
            "Minimum rating for pings, plus 'don't repeat' — quotes you were already notified about stay quiet for 7–90 days",
            "The Next button on a ping now uses the exact same filters as the ping itself"
        )
    ),
    ChangelogEntry(
        "1.73", listOf(
            "Voice search: tap the mic in any search box and say it — the transcription runs the search",
            "The expand arrow on the search bar now shows a badge with how many filters are active",
            "Search autocomplete: type two letters to get matching nicks (tap = their page) and categories (tap = filter and search)"
        )
    ),
    ChangelogEntry(
        "1.72.4", listOf(
            "Random: the Count / Shuffle / Options row is now the same height as the search box, and tucked up right under the search bar",
            "Fixed: the 'More like this' section was still showing on quote detail — tapping a quote now shows just the quote, nothing else"
        )
    ),
    ChangelogEntry(
        "1.72.3", listOf(
            "Random: the shuffle button just says \"Shuffle\" on one line now",
            "Global search: placeholder shortened to \"Search…\" so it always fits the compact bar on one line",
            "Quote detail: removed the \"Similar\" button — tapping a quote just shows the quote, no more \"More like this\" popup",
            "Search: slimmer top — the syntax tip row is gone (it's in Help), Save moved into the Filters header, and the Person jump lives inside the expanded filters so quotes are visible sooner"
        )
    ),
    ChangelogEntry(
        "1.72.2", listOf(
            "Fixed: mood chips in the search panel didn't respond to taps — they now toggle on tap (with a checkmark + highlight) and long-press still shows the categories each mood covers",
            "Fixed: the global search bar's placeholder wrapped to two lines — it stays on one line now, so the bar is the compact size you see while typing"
        )
    ),
    ChangelogEntry(
        "1.72", listOf(
            "New: a search bar now sits just under the top bar on every page (except Search itself, which has its own) — type, hit Search, and the Search page opens with your query",
            "The search bar has an expand menu next to it with the full filter set — moods, sort, rating, maturity, cast, exclusions, database scope, notes, voice, length — the same panel the Search page uses, so filters match everywhere",
            "Search: length moved inside the collapsible Filters section where it belongs, and joins the one-line summary, Reset, and the empty-state hints"
        )
    ),
    ChangelogEntry(
        "1.71", listOf(
            "Search: mood buttons are now toggle filters — tap to add a mood's categories, tap again to remove them. Stack several moods at once, and they combine with your query, database scope, and every other filter",
            "Search: long-press a mood button to see exactly which categories it covers in the current database",
            "Search: the filter stack now collapses under a “Filters” header so results are visible without scrolling — the collapsed header shows a one-line summary of what's active, and tapping Search auto-collapses it",
            "Search: new Reset button in the Filters header clears every filter (including moods) with one tap, keeping your query",
            "Search: results now show a count line, and an empty search tells you which active filters might be hiding everything",
            "Top bar: the categories pull-down is now simply labeled “Categories” (no icon) on Browse, Random, and Search — it still tints when a filter is active",
            "Random: moods in the Options pull-down are toggle filters too — check several at once and each toggle re-rolls the deck; the menu stays open so you can flip through vibes"
        )
    ),
    ChangelogEntry(
        "1.70", listOf(
            "Search: mood buttons now stack with your query, database scope, and every other filter instead of replacing them — tap “Geek me out” with “linux” typed and you get linux-flavored geek quotes",
            "Random: the six mood filters moved into the Options pull-down — pick one and the deck re-rolls in that vibe; the menu stays open so you can flip through moods"
        )
    ),
    ChangelogEntry(
        "1.69", listOf(
            "Search: new blue Search button next to the query field — it runs the search on your current filters with no query typed, so every filter (maturity, cast size, exclusions, rating, voice-only, categories) can pull up results on its own",
            "Search: three new mood buttons — “Geek me out”, “Warm my heart”, “Roast me” — alongside Make me laugh, Make me think, and Disturb me",
            "Search: typing a query now retires the active mood preset instead of stacking on top of it"
        )
    ),
    ChangelogEntry(
        "1.68", listOf(
            "Top bar: the categories picker is now a labeled “Categories” pull-down next to the database pill on Browse, Random, and Search; it tints when a filter is active",
            "Random screen: the top row is now the Count box, a wide blue “Shuffle quotes” button that fills the space, and a labeled Options pull-down; the action icons sit centered on the row underneath"
        )
    ),
    ChangelogEntry(
        "1.67", listOf(
            "Categories moved to the top bar: the picker now sits next to the database pill on Browse, Random, and Search — it tints when a filter is active, instead of living inside each screen",
            "Random screen: new two-row header — Shuffle, the Count box, and a ⋮ menu on the first row; the action icons (endless deck, flashcards, rate, read aloud, radio) on their own row right underneath",
            "Random screen: the old spelled-out Options section is gone — Endless deck, Flashcards, Rate, Read aloud, and Hide read now live in the ⋮ menu",
            "Random screen: the ⋮ menu has a new Export video — the current set joins the export tray and Export opens with the slideshow MP4 format preselected"
        )
    ),
    ChangelogEntry(
        "1.66", listOf(
            "Search history is now a full snapshot: every search keeps its query plus all filters (database, categories, exclusions, maturity, cast size, rating, voice-only, length). Tapping a history entry re-runs that exact search; entries can be deleted individually",
            "Named saved searches keep the same filters too — maturity, cast size, excluded categories/levels/sizes, and voice-only travel with the saved query",
            "Mood buttons on Search: “Make me laugh”, “Make me think”, “Disturb me” — one tap browses matching quotes with no query typed. Moods always obey your global maturity ceiling",
            "Cross-database search collapses duplicates: when the same quote lives in more than one database you see it once, with “also in …” naming the other databases",
            "“Explain this quote” has a new inline button on the quote detail screen, alongside the long-press menu option (needs the Explain feature toggle in Settings → Features)",
            "My Collections: named quote lists — create them from the ⋯ menu, add quotes from the detail screen's “Add to list” button or the long-press menu, reorder or remove items, play a collection as a read-aloud playlist, or send it to the export tray",
            "Quote Arena has a new Tournament button: an 8-quote single-elimination bracket",
            "Tournament and game quote pools now respect your global maturity ceiling",
            "“On this day” now says what it covers: real on-this-date quotes from qdb.us, a daily deep cut from each database without dates, and quotes you read or favorited on this date in past years"
        )
    ),
    ChangelogEntry(
        "1.65", listOf(
            "Content maturity ratings: every quote in every database is now rated Kid Safe / Clean / PG-13 / Rated R / NC-17, with a separate Spicy flag for FMyLife-style sexual content",
            "Global maturity ceiling in Settings → Reading → Content maturity: caps what Browse, Search, Random, Radio, the widget, games, and the quote of the day can show — nothing above your ceiling appears anywhere, and \"Show anyway\" banners can't lift it either",
            "Browse and Search filters: per-screen Maturity and Cast size (single person, multiple people, exactly 2/3/4/5 people) pull-downs; a screen's maturity pick can only narrow the global ceiling, never widen it",
            "Maturity levels: PG-13 and Safe for Work both cap at PG-13, but Safe for Work additionally screens out anything flagged Spicy",
            "Search exclusions: a new Exclude section keeps chosen categories, exact maturity levels, and cast sizes out of the results",
            "Categories are now grouped into collapsible sections (Tech & geek, Life, Entertainment, Humor, Maturity, Faith & thought, Length, My categories) on the Categories screen and in every category picker — tap a section header to collapse or expand it; the header checkbox checks the whole group at once",
            "Voice recordings: re-recording from one nick's page now regenerates only that nick's audio, even when another nick's voice assignment changed too"
        )
    ),
    ChangelogEntry(
        "1.64.2", listOf(
            "Voice recordings: Regenerate now makes the new audio right away — no more waiting for the next play. A progress bar shows it working, and the old audio is kept as an older version",
            "Speaker voices: tap the play button next to any voice to hear a sample before you assign it (works for Gemini and built-in TTS voices)",
            "Speaker voices: nick pages have a new \"Re-record all with this voice\" button — after changing a speaker's voice, re-record every saved quote they speak in, with progress and cancel",
            "Reading stats: new Top speakers and Top categories sections (tap a speaker to open their page), plus favorites and voice-recordings tiles",
            "Quote view: a \"More like this\" section under the quote shows the most similar quotes as tappable cards"
        )
    ),
    ChangelogEntry(
        "1.64.1", listOf(
            "Fixed nick taps: tapping a nick's name on a quote card now opens the nick page. The name tags were being attached as empty annotations, so taps never matched — this also fixes the previously dead nick taps on the single-quote view",
            "Speaker voices: the search box on the Manage speaker voices screen now searches every nick, not just the 300 most-quoted — rare nicks with only one or two quotes are findable now"
        )
    ),
    ChangelogEntry(
        "1.64", listOf(
            "Categories: 16 new ones — content ratings (Clean, Kid safe, PG, PG-13, R, X, NC-17; every quote is tagged with exactly one), Stupidity, separate Movies and TV categories, plus Tech, Science, Puns, Insults, Politics, and Philosophy. Filter any list by them just like the existing categories",
            "Nick pages: tapping a nick's name on any quote card now opens their page — the tap wasn't doing anything before. Works on Browse, Random, Search, Favorites, History, and the quote of the day card"
        )
    ),
    ChangelogEntry(
        "1.63", listOf(
            "Quote radio: new \"Everything shuffle\" station — one station that plays every database shuffled together. It's the first choice in the station picker",
            "Quote radio: sleep timer — tap the timer icon in the transport controls for 15/30/60 minutes or \"After this quote\". The mini player bar above the bottom nav has its own timer too",
            "Android Auto: quote radio now plays through your car stereo — browse stations (including Everything shuffle) on the car screen and use play/pause/skip from the wheel or dash; the car and the phone drive the same queue",
            "Speaker voices: automatic assignment now guesses whether a nick reads male or female and picks a matching voice; ambiguous nicks keep the old deterministic pick. Manual per-nick voices still win",
            "Speaker voices: new \"Manage speaker voices\" screen in Settings → Read aloud — search every speaker and assign voices in one list, instead of hunting down each nick's page (tapping a nick's name on a quote card still opens it)",
            "Nick colors: new nick-color theme system in Settings → Themes — presets include the Quote404 logo palette (the new default), Rainbow, Sunset, Ocean, Neon Nights, Pastel, Forest, and Ember. Build your own custom themes with up to 10 colors",
            "Nick colors: each nick's name and brackets share that nick's own color; an optional \"Color what they say\" toggle colors the whole spoken line too (off by default)",
            "Slideshow videos: pick an animated background — Midnight, Aurora, Starfield, Waves, or Ember — when exporting an MP4",
            "Slideshow videos: karaoke mode highlights each word in amber as it is read (needs \"Narrate each quote aloud\" on)"
        )
    ),
    ChangelogEntry(
        "1.62", listOf(
            "Quote radio: new endless read-aloud mode — pick a database as your station and quotes keep playing, shuffled, until you stop. Find it on the Random tab (radio icon). Pause, skip, and switch stations any time",
            "Auto-assign speaker voices: Settings → Read aloud has a new toggle (on by default) — speakers without a manual voice pick automatically get their own voice, and the same nick always sounds the same. Turn it off and unpicked speakers use the default voice",
            "Nick pages: a nick's read-aloud voice menu now shows the effective auto-assigned voice next to \"Automatic\", so you can see what they'll sound like before you play anything"
        )
    ),
    ChangelogEntry(
        "1.61.21", listOf(
            "Bash.org: 4 quotes that vanished from bash.org after the 2013 scrape are back in the database (21,096 → 21,100) — browse the new \"Removed\" category to find them",
            "Voice recordings: Settings → Voice recordings now has search (quote number, voice, or quote text) and sorting (newest, oldest, quote number, largest, database)",
            "Voice picker: corrected the wording in Help and nick voice menus — the · saved mark means a voice has recorded audio somewhere; picking it replays that audio wherever it was used, and new reads are generated once then saved"
        )
    ),
    ChangelogEntry(
        "1.61.20", listOf(
            "Share cards: long quotes no longer get cut off on background-art cards — the background now grows to fit the whole quote",
            "Share cards: the logo and quote sit higher on the card, so there's less empty space above the logo",
            "Voice recordings: every read-aloud is now tracked — change a speaker's voice and the next play generates fresh audio, with the old version kept as a backup",
            "Voice recordings: quote cards show a voice icon (top-right, next to Share) when a quote has saved audio — tap it to list all your voiced quotes, long-press it to manage that quote's recordings",
            "Voice recordings: Search has a \"Has voice\" filter — combine it with text search and sorting, or use it alone to browse every voiced quote",
            "Voice recordings: Settings has a Voice recordings screen — play versions, pick which one is active, regenerate, delete, and see total storage. All recordings are included in full backups",
            "Voice picker: voices that already have saved audio somewhere are marked (· saved) in a nick's voice menu — picking one replays that audio wherever it was used"
        )
    ),
    ChangelogEntry(
        "1.61.19", listOf(
            "Share cards: removed the second \"Funny quotes from everywhere.\" line — the logo art already shows it, so the extra line was a duplicate",
            "Share cards: the Quote404 logo is a little bigger on site-card exports",
            "Share cards: the black area above the card is shorter now"
        )
    ),
    ChangelogEntry(
        "1.61.18", listOf(
            "Site themes: the footer is now a colored strip like the header — no more plain white footer hanging under the card",
            "Site themes: the text in the bottom-left corner of the footer (the site version and the render-time number) is gone — just the quote count stays, on the right",
            "Translations: a translated quote now shows only the translation — tap the Translated badge to flip back to the original, tap again to go back",
            "Translations: long-press the Translated badge to show the original underneath the translation for that quote",
            "Translations: the Translate option in a quote's menu now lets you pick which language to translate into",
            "Read aloud: the narration audio is now saved permanently on your device, so each quote is only ever fetched once — and it's included in your backups along with the translations"
        )
    ),
    ChangelogEntry(
        "1.61.17", listOf(
            "Share cards: the Quote404 logo isn't cropped anymore — the crop was slicing through the Q's tail, so the whole emblem shows now",
            "Share cards: the dark glow behind the logo hugs it tightly now instead of ballooning way out around it",
            "Designed share cards: the tint over the background art is lighter, so more of the artwork shows through — the quote text still reads fine"
        )
    ),
    ChangelogEntry(
        "1.61.16", listOf(
            "Random screen: the Count box now sits centered between the shuffle icon and the categories icon, instead of hugging the shuffle button",
            "Share cards: tapping a card style now shows a full preview of the exact export image first — Share sends it, Back returns to the styles. No more sending a card to someone just to check how it looks",
            "Share cards: the logo sits just above the quote now, instead of floating far above it — logo and quote are one centered group",
            "Share cards: the \\\"Funny quotes from everywhere.\\\" line under the logo is drawn as crisp text now, so it's actually readable — the tiny version baked into the logo image never was",
            "Quote-theme exports: fixed the logo never showing up — the page background was being painted right over it",
            "Quote-theme exports: the logo plate now carries the readable tagline too, in a slimmer plate"
        )
    ),
    ChangelogEntry(
        "1.61.15", listOf(
            "Random screen: the Count box is a touch bigger (78x62dp) — at the old size the \"Count\" label and the number itself got clipped and were hard to read",
            "Random screen: the icon buttons shrank to 40dp with tighter spacing, so the whole control line (shuffle, count, categories, endless deck, flashcards, rate, read aloud) fits on screen — the read-aloud speaker is no longer cut off the edge",
            "Exports: the Quote404 logo now sits right on the card background — big, top-center — instead of a black strip floating above the card, on every exported image and video",
            "Designed share cards: the little blue line above the quote is gone, and the quote text sits centered in the background",
            "Quote-theme exports: the header bar is shorter now, matching the on-screen quote card — it used to render nearly twice as tall, with the slim logo plate flush right above it",
            "Bash.org's gold is slightly brighter on the export and the matching on-screen card (header, footer, vote links) — the old dark gold read too dark"
        )
    ),
    ChangelogEntry(
        "1.61.14", listOf(
            "Random screen: one control line now — the shuffle icon sits left of the smaller count box, and every action icon (categories, endless deck, flashcards, rate, read aloud) is pushed right",
            "Random screen: the category picker is now an icon too — tap it and the category menu drops right from the icon",
            "Random screen: Options still holds everything spelled out (Endless deck, Flashcards, Rate, Read aloud, categories, Hide read), collapsed by default"
        )
    ),
    ChangelogEntry(
        "1.61.13", listOf(
            "Random screen: the Count box now stays visible next to the Shuffle button at all times, and both are taller so the number is easy to read",
            "Random screen: the action buttons are now icon buttons in a row just under Shuffle — endless deck (♾), flashcards, and rate (star), with Read aloud as a speaker icon at the far right",
            "Random screen: \"Filters\" is now \"Options\" — it holds everything that isn't an icon button (category picker, Hide read)",
            "Exports: the black Quote404 logo strip across the top is now much larger, so every word on the logo is readable — on designed cards, Quote-theme cards, strips, stats cards, folder PNGs, and slideshow MP4s",
            "Quote-theme exports now match the on-screen site card: full-bleed header bar, vote line, and body with no black margins around the card — minus the buttons"
        )
    ),
    ChangelogEntry(
        "1.61.12", listOf(
            "Random screen: the Count box is now the same height as the Shuffle button",
            "Random screen: removed the duplicate database selector from the filter row — the database pill in the top bar is the one to use",
            "Random screen: the Read aloud / Flashcards / Rate buttons are now the same height as the filter row (category picker, Hide read) so the rows line up"
        )
    ),
    ChangelogEntry(
        "1.61.11", listOf(
            "Control row reshuffle: the rating and vote controls moved to the far left of the row under the header; the flag, share, favorite, and menu (⋯) buttons sit at the far right, menu last. Bash.org and qdb.us keep their vote controls in their special spots",
            "Report is now a flag icon on every database theme (no more text links)",
            "New per-database option: \"Short header title\" shows just the site name in the header bar (\"Bash\" instead of \"Bash.org\"). Off by default — find it in Settings → Databases → (database)",
            "Share cards: a black Quote404 logo strip now tops every exported image and video (designed cards, Quote-theme cards, strips, stats cards, folder PNGs, slideshow MP4s), replacing the faint centered watermark that washed out on light cards",
            "Share cards: the Quote-theme card now shows the real site name in its header (\"Bash.org\", not \"QDB\"), honors the Short header title option, and its vote line uses a flag icon instead of [X] — like the on-screen card",
            "Share cards: the Colorize nicknames toggle now works on designed cards too, and export coloring handles the [nick] format bash.org quotes use (previously only <nick> and Nick:)",
            "Share cards: quote text on artwork backgrounds (Quote404, DevXen) now sits on a dark panel so it stays readable over bright art"
        )
    ),
    ChangelogEntry(
        "1.61.10", listOf(
            "Every database header now uses the same professional layout: the site name on the left of the header bar (\"qdb.us\", \"CrapShack\", \"Bash.org\", \"QDB.MIT.edu\", \"xkcdb\", \"WQDB.org\", ...) with the quote number on the right",
            "The thumbs, flag, ⋯, share, and heart buttons sit in a row just under the header bar — never inside it — right-aligned on every database theme",
            "The database tag moved out of the header: it now sits bottom-right, across from your star rating — and it's a button. Tap it to jump straight to Browse filtered to that database",
            "If a database has a logo, you can show it centered in the header bar: Settings → Databases → (database) → header logo. Off by default",
            "Quote text now sits in a thin bordered box on every database theme except Bash.org, which keeps its borderless look like the original site",
            "qdb.us got its bluer header bar back, and the old decorative borders around the header title and quote number are gone"
        )
    ),
    ChangelogEntry(
        "1.61.9", listOf(
            "Card headers redesigned: the database name sits on the left of the header bar (\"CrapShack Quotes\", \"Bash.org Quotes\", \"qdb.us Quotes\") with the quote number on the right",
            "The thumbs, flag, ⋯, share, and heart buttons moved out of the header bar into a row just under it, right-aligned — easier to tap, and the header stays clean",
            "New one-tap share button just left of the favorite (heart) button — it opens the share sheet directly, no more digging through the ⋯ menu"
        )
    ),
    ChangelogEntry(
        "1.61.8", listOf(
            "Every exported image and video now carries the Quote404 logo as a faint centered watermark — designed cards, Quote-theme (site-styled) cards, multi-quote strips, reading-stats cards, exported PNGs, and slideshow MP4s",
            "The watermark sits alongside the existing branding: the Quote404 wordmark top-left and the source database name in the footer stay as they are"
        )
    ),
    ChangelogEntry(
        "1.61.7", listOf(
            "Every shared quote image now carries the Quote404 wordmark in the top-left of the header — on designed cards and on Quote-theme (site-styled) cards alike",
            "Every shared quote image now names where the quote is from: designed cards show the database in the footer, and Quote-theme cards always show the site name in the footer (even with the quote-count line)",
            "Dialogue line-splitting fixed for quotes like CrapShack #189: a {nick} (or <nick>) after a closing bracket now starts its own line, so multi-turn quotes read as separate turns",
            "Site-theme audit: every database's share card has a header and footer in its own site colors, with the Quote404 wordmark and the site name always visible"
        )
    ),
    ChangelogEntry(
        "1.61.6", listOf(
            "Quote404 background art updated to the version with the centered logo — it's the app background and the default share-card background"
        )
    ),
    ChangelogEntry(
        "1.61.5", listOf(
            "New name and look: the app is now Quote404, with a new icon, logo, and artwork",
            "New Quote404 theme (now the default): neon blue on black with the new artwork behind your quote lists — your old theme is still in Settings → Themes if you want it back",
            "Quote404 is the first share-as-image card style, using the new artwork as the card background",
            "Share-as-image \"Quote theme\" tile fixed: the quote number and rating now always show in the preview and the exported card, and the Web address checkbox controls the URL line",
            "Quote theme exports now use your chosen quote background color, and nicknames can be colorized (or not) with the new Colorize nicknames checkbox"
        )
    ),
    ChangelogEntry(
        "1.61.3", listOf(
            "DevXen background image: Settings → Themes → DevXen background lets you replace the DevXen artwork with your own picture — it shows dimmed behind lists, inside quote cards, and on DevXen share cards (Remove goes back to the bundled art)"
        )
    ),
    ChangelogEntry(
        "1.61.2", listOf(
            "Fixed \"Both\" translation mode on site-theme cards: the original text was printing on top of the translation instead of below it",
            "\"Database themes only on quote cards\" now defaults ON: the app chrome stays on your system theme (no more unreadable light-on-dark mixes), database site themes still style the quote cards"
        )
    ),
    ChangelogEntry(
        "1.61.1", listOf(
            "Fixed the 1.61 launch crash (translation database wasn't wired into the build — the app now starts normally)"
        )
    ),
    ChangelogEntry(
        "1.61", listOf(
            "Warpdrive.se translation: the Swedish archive now translates to English automatically (per-quote Translate action in the ⋯ menu too). Translations are saved on your device and backed up, so each quote is only ever translated once",
            "Translated quotes get a Translated badge — tap it to see the original. Settings → Reading → Translations controls the engine (Gemini or the free fallback), target language, and show-both/translated/original modes",
            "Share-as-image picker scrolls again, and your DevXen artwork is now a card style",
            "Share-as-image fixed: picking a style no longer fails with \"Couldn't render card\"",
            "Warpdrive.se footer cleaned up (removed the stray \"Tailsweep Analytics\" text from the archived page)"
        )
    ),
    ChangelogEntry(
        "1.60", listOf(
            "Gemini read-aloud fixed: whole conversations now go in one request (the old code burned one API call per dialogue turn — that's what killed it on the second voice and ate the tiny free quota), plus on-device caching so replays are free",
            "Share-sheet import finished: sharing text from any app opens My Quotes with the quote text, and any link in the shared text becomes the quote's source URL",
            "Database updates without app updates: Settings → Your data → Database updates — paste an update-list URL and pull newer quote databases, verified and merged in place (categories, favorites, ratings, and notes are kept)"
        )
    ),
    ChangelogEntry(
        "1.59.3", listOf(
            "Removed the stray empty \"I Rox\" database entry (a leftover 0-byte file) — the real I-Rox.com database is untouched",
            "qdb.us quote count repaired: the bundled-data refresh now re-runs and merges the current 15,573-quote collection on devices that were stuck at 13,813"
        )
    ),
    ChangelogEntry(
        "1.59.2", listOf(
            "Settings screen shortened: categories start collapsed (tap to expand), collapse choices are remembered, tighter row spacing"
        )
    ),
    ChangelogEntry(
        "1.59.1", listOf(
            "Top-bar order is now Random, Settings gear, then the overflow menu"
        )
    ),
    ChangelogEntry(
        "1.59", listOf(
            "Jump to quote number from Browse (toast if the number doesn't exist — never an error screen)",
            "Settings → Verify databases: scans for corruption (missing tables, FTS desync, bad rows) and repairs what it can",
            "Daily quote notifications can now draw \"From my favorites\"",
            "Swipe previous/next in quote detail, with a toast at the first/last quote",
            "Duplicate finder: the Bash.org copy is now the canonical one for identical cross-database duplicates",
            "New option (off by default): database themes style quote cards only — app chrome stays on your system theme",
            "Live theme preview screen: see your chrome plus every database's site card before changing anything",
            "Quick per-database theme picker right inside the database switcher",
            "Home-screen widgets now render the quote as a site-theme card",
            "Settings reorganized into collapsible categories (Appearance / Reading / Your data) with Recently changed, pinned settings, and grouped overflow menus",
            "What's new: every version entry now starts collapsed — tap to expand"
        )
    ),
    ChangelogEntry(
        "1.58", listOf(
            "\"View original\" link when an original/archive URL is stored",
            "Share/send as a styled site-theme image card (plain text still available)",
            "Typo-tolerant quote/nick search — AND/OR/NOT and exact phrases still work",
            "Settings search box",
            "Submitted/capture dates shown quietly where stored",
            "qdb.us Random-tab white-bleed fix",
            "Readable overflow-menu version footer",
            "furcqdb: one continuous bright-blue frame around header, body, and footer"
        )
    ),
    ChangelogEntry(
        "1.57", listOf(
            "Browse database multi-select: any subset of databases, persists",
            "App chrome (Shuffle, top bar, buttons, bottom nav, active tab) follows your app theme — only the database pill carries the DB accent",
            "qdb.us white-bleed fix in multi-database Browse",
            "I-Rox ?nick? nick colorization and footer spacing fix",
            "Random sources dialog now uses the shared multi-select with readable text",
            "Rounded accent-tinted overflow menus, dialogs, and info panels",
            "Quote of the Day renders as a tappable site-theme card",
            "Themed empty states; CrapShack inner border removed"
        )
    ),
    ChangelogEntry(
        "1.56", listOf(
            "Per-database page backgrounds: color picker + optional background image per database",
            "Per-database quote-body background color",
            "CrapShack footer simplified to load time + total count",
            "Database-switch reload fix: no more false \"quote doesn't exist\" screen",
            "Bundled-database refresh hardening for upgraders"
        )
    ),
    ChangelogEntry(
        "1.53–1.55", listOf(
            "Kid-safe profanity mask (display-only, off by default)",
            "Optional centered site logo in quote header bars",
            "\"On this day\" and Ambient mode (feature-gated)",
            "Database info panels: collection date, quote counts, favorites, and categories per database",
            "Quote footer refinements"
        )
    ),
    ChangelogEntry(
        "1.52", listOf(
            "Text transforms (Leet / Upside-down / Alternating case) are now one selector, defaulting to off"
        )
    ),
    ChangelogEntry(
        "1.51", listOf(
            "Per-database site themes: all 12 databases styled like their original sites (xkcdb, wqdb, furcqdb, Bash.org, CrapShack, bash.im, and more)",
            "Master theme override, per-database on/off, color and layout switches",
            "Mixed-database lists render each quote in its own database's theme",
            "Queued tag on unapproved quotes + Approved/Queued/Both content filter per database",
            "Offline personal rating buttons; \"Classic QDB\" layout option"
        )
    ),
    ChangelogEntry(
        "1.50–1.50.2", listOf(
            "Startup-screen chooser, random-quote and streak home-screen widgets",
            "Scheduled daily quote notifications (off by default)",
            "Quote Battle with ELO leaderboard, streak freeze / vacation mode",
            "Shareable stats card image, per-database scroll-position memory",
            "Persistent detail pinch zoom, rate-as-you-read stars",
            "Mangled replacement characters stripped at display time; (@nick) mentions colorized and tappable",
            "Stale bundled databases refresh in place on upgrade, preserving categories and favorites"
        )
    ),
    ChangelogEntry(
        "1.49", listOf(
            "Database select-all/deselect-all",
            "Eight new toggle-gated features (random without repeats, quote notes, MP4 slideshow export, reading goals, nick timeline, double-tap-to-favorite, cross-DB duplicate merge, multi-select select-all)",
            "Gemini read-aloud fixes: visible multi-voice errors, per-chunk resilience, all 30 voices, auto-fallback to built-in TTS"
        )
    ),
    ChangelogEntry(
        "1.48", listOf(
            "Read-aloud playlist, search operators (AND/OR/NOT, exact phrases)",
            "Search alerts on new imports, reading heatmap, configurable swipe actions",
            "Shake for random, shareable collections, nick interaction graph",
            "Three new games: line scramble, rating guess, blitz who-said-it"
        )
    ),
    ChangelogEntry(
        "1.47", listOf(
            "QuoteClean: cross-database text normalization + colorization with per-piece toggles",
            "Per-database archive dates; narrator voice for action lines",
            "Gemini read-aloud robustness (retry, friendlier errors, test-connection button)",
            "Source DB chip in all-databases mode; bulk categorize with auto-rules and built-in Offensive list",
            "Unfolded-Fold two-pane Browse fix"
        )
    ),
    ChangelogEntry(
        "1.46", listOf(
            "Per-speaker read-aloud voices (Gemini per-speaker voices; built-in TTS per-turn pitch/rate variation)",
            "Per-nick voice override picker"
        )
    ),
    ChangelogEntry(
        "1.45", listOf(
            "Achievements (24 badges), daily quiz widget, nick-vs-nick head-to-head",
            "Quote-of-the-Day notification actions, launcher shortcuts, pin quotes",
            "Six new games; Gemini cloud TTS engine option (API key stays on-device, 26 prebuilt voices)"
        )
    ),
    ChangelogEntry(
        "1.44", listOf(
            "Six games (fill in the blank, odd one out, sort by rating, guess the database, speed round, tournament)",
            "Shareable themes, up-next queue, search history with recent chips",
            "Nick avatars (identicons), deck resume, multi-quote image strip",
            "Full backup/restore with versioned zips"
        )
    ),
    ChangelogEntry(
        "1.43", listOf(
            "Random tab now has its own Random-sources picker instead of using the Browse database switcher"
        )
    ),
    ChangelogEntry(
        "1.42", listOf(
            "Two-pane Browse/Search on unfolded Fold; custom quote-body fonts (.ttf/.otf)",
            "Material You \"Dynamic\" theme; reading streaks with milestone badges",
            "Read-aloud sleep timer; EPUB export; muted nicks",
            "\"More like this\" + duplicate finder; named saved searches; Daily Challenge game"
        )
    ),
    ChangelogEntry(
        "1.34–1.41", listOf(
            "Incremental improvements across themes, widgets, search, and quote handling"
        )
    ),
    ChangelogEntry(
        "1.33", listOf(
            "Fixed installation failure on Android 12+: the Quick Settings tile service now declares android:exported"
        )
    ),
    ChangelogEntry(
        "1.32", listOf(
            "Quote viewer: swiping left/right on a quote now shows random quotes by default",
            "New \"Quote viewer\" settings: swipe through this database or all databases, in random or in-order mode",
            "Swiping back always returns to the previously viewed quote, even in random mode",
            "Quote pings: new Daily and Weekly schedules with a time picker (and weekday picker for weekly)",
            "Quote pings: choose which databases pings pull quotes from"
        )
    ),
    ChangelogEntry(
        "1.30", listOf(
            "Nine new quote databases: QDB.us, QDB.MIT.edu, I-Rox.com, Mandaliet.com/furcqdb, QuoteIRC.com, Quotes.BurntElectrons.org, Warpdrive.se, WQDB.org, xkcdb.com (April 2011 archives)",
            "Home now shows \"All databases\" by default — quotes from every collection in one stream, each tagged with its source",
            "Each database shows its collection date; turn dates off in Settings → Databases",
            "Reorder databases in Settings → Databases → Reorder (default: Bash.org, #CrapShack Quotes, QDB.us, then the rest A–Z)"
        )
    ),
    ChangelogEntry(
        "1.29", listOf(
            "Length filter is now a preset dropdown: All lengths, Longer, Shorter, 1 line, Custom",
            "Longer/Shorter word thresholds are configurable in Settings",
            "Quote-of-the-day card can be minimized to a slim row",
            "Home screen (Browse) opens with the latest-added quotes first",
            "Rating filter: All / >0 / >100 / >1000 presets plus a custom number box",
            "Share text from any app straight into My Quotes",
            "Quick Settings tile: tap for an instant random quote",
            "Rate quotes Tinder-style — drag right for yes, left for no",
            "EPUB export for My Quotes, folders, categories, and the export tray",
            "\"On this day\" card resurfaces quotes you read or favorited in past years",
            "Boolean search: AND, OR, NOT and \"exact phrases\"",
            "Flashcards are now real spaced repetition (SM-2): Again / Hard / Good / Easy",
            "Pin any favorites folder to your home screen",
            "Scheduled quote pings: a random quote notification on your chosen interval",
            "Configurable left/right swipe actions on quote rows",
            "Duplicate finder: exact and near-duplicate quotes across all databases",
            "Fixed a startup crash (duplicate sort_order column) when categories loaded concurrently",
            "This Help screen, with a What's New changelog"
        )
    ),
    ChangelogEntry(
        "1.28", listOf(
            "Share favorite folders as collections; import them (or categories) into My Quotes",
            "Never show again: per-quote hiding with a management screen",
            "Tap any nick for their quotes, average rating, and a most-quoted leaderboard",
            "My Quotes auto-tags new quotes",
            "Min/max quote-length filters on Browse and Search",
            "Reading heatmap and rating histogram on the Stats card",
            "Per-nick TTS voices (optional)",
            "Quote-of-the-day notification time picker",
            "Launcher shortcuts for Random, the endless deck, and Search",
            "Fixed the detail screen sometimes refusing to scroll"
        )
    ),
    ChangelogEntry(
        "1.27", listOf(
            "Redesigned endless deck: flick gestures, bigger type, tap for long quotes",
            "Content filter: one-tap suggested word sets and bulk word management"
        )
    ),
    ChangelogEntry(
        "1.26", listOf(
            "Multi-select quotes for bulk favorite, categorize, share, and more",
            "Per-widget configuration (database + category each)",
            "Reading heatmap and daily reading goal",
            "Higher-or-lower rating game",
            "Personal notes on quotes",
            "Flashcard mode",
            "Daily quote wallpaper",
            "Content filter with hidden-count banners and per-screen bypass",
            "Themes moved to the main menu"
        )
    ),
    ChangelogEntry(
        "1.25", listOf(
            "Content filter: hide offensive quotes plus your own blocked-word list"
        )
    ),
    ChangelogEntry(
        "1.24", listOf(
            "Import quotes from a bash.org URL or quote number",
            "Scheduled auto-backup of all databases",
            "PDF export (single file or per quote)",
            "Quote-of-the-day history",
            "Widget category filter",
            "Local thumbs up/down re-rating on the detail screen",
            "Paired bracket/name color palettes in the theme editor"
        )
    ),
    ChangelogEntry(
        "1.21–1.23", listOf(
            "Card surface theme slot actually applied; Midnight Blue cards go pure black",
            "Category bulk tools: add search results, drag-to-reorder"
        )
    ),
    ChangelogEntry(
        "1.19–1.20", listOf(
            "Compact Random header; pull-to-refresh works from the header",
            "Nick colors: separate Default/None/Pick for brackets and names"
        )
    ),
    ChangelogEntry(
        "1.15–1.17", listOf(
            "Endless random deck",
            "Reading stats and streaks",
            "Read-aloud playlists with a mini player",
            "Hide read quotes",
            "TTS speaks nicks naturally; IRC line separators in the widget",
            "Swipe right/left on Random for new/previous sets"
        )
    ),
    ChangelogEntry(
        "1.13–1.14", listOf(
            "#CrapShack Quotes database (2,855 quotes)",
            "Automatic per-nick colors, per-person overrides, IRC line separators"
        )
    ),
    ChangelogEntry(
        "1.12", listOf(
            "Saved searches, rating filters, jump to quote number, favorites folders",
            "Share as designed quote-card PNGs, read aloud, double-tap to favorite",
            "Compact/comfortable density, Material You theme, two-pane layout on wide screens",
            "Tappable web links; themeable rating color; no more purple switches"
        )
    ),
    ChangelogEntry(
        "1.9–1.11", listOf(
            "My Quotes database: add, edit, delete, import from text",
            "Multi-source Random, source chips, per-database backup and email share",
            "Quote of the day everywhere with matching daily rotation"
        )
    ),
    ChangelogEntry(
        "1.0–1.8", listOf(
            "The original Bash.org archive browser: search, random, history,",
            "favorites, top/bottom 100, rating sort, home-screen widget,",
            "theme editor with color pickers, and categories"
        )
    )
)

private data class HelpSection(val title: String, val body: List<String>)

private val HELP_SECTIONS = listOf(
    HelpSection(
        "Sharing quotes", listOf(
            "Tap the share icon just left of the heart — in the row of icons right under the card header — or ⋯ → Share — on any quote: send it as plain text, or tap \"Share as designed image…\" for a picture card.",
            "Tapping a card style shows a full preview of the exact image first — check it, then Share to send it or Back to pick another style. What you see is what gets shared.",
            "\"Quote theme\" renders the card in the quote's database site style (bash.org looks like bash.org) — full-bleed like the on-screen card, minus the buttons. It always includes the quote number and rating, the site name in the footer, and the Quote404 logo in a slim plate flush above the header.",
            "Designed styles (Quote404, DevXen, Midnight, and more) put the Quote404 logo just above the quote — logo and quote are one centered group — with the quote number top-right and the database name in the footer.",
            "Checkboxes before sharing: Quote number, Rating, Web address (adds the quote's source URL), Colorize nicknames, IRC line breaks (\" \\\" separators), and Dialogue line breaks (one line per speaker).",
            "Multi-quote strip: long-press quotes to select several, then share them as one tall image.",
            "Branding: every exported image and slideshow video carries the Quote404 logo — just above the quote on designed cards, stats cards, and video frames, and in a slim plate flush against the top on Quote-theme cards, strips, and folder PNG exports. The \"Funny quotes from everywhere.\" line under the logo is drawn as crisp text so it reads."
        )
    ),
    HelpSection(
        "Translations", listOf(
            "Warpdrive.se (Swedish) auto-translates to English; other databases translate on demand from the ⋯ menu.",
            "Translated quotes get a Translated badge — tap it to see the original text.",
            "Settings → Reading → Translations: pick the engine (Gemini or the free fallback), the target language, and whether to show both versions, translated only, or original only.",
            "Translations are saved on your device and included in backups, so each quote is translated once."
        )
    ),
    HelpSection(
        "Games", listOf(
            "Six games live under Games: fill in the blank, odd one out, sort by rating, guess the database, 60-second speed round, and the tournament bracket.",
            "Quote Battle pits two quotes head-to-head with an ELO leaderboard; streak freeze and vacation mode protect your streak.",
            "The tournament bracket (Tournament button in Quote Arena) runs 8 quotes single-elimination; game and tournament quote pools respect your global maturity ceiling.",
            "Higher or lower and the endless deck's rate-by-swipe feed your personal ratings, which power recommendations."
        )
    ),
    HelpSection(
        "Browsing & searching", listOf(
            "Browse lists every quote, newest first; Search finds text or jumps straight to a #number.",
            "Length filter presets: All lengths, Longer, Shorter, 1 line, or Custom min/max words — thresholds live in Settings.",
            "Filter by rating: All, >0, >100, >1000, or your own number.",
            "Boolean search: combine terms with AND, OR, NOT and \"exact phrases\". Example: kernel AND linux NOT \"windows xp\".",
            "Regex search: turn on the Regex chip and the query becomes a regular expression matched against quote text (case-insensitive). Examples: ^never.*again\$ finds quotes starting with “never” and ending with “again”; (cat|dog) finds either word; <[^>]+> finds IRC-style nick brackets. A pattern that won't compile shows “Invalid regular expression” instead of fake empty results. Saved regex searches re-run exactly, but they don't fire new-match alerts.",
            "Search history remembers the full filter setup of each search, not just the words — tap an entry to re-run that exact search, or delete entries individually.",
            "Mood buttons (Make me laugh, Make me think, Disturb me, Geek me out, Warm my heart, Roast me) are toggle filters: tap one to add its categories, tap again to remove it. Stack several moods at once — they combine with your query, database scope, and every other filter, and run with the Search button.",
            "Searching all databases collapses duplicate quotes into one row, with “also in …” naming every other database that has the same quote.",
            "Saved searches keep frequent queries one tap away — each one stores its query plus all filters (categories, exclusions, maturity, cast size, voice-only, rating, sort, length).",
            "Categories tag quotes across databases; filter any list by category. The Categories screen and every category picker now group them into collapsible sections (Tech & geek, Life, Entertainment, Humor, Maturity, Faith & thought, Length, plus My categories for your own). Tap a section header to collapse or expand it; the header checkbox checks or unchecks the whole group at once.",
            "Tap any nick's name on a quote card to open that nick's page — their quotes, stats, and read-aloud voice picker."
        )
    ),
    HelpSection(
        "Random & discovery", listOf(
            "Random deals a set of quotes — swipe right or pull down for a new set, swipe left to go back.",
            "The Count box sets how many quotes each shuffle deals; it sits next to the wide blue Shuffle quotes button on the top row, with a labeled Options pull-down at the far right.",
            "The Options pull-down holds the spelled-out actions: Endless deck, Flashcards, Rate, Read aloud, Export video, and Hide read — plus the six mood filters, which toggle on and off and re-roll the deck in that vibe.",
            "The second row is the quick-action icons, centered under the shuffle row: ♾ for the endless deck, the stacked-cards icon for flashcards, the star for rating, the speaker for read aloud, and radio.",
            "Export video adds the current set to the export tray and opens Export with the slideshow MP4 format preselected.",
            "The category picker is a labeled pull-down in the top bar next to the database pill (on Browse, Random, and Search) — it reads “All categories” until you filter, then shows what you picked, and tints when a filter is active.",
            "Shake the phone to shuffle (toggle in Settings).",
            "The endless deck is a full-screen vertical feed: flick up for the next quote, tap for details, double-tap to favorite.",
            "Rate quotes Tinder-style: drag a card right for thumbs-up, left for thumbs-down. Votes feed your personal ratings.",
            "\"On this day\" means what it says: quotes genuinely posted on today's date in past years (bash.im, the only bundled database with real posting dates), plus a separately-labeled section of qdb.us quotes the Wayback Machine archived on this date — those are capture dates, not posting dates — and quotes you read or favorited on this date in past years.",
            "Higher or lower: guess which of two quotes is rated higher."
        )
    ),
    HelpSection(
        "Favorites & folders", listOf(
            "Double-tap any quote row to favorite it; long-press enters multi-select for bulk actions.",
            "Organize favorites into folders; long-press a folder chip to rename, share, pin, or delete it.",
            "Pin a folder to your home screen for one-tap access.",
            "Share a folder as a collection file; others can import it into My Quotes.",
            "Swipe a quote row left or right for your chosen quick action (Settings → Swipe actions)."
        )
    ),
    HelpSection(
        "My Collections", listOf(
            "Named quote lists: ⋯ menu → My Collections, then New collection. Add quotes from the detail screen's “Add to list” button or the long-press menu.",
            "Open a collection to reorder items, remove them, or tap a quote to view it.",
            "Play a collection as a read-aloud playlist, or send it to the export tray for any export format."
        )
    ),
    HelpSection(
        "Explaining quotes", listOf(
            "Long-press any quote → “Explain this quote”, or tap the Explain button on the quote detail screen, for an AI breakdown of the references and in-jokes.",
            "Needs the “Explain this quote” toggle in Settings → Features, and your stored Gemini API key.",
            "Explanations are saved on your device — a purple icon on the quote card marks quotes that have one (tap it to view), and Search has a “Has explanation” filter."
        )
    ),
    HelpSection(
        "My Quotes", listOf(
            "Your own database: add, edit, delete, and import quotes from text files.",
            "Share text from any app and it lands in the My Quotes editor.",
            "Import from a bash.org URL or quote number; collections import with auto-tagging."
        )
    ),
    HelpSection(
        "Learning tools", listOf(
            "Flashcards use spaced repetition (SM-2): grade each card Again, Hard, Good, or Easy and it's rescheduled for you.",
            "Read aloud speaks any list; per-nick voices are optional in Settings.",
            "Quote radio (radio icon on the Random tab): endless shuffled read-aloud. Pick one database as your station, or \"Everything shuffle\" to play every database shuffled together. Quotes keep playing until you stop — pause, skip, or switch stations any time. Tapping the now-playing card opens that quote.",
            "Quote radio: the timer icon in the transport controls sets a sleep timer — 15/30/60 minutes or \"After this quote\". The mini player bar above the bottom nav has a sleep timer too; either one stops playback when it runs out.",
            "Quote radio on Android Auto: plug in and the radio stations appear in your car's media browser, with play/pause/skip on the wheel and dash. The car drives the same queue as the phone, so both stay in sync.",
            "Speaker voices: Settings → Read aloud → Speaker voices gives each speaker their own voice in multi-speaker quotes. Auto-assign speaker voices (on by default) automatically gives every speaker without a manual pick their own voice — the same nick always sounds the same — and it guesses whether a nick reads male or female to pick a matching voice. Turn it off and speakers you haven't picked use the default voice.",
            "Speaker voices: pick a specific voice per nick two ways — tap any nick's name on a quote card to open their page (the read-aloud voice picker is on it), or Settings → Read aloud → \"Manage speaker voices\" to search all speakers and set voices in one list.",
            "A nick's page has a read-aloud voice picker: \"Automatic\" shows the effective auto-assigned voice in parentheses, or pick a specific voice to override it.",
            "Reading stats track totals, streaks, a heatmap, and your daily goal.",
            "Voice recordings: every read-aloud is saved on your device, so replaying a quote costs nothing. Quotes with saved audio show a voice icon at the top-right of the card (next to Share): tap it to list all voiced quotes, long-press it to manage that quote's versions.",
            "Voice recordings: if you change a speaker's voice, the next play generates fresh audio and the old version is kept as a backup. In the manage dialog you can play each version, make an older one active again, or delete what you don't need.",
            "Voice recordings: Settings has a Voice recordings screen with every saved quote, total storage, and a delete-everything option. Search's \"Has voice\" filter finds voiced quotes by text or lists them all, with full sorting.",
            "Voice recordings: nick voice menus mark voices that already have saved audio somewhere (· saved) — picking one reuses that audio for quotes previously read with it; anything new is generated once, then kept."
        )
    ),
    HelpSection(
        "Exporting", listOf(
            "Export scopes: all quotes, favorites, history, categories, or the export tray.",
            "Formats: text, JSON, XML, CSV, PNG cards, PDF, EPUB e-books, slideshow video (MP4), and narrated audio (M4A).",
            "The export tray collects quotes from anywhere for one combined export. Use the up/down arrows to set the order — exports follow tray order — or “Share all as text” to send the whole tray through the system share sheet. The tray keeps its quotes until you clear it.",
            "Single-quote PNG: the “Designed image…” button offers the same card styles as Share (Quote theme, Quote404, Midnight, DevXen…), with a preview before saving.",
            "Slideshow video (MP4): one animated slide per quote with the Quote404 logo watermark. Pick a background — Midnight, Aurora, Starfield, Waves, or Ember — and optionally narrate each quote aloud.",
            "Slideshow video: \"Highlight each word as it is read\" (needs narration on) gives you karaoke-style amber word highlighting synced to the narration.",
            "Narrated audio (M4A): each quote is read aloud with your device's default text-to-speech voice and saved as an M4A audio file (AAC — Android can't encode MP3, and M4A plays everywhere MP3 does). Export one file per quote or a single file with the whole set.",
            "Auto-backup copies every database to a folder on a daily or weekly schedule."
        )
    ),
    HelpSection(
        "Content control", listOf(
            "Content Filter hides quotes matching your blocked words; suggested sets add common lists in one tap.",
            "Never show again hides individual quotes everywhere; manage them under Content Filter.",
            "\"Show anyway\" banners temporarily bypass word filtering per screen.",
            "Content maturity (Settings → Reading): a global ceiling for the whole app — Kid Safe, Clean Only, PG-13, Safe for Work, Rated R, NC-17, Spicy-only, NSFW-only, or everything. Every quote in every database is rated on that ladder, plus a separate Spicy flag for FMyLife-style sexual content. Browse, Search, Random, Radio, the widget, games, and the quote of the day can never show anything above your ceiling, and \"Show anyway\" banners can't lift it. (PG-13 and Safe for Work both cap at PG-13; Safe for Work additionally screens out Spicy-flagged quotes.)",
            "Per-screen maturity and cast-size filters: Browse and Search have their own Maturity (Kid Safe … Spicy) and Cast size (single person, multiple people, exactly 2/3/4/5 people) filters. A screen's maturity pick can only narrow the global ceiling, never widen it.",
            "Search exclusions: the Exclude section on Search keeps chosen categories, exact maturity levels, and cast sizes out of the results even when the rest of the filters would include them."
        )
    ),
    HelpSection(
        "Personalizing", listOf(
            "Themes: presets plus fully custom themes — every color slot has a picker and a None option.",
            "Nick colors: Settings → Themes → Nick color theme — presets include the Quote404 logo palette (the default), Rainbow, Sunset, Ocean, Neon Nights, Pastel, Forest, and Ember, or build your own custom theme with up to 10 colors. Each nick's name and brackets share that nick's own color; the optional \"Color what they say\" toggle colors the whole spoken line too (off by default).",
            "Nicks get automatic colors; override any person or the bracket/name palettes in Settings.",
            "Quote of the day: notification time is configurable, the card can be minimized, and history keeps the last 60 picks.",
            "Widgets: per-widget database and category; Quick Settings tile for an instant random quote.",
            "Quote pings send a random quote notification on your chosen interval (Settings)."
        )
    ),
    HelpSection(
        "Keeping things tidy", listOf(
            "Duplicate finder scans all databases for exact and near-duplicate quotes.",
            "Personal notes attach to any quote and are searchable.",
            "Hidden quotes, blocked words, and reading stats can all be reset from their screens."
        )
    )
)

@Composable
fun HelpScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        flingBehavior = rememberCappedFlingBehavior()) {
        item {
            ScreenHeader(title = "Help")
        }
        // v1.61.5: Clint's Quote404 branding.
        item {
            Image(
                painter = painterResource(id = com.quotebrowser.app.R.drawable.quote404_logo),
                contentDescription = "Quote404 — Funny quotes from everywhere.",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 4.dp)
            )
        }
        item {
            Text(
                "What's new",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        // v1.88.19: newest entry stays top-level; everything older folds
        // into its own collapsed "Older versions" menu.
        if (CHANGELOG.isNotEmpty()) {
            item {
                ChangelogCard(entry = CHANGELOG.first())
            }
        }
        if (CHANGELOG.size > 1) {
            item {
                OlderVersionsCard(entries = CHANGELOG.drop(1))
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "Feature guide",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        // v1.88.19: Diagnostics moved into its own collapsed section below
        // (was the always-visible card here). Share and the new Save live
        // inside, plus crash report, per-database timings, and device info.
        item {
            DiagnosticsSection()
        }
        items(HELP_SECTIONS) { section ->
            HelpSectionCard(section = section)
        }
        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChangelogCard(
    entry: ChangelogEntry,
    // v1.88.19: lets OlderVersionsCard drop the inner cards' own outer
    // padding so they don't double-indent inside the outer card.
    horizontalPadding: Dp = 16.dp
) {
    // v1.59: all version entries start collapsed — tap to expand.
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Version ${entry.version}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(4.dp))
                entry.items.forEach { item ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            item,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSectionCard(section: HelpSection) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                Spacer(Modifier.height(4.dp))
                section.body.forEach { line ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            line,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// v1.88.19: older changelog entries fold into their own collapsed menu.
// Inner cards keep the existing collapsed-by-default ChangelogCard style.
@Composable
private fun OlderVersionsCard(entries: List<ChangelogEntry>) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Older versions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                entries.forEachIndexed { index, entry ->
                    ChangelogCard(entry = entry, horizontalPadding = 0.dp)
                    if (index < entries.lastIndex) Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// v1.88.19: collapsed-by-default Diagnostics section. Replaces the old
// always-visible Diagnostics card (moved, not duplicated): it holds the
// startup-log share/save card, the last crash report snippet, the latest
// launch's per-database open timings, and an app/device header. Every
// read is defensive — diagnostics must never crash the app.
@Composable
private fun DiagnosticsSection() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    // Loaded once, when first expanded. applicationContext avoids leaking
    // the activity; every helper below is try/catch-guarded.
    val data = remember(expanded) {
        if (expanded) loadDiagnostics(context.applicationContext) else null
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        onClick = { expanded = !expanded }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Diagnostics",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
            if (expanded && data != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Quote404 ${data.versionName} (build ${data.versionCode})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    data.device,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Android ${data.androidRelease} (API ${data.androidSdk})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                StartupLogSubCard()
                Spacer(Modifier.height(8.dp))
                DiagnosticsSubCard(title = "Last crash") {
                    Text(
                        data.crashText ?: "No crash recorded",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
                Spacer(Modifier.height(8.dp))
                DiagnosticsSubCard(title = "Database open timings (latest launch)") {
                    if (data.dbTimings.isEmpty()) {
                        Text(
                            "Not recorded",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        data.dbTimings.forEach { (name, ms) ->
                            Text(
                                "$name — $ms",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// v1.88.19: compact inner card used by the Diagnostics section.
@Composable
private fun DiagnosticsSubCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

// v1.88.19: the moved "Share startup log" card — the share intent is
// preserved exactly from the old always-visible Diagnostics card, plus a
// new Save action. FileProvider authority "com.quotebrowser.app.fileprovider"
// is declared in the manifest (verified); the startup_logs cache path is
// whitelisted in res/xml/filepaths.xml.
@Composable
private fun StartupLogSubCard() {
    val context = LocalContext.current
    var msg by remember { mutableStateOf<String?>(null) }
    val mainHandler = remember {
        android.os.Handler(android.os.Looper.getMainLooper())
    }
    DiagnosticsSubCard(title = "Startup log") {
        Text(
            "If the app is slow to open, share or save the startup log — it records exactly what the app was doing while it opened.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                try {
                    val log = com.quotebrowser.app.StartupWatchdog.logFile(context)
                    if (log == null) {
                        msg = "No startup log yet — reopen the app first."
                    } else {
                        val uri = androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "com.quotebrowser.app.fileprovider",
                            log
                        )
                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Quote404 startup log")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(send, "Share startup log")
                        )
                    }
                } catch (e: Exception) {
                    msg = "Couldn't share the log: ${e.message}"
                }
            }) {
                Text("Share")
            }
            Button(onClick = {
                // v1.88.19: copy the file off the main thread; the result
                // (saved path or error) posts back to the message line.
                msg = "Saving…"
                try {
                    Thread({
                        val result = try {
                            saveStartupLog(context.applicationContext)
                        } catch (e: Exception) {
                            "Couldn't save the log: ${e.message}"
                        }
                        mainHandler.post { msg = result }
                    }, "save-startup-log").start()
                } catch (e: Exception) {
                    msg = "Couldn't save the log: ${e.message}"
                }
            }) {
                Text("Save")
            }
        }
        if (msg != null) {
            Spacer(Modifier.height(4.dp))
            Text(msg!!, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// v1.88.19: everything the Diagnostics section shows, loaded defensively.
// crashText is null when no crash report file exists; an error string when
// it exists but couldn't be read.
private data class DiagnosticsData(
    val versionName: String,
    val versionCode: String,
    val device: String,
    val androidRelease: String,
    val androidSdk: Int,
    val crashText: String?,
    val dbTimings: List<Pair<String, String>>
)

private fun loadDiagnostics(context: android.content.Context): DiagnosticsData {
    var versionName = "?"
    var versionCode = "?"
    try {
        val pi = context.packageManager.getPackageInfo(context.packageName, 0)
        versionName = pi.versionName ?: "?"
        versionCode = try {
            androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(pi).toString()
        } catch (_: Exception) {
            "?"
        }
    } catch (_: Exception) {
    }
    val device = try {
        "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()
            .replaceFirstChar { it.uppercaseChar() }
    } catch (_: Exception) {
        "?"
    }
    val androidRelease = try {
        android.os.Build.VERSION.RELEASE ?: "?"
    } catch (_: Exception) {
        "?"
    }
    val androidSdk = try {
        android.os.Build.VERSION.SDK_INT
    } catch (_: Exception) {
        -1
    }
    return DiagnosticsData(
        versionName = versionName,
        versionCode = versionCode,
        device = device.ifEmpty { "?" },
        androidRelease = androidRelease,
        androidSdk = androidSdk,
        crashText = readCrashSnippet(context),
        dbTimings = readLatestLaunchDbTimings(context)
    )
}

// v1.88.19: first ~30 lines of CrashHandler's crash_report.txt (filesDir),
// or null when no crash was recorded. Never throws.
private fun readCrashSnippet(
    context: android.content.Context,
    maxLines: Int = 30
): String? {
    return try {
        val f = com.quotebrowser.app.crashFile(context)
        if (!f.exists()) return null
        val lines = f.readLines().take(maxLines)
        if (lines.isEmpty()) "(empty crash report)" else lines.joinToString("\n")
    } catch (e: Exception) {
        "Couldn't read the crash report: ${e.message}"
    }
}

// v1.88.19: per-database open timings for the latest launch only — lines
// after the last "startup_log process started at" header in the ring-buffer
// log. DatabaseManager marks "db open end: <name> (<ms>ms)". Never throws.
private val DB_OPEN_END_RE = Regex("""db open end: (.+?) \((\d+)ms\)""")

private fun readLatestLaunchDbTimings(
    context: android.content.Context
): List<Pair<String, String>> {
    return try {
        val f = com.quotebrowser.app.StartupWatchdog.logFile(context) ?: return emptyList()
        val lines = f.readLines()
        val lastHeader = lines.indexOfLast { it.startsWith("startup_log process started at") }
        val recent = if (lastHeader >= 0) lines.drop(lastHeader + 1) else lines
        recent.mapNotNull { line ->
            DB_OPEN_END_RE.find(line)?.let { m ->
                m.groupValues[1].trim() to "${m.groupValues[2]} ms"
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

// v1.88.19: "Save" writes a timestamped copy of the startup log where the
// user can find it — the Downloads collection via MediaStore on Android 10+
// (no storage permission needed for app-owned files), falling back to a
// diagnostics folder in the app's files dir on older devices or failure.
// Returns a human-readable location. Throws only when there is no log.
private fun saveStartupLog(context: android.content.Context): String {
    val src = com.quotebrowser.app.StartupWatchdog.logFile(context)
        ?: throw IllegalStateException("No startup log yet — reopen the app first.")
    val stamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
        .format(java.util.Date())
    val name = "quote404_startup_log_$stamp.txt"
    if (android.os.Build.VERSION.SDK_INT >= 29) {
        try {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(
                    android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
            }
            val uri = context.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            ) ?: throw IllegalStateException("Downloads unavailable")
            context.contentResolver.openOutputStream(uri)?.use { out ->
                src.inputStream().use { it.copyTo(out) }
            } ?: throw IllegalStateException("Could not write to Downloads")
            return "Saved to Downloads/$name"
        } catch (e: Exception) {
            // Fall through to the files-dir copy below.
            android.util.Log.w("HelpScreen", "Downloads save failed, using files dir", e)
        }
    }
    val dir = java.io.File(context.filesDir, "diagnostics").apply { mkdirs() }
    val dest = java.io.File(dir, name)
    src.copyTo(dest, overwrite = true)
    return "Saved to ${dest.absolutePath}"
}
