package com.quotebrowser.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.CATEGORY_ORDER_CUSTOM
import com.quotebrowser.app.data.Category
import com.quotebrowser.app.data.SourcedQuote
import com.quotebrowser.app.ui.CategoryViewModel
import com.quotebrowser.app.ui.components.ColorPickerDialog
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.components.colorToHex
import com.quotebrowser.app.ui.hexToColorOrNull
import kotlinx.coroutines.launch
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/**
 * Manage quote categories: add, rename, delete, and arrange.
 *
 * - "Add search results" opens the bulk-categorize screen: search across
 *   databases, tick results, and add them to categories in one go.
 * - "Reorder" enters drag-and-drop mode (long-press a row, then drag);
 *   dropping saves the custom order, used everywhere categories are
 *   listed. "Sort A–Z" goes back to alphabetical.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesScreen(
    catVm: CategoryViewModel,
    onBulkCategorize: () -> Unit,
    onShareCategory: (Category) -> Unit = {},
    onReadAllAloud: (List<SourcedQuote>) -> Unit = {}
) {
    val categories by catVm.categories.collectAsStateWithLifecycle()
    val orderMode by catVm.orderMode.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }
    var reorderMode by remember { mutableStateOf(false) }

    // Working copy while reordering; committed on drop.
    var working by remember { mutableStateOf<List<Category>>(emptyList()) }
    LaunchedEffect(reorderMode) {
        if (reorderMode) working = categories
    }
    val displayed = if (reorderMode) working else categories

    var draggingId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Categories")
        Text(
            text = "Tag quotes from any quote screen. Filters on Browse, Search, " +
                "Random and Top/Bottom can pull from one category at a time.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(onClick = onBulkCategorize) {
                Icon(Icons.Filled.PlaylistAdd, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add search results", maxLines = 1)
            }
            if (reorderMode) {
                FilledTonalButton(onClick = {
                    reorderMode = false
                    draggingId = null
                    dragOffsetY = 0f
                }) {
                    Text("Done")
                }
            } else {
                FilledTonalButton(onClick = { reorderMode = true }) {
                    Icon(Icons.Filled.DragHandle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Reorder")
                }
                if (orderMode == CATEGORY_ORDER_CUSTOM) {
                    FilledTonalButton(onClick = { catVm.resetToAlphabetical() }) {
                        Icon(Icons.Filled.Sort, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Sort A–Z")
                    }
                }
            }
        }
        // v1.47 auto-rules: one tap reconciles every database — newly
        // matching quotes are tagged, stale auto tags removed (manual tags
        // are never touched). Runs off the UI thread with progress.
        val sweep by catVm.autoRuleSweep.collectAsStateWithLifecycle()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { catVm.applyAllAutoRulesNow() },
                enabled = sweep !is CategoryViewModel.AutoRuleSweep.Running
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Apply auto-rules now", maxLines = 1)
            }
            when (val s = sweep) {
                is CategoryViewModel.AutoRuleSweep.Running -> {
                    Column(Modifier.weight(1f)) {
                        if (s.label.isNotEmpty()) {
                            Text(
                                text = s.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                        LinearProgressIndicator(
                            progress = {
                                s.done.toFloat() /
                                    s.total.coerceAtLeast(1).toFloat()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is CategoryViewModel.AutoRuleSweep.Done -> {
                    Text(
                        text = "Tagged ${s.added} new, removed ${s.removed} stale",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { catVm.dismissAutoRuleSweep() }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Dismiss"
                        )
                    }
                }
                CategoryViewModel.AutoRuleSweep.Idle -> {}
            }
        }
        if (reorderMode) {
            Text(
                text = "Long-press a category, then drag it up or down. " +
                    "Release to save the new order.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        } else {
            Spacer(Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New category name") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        catVm.addCategory(newName.trim())
                        newName = ""
                    },
                    enabled = newName.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Add")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        // v1.65: one category card, shared by the flat reorder list
        // and the collapsible section list below.
        @Composable
        fun CategoryRow(cat: Category) {
            val isDragging = draggingId == cat.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffsetY else 0f
                        shadowElevation = if (isDragging) 12.dp.toPx() else 0f
                    }
                    .pointerInput(cat.id, reorderMode) {
                        if (!reorderMode) return@pointerInput
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingId = cat.id
                                dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetY += dragAmount.y
                                val current = working
                                val fromIndex =
                                    current.indexOfFirst { it.id == draggingId }
                                if (fromIndex == -1) return@detectDragGesturesAfterLongPress
                                val layout = listState.layoutInfo
                                val dragged = layout.visibleItemsInfo
                                    .firstOrNull { it.key == draggingId }
                                    ?: return@detectDragGesturesAfterLongPress
                                val center =
                                    dragged.offset + dragOffsetY + dragged.size / 2
                                val target = layout.visibleItemsInfo
                                    .filter { it.key != draggingId }
                                    .firstOrNull { item ->
                                        center >= item.offset.toFloat() &&
                                            center <= (item.offset + item.size).toFloat()
                                    }
                                if (target != null) {
                                    val toIndex = current.indexOfFirst {
                                        it.id == target.key as Long
                                    }
                                    if (toIndex != -1 && toIndex != fromIndex) {
                                        working = current.toMutableList().apply {
                                            add(toIndex, removeAt(fromIndex))
                                        }
                                        // Keep the row under the finger: the
                                        // layout offset shifted by the
                                        // target's height.
                                        dragOffsetY -= target.size.toFloat() *
                                            if (toIndex > fromIndex) 1 else -1
                                    }
                                }
                                // Gentle auto-scroll near the viewport edges.
                                val visualTop = dragged.offset + dragOffsetY
                                val visualBottom = visualTop + dragged.size
                                scope.launch {
                                    when {
                                        visualTop < layout.viewportStartOffset + 120 ->
                                            listState.scrollBy(-48f)
                                        visualBottom > layout.viewportEndOffset - 120 ->
                                            listState.scrollBy(48f)
                                    }
                                }
                            },
                            onDragEnd = {
                                draggingId = null
                                dragOffsetY = 0f
                                catVm.setOrder(working.map { it.id })
                            },
                            onDragCancel = {
                                draggingId = null
                                dragOffsetY = 0f
                                working = categories
                            }
                        )
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDragging)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reorderMode) {
                        Icon(
                            Icons.Filled.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        cat.name,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    cat.colorHex?.let { hexToColorOrNull(it) }?.let { chipColor ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape
                    ) {
                        Text(
                            text = "${cat.quoteCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    if (!reorderMode) {
                        // Read-aloud playlist entry point: queues the
                        // whole category (filter/hidden/muted honored).
                        IconButton(onClick = {
                            scope.launch {
                                val quotes =
                                    catVm.readAloudQuotes(cat.id)
                                if (quotes.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "No readable quotes in ${cat.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    onReadAllAloud(quotes)
                                    Toast.makeText(
                                        context,
                                        "Reading ${quotes.size} quotes aloud",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                contentDescription =
                                    "Read all aloud in ${cat.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onShareCategory(cat) }) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Share ${cat.name} as collection",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { renameTarget = cat }) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Rename ${cat.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { deleteTarget = cat }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete ${cat.name}",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // v1.65: collapsible sections by category group (seed groups +
        // "My categories" for user-created ones), like the Settings
        // sections. Reorder mode keeps the flat draggable list.
        val sections: List<Pair<String, List<Category>>> = remember(displayed, reorderMode) {
            if (reorderMode) emptyList()
            else {
                val order = linkedMapOf<String, MutableList<Category>>()
                val ungrouped = mutableListOf<Category>()
                for (c in displayed) {
                    if (c.groupName.isNotEmpty()) order.getOrPut(c.groupName) { mutableListOf() }.add(c)
                    else ungrouped.add(c)
                }
                buildList {
                    order.forEach { (name, cats) -> add(name to cats.toList()) }
                    if (ungrouped.isNotEmpty()) add("My categories" to ungrouped.toList())
                }
            }
        }
        var collapsedSections by remember { mutableStateOf(setOf<String>()) }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            flingBehavior = rememberCappedFlingBehavior()) {
            if (reorderMode || sections.isEmpty()) {
                items(displayed, key = { it.id }) { cat -> CategoryRow(cat) }
            } else {
                sections.forEach { (name, cats) ->
                    item(key = "section-$name") {
                        CategorySectionHeader(
                            name = name,
                            count = cats.size,
                            collapsed = name in collapsedSections,
                            onToggle = {
                                collapsedSections = if (name in collapsedSections)
                                    collapsedSections - name else collapsedSections + name
                            }
                        )
                    }
                    if (name !in collapsedSections) {
                        items(cats, key = { it.id }) { cat -> CategoryRow(cat) }
                    }
                }
            }
        }
    }

    renameTarget?.let { target ->
        var name by remember(target) { mutableStateOf(target.name) }
        var colorHex by remember(target) { mutableStateOf(target.colorHex) }
        var showPicker by remember { mutableStateOf(false) }
        // Auto-rule phrases, editable for every category (including the
        // built-in "Offensive" one). Null = still loading.
        var phrases by remember(target.id) { mutableStateOf<List<String>?>(null) }
        var newPhrase by remember(target.id) { mutableStateOf("") }
        LaunchedEffect(target.id) {
            phrases = catVm.getAutoRulePhrases(target.id)
        }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Edit category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Chip color",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        val swatch = colorHex?.let { hexToColorOrNull(it) }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    swatch ?: MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        if (colorHex != null) {
                            TextButton(onClick = { colorHex = null }) {
                                Text("Off")
                            }
                        }
                        TextButton(onClick = { showPicker = true }) {
                            Text(if (colorHex == null) "Pick" else "Change")
                        }
                    }
                    Text(
                        text = "Off keeps the default chip style.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Auto-add quotes matching…",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Quotes containing these phrases are tagged " +
                            "automatically — at import and when auto-rules " +
                            "are applied. Matching is case-insensitive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    val loaded = phrases
                    if (loaded == null) {
                        Text(
                            text = "Loading phrases…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        if (loaded.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                FlowRow(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(8.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(8.dp)
                                ) {
                                    loaded.forEach { phrase ->
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme
                                                .secondaryContainer
                                        ) {
                                            Row(
                                                verticalAlignment =
                                                    Alignment.CenterVertically,
                                                modifier = Modifier.padding(
                                                    start = 12.dp,
                                                    end = 4.dp,
                                                    top = 2.dp,
                                                    bottom = 2.dp
                                                )
                                            ) {
                                                Text(
                                                    text = phrase,
                                                    style = MaterialTheme.typography
                                                        .bodyMedium
                                                )
                                                IconButton(
                                                    onClick = {
                                                        phrases =
                                                            loaded - phrase
                                                    },
                                                    modifier =
                                                        Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Close,
                                                        contentDescription =
                                                            "Remove phrase",
                                                        modifier = Modifier
                                                            .size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newPhrase,
                                onValueChange = { newPhrase = it },
                                label = { Text("New phrase…") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = {
                                    val clean = newPhrase.trim()
                                    if (clean.isNotEmpty() &&
                                        clean !in loaded
                                    ) {
                                        phrases = loaded + clean
                                    }
                                    newPhrase = ""
                                },
                                enabled = newPhrase.isNotBlank()
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        catVm.renameCategory(target.id, name.trim())
                        catVm.setCategoryColor(target.id, colorHex)
                        catVm.saveAutoRulePhrases(
                            target.id,
                            phrases.orEmpty()
                        )
                        renameTarget = null
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text("Cancel")
                }
            }
        )
        if (showPicker) {
            ColorPickerDialog(
                initial = colorHex?.let { hexToColorOrNull(it) } ?: Color.Gray,
                onDismiss = { showPicker = false },
                onPick = { color ->
                    colorHex = colorToHex(color)
                    showPicker = false
                }
            )
        }
    }

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete \"${target.name}\"?") },
            text = {
                Text(
                    "The category is removed from its ${target.quoteCount} quotes. " +
                        "The quotes themselves are kept."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    catVm.deleteCategory(target.id)
                    deleteTarget = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * v1.65 collapsible section header for the Categories screen, styled
 * like the Settings section headers: uppercase label, quote count, and
 * an expand/collapse chevron.
 */
@Composable
private fun CategorySectionHeader(
    name: String,
    count: Int,
    collapsed: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Icon(
            imageVector = if (collapsed) Icons.Filled.ExpandMore else Icons.Filled.ExpandLess,
            contentDescription = if (collapsed) "Expand $name" else "Collapse $name",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}
