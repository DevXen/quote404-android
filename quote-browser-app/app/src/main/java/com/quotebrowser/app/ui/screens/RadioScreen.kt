package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.PlaylistViewModel
import com.quotebrowser.app.ui.RADIO_STATION_ALL

/**
 * v1.62 quote radio: endless shuffled read-aloud from one database.
 * Pick a station (database), hit play, and quotes keep coming until you
 * stop — the queue refills with fresh random quotes as it plays.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    manager: DatabaseManager,
    mainVm: MainViewModel,
    playlistVm: PlaylistViewModel,
    onBack: () -> Unit,
    onOpenQuote: (dbId: String, number: Int) -> Unit = { _, _ -> }
) {
    val databases by mainVm.databases.collectAsStateWithLifecycle()
    val currentDbId by manager.currentDbId.collectAsStateWithLifecycle()
    val playing by playlistVm.playing.collectAsStateWithLifecycle()
    val radioMode by playlistVm.radioMode.collectAsStateWithLifecycle()
    val radioDbId by playlistVm.radioDbId.collectAsStateWithLifecycle()
    val items by playlistVm.items.collectAsStateWithLifecycle()
    val index by playlistVm.index.collectAsStateWithLifecycle()

    val radioActive = radioMode && items.isNotEmpty()
    val current = items.getOrNull(index)
    val stationId = radioDbId ?: currentDbId
    val stationLabel = when (stationId) {
        RADIO_STATION_ALL -> "Everything shuffle"
        else -> databases.firstOrNull { it.id == stationId }?.label
            ?: manager.shortLabel(stationId)
    }
    var stationExpanded by remember { mutableStateOf(false) }
    // v1.63 sleep timer.
    val sleepMinutes by playlistVm.sleepMinutes.collectAsStateWithLifecycle()
    val sleepAfterCurrent by playlistVm.sleepAfterCurrent.collectAsStateWithLifecycle()
    var sleepDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quote radio") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Station picker.
            ExposedDropdownMenuBox(
                expanded = stationExpanded,
                onExpandedChange = { stationExpanded = it }
            ) {
                OutlinedTextField(
                    value = stationLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Station") },
                    leadingIcon = {
                        Icon(Icons.Filled.Radio, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = stationExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = stationExpanded,
                    onDismissRequest = { stationExpanded = false }
                ) {
                    // v1.63: shuffle every database as one station.
                    DropdownMenuItem(
                        text = { Text("Everything shuffle") },
                        onClick = {
                            stationExpanded = false
                            playlistVm.startRadio(RADIO_STATION_ALL)
                        }
                    )
                    databases.forEach { db ->
                        DropdownMenuItem(
                            text = { Text(db.label) },
                            onClick = {
                                stationExpanded = false
                                playlistVm.startRadio(db.id)
                            }
                        )
                    }
                }
            }

            if (radioActive && current != null) {
                // Now playing.
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    onClick = { onOpenQuote(current.dbId, current.number) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            "${manager.shortLabel(current.dbId)} #${current.number}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            current.rawText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                // Transport controls.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = { playlistVm.stopPlayback() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.Stop, contentDescription = "Stop radio")
                    }
                    Spacer(Modifier.width(16.dp))
                    FilledIconButton(
                        onClick = {
                            if (playing) playlistVm.pausePlayback()
                            else playlistVm.resumePlayback()
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playing) "Pause" else "Resume"
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    FilledIconButton(
                        onClick = { playlistVm.skip() },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next quote")
                    }
                    Spacer(Modifier.width(16.dp))
                    // v1.63 sleep timer.
                    FilledIconButton(
                        onClick = { sleepDialog = true },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.Timer, contentDescription = "Sleep timer")
                    }
                }
                val sleepLabel = when {
                    sleepAfterCurrent -> "Sleep timer: stops after this quote."
                    sleepMinutes != null -> "Sleep timer: stops in $sleepMinutes min."
                    else -> null
                }
                if (sleepLabel != null) {
                    Text(
                        sleepLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Text(
                    if (playing) "On air — shuffled endlessly from $stationLabel."
                    else "Paused.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Filled.Radio,
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Endless shuffled read-aloud from $stationLabel. " +
                        "Quotes keep coming until you stop it — " +
                        "hands-free listening.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Button(
                    onClick = { playlistVm.startRadio(stationId) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start radio", fontWeight = FontWeight.Bold)
                }
                if (playing) {
                    Text(
                        "A playlist is currently playing — starting the " +
                            "radio replaces it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }

    // v1.63 sleep timer dialog.
    if (sleepDialog) {
        val options = listOf(
            null to "Off",
            15 to "15 minutes",
            30 to "30 minutes",
            60 to "60 minutes",
            -1 to "After this quote"
        )
        val currentChoice = when {
            sleepAfterCurrent -> -1
            else -> sleepMinutes
        }
        AlertDialog(
            onDismissRequest = { sleepDialog = false },
            title = { Text("Sleep timer") },
            text = {
                Column {
                    options.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentChoice == minutes,
                                onClick = {
                                    if (minutes == -1) {
                                        playlistVm.setSleepAfterCurrent(true)
                                    } else {
                                        playlistVm.setSleepTimer(minutes)
                                    }
                                    sleepDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sleepDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
