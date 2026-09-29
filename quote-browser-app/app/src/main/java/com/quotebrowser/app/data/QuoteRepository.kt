package com.quotebrowser.app.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.sqlite.db.SimpleSQLiteQuery
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class SortOrder(val label: String) {
    NUMBER_DESC("Number ↓"),
    NUMBER_ASC("Number ↑"),
    RATING_DESC("Rating ↓"),
    RATING_ASC("Rating ↑")
}

/**
 * Quote-length filter presets. LONGER/SHORTER thresholds are configurable
 * in Settings (word counts); CUSTOM uses explicit min/max word boxes.
 */
enum class LengthMode(val label: String) {
    ALL("All lengths"),
    LONGER("Longer"),
    SHORTER("Shorter"),
    ONE_LINE("1 line"),
    CUSTOM("Custom…")
}

/**
 * Data access for one quote database, plus the per-database favorites/history
 * stored in the shared meta database.
 */

/**
 * v1.53: one history row — the quote, when it was most recently viewed
 * (epoch millis), and how many times it has been viewed in total.
 * viewedAt <= 0 means the timestamp was never recorded; display
 * "Unknown date" for those.
 */
data class HistoryItem(
    val quote: Quote,
    val viewedAt: Long,
    val viewCount: Int,
    // v1.77: the quote's true source database. The virtual "all" view mixes
    // databases, so every card must know its own source to resolve the
    // right site theme. Defaults to "" for single-DB constructions that
    // fill it in from context.
    val dbId: String = ""
)

