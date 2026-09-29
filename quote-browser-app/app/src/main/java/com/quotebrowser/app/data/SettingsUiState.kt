package com.quotebrowser.app.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf

/**
 * v1.59: UI-only state for the reorganized Settings screen — category and
 * section collapse state, pinned settings, and recently-changed settings.
 *
 * Persisted in its own SharedPreferences file ("settings_ui"), deliberately
 * separate from DatabaseManager's prefs: this remembers how the settings
 * list was arranged and nothing else. It never affects app behavior.
 */
class SettingsUiState(context: Context) {

    /**
     * One pinned or recently-changed row. [isSection] is true when a whole
     * section header was pinned rather than a single toggle.
     */
    data class Entry(
        val id: String,
        val title: String,
        val section: String,
        val category: String,
        val isSection: Boolean
    )

    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    // "cat:Appearance" / "section:Display" -> expanded. Default: expanded.
    private val expanded = mutableStateMapOf<String, Boolean>()

    /** Pinned entries, in pin order. */
    val pinned = mutableStateListOf<Entry>()

    /** Recently-changed entries, most-recent first (capped). */
    val recent = mutableStateListOf<Entry>()

    init {
        // v1.59.2: categories now start collapsed (they used to start
        // expanded, which made the screen very long). One-time reset of
        // stored collapse state so existing installs pick up the new
        // default; afterwards the user's own choices persist as before.
        if (prefs.getInt(KEY_COLLAPSE_REV, 0) < COLLAPSE_REV) {
            val ed = prefs.edit()
            prefs.all.keys
                .filter { it.startsWith("cat:") || it.startsWith("section:") }
                .forEach { ed.remove(it) }
            ed.putInt(KEY_COLLAPSE_REV, COLLAPSE_REV).apply()
        }
        pinned.addAll(loadList(KEY_PINNED))
        recent.addAll(loadList(KEY_RECENT))
    }

    /**
     * True when the category/section is expanded. Categories default to
     * collapsed (v1.59.2, to keep the screen short); sections default to
     * expanded once their category is open.
     */
    fun isExpanded(kind: String, key: String): Boolean {
        val k = "$kind:$key"
        val default = kind != "cat"
        return expanded.getOrPut(k) { prefs.getBoolean(k, default) }
    }

    fun setExpanded(kind: String, key: String, value: Boolean) {
        val k = "$kind:$key"
        expanded[k] = value
        prefs.edit().putBoolean(k, value).apply()
    }

    fun toggleExpanded(kind: String, key: String) {
        setExpanded(kind, key, !isExpanded(kind, key))
    }

    fun isPinned(id: String): Boolean = pinned.any { it.id == id }

    /**
     * Toggles the pin for [id]. Returns true when the entry is now pinned,
     * false when it was unpinned.
     */
    fun togglePin(
        id: String,
        title: String,
        section: String,
        category: String,
        isSection: Boolean = false
    ): Boolean {
        val at = pinned.indexOfFirst { it.id == id }
        return if (at >= 0) {
            pinned.removeAt(at)
            saveList(KEY_PINNED, pinned)
            false
        } else {
            pinned.add(Entry(id, title, section, category, isSection))
            saveList(KEY_PINNED, pinned)
            true
        }
    }

    fun unpin(id: String) {
        if (pinned.removeAll { it.id == id }) saveList(KEY_PINNED, pinned)
    }

    /** Records a toggle change; most-recent-first, capped at MAX_RECENT. */
    fun markChanged(id: String, title: String, section: String, category: String) {
        recent.removeAll { it.id == id }
        recent.add(0, Entry(id, title, section, category, false))
        while (recent.size > MAX_RECENT) recent.removeAt(recent.lastIndex)
        saveList(KEY_RECENT, recent)
    }

    // Single-string storage keeps the ordering that getStringSet would lose.
    private fun saveList(key: String, list: List<Entry>) {
        prefs.edit().putString(key, list.joinToString("\n") { encode(it) }).apply()
    }

    private fun loadList(key: String): List<Entry> =
        prefs.getString(key, "").orEmpty()
            .split("\n")
            .filter { it.isNotEmpty() }
            .mapNotNull { decode(it) }

    private fun encode(e: Entry): String =
        listOf(e.id, e.title, e.section, e.category, if (e.isSection) "1" else "0")
            .joinToString(FIELD_SEP)

    private fun decode(s: String): Entry? {
        val p = s.split(FIELD_SEP)
        return if (p.size == 5) Entry(p[0], p[1], p[2], p[3], p[4] == "1") else null
    }

    companion object {
        private const val PREFS_NAME = "settings_ui"
        private const val KEY_PINNED = "pinned"
        private const val KEY_RECENT = "recent"
        private const val KEY_COLLAPSE_REV = "collapse_rev"
        private const val COLLAPSE_REV = 1
        private const val FIELD_SEP = "\u0001"
        private const val MAX_RECENT = 8
    }
}
