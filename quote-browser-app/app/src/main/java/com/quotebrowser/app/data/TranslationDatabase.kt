package com.quotebrowser.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase

/**
 * v1.61: quote translations live in their own database file
 * (`translations.db`, next to meta.db) so they are translated exactly once
 * and survive backup/restore. Keyed by database id + quote number + language
 * pair, so switching the target language later only translates each quote
 * one more time.
 */
@Entity(
    tableName = "translations",
    primaryKeys = ["dbId", "number", "sourceLang", "targetLang"]
)
data class QuoteTranslation(
    val dbId: String,
    val number: Int,
    /** BCP-47-ish source code, e.g. "sv"; "auto" when auto-detected. */
    val sourceLang: String,
    /** e.g. "en". */
    val targetLang: String,
    val translatedText: String,
    /** "gemini" or "fallback" — which engine produced this row. */
    val engine: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface TranslationDao {
    @Query(
        "SELECT * FROM translations WHERE dbId = :dbId AND number = :number " +
            "AND targetLang = :targetLang LIMIT 1"
    )
    suspend fun get(dbId: String, number: Int, targetLang: String): QuoteTranslation?

    @Query(
        "SELECT * FROM translations WHERE dbId = :dbId AND number IN (:numbers) " +
            "AND targetLang = :targetLang"
    )
    suspend fun getBatch(
        dbId: String,
        numbers: List<Int>,
        targetLang: String
    ): List<QuoteTranslation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(t: QuoteTranslation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<QuoteTranslation>)

    /**
     * v1.87: delete every translation for one database + target language,
     * regardless of sourceLang — stale auto/machine rows would otherwise
     * shadow curated rows in the targetLang-only LIMIT 1 lookups.
     */
    @Query(
        "DELETE FROM translations WHERE dbId = :dbId AND targetLang = :targetLang"
    )
    suspend fun deleteForDbTarget(dbId: String, targetLang: String): Int

    /** v1.87: count rows for one database + target language (seed check). */
    @Query(
        "SELECT COUNT(*) FROM translations WHERE dbId = :dbId AND targetLang = :targetLang"
    )
    suspend fun countForDbTarget(dbId: String, targetLang: String): Int

    @Query("SELECT COUNT(*) FROM translations")
    suspend fun count(): Int
}

@Database(entities = [QuoteTranslation::class], version = 1, exportSchema = false)
abstract class TranslationDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
}
