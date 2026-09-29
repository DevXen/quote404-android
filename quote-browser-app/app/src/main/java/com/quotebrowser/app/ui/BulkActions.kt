package com.quotebrowser.app.ui

import android.content.Context
import android.content.Intent
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.components.SelectedQuote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shared implementation of the multi-select bulk actions: favorites,
 * categories, export queue, read-aloud playlist, and share-as-text.
 */
class BulkActions(
    private val manager: DatabaseManager,
    private val scope: CoroutineScope
) {

    /** Loads the full quotes for a selection (for playlist/share). */
    suspend fun loadQuotes(sel: Set<SelectedQuote>): List<SourcedQuote> =
        withContext(Dispatchers.IO) {
            sel.mapNotNull { s ->
                try {
                    manager.repoFor(s.dbId).getQuote(s.number)
                        ?.let { SourcedQuote(s.dbId, it) }
                } catch (e: Exception) {
                    null
                }
            }
        }

    /** Adds every selected quote to favorites (already-favorited ones stay). */
    fun addToFavorites(sel: Set<SelectedQuote>, onDone: (String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            var added = 0
            for (s in sel) {
                if (manager.metaDb.metaDao().favoriteCount(s.dbId, s.number) == 0) {
                    val q = try {
                        manager.repoFor(s.dbId).getQuote(s.number)
                    } catch (e: Exception) {
                        null
                    } ?: continue
                    manager.repoFor(s.dbId).toggleFavorite(q)
                    added++
                }
            }
            withContext(Dispatchers.Main) {
                onDone(if (added == 0) "Already in favorites" else "$added added to favorites")
            }
        }
    }

    /** Adds every selected quote to the export queue (no duplicates). */
    fun addToQueue(sel: Set<SelectedQuote>, onDone: (String) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val current = manager.basket.first()
            var added = 0
            for (s in sel) {
                if (BasketEntry(s.dbId, s.number) !in current) {
                    manager.toggleBasket(s.dbId, s.number)
                    added++
                }
            }
            withContext(Dispatchers.Main) {
                onDone(if (added == 0) "Already in the export tray" else "$added added to export tray")
            }
        }
    }

    /**
     * Adds every selected quote to the given categories. Categories are
     * matched by name in each quote's own database (ids are per-database).
     */
    fun addToCategories(
        sel: Set<SelectedQuote>,
        names: Set<String>,
        newName: String = "",
        autoRule: String? = null,
        onDone: (String) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            var tagged = 0
            var created = 0
            val byDb = sel.groupBy { it.dbId }
            for ((dbId, quotes) in byDb) {
                val store = try {
                    manager.categoryStoreFor(dbId)
                } catch (e: Exception) {
                    continue
                }
                val ids = names.mapNotNull { store.findByName(it)?.id }.toMutableSet()
                // New category, created in every selected database: the
                // optional auto-rule phrase rides along with it.
                val cleanNew = newName.trim()
                if (cleanNew.isNotEmpty()) {
                    val newId = store.add(cleanNew)
                    if (newId != -1L) {
                        ids.add(newId)
                        created++
                        if (!autoRule.isNullOrBlank()) {
                            store.addAutoRule(newId, autoRule)
                        }
                    }
                }
                if (ids.isEmpty()) continue
                for (s in quotes) {
                    store.addMappings(s.number, ids)
                    tagged++
                }
            }
            withContext(Dispatchers.Main) {
                onDone(
                    if (tagged == 0 && created == 0) "No categories applied"
                    else "$tagged quote${if (tagged == 1) "" else "s"} categorized" +
                        if (created > 0) " (new category created)" else ""
                )
            }
        }
    }

    /** Category names of one database, for the bulk category picker. */
    suspend fun categoryNames(dbId: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                manager.categoryStoreFor(dbId).list().map { it.name }
            } catch (e: Exception) {
                emptyList()
            }
        }

    /** Shares all selected quotes as one text block through the chooser. */
    fun shareText(context: Context, sel: Set<SelectedQuote>) {
        scope.launch(Dispatchers.IO) {
            val quotes = loadQuotes(sel)
            if (quotes.isEmpty()) return@launch
            val text = quotes.joinToString("\n\n---\n\n") { sq ->
                "#${sq.quote.number} (${manager.shortLabel(sq.dbId)})\n${sq.quote.text}"
            }
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, "Share quotes"))
            }
        }
    }
}
