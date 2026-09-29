package com.quotebrowser.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.quotebrowser.app.data.CastSize
import com.quotebrowser.app.data.MaturityLevel

/**
 * v1.65 single-select maturity pull-down. Null = "Use global setting"
 * (the Settings ceiling applies on its own); any other pick narrows
 * further — the global ceiling always ANDs on top, so this can never
 * show more than the ceiling allows.
 */
@Composable
fun MaturityFilterDropdown(
    selected: MaturityLevel?,
    onSelect: (MaturityLevel?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Maturity"
) {
    var expanded by remember { mutableStateOf(false) }
    val buttonLabel = when (selected) {
        null -> "Use global setting"
        else -> selected.label
    }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $buttonLabel", maxLines = 1)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose $label")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MaturityMenuItem(
                text = "Use global setting",
                checked = selected == null,
                onClick = { expanded = false; onSelect(null) }
            )
            HorizontalDivider()
            // EVERYTHING is a no-op next to the global ceiling, so it is
            // not offered as a per-screen pick.
            MaturityLevel.entries.filter { it != MaturityLevel.EVERYTHING }.forEach { level ->
                MaturityMenuItem(
                    text = level.label,
                    checked = selected == level,
                    onClick = { expanded = false; onSelect(level) }
                )
            }
        }
    }
}

@Composable
private fun MaturityMenuItem(
    text: String,
    checked: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (checked) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(text)
            }
        },
        onClick = onClick
    )
}

/**
 * v1.65 single-select cast-size pull-down. Null = all cast sizes.
 */
@Composable
fun CastSizeFilterDropdown(
    selected: CastSize?,
    onSelect: (CastSize?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Cast size"
) {
    var expanded by remember { mutableStateOf(false) }
    val buttonLabel = selected?.label ?: "All"
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $buttonLabel", maxLines = 1)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose $label")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selected == null) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text("All")
                    }
                },
                onClick = { expanded = false; onSelect(null) }
            )
            HorizontalDivider()
            CastSize.entries.forEach { cast ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selected == cast) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(cast.label)
                        }
                    },
                    onClick = { expanded = false; onSelect(cast) }
                )
            }
        }
    }
}

/**
 * v1.65 multi-select maturity exclusion pull-down for search. Checked
 * levels (0-4) are left out of the results entirely.
 */
@Composable
fun MaturityExcludeDropdown(
    excluded: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val buttonLabel = if (excluded.isEmpty()) "None" else "${excluded.size} excluded"
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Exclude maturity: $buttonLabel", maxLines = 1)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose maturity levels to exclude")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // The five ladder levels; checked levels are left out of results.
            for (lvl in 0..4) {
                val checked = lvl in excluded
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(levelLabelFor(lvl))
                        }
                    },
                    // Keep the menu open while toggling several levels.
                    onClick = { onToggle(lvl) }
                )
            }
        }
    }
}

private fun levelLabelFor(lvl: Int): String = when (lvl) {
    0 -> "Kid Safe (0)"
    1 -> "Clean (1)"
    2 -> "PG-13 (2)"
    3 -> "Rated R (3)"
    4 -> "NC-17 (4)"
    else -> "Level $lvl"
}

/**
 * v1.65 multi-select cast-size exclusion pull-down for search. Checked
 * buckets are left out of the results entirely.
 */
@Composable
fun CastSizeExcludeDropdown(
    excluded: Set<CastSize>,
    onToggle: (CastSize) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val buttonLabel = if (excluded.isEmpty()) "None" else "${excluded.size} excluded"
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Exclude cast: $buttonLabel", maxLines = 1)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Choose cast sizes to exclude")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CastSize.entries.forEach { cast ->
                val checked = cast in excluded
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(cast.label)
                        }
                    },
                    // Keep the menu open while toggling several buckets.
                    onClick = { onToggle(cast) }
                )
            }
        }
    }
}
