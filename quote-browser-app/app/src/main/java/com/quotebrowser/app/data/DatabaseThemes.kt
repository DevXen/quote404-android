package com.quotebrowser.app.data

import com.quotebrowser.app.data.plugins.PluginManager
import org.json.JSONObject

/**
 * v1.51 per-database themes: every bundled quote database gets its own
 * theme based on its original site's colors/design, each quote looking
 * like a mini version of its original site.
 */

/** Per-database layout: the app's standard card, the classic QDB
 * presentation, or a mini version of the chosen theme's original site. */
enum class SiteLayout(val key: String, val label: String) {
    MODERN("modern", "Modern"),
    CLASSIC("classic", "Classic QDB"),
    SITE("site", "Site");

    companion object {
        fun fromKey(key: String?): SiteLayout =
            entries.firstOrNull { it.key == key } ?: SITE
    }
}

/** Which quotes a database's Browse/Search/Random flows show. */
enum class StatusMode(val key: String, val label: String) {
    APPROVED("approved", "Approved only"),
    QUEUED("queued", "Queued only"),
    BOTH("both", "Both");

    companion object {
        fun fromKey(key: String?): StatusMode =
            entries.firstOrNull { it.key == key } ?: BOTH
    }

    /** SQL fragment for the quotes.status column; null = no filtering. */
    fun sqlWhere(): String? = when (this) {
        APPROVED -> "quotes.status = 'approved'"
        QUEUED -> "quotes.status = 'queued'"
        BOTH -> null
    }
}

/**
 * Per-database theme configuration. Defaults: theming ON, the database's
 * own site theme, site colors ON, Site layout (mini versions of the
 * original sites out of the box), and both approved + queued quotes.
 */
data class DatabaseThemeConfig(
    val themeEnabled: Boolean = true,
    /** Theme id; "" = this database's site default. */
    val themeId: String = "",
    val siteColors: Boolean = true,
    val layout: SiteLayout = SiteLayout.SITE,
    val statusMode: StatusMode = StatusMode.BOTH,
    /** v1.53: center the site's logo art in the quote header bar. */
    val centeredHeaderLogo: Boolean = false,
    /**
     * v1.61.11: hide the TLD in the quote header bar title ("Bash" instead
     * of "Bash.org"). Default off — the full site name shows.
     */
    val hideTldInHeader: Boolean = false,
    /**
     * v1.56: explicit per-database "Quote background" ("RRGGBB"); blank =
     * the theme's own default. Wins over the preset default and the
     * built-in override in every mode (Browse, Random, detail).
     */
    val quoteBackgroundHex: String = "",
    /**
     * v1.56: explicit per-database "Page background" ("RRGGBB"); blank =
     * the site default (the database theme's page color).
     */
    val pageBackgroundHex: String = "",
    /**
     * v1.56: optional page background image: file name under the app's
     * private page_backgrounds/ dir; blank = none.
     */
    val pageBackgroundImage: String = ""
) {
    fun toJson(): JSONObject = JSONObject()
        .put("themeEnabled", themeEnabled)
        .put("themeId", themeId)
        .put("siteColors", siteColors)
        .put("layout", layout.key)
        .put("statusMode", statusMode.key)
        .put("centeredHeaderLogo", centeredHeaderLogo)
        .put("hideTldInHeader", hideTldInHeader)
        .put("quoteBackgroundHex", quoteBackgroundHex)
        .put("pageBackgroundHex", pageBackgroundHex)
        .put("pageBackgroundImage", pageBackgroundImage)

    companion object {
        fun fromJson(raw: String?): DatabaseThemeConfig {
            if (raw.isNullOrBlank()) return DatabaseThemeConfig()
            return try {
                val o = JSONObject(raw)
                DatabaseThemeConfig(
                    themeEnabled = o.optBoolean("themeEnabled", true),
                    themeId = o.optString("themeId", ""),
                    siteColors = o.optBoolean("siteColors", true),
                    layout = SiteLayout.fromKey(o.optString("layout", null)),
                    statusMode = StatusMode.fromKey(o.optString("statusMode", null)),
                    centeredHeaderLogo = o.optBoolean("centeredHeaderLogo", false),
                    hideTldInHeader = o.optBoolean("hideTldInHeader", false),
                    quoteBackgroundHex = o.optString("quoteBackgroundHex", ""),
                    pageBackgroundHex = o.optString("pageBackgroundHex", ""),
                    pageBackgroundImage = o.optString("pageBackgroundImage", "")
                )
            } catch (e: Exception) {
                DatabaseThemeConfig()
            }
        }
    }
}

