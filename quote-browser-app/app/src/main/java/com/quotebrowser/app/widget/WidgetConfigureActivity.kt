package com.quotebrowser.app.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.ui.categoryStoreFor
import com.quotebrowser.app.ui.theme.QuoteBrowserTheme
import kotlinx.coroutines.flow.first

/**
 * Shown when the user places a new home-screen widget: picks the database
 * and category for THAT widget instance. The choice is persisted per
 * appWidgetId; widgets placed before this feature keep using the global
 * widget settings from Settings → Widget.
 */
class WidgetConfigureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        // Canceled until the user explicitly saves.
        setResult(Activity.RESULT_CANCELED)

        val manager = DatabaseManager.getInstance(applicationContext)

        setContent {
            val themeId by manager.themeId.collectAsStateWithLifecycle()
            val customThemes by manager.customThemes.collectAsStateWithLifecycle()
            QuoteBrowserTheme(themeId = themeId, customThemes = customThemes) {
                WidgetConfigureScreen(
                    manager = manager,
                    onSave = { dbIds, category ->
                        manager.setWidgetConfig(appWidgetId, dbIds, category)
                        // Ask the provider to render this widget right away.
                        val update = Intent(
                            this@WidgetConfigureActivity,
                            QuoteWidgetProvider::class.java
                        ).apply {
                            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            putExtra(
                                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                                intArrayOf(appWidgetId)
                            )
                        }
                        sendBroadcast(update)
                        val done = Intent().putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            appWidgetId
                        )
                        setResult(Activity.RESULT_OK, done)
                        finish()
                    },
                    onCancel = { finish() }
                )
            }
        }
    }
}

@Composable
private fun WidgetConfigureScreen(
    manager: DatabaseManager,
    onSave: (dbIds: Set<String>?, category: String?) -> Unit,
    onCancel: () -> Unit
) {
    var databases by remember { mutableStateOf<List<DbInfo>>(emptyList()) }
    // Null = all databases.
    var dbIds by remember { mutableStateOf<Set<String>?>(null) }
    var categoryNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var category by remember { mutableStateOf<String?>(null) }
    var showDbDialog by remember { mutableStateOf(false) }
    var catExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        databases = manager.availableDatabases().first()
    }
    LaunchedEffect(dbIds) {
        val ids = dbIds
        category = null
        categoryNames = if (ids != null && ids.size == 1) {
            // Categories are per-database; with several (or all) selected
            // the widget stays unfiltered by category.
            try {
                manager.categoryStoreFor(ids.first()).list().map { it.name }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    val dbLabel = when (val ids = dbIds) {
        null -> "All databases"
        else -> {
            val labels = databases.filter { it.id in ids }.map { it.displayTitle() }
            when {
                labels.isEmpty() -> "Choose databases"
                labels.size == 1 -> labels.first()
                else -> "${labels.size} databases"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Configure widget",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            "This widget will show random quotes from the databases and category you pick. " +
                "Pick one, several, or all databases.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text("Databases", style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { showDbDialog = true }) {
                Text(dbLabel, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        }
        if (showDbDialog) {
            com.quotebrowser.app.ui.components.DatabaseMultiSelectDialog(
                title = "Widget databases",
                databases = databases,
                selected = dbIds,
                onConfirm = { dbIds = it; showDbDialog = false },
                onDismiss = { showDbDialog = false }
            )
        }

        Text("Category", style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { catExpanded = true }) {
                Text(
                    category ?: "All categories",
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = catExpanded,
                onDismissRequest = { catExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All categories") },
                    onClick = { catExpanded = false; category = null }
                )
                categoryNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { catExpanded = false; category = name }
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { onSave(dbIds, category) }) { Text("Add widget") }
        }
        Spacer(Modifier.height(8.dp))
    }
}
