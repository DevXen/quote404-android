package com.quotebrowser.app.data

import android.content.Context
import org.json.JSONArray

/**
 * "Up Next" queue: an ordered list of quote keys ("dbId/number") the user
 * wants to read next, plus the current playback position. Backed by
 * SharedPreferences ("upnext_v144") so it survives restarts.
 */
class QueueStore(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "upnext_v144"
        private const val KEY_QUEUE = "queue"
        private const val KEY_POSITION = "position"
    }

    private fun keyFor(dbId: String, number: Int) = "$dbId/$number"

    private fun readQueue(): MutableList<Pair<String, Int>> {
        val out = mutableListOf<Pair<String, Int>>()
        val raw = prefs.getString(KEY_QUEUE, null) ?: return out
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val part = arr.optString(i, "")
                val dbId = part.substringBeforeLast("/")
                val number = part.substringAfterLast("/").toIntOrNull()
                if (dbId.isNotEmpty() && number != null) out.add(dbId to number)
            }
        } catch (_: Exception) {
            // Corrupt JSON: start fresh.
        }
        return out
    }

    private fun writeQueue(queue: List<Pair<String, Int>>) {
        val arr = JSONArray()
        queue.forEach { (dbId, number) -> arr.put(keyFor(dbId, number)) }
        prefs.edit().putString(KEY_QUEUE, arr.toString()).apply()
        // Keep the position valid after structural changes.
        val last = (queue.size - 1).coerceAtLeast(0)
        val clamped = position().coerceIn(0, last)
        if (clamped != position()) setPosition(clamped)
    }

    /** The queue in order, as (database id, quote number) pairs. */
    fun queue(): List<Pair<String, Int>> = readQueue()

    /**
     * Adds a quote unless it's already queued. Returns true when added,
     * false when it was already in the queue.
     */
    fun add(dbId: String, number: Int): Boolean {
        val q = readQueue()
        if (q.any { it.first == dbId && it.second == number }) return false
        q.add(dbId to number)
        writeQueue(q)
        return true
    }

    /** Adds several quotes, skipping ones already queued. */
    fun addAll(entries: List<Pair<String, Int>>) {
        if (entries.isEmpty()) return
        val q = readQueue()
        var changed = false
        for (e in entries) {
            if (q.none { it.first == e.first && it.second == e.second }) {
                q.add(e)
                changed = true
            }
        }
        if (changed) writeQueue(q)
    }

    /** Removes the entry at [i]; no-op when out of range. */
    fun removeAt(i: Int) {
        val q = readQueue()
        if (i !in q.indices) return
        q.removeAt(i)
        writeQueue(q)
    }

    /** Moves the entry at [from] to [to]; no-op when out of range. */
    fun move(from: Int, to: Int) {
        val q = readQueue()
        if (from !in q.indices || to !in q.indices || from == to) return
        val e = q.removeAt(from)
        q.add(to, e)
        writeQueue(q)
    }

    /** Empties the queue and resets the position. */
    fun clear() {
        prefs.edit().remove(KEY_QUEUE).remove(KEY_POSITION).apply()
    }

    /** Current playback position (index into [queue]). */
    fun position(): Int = prefs.getInt(KEY_POSITION, 0)

    /** Sets the current playback position. */
    fun setPosition(i: Int) {
        prefs.edit().putInt(KEY_POSITION, i.coerceAtLeast(0)).apply()
    }
}
