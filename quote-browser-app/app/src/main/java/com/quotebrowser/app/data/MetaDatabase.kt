package com.quotebrowser.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Favorites and history are stored per-database (keyed by dbId + quote number)
 * in this separate meta database, so switching or importing quote databases
 * never loses them.
 */

@Entity(tableName = "favorites", primaryKeys = ["dbId", "number"])
data class Favorite(
    val dbId: String,
    val number: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history", primaryKeys = ["dbId", "number"])
data class HistoryEntry(
    val dbId: String,
    val number: Int,
    val viewedAt: Long = System.currentTimeMillis()
)

/**
 * v1.53: every single view of a quote, one row per view. The [history]
 * table keeps one row per quote (the most recent view, used for ordering);
 * this table is the full log behind the per-entry view count and the
 * expandable view-datetime list.
 */
@Entity(
    tableName = "history_views",
    indices = [Index(value = ["dbId", "number"])]
)
data class HistoryView(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dbId: String,
    val number: Int,
    val viewedAt: Long = System.currentTimeMillis()
)

/**
 * Quotes the user has actually opened (detail screen or endless deck).
 * Feeds the reading-stats screen; unlike history it is never trimmed and
 * is only ever added to.
 */
@Entity(tableName = "seen_quotes", primaryKeys = ["dbId", "number"])
data class SeenQuote(
    val dbId: String,
    val number: Int,
    val seenAt: Long = System.currentTimeMillis()
)

/** Quote databases the user imported via the Storage Access Framework picker. */
@Entity(tableName = "imported_dbs")
data class ImportedDb(
    @PrimaryKey val id: String,
    val label: String,
    val fileName: String,
    val addedAt: Long = System.currentTimeMillis()
)

/** Per-day read counts for the reading heatmap. */
data class DayCount(
    val day: String,
    val count: Int
)

@Dao
interface MetaDao {
    // ---- favorites ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE dbId = :dbId AND number = :number")
    suspend fun removeFavorite(dbId: String, number: Int)

    @Query("SELECT * FROM favorites WHERE dbId = :dbId ORDER BY addedAt DESC")
    fun favoriteEntries(dbId: String): Flow<List<Favorite>>

    // v1.77: cross-database favorites in the virtual "all" view — every
    // database's favorites in one recency-ordered stream, each entry
    // keeping its own dbId so cards resolve their true source theme.
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun allFavoriteEntries(): Flow<List<Favorite>>

    @Query(
        "SELECT * FROM favorites WHERE " +
            "strftime('%m-%d', addedAt/1000, 'unixepoch', 'localtime') = :monthDay " +
            "AND strftime('%Y', addedAt/1000, 'unixepoch', 'localtime') < :year " +
            "ORDER BY addedAt DESC"
    )
    suspend fun favoritesOnThisDay(monthDay: String, year: String): List<Favorite>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE dbId = :dbId AND number = :number)")
    fun isFavorite(dbId: String, number: Int): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites WHERE dbId = :dbId AND number = :number")
    suspend fun favoriteCount(dbId: String, number: Int): Int

    // ---- history ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordView(entry: HistoryEntry)

    @Query("SELECT * FROM history WHERE dbId = :dbId ORDER BY viewedAt DESC LIMIT 100")
    fun historyEntries(dbId: String): Flow<List<HistoryEntry>>

    // v1.77: cross-database history in the virtual "all" view — every
    // database's history in one recency-ordered stream, each entry keeping
    // its own dbId so cards resolve their true source theme.
    @Query("SELECT * FROM history ORDER BY viewedAt DESC LIMIT 100")
    fun allHistoryEntries(): Flow<List<HistoryEntry>>

    @Query(
        "SELECT * FROM history WHERE " +
            "strftime('%m-%d', viewedAt/1000, 'unixepoch', 'localtime') = :monthDay " +
            "AND strftime('%Y', viewedAt/1000, 'unixepoch', 'localtime') < :year " +
            "ORDER BY viewedAt DESC"
    )
    suspend fun historyOnThisDay(monthDay: String, year: String): List<HistoryEntry>

    @Query("DELETE FROM history WHERE dbId = :dbId")
    suspend fun clearHistory(dbId: String)

    // v1.77: clear every database's history (the virtual "all" view).
    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    @Query("DELETE FROM history WHERE dbId = :dbId AND number = :number")
    suspend fun removeHistoryEntry(dbId: String, number: Int)

    // ---- history view log (v1.53: every view, not just the latest) ----
    @Insert
    suspend fun logView(view: HistoryView)

    @Query("SELECT viewedAt FROM history_views WHERE dbId = :dbId AND number = :number ORDER BY viewedAt DESC")
    suspend fun viewTimes(dbId: String, number: Int): List<Long>

    @Query("SELECT COUNT(*) FROM history_views WHERE dbId = :dbId AND number = :number")
    suspend fun viewCount(dbId: String, number: Int): Int

    @Query("DELETE FROM history_views WHERE dbId = :dbId")
    suspend fun clearHistoryViews(dbId: String)

    // v1.77: clear every database's view log (the virtual "all" view).
    @Query("DELETE FROM history_views")
    suspend fun clearAllHistoryViews()

    @Query("DELETE FROM history_views WHERE dbId = :dbId AND number = :number")
    suspend fun removeHistoryViews(dbId: String, number: Int)

    // ---- seen quotes (reading stats) ----
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markSeen(entry: SeenQuote)

    @Query("SELECT COUNT(*) FROM seen_quotes WHERE dbId = :dbId")
    fun seenCount(dbId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM seen_quotes")
    suspend fun totalSeenCount(): Int

    @Query(
        "SELECT DISTINCT date(seenAt / 1000, 'unixepoch', 'localtime') " +
            "FROM seen_quotes ORDER BY 1 DESC"
    )
    suspend fun seenDays(): List<String>

    @Query("SELECT number FROM seen_quotes WHERE dbId = :dbId")
    suspend fun seenNumbers(dbId: String): List<Int>

    @Query("DELETE FROM seen_quotes")
    suspend fun clearSeen()

    /**
     * Quotes read per local day ("YYYY-MM-DD" -> count), newest first,
     * capped at ~400 days for the reading heatmap.
     */
    @Query(
        "SELECT date(seenAt / 1000, 'unixepoch', 'localtime') AS day, " +
            "COUNT(*) AS count FROM seen_quotes " +
            "GROUP BY day ORDER BY day DESC LIMIT 400"
    )
    suspend fun seenCountsByDay(): List<DayCount>

    // ---- imported databases ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImportedDb(db: ImportedDb)

    @Query("SELECT * FROM imported_dbs ORDER BY addedAt ASC")
    fun importedDbs(): Flow<List<ImportedDb>>

    @Query("SELECT * FROM imported_dbs ORDER BY addedAt ASC")
    suspend fun importedDbsSync(): List<ImportedDb>

    @Query("DELETE FROM imported_dbs WHERE id = :id")
    suspend fun removeImportedDb(id: String)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `seen_quotes` (" +
                "`dbId` TEXT NOT NULL, `number` INTEGER NOT NULL, " +
                "`seenAt` INTEGER NOT NULL, PRIMARY KEY(`dbId`, `number`))"
        )
    }
}

/**
 * v1.53: per-view history log. Backfills one view row per existing
 * history entry (where a timestamp was recorded), so legacy entries show
 * a view count of 1 with their original date instead of 0/"Unknown".
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `history_views` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`dbId` TEXT NOT NULL, `number` INTEGER NOT NULL, " +
                "`viewedAt` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_history_views_dbId_number` " +
                "ON `history_views` (`dbId`, `number`)"
        )
        db.execSQL(
            "INSERT INTO `history_views` (`dbId`, `number`, `viewedAt`) " +
                "SELECT `dbId`, `number`, `viewedAt` FROM `history` " +
                "WHERE `viewedAt` > 0"
        )
    }
}

@Database(
    entities = [Favorite::class, HistoryEntry::class, HistoryView::class, ImportedDb::class, SeenQuote::class],
    version = 3,
    exportSchema = false
)
abstract class MetaDatabase : RoomDatabase() {
    abstract fun metaDao(): MetaDao
}
