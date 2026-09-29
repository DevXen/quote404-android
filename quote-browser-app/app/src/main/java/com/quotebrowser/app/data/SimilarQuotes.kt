package com.quotebrowser.app.data

import com.quotebrowser.app.ui.categoryStoreFor
import com.quotebrowser.app.ui.extractNicks
import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A quote ranked by text similarity to a source quote. */
data class ScoredQuote(val dbId: String, val quote: Quote, val score: Double)

private val Stopwords = setOf(
    "the", "and", "for", "are", "but", "not", "you", "all", "any", "can",
    "had", "her", "was", "one", "our", "out", "day", "get", "has", "him",
    "his", "how", "its", "may", "new", "now", "old", "see", "two", "who",
    "boy", "did", "she", "use", "your", "that", "this", "with", "from",
    "they", "have", "will", "would", "there", "their", "what", "about",
    "which", "when", "make", "like", "just", "know", "take", "into",
    "your", "good", "some", "them", "than", "then", "look", "only",
    "come", "over", "think", "also", "back", "after", "first", "well",
    "even", "want", "because", "these", "give", "most", "much", "more",
    "very", "said", "says", "say", "told", "tell", "went", "got", "get",
    "going", "don", "didn", "doesn", "isn", "aren", "wasn", "weren",
    "hasn", "haven", "couldn", "wouldn", "shouldn", "won", "can"
)

private fun tokenize(text: String): Set<String> =
    text.lowercase().split(Regex("[^a-z0-9']+"))
        .filter { it.length > 2 && it !in Stopwords }
        .toSet()

/**
 * Token-overlap similarity: the share of the smaller quote's tokens that
 * also appear in the larger one. Ranges 0..1.
 */
fun tokenOverlapScore(a: Set<String>, b: Set<String>): Double {
    if (a.isEmpty() || b.isEmpty()) return 0.0
    val inter = a.intersect(b).size.toDouble()
    return inter / minOf(a.size, b.size)
}

/**
 * Rate-as-you-read taste boost, precomputed once per "More like this"
 * search: the lowercase nicks whose average personal rating is >= 4.0
 * stars, plus (per searched database) the quote numbers sitting in a
 * category whose average personal rating is >= 4.0. Null when the user
 * has rated nothing (or the store is absent), so the ranking stays pure
 * token-overlap.
 */
private data class TasteBoost(
    val highNicks: Set<String>,
    val highCategoryNumbers: Map<String, Set<Int>>
)

private suspend fun buildTasteBoost(
    manager: DatabaseManager,
    ratings: PersonalRatings,
    dbIds: Set<String>
): TasteBoost? {
    val all = ratings.allRatings()
    if (all.isEmpty()) return null
    // Nick averages are global across databases: the same nick is the
    // same person. Only rated quotes contribute, so this is one cheap
    // lookup per rated quote.
    val nickSum = mutableMapOf<String, Double>()
    val nickCount = mutableMapOf<String, Int>()
    for ((id, stars) in all) {
        val dbId = id.substringBeforeLast('/')
        val number = id.substringAfterLast('/').toIntOrNull() ?: continue
        val text = try {
            manager.repoFor(dbId).getQuote(number)?.text
        } catch (_: Exception) {
            null
        } ?: continue
        for (nick in extractNicks(text)) {
            val key = nick.lowercase()
            nickSum[key] = (nickSum[key] ?: 0.0) + stars
            nickCount[key] = (nickCount[key] ?: 0) + 1
        }
    }
    val highNicks = nickSum
        .filter { (nick, sum) -> sum / (nickCount[nick] ?: 1) >= 4.0 }
        .keys
    // Category averages are per database (each database file carries its
    // own categories). One bulk query per category; only categories with
    // at least one rated quote can qualify.
    val highNumbers = mutableMapOf<String, Set<Int>>()
    for (dbId in dbIds) {
        val catStore = try {
            manager.categoryStoreFor(dbId)
        } catch (_: Exception) {
            continue
        }
        val cats = try {
            catStore.list()
        } catch (_: Exception) {
            continue
        }
        val good = mutableSetOf<Int>()
        for (cat in cats) {
            val nums = try {
                catStore.quoteNumbersInCategory(cat.id)
            } catch (_: Exception) {
                continue
            }
            var sum = 0
            var count = 0
            for (n in nums) {
                val s = all["$dbId/$n"] ?: continue
                sum += s
                count++
            }
            if (count > 0 && sum.toDouble() / count >= 4.0) good.addAll(nums)
        }
        if (good.isNotEmpty()) highNumbers[dbId] = good
    }
    if (highNicks.isEmpty() && highNumbers.isEmpty()) return null
    return TasteBoost(highNicks, highNumbers)
}

/**
 * Ranks quotes by token-overlap similarity to [source]. Searches
 * [sourceDbId] by default; pass explicit [dbIds] for an all-database
 * (or multi-database) search. Hidden, muted-nick, and word-filtered quotes
 * are excluded. When [personalRatings] is provided and the user has rated
 * quotes, nicks/categories averaging >= 4.0 stars get a small additive
 * bonus (token-overlap scores range 0..1, so +0.15/+0.10 is a 10-20%
 * nudge, not a takeover). Runs on Dispatchers.Default.
 */
suspend fun findSimilarQuotes(
    manager: DatabaseManager,
    sourceDbId: String,
    source: Quote,
    limit: Int = 10,
    dbIds: Set<String>? = null,
    personalRatings: PersonalRatings? = null
): List<ScoredQuote> = withContext(Dispatchers.Default) {
    val sourceTokens = tokenize(source.text)
    if (sourceTokens.isEmpty()) return@withContext emptyList()
    val ids = dbIds ?: setOf(sourceDbId)
    val taste = if (personalRatings != null) buildTasteBoost(manager, personalRatings, ids) else null
    val scored = mutableListOf<ScoredQuote>()
    for (id in ids) {
        val quotes = try {
            manager.repoFor(id).getAllQuotes()
        } catch (_: Exception) {
            continue
        }
        for (q in quotes) {
            if (id == sourceDbId && q.number == source.number) continue
            if (manager.isQuoteHiddenFromLists(id, q.number, q.text)) continue
            var score = tokenOverlapScore(sourceTokens, tokenize(q.text))
            if (score > 0.0) {
                if (taste != null) {
                    if (extractNicks(q.text).any { it.lowercase() in taste.highNicks }) {
                        score += 0.15
                    }
                    if (taste.highCategoryNumbers[id]?.contains(q.number) == true) {
                        score += 0.10
                    }
                }
                scored.add(ScoredQuote(id, q, score))
            }
        }
    }
    scored.sortedWith(
        compareByDescending<ScoredQuote> { it.score }
            .thenBy { it.dbId }
            .thenBy { it.quote.number }
    ).take(limit)
}
