package com.quotebrowser.app.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * v1.61.20: playback + management UI for saved voice recordings.
 *
 * The manifest ([VoiceRecordings]) tracks every read-aloud synthesis per
 * quote: the active generation plus older generations kept as alternates
 * when a speaker's voice changes. This file holds the raw-PCM player, the
 * per-quote manage dialog (long-press the card voice icon), and the
 * Settings → Voice recordings screen.
 */

/** Plays the raw 16-bit mono 24 kHz PCM files Gemini TTS produces. */
object VoiceAudioPlayer {
    private val _playingKey = MutableStateFlow<String?>(null)
    val playingKey: StateFlow<String?> = _playingKey.asStateFlow()
    private var thread: Thread? = null

    fun toggle(key: String, files: List<File>) {
        if (_playingKey.value == key) stop() else play(key, files)
    }

    fun play(key: String, files: List<File>) {
        stop()
        _playingKey.value = key
        thread = Thread {
            var track: AudioTrack? = null
            try {
                val minBuf = AudioTrack.getMinBufferSize(
                    24000, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(24000)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf.coerceAtLeast(16384))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
                track.play()
                val buf = ByteArray(16384)
                var stopped = false
                for (f in files) {
                    if (stopped || _playingKey.value != key) break
                    try {
                        FileInputStream(f).use { ins ->
                            var n = ins.read(buf)
                            while (n > 0 && !stopped && _playingKey.value == key) {
                                var off = 0
                                while (off < n) {
                                    val w = track.write(buf, off, n - off)
                                    if (w < 0) break
                                    off += w
                                }
                                n = ins.read(buf)
                            }
                            if (_playingKey.value != key) stopped = true
                        }
                    } catch (e: Exception) {
                    }
                }
            } catch (e: Exception) {
            } finally {
                try { track?.stop() } catch (e: Exception) { }
                try { track?.release() } catch (e: Exception) { }
                if (_playingKey.value == key) _playingKey.value = null
            }
        }.also { it.isDaemon = true; it.start() }
    }

    fun stop() {
        _playingKey.value = null
        try { thread?.join(800) } catch (e: Exception) { }
        thread = null
    }
}

private val voiceDateFmt = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US)

private fun formatVoiceBytes(bytes: Long): String = when {
    bytes < 0 -> "—"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
}

private fun generationBytes(gen: VoiceGeneration): Long =
    gen.files.sumOf { VoiceRecordings.audioFile(it)?.length() ?: 0L }

