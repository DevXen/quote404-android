package com.quotebrowser.app.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DupGroup
import com.quotebrowser.app.data.canonical
import com.quotebrowser.app.data.findDuplicateGroups
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.QuoteNumberBadge
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.SourceChip
import kotlinx.coroutines.launch

/**
 * Duplicate finder: scans every database for exact and near-duplicate
 * quotes and lists the groups so you can clean them up.
 */
@Composable
fun DuplicateScreen(
    manager: DatabaseManager,
    onQuoteClick: (dbId: String, number: Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var scanning by remember { mutableStateOf(false) }
    var scanned by remember { mutableStateOf(false) }
    var groups by remember { mutableStateOf<List<DupGroup>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    // v1.49: the duplicate finder and merge never run while the feature
    // toggle is off.
    val dupMergeEnabled by manager.dupMergeEnabled.collectAsStateWithLifecycle()
    var mergeTarget by remember { mutableStateOf<DupGroup?>(null) }

    fun scan() {
        scanning = true
        error = null
        scope.launch {
            try {
                groups = findDuplicateGroups(manager)
                scanned = true
            } catch (e: Exception) {
                error = "Scan failed: ${e.message}"
            } finally {
                scanning = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Duplicate finder") {
            if (scanned && !scanning) {
                Text(
                    "${groups.size} groups",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        when {
            scanning -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Scanning all databases…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            error != null -> {
                EmptyState(error ?: "Scan failed.")
                Button(
                    onClick = ::scan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("Try again") }
            }
            !scanned -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Finds quotes that appear more than once — identical " +
                            "copies and near-duplicates — across all databases.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    // v1.49: never auto-run or offer a scan while disabled.
                    if (dupMergeEnabled) {
                        Button(onClick = ::scan) { Text("Scan for duplicates") }
                    } else {
                        Text(
                            "Duplicate merge is turned off. Enable it under " +
                                "Settings → Features to scan and merge " +
                                "duplicates.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            groups.isEmpty() -> {
                EmptyState("No duplicates found.\nYour archives are clean.")
                Button(
                    onClick = ::scan,
                    // v1.49: never run while the feature toggle is off.
                    enabled = dupMergeEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("Scan again") }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Button(
                            onClick = ::scan,
                            // v1.49: never run while the feature toggle is off.
                            enabled = dupMergeEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) { Text("Scan again") }
                    }
                    items(groups, key = { g -> g.members.joinToString("|") { it.dbId + "/" + it.quote.number } + g.exact }) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Column(
                                Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (group.exact) "Exact duplicate"
                                        else "Near duplicate",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // v1.49 one-tap merge: keeps the best copy
                                    // and hides the rest after confirmation.
                                    TextButton(
                                        onClick = { mergeTarget = group }
                                    ) {
                                        Text("Merge")
                                    }
                                }
                                // v1.59: exact groups keep the canonical
                                // (Bash.org) copy — list it first and badge it.
                                val shownMembers = if (group.exact) {
                                    group.members.sortedWith(
                                        compareBy(
                                            { it != group.canonical },
                                            { it.quote.number }
                                        )
                                    )
                                } else {
                                    group.members
                                }
                                shownMembers.forEach { m ->
                                    Card(
                                        onClick = { onQuoteClick(m.dbId, m.quote.number) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                                        )
                                    ) {
                                        Column(
                                            Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                SourceChip(
                                                    label = manager.shortLabel(m.dbId),
                                                    dbId = m.dbId
                                                )
                                                Spacer(Modifier.padding(4.dp))
                                                QuoteNumberBadge(number = m.quote.number)
                                                // v1.59: badge the canonical copy.
                                                if (group.exact && m == group.canonical) {
                                                    Spacer(Modifier.padding(4.dp))
                                                    Text(
                                                        if (m.dbId.contains("bash", ignoreCase = true))
                                                            "Canonical — Bash.org"
                                                        else "Canonical",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    m.quote.text,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                TextButton(
                                                    onClick = {
                                                        manager.setQuoteHidden(
                                                            m.dbId, m.quote.number, true
                                                        )
                                                        // Drop the member from the
                                                        // results; groups with
                                                        // fewer than two members
                                                        // left disappear.
                                                        groups = groups.mapNotNull { g ->
                                                            val kept = g.members.filterNot {
                                                                it.dbId == m.dbId &&
                                                                    it.quote.number == m.quote.number
                                                            }
                                                            if (kept.size < 2) null
                                                            else g.copy(members = kept)
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            "Hidden — restore it under Content Filter",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                ) {
                                                    Text("Hide")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    // v1.49 merge confirmation: lists every source database involved and
    // hides the rest after confirmation. v1.59: exact groups keep the
    // canonical (Bash.org) copy; near-duplicate groups keep the
    // highest-rated copy (ties broken by lowest number).
    val target = mergeTarget
    if (target != null) {
        val keep = if (target.exact) {
            target.canonical
        } else {
            target.members.maxWithOrNull(
                compareBy(
                    { it.quote.rating ?: 0 },
                    { -it.quote.number }
                )
            )
        }
        // True when the canonical copy actually comes from Bash.org.
        val canonicalIsBash = target.exact &&
            target.canonical.dbId.contains("bash", ignoreCase = true)
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { mergeTarget = null },
            title = { Text("Merge duplicates?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "These ${target.members.size} copies of the same quote " +
                            "live in:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    target.members.forEach { m ->
                        Text(
                            "• ${manager.shortLabel(m.dbId)} " +
                                "#${m.quote.number}" +
                                (if (m == keep) "  — keep" else ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (m == keep)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        if (canonicalIsBash) {
                            "Merging keeps the canonical Bash.org copy and " +
                                "hides the others (restorable under Content " +
                                "Filter)."
                        } else if (target.exact) {
                            "Merging keeps the canonical copy and hides " +
                                "the others (restorable under Content Filter)."
                        } else {
                            "Merging keeps the best-rated copy and hides the " +
                                "others (restorable under Content Filter)."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        target.members
                            .filterNot { it == keep }
                            .forEach { m ->
                                manager.setQuoteHidden(m.dbId, m.quote.number, true)
                            }
                        groups = groups.filterNot { it == target }
                        mergeTarget = null
                        Toast.makeText(
                            context,
                            if (target.exact) "Merged — kept the canonical copy"
                            else "Merged — kept the best copy",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) { Text("Merge") }
            },
            dismissButton = {
                TextButton(onClick = { mergeTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
