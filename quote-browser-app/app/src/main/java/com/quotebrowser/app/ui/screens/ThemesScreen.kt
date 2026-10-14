package com.quotebrowser.app.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.ui.DefaultNickNameColors
import com.quotebrowser.app.ui.MAX_NICK_NAME_COLORS
import com.quotebrowser.app.ui.MAX_NICK_RULES
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.NickColorRule
import com.quotebrowser.app.ui.NickColorTheme
import com.quotebrowser.app.ui.NickColorThemePresets
import com.quotebrowser.app.ui.NickThemeDots
import com.quotebrowser.app.ui.MAX_NICK_THEME_COLORS
import com.quotebrowser.app.ui.NICK_THEME_APP
import com.quotebrowser.app.ui.NICK_COLOR_NONE
import com.quotebrowser.app.ui.LocalNickPalette
import com.quotebrowser.app.ui.hexToColorOrNull
import com.quotebrowser.app.ui.resolveRuleColor
import com.quotebrowser.app.ui.components.ColorPickerDialog
import com.quotebrowser.app.ui.components.colorToHex
import com.quotebrowser.app.ui.theme.CustomTheme
import com.quotebrowser.app.ui.theme.DevXenBgMode
import com.quotebrowser.app.ui.theme.DEFAULT_NICK_ACTION_HEX
import com.quotebrowser.app.ui.theme.DEFAULT_NICK_BRACKETS_HEX
import com.quotebrowser.app.ui.theme.DEFAULT_RATING_HEX
import com.quotebrowser.app.ui.theme.defaultBracketHexes
import com.quotebrowser.app.ui.theme.defaultBracketTint
import com.quotebrowser.app.ui.theme.effectivePreset
import com.quotebrowser.app.ui.theme.parseCustomThemeSlots
import com.quotebrowser.app.ui.theme.ThemePreset
import com.quotebrowser.app.ui.theme.ThemePresets
import com.quotebrowser.app.ui.theme.contrastHex
import com.quotebrowser.app.ui.theme.customThemeId
import com.quotebrowser.app.ui.theme.defaultDividerHex
import com.quotebrowser.app.ui.theme.themeGroups
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.quotebrowser.app.ui.rememberCappedFlingBehavior


