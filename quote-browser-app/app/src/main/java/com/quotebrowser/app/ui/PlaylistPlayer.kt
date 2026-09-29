package com.quotebrowser.app.ui

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.SourcedQuote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * v1.63 radio station id meaning "shuffle every database": each refill
 * batch is drawn from a random database, so the station never runs dry
 * and never repeats itself quickly.
 */
const val RADIO_STATION_ALL = "__all__"

/** One entry in the read-aloud playlist queue. */
data class PlaylistItem(
    val dbId: String,
    val number: Int,
    val spokenText: String,
    /** Original raw quote text, used for per-nick voice assignment. */
    val rawText: String
)

/**
 * Reads a queue of quotes aloud back-to-back (a whole random set or a
 * favorites folder), with stop and skip controls. Application-scoped so
 * playback survives screen switches — and so Android Auto's media browser
 * service can drive the very same queue the phone UI shows. The mini
 * player bar above the bottom nav reflects its state.
 *
 * v1.63: sleep timer (stop after N minutes, or after the current quote)
 * and the "everything shuffle" radio station ([RADIO_STATION_ALL]).
 */
class PlaylistPlayer(
    private val manager: DatabaseManager,
    private val scope: CoroutineScope
) {

    private val _items =
        kotlinx.coroutines.flow.MutableStateFlow<List<PlaylistItem>>(emptyList())
    val items: kotlinx.coroutines.flow.StateFlow<List<PlaylistItem>> = _items

    private val _index = kotlinx.coroutines.flow.MutableStateFlow(0)
    val index: StateFlow<Int> = _index

    private val _playing = kotlinx.coroutines.flow.MutableStateFlow(false)
    val playing: StateFlow<Boolean> = _playing

    private var tts: android.speech.tts.TextToSpeech? = null
    private var ttsReady = false
    private var pendingStart = false
    // Gemini cloud voice engine for read-aloud, used when the user picked it
    // in Settings -> Read aloud. The built-in TextToSpeech above stays the
    // default path and is untouched.
    private val geminiTts = GeminiTts(manager.context)

    private val listener = object : android.speech.tts.UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {}
        override fun onDone(utteranceId: String?) {
            maybeAdvance(utteranceId)
        }
        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            maybeAdvance(utteranceId)
        }
    }

    /**
     * Bumped every time a new queue starts; embedded in each utterance id so
     * stale callbacks from an older queue can never advance the new one.
     */
    private var queueGen = 0

    /**
     * Advances only for the utterance that is actually playing. Stale
     * callbacks (e.g. onError fired by stop() during a manual skip, or a
     * callback from the previous queue) are ignored, so skip() can't jump
     * two quotes and start() can't lose its first quote. Multi-turn quotes
     * advance only when their LAST turn finishes.
     */
    private fun maybeAdvance(utteranceId: String?) {
        // "playlist:<gen>:<dbId>:<number>:<index>:<turn>:<turnCount>";
        // index/turn/count are parsed from the end so a dbId containing
        // ":" can't shift them.
        val parts = utteranceId?.split(":")
        if (parts != null && parts.size >= 7 && parts[0] == "playlist") {
            val gen = parts[1].toIntOrNull()
            val idIndex = parts[parts.size - 3].toIntOrNull()
            val turn = parts[parts.size - 2].toIntOrNull()
            val turnCount = parts[parts.size - 1].toIntOrNull()
            if (gen != queueGen || idIndex != _index.value) return
            if (turn == null || turnCount == null || turn < turnCount - 1) return
        }
        advance()
    }

    /**
     * v1.62 quote radio: endless shuffled read-aloud from one database.
     * The queue refills with fresh random quotes as it plays, so it never
     * stops on its own. Replaces any current queue.
     */
    private val _radioMode = kotlinx.coroutines.flow.MutableStateFlow(false)
    val radioMode: StateFlow<Boolean> = _radioMode

    private val _radioDbId = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val radioDbId: StateFlow<String?> = _radioDbId

    /** Quote numbers recently played on radio, to avoid quick repeats. */
    private val radioRecent = ArrayDeque<Int>()
    /** Quotes fetched per radio refill. */
    private val radioBatchSize = 25

    // v1.63 sleep timer: stop playback after N minutes, or after the
    // current quote finishes. A minute-timer keeps running while paused
    // (like a real sleep timer); any explicit stop clears it.
    private val _sleepMinutes = MutableStateFlow<Int?>(null)
    val sleepMinutes: StateFlow<Int?> = _sleepMinutes.asStateFlow()
    private val _sleepEndsAt = MutableStateFlow(0L)
    val sleepEndsAt: StateFlow<Long> = _sleepEndsAt.asStateFlow()
    private val _sleepAfterCurrent = MutableStateFlow(false)
    val sleepAfterCurrent: StateFlow<Boolean> = _sleepAfterCurrent.asStateFlow()
    private var sleepJob: Job? = null

    /** Starts a sleep timer; null clears it. */
    fun setSleepTimer(minutes: Int?) {
        sleepJob?.cancel()
        sleepJob = null
        _sleepAfterCurrent.value = false
        _sleepMinutes.value = minutes
        _sleepEndsAt.value =
            if (minutes != null && minutes > 0) System.currentTimeMillis() + minutes * 60_000L
            else 0L
        if (minutes != null && minutes > 0) {
            sleepJob = scope.launch {
                delay(minutes * 60_000L)
                _sleepMinutes.value = null
                _sleepEndsAt.value = 0L
                stopPlayback()
            }
        }
    }

    /** "Stop after this quote": the queue halts when the current quote ends. */
    fun setSleepAfterCurrent(enabled: Boolean) {
        if (enabled) {
            sleepJob?.cancel()
            sleepJob = null
            _sleepMinutes.value = null
            _sleepEndsAt.value = 0L
        }
        _sleepAfterCurrent.value = enabled
    }

    fun clearSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _sleepMinutes.value = null
        _sleepEndsAt.value = 0L
        _sleepAfterCurrent.value = false
    }

    fun startRadio(dbId: String) {
        stopPlayback()
        _radioMode.value = true
        _radioDbId.value = dbId
        radioRecent.clear()
        queueGen++
        _playing.value = true
        scope.launch {
            val grew = appendRadioBatch()
            if (!_playing.value || !_radioMode.value) return@launch
            if (grew) {
                _index.value = 0
                beginPlayback()
            } else {
                stopPlayback()
            }
        }
    }

    /** Appends one fresh batch of radio quotes; false when the well ran dry. */
    private suspend fun appendRadioBatch(): Boolean {
        val stationId = _radioDbId.value ?: return false
        // v1.63 "everything shuffle": each batch comes from a random
        // database (skipping empties), so the station plays everything.
        val dbIds: List<String> = if (stationId == RADIO_STATION_ALL) {
            runCatching {
                manager.availableDatabases().first().map { it.id }.shuffled()
            }.getOrElse { emptyList() }
        } else listOf(stationId)
        if (dbIds.isEmpty()) return false
        var batch: List<Quote> = emptyList()
        var dbId = dbIds.first()
        // v1.65: radio honors the global maturity ceiling.
        val maturities = listOf(manager.maturityCeiling.value)
        for (id in dbIds.take(5)) {
            val repo = manager.repoFor(id)
            var b = repo.randomQuotes(radioBatchSize, excludeNumbers = radioRecent.toList(), maturities = maturities)
            if (b.isEmpty() && radioRecent.isNotEmpty()) {
                // Small database: allow repeats rather than going silent.
                radioRecent.clear()
                b = repo.randomQuotes(radioBatchSize, maturities = maturities)
            }
            if (b.isNotEmpty()) {
                batch = b
                dbId = id
                break
            }
        }
        if (batch.isEmpty()) return false
        val dialogueBreaks = manager.dialogueLineBreaks.value
        val items = batch.map {
            radioRecent.addLast(it.number)
            PlaylistItem(
                dbId,
                it.number,
                buildSpokenQuoteText(it.text, dialogueBreaks),
                it.text
            )
        }
        while (radioRecent.size > 200) radioRecent.removeFirst()
        // Append without bumping queueGen: the currently speaking utterance
        // still carries the old generation.
        _items.value = _items.value + items
        return true
    }

    /** Pauses playback keeping the queue; [resumePlayback] continues it. */
    fun pausePlayback() {
        if (!_playing.value) return
        _playing.value = false
        tts?.stop()
        geminiTts.stop()
    }

    /** Continues a paused queue from the current quote. */
    fun resumePlayback() {
        if (_playing.value || _items.value.isEmpty()) return
        _playing.value = true
        speakCurrent()
    }

    /** Starts reading [quotes] from the first one, replacing any queue. */
    fun start(quotes: List<SourcedQuote>) {
        stopPlayback()
        if (quotes.isEmpty()) return
        queueGen++
        val dialogueBreaks = manager.dialogueLineBreaks.value
        _items.value = quotes.map {
            PlaylistItem(
                it.dbId,
                it.quote.number,
                buildSpokenQuoteText(it.quote.text, dialogueBreaks),
                it.quote.text
            )
        }
        _index.value = 0
        _playing.value = true
        beginPlayback()
    }

    /** Speaks the current item once the engine is ready (shared by start/radio). */
    private fun beginPlayback() {
        // With the Gemini engine selected the built-in TTS engine is never
        // needed, so don't wait on (or create) it.
        if (GeminiTtsSettings.get(manager.context).currentEngine() == TTS_ENGINE_GEMINI || ttsReady) {
            speakCurrent()
        } else {
            pendingStart = true
            ensureTts()
        }
    }

    /**
     * Adds quotes to the end of the queue (bulk "read aloud" action). When
     * nothing is playing, starts with the new quotes.
     */
    fun append(quotes: List<SourcedQuote>) {
        if (quotes.isEmpty()) return
        val dialogueBreaks = manager.dialogueLineBreaks.value
        val items = quotes.map {
            PlaylistItem(
                it.dbId,
                it.quote.number,
                buildSpokenQuoteText(it.quote.text, dialogueBreaks),
                it.quote.text
            )
        }
        if (!_playing.value || _items.value.isEmpty()) {
            start(quotes)
            return
        }
        // Append without bumping queueGen: the currently speaking
        // utterance still carries the old generation, and bumping it would
        // make its completion callback get ignored, stalling playback.
        _items.value = _items.value + items
    }

    /** Skips to the next quote in the queue. */
    fun skip() {
        if (!_playing.value) return
        tts?.stop()
        geminiTts.stop()
        advance()
    }

    /** Stops playback and clears the queue. */
    fun stopPlayback() {
        clearSleepTimer()
        _playing.value = false
        pendingStart = false
        tts?.stop()
        // Also halts Gemini cloud playback: this is what the sleep timer
        // calls when it expires, so the timer works for both engines.
        geminiTts.stop()
        _items.value = emptyList()
        _index.value = 0
        // v1.62: leaving radio mode too.
        _radioMode.value = false
        _radioDbId.value = null
        radioRecent.clear()
    }

    private fun ensureTts() {
        if (tts != null) return
        tts = android.speech.tts.TextToSpeech(manager.context, { status ->
            ttsReady = status == android.speech.tts.TextToSpeech.SUCCESS
            tts?.setOnUtteranceProgressListener(listener)
            if (ttsReady && pendingStart) {
                pendingStart = false
                if (_playing.value) speakCurrent()
            } else if (!ttsReady) {
                stopPlayback()
            }
        })
    }

    private fun speakCurrent() {
        val item = _items.value.getOrNull(_index.value)
        if (item == null || !_playing.value) {
            stopPlayback()
            return
        }
        // Gemini cloud engine: the whole quote goes through the selected
        // cloud voice, or per-speaker cloud voices when Speaker voices is on.
        val geminiSettings = GeminiTtsSettings.get(manager.context)
        if (geminiSettings.currentEngine() == TTS_ENGINE_GEMINI) {
            speakCurrentWithGemini(item, geminiSettings)
            return
        }
        speakCurrentWithBuiltin(item, geminiSettings)
    }

    /**
     * Reads the current queue item with the built-in TTS engine. Also the
     * Gemini fallback target: when Gemini read-aloud fails and the fallback
     * toggle is on, playback continues here instead of stopping (v1.49).
     */
    private fun speakCurrentWithBuiltin(
        item: PlaylistItem,
        geminiSettings: GeminiTtsSettings
    ) {
        val engine = tts ?: return
        engine.language = java.util.Locale.getDefault()
        val index = _index.value
        if (manager.ttsPerNickVoices.value) {
            // True per-speaker playback: each speaker turn gets that nick's
            // stable installed voice (or their chosen override); narration
            // keeps the default voice. With 2+ speakers the voices identify
            // who is talking — falling back to a per-speaker pitch/rate
            // variation when the engine can't give everyone a distinct
            // voice — so the "says"/"replies" announcements are dropped.
            val overrides = geminiSettings.currentNickVoiceOverrides()
            val overrideFor: (String) -> String? = { n -> overrides[n.lowercase()] }
            val previewTurns = buildSpokenTurns(
                item.rawText, manager.dialogueLineBreaks.value
            )
            if (previewTurns.isEmpty()) {
                advance()
                return
            }
            val speakers = previewTurns.map { it.nick }
            val fallback = needsPitchRateFallback(
                engine, speakers, overrideFor,
                geminiSettings.currentAutoAssignSpeakerVoices()
            )
            val turns = if (dropSpeakerAnnouncements(speakers))
                buildSpokenTurns(
                    item.rawText, manager.dialogueLineBreaks.value,
                    announceSpeakers = false
                )
            else previewTurns
            val narratorBuiltin = geminiSettings.currentNarratorVoiceBuiltin()
            turns.forEachIndexed { ti, turn ->
                if (turn.isAction) {
                    // *Action* lines are read by the narrator voice, never
                    // by a speaker's voice.
                    applyNarratorVoice(
                        engine, narratorBuiltin
                    )
                } else {
                    applySpeakerVoice(
                        engine, turn.nick, turn.nick?.let(overrideFor), fallback,
                        geminiSettings.currentAutoAssignSpeakerVoices()
                    )
                }
                engine.speak(
                    turn.text,
                    if (ti == 0) android.speech.tts.TextToSpeech.QUEUE_FLUSH
                    else android.speech.tts.TextToSpeech.QUEUE_ADD,
                    null,
                    "playlist:$queueGen:${item.dbId}:${item.number}:$index:$ti:${turns.size}"
                )
            }
        } else {
            applyVoiceOrDefault(engine, null)
            engine.setPitch(1.0f)
            engine.setSpeechRate(1.0f)
            engine.speak(
                item.spokenText,
                android.speech.tts.TextToSpeech.QUEUE_FLUSH,
                null,
                "playlist:$queueGen:${item.dbId}:${item.number}:$index:0:1"
            )
        }
        scope.launch { manager.markSeen(item.dbId, item.number) }
    }

    /**
     * Reads the current queue item with the Gemini cloud voice. The
     * generation/index guard mirrors the built-in path's utterance-id
     * guard: stale callbacks (e.g. after skip/stop or a new queue) can
     * never advance the wrong quote.
     */
    private fun speakCurrentWithGemini(item: PlaylistItem, settings: GeminiTtsSettings) {
        val key = settings.currentApiKey()
        if (key.isBlank()) {
            stopPlayback()
            settings.recordLastError(
                "No Gemini API key set. Add your key in Settings \u2192 Read aloud."
            )
            android.widget.Toast.makeText(
                manager.context,
                "No Gemini API key set. Add your key in Settings \u2192 Read aloud.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        val gen = queueGen
        val idx = _index.value
        val onDone: () -> Unit = {
            if (gen == queueGen && idx == _index.value) {
                settings.clearLastError()
                advance()
            }
        }
        val onError: (String, Boolean) -> Unit = { msg, playedAny ->
            if (gen == queueGen && idx == _index.value && _playing.value) {
                // v1.49: every Gemini failure is persisted for the
                // Settings -> Read aloud "Last error" line.
                settings.recordLastError(msg)
                if (playedAny) {
                    // Part of this quote was already heard: don't replay
                    // the whole thing with the built-in voice — note the
                    // failure and move on to the next quote.
                    android.widget.Toast.makeText(
                        manager.context, msg, android.widget.Toast.LENGTH_LONG
                    ).show()
                    advance()
                } else if (settings.currentFallbackToBuiltin()) {
                    android.widget.Toast.makeText(
                        manager.context,
                        "Gemini read-aloud failed \u2014 using built-in voice.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    speakCurrentWithBuiltin(item, settings)
                } else {
                    android.widget.Toast.makeText(
                        manager.context, msg, android.widget.Toast.LENGTH_LONG
                    ).show()
                    stopPlayback()
                }
            }
        }
        if (manager.ttsPerNickVoices.value) {
            // Per-speaker cloud voices: each turn is synthesized with that
            // speaker's own Gemini voice (or their chosen override). When
            // 2+ speakers are identified by their voices, the
            // "says"/"replies" announcements are dropped; narration lines
            // keep the selected default cloud voice.
            val previewTurns = buildSpokenTurns(
                item.rawText, manager.dialogueLineBreaks.value
            )
            val speakers = previewTurns.map { it.nick }
            val turns = if (dropSpeakerAnnouncements(speakers))
                buildSpokenTurns(
                    item.rawText, manager.dialogueLineBreaks.value,
                    announceSpeakers = false
                )
            else previewTurns
            val voiceMap = geminiVoiceMapForSpeakers(
                settings, speakers
            )
            val narratorVoice = settings.currentNarratorVoice()
            val segments = turns.map { turn ->
                GeminiTtsSegment(
                    nick = turn.nick,
                    text = turn.text,
                    voice = when {
                        // *Action* lines use the dedicated narrator voice,
                        // always distinct from every speaker voice.
                        turn.isAction -> narratorVoice
                        else -> turn.nick?.lowercase()?.let { voiceMap[it] }
                            ?: settings.currentVoice()
                    },
                    // v1.60: labels for the multi-speaker request.
                    speakerLabel = when {
                        turn.isAction -> "Directions"
                        turn.nick.isNullOrBlank() -> "Narrator"
                        else -> null
                    }
                )
            }
            geminiTts.speakSegments(
                segments, key, onDone, onError,
                quoteDbId = item.dbId, quoteNumber = item.number
            )
        } else {
            geminiTts.speak(
                item.spokenText,
                key,
                settings.currentVoice(),
                onDone = onDone,
                onError = onError,
                quoteDbId = item.dbId,
                quoteNumber = item.number
            )
        }
        scope.launch { manager.markSeen(item.dbId, item.number) }
    }

    private fun advance() {
        if (!_playing.value) return
        // v1.63 sleep timer: "stop after this quote".
        if (_sleepAfterCurrent.value) {
            _sleepAfterCurrent.value = false
            stopPlayback()
            return
        }
        val next = _index.value + 1
        if (next < _items.value.size) {
            _index.value = next
            speakCurrent()
            return
        }
        if (!_radioMode.value) {
            stopPlayback()
            return
        }
        // v1.62 radio: the queue ran out — fetch the next batch and keep
        // going instead of stopping.
        scope.launch {
            val grew = appendRadioBatch()
            if (!_playing.value || !_radioMode.value) return@launch
            if (grew) {
                _index.value = next
                speakCurrent()
            } else {
                stopPlayback()
            }
        }
    }

    /** Releases TTS resources; called when the app process is going away. */
    fun release() {
        clearSleepTimer()
        tts?.stop()
        tts?.shutdown()
        tts = null
        geminiTts.release()
    }
}