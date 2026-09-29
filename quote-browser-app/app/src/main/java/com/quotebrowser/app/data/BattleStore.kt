package com.quotebrowser.app.data

import android.content.Context
import android.content.SharedPreferences

/** One quote's Quote-battle record: Elo rating plus total wins. */
data class BattleEntry(
    val dbId: String,
    val number: Int,
    val elo: Double,
    val wins: Int
)

/**
 * Persistent Elo ratings for Quote battle. One rating per quote, keyed
 * "dbId/number", stored in the app's own SharedPreferences file
 * "quote_battle" (keys "elo:<dbId>/<number>", default 1500.0; per-quote
 * win counts under "wins:<dbId>/<number>"). Reads are cached in memory;
 * writes use apply().
 */
class BattleStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** In-memory read-through caches; the prefs file is the source of truth. */
    private val eloCache = mutableMapOf<String, Double>()
    private val winsCache = mutableMapOf<String, Int>()

    /** Current Elo for a quote; 1500.0 when it has never battled. */
    @Synchronized
    fun eloFor(dbId: String, number: Int): Double {
        val key = eloKey(dbId, number)
        return eloCache.getOrPut(key) {
            prefs.getFloat(key, DEFAULT_ELO.toFloat()).toDouble()
        }
    }

    /** Total battle wins for a quote; 0 when it has never won. */
    @Synchronized
    fun winsFor(dbId: String, number: Int): Int {
        val key = winsKey(dbId, number)
        return winsCache.getOrPut(key) { prefs.getInt(key, 0) }
    }

    /**
     * Records a battle result using standard Elo (K = 32): the winner
     * gains rating, the loser loses the same amount, and the winner's
     * win count and the total battle count go up by one.
     */
    @Synchronized
    fun recordWin(winnerDbId: String, winnerNumber: Int, loserDbId: String, loserNumber: Int) {
        val wKey = eloKey(winnerDbId, winnerNumber)
        val lKey = eloKey(loserDbId, loserNumber)
        val wElo = eloFor(winnerDbId, winnerNumber)
        val lElo = eloFor(loserDbId, loserNumber)
        val expected = 1.0 / (1.0 + Math.pow(10.0, (lElo - wElo) / 400.0))
        val newW = wElo + K * (1.0 - expected)
        val newL = lElo + K * (0.0 - (1.0 - expected))
        eloCache[wKey] = newW
        eloCache[lKey] = newL
        val wWinKey = winsKey(winnerDbId, winnerNumber)
        val newWins = winsFor(winnerDbId, winnerNumber) + 1
        winsCache[wWinKey] = newWins
        prefs.edit()
            .putFloat(wKey, newW.toFloat())
            .putFloat(lKey, newL.toFloat())
            .putInt(wWinKey, newWins)
            .putInt(KEY_TOTAL, battleCount() + 1)
            .apply()
    }

    /** Total battles recorded. */
    @Synchronized
    fun battleCount(): Int = prefs.getInt(KEY_TOTAL, 0)

    /** Top quotes by Elo, highest first, capped at [limit]. */
    @Synchronized
    fun topRated(limit: Int): List<BattleEntry> {
        val entries = mutableListOf<BattleEntry>()
        for ((key, value) in prefs.all) {
            if (!key.startsWith(PREFIX_ELO)) continue
            val id = key.removePrefix(PREFIX_ELO)
            val number = id.substringAfterLast('/').toIntOrNull() ?: continue
            val dbId = id.substringBeforeLast('/')
            val elo = (value as? Number)?.toDouble() ?: continue
            entries += BattleEntry(dbId, number, elo, prefs.getInt(PREFIX_WINS + id, 0))
        }
        return entries.sortedByDescending { it.elo }.take(limit)
    }

    private fun eloKey(dbId: String, number: Int) = "$PREFIX_ELO$dbId/$number"
    private fun winsKey(dbId: String, number: Int) = "$PREFIX_WINS$dbId/$number"

    companion object {
        private const val PREFS_NAME = "quote_battle"
        private const val PREFIX_ELO = "elo:"
        private const val PREFIX_WINS = "wins:"
        private const val KEY_TOTAL = "battles_total"
        private const val DEFAULT_ELO = 1500.0
        private const val K = 32.0
    }
}
