package com.quotebrowser.app.ui.screens.games

import android.content.Context

/**
 * Persists per-game best score / best streak for the v1.44 games in the
 * "game_prefs_v144" SharedPreferences file.
 */
class GamePrefs private constructor(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBestScore(key: String): Int = prefs.getInt(KEY_BEST_SCORE + key, 0)

    /** Raises the stored best score for [key] when [value] beats it. */
    fun updateBestScore(key: String, value: Int) {
        if (value > getBestScore(key)) {
            prefs.edit().putInt(KEY_BEST_SCORE + key, value).apply()
        }
    }

    fun getBestStreak(key: String): Int = prefs.getInt(KEY_BEST_STREAK + key, 0)

    /** Raises the stored best streak for [key] when [value] beats it. */
    fun updateBestStreak(key: String, value: Int) {
        if (value > getBestStreak(key)) {
            prefs.edit().putInt(KEY_BEST_STREAK + key, value).apply()
        }
    }

    companion object {
        const val PREFS_NAME = "game_prefs_v144"
        private const val KEY_BEST_SCORE = "best_score_"
        private const val KEY_BEST_STREAK = "best_streak_"

        /** Game keys used with this store. */
        const val FILL_BLANK = "fillblank"
        const val ODD_ONE_OUT = "oddoneout"
        const val SORT_RATING = "sortrating"
        const val GUESS_DB = "guessdb"
        const val SPEED_ROUND = "speedround"
        const val TOURNAMENT = "tournament"

        /** Round-two game keys (v1.45). */
        const val PASS_AND_PLAY = "passandplay"
        const val COMPLETE_QUOTE = "completequote"
        const val TRUE_FALSE = "truefalse"
        const val TOP_100 = "top100game"
        const val MEMORY_MATCH = "memorymatch"
        const val SURVIVOR = "survivor"

        /** Round-three game keys (v1.48). */
        const val LINE_SCRAMBLE = "linescramble"
        const val BLITZ_WHO_SAID_IT = "blitzwhosaidit"
        const val RATING_GUESS = "ratingguess"

        /** Quote battle key (v1.49): all-time best pick streak. */
        const val QUOTE_BATTLE = "quotebattle"

        fun of(context: Context): GamePrefs = GamePrefs(context)
    }
}
