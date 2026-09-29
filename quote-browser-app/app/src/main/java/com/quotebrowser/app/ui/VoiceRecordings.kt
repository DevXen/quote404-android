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
 * v1.61.20: the voice-recordings manifest. Every read-aloud (Gemini cloud)
 * synthesis is content-keyed, so the cache alone can't say which quote an
 * audio file belongs to — this manifest maps a quote ("dbId|number") to
 * its generated recording: the active generation plus older generations
 * kept as alternates when a nick's voice changes.
 *
 * When a voice assignment changes, the next play synthesizes fresh audio
 * (the cache key includes the voices); [record] moves the previous
 * generation into [VoiceRecording.alternates] instead of orphaning it, and
 * the card icon + Settings → Voice recordings let Clint play, delete,
 * regenerate, or pick which generation is active. The manifest lives in
 * filesDir (persistent) and is included in full backups; [referencedFiles]
 * keeps the LRU cache prune from deleting files the manifest still points
 * at.
 */
/** One generated version of a quote's read-aloud audio. */
data class VoiceGeneration(
    /** Cache file names in play order. */
    val files: List<String>,
    /** Voice summary for display, e.g. "Zephyr + Aoede". */
    val voices: String,
    val updatedAt: Long,
    /**
     * v1.65: the voice each nick was synthesized with (lowercased nick to
     * voice name), so a later re-record can keep other nicks' existing
     * audio instead of regenerating it. Empty for pre-v1.65 recordings.
     */
    val nickVoices: Map<String, String> = emptyMap()
)

data class VoiceRecording(
    val dbId: String,
    val number: Int,
    val active: VoiceGeneration,
    /** Older generations, newest first. */
    val alternates: List<VoiceGeneration> = emptyList()
)

object VoiceRecordings {
    private const val MANIFEST_NAME = "voice_recordings.json"
    private const val MAX_ALTERNATES = 3

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null
    private var ttsDir: File? = null

    private val _recordings = MutableStateFlow<Map<String, VoiceRecording>>(emptyMap())
    val recordings: StateFlow<Map<String, VoiceRecording>> = _recordings.asStateFlow()

    fun key(dbId: String, number: Int): String = "$dbId|$number"

    /** Idempotent init; safe to call from anywhere with a Context. */
    fun ensure(context: Context) {
        if (appContext != null) return
        synchronized(this) {
            if (appContext != null) return
            appContext = context.applicationContext
            ttsDir = File(appContext!!.filesDir, "gemini_tts").apply { mkdirs() }
            _recordings.value = load()
            // v1.74.8: drop phantom entries whose audio files are gone
            // (failed writes, prunes, or user-cleared data) so "Has audio"
            // never lists a quote with nothing playable.
            sweepStaleFiles()
        }
    }

    /**
     * Removes manifest file references that no longer exist on disk, and
     * drops entries left with no playable audio at all. The intentional
     * "regenerate pending" shape (empty active + live alternates) is kept.
     */
    private fun sweepStaleFiles() {
        val ctx = appContext ?: return
        val current = _recordings.value
        if (current.isEmpty()) return
        var changed = false
        val out = LinkedHashMap<String, VoiceRecording>()
        for ((k, r) in current) {
            val liveActive = r.active.files.filter { audioFile(it) != null }
            val liveAlts = r.alternates.mapNotNull { g ->
                val live = g.files.filter { audioFile(it) != null }
                if (live.isEmpty()) null else g.copy(files = live)
            }
            if (liveActive.isEmpty() && liveAlts.isEmpty() &&
                (r.active.files.isNotEmpty() || r.alternates.isNotEmpty())
            ) {
                // Fully dead entry: nothing playable remains.
                changed = true
                continue
            }
            if (liveActive.size != r.active.files.size ||
                liveAlts.size != r.alternates.size ||
                liveAlts.zip(r.alternates).any { (a, b) -> a.files != b.files }
            ) {
                changed = true
                out[k] = r.copy(
                    active = r.active.copy(files = liveActive),
                    alternates = liveAlts
                )
            } else {
                out[k] = r
            }
        }
        if (changed) {
            _recordings.value = out
            save(ctx, out)
        }
    }

