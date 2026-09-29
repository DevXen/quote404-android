package com.quotebrowser.app.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * v1.88.22: Clint's "mind break between quotes" — blank lines between the
 * turns/lines WITHIN a quote body (the "menu option for the carriage
 * returns"). Off = render stored newlines exactly as-is (the original dense
 * look); Small/Medium/Large = 1/2/3 blank lines between turns.
 *
 * (v1.88.19 originally used this setting for extra dp between quote cards
 * in list views; v1.88.22 repurposed it because Clint's mental model is
 * that it controls the breaks in the quote text itself. The card spacing
 * was removed; the visible title and Off/Small/Medium/Large labels are
 * unchanged.)
 *
 * Stored as a string so new sizes can be added later without migration.
 * Uses KEY_BREAK_V2: v1.88.21 rendered 1 blank line unconditionally, so the
 * new key defaults to Small to preserve that look on update, and a stored
 * v1.88.19–21 "off" (which meant card spacing) does not silently carry over
 * into the new meaning.
 *
 * Kept OUT of DatabaseManager on purpose: it is a pure display preference
 * with its own SharedPreferences file. paragraphizeBody() (ui/QuoteClean.kt)
 * reads [breakBetweenQuotes] itself, so the single choke point in
 * cleanDisplayText/applyDisplayBreaks covers every surface. Screens also
 * collect it as state so the UI recomposes when the setting changes.
 *
 * Usage in a composable:
 *   DisplayPrefs.init(LocalContext.current)
 *   val breakSetting by DisplayPrefs.breakBetweenQuotes.collectAsState()
 */
object DisplayPrefs {
    const val BREAK_OFF = "off"
    const val BREAK_SMALL = "small"
    const val BREAK_MEDIUM = "medium"
    const val BREAK_LARGE = "large"

    val BREAK_OPTIONS = listOf(BREAK_OFF, BREAK_SMALL, BREAK_MEDIUM, BREAK_LARGE)
    val BREAK_LABELS = mapOf(
        BREAK_OFF to "Off",
        BREAK_SMALL to "Small",
        BREAK_MEDIUM to "Medium",
        BREAK_LARGE to "Large"
    )

    private const val PREFS_NAME = "display_prefs"
    /** v2 key: new meaning (blank lines between turns), defaults to Small. */
    private const val KEY_BREAK = "break_between_quotes_v2"

    @Volatile
    private var appContext: Context? = null

    private val _break = MutableStateFlow(BREAK_SMALL)

    /** Current setting; collect to recompose when it changes. */
    val breakBetweenQuotes: StateFlow<String> = _break.asStateFlow()

    /**
     * Idempotent — safe to call from every composable that reads the
     * setting, and from QuoteApp.onCreate so workers/widgets/export paths
     * (which never touch a composable) also see the persisted value.
     * First call wins and loads the persisted value.
     */
    fun init(context: Context) {
        if (appContext != null) return
        synchronized(this) {
            if (appContext != null) return
            val app = context.applicationContext
            appContext = app
            _break.value = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_BREAK, BREAK_SMALL)
                ?.takeIf { it in BREAK_OPTIONS }
                ?: BREAK_SMALL
        }
    }

    fun setBreak(value: String) {
        if (value !in BREAK_OPTIONS) return
        _break.value = value
        appContext
            ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_BREAK, value)
            ?.apply()
    }

    /** Blank lines inserted between quote-body turns for a setting value. */
    fun blanksFor(value: String): Int = when (value) {
        BREAK_SMALL -> 1
        BREAK_MEDIUM -> 2
        BREAK_LARGE -> 3
        else -> 0
    }
}
