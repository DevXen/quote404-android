package com.quotebrowser.app.ui

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * v1.66 recent-search history: full snapshots of submitted searches, most
 * recent first (max 30). Tapping a snapshot re-runs the exact search —
 * query, databases, categories, exclusions, maturity, cast size, and all.
 *
 * v1.66 migration: the pre-v1.66 store held bare query strings; those are
 * converted to snapshots with default filters on first read.
 */
data class SearchRecord(
    val query: String,
    /** Null = "this database" (the current one at re-run time). */
    val dbIds: Set<String>?,
    val categoryIds: Set<Long>,
    val excludedCategoryIds: Set<Long>,
    /** MaturityLevel.name, or null for "no per-search pick". */
    val maturity: String?,
    /** CastSize.name, or null for "any cast size". */
    val castSize: String?,
    /** Maturity levels (0-4) excluded. */
    val excludedMaturities: Set<Int>,
    /** CastSize names excluded. */
    val excludedCasts: Set<String>,
    val minRating: Int?,
    val voiceOnly: Boolean,
    /** LengthMode.name. */
    val lengthMode: String,
    /** v1.74.5: regex search mode. */
    val useRegex: Boolean = false,
    val recordedAt: Long = System.currentTimeMillis()
) {
    /** Identity for dedup: everything except the timestamp. */
    fun signature(): String = buildString {
        append(query.trim()); append('|')
        append(dbIds?.sorted()?.joinToString(",") ?: "*"); append('|')
        append(categoryIds.sorted().joinToString(",")); append('|')
        append(excludedCategoryIds.sorted().joinToString(",")); append('|')
        append(maturity ?: ""); append('|')
        append(castSize ?: ""); append('|')
        append(excludedMaturities.sorted().joinToString(",")); append('|')
        append(excludedCasts.sorted().joinToString(",")); append('|')
        append(minRating?.toString() ?: ""); append('|')
        append(voiceOnly); append('|')
        append(lengthMode); append('|')
        append(useRegex)
    }

    /** One-line summary of the non-query filters, for the history row. */
    fun filterSummary(): String {
        val parts = mutableListOf<String>()
        if (categoryIds.isNotEmpty()) parts.add("${categoryIds.size} cat.")
        if (excludedCategoryIds.isNotEmpty()) parts.add("excl. ${excludedCategoryIds.size} cat.")
        if (maturity != null) parts.add(maturity)
        if (castSize != null) parts.add(castSize)
        if (excludedMaturities.isNotEmpty()) parts.add("excl. ${excludedMaturities.size} mat.")
        if (excludedCasts.isNotEmpty()) parts.add("excl. ${excludedCasts.size} cast")
        if (minRating != null) parts.add("★$minRating+")
        if (voiceOnly) parts.add("voiced")
        if (useRegex) parts.add("regex")
        return parts.joinToString(" · ")
    }

    fun toJson(): JSONObject = JSONObject()
        .put("query", query)
        .put("dbIds", if (dbIds == null) JSONObject.NULL else JSONArray(dbIds.toList()))
        .put("categoryIds", JSONArray(categoryIds.toList()))
        .put("excludedCategoryIds", JSONArray(excludedCategoryIds.toList()))
        .put("maturity", maturity ?: JSONObject.NULL)
        .put("castSize", castSize ?: JSONObject.NULL)
        .put("excludedMaturities", JSONArray(excludedMaturities.toList()))
        .put("excludedCasts", JSONArray(excludedCasts.toList()))
        .put("minRating", minRating ?: JSONObject.NULL)
        .put("voiceOnly", voiceOnly)
        .put("lengthMode", lengthMode)
        .put("useRegex", useRegex)
        .put("recordedAt", recordedAt)

    companion object {
        fun fromJson(o: JSONObject): SearchRecord? = try {
            fun strSet(key: String): Set<String> {
                val arr = o.optJSONArray(key) ?: return emptySet()
                return (0 until arr.length()).mapTo(mutableSetOf()) { arr.getString(it) }
            }
            fun longSet(key: String): Set<Long> {
                val arr = o.optJSONArray(key) ?: return emptySet()
                return (0 until arr.length()).mapTo(mutableSetOf()) { arr.getLong(it) }
            }
            val dbRaw = o.opt("dbIds")
            val matRaw = o.opt("maturity")
            val castRaw = o.opt("castSize")
            val ratingRaw = o.opt("minRating")
            SearchRecord(
                query = o.optString("query", ""),
                dbIds = if (dbRaw == null || dbRaw == JSONObject.NULL) null else strSet("dbIds"),
                categoryIds = longSet("categoryIds"),
                excludedCategoryIds = longSet("excludedCategoryIds"),
                maturity = if (matRaw == null || matRaw == JSONObject.NULL) null
                else o.optString("maturity", null),
                castSize = if (castRaw == null || castRaw == JSONObject.NULL) null
                else o.optString("castSize", null),
                excludedMaturities = (o.optJSONArray("excludedMaturities")?.let { arr ->
                (0 until arr.length()).mapTo(mutableSetOf()) { arr.optInt(it, -1) }
                    .filterTo(mutableSetOf()) { it in 0..4 }
            } ?: emptySet()),
                excludedCasts = strSet("excludedCasts"),
                minRating = if (ratingRaw == null || ratingRaw == JSONObject.NULL) null
                else o.optInt("minRating"),
                voiceOnly = o.optBoolean("voiceOnly", false),
                lengthMode = o.optString("lengthMode", "ALL"),
                useRegex = o.optBoolean("useRegex", false),
                recordedAt = o.optLong("recordedAt", System.currentTimeMillis())
            )
        } catch (_: Exception) {
            null
        }

        /** Pre-v1.66 bare-query entry → snapshot with default filters. */
        fun fromLegacyQuery(query: String): SearchRecord = SearchRecord(
            query = query,
            dbIds = null,
            categoryIds = emptySet(),
            excludedCategoryIds = emptySet(),
            maturity = null,
            castSize = null,
            excludedMaturities = emptySet(),
            excludedCasts = emptySet(),
            minRating = null,
            voiceOnly = false,
            lengthMode = "ALL"
        )
    }
}

