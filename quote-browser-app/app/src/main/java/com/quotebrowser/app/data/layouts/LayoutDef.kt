package com.quotebrowser.app.data.layouts

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/**
 * v1.85: quote layouts. A layout arranges a quote's *elements* (header,
 * body, author, rating, meta, actions) — the theme still owns colors and
 * fonts. One LayoutDef drives both the on-screen card (LayoutRenderer)
 * and the HTML export, so picking "Poster" changes the app AND the export.
 *
 * Layouts come from three sources: bundled premades (assets/layouts),
 * layout plugin packs (PluginManager registry), and the user's own custom
 * HTML/CSS styles (LayoutEditorScreen, stored in SharedPreferences).
 */

/** The placeable elements of a quote card. */
enum class LayoutBlockType(val key: String) {
    SITE_HEADER("siteHeader"),
    QUOTE_TEXT("quoteText"),
    AUTHOR("author"),
    RATING("rating"),
    META("meta"),
    ACTIONS("actions"),
    DIVIDER("divider"),
    SPACER("spacer");

    companion object {
        fun fromKey(key: String): LayoutBlockType? =
            entries.firstOrNull { it.key == key }
    }
}

/**
 * One block in a layout.
 *
 * @param align start | center | end | spread (header only) | justify (text only)
 * @param textScale relative text size multiplier (0.5..3.0)
 * @param paddingDp vertical padding around the block
 * @param show visibility selectors: meta -> date/categories/url;
 *   actions -> share/copy/favorite/tts/readalong
 * @param prefix text prepended to the block (e.g. "— " for author)
 */
data class LayoutBlock(
    val type: LayoutBlockType,
    val align: String = "start",
    val textScale: Float = 1f,
    val paddingDp: Int = 0,
    val show: List<String> = emptyList(),
    val prefix: String = ""
)

data class LayoutDef(
    val id: String,
    val name: String,
    val author: String = "",
    val version: Int = 1,
    val description: String = "",
    val blocks: List<LayoutBlock>,
    val css: String = "",
    /**
     * Custom per-quote HTML template using {{tokens}}
     * ({{quote_text}}, {{author}}, {{quote_number}}, {{site_name}},
     * {{source_url}}, {{date}}, {{rating}}, {{categories}}).
     * Blank = HTML is generated from [blocks].
     */
    val htmlTemplate: String = "",
    /** builtin | plugin | custom */
    val source: String = "builtin"
) {
    /** True when this layout renders cards exactly like the pre-1.85 app. */
    val isClassic: Boolean get() = id == LayoutStore.CLASSIC_ID
}

private val ID_RE = Regex("^[A-Za-z0-9_-]{1,64}$")
private val ALIGN_RE = setOf("start", "center", "end", "spread", "justify")

/** Parses and validates a layout.json. Throws IllegalArgumentException on any problem. */
object LayoutParser {

