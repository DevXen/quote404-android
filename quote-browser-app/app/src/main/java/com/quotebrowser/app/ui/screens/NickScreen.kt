package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.GeminiTts
import com.quotebrowser.app.ui.GeminiTtsException
import com.quotebrowser.app.ui.GeminiTtsSettings
import com.quotebrowser.app.ui.NickDetail
import com.quotebrowser.app.ui.NickEntry
import com.quotebrowser.app.ui.NickViewModel
import com.quotebrowser.app.ui.VoiceRecordings
import com.quotebrowser.app.ui.buildGeminiSegments
import com.quotebrowser.app.ui.buildSpokenTurns
import com.quotebrowser.app.ui.cleanSpokenNick
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.components.NickAvatar
import com.quotebrowser.app.ui.components.NickVoicePicker
import com.quotebrowser.app.ui.components.LocalSingleDatabaseMode
import com.quotebrowser.app.ui.components.QuoteRow
import com.quotebrowser.app.ui.vmFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Deep-dive for one nick: quote count, average rating, per-database
 * breakdown, and their quotes (newest first).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NickScreen(
    nick: String,
    manager: DatabaseManager,
    onQuoteClick: (dbId: String, number: Int) -> Unit,
    onBack: () -> Unit,
    showNumber: Boolean,
    showRating: Boolean,
    onNickClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val vm: NickViewModel = viewModel(factory = vmFactory { NickViewModel(manager) })
    var detail by remember(nick) { mutableStateOf<NickDetail?>(null) }
    // Top connections, computed off the main thread and cached per nick
    // while the screen is open.
    var connections by remember(nick) { mutableStateOf<List<NickEntry>?>(null) }
    LaunchedEffect(nick) {
        detail = vm.detailFor(nick)
        connections = vm.topConnections(nick)
    }
    // v1.49: chronological nick timeline toggle.
    val timelineEnabled by manager.nickTimelineEnabled
        .collectAsStateWithLifecycle()
    // v1.58: this screen mixes databases, so site cards keep the global
    // page background outside the card (no single-DB page backdrop).
    CompositionLocalProvider(LocalSingleDatabaseMode provides false) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("<$nick>") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        val d = detail
        if (d == null) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            flingBehavior = rememberCappedFlingBehavior()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NickAvatar(nick = nick, size = 48.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "<$nick>",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatBlock(
                                value = d.quoteCount.toString(),
                                label = "quotes"
                            )
                            StatBlock(
                                value = d.avgRating?.let { "%.1f".format(it) } ?: "—",
                                label = "avg rating"
                            )
                            StatBlock(
                                value = d.dbCounts.size.toString(),
                                label = "databases"
                            )
                        }
                        if (d.dbCounts.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                d.dbCounts.forEach { (label, count) ->
                                    AssistChip(
                                        onClick = {},
                                        label = { Text("$label · $count") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                NickVoicePicker(nick = nick)
            }
            // v1.64.2: re-record every saved quote this nick speaks in, with
            // the nick's current voice assignment.
            item {
                NickRerecordCard(nick = nick, manager = manager)
            }
            // v1.49: chronological nick timeline — per database, oldest
            // quote first (quote numbers in these archives are roughly
            // chronological). Gated by the feature toggle.
            if (timelineEnabled) {
                item {
                    NickTimelineCard(
                        detail = d,
                        manager = manager,
                        onQuoteClick = onQuoteClick
                    )
                }
            }
            val conns = connections
            if (conns != null && conns.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Top connections",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(4.dp))
                            conns.forEach { entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNickClick(entry.nick) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    NickAvatar(nick = entry.nick, size = 36.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        "<${entry.nick}>",
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${entry.count} shared quotes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            items(d.quotes, key = { "${it.dbId}/${it.quote.number}" }) { sq ->
                QuoteRow(
                    quote = sq.quote,
                    dbId = sq.dbId,
                    isFavorite = false,
                    onToggleFavorite = {},
                    showFavoriteAction = false,
                    onClick = { onQuoteClick(sq.dbId, sq.quote.number) },
                    showNumber = showNumber,
                    showRating = showRating,
                    sourceLabel = manager.shortLabel(sq.dbId),
                    sourceDbId = sq.dbId,
                    onNickClick = onNickClick)
            }
        }
    }
    } // LocalSingleDatabaseMode
}

/**
 * v1.49: chronological nick timeline. The archives carry no per-quote
 * timestamps, but quote numbers are roughly chronological within each
 * database, so each database gets its own oldest-first rail with a
 * timeline dot per quote. Tapping a quote opens it.
 */
@Composable
private fun NickTimelineCard(
    detail: NickDetail,
    manager: DatabaseManager,
    onQuoteClick: (dbId: String, number: Int) -> Unit
) {
    // Per database, oldest quote first; cap the total entries shown.
    val groups = remember(detail) {
        detail.quotes.groupBy { it.dbId }
            .mapValues { (_, sqs) -> sqs.sortedBy { it.quote.number } }
            .toList()
            .sortedByDescending { (_, sqs) -> sqs.size }
    }
    if (groups.isEmpty()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Timeline", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Oldest first, per database",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            var shown = 0
            for ((dbId, sqs) in groups) {
                if (shown >= 80) break
                Text(
                    manager.shortLabel(dbId),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
                for (sq in sqs) {
                    if (shown >= 80) break
                    shown++
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQuoteClick(sq.dbId, sq.quote.number) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Timeline rail: dot with a connecting line.
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 6.dp, end = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                "#${sq.quote.number}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                sq.quote.text,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            if (shown >= 80) {
                Text(
                    "Showing the first 80 \u2014 open a database view for the rest.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Most-quoted nicks leaderboard across all databases.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NickLeaderboardScreen(
    manager: DatabaseManager,
    onNickClick: (String) -> Unit,
    onCompareClick: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: NickViewModel = viewModel(factory = vmFactory { NickViewModel(manager) })
    val leaderboard by vm.leaderboard.collectAsState()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Most quoted nicks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onCompareClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = "Compare nicks"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (leaderboard.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            flingBehavior = rememberCappedFlingBehavior()) {
            itemsIndexed(leaderboard, key = { _, e -> e.nick.lowercase() }) { index, entry ->
                ListItem(
                    headlineContent = {
                        Text(
                            "<${entry.nick}>",
                            fontWeight = if (index < 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    supportingContent = {
                        Text("${entry.count} quotes")
                    },
                    leadingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(32.dp)
                            )
                            NickAvatar(nick = entry.nick, size = 40.dp)
                        }
                    },
                    modifier = Modifier.clickable { onNickClick(entry.nick) }
                )
            }
        }
    }
}

/**
 * v1.64.2: re-record every saved voice recording this nick speaks in, using
 * the nick's current voice assignment. Runs quote by quote with a progress
 * indicator; each finished quote is filed as a new active generation and
 * the old audio is kept as an older version.
 */
@Composable
private fun NickRerecordCard(
    nick: String,
    manager: DatabaseManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiTts = remember { GeminiTts(context) }
    val geminiSettings = remember { GeminiTtsSettings.get(context) }
    DisposableEffect(geminiTts) { onDispose { geminiTts.release() } }

    var scanning by remember(nick) { mutableStateOf(false) }
    var matches by remember(nick) { mutableStateOf<List<Pair<String, Int>>?>(null) }
    var showConfirm by remember(nick) { mutableStateOf(false) }
    var progress by remember(nick) { mutableStateOf<Triple<Int, Int, String>?>(null) }
    var job by remember(nick) { mutableStateOf<Job?>(null) }

    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    /** Saved recordings where [nick] is one of the speakers. */
    suspend fun findMatches(): List<Pair<String, Int>> =
        withContext(Dispatchers.IO) {
            val cleaned = cleanSpokenNick(nick).lowercase()
            val breaks = manager.dialogueLineBreaks.value
            VoiceRecordings.recordings.value.keys.mapNotNull { k ->
                val dbId = k.substringBeforeLast("|")
                val number = k.substringAfterLast("|").toIntOrNull()
                    ?: return@mapNotNull null
                val text = try {
                    manager.repoFor(dbId).getQuote(number)?.text
                } catch (e: Exception) {
                    null
                } ?: return@mapNotNull null
                val speaks = buildSpokenTurns(text, breaks).any {
                    cleanSpokenNick(it.nick ?: "").lowercase() == cleaned
                }
                if (speaks) dbId to number else null
            }
        }

    fun startRun(matches: List<Pair<String, Int>>) {
        val key = geminiSettings.currentApiKey()
        if (key.isBlank()) {
            toast("Add a Gemini API key in Settings → Read aloud first")
            return
        }
        showConfirm = false
        progress = Triple(0, matches.size, "")
        job = scope.launch {
            var ok = 0
            var failed = 0
            for ((i, m) in matches.withIndex()) {
                ensureActive()
                val (dbId, number) = m
                progress = Triple(i, matches.size, "#$number")
                try {
                    val text = withContext(Dispatchers.IO) {
                        manager.repoFor(dbId).getQuote(number)?.text
                    } ?: throw GeminiTtsException("Quote #$number not found")
                    val segments = buildGeminiSegments(
                        text,
                        manager.dialogueLineBreaks.value,
                        geminiSettings,
                        manager.ttsPerNickVoices.value
                    )
                    // v1.65: re-recording from one nick's page regenerates
                    // only that nick's chunks. Every other speaker keeps the
                    // voice from the existing recording, so their chunks hit
                    // the on-device cache instead of burning fresh quota on
                    // a voice assignment that changed after the recording.
                    // (Pre-v1.65 recordings have no per-nick voices filed,
                    // so they fall back to the current assignments.)
                    val recordedVoices =
                        VoiceRecordings.get(dbId, number)?.active?.nickVoices
                            .orEmpty()
                    val initiating = cleanSpokenNick(nick).lowercase()
                    val fixed = if (recordedVoices.isEmpty()) segments
                    else segments.map { seg ->
                        val key = seg.nick?.let { cleanSpokenNick(it).lowercase() }
                        if (key != null && key != initiating &&
                            recordedVoices.containsKey(key)
                        ) {
                            seg.copy(voice = recordedVoices.getValue(key))
                        } else seg
                    }
                    val result = geminiTts.synthesizeSilently(fixed, key)
                    VoiceRecordings.record(
                        dbId, number, result.files, result.voices,
                        result.nickVoices
                    )
                    ok++
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    failed++
                }
            }
            progress = null
            job = null
            toast(
                if (failed == 0) "Re-recorded $ok quotes"
                else "Re-recorded $ok quotes, $failed failed"
            )
        }
    }

    val found = matches
    if (showConfirm && found != null) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Re-record ${found.size} quotes?") },
            text = {
                Text(
                    "Every saved recording where <$nick> speaks is " +
                        "re-generated with the nick's current voice; other " +
                        "speakers keep their existing audio. " +
                        "The old audio is kept as an older version. " +
                        "This uses Gemini quota and may take a while."
                )
            },
            confirmButton = {
                TextButton(onClick = { startRun(found) }) { Text("Re-record") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Voice recordings",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(4.dp))
            val prog = progress
            if (prog != null) {
                val (done, total, label) = prog
                Text(
                    "Re-recording $label ($done/$total)…",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { done.toFloat() / total.coerceAtLeast(1).toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        job?.cancel()
                        job = null
                        progress = null
                        toast("Re-recording cancelled")
                    }
                ) { Text("Cancel") }
            } else {
                Text(
                    "After changing this speaker's voice, re-record every " +
                        "saved quote they speak in — the new audio uses " +
                        "the current voice assignment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (scanning) return@Button
                        scanning = true
                        scope.launch {
                            try {
                                val found = findMatches()
                                matches = found
                                if (found.isEmpty()) {
                                    toast("No saved recordings feature <$nick>")
                                } else {
                                    showConfirm = true
                                }
                            } finally {
                                scanning = false
                            }
                        }
                    },
                    enabled = !scanning
                ) {
                    Text(if (scanning) "Scanning…" else "Re-record all with this voice")
                }
            }
        }
    }
}
