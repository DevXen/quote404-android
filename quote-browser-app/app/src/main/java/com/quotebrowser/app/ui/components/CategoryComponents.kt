package com.quotebrowser.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.Category
import com.quotebrowser.app.ui.hexToColorOrNull
import com.quotebrowser.app.ui.theme.contentOn
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Category filter used on the quote list screens. Empty = all quotes.
 * Multiple categories can be checked; a quote matches if it is in ANY
 * of the selected categories.
 */
@Composable
fun CategoryFilterDropdown(
    categories: List<Category>,
    selectedIds: Set<Long>,
    onSelect: (Set<Long>) -> Unit,
    modifier: Modifier = Modifier,
    // v1.61.14: optional custom trigger (e.g. an icon button on the
    // Random screen). It receives an onOpen callback; when null, the
    // default outlined "All quotes ▾" button is used.
    trigger: (@Composable (onOpen: () -> Unit) -> Unit)? = null,
    // v1.65: label shown when nothing is selected (e.g. "Exclude none"
    // in the Search exclusions section).
    emptyLabel: String = "All quotes"
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedNames = categories.filter { it.id in selectedIds }.map { it.name }
    val label = when {
        selectedIds.isEmpty() -> emptyLabel
        selectedNames.size == 1 -> {
            val cat = categories.firstOrNull { it.id in selectedIds }
            if (cat != null) "${cat.name} (${cat.quoteCount})" else "1 category"
        }
        else -> "${selectedNames.size} categories"
    }
    // v1.61.12: the caller's modifier goes on the button itself (not
    // the wrapping Box) so a fixed height lines it up with sibling
    // controls. No caller passes a modifier today, so this is neutral
    // everywhere else.
    Box {
        if (trigger != null) {
            trigger({ expanded = true })
        } else {
            OutlinedButton(onClick = { expanded = true }, modifier = modifier) {
                Text(label, maxLines = 1)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose categories")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(emptyLabel) },
                onClick = { expanded = false; onSelect(emptySet()) }
            )
            // v1.65: grouped, collapsible sections (seed groups in
            // first-appearance order, then "My categories" for user-created
            // ones), so the menu is a set of collapsible sections instead
            // of one giant scrolling list.
            val sections = remember(categories) {
                val order = linkedMapOf<String, MutableList<Category>>()
                val ungrouped = mutableListOf<Category>()
                for (cat in categories) {
                    if (cat.groupName.isNotEmpty()) {
                        order.getOrPut(cat.groupName) { mutableListOf() }.add(cat)
                    } else {
                        ungrouped.add(cat)
                    }
                }
                buildList {
                    order.forEach { (name, cats) -> add(name to cats.toList()) }
                    if (ungrouped.isNotEmpty()) add("My categories" to ungrouped.toList())
                }
            }
            // v1.65: per-section collapse state; the header tap toggles the
            // section open/closed, and the checkbox selects/deselects the
            // whole group without closing the menu.
            var collapsed by remember { mutableStateOf(setOf<String>()) }
            sections.forEach { (groupName, cats) ->
                val isCollapsed = groupName in collapsed
                val groupIds = cats.map { it.id }.toSet()
                val allInGroup = groupIds.all { it in selectedIds }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isCollapsed) Icons.Filled.ArrowDropDown
                                else Icons.Filled.KeyboardArrowUp,
                                contentDescription = if (isCollapsed) "Expand $groupName" else "Collapse $groupName"
                            )
                            Text(
                                groupName.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = allInGroup,
                                onCheckedChange = {
                                    onSelect(
                                        if (allInGroup) selectedIds - groupIds
                                        else selectedIds + groupIds
                                    )
                                }
                            )
                        }
                    },
                    onClick = {
                        collapsed = if (isCollapsed) collapsed - groupName
                        else collapsed + groupName
                    }
                )
                if (!isCollapsed) {
                    cats.forEach { cat ->
                        val checked = cat.id in selectedIds
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = null
                                    )
                                    Text("${cat.name} (${cat.quoteCount})")
                                }
                            },
                            // Keep the menu open while toggling so several
                            // categories can be checked in one go.
                            onClick = {
                                onSelect(
                                    if (checked) selectedIds - cat.id
                                    else selectedIds + cat.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Read-only chips shown at the bottom of a quote, plus an edit button. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    categories: List<Category>,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (categories.isEmpty()) {
                Text(
                    text = "No categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categories.forEach { cat ->
                    val chipColor = cat.colorHex?.let { hexToColorOrNull(it) }
                    FilterChip(
                        selected = false,
                        onClick = onEditClick,
                        label = { Text(cat.name) },
                        colors = if (chipColor != null) {
                            FilterChipDefaults.filterChipColors(
                                containerColor = chipColor,
                                labelColor = contentOn(chipColor)
                            )
                        } else {
                            FilterChipDefaults.filterChipColors()
                        }
                    )
                }
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onEditClick) {
            Icon(
                Icons.Filled.Edit,
                contentDescription = "Edit categories",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Checklist dialog to assign categories to one quote. Also allows creating
 * a brand-new category inline via [onCreateCategory].
 */
@Composable
fun CategoryEditDialog(
    allCategories: List<Category>,
    selectedIds: Set<Long>,
    onCreateCategory: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Set<Long>) -> Unit
) {
    var checked by remember(allCategories) { mutableStateOf(selectedIds) }
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Categories") },
        text = {
            Column {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    flingBehavior = rememberCappedFlingBehavior()) {
                    items(allCategories, key = { it.id }) { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = cat.id in checked,
                                onCheckedChange = { on ->
                                    checked = if (on) checked + cat.id
                                    else checked - cat.id
                                }
                            )
                            Text(
                                text = "${cat.name} (${cat.quoteCount})",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("New category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCreateCategory(newName.trim())
                            newName = ""
                        },
                        enabled = newName.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(checked)
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
