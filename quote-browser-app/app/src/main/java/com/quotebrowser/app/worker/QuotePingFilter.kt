package com.quotebrowser.app.worker

import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * v1.74: the single quote picker for notification pings.
 *
 * [QuotePingWorker] (the initial notification) and [QotdActionReceiver]'s
 * Next action both call [pickPingQuote], so the two paths share
 * byte-identical filtering and can never drift apart.
 *
 * All filtering is SQL-backed per database ([QuoteRepository.pingCandidates]):
 * the effective maturity ceiling (the notification setting, or the
 * work-hours ceiling while at work), the minimum rating, the
 * mood/category name filter, and the recently-notified exclusion. The
 * user's never-show-again / muted-nick / content-filter blocks apply on
 * top as shared Kotlin checks, and the picked quote is recorded via
 * [DatabaseManager.recordPingNotified] by the caller after posting.
 *
 * Returns null when no database yields a visible quote.
 */
suspend fun pickPingQuote(manager: DatabaseManager): Pair<String, Quote>? =
    withContext(Dispatchers.IO) {
        try {
            val now = LocalDateTime.now()
            val configured = manager.quotePingDbIds.value?.takeIf { it.isNotEmpty() }
                ?: manager.allDbIds().toSet()
            if (configured.isEmpty()) return@withContext null
            val ceiling = manager.effectivePingCeiling(now)
            val minRating = manager.quotePingMinRating.value
            val categoryNames = manager.quotePingCategoryNames.value
            val recencyDays = manager.quotePingRecencyDays.value
            val cutoff = if (recencyDays > 0)
                System.currentTimeMillis() - recencyDays * 86_400_000L
            else 0L
            val dbOrder = configured.shuffled()
            repeat(40) { attempt ->
                val id = dbOrder[attempt % dbOrder.size]
                val repo = try {
                    manager.repoFor(id)
                } catch (e: Exception) {
                    return@repeat
                }
                val recent =
                    if (cutoff > 0L) manager.recentPingNumbers(id, cutoff) else emptySet()
                val candidates = try {
                    repo.pingCandidates(ceiling, minRating, categoryNames, recent)
                } catch (e: Exception) {
                    // e.g. a database whose category tables were never
                    // created while the name filter is active.
                    return@repeat
                }
                for (q in candidates) {
                    if (!manager.isQuoteHiddenOrMuted(id, q.number, q.text) &&
                        !manager.isHiddenByContentFilter(q.text)
                    ) {
                        return@withContext id to q
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
