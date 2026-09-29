package com.quotebrowser.app.data

import com.quotebrowser.app.ui.repoFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** One member of a duplicate group. */
data class DupQuote(val dbId: String, val quote: Quote)

/**
 * A set of quotes that are exact or near duplicates of each other.
 * [exact] = normalized text identical; otherwise word-set similarity ≥ 0.8.
 */
data class DupGroup(val members: List<DupQuote>, val exact: Boolean)

private fun normalizeDupText(text: String): String =
    text.lowercase().replace("\\s+".toRegex(), " ").trim()

private fun wordSet(text: String): Set<String> =
    text.split(' ').filter { it.length > 2 }.toSet()

private fun jaccard(a: Set<String>, b: Set<String>): Double {
    if (a.isEmpty() || b.isEmpty()) return 0.0
    val inter = a.intersect(b).size.toDouble()
    return inter / (a.size + b.size - inter)
}

/**
 * Finds exact and near-duplicate quotes across (and within) all databases.
 * Runs on Dispatchers.Default; 24k quotes scan in well under a second.
 */
suspend fun findDuplicateGroups(manager: DatabaseManager): List<DupGroup> =
    withContext(Dispatchers.Default) {
        val all = mutableListOf<DupQuote>()
        val dbs = try {
            manager.availableDatabases().first()
        } catch (e: Exception) {
            emptyList()
        }
        for (db in dbs) {
            try {
                val quotes = manager.repoFor(db.id).getAllQuotes()
                for (q in quotes) all.add(DupQuote(db.id, q))
            } catch (e: Exception) {
            }
        }
        if (all.isEmpty()) return@withContext emptyList()

        val norm = all.associateWith { normalizeDupText(it.quote.text) }
        val groups = mutableListOf<DupGroup>()
        val claimed = mutableSetOf<DupQuote>()

        // Exact duplicates: identical normalized text.
        val byText = mutableMapOf<String, MutableList<DupQuote>>()
        for (dq in all) {
            val key = norm.getValue(dq)
            if (key.length < 20) continue // skip trivially short quotes
            byText.getOrPut(key) { mutableListOf() }.add(dq)
        }
        for ((_, members) in byText) {
            val distinct = members.distinctBy { it.dbId to it.quote.number }
            if (distinct.size > 1) {
                groups.add(DupGroup(distinct, exact = true))
                claimed.addAll(distinct)
            }
        }

        // Near duplicates: block by (first 40 chars, length bucket), then
        // require word-set Jaccard >= 0.8 and length within 20%.
        val rest = all.filter { it !in claimed && norm.getValue(it).length >= 40 }
        val blocks = mutableMapOf<String, MutableList<DupQuote>>()
        for (dq in rest) {
            val n = norm.getValue(dq)
            val key = n.take(40) + "|" + (n.length / 100)
            blocks.getOrPut(key) { mutableListOf() }.add(dq)
        }
        val nearClaimed = mutableSetOf<DupQuote>()
        for ((_, block) in blocks) {
            if (block.size < 2 || block.size > 500) continue
            val words = block.associateWith { wordSet(norm.getValue(it)) }
            val lens = block.associateWith { norm.getValue(it).length }
            for (i in block.indices) {
                if (block[i] in nearClaimed) continue
                val group = mutableListOf(block[i])
                for (j in i + 1 until block.size) {
                    if (block[j] in nearClaimed) continue
                    val li = lens.getValue(block[i])
                    val lj = lens.getValue(block[j])
                    val longer = maxOf(li, lj).toDouble()
                    if (kotlin.math.abs(li - lj) / longer > 0.2) continue
                    if (jaccard(words.getValue(block[i]), words.getValue(block[j])) >= 0.8) {
                        group.add(block[j])
                    }
                }
                if (group.size > 1) {
                    groups.add(DupGroup(group.distinctBy { it.dbId to it.quote.number }, exact = false))
                    nearClaimed.addAll(group)
                }
            }
        }

        groups.sortedWith(
            compareByDescending<DupGroup> { it.exact }
                .thenByDescending { it.members.size }
        )
    }

/**
 * v1.59: true when this database id belongs to Bash.org. Bundled ids look
 * like "asset:bash_org.db" (the canonical archive); the substring match
 * also covers user-imported copies whose file name mentions bash.
 */
private fun isBashOrgDb(dbId: String): Boolean =
    dbId.contains("bash", ignoreCase = true)

/**
 * v1.59: the canonical member of a duplicate group. When an exact
 * cross-database duplicate includes a Bash.org copy, that copy is
 * canonical — it is the original archive the mirrors derive from.
 * Near-duplicate groups, and exact groups with no Bash.org member, keep
 * the existing first-member behavior.
 */
val DupGroup.canonical: DupQuote
    get() = if (exact) {
        members.firstOrNull { isBashOrgDb(it.dbId) } ?: members.first()
    } else {
        members.first()
    }
