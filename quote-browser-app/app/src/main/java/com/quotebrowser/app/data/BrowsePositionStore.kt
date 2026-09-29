package com.quotebrowser.app.data

import android.content.Context
import android.content.SharedPreferences

/**
 * "Pick up where you left off": remembers the Browse scroll position per
 * database so returning to a database scrolls back to where the user was.
 *
 * Positions live in the app's own SharedPreferences file "browse_positions"
 * (keys "pos:<dbId>", default 0 = top of list). Reads are served from the
 * prefs file directly; writes use apply(). Only indices > 0 are persisted —
 * top-of-list is the default and needs no storage.
 */
class BrowsePositionStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /** Saves the scroll position for [dbId]; indices <= 0 are ignored. */
    @Synchronized
    fun savePosition(dbId: String, index: Int) {
        if (index <= 0) return
        prefs.edit().putInt(posKey(dbId), index).apply()
    }

    /** Last saved scroll position for [dbId]; 0 when never saved. */
    @Synchronized
    fun positionFor(dbId: String): Int = prefs.getInt(posKey(dbId), 0)

    /** Drops the saved position for [dbId] (e.g. its contents changed). */
    @Synchronized
    fun clearPosition(dbId: String) {
        prefs.edit().remove(posKey(dbId)).apply()
    }

    /** Drops every saved position. */
    @Synchronized
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun posKey(dbId: String) = "$PREFIX_POS$dbId"

    companion object {
        private const val PREFS_NAME = "browse_positions"
        private const val PREFIX_POS = "pos:"
    }
}
