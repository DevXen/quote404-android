package com.quotebrowser.app.data.plugins

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.Room
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.QuoteDatabase
import com.quotebrowser.app.data.layouts.LayoutDef
import com.quotebrowser.app.data.layouts.LayoutParser
import com.quotebrowser.app.ui.DefaultNickNameColors
import com.quotebrowser.app.ui.theme.ThemePreset
import com.quotebrowser.app.ui.theme.defaultBracketTint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * v1.83: the QuoteBrowser plugin framework (Plugin API 1).
 *
 * Data packs only in v1 — no code is ever executed from a pack. A pack is
 * a folder under the app-private `plugins/` dir containing `plugin.json`
 * (the manifest) plus its data file. Packs are discovered by [scan],
 * validated (bad packs are listed with their error, never crash), and
 * exposed to the rest of the app:
 * - quote packs register as real databases with id `plugin:<id>`
 *   (see DatabaseManager hooks)
 * - theme packs register [ThemePreset]s with id `plugin:<id>:<key>`
 *   (see [themePresets], consulted by effectivePreset/themeColorScheme)
 *
 * The manifest declares all six pack types and reserves `signature` /
 * `settings` fields so the four future types (export styles, games,
 * voices, video backgrounds), per-plugin settings, and pack signing slot
 * in without breaking v1 packs or redesigning the loader.
 */
object PluginManager {

    const val PLUGIN_API_VERSION = 1
    const val DB_ID_PREFIX = "plugin:"
    private const val TAG = "PluginManager"
    private const val PREFS = "quote_browser_prefs"
    private const val KEY_ENABLED_PREFIX = "plugin_enabled_"
    private val ID_RE = Regex("^[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+$")
    private val THEME_KEY_RE = Regex("^[A-Za-z0-9_-]+$")

    /** Pack types understood by Plugin API 1. Unknown types are rejected. */
    const val TYPE_QUOTE_PACK = "quote-pack"
    const val TYPE_THEME_PACK = "theme-pack"
    // v1.85: layout packs ship quote layouts (arrangement + CSS). Data
    // only, like theme packs — no code ever runs from a pack.
    const val TYPE_LAYOUT_PACK = "layout-pack"
    // v1.83: reserved future types. The manifest, validation, and
    // apiVersion gating already know them; the loaders land in a later
    // plugin API. A v1 app refuses them with "needs a newer QuoteBrowser"
    // instead of crashing — no redesign needed when they ship.
    const val TYPE_EXPORT_STYLE = "export-style"
    const val TYPE_GAME_PACK = "game-pack"
    const val TYPE_VOICE_PACK = "voice-pack"
    const val TYPE_VIDEO_BACKGROUND = "video-background"
    private val KNOWN_TYPES = setOf(
        TYPE_QUOTE_PACK, TYPE_THEME_PACK, TYPE_LAYOUT_PACK, TYPE_EXPORT_STYLE,
        TYPE_GAME_PACK, TYPE_VOICE_PACK, TYPE_VIDEO_BACKGROUND
    )

    /**
     * Types this app version can actually load. v1.87: export styles and
     * voice packs joined the loadable set (data-only, like the rest — no
     * code ever runs from a pack). Game packs and video backgrounds stay
     * reserved for a later update.
     */
    private val SUPPORTED_TYPES = setOf(
        TYPE_QUOTE_PACK, TYPE_THEME_PACK, TYPE_LAYOUT_PACK,
        TYPE_EXPORT_STYLE, TYPE_VOICE_PACK
    )

    /**
     * Declared-permission vocabulary (v1.83). Every pack lists what it may
     * touch in `permissions`; unknown strings are rejected, and the
     * Plugins screen shows the list for install-time approval. v1 packs
     * are data-only so there is no code to sandbox yet — but the
     * declarations are validated now, and [hasPermission] is the gate
     * future capability APIs (corpus read for games, the export pipeline
     * for export styles / video backgrounds, TTS for voices) will check.
     * A pack can never silently exceed its declarations.
     */
    const val PERM_QUOTES_READ = "quotes.read"   // read the quote corpus (games, future types)
    const val PERM_STORAGE = "storage"           // files in its own plugin sandbox dir
    const val PERM_NETWORK = "network"           // make network requests
    const val PERM_TTS = "tts"                   // text-to-speech / audio playback (voices)
    const val PERM_AUDIO = "audio"               // audio playback (games)
    const val PERM_EXPORT = "export"             // hook the export pipeline / shared renderer
    private val KNOWN_PERMISSIONS = setOf(
        PERM_QUOTES_READ, PERM_STORAGE, PERM_NETWORK,
        PERM_TTS, PERM_AUDIO, PERM_EXPORT
    )

    /** Suggested permissions per pack type (authors may declare a subset). */
    fun suggestedPermissions(type: String): List<String> = when (type) {
        TYPE_QUOTE_PACK -> emptyList()
        TYPE_THEME_PACK -> emptyList()
        // v1.85: layout packs are arrangement + CSS only — no permissions.
        TYPE_LAYOUT_PACK -> emptyList()
        TYPE_EXPORT_STYLE -> listOf(PERM_EXPORT, PERM_STORAGE)
        TYPE_GAME_PACK -> listOf(PERM_QUOTES_READ, PERM_STORAGE)
        TYPE_VOICE_PACK -> listOf(PERM_TTS, PERM_STORAGE)
        TYPE_VIDEO_BACKGROUND -> listOf(PERM_EXPORT, PERM_STORAGE)
        else -> emptyList()
    }

    /** Structure ids a theme pack may reference (existing site art). */
    private val KNOWN_STRUCTURES = setOf(
        "classic_qdb", "bashorg", "db_crapshack", "db_qdb_us", "db_qdb_mit",
        "db_quoteirc", "db_burntelectrons", "db_warpdrive", "db_wqdb",
        "db_xkcdb", "db_furcqdb", "db_irox",
        // v1.88.38: Clint's personal Friends database theme.
        "db_friends"
    )

    data class PluginManifest(
        val id: String,
        val name: String,
        val version: String,
        val author: String,
        val apiVersion: Int,
        val type: String,
        val entry: String,
        val description: String,
        /** Declared permissions; every entry is validated against [KNOWN_PERMISSIONS]. */
        val permissions: List<String> = emptyList(),
        /**
         * Reserved for real pack signing (a later plugin API). Parsed and
         * carried, not verified in v1.
         */
        val signature: String = "",
        /**
         * Reserved per-plugin settings schema (v1.87: rendered into a
         * per-plugin settings page on the Plugins screen). Raw JSON,
         * parsed by [settingDefs].
         */
        val settingsSchema: String = "",
        /**
         * v1.87: optional one-tap update source — an https/http URL to a
         * zip of a newer version of this pack. "Check for updates" on the
         * Plugins screen downloads it, validates it, and offers the
         * update when its version is newer than the installed one.
         */
        val updateUrl: String = ""
    )

    data class PluginTheme(
        val key: String,
        val name: String,
        val preset: ThemePreset,
        /** Existing site-structure art this theme mimics, or null. */
        val structure: String?
    ) {
        /** Full theme id used everywhere themes are referenced. */
        fun themeId(packId: String): String = "$DB_ID_PREFIX$packId:$key"
    }

    /**
     * v1.87: one export style from an export-style pack. The entry file is
     * JSON (`{"styles": [...]}`); each style is a CSS snippet applied on
     * top of the active layout's CSS when exporting HTML.
     */
    data class PluginExportStyle(
        val key: String,
        val name: String,
        val css: String,
        val description: String = ""
    ) {
        /** Full style id used by the HTML export picker. */
        fun styleId(packId: String): String = "$DB_ID_PREFIX$packId:$key"
    }

    /**
     * v1.87: one voice from a voice pack. The entry file is JSON
     * (`{"voices": [...]}`). Gemini-engine voices are named aliases for
     * a prebuilt cloud voice (resolved to [baseVoice] at synthesis time);
     * built-in-engine voices are pitch/rate presets applied to the
     * device's default TTS voice.
     */
    data class PluginVoice(
        val key: String,
        val name: String,
        /** "gemini" or "builtin". */
        val engine: String,
        /** Prebuilt cloud voice name (gemini engine). */
        val baseVoice: String = "",
        /** Pitch multiplier (built-in engine), 0.25..4.0. */
        val pitch: Float = 1.0f,
        /** Speech-rate multiplier (built-in engine), 0.25..4.0. */
        val rate: Float = 1.0f,
        /** "male", "female", or "" (unspecified). */
        val gender: String = "",
        val description: String = ""
    ) {
        /** Full voice id stored wherever voices are referenced. */
        fun voiceId(packId: String): String = "$DB_ID_PREFIX$packId:$key"
    }

