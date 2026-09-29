package com.quotebrowser.app.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * A named saved search: a display name plus the full search configuration
 * so one tap re-runs the exact search later. [dbIds] is null for "this
 * database" (the current database at re-run time).
 */
data class NamedSearch(
    val name: String,
    val query: String,
    val dbIds: Set<String>?,
    val categoryIds: Set<Long>,
    val minRating: Int?,
    val notesScope: Boolean,
    val sortOrder: SortOrder,
    val lengthMode: LengthMode,
    val minLength: Int,
    val maxLength: Int,
    /** v1.48: post a notification when newly imported quotes match this search. */
    val alertOnNewMatch: Boolean = false,
    /** v1.66: the v1.65 filter set, so a saved search re-runs exactly. */
    val excludedCategoryIds: Set<Long> = emptySet(),
    /** MaturityLevel.name, or null for "no per-search pick". */
    val maturity: String? = null,
    /** CastSize.name, or null for "any cast size". */
    val castSize: String? = null,
    /** MaturityLevel names excluded from the search. */
    val excludedMaturities: Set<Int> = emptySet(),
    /** CastSize names excluded from the search. */
    val excludedCasts: Set<String> = emptySet(),
    /** v1.61.20 "Has voice" filter. */
    val voiceOnly: Boolean = false,
    /** v1.75 "Has explanation" filter. */
    val explanationOnly: Boolean = false,
    /** v1.74.5 regex search mode. */
    val useRegex: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject()
        .put("name", name)
        .put("query", query)
        .put(
            "dbIds",
            if (dbIds == null) JSONObject.NULL else JSONArray(dbIds.toList())
        )
        .put("categoryIds", JSONArray(categoryIds.toList()))
        .put("minRating", if (minRating == null) JSONObject.NULL else minRating)
        .put("notesScope", notesScope)
        .put("sortOrder", sortOrder.name)
        .put("lengthMode", lengthMode.name)
        .put("minLength", minLength)
        .put("maxLength", maxLength)
        .put("alertOnNewMatch", alertOnNewMatch)
        .put("excludedCategoryIds", JSONArray(excludedCategoryIds.toList()))
        .put("maturity", maturity ?: JSONObject.NULL)
        .put("castSize", castSize ?: JSONObject.NULL)
        .put("excludedMaturities", JSONArray(excludedMaturities.toList()))
        .put("excludedCasts", JSONArray(excludedCasts.toList()))
        .put("voiceOnly", voiceOnly)
        .put("explanationOnly", explanationOnly)
        .put("useRegex", useRegex)

    companion object {
        fun fromJson(o: JSONObject): NamedSearch? {
            return try {
                val dbIdsRaw = o.opt("dbIds")
                val dbIds: Set<String>? =
                    if (dbIdsRaw == null || dbIdsRaw == JSONObject.NULL) null
                    else {
                        val arr = o.getJSONArray("dbIds")
                        (0 until arr.length())
                            .mapTo(mutableSetOf()) { arr.getString(it) }
                    }
                val catArr = o.optJSONArray("categoryIds")
                val catIds = if (catArr == null) emptySet()
                else (0 until catArr.length())
                    .mapTo(mutableSetOf()) { catArr.getLong(it) }
                val ratingRaw = o.opt("minRating")
                val xcArr = o.optJSONArray("excludedCategoryIds")
                val xcIds = if (xcArr == null) emptySet()
                else (0 until xcArr.length())
                    .mapTo(mutableSetOf()) { xcArr.getLong(it) }
                val xmArr = o.optJSONArray("excludedMaturities")
                val xmIds = if (xmArr == null) emptySet()
                else (0 until xmArr.length())
                    .mapTo(mutableSetOf()) { xmArr.optInt(it, -1) }
                    .filterTo(mutableSetOf()) { it in 0..4 }
                val xtArr = o.optJSONArray("excludedCasts")
                val xtIds = if (xtArr == null) emptySet()
                else (0 until xtArr.length())
                    .mapTo(mutableSetOf()) { xtArr.getString(it) }
                val matRaw = o.opt("maturity")
                val castRaw = o.opt("castSize")
                NamedSearch(
                    name = o.getString("name"),
                    query = o.optString("query", ""),
                    dbIds = dbIds,
                    categoryIds = catIds,
                    minRating = if (ratingRaw == null ||
                        ratingRaw == JSONObject.NULL
                    ) null else o.getInt("minRating"),
                    notesScope = o.optBoolean("notesScope", false),
                    sortOrder = try {
                        SortOrder.valueOf(o.optString("sortOrder", ""))
                    } catch (_: Exception) {
                        SortOrder.NUMBER_DESC
                    },
                    lengthMode = try {
                        LengthMode.valueOf(o.optString("lengthMode", ""))
                    } catch (_: Exception) {
                        LengthMode.ALL
                    },
                    minLength = o.optInt("minLength", 0),
                    maxLength = o.optInt("maxLength", 0),
                    // Missing key (pre-v1.48 saves) reads as false.
                    alertOnNewMatch = o.optBoolean("alertOnNewMatch", false),
                    // Missing keys (pre-v1.66 saves) read as "no filter".
                    excludedCategoryIds = xcIds,
                    maturity = if (matRaw == null || matRaw == JSONObject.NULL) null
                    else o.optString("maturity", null),
                    castSize = if (castRaw == null || castRaw == JSONObject.NULL) null
                    else o.optString("castSize", null),
                    excludedMaturities = xmIds,
                    excludedCasts = xtIds,
                    voiceOnly = o.optBoolean("voiceOnly", false),
                    // Missing key (pre-v1.75 saves) reads as false.
                    explanationOnly = o.optBoolean("explanationOnly", false),
                    useRegex = o.optBoolean("useRegex", false)
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