/**
 * Bundled database id -> its site theme id. Every one of the 12 bundled
 * databases has a dedicated theme; Bash.org keeps the existing Bash theme.
 */
val DB_SITE_THEMES: Map<String, String> = mapOf(
    "asset:bash_org.db" to "bashorg",
    "asset:qdb_us.db" to "db_qdb_us",
    "asset:qdb_mit_edu.db" to "db_qdb_mit",
    "asset:quoteirc_com.db" to "db_quoteirc",
    "asset:burntelectrons_org.db" to "db_burntelectrons",
    "asset:warpdrive_se.db" to "db_warpdrive",
    "asset:bash_im.db" to "db_bashim",
    "asset:wqdb_org.db" to "db_wqdb",
    "asset:xkcdb_com.db" to "db_xkcdb",
    "asset:mandaliet_furcqdb.db" to "db_furcqdb",
    "asset:irox_com.db" to "db_irox",
    "asset:crapshack_quotes.db" to "db_crapshack",
    // v1.88.38: Clint's personal "Friends" database (Jordyn & Tiffany
    // banter). v1.88.39 also ships it as a bundled asset, so both the
    // import id ("Friends") and the bundled id ("asset:friends.db") map
    // here; resolveDbTheme falls back to this when the per-db themeId is
    // blank, so the Friends theme auto-applies either way.
    "Friends" to "db_friends",
    "asset:friends.db" to "db_friends"
)

/** Theme ids that are database site themes (built-in, directly editable). */
val DB_THEME_IDS: Set<String> = DB_SITE_THEMES.values.toSet()

/** The site theme for a database id, or null for non-bundled databases. */
fun siteThemeFor(dbId: String): String? = DB_SITE_THEMES[dbId]

/**
 * Pure resolution of one database's effective theming. No I/O: everything
 * needed is passed in, so card composition can call this safely.
 */
data class ResolvedDbTheme(
    /** True = global theme entirely (global colors, Modern layout, no site structure). */
    val bypassed: Boolean,
    /** Theme id that paints the palette. */
    val paletteThemeId: String,
    /** Theme id whose site presentation to mimic; null = Modern card. */
    val structureThemeId: String?,
    val layout: SiteLayout,
    /** The per-database "site colors" switch (structural art still shows when off). */
    val siteColors: Boolean = true,
    /** v1.53: center the site logo art in the quote header bar. */
    val centeredHeaderLogo: Boolean = false,
    /** v1.61.11: hide the TLD in the quote header bar title. */
    val hideTldInHeader: Boolean = false
)

/**
 * Immutable snapshot of everything per-card resolution needs, provided
 * once at the activity root. Cards resolve purely from this.
 */
data class DbThemeSnapshots(
    val globalThemeId: String = "quote404",
    val perDbThemesEnabled: Boolean = true,
    val masterOverride: Boolean = false,
    val configs: Map<String, DatabaseThemeConfig> = emptyMap(),
    val customThemes: List<com.quotebrowser.app.ui.theme.CustomTheme> = emptyList(),
    val builtInOverrides: Map<String, String> = emptyMap(),
    /** The "Classic layout option" feature toggle; off demotes Classic to Site. */
    val classicLayoutEnabled: Boolean = true
) {
    companion object {
        /** Feature off / unknown state: everything resolves bypassed. */
        val BYPASSED = DbThemeSnapshots(perDbThemesEnabled = false)
    }
}

/**
 * Resolves one database's theming from an immutable snapshot (pure, no I/O).
 *
 * - Master override on, feature off, per-DB theme off, or unknown db:
 *   global theme entirely (global colors, Modern layout, no site structure).
 * - Otherwise the structural site styling comes from the per-DB theme
 *   choice; the palette comes from the per-DB theme when site colors are
 *   on, else the global palette. The per-DB layout (Modern/Classic/Site)
 *   stays active either way.
 */
