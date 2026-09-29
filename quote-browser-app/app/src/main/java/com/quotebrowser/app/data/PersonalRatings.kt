package com.quotebrowser.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Rate-as-you-read: the user's own 1-5 star ratings per quote, distinct
 * from the archive's public score. Stored in the app's own
 * SharedPreferences file "personal_ratings" (keys
 * "rating:<dbId>/<number>"; 0 or missing = unrated). Reads are cached in
 * memory; writes use apply(). Nick/category averages are resolved by the
 * caller via [allRatings] (key "dbId/number"), which also powers the
 * "More like this" taste boost.
 */
class PersonalRatings(context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** In-memory read-through cache; the prefs file is the source of truth. */
    private val cache = mutableMapOf<String, Int>()

    /**
     * The user's rating for a quote, 1..5; 0 when it has never been rated
     * (tapping the currently-selected star clears back to 0).
     */
    @Synchronized
    fun ratingFor(dbId: String, number: Int): Int {
        val key = ratingKey(dbId, number)
        return cache.getOrPut(key) { prefs.getInt(key, 0).coerceIn(0, 5) }
    }

    /** Sets the rating (1..5); 0 clears it back to unrated. */
    @Synchronized
    fun setRating(dbId: String, number: Int, stars: Int) {
        val key = ratingKey(dbId, number)
        val value = stars.coerceIn(0, 5)
        cache[key] = value
        if (value == 0) prefs.edit().remove(key).apply()
        else prefs.edit().putInt(key, value).apply()
    }

    /**
     * Every rated quote as "dbId/number" -> stars (1..5). Unrated quotes
     * are absent. Callers resolve nicks (from quote text) and categories
     * (from the per-database CategoryStore) themselves.
     */
    @Synchronized
    fun allRatings(): Map<String, Int> {
        val out = mutableMapOf<String, Int>()
        for ((key, value) in prefs.all) {
            if (!key.startsWith(PREFIX)) continue
            val stars = (value as? Number)?.toInt()?.coerceIn(0, 5) ?: continue
            if (stars == 0) continue
            out[key.removePrefix(PREFIX)] = stars
        }
        for ((id, stars) in out) cache[PREFIX + id] = stars
        return out
    }

    /** Mean rating over all rated quotes; 0.0 when nothing is rated. */
    @Synchronized
    fun averageRating(): Double {
        val ratings = allRatings().values
        return if (ratings.isEmpty()) 0.0 else ratings.average()
    }

    /** How many quotes the user has rated. */
    @Synchronized
    fun ratedCount(): Int = allRatings().size

    private fun ratingKey(dbId: String, number: Int) = "$PREFIX$dbId/$number"

    companion object {
        private const val PREFS_NAME = "personal_ratings"
        private const val PREFIX = "rating:"
    }
}
