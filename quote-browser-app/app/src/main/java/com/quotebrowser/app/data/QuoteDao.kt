package com.quotebrowser.app.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery

/**
 * DAO for a single quote database. The FTS5 index (quotes_fts, external-content
 * on quotes) is queried directly with MATCH; it is pre-populated in the .db
 * file and never rebuilt by the app.
 */
@Dao
interface QuoteDao {

    /** Paged browse with a caller-supplied ORDER BY (built via SimpleSQLiteQuery). */
    @RawQuery(observedEntities = [Quote::class])
    fun browsePaged(query: SupportSQLiteQuery): PagingSource<Int, Quote>

    /** Paged full-text search (caller supplies the MATCH query). */
    @RawQuery(observedEntities = [Quote::class])
    fun searchPaged(query: SupportSQLiteQuery): PagingSource<Int, Quote>

    @Query("SELECT * FROM quotes WHERE number = :number")
    suspend fun getByNumber(number: Int): Quote?

    @Query("SELECT * FROM quotes WHERE number IN (:numbers)")
    suspend fun getByNumbers(numbers: List<Int>): List<Quote>

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1")
    suspend fun randomQuote(): Quote?

    @Query("SELECT * FROM quotes ORDER BY RANDOM() LIMIT :limit")
    suspend fun randomQuotes(limit: Int): List<Quote>

    /** Single quote by position in number order (used for Quote of the Day). */
    @Query("SELECT * FROM quotes ORDER BY number LIMIT 1 OFFSET :offset")
    suspend fun getByOffset(offset: Int): Quote?

    @Query("SELECT * FROM quotes ORDER BY rating IS NULL, rating DESC LIMIT :limit OFFSET :offset")
    suspend fun topRated(limit: Int, offset: Int): List<Quote>

    @Query("SELECT * FROM quotes ORDER BY rating IS NULL, rating ASC LIMIT :limit OFFSET :offset")
    suspend fun bottomRated(limit: Int, offset: Int): List<Quote>

    /** v1.51 per-database status filter for the Top/Bottom flows. */
    @Query("SELECT * FROM quotes WHERE status = :status ORDER BY rating IS NULL, rating DESC LIMIT :limit OFFSET :offset")
    suspend fun topRatedByStatus(status: String, limit: Int, offset: Int): List<Quote>

    @Query("SELECT * FROM quotes WHERE status = :status ORDER BY rating IS NULL, rating ASC LIMIT :limit OFFSET :offset")
    suspend fun bottomRatedByStatus(status: String, limit: Int, offset: Int): List<Quote>

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM quotes WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @RawQuery
    suspend fun countRaw(query: SupportSQLiteQuery): Int

    @Query("SELECT * FROM quotes ORDER BY number")
    suspend fun getAll(): List<Quote>

    /** Caller-built list queries (used for category-filtered variants). */
    @RawQuery(observedEntities = [Quote::class])
    suspend fun rawQuotes(query: SupportSQLiteQuery): List<Quote>

    /** Caller-built COUNT(*) queries (used for category-filtered counts). */
    @RawQuery
    suspend fun rawCount(query: SupportSQLiteQuery): Int

    // --- User quote management (used by the "My Quotes" database) ---

    @Insert
    suspend fun insert(quote: Quote)

    /** v1.83: bulk insert used when building a plugin quote-pack database. */
    @Insert
    suspend fun insertAll(quotes: List<Quote>)

    @Update
    suspend fun update(quote: Quote)

    @Query("DELETE FROM quotes WHERE number = :number")
    suspend fun deleteByNumber(number: Int)

    @Query("SELECT MAX(number) FROM quotes")
    suspend fun maxNumber(): Int?

    /** Number of the first quote whose text matches exactly, or null. */
    @Query("SELECT number FROM quotes WHERE text = :text LIMIT 1")
    suspend fun findNumberByText(text: String): Int?

    /** Nearest existing quote numbers below/above [n] (numbers can have gaps). */
    @Query("SELECT number FROM quotes WHERE number < :n ORDER BY number DESC LIMIT 1")
    suspend fun prevNumber(n: Int): Int?

    @Query("SELECT number FROM quotes WHERE number > :n ORDER BY number ASC LIMIT 1")
    suspend fun nextNumber(n: Int): Int?

    /** Smallest quote number in the database, or null when empty. */
    @Query("SELECT number FROM quotes ORDER BY number ASC LIMIT 1")
    suspend fun minNumber(): Int?

    /** All non-null ratings, for the stats rating-distribution histogram. */
    @Query("SELECT rating FROM quotes WHERE rating IS NOT NULL")
    suspend fun allRatings(): List<Int>

    /** How many quotes carry a rating (0 for the 2011 archives). */
    @Query("SELECT COUNT(*) FROM quotes WHERE rating IS NOT NULL")
    suspend fun countRated(): Int
}
