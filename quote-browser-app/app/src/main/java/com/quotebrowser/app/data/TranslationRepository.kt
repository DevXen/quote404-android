package com.quotebrowser.app.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.quotebrowser.app.ui.GeminiTtsSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * v1.61: quote translation orchestration.
 *
 * - Translations are cached in [TranslationDatabase] (`translations.db`),
 *   keyed by (dbId, quote number, target language): each quote is
 *   translated exactly once per target language, forever.
 * - [ensureTranslated] batches concurrent requests: cards call it as they
 *   compose, and the repo flushes the queue in Gemini batches of
 *   [TranslationEngines.GEMINI_BATCH_SIZE] after a short settle delay.
 * - Engine order is "auto" by default: Gemini when an API key is present,
 *   falling back to the keyless endpoint on quota/network failure; "gemini"
 *   and "fallback" pin one engine.
 *
 * Settings live in their own prefs file ("translation_prefs_v161"), which
 * the full backup already captures via the shared_prefs XML files.
 */
class TranslationRepository(private val appContext: Context) {

    data class Key(val dbId: String, val number: Int, val targetLang: String) {
        fun wire(): String = "$dbId/$number/$targetLang"
    }

    companion object {
        const val PREFS = "translation_prefs_v161"
        private const val KEY_ENGINE = "engine" // auto | gemini | fallback
        private const val KEY_TARGET_LANG = "target_lang"
        // v1.61.18: KEY_DISPLAY_V2 — the default flipped from "both" to
        // "translated" (translated-only unless the badge is tapped /
        // long-pressed), so existing installs pick the new behavior up.
        private const val KEY_DISPLAY = "display_v2" // both | translated | original
        private const val KEY_AUTO_PREFIX = "auto_translate_"
        private const val KEY_SOURCE_PREFIX = "source_lang_"

        const val ENGINE_AUTO = "auto"
        const val ENGINE_GEMINI = "gemini"
        const val ENGINE_FALLBACK = "fallback"

        const val DISPLAY_BOTH = "both"
        const val DISPLAY_TRANSLATED = "translated"
        const val DISPLAY_ORIGINAL = "original"

        const val WARPDRIVE_ID = "asset:warpdrive_se.db"
        /** v1.88: bash.im — the Russian quote archive (Цитатник Рунета). */
        const val BASHIM_ID = "asset:bash_im.db"

        /** Languages offered for translation target/source. */
        val LANGUAGES = listOf(
            "en" to "English",
            "sv" to "Swedish",
            "no" to "Norwegian",
            "da" to "Danish",
            "fi" to "Finnish",
            "de" to "German",
            "fr" to "French",
            "es" to "Spanish",
            "it" to "Italian",
            "pt" to "Portuguese",
            "nl" to "Dutch",
            "pl" to "Polish",
            "ru" to "Russian",
            "ja" to "Japanese",
            "zh" to "Chinese",
            "ko" to "Korean",
            "ar" to "Arabic",
            "hi" to "Hindi"
        )

        /** Display name for a language code ("auto" -> "Detect"). */
        fun languageName(code: String): String =
            if (code == "auto") "Detect"
            else LANGUAGES.firstOrNull { it.first == code }?.second ?: code

        /** Settle delay before a batch flushes, so a scrolling list coalesces. */
        private const val FLUSH_DELAY_MS = 1_200L

        // ---- v1.87: curated warpdrive.se English seed ----
        private const val TAG = "TranslationRepository"
        /** One-time migration flag: curated warpdrive en seed applied. */
        private const val KEY_SEED_WARPDRIVE_EN = "translation_seed_warpdrive_en_v187"
        private const val SEED_ASSET = "translation_seeds/warpdrive_se_en.jsonl"
        private const val SEED_SOURCE_LANG = "sv"
        private const val SEED_TARGET_LANG = "en"
        private const val SEED_BATCH_SIZE = 500
        /**
         * Exact curated corpus size. The asset must parse to exactly this
         * many rows with this many DISTINCT quote numbers — anything else
         * means the asset is damaged and the old translations stay put.
         */
        private const val SEED_EXPECTED_ROWS = 6025
        // ---- v1.88: curated bash.im English seed ----
        /** One-time migration flag: curated bash.im en seed applied. */
        private const val KEY_SEED_BASHIM_EN = "translation_seed_bashim_en_v188"
        private const val SEED_BASHIM_ASSET = "translation_seeds/bashim_ru_en.jsonl"
        private const val SEED_BASHIM_SOURCE_LANG = "ru"
        /**
         * Exact curated corpus size. The asset must parse to exactly this
         * many rows with this many DISTINCT quote numbers — anything else
         * means the asset is damaged and the old translations stay put.
         */
        private const val SEED_BASHIM_EXPECTED_ROWS = 81497
    }

    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val db: TranslationDatabase =
        Room.databaseBuilder(appContext, TranslationDatabase::class.java, "translations.db")
            .build()
    private val dao get() = db.translationDao()