fun resolveDbTheme(snap: DbThemeSnapshots, dbId: String?): ResolvedDbTheme {
    if (dbId == null || dbId == DatabaseManager.ALL_DBS_ID) {
        return ResolvedDbTheme(true, snap.globalThemeId, null, SiteLayout.MODERN)
    }
    if (snap.masterOverride || !snap.perDbThemesEnabled) {
        return ResolvedDbTheme(true, snap.globalThemeId, null, SiteLayout.MODERN)
    }
    val cfg = snap.configs[dbId] ?: DatabaseThemeConfig()
    if (!cfg.themeEnabled) {
        return ResolvedDbTheme(true, snap.globalThemeId, null, SiteLayout.MODERN)
    }
    val chosen = cfg.themeId.ifBlank { siteThemeFor(dbId) }.orEmpty()
    val palette = if (cfg.siteColors && chosen.isNotBlank()) chosen else snap.globalThemeId
    // The "Classic layout option" feature toggle gates the Classic QDB
    // choice; when off, Classic demotes to Site.
    val layout = if (cfg.layout == SiteLayout.CLASSIC && !snap.classicLayoutEnabled)
        SiteLayout.SITE else cfg.layout
    val structure = when (layout) {
        SiteLayout.MODERN -> null
        SiteLayout.CLASSIC -> CLASSIC_STRUCTURE_ID
        SiteLayout.SITE -> chosen.ifBlank { null }
    }
    // Centered header logo only for the DB's own site theme in Site layout
    // (the same gate as site art).
    val centeredLogo = cfg.centeredHeaderLogo &&
        layout == SiteLayout.SITE && chosen in DB_THEME_IDS
    return ResolvedDbTheme(
        false, palette, structure, layout, cfg.siteColors, centeredLogo,
        hideTldInHeader = cfg.hideTldInHeader
    )
}

/**
 * Pseudo structure id for the Classic QDB presentation: bold #number +
 * score top bar, monospace typewriter body, bottom info bar. This IS the
 * qdb.us "Site" look, so Site and Classic QDB coincide for that theme.
 */
const val CLASSIC_STRUCTURE_ID = "classic_qdb"

/**
 * Maps a structure theme id to its site-presentation style. Database site
 * themes each get their own mini-site look; anything else (custom themes,
 * system/dynamic, other presets) falls back to the Classic QDB
 * presentation. v1.53: Bash.org and CrapShack get their own site chrome
 * too (same-color header/footer bars, black page outside the quotes).
 */
enum class SiteStyle {
    /** The app's standard card (only when site themes are bypassed). */
    MODERN_CARD,
    // v1.88.23 novelty card themes: bespoke presentations rendered by
    // NoveltyThemeCard (ui/components/NoveltyThemes.kt).
    /** White-on-black DOS console: C:\> header line, monospace. */
    DOS,
    /** Modern Mac window: traffic lights, rounded, light gray. */
    MACOS,
    /** Classic 1984 Mac: pinstripe title bar, 1-bit black on white. */
    MACINTOSH,
    /** Chat bubbles alternating blue/gray, one bubble per turn. */
    IMESSAGE,
    /** Material You RCS bubbles, expressive asymmetric rounding. */
    GMESSAGES,
    /** Blue Screen of Death: :( + QUOTE_FAULT on #0000AA. */
    BSOD,
    /** Terminal: black, green monospace, $ prompts, status bar. */
    LINUX,
    /** mIRC channel window: #quote404 bar, [hh:mm] stamps, mIRC nicks. */
    MIRC,
    /** Generic modern terminal: title bar, block cursor. */
    TERMINAL,
    /** P1 green phosphor: pure black, glowing green, no chrome. */
    GREEN_MONO,
    /** v1.88.25 novelty card themes. */
    /** BBS ANSI-art: bright multi-colored mono on black, block frames. */
    ANSI,
    /** Classic .nfo textmode: light-gray mono, ASCII box frame, no color. */
    ASCII,
    /** Digital rain: faint green katakana columns behind bright text. */
    MATRIX,
    /** Green terminal emulator window: title bar, block cursor. */
    GREEN_TERM,
    /** v1.88.27 novelty card themes. */
    /** Facebook Messenger light mode: white, gray incoming, gradient blue outgoing. */
    MESSENGER_LIGHT,
    /** Facebook Messenger dark mode: near-black, dark-gray incoming, gradient blue outgoing. */
    MESSENGER_DARK,
    /** v1.88.28 novelty card themes. */
    /** AOL Instant Messenger: blue title bar, red/blue nicks, Send button. */
    AIM,
    /** v1.88.28 novelty card themes. */
    /** ICQ: light-blue gradient client, flower logo, timestamped nicks. */
    ICQ,
    /** v1.88.28 novelty card themes. */
    /** MSN Messenger: butterfly logo, display pictures, "says:" lines. */
    MSN_MESSENGER,
    /** Bold #number + score top bar, monospace body, bottom info bar. */
    CLASSIC_QDB,
    /** Pale-orange #FFE9C9 highlight blocks, purple #number links. */
    QDB_MIT,
    /** Brick-red #number headers + speech-bubble logo. */
    QUOTEIRC,
    /** White beveled cards, monospace body, red accents. */
    BURNT_ELECTRONS,
    /** White rounded content frame, cyan links, gray meta. */
    WARPDRIVE,
    /** Minimalist white: hairline dividers, dark text, muted blue links. */
    BASHIM,
    /** "- Rating N -" bar with vote icons, alternating row shades. */
    WQDB,
    /** Blue #number, green/red vote arrows, monospace, header figures. */
    XKCDB,
    /** Tan/beige cards, thin orange borders, orange #number header. */
    FURCQDB,
    /** Light-gray bordered cards with orange headers. */
    IROX,
    /** Orange header/footer bars, white bordered body, monospace. */
    BASH,
    /** Deep-blue header/footer bars, dark bordered body. */
    CRAPSHACK,
    /** v1.88.38: Clint's Friends database — sunset chat bubbles on deep plum. */
    FRIENDS
}