    data class PluginInfo(
        val manifest: PluginManifest,
        val dir: File,
        val enabled: Boolean,
        /** Non-null when the pack was rejected or failed to load. */
        val error: String?,
        val themes: List<PluginTheme> = emptyList(),
        val quoteCount: Int? = null,
        /** v1.85: parsed layouts from an enabled layout pack. */
        val layouts: List<LayoutDef> = emptyList(),
        /** v1.87: parsed export styles from an enabled export-style pack. */
        val exportStyles: List<PluginExportStyle> = emptyList(),
        /** v1.87: parsed voices from an enabled voice pack. */
        val voices: List<PluginVoice> = emptyList(),
        /**
         * SHA-256 of the entry file at scan time (author identity is
         * [PluginManifest.author]). Shown on the Plugins screen so the
         * user can confirm pack integrity; the install marker records it
         * so silent in-place edits are detected and rebuilt.
         */
        val entryChecksum: String = ""
    )

    private val _plugins = MutableStateFlow<List<PluginInfo>>(emptyList())
    /** All discovered packs (valid and rejected), in install order. */
    val plugins: StateFlow<List<PluginInfo>> = _plugins.asStateFlow()

    /**
     * Resolved theme presets from enabled theme packs, keyed by full
     * theme id (`plugin:<id>:<key>`). Consulted by effectivePreset() and
     * themeColorScheme() so plugin themes style the app everywhere.
     */
    @Volatile
    var themePresets: Map<String, ThemePreset> = emptyMap()
        private set

    /**
     * theme id -> site structure id the theme mimics (subset of
     * [themePresets]). Consulted by siteStyleFor().
     */
    @Volatile
    var themeStructures: Map<String, String> = emptyMap()
        private set

    /** database id (`plugin:<id>`) -> pack display name. */
    @Volatile
    var dbLabels: Map<String, String> = emptyMap()
        private set

    /**
     * v1.85: resolved layouts from enabled layout packs, keyed by full
     * layout id (`plugin:<packId>:<layoutId>`). Consulted by
     * LayoutStore.allLayouts() so pack layouts appear in the picker.
     */
    @Volatile
    var pluginLayouts: Map<String, LayoutDef> = emptyMap()
        private set

    /**
     * v1.87: resolved export styles from enabled export-style packs,
     * keyed by full style id (`plugin:<packId>:<key>`). Consulted by the
     * HTML export picker in ExportScreen.
     */
    @Volatile
    var exportStyles: Map<String, PluginExportStyle> = emptyMap()
        private set

    /**
     * v1.87: resolved voices from enabled voice packs, keyed by full
     * voice id (`plugin:<packId>:<key>`). Consulted by the voice pickers
     * and resolved to concrete voices at synthesis time.
     */
    @Volatile
    var pluginVoices: Map<String, PluginVoice> = emptyMap()
        private set

    /** Full export-style id used by the HTML export picker. */
    fun exportStyleId(packId: String, key: String): String = "$DB_ID_PREFIX$packId:$key"

    /** Full voice id stored wherever voices are referenced. */
    fun pluginVoiceId(packId: String, key: String): String = "$DB_ID_PREFIX$packId:$key"

    /** Pack id embedded in a `plugin:<packId>:<key>` id, or null. */
    private fun packIdOf(fullId: String): String? =
        fullId.removePrefix(DB_ID_PREFIX).substringBefore(":")
            .takeIf { fullId.startsWith(DB_ID_PREFIX) && ":" in fullId.removePrefix(DB_ID_PREFIX) }

    /** Display name of the pack that owns [fullId], or null. */
    private fun packNameOf(fullId: String): String? {
        val packId = packIdOf(fullId) ?: return null
        return _plugins.value.firstOrNull { it.manifest.id == packId }?.manifest?.name
    }

    /** (style id, "Name (Pack)") for every enabled export-style pack style. */
    fun exportStyleOptions(): List<Pair<String, String>> =
        exportStyles.entries
            .sortedBy { it.value.name.lowercase() }
            .map { (id, s) ->
                val pack = packNameOf(id)
                id to if (pack != null) "${s.name} ($pack)" else s.name
            }

    /** (voice id, "Name (Pack)") for every enabled voice-pack voice on [engine]. */
    fun pluginVoiceOptions(engine: String): List<Pair<String, String>> =
        pluginVoices.entries
            .filter { it.value.engine == engine }
            .sortedBy { it.value.name.lowercase() }
            .map { (id, v) ->
                val pack = packNameOf(id)
                id to if (pack != null) "${v.name} ($pack)" else v.name
            }

    /**
     * Display label for a plugin export-style id ("Name (Pack)"); the
     * raw id when unknown (e.g. the pack was uninstalled).
     */
    fun pluginExportStyleDisplayName(id: String): String =
        exportStyles[id]?.let { s ->
            val pack = packNameOf(id)
            if (pack != null) "${s.name} ($pack)" else s.name
        } ?: id

    /**
     * Display label for a plugin voice id ("Name (Pack)"); the raw id
     * when unknown (e.g. the pack was uninstalled).
     */
    fun pluginVoiceDisplayName(id: String): String =
        pluginVoices[id]?.let { v ->
            val pack = packNameOf(id)
            if (pack != null) "${v.name} ($pack)" else v.name
        } ?: id

    /**
     * Resolve a plugin voice id to its concrete Gemini prebuilt voice
     * name for synthesis. Null when the id is not a loadable
     * gemini-engine plugin voice (unknown, disabled, or uninstalled) —
     * callers treat null as "not a plugin voice".
     */
    fun resolveGeminiVoiceName(id: String): String? =
        pluginVoices[id]
            ?.takeIf { it.engine == "gemini" && it.baseVoice.isNotBlank() }
            ?.baseVoice

    /**
     * Resolve a plugin voice id to its (pitch, rate) preset for the
     * built-in TTS engine. Null when the id is not a loadable
     * builtin-engine plugin voice.
     */
    fun resolveBuiltinVoicePitchRate(id: String): Pair<Float, Float>? =
        pluginVoices[id]
            ?.takeIf { it.engine == "builtin" }
            ?.let { it.pitch to it.rate }

    /** Full layout id used everywhere layouts are referenced. */
    fun layoutId(packId: String, layoutId: String): String = "$DB_ID_PREFIX$packId:$layoutId"

    private val scanLock = Any()

    fun pluginsDir(context: Context): File = File(context.filesDir, "plugins")

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun dirNameFor(pluginId: String): String =
        pluginId.replace(Regex("[^A-Za-z0-9._-]"), "_")

    fun isEnabled(context: Context, pluginId: String): Boolean =
        prefs(context).getBoolean(KEY_ENABLED_PREFIX + dirNameFor(pluginId), true)

