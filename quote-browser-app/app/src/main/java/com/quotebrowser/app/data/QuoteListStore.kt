package com.quotebrowser.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * v1.66 user quote lists ("Collections" in the UI): named, reorderable,
 * persistent lists of quotes. Each item is a (database id, quote number)
 * pair — the quote text is always read live from its database, so lists
 * survive database refreshes. Backed by SharedPreferences
 * ("quote_lists_v166") as JSON, same pattern as [QueueStore].
 */
data class QuoteListItem(val dbId: String, val number: Int) {
    fun toJson(): JSONObject = JSONObject()
        .put("db", dbId)
        .put("n", number)

    companion object {
        fun fromJson(o: JSONObject): QuoteListItem? = try {
            QuoteListItem(o.getString("db"), o.getInt("n"))
        } catch (_: Exception) {
            null
        }
    }
}

data class QuoteList(
    val id: String,
    val name: String,
    val items: List<QuoteListItem>,
    val createdAt: Long = System.currentTimeMillis()
)

class QuoteListStore(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "quote_lists_v166"
        private const val KEY_LISTS = "lists"
    }

    private fun readLists(): MutableList<QuoteList> {
        val out = mutableListOf<QuoteList>()
        val raw = prefs.getString(KEY_LISTS, null) ?: return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                val itemsArr = o.optJSONArray("items")
                val items = mutableListOf<QuoteListItem>()
                if (itemsArr != null) {
                    for (j in 0 until itemsArr.length()) {
                        itemsArr.optJSONObject(j)?.let {
                            QuoteListItem.fromJson(it)?.let(items::add)
                        }
                    }
                }
                val id = o.optString("id", "")
                val name = o.optString("name", "")
                if (id.isNotEmpty() && name.isNotEmpty()) {
                    out.add(
                        QuoteList(
                            id = id,
                            name = name,
                            items = items,
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Corrupt JSON: start fresh.
        }
        return out
    }

    private fun writeLists(lists: List<QuoteList>) {
        val arr = JSONArray()
        for (l in lists) {
            val itemsArr = JSONArray()
            l.items.forEach { itemsArr.put(it.toJson()) }
            arr.put(
                JSONObject()
                    .put("id", l.id)
                    .put("name", l.name)
                    .put("items", itemsArr)
                    .put("createdAt", l.createdAt)
            )
        }
        prefs.edit().putString(KEY_LISTS, arr.toString()).apply()
    }

    /** All lists, oldest first. */
    fun lists(): List<QuoteList> = readLists()

    fun get(id: String): QuoteList? = readLists().firstOrNull { it.id == id }

    /** Creates a list; returns null when the name is blank. */
    fun create(name: String): QuoteList? {
        val clean = name.trim()
        if (clean.isEmpty()) return null
        val lists = readLists()
        val list = QuoteList(
            id = UUID.randomUUID().toString(),
            name = clean,
            items = emptyList()
        )
        lists.add(list)
        writeLists(lists)
        return list
    }

    fun rename(id: String, name: String): Boolean {
        val clean = name.trim()
        if (clean.isEmpty()) return false
        val lists = readLists()
        val i = lists.indexOfFirst { it.id == id }
        if (i < 0) return false
        lists[i] = lists[i].copy(name = clean)
        writeLists(lists)
        return true
    }

    fun delete(id: String) {
        val lists = readLists()
        val updated = lists.filter { it.id != id }
        if (updated.size != lists.size) writeLists(updated)
    }

    /**
     * Adds a quote to a list. Returns false when the list doesn't exist
     * or the quote is already in it.
     */
    fun addItem(id: String, dbId: String, number: Int): Boolean {
        val lists = readLists()
        val i = lists.indexOfFirst { it.id == id }
        if (i < 0) return false
        val list = lists[i]
        if (list.items.any { it.dbId == dbId && it.number == number }) return false
        lists[i] = list.copy(items = list.items + QuoteListItem(dbId, number))
        writeLists(lists)
        return true
    }

    fun removeItem(id: String, dbId: String, number: Int) {
        val lists = readLists()
        val i = lists.indexOfFirst { it.id == id }
        if (i < 0) return
        val list = lists[i]
        val updated = list.items.filter { it.dbId != dbId || it.number != number }
        if (updated.size != list.items.size) {
            lists[i] = list.copy(items = updated)
            writeLists(lists)
        }
    }

    /** Moves an item from [fromIndex] to [toIndex]; no-op when out of range. */
    fun moveItem(id: String, fromIndex: Int, toIndex: Int) {
        val lists = readLists()
        val i = lists.indexOfFirst { it.id == id }
        if (i < 0) return
        val items = lists[i].items.toMutableList()
        if (fromIndex !in items.indices || toIndex !in items.indices) return
        if (fromIndex == toIndex) return
        val item = items.removeAt(fromIndex)
        items.add(toIndex, item)
        lists[i] = lists[i].copy(items = items)
        writeLists(lists)
    }

    /** Ids of the lists that already contain this quote. */
    fun containingLists(dbId: String, number: Int): List<String> =
        readLists()
            .filter { l -> l.items.any { it.dbId == dbId && it.number == number } }
            .map { it.id }
}