    fun parse(json: String): LayoutDef {
        val o = try {
            JSONObject(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("Not valid JSON: ${e.message}")
        }
        val id = o.optString("id", "").trim()
        if (!ID_RE.matches(id)) {
            throw IllegalArgumentException(
                "\"id\" must match [A-Za-z0-9_-] (1-64 chars)"
            )
        }
        val name = o.optString("name", "").trim()
        if (name.isEmpty()) throw IllegalArgumentException("\"name\" is required")
        val blocksJson = o.optJSONArray("blocks")
            ?: throw IllegalArgumentException("\"blocks\" array is required")
        if (blocksJson.length() == 0) throw IllegalArgumentException("\"blocks\" must not be empty")
        if (blocksJson.length() > 40) throw IllegalArgumentException("\"blocks\" has too many entries (max 40)")
        val blocks = (0 until blocksJson.length()).map { i ->
            parseBlock(blocksJson.optJSONObject(i)
                ?: throw IllegalArgumentException("blocks[$i] must be an object"), i)
        }
        return LayoutDef(
            id = id,
            name = name,
            author = o.optString("author", "").trim(),
            version = o.optInt("version", 1).coerceAtLeast(1),
            description = o.optString("description", "").trim(),
            blocks = blocks,
            css = o.optString("css", ""),
            htmlTemplate = o.optString("htmlTemplate", "")
        )
    }

    private fun parseBlock(o: JSONObject, i: Int): LayoutBlock {
        val typeKey = o.optString("type", "").trim()
        val type = LayoutBlockType.fromKey(typeKey)
            ?: throw IllegalArgumentException(
                "blocks[$i]: unknown type \"$typeKey\""
            )
        val align = o.optString("align", "start").trim().lowercase()
        if (align !in ALIGN_RE) {
            throw IllegalArgumentException("blocks[$i]: unknown align \"$align\"")
        }
        val textScale = o.optDouble("textScale", 1.0).toFloat().coerceIn(0.5f, 3f)
        val paddingDp = o.optInt("paddingDp", 0).coerceIn(0, 64)
        val show = o.optJSONArray("show")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it, "").trim() }
                .filter { it.isNotEmpty() }
        } ?: emptyList()
        return LayoutBlock(
            type = type,
            align = align,
            textScale = textScale,
            paddingDp = paddingDp,
            show = show,
            prefix = o.optString("prefix", "")
        )
    }
}

/**
 * Loads and resolves layouts: bundled premades, plugin-pack layouts, and
 * the user's custom styles. Also owns the global + per-database layout
 * preference (SharedPreferences, no Room entity needed).
 */
object LayoutStore {
    const val CLASSIC_ID = "classic"

    /**
     * Bump counter, incremented on every layout preference change
     * (global, per-db, save, delete). Composables read it inside
     * composition so layout changes recompute immediately.
     */
    var prefsVersion by mutableStateOf(0)
        private set

    private fun bump() { prefsVersion++ }
    private const val TAG = "LayoutStore"
    private const val PREFS = "quote_layouts"
    private const val KEY_GLOBAL = "global_layout_id"
    private const val KEY_DB_PREFIX = "db_layout_"
    private const val KEY_CUSTOM_PREFIX = "custom_layout_"

    @Volatile private var premadeCache: List<LayoutDef>? = null

    /** The six bundled premade layouts, parsed once from assets. */
    fun premade(context: Context): List<LayoutDef> {
        premadeCache?.let { return it }
        val ids = listOf("classic", "minimal", "poster", "indexcard", "newspaper", "chat")
        val out = mutableListOf<LayoutDef>()
        val am = context.applicationContext.assets
        for (id in ids) {
            try {
                am.open("layouts/$id.json").bufferedReader().use { r ->
                    out.add(LayoutParser.parse(r.readText()))
                }
            } catch (e: Exception) {
                Log.w(TAG, "premade layout $id failed to load", e)
            }
        }
        // Classic must always exist: fall back to a generated minimal one
        // so a broken asset can never leave cards unrenderable.
        if (out.none { it.id == CLASSIC_ID }) {
            out.add(0, fallbackClassic())
        }
        premadeCache = out
        return out
    }

    private fun fallbackClassic(): LayoutDef = LayoutDef(
        id = CLASSIC_ID,
        name = "Classic",
        description = "The standard card.",
        blocks = listOf(
            LayoutBlock(LayoutBlockType.SITE_HEADER, align = "spread"),
            LayoutBlock(LayoutBlockType.QUOTE_TEXT),
            LayoutBlock(LayoutBlockType.AUTHOR, align = "end", prefix = "— "),
            LayoutBlock(LayoutBlockType.RATING),
            LayoutBlock(LayoutBlockType.META, show = listOf("date", "categories")),
            LayoutBlock(LayoutBlockType.ACTIONS, show = listOf("share", "copy", "favorite", "tts"))
        )
    )

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** User-saved custom HTML/CSS styles. */
    fun customLayouts(context: Context): List<LayoutDef> {
        val p = prefs(context)
        return p.all.keys
            .filter { it.startsWith(KEY_CUSTOM_PREFIX) }
            .mapNotNull { key ->
                try {
                    val o = JSONObject(p.getString(key, "") ?: "")
                    LayoutDef(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        author = "You",
                        description = "Your custom HTML/CSS style.",
                        blocks = fallbackClassic().blocks,
                        css = o.optString("css", ""),
                        htmlTemplate = o.optString("htmlTemplate", ""),
                        source = "custom"
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "bad custom layout $key", e)
                    null
                }
            }
            .sortedBy { it.name.lowercase() }
    }