@Composable
private fun ThemePresetRow(
    name: String,
    selected: Boolean,
    previewColors: List<Color>?,
    onCustomize: (() -> Unit)?,
    onExport: (() -> Unit)?,
    /** v1.51: shown as "Reset" when this preset has a saved override. */
    onReset: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (previewColors != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(-8.dp)) {
                    previewColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
            }
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            if (onReset != null) {
                TextButton(onClick = onReset) {
                    Text("Reset")
                }
            }
            if (onCustomize != null) {
                IconButton(onClick = onCustomize) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onExport != null) {
                IconButton(onClick = onExport) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Export theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * v1.88.23: collapsible labeled section header for a theme group. The
 * dot marks the group holding the currently selected theme.
 */
@Composable
private fun ThemeGroupHeader(
    label: String,
    count: Int,
    expanded: Boolean,
    hasSelection: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (hasSelection) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * v1.88.23: one built-in preset row with its customize/export/reset
 * wiring, shared by every picker group.
 */
@Composable
private fun BuiltInThemePresetRow(
    preset: ThemePreset,
    themeId: String,
    builtInOverrides: Map<String, String>,
    vm: MainViewModel,
    onOpenEditor: (ThemeEditorState) -> Unit,
    onExportTheme: (String, String) -> Unit
) {
    // System and Dynamic have no fixed colors to preview and can't
    // be customized (Dynamic follows the wallpaper).
    val fixed = preset.id != "system" && preset.id != "dynamic"
    // v1.51: previews (and the editor's starting values) reflect a
    // saved per-preset override when one exists.
    val effective = effectivePreset(preset.id, builtInOverrides) ?: preset
    ThemePresetRow(
        name = preset.name,
        selected = themeId == preset.id,
        previewColors = if (fixed)
            listOf(
                Color(effective.background),
                Color(effective.surface),
                Color(effective.primary),
                Color(effective.number),
                Color(effective.rating),
                Color(effective.ratingText),
                Color(effective.nickBrackets)
            )
        else null,
        // v1.51: every real preset (including the database site
        // themes) is directly editable; saving stores a per-preset
        // override, and "Reset to default" deletes it to restore
        // the hardcoded spec. The system theme follows the OS and
        // Dynamic follows the wallpaper.
        onCustomize = if (fixed) ({
            onOpenEditor(
                ThemeEditorState(
                    title = "Edit ${preset.name}",
                    initialName = preset.name,
                    initialSlots = listOf(
                        effective.background,
                        effective.surface,
                        effective.primary,
                        effective.onBackground,
                        effective.onSurface,
                        effective.number,
                        effective.rating,
                        effective.ratingText,
                        effective.nickBrackets,
                        effective.nickAction,
                        effective.divider,
                        // v1.55: "Quote background" slot.
                        effective.quoteBackground
                    ).mapIndexed { i, v ->
                        // Slots 8/9/10 accept "none" (= uncolored/invisible).
                        if ((i == 8 || i == 9 || i == 10) && v == 0L) "none" else v.toRgbHex()
                    },
                    initialNameColors = effective.nickNames.map { it.toRgbHex() },
                    initialBracketColors = effective.nickBracketPalette.map { it.toRgbHex() },
                    initialActionOverridesName = effective.actionOverridesName,
                    initialColorMessageWithSpeaker = effective.colorMessageWithSpeaker,
                    saveLabel = "Save changes",
                    onSave = { _, colors ->
                        vm.setBuiltInThemeOverride(preset.id, colors)
                    }
                )
            )
        }) else null,
        // Every real preset serializes to the custom-theme CSV, so it
        // can be exported like a user theme.
        onExport = if (fixed) ({ onExportTheme(preset.name, preset.toExportCsv()) }) else null,
        onReset = if (fixed && builtInOverrides.containsKey(preset.id)) {
            { vm.clearBuiltInThemeOverride(preset.id) }
        } else null,
        onClick = { vm.setThemeId(preset.id) }
    )
}

@Composable
private fun CustomThemeRow(
    theme: CustomTheme,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val swatches = remember(theme.colors) {
        val all = theme.colors.split(",").map { hex ->
            try {
                Color(("FF${hex.trim().removePrefix("#")}").toLong(16))
            } catch (e: Exception) {
                Color.Gray
            }
        }
        // Same semantic slots as the preset previews:
        // background, surface, accent, quote-number, rating, rating text,
        // nick <> color.
        listOf(0, 1, 2, 5, 6, 7, 8).mapNotNull { all.getOrNull(it) }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(-8.dp)) {
                swatches.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = theme.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge
            )
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onExport) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "Export theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete theme",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val customSlotLabels = listOf(
    "Background",
    "Card surface",
    "Accent",
    "Text on background",
    "Text on cards",
    "Quote number background",
    "Rating background",
    "Rating text",
    "Nick <> color",
    "Action color",
    "Divider",
    // v1.55: the surface behind the quote body text on quote cards.
    "Quote background"
)

/** Fixed editor slots: the 12 entries above. The name palette is separate. */
private const val FIXED_SLOT_COUNT = 12

/**
 * State for the theme editor dialog. Used for three flows:
 * - creating a new custom theme,
 * - customizing a built-in preset (saved as a new custom theme),
 * - editing an existing custom theme in place.
 * (Public since v1.89.5: the per-database theme rows in Settings open
 * the same editor for a database's own themes.)
 */
data class ThemeEditorState(
    val title: String,
    val initialName: String,
    val initialSlots: List<String>,
    /** Default name-color palette entries ("RRGGBB"). */
    val initialNameColors: List<String>,
    /**
     * Default bracket-color palette entries ("RRGGBB"), paired 1:1 with
     * [initialNameColors]. Empty = fall back to the single <> color.
     */
    val initialBracketColors: List<String>,
    /** Whether the action-line color covers the nick in action lines. */
    val initialActionOverridesName: Boolean,
    /** Whether a speaker's message text takes their name/bracket color. */
    val initialColorMessageWithSpeaker: Boolean,
    val saveLabel: String,
    val onSave: (name: String, colors: String) -> Unit
)

/** Theme editor (name + fixed color slots + name palette) shown as a dialog. */
@Composable
fun ThemeEditorDialog(
    state: ThemeEditorState,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(state.initialName) }
    // Missing slots fall back to black, except the rating slot which
    // defaults to light blue (matching the built-in presets), the rating
    // text slot which defaults to auto-contrast on the rating background,
    // the nick <> slot which defaults to light blue, the action slot
    // which defaults to green, and the divider slot which defaults to a
    // subtle neutral. Slots 8/9/10 accept "none" (= uncolored/invisible).
    val slotDefaults = listOf(
        "000000", "000000", "000000", "000000", "000000", "000000",
        DEFAULT_RATING_HEX, "auto", DEFAULT_NICK_BRACKETS_HEX, DEFAULT_NICK_ACTION_HEX,
        "auto",
        // v1.55: quote background defaults to black.
        "000000"
    )
    var slots by remember {
        mutableStateOf(
            // Pad by index so a missing 7th (rating) slot gets the light-blue
            // default rather than the first generic fallback.
            List(FIXED_SLOT_COUNT) { i -> state.initialSlots.getOrNull(i) ?: slotDefaults[i] }
                .map { it.trim().removePrefix("#") }
                .toMutableList()
                .also { list ->
                    list[7] = when {
                        list[7].equals("auto", ignoreCase = true) || list[7].isBlank() ->
                            contrastHex(list[6].ifBlank { DEFAULT_RATING_HEX })
                        else -> list[7]
                    }
                    // Divider "auto"/blank follows the background's darkness.
                    if (list[10].equals("auto", ignoreCase = true) || list[10].isBlank()) {
                        list[10] = defaultDividerHex(list[0])
                    }
                    list.forEachIndexed { i, hex ->
                        if (i != 10 && hex.isBlank()) list[i] = slotDefaults[i].takeUnless { it == "auto" }
                            ?: contrastHex(list[6])
                    }
                }
        )
    }
    // Theme's default name-color palette (up to MAX_NICK_NAME_COLORS).
    var nameColors by remember {
        mutableStateOf(
            state.initialNameColors
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
                .take(MAX_NICK_NAME_COLORS)
                .toMutableList()
        )
    }
    var pickerSlot by remember { mutableStateOf<Int?>(null) }
    // Palette picker: index into nameColors, or -1 for a new entry.
    var pickerNameIndex by remember { mutableStateOf<Int?>(null) }
    // Bracket palette picker: index into bracketColors, or -1 for new.
    var pickerBracketIndex by remember { mutableStateOf<Int?>(null) }
    // Theme's default bracket-color palette (up to MAX_NICK_NAME_COLORS),
    // paired 1:1 with the name colors above.
    var bracketColors by remember {
        mutableStateOf(
            state.initialBracketColors
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
                .take(MAX_NICK_NAME_COLORS)
                .toMutableList()
        )
    }
    var actionOverridesName by remember {
        mutableStateOf(state.initialActionOverridesName)
    }
    var colorMessageWithSpeaker by remember {
        mutableStateOf(state.initialColorMessageWithSpeaker)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Theme name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                slots.forEachIndexed { index, hex ->
                    ColorSlotRow(
                        label = customSlotLabels[index],
                        hex = hex,
                        allowNone = index == 8 || index == 9 || index == 10,
                        onNoneClick = {
                            slots = slots.toMutableList().also { it[index] = "none" }
                        },
                        onPickClick = { pickerSlot = index }
                    )
                }
                Text(
                    text = "Default name colors (${nameColors.size}/$MAX_NICK_NAME_COLORS)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                Text(
                    text = "Names without a personal rule get one of these. " +
                            "Remove them all and names stay uncolored " +
                            "(the <> brackets keep their own color).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                nameColors.forEachIndexed { i, hex ->
                    PaletteColorRow(
                        hex = hex,
                        onPickClick = { pickerNameIndex = i },
                        onDeleteClick = {
                            nameColors = nameColors.toMutableList().also { it.removeAt(i) }
                        }
                    )
                }
                if (nameColors.size < MAX_NICK_NAME_COLORS) {
                    TextButton(onClick = { pickerNameIndex = -1 }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add color")
                    }
                }
                Text(
                    text = "Default bracket colors (${bracketColors.size}/$MAX_NICK_NAME_COLORS)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
                Text(
                    text = "The <> around each nick get the bracket color paired " +
                            "with that nick's name color above. Remove them all " +
                            "to use the single <> color instead.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                bracketColors.forEachIndexed { i, hex ->
                    PaletteColorRow(
                        hex = hex,
                        onPickClick = { pickerBracketIndex = i },
                        onDeleteClick = {
                            bracketColors = bracketColors.toMutableList().also { it.removeAt(i) }
                        }
                    )
                }
                if (bracketColors.size < MAX_NICK_NAME_COLORS) {
                    TextButton(onClick = { pickerBracketIndex = -1 }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Add color")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Action color overrides name color",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "When on, the whole action line \u2014 nick included \u2014 " +
                                    "uses the action color. When off, the nick keeps " +
                                    "its own color inside action lines.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = actionOverridesName,
                        onCheckedChange = { actionOverridesName = it }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Color message text with speaker color",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "When on, the words a person says take their name " +
                                    "color \u2014 or their bracket color when the name " +
                                    "has none.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = colorMessageWithSpeaker,
                        onCheckedChange = { colorMessageWithSpeaker = it }
                    )
                }
                Text(
                    text = "Tap Pick on any color to choose it visually \u2014 no codes to look up.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // CSV positions: 0-8 fixed slots, 9 = legacy single name
                // slot (always blank now), 10 = action, 11 = ";"-joined
                // default name palette ("-" = name coloring disabled),
                // 12 = action-overrides-name flag, 13 = ";"-joined default
                // bracket palette ("-" = use the single <> color),
                // 14 = divider color ("none" = invisible),
                // 15 = color-message-with-speaker flag (off by default),
                // 16 = quote background (v1.55; blank = theme default).
                val csv = (slots.take(9) + listOf("") + listOf(slots[9]) +
                        listOf(if (nameColors.isEmpty()) "-" else nameColors.joinToString(";")) +
                        listOf(if (actionOverridesName) "1" else "0") +
                        listOf(if (bracketColors.isEmpty()) "-" else bracketColors.joinToString(";")) +
                        listOf(slots[10]) +
                        listOf(if (colorMessageWithSpeaker) "1" else "0") +
                        listOf(slots[11]))
                    .joinToString(",")
                state.onSave(name, csv)
                onDismiss()
            }) {
                Text(state.saveLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    val slot = pickerSlot
    if (slot != null) {
        val initial = try {
            Color(("FF${slots[slot]}").toLong(16))
        } catch (e: Exception) {
            Color.Gray
        }
        ColorPickerDialog(
            initial = initial,
            onDismiss = { pickerSlot = null },
            onPick = { color ->
                slots = slots.toMutableList().also { it[slot] = colorToHex(color) }
                pickerSlot = null
            }
        )
    }

    val nameIndex = pickerNameIndex
    if (nameIndex != null) {
        val initialHex = if (nameIndex >= 0) nameColors.getOrNull(nameIndex) else null
        val initial = try {
            Color(("FF${initialHex ?: "90CAF9"}").toLong(16))
        } catch (e: Exception) {
            Color.Gray
        }
        ColorPickerDialog(
            initial = initial,
            onDismiss = { pickerNameIndex = null },
            onPick = { color ->
                val hex = colorToHex(color)
                nameColors = nameColors.toMutableList().also {
                    if (nameIndex >= 0) it[nameIndex] = hex else it.add(hex)
                }
                pickerNameIndex = null
            }
        )
    }

    val bracketIndex = pickerBracketIndex
    if (bracketIndex != null) {
        val initialHex = if (bracketIndex >= 0) bracketColors.getOrNull(bracketIndex) else null
        val initial = try {
            Color(("FF${initialHex ?: "90CAF9"}").toLong(16))
        } catch (e: Exception) {
            Color.Gray
        }
        ColorPickerDialog(
            initial = initial,
            onDismiss = { pickerBracketIndex = null },
            onPick = { color ->
                val hex = colorToHex(color)
                bracketColors = bracketColors.toMutableList().also {
                    if (bracketIndex >= 0) it[bracketIndex] = hex else it.add(hex)
                }
                pickerBracketIndex = null
            }
        )
    }
}

@Composable
private fun ColorSlotRow(
    label: String,
    hex: String,
    allowNone: Boolean = false,
    onNoneClick: () -> Unit = {},
    onPickClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isNone = hex.equals("none", ignoreCase = true)
        val preview = if (isNone) null else try {
            Color(("FF${hex.trim()}").toLong(16))
        } catch (e: Exception) {
            Color.Gray
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(preview ?: MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (isNone) "Not colored" else "#$hex",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (allowNone && !isNone) {
            TextButton(onClick = onNoneClick) {
                Text("None")
            }
        }
        TextButton(onClick = onPickClick) {
            Text("Pick")
        }
    }
}

@Composable
private fun PaletteColorRow(
    hex: String,
    onPickClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val preview = try {
            Color(("FF${hex.trim()}").toLong(16))
        } catch (e: Exception) {
            Color.Gray
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(preview)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "#$hex",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onPickClick) {
            Text("Pick")
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Remove color",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** State for the add/edit nick-color-rule dialog: null index = adding. */
private data class NickRuleDialogState(
    val index: Int?,
    val rule: NickColorRule?
)

/**
 * Nick colors section: master toggle plus up to 10 per-person <nick>
 * color rules. A rule always wins wherever that name talks. Unlisted
 * nicks get a deterministic pick from the theme's default name-color
 * palette (empty palette = names uncolored).
 */
@Composable
private fun NickColorsSection(vm: MainViewModel) {
    val enabled by vm.nickColoringEnabled.collectAsStateWithLifecycle()
    val rules by vm.nickRules.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<NickRuleDialogState?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(8.dp)) {
            SettingToggle(
                title = "Color <nick> mentions",
                subtitle = "Color the <> and names in IRC-style mentions like <DevXen>",
                checked = enabled,
                onCheckedChange = vm::setNickColoringEnabled
            )
            Text(
                text = "Give people their own colors — a nick keeps the same " +
                    "colors every time it appears. Anyone not listed gets an " +
                    "automatic color based on their name, so different people " +
                    "show in different colors.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            rules.forEachIndexed { index, rule ->
                NickRuleRow(
                    rule = rule,
                    onEdit = { dialog = NickRuleDialogState(index, rule); error = null },
                    onDelete = { vm.deleteNickRule(index) }
                )
            }
            if (rules.size < MAX_NICK_RULES) {
                TextButton(onClick = { dialog = NickRuleDialogState(null, null); error = null }) {
                    Text("Add person")
                }
            } else {
                Text(
                    text = "Maximum of $MAX_NICK_RULES people reached.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            if (error != null) {
                Text(
                    text = error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }

    val state = dialog
    if (state != null) {
        NickRuleDialog(
            initial = state.rule,
            onDismiss = { dialog = null },
            onSave = { nick, bracketsHex, nameHex ->
                val ok = if (state.index == null) {
                    vm.addNickRule(nick, bracketsHex, nameHex)
                } else {
                    vm.updateNickRule(state.index, nick, bracketsHex, nameHex)
                }
                if (ok) {
                    dialog = null
                    error = null
                } else {
                    error = "That name is already listed, or all $MAX_NICK_RULES slots are full."
                }
            }
        )
    }
}

/**
 * v1.63 nick color themes: pick a preset palette (Quote404 logo colors by
 * default, Rainbow, and more), build your own, or fall back to the app
 * theme's classic nick colors. Each theme paints a nick's name, brackets,
 * and — when enabled — what they say in that nick's own color.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NickColorThemeSection(vm: MainViewModel) {
    val themeId by vm.nickColorThemeId.collectAsStateWithLifecycle()
    val customs by vm.customNickColorThemes.collectAsStateWithLifecycle()
    val colorMessage by vm.nickColorMessage.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    var editorFor by remember { mutableStateOf<NickColorTheme?>(null) }
    var creating by remember { mutableStateOf(false) }

    val options: List<NickColorTheme> = remember(customs) {
        NickColorThemePresets + customs
    }
    val selected: NickColorTheme? = options.firstOrNull { it.id == themeId }
    val selectedLabel = when {
        themeId == NICK_THEME_APP -> "App theme (classic)"
        selected != null -> selected.name + if (selected.isCustom) " (yours)" else ""
        else -> "Quote404"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Nick color theme", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "One color per person, used for their name, their " +
                    "brackets, and — if you turn it on below — what they say.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Theme") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        options.forEach { theme ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        NickThemeDots(
                                            colors = theme.nameColors,
                                            max = 6
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            theme.name + if (theme.isCustom) " (yours)" else ""
                                        )
                                    }
                                },
                                onClick = {
                                    vm.setNickColorThemeId(theme.id)
                                    menuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("App theme (classic)") },
                            onClick = {
                                vm.setNickColorThemeId(NICK_THEME_APP)
                                menuExpanded = false
                            }
                        )
                    }
                }
                val custom = customs.firstOrNull { it.id == themeId }
                if (custom != null) {
                    IconButton(onClick = {
                        editorFor = custom
                        creating = false
                    }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { vm.deleteCustomNickColorTheme(custom.id) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // Palette preview dots for the selected theme.
            val previewColors = selected?.nameColors
            if (!previewColors.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                NickThemeDots(colors = previewColors, max = MAX_NICK_THEME_COLORS)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                editorFor = null
                creating = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New theme")
            }
            Spacer(Modifier.height(4.dp))
            SettingToggle(
                title = "Color what they say",
                subtitle = "Paint each person's message text in their own color too (off by default)",
                checked = colorMessage,
                onCheckedChange = vm::setNickColorMessage
            )
        }
    }

    if (creating || editorFor != null) {
        NickColorThemeEditorDialog(
            initial = editorFor,
            onDismiss = { creating = false; editorFor = null },
            onSave = { name, colors ->
                if (creating) {
                    val added = vm.addCustomNickColorTheme(name, colors)
                    if (added != null) vm.setNickColorThemeId(added.id)
                } else {
                    editorFor?.let { vm.updateCustomNickColorTheme(it.id, name, colors) }
                }
                creating = false
                editorFor = null
            }
        )
    }
}

/** Add/edit dialog for a custom nick color theme. */
@Composable
private fun NickColorThemeEditorDialog(
    initial: NickColorTheme?,
    onDismiss: () -> Unit,
    onSave: (name: String, colors: List<Int>) -> Unit
) {
    var name by remember(initial) { mutableStateOf(initial?.name ?: "") }
    var colors by remember(initial) {
        mutableStateOf(initial?.nameColors ?: listOf(0xFF00D4FF.toInt()))
    }
    var pickingIndex by remember { mutableStateOf<Int?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New nick color theme" else "Edit theme") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Theme name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Colors (up to $MAX_NICK_THEME_COLORS) — people are " +
                        "dealt these in order, so put your favorites first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                colors.forEachIndexed { index, argb ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(argb))
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "#${"%08X".format(argb)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { pickingIndex = index }) {
                            Text("Pick")
                        }
                        IconButton(
                            onClick = { colors = colors.filterIndexed { i, _ -> i != index } },
                            enabled = colors.size > 1
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Remove color",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (colors.size < MAX_NICK_THEME_COLORS) {
                    TextButton(onClick = {
                        colors = colors + 0xFF00D4FF.toInt()
                        pickingIndex = colors.size - 1
                    }) {
                        Text("Add color")
                    }
                }
                if (error != null) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (colors.isEmpty()) {
                    error = "Add at least one color."
                    return@Button
                }
                onSave(name.trim().ifEmpty { "Custom" }, colors)
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

    val pickIdx = pickingIndex
    if (pickIdx != null && pickIdx in colors.indices) {
        ColorPickerDialog(
            initial = Color(colors[pickIdx]),
            onDismiss = { pickingIndex = null },
            onPick = { picked ->
                colors = colors.toMutableList().also {
                    it[pickIdx] = picked.toArgb()
                }
                pickingIndex = null
            }
        )
    }
}

@Composable
private fun NickRuleRow(
    rule: NickColorRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val themeBrackets = LocalNickPalette.current.brackets
    val bracketColor = resolveRuleColor(rule.bracketsHex, themeBrackets)
        ?: MaterialTheme.colorScheme.onSurface
    val nameLabel = when (rule.nameHex) {
        null -> "auto name color"
        NICK_COLOR_NONE -> "plain name"
        else -> null
    }
    val bracketLabel = when (rule.bracketsHex) {
        null -> null
        NICK_COLOR_NONE -> "plain <>"
        else -> null
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "<",
            color = bracketColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        val nameColor = hexToColorOrNull(rule.nameHex)
        Text(
            text = rule.nick,
            color = nameColor ?: MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = ">",
            color = bracketColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        val detailLabel = listOfNotNull(nameLabel, bracketLabel).joinToString(" · ")
        if (detailLabel.isNotEmpty()) {
            Text(
                text = detailLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit colors")
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Remove person",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** Add/edit dialog for one per-person nick color rule. */
@Composable
private fun NickRuleDialog(
    initial: NickColorRule?,
    onDismiss: () -> Unit,
    onSave: (nick: String, bracketsHex: String?, nameHex: String?) -> Unit
) {
    var nick by remember { mutableStateOf(initial?.nick ?: "") }
    var bracketsHex by remember { mutableStateOf(initial?.bracketsHex) }
    var nameHex by remember { mutableStateOf(initial?.nameHex) }
    var pickerFor by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add person" else "Edit person") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nick,
                    onValueChange = { nick = it },
                    label = { Text("Nick name (e.g. DevXen)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                NickDialogColorRow(
                    label = "<> color",
                    hex = bracketsHex,
                    defaultLabel = "theme default",
                    onPickClick = { pickerFor = "brackets" },
                    onDefaultClick = { bracketsHex = null },
                    onNoneClick = { bracketsHex = NICK_COLOR_NONE }
                )
                NickDialogColorRow(
                    label = "Name color",
                    hex = nameHex,
                    defaultLabel = "automatic",
                    onPickClick = { pickerFor = "name" },
                    onDefaultClick = { nameHex = null },
                    onNoneClick = { nameHex = NICK_COLOR_NONE }
                )
                Text(
                    text = "Default <> uses the theme's nick color; automatic " +
                        "name gives them a color based on their name. " +
                        "Choose None to leave that part uncolored — e.g. " +
                        "colored <> with a plain name.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nick.trim(), bracketsHex, nameHex) },
                enabled = nick.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    val target = pickerFor
    if (target != null) {
        val currentHex = if (target == "brackets") bracketsHex else nameHex
        val initialColor = hexToColorOrNull(currentHex) ?: Color(0xFF7FC4FF)
        ColorPickerDialog(
            initial = initialColor,
            onDismiss = { pickerFor = null },
            onPick = { color ->
                val hex = colorToHex(color)
                if (target == "brackets") bracketsHex = hex else nameHex = hex
                pickerFor = null
            }
        )
    }
}

@Composable
private fun NickDialogColorRow(
    label: String,
    hex: String?,
    defaultLabel: String,
    onPickClick: () -> Unit,
    onDefaultClick: () -> Unit,
    onNoneClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val preview = hexToColorOrNull(hex)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(preview ?: MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            val status = when {
                hex == null -> defaultLabel
                hex.isEmpty() -> "None"
                else -> "#$hex"
            }
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onDefaultClick) { Text("Default") }
        TextButton(onClick = onNoneClick) { Text("None") }
        TextButton(onClick = onPickClick) { Text("Pick") }
    }
}


/** Dedicated Themes screen: theme presets/custom themes, nick colors, chip colors. */
@Composable
fun ThemesScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val themeId by vm.themeId.collectAsStateWithLifecycle()
    val devXenBgMode by vm.devXenBgMode.collectAsStateWithLifecycle()
    // v1.61.3: the user's own DevXen background picture.
    val devXenCustomBg by vm.devXenCustomBg.collectAsStateWithLifecycle()
    val customThemes by vm.customThemes.collectAsStateWithLifecycle()
    // v1.51: per-preset color overrides (built-ins are directly editable;
    // "Reset to default" deletes the override).
    val builtInOverrides by vm.builtInThemeOverrides.collectAsStateWithLifecycle()
    // v1.83: plugin theme packs — collected so the picker re-emits when
    // packs are installed/uninstalled/toggled.
    val pluginInfos by com.quotebrowser.app.data.plugins.PluginManager.plugins
        .collectAsStateWithLifecycle()
    val pluginThemes = remember(pluginInfos) {
        pluginInfos
            .filter {
                it.error == null && it.enabled &&
                    it.manifest.type == com.quotebrowser.app.data.plugins.PluginManager.TYPE_THEME_PACK
            }
            .flatMap { info ->
                info.themes.map { t ->
                    t.themeId(info.manifest.id) to t.preset
                }
            }
    }

    // Open theme editor dialog: null = closed.
    var themeEditor by remember { mutableStateOf<ThemeEditorState?>(null) }

    // v1.88.23: labeled theme groups, collapsed except the one holding
    // the selected theme — the picker stays compact.
    val presetGroups = remember { themeGroups() }
    var collapsedGroups by remember {
        mutableStateOf(
            presetGroups
                .filter { group -> group.presets.none { it.id == themeId } }
                .map { it.label }
                .toSet()
        )
    }

    // Pending theme export: (name, colors CSV) while the SAF picker is open.
    var pendingExport by remember { mutableStateOf<Pair<String, String>?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val pending = pendingExport
        pendingExport = null
        if (pending == null || uri == null) return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(
                    buildThemeJson(pending.first, pending.second)
                        .toByteArray(Charsets.UTF_8)
                )
            }
            Toast.makeText(context, "Theme exported", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Couldn't export theme", Toast.LENGTH_SHORT).show()
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val text = context.contentResolver.openInputStream(uri)
                ?.use { it.bufferedReader().readText() }
            val theme = text?.let { parseThemeJson(it) }
            if (theme != null) {
                vm.addCustomTheme(theme.name, theme.colors)
                Toast.makeText(
                    context,
                    "Imported theme \"${theme.name}\"",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context, "Couldn't import theme file", Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context, "Couldn't import theme file", Toast.LENGTH_SHORT
            ).show()
        }
    }
    fun launchExport(name: String, colors: String) {
        pendingExport = name to colors
        exportLauncher.launch("${safeThemeFileName(name)}-theme.json")
    }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            flingBehavior = rememberCappedFlingBehavior()) {
        item {
            SettingsSectionHeader(text = "Theme")
        }
        // v1.88.23: labeled collapsible groups instead of one long flat
        // list; alphabetical within each group.
        presetGroups.forEach { group ->
            val expanded = group.label !in collapsedGroups
            item(key = "theme-group-${group.label}") {
                ThemeGroupHeader(
                    label = group.label,
                    count = group.presets.size,
                    expanded = expanded,
                    hasSelection = group.presets.any { it.id == themeId },
                    onToggle = {
                        collapsedGroups =
                            if (group.label in collapsedGroups)
                                collapsedGroups - group.label
                            else collapsedGroups + group.label
                    }
                )
            }
            if (expanded) {
                items(group.presets, key = { "preset-${it.id}" }) { preset ->
                    BuiltInThemePresetRow(
                        preset = preset,
                        themeId = themeId,
                        builtInOverrides = builtInOverrides,
                        vm = vm,
                        onOpenEditor = { themeEditor = it },
                        onExportTheme = { name, csv -> launchExport(name, csv) }
                    )
                }
            }
        }
        // v1.83: plugin theme packs. Pack data is read-only (no customize /
        // export / reset); selecting applies the theme everywhere like a
        // built-in preset.
        if (pluginThemes.isNotEmpty()) {
            item {
                SettingsSectionHeader(text = "Plugin themes")
            }
            items(pluginThemes, key = { it.first }) { (id, preset) ->
                ThemePresetRow(
                    name = preset.name,
                    selected = themeId == id,
                    previewColors = listOf(
                        Color(preset.background),
                        Color(preset.surface),
                        Color(preset.primary),
                        Color(preset.number),
                        Color(preset.rating),
                        Color(preset.ratingText),
                        Color(preset.nickBrackets)
                    ),
                    onCustomize = null,
                    onExport = null,
                    onReset = null,
                    onClick = { vm.setThemeId(id) }
                )
            }
        }
        // DevXen background-art placement: only meaningful while the DevXen
        // theme is selected.
        if (themeId == "devxen") {
            item {
                SettingsSectionHeader(text = "DevXen background")
            }
            items(DevXenBgMode.entries.toList()) { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { vm.setDevXenBgMode(mode) }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = devXenBgMode == mode,
                        onClick = { vm.setDevXenBgMode(mode) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = when (mode) {
                                DevXenBgMode.BEHIND_QUOTES ->
                                    "Artwork dimmed behind the lists; quote cards go semi-transparent with blue borders"
                                DevXenBgMode.ON_EACH_QUOTE ->
                                    "Artwork dimmed inside every quote card"
                                DevXenBgMode.COLORS_ONLY ->
                                    "Solid black — just the blue and gray colors"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            // v1.61.3: use your own picture as the DevXen background art.
            if (devXenBgMode != DevXenBgMode.COLORS_ONLY) {
                item {
                    val customBgPicker = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        if (uri != null) vm.setDevXenCustomBgImage(uri)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Background image",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (devXenCustomBg.isNotBlank())
                                    "Your picture is shown dimmed behind " +
                                        "lists, quote cards and DevXen " +
                                        "share cards."
                                else "Replace the DevXen artwork with your " +
                                    "own picture.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (devXenCustomBg.isNotBlank()) {
                            TextButton(onClick = { vm.clearDevXenCustomBgImage() }) {
                                Text("Remove")
                            }
                        } else {
                            Button(
                                onClick = { customBgPicker.launch(arrayOf("image/*")) }
                            ) { Text("Choose") }
                        }
                    }
                }
            }
        }
        if (customThemes.isNotEmpty()) {
            item {
                Text(
                    text = "My themes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        itemsIndexed(customThemes) { index, theme ->
            CustomThemeRow(
                theme = theme,
                selected = themeId == customThemeId(index),
                onClick = { vm.setThemeId(customThemeId(index)) },
                onExport = { launchExport(theme.name, theme.colors) },
                onEdit = {
                    val parsed = parseCustomThemeSlots(theme.colors)
                    val (fixed, nameColors, actionOverridesName) = parsed
                    // Divider lives at CSV position 14; older themes fall
                    // back to "auto" (derived from the background).
                    val dividerHex = theme.colors.split(",")
                        .getOrNull(14)?.trim().orEmpty().ifBlank { "auto" }
                    // v1.55: quote background lives at CSV position 16;
                    // older themes fall back to the black default.
                    val quoteBgHex = theme.colors.split(",")
                        .getOrNull(16)?.trim().orEmpty().ifBlank { "000000" }
                    themeEditor = ThemeEditorState(
                        title = "Edit theme",
                        initialName = theme.name,
                        initialSlots = fixed + dividerHex + quoteBgHex,
                        initialNameColors = nameColors,
                        initialBracketColors = defaultBracketHexes(
                            parsed.bracketColors,
                            nameColors,
                            parsed.bracketsSingle,
                            fixed.getOrNull(0).orEmpty()
                        ),
                        initialActionOverridesName = actionOverridesName,
                        initialColorMessageWithSpeaker = parsed.colorMessageWithSpeaker,
                        saveLabel = "Save changes",
                        onSave = { name, colors ->
                            vm.updateCustomTheme(index, name, colors)
                        }
                    )
                },
                onDelete = { vm.deleteCustomTheme(index) }
            )
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        themeEditor = ThemeEditorState(
                            title = "Create a new theme",
                            initialName = "",
                            initialSlots = listOf(
                                "000000", "2B2B2B", "008CFF", "FFFFFF", "F2F2F2", "008CFF",
                                DEFAULT_RATING_HEX, "auto", DEFAULT_NICK_BRACKETS_HEX
                            ),
                            initialNameColors = DefaultNickNameColors.map {
                                it.toString(16).takeLast(6).uppercase()
                            },
                            initialBracketColors = DefaultNickNameColors.map {
                                val tinted = defaultBracketTint(it, true)
                                "%06X".format(tinted and 0xFFFFFFL)
                            },
                            initialActionOverridesName = true,
                            initialColorMessageWithSpeaker = false,
                            saveLabel = "Save theme",
                            onSave = { name, colors ->
                                val newIndex = customThemes.size
                                vm.addCustomTheme(name, colors)
                                vm.setThemeId(customThemeId(newIndex))
                            }
                        )
                    },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Create a new theme",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item {
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import theme")
            }
        }
        item {
            SettingsSectionHeader(text = "Nick colors")
        }
        item {
            NickColorsSection(vm = vm)
        }
        item {
            NickColorThemeSection(vm = vm)
        }
        item {
            SettingsSectionHeader(text = "Database tag colors")
        }
        item {
            DbTagColorsSection(vm = vm)
        }
    }

        themeEditor?.let { state ->
            ThemeEditorDialog(
                state = state,
                onDismiss = { themeEditor = null }
            )
        }
    }
}

/** Per-database source-tag chip colors. Missing color = default tag style (off). */
@Composable
private fun DbTagColorsSection(vm: MainViewModel) {
    val databases by vm.databases.collectAsStateWithLifecycle()
    val tagColors by vm.dbTagColors.collectAsStateWithLifecycle()
    var pickerTarget by remember { mutableStateOf<DbInfo?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Color the source tag shown on quotes from each database. " +
                    "Off keeps the default tag style.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            databases.forEach { db ->
                val hex = tagColors[db.id]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                hex?.let { hexToColorOrNull(it) }
                                    ?: MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Text(
                        text = db.label,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (hex != null) {
                        TextButton(onClick = { vm.setDbTagColor(db.id, null) }) {
                            Text("Off")
                        }
                    }
                    TextButton(onClick = { pickerTarget = db }) {
                        Text(if (hex == null) "Pick" else "Change")
                    }
                }
            }
        }
    }

    pickerTarget?.let { db ->
        val initial = tagColors[db.id]?.let { hexToColorOrNull(it) } ?: Color.Gray
        ColorPickerDialog(
            initial = initial,
            onDismiss = { pickerTarget = null },
            onPick = { color ->
                vm.setDbTagColor(db.id, colorToHex(color))
                pickerTarget = null
            }
        )
    }
}
/**
 * Shareable theme JSON format. The colors value is the raw CustomTheme CSV
 * string (the same format the theme editor writes and
 * [parseCustomThemeSlots] reads), so an imported theme round-trips exactly.
 *
 * {
 *   "app": "quotebrowser-theme",
 *   "version": 1,
 *   "name": "Midnight Blue copy",
 *   "colors": "000000,000000,008CFF,...,1"
 * }
 */
private const val THEME_JSON_APP = "quotebrowser-theme"
private const val THEME_JSON_VERSION = 1

/** Serializes a theme to its shareable JSON file format. */
fun buildThemeJson(name: String, colors: String): String =
    JSONObject()
        .put("app", THEME_JSON_APP)
        .put("version", THEME_JSON_VERSION)
        .put("name", name)
        .put("colors", colors)
        .toString(2)

/**
 * Parses a theme JSON file. Returns null when the text isn't a valid
 * QuoteBrowser theme (wrong app/version, missing name or colors).
 */
fun parseThemeJson(text: String): CustomTheme? = try {
    val root = JSONObject(text)
    if (root.optString("app") != THEME_JSON_APP) null
    else if (root.optInt("version", -1) != THEME_JSON_VERSION) null
    else {
        val name = root.optString("name")
        val colors = root.optString("colors")
        if (name.isEmpty() || colors.isEmpty()) null
        else CustomTheme(name, colors)
    }
} catch (e: Exception) {
    null
}

/** Sanitizes a theme name for use as a file name. */
private fun safeThemeFileName(name: String): String =
    name.replace(Regex("[^A-Za-z0-9 _.-]"), "")
        .trim().ifEmpty { "theme" }
        .replace(' ', '_')

/**
 * Serializes a built-in preset into the custom-theme CSV format
 * (same slot order the theme editor writes) so presets can be exported
 * exactly like user themes.
 */
private fun ThemePreset.toExportCsv(): String {
    fun Long.rgbOrNone(): String = if (this == 0L) "none" else toRgbHex()
    return listOf(
        background.toRgbHex(), surface.toRgbHex(), primary.toRgbHex(),
        onBackground.toRgbHex(), onSurface.toRgbHex(), number.toRgbHex(),
        rating.toRgbHex(), ratingText.toRgbHex(), nickBrackets.rgbOrNone(),
        // CSV position 9: legacy single name slot (always blank now).
        "",
        nickAction.rgbOrNone(),
        if (nickNames.isEmpty()) "-" else nickNames.joinToString(";") { it.toRgbHex() },
        if (actionOverridesName) "1" else "0",
        if (nickBracketPalette.isEmpty()) "-" else nickBracketPalette.joinToString(";") { it.toRgbHex() },
        divider.rgbOrNone(),
        if (colorMessageWithSpeaker) "1" else "0",
        // v1.55: quote background (CSV position 16).
        quoteBackground.toRgbHex()
    ).joinToString(",")
}
