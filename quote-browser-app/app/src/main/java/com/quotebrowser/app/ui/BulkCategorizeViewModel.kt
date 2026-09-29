package com.quotebrowser.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.data.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One bulk-search hit, tagged with the database it came from. */
data class BulkResult(val dbId: String, val dbLabel: String, val quote: Quote)

/**
 * Search quotes across any combination of databases, checkbox-select the
 * hits, and add them to one or more categories (creating names that don't
 * exist yet, per database).
 */
class BulkCategorizeViewModel(private val manager: DatabaseManager) : ViewModel() {

    val databases: StateFlow<List<DbInfo>> = manager.availableDatabases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var query by mutableStateOf("")
        private set

    fun updateQuery(q: String) {
        query = q
    }

    /** Null = all databases. */
    private val _selectedDbIds = MutableStateFlow<Set<String>?>(null)
    val selectedDbIds: StateFlow<Set<String>?> = _selectedDbIds.asStateFlow()

    fun toggleDb(id: String) {
        val all = databases.value.map { it.id }.toSet()
        if (all.isEmpty()) return
        val cur = _selectedDbIds.value ?: all
        val next = if (id in cur) cur - id else cur + id
        _selectedDbIds.value = if (next == all || next.isEmpty()) null else next
    }

    fun selectAllDbs() {
        _selectedDbIds.value = null
    }

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching.asStateFlow()

    private val _results = MutableStateFlow<List<BulkResult>>(emptyList())
    val results: StateFlow<List<BulkResult>> = _results.asStateFlow()

    private val _selected = MutableStateFlow<Set<Pair<String, Int>>>(emptySet())
    val selected: StateFlow<Set<Pair<String, Int>>> = _selected.asStateFlow()

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status.asStateFlow()

    fun clearStatus() {
        _status.value = null
    }

    fun search() {
        val q = query.trim()
        if (q.isEmpty() || _searching.value) return
        viewModelScope.launch {
            _searching.value = true
            _status.value = null
            try {
                val found = withContext(Dispatchers.IO) {
                    val allIds = databases.value.map { it.id }
                    val ids = _selectedDbIds.value ?: allIds.toSet()
                    val labels = databases.value.associateBy({ it.id }, { it.label })
                    val out = mutableListOf<BulkResult>()
                    for (id in allIds) {
                        if (id !in ids) continue
                        val quotes = manager.repoFor(id).searchList(q, limit = 200)
                        for (quote in quotes) {
                            out.add(BulkResult(id, labels[id] ?: id, quote))
                        }
                    }
                    out
                }
                _results.value = found
                _selected.value = emptySet()
                if (found.isEmpty()) _status.value = "No quotes match \"$q\"."
            } finally {
                _searching.value = false
            }
        }
    }

    fun toggleSelect(result: BulkResult) {
        val key = result.dbId to result.quote.number
        _selected.value =
            if (key in _selected.value) _selected.value - key else _selected.value + key
    }

    fun selectAll() {
        _selected.value = _results.value.map { it.dbId to it.quote.number }.toSet()
    }

    fun clearSelection() {
        _selected.value = emptySet()
    }

    /**
     * Union of category names across the databases in the current result
     * set, for the add-to-categories picker.
     */
    val allCategoryNames: StateFlow<List<String>> =
        _results.flatMapLatest { results ->
            flow {
                val ids = results.map { it.dbId }.toSet()
                    .ifEmpty { databases.value.map { it.id }.toSet() }
                val names = mutableSetOf<String>()
                withContext(Dispatchers.IO) {
                    for (id in ids) {
                        manager.categoryStoreFor(id).list().forEach { names.add(it.name) }
                    }
                }
                emit(names.sortedWith(String.CASE_INSENSITIVE_ORDER))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Add every selected quote to each named category, creating categories
     * that don't exist yet (per database). Returns the number of quotes
     * updated.
     */
    fun applyCategories(names: List<String>) {
        val clean = names.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (clean.isEmpty() || _selected.value.isEmpty()) return
        viewModelScope.launch {
            val updated = withContext(Dispatchers.IO) {
                var count = 0
                val byDb = _selected.value.groupBy({ it.first }, { it.second })
                for ((dbId, numbers) in byDb) {
                    val store = manager.categoryStoreFor(dbId)
                    val ids = clean.map { store.add(it) }.filter { it != -1L }
                    for (number in numbers) {
                        store.addMappings(number, ids)
                        count++
                    }
                }
                count
            }
            val catWord = if (clean.size == 1) "category" else "categories"
            _status.value = "Added $updated quote(s) to ${clean.size} $catWord."
        }
    }
}