    fun saveCustomLayout(
        context: Context, id: String, name: String,
        htmlTemplate: String, css: String
    ) {
        val o = JSONObject()
            .put("id", id)
            .put("name", name)
            .put("htmlTemplate", htmlTemplate)
            .put("css", css)
        prefs(context).edit().putString(KEY_CUSTOM_PREFIX + id, o.toString()).apply()
        bump()
    }

    /** Delete a saved custom style. [id] may be the raw id or the
     * "custom:<id>" registry id — both resolve to the same stored entry. */
    fun deleteCustomLayout(context: Context, id: String) {
        val raw = id.removePrefix("custom:")
        prefs(context).edit().remove(KEY_CUSTOM_PREFIX + raw).apply()
        bump()
        if (globalLayoutId(context) == customId(raw)) setGlobalLayoutId(context, CLASSIC_ID)
    }

    /** Registry id used for a custom style everywhere else. */
    fun customId(id: String): String = "custom:$id"

    /**
     * Every layout available to the picker: premades, enabled plugin-pack
     * layouts, then custom styles.
     */
    fun allLayouts(
        context: Context,
        pluginLayouts: Map<String, LayoutDef> = emptyMap()
    ): List<LayoutDef> {
        val customs = customLayouts(context).associateBy { customId(it.id) }
            .map { (k, v) -> v.copy(id = k) }
        return premade(context) +
            pluginLayouts.values.sortedBy { it.name.lowercase() } +
            customs
    }

    fun layoutById(
        context: Context, id: String?,
        pluginLayouts: Map<String, LayoutDef> = emptyMap()
    ): LayoutDef {
        if (id != null) {
            allLayouts(context, pluginLayouts).firstOrNull { it.id == id }?.let { return it }
        }
        return premade(context).first { it.id == CLASSIC_ID }
    }

    fun globalLayoutId(context: Context): String =
        prefs(context).getString(KEY_GLOBAL, CLASSIC_ID) ?: CLASSIC_ID

    fun setGlobalLayoutId(context: Context, id: String) {
        prefs(context).edit().putString(KEY_GLOBAL, id).apply()
        bump()
    }

    /** Per-database override; null/blank = follow the global layout. */
    fun dbLayoutId(context: Context, dbId: String): String? =
        prefs(context).getString(KEY_DB_PREFIX + dbId, null)?.takeIf { it.isNotBlank() }

    fun setDbLayoutId(context: Context, dbId: String, id: String?) {
        val ed = prefs(context).edit()
        if (id.isNullOrBlank()) ed.remove(KEY_DB_PREFIX + dbId)
        else ed.putString(KEY_DB_PREFIX + dbId, id)
        ed.apply()
        bump()
    }

    /**
     * The layout for a database's cards: per-db override, else global,
     * else Classic. Unknown ids fall back to Classic (a removed plugin
     * pack can never break card rendering).
     */
    fun layoutForDb(
        context: Context, dbId: String?,
        pluginLayouts: Map<String, LayoutDef> = emptyMap()
    ): LayoutDef {
        val id = dbId?.let { dbLayoutId(context, it) } ?: globalLayoutId(context)
        return layoutById(context, id, pluginLayouts)
    }
}