/** Per-quote recording management, opened by long-pressing the card voice icon. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceManageDialog(
    dbId: String,
    number: Int,
    manager: DatabaseManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { VoiceRecordings.ensure(context) }
    val recordings by VoiceRecordings.recordings.collectAsStateWithLifecycle()
    val rec = recordings[VoiceRecordings.key(dbId, number)]
    var quoteText by remember(dbId, number) { mutableStateOf<String?>(null) }
    LaunchedEffect(dbId, number) {
        quoteText = try {
            withContext(Dispatchers.IO) { manager.repoFor(dbId).getQuote(number)?.text }
        } catch (e: Exception) { null }
    }
    var confirm by remember { mutableStateOf<VoiceConfirm?>(null) }

    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    // v1.64.2: eager regeneration. Tapping Regenerate synthesizes the
    // replacement audio RIGHT AWAY (same segments/voices read-aloud would
    // use, including current speaker-voice assignments) instead of waiting
    // for the next play. The finished audio is filed as the new active
    // generation; the old active is kept as an older version.
    val geminiTts = remember { GeminiTts(context) }
    val geminiSettings = remember { GeminiTtsSettings.get(context) }
    val regenScope = rememberCoroutineScope()
    var genProgress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    DisposableEffect(geminiTts) { onDispose { geminiTts.release() } }

    fun regenerateNow() {
        val text = quoteText
        if (text.isNullOrBlank()) {
            toast("Quote text not loaded yet")
            return
        }
        if (genProgress != null) return
        val key = geminiSettings.currentApiKey()
        if (key.isBlank()) {
            // No API key: fall back to the legacy lazy path — retire the
            // active now, re-generate on the next play.
            VoiceRecordings.regenerate(dbId, number)
            toast("Will re-generate on next play")
            return
        }
        genProgress = 0 to 1
        regenScope.launch {
            try {
                val segments = buildGeminiSegments(
                    text,
                    manager.dialogueLineBreaks.value,
                    geminiSettings,
                    manager.ttsPerNickVoices.value
                )
                val result = geminiTts.synthesizeSilently(
                    segments, key,
                    onProgress = { done, total -> genProgress = done to total }
                )
                // record() retires the previous active generation into the
                // older-versions list automatically.
                VoiceRecordings.record(
                    dbId, number, result.files, result.voices, result.nickVoices
                )
                genProgress = null
                toast("New audio ready")
            } catch (e: Exception) {
                genProgress = null
                val msg = e.message ?: "unknown error"
                toast("Regeneration failed: ${msg.take(120)}")
            }
        }
    }

    Dialog(onDismissRequest = {
        VoiceAudioPlayer.stop()
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.heightIn(max = 560.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Voice recordings",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "#$number · ${manager.shortLabel(dbId)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                quoteText?.let { text ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text.take(160),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(12.dp))
                // v1.64.2: eager-regeneration progress indicator.
                genProgress?.let { (done, total) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "Generating audio… ($done/$total)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { done.toFloat() / total.coerceAtLeast(1).toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (rec == null ||
                    (rec.active.files.isEmpty() && rec.alternates.isEmpty())
                ) {
                    Text(
                        "No saved recordings for this quote yet. Play it " +
                            "with read-aloud and the audio is kept here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        flingBehavior = rememberCappedFlingBehavior()) {
                        if (rec.active.files.isNotEmpty()) {
                            item {
                                VoiceGenerationCard(
                                    title = "Active",
                                    isActive = true,
                                    gen = rec.active,
                                    playKey = "$dbId|$number|active",
                                    onRegenerate = if (genProgress != null) null else {
                                        {
                                            confirm = VoiceConfirm(
                                                title = "Regenerate audio?",
                                                message = "The current audio is kept as an older " +
                                                    "version, and fresh audio is generated " +
                                                    "right away with the current voice " +
                                                    "assignments.",
                                                onConfirm = { regenerateNow() }
                                            )
                                        }
                                    },
                                    onDelete = {
                                        confirm = VoiceConfirm(
                                            title = "Delete this recording?",
                                            message = if (rec.alternates.isNotEmpty())
                                                "The newest older version becomes active."
                                            else "This removes the quote's saved audio.",
                                            onConfirm = {
                                                VoiceRecordings.deleteActive(dbId, number)
                                                toast("Recording deleted")
                                            }
                                        )
                                    },
                                    onMakeActive = null,
                                    onDeleteAlt = null
                                )
                            }
                        }
                        if (rec.alternates.isNotEmpty()) {
                            item {
                                Text(
                                    "Older versions",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            items(
                                rec.alternates,
                                key = { gen -> gen.files.firstOrNull() ?: gen.updatedAt.toString() }
                            ) { gen ->
                                val index = rec.alternates.indexOf(gen)
                                VoiceGenerationCard(
                                    title = "Version ${index + 2}",
                                    isActive = false,
                                    gen = gen,
                                    playKey = "$dbId|$number|alt|$index",
                                    onRegenerate = null,
                                    onDelete = null,
                                    onMakeActive = {
                                        VoiceRecordings.setActiveAlternate(
                                            dbId, number, index
                                        )
                                        toast("Older version is now active")
                                    },
                                    onDeleteAlt = {
                                        confirm = VoiceConfirm(
                                            title = "Delete this version?",
                                            message = "Its audio files are removed.",
                                            onConfirm = {
                                                VoiceRecordings.deleteAlternate(
                                                    dbId, number, index
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Replaying a quote reuses its saved audio — nothing new " +
                        "is downloaded. Changing a speaker's voice generates " +
                        "fresh audio on the next play; the old version stays " +
                        "here as a backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        VoiceAudioPlayer.stop()
                        onDismiss()
                    }) { Text("Close") }
                }
            }
        }
    }

    confirm?.let { c ->
        AlertDialog(
            onDismissRequest = { confirm = null },
            title = { Text(c.title) },
            text = { Text(c.message) },
            confirmButton = {
                TextButton(onClick = {
                    confirm = null
                    c.onConfirm()
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { confirm = null }) { Text("Cancel") }
            }
        )
    }
}

private data class VoiceConfirm(
    val title: String,
    val message: String,
    val onConfirm: () -> Unit
)

@Composable
private fun VoiceGenerationCard(
    title: String,
    isActive: Boolean,
    gen: VoiceGeneration,
    playKey: String,
    onRegenerate: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onMakeActive: (() -> Unit)?,
    onDeleteAlt: (() -> Unit)?
) {
    val playingKey by VoiceAudioPlayer.playingKey.collectAsStateWithLifecycle()
    val isPlaying = playingKey == playKey
    val files = remember(gen) {
        gen.files.mapNotNull { VoiceRecordings.audioFile(it) }
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AudioFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    formatVoiceBytes(generationBytes(gen)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "${gen.voices} · ${voiceDateFmt.format(Date(gen.updatedAt))} · " +
                    "${gen.files.size} clip${if (gen.files.size == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { VoiceAudioPlayer.toggle(playKey, files) },
                    enabled = files.isNotEmpty()
                ) {
                    Icon(
                        if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isPlaying) "Stop" else "Play")
                }
                if (onMakeActive != null) {
                    OutlinedButton(onClick = onMakeActive) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Make active")
                    }
                }
                if (onRegenerate != null) {
                    OutlinedButton(onClick = onRegenerate) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Regenerate")
                    }
                }
                Spacer(Modifier.weight(1f))
                val deleteFn = onDelete ?: onDeleteAlt
                if (deleteFn != null) {
                    IconButton(onClick = deleteFn) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/** v1.61.21: sort modes for Settings → Voice recordings. */
