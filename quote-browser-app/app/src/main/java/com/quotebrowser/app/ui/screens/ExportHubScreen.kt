package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Export hub: the export queue and the export-quotes screen in one place.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportHubScreen(
    queueCount: Int,
    onQueueClick: () -> Unit,
    onExportClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Export") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                HubRow(
                    title = if (queueCount == 0) "Export tray"
                    else "Export tray ($queueCount)",
                    subtitle = "Quotes staged for export, ready to write out.",
                    icon = Icons.Filled.PlaylistAddCheck,
                    onClick = onQueueClick
                )
            }
            item {
                HubRow(
                    title = "Export quotes",
                    subtitle = "Export from any collection: database, favorites, category and more.",
                    icon = Icons.Filled.Download,
                    onClick = onExportClick
                )
            }
        }
    }
}
