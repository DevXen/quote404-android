package com.quotebrowser.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room wrapper around one quote database file. Any .db whose schema matches
 * quotes(number, rating, text, url[, status]) can be opened with this class —
 * bundled assets via createFromAsset(), imported files via their file path.
 * Extra tables in the file (e.g. the quotes_fts FTS5 index and its shadow
 * tables) are ignored by Room's schema validation.
 *
 * v1.50.1 adds the `status` column ('approved' / 'queued'). The startup
 * bundled-data refresh (DatabaseManager) normally adds it before Room ever
 * opens the file; this migration is the safety net for any file it missed.
 *
 * v1.65 adds the content columns `maturity`, `spicy`, and `speakers`
 * (see Quote.kt). Same arrangement: the bundled-data refresh merges them
 * onto the on-device copy, and Migration(2,3) is the safety net.
 */
@Database(entities = [Quote::class], version = 3, exportSchema = false)
abstract class QuoteDatabase : RoomDatabase() {
    abstract fun quoteDao(): QuoteDao
}

/**
 * Ensures the `status` column exists on [db]'s quotes table, adding it with
 * the same definition the bundled assets carry when it is missing. SQLite
 * has no ADD COLUMN IF NOT EXISTS, so the PRAGMA check comes first. Safe
 * to call on every open — it is a no-op when the column already exists.
 */
fun ensureQuotesStatusColumn(db: SupportSQLiteDatabase) {
    var hasStatus = false
    db.query("PRAGMA table_info(quotes)").use { c ->
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

/** v1 -> v2: adds the `status` column to pre-existing quote files. */
 val QUOTES_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureQuotesStatusColumn(db)
    }
}

/**
 * v1.65 content columns, installed with the same definitions the bundled
 * assets carry. Safe to call on every open — each ALTER is a no-op when
 * the column already exists. Quotes on files that never got rated keep
 * the defaults (maturity 0 / not spicy / 0 speakers).
 */
fun ensureQuotesContentColumns(db: SupportSQLiteDatabase) {
    val existing = mutableSetOf<String>()
    db.query("PRAGMA table_info(quotes)").use { c ->
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

/** v2 -> v3: adds the v1.65 content columns to pre-existing quote files. */
val QUOTES_MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        ensureQuotesContentColumns(db)
    }
}