private enum class VoiceRecSort(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    NUMBER("Quote #"),
    LARGEST("Largest"),
    DATABASE("Database")
}

/** Settings → Voice recordings: every saved read-aloud, with storage totals. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceRecordingsScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    onManageQuote: (dbId: String, number: Int) -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { VoiceRecordings.ensure(context) }
    val recordings by VoiceRecordings.recordings.collectAsStateWithLifecycle()
    val entries = remember(recordings) { recordings.values.toList() }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    // v1.61.21: search + sorting.
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(VoiceRecSort.NEWEST) }
    var sortExpanded by remember { mutableStateOf(false) }
    val texts = remember { mutableStateMapOf<String, String>() }

    // Quote text is loaded in the background so search can match it; the
    // map is cached, so re-filtering and re-sorting never re-query.
    LaunchedEffect(entries) {
        withContext(Dispatchers.IO) {
            for (rec in entries) {
                val k = VoiceRecordings.key(rec.dbId, rec.number)
                if (texts.containsKey(k)) continue
                val t = try {
                    manager.repoFor(rec.dbId).getQuote(rec.number)?.text
                } catch (e: Exception) { null }
                if (!t.isNullOrBlank()) texts[k] = t
            }
        }
    }

    val visible = remember(entries, query, sortMode, texts.toMap()) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) entries else entries.filter { rec ->
            val k = VoiceRecordings.key(rec.dbId, rec.number)
            rec.number.toString().contains(q) ||
                rec.active.voices.lowercase().contains(q) ||
                manager.shortLabel(rec.dbId).lowercase().contains(q) ||
                (texts[k]?.lowercase()?.contains(q) == true)
        }
        when (sortMode) {
            VoiceRecSort.NEWEST -> filtered.sortedByDescending { it.active.updatedAt }
            VoiceRecSort.OLDEST -> filtered.sortedBy { it.active.updatedAt }
            VoiceRecSort.NUMBER -> filtered.sortedBy { it.number }
            VoiceRecSort.LARGEST ->
                filtered.sortedByDescending { generationBytes(it.active) }
            VoiceRecSort.DATABASE -> filtered.sortedWith(
                compareBy({ manager.shortLabel(it.dbId) }, { it.number })
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice recordings") },
                navigationIcon = {
                    IconButton(onClick = {
                        VoiceAudioPlayer.stop()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            flingBehavior = rememberCappedFlingBehavior()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${entries.size} quote${if (entries.size == 1) "" else "s"} · " +
                                formatVoiceBytes(VoiceRecordings.totalBytes()),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Saved read-aloud audio is included in full " +
                                "backups, and replaying a quote reuses it " +
                                "instead of downloading again.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entries.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { confirmDeleteAll = true }
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Delete all recordings")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            // v1.61.21: search + sort row.
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search #, voice, text…") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { sortExpanded = true }) {
                        Text(sortMode.label)
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = "Change sort order"
                        )
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        VoiceRecSort.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) },
                                onClick = { sortExpanded = false; sortMode = mode },
                                trailingIcon = if (mode == sortMode) {
                                    {
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
            if (visible.isEmpty()) {
                item {
                    Text(
                        if (query.isBlank())
                            "No voice recordings yet. Play any quote with " +
                                "read-aloud and its generated audio is saved here " +
                                "for replay, backup, and version management."
                        else "No recordings match \"$query\".",

                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            items(visible, key = { r -> VoiceRecordings.key(r.dbId, r.number) }) { rec ->
                Card(
                    onClick = { onManageQuote(rec.dbId, rec.number) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "#${rec.number} · ${manager.shortLabel(rec.dbId)}",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                "${rec.active.voices} · " +
                                    voiceDateFmt.format(Date(rec.active.updatedAt)) +
                                    if (rec.alternates.isNotEmpty())
                                        " · ${rec.alternates.size} older"
                                    else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            texts[VoiceRecordings.key(rec.dbId, rec.number)]?.let { t ->
                                Text(
                                    t.replace("\n", " ").take(110),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            formatVoiceBytes(generationBytes(rec.active)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text("Delete all recordings?") },
            text = {
                Text(
                    "Every saved voice recording and its audio files will " +
                        "be removed. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteAll = false
                    VoiceRecordings.deleteAll()
                    Toast.makeText(
                        context, "All recordings deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/** v1.61.20: keep the manifest warm from app start so cards/search see it. */
fun warmVoiceRecordings(context: android.content.Context) {
    VoiceRecordings.ensure(context)
}