fun siteStyleFor(structureThemeId: String?): SiteStyle {
    // v1.83: a plugin theme id maps through the pack's declared site
    // structure (what existing site art the theme mimics). The mapped id
    // is never a plugin id, so the recursion below always terminates.
    if (structureThemeId != null && structureThemeId.startsWith(PluginManager.DB_ID_PREFIX)) {
        return siteStyleFor(PluginManager.themeStructures[structureThemeId])
    }
    return when (structureThemeId) {
    null -> SiteStyle.MODERN_CARD
    CLASSIC_STRUCTURE_ID, "db_qdb_us" -> SiteStyle.CLASSIC_QDB
    "db_qdb_mit" -> SiteStyle.QDB_MIT
    "db_quoteirc" -> SiteStyle.QUOTEIRC
    "db_burntelectrons" -> SiteStyle.BURNT_ELECTRONS
    "db_warpdrive" -> SiteStyle.WARPDRIVE
    "db_bashim" -> SiteStyle.BASHIM
    "db_wqdb" -> SiteStyle.WQDB
    "db_xkcdb" -> SiteStyle.XKCDB
    "db_furcqdb" -> SiteStyle.FURCQDB
    "db_irox" -> SiteStyle.IROX
    "bashorg" -> SiteStyle.BASH
    "db_crapshack" -> SiteStyle.CRAPSHACK
    // v1.88.23 novelty card themes.
    "theme_dos" -> SiteStyle.DOS
    "theme_macos" -> SiteStyle.MACOS
    "theme_macintosh" -> SiteStyle.MACINTOSH
    "theme_imessage" -> SiteStyle.IMESSAGE
    "theme_gmessages" -> SiteStyle.GMESSAGES
    "theme_bsod" -> SiteStyle.BSOD
    "theme_linux" -> SiteStyle.LINUX
    "theme_mirc" -> SiteStyle.MIRC
    "theme_terminal" -> SiteStyle.TERMINAL
    "theme_greenmono" -> SiteStyle.GREEN_MONO
    // v1.88.25 novelty card themes.
    "theme_ansi" -> SiteStyle.ANSI
    "theme_ascii" -> SiteStyle.ASCII
    "theme_matrix" -> SiteStyle.MATRIX
    "theme_greenterm" -> SiteStyle.GREEN_TERM
    // v1.88.27 novelty card themes.
    "theme_messenger_light" -> SiteStyle.MESSENGER_LIGHT
    "theme_messenger_dark" -> SiteStyle.MESSENGER_DARK
    // v1.88.28 novelty card themes.
    "theme_aim" -> SiteStyle.AIM
    "theme_icq" -> SiteStyle.ICQ
    "theme_msn_messenger" -> SiteStyle.MSN_MESSENGER
    // v1.88.38: Clint's personal Friends database theme.
    "db_friends" -> SiteStyle.FRIENDS
    else -> SiteStyle.CLASSIC_QDB
    }
}