    // ---- settings ----

    private val _engine =
        MutableStateFlow(prefs.getString(KEY_ENGINE, ENGINE_AUTO) ?: ENGINE_AUTO)
    val engine: StateFlow<String> = _engine.asStateFlow()
    fun setEngine(e: String) {
        prefs.edit().putString(KEY_ENGINE, e).apply()
        _engine.value = e
    }

    private val _targetLang =
        MutableStateFlow(prefs.getString(KEY_TARGET_LANG, "en") ?: "en")
    val targetLang: StateFlow<String> = _targetLang.asStateFlow()
    fun setTargetLang(code: String) {
        val prev = _targetLang.value
        prefs.edit().putString(KEY_TARGET_LANG, code).apply()
        _targetLang.value = code
        if (prev != code) {
            // Language switch: re-resolve cached translations for the new
            // target; keys embed the target so the cache map just rebuilds.
            scope.launch { rebuildCache() }
        }
    }

    private val _displayMode =
        MutableStateFlow(prefs.getString(KEY_DISPLAY, DISPLAY_TRANSLATED) ?: DISPLAY_TRANSLATED)
    val displayMode: StateFlow<String> = _displayMode.asStateFlow()
    fun setDisplayMode(m: String) {
        prefs.edit().putString(KEY_DISPLAY, m).apply()
        _displayMode.value = m
    }

    private val autoFlows = ConcurrentHashMap<String, MutableStateFlow<Boolean>>()
    private val sourceFlows = ConcurrentHashMap<String, MutableStateFlow<String>>()

    /**
     * Per-database auto-translate. Warpdrive.se and bash.im default ON
     * (Swedish / Russian archives with curated English seeds); everything
     * else defaults OFF.
     */
    private fun mutableAutoFlow(dbId: String): MutableStateFlow<Boolean> =
        autoFlows.getOrPut(dbId) {
            val def = dbId == WARPDRIVE_ID || dbId == BASHIM_ID
            MutableStateFlow(prefs.getBoolean(KEY_AUTO_PREFIX + dbId, def))
        }

    fun autoTranslateFlow(dbId: String): StateFlow<Boolean> = mutableAutoFlow(dbId)

