package com.quotebrowser.app.ui

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * v1.75: the saved-explanations manifest. Every "Explain this quote"
 * (Gemini cloud) result is cached on-device, keyed by quote
 * ("dbId|number"), so re-opening the dialog is instant and free, cards
 * can show a saved-explanation icon, and Search can filter to
 * "Has explanation" — the same shape as the voice-recordings manifest.
 *
 * The manifest lives in filesDir (persistent) and is included in full
 * backups. Only successful explanations are stored; NeedKey/Error
 * results are never cached.
 */
data class SavedExplanation(
    val dbId: String,
    val number: Int,
    /** The explanation text returned by Gemini. */
    val text: String,
    val updatedAt: Long
)

object ExplanationCache {
    private const val MANIFEST_NAME = "quote_explanations.json"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null

    private val _explanations = MutableStateFlow<Map<String, SavedExplanation>>(emptyMap())
    val explanations: StateFlow<Map<String, SavedExplanation>> = _explanations.asStateFlow()

    fun key(dbId: String, number: Int): String = "$dbId|$number"

    /** Idempotent init; safe to call from anywhere with a Context. */
    fun ensure(context: Context) {
        if (appContext != null) return
        synchronized(this) {
            if (appContext != null) return
            appContext = context.applicationContext
            _explanations.value = load()
        }
    }

    /** True when a saved explanation exists for this quote. */
    fun hasExplanation(dbId: String?, number: Int?): Boolean {
        if (dbId == null || number == null) return false
        return _explanations.value.containsKey(key(dbId, number))
    }

    /** The cached text, or null. */
    fun get(dbId: String?, number: Int?): String? {
        if (dbId == null || number == null) return null
        return _explanations.value[key(dbId, number)]?.text
    }

    /** Saves (or replaces) the explanation for a quote. Blank text is ignored. */
    fun save(context: Context, dbId: String, number: Int, text: String) {
        if (text.isBlank()) return
        ensure(context)
        val entry = SavedExplanation(
            dbId = dbId,
            number = number,
            text = text,
            updatedAt = System.currentTimeMillis()
        )
        val updated = LinkedHashMap(_explanations.value)
        // Newest first, like the voice manifest.
        updated.remove(key(dbId, number))
        val reordered = LinkedHashMap<String, SavedExplanation>()
        reordered[key(dbId, number)] = entry
        reordered.putAll(updated)
        _explanations.value = reordered
        val snapshot = reordered
        scope.launch { saveToDisk(snapshot) }
    }

    /** Deletes the cached explanation for a quote, if any. */
    fun delete(context: Context, dbId: String, number: Int) {
        ensure(context)
        val k = key(dbId, number)
        if (!_explanations.value.containsKey(k)) return
        val updated = LinkedHashMap(_explanations.value)
        updated.remove(k)
        _explanations.value = updated
        val snapshot = updated
        scope.launch { saveToDisk(snapshot) }
    }

    private fun manifestFile(): File? {
        val ctx = appContext ?: return null
        return File(ctx.filesDir, MANIFEST_NAME)
    }

    private fun load(): Map<String, SavedExplanation> {
        val file = manifestFile() ?: return emptyMap()
        if (!file.exists()) return emptyMap()
        return try {
            val root = JSONObject(file.readText())
            val arr = root.optJSONArray("entries") ?: JSONArray()
            val out = LinkedHashMap<String, SavedExplanation>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val dbId = o.optString("dbId", "")
                val number = o.optInt("number", -1)
                val text = o.optString("text", "")
                if (dbId.isEmpty() || number < 0 || text.isEmpty()) continue
                val entry = SavedExplanation(
                    dbId = dbId,
                    number = number,
                    text = text,
                    updatedAt = o.optLong("updatedAt", 0L)
                )
                out[key(dbId, number)] = entry
            }
            out
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun saveToDisk(map: Map<String, SavedExplanation>) {
        val file = manifestFile() ?: return
        try {
            val arr = JSONArray()
            for ((_, e) in map) {
                arr.put(
                    JSONObject()
                        .put("dbId", e.dbId)
                        .put("number", e.number)
                        .put("text", e.text)
                        .put("updatedAt", e.updatedAt)
                )
            }
            file.writeText(JSONObject().put("entries", arr).toString())
        } catch (_: Exception) {
            // Best-effort cache; a failed write just means it re-fetches.
        }
    }
}