    fun setEnabled(context: Context, pluginId: String, enabled: Boolean) {
        try {
            prefs(context).edit()
                .putBoolean(KEY_ENABLED_PREFIX + dirNameFor(pluginId), enabled)
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "setEnabled failed", e)
        }
        // Re-emit with the new flag and rebuild the registries. Cheap and
        // keeps every consumer (DB list, theme picker) in sync.
        synchronized(scanLock) {
            val ctx = context.applicationContext
            _plugins.value = _plugins.value.map {
                if (it.manifest.id == pluginId) it.copy(enabled = enabled)
                else it
            }
            rebuildRegistries(ctx)
        }
    }

    /** Display name for a plugin database id, or null when unknown. */
    fun dbLabel(pluginId: String): String? = dbLabels[DB_ID_PREFIX + pluginId]

    /**
     * Full scan of the plugins dir. Never throws — every failure is
     * captured as a [PluginInfo.error]. Safe to call from a background
     * thread; callers must NOT call this on the main thread.
     */
    fun scan(context: Context) {
        synchronized(scanLock) {
            try {
                scanLocked(context.applicationContext)
            } catch (e: Exception) {
                Log.w(TAG, "plugin scan failed", e)
            }
        }
    }

    private fun scanLocked(context: Context) {
        val dir = pluginsDir(context)
        try {
            dir.mkdirs()
        } catch (e: Exception) {
            Log.w(TAG, "cannot create plugins dir", e)
            return
        }
        installSamplePacks(context, dir)
        val found = mutableListOf<PluginInfo>()
        val seenIds = mutableSetOf<String>()
        val subdirs = try {
            dir.listFiles { f -> f.isDirectory }?.sortedBy { it.name }
        } catch (e: Exception) {
            null
        } ?: emptyList()
        for (packDir in subdirs) {
            val manifestFile = File(packDir, "plugin.json")
            if (!manifestFile.isFile) continue // not a pack; ignore quietly
            val (manifest, manifestError) = readManifest(manifestFile)
            if (manifest == null) {
                found.add(
                    PluginInfo(
                        manifest = PluginManifest(
                            id = packDir.name, name = packDir.name,
                            version = "", author = "", apiVersion = 0,
                            type = "", entry = "", description = ""
                        ),
                        dir = packDir, enabled = false,
                        error = manifestError ?: "Bad plugin.json"
                    )
                )
                continue
            }
            if (!seenIds.add(manifest.id)) {
                found.add(
                    PluginInfo(
                        manifest, packDir, enabled = false,
                        error = "Duplicate plugin id \"${manifest.id}\" — only the first was loaded"
                    )
                )
                continue
            }
            val enabled = isEnabled(context, manifest.id)
            val entryFile = safeEntryFile(packDir, manifest.entry)
            if (entryFile == null || !entryFile.isFile) {
                found.add(
                    PluginInfo(
                        manifest, packDir, enabled = enabled,
                        error = "Entry file \"${manifest.entry}\" is missing"
                    )
                )
                continue
            }
            when (manifest.type) {
                TYPE_QUOTE_PACK -> {
                    val quotes = try {
                        readQuotePack(entryFile)
                    } catch (e: Exception) {
                        Log.w(TAG, "quote pack ${manifest.id} failed to parse", e)
                        found.add(
                            PluginInfo(
                                manifest, packDir, enabled = enabled,
                                error = "Could not parse ${manifest.entry}: ${e.message}",
                                entryChecksum = sha256(entryFile)
                            )
                        )
                        continue
                    }
                    var dbError: String? = null
                    var count: Int? = null
                    if (enabled) {
                        try {
                            count = ensureQuotePackDbLocked(context, manifest, packDir, entryFile, quotes)
                        } catch (e: Exception) {
                            Log.w(TAG, "quote pack ${manifest.id} db build failed", e)
                            dbError = "Could not build quote database: ${e.message}"
                        }
                    }
                    found.add(
                        PluginInfo(
                            manifest, packDir, enabled = enabled,
                            error = dbError, quoteCount = count,
                            entryChecksum = sha256(entryFile)
                        )
                    )
                }
                TYPE_THEME_PACK -> {
                    val themes = try {
                        readThemePack(manifest, entryFile)
                    } catch (e: Exception) {
                        Log.w(TAG, "theme pack ${manifest.id} failed to parse", e)
                        found.add(
                            PluginInfo(
                                manifest, packDir, enabled = enabled,
                                error = "Could not parse ${manifest.entry}: ${e.message}",
                                entryChecksum = sha256(entryFile)
                            )
                        )
                        continue
                    }
                    found.add(
                        PluginInfo(
                            manifest, packDir, enabled = enabled,
                            error = if (themes.isEmpty()) "No valid themes in ${manifest.entry}" else null,
                            themes = themes,
                            entryChecksum = sha256(entryFile)
                        )
                    )
                }
                TYPE_LAYOUT_PACK -> {
                    // v1.85: layout packs. The entry file is a layout.json
                    // (same schema as the bundled premades); an optional
                    // sibling "style.css" overrides the layout's css.
                    val layout = try {
                        val parsed = LayoutParser.parse(entryFile.readText())
                        val cssFile = File(packDir, "style.css")
                        val css = if (cssFile.isFile) {
                            try { cssFile.readText() } catch (e: Exception) { parsed.css }
                        } else parsed.css
                        parsed.copy(
                            id = layoutId(manifest.id, parsed.id),
                            css = css,
                            source = "plugin"
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "layout pack ${manifest.id} failed to parse", e)
                        found.add(
                            PluginInfo(
                                manifest, packDir, enabled = enabled,
                                error = "Could not parse ${manifest.entry}: ${e.message}",
                                entryChecksum = sha256(entryFile)
                            )
                        )
                        continue
                    }
                    found.add(
                        PluginInfo(
                            manifest, packDir, enabled = enabled,
                            error = null,
                            layouts = listOf(layout),
                            entryChecksum = sha256(entryFile)
                        )
                    )
                }
                TYPE_EXPORT_STYLE -> {
                    // v1.87: export-style packs. The entry file is JSON:
                    // {"styles": [{"key", "name", "css", "description"}]}.
                    // Each style's CSS is layered on top of the active
                    // layout's CSS in HTML export.
                    val styles = try {
                        readExportStylePack(entryFile)
                    } catch (e: Exception) {
                        Log.w(TAG, "export-style pack ${manifest.id} failed to parse", e)
                        found.add(
                            PluginInfo(
                                manifest, packDir, enabled = enabled,
                                error = "Could not parse ${manifest.entry}: ${e.message}",
                                entryChecksum = sha256(entryFile)
                            )
                        )
                        continue
                    }
                    found.add(
                        PluginInfo(
                            manifest, packDir, enabled = enabled,
                            error = if (styles.isEmpty()) "No valid styles in ${manifest.entry}" else null,
                            exportStyles = styles,
                            entryChecksum = sha256(entryFile)
                        )
                    )
                }
                TYPE_VOICE_PACK -> {
                    // v1.87: voice packs. The entry file is JSON:
                    // {"voices": [{"key", "name", "engine", "baseVoice",
                    //   "pitch", "rate", "gender", "description"}]}.
                    val voices = try {
                        readVoicePack(entryFile)
                    } catch (e: Exception) {
                        Log.w(TAG, "voice pack ${manifest.id} failed to parse", e)
                        found.add(
                            PluginInfo(
                                manifest, packDir, enabled = enabled,
                                error = "Could not parse ${manifest.entry}: ${e.message}",
                                entryChecksum = sha256(entryFile)
                            )
                        )
                        continue
                    }
                    found.add(
                        PluginInfo(
                            manifest, packDir, enabled = enabled,
                            error = if (voices.isEmpty()) "No valid voices in ${manifest.entry}" else null,
                            voices = voices,
                            entryChecksum = sha256(entryFile)
                        )
                    )
                }
                else -> {
                    // Known but not loadable by this app version (readManifest
                    // already rejected these, so this is unreachable — kept
                    // so future supported types slot in here).
                    found.add(
                        PluginInfo(
                            manifest, packDir, enabled = false,
                            error = "Pack type \"${manifest.type}\" is not supported by this QuoteBrowser version"
                        )
                    )
                }
            }
        }
        _plugins.value = found
        rebuildRegistries(context)
    }

    private fun rebuildRegistries(context: Context) {
        val presets = mutableMapOf<String, ThemePreset>()
        val structures = mutableMapOf<String, String>()
        val labels = mutableMapOf<String, String>()
        val layouts = mutableMapOf<String, LayoutDef>()
        val styles = mutableMapOf<String, PluginExportStyle>()
        val voices = mutableMapOf<String, PluginVoice>()
        for (info in _plugins.value) {
            if (info.error != null || !info.enabled) continue
            val id = info.manifest.id
            if (info.manifest.type == TYPE_QUOTE_PACK) {
                labels[DB_ID_PREFIX + id] = info.manifest.name
            } else if (info.manifest.type == TYPE_THEME_PACK) {
                for (t in info.themes) {
                    val themeId = t.themeId(id)
                    presets[themeId] = t.preset
                    t.structure?.let { structures[themeId] = it }
                }
            } else if (info.manifest.type == TYPE_LAYOUT_PACK) {
                for (l in info.layouts) {
                    layouts[l.id] = l
                }
            } else if (info.manifest.type == TYPE_EXPORT_STYLE) {
                for (s in info.exportStyles) {
                    styles[s.styleId(id)] = s
                }
            } else if (info.manifest.type == TYPE_VOICE_PACK) {
                for (v in info.voices) {
                    voices[v.voiceId(id)] = v
                }
            }
        }
        themePresets = presets
        themeStructures = structures
        dbLabels = labels
        pluginLayouts = layouts
        exportStyles = styles
        pluginVoices = voices
    }

    // ------------------------------------------------------------------
    // Manifest
    // ------------------------------------------------------------------

    private fun readManifest(file: File): Pair<PluginManifest?, String?> {
        val o = try {
            JSONObject(file.readText())
        } catch (e: Exception) {
            return null to "plugin.json is not valid JSON: ${e.message}"
        }
        fun req(key: String): String? {
            val v = o.optString(key, "").trim()
            return if (v.isEmpty()) null else v
        }
        val id = req("id") ?: return null to "manifest is missing \"id\""
        val name = req("name") ?: return null to "manifest is missing \"name\""
        val version = req("version") ?: return null to "manifest is missing \"version\""
        val author = req("author") ?: return null to "manifest is missing \"author\""
        val type = req("type") ?: return null to "manifest is missing \"type\""
        val entry = req("entry") ?: return null to "manifest is missing \"entry\""
        if (!ID_RE.matches(id)) {
            return null to "bad plugin id \"$id\" — use reverse-domain, e.g. com.example.pack"
        }
        val apiVersion = o.optInt("apiVersion", -1)
        if (apiVersion < 1) return null to "manifest is missing \"apiVersion\""
        if (apiVersion > PLUGIN_API_VERSION) {
            return null to "pack needs plugin API $apiVersion — this QuoteBrowser supports up to $PLUGIN_API_VERSION (update the app)"
        }
        if (type !in KNOWN_TYPES) {
            return null to "unknown pack type \"$type\" — this QuoteBrowser knows: ${KNOWN_TYPES.sorted().joinToString(", ")}"
        }
        if (type !in SUPPORTED_TYPES) {
            return null to "pack type \"$type\" needs a newer QuoteBrowser — this version loads: ${SUPPORTED_TYPES.sorted().joinToString(", ")}"
        }
        val permissions = mutableListOf<String>()
        val permArr = o.optJSONArray("permissions")
        if (permArr != null) {
            for (i in 0 until permArr.length()) {
                val p = permArr.optString(i, "").trim()
                if (p.isEmpty()) continue
                if (p !in KNOWN_PERMISSIONS) {
                    return null to "unknown permission \"$p\" — known permissions: ${KNOWN_PERMISSIONS.sorted().joinToString(", ")}"
                }
                if (p !in permissions) permissions.add(p)
            }
        }
        // v1.87: optional update source. Only http(s) URLs are honored;
        // anything else is ignored (never fails validation).
        val updateUrlRaw = o.optString("updateUrl", "").trim()
        val updateUrl =
            if (updateUrlRaw.startsWith("https://") || updateUrlRaw.startsWith("http://")) updateUrlRaw
            else ""
        return PluginManifest(
            id = id, name = name, version = version, author = author,
            apiVersion = apiVersion, type = type, entry = entry,
            description = o.optString("description", ""),
            permissions = permissions,
            signature = o.optString("signature", ""),
            settingsSchema = o.opt("settings")?.toString().orEmpty(),
            updateUrl = updateUrl
        ) to null
    }

    /** Resolve the entry file, guarding against path traversal. */
    private fun safeEntryFile(packDir: File, entry: String): File? {
        if (entry.isBlank() || ".." in entry || entry.startsWith("/") || ":" in entry) return null
        return try {
            val f = File(packDir, entry).canonicalFile
            if (!f.path.startsWith(packDir.canonicalPath + File.separator)) null else f
        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------
    // Quote packs
    // ------------------------------------------------------------------

    /** DbInfo entries for every enabled, healthy quote pack. */
    fun quotePackDbInfos(): List<DbInfo> =
        _plugins.value
            .filter { it.error == null && it.enabled && it.manifest.type == TYPE_QUOTE_PACK }
            .map { DbInfo(id = DB_ID_PREFIX + it.manifest.id, label = it.manifest.name) }

    /** Database ids (`plugin:<id>`) of enabled, healthy quote packs. */
    fun quotePackDbIds(): List<String> = quotePackDbInfos().map { it.id }

    /** Room database file for a quote pack (inside the pack dir). */
    fun pluginDbFile(context: Context, pluginId: String): File =
        File(File(pluginsDir(context), dirNameFor(pluginId)), "quotes.db")

    /**
     * Make sure the pack's Room database exists and is current; rebuild it
     * when the manifest version or entry file changes. Returns the quote
     * count, or throws on failure. Never called on the main thread.
     */
    fun ensureQuotePackDb(context: Context, pluginId: String): File {
        synchronized(scanLock) {
            val ctx = context.applicationContext
            val info = _plugins.value.firstOrNull {
                it.manifest.id == pluginId &&
                    it.manifest.type == TYPE_QUOTE_PACK
            } ?: throw IllegalArgumentException("Unknown quote pack: $pluginId")
            if (!info.enabled) throw IllegalArgumentException("Quote pack disabled: $pluginId")
            if (info.error != null) throw IllegalArgumentException("Quote pack broken: $pluginId")
            val entryFile = safeEntryFile(info.dir, info.manifest.entry)
                ?: throw IllegalArgumentException("Quote pack entry missing: $pluginId")
            val quotes = readQuotePack(entryFile)
            ensureQuotePackDbLocked(ctx, info.manifest, info.dir, entryFile, quotes)
            return pluginDbFile(ctx, pluginId)
        }
    }

    /**
     * Returns the number of quotes installed. Rebuilds the Room file when
     * the install marker doesn't match the current manifest version +
     * entry checksum — so updating a pack (or silently editing its entry
     * file) is picked up on the next scan.
     */
    private fun ensureQuotePackDbLocked(
        context: Context,
        manifest: PluginManifest,
        packDir: File,
        entryFile: File,
        quotes: List<Quote>
    ): Int {
        val dbFile = pluginDbFile(context, manifest.id)
        val marker = File(packDir, ".installed.json")
        val checksum = sha256(entryFile)
        val markerOk = try {
            if (!marker.isFile) false else {
                val o = JSONObject(marker.readText())
                o.optString("version") == manifest.version &&
                    o.optString("entrySha256") == checksum &&
                    dbFile.isFile
            }
        } catch (e: Exception) {
            false
        }
        if (!markerOk) {
            try {
                if (dbFile.exists()) dbFile.delete()
                dbFile.parentFile?.mkdirs()
            } catch (e: Exception) {
                // best effort; Room will surface real failures
            }
            val db = Room.databaseBuilder(
                context, QuoteDatabase::class.java, dbFile.absolutePath
            ).build()
            try {
                // Fresh Room file == current schema (v3); no migrations needed.
                // Chunked so very large packs don't blow the Binder/statement budget.
                kotlinx.coroutines.runBlocking {
                    quotes.chunked(500).forEach { db.quoteDao().insertAll(it) }
                }
            } finally {
                try {
                    db.close()
                } catch (_: Exception) {
                }
            }
            try {
                marker.writeText(
                    JSONObject()
                        .put("version", manifest.version)
                        .put("entrySha256", checksum)
                        .toString()
                )
            } catch (e: Exception) {
                Log.w(TAG, "could not write install marker", e)
            }
        }
        return quotes.size
    }

    /**
     * Parse a quote-pack entry file: either a bare JSON array or
     * `{"quotes": [...]}`. Bad entries are skipped (never fatal).
     */
    fun readQuotePack(entryFile: File): List<Quote> {
        val text = entryFile.readText()
        val arr: JSONArray = try {
            val t = text.trimStart()
            if (t.startsWith("[")) JSONArray(text)
            else JSONObject(text).optJSONArray("quotes") ?: JSONArray()
        } catch (e: Exception) {
            throw IllegalArgumentException("not valid JSON: ${e.message}")
        }
        val out = mutableListOf<Quote>()
        val seen = mutableSetOf<Int>()
        var skipped = 0
        for (i in 0 until arr.length()) {
            try {
                val o = arr.getJSONObject(i)
                val number = o.optInt("number", Int.MIN_VALUE)
                val qtext = o.optString("text", "").trim()
                if (number == Int.MIN_VALUE || qtext.isEmpty()) {
                    skipped++
                    continue
                }
                if (!seen.add(number)) {
                    skipped++
                    continue
                }
                val rating = if (o.isNull("rating")) null else o.optInt("rating")
                val status = o.optString("status", "approved").trim().lowercase()
                out.add(
                    Quote(
                        number = number,
                        rating = rating,
                        text = qtext,
                        url = o.optString("url", ""),
                        status = if (status == "queued") "queued" else "approved"
                    )
                )
            } catch (e: Exception) {
                skipped++
            }
        }
        if (out.isEmpty()) throw IllegalArgumentException("no usable quotes (skipped $skipped)")
        if (skipped > 0) Log.i(TAG, "quote pack: skipped $skipped bad entries")
        return out
    }

    // ------------------------------------------------------------------
    // Theme packs
    // ------------------------------------------------------------------

    /** Parse a theme-pack entry file into validated plugin themes. */
    fun readThemePack(manifest: PluginManifest, entryFile: File): List<PluginTheme> {
        val o = try {
            JSONObject(entryFile.readText())
        } catch (e: Exception) {
            throw IllegalArgumentException("not valid JSON: ${e.message}")
        }
        val arr = o.optJSONArray("themes") ?: JSONArray()
        val out = mutableListOf<PluginTheme>()
        val seenKeys = mutableSetOf<String>()
        for (i in 0 until arr.length()) {
            try {
                val t = arr.getJSONObject(i)
                val key = t.optString("key", "").trim()
                val name = t.optString("name", "").trim()
                if (key.isEmpty() || name.isEmpty() || !THEME_KEY_RE.matches(key)) continue
                if (!seenKeys.add(key)) continue
                val dark = t.optBoolean("dark", true)
                // Spec: a theme with any explicitly-supplied invalid color is
                // skipped (absent colors fall back to defaults).
                val colorFields = listOf(
                    "primary", "background", "surface", "onBackground",
                    "onSurface", "number", "rating", "ratingText",
                    "quoteBackground", "divider", "nickBrackets", "nickAction"
                )
                val hasBadColor = colorFields.any { f ->
                    val raw = t.optString(f, "").trim()
                    raw.isNotEmpty() && parseHexColor(raw) == null
                }
                if (hasBadColor) continue
                fun hex(field: String, fallback: Long): Long =
                    parseHexColor(t.optString(field, "")) ?: fallback
                fun hexOpt(field: String): Long? =
                    t.optString(field, "").trim().takeIf { it.isNotEmpty() }
                        ?.let { parseHexColor(it) }
                val primary = hex("primary", 0xFF0090FF)
                val background = hex("background", if (dark) 0xFF000000 else 0xFFFFFFFF)
                val surface = hex("surface", if (dark) 0xFF0A0E14 else 0xFFF5F5F5)
                val onBackground = hex("onBackground", if (dark) 0xFFFFFFFF else 0xFF000000)
                val onSurface = hex("onSurface", if (dark) 0xFFFFFFFF else 0xFF000000)
                val number = hexOpt("number") ?: primary
                val rating = hexOpt("rating") ?: 0xFF90CAF9
                val ratingText = hexOpt("ratingText")
                    ?: if (isLightColor(rating)) 0xFF000000 else 0xFFFFFFFF
                val quoteBackground = hexOpt("quoteBackground") ?: background
                val divider = hexOpt("divider")
                    ?: if (dark) 0xFF494949 else 0xFFCAC4D0
                val nickBrackets = hexOpt("nickBrackets") ?: 0xFF7FC4FF
                val nickAction = hexOpt("nickAction") ?: 0xFF4CAF50
                val nickNames =
                    if (dark) DefaultNickNameColors else DefaultNickNameColors
                val structureRaw = t.optString("structure", "classic_qdb").trim()
                val structure =
                    if (structureRaw.isEmpty() || structureRaw !in KNOWN_STRUCTURES) "classic_qdb"
                    else structureRaw
                out.add(
                    PluginTheme(
                        key = key,
                        name = name,
                        preset = ThemePreset(
                            id = "$DB_ID_PREFIX${manifest.id}:$key",
                            name = name,
                            dark = dark,
                            background = background,
                            surface = surface,
                            primary = primary,
                            onBackground = onBackground,
                            onSurface = onSurface,
                            number = number,
                            rating = rating,
                            ratingText = ratingText,
                            nickBrackets = nickBrackets,
                            nickNames = nickNames,
                            nickBracketPalette = nickNames.map { defaultBracketTint(it, dark) },
                            nickAction = nickAction,
                            divider = divider,
                            quoteBackground = quoteBackground
                        ),
                        structure = structure
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "skipping bad theme entry #$i", e)
            }
        }
        return out
    }

    /**
     * v1.87: parse an export-style pack entry file:
     * `{"styles": [{"key", "name", "css", "description"}]}`. Bad entries
     * are skipped; an empty result fails the pack.
     */
    fun readExportStylePack(entryFile: File): List<PluginExportStyle> {
        val o = try {
            JSONObject(entryFile.readText())
        } catch (e: Exception) {
            throw IllegalArgumentException("not valid JSON: ${e.message}")
        }
        val arr = o.optJSONArray("styles") ?: JSONArray()
        val out = mutableListOf<PluginExportStyle>()
        val seenKeys = mutableSetOf<String>()
        for (i in 0 until arr.length()) {
            try {
                val s = arr.getJSONObject(i)
                val key = s.optString("key", "").trim()
                val name = s.optString("name", "").trim()
                val css = s.optString("css", "")
                if (key.isEmpty() || name.isEmpty() || css.isBlank() ||
                    !THEME_KEY_RE.matches(key)
                ) continue
                if (!seenKeys.add(key)) continue
                // 512 KB sanity cap per style — CSS is text.
                if (css.length > 512 * 1024) continue
                out.add(
                    PluginExportStyle(
                        key = key,
                        name = name,
                        css = css,
                        description = s.optString("description", "")
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "skipping bad export style #$i", e)
            }
        }
        return out
    }

    /**
     * v1.87: parse a voice-pack entry file:
     * `{"voices": [{"key", "name", "engine", "baseVoice", "pitch",
     * "rate", "gender", "description"}]}`. Bad entries are skipped; an
     * empty result fails the pack.
     */
    fun readVoicePack(entryFile: File): List<PluginVoice> {
        val o = try {
            JSONObject(entryFile.readText())
        } catch (e: Exception) {
            throw IllegalArgumentException("not valid JSON: ${e.message}")
        }
        val arr = o.optJSONArray("voices") ?: JSONArray()
        val out = mutableListOf<PluginVoice>()
        val seenKeys = mutableSetOf<String>()
        for (i in 0 until arr.length()) {
            try {
                val v = arr.getJSONObject(i)
                val key = v.optString("key", "").trim()
                val name = v.optString("name", "").trim()
                if (key.isEmpty() || name.isEmpty() || !THEME_KEY_RE.matches(key)) continue
                if (!seenKeys.add(key)) continue
                val engine = v.optString("engine", "gemini").trim().lowercase()
                if (engine != "gemini" && engine != "builtin") continue
                val baseVoice = v.optString("baseVoice", "").trim()
                if (engine == "gemini") {
                    // Gemini pack voices alias a real prebuilt cloud voice.
                    if (baseVoice !in com.quotebrowser.app.ui.GeminiPrebuiltVoices) continue
                }
                fun clamped(raw: Double, def: Float): Float =
                    raw.toFloat().let { if (it.isNaN()) def else it.coerceIn(0.25f, 4.0f) }
                val pitch = clamped(v.optDouble("pitch", 1.0), 1.0f)
                val rate = clamped(v.optDouble("rate", 1.0), 1.0f)
                val gender = v.optString("gender", "").trim().lowercase()
                    .takeIf { it == "male" || it == "female" }.orEmpty()
                out.add(
                    PluginVoice(
                        key = key,
                        name = name,
                        engine = engine,
                        baseVoice = baseVoice,
                        pitch = pitch,
                        rate = rate,
                        gender = gender,
                        description = v.optString("description", "")
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "skipping bad voice #$i", e)
            }
        }
        return out
    }

    /**
     * Parse "#RRGGBB" / "RRGGBB" / "AARRGGBB". Null when invalid —
     * callers fall back to defaults instead of failing.
     */
    fun parseHexColor(raw: String): Long? {
        val h = raw.trim().removePrefix("#").removePrefix("0x")
        if (h.length != 6 && h.length != 8) return null
        return try {
            val v = h.toLong(16)
            if (h.length == 6) v or 0xFF000000L else v
        } catch (e: Exception) {
            null
        }
    }

    private fun isLightColor(argb: Long): Boolean {
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        return r * 0.299f + g * 0.587f + b * 0.114f >= 0.5f
    }

    private fun sha256(file: File): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { ins ->
                val buf = ByteArray(8192)
                var n: Int
                while (ins.read(buf).also { n = it } > 0) md.update(buf, 0, n)
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fall back to length+mtime so a changed file still rebuilds.
            "${file.length()}:${file.lastModified()}"
        }
    }

    // ------------------------------------------------------------------
    // v1.87: per-plugin settings
    //
    // A pack declares a settings schema in its manifest ("settings"):
    // {"settings": [{"key", "type", "title", "description", "default",
    // "options"}]}. Types: "toggle" (boolean), "text" (free text),
    // "choice" (one of "options"). The Plugins screen renders one settings
    // page per pack from the schema; values live in their own prefs file,
    // keyed by plugin id, so they simply go dormant if the pack is
    // uninstalled and come back if it is reinstalled. Pack
    // code never runs, so settings are plain stored values the app's own
    // UI reads through [getSetting].
    // ------------------------------------------------------------------

    data class PluginSettingDef(
        val key: String,
        /** "toggle" | "text" | "choice". */
        val type: String,
        val title: String,
        val description: String = "",
        /** Stored default, as a string ("true"/"false" for toggles). */
        val default: String = "",
        /** Allowed values for "choice". */
        val options: List<String> = emptyList()
    )

    private const val PLUGIN_SETTINGS_PREFS = "plugin_settings_v187"

    /**
     * Parse the manifest's settings schema into setting definitions.
     * Empty when the pack declares none (or the schema is malformed —
     * never fatal).
     */
    fun settingDefs(pluginId: String): List<PluginSettingDef> {
        val raw = _plugins.value.firstOrNull { it.manifest.id == pluginId }
            ?.manifest?.settingsSchema.orEmpty()
        if (raw.isBlank()) return emptyList()
        return try {
            val o = JSONObject(raw)
            val arr = o.optJSONArray("settings") ?: JSONArray()
            val out = mutableListOf<PluginSettingDef>()
            val seen = mutableSetOf<String>()
            for (i in 0 until arr.length()) {
                try {
                    val s = arr.getJSONObject(i)
                    val key = s.optString("key", "").trim()
                    val type = s.optString("type", "").trim().lowercase()
                    val title = s.optString("title", "").trim()
                    if (key.isEmpty() || title.isEmpty() ||
                        !THEME_KEY_RE.matches(key) ||
                        type !in setOf("toggle", "text", "choice")
                    ) continue
                    if (!seen.add(key)) continue
                    val options = mutableListOf<String>()
                    val optArr = s.optJSONArray("options")
                    if (optArr != null) {
                        for (j in 0 until optArr.length()) {
                            val v = optArr.optString(j, "").trim()
                            if (v.isNotEmpty() && v !in options) options.add(v)
                        }
                    }
                    if (type == "choice" && options.isEmpty()) continue
                    val defRaw = s.opt("default")
                    val default = when {
                        defRaw == null || defRaw == JSONObject.NULL -> ""
                        type == "toggle" ->
                            if (s.optBoolean("default", false)) "true" else "false"
                        else -> s.optString("default", "")
                    }
                    val defaultFixed =
                        if (type == "choice" && default !in options) options.first()
                        else default
                    out.add(
                        PluginSettingDef(
                            key = key, type = type, title = title,
                            description = s.optString("description", ""),
                            default = defaultFixed, options = options
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "skipping bad setting def #$i", e)
                }
            }
            out
        } catch (e: Exception) {
            Log.w(TAG, "bad settings schema for $pluginId", e)
            emptyList()
        }
    }

    private fun pluginSettingsPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(
            PLUGIN_SETTINGS_PREFS, Context.MODE_PRIVATE
        )

    private fun settingPrefKey(pluginId: String, key: String): String =
        dirNameFor(pluginId) + "/" + key

    /** Raw stored value, or null when never set. */
    fun getSetting(context: Context, pluginId: String, key: String): String? =
        try {
            pluginSettingsPrefs(context)
                .getString(settingPrefKey(pluginId, key), null)
        } catch (e: Exception) {
            null
        }

    /** Stored value or the schema default. */
    fun getSettingOrDefault(
        context: Context,
        pluginId: String,
        def: PluginSettingDef
    ): String = getSetting(context, pluginId, def.key) ?: def.default

    /** Boolean convenience for "toggle" settings. */
    fun getSettingBoolean(
        context: Context,
        pluginId: String,
        key: String,
        def: Boolean = false
    ): Boolean =
        getSetting(context, pluginId, key)?.let { it == "true" } ?: def

    fun setSetting(context: Context, pluginId: String, key: String, value: String) {
        try {
            pluginSettingsPrefs(context).edit()
                .putString(settingPrefKey(pluginId, key), value)
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "setSetting failed", e)
        }
    }

    // ------------------------------------------------------------------
    // Sample packs + zip import
    // ------------------------------------------------------------------

    /**
     * Copy the bundled sample packs (assets/sample-plugins/, one dir per
     * pack) into the plugins dir on first scan. Never overwrites an
     * existing pack dir, so user edits and enable/disable state survive.
     */
    private fun installSamplePacks(context: Context, pluginsDir: File) {
        val names = try {
            context.assets.list("sample-plugins")
        } catch (e: Exception) {
            null
        } ?: return
        for (name in names) {
            if (name.isBlank() || ".." in name) continue
            val target = File(pluginsDir, dirNameFor(name))
            if (target.exists()) continue
            try {
                copyAssetDir(context, "sample-plugins/$name", target)
                Log.i(TAG, "installed sample pack $name")
            } catch (e: Exception) {
                Log.w(TAG, "sample pack $name failed", e)
                try {
                    target.deleteRecursively()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun copyAssetDir(context: Context, assetPath: String, target: File) {
        val entries = context.assets.list(assetPath) ?: return
        if (entries.isEmpty()) {
            // A file (or an empty dir listing): try as a file.
            try {
                context.assets.open(assetPath).use { ins ->
                    target.parentFile?.mkdirs()
                    target.outputStream().use { out -> ins.copyTo(out) }
                }
            } catch (_: Exception) {
                // Empty dir — nothing to copy.
            }
            return
        }
        target.mkdirs()
        for (entry in entries) {
            if (entry.isBlank() || ".." in entry) continue
            copyAssetDir(context, "$assetPath/$entry", File(target, entry))
        }
    }

    // ------------------------------------------------------------------
    // Staged import with install-time approval + uninstall
    // ------------------------------------------------------------------

    /**
     * A pack validated from a zip but not yet installed. The Plugins
     * screen shows [manifest], [permissions], and [entryChecksum] for
     * install-time user approval before [installStagedPack] is called —
     * nothing lands in the plugins dir until the user approves.
     */
    data class StagedPack(
        val manifest: PluginManifest,
        val permissions: List<String>,
        val entryChecksum: String,
        internal val stagedDir: File,
        internal val tmpRoot: File
    )

    /**
     * Validate a pack zip (from SAF) without installing it. Returns the
     * staged pack for the approval UI, or an error string.
     */
    fun stagePackZip(context: Context, uri: Uri): Pair<StagedPack?, String?> {
        return try {
            stagePackZipLocked(context.applicationContext, uri)
        } catch (e: Exception) {
            Log.w(TAG, "zip staging failed", e)
            null to "Import failed: ${e.message}"
        }
    }

    /**
     * Install a user-approved staged pack into the plugins dir, then
     * rescan. Returns null on success or a human-readable error.
     * Always call [discardStagedPack] after (it is idempotent).
     */
    fun installStagedPack(context: Context, staged: StagedPack): String? {
        return synchronized(scanLock) {
            val ctx = context.applicationContext
            return try {
                val dir = pluginsDir(ctx)
                dir.mkdirs()
                val target = File(dir, dirNameFor(staged.manifest.id))
                if (target.exists()) {
                    return@synchronized "\"${staged.manifest.name}\" is already installed — uninstall it first to reinstall"
                }
                staged.stagedDir.copyRecursively(target)
                scanLocked(ctx)
                val installed = _plugins.value.firstOrNull { it.manifest.id == staged.manifest.id }
                installed?.error?.let { "Installed with a problem: $it" }
            } catch (e: Exception) {
                Log.w(TAG, "staged install failed", e)
                "Install failed: ${e.message}"
            }
        }
    }

    /** Throw away a staged pack the user declined. Idempotent; null is a no-op. */
    fun discardStagedPack(staged: StagedPack?) {
        if (staged == null) return
        try {
            staged.tmpRoot.deleteRecursively()
        } catch (_: Exception) {
        }
    }

    /**
     * Clean uninstall: remove the pack dir, drop its enabled flag and
     * install marker, and rescan so every registry forgets it. Callers
     * should also evict the cached Room instance via
     * `DatabaseManager.evictQuoteDatabase("plugin:<id>")` for quote packs.
     */
    fun uninstall(context: Context, pluginId: String): Boolean {
        synchronized(scanLock) {
            val ctx = context.applicationContext
            return try {
                val target = File(pluginsDir(ctx), dirNameFor(pluginId))
                if (target.exists()) target.deleteRecursively()
                try {
                    prefs(ctx).edit()
                        .remove(KEY_ENABLED_PREFIX + dirNameFor(pluginId))
                        .apply()
                } catch (e: Exception) {
                    Log.w(TAG, "uninstall pref clear failed", e)
                }
                scanLocked(ctx)
                true
            } catch (e: Exception) {
                Log.w(TAG, "uninstall failed", e)
                false
            }
        }
    }

    /**
     * Capability gate: does [pluginId] declare [permission]? Future
     * capability APIs (quote-corpus read for games, the export pipeline
     * for export styles / video backgrounds, TTS for voices) must check
     * this before granting access — a pack can never silently exceed its
     * install-time declarations.
     */
    fun hasPermission(pluginId: String, permission: String): Boolean =
        _plugins.value.firstOrNull { it.manifest.id == pluginId }
            ?.takeIf { it.error == null && it.enabled }
            ?.manifest?.permissions?.contains(permission) == true

    private fun stagePackZipLocked(
        context: Context,
        uri: Uri
    ): Pair<StagedPack?, String?> {
        val ins = try {
            context.contentResolver.openInputStream(uri)
                ?: return null to "Could not read the zip"
        } catch (e: Exception) {
            return null to "Could not read the zip: ${e.message}"
        }
        return ins.use { stagePackStream(context, it) }
    }

    /** Zip entry carried in memory while staging (packs are small, data only). */
    private data class ZipEntry(val name: String, val bytes: ByteArray)

    /**
     * Read zip entries from a stream (shared by SAF import and update
     * downloads). 64 MB sanity cap — packs are JSON data.
     */
    private fun readZipEntries(ins: java.io.InputStream): Pair<List<ZipEntry>?, String?> {
        val entries = mutableListOf<ZipEntry>()
        return try {
            ZipInputStream(ins).use { zip ->
                var e = zip.nextEntry
                var total = 0L
                while (e != null) {
                    val name = e.name.trim().trimStart('/')
                    if (name.isNotEmpty() && !e.isDirectory) {
                        val bytes = zip.readBytes()
                        total += bytes.size
                        if (total > 64L * 1024 * 1024) {
                            return null to "Zip is too large to be a pack"
                        }
                        entries.add(ZipEntry(name, bytes))
                    }
                    zip.closeEntry()
                    e = zip.nextEntry
                }
            }
            entries to null
        } catch (e: Exception) {
            null to "Could not read the zip: ${e.message}"
        }
    }

    private fun stagePackStream(
        context: Context,
        ins: java.io.InputStream
    ): Pair<StagedPack?, String?> {
        // Read all entries into memory first (packs are small, data only).
        val (entries, readError) = readZipEntries(ins)
        if (entries == null) return null to (readError ?: "Could not read the zip")
        if (entries.isEmpty()) return null to "The zip is empty"

        // Locate plugin.json: at the root, or inside a single top-level folder.
        val manifestEntry = entries.firstOrNull { it.name == "plugin.json" }
            ?: entries.firstOrNull {
                it.name.count { c -> c == '/' } == 1 && it.name.endsWith("/plugin.json")
            } ?: return null to "No plugin.json found — the zip is not a QuoteBrowser pack"
        val prefix = manifestEntry.name.removeSuffix("plugin.json")

        // Stage into a temp dir and run the real validator.
        val tmpRoot = File(context.cacheDir, "pack_import_${System.currentTimeMillis()}")
        try {
            tmpRoot.mkdirs()
            val tmpPack = File(tmpRoot, "pack")
            tmpPack.mkdirs()
            for (ze in entries) {
                if (!ze.name.startsWith(prefix)) continue
                val rel = ze.name.removePrefix(prefix)
                if (rel.isEmpty() || ".." in rel || rel.startsWith("/")) continue
                val out = File(tmpPack, rel)
                if (!out.canonicalPath.startsWith(tmpPack.canonicalPath + File.separator)) continue
                out.parentFile?.mkdirs()
                out.writeBytes(ze.bytes)
            }
            val (validated, manifestError) = readManifest(File(tmpPack, "plugin.json"))
            if (validated == null) {
                tmpRoot.deleteRecursively()
                return null to (manifestError ?: "Bad plugin.json")
            }
            val entryFile = safeEntryFile(tmpPack, validated.entry)
            if (entryFile == null || !entryFile.isFile) {
                tmpRoot.deleteRecursively()
                return null to "Entry file \"${validated.entry}\" is missing from the zip"
            }
            // Collision check before bothering the user with approval.
            val target = File(pluginsDir(context), dirNameFor(validated.id))
            if (target.exists()) {
                tmpRoot.deleteRecursively()
                return null to "\"${validated.name}\" is already installed — uninstall it first to reinstall"
            }
            val checksum = sha256(entryFile)
            return StagedPack(
                manifest = validated,
                permissions = validated.permissions,
                entryChecksum = checksum,
                stagedDir = tmpPack,
                tmpRoot = tmpRoot
            ) to null
        } catch (e: Exception) {
            try {
                tmpRoot.deleteRecursively()
            } catch (_: Exception) {
            }
            return null to "Import failed: ${e.message}"
        }
    }

    /** Legacy single call kept out of the codebase: use stage/install. */
    @Deprecated("Use stagePackZip + installStagedPack so the user approves permissions first")
    fun importPackZip(context: Context, uri: Uri): String? {
        val (staged, error) = stagePackZip(context, uri)
        if (staged == null) return error
        return try {
            installStagedPack(context, staged)
        } finally {
            discardStagedPack(staged)
        }
    }

    // ------------------------------------------------------------------
    // v1.87: one-tap plugin updates
    //
    // A pack whose manifest declares "updateUrl" (an https/http URL to a
    // zip of a newer version) can be checked and updated from the Plugins
    // screen. The download is staged through the same validator as manual
    // imports; the update only applies when the staged version is newer
    // than the installed one, and the pack's enabled flag survives (it is
    // keyed by plugin id, which never changes across versions).
    // ------------------------------------------------------------------

    enum class UpdateStatus { NO_SOURCE, UP_TO_DATE, AVAILABLE, ERROR }

    data class UpdateCheck(
        val status: UpdateStatus,
        /** Version found at the update URL (AVAILABLE only). */
        val latestVersion: String = "",
        /** Staged newer pack, ready for [applyUpdate] (AVAILABLE only). */
        val staged: StagedPack? = null,
        val error: String? = null,
        /**
         * v1.87: permissions the update newly declares that the installed
         * pack did not — surfaced in the update UI so a pack can never
         * silently broaden what it may do.
         */
        val newPermissions: List<String> = emptyList()
    )

    /**
     * Compare dotted version strings ("1.2.10" > "1.2.9"). Numeric
     * segments compare numerically; anything else falls back to string
     * comparison per segment. Returns negative/zero/positive.
     */
    fun compareVersions(a: String, b: String): Int {
        val pa = a.trim().split(".", "-")
        val pb = b.trim().split(".", "-")
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val sa = pa.getOrNull(i).orEmpty()
            val sb = pb.getOrNull(i).orEmpty()
            val na = sa.toIntOrNull()
            val nb = sb.toIntOrNull()
            val c = if (na != null && nb != null) na.compareTo(nb)
            else sa.compareTo(sb)
            if (c != 0) return c
        }
        return 0
    }

    /**
     * Check [pluginId] for an update. Downloads the manifest's updateUrl
     * zip and stages it; AVAILABLE carries the staged pack. Never throws.
     * Call off the main thread (network I/O).
     */
    suspend fun checkForUpdate(context: Context, pluginId: String): UpdateCheck =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val ctx = context.applicationContext
            val installed = synchronized(scanLock) {
                _plugins.value.firstOrNull { it.manifest.id == pluginId }
            } ?: return@withContext UpdateCheck(
                UpdateStatus.ERROR, error = "Pack is no longer installed"
            )
            val url = installed.manifest.updateUrl
            if (url.isBlank()) return@withContext UpdateCheck(UpdateStatus.NO_SOURCE)
            val bytes = try {
                downloadBytes(url)
            } catch (e: Exception) {
                Log.w(TAG, "update download failed for $pluginId", e)
                return@withContext UpdateCheck(
                    UpdateStatus.ERROR, error = "Download failed: ${e.message}"
                )
            }
            val (staged, stageError) = try {
                synchronized(scanLock) {
                    // Bypass the "already installed" collision check: this
                    // is an update of the same pack, not a fresh install.
                    val (entries, readError) = readZipEntries(bytes.inputStream())
                    if (entries == null) {
                        return@withContext UpdateCheck(
                            UpdateStatus.ERROR,
                            error = readError ?: "Could not read the update zip"
                        )
                    }
                    stageEntriesForUpdate(ctx, entries, pluginId)
                }
            } catch (e: Exception) {
                Log.w(TAG, "update staging failed for $pluginId", e)
                return@withContext UpdateCheck(
                    UpdateStatus.ERROR, error = "Update failed: ${e.message}"
                )
            }
            if (staged == null) {
                return@withContext UpdateCheck(
                    UpdateStatus.ERROR, error = stageError ?: "Update is not a valid pack"
                )
            }
            if (staged.manifest.id != pluginId) {
                discardStagedPack(staged)
                return@withContext UpdateCheck(
                    UpdateStatus.ERROR,
                    error = "Update is for a different pack (${staged.manifest.id})"
                )
            }
            return@withContext if (compareVersions(
                    staged.manifest.version,
                    installed.manifest.version
                ) > 0
            ) {
                UpdateCheck(
                    UpdateStatus.AVAILABLE,
                    latestVersion = staged.manifest.version,
                    staged = staged,
                    newPermissions = staged.manifest.permissions -
                        installed.manifest.permissions.toSet()
                )
            } else {
                discardStagedPack(staged)
                UpdateCheck(UpdateStatus.UP_TO_DATE)
            }
        }

    /**
     * Apply an AVAILABLE [UpdateCheck]: replace the installed pack dir
     * with the staged one and rescan. The enabled flag survives (keyed by
     * plugin id). For quote packs the caller must evict the cached Room
     * handle first (DatabaseManager.evictQuoteDatabase), like uninstall.
     * Returns null on success or a human-readable error. Always call
     * [discardStagedPack] on the check's staged pack after (idempotent).
     */
    fun applyUpdate(context: Context, check: UpdateCheck): String? {
        val staged = check.staged
            ?: return "Nothing to update"
        val pluginId = staged.manifest.id
        return synchronized(scanLock) {
            val ctx = context.applicationContext
            try {
                val dir = pluginsDir(ctx)
                dir.mkdirs()
                val target = java.io.File(dir, dirNameFor(pluginId))
                if (!target.exists()) {
                    return@synchronized "Pack is no longer installed"
                }
                // Swap via a backup so a failed copy can't destroy the old pack.
                val backup = java.io.File(dir, dirNameFor(pluginId) + ".update-backup")
                try { if (backup.exists()) backup.deleteRecursively() } catch (_: Exception) { }
                if (!target.renameTo(backup)) {
                    discardStagedPack(staged)
                    return@synchronized "Could not remove the old version"
                }
                try {
                    staged.stagedDir.copyRecursively(target)
                } catch (e: Exception) {
                    try { target.deleteRecursively() } catch (_: Exception) { }
                    try { backup.renameTo(target) } catch (_: Exception) { }
                    discardStagedPack(staged)
                    return@synchronized "Update failed: ${e.message}"
                }
                try { backup.deleteRecursively() } catch (_: Exception) { }
                staged.tmpRoot.deleteRecursively()
                scanLocked(ctx)
                val installed = _plugins.value.firstOrNull { it.manifest.id == pluginId }
                installed?.error?.let { "Updated with a problem: $it" }
            } catch (e: Exception) {
                Log.w(TAG, "update apply failed", e)
                "Update failed: ${e.message}"
            }
        }
    }

    /**
     * Stage zip entries for an update of [expectedId]: same validation as
     * a fresh import, minus the already-installed collision check.
     */
    private fun stageEntriesForUpdate(
        context: Context,
        entries: List<ZipEntry>,
        expectedId: String
    ): Pair<StagedPack?, String?> {
        if (entries.isEmpty()) return null to "The update zip is empty"
        val manifestEntry = entries.firstOrNull { it.name == "plugin.json" }
            ?: entries.firstOrNull {
                it.name.count { c -> c == '/' } == 1 && it.name.endsWith("/plugin.json")
            } ?: return null to "No plugin.json found — the update is not a QuoteBrowser pack"
        val prefix = manifestEntry.name.removeSuffix("plugin.json")
        val tmpRoot =
            java.io.File(context.cacheDir, "pack_update_${System.currentTimeMillis()}")
        return try {
            tmpRoot.mkdirs()
            val tmpPack = java.io.File(tmpRoot, "pack")
            tmpPack.mkdirs()
            for (ze in entries) {
                if (!ze.name.startsWith(prefix)) continue
                val rel = ze.name.removePrefix(prefix)
                if (rel.isEmpty() || ".." in rel || rel.startsWith("/")) continue
                val out = java.io.File(tmpPack, rel)
                if (!out.canonicalPath.startsWith(tmpPack.canonicalPath + java.io.File.separator)) continue
                out.parentFile?.mkdirs()
                out.writeBytes(ze.bytes)
            }
            val (validated, manifestError) = readManifest(java.io.File(tmpPack, "plugin.json"))
            if (validated == null) {
                tmpRoot.deleteRecursively()
                return null to (manifestError ?: "Bad plugin.json")
            }
            if (validated.id != expectedId) {
                tmpRoot.deleteRecursively()
                return null to "Update is for a different pack (${validated.id})"
            }
            val entryFile = safeEntryFile(tmpPack, validated.entry)
            if (entryFile == null || !entryFile.isFile) {
                tmpRoot.deleteRecursively()
                return null to "Entry file \"${validated.entry}\" is missing from the update"
            }
            StagedPack(
                manifest = validated,
                permissions = validated.permissions,
                entryChecksum = sha256(entryFile),
                stagedDir = tmpPack,
                tmpRoot = tmpRoot
            ) to null
        } catch (e: Exception) {
            try {
                tmpRoot.deleteRecursively()
            } catch (_: Exception) {
            }
            null to "Update failed: ${e.message}"
        }
    }

    /** Download capped at 64 MB. Call off the main thread. */
    @Throws(Exception::class)
    private fun downloadBytes(url: String): ByteArray {
        val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        try {
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.instanceFollowRedirects = true
            conn.connect()
            if (conn.responseCode !in 200..299) {
                throw java.io.IOException("HTTP ${conn.responseCode}")
            }
            val out = java.io.ByteArrayOutputStream()
            conn.inputStream.use { ins ->
                val buf = ByteArray(8192)
                var total = 0L
                while (true) {
                    val n = ins.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > 64L * 1024 * 1024) {
                        throw java.io.IOException("Update is too large")
                    }
                    out.write(buf, 0, n)
                }
            }
            return out.toByteArray()
        } finally {
            conn.disconnect()
        }
    }
}
