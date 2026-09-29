package com.quotebrowser.app.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.OpenableColumns
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.quotebrowser.app.data.plugins.PluginManager
import com.quotebrowser.app.ui.TransformMode
import com.quotebrowser.app.ui.theme.CustomTheme
import com.quotebrowser.app.ui.theme.DevXenBgMode
import com.quotebrowser.app.ui.theme.customThemeId
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.cleanDisplayText
import com.quotebrowser.app.ui.cleanSpokenText
import com.quotebrowser.app.ui.QuoteCleanOptions
import com.quotebrowser.app.worker.BackupWorker
import com.quotebrowser.app.worker.QotdWorker
import com.quotebrowser.app.worker.QuoteReminderScheduler
import com.quotebrowser.app.worker.QuoteAlarmScheduler
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** A quote database the user can switch to. id is "asset:<file>" for bundled DBs. */
data class DbInfo(
    val id: String,
    val label: String,
    val quoteCount: Int? = null,
    /** Collection/snapshot date of a bundled database, e.g. "04.03.2011". */
    val date: String? = null
) {
    /**
     * Title shown everywhere the database appears: just the label.
     * v1.55: collection dates no longer appear in database names —
     * they live in the per-database info panel instead.
     */
    fun displayTitle(showDates: Boolean = false): String = label
}

/** One quote staged in the export queue: the database it came from plus its number. */
data class BasketEntry(val dbId: String, val number: Int)

/**
 * v1.53 report/flag: one user's flag on a quote — a preset reason, optional
 * free text, and when it was flagged. Stored on-device only.
 */
data class FlagInfo(
    val reason: String,
    val customText: String = "",
    val flaggedAt: Long = System.currentTimeMillis()
)

/** A flagged quote with its database, text, and flag details for review. */
data class FlaggedSourcedQuote(
    val dbId: String,
    val quote: Quote,
    val reason: String,
    val customText: String,
    val flaggedAt: Long
)

/**
 * Owns the set of available quote databases (bundled assets + user imports),
 * the currently selected one, and the cached Room instances.
 *
 * Bundled databases are discovered at runtime from the assets/databases/ folder, so
 * dropping another .db into that folder is enough to add it — no code changes.
 *
 * v1.88.19: application-scoped singleton — the constructor is private and
 * every entry point (QuoteApp, workers, widget providers, broadcast
 * receivers) goes through [getInstance]. One instance per process means
 * one meta.db Room handle, one [quoteDbCache], and one set of in-memory
 * caches (currentDbId, notes, ratings, folders, themes), so no caller can
 * re-open a database the singleton already has open or read divergent
 * cached state.
 */
class DatabaseManager private constructor(private val appContext: Context) {

    /** Per-database stats shown in the v1.55 database info panel. */
    data class DatabaseStats(
        val dbId: String,
        val label: String,
        val collectionDate: String?,
        val totalQuotes: Int?,
        val approvedCount: Int?,
        val queuedCount: Int?,
        val favoritesCount: Int,
        val categoryCount: Int
    )

    /**
     * v1.55: gather stats for the database info panel — collection date,
     * total/approved/queued quote counts, favorites in this database, and
     * the number of categories defined for it.
     */
    suspend fun databaseStats(id: String): DatabaseStats = withContext(Dispatchers.IO) {
        val label = shortLabel(id)
        val fileName = id.removePrefix("asset:")
        val date = BUNDLED_DB_DATES[fileName]
            ?: bundledDbs().find { it.id == id }?.date
        var total: Int? = null
        var approved: Int? = null
        var queued: Int? = null
        var categories = 0
        try {
            val qdb = getQuoteDatabase(id)
            val dao = qdb.quoteDao()
            total = dao.count()
            approved = dao.countByStatus("approved")
            queued = dao.countByStatus("queued")
            categories = try {
                CategoryStore(qdb).list().size
            } catch (e: Exception) { 0 }
        } catch (e: Exception) {
            // Unavailable (e.g. not downloaded yet) — leave counts null.
        }
        val favorites = try {
            metaDb.metaDao().favoriteEntries(id).first().size
        } catch (e: Exception) { 0 }
        DatabaseStats(id, label, date, total, approved, queued, favorites, categories)
    }

    /** Application context for components that need it (e.g. TTS). */
    internal val context: Context get() = appContext