    fun manifestFile(): File? =
        appContext?.let { File(it.filesDir, MANIFEST_NAME) }

    /** The on-disk audio file for a manifest file name (null if missing). */
    fun audioFile(name: String): File? {
        val dir = ttsDir ?: return null
        return File(dir, name).takeIf { it.exists() && it.length() > 0 }
    }

    /** Every file name the manifest references (active + alternates). */
    fun referencedFiles(): Set<String> = _recordings.value.values
        .flatMap { r -> r.active.files + r.alternates.flatMap { it.files } }
        .toSet()

    fun hasRecording(dbId: String, number: Int): Boolean {
        // v1.74.8: a quote only "has audio" when every filed audio file
        // physically exists on disk and is non-empty — the manifest alone
        // is not proof (a failed cache write or a prune could leave a
        // phantom entry behind).
        val files = _recordings.value[key(dbId, number)]?.active?.files.orEmpty()
        if (files.isEmpty()) return false
        val dir = ttsDir ?: return false
        return files.all { name ->
            try {
                File(dir, name).let { it.isFile && it.length() > 0 }
            } catch (e: Exception) {
                false
            }
        }
    }

    fun get(dbId: String, number: Int): VoiceRecording? =
        _recordings.value[key(dbId, number)]

    /**
     * Records a finished synthesis. A changed file list (new voices, edited
     * text) retires the previous generation into [alternates]; an identical
     * replay just refreshes the timestamp.
     */
    fun record(
        dbId: String,
        number: Int,
        files: List<String>,
        voices: String,
        nickVoices: Map<String, String> = emptyMap()
    ) {
        val ctx = appContext ?: return
        // v1.74.8: only file audio that actually made it to disk — a
        // failed or pruned cache write must never create a phantom
        // "has audio" entry. If nothing survived, leave any existing
        // entry untouched.
        val live = files.distinct().filter { audioFile(it) != null }
        if (live.isEmpty()) return
        scope.launch {
            synchronized(this@VoiceRecordings) {
                val k = key(dbId, number)
                val current = _recordings.value.toMutableMap()
                val existing = current[k]
                val now = System.currentTimeMillis()
                if (existing != null && existing.active.files == live) {
                    val refreshed = existing.active.copy(updatedAt = now)
                    current[k] = existing.copy(active = refreshed)
                } else {
                    val newAlternates = mutableListOf<VoiceGeneration>()
                    if (existing != null && existing.active.files.isNotEmpty()) {
                        newAlternates += existing.active
                    }
                    newAlternates += existing?.alternates.orEmpty()
                    // Cap generations; delete evicted audio from disk.
                    val evicted = newAlternates.drop(MAX_ALTERNATES)
                        .flatMap { it.files }
                    val kept = newAlternates.take(MAX_ALTERNATES)
                    evicted.forEach { deleteFile(it) }
                    current[k] = VoiceRecording(
                        dbId = dbId,
                        number = number,
                        active = VoiceGeneration(
                            files = live,
                            voices = voices.ifBlank { "—" },
                            updatedAt = now,
                            nickVoices = nickVoices
                        ),
                        alternates = kept
                    )
                }
                _recordings.value = current
                save(ctx, current)
            }
        }
    }

    /** Deletes a recording: manifest entry plus every audio file. */
    fun delete(dbId: String, number: Int) {
        val ctx = appContext ?: return
        scope.launch {
            synchronized(this@VoiceRecordings) {
                val k = key(dbId, number)
                val existing = _recordings.value[k] ?: return@launch
                (existing.active.files + existing.alternates.flatMap { it.files })
                    .forEach { deleteFile(it) }
                val current = _recordings.value.toMutableMap()
                current.remove(k)
                _recordings.value = current
                save(ctx, current)
            }
        }
    }

