package com.quotebrowser.app.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * v1.78 Read-along mode: a full-screen, karaoke-style reader that
 * highlights each word of the quote as the built-in TTS engine speaks it.
 *
 * Word tracking uses [UtteranceProgressListener.onRangeStart] (API 26+),
 * which reports the character offsets of the word being spoken. Some
 * engines never emit onRangeStart — when no range callback arrives
 * shortly after speech starts, the dialog falls back to estimated
 * per-word timing weighted by word length (the same weighting the
 * slideshow exporter's karaoke timing uses).
 *
 * The spoken text is exactly the displayed text, so engine offsets line
 * up with the highlight spans. The engine always uses the built-in TTS
 * voice path (cloud voices can't report word offsets); the user's chosen
 * narrator voice is applied, just like Read aloud does.
 */
private const val READ_ALONG_UTTERANCE_ID = "readalong"

private val READ_ALONG_SPEEDS = listOf(0.75f, 1.0f, 1.25f)
private val READ_ALONG_SPEED_LABELS = listOf("0.75x", "1x", "1.25x")

/** Char spans of each whitespace-separated word in [text], in order. */
private fun readAlongWordSpans(text: String): List<IntRange> {
    val spans = mutableListOf<IntRange>()
    var i = 0
    while (i < text.length) {
        while (i < text.length && text[i].isWhitespace()) i++
        if (i >= text.length) break
        val start = i
        while (i < text.length && !text[i].isWhitespace()) i++
        spans.add(start..(i - 1))
    }
    return spans
}

@Composable
fun ReadAlongDialog(
    dbId: String,
    number: Int,
    displayText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val wordSpans = remember(displayText) { readAlongWordSpans(displayText) }
    var highlight by remember { mutableStateOf<IntRange?>(null) }
    var playing by remember { mutableStateOf(false) }
    var engineReady by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Starting speech engine…") }
    var speedIndex by remember { mutableStateOf(1) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    // Offset of the current utterance's first char inside displayText
    // (non-zero when resuming from mid-quote).
    var baseOffset by remember { mutableStateOf(0) }
    // Whether onRangeStart fired for the current utterance.
    var rangeSeen by remember { mutableStateOf(false) }
    // Set around engine.stop() calls we initiate ourselves, so the
    // resulting onError callback isn't reported as a failure.
    var manualStop by remember { mutableStateOf(false) }
    var fallbackJob by remember { mutableStateOf<Job?>(null) }
    // v1.85: per-turn utterance bookkeeping for multi-speaker read-along.
    // Each queued utterance maps to the global char offset its first
    // character sits at inside displayText, so onRangeStart word offsets
    // (utterance-relative) map back to the highlight spans. The dialog is
    // short-lived, so the speaker-voices toggle is read once as a snapshot.
    val perNickOn = remember {
        context.getSharedPreferences("quote_browser_prefs", Context.MODE_PRIVATE)
            .getBoolean("tts_per_nick_voices", true)
    }
    val turns = remember(displayText, perNickOn) {
        if (perNickOn) displayTurns(displayText) else emptyList()
    }
    val multiSpeaker = turns.mapNotNull { it.nick }.distinct().size >= 2
    val utteranceBases = remember { mutableMapOf<String, Int>() }
    var lastUtteranceId by remember { mutableStateOf(READ_ALONG_UTTERANCE_ID) }
    // Per-speaker voice helpers, resolved against the live engine when
    // speaking (installed voices can only be queried from the engine).
    val geminiSettings = remember { GeminiTtsSettings.get(context) }
    val nickOverrides = remember { geminiSettings.currentNickVoiceOverrides() }
    val autoAssignVoices = remember { geminiSettings.currentAutoAssignSpeakerVoices() }
    val narratorBuiltin = remember { geminiSettings.currentNarratorVoiceBuiltin() }
    var pitchRateFallback by remember { mutableStateOf<Boolean?>(null) }

    /**
     * Applies one display turn's voice: the nick's stable installed voice
     * (or their chosen override), the narrator voice for *action* lines,
     * the default voice for narration — then multiplies the read-along
     * speed by whatever rate the voice itself applied (voice-pack preset
     * or deterministic fallback), so both stay in effect.
     */
    fun applyTurnVoice(engine: TextToSpeech, turn: DisplayTurn) {
        val speed = READ_ALONG_SPEEDS[speedIndex]
        val rateMul = if (turn.isAction) {
            applyNarratorVoice(engine, narratorBuiltin)
        } else {
            applySpeakerVoice(
                engine, turn.nick,
                turn.nick?.let { nickOverrides[it.lowercase()] },
                pitchRateFallback == true, autoAssignVoices
            )
        }
        engine.setSpeechRate(speed * rateMul)
    }

    fun stopFallback() {
        fallbackJob?.cancel()
        fallbackJob = null
    }

    /** Index of the word currently highlighted (or being read), for resume. */
    fun currentWordIndex(): Int {
        val h = highlight ?: return 0
        val idx = wordSpans.indexOfFirst { it.first <= h.first && h.first <= it.last }
        return if (idx >= 0) idx else 0
    }

    /**
     * Estimated word timing used only when the engine never emits
     * onRangeStart: walks the remaining words with per-word delays
     * weighted by word length, scaled by the selected speech rate.
     */
    suspend fun runEstimatedTiming(from: Int) {
        if (wordSpans.isEmpty()) return
        val speed = READ_ALONG_SPEEDS[speedIndex]
        val remaining = wordSpans.subList(from, wordSpans.size)
        val totalWeight = remaining.sumOf { (it.last - it.first + 1).toLong() }.toDouble()
        if (totalWeight <= 0) return
        // Rough speech rate: ~72ms per char at 1x.
        val estTotalMs = (displayText.length * 72.0 / speed).toLong().coerceAtLeast(600)
        for (span in remaining) {
            if (!playing) return
            highlight = span
            val w = (span.last - span.first + 1).toDouble()
            delay((w / totalWeight * estTotalMs).toLong().coerceAtLeast(60))
        }
    }

    fun speakFrom(wordIndex: Int) {
        val engine = tts ?: return
        if (wordSpans.isEmpty()) {
            statusText = "Nothing to read."
            return
        }
        stopFallback()
        val from = wordIndex.coerceIn(0, wordSpans.size - 1)
        baseOffset = wordSpans[from].first
        rangeSeen = false
        manualStop = false
        highlight = null
        utteranceBases.clear()
        if (multiSpeaker) {
            // v1.85: multi-speaker read-along — one queued utterance per
            // display turn, each in its speaker's stable voice (the same
            // deterministic assignment Read aloud uses). Word-range
            // callbacks stay utterance-relative, so each utterance maps
            // to its global offset for highlighting.
            if (pitchRateFallback == null) {
                pitchRateFallback = needsPitchRateFallback(
                    engine,
                    turns.map { it.nick },
                    { n -> nickOverrides[n.lowercase()] },
                    autoAssignVoices
                )
            }
            val charOffset = wordSpans[from].first
            var first = true
            for ((ti, turn) in turns.withIndex()) {
                val turnEnd = turn.lineStart + turn.text.length
                if (turnEnd <= charOffset) continue
                val startInTurn = (charOffset - turn.lineStart).coerceAtLeast(0)
                val text =
                    if (startInTurn > 0) turn.text.substring(startInTurn) else turn.text
                if (text.isBlank()) continue
                val utteranceId = "$READ_ALONG_UTTERANCE_ID:$ti"
                utteranceBases[utteranceId] = turn.lineStart + startInTurn
                lastUtteranceId = utteranceId
                applyTurnVoice(engine, turn)
                engine.speak(
                    text,
                    if (first) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                    null,
                    utteranceId
                )
                first = false
            }
            if (first) {
                statusText = "Nothing to read."
                return
            }
        } else {
            engine.setSpeechRate(READ_ALONG_SPEEDS[speedIndex])
            val utterance =
                if (baseOffset > 0) displayText.substring(baseOffset) else displayText
            utteranceBases[READ_ALONG_UTTERANCE_ID] = baseOffset
            lastUtteranceId = READ_ALONG_UTTERANCE_ID
            engine.speak(utterance, TextToSpeech.QUEUE_FLUSH, null, READ_ALONG_UTTERANCE_ID)
        }
        playing = true
        statusText = "Reading…"
        // Fallback watchdog: if the engine never reports word ranges,
        // estimate the timing instead of leaving the text unhighlighted.
        fallbackJob = scope.launch {
            delay(900)
            if (!rangeSeen && playing) runEstimatedTiming(from)
        }
    }

    fun pausePlayback() {
        manualStop = true
        tts?.stop()
        stopFallback()
        playing = false
        statusText = "Paused"
    }

    fun stopPlayback() {
        manualStop = true
        tts?.stop()
        stopFallback()
        playing = false
        highlight = null
        baseOffset = 0
        statusText = "Stopped"
    }

    fun onRangeStartMain(utteranceId: String?, start: Int, end: Int) {
        rangeSeen = true
        stopFallback()
        // v1.85: per-turn utterances report offsets relative to their own
        // text; map back through the utterance's global base offset.
        val base = utteranceBases[utteranceId] ?: baseOffset
        val s = base + start
        val e = base + end - 1
        highlight = s..e
    }

    /** True for this dialog's utterances, single or per-turn. */
    fun isReadAlongUtterance(utteranceId: String?): Boolean =
        utteranceId != null &&
            (utteranceId == READ_ALONG_UTTERANCE_ID ||
                utteranceId.startsWith("$READ_ALONG_UTTERANCE_ID:"))

    fun onTtsErrorMain() {
        if (manualStop) {
            manualStop = false
            return
        }
        playing = false
        stopFallback()
        highlight = null
        statusText = "The speech engine hit an error."
    }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            mainHandler.post {
                if (status == TextToSpeech.SUCCESS) {
                    engine?.let { e ->
                        e.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                if (isReadAlongUtterance(utteranceId)) {
                                    mainHandler.post { playing = true }
                                }
                            }

                            override fun onDone(utteranceId: String?) {
                                // v1.85: per-turn speech queues several
                                // utterances; only the last one finishing
                                // means the quote is done.
                                if (utteranceId == lastUtteranceId) {
                                    mainHandler.post {
                                        playing = false
                                        stopFallback()
                                        statusText = "Finished"
                                    }
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                if (isReadAlongUtterance(utteranceId)) {
                                    mainHandler.post { onTtsErrorMain() }
                                }
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                if (isReadAlongUtterance(utteranceId)) {
                                    mainHandler.post { onTtsErrorMain() }
                                }
                            }

                            override fun onRangeStart(
                                utteranceId: String?,
                                start: Int,
                                end: Int,
                                frame: Int
                            ) {
                                if (isReadAlongUtterance(utteranceId)) {
                                    mainHandler.post { onRangeStartMain(utteranceId, start, end) }
                                }
                            }
                        })
                        e.language = Locale.getDefault()
                        // Honor the user's chosen narrator voice, the same
                        // one Read aloud uses (falls back to the default).
                        val narratorVoice = runCatching {
                            GeminiTtsSettings.get(context).currentNarratorVoiceBuiltin()
                        }.getOrNull()
                        applyNarratorVoice(e, narratorVoice)
                        tts = e
                        engineReady = true
                        speakFrom(0)
                    }
                } else {
                    statusText = "Couldn't start the speech engine on this device."
                }
            }
        }
        onDispose {
            val e = tts
            tts = null
            e?.stop()
            e?.shutdown()
        }
    }

    val annotatedText = remember(displayText, highlight) {
        buildAnnotatedString {
            append(displayText)
            highlight?.let { r ->
                val s = r.first.coerceIn(0, displayText.length)
                val e = r.last.coerceIn(0, displayText.length - 1)
                if (displayText.isNotEmpty() && s <= e) {
                    addStyle(
                        SpanStyle(
                            background = Color(0xFFFFC400).copy(alpha = 0.45f),
                            color = Color(0xFFFFF8E1),
                            fontWeight = FontWeight.Bold
                        ),
                        s,
                        e + 1
                    )
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Read along",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
                HorizontalDivider()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "#$number",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                HorizontalDivider()
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { if (playing) pausePlayback() else speakFrom(currentWordIndex()) },
                        enabled = engineReady
                    ) {
                        Icon(
                            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (playing) "Pause" else "Play")
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { stopPlayback() },
                        enabled = engineReady
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Stop")
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = {
                            speedIndex = (speedIndex + 1) % READ_ALONG_SPEEDS.size
                            // A rate change takes effect on the next speak,
                            // so restart the current word when playing.
                            if (playing) speakFrom(currentWordIndex())
                        },
                        enabled = engineReady
                    ) {
                        Text(READ_ALONG_SPEED_LABELS[speedIndex])
                    }
                }
            }
        }
    }
}
