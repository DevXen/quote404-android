package com.quotebrowser.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.Category
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.data.LengthMode
import com.quotebrowser.app.data.SortOrder
import com.quotebrowser.app.ui.MOOD_PRESETS
import com.quotebrowser.app.ui.MoodPreset
import com.quotebrowser.app.ui.SearchViewModel
import com.quotebrowser.app.ui.isMoodActive
import com.quotebrowser.app.ui.moodNameToId
import com.quotebrowser.app.ui.toggleMoodSelection

/**
 * v1.72: the full Search filter stack as a reusable panel. The Search
 * screen and the global search row (under the top bar on every other
 * page) share this, so filters are always the same set in both places.
 *
 * @param autoSubmit when true (the Search screen), toggling a mood
 *   applies immediately via [onSubmit]; when false (the global row),
 *   changes are staged in the shared [SearchViewModel] and take effect
 *   when the user opens the Search page.
 * @param onSubmit runs after an immediate-apply action (history
 *   recording on the Search screen).
 * @param onVoiceToggled extra work after the voice filter flips (the
 *   Search screen refreshes its paging source).
 */
/**
 * v1.72.2: mood chip built directly on combinedClickable. The old
 * FilterChip(onClick = {}) swallowed taps in its own internal clickable,
 * so the toggle modifier never fired and chips looked dead.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MoodChip(
    preset: MoodPreset,
    active: Boolean,
    onToggle: () -> Unit,
    onInfo: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (active) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceContainerHigh
            )
            .combinedClickable(onClick = onToggle, onLongClick = onInfo)
            .border(
                1.dp,
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (active) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Mood on",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "${preset.emoji} ${preset.label}",
                style = MaterialTheme.typography.labelLarge,
                color = if (active) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchFilterPanel(
    vm: SearchViewModel,
    categories: List<Category>,
    databases: List<DbInfo>,
    filtersExpanded: Boolean,
    onFiltersExpandedChange: (Boolean) -> Unit,
    toast: (String) -> Unit,
    autoSubmit: Boolean,
    onSubmit: () -> Unit,
    onVoiceToggled: () -> Unit = {},
    /**
     * v1.72: the global search row has its own expand control next to the
     * query field, so it hides this panel's Filters header and shows the
     * content directly.
     */
    showFilterHeader: Boolean = true,
    /**
     * v1.72.3: Search screen passes its save-search action so the Save
     * button can live in the Filters header instead of costing the Tip
     * row a full line. Null (global row) hides it.
     */
    onSaveSearch: (() -> Unit)? = null,
    /**
     * v1.72.3: optional content pinned to the top of the expanded filter
     * section. The Search screen uses it for the Person quick-jump so the
     * top of the page stays compact.
     */
    extraExpandedContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dbId by vm.dbId.collectAsStateWithLifecycle()
    val searchHasRatings by vm.searchHasRatings.collectAsStateWithLifecycle()
    var moodInfoPreset by remember { mutableStateOf<MoodPreset?>(null) }
    var sortExpanded by remember { mutableStateOf(false) }
    var showDbDialog by remember { mutableStateOf(false) }

    // v1.71: moods are additive toggle filters — tap to add a mood's
    // categories, tap again to remove them. Several moods can be on at
    // once, and they stack with the query, database scope, and every other
    // filter. Active state is derived from the live category selection, so
    // manual top-bar category edits just work.
    val nameToId = remember(categories) { moodNameToId(categories) }
    fun toggleMood(preset: MoodPreset) {
        if (categories.isEmpty()) {
            toast("Categories still loading — try again in a moment")
            return
        }
        val next = toggleMoodSelection(vm.categoryIds, preset, nameToId)
        if (next == vm.categoryIds) {
            toast("No matching categories in this database")
            return
        }
        vm.updateCategories(next)
        if (vm.query.isBlank()) vm.updateFilterBrowse(true)
        if (autoSubmit) onSubmit()
    }

    // v1.71: one-tap reset for the whole filter stack. Clears every
    // search filter (including mood/category picks and length) but keeps
    // the query.
    fun resetFilters() {
        vm.updateSortOrder(SortOrder.NUMBER_DESC)
        vm.updateMinRating(null)
        vm.updateMaturity(null)
        vm.updateCastSize(null)
        vm.updateExcludedCategories(emptySet())
        vm.updateExcludedMaturities(emptySet())
        vm.updateExcludedCasts(emptySet())
        vm.updateSearchDbIds(null)
        vm.updateLengthMode(LengthMode.ALL)
        vm.updateMinLength(null)
        vm.updateMaxLength(null)
        if (vm.notesScope) vm.updateNotesScope(false)
        if (vm.voiceOnly) {
            vm.updateVoiceOnly(false)
            onVoiceToggled()
        }
        // v1.75: "Has explanation" clears with the rest.
        if (vm.explanationOnly) {
            vm.updateExplanationOnly(false)
            onVoiceToggled()
        }
        if (vm.useRegex) vm.updateUseRegex(false)
        vm.updateCategories(emptySet())
        if (autoSubmit) onSubmit()
    }

    // Database scope: null = this database only; otherwise the chosen set.
    // v1.77: the effective scope — the virtual "all" database expands to
    // every real id, so the chip reads "All databases" (not "This
    // database") when "all" is selected with no explicit scope override.
    // (Local copy so smart casts work — a delegated property can't be
    // smart-cast.)
    val scopeIds: Set<String>? = vm.effectiveSearchDbIds.collectAsStateWithLifecycle().value
    val scopeLabel = when {
        scopeIds == null -> "This database"
        databases.isNotEmpty() && scopeIds.size >= databases.size -> "All databases"
        scopeIds.size == 1 ->
            databases.firstOrNull { it.id in scopeIds }?.displayTitle() ?: "1 database"
        else -> "${scopeIds.size} databases"
    }

    // v1.71: long-press a mood chip to see which categories it covers in
    // this database.
    moodInfoPreset?.let { preset ->
        val covered = preset.categories.filter { it in nameToId }
        AlertDialog(
            onDismissRequest = { moodInfoPreset = null },
            title = { Text("${preset.emoji} ${preset.label}") },
            text = {
                if (covered.isEmpty()) {
                    Text("No matching categories in this database.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Toggles these categories:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        covered.forEach { name ->
                            Text("• $name", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { moodInfoPreset = null }) {
                    Text("Close")
                }
            }
        )
    }
    if (showDbDialog) {
        DatabaseMultiSelectDialog(
            title = "Search databases",
            databases = databases,
            selected = when (val ids = vm.searchDbIds) {
                // "This database" shows with the current one checked.
                null -> setOf(dbId)
                else -> if (databases.isNotEmpty() && ids.size >= databases.size) null else ids
            },
            onConfirm = { result ->
                vm.updateSearchDbIds(
                    when {
                        // "All databases" checked: snapshot the full list.
                        result == null -> databases.map { it.id }.toSet()
                        // Nothing checked: back to "this database".
                        result.isEmpty() -> null
                        else -> result
                    }
                )
                showDbDialog = false
            },
            onDismiss = { showDbDialog = false }
        )
    }

    Column(modifier = modifier) {
        // v1.66 mood buttons: one-tap filter presets, no typing needed.
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(MOOD_PRESETS.size) { i ->
                val preset = MOOD_PRESETS[i]
                // v1.72.2: tap toggles the mood, long-press shows which
                // categories it covers — handled by MoodChip's
                // combinedClickable directly.
                MoodChip(
                    preset = preset,
                    active = isMoodActive(vm.categoryIds, preset, nameToId),
                    onToggle = { toggleMood(preset) },
                    onInfo = { moodInfoPreset = preset }
                )
            }
        }
        // v1.71: the filter stack collapses so quote results are visible
        // without scrolling. The collapsed header shows a one-line summary
        // of whatever is active, so no filter is ever secretly on.
        val filterSummary = buildList {
            if (vm.categoryIds.isNotEmpty()) add("${vm.categoryIds.size} categories")
            if (vm.sortOrder != SortOrder.NUMBER_DESC) add("Sort: ${vm.sortOrder.label}")
            if (vm.minRating != null) add("Min rating ${vm.minRating}")
            vm.maturity?.let { add("Maturity: ${it.label}") }
            vm.castSize?.let { add("Cast: ${it.label}") }
            if (vm.excludedCategoryIds.isNotEmpty()) add("${vm.excludedCategoryIds.size} cat. excl.")
            if (vm.excludedMaturities.isNotEmpty()) add("${vm.excludedMaturities.size} mat. excl.")
            if (vm.excludedCasts.isNotEmpty()) add("${vm.excludedCasts.size} cast excl.")
            if (scopeIds != null) add(scopeLabel)
            if (vm.notesScope) add("Notes")
            if (vm.voiceOnly) add("Has audio")
            if (vm.useRegex) add("Regex")
            if (vm.lengthMode != LengthMode.ALL) add("Length: ${vm.lengthMode.label}")
        }.joinToString(" · ")
        val filtersActive = filterSummary.isNotEmpty()
        if (showFilterHeader) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFiltersExpandedChange(!filtersExpanded) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Filters",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            if (!filtersExpanded && filterSummary.isNotEmpty()) {
                Text(
                    filterSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(2f)
                )
            }
            if (filtersActive) {
                TextButton(onClick = { resetFilters() }) {
                    Text("Reset")
                }
            }
            // v1.72.3: Save lives here now that the Tip row is gone.
            if (onSaveSearch != null) {
                TextButton(
                    onClick = onSaveSearch,
                    enabled = vm.query.isNotBlank()
                ) {
                    Text("Save")
                }
            }
            Icon(
                if (filtersExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (filtersExpanded) "Hide filters" else "Show filters"
            )
        }
        } // showFilterHeader
        // v1.72: in global mode the outer expand control owns visibility,
        // so this slim row only carries the active summary + Reset.
        if (!showFilterHeader && filtersActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    filterSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { resetFilters() }) {
                    Text("Reset")
                }
            }
        }
        if (filtersExpanded) {
        // v1.72.3: caller-supplied content (Search screen's Person
        // quick-jump) pinned above the filter rows.
        extraExpandedContent?.invoke()
        // v1.67: categories moved to the top bar next to the database
        // switcher — the in-screen picker is gone; Sort keeps its row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                "Sort:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = 4.dp)
            )
            Box {
                TextButton(onClick = { sortExpanded = true }) {
                    Text(vm.sortOrder.label)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Change sort order")
                }
                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    SortOrder.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort.label) },
                            onClick = { sortExpanded = false; vm.updateSortOrder(sort) }
                        )
                    }
                }
            }
        }
        if (searchHasRatings) {
            RatingFilterRow(
                selected = vm.minRating,
                onSelect = { vm.updateMinRating(it) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        } else {
            Text(
                "No ratings available for this database",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        // v1.65 content filters: the maturity pick ANDs with the global
        // ceiling from Settings; cast size is per-screen.
        // v1.74: FlowRow so the pills wrap on narrow screens; the
        // dropdown pills are self-labeled ("Maturity: …", "Cast size: …")
        // so the old separate Text labels are gone.
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MaturityFilterDropdown(
                selected = vm.maturity,
                onSelect = { vm.updateMaturity(it) }
            )
            CastSizeFilterDropdown(
                selected = vm.castSize,
                onSelect = { vm.updateCastSize(it) }
            )
        }
        // v1.65 search exclusions: checked items stay out of the results.
        var exclusionsExpanded by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { exclusionsExpanded = !exclusionsExpanded }) {
                Text("Exclude…")
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = if (exclusionsExpanded) "Hide exclusions" else "Show exclusions"
                )
            }
            val exclusionSummary = buildList {
                if (vm.excludedCategoryIds.isNotEmpty()) add("${vm.excludedCategoryIds.size} cat.")
                if (vm.excludedMaturities.isNotEmpty()) add("${vm.excludedMaturities.size} mat.")
                if (vm.excludedCasts.isNotEmpty()) add("${vm.excludedCasts.size} cast")
            }.joinToString(" · ")
            if (exclusionSummary.isNotEmpty()) {
                Text(
                    exclusionSummary,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        if (exclusionsExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Categories:",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    CategoryFilterDropdown(
                        categories = categories,
                        selectedIds = vm.excludedCategoryIds,
                        onSelect = { vm.updateExcludedCategories(it) },
                        emptyLabel = "Exclude none"
                    )
                }
                MaturityExcludeDropdown(
                    excluded = vm.excludedMaturities,
                    onToggle = { lvl ->
                        vm.updateExcludedMaturities(
                            if (lvl in vm.excludedMaturities) vm.excludedMaturities - lvl
                            else vm.excludedMaturities + lvl
                        )
                    }
                )
                CastSizeExcludeDropdown(
                    excluded = vm.excludedCasts,
                    onToggle = { cast ->
                        vm.updateExcludedCasts(
                            if (cast in vm.excludedCasts) vm.excludedCasts - cast
                            else vm.excludedCasts + cast
                        )
                    }
                )
            }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = scopeIds != null,
                    onClick = { showDbDialog = true },
                    label = { Text(scopeLabel) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            item {
                FilterChip(
                    selected = vm.notesScope,
                    onClick = { vm.updateNotesScope(!vm.notesScope) },
                    label = { Text("Notes") },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            // v1.61.20: "Has audio" — only quotes with a saved read-aloud
            // recording. Works with text search and sorting; with no text
            // it lists every voiced quote in scope. v1.74.7: renamed from
            // "Has voice" — Clint looks for "audio".
            item {
                FilterChip(
                    selected = vm.voiceOnly,
                    onClick = {
                        vm.updateVoiceOnly(!vm.voiceOnly)
                        onVoiceToggled()
                    },
                    label = { Text("Has audio") },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            // v1.75: "Has explanation" — only quotes with a saved Gemini
            // explanation. Mirrors "Has audio": works with text search and
            // sorting; with no text it lists every explained quote in scope.
            item {
                FilterChip(
                    selected = vm.explanationOnly,
                    onClick = {
                        vm.updateExplanationOnly(!vm.explanationOnly)
                        onVoiceToggled()
                    },
                    label = { Text("Has explanation") },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            // v1.74.5: regex search — the query is treated as a regular
            // expression matched against quote text (case-insensitive).
            item {
                FilterChip(
                    selected = vm.useRegex,
                    onClick = {
                        vm.updateUseRegex(!vm.useRegex)
                        if (autoSubmit) onSubmit()
                    },
                    label = { Text("Regex") },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
        // v1.72: length lives inside the collapsed filter stack (it was
        // outside by mistake in v1.71).
        LengthModeDropdown(
            mode = vm.lengthMode,
            onModeChange = { vm.updateLengthMode(it) },
            minLength = vm.minLength,
            maxLength = vm.maxLength,
            onMinChange = { vm.updateMinLength(it) },
            onMaxChange = { vm.updateMaxLength(it) },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
        } // filtersExpanded
    }
}