    /**
     * Deletes just the active generation's audio files. The newest
     * alternate (if any) is promoted to active; otherwise the entry is
     * removed.
     */
    fun deleteActive(dbId: String, number: Int) {
        val ctx = appContext ?: return
        scope.launch {
            synchronized(this@VoiceRecordings) {
                val k = key(dbId, number)
                val existing = _recordings.value[k] ?: return@launch
                existing.active.files.forEach { deleteFile(it) }
                val current = _recordings.value.toMutableMap()
                val promoted = existing.alternates.firstOrNull()
                if (promoted == null) {
                    current.remove(k)
                } else {
                    current[k] = existing.copy(
                        active = promoted.copy(updatedAt = System.currentTimeMillis()),
                        alternates = existing.alternates.drop(1)
                    )
                }
                _recordings.value = current
                save(ctx, current)
            }
        }
    }

    /** Deletes one alternate generation's audio files. */
    fun deleteAlternate(dbId: String, number: Int, alternateIndex: Int) {
        val ctx = appContext ?: return
        scope.launch {
            synchronized(this@VoiceRecordings) {
                val k = key(dbId, number)
                val existing = _recordings.value[k] ?: return@launch
                if (alternateIndex !in existing.alternates.indices) return@launch
                existing.alternates[alternateIndex].files.forEach { deleteFile(it) }
                val current = _recordings.value.toMutableMap()
                current[k] = existing.copy(
                    alternates = existing.alternates
                        .filterIndexed { i, _ -> i != alternateIndex }
                )
                _recordings.value = current
                save(ctx, current)
            }
        }
    }

    /**
     * Makes one of the older generations the active one; the previous
     * active generation becomes the newest alternate.
     */
    fun setActiveAlternate(dbId: String, number: Int, alternateIndex: Int) {
        val ctx = appContext ?: return
        scope.launch {
            synchronized(this@VoiceRecordings) {
                val k = key(dbId, number)
                val existing = _recordings.value[k] ?: return@launch
                if (alternateIndex !in existing.alternates.indices) return@launch
                val picked = existing.alternates[alternateIndex]
                val newAlternates = mutableListOf<VoiceGeneration>()
                if (existing.active.files.isNotEmpty()) newAlternates += existing.active
                newAlternates += existing.alternates
                    .filterIndexed { i, _ -> i != alternateIndex }
                val current = _recordings.value.toMutableMap()
                current[k] = existing.copy(
                    active = picked.copy(updatedAt = System.currentTimeMillis()),
                    alternates = newAlternates.take(MAX_ALTERNATES)
                )
                _recordings.value = current
                save(ctx, current)
            }
        }
    }

    /**
     * "Regenerate": the current active generation is retired into
     * alternates and its audio files are deleted, so the next play
     * synthesizes fresh audio (which then becomes the new active
     * generation, keeping the retired one as the secondary version).
     */
    fun regenerate(dbId: String, number: Int) {
        val ctx = appContext ?: return
        scope.launch {
            synchronized(this@VoiceRecordings) {
                val k = key(dbId, number)
                val existing = _recordings.value[k] ?: return@launch
                val newAlternates = mutableListOf<VoiceGeneration>()
                if (existing.active.files.isNotEmpty()) {
                    // v1.64.2: the retired active is KEPT as the newest
                    // alternate, so its files must NOT be deleted here.
                    // Only genuinely evicted files are deleted below.
                    newAlternates += existing.active
                }
                newAlternates += existing.alternates
                val evicted = newAlternates.drop(MAX_ALTERNATES).flatMap { it.files }
                evicted.forEach { deleteFile(it) }
                val current = _recordings.value.toMutableMap()
                current[k] = existing.copy(
                    active = VoiceGeneration(
                        files = emptyList(),
                        voices = existing.active.voices,
                        updatedAt = System.currentTimeMillis()
                    ),
                    alternates = newAlternates.take(MAX_ALTERNATES)
                )
                _recordings.value = current
                save(ctx, current)
            }
        }
    }

    /** Deletes every recording and all of their audio files. */
    fun deleteAll() {
        val ctx = appContext ?: return
        scope.launch {
            synchronized(this@VoiceRecordings) {
                _recordings.value.values.forEach { r ->
                    (r.active.files + r.alternates.flatMap { it.files })
                        .forEach { deleteFile(it) }
                }
                _recordings.value = emptyMap()
                save(ctx, emptyMap())
            }
        }
    }

