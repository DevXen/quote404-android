package com.quotebrowser.app.ui.screens

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.VerifyReport
import com.quotebrowser.app.data.VerifyStatus
import com.quotebrowser.app.data.verifyDatabases
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ScreenHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * v1.59: database verification / repair screen. A Run button executes
 * [verifyDatabases] (via LaunchedEffect) with a progress indicator, then
 * shows the per-database report. Repairs are conservative — out-of-sync
 * FTS indexes are rebuilt and blank-text rows deleted; anything
 * destructive beyond that is flagged NEEDS_ATTENTION, never auto-fixed.
 *
 * Route string: "verify_databases" ([VERIFY_DATABASES_ROUTE]). The
 * Settings entry point is wired by the settings agent.
 */
const val VERIFY_DATABASES_ROUTE = "verify_databases"

@Composable
fun VerifyDatabasesScreen(
    manager: DatabaseManager,
    onBack: () -> Unit
) {
    var runId by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var doneCount by remember { mutableIntStateOf(0) }
    var totalCount by remember { mutableIntStateOf(0) }
    var report by remember { mutableStateOf<VerifyReport?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(runId) {
        if (runId == 0) return@LaunchedEffect
        running = true
        report = null
        error = null
        doneCount = 0
        totalCount = 0
        try {
            report = withContext(Dispatchers.IO) {
                verifyDatabases(manager) { done, total ->
                    doneCount = done
                    totalCount = total
                }
            }
        } catch (e: Exception) {
            error = "Verification failed: ${e.message}"
        } finally {
            running = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Verify databases") {
            TextButton(onClick = onBack) { Text("Back") }
        }
        when {
            running -> {
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
                        if (totalCount > 0) "Checking database $doneCount of $totalCount…"
                        else "Starting…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            error != null -> {
                EmptyState(error ?: "Verification failed.")
                Button(
                    onClick = { runId++ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("Try again") }
            }
            report == null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Checks every quote database for missing tables, an " +
                            "out-of-sync search index, blank or mis-numbered " +
                            "quotes, and empty bundled databases — and " +
                            "applies safe repairs automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Repairs are conservative: out-of-sync search indexes " +
                            "are rebuilt and blank quotes deleted. Anything " +
                            "riskier is flagged for your review, never " +
                            "auto-fixed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { runId++ }) { Text("Run verification") }
                }
            }
            else -> {
                val r = report!!
                val summary = when {
                    r.attentionCount > 0 ->
                        "${r.results.size} databases checked — " +
                            "${r.fixedCount} repair(s) applied, " +
                            "${r.attentionCount} item(s) need your attention."
                    r.fixedCount > 0 ->
                        "${r.results.size} databases checked — " +
                            "${r.fixedCount} repair(s) applied, all clean."
                    else -> "${r.results.size} databases checked — all clean."
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { runId++ },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Run again") }
                        }
                    }
                    items(
                        r.results,
                        key = { it.dbId }
                    ) { db ->
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
                                Text(
                                    db.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                db.checks.forEach { c ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StatusPill(c.status)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                c.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                c.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
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

@Composable
private fun StatusPill(status: VerifyStatus) {
    val (container, content, label) = when (status) {
        VerifyStatus.OK -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "OK"
        )
        VerifyStatus.FIXED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "FIXED"
        )
        VerifyStatus.NEEDS_ATTENTION -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "REVIEW"
        )
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = container,
        contentColor = content
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
