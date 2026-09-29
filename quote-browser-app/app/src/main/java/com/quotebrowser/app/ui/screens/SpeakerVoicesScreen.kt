package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.GeminiTtsSettings
import com.quotebrowser.app.ui.cleanSpokenNick
import com.quotebrowser.app.ui.components.NickVoicePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class SpeakerRow(
    val nick: String,
    val quoteCount: Int,
    val key: String
)

/**
 * v1.63: manage per-nick read-aloud voices from Settings → Read aloud,
 * without hunting for a nick's page. Nicks with a manual voice come
 * first, then the most-quoted speakers; search filters by name. Tapping a
 * row expands the same voice picker the nick page uses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerVoicesScreen(
    manager: DatabaseManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember { GeminiTtsSettings.get(context) }
    val geminiOverrides by settings.nickGeminiVoiceOverrides.collectAsStateWithLifecycle()
    val builtinOverrides by settings.nickVoiceOverrides.collectAsStateWithLifecycle()

    var tops by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        tops = withContext(Dispatchers.IO) {
            // v1.65: load every nick, not just the top 300 — the search box
            // must find rare nicks too (e.g. a nick with a single quote).
            // topNicks already scans all quotes; keeping the full list costs
            // the same I/O, and filtering happens in memory below.
            runCatching { manager.topNicks(Int.MAX_VALUE) }.getOrDefault(emptyList())
        }
        loading = false
    }

    var query by remember { mutableStateOf("") }

    val rows = remember(tops, geminiOverrides, builtinOverrides, query) {
        val countByKey = mutableMapOf<String, Pair<String, Int>>()
        for ((nick, count) in tops) {
            val k = cleanSpokenNick(nick).lowercase()
            val cur = countByKey[k]
            if (cur == null || count > cur.second) countByKey[k] = nick to count
        }
        val allKeys = (countByKey.keys +
            geminiOverrides.keys + builtinOverrides.keys).toSet()
        val q = query.trim().lowercase()
        allKeys
            .map { k ->
                val (display, count) = countByKey[k] ?: (k to 0)
                SpeakerRow(display, count, k)
            }
            .filter { r ->
                q.isEmpty() || r.nick.lowercase().contains(q)
            }
            .sortedWith(
                compareByDescending<SpeakerRow> {
                    val g = geminiOverrides[it.key]
                    val b = builtinOverrides[it.key]
                    !g.isNullOrBlank() || !b.isNullOrBlank()
                }.thenByDescending { it.quoteCount }
                    .thenBy { it.nick.lowercase() }
            )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Speaker voices") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Give any speaker their own voice for read-aloud. " +
                        "Used when Speaker voices is on (Settings \u2192 " +
                        "Read aloud). Nicks without a pick get the " +
                        "automatic voice — the same nick always sounds " +
                        "the same.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Find a nick") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
            }
            if (loading) {
                item {
                    Text(
                        "Loading speakers…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(rows, key = { it.key }) { row ->
                SpeakerVoiceRow(row = row, geminiOverrides, builtinOverrides)
            }
        }
    }
}

@Composable
private fun SpeakerVoiceRow(
    row: SpeakerRow,
    geminiOverrides: Map<String, String>,
    builtinOverrides: Map<String, String>
) {
    var expanded by remember(row.key) { mutableStateOf(false) }
    val custom = geminiOverrides[row.key]?.takeIf { it.isNotBlank() }
        ?: builtinOverrides[row.key]?.takeIf { it.isNotBlank() }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        row.nick,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        buildString {
                            if (row.quoteCount > 0) append("${row.quoteCount} quotes · ")
                            append(if (custom != null) "Custom: $custom" else "Automatic")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
            if (expanded) {
                NickVoicePicker(
                    nick = row.nick,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