class SearchHistory(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "search_history_v144"
        private const val KEY_HISTORY = "history"
        private const val KEY_MIGRATED = "migrated_v1"
        private const val MAX_HISTORY = 30
    }

    /** Snapshots, most recent first (max 30). */
    fun history(): List<SearchRecord> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val out = mutableListOf<SearchRecord>()
            for (i in 0 until arr.length()) {
                val el = arr.opt(i) ?: continue
                when (el) {
                    // Pre-v1.66 entry: a bare query string.
                    is String -> if (el.isNotBlank()) {
                        out.add(SearchRecord.fromLegacyQuery(el))
                    }
                    is JSONObject -> SearchRecord.fromJson(el)?.let { rec ->
                        if (rec.query.isNotBlank()) out.add(rec)
                    }
                }
            }
            out.take(MAX_HISTORY)
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Records a search: blank queries are ignored, and a search with the
     * same signature already in the history moves back to the front.
     */
    fun add(record: SearchRecord) {
        if (record.query.isBlank()) return
        val sig = record.signature()
        val updated =
            (listOf(record) + history().filter { it.signature() != sig })
                .take(MAX_HISTORY)
        save(updated)
    }

    /** Removes one snapshot from the history; no-op when absent. */
    fun remove(record: SearchRecord) {
        val current = history()
        val updated = current.filter {
            it.recordedAt != record.recordedAt || it.signature() != record.signature()
        }
        if (updated.size != current.size) save(updated)
    }

    /** Clears the whole history. */
    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    /**
     * One-time migration guard for the legacy saved-searches list. The
     * Search screen sets this after copying the old entries over.
     */
    var migrationDone: Boolean
        get() = prefs.getBoolean(KEY_MIGRATED, false)
        set(value) = prefs.edit().putBoolean(KEY_MIGRATED, value).apply()

    private fun save(history: List<SearchRecord>) {
        val arr = JSONArray()
        history.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
    }
}