class QuoteRepository(
    db: QuoteDatabase,
    val dbId: String,
    private val meta: MetaDao
) {
    private val dao: QuoteDao = db.quoteDao()

    /** Direct access to the per-database category tables. */
    val categories: CategoryStore = CategoryStore(db)

    /** Categories in this database, each with its quote count. */
    suspend fun listCategories(): List<Category> = categories.list()

    /**
     * JOIN fragment restricting quotes to a set of categories (a quote
     * matches if it is in ANY of them). The ids come from our own category
     * list, so interpolating them is safe.
     */
    private fun categoryJoin(categoryIds: Set<Long>): String =
        if (categoryIds.isEmpty()) ""
        else "JOIN quote_categories qc ON qc.quote_number = quotes.number " +
            "AND qc.category_id IN (${categoryIds.joinToString(",")}) "

    fun browse(
        sort: SortOrder,
        minRating: Int? = null,
        categoryIds: Set<Long> = emptySet(),
        /** v1.51 per-database status filter; null = no status filtering. */
        statusMode: StatusMode? = null,
        /** v1.65 content filters (ANDed); empty = no filtering. */
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null,
        /** v1.65 exclusion: leave these categories out. */
        excludedCategoryIds: Set<Long> = emptySet()
    ): Flow<PagingData<Quote>> {
        val orderBy = orderByFor(sort)
        // minRating only ever comes from the fixed chip set, so interpolation is safe.
        val where = withExcludedCategories(
            withMaturities(
                withCast(withStatus(ratingWhere(minRating), statusMode), cast),
                maturities
            ),
            excludedCategoryIds
        )
        return Pager(PagingConfig(pageSize = 25, enablePlaceholders = false)) {
            dao.browsePaged(
                SimpleSQLiteQuery(
                    "SELECT DISTINCT quotes.* FROM quotes ${categoryJoin(categoryIds)}$where" +
                        "ORDER BY $orderBy"
                )
            )
        }.flow
    }

    /**
     * v1.66: non-paging filter browse — the list equivalent of [browse],
     * for all-databases filter browsing (mood buttons). Same WHERE
     * construction, capped at [limit] rows.
     */
    suspend fun browseList(
        sort: SortOrder,
        minRating: Int? = null,
        categoryIds: Set<Long> = emptySet(),
        statusMode: StatusMode? = null,
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null,
        excludedCategoryIds: Set<Long> = emptySet(),
        limit: Int = 200
    ): List<Quote> {
        val n = limit.coerceIn(1, 1000)
        val orderBy = orderByFor(sort)
        // minRating only ever comes from the fixed chip set, so interpolation is safe.
        val where = withExcludedCategories(
            withMaturities(
                withCast(withStatus(ratingWhere(minRating), statusMode), cast),
                maturities
            ),
            excludedCategoryIds
        )
        return dao.rawQuotes(
            SimpleSQLiteQuery(
                "SELECT DISTINCT quotes.* FROM quotes ${categoryJoin(categoryIds)}$where" +
                    "ORDER BY $orderBy LIMIT $n"
            )
        )
    }

    /** Total quote count (used to skip empty databases in QotD rotation). */
    suspend fun count(): Int = dao.count()

    suspend fun countByStatus(status: String): Int = dao.countByStatus(status)

    /** v1.74.3 diagnostic: count quotes matching a cast-size filter. */
    suspend fun countByCast(cast: CastSize): Int {
        val where = castWhere(cast)
        return dao.countRaw(SimpleSQLiteQuery("SELECT COUNT(*) FROM quotes $where"))
    }

    /**
     * Random quotes carrying one category (v1.53 memorize deck). The
     * category tables live in each database file, so this is per-database.
     */
    suspend fun quotesInCategory(categoryId: Long, limit: Int = 200): List<Quote> =
        dao.rawQuotes(
            SimpleSQLiteQuery(
                "SELECT quotes.* FROM quotes " +
                    "INNER JOIN quote_categories qc " +
                    "ON qc.quote_number = quotes.number " +
                    "WHERE qc.category_id = ? " +
                    "ORDER BY RANDOM() LIMIT ?",
                arrayOf<Any>(categoryId, limit.coerceIn(1, 500))
            )
        )

    /**
     * v1.74: candidate quotes for a notification ping. All filtering is
     * SQL-backed so the initial ping and the notification's Next action
     * share byte-identical logic (see worker/QuotePingFilter.kt):
     * - the effective maturity ceiling ([MaturityLevel.sqlWhere])
     * - the minimum rating (null ratings never satisfy a minimum)
     * - the category-name filter (names matched case-insensitively,
     *   so they work across every database like the mood presets)
     * - the recently-notified exclusion (quote numbers shown as pings
     *   within the recency window)
     *
     * Interpolation is safe: the maturity predicate comes from the fixed
     * enum, ratings/recency are our own Ints, and category names go in as
     * bind args. Callers apply the never-show-again / muted-nick /
     * content-filter checks on the returned candidates.
     */
    suspend fun pingCandidates(
        ceiling: MaturityLevel,
        minRating: Int?,
        categoryNames: Set<String>,
        recentNumbers: Set<Int>,
        limit: Int = 5
    ): List<Quote> {
        val conds = mutableListOf<String>()
        if (ceiling != MaturityLevel.EVERYTHING) conds.add("(${ceiling.sqlWhere()})")
        if (minRating != null) conds.add("rating >= ${minRating.coerceAtLeast(0)}")
        val args = mutableListOf<Any>()
        if (categoryNames.isNotEmpty()) {
            val placeholders = categoryNames.indices.joinToString(",") { "?" }
            conds.add(
                "EXISTS (SELECT 1 FROM quote_categories qc " +
                    "JOIN categories c ON c.id = qc.category_id " +
                    "WHERE qc.quote_number = quotes.number " +
                    "AND LOWER(c.name) IN ($placeholders))"
            )
            args.addAll(categoryNames.map { it.lowercase() })
        }
        if (recentNumbers.isNotEmpty()) {
            conds.add("number NOT IN (${recentNumbers.joinToString(",")})")
        }
        val where = if (conds.isEmpty()) "" else "WHERE " + conds.joinToString(" AND ")
        return dao.rawQuotes(
            SimpleSQLiteQuery(
                "SELECT quotes.* FROM quotes $where " +
                    "ORDER BY RANDOM() LIMIT ${limit.coerceIn(1, 25)}",
                args.toTypedArray()
            )
        )
    }

    /**
     * Quote count honoring the same rating filter as [browse] (used by the
     * cross-database merged view).
     */
    suspend fun countFiltered(
        minRating: Int?,
        statusMode: StatusMode? = null,
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null
    ): Int = dao.rawCount(
        SimpleSQLiteQuery(
            "SELECT COUNT(*) FROM quotes " +
                withMaturities(withCast(withStatus(ratingWhere(minRating), statusMode), cast), maturities)
        )
    )

    /** Whether any quote in this database carries a rating. */
    suspend fun hasRatings(): Boolean = dao.countRated() > 0

    /**
     * One slice of the rating-filtered, sorted quote list (used by the
     * cross-database merged view).
     */
    suspend fun pageFiltered(
        minRating: Int?,
        sort: SortOrder,
        limit: Int,
        offset: Int,
        statusMode: StatusMode? = null,
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null
    ): List<Quote> =
        dao.rawQuotes(
            SimpleSQLiteQuery(
                "SELECT * FROM quotes " +
                    withMaturities(withCast(withStatus(ratingWhere(minRating), statusMode), cast), maturities) +
                    "ORDER BY ${orderByFor(sort)} LIMIT $limit OFFSET $offset"
            )
        )

    companion object {
        /** ORDER BY fragment for a sort order ("rating IS NULL" sorts unrated quotes last). */
        fun orderByFor(sort: SortOrder): String = when (sort) {
            SortOrder.NUMBER_ASC -> "number ASC"
            SortOrder.NUMBER_DESC -> "number DESC"
            SortOrder.RATING_ASC -> "rating IS NULL, rating ASC, number ASC"
            SortOrder.RATING_DESC -> "rating IS NULL, rating DESC, number DESC"
        }

        /** WHERE fragment for the fixed rating-chip filter (interpolation is safe). */
        fun ratingWhere(minRating: Int?): String =
            if (minRating != null) "WHERE rating >= $minRating " else ""

        /**
         * Appends a v1.51 status condition ("approved"/"queued" only) to an
         * existing WHERE fragment. Null mode = unchanged.
         */
        fun withStatus(where: String, statusMode: StatusMode?): String {
            val cond = statusMode?.sqlWhere() ?: return where
            val trimmed = where.trimEnd()
            return if (trimmed.isEmpty()) "WHERE $cond "
            else "$trimmed AND $cond "
        }

        /**
         * Appends a v1.51 status condition to an AND-chain fragment (the
         * searchWhere style, where the WHERE keyword already exists).
         */
        fun andStatus(statusMode: StatusMode?): String =
            statusMode?.sqlWhere()?.let { " AND $it" } ?: ""

        // --- v1.65 content filters (maturity ladder + cast size) ---

        /**
         * WHERE fragment for the maturity filter (interpolation is safe:
         * the predicate comes from the fixed MaturityLevel enum).
         * EVERYTHING (or null) = no filtering.
         */
        fun maturityWhere(level: MaturityLevel?): String {
            if (level == null || level == MaturityLevel.EVERYTHING) return ""
            return "WHERE ${level.sqlWhere()} "
        }

        /** AND-chain variant of [maturityWhere] for the searchWhere style. */
        fun andMaturity(level: MaturityLevel?): String {
            if (level == null || level == MaturityLevel.EVERYTHING) return ""
            return " AND ${level.sqlWhere()}"
        }

        /**
         * ANDs several maturity levels (e.g. the global ceiling plus the
         * per-screen pick). EVERYTHING entries are no-ops.
         */
        fun andMaturities(levels: List<MaturityLevel>): String =
            levels.joinToString("") { andMaturity(it) }

        /**
         * Appends a v1.51 status condition to an existing WHERE fragment
         * (the browse style, mirroring [withStatus]).
         */
        fun withMaturity(where: String, level: MaturityLevel?): String {
            if (level == null || level == MaturityLevel.EVERYTHING) return where
            val cond = level.sqlWhere()
            val trimmed = where.trimEnd()
            return if (trimmed.isEmpty()) "WHERE $cond "
            else "$trimmed AND $cond "
        }

        /** Browse-style variant of [andMaturities]. */
        fun withMaturities(where: String, levels: List<MaturityLevel>): String {
            var w = where
            for (l in levels) w = withMaturity(w, l)
            return w
        }

        /** WHERE fragment for the cast-size filter (fixed CastSize enum). */
        fun castWhere(cast: CastSize?): String =
            if (cast == null) "" else "WHERE ${cast.sqlWhere()} "

        /** AND-chain variant of [castWhere] for the searchWhere style. */
        fun andCast(cast: CastSize?): String =
            if (cast == null) "" else " AND ${cast.sqlWhere()}"

        /** Appends a cast-size condition to an existing WHERE fragment. */
        fun withCast(where: String, cast: CastSize?): String {
            if (cast == null) return where
            val cond = cast.sqlWhere()
            val trimmed = where.trimEnd()
            return if (trimmed.isEmpty()) "WHERE $cond "
            else "$trimmed AND $cond "
        }

        /**
         * v1.65 search exclusion: quotes in NONE of [excluded] categories.
         * Ids come from our own category list, so interpolation is safe.
         */
        fun andExcludedCategories(excluded: Set<Long>): String =
            if (excluded.isEmpty()) ""
            else " AND NOT EXISTS (SELECT 1 FROM quote_categories qc " +
                "WHERE qc.quote_number = quotes.number " +
                "AND qc.category_id IN (${excluded.joinToString(",")}))"

        /**
         * Browse-style variant of [andExcludedCategories]: appends to an
         * existing WHERE fragment, or starts one when it's empty.
         */
        fun withExcludedCategories(where: String, excluded: Set<Long>): String {
            if (excluded.isEmpty()) return where
            val cond = "NOT EXISTS (SELECT 1 FROM quote_categories qc " +
                "WHERE qc.quote_number = quotes.number " +
                "AND qc.category_id IN (${excluded.joinToString(",")}))"
            val trimmed = where.trimEnd()
            return if (trimmed.isEmpty()) "WHERE $cond "
            else "$trimmed AND $cond "
        }

        /**
         * v1.65 search exclusion: maturity levels to leave out
         * (values are our own Ints, so interpolation is safe).
         */
        fun andExcludedMaturities(excluded: Set<Int>): String =
            if (excluded.isEmpty()) ""
            else " AND maturity NOT IN (${excluded.joinToString(",")})"

        /**
         * v1.65 search exclusion: cast sizes to leave out. Each bucket is
         * a fixed predicate, so interpolation is safe.
         */
        fun andExcludedCasts(excluded: Set<CastSize>): String =
            if (excluded.isEmpty()) ""
            else " AND NOT (${excluded.joinToString(" OR ") { "(${it.sqlWhere()})" }})"
    }

    /**
     * Text search over quote text using LIKE (FTS5 is not available on all
     * devices, e.g. Android 16). A query that is purely digits is treated
     * as an exact quote-number lookup.
     */
    /**
     * Boolean search: supports AND, OR, NOT (case-insensitive) and
     * "exact phrases" in quotes. Bare words are ANDed together, AND binds
     * tighter than OR. Plain queries without operators keep the old
     * substring behavior.
     *
     * Examples: `kernel AND linux`, `"blue screen" NOT windows`,
     * `vim OR emacs`.
     */
    private data class SearchTerm(
        val negate: Boolean,
        val or: Boolean,
        val text: String,
        /** v1.58: true when the term was a "quoted phrase" — stays exact, never fuzzy. */
        val exact: Boolean
    )

    private fun parseBooleanQuery(raw: String): List<SearchTerm>? {
        if (!raw.contains('"') &&
            !Regex("\\b(AND|OR|NOT)\\b", RegexOption.IGNORE_CASE).containsMatchIn(raw)
        ) {
            return null
        }
        // Tokenize, keeping "quoted phrases" together.
        val tokens = mutableListOf<String>()
        val cur = StringBuilder()
        var inQuotes = false
        for (c in raw) {
            when {
                c == '"' -> {
                    if (inQuotes) {
                        tokens.add("\"$cur\"")
                        cur.clear()
                    }
                    inQuotes = !inQuotes
                }
                c.isWhitespace() && !inQuotes -> {
                    if (cur.isNotEmpty()) {
                        tokens.add(cur.toString())
                        cur.clear()
                    }
                }
                else -> cur.append(c)
            }
        }
        if (cur.isNotEmpty()) tokens.add(cur.toString())
        if (tokens.isEmpty()) return null

        val terms = mutableListOf<SearchTerm>()
        var pendingOr = false
        var pendingNot = false
        for (tok in tokens) {
            when {
                tok.equals("AND", ignoreCase = true) -> { /* default connective */ }
                tok.equals("OR", ignoreCase = true) -> pendingOr = true
                tok.equals("NOT", ignoreCase = true) -> pendingNot = true
                else -> {
                    val quoted = tok.length >= 2 && tok.first() == '"' && tok.last() == '"'
                    val text = tok.removeSurrounding("\"").trim()
                    if (text.isNotEmpty()) {
                        terms.add(
                            SearchTerm(
                                negate = pendingNot,
                                or = pendingOr,
                                text = text,
                                exact = quoted
                            )
                        )
                    }
                    pendingOr = false
                    pendingNot = false
                }
            }
        }
        return terms.ifEmpty { null }
    }

    private fun likeEscape(s: String): String =
        s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    /**
     * v1.58: typo-tolerant LIKE patterns for one plain search term — the
     * exact substring plus every edit-distance-1 variant (one substituted,
     * one inserted, one deleted character at each position), expressed with
     * the single-character `_` wildcard. The `_` wildcards are added after
     * escaping, so a literal `_`/`%` in the term can never become a
     * wildcard. Terms shorter than 4 characters stay exact (fuzzy variants
     * would match nearly everything). Quoted phrases and pure-digit
     * number jumps never go through here — they stay exact.
     */
    private fun fuzzyLikePatterns(term: String): List<String> {
        val exact = "%" + likeEscape(term) + "%"
        if (term.length < 4) return listOf(exact)
        val out = LinkedHashSet<String>()
        out.add(exact)
        for (i in term.indices) {
            val head = likeEscape(term.substring(0, i))
            val tail = likeEscape(term.substring(i + 1))
            val tailWith = likeEscape(term.substring(i))
            // One wrong letter: "helo" matches "hello".
            out.add("%$head" + "_" + "$tail%")
            // One missing letter: "hllo" matches "hello".
            out.add("%$head" + "_" + "$tailWith%")
            // One extra letter: "helllo" matches "hello".
            out.add("%$head$tail%")
        }
        return out.toList()
    }

    /**
     * v1.58: `(text LIKE ? ESCAPE '\' OR ...)` fragment for one term plus
     * its bind args. [exactOnly] keeps the single exact pattern (quoted
     * phrases); otherwise the typo-tolerant expansion applies.
     */
    private fun fuzzyTermFragment(term: String, exactOnly: Boolean): Pair<String, List<Any>> {
        val patterns =
            if (exactOnly) listOf("%" + likeEscape(term) + "%")
            else fuzzyLikePatterns(term)
        val frag = patterns.joinToString(" OR ", prefix = "(", postfix = ")") {
            "text LIKE ? ESCAPE '\\'"
        }
        return frag to patterns
    }

    /**
     * Builds the WHERE fragment + bind args for a boolean query, or null
     * when [rawQuery] has no boolean syntax (caller uses substring search).
     */
    private fun booleanWhere(raw: String): Pair<String, List<Any>>? {
        val terms = parseBooleanQuery(raw) ?: return null
        val where = StringBuilder("(")
        val args = mutableListOf<Any>()
        terms.forEachIndexed { i, t ->
            if (i > 0) where.append(if (t.or) " OR " else " AND ")
            if (t.negate) where.append("NOT ")
            // v1.58: plain terms are typo-tolerant; quoted phrases stay
            // exact. AND/OR/NOT structure is unchanged.
            val (frag, fargs) = fuzzyTermFragment(t.text, t.exact)
            where.append(frag)
            args.addAll(fargs)
        }
        where.append(")")
        return where.toString() to args
    }

    /**
     * Shared WHERE-clause construction for [search] and [countMatches]:
     * the JOIN + WHERE fragment, its bind args, and whether the query
     * matches nothing at all (blank query). Both use the same boolean
     * search / substring / quote-number logic, so the alert count always
     * agrees with what the search screen would show.
     */
    private fun searchWhere(
        rawQuery: String,
        categoryIds: Set<Long>,
        minRating: Int?,
        statusMode: StatusMode? = null,
        /** v1.65 content filters (ANDed); empty = no filtering. */
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null,
        /** v1.65 exclusions: leave these out of the results. */
        excludedCategoryIds: Set<Long> = emptySet(),
        excludedMaturities: Set<Int> = emptySet(),
        excludedCasts: Set<CastSize> = emptySet(),
        /**
         * v1.74.5 regex mode: skip the text constraint in SQL (the pattern
         * is applied in Kotlin, since Android's SQLite has no REGEXP) but
         * keep every other filter.
         */
        skipText: Boolean = false
    ): Triple<String, Array<Any>, Boolean> {
        val query = rawQuery.trim()
        val join = categoryJoin(categoryIds)
        // minRating only ever comes from the fixed chip set, so interpolation is safe.
        val ratingWhere = if (minRating != null) " AND rating >= $minRating" else ""
        // v1.51 status filter: applies to text searches, never to the
        // exact quote-number jump.
        val statusAnd = andStatus(statusMode)
        // v1.65 content filters + exclusions: same scope as the status filter.
        val contentAnd = andMaturities(maturities) + andCast(cast) +
            andExcludedCategories(excludedCategoryIds) +
            andExcludedMaturities(excludedMaturities) +
            andExcludedCasts(excludedCasts)
        val boolean = if (query.toIntOrNull() == null) booleanWhere(query) else null
        return when {
            query.isEmpty() -> Triple("", emptyArray(), true)
            // v1.74.5: regex mode — every filter except the text match.
            skipText -> Triple(
                "$join WHERE 1=1$ratingWhere$statusAnd$contentAnd",
                emptyArray(),
                false
            )
            // A pure-digit query is an exact quote-number jump: filters don't apply.
            query.toIntOrNull() != null -> Triple(
                "$join WHERE number = ?",
                arrayOf<Any>(query.toInt()),
                false
            )
            boolean != null -> Triple(
                "$join WHERE ${boolean.first}$ratingWhere$statusAnd$contentAnd",
                boolean.second.toTypedArray(),
                false
            )
            // v1.58: plain substring queries are typo-tolerant.
            else -> {
                val (frag, fargs) = fuzzyTermFragment(query, exactOnly = false)
                Triple(
                    "$join WHERE $frag$ratingWhere$statusAnd$contentAnd",
                    fargs.toTypedArray(),
                    false
                )
            }
        }
    }

    fun search(
        rawQuery: String,
        categoryIds: Set<Long> = emptySet(),
        minRating: Int? = null,
        sort: SortOrder = SortOrder.NUMBER_DESC,
        statusMode: StatusMode? = null,
        /** v1.65 content filters (ANDed); empty = no filtering. */
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null,
        excludedCategoryIds: Set<Long> = emptySet(),
        excludedMaturities: Set<Int> = emptySet(),
        excludedCasts: Set<CastSize> = emptySet(),
        /**
         * v1.74.5 regex mode: SQL applies every filter except the text
         * constraint; the caller applies the compiled pattern in Kotlin.
         */
        useRegex: Boolean = false
    ): Flow<PagingData<Quote>> {
        val orderBy = when (sort) {
            SortOrder.NUMBER_ASC -> "number ASC"
            SortOrder.NUMBER_DESC -> "number DESC"
            SortOrder.RATING_ASC -> "rating IS NULL, rating ASC, number ASC"
            SortOrder.RATING_DESC -> "rating IS NULL, rating DESC, number DESC"
        }
        val (joinWhere, args, empty) = searchWhere(
            rawQuery, categoryIds, minRating, statusMode,
            maturities, cast, excludedCategoryIds, excludedMaturities, excludedCasts,
            skipText = useRegex
        )
        val sqliteQuery = if (empty) {
            SimpleSQLiteQuery("SELECT * FROM quotes WHERE 0")
        } else {
            SimpleSQLiteQuery(
                "SELECT DISTINCT quotes.* FROM quotes $joinWhere ORDER BY $orderBy",
                args
            )
        }
        return Pager(PagingConfig(pageSize = 25, enablePlaceholders = false)) {
            dao.searchPaged(sqliteQuery)
        }.flow
    }

    /**
     * Counts quotes matching the same criteria as [search], reusing its
     * WHERE construction (no paging, single COUNT query). Used by the
     * new-import search alerts so only the just-imported database is
     * scanned. Length filters and notes scope are not applied to the
     * alert count.
     */
    suspend fun countMatches(
        rawQuery: String,
        categoryIds: Set<Long> = emptySet(),
        minRating: Int? = null,
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null,
        excludedCategoryIds: Set<Long> = emptySet(),
        excludedMaturities: Set<Int> = emptySet(),
        excludedCasts: Set<CastSize> = emptySet()
    ): Int {
        val (joinWhere, args, empty) = searchWhere(
            rawQuery, categoryIds, minRating, null,
            maturities, cast, excludedCategoryIds, excludedMaturities, excludedCasts
        )
        if (empty) return 0
        return dao.rawCount(
            SimpleSQLiteQuery(
                "SELECT COUNT(DISTINCT quotes.number) FROM quotes $joinWhere",
                args
            )
        )
    }

    /**
     * Non-paged text search returning up to [limit] matches, newest first.
     * Used by the bulk-categorize screen to search across databases. A
     * pure-digit query is an exact quote-number lookup, like [search].
     */
    suspend fun searchList(
        rawQuery: String,
        limit: Int = 200,
        statusMode: StatusMode? = null,
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null
    ): List<Quote> {
        val query = rawQuery.trim()
        if (query.isEmpty()) return emptyList()
        val n = limit.coerceIn(1, 1000)
        // v1.51: the status filter never applies to exact number jumps.
        val statusAnd = andStatus(statusMode)
        val contentAnd = andMaturities(maturities) + andCast(cast)
        val sqliteQuery = if (query.toIntOrNull() != null) {
            SimpleSQLiteQuery(
                "SELECT * FROM quotes WHERE number = ? LIMIT $n",
                arrayOf<Any>(query.toInt())
            )
        } else {
            // v1.58: typo-tolerant like [search]; the boolean operators live
            // in searchWhere, this list path only handles plain queries.
            val (frag, fargs) = fuzzyTermFragment(query, exactOnly = false)
            SimpleSQLiteQuery(
                "SELECT * FROM quotes WHERE $frag$statusAnd$contentAnd " +
                    "ORDER BY number DESC LIMIT $n",
                fargs.toTypedArray()
            )
        }
        return dao.rawQuotes(sqliteQuery)
    }

    /**
     * Loads a quote and records the view in "previously viewed" history.
     * v1.53: EVERY view is logged — the per-quote row keeps the latest
     * timestamp (for ordering) and the view log gains one row per view
     * (for the count and the expandable datetime list).
     */
    suspend fun getQuote(number: Int): Quote? {
        val quote = dao.getByNumber(number)
        if (quote != null) {
            val now = System.currentTimeMillis()
            meta.recordView(HistoryEntry(dbId = dbId, number = number, viewedAt = now))
            meta.logView(HistoryView(dbId = dbId, number = number, viewedAt = now))
        }
        return quote
    }

    suspend fun peekQuote(number: Int): Quote? = dao.getByNumber(number)

    /** Nearest existing quote numbers below/above [n] (for swipe navigation). */
    suspend fun prevNumber(n: Int): Int? = dao.prevNumber(n)
    suspend fun nextNumber(n: Int): Int? = dao.nextNumber(n)

    /** First/last quote numbers (null when the database is empty). */
    suspend fun firstNumber(): Int? = dao.minNumber()
    suspend fun lastNumber(): Int? = dao.maxNumber()

    suspend fun randomQuote(): Quote? = dao.randomQuote()

    suspend fun randomQuotes(
        limit: Int,
        categoryIds: Set<Long> = emptySet(),
        excludeNumbers: List<Int> = emptyList(),
        statusMode: StatusMode? = null,
        maturities: List<MaturityLevel> = emptyList(),
        cast: CastSize? = null
    ): List<Quote> {
        val n = limit.coerceIn(1, 500)
        val join = categoryJoin(categoryIds)
        // v1.49 "random without repeats": exclude already-shown quote
        // numbers at the SQL level so the sampled batch is actually fresh.
        // Numbers are our own Ints, so inlining them is safe; chunked to
        // stay under SQLite's bind-parameter limits on old devices.
        val notIn = excludeNumbers.distinct().chunked(900)
            .joinToString(" AND ") { chunk ->
                "quotes.number NOT IN (${chunk.joinToString(",")})"
            }
        // v1.51 per-database status filter joins the exclusion conditions.
        val conds = mutableListOf<String>()
        if (notIn.isNotEmpty()) conds.add("($notIn)")
        statusMode?.sqlWhere()?.let { conds.add(it) }
        // v1.65 content filters.
        for (m in maturities) {
            if (m != MaturityLevel.EVERYTHING) conds.add("(${m.sqlWhere()})")
        }
        cast?.let { conds.add("(${it.sqlWhere()})") }
        val where = if (conds.isEmpty()) "" else "WHERE ${conds.joinToString(" AND ")} "
        return if (categoryIds.isEmpty() && conds.isEmpty()) {
            dao.randomQuotes(n)
        } else {
            dao.rawQuotes(
                SimpleSQLiteQuery(
                    "SELECT DISTINCT quotes.* FROM quotes $join${where}ORDER BY RANDOM() LIMIT $n"
                )
            )
        }
    }

    /**
     * Deterministic pick for a calendar day: the same quote all day, a
     * different one each day. Based on the date's epoch day modulo the
     * quote count, so it needs no stored state.
     */
    suspend fun quoteOfTheDay(date: LocalDate = LocalDate.now()): Quote? {
        val count = dao.count()
        if (count <= 0) return null
        val offset = Math.floorMod(date.toEpochDay(), count.toLong()).toInt()
        return dao.getByOffset(offset)
    }

    /**
     * Quote of the Day that honors the content filter: when the
     * deterministic pick for [date] is hidden, walks forward through the
     * database (wrapping) and returns the first visible quote. All QotD
     * surfaces (card, notification, history) use this so they agree.
     */
    suspend fun visibleQuoteOfTheDay(
        date: LocalDate = LocalDate.now(),
        isHidden: (Quote) -> Boolean
    ): Quote? {
        val count = dao.count()
        if (count <= 0) return null
        val base = Math.floorMod(date.toEpochDay(), count.toLong()).toInt()
        val tries = minOf(count, 200)
        for (i in 0 until tries) {
            val q = dao.getByOffset((base + i) % count) ?: continue
            if (!isHidden(q)) return q
        }
        // Pathological (everything hidden): return null so every QotD
        // surface renders an empty/placeholder state instead of leaking a
        // hidden quote.
        return null
    }

    suspend fun topRated(
        limit: Int,
        offset: Int,
        categoryIds: Set<Long> = emptySet(),
        statusMode: StatusMode? = null
    ): List<Quote> {
        val status = statusMode?.let { if (it == StatusMode.BOTH) null else it.key }
        return if (categoryIds.isEmpty() && status == null) {
            dao.topRated(limit, offset)
        } else if (categoryIds.isEmpty()) {
            dao.topRatedByStatus(status!!, limit, offset)
        } else {
            dao.rawQuotes(
                SimpleSQLiteQuery(
                    "SELECT DISTINCT quotes.* FROM quotes ${categoryJoin(categoryIds)}" +
                        withStatus("", statusMode) +
                        "ORDER BY rating IS NULL, rating DESC " +
                        "LIMIT $limit OFFSET $offset"
                )
            )
        }
    }

    suspend fun bottomRated(
        limit: Int,
        offset: Int,
        categoryIds: Set<Long> = emptySet(),
        statusMode: StatusMode? = null
    ): List<Quote> {
        val status = statusMode?.let { if (it == StatusMode.BOTH) null else it.key }
        return if (categoryIds.isEmpty() && status == null) {
            dao.bottomRated(limit, offset)
        } else if (categoryIds.isEmpty()) {
            dao.bottomRatedByStatus(status!!, limit, offset)
        } else {
            dao.rawQuotes(
                SimpleSQLiteQuery(
                    "SELECT DISTINCT quotes.* FROM quotes ${categoryJoin(categoryIds)}" +
                        withStatus("", statusMode) +
                        "ORDER BY rating IS NULL, rating ASC " +
                        "LIMIT $limit OFFSET $offset"
                )
            )
        }
    }

    /** All non-null ratings, for the stats rating-distribution histogram. */
    suspend fun allRatings(): List<Int> = dao.allRatings()

    suspend fun quoteCount(categoryIds: Set<Long> = emptySet()): Int =
        if (categoryIds.isEmpty()) {
            dao.count()
        } else {
            dao.rawCount(
                SimpleSQLiteQuery(
                    "SELECT COUNT(DISTINCT quotes.number) FROM quotes ${categoryJoin(categoryIds)}"
                )
            )
        }

    suspend fun getAllQuotes(categoryIds: Set<Long> = emptySet()): List<Quote> =
        if (categoryIds.isEmpty()) {
            dao.getAll()
        } else {
            dao.rawQuotes(
                SimpleSQLiteQuery(
                    "SELECT DISTINCT quotes.* FROM quotes ${categoryJoin(categoryIds)}" +
                        "ORDER BY number"
                )
            )
        }

    fun favorites(): Flow<List<Quote>> =
        meta.favoriteEntries(dbId).map { entries ->
            if (entries.isEmpty()) {
                emptyList()
            } else {
                val byNumber = dao.getByNumbers(entries.map { it.number })
                    .associateBy { it.number }
                entries.mapNotNull { byNumber[it.number] }
            }
        }

    fun isFavorite(number: Int): Flow<Boolean> = meta.isFavorite(dbId, number)

    suspend fun toggleFavorite(quote: Quote) {
        if (meta.favoriteCount(dbId, quote.number) > 0) {
            meta.removeFavorite(dbId, quote.number)
        } else {
            meta.addFavorite(Favorite(dbId = dbId, number = quote.number))
        }
    }

    fun history(): Flow<List<Quote>> =
        meta.historyEntries(dbId).map { entries ->
            if (entries.isEmpty()) {
                emptyList()
            } else {
                val byNumber = dao.getByNumbers(entries.map { it.number })
                    .associateBy { it.number }
                entries.mapNotNull { byNumber[it.number] }
            }
        }

    /**
     * v1.53: history entries with the most-recent viewed timestamp and the
     * total view count per quote (from the every-view log). Entries whose
     * quote no longer exists are dropped; a viewedAt of 0 or less means
     * "unknown" and the caller should display "Unknown date" rather than
     * crash.
     */
    fun historyWithTimes(): Flow<List<HistoryItem>> =
        meta.historyEntries(dbId).map { entries ->
            if (entries.isEmpty()) {
                emptyList()
            } else {
                val byNumber = dao.getByNumbers(entries.map { it.number })
                    .associateBy { it.number }
                entries.mapNotNull { e ->
                    byNumber[e.number]?.let { q ->
                        // v1.77: stamp the true source db so mixed-database
                        // views theme each card correctly.
                        HistoryItem(q, e.viewedAt, meta.viewCount(dbId, e.number), dbId)
                    }
                }
            }
        }

    suspend fun clearHistory() {
        meta.clearHistory(dbId)
        meta.clearHistoryViews(dbId)
    }

    /** Every recorded view datetime for one quote, newest first. */
    suspend fun viewTimes(number: Int): List<Long> = meta.viewTimes(dbId, number)

    // --- User quote management (the "My Quotes" database) ---

    /** Inserts a quote, assigning the next free number. Returns the new quote. */
    suspend fun addQuote(text: String, rating: Int?, url: String = ""): Quote {
        val number = (dao.maxNumber() ?: 0) + 1
        val quote = Quote(number = number, rating = rating, text = text, url = url)
        dao.insert(quote)
        // v1.47 auto-categorize: tag the new quote with matching rule
        // phrases (user phrases + the built-in "Offensive" list). Never
        // fails the insert.
        try {
            categories.applyAutoRules(listOf(number))
        } catch (e: Exception) {
            android.util.Log.w("AutoRules", "tagging failed for new quote $number", e)
        }
        return quote
    }

    suspend fun updateQuote(quote: Quote) = dao.update(quote)

    /** Deletes a quote and its category mappings, favorites, and history entries. */
    suspend fun deleteQuote(number: Int) {
        dao.deleteByNumber(number)
        categories.removeQuote(number)
        meta.removeFavorite(dbId, number)
        meta.removeHistoryEntry(dbId, number)
        meta.removeHistoryViews(dbId, number)
    }

    /** Number of the first quote with exactly this text, or null. */
    suspend fun findDuplicate(text: String): Int? = dao.findNumberByText(text)
}
