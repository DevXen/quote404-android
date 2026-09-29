package com.quotebrowser.app.data

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * Merges the quote lists of several databases into one stream for the "All
 * databases" home view.
 *
 * Quotes are interleaved round-robin: the first quote of each database, then
 * the second of each, and so on, each database internally ordered by [sort]
 * (and the [minRating] filter applied per database). Databases with fewer
 * quotes simply drop out of later rounds, so the merged stream is dense —
 * every merged position maps to a real quote.
 *
 * Positional mapping: with per-database filtered counts c[0..R), the merged
 * index of database r's t-th quote (0-based) is
 *   mergedIndex(r, t) = sum(min(t, c[r'])) + #{r' < r : c[r'] > t}.
 * For a fixed r this is strictly increasing in t, so each page load fetches
 * one consecutive slice per database (a single LIMIT/OFFSET query each).
 */
class AllDbPagingSource(
    private val repos: List<QuoteRepository>,
    private val sort: SortOrder,
    private val minRating: Int?,
    /** v1.51: each source database's own status mode (missing = no filter). */
    private val statusModes: Map<String, StatusMode?> = emptyMap(),
    /** v1.65: content filters applied on top of every source database. */
    private val maturities: List<MaturityLevel> = emptyList(),
    private val cast: CastSize? = null
) : PagingSource<Int, SourcedQuote>() {

    /** Per-database filtered quote counts, computed once on first load. */
    private var counts: IntArray? = null

    // v1.88.16 diagnostics: one-shot startup marks for the merged pager.
    private val diagCountsDone = java.util.concurrent.atomic.AtomicBoolean(false)
    private val diagPageDone = java.util.concurrent.atomic.AtomicBoolean(false)

    private suspend fun counts(): IntArray {
        counts?.let { return it }
        val t0 = android.os.SystemClock.elapsedRealtime()
        val c = IntArray(repos.size) { i ->
            repos[i].countFiltered(
                minRating, statusModes[repos[i].dbId], maturities, cast
            )
        }
        counts = c
        if (diagCountsDone.compareAndSet(false, true)) {
            com.quotebrowser.app.StartupWatchdog.mark(
                "merged pager counts done (${android.os.SystemClock.elapsedRealtime() - t0}ms, ${repos.size} dbs)"
            )
        }
        return c
    }

    private fun mergedIndex(r: Int, t: Int, c: IntArray): Int {
        var s = 0
        for (r2 in c.indices) s += minOf(t, c[r2])
        var rank = 0
        for (r2 in 0 until r) if (c[r2] > t) rank++
        return s + rank
    }

    override fun getRefreshKey(state: PagingState<Int, SourcedQuote>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SourcedQuote> {
        return try {
            val c = counts()
            val total = c.sum()
            val page = params.key ?: 0
            val size = params.loadSize
            val start = page * size
            if (start >= total || repos.isEmpty()) {
                return LoadResult.Page(
                    emptyList(),
                    prevKey = if (page == 0) null else page - 1,
                    nextKey = null
                )
            }
            val end = minOf(start + size, total)
            val out = arrayOfNulls<SourcedQuote>(end - start)
            for (r in repos.indices) {
                val cr = c[r]
                if (cr == 0) continue
                // Smallest t with mergedIndex(r, t) >= start (binary search).
                var lo = 0
                var hi = cr
                while (lo < hi) {
                    val mid = (lo + hi) ushr 1
                    if (mergedIndex(r, mid, c) < start) lo = mid + 1 else hi = mid
                }
                var t = lo
                val t1 = t
                while (t < cr && mergedIndex(r, t, c) < end) t++
                if (t > t1) {
                    val quotes = repos[r].pageFiltered(
                        minRating, sort, t - t1, t1,
                        statusModes[repos[r].dbId], maturities, cast
                    )
                    quotes.forEachIndexed { i, q ->
                        val pos = mergedIndex(r, t1 + i, c) - start
                        if (pos in out.indices) {
                            out[pos] = SourcedQuote(repos[r].dbId, q)
                        }
                    }
                }
            }
            LoadResult.Page(
                data = out.filterNotNull(),
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (end >= total) null else page + 1
            ).also {
                // v1.88.16 diagnostics: first merged page delivered.
                if (page == 0 && diagPageDone.compareAndSet(false, true)) {
                    com.quotebrowser.app.StartupWatchdog.mark("merged pager first page loaded")
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