    fun totalBytes(): Long {
        val dir = ttsDir ?: return 0L
        return referencedFiles().sumOf { name ->
            try { File(dir, name).takeIf { it.exists() }?.length() ?: 0L } catch (e: Exception) { 0L }
        }
    }

    private fun deleteFile(name: String) {
        try {
            val dir = ttsDir ?: return
            // Never delete a file another entry still references.
            File(dir, name).takeIf { it.exists() }?.delete()
        } catch (e: Exception) {
        }
    }

    private fun load(): Map<String, VoiceRecording> {
        val f = manifestFile() ?: return emptyMap()
        if (!f.exists()) return emptyMap()
        return try {
            val root = JSONObject(f.readText())
            val arr = root.optJSONArray("entries") ?: return emptyMap()
            val out = LinkedHashMap<String, VoiceRecording>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val rec = VoiceRecording(
                    dbId = o.optString("dbId"),
                    number = o.optInt("number"),
                    active = o.optJSONObject("active")?.toGeneration()
                        ?: VoiceGeneration(emptyList(), "—", 0L),
                    alternates = o.optJSONArray("alternates")
                        ?.toGenerationList().orEmpty()
                )
                // Back-compat with the unreleased v1.61.20 draft shape
                // (flat files/voices/updatedAt + string-list alternates).
                val legacyFiles = o.optJSONArray("files")?.toStringList().orEmpty()
                val legacy = if (legacyFiles.isNotEmpty() && rec.active.files.isEmpty()) {
                    val legacyAlts = mutableListOf<VoiceGeneration>()
                    val ja = o.optJSONArray("alternates")
                    if (ja != null) for (j in 0 until ja.length()) {
                        val files = ja.optJSONArray(j)?.toStringList().orEmpty()
                        if (files.isNotEmpty()) legacyAlts +=
                            VoiceGeneration(files, "—", 0L)
                    }
                    VoiceRecording(
                        dbId = rec.dbId,
                        number = rec.number,
                        active = VoiceGeneration(
                            legacyFiles,
                            o.optString("voices", "—"),
                            o.optLong("updatedAt", 0L)
                        ),
                        alternates = legacyAlts
                    )
                } else rec
                out[key(legacy.dbId, legacy.number)] = legacy
            }
            out
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun save(ctx: Context, map: Map<String, VoiceRecording>) {
        try {
            val arr = JSONArray()
            // Newest first — the management screen reads it in order.
            map.values.sortedByDescending { it.active.updatedAt }.forEach { r ->
                val o = JSONObject()
                    .put("dbId", r.dbId)
                    .put("number", r.number)
                    .put("active", r.active.toJson())
                val ja = JSONArray()
                r.alternates.forEach { gen -> ja.put(gen.toJson()) }
                o.put("alternates", ja)
                arr.put(o)
            }
            File(ctx.filesDir, MANIFEST_NAME).writeText(
                JSONObject().put("entries", arr).toString()
            )
        } catch (e: Exception) {
            // Best-effort; the audio files themselves are intact.
        }
    }

    private fun VoiceGeneration.toJson(): JSONObject = JSONObject()
        .put("files", JSONArray(files))
        .put("voices", voices)
        .put("updatedAt", updatedAt)
        .put("nickVoices", JSONObject(nickVoices))

    private fun JSONObject.toGeneration(): VoiceGeneration {
        // v1.65 nickVoices is optional: pre-v1.65 manifests don't have it.
        val nv = optJSONObject("nickVoices")?.let { o ->
            buildMap {
                val keys = o.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    put(k, o.optString(k))
                }
            }
        }.orEmpty()
        return VoiceGeneration(
            files = optJSONArray("files")?.toStringList().orEmpty(),
            voices = optString("voices", "—"),
            updatedAt = optLong("updatedAt", 0L),
            nickVoices = nv
        )
    }

    private fun JSONArray.toGenerationList(): List<VoiceGeneration> {
        val out = mutableListOf<VoiceGeneration>()
        for (i in 0 until length()) {
            optJSONObject(i)?.let { out += it.toGeneration() }
        }
        return out
    }

    private fun JSONArray.toStringList(): List<String> {
        val out = mutableListOf<String>()
        for (i in 0 until length()) out += optString(i)
        return out
    }
}