    fun setAutoTranslate(dbId: String, on: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PREFIX + dbId, on).apply()
        mutableAutoFlow(dbId).value = on
    }

    /** Per-database source language; Warpdrive defaults to Swedish, bash.im to Russian. */
    private fun mutableSourceFlow(dbId: String): MutableStateFlow<String> =
        sourceFlows.getOrPut(dbId) {
            val def = when (dbId) {
                WARPDRIVE_ID -> "sv"
                BASHIM_ID -> "ru"
                else -> "auto"
            }
            MutableStateFlow(prefs.getString(KEY_SOURCE_PREFIX + dbId, def) ?: def)
        }

    fun sourceLangFlow(dbId: String): StateFlow<String> = mutableSourceFlow(dbId)

    fun setSourceLang(dbId: String, code: String) {
        prefs.edit().putString(KEY_SOURCE_PREFIX + dbId, code).apply()
        mutableSourceFlow(dbId).value = code
    }

    // ---- cache + in-flight ----

    private val _cache = MutableStateFlow<Map<Key, String>>(emptyMap())
    /** Resolved translations for the current target language. */
    val cache: StateFlow<Map<Key, String>> = _cache.asStateFlow()

    private val _inFlight = MutableStateFlow<Set<Key>>(emptySet())
    val inFlight: StateFlow<Set<Key>> = _inFlight.asStateFlow()

    private val _rowCount = MutableStateFlow(-1)
    /** Total cached translations across all languages (Settings shows it). */
    val rowCount: StateFlow<Int> = _rowCount.asStateFlow()

    init {
        scope.launch { rebuildCache() }
        // v1.87: one-time seed of the curated warpdrive English
        // translations — off the main thread, guarded by a pref.
        scope.launch { seedWarpdriveTranslations() }
        // v1.88: one-time seed of the curated bash.im English
        // translations — same pattern, guarded by its own pref.
        scope.launch { seedBashimTranslations() }
    }

    private suspend fun rebuildCache() = withContext(Dispatchers.IO) {
        // Rebuild is keyed on the current target language only; other
        // languages stay in the DB untouched for later switches.
        _rowCount.value = try {
            dao.count()
        } catch (e: Exception) {
            -1
        }
        // NOTE: full-table read is bounded by real usage (one row per
        // translated quote); a targeted per-screen warm is unnecessary.
        _cache.value = emptyMap()
    }

    /**
     * Local-first lookup used by cards: returns the cached translation for
     * the current target language, or null. Never hits the network.
     */
    fun cached(dbId: String, number: Int): String? =
        _cache.value[Key(dbId, number, _targetLang.value)]

    /**
     * Warms [cache] for a batch of quotes from Room (no network). Called by
     * [ensureTranslatedBatch] before translating the remainder.
     */
    private suspend fun warmFromDb(
        dbId: String,
        numbers: List<Int>,
        targetLang: String
    ): Map<Int, String> = withContext(Dispatchers.IO) {
        if (numbers.isEmpty()) return@withContext emptyMap()
        val rows = try {
            dao.getBatch(dbId, numbers, targetLang)
        } catch (e: Exception) {
            emptyList()
        }
        val found = rows.associate { it.number to it.translatedText }
        if (found.isNotEmpty()) {
            val overlay = found.mapKeys { Key(dbId, it.key, targetLang) }
            _cache.value = _cache.value + overlay
        }
        found
    }

    // ---- batched network translation ----

    private data class Pending(val text: String, val sourceLang: String)

    private val pending = ConcurrentHashMap<Key, Pending>()
    private val flushMutex = Mutex()
    private var flushJob: Job? = null

    /**
     * Queue a quote for translation; no-op when already cached, in flight,
     * or queued. The queue flushes [FLUSH_DELAY_MS] after the last enqueue,
     * in batches of [TranslationEngines.GEMINI_BATCH_SIZE].
     */
    fun ensureTranslated(dbId: String, number: Int, text: String) {
        val target = _targetLang.value
        val key = Key(dbId, number, target)
        if (_cache.value.containsKey(key) || _inFlight.value.contains(key) ||
            pending.containsKey(key) || text.isBlank()
        ) return
        // Hold off while the curated seed is mid-import: a network
        // translation now would land machine rows inside the curated set.
        // Cards re-request on the next render, which warms the curated row.
        if (seedingWarpdrive && dbId == WARPDRIVE_ID && target == SEED_TARGET_LANG) return
        pending[key] = Pending(text, sourceLangFlow(dbId).value)
        scheduleFlush()
    }

    private fun scheduleFlush() {
        if (flushJob?.isActive == true) return
        flushJob = scope.launch {
            delay(FLUSH_DELAY_MS)
            flushMutex.withLock { flushPending() }
        }
    }

    private suspend fun flushPending() {
        while (pending.isNotEmpty()) {
            val target = _targetLang.value
            // Group by (dbId, sourceLang); keys carry the target at enqueue
            // time, so drop any whose target has since changed.
            val batch = pending.entries
                .filter { it.key.targetLang == target }
                .take(TranslationEngines.GEMINI_BATCH_SIZE)
            pending.keys.removeAll(batch.map { it.key }.toSet())
            if (batch.isEmpty()) continue
            // Grouping keeps one Gemini prompt per language pair.
            batch.groupBy({ it.key.dbId to it.value.sourceLang },
                { it.key to it.value.text })
                .forEach { (dbAndSource, pairs) ->
                    translatePairs(dbAndSource.first, dbAndSource.second, pairs, target)
                }
        }
    }

    private suspend fun translatePairs(
        dbId: String,
        sourceLang: String,
        pairs: List<Pair<Key, String>>,
        targetLang: String
    ) {
        if (pairs.isEmpty()) return
        val keys = pairs.map { it.first }
        // 1. Room first — another screen may have translated these already.
        val warmed = warmFromDb(dbId, keys.map { it.number }, targetLang)
        var missing = pairs.filter { warmed[it.first.number] == null }
        // Never network-translate warpdrive/en or bash.im/en while the
        // curated seed is mid-import; drop those pairs so no machine
        // rows pollute the set.
        if (targetLang == SEED_TARGET_LANG &&
            ((seedingWarpdrive && dbId == WARPDRIVE_ID) ||
             (seedingBashim && dbId == BASHIM_ID))
        ) {
            missing = emptyList()
        }
        if (missing.isEmpty()) return
        _inFlight.value = _inFlight.value + missing.map { it.first }.toSet()
        try {
            val results = translateTexts(
                missing.map { it.second }, sourceLang, targetLang
            )
            val rows = mutableListOf<QuoteTranslation>()
            val overlay = mutableMapOf<Key, String>()
            missing.forEachIndexed { i, (key, _) ->
                val t = results.getOrNull(i)?.trim().orEmpty()
                if (t.isNotEmpty()) {
                    rows += QuoteTranslation(
                        dbId = key.dbId,
                        number = key.number,
                        sourceLang = sourceLang,
                        targetLang = targetLang,
                        translatedText = t,
                        engine = lastEngineUsed
                    )
                    overlay[key] = t
                }
            }
            if (rows.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    try {
                        dao.upsertAll(rows)
                    } catch (e: Exception) {
                        // Cache update below still shows them this session.
                    }
                }
                _cache.value = _cache.value + overlay
                _rowCount.value = (_rowCount.value.takeIf { it >= 0 } ?: 0) + rows.size
            }
        } finally {
            _inFlight.value = _inFlight.value - keys.toSet()
        }
    }

    @Volatile
    private var lastEngineUsed: String = ENGINE_FALLBACK

    /**
     * Translates [texts] with the configured engine. "auto" tries Gemini
     * when a key exists and drops to the keyless fallback on quota or
     * network failure. Returns one string per input; "" means that quote
     * failed and must not be cached.
     */
    private suspend fun translateTexts(
        texts: List<String>,
        sourceLang: String,
        targetLang: String
    ): List<String> {
        val eng = _engine.value
        val apiKey = try {
            GeminiTtsSettings.get(appContext).apiKey.value
        } catch (e: Exception) {
            ""
        }
        val useGemini = (eng == ENGINE_AUTO || eng == ENGINE_GEMINI) && apiKey.isNotBlank()
        if (useGemini) {
            try {
                lastEngineUsed = ENGINE_GEMINI
                return TranslationEngines.geminiBatch(texts, sourceLang, targetLang, apiKey)
            } catch (e: TranslationEngines.GeminiQuotaException) {
                // Quota dead: fall through to the free engine (auto) or
                // surface the friendly message (gemini pinned).
                if (eng == ENGINE_GEMINI) return texts.map { "" }
            } catch (e: Exception) {
                if (eng == ENGINE_GEMINI) return texts.map { "" }
            }
        }
        lastEngineUsed = ENGINE_FALLBACK
        // Fallback is single-text; sequential is fine (it's the rare path,
        // and each result is cached forever).
        return texts.map { TranslationEngines.fallbackSingle(it, sourceLang, targetLang) }
    }

    /**
     * Fire-and-forget translation for UI actions. Runs in the repo's own
     * scope so it survives the dialog/sheet that triggered it (a
     * composition-scoped launch would be cancelled by onDismiss()).
     */
    fun translateAsync(dbId: String, number: Int, text: String) {
        scope.launch { translateNow(dbId, number, text) }
    }

    /**
     * Immediate translation for the per-quote "Translate" action. Returns
     * true when a translation was produced (and cached).
     */
    suspend fun translateNow(dbId: String, number: Int, text: String): Boolean {
        val target = _targetLang.value
        val key = Key(dbId, number, target)
        cached(dbId, number)?.let { return true }
        if (text.isBlank()) return false
        // v1.87: don't race the curated seed — wait for it to land so the
        // warm below picks up the curated row instead of network-
        // translating over it. Bounded; the seed is a few seconds of work.
        if (seedingWarpdrive && dbId == WARPDRIVE_ID && target == SEED_TARGET_LANG) {
            var waited = 0
            while (seedingWarpdrive && waited < 10_000) {
                kotlinx.coroutines.delay(100)
                waited += 100
            }
            cached(dbId, number)?.let { return true }
        }
        pending.remove(key)
        _inFlight.value = _inFlight.value + key
        return try {
            val warmed = warmFromDb(dbId, listOf(number), target)
            val got = warmed[number]
            if (got != null) {
                true
            } else {
                val sourceLang = sourceLangFlow(dbId).value
                val results = translateTexts(listOf(text), sourceLang, target)
                val t = results.firstOrNull()?.trim().orEmpty()
                if (t.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        try {
                            dao.upsert(
                                QuoteTranslation(
                                    dbId, number, sourceLang, target,
                                    t, lastEngineUsed
                                )
                            )
                        } catch (e: Exception) {
                        }
                    }
                    _cache.value = _cache.value + (key to t)
                    _rowCount.value = (_rowCount.value.takeIf { it >= 0 } ?: 0) + 1
                    true
                } else false
            }
        } finally {
            _inFlight.value = _inFlight.value - key
        }
    }

    /** Clears every cached translation (Settings → Translations). */
    suspend fun clearAll(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            db.openHelper.writableDatabase.execSQL("DELETE FROM translations")
            _cache.value = emptyMap()
            _rowCount.value = 0
            true
        } catch (e: Exception) {
            false
        }
    }

    // ---- v1.87: one-time curated translation seed (warpdrive.se) ----

    /**
     * True while the curated warpdrive seed is deleting/inserting. UI
     * translation requests for warpdrive/en are held off while set so a
     * network translation can't sneak machine rows into the curated set.
     */
    @Volatile
    private var seedingWarpdrive = false

    /**
     * Seeds the 6,025 curated English translations from the bundled
     * JSONL asset. Runs once (pref-guarded), off the main thread. The
     * asset must parse to exactly SEED_EXPECTED_ROWS rows with distinct
     * quote numbers, or nothing is touched. Old machine rows for
     * warpdrive/en are deleted first — ignoring sourceLang — so they
     * can't shadow the curated rows in the targetLang-only LIMIT 1
     * lookups. The migration flag is stamped only after every row
     * verifies present; the cache is invalidated so cards use the
     * curated text without a restart.
     */
    private suspend fun seedWarpdriveTranslations() = withContext(Dispatchers.IO) {
        if (prefs.getBoolean(KEY_SEED_WARPDRIVE_EN, false)) return@withContext
        seedingWarpdrive = true
        try {
            val rows = appContext.assets.open(SEED_ASSET).use { ins ->
                parseSeedLines(
                    java.io.BufferedReader(
                        java.io.InputStreamReader(ins, Charsets.UTF_8)
                    )
                )
            }
            if (rows.size != SEED_EXPECTED_ROWS ||
                rows.distinctBy { it.first }.size != SEED_EXPECTED_ROWS
            ) {
                Log.w(
                    TAG,
                    "warpdrive seed asset has ${rows.size} rows " +
                        "(${rows.distinctBy { it.first }.size} distinct); " +
                        "expected $SEED_EXPECTED_ROWS — leaving old translations, " +
                        "will retry next launch"
                )
                return@withContext
            }
            dao.deleteForDbTarget(WARPDRIVE_ID, SEED_TARGET_LANG)
            val now = System.currentTimeMillis()
            rows.chunked(SEED_BATCH_SIZE).forEach { chunk ->
                dao.upsertAll(chunk.map { (n, en) ->
                    QuoteTranslation(
                        dbId = WARPDRIVE_ID,
                        number = n,
                        sourceLang = SEED_SOURCE_LANG,
                        targetLang = SEED_TARGET_LANG,
                        translatedText = en,
                        engine = "curated",
                        updatedAt = now
                    )
                })
            }
            val count = dao.countForDbTarget(WARPDRIVE_ID, SEED_TARGET_LANG)
            if (count == SEED_EXPECTED_ROWS) {
                prefs.edit().putBoolean(KEY_SEED_WARPDRIVE_EN, true).apply()
                Log.i(TAG, "seeded $count curated warpdrive en translations")
            } else {
                Log.w(TAG, "warpdrive seed incomplete ($count/$SEED_EXPECTED_ROWS); will retry")
            }
            rebuildCache()
        } catch (e: Exception) {
            Log.w(TAG, "warpdrive seed failed; will retry next launch", e)
        } finally {
            seedingWarpdrive = false
        }
    }

    // ---- v1.88: one-time curated translation seed (bash.im) ----

    /**
     * True while the curated bash.im seed is deleting/inserting. UI
     * translation requests for bash.im/en are held off while set so a
     * network translation can't sneak machine rows into the curated set.
     */
    @Volatile
    private var seedingBashim = false

    /**
     * Seeds the 81,497 curated English translations from the bundled
     * JSONL asset. Runs once (pref-guarded), off the main thread. The
     * asset must parse to exactly SEED_BASHIM_EXPECTED_ROWS rows with
     * distinct quote numbers, or nothing is touched. Old machine rows
     * for bash.im/en are deleted first — ignoring sourceLang — so they
     * can't shadow the curated rows in the targetLang-only LIMIT 1
     * lookups. The migration flag is stamped only after every row
     * verifies present; the cache is invalidated so cards use the
     * curated text without a restart.
     */
    private suspend fun seedBashimTranslations() = withContext(Dispatchers.IO) {
        if (prefs.getBoolean(KEY_SEED_BASHIM_EN, false)) return@withContext
        seedingBashim = true
        try {
            val rows = appContext.assets.open(SEED_BASHIM_ASSET).use { ins ->
                parseSeedLines(
                    java.io.BufferedReader(
                        java.io.InputStreamReader(ins, Charsets.UTF_8)
                    )
                )
            }
            if (rows.size != SEED_BASHIM_EXPECTED_ROWS ||
                rows.distinctBy { it.first }.size != SEED_BASHIM_EXPECTED_ROWS
            ) {
                Log.w(
                    TAG,
                    "bash.im seed asset has ${rows.size} rows " +
                        "(${rows.distinctBy { it.first }.size} distinct); " +
                        "expected $SEED_BASHIM_EXPECTED_ROWS — leaving old translations, " +
                        "will retry next launch"
                )
                return@withContext
            }
            dao.deleteForDbTarget(BASHIM_ID, SEED_TARGET_LANG)
            val now = System.currentTimeMillis()
            rows.chunked(SEED_BATCH_SIZE).forEach { chunk ->
                dao.upsertAll(chunk.map { (n, en) ->
                    QuoteTranslation(
                        dbId = BASHIM_ID,
                        number = n,
                        sourceLang = SEED_BASHIM_SOURCE_LANG,
                        targetLang = SEED_TARGET_LANG,
                        translatedText = en,
                        engine = "curated",
                        updatedAt = now
                    )
                })
            }
            val count = dao.countForDbTarget(BASHIM_ID, SEED_TARGET_LANG)
            if (count == SEED_BASHIM_EXPECTED_ROWS) {
                prefs.edit().putBoolean(KEY_SEED_BASHIM_EN, true).apply()
                Log.i(TAG, "seeded $count curated bash.im en translations")
            } else {
                Log.w(TAG, "bash.im seed incomplete ($count/$SEED_BASHIM_EXPECTED_ROWS); will retry")
            }
            rebuildCache()
        } catch (e: Exception) {
            Log.w(TAG, "bash.im seed failed; will retry next launch", e)
        } finally {
            seedingBashim = false
        }
    }

    /** Parse seed JSONL lines into (quote number, english text) pairs. */
    private fun parseSeedLines(
        reader: java.io.BufferedReader
    ): List<Pair<Int, String>> {
        val out = ArrayList<Pair<Int, String>>(6200)
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val s = line!!.trim()
            if (s.isEmpty()) continue
            try {
                val o = org.json.JSONObject(s)
                val n = o.optInt("n", -1)
                val en = o.optString("en", "").trim()
                if (n > 0 && en.isNotEmpty()) out += n to en
            } catch (e: Exception) {
                // Skip malformed lines; the count check below catches gaps.
            }
        }
        return out
    }
}