    companion object {
        /**
         * v1.88.19: the one application-scoped instance. Workers, widget
         * providers, and broadcast receivers used to each construct their
         * own DatabaseManager(context), so every widget refresh and worker
         * run re-opened meta.db and rebuilt a Room instance for every
         * database it touched (including the Bash.org DB), with separate
         * in-memory caches that could serve divergent data. The instance
         * always holds the application context, so callers may pass any
         * Context. Double-checked locking: construction is rare (once per
         * process) and every later call is a volatile read.
         */
        @Volatile
        private var singletonInstance: DatabaseManager? = null

        fun getInstance(context: Context): DatabaseManager {
            singletonInstance?.let { return it }
            return synchronized(DatabaseManager::class.java) {
                singletonInstance ?: DatabaseManager(
                    context.applicationContext
                ).also { singletonInstance = it }
            }
        }

        private const val PREFS = "quote_browser_prefs"
        private const val KEY_CURRENT_DB = "current_db_id"
        // v1.57: Browse/home database multi-select — comma-separated database
        // ids. Empty/missing = all databases (matches the v1.30 all-home
        // default). Selection persists across launches.
        private const val KEY_BROWSE_DB_IDS = "browse_db_ids"
        private const val KEY_QOTD_NOTIF = "qotd_notif_enabled"
        private const val KEY_SHOW_NUMBER = "show_quote_number"
        private const val KEY_SHOW_RATING = "show_rating"
        private const val KEY_SHOW_URL = "show_url"
        private const val KEY_THEME = "theme_id"
        private const val KEY_DEVXEN_BG_MODE = "devxen_bg_mode"
        private const val KEY_DEVXEN_CUSTOM_BG = "devxen_custom_bg"
        private const val KEY_CUSTOM_THEME = "custom_theme_colors"
        private const val KEY_CUSTOM_THEMES = "custom_themes_json"
        private const val KEY_TEXT_SCALE = "text_scale"
        private const val KEY_RANDOM_SOURCES = "random_source_ids"
        private const val KEY_BASKET = "export_basket"
        private const val KEY_SAVED_SEARCHES = "saved_searches"
        private const val KEY_FAV_FOLDERS = "favorite_folders"
        private const val KEY_FAV_FOLDER_MAP = "favorite_folder_map"
        private const val KEY_SHAKE_SHUFFLE = "shake_to_shuffle"
        private const val KEY_HIDE_SEEN = "hide_seen_quotes"
        private const val KEY_CONTENT_FILTER = "content_filter_enabled"
        private const val KEY_OFFENSIVE_WORDS = "offensive_words_json"
        private const val KEY_NOTES = "quote_notes_json"
        private const val KEY_MEMORIZED_FLASHCARDS = "flashcard_memorized_set"
        private const val KEY_SRS_CARDS = "flashcard_srs_cards"
        private const val KEY_DAILY_GOAL = "daily_reading_goal"
        private const val KEY_GAME_BEST_STREAK = "game_best_streak"
        private const val KEY_QOTD_MINIMIZED = "qotd_card_minimized"
        private const val KEY_LENGTH_LONGER_WORDS = "length_longer_words"
        private const val KEY_LENGTH_SHORTER_WORDS = "length_shorter_words"
        private const val KEY_WALLPAPER_DAILY = "wallpaper_daily_enabled"
        private const val KEY_WALLPAPER_SOURCE = "wallpaper_source"
        private const val KEY_WIDGET_CFG_PREFIX = "widget_cfg_"
        private const val KEY_WIDGET_CATEGORY = "widget_category_name"
        private const val KEY_LOCAL_RATINGS = "local_ratings_json"
        private const val KEY_AUTO_BACKUP_FREQ = "auto_backup_freq"
        private const val KEY_AUTO_BACKUP_TREE = "auto_backup_tree_uri"
        private const val KEY_AUTO_BACKUP_LAST = "auto_backup_last"
        private const val AUTO_BACKUP_WORK = "auto_backup"
        private const val KEY_QUOTE_PING_ENABLED = "quote_ping_enabled"
        private const val KEY_QUOTE_PING_MINUTES = "quote_ping_minutes"
        private const val KEY_QUOTE_PING_HOUR = "quote_ping_hour"
        private const val KEY_QUOTE_PING_MINUTE = "quote_ping_minute"
        private const val KEY_QUOTE_PING_DAY = "quote_ping_day"
        private const val KEY_QUOTE_PING_DBS = "quote_ping_dbs"
        private const val QUOTE_PING_WORK = "quote_ping"
        // v1.74: notification-quote filters.
        private const val KEY_QUOTE_PING_USE_GLOBAL_CEILING = "quote_ping_use_global_ceiling"
        private const val KEY_QUOTE_PING_CUSTOM_CEILING = "quote_ping_custom_ceiling"
        private const val KEY_QUOTE_PING_WORK_HOURS = "quote_ping_work_hours"
        private const val KEY_QUOTE_PING_WORK_DAYS = "quote_ping_work_days"
        private const val KEY_QUOTE_PING_WORK_START = "quote_ping_work_start"
        private const val KEY_QUOTE_PING_WORK_END = "quote_ping_work_end"
        private const val KEY_QUOTE_PING_WORK_CEILING = "quote_ping_work_ceiling"
        private const val KEY_QUOTE_PING_MIN_RATING = "quote_ping_min_rating"
        private const val KEY_QUOTE_PING_CATEGORIES = "quote_ping_categories_json"
        private const val KEY_QUOTE_PING_RECENCY_DAYS = "quote_ping_recency_days"
        private const val KEY_QUOTE_PING_HISTORY = "quote_ping_history_json"
        private const val KEY_SWIPE_LEFT = "swipe_action_left"
        private const val KEY_SWIPE_RIGHT = "swipe_action_right"
        private const val KEY_SWIPE_DEFAULTS_V2 = "swipe_defaults_v2"
        private const val KEY_SWIPE_HOME = "swipe_enabled_home"
        private const val KEY_SWIPE_RANDOM = "swipe_enabled_random"
        private const val KEY_WHO_SAID_BEST = "who_said_best_streak"
        private const val KEY_RATING_GUESS_BEST = "rating_guess_best_streak"
        private const val KEY_DB_ORDER = "db_order_json"
        private const val KEY_SHOW_DB_DATES = "show_db_dates"
        private const val KEY_QUOTE_FONT = "quote_font"
        private const val KEY_MUTED_NICKS = "muted_nicks_json"
        private const val KEY_NAMED_SEARCHES = "named_searches_json"
        private const val KEY_DC_LAST_DATE = "daily_challenge_last_date"
        private const val KEY_DC_LAST_SCORE = "daily_challenge_last_score"
        // v1.49 feature toggles (all default ON unless noted).
        private const val KEY_VACATION_MODE = "vacation_mode"
        private const val KEY_VACATION_ENABLED = "vacation_enabled"
        private const val KEY_VACATION_START_DAY = "vacation_start_day"
        private const val KEY_VACATION_END_DAY = "vacation_end_day"
        private const val KEY_RANDOM_NOREPEAT = "random_no_repeat"
        private const val KEY_STARTUP_SCREEN = "startup_screen"
        private const val KEY_QUOTE_BATTLE_ENABLED = "quote_battle_enabled"
        // v1.50 feature toggles (all default ON).
        private const val KEY_STATS_CARD_SHARE = "stats_card_share"
        private const val KEY_EXTRA_WIDGETS = "extra_widgets"
        private const val KEY_RANDOM_CYCLE = "random_cycle_seen_json"
        private const val KEY_WEEKLY_GOAL = "weekly_reading_goal"
        private const val KEY_MONTHLY_GOAL = "monthly_reading_goal"
        private const val KEY_GESTURES = "tap_gestures_enabled"
        private const val KEY_DUP_MERGE = "dup_merge_enabled"
        private const val KEY_BULK_SELECT_ALL = "bulk_select_all"
        // v1.50: rate-as-you-read personal star ratings.
        private const val KEY_PERSONAL_RATINGS = "personal_ratings"
        private const val KEY_NOTES_ENABLED = "notes_enabled"
        private const val KEY_SLIDESHOW_EXPORT = "slideshow_export_enabled"
        private const val KEY_READING_GOALS = "reading_goals_enabled"
        private const val KEY_NICK_TIMELINE = "nick_timeline_enabled"
        // v1.51: "Pick up where you left off" — remember the Browse scroll
        // position per database (default ON).
        private const val KEY_BROWSE_POSITION_MEMORY = "browse_position_memory"
        // v1.50 feature toggles (all default ON unless noted).
        private const val KEY_PINCH_ZOOM_TEXT = "pinch_zoom_text"
        private const val KEY_DETAIL_TEXT_SCALE = "detail_text_scale"
        private const val KEY_SWIPE_DEFAULTS_V3 = "swipe_defaults_v3"
        private const val KEY_DC_BEST_SCORE = "daily_challenge_best_score"
        private const val KEY_DC_STREAK = "daily_challenge_streak"
        private const val KEY_DETAIL_SWIPE_ALL = "detail_swipe_all_dbs"
        private const val KEY_DETAIL_SWIPE_ORDER = "detail_swipe_order"
        private const val KEY_ALL_HOME_MIGRATED = "all_home_migrated_v130"
        private const val KEY_DENSITY = "list_density"
        private const val KEY_NICK_RULES = "nick_color_rules"
        private const val KEY_NICK_ENABLED = "nick_coloring_enabled"
        // v1.63 nick color themes.
        private const val KEY_NICK_COLOR_THEME = "nick_color_theme"
        private const val KEY_CUSTOM_NICK_THEMES = "custom_nick_color_themes"
        private const val KEY_NICK_COLOR_MESSAGE = "nick_color_message"
        // v1.88.32: per-card-theme nick palette overrides — JSON object
        // {themeKey: nickThemeId}; a missing key = "Use global".
        private const val KEY_THEME_NICK_PALETTES = "theme_nick_palettes"
        // v1.88.32: per-chat-theme "remove nicks from message text" —
        // JSON array of card theme keys where the speaker prefix is
        // stripped from message bodies. Absent = OFF (default).
        private const val KEY_THEME_NICK_STRIP = "theme_nick_strip_nicks"
        private const val KEY_IRC_LINE_BREAKS = "irc_line_breaks"
        // Daily quote reminder notification (defaults OFF).
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour"
        private const val KEY_DAILY_REMINDER_MINUTE = "daily_reminder_minute"
        // Quote alarm (v1.78): reads a favorite quote aloud daily (defaults OFF).
        private const val KEY_QUOTE_ALARM_ENABLED = "quote_alarm_enabled"
        private const val KEY_QUOTE_ALARM_HOUR = "quote_alarm_hour"
        private const val KEY_QUOTE_ALARM_MINUTE = "quote_alarm_minute"
        // QuoteClean (v1.47) toggles.
        private const val KEY_QC_COLOR_NAMES = "quoteclean_color_names"
        private const val KEY_QC_COLOR_ACTIONS = "quoteclean_color_actions"
        private const val KEY_QC_NORMALIZE_BREAKS = "quoteclean_normalize_breaks"
        private const val KEY_QC_SHOW_TIMESTAMPS = "quoteclean_show_timestamps"
        // v1.86: per-category QuoteClean controls.
        private const val KEY_QC_COLOR_MESSAGE = "quoteclean_color_message"
        private const val KEY_QC_NAME_COLOR = "quoteclean_name_color"
        private const val KEY_QC_MESSAGE_COLOR = "quoteclean_message_color"
        private const val KEY_QC_ACTION_COLOR = "quoteclean_action_color"
        private const val KEY_QC_TIMESTAMP_COLOR = "quoteclean_timestamp_color"
        private const val KEY_QC_COLOR_OTHER = "quoteclean_color_other"
        private const val KEY_QC_OTHER_COLOR = "quoteclean_other_color"
        // v1.47 one-time upgrade stamp: QuoteClean render/read-aloud
        // pipeline active for all databases.
        private const val KEY_QC_UPGRADED_V147 = "quoteclean_upgraded_v147"
        // v1.47 one-time upgrade stamp: the first auto-rules sweep (user
        // phrases + built-in "Offensive" list) has run over every database.
        private const val KEY_AUTORULES_UPGRADED_V147 = "autorules_upgraded_v147"
        private const val KEY_DIALOGUE_BREAKS = "dialogue_line_breaks"
        // v1.65 global content maturity ceiling (MaturityLevel enum name).
        private const val KEY_MATURITY_CEILING = "maturity_ceiling"
        private const val KEY_DB_TAG_COLORS = "db_tag_colors"
        // v1.51 per-database themes + site presentations.
        private const val KEY_BUILTIN_THEME_OVERRIDES = "builtin_theme_overrides_json"
        private const val KEY_PERDB_THEMES_ENABLED = "perdb_themes_enabled"
        private const val KEY_MASTER_THEME_OVERRIDE = "master_theme_override"
        private const val KEY_DB_THEME_CONFIGS = "db_theme_configs_json"
        private const val KEY_CLASSIC_LAYOUT_OPTION = "classic_layout_option"
        // v1.59 "database themes only on quote cards" (default OFF).
        private const val KEY_DB_THEMES_CARDS_ONLY = "db_themes_cards_only"
        // v1.59 daily-notification quote source: "qotd" (default, current
        // behavior), "favorites", or "random".
        private const val KEY_DAILY_QUOTE_SOURCE = "daily_quote_source"
        private const val KEY_PLUSMINUS_RATINGS = "plusminus_ratings_enabled"
        private const val KEY_QUEUED_TAGS = "queued_tags_enabled"
        private const val KEY_STATUS_FILTER = "status_filter_enabled"
        private const val KEY_LEET_TRANSFORM = "leet_transform_enabled"
        private const val KEY_UPSIDEDOWN_TRANSFORM = "upsidedown_transform_enabled"
        private const val KEY_ALTERNATING_TRANSFORM = "alternating_case_enabled"
        // v1.52: single mutually-exclusive transform mode (replaces the
        // three v1.51 stacking toggles above, which shipped defaulting ON).
        private const val KEY_TRANSFORM_MODE = "transform_mode"
        private const val KEY_TRANSFORM_MODE_MIGRATED = "transform_mode_migrated_v152"
        // v1.53 feature toggles.
        private const val KEY_MY_QUOTES_FEATURE = "my_quotes_feature_enabled"
        private const val KEY_PER_DB_WIDGET = "per_db_widget_enabled"
        private const val KEY_MEMORIZE_MODE = "memorize_mode_enabled"
        private const val KEY_ON_THIS_DAY = "on_this_day_enabled"
        private const val KEY_KID_SAFE = "kid_safe_enabled"
        private const val KEY_SITE_SHARE_CARDS = "site_share_cards_enabled"
        private const val KEY_EXPLAIN_QUOTE = "explain_quote_enabled"
        private const val KEY_AMBIENT_MODE = "ambient_mode_enabled"
        private const val KEY_AMBIENT_INTERVAL = "ambient_interval_sec"
        // v1.53 report/flag feature: per-quote flags stored on-device only.
        private const val KEY_FLAG_QUOTES = "flag_quotes_enabled"
        private const val KEY_HIDE_FLAGGED_QUOTES = "hide_flagged_quotes"
        private const val KEY_FLAGGED_QUOTES = "flagged_quotes_json"
        // v1.53 main-theme option: black page background outside the
        // bordered quote cards (QDB screenshot aesthetic). Default ON;
        // applies even when a site theme is the global theme.
        private const val KEY_BLACK_OUTSIDE_QUOTES = "black_outside_quotes"
        private const val KEY_HIDDEN_QUOTES = "hidden_quotes_json"
        private const val KEY_QOTD_HOUR = "qotd_hour"
        private const val KEY_QOTD_MINUTE = "qotd_minute"
        private const val KEY_TTS_PER_NICK = "tts_per_nick_voices"
        // v1.86: one-time stamp for the Speaker-voices force-ON migration.
        private const val KEY_TTS_VOICES_V186_MIGRATED = "tts_voices_v186_migrated"
        private const val ASSET_DIR = "databases"
        private const val QOTD_WORK = "qotd_daily"

        /** Id of the built-in database holding quotes the user adds themselves. */
        const val MY_QUOTES_ID = "myquotes"
        const val MY_QUOTES_LABEL = "My Quotes"

        /**
         * Virtual database id selecting every database at once. Only the
         * Browse (home) screen renders it; every other screen falls back to
         * [firstBundledDbId] when it is selected.
         */
        const val ALL_DBS_ID = "all"
        const val ALL_DBS_LABEL = "All databases"
        /** v1.58: asset id of the qdb.us database (per-quote capture dates). */
        const val QDB_US_ASSET_ID = "asset:qdb_us.db"
        /** v1.88.2: asset id of the bash.im database (genuine posted dates). */
        const val BASH_IM_ASSET_ID = "asset:bash_im.db"

        /** Friendly labels for bundled databases whose file names don't prettify well. */
        private val BUNDLED_LABEL_OVERRIDES = mapOf(
            // v1.47: user-specified bash.org archive date (08.20.2023)
            // is appended as the collection-date suffix, like other DBs.
            "bash_org.db" to "Bash.org",
            "crapshack_quotes.db" to "#CrapShack Quotes",
            "irox_com.db" to "I-Rox.com",
            "mandaliet_furcqdb.db" to "Mandaliet.com/furcqdb",
            "qdb_mit_edu.db" to "QDB.MIT.edu",
            "qdb_us.db" to "qdb.us",
            "quoteirc_com.db" to "QuoteIRC.com",
            "burntelectrons_org.db" to "Quotes.BurntElectrons.org",
            "wqdb_org.db" to "WQDB.org",
            "warpdrive_se.db" to "Warpdrive.se",
            "bash_im.db" to "bash.im",
            "xkcdb_com.db" to "xkcdb.com"
        )

        /**
         * Collection (scrape) date for bundled databases, taken from the
         * archive file names ("DOMAIN.TLD Quote Collection YYYY-MM-DD").
         * Displayed as MM.DD.YYYY with periods, never dashes (v1.47).
         * Databases with no known date are absent from the map.
         */
        private val BUNDLED_DB_DATES = mapOf(
            "bash_org.db" to "08.20.2023",
            "irox_com.db" to "04.07.2011",
            "mandaliet_furcqdb.db" to "04.06.2011",
            "qdb_mit_edu.db" to "04.05.2011",
            "qdb_us.db" to "01.23.2020",
            "quoteirc_com.db" to "04.04.2011",
            "burntelectrons_org.db" to "04.06.2011",
            "wqdb_org.db" to "04.05.2011",
            "warpdrive_se.db" to "04.05.2011",
            // v1.88: bash.im — newest quote in the corpus (2004-08-30 → 2020-11-26).
            "bash_im.db" to "11.26.2020",
            "xkcdb_com.db" to "04.05.2011"
        )

        /** Maximum per-person nick color rules (see NickColors.kt). */
        const val MAX_NICK_RULES = 10

        /** Bump when shipping a new category_seed.json. v1.61.21 = 2 (adds
         * the "Removed" category and tags the 4 restored bash.org quotes). */
        const val CATEGORY_SEED_VERSION = 4

        /**
         * Bundled-data revision. Bump when a bundled asset database ships
         * new/changed quote data; [refreshBundledDatabases] re-merges every
         * bundled file whose stored revision is older. v1.50.1 = 1 (qdb.us
         * refresh: 13,813 -> 15,573 quotes, plus the `status` column).
         * v1.50.2 = 2 (re-run the refresh so files mangled by the v1.50.1
         * raw user_version=2 bump get reset to 1 and go through Room's
         * Migration(1,2), which stamps the correct identity hash).
         * v1.56 = 3 (re-run again: v1.50.1/1.50.2 could stamp revision 2
         * without the merge actually happening — the old gate only merged
         * when asset user_version > device user_version, so devices whose
         * file already reported version 2 kept stale data (e.g. qdb.us at
         * 13,813 quotes) forever. The merge is now gated on the actual
         * data fingerprint, and the stamp is written only after the merge
         * validates.)
         * v1.61.21 = 5 (bash.org: 21,096 -> 21,100 quotes — the 4 quotes
         * removed from bash.org after the 2013 scrape, tagged "Removed").
         * v1.87 = 11 (warpdrive.se: repaired Swedish source text —
         * the 5,901 �-damaged quotes restored from the retranslation
         * pass; English translations now seed from the curated JSONL).
         * v1.88 = 12 (bash.im: new 81,497-quote Russian archive added;
         * English translations seed from the curated JSONL).
         */
        private const val BUNDLED_DATA_REVISION = 14

        /**
         * v1.87: process-wide guard so the background bundled refresh
         * starts exactly once — workers construct DatabaseManager too.
         */
        private val bundledRefreshStarted =
            java.util.concurrent.atomic.AtomicBoolean(false)

        /**
         * v1.88.6: one JVM lock per bundled database file, shared by the
         * schema healer and the background refresh merge. The v1.88.4/
         * v1.88.5 crashes were a lock race: the refresh thread can hold a
         * SQLite write lock on a stale file for many seconds during its
         * merge, while the UI thread's pre-heal only waited 5s
         * (busy_timeout) and then failed SQLITE_BUSY — Room validated the
         * unhealed schema, the process died, and the refresh never
         * finished, so the next launch crashed the same way. With this
         * lock the healer waits on the JVM monitor (no timeout) instead
         * of on SQLite's busy handler, so the heal always lands once the
         * merge releases the file.
         */
        private val bundledDbLocks =
            java.util.concurrent.ConcurrentHashMap<String, Any>()

        private fun bundledDbLock(assetName: String): Any =
            bundledDbLocks.getOrPut(assetName) { Any() }

        /**
         * v1.87: the bundled refresh now merges on a background thread
         * while Room may hold the same file open (the merge is one bulk
         * INSERT OR REPLACE — a single short write transaction — but a
         * user write landing in the same instant, e.g. assigning a
         * category, or a v1.60 download-merge, would otherwise hit
         * SQLITE_BUSY). A 5-second busy timeout on both sides makes each
         * writer wait out the other's short lock instead of failing.
         * The merge sets the same pragma on its raw connection (below).
         */
        private val bundledBusyTimeoutCallback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("PRAGMA busy_timeout = 5000")
                } catch (_: Exception) {
                }
            }
        }

        /** SharedPreferences key prefix tracking per-file bundled revisions. */
        private const val KEY_BUNDLED_DATA_REV = "bundled_data_rev_"

        /** v1.60: the user's configured database-update manifest URL. */
        private const val KEY_DB_UPDATE_URL = "db_update_url"

        /** v1.60: per-file revision stamps for applied download updates. */
        private const val KEY_DB_UPDATE_REV = "db_update_rev_"

        /** v1.60: caps for the update manifest / database downloads. */
        private const val MAX_MANIFEST_BYTES = 256 * 1024
        private const val MAX_DB_DOWNLOAD_BYTES = 150L * 1024 * 1024

        /** "bash_org" -> "Bash Org", "qdb_us" -> "Qdb Us". */
        fun prettifyLabel(raw: String): String =
            raw.split('_', '-', ' ')
                .filter { it.isNotEmpty() }
                .joinToString(" ") { part ->
                    part.replaceFirstChar { it.uppercaseChar() }
                }
    }

    val metaDb: MetaDatabase = Room.databaseBuilder(
        appContext, MetaDatabase::class.java, "meta.db"
    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

    /**
     * v1.61: quote translations. Own Room file (`translations.db`) so each
     * quote is translated once per target language; included in full
     * backups (see BackupEngine). Lazy: opened on first translation use.
     */
    val translations: TranslationRepository by lazy { TranslationRepository(appContext) }

    /** Records that the user opened a quote (feeds reading stats). */
    suspend fun markSeen(dbId: String, number: Int) = withContext(Dispatchers.IO) {
        metaDb.metaDao().markSeen(SeenQuote(dbId, number))
    }

    /** Number of distinct quotes seen in one database. */
    fun seenCount(dbId: String): kotlinx.coroutines.flow.Flow<Int> =
        metaDb.metaDao().seenCount(dbId)

    /**
     * Consecutive days (ending today, or yesterday if nothing was seen yet
     * today) with at least one seen quote. 0 when nothing was ever seen.
     *
     * v1.50 vacation mode: days inside the vacation window are NEUTRAL —
     * they neither count as read days nor break the streak, so the walk
     * simply skips over them.
     */
    suspend fun readingStreakDays(): Int = withContext(Dispatchers.IO) {
        val days = metaDb.metaDao().seenDays()
        if (days.isEmpty()) return@withContext 0
        val seen = days.toSet()
        val today = java.time.LocalDate.now()
        var cursor = today
        // A streak still counts if today has no views yet: start from
        // yesterday — unless today is a vacation day, which is skipped by
        // the walk below anyway.
        if (!seen.contains(cursor.toString()) && !isVacationDay(cursor)) {
            cursor = today.minusDays(1)
            if (!seen.contains(cursor.toString()) && !isVacationDay(cursor)) {
                return@withContext 0
            }
        }
        var streak = 0
        while (true) {
            when {
                isVacationDay(cursor) -> {
                    // Neutral: skip without counting and without breaking.
                }
                seen.contains(cursor.toString()) -> streak++
                else -> break
            }
            cursor = cursor.minusDays(1)
        }
        streak
    }

    /**
     * Longest run of consecutive reading days ever, from the read log.
     *
     * v1.50 vacation mode: a run continues across a vacation gap — the gap
     * does not interrupt the run, and vacation days do not add to its
     * length (only actual read days count).
     */
    suspend fun longestReadingStreakDays(): Int = withContext(Dispatchers.IO) {
        val days = metaDb.metaDao().seenDays()
        if (days.isEmpty()) return@withContext 0
        val dates = days.mapNotNull {
            try {
                java.time.LocalDate.parse(it)
            } catch (_: Exception) {
                null
            }
        }
        var best = 0
        var run = 0
        var prev: java.time.LocalDate? = null
        for (date in dates) {
            val p = prev
            if (p == null) {
                run = 1
            } else {
                val gapDays = p.toEpochDay() - date.toEpochDay()
                if (gapDays == 1L) {
                    // Consecutive read days.
                    run++
                } else if (gapDays > 1L &&
                    vacationCovers(date.plusDays(1), p.minusDays(1))
                ) {
                    // Every day between the two read days is a vacation
                    // day: the run bridges the gap (vacation days add no
                    // length of their own).
                    run++
                } else {
                    run = 1
                }
            }
            if (run > best) best = run
            prev = date
        }
        best
    }

    /**
     * v1.50 vacation mode: true when [day] falls inside the active vacation
     * window. The window runs from [vacationStartDay] through
     * [vacationEndDay], or from [vacationStartDay] onward when the end is -1
     * ("until I turn it off"). Days are [java.time.LocalDate.toEpochDay]
     * values, matching the ISO strings from the read log.
     */
    fun isVacationDay(day: java.time.LocalDate): Boolean {
        if (!_vacationEnabled.value) return false
        val start = _vacationStartDay.value
        if (start < 0) return false
        val epoch = day.toEpochDay()
        if (epoch < start) return false
        val end = _vacationEndDay.value
        return end < 0 || epoch <= end
    }

    /**
     * True when every day in [from]..[to] (inclusive) is a vacation day.
     * The window is contiguous, so covering both endpoints covers all of it.
     */
    private fun vacationCovers(
        from: java.time.LocalDate,
        to: java.time.LocalDate
    ): Boolean {
        if (!_vacationEnabled.value || _vacationStartDay.value < 0) return false
        if (from.isAfter(to)) return true // empty gap
        return isVacationDay(from) && isVacationDay(to)
    }

    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /**
     * Lazily-held Browse scroll-position store ("Pick up where you left
     * off"). Its own SharedPreferences file; cleared per-database when a
     * database's contents are replaced (see importDatabase).
     */
    val browsePositionStore: BrowsePositionStore by lazy {
        BrowsePositionStore(appContext.applicationContext)
    }

    // v1.47: background scope for the one-time auto-rules upgrade sweep.
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Progress of the one-time v1.47 auto-rules upgrade sweep (runs every
     * database's auto-rules — user phrases plus the built-in "Offensive"
     * list — over all quotes, once, in the background).
     */
    sealed interface UpgradeAutoRules {
        data object Idle : UpgradeAutoRules
        data class Running(val label: String, val done: Int, val total: Int) :
            UpgradeAutoRules
        data class Done(val added: Int, val removed: Int) : UpgradeAutoRules
    }

    private val _upgradeAutoRules =
        MutableStateFlow<UpgradeAutoRules>(UpgradeAutoRules.Idle)
    val upgradeAutoRules: StateFlow<UpgradeAutoRules> =
        _upgradeAutoRules.asStateFlow()

    private val _currentDbId = MutableStateFlow(
        prefs.getString(KEY_CURRENT_DB, null) ?: defaultDbId()
    )
    val currentDbId: StateFlow<String> = _currentDbId.asStateFlow()

    /**
     * v1.57: Browse/home database multi-select. Empty set = all databases.
     * Persisted; BrowseScreen resolves it against the available databases
     * (a single id → the classic single-database view, otherwise the
     * merged view over the subset).
     */
    private val _browseDbIds = MutableStateFlow(loadBrowseDbIds())
    val browseDbIds: StateFlow<Set<String>> = _browseDbIds.asStateFlow()

    private fun loadBrowseDbIds(): Set<String> {
        val raw = prefs.getString(KEY_BROWSE_DB_IDS, null)
        if (raw.isNullOrBlank()) return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    /** v1.57: replace the Browse/home database selection (empty = all). */
    fun setBrowseDbIds(ids: Set<String>) {
        val clean = ids.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        prefs.edit().putString(KEY_BROWSE_DB_IDS, clean.joinToString(",")).apply()
        _browseDbIds.value = clean
    }

    init {
        // v1.87: bundled-database refresh now runs on a background thread.
        // It used to run synchronously here, blocking the first frame on
        // every upgraded install (worse with each revision bump). Old data
        // may show briefly until the merge lands — acceptable. The
        // per-file revision stamp is still written only after a merge
        // validates, so failures retry on the next launch; the merge is
        // idempotent and never deletes on-device rows. Concurrency: the
        // merge is one bulk INSERT OR REPLACE per file — a single short
        // write transaction on its own raw connection (WAL mode keeps
        // Room readers unblocked throughout, and the wal_checkpoint is
        // passive under active readers). A Room *writer* landing in the
        // same instant (a category assignment, or a v1.60 download-merge)
        // no longer risks SQLITE_BUSY: both the merge connection and
        // Room's bundled connections carry PRAGMA busy_timeout = 5000,
        // so each side waits out the other's short lock. Migrations stay
        // idempotent (check-then-ALTER), and the user_version=1 dance is
        // unchanged — it only matters at Room open time, and Room never
        // re-validates mid-process. Exactly one refresh per process
        // (workers also construct this).
        //
        // v1.88.6: synchronously heal bundled schemas BEFORE the refresh
        // thread starts. A previous release's failed pre-packaged
        // validation can leave a copied file without the `status` column.
        // v1.88.7: the heal runs on a dedicated background thread, NOT on
        // the calling (main) thread — v1.88.6 ran it synchronously in
        // Application.onCreate and the app never drew its first frame
        // (frozen launch splash / ANR). The heal still runs BEFORE the
        // refresh merge on the same worker thread, so there is no lock
        // contender when it lands, and the per-database JVM lock keeps the
        // UI thread's on-demand pre-heal (getQuoteDatabase) serialized
        // against both — Room can never validate a stale schema even if
        // the user reaches a quote screen before this pass finishes.
        // This breaks the v1.88.4/v1.88.5 self-perpetuating crash loop
        // (pre-heal lost the race to the refresh merge, Room crashed, the
        // process died, the refresh never finished).
        if (bundledRefreshStarted.compareAndSet(false, true)) {
            kotlin.concurrent.thread(
                name = "bundled-heal-refresh",
                isDaemon = true
            ) {
                try {
                    healBundledSchemasSync()
                } catch (t: Throwable) {
                    Log.w("QuoteBrowser", "bundled sync heal failed", t)
                }
                // v1.88.14: the v1.88.11 pre-warm loop (opening every bundled
                // Room database here) is REMOVED. Since v1.88.12 every UI
                // open runs on a Dispatchers.IO thread, so a cold open can
                // never block the main thread — the pre-warm no longer buys
                // anything and only hurts: it holds each per-database lock
                // for a full open (pre-heal + Room validation) while the
                // UI's own IO threads queue behind those same locks, which
                // is why startup still took ~a minute on 2026-09-20 even
                // with no main-thread I/O left. On-demand opens now pay
                // only for the databases the current screen actually needs.
                // v1.88.11: the refresh merge bulk-copies up to ~81k rows
                // per database with FTS triggers firing per row — minutes
                // of disk I/O on a damaged/stale install, while holding
                // the per-database lock the whole time. On 2026-09-20
                // Clint's Fold7 sat behind ~15 ANR "Wait" taps for several
                // minutes because startup opens contended with this merge.
                // The merge only refreshes bundled *data*; the app is fully
                // usable on the existing on-device copies, so let startup
                // and the first queries settle before it runs, and run it
                // at minimum thread priority so scrolling stays smooth
                // while it grinds in the background.
                try {
                    Thread.currentThread().priority = Thread.MIN_PRIORITY
                    Thread.sleep(20000)
                } catch (t: Throwable) {
                    Log.w("QuoteBrowser", "bundled refresh delay interrupted", t)
                }
                try {
                    refreshBundledDatabases()
                } catch (t: Throwable) {
                    Log.w("QuoteBrowser", "bundled refresh failed", t)
                } finally {
                    try {
                        Thread.currentThread().priority = Thread.NORM_PRIORITY
                    } catch (_: Exception) {
                    }
                }
            }
        }
        // v1.30: the home screen now shows all databases by default.
        // One-time migration of the previous default selection.
        if (!prefs.getBoolean(KEY_ALL_HOME_MIGRATED, false)) {
            prefs.edit().putBoolean(KEY_ALL_HOME_MIGRATED, true).apply()
            _currentDbId.value = ALL_DBS_ID
            prefs.edit().putString(KEY_CURRENT_DB, ALL_DBS_ID).apply()
        }
        // v1.57: seed the Browse multi-select from the existing single
        // selection once — a user who had picked one database keeps it;
        // everyone else starts on all databases. Never clobbers a stored
        // multi-select afterwards.
        if (!prefs.getBoolean("browse_db_ids_migrated_v157", false)) {
            prefs.edit().putBoolean("browse_db_ids_migrated_v157", true).apply()
            val cur = _currentDbId.value
            if (cur != ALL_DBS_ID) setBrowseDbIds(setOf(cur))
        }
        // v1.37/v1.48: the swipe defaults were revised — swipe right
        // favorites/unfavorites, swipe left shows the next random quote.
        // Applied once so later user customizations are never clobbered.
        if (!prefs.getBoolean(KEY_SWIPE_DEFAULTS_V2, false)) {
            prefs.edit()
                .putString(KEY_SWIPE_LEFT, "next")
                .putString(KEY_SWIPE_RIGHT, "favorite")
                .putBoolean(KEY_SWIPE_DEFAULTS_V2, true)
                .apply()
        }
        // v1.49: quote-row swipes default to off (no action either
        // direction); the actions stay configurable in Settings. Only
        // overwrites when the stored actions are still the v1.48
        // defaults, so a user's own choices are never clobbered.
        if (!prefs.getBoolean(KEY_SWIPE_DEFAULTS_V3, false)) {
            val e = prefs.edit()
            if (prefs.getString(KEY_SWIPE_LEFT, "next") == "next") {
                e.putString(KEY_SWIPE_LEFT, "none")
            }
            if (prefs.getString(KEY_SWIPE_RIGHT, "favorite") == "favorite") {
                e.putString(KEY_SWIPE_RIGHT, "none")
            }
            e.putBoolean(KEY_SWIPE_DEFAULTS_V3, true).apply()
        }
        // v1.47 QuoteClean: all quote text (every bundled database, user
        // imports, My Quotes) goes through the non-destructive QuoteClean
        // display/read-aloud pipeline at render time — speaker/action
        // colorization, "/" and "\\" line-break normalization, timestamp
        // stripping — so existing data needs no rewrite; this stamp marks
        // the upgrade as processed.
        if (!prefs.getBoolean(KEY_QC_UPGRADED_V147, false)) {
            prefs.edit().putBoolean(KEY_QC_UPGRADED_V147, true).apply()
        }
        // v1.86: Clint approved turning Speaker voices ON for existing
        // installs (previously it defaulted on only for fresh installs).
        // One-time migration: a stored OFF choice flips to ON once;
        // afterwards the user's own choice is never touched. Runs in the
        // first init block, before _ttsPerNickVoices is initialized.
        if (!prefs.getBoolean(KEY_TTS_VOICES_V186_MIGRATED, false)) {
            prefs.edit().putBoolean(KEY_TTS_VOICES_V186_MIGRATED, true)
                .putBoolean(KEY_TTS_PER_NICK, true).apply()
        }
        // v1.47 auto-rules upgrade: one automatic pass over every database
        // (user phrases + the built-in "Offensive" list), reconciling tags
        // in the background with progress. The user never needs the manual
        // "Apply auto-rules now" button for this first sweep; it stays as
        // a re-run option. The stamp is written only after the sweep
        // finishes (the run is idempotent, so an interrupted pass simply
        // resumes on the next launch).
        if (!prefs.getBoolean(KEY_AUTORULES_UPGRADED_V147, false)) {
            managerScope.launch {
                _upgradeAutoRules.value = UpgradeAutoRules.Running("", 0, 1)
                val result = applyAllAutoRules { label, done, total ->
                    _upgradeAutoRules.value =
                        UpgradeAutoRules.Running(label, done, total)
                }
                prefs.edit().putBoolean(KEY_AUTORULES_UPGRADED_V147, true).apply()
                _upgradeAutoRules.value =
                    UpgradeAutoRules.Done(result.added, result.removed)
                Log.i(
                    "AutoRules",
                    "v1.47 upgrade sweep done: added=${result.added} " +
                        "removed=${result.removed}"
                )
            }
        }
    }

    /**
     * The database used whenever nothing is selected: every database at
     * once (the Browse home screen renders the merged view).
     */
    fun defaultDbId(): String = ALL_DBS_ID

    /**
     * The single database used by screens that cannot render the merged
     * "all" view: Bash.org when it's bundled, otherwise the first bundled
     * database in default order.
     */
    fun firstBundledDbId(): String {
        val bundled = bundledDbs().map { it.id }
        return bundled.firstOrNull { it == "asset:bash_org.db" }
            ?: bundled.firstOrNull()
            ?: "asset:bash_org.db"
    }

    /** Maps the virtual "all" id to a real database for single-DB screens. */
    fun singleDbId(id: String): String =
        if (id == ALL_DBS_ID) firstBundledDbId() else id

    private val _qotdNotifEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_QOTD_NOTIF, false))
    val qotdNotificationsEnabled: StateFlow<Boolean> = _qotdNotifEnabled.asStateFlow()

    // Display options: quote number, rating, and URL are hidden by default.
    private val _showNumber = MutableStateFlow(prefs.getBoolean(KEY_SHOW_NUMBER, false))
    val showQuoteNumber: StateFlow<Boolean> = _showNumber.asStateFlow()

    private val _showRating = MutableStateFlow(prefs.getBoolean(KEY_SHOW_RATING, false))
    val showRating: StateFlow<Boolean> = _showRating.asStateFlow()

    private val _showUrl = MutableStateFlow(prefs.getBoolean(KEY_SHOW_URL, false))
    val showUrl: StateFlow<Boolean> = _showUrl.asStateFlow()

    fun setShowQuoteNumber(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_NUMBER, show).apply()
        _showNumber.value = show
    }

    fun setShowRating(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_RATING, show).apply()
        _showRating.value = show
    }

    fun setShowUrl(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_URL, show).apply()
        _showUrl.value = show
    }

    // Theme: preset id or "custom:<index>".
    private val _themeId = MutableStateFlow(prefs.getString(KEY_THEME, "quote404") ?: "quote404")
    val themeId: StateFlow<String> = _themeId.asStateFlow()

    fun setThemeId(id: String) {
        prefs.edit().putString(KEY_THEME, id).apply()
        _themeId.value = id
    }

    // DevXen theme background-art placement (Settings → Themes).
    private val _devXenBgMode = MutableStateFlow(
        DevXenBgMode.fromPref(prefs.getString(KEY_DEVXEN_BG_MODE, null))
    )
    val devXenBgMode: StateFlow<DevXenBgMode> = _devXenBgMode.asStateFlow()

    fun setDevXenBgMode(mode: DevXenBgMode) {
        prefs.edit().putString(KEY_DEVXEN_BG_MODE, mode.prefValue).apply()
        _devXenBgMode.value = mode
    }

    /**
     * v1.61.3: the user's own DevXen background picture. Stored as the
     * app-private file name produced by saveDbBackgroundImage ("" = none,
     * use the bundled DevXen artwork). Replaces the artwork everywhere
     * the DevXen background is drawn: behind lists, inside quote cards,
     * and the DevXen share-card style.
     */
    private val _devXenCustomBg = MutableStateFlow(
        prefs.getString(KEY_DEVXEN_CUSTOM_BG, "") ?: ""
    )
    val devXenCustomBg: StateFlow<String> = _devXenCustomBg.asStateFlow()

    fun setDevXenCustomBg(name: String) {
        prefs.edit().putString(KEY_DEVXEN_CUSTOM_BG, name).apply()
        _devXenCustomBg.value = name
    }

    /** Resolves the stored custom DevXen background file (may not exist). */
    fun devXenCustomBgFile(): java.io.File =
        dbBackgroundImageFile(_devXenCustomBg.value)

    // Database source-tag chip colors: dbId -> RRGGBB hex. Missing key =
    // default tag style (off). Stored as "dbId=RRGGBB;dbId2=RRGGBB".
    private val _dbTagColors =
        MutableStateFlow(loadDbTagColors())
    val dbTagColors: StateFlow<Map<String, String>> = _dbTagColors.asStateFlow()

    private fun loadDbTagColors(): Map<String, String> =
        prefs.getString(KEY_DB_TAG_COLORS, "").orEmpty()
            .split(";")
            .mapNotNull { entry ->
                val kv = entry.split("=", limit = 2)
                val id = kv.getOrNull(0)?.trim().orEmpty()
                val hex = kv.getOrNull(1)?.trim().orEmpty()
                if (id.isNotEmpty() && hex.matches(Regex("[0-9a-fA-F]{6}"))) id to hex.uppercase()
                else null
            }
            .toMap()

    fun setDbTagColor(dbId: String, hex: String?) {
        val map = _dbTagColors.value.toMutableMap()
        if (hex == null) map.remove(dbId)
        else map[dbId] = hex.trim().removePrefix("#").uppercase().take(6)
        _dbTagColors.value = map
        prefs.edit()
            .putString(KEY_DB_TAG_COLORS, map.entries.joinToString(";") { "${it.key}=${it.value}" })
            .apply()
    }

    // Quote text size multiplier (1.0 = default).
    private val _textScale = MutableStateFlow(prefs.getFloat(KEY_TEXT_SCALE, 1f))
    val textScale: StateFlow<Float> = _textScale.asStateFlow()

    fun setTextScale(scale: Float) {
        val clean = scale.coerceIn(0.8f, 1.5f)
        prefs.edit().putFloat(KEY_TEXT_SCALE, clean).apply()
        _textScale.value = clean
    }

    // List density: "comfortable" (default) or "compact".
    private val _density =
        MutableStateFlow(prefs.getString(KEY_DENSITY, "comfortable") ?: "comfortable")
    val density: StateFlow<String> = _density.asStateFlow()

    fun setDensity(d: String) {
        val clean = if (d == "compact") "compact" else "comfortable"
        prefs.edit().putString(KEY_DENSITY, clean).apply()
        _density.value = clean
    }

    // Shake-to-shuffle on the Random tab (default off; fresh installs
    // must opt in; existing users keep their stored value).
    private val _shakeToShuffle =
        MutableStateFlow(prefs.getBoolean(KEY_SHAKE_SHUFFLE, false))
    val shakeToShuffle: StateFlow<Boolean> = _shakeToShuffle.asStateFlow()

    fun setShakeToShuffle(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHAKE_SHUFFLE, enabled).apply()
        _shakeToShuffle.value = enabled
    }

    /** Hide quotes already marked seen from new random picks. */
    private val _hideSeen =
        MutableStateFlow(prefs.getBoolean(KEY_HIDE_SEEN, false))
    val hideSeen: StateFlow<Boolean> = _hideSeen.asStateFlow()

    fun setHideSeen(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIDE_SEEN, enabled).apply()
        _hideSeen.value = enabled
    }

    /**
     * Content filter: when enabled, quotes containing any word on the
     * user's offensive-words list are hidden from browse/search/random/
     * top/favorites/history/QotD/widget/export surfaces. Off by default.
     */
    private val _contentFilterEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_CONTENT_FILTER, false))
    val contentFilterEnabled: StateFlow<Boolean> = _contentFilterEnabled.asStateFlow()

    fun setContentFilterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONTENT_FILTER, enabled).apply()
        _contentFilterEnabled.value = enabled
    }

    /** The user's offensive-words list (lowercased, deduped). */
    private val _offensiveWords =
        MutableStateFlow(loadOffensiveWords())
    val offensiveWords: StateFlow<List<String>> = _offensiveWords.asStateFlow()

    /** Compiled word-boundary matchers, rebuilt whenever the list changes. */
    @Volatile
    private var offensiveMatchers: List<Regex> = buildOffensiveMatchers(_offensiveWords.value)

    private fun loadOffensiveWords(): List<String> {
        return try {
            val raw = prefs.getString(KEY_OFFENSIVE_WORDS, null) ?: return emptyList()
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                arr.optString(i, "").trim().lowercase().takeIf { it.isNotEmpty() }
            }.distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun buildOffensiveMatchers(words: List<String>): List<Regex> =
        words.map { w ->
            Regex("\\b${Regex.escape(w)}\\b", RegexOption.IGNORE_CASE)
        }

    private fun saveOffensiveWords(words: List<String>) {
        val arr = org.json.JSONArray()
        words.forEach { arr.put(it) }
        prefs.edit().putString(KEY_OFFENSIVE_WORDS, arr.toString()).apply()
        _offensiveWords.value = words
        offensiveMatchers = buildOffensiveMatchers(words)
    }

    /** Adds a word to the offensive-words list; false when blank/duplicate. */
    fun addOffensiveWord(word: String): Boolean {
        val clean = word.trim().lowercase()
        if (clean.isEmpty() || clean in _offensiveWords.value) return false
        saveOffensiveWords(_offensiveWords.value + clean)
        return true
    }

    /** Removes a word from the offensive-words list. */
    fun removeOffensiveWord(word: String) {
        saveOffensiveWords(_offensiveWords.value - word.trim().lowercase())
    }

    /**
     * Adds several words at once; returns how many were actually new.
     * Blank entries and words already on the list are skipped.
     */
    fun addOffensiveWords(words: Collection<String>): Int {
        val current = _offensiveWords.value
        val fresh = words.map { it.trim().lowercase() }
            .filter { it.isNotEmpty() && it !in current }
            .distinct()
        if (fresh.isEmpty()) return 0
        saveOffensiveWords(current + fresh)
        return fresh.size
    }

    /** Removes every word from the offensive-words list. */
    fun clearOffensiveWords() {
        saveOffensiveWords(emptyList())
    }

    /**
     * True when the content filter is on and [text] contains any listed
     * word (case-insensitive, whole-word match).
     */
    fun isHiddenByContentFilter(text: String): Boolean {
        if (!_contentFilterEnabled.value) return false
        val matchers = offensiveMatchers
        if (matchers.isEmpty()) return false
        return matchers.any { it.containsMatchIn(text) }
    }

    // ------------------------------------------------------------------
    // Muted nicks: every quote containing a muted nick is hidden at the
    // same enforcement points as "Never show again" — "Show anyway"
    // bypasses only the offensive-word filter, never nick mutes.
    // Declared before contentFilterTick because that flow combines it.
    // ------------------------------------------------------------------

    private val _mutedNicks = MutableStateFlow(loadMutedNicks())
    val mutedNicks: StateFlow<Set<String>> = _mutedNicks.asStateFlow()

    private fun loadMutedNicks(): Set<String> {
        return try {
            val raw = prefs.getString(KEY_MUTED_NICKS, null) ?: return emptySet()
            val arr = org.json.JSONArray(raw)
            (0 until arr.length())
                .mapTo(mutableSetOf()) { arr.getString(it).lowercase() }
        } catch (_: Exception) {
            emptySet()
        }
    }

    private fun saveMutedNicks(nicks: Set<String>) {
        val arr = org.json.JSONArray()
        nicks.sorted().forEach { arr.put(it) }
        prefs.edit().putString(KEY_MUTED_NICKS, arr.toString()).apply()
    }

    fun addMutedNick(nick: String): Boolean {
        val clean = nick.trim().lowercase()
        if (clean.isEmpty() || clean in _mutedNicks.value) return false
        val next = _mutedNicks.value + clean
        _mutedNicks.value = next
        saveMutedNicks(next)
        return true
    }

    fun removeMutedNick(nick: String) {
        val next = _mutedNicks.value - nick.lowercase()
        if (next == _mutedNicks.value) return
        _mutedNicks.value = next
        saveMutedNicks(next)
    }

    fun clearMutedNicks() {
        _mutedNicks.value = emptySet()
        prefs.edit().remove(KEY_MUTED_NICKS).apply()
    }

    /**
     * True when [text] contains any muted nick (case-insensitive), reusing
     * the same nick extraction as nick coloring.
     */
    fun isQuoteMutedNick(text: String): Boolean {
        val muted = _mutedNicks.value
        if (muted.isEmpty()) return false
        return com.quotebrowser.app.ui.extractNicks(text)
            .any { it.lowercase() in muted }
    }

    // ------------------------------------------------------------------
    // "Never show again": per-quote hiding, separate from the word filter.
    // Keys are "$dbId/$number", stored as JSON in prefs. Declared before
    // contentFilterTick because that flow combines it.
    // ------------------------------------------------------------------

    private val _hiddenQuoteKeys =
        kotlinx.coroutines.flow.MutableStateFlow(loadHiddenQuoteKeys())
    val hiddenQuoteKeys: kotlinx.coroutines.flow.StateFlow<Set<String>> =
        _hiddenQuoteKeys.asStateFlow()

    private fun loadHiddenQuoteKeys(): Set<String> {
        return try {
            val raw = prefs.getString(KEY_HIDDEN_QUOTES, null) ?: return emptySet()
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).mapTo(mutableSetOf()) { arr.getString(it) }
        } catch (e: Exception) {
            emptySet()
        }
    }

    /**
     * Ticks whenever the content filter or its word list changes, so quote
     * lists can rebuild without each screen combining two flows. Also ticks
     * when the user-hidden quote set changes (same "don't show" semantics).
     * Lazy because the flag flows are declared later in this class.
     */
    val contentFilterTick: kotlinx.coroutines.flow.Flow<Unit> by lazy {
        kotlinx.coroutines.flow.combine(
            contentFilterEnabled, offensiveWords, hiddenQuoteKeys, mutedNicks
        ) { _, _, _, _ -> }
            .combine(hideFlaggedQuotes) { _, _ -> }
            .combine(flaggedQuotes) { _, _ -> }
    }

    private fun saveHiddenQuoteKeys(keys: Set<String>) {
        val arr = org.json.JSONArray()
        keys.forEach { arr.put(it) }
        prefs.edit().putString(KEY_HIDDEN_QUOTES, arr.toString()).apply()
    }

    private fun hiddenQuoteKey(dbId: String, number: Int) = "$dbId/$number"

    /** True when the user hid this exact quote via "Never show again". */
    fun isQuoteHidden(dbId: String, number: Int): Boolean =
        hiddenQuoteKey(dbId, number) in _hiddenQuoteKeys.value

    /**
     * Combined "don't show" check used by every list: the word filter OR the
     * per-quote hide OR a muted nick. Explicit navigation (jump-to-number,
     * detail deep links) bypasses this.
     */
    fun isQuoteHiddenFromLists(dbId: String, number: Int, text: String): Boolean =
        isHiddenByContentFilter(text) || isQuoteHidden(dbId, number) ||
            isQuoteMutedNick(text) || isQuoteFlaggedHidden(dbId, number)

    /**
     * The non-bypassable part of the "don't show" check: "Never show again"
     * or a muted nick. "Show anyway" bypasses only the word filter, so
     * every screen's filter predicate should use this instead of
     * [isQuoteHidden] alone.
     */
    fun isQuoteHiddenOrMuted(dbId: String, number: Int, text: String): Boolean =
        isQuoteHidden(dbId, number) || isQuoteMutedNick(text)

    fun setQuoteHidden(dbId: String, number: Int, hidden: Boolean) {
        val key = hiddenQuoteKey(dbId, number)
        val cur = _hiddenQuoteKeys.value
        val next = if (hidden) cur + key else cur - key
        if (next == cur) return
        _hiddenQuoteKeys.value = next
        saveHiddenQuoteKeys(next)
    }

    fun clearHiddenQuotes() {
        if (_hiddenQuoteKeys.value.isEmpty()) return
        _hiddenQuoteKeys.value = emptySet()
        saveHiddenQuoteKeys(emptySet())
    }

    // ------------------------------------------------------------------
    // v1.53 report/flag: per-quote flags with a reason, stored on-device
    // only (these are dead archived sites - flags are never sent anywhere).
    // Keys are "$dbId/$number", stored as a JSON object in prefs, mirroring
    // the hidden-quotes pattern.
    // ------------------------------------------------------------------

    private val _flaggedQuotes =
        kotlinx.coroutines.flow.MutableStateFlow(loadFlaggedQuotes())
    val flaggedQuotes: kotlinx.coroutines.flow.StateFlow<Map<String, FlagInfo>> =
        _flaggedQuotes.asStateFlow()

    private fun loadFlaggedQuotes(): Map<String, FlagInfo> {
        return try {
            val raw = prefs.getString(KEY_FLAGGED_QUOTES, null) ?: return emptyMap()
            val obj = org.json.JSONObject(raw)
            val out = mutableMapOf<String, FlagInfo>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val e = obj.optJSONObject(k) ?: continue
                out[k] = FlagInfo(
                    reason = e.optString("reason", "Other"),
                    customText = e.optString("custom", ""),
                    flaggedAt = e.optLong("at", 0L)
                )
            }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveFlaggedQuotes(flags: Map<String, FlagInfo>) {
        val obj = org.json.JSONObject()
        flags.forEach { (k, v) ->
            obj.put(
                k, org.json.JSONObject()
                    .put("reason", v.reason)
                    .put("custom", v.customText)
                    .put("at", v.flaggedAt)
            )
        }
        prefs.edit().putString(KEY_FLAGGED_QUOTES, obj.toString()).apply()
    }

    private fun flaggedQuoteKey(dbId: String, number: Int) = "$dbId/$number"

    /** True when this exact quote currently carries a user flag. */
    fun isQuoteFlagged(dbId: String, number: Int): Boolean =
        flaggedQuoteKey(dbId, number) in _flaggedQuotes.value

    fun flagInfo(dbId: String, number: Int): FlagInfo? =
        _flaggedQuotes.value[flaggedQuoteKey(dbId, number)]

    fun flagQuote(dbId: String, number: Int, reason: String, customText: String) {
        val key = flaggedQuoteKey(dbId, number)
        val next = _flaggedQuotes.value + (key to FlagInfo(
            reason = reason,
            customText = customText,
            flaggedAt = System.currentTimeMillis()
        ))
        _flaggedQuotes.value = next
        saveFlaggedQuotes(next)
    }

    fun unflagQuote(dbId: String, number: Int) {
        val key = flaggedQuoteKey(dbId, number)
        val cur = _flaggedQuotes.value
        if (key !in cur) return
        val next = cur - key
        _flaggedQuotes.value = next
        saveFlaggedQuotes(next)
    }

    fun clearFlaggedQuotes() {
        if (_flaggedQuotes.value.isEmpty()) return
        _flaggedQuotes.value = emptyMap()
        saveFlaggedQuotes(emptyMap())
    }

    /** Flagged quotes with their details and reasons, for the review UI. */
    suspend fun flaggedQuoteDetails(): List<FlaggedSourcedQuote> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            _flaggedQuotes.value.mapNotNull { (key, info) ->
                val dbId = key.substringBeforeLast("/")
                val number = key.substringAfterLast("/").toIntOrNull() ?: return@mapNotNull null
                try {
                    val q = repoFor(dbId).getQuote(number) ?: return@mapNotNull null
                    FlaggedSourcedQuote(dbId, q, info.reason, info.customText, info.flaggedAt)
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.flaggedAt }
        }

    /** Hidden from lists when the "hide flagged quotes" toggle is on. */
    private fun isQuoteFlaggedHidden(dbId: String, number: Int): Boolean =
        _hideFlaggedQuotes.value && isQuoteFlagged(dbId, number)

    /** Hidden quotes with their details, for the management UI. */
    suspend fun hiddenQuoteDetails(): List<com.quotebrowser.app.data.SourcedQuote> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            _hiddenQuoteKeys.value.mapNotNull { key ->
                val dbId = key.substringBeforeLast("/")
                val number = key.substringAfterLast("/").toIntOrNull() ?: return@mapNotNull null
                try {
                    val q = repoFor(dbId).getQuote(number) ?: return@mapNotNull null
                    com.quotebrowser.app.data.SourcedQuote(dbId, q)
                } catch (e: Exception) {
                    null
                }
            }.sortedWith(compareBy({ it.dbId }, { it.quote.number }))
        }

    // ------------------------------------------------------------------
    // "On this day": quotes favorited or read on this month/day in prior
    // years, resurfaced as a little time capsule.
    // ------------------------------------------------------------------

    data class MemoryLaneEntry(
        val dbId: String,
        val quote: com.quotebrowser.app.data.Quote,
        val year: Int,
        /** "favorited" or "read". */
        val kind: String
    )

    /** Up to [limit] quotes favorited/read on this date in previous years. */
    suspend fun onThisDay(limit: Int = 12): List<MemoryLaneEntry> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val now = java.time.LocalDate.now()
                val monthDay = "%02d-%02d".format(now.monthValue, now.dayOfMonth)
                val yearStr = now.year.toString()
                val zone = java.time.ZoneId.systemDefault()
                fun yearOf(millis: Long): Int =
                    java.time.Instant.ofEpochMilli(millis).atZone(zone).year
                val seen = mutableSetOf<Pair<String, Int>>()
                val out = mutableListOf<MemoryLaneEntry>()
                val favs = metaDb.metaDao().favoritesOnThisDay(monthDay, yearStr)
                for (f in favs) {
                    if (out.size >= limit) break
                    if (!seen.add(f.dbId to f.number)) continue
                    val q = try {
                        repoFor(f.dbId).peekQuote(f.number)
                    } catch (e: Exception) {
                        null
                    } ?: continue
                    if (isQuoteHidden(f.dbId, f.number) ||
                        isQuoteMutedNick(q.text) ||
                        isHiddenByContentFilter(q.text)
                    ) continue
                    out.add(MemoryLaneEntry(f.dbId, q, yearOf(f.addedAt), "favorited"))
                }
                if (out.size < limit) {
                    val hist = metaDb.metaDao().historyOnThisDay(monthDay, yearStr)
                    for (h in hist) {
                        if (out.size >= limit) break
                        if (!seen.add(h.dbId to h.number)) continue
                        val q = try {
                            repoFor(h.dbId).peekQuote(h.number)
                        } catch (e: Exception) {
                            null
                        } ?: continue
                        if (isQuoteHidden(h.dbId, h.number) ||
                            isQuoteMutedNick(q.text) ||
                            isHiddenByContentFilter(q.text)
                        ) continue
                        out.add(MemoryLaneEntry(h.dbId, q, yearOf(h.viewedAt), "read"))
                    }
                }
                out
            } catch (e: Exception) {
                emptyList()
            }
        }

    // ------------------------------------------------------------------
    // v1.88.2 "On this day" (archive edition), rewritten for honesty:
    // "On this day" now means quotes genuinely POSTED on today's
    // month/day in past years — never Wayback capture dates, and never
    // deterministic "deep cuts" randoms dressed up as anniversaries.
    //
    // Genuine per-record publication dates exist for exactly one bundled
    // database: bash.im, whose corpus ships real posting timestamps (the
    // `bashim_posted_dates.txt` sidecar asset, "<id> <YYYY-MM-DD>" per
    // line). No other bundled source has trustworthy record-level dates,
    // so nothing is invented for them.
    //
    // qdb.us keeps its own honestly-labeled section: the Wayback capture
    // dates from `qdb_us_served.json` say when a quote was ARCHIVED, not
    // when it was posted, and are presented as exactly that.
    // ------------------------------------------------------------------

    private var bashImPostedCache: Map<Int, String>? = null

    /**
     * Genuine per-quote publication dates for bash.im ("<id> -> YYYY-MM-DD"),
     * from the corpus's own posting timestamps. Cached after first load.
     */
    fun bashImPostedDates(): Map<Int, String> {
        bashImPostedCache?.let { return it }
        val map = mutableMapOf<Int, String>()
        try {
            appContext.assets.open("bashim_posted_dates.txt").use { ins ->
                ins.bufferedReader().forEachLine { raw ->
                    val parts = raw.trim().split(' ', '\t')
                    if (parts.size == 2) {
                        val id = parts[0].toIntOrNull()
                        val d = parts[1]
                        if (id != null && d.length == 10 && d[4] == '-' && d[7] == '-') {
                            map[id] = d
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // No sidecar: no genuine dates available.
        }
        bashImPostedCache = map
        return map
    }

    /** A quote genuinely posted on today's month/day in a past year. */
    data class PostedDayQuote(
        val dbId: String,
        val quote: com.quotebrowser.app.data.Quote,
        /** "YYYY-MM-DD" from the source corpus's own timestamps. */
        val postedDate: String
    )

    /**
     * Up to [limit] quotes genuinely posted on today's month/day in past
     * years (bash.im — the only bundled database with trustworthy
     * record-level publication dates). Deterministic order per day.
     */
    suspend fun postedOnThisDay(limit: Int = 12): List<PostedDayQuote> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val out = mutableListOf<PostedDayQuote>()
            try {
                val now = java.time.LocalDate.now()
                val mmdd = "%02d-%02d".format(now.monthValue, now.dayOfMonth)
                val seed = now.toEpochDay()
                val dbId = BASH_IM_ASSET_ID
                val dates = bashImPostedDates()
                if (dates.isEmpty()) return@withContext out
                val repo = try {
                    repoFor(dbId)
                } catch (e: Exception) {
                    return@withContext out
                }
                val numbers = dates
                    .filterValues { it.length == 10 && it.substring(5) == mmdd }
                    .keys.shuffled(java.util.Random(seed))
                for (n in numbers) {
                    if (out.size >= limit) break
                    val posted = dates[n] ?: continue
                    // Past years only: posted-today-this-year is just "new".
                    if (posted.substring(0, 4) == now.year.toString()) continue
                    val q = try {
                        repo.getQuote(n)
                    } catch (e: Exception) {
                        null
                    } ?: continue
                    if (isQuoteHidden(dbId, n) || isQuoteMutedNick(q.text) ||
                        isHiddenByContentFilter(q.text) || !isMaturityAllowed(q)
                    ) continue
                    out.add(PostedDayQuote(dbId, q, posted))
                }
            } catch (e: Exception) {
                // Date APIs unavailable: return whatever we have.
            }
            out
        }

    private var qdbUsServedCache: Map<String, String>? = null

    /**
     * quote number -> capture date for qdb.us (sidecar asset). Values are
     * "YYYYMMDD"; legacy "MMDD"-only sidecars are still accepted.
     */
    fun qdbUsServedMap(): Map<String, String> {
        qdbUsServedCache?.let { return it }
        val map = mutableMapOf<String, String>()
        try {
            appContext.assets.open("qdb_us_served.json").use { ins ->
                val obj = JSONObject(ins.bufferedReader().readText())
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = obj.optString(k, "")
                    if (v.length == 8 || v.length == 4) map[k] = v
                }
            }
        } catch (e: Exception) {
            // No sidecar: the deep-cuts fallback below still works.
        }
        qdbUsServedCache = map
        return map
    }

    /**
     * Up to [limit] qdb.us quotes the Wayback Machine captured on today's
     * month/day. These are ARCHIVE dates — when the quote was captured,
     * not when it was originally posted — and the UI labels them as such.
     */
    suspend fun capturedOnThisDay(limit: Int = 12): List<com.quotebrowser.app.data.SourcedQuote> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val out = mutableListOf<com.quotebrowser.app.data.SourcedQuote>()
            val seen = mutableSetOf<Pair<String, Int>>()
            try {
                val now = java.time.LocalDate.now()
                val mmdd = "%02d%02d".format(now.monthValue, now.dayOfMonth)
                val seed = now.toEpochDay()
                val qdbId = QDB_US_ASSET_ID
                val repo = repoFor(qdbId)
                val numbers = qdbUsServedMap()
                    // Values are "YYYYMMDD" (legacy "MMDD" accepted);
                    // match on the month/day suffix.
                    .filterValues { it.takeLast(4) == mmdd }.keys
                    .mapNotNull { it.toIntOrNull() }
                    .shuffled(java.util.Random(seed))
                    .take(limit)
                for (n in numbers) {
                    if (out.size >= limit) break
                    if (!seen.add(qdbId to n)) continue
                    val q = repo.getQuote(n) ?: continue
                    if (isQuoteHidden(qdbId, n) || isQuoteMutedNick(q.text) ||
                        isHiddenByContentFilter(q.text) || !isMaturityAllowed(q)
                    ) continue
                    out.add(com.quotebrowser.app.data.SourcedQuote(qdbId, q))
                }
            } catch (e: Exception) {
                // Sidecar missing or DB unavailable.
            }
            out
        }

    /**
     * Approved/pending quote counts for qdb.us, for the site footer's real
     * counts ("N quotes approved; M quotes pending").
     */
    suspend fun qdbUsCounts(): Pair<Int, Int> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val repo = repoFor(QDB_US_ASSET_ID)
                repo.countByStatus("approved") to repo.countByStatus("queued")
            } catch (e: Exception) {
                0 to 0
            }
        }

    // ------------------------------------------------------------------
    // Personal notes on quotes: free text per "$dbId/$number", stored as
    // JSON in prefs (no schema migration needed).
    // ------------------------------------------------------------------

    private fun noteKey(dbId: String, number: Int) = "$dbId/$number"

    private fun loadNotes(): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val obj = JSONObject(prefs.getString(KEY_NOTES, "{}") ?: "{}")
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.optString(k, "")
                if (v.isNotBlank()) map[k] = v
            }
        } catch (e: Exception) {
            // Corrupt JSON: start fresh.
        }
        return map
    }

    private val _notes = MutableStateFlow(loadNotes())
    val notes: StateFlow<Map<String, String>> = _notes.asStateFlow()

    /** The personal note on one quote, or null. */
    fun noteFor(dbId: String, number: Int): String? = _notes.value[noteKey(dbId, number)]

    /** Sets a note (blank text deletes it). */
    fun setNote(dbId: String, number: Int, text: String) {
        val next = _notes.value.toMutableMap()
        val clean = text.trim()
        if (clean.isEmpty()) next.remove(noteKey(dbId, number))
        else next[noteKey(dbId, number)] = clean
        prefs.edit().putString(KEY_NOTES, JSONObject(next as Map<*, *>).toString()).apply()
        _notes.value = next
    }

    // ------------------------------------------------------------------
    // Flashcard spaced repetition (SM-2): per-quote ease, interval, reps,
    // and due date. Cards whose due date hasn't arrived are excluded from
    // the pool; grading a card reschedules it. Migrates the old plain
    // "memorized" set into graduated cards.
    // ------------------------------------------------------------------

    data class SrsCard(
        val ease: Double = 2.5,
        val intervalDays: Int = 0,
        val reps: Int = 0,
        val dueAt: Long = 0L
    )

    private fun readSrsCards(): MutableMap<String, SrsCard> {
        val out = mutableMapOf<String, SrsCard>()
        try {
            val json = prefs.getString(KEY_SRS_CARDS, null)
            if (json != null) {
                val obj = org.json.JSONObject(json)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val c = obj.optJSONObject(k) ?: continue
                    out[k] = SrsCard(
                        ease = c.optDouble("e", 2.5),
                        intervalDays = c.optInt("i", 0),
                        reps = c.optInt("r", 0),
                        dueAt = c.optLong("d", 0L)
                    )
                }
                return out
            }
        } catch (e: Exception) {
        }
        // One-time migration from the old "memorized" set: graduated cards
        // due again in 30 days.
        val legacy = prefs.getStringSet(KEY_MEMORIZED_FLASHCARDS, emptySet())
        if (!legacy.isNullOrEmpty()) {
            val due = System.currentTimeMillis() + 30L * 86_400_000L
            for (k in legacy) out[k] = SrsCard(2.5, 30, 3, due)
            saveSrsCards(out)
            prefs.edit().remove(KEY_MEMORIZED_FLASHCARDS).apply()
        }
        return out
    }

    private fun saveSrsCards(cards: Map<String, SrsCard>) {
        val obj = org.json.JSONObject()
        for ((k, c) in cards) {
            obj.put(
                k, org.json.JSONObject()
                    .put("e", c.ease)
                    .put("i", c.intervalDays)
                    .put("r", c.reps)
                    .put("d", c.dueAt)
            )
        }
        prefs.edit().putString(KEY_SRS_CARDS, obj.toString()).apply()
    }

    private val _srsCards = MutableStateFlow(readSrsCards().toMap())
    val srsCards: StateFlow<Map<String, SrsCard>> = _srsCards.asStateFlow()

    /**
     * Grades a flashcard with SM-2. [quality] 0-5: 0 = Again, 3 = Hard,
     * 4 = Good, 5 = Easy.
     */
    fun gradeFlashcard(dbId: String, number: Int, quality: Int) {
        val q = quality.coerceIn(0, 5)
        val k = noteKey(dbId, number)
        val cards = readSrsCards()
        val prev = cards[k]
        val (ease, interval, reps) = if (q < 3) {
            Triple(2.5, 1, 0)
        } else {
            val p = prev ?: SrsCard()
            val e = (p.ease + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
                .coerceAtLeast(1.3)
            val r = p.reps + 1
            val i = when (r) {
                1 -> 1
                2 -> 6
                else -> (p.intervalDays * e).roundToInt().coerceAtLeast(1)
            }
            Triple(e, i, r)
        }
        val dueAt = System.currentTimeMillis() + interval * 86_400_000L
        cards[k] = SrsCard(ease, interval, reps, dueAt)
        saveSrsCards(cards)
        publishSrs(cards)
    }

    /** Legacy shim: old "memorized" semantics = graduated (Easy) in SM-2. */
    fun setFlashcardMemorized(dbId: String, number: Int, memorized: Boolean) {
        if (memorized) gradeFlashcard(dbId, number, 5)
        else {
            val k = noteKey(dbId, number)
            val cards = readSrsCards()
            cards.remove(k)
            saveSrsCards(cards)
            publishSrs(cards)
        }
    }

    fun clearMemorizedFlashcards() {
        prefs.edit().remove(KEY_SRS_CARDS).apply()
        publishSrs(emptyMap())
    }

    /** Kept for the UI: the set of keys with any SRS record. */
    private val _srsKeys = MutableStateFlow(readSrsCards().keys.toSet())
    val memorizedFlashcards: StateFlow<Set<String>> = _srsKeys.asStateFlow()

    private fun publishSrs(cards: Map<String, SrsCard>) {
        _srsCards.value = cards.toMap()
        _srsKeys.value = cards.keys.toSet()
    }

    // ------------------------------------------------------------------
    // Scheduled quote pings: a random-quote notification on a user-chosen
    // interval (WorkManager periodic work).
    // ------------------------------------------------------------------

    private val _quotePingEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_QUOTE_PING_ENABLED, false))
    val quotePingEnabled: StateFlow<Boolean> = _quotePingEnabled.asStateFlow()

    private val _quotePingMinutes =
        MutableStateFlow(prefs.getLong(KEY_QUOTE_PING_MINUTES, 60L))
    val quotePingMinutes: StateFlow<Long> = _quotePingMinutes.asStateFlow()

    /** Ping frequency choices offered in Settings, in minutes. */
    val quotePingChoices = listOf(30L, 60L, 180L, 360L, 720L, 1440L, 10080L)

    fun quotePingLabel(minutes: Long): String = when (minutes) {
        30L -> "Every 30 minutes"
        60L -> "Every hour"
        180L -> "Every 3 hours"
        360L -> "Every 6 hours"
        720L -> "Every 12 hours"
        1440L -> "Daily"
        10080L -> "Weekly"
        else -> "Every $minutes minutes"
    }

    /** Delivery time for the Daily/Weekly ping (default 9:00am). */
    private val _quotePingHour =
        MutableStateFlow(prefs.getInt(KEY_QUOTE_PING_HOUR, 9))
    val quotePingHour: StateFlow<Int> = _quotePingHour.asStateFlow()

    private val _quotePingMinute =
        MutableStateFlow(prefs.getInt(KEY_QUOTE_PING_MINUTE, 0))
    val quotePingMinute: StateFlow<Int> = _quotePingMinute.asStateFlow()

    fun setQuotePingTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        prefs.edit().putInt(KEY_QUOTE_PING_HOUR, h).putInt(KEY_QUOTE_PING_MINUTE, m).apply()
        _quotePingHour.value = h
        _quotePingMinute.value = m
        if (_quotePingEnabled.value) scheduleQuotePingWork()
    }

    /** Day of week for the Weekly ping (1 = Monday .. 7 = Sunday). */
    private val _quotePingDay =
        MutableStateFlow(prefs.getInt(KEY_QUOTE_PING_DAY, 7).coerceIn(1, 7))
    val quotePingDay: StateFlow<Int> = _quotePingDay.asStateFlow()

    fun setQuotePingDay(day: Int) {
        val d = day.coerceIn(1, 7)
        prefs.edit().putInt(KEY_QUOTE_PING_DAY, d).apply()
        _quotePingDay.value = d
        if (_quotePingEnabled.value) scheduleQuotePingWork()
    }

    /**
     * Databases pings pull from: null = all databases, otherwise the
     * explicit set of database ids.
     */
    private val _quotePingDbIds =
        MutableStateFlow(
            prefs.getString(KEY_QUOTE_PING_DBS, null)
                ?.split(",")?.filter { it.isNotBlank() }?.toSet()?.takeIf { it.isNotEmpty() }
        )
    val quotePingDbIds: StateFlow<Set<String>?> = _quotePingDbIds.asStateFlow()

    fun setQuotePingDbIds(ids: Set<String>?) {
        val clean = ids?.filter { it.isNotBlank() }?.toSet()?.takeIf { it.isNotEmpty() }
        if (clean == null) prefs.edit().remove(KEY_QUOTE_PING_DBS).apply()
        else prefs.edit().putString(KEY_QUOTE_PING_DBS, clean.joinToString(",")).apply()
        _quotePingDbIds.value = clean
    }

    fun setQuotePingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUOTE_PING_ENABLED, enabled).apply()
        _quotePingEnabled.value = enabled
        if (enabled) scheduleQuotePingWork() else cancelQuotePingWork()
    }

    fun setQuotePingMinutes(minutes: Long) {
        val m = (quotePingChoices.firstOrNull { it == minutes } ?: 60L)
        prefs.edit().putLong(KEY_QUOTE_PING_MINUTES, m).apply()
        _quotePingMinutes.value = m
        if (_quotePingEnabled.value) scheduleQuotePingWork()
    }

    // ------------------------------------------------------------------
    // v1.74: notification-quote filters. Read at fire time by the shared
    // ping picker (worker/QuotePingFilter.kt), so changing them needs no
    // work reschedule.
    // ------------------------------------------------------------------

    /**
     * Maturity ceiling for pings: true (default) = follow the app's global
     * ceiling; false = use [quotePingCustomCeiling].
     */
    private val _quotePingUseGlobalCeiling =
        MutableStateFlow(prefs.getBoolean(KEY_QUOTE_PING_USE_GLOBAL_CEILING, true))
    val quotePingUseGlobalCeiling: StateFlow<Boolean> =
        _quotePingUseGlobalCeiling.asStateFlow()

    fun setQuotePingUseGlobalCeiling(useGlobal: Boolean) {
        prefs.edit().putBoolean(KEY_QUOTE_PING_USE_GLOBAL_CEILING, useGlobal).apply()
        _quotePingUseGlobalCeiling.value = useGlobal
    }

    /** Notification-specific maturity ceiling (used when the global one isn't inherited). */
    private val _quotePingCustomCeiling =
        MutableStateFlow(
            MaturityLevel.fromStored(
                prefs.getString(KEY_QUOTE_PING_CUSTOM_CEILING, null) ?: "EVERYTHING"
            )
        )
    val quotePingCustomCeiling: StateFlow<MaturityLevel> =
        _quotePingCustomCeiling.asStateFlow()

    fun setQuotePingCustomCeiling(level: MaturityLevel) {
        prefs.edit().putString(KEY_QUOTE_PING_CUSTOM_CEILING, level.name).apply()
        _quotePingCustomCeiling.value = level
    }

    /** Work-hours mode: during work hours pings use [quotePingWorkCeiling] instead. */
    private val _quotePingWorkHoursEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_QUOTE_PING_WORK_HOURS, false))
    val quotePingWorkHoursEnabled: StateFlow<Boolean> =
        _quotePingWorkHoursEnabled.asStateFlow()

    fun setQuotePingWorkHoursEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUOTE_PING_WORK_HOURS, enabled).apply()
        _quotePingWorkHoursEnabled.value = enabled
    }

    /**
     * Workdays as java.time DayOfWeek values (1 = Monday .. 7 = Sunday).
     * Default Monday–Friday.
     */
    private val _quotePingWorkDays =
        MutableStateFlow(
            prefs.getString(KEY_QUOTE_PING_WORK_DAYS, null)
                ?.split(",")?.mapNotNull { it.toIntOrNull() }
                ?.filter { it in 1..7 }?.toSet()
                ?.takeIf { it.isNotEmpty() } ?: setOf(1, 2, 3, 4, 5)
        )
    val quotePingWorkDays: StateFlow<Set<Int>> = _quotePingWorkDays.asStateFlow()

    fun toggleQuotePingWorkDay(day: Int) {
        val d = day.coerceIn(1, 7)
        val next = _quotePingWorkDays.value.toMutableSet()
        if (d in next) next.remove(d) else next.add(d)
        prefs.edit().putString(KEY_QUOTE_PING_WORK_DAYS, next.sorted().joinToString(",")).apply()
        _quotePingWorkDays.value = next
    }

    /** Workday start/end as minutes since midnight (default 9:00–17:00). */
    private val _quotePingWorkStartMin =
        MutableStateFlow(prefs.getInt(KEY_QUOTE_PING_WORK_START, 9 * 60).coerceIn(0, 1439))
    val quotePingWorkStartMin: StateFlow<Int> = _quotePingWorkStartMin.asStateFlow()

    private val _quotePingWorkEndMin =
        MutableStateFlow(prefs.getInt(KEY_QUOTE_PING_WORK_END, 17 * 60).coerceIn(0, 1439))
    val quotePingWorkEndMin: StateFlow<Int> = _quotePingWorkEndMin.asStateFlow()

    fun setQuotePingWorkStart(hour: Int, minute: Int) {
        val m = (hour.coerceIn(0, 23) * 60 + minute.coerceIn(0, 59)).coerceIn(0, 1439)
        prefs.edit().putInt(KEY_QUOTE_PING_WORK_START, m).apply()
        _quotePingWorkStartMin.value = m
    }

    fun setQuotePingWorkEnd(hour: Int, minute: Int) {
        val m = (hour.coerceIn(0, 23) * 60 + minute.coerceIn(0, 59)).coerceIn(0, 1439)
        prefs.edit().putInt(KEY_QUOTE_PING_WORK_END, m).apply()
        _quotePingWorkEndMin.value = m
    }

    /**
     * Maturity ceiling applied during work hours. Defaults to Safe for
     * Work (PG-13 range, Spicy screened out).
     */
    private val _quotePingWorkCeiling =
        MutableStateFlow(
            MaturityLevel.fromStored(
                prefs.getString(KEY_QUOTE_PING_WORK_CEILING, null) ?: "SAFE_FOR_WORK"
            )
        )
    val quotePingWorkCeiling: StateFlow<MaturityLevel> =
        _quotePingWorkCeiling.asStateFlow()

    fun setQuotePingWorkCeiling(level: MaturityLevel) {
        prefs.edit().putString(KEY_QUOTE_PING_WORK_CEILING, level.name).apply()
        _quotePingWorkCeiling.value = level
    }

    /** Minimum rating for pings (null = no minimum). Stored as -1 when unset. */
    private val _quotePingMinRating =
        MutableStateFlow(
            prefs.getInt(KEY_QUOTE_PING_MIN_RATING, -1).takeIf { it >= 0 }
        )
    val quotePingMinRating: StateFlow<Int?> = _quotePingMinRating.asStateFlow()

    fun setQuotePingMinRating(minRating: Int?) {
        val m = minRating?.coerceAtLeast(0)
        if (m == null) prefs.edit().remove(KEY_QUOTE_PING_MIN_RATING).apply()
        else prefs.edit().putInt(KEY_QUOTE_PING_MIN_RATING, m).apply()
        _quotePingMinRating.value = m
    }

    /**
     * Category names pings are restricted to (empty = all categories).
     * Names are matched case-insensitively across every ping database,
     * the same way the mood presets match.
     */
    private val _quotePingCategoryNames =
        MutableStateFlow(loadQuotePingCategoryNames())
    val quotePingCategoryNames: StateFlow<Set<String>> =
        _quotePingCategoryNames.asStateFlow()

    private fun loadQuotePingCategoryNames(): Set<String> {
        return try {
            val raw = prefs.getString(KEY_QUOTE_PING_CATEGORIES, null) ?: return emptySet()
            val arr = org.json.JSONArray(raw)
            (0 until arr.length())
                .mapTo(mutableSetOf()) { arr.getString(it) }
                .filter { it.isNotBlank() }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    fun setQuotePingCategoryNames(names: Set<String>) {
        val clean = names.map { it.trim() }.filter { it.isNotBlank() }.toSet()
        if (clean.isEmpty()) prefs.edit().remove(KEY_QUOTE_PING_CATEGORIES).apply()
        else {
            val arr = org.json.JSONArray()
            clean.sorted().forEach { arr.put(it) }
            prefs.edit().putString(KEY_QUOTE_PING_CATEGORIES, arr.toString()).apply()
        }
        _quotePingCategoryNames.value = clean
    }

    /**
     * Don't repeat a quote notified within this many days (0 = repeats
     * allowed). Default 30.
     */
    val quotePingRecencyChoices = listOf(0, 7, 14, 30, 90)

    fun quotePingRecencyLabel(days: Int): String = when (days) {
        0 -> "Off"
        1 -> "1 day"
        else -> "$days days"
    }

    private val _quotePingRecencyDays =
        MutableStateFlow(
            prefs.getInt(KEY_QUOTE_PING_RECENCY_DAYS, 30).coerceIn(0, 3650)
        )
    val quotePingRecencyDays: StateFlow<Int> = _quotePingRecencyDays.asStateFlow()

    fun setQuotePingRecencyDays(days: Int) {
        val d = (quotePingRecencyChoices.firstOrNull { it == days } ?: 30)
        prefs.edit().putInt(KEY_QUOTE_PING_RECENCY_DAYS, d).apply()
        _quotePingRecencyDays.value = d
    }

    /**
     * True when [now] falls inside the user's work hours (selected
     * workdays plus the start/end window). Overnight shifts (end <=
     * start, e.g. 22:00–06:00) wrap past midnight; start == end means a
     * 24-hour shift.
     */
    fun isWorkTime(now: java.time.LocalDateTime = java.time.LocalDateTime.now()): Boolean {
        val mins = now.hour * 60 + now.minute
        val start = _quotePingWorkStartMin.value
        val end = _quotePingWorkEndMin.value
        if (start == end) return now.dayOfWeek.value in _quotePingWorkDays.value
        return if (end > start) {
            // Daytime window: today's workday selection and today's hours.
            now.dayOfWeek.value in _quotePingWorkDays.value && mins in start until end
        } else {
            // Overnight window (e.g. 22:00–06:00): the after-midnight
            // part belongs to the previous day's shift, so Tuesday at
            // 02:00 checks Monday's workday selection.
            val shiftDay = if (mins < end) now.minusDays(1).dayOfWeek.value
            else now.dayOfWeek.value
            shiftDay in _quotePingWorkDays.value && (mins >= start || mins < end)
        }
    }

    /**
     * The maturity ceiling a ping fired at [now] must honor: the
     * work-hours ceiling while at work, otherwise the notification
     * ceiling (the app's global ceiling when inherited, the custom one
     * when not).
     */
    fun effectivePingCeiling(
        now: java.time.LocalDateTime = java.time.LocalDateTime.now()
    ): MaturityLevel {
        if (_quotePingWorkHoursEnabled.value && isWorkTime(now)) {
            return _quotePingWorkCeiling.value
        }
        return if (_quotePingUseGlobalCeiling.value) _maturityCeiling.value
        else _quotePingCustomCeiling.value
    }

    // ------------------------------------------------------------------
    // Ping history: quotes already shown as notifications, so recent ones
    // aren't repeated. Stored as a JSON object {"dbId/number": epochMs},
    // pruned to the longest offered window plus margin.
    // ------------------------------------------------------------------

    private fun loadPingHistory(): Map<String, Long> {
        return try {
            val raw = prefs.getString(KEY_QUOTE_PING_HISTORY, null) ?: return emptyMap()
            val obj = org.json.JSONObject(raw)
            val out = mutableMapOf<String, Long>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                out[k] = obj.optLong(k, 0L)
            }
            out
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun savePingHistory(entries: Map<String, Long>) {
        val obj = org.json.JSONObject()
        entries.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(KEY_QUOTE_PING_HISTORY, obj.toString()).apply()
    }

    /** Records a quote as shown in a ping notification (initial or Next). */
    fun recordPingNotified(dbId: String, number: Int) {
        val now = System.currentTimeMillis()
        val pruned = loadPingHistory()
            .filterValues { it >= now - 120L * 86_400_000L }
            .toMutableMap()
        pruned["$dbId/$number"] = now
        savePingHistory(pruned)
    }

    /**
     * Quote numbers in [dbId] notified at or after [cutoffMs]. Used by
     * the shared ping picker to exclude recent repeats.
     */
    fun recentPingNumbers(dbId: String, cutoffMs: Long): Set<Int> {
        if (cutoffMs <= 0L) return emptySet()
        val prefix = "$dbId/"
        return loadPingHistory()
            .filter { (k, v) -> v >= cutoffMs && k.startsWith(prefix) }
            .mapNotNullTo(mutableSetOf()) { (k, _) ->
                k.removePrefix(prefix).toIntOrNull()
            }
    }

    /**
     * v1.74: union of category names across the given databases, for the
     * ping category picker. Names keep their stored casing; matching is
     * case-insensitive.
     */
    suspend fun pingCategoryUniverse(dbIds: Collection<String>): List<String> =
        withContext(Dispatchers.IO) {
            val names = mutableSetOf<String>()
            for (id in dbIds) {
                try {
                    repoFor(id).listCategories().forEach { names.add(it.name) }
                } catch (_: Exception) {
                }
            }
            names.sorted()
        }

    private fun scheduleQuotePingWork() {
        val minutes = _quotePingMinutes.value.coerceAtLeast(15L)
        val builder = androidx.work.PeriodicWorkRequestBuilder<com.quotebrowser.app.worker.QuotePingWorker>(
            minutes, java.util.concurrent.TimeUnit.MINUTES
        ).addTag(QUOTE_PING_WORK)
        // Daily/Weekly pings land on the user's chosen time (and weekday);
        // shorter intervals just start from now.
        if (minutes >= 1440L) {
            builder.setInitialDelay(
                pingInitialDelayMs(weekly = minutes >= 10080L),
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
        }
        androidx.work.WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(
                QUOTE_PING_WORK,
                androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
                builder.build()
            )
    }

    /**
     * Milliseconds until the next Daily (or Weekly, on the chosen weekday)
     * ping time.
     */
    private fun pingInitialDelayMs(weekly: Boolean): Long {
        val now = LocalDateTime.now()
        val targetDay = DayOfWeek.of(_quotePingDay.value)
        var date = now.toLocalDate()
        var next = date.atTime(_quotePingHour.value, _quotePingMinute.value)
        while ((weekly && next.dayOfWeek != targetDay) || !next.isAfter(now)) {
            date = date.plusDays(1)
            next = date.atTime(_quotePingHour.value, _quotePingMinute.value)
        }
        return Duration.between(now, next).toMillis().coerceAtLeast(0L)
    }

    private fun cancelQuotePingWork() {
        androidx.work.WorkManager.getInstance(appContext).cancelUniqueWork(QUOTE_PING_WORK)
    }

    /** Re-arms the ping schedule after reboot/update when enabled. */
    fun ensureQuotePingWorker() {
        if (_quotePingEnabled.value) scheduleQuotePingWork()
    }

    // ------------------------------------------------------------------
    // Configurable quote-row swipe actions (left/right).
    // ------------------------------------------------------------------

    private val _swipeLeft =
        MutableStateFlow(
            com.quotebrowser.app.ui.components.SwipeAction.fromId(
                prefs.getString(KEY_SWIPE_LEFT, "none")
            )
        )
    val swipeLeft: StateFlow<com.quotebrowser.app.ui.components.SwipeAction> =
        _swipeLeft.asStateFlow()

    private val _swipeRight =
        MutableStateFlow(
            com.quotebrowser.app.ui.components.SwipeAction.fromId(
                prefs.getString(KEY_SWIPE_RIGHT, "none")
            )
        )
    val swipeRight: StateFlow<com.quotebrowser.app.ui.components.SwipeAction> =
        _swipeRight.asStateFlow()

    fun setSwipeAction(left: Boolean, action: com.quotebrowser.app.ui.components.SwipeAction) {
        prefs.edit().putString(if (left) KEY_SWIPE_LEFT else KEY_SWIPE_RIGHT, action.id).apply()
        if (left) _swipeLeft.value = action else _swipeRight.value = action
    }

    // Row swipe actions are off by default; each can be enabled separately
    // for Home (Browse) and Random.
    private val _swipeHomeEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_SWIPE_HOME, false))
    val swipeHomeEnabled: StateFlow<Boolean> = _swipeHomeEnabled.asStateFlow()

    private val _swipeRandomEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_SWIPE_RANDOM, false))
    val swipeRandomEnabled: StateFlow<Boolean> = _swipeRandomEnabled.asStateFlow()

    fun setSwipeHomeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SWIPE_HOME, enabled).apply()
        _swipeHomeEnabled.value = enabled
    }

    fun setSwipeRandomEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SWIPE_RANDOM, enabled).apply()
        _swipeRandomEnabled.value = enabled
    }

    // ------------------------------------------------------------------
    // v1.50 vacation mode (reading streak freeze). The master feature
    // toggle (default ON) gates the settings UI; the actual freeze only
    // applies while _vacationEnabled is true and the window covers the day.
    // Days are LocalDate.toEpochDay values; end -1 means "until I turn it
    // off". See readingStreakDays()/longestReadingStreakDays().
    // ------------------------------------------------------------------

    /** Feature toggle: show the Vacation mode settings section (default ON). */
    private val _vacationMode =
        MutableStateFlow(prefs.getBoolean(KEY_VACATION_MODE, true))
    val vacationMode: StateFlow<Boolean> = _vacationMode.asStateFlow()

    fun setVacationMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VACATION_MODE, enabled).apply()
        _vacationMode.value = enabled
    }

    /** Vacation mode is switched on (the streak freeze is live). */
    private val _vacationEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_VACATION_ENABLED, false))
    val vacationEnabled: StateFlow<Boolean> = _vacationEnabled.asStateFlow()

    fun setVacationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VACATION_ENABLED, enabled).apply()
        _vacationEnabled.value = enabled
    }

    /** First vacation day (epoch day), or -1 when unset. */
    private val _vacationStartDay =
        MutableStateFlow(prefs.getLong(KEY_VACATION_START_DAY, -1L))
    val vacationStartDay: StateFlow<Long> = _vacationStartDay.asStateFlow()

    /** Last vacation day (epoch day), or -1 for "until I turn it off". */
    private val _vacationEndDay =
        MutableStateFlow(prefs.getLong(KEY_VACATION_END_DAY, -1L))
    val vacationEndDay: StateFlow<Long> = _vacationEndDay.asStateFlow()

    fun setVacationRange(startDay: Long, endDay: Long) {
        prefs.edit()
            .putLong(KEY_VACATION_START_DAY, startDay)
            .putLong(KEY_VACATION_END_DAY, endDay)
            .apply()
        _vacationStartDay.value = startDay
        _vacationEndDay.value = endDay
    }

    // ------------------------------------------------------------------
    // v1.49 feature toggles. Everything defaults ON; each can be
    // disabled individually in Settings.
    // ------------------------------------------------------------------

    /** Random without repeats: skip quotes already shown by Random. */
    private val _randomNoRepeat =
        MutableStateFlow(prefs.getBoolean(KEY_RANDOM_NOREPEAT, true))
    val randomNoRepeat: StateFlow<Boolean> = _randomNoRepeat.asStateFlow()

    fun setRandomNoRepeat(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_RANDOM_NOREPEAT, enabled).apply()
        _randomNoRepeat.value = enabled
    }

    /**
     * "Pick up where you left off": remember the Browse scroll position
     * per database and restore it the next time the database is opened.
     */
    private val _browsePositionMemory =
        MutableStateFlow(prefs.getBoolean(KEY_BROWSE_POSITION_MEMORY, true))
    val browsePositionMemory: StateFlow<Boolean> = _browsePositionMemory.asStateFlow()

    fun setBrowsePositionMemory(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BROWSE_POSITION_MEMORY, enabled).apply()
        _browsePositionMemory.value = enabled
    }

    /**
     * Startup screen choice (default "home" = today's Browse-tab launch).
     * Stored as a choice key; see StartupDestinations for the catalog and
     * key-to-route resolution. Unknown keys fall back to the Browse route.
     */
    private val _startupScreen =
        MutableStateFlow(prefs.getString(KEY_STARTUP_SCREEN, "home") ?: "home")
    val startupScreen: StateFlow<String> = _startupScreen.asStateFlow()

    fun setStartupScreen(key: String) {
        prefs.edit().putString(KEY_STARTUP_SCREEN, key).apply()
        _startupScreen.value = key
    }

    /** Quote battle game: head-to-head Elo battles in the Games hub. */
    private val _quoteBattleEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_QUOTE_BATTLE_ENABLED, true))
    val quoteBattleEnabled: StateFlow<Boolean> = _quoteBattleEnabled.asStateFlow()

    fun setQuoteBattleEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUOTE_BATTLE_ENABLED, enabled).apply()
        _quoteBattleEnabled.value = enabled
    }

    /** Shareable stats card: render reading stats as a PNG and share it. */
    private val _statsCardShare =
        MutableStateFlow(prefs.getBoolean(KEY_STATS_CARD_SHARE, true))
    val statsCardShare: StateFlow<Boolean> = _statsCardShare.asStateFlow()

    fun setStatsCardShare(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STATS_CARD_SHARE, enabled).apply()
        _statsCardShare.value = enabled
    }

    /**
     * Extra home-screen widgets (Random Quote, Reading Streak). When off,
     * the provider components are disabled so they leave the widget picker.
     */
    private val _extraWidgets =
        MutableStateFlow(prefs.getBoolean(KEY_EXTRA_WIDGETS, true))
    val extraWidgets: StateFlow<Boolean> = _extraWidgets.asStateFlow()

    fun setExtraWidgets(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EXTRA_WIDGETS, enabled).apply()
        _extraWidgets.value = enabled
        val pm = appContext.packageManager
        val state = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        for (cls in listOf(
            "com.quotebrowser.app.widget.RandomQuoteWidgetProvider",
            "com.quotebrowser.app.widget.StreakWidgetProvider"
        )) {
            pm.setComponentEnabledSetting(
                ComponentName(appContext, cls),
                state, PackageManager.DONT_KILL_APP
            )
        }
    }

    /**
     * Quotes already shown by Random this cycle ("dbId/number"), so
     * shuffles never repeat until the pool is exhausted — then the cycle
     * resets. Kept separate from the reading-stats seen set so a reset
     * never wipes stats. Bounded to keep the pref small.
     */
    private val _randomCycle =
        MutableStateFlow(readStringSetPref(KEY_RANDOM_CYCLE))
    val randomCycle: StateFlow<Set<String>> = _randomCycle.asStateFlow()

    fun addToRandomCycle(keys: Collection<String>) {
        if (keys.isEmpty()) return
        val next = (_randomCycle.value + keys).toList().takeLast(20000).toSet()
        _randomCycle.value = next
        writeStringSetPref(KEY_RANDOM_CYCLE, next)
    }

    fun clearRandomCycle() {
        _randomCycle.value = emptySet()
        prefs.edit().remove(KEY_RANDOM_CYCLE).apply()
    }

    /** Weekly reading goal (quotes per 7 days, 0 = off). */
    private val _weeklyGoal = MutableStateFlow(prefs.getInt(KEY_WEEKLY_GOAL, 0))
    val weeklyGoal: StateFlow<Int> = _weeklyGoal.asStateFlow()

    fun setWeeklyGoal(n: Int) {
        val clean = n.coerceIn(0, 100000)
        prefs.edit().putInt(KEY_WEEKLY_GOAL, clean).apply()
        _weeklyGoal.value = clean
    }

    /** Monthly reading goal (quotes per calendar month, 0 = off). */
    private val _monthlyGoal = MutableStateFlow(prefs.getInt(KEY_MONTHLY_GOAL, 0))
    val monthlyGoal: StateFlow<Int> = _monthlyGoal.asStateFlow()

    fun setMonthlyGoal(n: Int) {
        val clean = n.coerceIn(0, 1000000)
        prefs.edit().putInt(KEY_MONTHLY_GOAL, clean).apply()
        _monthlyGoal.value = clean
    }

    /** Tap gestures: double-tap to favorite, two-finger tap to share. */
    private val _gesturesEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_GESTURES, true))
    val gesturesEnabled: StateFlow<Boolean> = _gesturesEnabled.asStateFlow()

    fun setGesturesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GESTURES, enabled).apply()
        _gesturesEnabled.value = enabled
    }

    /** Duplicate finder + cross-database merge. */
    private val _dupMergeEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_DUP_MERGE, true))
    val dupMergeEnabled: StateFlow<Boolean> = _dupMergeEnabled.asStateFlow()

    fun setDupMergeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DUP_MERGE, enabled).apply()
        _dupMergeEnabled.value = enabled
    }

    /** "Select all" button in the Search/Favorites bulk-selection bar. */
    private val _bulkSelectAll =
        MutableStateFlow(prefs.getBoolean(KEY_BULK_SELECT_ALL, true))
    val bulkSelectAll: StateFlow<Boolean> = _bulkSelectAll.asStateFlow()

    fun setBulkSelectAll(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BULK_SELECT_ALL, enabled).apply()
        _bulkSelectAll.value = enabled
    }

    /** Notes feature: the notes screens and note editing UI. */
    private val _notesEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_NOTES_ENABLED, true))
    val notesEnabled: StateFlow<Boolean> = _notesEnabled.asStateFlow()

    fun setNotesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTES_ENABLED, enabled).apply()
        _notesEnabled.value = enabled
    }

    /**
     * Rate-as-you-read: personal 1-5 star ratings on quote cards, plus the
     * "More like this" taste boost. Defaults ON.
     */
    private val _personalRatings =
        MutableStateFlow(prefs.getBoolean(KEY_PERSONAL_RATINGS, true))
    val personalRatings: StateFlow<Boolean> = _personalRatings.asStateFlow()

    fun setPersonalRatings(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERSONAL_RATINGS, enabled).apply()
        _personalRatings.value = enabled
    }

    /** Slideshow video export on the Export screen. */
    private val _slideshowExportEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_SLIDESHOW_EXPORT, true))
    val slideshowExportEnabled: StateFlow<Boolean> =
        _slideshowExportEnabled.asStateFlow()

    fun setSlideshowExportEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SLIDESHOW_EXPORT, enabled).apply()
        _slideshowExportEnabled.value = enabled
    }

    /** Reading goals on the Stats screen. */
    private val _readingGoalsEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_READING_GOALS, true))
    val readingGoalsEnabled: StateFlow<Boolean> = _readingGoalsEnabled.asStateFlow()

    fun setReadingGoalsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_READING_GOALS, enabled).apply()
        _readingGoalsEnabled.value = enabled
    }

    /** Chronological nick timeline on the nick page. */
    private val _nickTimelineEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_NICK_TIMELINE, true))
    val nickTimelineEnabled: StateFlow<Boolean> = _nickTimelineEnabled.asStateFlow()

    fun setNickTimelineEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NICK_TIMELINE, enabled).apply()
        _nickTimelineEnabled.value = enabled
    }

    // ------------------------------------------------------------------
    // v1.50 feature toggles. Everything defaults ON; each can be
    // disabled individually in Settings.
    // ------------------------------------------------------------------

    /** Pinch-to-zoom on the quote detail body (two-finger pinch). */
    private val _pinchZoomEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_PINCH_ZOOM_TEXT, true))
    val pinchZoomEnabled: StateFlow<Boolean> = _pinchZoomEnabled.asStateFlow()

    fun setPinchZoomEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PINCH_ZOOM_TEXT, enabled).apply()
        _pinchZoomEnabled.value = enabled
    }

    /**
     * Persisted pinch-zoom scale for the quote detail body. 1.0 is the
     * user's base font size (their text-size setting); the UI clamps
     * gestures to 0.8–2.5x and persists only when a gesture ends.
     */
    private val _detailTextScale =
        MutableStateFlow(prefs.getFloat(KEY_DETAIL_TEXT_SCALE, 1f))
    val detailTextScale: StateFlow<Float> = _detailTextScale.asStateFlow()

    fun setDetailTextScale(scale: Float) {
        val clean = scale.coerceIn(0.8f, 2.5f)
        prefs.edit().putFloat(KEY_DETAIL_TEXT_SCALE, clean).apply()
        _detailTextScale.value = clean
    }

    // ------------------------------------------------------------------
    // Daily quote reminder notification (AlarmManager). Defaults OFF; the
    // setting itself is the toggle. Setters arm/cancel the alarm directly
    // so the schedule always matches the stored prefs.
    // ------------------------------------------------------------------

    private val _dailyReminderEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, false))
    val dailyReminderEnabled: StateFlow<Boolean> = _dailyReminderEnabled.asStateFlow()

    private val _dailyReminderHour =
        MutableStateFlow(prefs.getInt(KEY_DAILY_REMINDER_HOUR, 8))
    val dailyReminderHour: StateFlow<Int> = _dailyReminderHour.asStateFlow()

    private val _dailyReminderMinute =
        MutableStateFlow(prefs.getInt(KEY_DAILY_REMINDER_MINUTE, 0))
    val dailyReminderMinute: StateFlow<Int> = _dailyReminderMinute.asStateFlow()

    fun setDailyReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
        _dailyReminderEnabled.value = enabled
        if (enabled) QuoteReminderScheduler.schedule(appContext)
        else QuoteReminderScheduler.cancel(appContext)
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        prefs.edit().putInt(KEY_DAILY_REMINDER_HOUR, h)
            .putInt(KEY_DAILY_REMINDER_MINUTE, m).apply()
        _dailyReminderHour.value = h
        _dailyReminderMinute.value = m
        if (_dailyReminderEnabled.value) QuoteReminderScheduler.schedule(appContext)
    }

    // ------------------------------------------------------------------
    // Quote alarm (v1.78): at the chosen time each day the phone reads a
    // random favorite quote aloud through a foreground TTS service, even
    // with the app closed. Defaults OFF; the setting itself is the
    // toggle. Setters arm/cancel the alarm directly so the schedule
    // always matches the stored prefs.
    // ------------------------------------------------------------------

    private val _quoteAlarmEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_QUOTE_ALARM_ENABLED, false))
    val quoteAlarmEnabled: StateFlow<Boolean> = _quoteAlarmEnabled.asStateFlow()

    private val _quoteAlarmHour =
        MutableStateFlow(prefs.getInt(KEY_QUOTE_ALARM_HOUR, 8))
    val quoteAlarmHour: StateFlow<Int> = _quoteAlarmHour.asStateFlow()

    private val _quoteAlarmMinute =
        MutableStateFlow(prefs.getInt(KEY_QUOTE_ALARM_MINUTE, 0))
    val quoteAlarmMinute: StateFlow<Int> = _quoteAlarmMinute.asStateFlow()

    fun setQuoteAlarmEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QUOTE_ALARM_ENABLED, enabled).apply()
        _quoteAlarmEnabled.value = enabled
        if (enabled) QuoteAlarmScheduler.schedule(appContext)
        else QuoteAlarmScheduler.cancel(appContext)
    }

    fun setQuoteAlarmTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        prefs.edit().putInt(KEY_QUOTE_ALARM_HOUR, h)
            .putInt(KEY_QUOTE_ALARM_MINUTE, m).apply()
        _quoteAlarmHour.value = h
        _quoteAlarmMinute.value = m
        if (_quoteAlarmEnabled.value) QuoteAlarmScheduler.schedule(appContext)
    }

    private fun readStringSetPref(key: String): Set<String> {
        val raw = prefs.getString(key, null) ?: return emptySet()
        return try {
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).mapTo(mutableSetOf()) { arr.getString(it) }
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun writeStringSetPref(key: String, values: Set<String>) {
        val arr = org.json.JSONArray()
        values.forEach { arr.put(it) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    // ------------------------------------------------------------------
    // Daily reading goal: target quotes per day, 0 = off.
    // ------------------------------------------------------------------

    private val _dailyGoal = MutableStateFlow(prefs.getInt(KEY_DAILY_GOAL, 0))
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    fun setDailyGoal(n: Int) {
        val clean = n.coerceIn(0, 10000)
        prefs.edit().putInt(KEY_DAILY_GOAL, clean).apply()
        _dailyGoal.value = clean
    }

    /** Quotes read per local day ("YYYY-MM-DD" -> count), newest first. */
    suspend fun readCountsByDay(): Map<String, Int> = withContext(Dispatchers.IO) {
        metaDb.metaDao().seenCountsByDay().associate { it.day to it.count }
    }

    // ------------------------------------------------------------------
    // Higher-or-lower game: best streak.
    // ------------------------------------------------------------------

    private val _gameBestStreak = MutableStateFlow(prefs.getInt(KEY_GAME_BEST_STREAK, 0))
    val gameBestStreak: StateFlow<Int> = _gameBestStreak.asStateFlow()

    /** Records a new best streak (no-op when not actually better). */
    fun setGameBestStreak(n: Int) {
        if (n <= _gameBestStreak.value) return
        prefs.edit().putInt(KEY_GAME_BEST_STREAK, n).apply()
        _gameBestStreak.value = n
    }

    private val _whoSaidBestStreak = MutableStateFlow(prefs.getInt(KEY_WHO_SAID_BEST, 0))
    val whoSaidBestStreak: StateFlow<Int> = _whoSaidBestStreak.asStateFlow()

    /** Records a new best streak (no-op when not actually better). */
    fun setWhoSaidBestStreak(n: Int) {
        if (n <= _whoSaidBestStreak.value) return
        prefs.edit().putInt(KEY_WHO_SAID_BEST, n).apply()
        _whoSaidBestStreak.value = n
    }

    private val _ratingGuessBestStreak =
        MutableStateFlow(prefs.getInt(KEY_RATING_GUESS_BEST, 0))
    val ratingGuessBestStreak: StateFlow<Int> = _ratingGuessBestStreak.asStateFlow()

    /** Records a new best streak (no-op when not actually better). */
    fun setRatingGuessBestStreak(n: Int) {
        if (n <= _ratingGuessBestStreak.value) return
        prefs.edit().putInt(KEY_RATING_GUESS_BEST, n).apply()
        _ratingGuessBestStreak.value = n
    }

    /**
     * Most-quoted nicks across all databases: (display nick, quote count),
     * most quotes first. Each nick counts once per quote. Shared by the
     * leaderboard, the person picker, and the "Who said it?" game.
     */
    suspend fun topNicks(limit: Int = 50): List<Pair<String, Int>> =
        withContext(Dispatchers.IO) {
            val counts = mutableMapOf<String, Pair<String, Int>>()
            for (id in allDbIds()) {
                val repo = repoFor(id)
                for (q in repo.getAllQuotes()) {
                    for (n in com.quotebrowser.app.ui.extractNicks(q.text)) {
                        val key = n.lowercase()
                        val cur = counts[key]
                        counts[key] = (cur?.first ?: n) to ((cur?.second ?: 0) + 1)
                    }
                }
            }
            counts.values.sortedByDescending { it.second }.take(limit)
        }

    // ------------------------------------------------------------------
    // Per-widget configuration: each home-screen widget instance can pick
    // its own database + category. Widgets placed before this feature keep
    // using the global widget settings (null config = legacy behavior).
    // ------------------------------------------------------------------

    /** Per-widget database + category choice. Null fields mean "use global". */
    /** Per-widget database choice: null = all databases. */
    data class WidgetConfig(val dbIds: Set<String>?, val category: String?)

    private fun widgetCfgKey(appWidgetId: Int) = "$KEY_WIDGET_CFG_PREFIX$appWidgetId"

    fun widgetConfig(appWidgetId: Int): WidgetConfig? {
        val raw = prefs.getString(widgetCfgKey(appWidgetId), null) ?: return null
        return try {
            val obj = JSONObject(raw)
            // New format: "dbIds" JSON array (null/missing = all databases).
            // Legacy format: single "dbId" ("all" = all databases).
            val dbIds: Set<String>? = if (obj.has("dbIds")) {
                val arr = obj.optJSONArray("dbIds")
                if (arr == null) null
                else (0 until arr.length()).map { arr.optString(it) }
                    .filter { it.isNotEmpty() }.toSet()
            } else {
                when (val legacy = obj.optString("dbId", "").ifEmpty { null }) {
                    null, ALL_DBS_ID -> null
                    else -> setOf(legacy)
                }
            }
            WidgetConfig(
                dbIds,
                obj.optString("category", "").ifEmpty { null }
            )
        } catch (e: Exception) {
            null
        }
    }

    fun setWidgetConfig(appWidgetId: Int, dbIds: Set<String>?, category: String?) {
        val obj = JSONObject()
        if (dbIds == null) obj.put("dbIds", JSONObject.NULL)
        else obj.put("dbIds", JSONArray(dbIds.toList()))
        obj.put("category", category ?: "")
        prefs.edit().putString(widgetCfgKey(appWidgetId), obj.toString()).apply()
    }

    fun clearWidgetConfig(appWidgetId: Int) {
        prefs.edit().remove(widgetCfgKey(appWidgetId)).apply()
    }

    // Last quote shown on a widget, so a tap refresh never repeats the same
    // quote back to back. Stored as "dbId:number".
    private fun widgetLastKey(appWidgetId: Int) = "widget_last_$appWidgetId"

    fun widgetLastQuote(appWidgetId: Int): String? =
        prefs.getString(widgetLastKey(appWidgetId), null)

    fun setWidgetLastQuote(appWidgetId: Int, dbId: String, number: Int) {
        prefs.edit().putString(widgetLastKey(appWidgetId), "$dbId:$number").apply()
    }

    // ------------------------------------------------------------------
    // Daily quote wallpaper (WorkManager, once per day).
    // ------------------------------------------------------------------

    private val _wallpaperDailyEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_WALLPAPER_DAILY, false))
    val wallpaperDailyEnabled: StateFlow<Boolean> = _wallpaperDailyEnabled.asStateFlow()

    /** Wallpaper quote source: "favorites" (default) or "random". */
    private val _wallpaperSource =
        MutableStateFlow(prefs.getString(KEY_WALLPAPER_SOURCE, "favorites") ?: "favorites")
    val wallpaperSource: StateFlow<String> = _wallpaperSource.asStateFlow()

    fun setWallpaperSource(source: String) {
        val clean = if (source == "random") "random" else "favorites"
        prefs.edit().putString(KEY_WALLPAPER_SOURCE, clean).apply()
        _wallpaperSource.value = clean
    }

    fun setWallpaperDailyEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WALLPAPER_DAILY, enabled).apply()
        _wallpaperDailyEnabled.value = enabled
        if (enabled) scheduleWallpaperWorker() else cancelWallpaperWorker()
    }

    /** Re-arms the daily worker when the setting is on (e.g. after reboot). */
    fun ensureWallpaperWorker() {
        if (_wallpaperDailyEnabled.value) scheduleWallpaperWorker()
    }

    private fun scheduleWallpaperWorker() {
        val req = PeriodicWorkRequestBuilder<com.quotebrowser.app.worker.WallpaperWorker>(
            24, TimeUnit.HOURS
        ).addTag("wallpaper_daily").build()
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            "wallpaper_daily",
            ExistingPeriodicWorkPolicy.UPDATE,
            req
        )
    }

    private fun cancelWallpaperWorker() {
        WorkManager.getInstance(appContext).cancelUniqueWork("wallpaper_daily")
    }

    /**
     * Widget category filter: the category name the home-screen widget
     * limits its random picks to, or null = all categories. Stored by name
     * (not id) because category ids are per-database; the widget resolves
     * the name in whichever database it draws from and falls back to
     * unfiltered when that database has no such category.
     */
    private val _widgetCategory =
        MutableStateFlow(prefs.getString(KEY_WIDGET_CATEGORY, null))
    val widgetCategory: StateFlow<String?> = _widgetCategory.asStateFlow()

    fun setWidgetCategory(name: String?) {
        if (name.isNullOrBlank()) prefs.edit().remove(KEY_WIDGET_CATEGORY).apply()
        else prefs.edit().putString(KEY_WIDGET_CATEGORY, name).apply()
        _widgetCategory.value = name?.takeIf { it.isNotBlank() }
    }

    /**
     * Local per-quote re-ratings ("$dbId/$number" -> -1/0/1), stored as JSON
     * in prefs so they survive without touching the quote database schema.
     * A thumbs-up adds +1 to the displayed rating, thumbs-down −1.
     */
    private fun readLocalRatings(): MutableMap<String, Int> {
        val map = mutableMapOf<String, Int>()
        try {
            val obj = org.json.JSONObject(prefs.getString(KEY_LOCAL_RATINGS, "{}") ?: "{}")
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                obj.optInt(k, 0).takeIf { it in -1..1 && it != 0 }?.let { map[k] = it }
            }
        } catch (e: Exception) {
            // Corrupt JSON: start fresh.
        }
        return map
    }

    private val _localRatings = MutableStateFlow(readLocalRatings())
    val localRatings: StateFlow<Map<String, Int>> = _localRatings.asStateFlow()

    fun localRatingFor(dbId: String, number: Int): Int =
        _localRatings.value["$dbId/$number"] ?: 0

    fun setLocalRating(dbId: String, number: Int, delta: Int) {
        val key = "$dbId/$number"
        val next = readLocalRatings()
        if (delta == 0) next.remove(key) else next[key] = delta.coerceIn(-1, 1)
        prefs.edit().putString(KEY_LOCAL_RATINGS, org.json.JSONObject(next as Map<*, *>).toString()).apply()
        _localRatings.value = next
    }

    /** Auto-backup frequency: "off", "daily", or "weekly". */
    private val _autoBackupFreq =
        MutableStateFlow(prefs.getString(KEY_AUTO_BACKUP_FREQ, "off") ?: "off")
    val autoBackupFreq: StateFlow<String> = _autoBackupFreq.asStateFlow()

    /** Persisted SAF tree URI of the auto-backup folder, or null. */
    private val _autoBackupTree =
        MutableStateFlow(prefs.getString(KEY_AUTO_BACKUP_TREE, null))
    val autoBackupTreeUri: StateFlow<String?> = _autoBackupTree.asStateFlow()

    /** Human-readable outcome of the last auto/manual backup run. */
    private val _lastAutoBackup =
        MutableStateFlow(prefs.getString(KEY_AUTO_BACKUP_LAST, null))
    val lastAutoBackup: StateFlow<String?> = _lastAutoBackup.asStateFlow()

    fun setAutoBackupFreq(freq: String) {
        val f = if (freq == "weekly") "weekly" else if (freq == "daily") "daily" else "off"
        prefs.edit().putString(KEY_AUTO_BACKUP_FREQ, f).apply()
        _autoBackupFreq.value = f
        if (f == "off") {
            WorkManager.getInstance(appContext).cancelUniqueWork(AUTO_BACKUP_WORK)
        } else {
            scheduleAutoBackupWork()
        }
    }

    fun setAutoBackupTreeUri(uri: String?) {
        if (uri.isNullOrBlank()) prefs.edit().remove(KEY_AUTO_BACKUP_TREE).apply()
        else prefs.edit().putString(KEY_AUTO_BACKUP_TREE, uri).apply()
        _autoBackupTree.value = uri?.takeIf { it.isNotBlank() }
    }

    fun setLastAutoBackup(text: String) {
        prefs.edit().putString(KEY_AUTO_BACKUP_LAST, text).apply()
        _lastAutoBackup.value = text
    }

    private fun scheduleAutoBackupWork() {
        val days = if (_autoBackupFreq.value == "weekly") 7L else 1L
        val request = PeriodicWorkRequestBuilder<BackupWorker>(days, TimeUnit.DAYS)
            .setInputData(
                androidx.work.Data.Builder()
                    .putString(
                        BackupWorker.KEY_TREE_URI,
                        _autoBackupTree.value
                    )
                    .build()
            )
            .addTag(AUTO_BACKUP_WORK)
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(
                AUTO_BACKUP_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }

    /**
     * Copies every database into the given SAF tree folder, named
     * "<label>-<timestamp>.db". Returns (databases written, error or null).
     */
    suspend fun backupDatabasesToTree(treeUri: Uri): Pair<Int, String?> =
        withContext(Dispatchers.IO) {
            val dbs = try {
                availableDatabases().first()
            } catch (e: Exception) {
                return@withContext 0 to "Couldn't list databases."
            }
            var count = 0
            val stamp = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            )
            for (db in dbs) {
                ensureExportable(db.id)
                val src = dbFileForExport(db.id) ?: continue
                val name = exportFileName(db.id).removeSuffix(".db") + "-$stamp.db"
                try {
                    val docUri = android.provider.DocumentsContract.createDocument(
                        appContext.contentResolver,
                        treeUri,
                        "application/octet-stream",
                        name
                    ) ?: return@withContext count to "Couldn't create the backup file."
                    appContext.contentResolver.openOutputStream(docUri)?.use { out ->
                        src.inputStream().use { input -> input.copyTo(out) }
                    } ?: return@withContext count to "Couldn't write the backup."
                    count++
                } catch (e: SecurityException) {
                    return@withContext count to
                        "Lost access to the backup folder — please choose it again."
                } catch (e: Exception) {
                    return@withContext count to (e.message ?: "Backup failed.")
                }
            }
            count to null
        }

    /** Numbers already seen in one database (for hide-seen filtering). */
    suspend fun seenNumbers(dbId: String): Set<Int> = withContext(Dispatchers.IO) {
        metaDb.metaDao().seenNumbers(dbId).toSet()
    }

    /** Wipes all reading-stats data. */
    suspend fun clearSeen() = withContext(Dispatchers.IO) {
        metaDb.metaDao().clearSeen()
    }

    // Random sources: null = all databases; otherwise the explicit set of
    // database ids that Random, Quote of the Day, and the widget pull from.
    private val _randomSourceIds =
        MutableStateFlow(prefs.getString(KEY_RANDOM_SOURCES, null)?.split(",")
            ?.filter { it.isNotBlank() }?.toSet())
    val randomSourceIds: StateFlow<Set<String>?> = _randomSourceIds.asStateFlow()

    fun setRandomSourceIds(ids: Set<String>?) {
        val clean = ids?.filter { it.isNotBlank() }?.toSet()?.takeIf { it.isNotEmpty() }
        if (clean == null) prefs.edit().remove(KEY_RANDOM_SOURCES).apply()
        else prefs.edit().putString(KEY_RANDOM_SOURCES, clean.joinToString(",")).apply()
        _randomSourceIds.value = clean
    }

    // Export queue: a staging list of quotes the user wants to export together.
    // Persisted so it survives process death, and cleared on startup only when
    // it was exported in a previous run ("exported and restarted to clear").
    private val _basket = MutableStateFlow(loadBasket())
    val basket: StateFlow<List<BasketEntry>> = _basket.asStateFlow()

    private fun loadBasket(): List<BasketEntry> {
        val raw = prefs.getString(KEY_BASKET, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                BasketEntry(o.getString("db"), o.getInt("n"))
            }.filter { it.dbId.isNotBlank() && it.number > 0 }.distinct()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveBasket() {
        val arr = JSONArray()
        for (e in _basket.value) {
            arr.put(JSONObject().put("db", e.dbId).put("n", e.number))
        }
        prefs.edit().putString(KEY_BASKET, arr.toString()).apply()
    }

    fun toggleBasket(dbId: String, number: Int) {
        val entry = BasketEntry(dbId, number)
        val cur = _basket.value
        _basket.value = if (entry in cur) cur - entry else cur + entry
        saveBasket()
    }

    fun removeFromBasket(dbId: String, number: Int) {
        val entry = BasketEntry(dbId, number)
        if (entry in _basket.value) {
            _basket.value = _basket.value - entry
            saveBasket()
        }
    }

    /**
     * v1.86: reorders the export tray (staging queue). Moves the entry at
     * [fromIndex] to [toIndex]; the tray order is the export order.
     */
    fun moveBasketEntry(fromIndex: Int, toIndex: Int) {
        val cur = _basket.value.toMutableList()
        if (fromIndex !in cur.indices || toIndex !in cur.indices) return
        if (fromIndex == toIndex) return
        val entry = cur.removeAt(fromIndex)
        cur.add(toIndex, entry)
        _basket.value = cur
        saveBasket()
    }

    fun clearBasket() {
        _basket.value = emptyList()
        saveBasket()
    }

    // Saved searches: most recent first, capped at 20.
    private val _savedSearches = MutableStateFlow(loadSavedSearches())
    val savedSearches: StateFlow<List<String>> = _savedSearches.asStateFlow()

    private fun loadSavedSearches(): List<String> {
        val raw = prefs.getString(KEY_SAVED_SEARCHES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { arr.getString(it) }
                .filter { it.isNotBlank() }.distinct()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveSearch(query: String) {
        val clean = query.trim()
        if (clean.isEmpty()) return
        val updated = (listOf(clean) + _savedSearches.value.filter { it != clean }).take(20)
        if (updated == _savedSearches.value) return
        _savedSearches.value = updated
        val arr = JSONArray()
        updated.forEach { arr.put(it) }
        prefs.edit().putString(KEY_SAVED_SEARCHES, arr.toString()).apply()
    }

    fun removeSavedSearch(query: String) {
        val updated = _savedSearches.value - query
        if (updated.size == _savedSearches.value.size) return
        _savedSearches.value = updated
        val arr = JSONArray()
        updated.forEach { arr.put(it) }
        prefs.edit().putString(KEY_SAVED_SEARCHES, arr.toString()).apply()
    }

    fun clearSavedSearches() {
        _savedSearches.value = emptyList()
        prefs.edit().remove(KEY_SAVED_SEARCHES).apply()
    }

    // ------------------------------------------------------------------
    // Named saved searches: a name plus the full search configuration
    // (query, database scope, categories, rating, sort, notes scope, and
    // the length filter) so one tap re-runs the exact search later.
    // Stored as a JSON array in prefs; coexists with the plain query
    // history above.
    // ------------------------------------------------------------------

    private val _namedSearches = MutableStateFlow(loadNamedSearches())
    val namedSearches: StateFlow<List<NamedSearch>> = _namedSearches.asStateFlow()

    private fun loadNamedSearches(): List<NamedSearch> {
        val raw = prefs.getString(KEY_NAMED_SEARCHES, null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            (0 until arr.length()).mapNotNull {
                NamedSearch.fromJson(arr.getJSONObject(it))
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveNamedSearches(list: List<NamedSearch>) {
        _namedSearches.value = list
        val arr = org.json.JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_NAMED_SEARCHES, arr.toString()).apply()
    }

    /**
     * Saves a named search. Replaces the entry with the same name (names
     * are unique); returns the saved search.
     */
    fun saveNamedSearch(search: NamedSearch) {
        val next = (_namedSearches.value.filter { it.name != search.name } +
            search).take(100)
        saveNamedSearches(next)
    }

    fun renameNamedSearch(oldName: String, newName: String): Boolean {
        val clean = newName.trim()
        if (clean.isEmpty()) return false
        val cur = _namedSearches.value
        val existing = cur.firstOrNull { it.name == oldName } ?: return false
        if (oldName != clean && cur.any { it.name == clean }) return false
        saveNamedSearches(
            cur.map { if (it.name == oldName) it.copy(name = clean) else it }
        )
        return true
    }

    fun deleteNamedSearch(name: String) {
        saveNamedSearches(_namedSearches.value.filter { it.name != name })
    }

    /**
     * v1.48: toggles the new-match alert for a saved search, keeping the
     * rest of the entry unchanged. Persisted with the same JSON store.
     */
    fun setNamedSearchAlert(name: String, alert: Boolean) {
        saveNamedSearches(
            _namedSearches.value.map {
                if (it.name == name) it.copy(alertOnNewMatch = alert) else it
            }
        )
    }

    fun clearNamedSearches() {
        saveNamedSearches(emptyList())
    }

    /**
     * v1.48 search alerts: runs every saved search with alertOnNewMatch
     * enabled over the newly imported database [newDbId] and posts one
     * notification per search with hits. Only alert-enabled searches are
     * evaluated, and only against the new database (a single COUNT query
     * per search reusing the same WHERE construction as the search
     * screen). Notes-scope searches are skipped: a fresh import has no
     * notes. Runs on the caller's thread — importDatabase calls it on
     * Dispatchers.IO.
     */
    private suspend fun checkSearchAlerts(context: android.content.Context, newDbId: String) {
        // v1.74.5: regex saved searches are skipped — the alert counter
        // does literal text matching, so a pattern would misfire.
        val alerted = _namedSearches.value.filter {
            it.alertOnNewMatch && !it.notesScope && !it.useRegex
        }
        if (alerted.isEmpty()) return
        val repo = QuoteRepository(getQuoteDatabase(newDbId), newDbId, metaDb.metaDao())
        for (ns in alerted) {
            val count = try {
                repo.countMatches(ns.query, ns.categoryIds, ns.minRating)
            } catch (e: Exception) {
                Log.w("SearchAlerts", "alert count failed for '${ns.name}'", e)
                0
            }
            if (count > 0) {
                com.quotebrowser.app.worker.postSearchAlertNotification(
                    context, ns.name, count
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Quote-body font: default, serif, monospace, or a user-supplied font
    // file picked through the Storage Access Framework. The value is
    // "default", "serif", "monospace", or the persisted content URI of the
    // picked file.
    // ------------------------------------------------------------------

    private val _quoteFont = MutableStateFlow(
        prefs.getString(KEY_QUOTE_FONT, "default") ?: "default"
    )
    val quoteFont: StateFlow<String> = _quoteFont.asStateFlow()

    fun setQuoteFont(value: String) {
        if (value == _quoteFont.value) return
        _quoteFont.value = value
        prefs.edit().putString(KEY_QUOTE_FONT, value).apply()
    }

    // ------------------------------------------------------------------
    // Daily Challenge: one scored attempt per day. Completion is keyed by
    // the local date (yyyy-MM-dd); replay after completion is unscored and
    // never touches the streak/best.
    // ------------------------------------------------------------------

    private val _dcLastDate = MutableStateFlow(
        prefs.getString(KEY_DC_LAST_DATE, null)
    )
    val dcLastDate: StateFlow<String?> = _dcLastDate.asStateFlow()

    private val _dcLastScore =
        MutableStateFlow(prefs.getInt(KEY_DC_LAST_SCORE, 0))
    val dcLastScore: StateFlow<Int> = _dcLastScore.asStateFlow()

    private val _dcBestScore =
        MutableStateFlow(prefs.getInt(KEY_DC_BEST_SCORE, 0))
    val dcBestScore: StateFlow<Int> = _dcBestScore.asStateFlow()

    private val _dcStreak = MutableStateFlow(prefs.getInt(KEY_DC_STREAK, 0))
    val dcStreak: StateFlow<Int> = _dcStreak.asStateFlow()

    /**
     * Records a finished scored attempt for [date] (yyyy-MM-dd). No-op when
     * an attempt for that date is already recorded.
     */
    fun recordDailyChallenge(date: String, score: Int) {
        if (_dcLastDate.value == date) return
        val prev = _dcLastDate.value
        val yesterday = try {
            java.time.LocalDate.parse(date).minusDays(1).toString()
        } catch (_: Exception) {
            null
        }
        val streak = if (prev != null && prev == yesterday) _dcStreak.value + 1
        else 1
        _dcLastDate.value = date
        _dcLastScore.value = score
        _dcBestScore.value = maxOf(_dcBestScore.value, score)
        _dcStreak.value = streak
        prefs.edit()
            .putString(KEY_DC_LAST_DATE, date)
            .putInt(KEY_DC_LAST_SCORE, score)
            .putInt(KEY_DC_BEST_SCORE, _dcBestScore.value)
            .putInt(KEY_DC_STREAK, streak)
            .apply()
    }

    // Favorite folders: named groups for organizing favorites. Assignments
    // are keyed "dbId:number" -> folder name; both live in prefs so no
    // database migration is needed.
    private val _favoriteFolders = MutableStateFlow(loadFavoriteFolders())
    val favoriteFolders: StateFlow<List<String>> = _favoriteFolders.asStateFlow()

    private val _folderTick = MutableStateFlow(0)
    /** Bumped whenever folder assignments change, so lists re-filter. */
    val folderTick: StateFlow<Int> = _folderTick.asStateFlow()

    private val folderMap: MutableMap<String, String> = loadFolderMap()

    private fun loadFavoriteFolders(): List<String> {
        val raw = prefs.getString(KEY_FAV_FOLDERS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { arr.getString(it) }
                .filter { it.isNotBlank() }.distinct()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadFolderMap(): MutableMap<String, String> {
        val raw = prefs.getString(KEY_FAV_FOLDER_MAP, null) ?: return mutableMapOf()
        return try {
            val obj = JSONObject(raw)
            val out = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.optString(k, "")
                if (v.isNotBlank()) out[k] = v
            }
            out
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun persistFolders() {
        val arr = JSONArray()
        _favoriteFolders.value.forEach { arr.put(it) }
        val obj = JSONObject()
        folderMap.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit()
            .putString(KEY_FAV_FOLDERS, arr.toString())
            .putString(KEY_FAV_FOLDER_MAP, obj.toString())
            .apply()
        _folderTick.value += 1
    }

    private fun folderKey(dbId: String, number: Int) = "$dbId:$number"

    /** Returns false when the name is blank or already exists. */
    fun addFavoriteFolder(name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty() || clean in _favoriteFolders.value) return false
        _favoriteFolders.value = _favoriteFolders.value + clean
        persistFolders()
        return true
    }

    fun renameFavoriteFolder(old: String, new: String): Boolean {
        val clean = new.trim()
        if (clean.isEmpty() || old !in _favoriteFolders.value) return false
        if (clean != old && clean in _favoriteFolders.value) return false
        _favoriteFolders.value = _favoriteFolders.value.map { if (it == old) clean else it }
        folderMap.entries.forEach { (k, v) -> if (v == old) folderMap[k] = clean }
        persistFolders()
        return true
    }

    fun deleteFavoriteFolder(name: String) {
        if (name !in _favoriteFolders.value) return
        _favoriteFolders.value = _favoriteFolders.value - name
        folderMap.entries.removeAll { it.value == name }
        persistFolders()
    }

    /** Assigns a favorite to a folder; null removes the assignment. */
    fun setFavoriteFolder(dbId: String, number: Int, folder: String?) {
        val key = folderKey(dbId, number)
        if (folder.isNullOrBlank()) {
            if (folderMap.remove(key) != null) persistFolders()
        } else {
            if (folderMap[key] != folder) {
                folderMap[key] = folder
                persistFolders()
            }
        }
    }

    fun folderForFavorite(dbId: String, number: Int): String? =
        folderMap[folderKey(dbId, number)]?.takeIf { it in _favoriteFolders.value }

    /**
     * Resolves the queued entries to quotes, skipping entries whose database
     * is gone or whose quote no longer exists (those are pruned).
     */
    /**
     * v1.66: resolves a user quote list's items to live quotes, skipping
     * entries whose database or quote is gone (without mutating the list —
     * the quote may come back after a database refresh).
     */
    suspend fun quoteListQuotes(items: List<QuoteListItem>): List<SourcedQuote> =
        withContext(Dispatchers.IO) {
            if (items.isEmpty()) return@withContext emptyList()
            val ids = allDbIds().toSet()
            val out = ArrayList<SourcedQuote>(items.size)
            for (e in items) {
                if (e.dbId !in ids) continue
                val q = try {
                    repoFor(e.dbId).peekQuote(e.number)
                } catch (_: Exception) {
                    null
                }
                if (q != null) out += SourcedQuote(e.dbId, q)
            }
            out
        }

    suspend fun basketQuotes(): List<SourcedQuote> = withContext(Dispatchers.IO) {
        val entries = _basket.value
        if (entries.isEmpty()) return@withContext emptyList()
        val ids = allDbIds().toSet()
        val out = ArrayList<SourcedQuote>(entries.size)
        val dead = ArrayList<BasketEntry>()
        for (e in entries) {
            if (e.dbId !in ids) {
                dead += e
                continue
            }
            val q = try {
                repoFor(e.dbId).peekQuote(e.number)
            } catch (_: Exception) {
                null
            }
            if (q == null) dead += e else out += SourcedQuote(e.dbId, q)
        }
        if (dead.isNotEmpty()) {
            _basket.value = entries - dead.toSet()
            saveBasket()
        }
        out
    }

    /**
     * The on-disk file backing a database id, for backup export / sharing.
     * Bundled databases live as Room's internal copy; imported and My Quotes
     * live under filesDir/databases/.
     */
    fun dbFileForExport(id: String): File? {
        val file = when {
            id.startsWith("asset:") ->
                appContext.getDatabasePath("bundled_${id.removePrefix("asset:")}")
            else -> File(appContext.filesDir, "databases/$id.db")
        }
        return file.takeIf { it.exists() }
    }

    /** Suggested file name when exporting/sharing a database. */
    fun exportFileName(id: String): String {
        val label = shortLabel(id).replace(Regex("[^A-Za-z0-9 _-]"), "").trim()
            .ifEmpty { "quotes" }
        return "$label.db"
    }

    /**
     * Opens the database (copying a bundled asset out on first use) and
     * checkpoints any WAL content into the main file, so a straight file
     * copy is a complete, valid database.
     */
    private fun ensureExportable(id: String) {
        try {
            val db = getQuoteDatabase(id).openHelper.writableDatabase
            try {
                db.query("PRAGMA wal_checkpoint(TRUNCATE)").use { it.moveToFirst() }
            } catch (ignored: Exception) {
            }
        } catch (e: Exception) {
            // Best effort; the copy below reports failure itself.
        }
    }

    /**
     * Copies a database file to the user-picked SAF uri (backup export).
     * Returns null on success, otherwise a human-readable error.
     */
    suspend fun exportDatabaseToUri(id: String, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            ensureExportable(id)
            val src = dbFileForExport(id)
                ?: return@withContext "Database file not found."
            try {
                appContext.contentResolver.openOutputStream(uri)?.use { out ->
                    src.inputStream().use { input -> input.copyTo(out) }
                } ?: return@withContext "Could not write to the selected location."
                null
            } catch (e: Exception) {
                e.message ?: "Export failed."
            }
        }

    /**
     * Stages a complete copy of the database under the cache dir for
     * sharing (email, etc.) via FileProvider. Call off the main thread.
     * Returns the staged file, or null on failure.
     */
    fun stagedShareFile(id: String): File? {
        ensureExportable(id)
        val src = dbFileForExport(id) ?: return null
        return try {
            val dest = File(appContext.cacheDir, "shared_dbs").apply { mkdirs() }
            val out = File(dest, exportFileName(id))
            src.copyTo(out, overwrite = true)
            out
        } catch (e: Exception) {
            null
        }
    }

    // Named custom themes, stored as a JSON array of {name, colors}.
    private val _customThemes = MutableStateFlow(loadCustomThemes())
    val customThemes: StateFlow<List<CustomTheme>> = _customThemes.asStateFlow()

    private fun loadCustomThemes(): List<CustomTheme> {
        val raw = prefs.getString(KEY_CUSTOM_THEMES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                CustomTheme(
                    name = obj.optString("name", "Custom ${i + 1}"),
                    colors = obj.optString("colors", "")
                )
            }.filter { it.colors.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistCustomThemes(themes: List<CustomTheme>) {
        val arr = JSONArray()
        themes.forEach { t ->
            arr.put(JSONObject().apply {
                put("name", t.name)
                put("colors", t.colors)
            })
        }
        prefs.edit().putString(KEY_CUSTOM_THEMES, arr.toString()).apply()
        _customThemes.value = themes
    }

    fun addCustomTheme(name: String, colors: String) {
        val cleanName = name.trim().ifEmpty { "Custom ${_customThemes.value.size + 1}" }
        persistCustomThemes(_customThemes.value + CustomTheme(cleanName, colors))
    }

    fun updateCustomTheme(index: Int, name: String, colors: String) {
        val themes = _customThemes.value.toMutableList()
        if (index !in themes.indices) return
        val cleanName = name.trim().ifEmpty { themes[index].name }
        themes[index] = themes[index].copy(name = cleanName, colors = colors)
        persistCustomThemes(themes)
    }

    fun deleteCustomTheme(index: Int) {
        val themes = _customThemes.value.toMutableList()
        if (index !in themes.indices) return
        themes.removeAt(index)
        persistCustomThemes(themes)
        // If the deleted theme was selected, fall back to Quote404.
        if (_themeId.value == customThemeId(index)) {
            setThemeId("quote404")
        } else if (_themeId.value.startsWith("custom:")) {
            // Indexes shifted: re-point the selection at the same theme.
            val oldIndex = _themeId.value.removePrefix("custom:").toIntOrNull()
            if (oldIndex != null && oldIndex > index) {
                setThemeId(customThemeId(oldIndex - 1))
            }
        }
    }

    // v1.51 built-in theme overrides: presetId -> custom-theme-format
    // colors CSV. The hardcoded ThemePresets stay the never-lost defaults;
    // "Reset to default" deletes the override. Stored as a JSON object.
    private val _builtInThemeOverrides = MutableStateFlow(loadBuiltInThemeOverrides())
    val builtInThemeOverrides: StateFlow<Map<String, String>> =
        _builtInThemeOverrides.asStateFlow()

    private fun loadBuiltInThemeOverrides(): Map<String, String> {
        val raw = prefs.getString(KEY_BUILTIN_THEME_OVERRIDES, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            obj.keys().asSequence()
                .mapNotNull { k ->
                    val v = obj.optString(k, "")
                    if (v.isNotBlank()) k to v else null
                }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setBuiltInThemeOverride(presetId: String, colorsCsv: String) {
        val map = _builtInThemeOverrides.value.toMutableMap()
        map[presetId] = colorsCsv
        _builtInThemeOverrides.value = map
        prefs.edit().putString(KEY_BUILTIN_THEME_OVERRIDES, JSONObject(map as Map<*, *>).toString()).apply()
    }

    fun clearBuiltInThemeOverride(presetId: String) {
        val map = _builtInThemeOverrides.value.toMutableMap()
        if (map.remove(presetId) == null) return
        _builtInThemeOverrides.value = map
        prefs.edit().putString(KEY_BUILTIN_THEME_OVERRIDES, JSONObject(map as Map<*, *>).toString()).apply()
    }

    // v1.51 per-database themes: feature switch (default ON) and the
    // master override switch (default OFF).
    private val _perDbThemesEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_PERDB_THEMES_ENABLED, true))
    val perDbThemesEnabled: StateFlow<Boolean> = _perDbThemesEnabled.asStateFlow()

    fun setPerDbThemesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERDB_THEMES_ENABLED, enabled).apply()
        _perDbThemesEnabled.value = enabled
    }

    private val _masterThemeOverride =
        MutableStateFlow(prefs.getBoolean(KEY_MASTER_THEME_OVERRIDE, false))
    val masterThemeOverride: StateFlow<Boolean> = _masterThemeOverride.asStateFlow()

    fun setMasterThemeOverride(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_THEME_OVERRIDE, enabled).apply()
        _masterThemeOverride.value = enabled
    }

    // v1.51 per-database theme configs: dbId -> DatabaseThemeConfig JSON.
    // Missing entries resolve to DatabaseThemeConfig() defaults (site
    // theme, site colors on, Site layout, both statuses).
    private val _dbThemeConfigs = MutableStateFlow(loadDbThemeConfigs())
    val dbThemeConfigs: StateFlow<Map<String, DatabaseThemeConfig>> =
        _dbThemeConfigs.asStateFlow()

    private fun loadDbThemeConfigs(): Map<String, DatabaseThemeConfig> {
        val raw = prefs.getString(KEY_DB_THEME_CONFIGS, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            obj.keys().asSequence()
                .mapNotNull { k ->
                    val v = obj.optString(k, null)
                    if (v != null) k to DatabaseThemeConfig.fromJson(v) else null
                }.toMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun dbThemeConfig(dbId: String): DatabaseThemeConfig =
        _dbThemeConfigs.value[dbId] ?: DatabaseThemeConfig()

    fun setDbThemeConfig(dbId: String, config: DatabaseThemeConfig) {
        val map = _dbThemeConfigs.value.toMutableMap()
        map[dbId] = config
        _dbThemeConfigs.value = map
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v.toJson().toString()) }
        prefs.edit().putString(KEY_DB_THEME_CONFIGS, obj.toString()).apply()
    }

    /**
     * v1.56: app-private directory for per-database page background
     * images (Settings → database themes → "Background image"). Images
     * picked via SAF are copied here so the app keeps working after the
     * original URI permission lapses.
     */
    fun dbBackgroundImageDir(): File =
        File(appContext.filesDir, "db_page_backgrounds").apply { mkdirs() }

    /** Resolves a stored background-image file name ("" = none). */
    fun dbBackgroundImageFile(name: String): File =
        File(dbBackgroundImageDir(), sanitizeFileName(name))

    /**
     * Copies a SAF-picked image into app-private storage and returns the
     * stored file name. The copy is re-encoded from the decoded bitmap
     * (JPEG, quality 90, long side ≤ 1920px) so exotic formats and huge
     * files can't break the background layer. Runs on Dispatchers.IO.
     */
    suspend fun saveDbBackgroundImage(uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = appContext.contentResolver.openInputStream(uri)?.use { input ->
                    android.graphics.BitmapFactory.decodeStream(input)
                } ?: return@withContext Result.failure(
                    IllegalArgumentException("Could not read the selected image.")
                )
                val maxSide = 1920
                val scale = minOf(
                    1f,
                    maxSide / maxOf(bitmap.width, bitmap.height).toFloat()
                )
                val scaled = if (scale < 1f) {
                    android.graphics.Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * scale).toInt().coerceAtLeast(1),
                        (bitmap.height * scale).toInt().coerceAtLeast(1),
                        true
                    )
                } else bitmap
                val name = "bg_${System.currentTimeMillis()}.jpg"
                val file = File(dbBackgroundImageDir(), name)
                file.outputStream().use { out ->
                    if (!scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)) {
                        throw IllegalStateException("Could not encode the image.")
                    }
                }
                if (scaled !== bitmap) bitmap.recycle()
                scaled.recycle()
                Result.success(name)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /** Deletes a stored background image (best effort). */
    fun deleteDbBackgroundImage(name: String) {
        try {
            if (name.isNotBlank()) dbBackgroundImageFile(name).delete()
        } catch (_: Exception) {
        }
    }

    /**
     * The effective status mode for a database's Browse/Search/Random
     * flows: null when the status/content filter feature is off (no
     * filtering), else the per-database mode (Both = no filtering).
     */
    fun statusModeFor(dbId: String): StatusMode? {
        if (!_statusFilterEnabled.value) return null
        return when (val mode = dbThemeConfig(dbId).statusMode) {
            StatusMode.BOTH -> null
            else -> mode
        }
    }

    // v1.51 feature toggles (all default ON).
    private fun boolPref(key: String, default: Boolean): MutableStateFlow<Boolean> =
        MutableStateFlow(prefs.getBoolean(key, default))

    private fun setBoolPref(key: String, flow: MutableStateFlow<Boolean>, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
        flow.value = value
    }

    private fun stringPref(key: String, default: String): MutableStateFlow<String> =
        MutableStateFlow(prefs.getString(key, default) ?: default)

    private fun setStringPref(key: String, flow: MutableStateFlow<String>, value: String) {
        prefs.edit().putString(key, value).apply()
        flow.value = value
    }

    private val _classicLayoutOption = boolPref(KEY_CLASSIC_LAYOUT_OPTION, true)
    val classicLayoutOption: StateFlow<Boolean> = _classicLayoutOption.asStateFlow()
    fun setClassicLayoutOption(enabled: Boolean) =
        setBoolPref(KEY_CLASSIC_LAYOUT_OPTION, _classicLayoutOption, enabled)

    private val _plusMinusRatings = boolPref(KEY_PLUSMINUS_RATINGS, true)
    val plusMinusRatingsEnabled: StateFlow<Boolean> = _plusMinusRatings.asStateFlow()
    fun setPlusMinusRatingsEnabled(enabled: Boolean) =
        setBoolPref(KEY_PLUSMINUS_RATINGS, _plusMinusRatings, enabled)

    private val _queuedTags = boolPref(KEY_QUEUED_TAGS, true)
    val queuedTagsEnabled: StateFlow<Boolean> = _queuedTags.asStateFlow()
    fun setQueuedTagsEnabled(enabled: Boolean) =
        setBoolPref(KEY_QUEUED_TAGS, _queuedTags, enabled)

    private val _statusFilterEnabled = boolPref(KEY_STATUS_FILTER, true)
    val statusFilterEnabled: StateFlow<Boolean> = _statusFilterEnabled.asStateFlow()
    fun setStatusFilterEnabled(enabled: Boolean) =
        setBoolPref(KEY_STATUS_FILTER, _statusFilterEnabled, enabled)

    // v1.52 display-only text transformation (never alters stored text,
    // never applied to TTS): a single mutually-exclusive mode, default OFF.
    // v1.51's three stacking toggles (which shipped defaulting ON) are
    // retired; the one-time migration in init() forces OFF on upgrade.
    private val _transformMode =
        stringPref(KEY_TRANSFORM_MODE, TransformMode.OFF.name)
    val transformMode: StateFlow<String> = _transformMode.asStateFlow()
    fun setTransformMode(mode: TransformMode) =
        setStringPref(KEY_TRANSFORM_MODE, _transformMode, mode.name)

    // v1.53 feature toggles. All default ON except Kid-safe (default OFF).
    private val _myQuotesFeature = boolPref(KEY_MY_QUOTES_FEATURE, true)
    val myQuotesEnabled: StateFlow<Boolean> = _myQuotesFeature.asStateFlow()
    fun setMyQuotesEnabled(enabled: Boolean) =
        setBoolPref(KEY_MY_QUOTES_FEATURE, _myQuotesFeature, enabled)

    private val _perDbWidget = boolPref(KEY_PER_DB_WIDGET, true)
    val perDbWidgetEnabled: StateFlow<Boolean> = _perDbWidget.asStateFlow()
    fun setPerDbWidgetEnabled(enabled: Boolean) =
        setBoolPref(KEY_PER_DB_WIDGET, _perDbWidget, enabled)

    private val _memorizeMode = boolPref(KEY_MEMORIZE_MODE, true)
    val memorizeModeEnabled: StateFlow<Boolean> = _memorizeMode.asStateFlow()
    fun setMemorizeModeEnabled(enabled: Boolean) =
        setBoolPref(KEY_MEMORIZE_MODE, _memorizeMode, enabled)

    private val _onThisDay = boolPref(KEY_ON_THIS_DAY, true)
    val onThisDayEnabled: StateFlow<Boolean> = _onThisDay.asStateFlow()
    fun setOnThisDayEnabled(enabled: Boolean) =
        setBoolPref(KEY_ON_THIS_DAY, _onThisDay, enabled)

    private val _kidSafe = boolPref(KEY_KID_SAFE, false)
    val kidSafeEnabled: StateFlow<Boolean> = _kidSafe.asStateFlow()
    fun setKidSafeEnabled(enabled: Boolean) =
        setBoolPref(KEY_KID_SAFE, _kidSafe, enabled)

    private val _siteShareCards = boolPref(KEY_SITE_SHARE_CARDS, true)
    val siteShareCardsEnabled: StateFlow<Boolean> = _siteShareCards.asStateFlow()
    fun setSiteShareCardsEnabled(enabled: Boolean) =
        setBoolPref(KEY_SITE_SHARE_CARDS, _siteShareCards, enabled)

    private val _explainQuote = boolPref(KEY_EXPLAIN_QUOTE, true)
    val explainQuoteEnabled: StateFlow<Boolean> = _explainQuote.asStateFlow()
    fun setExplainQuoteEnabled(enabled: Boolean) =
        setBoolPref(KEY_EXPLAIN_QUOTE, _explainQuote, enabled)

    // v1.53 report/flag feature toggle (default ON) and the "hide flagged
    // quotes" toggle (default OFF — flagging alone never hides anything).
    private val _flagQuotes = boolPref(KEY_FLAG_QUOTES, true)
    val flagQuotesEnabled: StateFlow<Boolean> = _flagQuotes.asStateFlow()
    fun setFlagQuotesEnabled(enabled: Boolean) =
        setBoolPref(KEY_FLAG_QUOTES, _flagQuotes, enabled)

    private val _hideFlaggedQuotes = boolPref(KEY_HIDE_FLAGGED_QUOTES, false)
    val hideFlaggedQuotes: StateFlow<Boolean> = _hideFlaggedQuotes.asStateFlow()
    fun setHideFlaggedQuotes(enabled: Boolean) =
        setBoolPref(KEY_HIDE_FLAGGED_QUOTES, _hideFlaggedQuotes, enabled)

    private val _ambientMode = boolPref(KEY_AMBIENT_MODE, true)
    val ambientModeEnabled: StateFlow<Boolean> = _ambientMode.asStateFlow()
    fun setAmbientModeEnabled(enabled: Boolean) =
        setBoolPref(KEY_AMBIENT_MODE, _ambientMode, enabled)

    private val _ambientInterval =
        MutableStateFlow(prefs.getInt(KEY_AMBIENT_INTERVAL, 15).coerceIn(5, 120))
    val ambientIntervalSec: StateFlow<Int> = _ambientInterval.asStateFlow()
    fun setAmbientIntervalSec(seconds: Int) {
        val v = seconds.coerceIn(5, 120)
        prefs.edit().putInt(KEY_AMBIENT_INTERVAL, v).apply()
        _ambientInterval.value = v
    }

    // v1.53 main-theme option: black background outside the quote cards.
    // Defaults ON (v1.87.2): the page around quote cards is black; the qdb.us
    // site theme's white page shows only when the user disables this.
    private val _blackOutsideQuotes = boolPref(KEY_BLACK_OUTSIDE_QUOTES, true)
    val blackOutsideQuotes: StateFlow<Boolean> = _blackOutsideQuotes.asStateFlow()
    fun setBlackOutsideQuotes(enabled: Boolean) =
        setBoolPref(KEY_BLACK_OUTSIDE_QUOTES, _blackOutsideQuotes, enabled)

    init {
        // One-time migrations:
        // - v1.61.5: the default theme is now Quote404 (Clint's new
        //   branding). "system" was already migrated to "midnight" in an
        //   earlier release; both plus "devxen" move to Quote404 once so
        //   the new artwork shows everywhere by default. Afterwards the
        //   user's own theme choice is respected.
        if (prefs.getString(KEY_THEME, null) in setOf("system", "midnight", "devxen"))
            setThemeId("quote404")
        // - The legacy single custom theme becomes a named entry.
        val legacy = prefs.getString(KEY_CUSTOM_THEME, null)
        if (!legacy.isNullOrBlank() && _customThemes.value.isEmpty()) {
            persistCustomThemes(listOf(CustomTheme("Custom", legacy)))
            prefs.edit().remove(KEY_CUSTOM_THEME).apply()
        }
        if (prefs.getString(KEY_THEME, null) == "custom") setThemeId(customThemeId(0))
        // v1.86: the export tray is staging — it persists across restarts
        // until the user clears it. (Previously an exported tray was
        // dropped on the next restart; Clint asked that it stick around.)
        // v1.52: the v1.51 display-transform toggles shipped defaulting ON
        // and stacked, rendering quote text unreadable. Force the new
        // single mode to Off once on upgrade; afterwards the user's choice
        // is respected. Fresh installs default to Off via stringPref.
        if (!prefs.getBoolean(KEY_TRANSFORM_MODE_MIGRATED, false)) {
            prefs.edit()
                .putString(KEY_TRANSFORM_MODE, TransformMode.OFF.name)
                .remove(KEY_LEET_TRANSFORM)
                .remove(KEY_UPSIDEDOWN_TRANSFORM)
                .remove(KEY_ALTERNATING_TRANSFORM)
                .putBoolean(KEY_TRANSFORM_MODE_MIGRATED, true)
                .apply()
            _transformMode.value = TransformMode.OFF.name
        }
        // Re-schedule after a reboot / process restart when the user opted in.
        if (prefs.getBoolean(KEY_QOTD_NOTIF, false)) scheduleQotdWork()
        if ((prefs.getString(KEY_AUTO_BACKUP_FREQ, "off") ?: "off") != "off") {
            scheduleAutoBackupWork()
        }
    }

    /**
     * v1.88.19: the single process-wide cache of open Room instances,
     * keyed by database id — one cache per database, owned by the
     * singleton. getQuoteDatabase() reuses the cached handle instead
     * of re-opening (the bundled-asset branch additionally shares
     * in-flight opens via [inFlightBundledOpens]), and
     * evictQuoteDatabase() drops the only handle. Because every entry
     * point resolves through DatabaseManager.getInstance(), no second
     * manager can build a parallel Room instance for the same id.
     */
    private val quoteDbCache = mutableMapOf<String, QuoteDatabase>()

    /**
     * v1.88.17: in-flight bundled opens. The 2026-09-20 startup log proved
     * that concurrent opens of the same bundled DB each run a FULL expensive
     * open (Room's SQLiteCopyOpenHelper.verifyDatabaseFile takes a per-DB
     * ProcessLock, so 3 parallel opens of bash_im.db serialized to
     * 18.7s/28.7s/38.5s). The cache only deduplicates AFTER an open
     * completes, so races pay the full cost N times. This registry lets
     * concurrent callers share one open: the first caller performs it, the
     * rest await its Deferred. Entries are removed when the open completes
     * (success or failure).
     */
    private val inFlightBundledOpens = mutableMapOf<String, kotlinx.coroutines.CompletableDeferred<QuoteDatabase>>()
    private val inFlightOpensLock = Any()

    /** Bundled databases found in the assets/databases/ folder. */
    fun bundledDbs(): List<DbInfo> {
        val files = try {
            appContext.assets.list(ASSET_DIR)
        } catch (e: Exception) {
            emptyArray()
        } ?: emptyArray()
        return files
            .filter { it.endsWith(".db", ignoreCase = true) }
            .sorted()
            .map {
                DbInfo(
                    id = "asset:$it",
                    label = bundledLabel(it),
                    date = BUNDLED_DB_DATES[it]
                )
            }
    }

    /** User-arranged database order (empty = default order). */
    private val _dbOrder = MutableStateFlow(loadDbOrder())
    val dbOrder: StateFlow<List<String>> = _dbOrder.asStateFlow()

    private fun loadDbOrder(): List<String> {
        val raw = prefs.getString(KEY_DB_ORDER, null) ?: return emptyList()
        return try {
            org.json.JSONArray(raw).let { arr ->
                List(arr.length()) { i -> arr.getString(i) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setDbOrder(ids: List<String>) {
        _dbOrder.value = ids
        prefs.edit()
            .putString(KEY_DB_ORDER, org.json.JSONArray(ids).toString())
            .apply()
    }

    /**
     * Default database order: Bash.org, #CrapShack Quotes, qdb.us, then the
     * remaining bundled databases alphabetically. My Quotes and imported
     * databases keep their usual slots after the bundled ones.
     */
    private fun applyDbOrder(list: List<DbInfo>): List<DbInfo> {
        val order = _dbOrder.value
        if (order.isEmpty()) {
            val priority =
                listOf("asset:bash_org.db", "asset:crapshack_quotes.db", "asset:qdb_us.db")
            val (bundled, rest) = list.partition { it.id.startsWith("asset:") }
            val orderedBundled = bundled.sortedWith(
                compareBy(
                    { val i = priority.indexOf(it.id); if (i < 0) Int.MAX_VALUE else i },
                    { it.label.lowercase() }
                )
            )
            return orderedBundled + rest
        }
        val index = order.withIndex().associate { it.value to it.index }
        return list.sortedWith(
            compareBy(
                { index[it.id] ?: Int.MAX_VALUE },
                { it.label.lowercase() }
            )
        )
    }

    /** v1.55: "Show collection dates" removed — collection dates now
     * live in the per-database info panel. The preference key is kept
     * so old prefs don't leave stale values. */

    /**
     * Quote-detail swipe source: false = swiping stays in the current
     * database, true = swiping walks through all databases.
     */
    private val _detailSwipeAllDbs =
        MutableStateFlow(prefs.getBoolean(KEY_DETAIL_SWIPE_ALL, false))
    val detailSwipeAllDbs: StateFlow<Boolean> = _detailSwipeAllDbs.asStateFlow()

    fun setDetailSwipeAllDbs(all: Boolean) {
        _detailSwipeAllDbs.value = all
        prefs.edit().putBoolean(KEY_DETAIL_SWIPE_ALL, all).apply()
    }

    /**
     * Quote-detail swipe order: "random" (default) or "ordered"
     * (by quote number, like the browse list).
     */
    private val _detailSwipeOrder =
        MutableStateFlow(prefs.getString(KEY_DETAIL_SWIPE_ORDER, "random") ?: "random")
    val detailSwipeOrder: StateFlow<String> = _detailSwipeOrder.asStateFlow()

    fun setDetailSwipeOrder(order: String) {
        val value = if (order == "ordered") "ordered" else "random"
        _detailSwipeOrder.value = value
        prefs.edit().putString(KEY_DETAIL_SWIPE_ORDER, value).apply()
    }

    private fun bundledLabel(fileName: String): String =
        BUNDLED_LABEL_OVERRIDES[fileName]
            ?: prettifyLabel(fileName.removeSuffix(".db"))

    /** All databases: bundled first, then My Quotes, then imported, then plugin quote packs. */
    fun availableDatabases(): Flow<List<DbInfo>> =
        combine(
            metaDb.metaDao().importedDbs(),
            // v1.53: My Quotes is a gated feature (default ON); the list
            // re-emits when the toggle changes.
            myQuotesEnabled,
            // v1.83: plugin quote packs register as real databases; the
            // list re-emits when packs are installed/uninstalled/toggled.
            PluginManager.plugins
        ) { imported, myQuotesOn, _ ->
            applyDbOrder(
                bundledDbs() +
                    (if (myQuotesOn) {
                        listOf(DbInfo(id = MY_QUOTES_ID, label = MY_QUOTES_LABEL))
                    } else emptyList()) +
                    imported.map { DbInfo(id = it.id, label = it.label) } +
                    PluginManager.quotePackDbInfos()
            )
        }

    /**
     * Databases for the top-bar switcher: the virtual "All databases" entry
     * pinned first, then [availableDatabases].
     */
    fun browseDatabases(): Flow<List<DbInfo>> =
        availableDatabases().map { list ->
            listOf(DbInfo(id = ALL_DBS_ID, label = ALL_DBS_LABEL)) + list
        }

    /**
     * Quote-of-the-Day candidate databases: keeps only ids that contain at
     * least one quote. The daily rotation must never land on an empty pool
     * (e.g. a fresh "My Quotes" database) — that used to surface on the
     * Home card as a false "All quotes are hidden by the content filter."
     */
    suspend fun qotdNonEmptyDbIds(ids: List<String>): List<String> =
        withContext(Dispatchers.IO) {
            ids.filter { id ->
                try {
                    repoFor(id).count() > 0
                } catch (e: Exception) {
                    false
                }
            }
        }

    /** Ids of every known database (used when random sources = "all"). */
    suspend fun allDbIds(): List<String> = withContext(Dispatchers.IO) {
        bundledDbs().map { it.id } + MY_QUOTES_ID +
            metaDb.metaDao().importedDbsSync().map { it.id } +
            PluginManager.quotePackDbIds()
    }

    /**
     * v1.47: runs every database's auto-categorize rules (user phrases plus
     * the built-in "Offensive" list) over all of its quotes. Returns the
     * number of newly created quote<->category mappings. Runs off the UI
     * thread; [onProgress] reports per-database progress for a progress
     * bar. A failing database is skipped, never aborts the sweep.
     */
    suspend fun applyAllAutoRules(
        onProgress: (dbLabel: String, done: Int, total: Int) -> Unit =
            { _, _, _ -> }
    ): CategoryStore.AutoRuleResult = withContext(Dispatchers.IO) {
        var total = CategoryStore.AutoRuleResult(0, 0)
        val ids = allDbIds()
        ids.forEachIndexed { index, id ->
            val store = try {
                CategoryStore(getQuoteDatabase(id))
            } catch (e: Exception) {
                Log.w("AutoRules", "skipping $id", e)
                null
            }
            if (store != null) {
                try {
                    total += store.applyAutoRules { _, _ -> }
                } catch (e: Exception) {
                    Log.w("AutoRules", "sweep failed for $id", e)
                }
            }
            onProgress(shortLabel(id), index + 1, ids.size)
        }
        total
    }

    /**
     * Display title for a database id. v1.55: collection dates no longer
     * appear in the database switcher — just the plain name ("Bash.org").
     * Dates live in the per-database info panel instead.
     */
    fun currentLabel(id: String): String = shortLabel(id)

    /**
     * Short display name of a database ("Bash.org", "qdb.us"): the base
     * label with no collection-date suffix. Used for source chips on quote
     * cards, where space is tight.
     */
    fun shortLabel(id: String): String = when {
        id == MY_QUOTES_ID -> MY_QUOTES_LABEL
        id == ALL_DBS_ID -> ALL_DBS_LABEL
        // v1.83: plugin quote packs carry their display name from the pack manifest.
        id.startsWith(PluginManager.DB_ID_PREFIX) ->
            PluginManager.dbLabel(id.removePrefix(PluginManager.DB_ID_PREFIX))
                ?: prettifyLabel(id)
        else -> bundledDbs().find { it.id == id }?.label
            ?: BUNDLED_LABEL_OVERRIDES[id.removePrefix("asset:")]
            ?: prettifyLabel(id)
    }

    /**
     * Number of quotes in a database, or null when it can't be read.
     * Shown next to each database in the switcher, like category counts.
     */
    suspend fun quoteCount(id: String): Int? = withContext(Dispatchers.IO) {
        try {
            if (id == ALL_DBS_ID) {
                var total = 0
                allDbIds().forEach { realId ->
                    total += getQuoteDatabase(realId).quoteDao().count()
                }
                total
            } else {
                getQuoteDatabase(id).quoteDao().count()
            }
        } catch (e: Exception) {
            null
        }
    }

    // Nick colors: up to MAX_NICK_RULES per-person <nick> color overrides,
    // stored as JSON [{nick, brackets, name}] with RRGGBB hex or null.
    private val _nickRules = MutableStateFlow(loadNickRules())
    val nickRules: StateFlow<List<com.quotebrowser.app.ui.NickColorRule>> =
        _nickRules.asStateFlow()

    private fun loadNickRules(): List<com.quotebrowser.app.ui.NickColorRule> {
        val raw = prefs.getString(KEY_NICK_RULES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                com.quotebrowser.app.ui.NickColorRule(
                    nick = o.getString("nick"),
                    bracketsHex = loadRuleHex(o.optString("brackets", "")),
                    nameHex = loadRuleHex(o.optString("name", ""))
                )
            }.filter { it.nick.isNotBlank() }.distinctBy { it.nick.lowercase() }
                .take(MAX_NICK_RULES)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * JSON nick-rule color decoding: "" (legacy) -> null (theme default),
     * "none" -> [NICK_COLOR_NONE] (explicitly uncolored), else the hex color.
     */
    private fun loadRuleHex(raw: String): String? = when {
        raw.isBlank() -> null
        raw == "none" -> com.quotebrowser.app.ui.NICK_COLOR_NONE
        else -> raw
    }

    /** Inverse of [loadRuleHex] for persisting nick rules. */
    private fun saveRuleHex(hex: String?): String = when {
        hex == null -> ""
        hex.isEmpty() -> "none"
        else -> hex
    }

    private fun persistNickRules(rules: List<com.quotebrowser.app.ui.NickColorRule>) {
        val arr = JSONArray()
        rules.forEach { r ->
            arr.put(
                JSONObject().put("nick", r.nick)
                    .put("brackets", saveRuleHex(r.bracketsHex))
                    .put("name", saveRuleHex(r.nameHex))
            )
        }
        prefs.edit().putString(KEY_NICK_RULES, arr.toString()).apply()
        _nickRules.value = rules
    }

    /** Returns false when the nick is blank/duplicate or the 10 slots are full. */
    fun addNickRule(
        nick: String,
        bracketsHex: String?,
        nameHex: String?
    ): Boolean {
        val clean = nick.trim()
        if (clean.isEmpty() || _nickRules.value.size >= MAX_NICK_RULES) return false
        if (_nickRules.value.any { it.nick.equals(clean, ignoreCase = true) }) return false
        persistNickRules(
            _nickRules.value + com.quotebrowser.app.ui.NickColorRule(
                clean, bracketsHex, nameHex
            )
        )
        return true
    }

    fun updateNickRule(
        index: Int,
        nick: String,
        bracketsHex: String?,
        nameHex: String?
    ): Boolean {
        val rules = _nickRules.value.toMutableList()
        if (index !in rules.indices) return false
        val clean = nick.trim()
        if (clean.isEmpty()) return false
        if (rules.indices.any { it != index && rules[it].nick.equals(clean, ignoreCase = true) }) {
            return false
        }
        rules[index] = com.quotebrowser.app.ui.NickColorRule(
            clean, bracketsHex, nameHex
        )
        persistNickRules(rules)
        return true
    }

    fun deleteNickRule(index: Int) {
        val rules = _nickRules.value.toMutableList()
        if (index !in rules.indices) return
        rules.removeAt(index)
        persistNickRules(rules)
    }

    // v1.63: nick color themes. The selected theme id (a preset id, a
    // custom id, or "app" = follow the app theme); defaults to the
    // Quote404 logo palette.
    private val _nickColorThemeId = MutableStateFlow(
        prefs.getString(
            KEY_NICK_COLOR_THEME,
            com.quotebrowser.app.ui.NICK_THEME_DEFAULT
        ) ?: com.quotebrowser.app.ui.NICK_THEME_DEFAULT
    )
    val nickColorThemeId: StateFlow<String> = _nickColorThemeId.asStateFlow()

    fun setNickColorThemeId(id: String) {
        prefs.edit().putString(KEY_NICK_COLOR_THEME, id).apply()
        _nickColorThemeId.value = id
    }

    // v1.88.32: per-card-theme nick palette overrides. Map of card theme
    // key -> nick color theme id; absent = "Use global". Persisted as one
    // JSON object so a single write stays atomic.
    private val _themeNickPalettes =
        MutableStateFlow(loadThemeNickPalettes())
    val themeNickPalettes: StateFlow<Map<String, String>> =
        _themeNickPalettes.asStateFlow()

    private fun loadThemeNickPalettes(): Map<String, String> {
        val raw = prefs.getString(KEY_THEME_NICK_PALETTES, null) ?: return emptyMap()
        return try {
            val o = JSONObject(raw)
            buildMap {
                val keys = o.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val v = o.optString(k, "")
                    if (k.isNotBlank() && v.isNotBlank()) put(k, v)
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Sets a card theme's nick palette override. [nickThemeId] is
     * "default" ([THEME_NICK_USE_GLOBAL]) to go back to the global
     * palette, otherwise a nick color theme id (preset or custom).
     */
    fun setThemeNickPalette(themeKey: String, nickThemeId: String) {
        val map = _themeNickPalettes.value.toMutableMap()
        if (nickThemeId == com.quotebrowser.app.ui.THEME_NICK_USE_GLOBAL) {
            map.remove(themeKey)
        } else {
            map[themeKey] = nickThemeId
        }
        prefs.edit()
            .putString(KEY_THEME_NICK_PALETTES, JSONObject(map as Map<*, *>).toString())
            .apply()
        _themeNickPalettes.value = map
    }

    // v1.88.32: per-chat-theme "remove nicks from message text" (AIM,
    // ICQ, MSN). Set of card theme keys where the speaker prefix is
    // stripped from each turn's body; missing = OFF (the default).
    private val _themeNickStrip = MutableStateFlow(loadThemeNickStrip())
    val themeNickStrip: StateFlow<Set<String>> = _themeNickStrip.asStateFlow()

    private fun loadThemeNickStrip(): Set<String> {
        val raw = prefs.getString(KEY_THEME_NICK_STRIP, null) ?: return emptySet()
        return try {
            val arr = JSONArray(raw)
            buildSet {
                for (i in 0 until arr.length()) {
                    val k = arr.optString(i)
                    if (k.isNotBlank()) add(k)
                }
            }
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun setThemeNickStrip(themeKey: String, strip: Boolean) {
        val set = _themeNickStrip.value.toMutableSet()
        if (strip) set.add(themeKey) else set.remove(themeKey)
        val arr = JSONArray()
        set.forEach { arr.put(it) }
        prefs.edit().putString(KEY_THEME_NICK_STRIP, arr.toString()).apply()
        _themeNickStrip.value = set
    }

    // v1.63: "color what they say" — paint a speaker's message text in
    // their own color too. Off by default.
    private val _nickColorMessage =
        MutableStateFlow(prefs.getBoolean(KEY_NICK_COLOR_MESSAGE, false))
    val nickColorMessage: StateFlow<Boolean> = _nickColorMessage.asStateFlow()

    fun setNickColorMessage(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NICK_COLOR_MESSAGE, enabled).apply()
        _nickColorMessage.value = enabled
    }

    // v1.63: user-created nick color themes, stored as JSON
    // [{id, name, colors:[AARRGGBB], action}].
    private val _customNickColorThemes =
        MutableStateFlow(loadCustomNickColorThemes())
    val customNickColorThemes: StateFlow<List<com.quotebrowser.app.ui.NickColorTheme>> =
        _customNickColorThemes.asStateFlow()

    private fun loadCustomNickColorThemes(): List<com.quotebrowser.app.ui.NickColorTheme> {
        val raw = prefs.getString(KEY_CUSTOM_NICK_THEMES, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                val colors = o.optJSONArray("colors")?.let { ca ->
                    List(ca.length()) { k ->
                        ca.optString(k).toLongOrNull(16)?.toInt()
                    }.mapNotNull { it }
                }.orEmpty()
                com.quotebrowser.app.ui.NickColorTheme(
                    id = o.getString("id"),
                    name = o.optString("name", "Custom").ifBlank { "Custom" },
                    nameColors = colors.take(
                        com.quotebrowser.app.ui.MAX_NICK_THEME_COLORS
                    ),
                    actionColor = o.optString("action").takeIf { it.isNotBlank() }
                        ?.toLongOrNull(16)?.toInt(),
                    isCustom = true
                )
            }.filter { it.id.isNotBlank() && it.nameColors.isNotEmpty() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistCustomNickColorThemes(themes: List<com.quotebrowser.app.ui.NickColorTheme>) {
        val arr = JSONArray()
        themes.forEach { t ->
            val o = JSONObject()
                .put("id", t.id)
                .put("name", t.name)
            val ca = JSONArray()
            t.nameColors.forEach { c -> ca.put("%08X".format(c)) }
            o.put("colors", ca)
            if (t.actionColor != null) o.put("action", "%08X".format(t.actionColor))
            arr.put(o)
        }
        prefs.edit().putString(KEY_CUSTOM_NICK_THEMES, arr.toString()).apply()
        _customNickColorThemes.value = themes
    }

    /** Adds a custom nick color theme; returns the new theme, or null when invalid. */
    fun addCustomNickColorTheme(
        name: String,
        colors: List<Int>
    ): com.quotebrowser.app.ui.NickColorTheme? {
        val clean = name.trim().ifEmpty { "Custom" }
        val sane = colors.take(com.quotebrowser.app.ui.MAX_NICK_THEME_COLORS)
        if (sane.isEmpty()) return null
        val theme = com.quotebrowser.app.ui.NickColorTheme(
            id = "custom:${java.util.UUID.randomUUID()}",
            name = clean,
            nameColors = sane,
            isCustom = true
        )
        persistCustomNickColorThemes(_customNickColorThemes.value + theme)
        return theme
    }

    /** Updates a custom theme's name/colors; false when the id isn't a custom theme. */
    fun updateCustomNickColorTheme(
        id: String,
        name: String,
        colors: List<Int>
    ): Boolean {
        val themes = _customNickColorThemes.value.toMutableList()
        val idx = themes.indexOfFirst { it.id == id }
        if (idx < 0) return false
        val clean = name.trim().ifEmpty { themes[idx].name }
        val sane = colors.take(com.quotebrowser.app.ui.MAX_NICK_THEME_COLORS)
        if (sane.isEmpty()) return false
        themes[idx] = themes[idx].copy(name = clean, nameColors = sane)
        persistCustomNickColorThemes(themes)
        return true
    }

    fun deleteCustomNickColorTheme(id: String) {
        val themes = _customNickColorThemes.value.filter { it.id != id }
        if (themes.size == _customNickColorThemes.value.size) return
        persistCustomNickColorThemes(themes)
        if (_nickColorThemeId.value == id) {
            setNickColorThemeId(com.quotebrowser.app.ui.NICK_THEME_DEFAULT)
        }
    }

    // Master toggle for <nick> coloring in quote text (default on).
    private val _nickColoringEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_NICK_ENABLED, true))
    val nickColoringEnabled: StateFlow<Boolean> = _nickColoringEnabled.asStateFlow()

    fun setNickColoringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NICK_ENABLED, enabled).apply()
        _nickColoringEnabled.value = enabled
    }

    // QuoteClean: color speaker names (<nick>, <@nick>, "Nick:", "[nick]")
    // with the theme's name palette (default on).
    private val _qcColorNames =
        MutableStateFlow(prefs.getBoolean(KEY_QC_COLOR_NAMES, true))
    val qcColorNames: StateFlow<Boolean> = _qcColorNames.asStateFlow()

    fun setQcColorNames(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QC_COLOR_NAMES, enabled).apply()
        _qcColorNames.value = enabled
    }

    // QuoteClean: color full-line *action* lines with the theme's action
    // color (default on).
    private val _qcColorActions =
        MutableStateFlow(prefs.getBoolean(KEY_QC_COLOR_ACTIONS, true))
    val qcColorActions: StateFlow<Boolean> = _qcColorActions.asStateFlow()

    fun setQcColorActions(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QC_COLOR_ACTIONS, enabled).apply()
        _qcColorActions.value = enabled
    }

    // QuoteClean: normalize "/" line separators into real line breaks
    // (default on). The "\\" IRC separator stays under its own toggle.
    private val _qcNormalizeBreaks =
        MutableStateFlow(prefs.getBoolean(KEY_QC_NORMALIZE_BREAKS, true))
    val qcNormalizeBreaks: StateFlow<Boolean> = _qcNormalizeBreaks.asStateFlow()

    fun setQcNormalizeBreaks(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QC_NORMALIZE_BREAKS, enabled).apply()
        _qcNormalizeBreaks.value = enabled
    }

    // QuoteClean: show [HH:MM]/[HH:MM:SS] timestamp prefixes (default off:
    // they are stripped).
    private val _qcShowTimestamps =
        MutableStateFlow(prefs.getBoolean(KEY_QC_SHOW_TIMESTAMPS, false))
    val qcShowTimestamps: StateFlow<Boolean> = _qcShowTimestamps.asStateFlow()

    fun setQcShowTimestamps(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QC_SHOW_TIMESTAMPS, enabled).apply()
        _qcShowTimestamps.value = enabled
    }

    // v1.86: QuoteClean per-category controls. Color speaker names on/off
    // is KEY_QC_COLOR_NAMES above; here the optional custom color for
    // speaker names (hex string, null/blank = theme default).
    private val _qcNameColor =
        MutableStateFlow(prefs.getString(KEY_QC_NAME_COLOR, null)?.takeIf { it.isNotBlank() })
    val qcNameColor: StateFlow<String?> = _qcNameColor.asStateFlow()

    fun setQcNameColor(hex: String?) {
        val v = hex?.takeIf { it.isNotBlank() }
        if (v == null) prefs.edit().remove(KEY_QC_NAME_COLOR).apply()
        else prefs.edit().putString(KEY_QC_NAME_COLOR, v).apply()
        _qcNameColor.value = v
    }

    // v1.86: color the words a speaker says (the message after any
    // recognized speaker label), default on.
    private val _qcColorMessage =
        MutableStateFlow(prefs.getBoolean(KEY_QC_COLOR_MESSAGE, true))
    val qcColorMessage: StateFlow<Boolean> = _qcColorMessage.asStateFlow()

    fun setQcColorMessage(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QC_COLOR_MESSAGE, enabled).apply()
        _qcColorMessage.value = enabled
    }

    // v1.86: optional custom color for speaker message text.
    private val _qcMessageColor =
        MutableStateFlow(prefs.getString(KEY_QC_MESSAGE_COLOR, null)?.takeIf { it.isNotBlank() })
    val qcMessageColor: StateFlow<String?> = _qcMessageColor.asStateFlow()

    fun setQcMessageColor(hex: String?) {
        val v = hex?.takeIf { it.isNotBlank() }
        if (v == null) prefs.edit().remove(KEY_QC_MESSAGE_COLOR).apply()
        else prefs.edit().putString(KEY_QC_MESSAGE_COLOR, v).apply()
        _qcMessageColor.value = v
    }

    // v1.86: optional custom color for action lines (overrides the theme's
    // action color when set).
    private val _qcActionColor =
        MutableStateFlow(prefs.getString(KEY_QC_ACTION_COLOR, null)?.takeIf { it.isNotBlank() })
    val qcActionColor: StateFlow<String?> = _qcActionColor.asStateFlow()

    fun setQcActionColor(hex: String?) {
        val v = hex?.takeIf { it.isNotBlank() }
        if (v == null) prefs.edit().remove(KEY_QC_ACTION_COLOR).apply()
        else prefs.edit().putString(KEY_QC_ACTION_COLOR, v).apply()
        _qcActionColor.value = v
    }

    // v1.86: optional custom color for [HH:MM] timestamp prefixes.
    private val _qcTimestampColor =
        MutableStateFlow(prefs.getString(KEY_QC_TIMESTAMP_COLOR, null)?.takeIf { it.isNotBlank() })
    val qcTimestampColor: StateFlow<String?> = _qcTimestampColor.asStateFlow()

    fun setQcTimestampColor(hex: String?) {
        val v = hex?.takeIf { it.isNotBlank() }
        if (v == null) prefs.edit().remove(KEY_QC_TIMESTAMP_COLOR).apply()
        else prefs.edit().putString(KEY_QC_TIMESTAMP_COLOR, v).apply()
        _qcTimestampColor.value = v
    }

    // v1.86: color "other" lines (anything that is not speech or action:
    // narration, notices, join/part lines, system lines), default on.
    private val _qcColorOther =
        MutableStateFlow(prefs.getBoolean(KEY_QC_COLOR_OTHER, true))
    val qcColorOther: StateFlow<Boolean> = _qcColorOther.asStateFlow()

    fun setQcColorOther(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QC_COLOR_OTHER, enabled).apply()
        _qcColorOther.value = enabled
    }

    // v1.86: optional custom color for "other" lines.
    private val _qcOtherColor =
        MutableStateFlow(prefs.getString(KEY_QC_OTHER_COLOR, null)?.takeIf { it.isNotBlank() })
    val qcOtherColor: StateFlow<String?> = _qcOtherColor.asStateFlow()

    fun setQcOtherColor(hex: String?) {
        val v = hex?.takeIf { it.isNotBlank() }
        if (v == null) prefs.edit().remove(KEY_QC_OTHER_COLOR).apply()
        else prefs.edit().putString(KEY_QC_OTHER_COLOR, v).apply()
        _qcOtherColor.value = v
    }

    // IRC line separators: turn " \" sequences into real line breaks when
    // rendering quote text (default on). Nicks like {Dev\Xen} have no
    // surrounding whitespace, so they are never affected.
    private val _ircLineBreaks =
        MutableStateFlow(prefs.getBoolean(KEY_IRC_LINE_BREAKS, true))
    val ircLineBreaks: StateFlow<Boolean> = _ircLineBreaks.asStateFlow()

    fun setIrcLineBreaks(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IRC_LINE_BREAKS, enabled).apply()
        _ircLineBreaks.value = enabled
    }

    // Dialogue line breaks: a <nick> that follows quoted text on the same
    // line starts a new line (default on).
    private val _dialogueLineBreaks =
        MutableStateFlow(prefs.getBoolean(KEY_DIALOGUE_BREAKS, true))
    val dialogueLineBreaks: StateFlow<Boolean> = _dialogueLineBreaks.asStateFlow()

    fun setDialogueLineBreaks(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DIALOGUE_BREAKS, enabled).apply()
        _dialogueLineBreaks.value = enabled
    }

    // v1.65 content maturity ceiling: the strictest maturity level shown
    // anywhere (browse, random, search, radio, widget, games, quote of the
    // day). Stored as the MaturityLevel enum name; default EVERYTHING
    // preserves the pre-v1.65 behavior of showing all quotes.
    private val _maturityCeiling =
        MutableStateFlow(MaturityLevel.fromStored(prefs.getString(KEY_MATURITY_CEILING, null) ?: "EVERYTHING"))
    val maturityCeiling: StateFlow<MaturityLevel> = _maturityCeiling.asStateFlow()

    fun setMaturityCeiling(level: MaturityLevel) {
        prefs.edit().putString(KEY_MATURITY_CEILING, level.name).apply()
        _maturityCeiling.value = level
    }

    /**
     * v1.65: true when a quote passes the global maturity ceiling.
     * Used by non-SQL entry points (on-this-day, detail swipe,
     * radio, widget) that fetch quotes outside the repository filters.
     */
    fun isMaturityAllowed(q: Quote): Boolean =
        maturityCeiling.value.matches(q.maturity, q.spicy)

    /**
     * v1.56 bundled-database refresh. For every bundled asset whose stored
     * revision is older than [BUNDLED_DATA_REVISION], merges the current
     * asset's quote rows into the on-device copy (Room's createFromAsset()
     * only copies on first install, so upgrades would otherwise keep stale
     * data). The merge is done with raw SQLite, never by replacing the
     * file, so the user's category tables (categories, quote_categories,
     * category_settings, category_auto_rules) in the same file are
     * preserved. Favorites, history, personal ratings, and notes live in
     * MetaDatabase/SharedPreferences and are untouched.
     *
     * Per file: ensure the `status` column exists, then — whenever the
     * asset carries the status column and its data fingerprint differs
     * from the on-device file's (count plus checksums over number, text,
     * url, rating, and queued-status, so text/rating/URL/status-only
     * refreshes are all caught) — ATTACH the asset and INSERT OR REPLACE
     * every quote row. The file's own quotes_ai/ad/au
     * triggers keep the FTS index in sync; FTS tables are never touched
     * directly. The old v1.50.x gate (asset user_version > device
     * user_version) is gone: it let devices whose file already reported
     * version 2 stamp the revision without merging, leaving stale data
     * (qdb.us stuck at 13,813 quotes) forever. Files that don't exist
     * on-device are skipped (fresh installs get the asset verbatim via
     * createFromAsset). Idempotent: the merge never deletes on-device
     * rows, and the revision stamp is written only after the merge
     * validates (every asset row represented on-device with identical
     * columns, and the post-merge count covers every asset row).
     *
     * v1.87: runs on a background thread (started from init) — never
     * call on the main thread.
     */
    private fun refreshBundledDatabases() {
        val files = try {
            appContext.assets.list(ASSET_DIR)
        } catch (e: Exception) {
            Log.w("QuoteBrowser", "bundled refresh: cannot list assets", e)
            return
        } ?: return
        for (name in files.filter { it.endsWith(".db", ignoreCase = true) }.sorted()) {
            val revKey = KEY_BUNDLED_DATA_REV + name
            if (prefs.getInt(revKey, 0) >= BUNDLED_DATA_REVISION) continue
            // v1.88.16 diagnostics: is the deferred merge doing heavy
            // work during slow launches?
            com.quotebrowser.app.StartupWatchdog.mark("bundled refresh merge start: $name")
            val mergeT0 = android.os.SystemClock.elapsedRealtime()
            try {
                refreshOneBundledDatabase(name)
            } catch (e: Exception) {
                Log.w("QuoteBrowser", "bundled refresh failed for $name", e)
                com.quotebrowser.app.StartupWatchdog.mark("bundled refresh merge FAILED: $name")
                continue // no stamp: retry on next launch
            }
            com.quotebrowser.app.StartupWatchdog.mark(
                "bundled refresh merge end: $name (${android.os.SystemClock.elapsedRealtime() - mergeT0}ms)"
            )
            prefs.edit().putInt(revKey, BUNDLED_DATA_REVISION).apply()
        }
    }

    /**
     * Data fingerprint of a quotes table: count plus checksums over the
     * columns the merge writes (number, text, url, rating, status, plus
     * the v1.65 content columns maturity/spicy/speakers when present), so
     * text-only, rating-only, URL-only, status-only, and rating-only
     * refreshes are all caught. Used only as a fast-path skip: a mismatch
     * always merges (INSERT OR REPLACE is idempotent), so a missed change
     * just costs a redundant merge, never stale data.
     */
    private fun quotesFingerprint(
        db: android.database.sqlite.SQLiteDatabase,
        tableExpr: String
    ): String {
        val cols = mutableSetOf<String>()
        // tableExpr may be "main.quotes" or an attached alias; PRAGMA needs
        // the bare table name.
        val tableName = tableExpr.substringAfter(".")
        db.rawQuery("PRAGMA table_info($tableName)", null).use { c ->
            val nameIdx = c.getColumnIndex("name")
            while (c.moveToNext()) {
                cols.add(c.getString(nameIdx))
            }
        }
        val contentSums = listOf("maturity", "spicy", "speakers")
            .filter { it in cols }
            .joinToString(", ") { "COALESCE(SUM($it),0)" }
        val extra = if (contentSums.isNotEmpty()) ", $contentSums" else ""
        // v1.74.4: 10 of 11 bundled assets have no status column — don't
        // reference it when it's absent.
        val queuedSum = if ("status" in cols)
            "COALESCE(SUM(CASE WHEN status='queued' THEN 1 ELSE 0 END),0)"
        else "0"
        db.rawQuery(
            "SELECT COUNT(*), COALESCE(SUM(number),0), " +
                "COALESCE(SUM(LENGTH(text)),0), COALESCE(SUM(LENGTH(url)),0), " +
                "COALESCE(SUM(rating),0), $queuedSum$extra " +
                "FROM $tableExpr",
            null
        ).use { c ->
            c.moveToFirst()
            val sb = StringBuilder()
            for (i in 0 until c.columnCount) {
                if (i > 0) sb.append(":")
                sb.append(c.getLong(i))
            }
            return sb.toString()
        }
    }

    // ------------------------------------------------------------------
    // v1.60: download-based database updates.
    //
    // Bundled quote data otherwise only changes when a new APK ships. This
    // lets the app pull newer .db files from a manifest URL the user
    // configures (Settings → Your data): downloads are SHA-256 verified
    // and merged with the same raw-SQLite machinery as the bundled
    // refresh, so categories, favorites, and user data are preserved.
    // Until the user sets a URL, the feature is dormant.
    // ------------------------------------------------------------------

    /** v1.60: per-database outcome of an update check. */
    data class DbUpdateOutcome(
        val assetName: String,
        val label: String,
        val status: UpdateStatus,
        /** e.g. "13,813 → 15,573 quotes", "already current", or an error. */
        val detail: String
    ) {
        enum class UpdateStatus { UP_TO_DATE, UPDATED, FAILED }
    }

    fun getDbUpdateUrl(): String = prefs.getString(KEY_DB_UPDATE_URL, "").orEmpty()

    fun setDbUpdateUrl(url: String) {
        prefs.edit().putString(KEY_DB_UPDATE_URL, url.trim()).apply()
    }

    /**
     * v1.60: downloads the manifest at the configured URL and merges every
     * database whose manifest revision is newer than the local one (the
     * greater of the last applied update stamp and [BUNDLED_DATA_REVISION]).
     * Call off the main thread. Never throws: per-database failures come
     * back as [DbUpdateOutcome]s.
     */
    fun checkForDatabaseUpdates(): List<DbUpdateOutcome> {
        fun failed(detail: String) = listOf(
            DbUpdateOutcome("", "", DbUpdateOutcome.UpdateStatus.FAILED, detail)
        )
        val manifestUrl = getDbUpdateUrl().trim()
        if (manifestUrl.isEmpty()) {
            return failed("No update URL set — add one in Settings → Your data.")
        }
        val manifestText = try {
            downloadText(manifestUrl, MAX_MANIFEST_BYTES)
        } catch (e: Exception) {
            return failed(
                "Couldn't download the update list: ${e.message ?: e.javaClass.simpleName}"
            )
        }
        val dbs = try {
            JSONObject(manifestText).optJSONObject("databases")
        } catch (e: Exception) {
            null
        } ?: return failed(
            "The update list isn't valid (expected a JSON \"databases\" section)."
        )
        val assets = try {
            appContext.assets.list(ASSET_DIR)
        } catch (e: Exception) {
            null
        } ?: emptyArray()
        val outcomes = mutableListOf<DbUpdateOutcome>()
        for (assetName in assets.filter { it.endsWith(".db", ignoreCase = true) }.sorted()) {
            val entry = dbs.optJSONObject(assetName) ?: continue
            val label = shortLabel("asset:$assetName")
            try {
                outcomes += applyDbUpdate(assetName, label, manifestUrl, entry)
            } catch (e: Exception) {
                Log.w("QuoteBrowser", "db update failed for $assetName", e)
                outcomes += DbUpdateOutcome(
                    assetName, label, DbUpdateOutcome.UpdateStatus.FAILED,
                    e.message ?: e.javaClass.simpleName
                )
            }
        }
        if (outcomes.isEmpty()) {
            outcomes += DbUpdateOutcome(
                "", "Update list", DbUpdateOutcome.UpdateStatus.UP_TO_DATE,
                "No entries matched this app's data files."
            )
        }
        return outcomes
    }

    private fun applyDbUpdate(
        assetName: String,
        label: String,
        manifestUrl: String,
        entry: JSONObject
    ): DbUpdateOutcome {
        val remoteRev = entry.optInt("revision", -1)
        if (remoteRev < 0) {
            return DbUpdateOutcome(
                assetName, label, DbUpdateOutcome.UpdateStatus.FAILED,
                "Update entry has no revision number."
            )
        }
        val localRev = maxOf(
            prefs.getInt(KEY_DB_UPDATE_REV + assetName, 0),
            BUNDLED_DATA_REVISION
        )
        if (remoteRev <= localRev) {
            return DbUpdateOutcome(
                assetName, label, DbUpdateOutcome.UpdateStatus.UP_TO_DATE,
                "Already current (revision $localRev)."
            )
        }
        val fileUrl = entry.optString("url", "").trim()
        if (fileUrl.isEmpty()) {
            return DbUpdateOutcome(
                assetName, label, DbUpdateOutcome.UpdateStatus.FAILED,
                "Update entry has no download URL."
            )
        }
        val resolved = try {
            java.net.URL(java.net.URL(manifestUrl), fileUrl).toString()
        } catch (e: Exception) {
            return DbUpdateOutcome(
                assetName, label, DbUpdateOutcome.UpdateStatus.FAILED,
                "Bad download URL: $fileUrl"
            )
        }
        val tmp = File.createTempFile("db_update_", ".db", appContext.cacheDir)
        try {
            downloadToFile(resolved, tmp, MAX_DB_DOWNLOAD_BYTES)
            val expected = entry.optString("sha256", "").trim().lowercase()
            if (expected.isNotEmpty() && sha256Of(tmp) != expected) {
                return DbUpdateOutcome(
                    assetName, label, DbUpdateOutcome.UpdateStatus.FAILED,
                    "Download failed verification (SHA-256 mismatch) — not applied."
                )
            }
            val deviceFile = appContext.getDatabasePath("bundled_$assetName")
            val before = quoteCountOnFile(deviceFile)
            mergeDbFileIntoDevice(assetName, tmp)
            val after = quoteCountOnFile(deviceFile)
            prefs.edit().putInt(KEY_DB_UPDATE_REV + assetName, remoteRev).apply()
            val fmt = java.text.NumberFormat.getIntegerInstance()
            return DbUpdateOutcome(
                assetName, label, DbUpdateOutcome.UpdateStatus.UPDATED,
                "${fmt.format(before)} → ${fmt.format(after)} quotes (revision $remoteRev)."
            )
        } finally {
            tmp.delete()
        }
    }

    private fun quoteCountOnFile(file: File): Long {
        if (!file.exists()) return 0
        var db: android.database.sqlite.SQLiteDatabase? = null
        return try {
            db = android.database.sqlite.SQLiteDatabase.openDatabase(
                file.absolutePath, null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            db.rawQuery("SELECT COUNT(*) FROM quotes", null).use { c ->
                c.moveToFirst()
                c.getLong(0)
            }
        } catch (e: Exception) {
            0
        } finally {
            try {
                db?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun downloadText(url: String, maxBytes: Int): String {
        val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("User-Agent", "QuoteBrowser-db-update")
        }
        try {
            val code = conn.responseCode
            if (code != java.net.HttpURLConnection.HTTP_OK) {
                throw java.io.IOException("HTTP $code")
            }
            val out = java.io.ByteArrayOutputStream()
            conn.inputStream.use { input ->
                val buf = ByteArray(8192)
                var total = 0
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    total += n
                    if (total > maxBytes) throw java.io.IOException("manifest too large")
                    out.write(buf, 0, n)
                }
            }
            return out.toString(Charsets.UTF_8.name())
        } finally {
            conn.disconnect()
        }
    }

    private fun downloadToFile(url: String, dest: File, maxBytes: Long) {
        val conn = (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 120_000
            setRequestProperty("User-Agent", "QuoteBrowser-db-update")
        }
        try {
            val code = conn.responseCode
            if (code != java.net.HttpURLConnection.HTTP_OK) {
                throw java.io.IOException("HTTP $code")
            }
            dest.outputStream().use { out ->
                conn.inputStream.use { input ->
                    val buf = ByteArray(32768)
                    var total = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        total += n
                        if (total > maxBytes) throw java.io.IOException("database too large")
                        out.write(buf, 0, n)
                    }
                }
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun sha256Of(file: File): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buf = ByteArray(32768)
            while (true) {
                val n = input.read(buf)
                if (n < 0) break
                md.update(buf, 0, n)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    /** Merge one bundled asset into its on-device copy (see [refreshBundledDatabases]). */
    private fun refreshOneBundledDatabase(assetName: String) {
        // The asset isn't a real file — stage it in cacheDir to ATTACH.
        val tmp = File.createTempFile("bundled_refresh_", ".db", appContext.cacheDir)
        try {
            appContext.assets.open("$ASSET_DIR/$assetName").use { input ->
                tmp.outputStream().use { out -> input.copyTo(out) }
            }
            mergeDbFileIntoDevice(assetName, tmp)
        } finally {
            tmp.delete()
        }
    }

    /**
     * v1.60: merges [srcFile] (a SQLite .db with a quotes table) into the
     * on-device copy of [assetName], preserving the user's category tables
     * (same guarantees as [refreshBundledDatabases]). Shared by the bundled
     * refresh and the download-based update check.
     */
    private fun mergeDbFileIntoDevice(assetName: String, srcFile: File) {
        // v1.88.6: serialize with the schema healer on the per-database
        // lock — the healer waits on this JVM monitor instead of losing
        // SQLite's 5s busy race against the merge's long write lock.
        synchronized(bundledDbLock(assetName)) {
            mergeDbFileIntoDeviceLocked(assetName, srcFile)
        }
    }

    private fun mergeDbFileIntoDeviceLocked(assetName: String, srcFile: File) {
        val deviceFile = appContext.getDatabasePath("bundled_$assetName")
        if (!deviceFile.exists()) return
        var db: android.database.sqlite.SQLiteDatabase? = null
        try {
            db = android.database.sqlite.SQLiteDatabase.openDatabase(
                deviceFile.absolutePath, null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
            )
            // v1.87: the refresh runs on a background thread now and may
            // overlap a Room writer (a category assignment, or a v1.60
            // download-merge). Wait up to 5s for a rival write lock
            // instead of failing SQLITE_BUSY and deferring to next launch.
            // (Room's bundled connections get the same pragma via
            // bundledBusyTimeoutCallback.)
            db.execSQL("PRAGMA busy_timeout = 5000")
            ensureStatusColumnOnFile(db)
            // v1.65: content columns must exist before the fingerprint and
            // the INSERT OR REPLACE below reference them.
            ensureContentColumnsOnFile(db)
            val deviceFingerprint = try {
                quotesFingerprint(db, "main.quotes")
            } catch (e: Exception) {
                // v1.74.6: a fingerprint failure used to return silently,
                // and the caller then stamped the revision as complete —
                // the merge never ran again for that revision. Throw
                // instead so no stamp is written and the merge is retried
                // on the next launch.
                Log.w("QuoteBrowser", "db merge: cannot fingerprint $assetName", e)
                throw IllegalStateException(
                    "db merge: cannot fingerprint $assetName", e
                )
            }
            run {
                var assetHasStatus = false
                val assetContentCols = mutableSetOf<String>()
                var assetCount = 0L
                var assetFingerprint = ""
                android.database.sqlite.SQLiteDatabase.openDatabase(
                    srcFile.absolutePath, null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                ).use { adb ->
                    adb.rawQuery("PRAGMA table_info(quotes)", null).use { c ->
                        val nameIdx = c.getColumnIndex("name")
                        while (c.moveToNext()) {
                            when (c.getString(nameIdx)) {
                                "status" -> assetHasStatus = true
                                // v1.65: all three content columns must be
                                // present before they ride along in the merge.
                                "maturity", "spicy", "speakers" ->
                                    assetContentCols += c.getString(nameIdx)
                            }
                        }
                    }
                    // v1.74.4: fingerprint regardless of the status column —
                    // 10 of 11 bundled assets don't have it.
                    assetFingerprint = quotesFingerprint(adb, "quotes")
                    assetCount = assetFingerprint.substringBefore(":").toLongOrNull() ?: 0L
                }
                // v1.65: all three content columns travel together in the
                // rated assets; require all three, not just maturity.
                val assetHasContent = assetContentCols.containsAll(
                    setOf("maturity", "spicy", "speakers")
                )
                // v1.74.4: merge whenever the asset's data differs from the
                // device copy — no longer requires the asset to carry a
                // status column (10 of 11 bundled assets don't have one).
                // INSERT OR REPLACE is idempotent: rows absent from the
                // asset (or the user's own queued imports) are never deleted.
                if (assetCount > 0 && assetFingerprint != deviceFingerprint) {
                    db.execSQL(
                        "ATTACH DATABASE ? AS refresh_src",
                        arrayOf<Any?>(srcFile.absolutePath)
                    )
                    try {
                        // v1.65: the content columns ride along when the
                        // asset carries them, so on-device rows get their
                        // maturity/spicy/speakers ratings (the device copy
                        // already has the columns via ensureContentColumnsOnFile).
                        // v1.74.4: assets without a status column get
                        // 'approved' (bundled data is all approved).
                        val contentCols = if (assetHasContent) ", maturity, spicy, speakers" else ""
                        val statusSel = if (assetHasStatus) "status" else "'approved'"
                        db.execSQL(
                            "INSERT OR REPLACE INTO main.quotes(number, rating, text, url, status$contentCols) " +
                                "SELECT number, rating, text, url, $statusSel$contentCols FROM refresh_src.quotes"
                        )
                        // Validate representation, not just the total count:
                        // every asset row must be present on-device with
                        // identical columns afterwards (IS is null-safe).
                        val contentCheck = if (assetHasContent) {
                            " AND d.maturity IS a.maturity AND d.spicy IS a.spicy" +
                                " AND d.speakers IS a.speakers"
                        } else ""
                        // v1.74.4: match the status literal used in the INSERT.
                        val statusCheck = if (assetHasStatus) "a.status" else "'approved'"
                        val unrepresented = db.rawQuery(
                            "SELECT COUNT(*) FROM refresh_src.quotes a WHERE NOT EXISTS (" +
                                "SELECT 1 FROM main.quotes d WHERE d.number = a.number " +
                                "AND d.rating IS a.rating AND d.text = a.text " +
                                "AND d.url IS a.url AND d.status = $statusCheck$contentCheck)",
                            null
                        ).use { c -> c.moveToFirst(); c.getLong(0) }
                        if (unrepresented > 0) {
                            throw IllegalStateException(
                                "db merge incomplete for $assetName: " +
                                    "$unrepresented asset rows not represented after merge"
                            )
                        }
                    } finally {
                        db.execSQL("DETACH DATABASE refresh_src")
                    }
                    try {
                        db.execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
                    } catch (_: Exception) {
                    }
                    // Validate before the caller stamps the revision: the
                    // device copy must cover every asset row afterwards.
                    val afterCount = db.rawQuery("SELECT COUNT(*) FROM main.quotes", null)
                        .use { c -> c.moveToFirst(); c.getLong(0) }
                    if (afterCount < assetCount) {
                        throw IllegalStateException(
                            "db merge incomplete for $assetName: " +
                                "$afterCount rows after merge, asset has $assetCount"
                        )
                    }
                    Log.i(
                        "QuoteBrowser",
                        "db merge: merged $assetName: device now $afterCount quotes"
                    )
                    // v1.74: diagnosable backfill — when the asset carried
                    // the content columns, this merge also refreshed the
                    // on-device maturity/spicy/speakers ratings (e.g. a
                    // device that stamped an older revision before the
                    // assets were rated kept all-zero content columns).
                    if (assetHasContent) {
                        Log.i(
                            "QuoteBrowser",
                            "db merge: $assetName content ratings backfilled " +
                                "(maturity/spicy/speakers merged from asset)"
                        )
                    }
                }
                // Leave the file at user_version 1 and let Room run its own
                // Migration(1,2) on open: Room validates the schema and
                // stamps the new identity hash itself afterwards. A raw
                // version bump to 2 here skips the migration and leaves a
                // stale identity_hash, which crashes Room on open with
                // "cannot verify the data integrity" (the v1.50.1 bug).
                db.version = 1
            }
        } finally {
            try {
                db?.close()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * Adds the `status` column (TEXT NOT NULL DEFAULT 'approved') to an
     * already-open raw database file when its quotes table lacks it.
     * No-op when the table or column doesn't exist / already exists.
     */
    private fun ensureStatusColumnOnFile(db: android.database.sqlite.SQLiteDatabase) {
        var hasQuotes = false
        db.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name='quotes' LIMIT 1", null
        ).use { c -> hasQuotes = c.moveToFirst() }
        if (!hasQuotes) return
        var hasStatus = false
        db.rawQuery("PRAGMA table_info(quotes)", null).use { c ->
            val nameIdx = c.getColumnIndex("name")
            while (c.moveToNext()) {
                if (c.getString(nameIdx) == "status") {
                    hasStatus = true
                    break
                }
            }
        }
        if (!hasStatus) {
            db.execSQL("ALTER TABLE quotes ADD COLUMN status TEXT NOT NULL DEFAULT 'approved'")
        }
    }

    /**
     * v1.65: ensures the content columns (`maturity`, `spicy`, `speakers`)
     * on a raw database file before Room validates the schema. Same
     * definitions the bundled assets carry; unrated files keep the
     * defaults (maturity 0 / not spicy / 0 speakers).
     */
    private fun ensureContentColumnsOnFile(db: android.database.sqlite.SQLiteDatabase) {
        db.rawQuery(
            "SELECT 1 FROM sqlite_master WHERE type='table' AND name='quotes' LIMIT 1", null
        ).use { c -> if (!c.moveToFirst()) return }
        val existing = mutableSetOf<String>()
        db.rawQuery("PRAGMA table_info(quotes)", null).use { c ->
            val nameIdx = c.getColumnIndex("name")
            while (c.moveToNext()) {
                existing.add(c.getString(nameIdx))
            }
        }
        if ("maturity" !in existing) {
            db.execSQL("ALTER TABLE quotes ADD COLUMN maturity INTEGER NOT NULL DEFAULT 0")
        }
        if ("spicy" !in existing) {
            db.execSQL("ALTER TABLE quotes ADD COLUMN spicy INTEGER NOT NULL DEFAULT 0")
        }
        if ("speakers" !in existing) {
            db.execSQL("ALTER TABLE quotes ADD COLUMN speakers INTEGER NOT NULL DEFAULT 0")
        }
    }

    /**
     * Ensures the `status` column on an imported (non-bundled) database
     * file before Room opens it, so pre-v1.50.1 imports and their data
     * keep working under the v2 schema. Fresh/empty files are left alone
     * (Room creates the table with the column itself).
     */
    private fun ensureImportedStatusColumn(file: File) {
        if (!file.exists()) return
        var db: android.database.sqlite.SQLiteDatabase? = null
        try {
            db = android.database.sqlite.SQLiteDatabase.openDatabase(
                file.absolutePath, null,
                android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
            )
            ensureStatusColumnOnFile(db)
            // v1.65: imported files also need the content columns before
            // Room validates the schema (unrated imports keep defaults).
            ensureContentColumnsOnFile(db)
        } catch (e: Exception) {
            Log.w("QuoteBrowser", "status column ensure failed for ${file.name}", e)
        } finally {
            try {
                db?.close()
            } catch (_: Exception) {
            }
        }
    }

    /**
     * v1.88.4: ensures the required Room columns exist on an on-device
     * bundled database file BEFORE Room opens (and validates) it.
     *
     * A previous release's failed pre-packaged validation leaves its
     * copied file behind; Room copies the asset only when the file is
     * absent, so every later open re-validates the stale broken schema
     * in onCreate and crashes — winning the race against the background
     * bundled-data refresh that would otherwise heal it. Raw SQLite
     * only, and the file is never deleted, so the user's category
     * tables and other on-device data are preserved. Idempotent and
     * metadata-only (ALTER TABLE ADD COLUMN), safe on the calling
     * thread; a failure just logs and lets Room proceed as before.
     */
    /**
     * v1.88.6: bundled-schema heal pass, run on the background heal/refresh
     * worker thread BEFORE the refresh merge starts (v1.88.7: moved off the
     * calling thread — running it synchronously in Application.onCreate
     * froze the launch splash). For every bundled database file already
     * on-device, ensures the Room-required columns (`status`, `maturity`,
     * `spicy`, `speakers`) exist. At this point no other thread in this
     * process touches these files (the refresh merge runs after this on
     * the same thread; the UI thread's on-demand pre-heal serializes
     * through the same per-database lock), so the heal cannot lose a lock
     * race — the schema Room validates is always the healed one.
     * Read-only fast path: files that already carry the columns are never
     * opened for write.
     */
    private fun healBundledSchemasSync() {
        val files = try {
            appContext.assets.list(ASSET_DIR)
        } catch (e: Exception) {
            Log.w("QuoteBrowser", "bundled sync heal: cannot list assets", e)
            return
        } ?: return
        for (name in files.filter { it.endsWith(".db", ignoreCase = true) }.sorted()) {
            val dbName = "bundled_$name"
            if (!appContext.getDatabasePath(dbName).exists()) continue
            try {
                if (!ensureBundledSchemaHealed(dbName)) {
                    Log.w("QuoteBrowser", "bundled sync heal: FAILED for $dbName")
                }
            } catch (t: Throwable) {
                Log.w("QuoteBrowser", "bundled sync heal: error for $dbName", t)
            }
        }
    }

    private fun ensureBundledSchemaHealed(dbName: String): Boolean {
        // Fast path: read-only column check, no lock. Healthy files
        // return here without ever blocking on the refresh merge.
        if (!bundledSchemaNeedsHeal(dbName)) return true
        // Slow path: the file is missing a required column. Take the
        // per-database lock shared with the refresh merge and heal
        // patiently — the only long writer is the refresh, which honors
        // the same lock, so the heal waits on the JVM monitor (no
        // timeout) instead of losing SQLite's 5s busy race.
        val assetName = dbName.removePrefix("bundled_")
        synchronized(bundledDbLock(assetName)) {
            if (!bundledSchemaNeedsHeal(dbName)) return true
            return healBundledSchemaLocked(dbName)
        }
    }

    /** Read-only check: true when the file lacks a Room-required column. */
    private fun bundledSchemaNeedsHeal(dbName: String): Boolean {
        val file = appContext.getDatabasePath(dbName)
        if (!file.exists()) return false // fresh copy path: createFromAsset handles it
        var db: android.database.sqlite.SQLiteDatabase? = null
        return try {
            db = android.database.sqlite.SQLiteDatabase.openDatabase(
                file.absolutePath, null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY
            )
            db.execSQL("PRAGMA busy_timeout = 5000")
            !hasBundledRequiredColumns(db)
        } catch (e: Exception) {
            // Cannot tell — let the locked heal attempt decide.
            Log.w("QuoteBrowser", "bundled schema check failed for $dbName", e)
            // v1.88.18: mirror to the startup log — the 2026-09-20 v1.88.17
            // log showed every healthy DB taking the 10s slow path, so the
            // fast check is either throwing or misreporting on-device.
            com.quotebrowser.app.StartupWatchdog.mark(
                "db open heal check failed: $dbName " +
                    "${e.javaClass.simpleName}: ${e.message}"
            )
            true
        } finally {
            try {
                db?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun hasBundledRequiredColumns(
        db: android.database.sqlite.SQLiteDatabase
    ): Boolean {
        val needed = setOf("status", "maturity", "spicy", "speakers")
        val seen = mutableSetOf<String>()
        db.rawQuery("PRAGMA table_info(quotes)", null).use { c ->
            val nameIdx = c.getColumnIndex("name")
            while (c.moveToNext()) {
                seen.add(c.getString(nameIdx))
            }
        }
        return needed.all { it in seen }
    }

    /**
     * v1.88.6: the actual heal, called with the per-database lock held.
     * Retries a few times with short sleeps: the refresh (the only long
     * writer) honors the same lock, so remaining contenders are brief
     * Room writes covered by the busy_timeout plus these retries.
     */
    private fun healBundledSchemaLocked(dbName: String): Boolean {
        val file = appContext.getDatabasePath(dbName)
        var lastError: Exception? = null
        for (attempt in 1..4) {
            var db: android.database.sqlite.SQLiteDatabase? = null
            try {
                db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    file.absolutePath, null,
                    android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                )
                db.execSQL("PRAGMA busy_timeout = 5000")
                ensureStatusColumnOnFile(db)
                ensureContentColumnsOnFile(db)
                if (hasBundledRequiredColumns(db)) {
                    if (attempt > 1) {
                        Log.i(
                            "QuoteBrowser",
                            "bundled schema heal succeeded for $dbName " +
                                "on attempt $attempt"
                        )
                    }
                    return true
                }
                // v1.88.18: the ALTERs ran without throwing but the columns
                // are still missing — deterministic, retrying cannot help.
                lastError = IllegalStateException("required column still missing")
                break
            } catch (e: Exception) {
                lastError = e
                Log.w(
                    "QuoteBrowser",
                    "bundled schema heal attempt $attempt failed for $dbName", e
                )
                // v1.88.18: mirror to the startup log — the 2026-09-20
                // v1.88.17 log proved the heal burns 10s failing on healthy
                // DBs (Room opens them fine 30ms later); the next log must
                // show WHY the attempts fail.
                com.quotebrowser.app.StartupWatchdog.mark(
                    "db open heal attempt failed: $dbName attempt=$attempt " +
                        "${e.javaClass.simpleName}: ${e.message}"
                )
                // v1.88.18: only retry transient lock contention. Anything
                // else (not-a-database, no-such-table, etc.) fails the same
                // way on every attempt — fail fast instead of sleeping.
                val msg = e.message.orEmpty()
                val busy = e is android.database.sqlite.SQLiteDatabaseLockedException ||
                    msg.contains("busy", ignoreCase = true) ||
                    msg.contains("locked", ignoreCase = true)
                if (!busy) break
            } finally {
                try {
                    db?.close()
                } catch (_: Exception) {
                }
            }
            // v1.88.18: was Thread.sleep(1000L * attempt) — 1+2+3+4 = 10s
            // of pure sleep on the startup path for every bundled DB, even
            // though the heal always failed and Room opened the DB fine
            // without it. The per-database JVM lock already serializes
            // against the refresh merge (the race v1.88.6 fixed), so a
            // short transient-busy backoff is plenty.
            if (attempt < 4) {
                try {
                    Thread.sleep(250L)
                } catch (_: InterruptedException) {
                    Thread.currentThread().interrupt()
                    break
                }
            }
        }
        Log.w("QuoteBrowser", "bundled schema heal FAILED for $dbName", lastError)
        return false
    }

    /**
     * Delete a database file plus its WAL/SHM sidecars, best-effort.
     * Used by the v1.88.10 last-resort rebuild so a broken copy can
     * never be left behind to poison the next launch.
     */
    private fun clearDbFiles(dbFile: File) {
        try { dbFile.delete() } catch (_: Exception) {}
        try { File(dbFile.absolutePath + "-wal").delete() } catch (_: Exception) {}
        try { File(dbFile.absolutePath + "-shm").delete() } catch (_: Exception) {}
    }

    /**
     * v1.88.10: last-resort recovery for a bundled database file that fails
     * Room's pre-packaged validation even after the heal passes (a corrupt
     * or otherwise unhealable leftover — e.g. a version-0 copy from the
     * v1.88.x era whose schema still lacks `status` after every heal
     * attempt). The APK asset is verified good, so: back the stale file
     * up, delete it, and let Room copy the asset fresh (validation then
     * passes by construction). Afterwards the user's tables (categories
     * etc. — everything except `quotes`, which comes from the asset) are
     * restored from the backup on a best-effort basis. Throws only if the
     * packaged asset itself cannot be opened (two attempts); the broken
     * file is always cleared first, so the next launch starts clean rather
     * than re-validating a stale copy.
     */
    private fun rebuildBundledFromAsset(dbName: String, assetPath: String): QuoteDatabase {
        val dbFile = appContext.getDatabasePath(dbName)
        val bakFile = File(
            dbFile.parent,
            "${dbFile.name}.corrupt-${System.currentTimeMillis()}.bak"
        )
        try {
            if (dbFile.exists()) dbFile.copyTo(bakFile, overwrite = true)
            Log.w("QuoteBrowser", "bundled db $dbName backed up to ${bakFile.name}")
        } catch (e: Exception) {
            Log.w("QuoteBrowser", "bundled db $dbName backup failed", e)
        }
        try { dbFile.delete() } catch (_: Exception) {}
        try { File(dbFile.absolutePath + "-wal").delete() } catch (_: Exception) {}
        try { File(dbFile.absolutePath + "-shm").delete() } catch (_: Exception) {}
        val fresh = Room.databaseBuilder(
            appContext, QuoteDatabase::class.java,
            dbName
        )
            .createFromAsset(assetPath)
            .addMigrations(QUOTES_MIGRATION_1_2, QUOTES_MIGRATION_2_3)
            .addCallback(bundledBusyTimeoutCallback)
            .build()
        try {
            fresh.openHelper.writableDatabase
        } catch (e: Exception) {
            // The APK asset is verified at build time, so this is not
            // expected — but if the fresh copy still fails, never leave
            // the broken copy behind to poison the next launch: close
            // the failed handle, clear the file, and try once more from
            // the asset. If the retry also fails, clear the file again
            // so the next launch starts clean, and return the instance
            // so the real error surfaces at the query site.
            Log.e("QuoteBrowser", "bundled asset copy failed for $dbName, retrying once", e)
            try { fresh.close() } catch (_: Exception) {}
            clearDbFiles(dbFile)
            val retry = Room.databaseBuilder(
                appContext, QuoteDatabase::class.java,
                dbName
            )
                .createFromAsset(assetPath)
                .addMigrations(QUOTES_MIGRATION_1_2, QUOTES_MIGRATION_2_3)
                .addCallback(bundledBusyTimeoutCallback)
                .build()
            try {
                retry.openHelper.writableDatabase
            } catch (e2: Exception) {
                // Both copies from the APK asset failed — the asset itself
                // is broken. The failed copy was cleared above; throw the
                // real error so it surfaces at the query site, and the next
                // launch starts clean instead of re-validating a stale file.
                Log.e("QuoteBrowser", "bundled asset retry failed for $dbName", e2)
                try { retry.close() } catch (_: Exception) {}
                clearDbFiles(dbFile)
                throw IllegalStateException(
                    "bundled db $dbName could not be opened from the packaged asset", e2
                )
            }
            if (bakFile.exists()) restoreUserTablesFromBackup(retry, bakFile, dbName)
            return retry
        }
        if (bakFile.exists()) restoreUserTablesFromBackup(fresh, bakFile, dbName)
        return fresh
    }

    /**
     * v1.88.10: best-effort restore of the user's own tables (categories,
     * quote_categories, category_settings, ...) from a backed-up stale
     * bundled database into a fresh asset copy. `quotes` and its FTS
     * tables always come from the asset and are never restored; Room/FTS
     * internals are skipped. Every table is attempted independently — a
     * corrupt backup simply yields fewer restored tables, never a crash.
     */
    private fun restoreUserTablesFromBackup(
        fresh: QuoteDatabase,
        bakFile: File,
        dbName: String
    ) {
        var attached = false
        try {
            val sdb = fresh.openHelper.writableDatabase
            sdb.execSQL("ATTACH DATABASE ? AS bak", arrayOf<Any?>(bakFile.absolutePath))
            attached = true
            val tables = mutableListOf<Pair<String, String?>>()
            try {
                sdb.query(
                    SimpleSQLiteQuery(
                        "SELECT name, sql FROM bak.sqlite_master " +
                            "WHERE type='table' AND sql IS NOT NULL"
                    )
                ).use { c ->
                    val nameIdx = c.getColumnIndex("name")
                    val sqlIdx = c.getColumnIndex("sql")
                    while (c.moveToNext()) {
                        tables += c.getString(nameIdx) to c.getString(sqlIdx)
                    }
                }
            } catch (e: Exception) {
                Log.w("QuoteBrowser", "bundled db $dbName: cannot list backup tables", e)
            }
            var restored = 0
            for ((name, sql) in tables) {
                if (name == "quotes" || name.startsWith("quotes_fts") ||
                    name.startsWith("sqlite_") || name.startsWith("room_") ||
                    name == "android_metadata"
                ) continue
                try {
                    // Recreate the table from the backup's own DDL, then copy rows.
                    val ddl = (sql ?: continue).replaceFirst("CREATE TABLE", "CREATE TABLE IF NOT EXISTS")
                    sdb.execSQL(ddl)
                    sdb.compileStatement(
                        "INSERT OR IGNORE INTO main.\"$name\" SELECT * FROM bak.\"$name\""
                    ).executeInsert()
                    restored++
                    Log.i("QuoteBrowser", "bundled db $dbName: restored table $name")
                } catch (e: Exception) {
                    Log.w("QuoteBrowser", "bundled db $dbName: cannot restore table $name", e)
                }
            }
            Log.i("QuoteBrowser", "bundled db $dbName: restored $restored user tables from backup")
        } catch (e: Exception) {
            Log.w("QuoteBrowser", "bundled db $dbName: backup restore failed", e)
        } finally {
            if (attached) {
                try {
                    fresh.openHelper.writableDatabase.execSQL("DETACH DATABASE bak")
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Close and drop the cached Room instance for a database id (v1.83:
     * used when a plugin quote pack is uninstalled so no stale handle
     * survives the pack's removal).
     */
    fun evictQuoteDatabase(id: String) {
        val db = synchronized(quoteDbCache) { quoteDbCache.remove(id) }
        if (db != null) {
            try {
                db.close()
            } catch (_: Exception) {
            }
        }
    }

    /** Open (or reuse the cached) Room instance for a database id. */
    fun getQuoteDatabase(id: String): QuoteDatabase {
        // v1.83: plugin quote packs live in their own pack dir. Resolve
        // and build the pack's database OUTSIDE the cache lock — the build
        // can take a while and must not hold it.
        if (id.startsWith(PluginManager.DB_ID_PREFIX)) {
            val pluginId = id.removePrefix(PluginManager.DB_ID_PREFIX)
            val dbFile = try {
                PluginManager.ensureQuotePackDb(appContext, pluginId)
            } catch (e: Exception) {
                throw IllegalArgumentException("Quote pack unavailable: $pluginId")
            }
            return synchronized(quoteDbCache) {
                quoteDbCache.getOrPut(id) {
                    Room.databaseBuilder(
                        appContext, QuoteDatabase::class.java, dbFile.absolutePath
                    )
                        .addMigrations(QUOTES_MIGRATION_1_2, QUOTES_MIGRATION_2_3)
                        .build()
                }
            }
        }
        // v1.88.11: the bundled-asset branch can run the v1.88.10
        // last-resort rebuild (backup + delete + multi-MB asset copy +
        // table restore). That heavy disk I/O must never run under the
        // global cache monitor — on 2026-09-20 it ran on the main
        // thread inside synchronized(quoteDbCache) long enough to trip
        // the system ANR. Build outside the lock, then publish; on a
        // race the loser closes its duplicate instance.
        if (id.startsWith("asset:")) {
            // v1.88.18: diagnostic — a main-thread open that becomes the
            // opener (or runBlocks on an in-flight open) trips the ANR
            // dialog; the 2026-09-20 v1.88.17 log showed one Wait tap.
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                com.quotebrowser.app.StartupWatchdog.mark("db open ON MAIN THREAD: $id")
            }
            synchronized(quoteDbCache) { quoteDbCache[id] }?.let { return it }
            // v1.88.17: share in-flight opens. If another thread is already
            // opening this DB, wait for it instead of starting a duplicate
            // full open (each duplicate re-takes Room's per-DB ProcessLock
            // and serializes behind the in-progress open — the 10s/20s/30s
            // queueing seen in the 2026-09-20 startup log).
            var weAreOpener = false
            val deferred = synchronized(inFlightOpensLock) {
                val existing = inFlightBundledOpens[id]
                if (existing != null) {
                    existing
                } else {
                    weAreOpener = true
                    val fresh = kotlinx.coroutines.CompletableDeferred<QuoteDatabase>()
                    inFlightBundledOpens[id] = fresh
                    fresh
                }
            }
            if (!weAreOpener) {
                // Another thread is opening this DB — share its result.
                // getQuoteDatabase is blocking, so bridge the suspend await.
                com.quotebrowser.app.StartupWatchdog.mark(
                    "db open shared in-flight: $id"
                )
                return kotlinx.coroutines.runBlocking { deferred.await() }
            }
            try {
                val db = openAssetDatabase(id)
                synchronized(quoteDbCache) {
                    val existing = quoteDbCache[id]
                    if (existing != null) {
                        try { db.close() } catch (_: Exception) {}
                        deferred.complete(existing)
                        return existing
                    }
                    quoteDbCache[id] = db
                    deferred.complete(db)
                    return db
                }
            } catch (e: Throwable) {
                deferred.completeExceptionally(e)
                throw e
            } finally {
                synchronized(inFlightOpensLock) {
                    // Remove only if it's still ours (a waiter can't remove).
                    if (inFlightBundledOpens[id] === deferred) {
                        inFlightBundledOpens.remove(id)
                    }
                }
            }
        }
        return synchronized(quoteDbCache) {
            quoteDbCache.getOrPut(id) {
                val file = File(appContext.filesDir, "databases/$id.db")
                // Pre-v1.50.1 imports lack the status column; add it before
                // Room validates the schema.
                ensureImportedStatusColumn(file)
                Room.databaseBuilder(appContext, QuoteDatabase::class.java, file.absolutePath)
                    .addMigrations(QUOTES_MIGRATION_1_2, QUOTES_MIGRATION_2_3)
                    .build()
            }
        }
    }

    /**
     * Opens the bundled-asset database: raw-SQL pre-heal, Room build,
     * eager validation, re-heal on schema failure, and as a last
     * resort a backup + rebuild-from-asset with user-table restore
     * (v1.88.10). Heavy disk I/O — never call under the quoteDbCache
     * monitor and never on the main thread. Per-database work stays
     * serialized on bundledDbLock, shared with the startup healer
     * and the background refresh merge.
     */
    private fun openAssetDatabase(id: String): QuoteDatabase {
        val assetPath = "$ASSET_DIR/${id.removePrefix("asset:")}"
        val dbName = "bundled_${id.removePrefix("asset:")}"
        // v1.88.16: startup diagnostics — time every bundled open.
        val openT0 = android.os.SystemClock.elapsedRealtime()
        com.quotebrowser.app.StartupWatchdog.mark("db open start: $dbName")
        try {
        // v1.88.4: a previous release's failed pre-packaged
        // validation leaves its copied file behind, and Room
        // re-validates that stale file on every open — before
        // the background bundled-data refresh can heal it.
        // Ensure the required columns with raw SQLite BEFORE
        // Room validates, mirroring ensureImportedStatusColumn.
        // v1.88.5: v1.88.4's pre-heal wasn't enough — the 10:50
        // crash showed the stale schema surviving it. Eagerly
        // open the DB after build to force Room's validation
        // now (not on first query), and on
        // "Pre-packaged database has an invalid schema" close,
        // re-heal (clearing stale WAL/SHM first), and rebuild
        // once before giving up. The file is never deleted, so
        // user category tables are preserved.
        // v1.88.6: root cause found — the 11:06 crash was a lock
        // race: the refresh thread held a SQLite write lock on
        // the stale file during its multi-second merge while
        // the pre-heal waited only 5s (busy_timeout), failed
        // SQLITE_BUSY, and Room validated the unhealed schema;
        // the crash then killed the process before the refresh
        // ever finished (self-perpetuating). Fixed with a
        // heal pass on the background worker thread before the
        // refresh merge (no contender — v1.88.7 moved it off
        // the main thread, which had frozen the launch splash)
        // plus a per-database JVM lock shared by the healer
        // and the refresh merge, so any residual overlap waits
        // instead of racing.
        val healOk = ensureBundledSchemaHealed(dbName)
        // v1.88.17: fine-grained markers to pinpoint the ~10s open cost
        // seen in the 2026-09-20 startup log.
        // v1.88.18: also record the heal outcome — the v1.88.17 log proved
        // the heal fails on healthy DBs and Room opens them fine without
        // it, so a "healed=false" here is expected, not alarming.
        com.quotebrowser.app.StartupWatchdog.mark(
            "db open heal done: $dbName " +
                "(${(android.os.SystemClock.elapsedRealtime() - openT0)}ms, healed=$healOk)"
        )
        fun buildBundled(): QuoteDatabase {
            return Room.databaseBuilder(
                appContext, QuoteDatabase::class.java,
                dbName
            )
                .createFromAsset(assetPath)
                .addMigrations(QUOTES_MIGRATION_1_2, QUOTES_MIGRATION_2_3)
                .addCallback(bundledBusyTimeoutCallback)
                .build()
        }
        var db = buildBundled()
        com.quotebrowser.app.StartupWatchdog.mark(
            "db open built: $dbName " +
                "(${(android.os.SystemClock.elapsedRealtime() - openT0)}ms)"
        )
        try {
            db.openHelper.writableDatabase
            com.quotebrowser.app.StartupWatchdog.mark(
                "db open validated: $dbName " +
                    "(${(android.os.SystemClock.elapsedRealtime() - openT0)}ms)"
            )
        } catch (e: IllegalStateException) {
            if (e.message?.contains("Pre-packaged database has an invalid schema") == true) {
                Log.w("QuoteBrowser", "bundled db $dbName failed validation, re-healing", e)
                try { db.close() } catch (_: Exception) {}
                // Clear stale WAL/SHM that can pin the old schema view.
                try {
                    val f = appContext.getDatabasePath(dbName)
                    File(f.absolutePath + "-wal").delete()
                    File(f.absolutePath + "-shm").delete()
                } catch (_: Exception) {}
                // v1.88.10: serialize the whole re-heal + rebuild
                // with the refresh merge on the per-database
                // lock — the merge can otherwise be mid-write
                // on this file while the rebuild deletes it.
                // (synchronized is reentrant, and
                // ensureBundledSchemaHealed takes the same
                // lock internally.)
                synchronized(bundledDbLock(dbName.removePrefix("bundled_"))) {
                    ensureBundledSchemaHealed(dbName)
                    db = buildBundled()
                    try {
                        db.openHelper.writableDatabase
                    } catch (e2: IllegalStateException) {
                        if (e2.message?.contains("Pre-packaged database has an invalid schema") == true) {
                            // v1.88.10: the stale file resists healing
                            // (seen 2026-09-20: a version-0 leftover
                            // copy from the v1.88.x era whose schema
                            // still lacks `status` after 8 heal
                            // attempts — corrupt or otherwise
                            // unhealable via ALTER). The APK asset is
                            // verified good, so back the stale file
                            // up, delete it, re-copy from the asset,
                            // and restore the user's tables from the
                            // backup. Last resort for an unhealable
                            // stale copy; the packaged asset is
                            // verified good.
                            Log.w("QuoteBrowser", "bundled db $dbName unhealable, rebuilding from asset", e2)
                            try { db.close() } catch (_: Exception) {}
                            db = rebuildBundledFromAsset(dbName, assetPath)
                        } else {
                            throw e2
                        }
                    }
                }
            } else {
                throw e
            }
        }
        val openMs = android.os.SystemClock.elapsedRealtime() - openT0
        com.quotebrowser.app.StartupWatchdog.mark("db open end: $dbName (${openMs}ms)")
        return db
        } catch (t: Throwable) {
            val openMs = android.os.SystemClock.elapsedRealtime() - openT0
            com.quotebrowser.app.StartupWatchdog.mark("db open FAILED: $dbName (${openMs}ms) ${t.javaClass.simpleName}")
            throw t
        }
    }

    fun switchDatabase(id: String) {
        prefs.edit().putString(KEY_CURRENT_DB, id).apply()
        _currentDbId.value = id
    }

    /**
     * Imports the bundled prebuilt category tags into the bundled Bash.org
     * database the first time it is seen (tracked per-database by a version
     * table). Safe to call on every launch; user edits are never overwritten.
     *
     * The seed maps Bash.org quote numbers, so it is only applied to the
     * bundled Bash.org database — never to imported databases, whose quote
     * numbers would collide by coincidence.
     */
    suspend fun seedCategories(id: String) {
        if (id != "asset:bash_org.db") return
        try {
            val store = CategoryStore(getQuoteDatabase(id))
            if (store.seedVersion() >= CATEGORY_SEED_VERSION) return
            val json = appContext.assets.open("category_seed.json")
                .bufferedReader().use { it.readText() }
            store.seedFromJson(json, CATEGORY_SEED_VERSION)
        } catch (e: Exception) {
            // Seeding is best-effort; categories still work empty.
        }
    }

    /** Enables/disables the daily 9am Quote-of-the-Day notification. */
    fun setQotdNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_QOTD_NOTIF, enabled).apply()
        _qotdNotifEnabled.value = enabled
        if (enabled) {
            scheduleQotdWork()
        } else {
            WorkManager.getInstance(appContext).cancelUniqueWork(QOTD_WORK)
        }
    }

    /** Schedules a daily worker whose first run lands on the next QotD time. */
    private fun scheduleQotdWork() {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(_qotdHour.value, _qotdMinute.value)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delayMs = Duration.between(now, next).toMillis()
        val request = PeriodicWorkRequestBuilder<QotdWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag(QOTD_WORK)
            .build()
        WorkManager.getInstance(appContext)
            .enqueueUniquePeriodicWork(QOTD_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    // ------------------------------------------------------------------
    // Quote-of-the-Day delivery time (default 9:00am).
    // ------------------------------------------------------------------

    private val _qotdHour =
        kotlinx.coroutines.flow.MutableStateFlow(prefs.getInt(KEY_QOTD_HOUR, 9))
    val qotdHour: kotlinx.coroutines.flow.StateFlow<Int> = _qotdHour.asStateFlow()

    private val _qotdMinute =
        kotlinx.coroutines.flow.MutableStateFlow(prefs.getInt(KEY_QOTD_MINUTE, 0))
    val qotdMinute: kotlinx.coroutines.flow.StateFlow<Int> = _qotdMinute.asStateFlow()

    /**
     * Sets the daily QotD delivery time and reschedules the worker when
     * notifications are on.
     */
    fun setQotdTime(hour: Int, minute: Int) {
        val h = hour.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        prefs.edit().putInt(KEY_QOTD_HOUR, h).putInt(KEY_QOTD_MINUTE, m).apply()
        _qotdHour.value = h
        _qotdMinute.value = m
        if (qotdNotificationsEnabled.value) scheduleQotdWork()
    }

    // ------------------------------------------------------------------
    // Quote of the Day card: minimized state (persisted).
    // ------------------------------------------------------------------

    private val _qotdMinimized =
        MutableStateFlow(prefs.getBoolean(KEY_QOTD_MINIMIZED, false))
    val qotdMinimized: StateFlow<Boolean> = _qotdMinimized.asStateFlow()

    fun setQotdMinimized(minimized: Boolean) {
        prefs.edit().putBoolean(KEY_QOTD_MINIMIZED, minimized).apply()
        _qotdMinimized.value = minimized
    }

    // ------------------------------------------------------------------
    // Quote-length filter presets: word thresholds (Settings).
    // ------------------------------------------------------------------

    private val _lengthLongerWords =
        MutableStateFlow(prefs.getInt(KEY_LENGTH_LONGER_WORDS, 100))
    val lengthLongerWords: StateFlow<Int> = _lengthLongerWords.asStateFlow()

    private val _lengthShorterWords =
        MutableStateFlow(prefs.getInt(KEY_LENGTH_SHORTER_WORDS, 30))
    val lengthShorterWords: StateFlow<Int> = _lengthShorterWords.asStateFlow()

    fun setLengthLongerWords(words: Int) {
        val w = words.coerceIn(1, 10000)
        prefs.edit().putInt(KEY_LENGTH_LONGER_WORDS, w).apply()
        _lengthLongerWords.value = w
    }

    fun setLengthShorterWords(words: Int) {
        val w = words.coerceIn(1, 10000)
        prefs.edit().putInt(KEY_LENGTH_SHORTER_WORDS, w).apply()
        _lengthShorterWords.value = w
    }

    // ------------------------------------------------------------------
    // Read-aloud: a different TTS voice per speaker nick (default on —
    // v1.85: Clint explicitly requested the deterministic per-nickname
    // voice assignment, so multi-speaker quotes separate speakers by
    // voice out of the box; the toggle still opts out).
    // ------------------------------------------------------------------

    private val _ttsPerNickVoices =
        kotlinx.coroutines.flow.MutableStateFlow(prefs.getBoolean(KEY_TTS_PER_NICK, true))
    val ttsPerNickVoices: kotlinx.coroutines.flow.StateFlow<Boolean> =
        _ttsPerNickVoices.asStateFlow()

    fun setTtsPerNickVoices(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TTS_PER_NICK, enabled).apply()
        _ttsPerNickVoices.value = enabled
    }

    /** Best-effort display name of a SAF Uri (used to name imported databases). */
    fun queryDisplayName(uri: Uri): String? = try {
        appContext.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { c -> if (c.moveToFirst()) c.getString(0) else null }
    } catch (e: Exception) {
        null
    }

    /**
     * Import a .db chosen through the Storage Access Framework picker.
     * Validates that it contains quotes(number, rating, text, url), copies it
     * into internal storage, and registers it. Returns the new DbInfo, or a
     * failure with a human-readable message.
     */
    suspend fun importDatabase(displayName: String?, uri: Uri): Result<DbInfo> =
        withContext(Dispatchers.IO) {
            try {
                var base = sanitizeFileName(displayName?.substringBeforeLast('.') ?: "imported")
                // Never collide with the built-in My Quotes database.
                if (base == MY_QUOTES_ID) base = "${MY_QUOTES_ID}_import"
                val dir = File(appContext.filesDir, "databases").apply { mkdirs() }
                var file = File(dir, "$base.db")
                var n = 1
                while (file.exists()) {
                    n += 1
                    file = File(dir, "$base$n.db")
                }
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext Result.failure(
                    IllegalArgumentException("Could not read the selected file.")
                )

                val problems = validateQuoteDb(file)
                if (problems != null) {
                    file.delete()
                    return@withContext Result.failure(IllegalArgumentException(problems))
                }
                // v1.47 QuoteClean: run the same text checks over a sample of
                // the newly imported quotes (slash separators, timestamps,
                // speaker/action formats) so the database renders and reads
                // aloud correctly through the QuoteClean pipeline.
                runQuoteCleanImportChecks(file)

                val id = file.nameWithoutExtension
                metaDb.metaDao().addImportedDb(
                    ImportedDb(id = id, label = prettifyLabel(id), fileName = file.name)
                )
                // Drop any stale cached instance so the fresh file is opened.
                synchronized(quoteDbCache) { quoteDbCache.remove(id)?.close() }
                // v1.47 auto-categorize: tag the newly imported quotes with
                // every auto-rule phrase (user phrases + the built-in
                // "Offensive" list). Never fails the import.
                try {
                    CategoryStore(getQuoteDatabase(id)).applyAutoRules()
                } catch (e: Exception) {
                    Log.w("AutoRules", "import-time tagging failed for $id", e)
                }
                // v1.48 search alerts: evaluate alert-enabled saved
                // searches against the new database and notify for
                // matches. Never fails the import.
                try {
                    checkSearchAlerts(appContext, id)
                } catch (e: Exception) {
                    Log.w("SearchAlerts", "alert check failed for $id", e)
                }
                // v1.51 "Pick up where you left off": an import replaces a
                // database's contents, so drop any saved Browse scroll
                // position for this id — it would point at the wrong place.
                // Fresh imports get a unique file name, making this a no-op
                // for brand-new ids, but it keeps the store consistent for
                // any path that overwrites an existing database in place.
                try {
                    browsePositionStore.clearPosition(id)
                } catch (e: Exception) {
                    Log.w("BrowsePosition", "position clear failed for $id", e)
                }
                Result.success(DbInfo(id = id, label = prettifyLabel(id)))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * v1.47 QuoteClean import check: samples the newly imported database
     * and runs every quote through the same display/read-aloud cleaning
     * ("/" and "\\" line-break normalization, timestamp stripping) that
     * the renderers apply. Any exception is logged (never blocks the
     * import — rendering is defensive anyway) and format counts are logged
     * so unusual layouts show up in logcat.
     */
    private fun runQuoteCleanImportChecks(file: File) {
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(
                file.absolutePath, null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )
            val opts = QuoteCleanOptions()
            var sampled = 0
            var displayCleaned = 0
            var spokenCleaned = 0
            var failures = 0
            db.rawQuery("SELECT text FROM quotes LIMIT 500", null).use { c ->
                while (c.moveToNext()) {
                    val text = c.getString(0) ?: continue
                    sampled += 1
                    try {
                        val d = cleanDisplayText(text, true, true, opts)
                        val sp = cleanSpokenText(text, true)
                        if (d != text) displayCleaned += 1
                        if (sp != text) spokenCleaned += 1
                    } catch (e: Exception) {
                        failures += 1
                        if (failures <= 3) {
                            Log.w("QuoteClean", "import check failed on a quote", e)
                        }
                    }
                }
            }
            Log.i(
                "QuoteClean",
                "import checks: $sampled sampled, $displayCleaned display-cleaned, " +
                    "$spokenCleaned spoken-cleaned, $failures failures"
            )
        } catch (e: Exception) {
            Log.w("QuoteClean", "import checks could not run", e)
        } finally {
            db?.close()
        }
    }

    /** Returns null when the file is a valid quote database, else an error message. */
    private fun validateQuoteDb(file: File): String? {
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(
                file.absolutePath, null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )
            val tables = mutableListOf<String>()
            db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='quotes'", null
            ).use { c -> while (c.moveToNext()) tables.add(c.getString(0)) }
            if (!tables.contains("quotes")) {
                return "The file has no 'quotes' table."
            }
            val cols = mutableMapOf<String, Triple<String, Int, Int>>()
            db.rawQuery("PRAGMA table_info(quotes)", null).use { c ->
                while (c.moveToNext()) {
                    // name -> (type, notnull, pk)
                    cols[c.getString(1)] =
                        Triple(c.getString(2).uppercase(), c.getInt(3), c.getInt(5))
                }
            }
            val required = mapOf(
                "number" to "INTEGER",
                "rating" to "INTEGER",
                "text" to "TEXT",
                "url" to "TEXT"
            )
            for ((name, type) in required) {
                val actual = cols[name]
                    ?: return "The 'quotes' table is missing column '$name'."
                if (!actual.first.contains(type)) {
                    return "Column '$name' must be $type (found ${actual.first})."
                }
                // 'rating' is nullable (deleted quotes carry no rating);
                // all other columns must be NOT NULL.
                if (name != "rating" && actual.second != 1) {
                    return "Column '$name' must be declared NOT NULL."
                }
            }
            val pk = cols["number"]?.third ?: 0
            if (pk != 1) {
                return "Column 'number' must be the PRIMARY KEY."
            }
            null
        } catch (e: Exception) {
            "Not a readable SQLite database: ${e.message}"
        } finally {
            try { db?.close() } catch (ignored: Exception) { }
        }
    }

    private fun sanitizeFileName(name: String): String {
        val clean = name.replace(Regex("[^A-Za-z0-9_-]"), "_").take(48).trim('_')
        return clean.ifEmpty { "imported" }
    }

    // ------------------------------------------------------------------
    // v1.59 data-layer additions (appended; existing code above untouched).
    // ------------------------------------------------------------------

    private val KEY_QOTD_SOURCE = "qotd_source"
    private val KEY_DB_THEMES_CARDS_ONLY = "db_themes_cards_only"

    /**
     * v1.59: daily-notification quote source — "random_sources" (default)
     * or "favorites". Anything else stored is normalized back to
     * "random_sources" on read/write.
     */
    private val _qotdSource = MutableStateFlow(
        prefs.getString(KEY_QOTD_SOURCE, "random_sources")?.takeIf {
            it == "favorites" || it == "random_sources"
        } ?: "random_sources"
    )
    val qotdSource: StateFlow<String> = _qotdSource.asStateFlow()

    fun setQotdSource(source: String) {
        val clean = if (source == "favorites") "favorites" else "random_sources"
        prefs.edit().putString(KEY_QOTD_SOURCE, clean).apply()
        _qotdSource.value = clean
    }

    /**
     * v1.59: when true, per-database site themes render on quote cards
     * only; the app chrome (top bar, nav, buttons) keeps the active app
     * theme. Default false = pre-v1.59 behavior (themes can tint chrome).
     */
    private val _dbThemesCardsOnly =
        MutableStateFlow(prefs.getBoolean(KEY_DB_THEMES_CARDS_ONLY, true))
    val dbThemesCardsOnly: StateFlow<Boolean> = _dbThemesCardsOnly.asStateFlow()

    fun setDbThemesCardsOnly(cardsOnly: Boolean) {
        prefs.edit().putBoolean(KEY_DB_THEMES_CARDS_ONLY, cardsOnly).apply()
        _dbThemesCardsOnly.value = cardsOnly
    }
}
