package com.quotebrowser.app.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.BulkActions
import kotlinx.coroutines.launch

/** One selected quote: its database plus its quote number. */
data class SelectedQuote(val dbId: String, val number: Int)

/**
 * Multi-select state for bulk actions. Long-press enters selection mode;
 * taps toggle; emptying the selection exits it.
 */
class QuoteSelection {
    var active by mutableStateOf(false)
        private set
    var selected by mutableStateOf(setOf<SelectedQuote>())
        private set

    val count: Int get() = selected.size

    fun enter(sq: SelectedQuote) {
        active = true
        selected = setOf(sq)
    }

    fun toggle(sq: SelectedQuote) {
        selected = if (sq in selected) selected - sq else selected + sq
        if (selected.isEmpty()) active = false
    }

    fun selectAll(sqs: List<SelectedQuote>) {
        selected = sqs.toSet()
        if (selected.isEmpty()) active = false
    }

    fun isSelected(sq: SelectedQuote): Boolean = sq in selected

    fun exit() {
        active = false
        selected = emptySet()
    }

    /** Deselects everything but stays in selection mode. */
    fun clear() {
        selected = emptySet()
    }
}

@Composable
fun rememberQuoteSelection(): QuoteSelection = remember { QuoteSelection() }

/**
 * Contextual bar shown while multi-select is active: selected count plus
 * bulk actions (favorites, categories, export queue, read-aloud, share).
 */
@Composable
fun SelectionBar(
    selection: QuoteSelection,
    allCandidates: List<SelectedQuote>,
    onFavorite: () -> Unit,
    onCategory: () -> Unit,
    onQueue: () -> Unit,
    onPlaylist: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * v1.49: when false, the Select all button is hidden (Settings toggle
     * "Select all in bulk mode"). Deselect-all still works via Clear.
     */
    showSelectAll: Boolean = true
) {
    if (!selection.active) return
    // v1.49: the Select all control becomes Deselect all when everything
    // on screen is already selected; tapping it then clears the selection.
    val allSelected = showSelectAll && allCandidates.isNotEmpty() &&
        selection.count >= allCandidates.size &&
        allCandidates.all { selection.isSelected(it) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${selection.count} selected",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (showSelectAll) {
                    TextButton(
                        onClick = {
                            if (allSelected) selection.clear()
                            else selection.selectAll(allCandidates)
                        }
                    ) {
                        Text(if (allSelected) "Deselect all" else "Select all")
                    }
                }
                TextButton(onClick = { selection.clear() }) {
                    Text("Clear")
                }
                IconButton(onClick = { selection.exit() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Done selecting")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onFavorite) { Text("Favorite") }
                TextButton(onClick = onCategory) { Text("Category") }
                TextButton(onClick = onQueue) { Text("Queue") }
                TextButton(onClick = onPlaylist) { Text("Read aloud") }
                TextButton(onClick = onShare) { Text("Share") }
            }
        }
    }
}

/**
 * "N quotes hidden by content filter · Show" banner. Tapping Show
 * temporarily unhides them for the current screen only.
 */
@Composable
fun HiddenQuotesBanner(
    hiddenCount: Int,
    showingHidden: Boolean,
    onShow: () -> Unit,
    onHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hiddenCount <= 0 && !showingHidden) return
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (showingHidden) "Showing hidden quotes"
                else "$hiddenCount quote${if (hiddenCount == 1) "" else "s"} hidden by content filter",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { if (showingHidden) onHide() else onShow() }) {
                Text(if (showingHidden) "Hide" else "Show")
            }
        }
    }
}

/**
 * Category picker for bulk "add to category": checkboxes over the category
 * names of one database; the caller resolves names per database.
 */@Composable
fun BulkCategoryDialog(
    categoryNames: List<String>,
    onDismiss: () -> Unit,
    onAdd: (existingNames: Set<String>, newName: String, autoRule: String?) -> Unit,
    /** Search phrase offered as an auto-rule when creating a new category. */
    autoRulePhrase: String? = null
) {
    var checked by remember { mutableStateOf(setOf<String>()) }
    var newName by remember { mutableStateOf("") }
    var addRule by remember { mutableStateOf(true) }
    val showRuleRow = !autoRulePhrase.isNullOrBlank() && newName.isNotBlank()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to categories") },
        text = {
            Column {
                if (categoryNames.isEmpty()) {
                    Text("No categories yet — type a name below to create one.")
                } else {
                    categoryNames.forEach { name ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = name in checked,
                                onCheckedChange = { on ->
                                    checked = if (on) checked + name else checked - name
                                }
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Spacer(Modifier.padding(top = 8.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New category…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showRuleRow) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = addRule,
                            onCheckedChange = { addRule = it }
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Also auto-add future quotes matching " +
                                "'$autoRulePhrase'",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val rule = if (showRuleRow && addRule) autoRulePhrase else null
                    onAdd(checked, newName.trim(), rule)
                    onDismiss()
                },
                enabled = checked.isNotEmpty() || newName.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Hosts the multi-select [SelectionBar] plus the bulk-action plumbing
 * (favorites, categories, export queue, read-aloud playlist, share) and
 * the category picker dialog. Screens render this above their quote list.
 */
@Composable
fun QuoteSelectionHost(
    selection: QuoteSelection,
    manager: DatabaseManager,
    allCandidates: List<SelectedQuote>,
    onAddToPlaylist: (List<SourcedQuote>) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Search phrase offered as an auto-rule when the user creates a new
     * category from the bulk dialog (Search screen passes its query).
     */
    autoRulePhrase: String? = null,
    /** v1.49: hides the Select all button when the toggle is off. */
    showSelectAll: Boolean = true
) {
    if (!selection.active) return
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bulk = remember(manager) { BulkActions(manager, scope) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var categoryNames by remember { mutableStateOf(listOf<String>()) }

    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    SelectionBar(
        selection = selection,
        allCandidates = allCandidates,
        onFavorite = {
            val sel = selection.selected
            bulk.addToFavorites(sel) { toast(it) }
            selection.exit()
        },
        onCategory = {
            val first = selection.selected.firstOrNull() ?: return@SelectionBar
            scope.launch {
                categoryNames = bulk.categoryNames(first.dbId)
                showCategoryPicker = true
            }
        },
        onQueue = {
            val sel = selection.selected
            bulk.addToQueue(sel) { toast(it) }
            selection.exit()
        },
        onPlaylist = {
            val sel = selection.selected
            scope.launch {
                val quotes = bulk.loadQuotes(sel)
                if (quotes.isEmpty()) {
                    toast("Couldn't load quotes")
                } else {
                    onAddToPlaylist(quotes)
                    toast("${quotes.size} added to the read-aloud queue")
                }
                selection.exit()
            }
        },
        onShare = {
            bulk.shareText(context, selection.selected)
            selection.exit()
        },
        modifier = modifier,
        showSelectAll = showSelectAll
    )
    if (showCategoryPicker) {
        BulkCategoryDialog(
            categoryNames = categoryNames,
            onDismiss = { showCategoryPicker = false },
            autoRulePhrase = autoRulePhrase,
            onAdd = { names, newName, autoRule ->
                val sel = selection.selected
                bulk.addToCategories(sel, names, newName, autoRule) { toast(it) }
                selection.exit()
            }
        )
    }
}