/** Which site styles render the quote body in monospace. */
fun siteStyleMonospace(style: SiteStyle): Boolean = when (style) {
    SiteStyle.CLASSIC_QDB, SiteStyle.BURNT_ELECTRONS, SiteStyle.XKCDB,
    SiteStyle.BASH, SiteStyle.WARPDRIVE,
    // v1.88.23 novelty terminals/consoles.
    SiteStyle.DOS, SiteStyle.MACINTOSH, SiteStyle.BSOD, SiteStyle.LINUX,
    SiteStyle.MIRC, SiteStyle.TERMINAL, SiteStyle.GREEN_MONO,
    // v1.88.25 textmode additions.
    SiteStyle.ANSI, SiteStyle.ASCII, SiteStyle.MATRIX,
    // v1.88.27 chat additions.
    SiteStyle.MESSENGER_LIGHT, SiteStyle.MESSENGER_DARK,
    SiteStyle.GREEN_TERM -> true
    // v1.88.28 chat additions use proportional fonts (noveltyPlainStyle),
    // not monospace — so AIM, ICQ and MSN_MESSENGER are NOT in the true branch.
    // v1.88.38: FRIENDS is a chat-bubble theme with proportional fonts too.
    SiteStyle.FRIENDS -> false
    else -> false
}

/**
 * v1.56: the effective "Quote background" for one database's cards — the
 * single resolution every card path uses (Browse, Random, single-quote
 * detail). Starts from the palette theme's slot (preset default, built-in
 * override, or custom theme), then the explicit per-database
 * "Quote background" (Settings → database themes) wins whenever set, in
 * every mode. The authentic site defaults are never changed here; the
 * editor value simply wins when present.
 */
fun quoteBackgroundForDb(
    resolved: ResolvedDbTheme,
    dbId: String?,
    snap: DbThemeSnapshots
): androidx.compose.ui.graphics.Color {
    val base = com.quotebrowser.app.ui.theme.quoteBackgroundFor(
        resolved.paletteThemeId, snap.customThemes, snap.builtInOverrides
    )
    if (resolved.bypassed || dbId == null) return base
    val hex = snap.configs[dbId]?.quoteBackgroundHex?.takeIf { it.isNotBlank() }
        ?: return base
    return com.quotebrowser.app.ui.hexToColorOrNull(hex) ?: base
}

/**
 * v1.56: the page background color for a single-database browsing context
 * (Browse / Random / detail). Null = keep the current behavior (the global
 * theme's background, e.g. All-databases mixed mode).
 *
 * - Explicit per-database "Page background" wins when set.
 * - Otherwise the database's own site page color: the active per-database
 *   palette theme's background slot (authentic site default — CrapShack
 *   black, qdb.us white — plus any built-in override). A palette that
 *   resolves back to the global theme keeps the global page, so dark site
 *   cards never sit on a mismatched white page (or vice versa).
 */
fun pageBackgroundFor(
    dbId: String?,
    snap: DbThemeSnapshots
): androidx.compose.ui.graphics.Color? {
    if (dbId == null || dbId == DatabaseManager.ALL_DBS_ID) return null
    val resolved = resolveDbTheme(snap, dbId)
    if (resolved.bypassed) return null
    snap.configs[dbId]?.pageBackgroundHex?.takeIf { it.isNotBlank() }?.let { hex ->
        com.quotebrowser.app.ui.hexToColorOrNull(hex)?.let { return it }
    }
    if (resolved.paletteThemeId == snap.globalThemeId) return null
    val bg = com.quotebrowser.app.ui.theme.effectivePreset(
        resolved.paletteThemeId, snap.builtInOverrides
    )?.background ?: return null
    return androidx.compose.ui.graphics.Color(bg)
}

/**
 * v1.56: the per-database page background image file name for a
 * single-database browsing context, or null when none is set (or the
 * database's theming is bypassed / mixed mode).
 */
fun pageBackgroundImageFor(dbId: String?, snap: DbThemeSnapshots): String? {
    if (dbId == null || dbId == DatabaseManager.ALL_DBS_ID) return null
    if (resolveDbTheme(snap, dbId).bypassed) return null
    return snap.configs[dbId]?.pageBackgroundImage?.takeIf { it.isNotBlank() }
}
