package com.quotebrowser.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.core.content.ContextCompat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import kotlin.math.roundToInt
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DisplayPrefs
import com.quotebrowser.app.data.layouts.LayoutStore
import com.quotebrowser.app.data.plugins.PluginManager
import com.quotebrowser.app.ui.layouts.LayoutPickerDialog
import com.quotebrowser.app.ui.layouts.QuoteLayoutsSettingsContent
import com.quotebrowser.app.data.MaturityLevel
import com.quotebrowser.app.data.DatabaseThemeConfig
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.data.TranslationRepository
import com.quotebrowser.app.data.FlaggedSourcedQuote
import com.quotebrowser.app.data.SettingsUiState
import com.quotebrowser.app.data.SiteLayout
import com.quotebrowser.app.data.plugins.rememberPluginThemeOptions
import com.quotebrowser.app.data.StatusMode
import com.quotebrowser.app.data.siteThemeFor
import com.quotebrowser.app.ui.MainViewModel
import com.quotebrowser.app.ui.MOOD_PRESETS
import com.quotebrowser.app.ui.MoodPreset
import com.quotebrowser.app.ui.components.AccentAlertDialog
import com.quotebrowser.app.ui.components.RatingFilterRow
import com.quotebrowser.app.ui.StartupDestinations
import com.quotebrowser.app.ui.TransformMode
import com.quotebrowser.app.ui.hexToColorOrNull
import com.quotebrowser.app.ui.TtsEngineSettingsCard
import com.quotebrowser.app.ui.VoiceRecordings
import com.quotebrowser.app.ui.components.ColorPickerDialog
import com.quotebrowser.app.ui.components.DailyReminderSetting
import com.quotebrowser.app.ui.components.QuoteAlarmSetting
import com.quotebrowser.app.ui.components.LocalDbThemeSnapshots
import com.quotebrowser.app.ui.components.VacationModeSetting
import com.quotebrowser.app.ui.components.colorToHex
import com.quotebrowser.app.ui.theme.CustomTheme
import com.quotebrowser.app.ui.theme.ThemeOption
import com.quotebrowser.app.ui.theme.groupedDbThemeOptions
import com.quotebrowser.app.ui.components.DatabaseMultiSelectDialog
import com.quotebrowser.app.ui.theme.effectivePreset
import com.quotebrowser.app.ui.theme.quoteBackgroundFor
import com.quotebrowser.app.data.resolveDbTheme
import com.quotebrowser.app.ui.theme.ThemePresets
import com.quotebrowser.app.ui.theme.customThemeId
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

private const val FILE_PROVIDER_AUTHORITY = "com.quotebrowser.app.fileprovider"

/**
 * v1.58: settings-search keywords per section — section title, setting
 * titles, and useful synonyms. The search matches all query words
 * (AND) as substrings of the section title or any keyword.
 */
private val SETTINGS_SEARCH_KEYWORDS = mapOf(
    "Display" to "theme appearance dark light color font size text scale quote numbers rating badges web links url address permalink shake shuffle irc separators dialogue density compact comfortable black background outside site cards startup screen break spacing gap between quotes",
    "Quote layouts" to "quote layouts card arrangement poster minimal newspaper chat index card html css export customize template style",
    "Quote text" to "colorize speaker names nick colors action lines timestamps quoteclean normalize breaks slash separators",
    "Features" to "random without repeats notes slideshow video export reading goals weekly monthly nick timeline tap gestures double tap duplicate merge select all bulk quote battle vacation mode streak freeze personal ratings stars pinch zoom similar more like this explain gemini flag report hide quotes mute nicks content filter categories auto rules offensive games achievements collections up next queue search history alerts ambient mode",
    "Database themes" to "site themes master override per-database colors layout header footer vote page background quote background background image logo",
    "Read aloud" to "tts text to speech voice gemini speaker voices narrator sleep timer playlist",
    "Translations" to "translate translation language swedish english warpdrive gemini engine auto-translate cache clear",
    "Quote viewer" to "detail view swipe",
    "Widget" to "home screen widget quote of the day streak widget daily quiz tap refresh",
    "Reading goal" to "daily goal streak",
    "Quote length presets" to "length presets short medium long one line",
    "Wallpaper" to "wallpaper daily",
    "Auto backup" to "auto backup automatic folder",
    "Backup & restore" to "backup restore export import file",
    "Quote pings" to "pings notifications new quotes",
    "Daily reminder" to "daily reminder notification",
    "Quote alarm" to "quote alarm read aloud spoken favorites time",
    "Swipe actions" to "swipe actions left right home random",
    "Reading stats" to "statistics stats heatmap",
    "My Quotes" to "my quotes personal add quote",
    "Databases" to "databases order reorder import verify collection date info counts backup"
)

private val SETTINGS_SECTION_TITLES = SETTINGS_SEARCH_KEYWORDS.keys.toList()

/**
 * v1.59: the 18 flat settings sections, grouped into three collapsible
 * categories. Every title here must stay in sync with
 * SETTINGS_SEARCH_KEYWORDS above.
 */
private data class SettingsCategory(val name: String, val sections: List<String>)

private val SETTINGS_CATEGORIES = listOf(
    SettingsCategory(
        "Appearance",
        listOf("Display", "Quote layouts", "Database themes", "Wallpaper", "Widget")
    ),
    SettingsCategory(
        "Reading",
        listOf(
            "Quote text", "Quote viewer", "Read aloud", "Translations", "Reading goal",
            "Reading stats", "Quote pings", "Daily reminder", "Quote alarm", "Swipe actions",
            "Quote length presets", "Content maturity"
        )
    ),
    SettingsCategory(
        "Your data",
        listOf(
            "My Quotes", "Backup & restore", "Auto backup", "Databases",
            "Features"
        )
    )
)

/** Which (category, section) a setting row is rendered in. */
private data class SettingsLocation(val category: String, val section: String)

private val LocalSettingsLocation = compositionLocalOf { SettingsLocation("", "") }

/**
 * v1.59: UI-only settings-screen state (collapse, pins, recent). Null when
 * [SettingToggle] is rendered outside SettingsScreen — then the toggle
 * behaves exactly as before (no tracking, no long-press pin).
 */
val LocalSettingsUiState = compositionLocalOf<SettingsUiState?> { null }

/** Provides the (category, section) location for tracking/pinning. */
@Composable
private fun InSettingsSection(
    category: String,
    section: String,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSettingsLocation provides SettingsLocation(category, section)
    ) {
        content()
    }
}

/** v1.59: tappable category header — collapses/expands the whole category. */
@Composable
private fun SettingsCategoryHeader(
    name: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess
            else Icons.Filled.ExpandMore,
            contentDescription = if (expanded) "Collapse $name" else "Expand $name",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * v1.59: "Recently changed" row — the last-touched toggles,
 * most-recent first. Tapping a row scrolls to that setting.
 */
@Composable
private fun RecentlyChangedRow(
    entries: List<SettingsUiState.Entry>,
    onJump: (SettingsUiState.Entry) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Text(
                text = "Recently changed",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            entries.forEach { e ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onJump(e) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(e.title, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            e.section,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "Go to setting",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * v1.59: "Pinned" row — long-press any toggle (or section header) to pin
 * it here. Tapping a row scrolls to that setting; the X unpins it.
 */
@Composable
private fun PinnedRow(
    entries: List<SettingsUiState.Entry>,
    onJump: (SettingsUiState.Entry) -> Unit,
    onUnpin: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            Text(
                text = "Pinned",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            entries.forEach { e ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onJump(e) }
                        .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (e.isSection) "${e.title} (section)" else e.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            if (e.isSection) "Section in ${e.category}"
                            else "${e.section} · ${e.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onUnpin(e.id) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Unpin ${e.title}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    vm: MainViewModel,
    onMyQuotesClick: () -> Unit = {},
    // v1.88.36: opens the user-created databases ("My Databases") screen.
    onMyDatabasesClick: () -> Unit = {},
    onStatsClick: () -> Unit = {},
    onDbOrderClick: () -> Unit = {},
    onDuplicatesClick: () -> Unit = {},
    onBackupRestore: () -> Unit = {},
    // v1.59: navigation hooks (wired by the nav owner).
    onPreviewThemesClick: () -> Unit = {},
    onVerifyDatabasesClick: () -> Unit = {},
    /** v1.61.20: opens the saved voice recordings screen. */
    onVoiceRecordingsClick: () -> Unit = {},
    /** v1.63: opens the per-nick speaker voices screen. */
    onSpeakerVoicesClick: () -> Unit = {},
    /** v1.83: opens the Plugins screen. */
    onPluginsClick: () -> Unit = {},
    /** v1.85: opens the custom HTML/CSS layout editor. */
    onLayoutEditorClick: () -> Unit = {}
) {
    val showNumber by vm.showQuoteNumber.collectAsStateWithLifecycle()
    val showRating by vm.showRating.collectAsStateWithLifecycle()
    val showUrl by vm.showUrl.collectAsStateWithLifecycle()
    val quoteFont by vm.quoteFont.collectAsStateWithLifecycle()
    val textScale by vm.textScale.collectAsStateWithLifecycle()
    val themeId by vm.themeId.collectAsStateWithLifecycle()
    val customThemes by vm.customThemes.collectAsStateWithLifecycle()
    val databases by vm.databases.collectAsStateWithLifecycle()
    val shakeToShuffle by vm.shakeToShuffle.collectAsStateWithLifecycle()
    val density by vm.density.collectAsStateWithLifecycle()
    val ircLineBreaks by vm.ircLineBreaks.collectAsStateWithLifecycle()
    val dialogueLineBreaks by vm.dialogueLineBreaks.collectAsStateWithLifecycle()
    val qcColorNames by vm.qcColorNames.collectAsStateWithLifecycle()
    val qcColorActions by vm.qcColorActions.collectAsStateWithLifecycle()
    val qcNormalizeBreaks by vm.qcNormalizeBreaks.collectAsStateWithLifecycle()
    val qcShowTimestamps by vm.qcShowTimestamps.collectAsStateWithLifecycle()
    // v1.86: per-category QuoteClean color controls.
    val qcColorMessage by vm.qcColorMessage.collectAsStateWithLifecycle()
    val qcNameColor by vm.qcNameColor.collectAsStateWithLifecycle()
    val qcMessageColor by vm.qcMessageColor.collectAsStateWithLifecycle()
    val qcActionColor by vm.qcActionColor.collectAsStateWithLifecycle()
    val qcTimestampColor by vm.qcTimestampColor.collectAsStateWithLifecycle()
    val qcColorOther by vm.qcColorOther.collectAsStateWithLifecycle()
    val qcOtherColor by vm.qcOtherColor.collectAsStateWithLifecycle()
    val perNickVoices by vm.ttsPerNickVoices.collectAsStateWithLifecycle()
    val widgetCategory by vm.widgetCategory.collectAsStateWithLifecycle()
    val widgetCategoryNames by vm.widgetCategoryNames.collectAsStateWithLifecycle()
    val autoBackupFreq by vm.autoBackupFreq.collectAsStateWithLifecycle()
    val quotePingEnabled by vm.quotePingEnabled.collectAsStateWithLifecycle()
    val quotePingMinutes by vm.quotePingMinutes.collectAsStateWithLifecycle()
    val quotePingHour by vm.quotePingHour.collectAsStateWithLifecycle()
    /** v1.65 global content maturity ceiling. */
    val maturityCeiling by vm.maturityCeiling.collectAsStateWithLifecycle()
    val quotePingMinute by vm.quotePingMinute.collectAsStateWithLifecycle()
    val quotePingDay by vm.quotePingDay.collectAsStateWithLifecycle()
    val quotePingDbIds by vm.quotePingDbIds.collectAsStateWithLifecycle()
    // v1.74 notification-quote filters.
    val quotePingUseGlobalCeiling by vm.quotePingUseGlobalCeiling.collectAsStateWithLifecycle()
    val quotePingCustomCeiling by vm.quotePingCustomCeiling.collectAsStateWithLifecycle()
    val quotePingWorkHoursEnabled by vm.quotePingWorkHoursEnabled.collectAsStateWithLifecycle()
    val quotePingWorkDays by vm.quotePingWorkDays.collectAsStateWithLifecycle()
    val quotePingWorkStartMin by vm.quotePingWorkStartMin.collectAsStateWithLifecycle()
    val quotePingWorkEndMin by vm.quotePingWorkEndMin.collectAsStateWithLifecycle()
    val quotePingWorkCeiling by vm.quotePingWorkCeiling.collectAsStateWithLifecycle()
    val quotePingMinRating by vm.quotePingMinRating.collectAsStateWithLifecycle()
    val quotePingCategoryNames by vm.quotePingCategoryNames.collectAsStateWithLifecycle()
    val quotePingRecencyDays by vm.quotePingRecencyDays.collectAsStateWithLifecycle()
    val pingCategoryLower = remember(quotePingCategoryNames) {
        quotePingCategoryNames.map { it.lowercase() }.toSet()
    }
    val swipeLeft by vm.swipeLeft.collectAsStateWithLifecycle()
    val swipeRight by vm.swipeRight.collectAsStateWithLifecycle()
    val swipeHomeEnabled by vm.swipeHomeEnabled.collectAsStateWithLifecycle()
    val swipeRandomEnabled by vm.swipeRandomEnabled.collectAsStateWithLifecycle()
    val autoBackupTree by vm.autoBackupTreeUri.collectAsStateWithLifecycle()
    val lastAutoBackup by vm.lastAutoBackup.collectAsStateWithLifecycle()
    val dailyGoal by vm.dailyGoal.collectAsStateWithLifecycle()
    val wallpaperDaily by vm.wallpaperDailyEnabled.collectAsStateWithLifecycle()
    val wallpaperSource by vm.wallpaperSource.collectAsStateWithLifecycle()
    val detailSwipeAllDbs by vm.detailSwipeAllDbs.collectAsStateWithLifecycle()
    val detailSwipeOrder by vm.detailSwipeOrder.collectAsStateWithLifecycle()
    // v1.49 feature toggles (all default ON).
    val randomNoRepeat by vm.randomNoRepeat.collectAsStateWithLifecycle()
    val weeklyGoal by vm.weeklyGoal.collectAsStateWithLifecycle()
    val monthlyGoal by vm.monthlyGoal.collectAsStateWithLifecycle()
    val gesturesEnabled by vm.gesturesEnabled.collectAsStateWithLifecycle()
    val dupMergeEnabled by vm.dupMergeEnabled.collectAsStateWithLifecycle()
    val bulkSelectAll by vm.bulkSelectAll.collectAsStateWithLifecycle()
    val notesEnabled by vm.notesEnabled.collectAsStateWithLifecycle()
    val slideshowExportEnabled by vm.slideshowExportEnabled.collectAsStateWithLifecycle()
    val readingGoalsEnabled by vm.readingGoalsEnabled.collectAsStateWithLifecycle()
    val nickTimelineEnabled by vm.nickTimelineEnabled.collectAsStateWithLifecycle()
    // v1.50 feature toggles (all default ON).
    val startupScreen by vm.startupScreen.collectAsStateWithLifecycle()
    val quoteBattleOn by vm.quoteBattleEnabled.collectAsStateWithLifecycle()
    val vacationMode by vm.vacationMode.collectAsStateWithLifecycle()
    val vacationEnabled by vm.vacationEnabled.collectAsStateWithLifecycle()
    val vacationStartDay by vm.vacationStartDay.collectAsStateWithLifecycle()
    val vacationEndDay by vm.vacationEndDay.collectAsStateWithLifecycle()
    val statsCardShare by vm.statsCardShare.collectAsStateWithLifecycle()
    val extraWidgets by vm.extraWidgets.collectAsStateWithLifecycle()
    val pinchZoomOn by vm.pinchZoomEnabled.collectAsStateWithLifecycle()
    val personalRatingsOn by vm.personalRatingsEnabled.collectAsStateWithLifecycle()
    val browsePositionMemory by vm.browsePositionMemory.collectAsStateWithLifecycle()
    val dailyReminderEnabled by vm.dailyReminderEnabled.collectAsStateWithLifecycle()
    val dailyReminderHour by vm.dailyReminderHour.collectAsStateWithLifecycle()
    val dailyReminderMinute by vm.dailyReminderMinute.collectAsStateWithLifecycle()
    // v1.78 quote alarm (defaults OFF).
    val quoteAlarmEnabled by vm.quoteAlarmEnabled.collectAsStateWithLifecycle()
    val quoteAlarmHour by vm.quoteAlarmHour.collectAsStateWithLifecycle()
    val quoteAlarmMinute by vm.quoteAlarmMinute.collectAsStateWithLifecycle()
    // v1.51 feature toggles (all default ON).
    val perDbThemesOn by vm.perDbThemesEnabled.collectAsStateWithLifecycle()
    val classicLayoutOn by vm.classicLayoutOption.collectAsStateWithLifecycle()
    val voteControlsOn by vm.plusMinusRatingsEnabled.collectAsStateWithLifecycle()
    val queuedTagsOn by vm.queuedTagsEnabled.collectAsStateWithLifecycle()
    val statusFilterOn by vm.statusFilterEnabled.collectAsStateWithLifecycle()
    // v1.53 feature toggles (all default ON except Kid-safe, default OFF).
    val myQuotesOn by vm.myQuotesEnabled.collectAsStateWithLifecycle()
    val perDbWidgetOn by vm.perDbWidgetEnabled.collectAsStateWithLifecycle()
    val memorizeOn by vm.memorizeModeEnabled.collectAsStateWithLifecycle()
    val onThisDayOn by vm.onThisDayEnabled.collectAsStateWithLifecycle()
    val kidSafeOn by vm.kidSafeEnabled.collectAsStateWithLifecycle()
    val siteShareCardsOn by vm.siteShareCardsEnabled.collectAsStateWithLifecycle()
    val explainOn by vm.explainQuoteEnabled.collectAsStateWithLifecycle()
    val ambientOn by vm.ambientModeEnabled.collectAsStateWithLifecycle()
    val ambientInterval by vm.ambientIntervalSec.collectAsStateWithLifecycle()
    // v1.53 report/flag (feature toggle default ON, hide default OFF).
    val flagQuotesOn by vm.flagQuotesEnabled.collectAsStateWithLifecycle()
    val hideFlaggedOn by vm.hideFlaggedQuotes.collectAsStateWithLifecycle()
    val flaggedQuotes by vm.flaggedQuotes.collectAsStateWithLifecycle()
    var showFlaggedReview by remember { mutableStateOf(false) }
    // v1.53 main-theme option: black background outside the quote cards.
    val blackOutsideOn by vm.blackOutsideQuotes.collectAsStateWithLifecycle()
    val transformModeName by vm.transformMode.collectAsStateWithLifecycle()
    val transformMode = remember(transformModeName) {
        runCatching { TransformMode.valueOf(transformModeName) }
            .getOrDefault(TransformMode.OFF)
    }
    val masterOverride by vm.masterThemeOverride.collectAsStateWithLifecycle()
    // v1.59: Quote-of-the-Day notification source ("random_sources" | "favorites").
    val qotdSource by vm.qotdSource.collectAsStateWithLifecycle()
    val dbThemeConfigs by vm.dbThemeConfigs.collectAsStateWithLifecycle()
    // "All databases" is a virtual entry; backups only list real databases.
    val backupDbs = remember(databases) {
        databases.filter { it.id != com.quotebrowser.app.data.DatabaseManager.ALL_DBS_ID }
    }
    // v1.51: per-database theme rows skip the virtual "All databases" entry.
    val themeDbs = remember(databases) {
        databases.filter { it.id != com.quotebrowser.app.data.DatabaseManager.ALL_DBS_ID }
    }
    var backupStatus by remember { mutableStateOf<String?>(null) }
    var wallpaperStatus by remember { mutableStateOf<String?>(null) }

    // Database backup export / share.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // v1.88.22: "Break between quotes" (DisplayPrefs shared-prefs store;
    // init is idempotent) — blank lines between quote-body turns.
    DisplayPrefs.init(context)
    val breakBetweenQuotes by DisplayPrefs.breakBetweenQuotes.collectAsStateWithLifecycle()

    // Notification permission for scheduled quote pings.
    val pingPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) vm.setQuotePingEnabled(true)
        }
    // Daily/Weekly ping time picker (framework dialog, like the QotD one).
    var showPingDbDialog by remember { mutableStateOf(false) }
    val pingTimeLabel = remember(quotePingHour, quotePingMinute, context) {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, quotePingHour)
            set(java.util.Calendar.MINUTE, quotePingMinute)
        }
        android.text.format.DateFormat.getTimeFormat(context).format(cal.time)
    }
    val pingTimeDialog = remember(quotePingHour, quotePingMinute, context) {
        android.app.TimePickerDialog(
            context,
            { _, h, m -> vm.setQuotePingTime(h, m) },
            quotePingHour,
            quotePingMinute,
            android.text.format.DateFormat.is24HourFormat(context)
        )
    }
    // v1.74: work-hours start/end pickers for quote pings.
    fun pingMinutesLabel(mins: Int): String {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, (mins / 60).coerceIn(0, 23))
            set(java.util.Calendar.MINUTE, (mins % 60).coerceIn(0, 59))
        }
        return android.text.format.DateFormat.getTimeFormat(context).format(cal.time)
    }
    val workStartLabel = remember(quotePingWorkStartMin, context) {
        pingMinutesLabel(quotePingWorkStartMin)
    }
    val workEndLabel = remember(quotePingWorkEndMin, context) {
        pingMinutesLabel(quotePingWorkEndMin)
    }
    val workStartDialog = remember(quotePingWorkStartMin, context) {
        android.app.TimePickerDialog(
            context,
            { _, h, m -> vm.setQuotePingWorkStart(h, m) },
            (quotePingWorkStartMin / 60).coerceIn(0, 23),
            (quotePingWorkStartMin % 60).coerceIn(0, 59),
            android.text.format.DateFormat.is24HourFormat(context)
        )
    }
    val workEndDialog = remember(quotePingWorkEndMin, context) {
        android.app.TimePickerDialog(
            context,
            { _, h, m -> vm.setQuotePingWorkEnd(h, m) },
            (quotePingWorkEndMin / 60).coerceIn(0, 23),
            (quotePingWorkEndMin % 60).coerceIn(0, 59),
            android.text.format.DateFormat.is24HourFormat(context)
        )
    }
    var showPingMaturityDialog by remember { mutableStateOf(false) }
    var showPingWorkCeilingDialog by remember { mutableStateOf(false) }
    var showPingCategoryDialog by remember { mutableStateOf(false) }
    var exportTarget by remember { mutableStateOf<DbInfo?>(null) }
    var dbStatus by remember { mutableStateOf<String?>(null) }

    // Auto-backup folder picker.
    val treeLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                try {
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    // Some providers don't support persistable permissions.
                }
                vm.setAutoBackupTreeUri(uri.toString())
            }
        }
    val backupFolderName = remember(autoBackupTree) {
        autoBackupTree?.let { vm.autoBackupFolderName() }
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        val target = exportTarget
        exportTarget = null
        if (uri != null && target != null) {
            scope.launch {
                val error = vm.exportDatabase(target.id, uri)
                dbStatus = error ?: "Saved ${vm.exportFileName(target.id)}."
            }
        }
    }

    // v1.58: settings search — filters sections by section name,
    // setting titles, and keywords. Empty query shows everything.
    var settingsQuery by remember { mutableStateOf("") }
    val settingsQueryWords = remember(settingsQuery) {
        settingsQuery.trim().lowercase().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
    }
    fun sectionVisible(title: String): Boolean {
        if (settingsQueryWords.isEmpty()) return true
        val hay = (title + " " +
            (SETTINGS_SEARCH_KEYWORDS[title] ?: "")).lowercase()
        return settingsQueryWords.all { w -> hay.contains(w) }
    }
    val anySectionVisible = SETTINGS_SECTION_TITLES.any { sectionVisible(it) }

    // v1.59: settings-screen UI state — category/section collapse (persisted),
    // pinned settings, recently-changed settings. Own SharedPreferences file;
    // DatabaseManager is untouched.
    val uiState = remember { SettingsUiState(context.applicationContext) }
    val listState = rememberLazyListState()

    // v1.59: category-level search — a query matches a category when it
    // matches the category name or any of its sections.
    fun categoryVisible(cat: SettingsCategory): Boolean {
        if (settingsQueryWords.isEmpty()) return true
        if (settingsQueryWords.all { cat.name.lowercase().contains(it) }) return true
        return cat.sections.any { sectionVisible(it) }
    }
    // While searching, everything is force-expanded so matches are visible.
    val forceExpand = settingsQueryWords.isNotEmpty()

    /**
     * v1.59: item keys of the LazyColumn below, in emission order. Must stay
     * in sync with the LazyColumn content — it drives scroll-to-setting.
     */
    fun visibleKeys(): List<String> = buildList {
        if (!anySectionVisible) {
            add("row:nomatch")
            return@buildList
        }
        if (uiState.recent.isNotEmpty()) add("row:recent")
        if (uiState.pinned.isNotEmpty()) add("row:pinned")
        SETTINGS_CATEGORIES.forEach { cat ->
            if (!categoryVisible(cat)) return@forEach
            add("cat:${cat.name}")
            val catNameMatches = settingsQueryWords.isNotEmpty() &&
                settingsQueryWords.all { cat.name.lowercase().contains(it) }
            if (!(forceExpand || uiState.isExpanded("cat", cat.name))) return@forEach
            cat.sections.forEach { s ->
                if (!sectionVisible(s) && !catNameMatches) return@forEach
                add("section:$s")
                if (forceExpand || uiState.isExpanded("section", s)) {
                    add("content:$s")
                }
            }
        }
    }

    /**
     * v1.59: jump to a pinned / recently-changed setting — expand its
     * category and section, then scroll to the section header.
     */
    fun jumpToSetting(entry: SettingsUiState.Entry) {
        // Clear the search so a filtered-out target can't stay hidden.
        settingsQuery = ""
        scope.launch {
            uiState.setExpanded("cat", entry.category, true)
            uiState.setExpanded("section", entry.section, true)
            // Wait for the expand to recompose, then scroll to the header.
            var idx = -1
            for (i in 0 until 10) {
                withFrameNanos { }
                idx = visibleKeys().indexOf("section:${entry.section}")
                if (idx >= 0) break
            }
            if (idx >= 0) listState.animateScrollToItem(idx)
        }
    }

    CompositionLocalProvider(LocalSettingsUiState provides uiState) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = settingsQuery,
            onValueChange = { settingsQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search settings…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (settingsQuery.isNotEmpty()) {
                    IconButton(onClick = { settingsQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp)
        )
    LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        flingBehavior = rememberCappedFlingBehavior()) {
        if (!anySectionVisible) {
            item(key = "row:nomatch") {
                Text(
                    text = "No settings match \"" + settingsQuery.trim() + "\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            // v1.59: last-touched settings; tap a row to jump to it.
            if (uiState.recent.isNotEmpty()) {
                item(key = "row:recent") {
                    RecentlyChangedRow(
                        entries = uiState.recent,
                        onJump = ::jumpToSetting
                    )
                }
            }
            // v1.59: long-press any toggle (or section header) to pin it here.
            if (uiState.pinned.isNotEmpty()) {
                item(key = "row:pinned") {
                    PinnedRow(
                        entries = uiState.pinned,
                        onJump = ::jumpToSetting,
                        onUnpin = uiState::unpin
                    )
                }
            }
            // v1.59 settings category: Appearance.
            run {
                val cat = SETTINGS_CATEGORIES.first { it.name == "Appearance" }
                if (categoryVisible(cat)) {
                    val catNameMatches = settingsQueryWords.isNotEmpty() &&
                        settingsQueryWords.all { cat.name.lowercase().contains(it) }
                    val catOpen = forceExpand || uiState.isExpanded("cat", cat.name)
                    item(key = "cat:${cat.name}") {
                        SettingsCategoryHeader(
                            name = cat.name,
                            expanded = catOpen,
                            onToggle = { uiState.toggleExpanded("cat", cat.name) }
                        )
                    }
                    if (catOpen) {
                        if (sectionVisible("Display") || catNameMatches) {
                            item(key = "section:Display") {
                                InSettingsSection(cat.name, "Display") {
                                    SettingsSectionHeader(text = "Display")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Display")) {
                                item(key = "content:Display") {
                                    InSettingsSection(cat.name, "Display") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(8.dp)) {
                                                    SettingToggle(
                                                        title = "Show quote numbers",
                                                        subtitle = "Display #number on quote cards",
                                                        checked = showNumber,
                                                        onCheckedChange = vm::setShowQuoteNumber
                                                    )
                                                    SettingToggle(
                                                        title = "Show ratings",
                                                        subtitle = "Display rating badges on quote cards",
                                                        checked = showRating,
                                                        onCheckedChange = vm::setShowRating
                                                    )
                                                    SettingToggle(
                                                        title = "Show web links",
                                                        subtitle = "Display permalink URLs on detail view",
                                                        checked = showUrl,
                                                        onCheckedChange = vm::setShowUrl
                                                    )
                                                    TextSizeSetting(
                                                        scale = textScale,
                                                        onScaleChange = vm::setTextScale
                                                    )
                                                    SettingToggle(
                                                        title = "Shake to shuffle",
                                                        subtitle = "Shake the phone on the Random tab for new quotes",
                                                        checked = shakeToShuffle,
                                                        onCheckedChange = vm::setShakeToShuffle
                                                    )
                                                    DensitySetting(
                                                        density = density,
                                                        onDensityChange = vm::setDensity
                                                    )
                                                    // v1.88.19: blank breathing room between quote
                                                    // cards in list views (Off = today's look).
                                                    BreakBetweenQuotesSetting(
                                                        current = breakBetweenQuotes,
                                                        onSelect = DisplayPrefs::setBreak
                                                    )
                                                    QuoteFontSetting(
                                                        value = quoteFont,
                                                        onValueChange = vm::setQuoteFont
                                                    )
                                                    // v1.50: choose which screen the app opens on.
                                                    StartupScreenSettingRow(
                                                        current = startupScreen,
                                                        options = StartupDestinations.all.map { it.key to it.label },
                                                        onSelect = vm::setStartupScreen
                                                    )
                                                    SettingToggle(
                                                        title = "IRC line separators",
                                                        subtitle = "Turn \\ separators into line breaks in quotes",
                                                        checked = ircLineBreaks,
                                                        onCheckedChange = vm::setIrcLineBreaks
                                                    )
                                                    // v1.53 main-theme option: black page outside the
                                                    // bordered site quotes (works with any global theme).
                                                    SettingToggle(
                                                        title = "Black background outside quotes",
                                                        subtitle = "Site-styled quote cards sit on a black " +
                                                            "page instead of the theme background.",
                                                        checked = blackOutsideOn,
                                                        onCheckedChange = vm::setBlackOutsideQuotes
                                                    )
                                                    SettingToggle(
                                                        title = "Dialogue line breaks",
                                                        subtitle = "Start a new line when another <nick> speaks after quoted text",
                                                        checked = dialogueLineBreaks,
                                                        onCheckedChange = vm::setDialogueLineBreaks
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // v1.85: quote layouts — arrangement of the card's
                        // elements, independent of theme colors. The picked
                        // layout changes on-screen cards AND HTML export.
                        if (sectionVisible("Quote layouts") || catNameMatches) {
                            item(key = "section:Quote layouts") {
                                InSettingsSection(cat.name, "Quote layouts") {
                                    SettingsSectionHeader(text = "Quote layouts")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Quote layouts")) {
                                item(key = "content:Quote layouts") {
                                    InSettingsSection(cat.name, "Quote layouts") {
                                        QuoteLayoutsSettingsContent(
                                            themeDbs = themeDbs,
                                            onLayoutEditorClick = onLayoutEditorClick
                                        )
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Database themes") || catNameMatches) {
                            item(key = "section:Database themes") {
                                InSettingsSection(cat.name, "Database themes") {
                                    SettingsSectionHeader(text = "Database themes")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Database themes")) {
                                item(key = "content:Database themes") {
                                    InSettingsSection(cat.name, "Database themes") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(4.dp)) {
                                                    SettingToggle(
                                                        title = "Master theme override",
                                                        subtitle = "Use the global theme everywhere and " +
                                                            "ignore per-database themes.",
                                                        checked = masterOverride,
                                                        onCheckedChange = vm::setMasterThemeOverride
                                                    )
                                                    // v1.59: database themes only on quote cards (default ON
                                                    // since v1.61.2 — the full-chrome DB palette made
                                                    // Settings unreadable against the DevXen art).
                                                    val dbThemesCardsOnly by vm.dbThemesCardsOnly.collectAsStateWithLifecycle()
                                                    SettingToggle(
                                                        title = "Database themes only on quote cards",
                                                        subtitle = "App chrome stays on your system theme; " +
                                                            "database themes style quote cards only.",
                                                        checked = dbThemesCardsOnly,
                                                        onCheckedChange = vm::setDbThemesCardsOnly
                                                    )
                                                    TextButton(
                                                        onClick = onPreviewThemesClick,
                                                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                                                    ) {
                                                        Text("Preview themes")
                                                    }
                                                    if (!perDbThemesOn) {
                                                        Text(
                                                            text = "Per-database themes are off (see " +
                                                                "Features above); every database uses the " +
                                                                "global theme.",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        // One row per real database: enable toggle, theme picker, site
                                        // colors toggle, layout picker, and approved/queued content mode.
                                        themeDbs.forEach { db ->
                                            DbThemeRow(
                                                db = db,
                                                config = dbThemeConfigs[db.id]
                                                    ?: com.quotebrowser.app.data.DatabaseThemeConfig(),
                                                customThemes = customThemes,
                                                onConfigChange = { vm.setDbThemeConfig(db.id, it) },
                                                onPickBackgroundImage = { vm.setDbPageBackgroundImage(db.id, it) },
                                                onClearBackgroundImage = { vm.clearDbPageBackgroundImage(db.id) }
                                            )
                                        }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Wallpaper") || catNameMatches) {
                            item(key = "section:Wallpaper") {
                                InSettingsSection(cat.name, "Wallpaper") {
                                    SettingsSectionHeader(text = "Wallpaper")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Wallpaper")) {
                                item(key = "content:Wallpaper") {
                                    InSettingsSection(cat.name, "Wallpaper") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(16.dp)) {
                                                    SettingToggle(
                                                        title = "Daily quote wallpaper",
                                                        subtitle = "Set a fresh quote card as your wallpaper each day",
                                                        checked = wallpaperDaily,
                                                        onCheckedChange = vm::setWallpaperDailyEnabled
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        FilterChip(
                                                            selected = wallpaperSource == "favorites",
                                                            onClick = { vm.setWallpaperSource("favorites") },
                                                            label = { Text("Favorites") }
                                                        )
                                                        FilterChip(
                                                            selected = wallpaperSource == "random",
                                                            onClick = { vm.setWallpaperSource("random") },
                                                            label = { Text("Random") }
                                                        )
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        TextButton(
                                                            onClick = {
                                                                wallpaperStatus = "Setting wallpaper…"
                                                                vm.applyWallpaperNow { wallpaperStatus = it }
                                                            }
                                                        ) {
                                                            Text("Set wallpaper now")
                                                        }
                                                        Spacer(Modifier.weight(1f))
                                                    }
                                                    if (wallpaperStatus != null) {
                                                        Text(
                                                            text = wallpaperStatus!!,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Widget") || catNameMatches) {
                            item(key = "section:Widget") {
                                InSettingsSection(cat.name, "Widget") {
                                    SettingsSectionHeader(text = "Widget")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Widget")) {
                                item(key = "content:Widget") {
                                    InSettingsSection(cat.name, "Widget") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                WidgetCategorySetting(
                                                    categories = widgetCategoryNames,
                                                    selected = widgetCategory,
                                                    onSelect = vm::setWidgetCategory
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
            // v1.59 settings category: Reading.
            run {
                val cat = SETTINGS_CATEGORIES.first { it.name == "Reading" }
                if (categoryVisible(cat)) {
                    val catNameMatches = settingsQueryWords.isNotEmpty() &&
                        settingsQueryWords.all { cat.name.lowercase().contains(it) }
                    val catOpen = forceExpand || uiState.isExpanded("cat", cat.name)
                    item(key = "cat:${cat.name}") {
                        SettingsCategoryHeader(
                            name = cat.name,
                            expanded = catOpen,
                            onToggle = { uiState.toggleExpanded("cat", cat.name) }
                        )
                    }
                    if (catOpen) {
                        if (sectionVisible("Quote text") || catNameMatches) {
                            item(key = "section:Quote text") {
                                InSettingsSection(cat.name, "Quote text") {
                                    SettingsSectionHeader(text = "Quote text")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Quote text")) {
                                item(key = "content:Quote text") {
                                    InSettingsSection(cat.name, "Quote text") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(8.dp)) {
                                                    // v1.86: every quote-text category gets its own
                                                    // on/off toggle plus a visual color picker.
                                                    SettingToggle(
                                                        title = "Color speaker names",
                                                        subtitle = "<nick>, <@nick>, Nick:, [nick] and nick |",
                                                        checked = qcColorNames,
                                                        onCheckedChange = vm::setQcColorNames
                                                    )
                                                    DbThemeColorSlot(
                                                        label = "Speaker name color",
                                                        description = "Blank = the theme's name colors",
                                                        hex = qcNameColor ?: "",
                                                        enabled = qcColorNames,
                                                        onPick = { vm.setQcNameColor(it) },
                                                        onReset = { vm.setQcNameColor(null) }
                                                    )
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                    SettingToggle(
                                                        title = "Color message text",
                                                        subtitle = "The words each speaker says",
                                                        checked = qcColorMessage,
                                                        onCheckedChange = vm::setQcColorMessage
                                                    )
                                                    DbThemeColorSlot(
                                                        label = "Message text color",
                                                        description = "Blank = each speaker's name color",
                                                        hex = qcMessageColor ?: "",
                                                        enabled = qcColorMessage,
                                                        onPick = { vm.setQcMessageColor(it) },
                                                        onReset = { vm.setQcMessageColor(null) }
                                                    )
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                    SettingToggle(
                                                        title = "Color action lines",
                                                        subtitle = "Full-line *actions* and join/part notices",
                                                        checked = qcColorActions,
                                                        onCheckedChange = vm::setQcColorActions
                                                    )
                                                    DbThemeColorSlot(
                                                        label = "Action line color",
                                                        description = "Blank = the theme's action color",
                                                        hex = qcActionColor ?: "",
                                                        enabled = qcColorActions,
                                                        onPick = { vm.setQcActionColor(it) },
                                                        onReset = { vm.setQcActionColor(null) }
                                                    )
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                    SettingToggle(
                                                        title = "Show timestamps",
                                                        subtitle = "Keep [HH:MM] prefixes; off strips them",
                                                        checked = qcShowTimestamps,
                                                        onCheckedChange = vm::setQcShowTimestamps
                                                    )
                                                    DbThemeColorSlot(
                                                        label = "Timestamp color",
                                                        description = "Blank = normal text color",
                                                        hex = qcTimestampColor ?: "",
                                                        enabled = qcShowTimestamps,
                                                        onPick = { vm.setQcTimestampColor(it) },
                                                        onReset = { vm.setQcTimestampColor(null) }
                                                    )
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                    SettingToggle(
                                                        title = "Color other lines",
                                                        subtitle = "Narration, notices and anything that isn't speech or an action",
                                                        checked = qcColorOther,
                                                        onCheckedChange = vm::setQcColorOther
                                                    )
                                                    DbThemeColorSlot(
                                                        label = "Other lines color",
                                                        description = "Blank = normal text color",
                                                        hex = qcOtherColor ?: "",
                                                        enabled = qcColorOther,
                                                        onPick = { vm.setQcOtherColor(it) },
                                                        onReset = { vm.setQcOtherColor(null) }
                                                    )
                                                    HorizontalDivider(
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        color = MaterialTheme.colorScheme.outlineVariant
                                                    )
                                                    SettingToggle(
                                                        title = "Normalize / line breaks",
                                                        subtitle = "Split quote lines on / separators used as turn breaks",
                                                        checked = qcNormalizeBreaks,
                                                        onCheckedChange = vm::setQcNormalizeBreaks
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Quote viewer") || catNameMatches) {
                            item(key = "section:Quote viewer") {
                                InSettingsSection(cat.name, "Quote viewer") {
                                    SettingsSectionHeader(text = "Quote viewer")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Quote viewer")) {
                                item(key = "content:Quote viewer") {
                                    InSettingsSection(cat.name, "Quote viewer") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(16.dp)) {
                                                    Text("Swipe through", style = MaterialTheme.typography.bodyLarge)
                                                    Text(
                                                        "Which quotes the left/right swipe on a quote walks through",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        FilterChip(
                                                            selected = !detailSwipeAllDbs,
                                                            onClick = { vm.setDetailSwipeAllDbs(false) },
                                                            label = { Text("This database") }
                                                        )
                                                        FilterChip(
                                                            selected = detailSwipeAllDbs,
                                                            onClick = { vm.setDetailSwipeAllDbs(true) },
                                                            label = { Text("All databases") }
                                                        )
                                                    }
                                                    Spacer(Modifier.height(12.dp))
                                                    Text("Swipe order", style = MaterialTheme.typography.bodyLarge)
                                                    Text(
                                                        "Random jumps to a random quote; in order walks by quote number",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        FilterChip(
                                                            selected = detailSwipeOrder == "random",
                                                            onClick = { vm.setDetailSwipeOrder("random") },
                                                            label = { Text("Random") }
                                                        )
                                                        FilterChip(
                                                            selected = detailSwipeOrder == "ordered",
                                                            onClick = { vm.setDetailSwipeOrder("ordered") },
                                                            label = { Text("In order") }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Read aloud") || catNameMatches) {
                            item(key = "section:Read aloud") {
                                InSettingsSection(cat.name, "Read aloud") {
                                    SettingsSectionHeader(text = "Read aloud")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Read aloud")) {
                                item(key = "content:Read aloud") {
                                    InSettingsSection(cat.name, "Read aloud") {
                                        TtsEngineSettingsCard(
                                            speakerVoices = perNickVoices,
                                            onSpeakerVoicesChange = vm::setTtsPerNickVoices,
                                            onManageSpeakerVoices = onSpeakerVoicesClick
                                        )
                                    }
                                }
                            }
                        }
                        // v1.61.20: saved voice recordings.
                        if (sectionVisible("Voice recordings") || catNameMatches) {
                            item(key = "section:Voice recordings") {
                                InSettingsSection(cat.name, "Voice recordings") {
                                    SettingsSectionHeader(text = "Voice recordings")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Voice recordings")) {
                                item(key = "content:Voice recordings") {
                                    InSettingsSection(cat.name, "Voice recordings") {
                                        VoiceRecordingsSettingsCard(
                                            onManageClick = onVoiceRecordingsClick
                                        )
                                    }
                                }
                            }
                        }
                        // v1.61: quote translations.
                        if (sectionVisible("Translations") || catNameMatches) {
                            item(key = "section:Translations") {
                                InSettingsSection(cat.name, "Translations") {
                                    SettingsSectionHeader(text = "Translations")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Translations")) {
                                item(key = "content:Translations") {
                                    InSettingsSection(cat.name, "Translations") {
                                        TranslationSettingsCard(
                                            repo = vm.translationRepo,
                                            databases = databases
                                        )
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Reading goal") || catNameMatches) {                            item(key = "section:Reading goal") {
                                InSettingsSection(cat.name, "Reading goal") {
                                    SettingsSectionHeader(text = "Reading goal")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Reading goal")) {
                                item(key = "content:Reading goal") {
                                    InSettingsSection(cat.name, "Reading goal") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(16.dp)) {
                                                    Text(
                                                        "Daily reading goal (quotes per day). 0 turns it off.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    var goalText by remember(dailyGoal) { mutableStateOf(dailyGoal.toString()) }
                                                    OutlinedTextField(
                                                        value = goalText,
                                                        onValueChange = { input ->
                                                            goalText = input.filter { it.isDigit() }.take(4)
                                                            goalText.toIntOrNull()?.let { vm.setDailyGoal(it) }
                                                        },
                                                        label = { Text("Quotes per day") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    // v1.49: weekly and monthly goals.
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        "Weekly goal (quotes per 7 days) and monthly goal " +
                                                            "(quotes per calendar month). 0 turns a goal off.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    var weeklyText by remember(weeklyGoal) { mutableStateOf(weeklyGoal.toString()) }
                                                    OutlinedTextField(
                                                        value = weeklyText,
                                                        onValueChange = { input ->
                                                            weeklyText = input.filter { it.isDigit() }.take(5)
                                                            weeklyText.toIntOrNull()?.let { vm.setWeeklyGoal(it) }
                                                        },
                                                        label = { Text("Quotes per week") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    var monthlyText by remember(monthlyGoal) { mutableStateOf(monthlyGoal.toString()) }
                                                    OutlinedTextField(
                                                        value = monthlyText,
                                                        onValueChange = { input ->
                                                            monthlyText = input.filter { it.isDigit() }.take(6)
                                                            monthlyText.toIntOrNull()?.let { vm.setMonthlyGoal(it) }
                                                        },
                                                        label = { Text("Quotes per month") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        // v1.50 vacation mode (reading streak freeze).
                                        if (vacationMode) {
                                                VacationModeSetting(
                                                    enabled = vacationEnabled,
                                                    startDay = vacationStartDay,
                                                    endDay = vacationEndDay,
                                                    onEnabledChange = vm::setVacationEnabled,
                                                    onRangeChange = vm::setVacationRange
                                                )
                                        }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Reading stats") || catNameMatches) {
                            item(key = "section:Reading stats") {
                                InSettingsSection(cat.name, "Reading stats") {
                                    SettingsSectionHeader(text = "Reading stats")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Reading stats")) {
                                item(key = "content:Reading stats") {
                                    InSettingsSection(cat.name, "Reading stats") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(
                                                    Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        "Quotes you've opened, per-database progress, and your reading streak.",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Button(onClick = onStatsClick) {
                                                        Text("View reading stats")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // v1.65 global content maturity ceiling: caps what every
                        // screen can show, no matter what its own filters say.
                        if (sectionVisible("Content maturity") || catNameMatches) {
                            item(key = "section:Content maturity") {
                                InSettingsSection(cat.name, "Content maturity") {
                                    SettingsSectionHeader(text = "Content maturity")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Content maturity")) {
                                item(key = "content:Content maturity") {
                                    InSettingsSection(cat.name, "Content maturity") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(12.dp)) {
                                                    Text(
                                                        "The maximum maturity shown anywhere in the app — " +
                                                            "Browse, Search, Random, Radio, the widget, games, " +
                                                            "and the quote of the day. Per-screen maturity " +
                                                            "filters can only narrow this down, never widen it.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    MaturityCeilingSetting(
                                                        selected = maturityCeiling,
                                                        onSelect = { vm.setMaturityCeiling(it) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Quote pings") || catNameMatches) {
                            item(key = "section:Quote pings") {
                                InSettingsSection(cat.name, "Quote pings") {
                                    SettingsSectionHeader(text = "Quote pings")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Quote pings")) {
                                item(key = "content:Quote pings") {
                                    InSettingsSection(cat.name, "Quote pings") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(12.dp)) {
                                                    Text(
                                                        "Get a random quote notification on a schedule.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            "Quote pings",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Switch(
                                                            checked = quotePingEnabled,
                                                            onCheckedChange = { enabled ->
                                                                if (enabled) {
                                                                    val needsPermission =
                                                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                                                            ContextCompat.checkSelfPermission(
                                                                                context,
                                                                                Manifest.permission.POST_NOTIFICATIONS
                                                                            ) != PackageManager.PERMISSION_GRANTED
                                                                    if (needsPermission) {
                                                                        pingPermissionLauncher.launch(
                                                                            Manifest.permission.POST_NOTIFICATIONS
                                                                        )
                                                                    } else {
                                                                        vm.setQuotePingEnabled(true)
                                                                    }
                                                                } else {
                                                                    vm.setQuotePingEnabled(false)
                                                                }
                                                            }
                                                        )
                                                    }
                                                    if (quotePingEnabled) {
                                                        Spacer(Modifier.height(4.dp))
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.horizontalScroll(rememberScrollState())
                                                        ) {
                                                            vm.quotePingChoices.forEach { minutes ->
                                                                FilterChip(
                                                                    selected = quotePingMinutes == minutes,
                                                                    onClick = { vm.setQuotePingMinutes(minutes) },
                                                                    label = { Text(vm.quotePingLabel(minutes)) }
                                                                )
                                                            }
                                                        }
                                                        if (quotePingMinutes == 1440L || quotePingMinutes == 10080L) {
                                                            Spacer(Modifier.height(8.dp))
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Text(
                                                                    "Time",
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                                TextButton(onClick = { pingTimeDialog.show() }) {
                                                                    Text(pingTimeLabel)
                                                                }
                                                            }
                                                        }
                                                        if (quotePingMinutes == 10080L) {
                                                            Spacer(Modifier.height(4.dp))
                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                modifier = Modifier.horizontalScroll(rememberScrollState())
                                                            ) {
                                                                (1..7).forEach { day ->
                                                                    val label = DayOfWeek.of(day).getDisplayName(
                                                                        TextStyle.SHORT, Locale.getDefault()
                                                                    )
                                                                    FilterChip(
                                                                        selected = quotePingDay == day,
                                                                        onClick = { vm.setQuotePingDay(day) },
                                                                        label = { Text(label) }
                                                                    )
                                                                }
                                                            }
                                                        }
                                                        Spacer(Modifier.height(8.dp))
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                "Databases",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            val pingDbCount = quotePingDbIds?.size
                                                            TextButton(onClick = { showPingDbDialog = true }) {
                                                                Text(if (pingDbCount == null) "All" else "$pingDbCount selected")
                                                            }
                                                        }
                                                        // v1.74: notification-quote filters. These shape
                                                        // which quotes the pings (and their Next button)
                                                        // may show.
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            "Notification filters",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                "Maturity",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            TextButton(onClick = { showPingMaturityDialog = true }) {
                                                                Text(
                                                                    if (quotePingUseGlobalCeiling) "Same as app"
                                                                    else quotePingCustomCeiling.label
                                                                )
                                                            }
                                                        }
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                "Work hours",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            Switch(
                                                                checked = quotePingWorkHoursEnabled,
                                                                onCheckedChange = { vm.setQuotePingWorkHoursEnabled(it) }
                                                            )
                                                        }
                                                        if (quotePingWorkHoursEnabled) {
                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                modifier = Modifier.horizontalScroll(rememberScrollState())
                                                            ) {
                                                                (1..7).forEach { day ->
                                                                    val label = DayOfWeek.of(day).getDisplayName(
                                                                        TextStyle.SHORT, Locale.getDefault()
                                                                    )
                                                                    FilterChip(
                                                                        selected = day in quotePingWorkDays,
                                                                        onClick = { vm.toggleQuotePingWorkDay(day) },
                                                                        label = { Text(label) }
                                                                    )
                                                                }
                                                            }
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Text(
                                                                    "Workday",
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                                TextButton(onClick = { workStartDialog.show() }) {
                                                                    Text(workStartLabel)
                                                                }
                                                                Text(
                                                                    "–",
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                                TextButton(onClick = { workEndDialog.show() }) {
                                                                    Text(workEndLabel)
                                                                }
                                                            }
                                                            Text(
                                                                "Overnight shifts are fine (e.g. 10 PM – 6 AM).",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Text(
                                                                    "At work, max maturity",
                                                                    style = MaterialTheme.typography.bodyLarge,
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                                TextButton(onClick = { showPingWorkCeilingDialog = true }) {
                                                                    Text(quotePingWorkCeiling.label)
                                                                }
                                                            }
                                                        }
                                                        Text(
                                                            "Moods",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.horizontalScroll(rememberScrollState())
                                                        ) {
                                                            MOOD_PRESETS.forEach { preset ->
                                                                val active = preset.categories.all {
                                                                    it.lowercase() in pingCategoryLower
                                                                }
                                                                FilterChip(
                                                                    selected = active,
                                                                    onClick = {
                                                                        vm.setQuotePingCategoryNames(
                                                                            togglePingMoodNames(
                                                                                quotePingCategoryNames,
                                                                                preset
                                                                            )
                                                                        )
                                                                    },
                                                                    label = { Text("${preset.emoji} ${preset.label}") }
                                                                )
                                                            }
                                                        }
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.fillMaxWidth()
                                                        ) {
                                                            Text(
                                                                "Categories",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                            TextButton(onClick = { showPingCategoryDialog = true }) {
                                                                Text(
                                                                    if (quotePingCategoryNames.isEmpty()) "All"
                                                                    else "${quotePingCategoryNames.size} selected"
                                                                )
                                                            }
                                                        }
                                                        RatingFilterRow(
                                                            selected = quotePingMinRating,
                                                            onSelect = { vm.setQuotePingMinRating(it) },
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                        Text(
                                                            "Don't repeat quotes notified within",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier.horizontalScroll(rememberScrollState())
                                                        ) {
                                                            vm.quotePingRecencyChoices.forEach { days ->
                                                                FilterChip(
                                                                    selected = quotePingRecencyDays == days,
                                                                    onClick = { vm.setQuotePingRecencyDays(days) },
                                                                    label = { Text(vm.quotePingRecencyLabel(days)) }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if (showPingDbDialog) {
                                                DatabaseMultiSelectDialog(
                                                    title = "Ping sources",
                                                    databases = databases,
                                                    selected = quotePingDbIds,
                                                    onConfirm = { vm.setQuotePingDbIds(it) },
                                                    onDismiss = { showPingDbDialog = false }
                                                )
                                            }
                                            // v1.74: ping maturity choice (inherit global vs custom).
                                            if (showPingMaturityDialog) {
                                                PingMaturityDialog(
                                                    useGlobal = quotePingUseGlobalCeiling,
                                                    globalLabel = maturityCeiling.label,
                                                    custom = quotePingCustomCeiling,
                                                    onUseGlobal = {
                                                        vm.setQuotePingUseGlobalCeiling(true)
                                                        showPingMaturityDialog = false
                                                    },
                                                    onCustom = {
                                                        vm.setQuotePingUseGlobalCeiling(false)
                                                        vm.setQuotePingCustomCeiling(it)
                                                        showPingMaturityDialog = false
                                                    },
                                                    onDismiss = { showPingMaturityDialog = false }
                                                )
                                            }
                                            // v1.74: work-hours maturity ceiling.
                                            if (showPingWorkCeilingDialog) {
                                                AccentAlertDialog(
                                                    onDismissRequest = { showPingWorkCeilingDialog = false },
                                                    confirmButton = {
                                                        TextButton(onClick = { showPingWorkCeilingDialog = false }) {
                                                            Text("Done")
                                                        }
                                                    },
                                                    title = { Text("At work, max maturity") },
                                                    text = {
                                                        Column(Modifier.verticalScroll(rememberScrollState())) {
                                                            Text(
                                                                "While you're at work, pings use this cap instead of the one above.",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Spacer(Modifier.height(8.dp))
                                                            MaturityCeilingSetting(
                                                                selected = quotePingWorkCeiling,
                                                                onSelect = {
                                                                    vm.setQuotePingWorkCeiling(it)
                                                                    showPingWorkCeilingDialog = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                            // v1.74: ping category picker.
                                            if (showPingCategoryDialog) {
                                                PingCategoryDialog(
                                                    vm = vm,
                                                    dbIds = quotePingDbIds,
                                                    selected = quotePingCategoryNames,
                                                    onConfirm = {
                                                        vm.setQuotePingCategoryNames(it)
                                                        showPingCategoryDialog = false
                                                    },
                                                    onDismiss = { showPingCategoryDialog = false }
                                                )
                                            }
                                        // v1.50 daily quote reminder.
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Daily reminder") || catNameMatches) {
                            item(key = "section:Daily reminder") {
                                InSettingsSection(cat.name, "Daily reminder") {
                                    SettingsSectionHeader(text = "Daily reminder")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Daily reminder")) {
                                item(key = "content:Daily reminder") {
                                    InSettingsSection(cat.name, "Daily reminder") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            DailyReminderSetting(
                                                enabled = dailyReminderEnabled,
                                                hour = dailyReminderHour,
                                                minute = dailyReminderMinute,
                                                onEnabledChange = { vm.setDailyReminderEnabled(it) },
                                                onTimeChange = { h, m -> vm.setDailyReminderTime(h, m) },
                                                // v1.59: Quote-of-the-Day source ("random_sources" | "favorites").
                                                qotdSource = qotdSource,
                                                onQotdSourceChange = vm::setQotdSource
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Quote alarm") || catNameMatches) {
                            item(key = "section:Quote alarm") {
                                InSettingsSection(cat.name, "Quote alarm") {
                                    SettingsSectionHeader(text = "Quote alarm")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Quote alarm")) {
                                item(key = "content:Quote alarm") {
                                    InSettingsSection(cat.name, "Quote alarm") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            QuoteAlarmSetting(
                                                enabled = quoteAlarmEnabled,
                                                hour = quoteAlarmHour,
                                                minute = quoteAlarmMinute,
                                                onEnabledChange = { vm.setQuoteAlarmEnabled(it) },
                                                onTimeChange = { h, m -> vm.setQuoteAlarmTime(h, m) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Swipe actions") || catNameMatches) {
                            item(key = "section:Swipe actions") {
                                InSettingsSection(cat.name, "Swipe actions") {
                                    SettingsSectionHeader(text = "Swipe actions")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Swipe actions")) {
                                item(key = "content:Swipe actions") {
                                    InSettingsSection(cat.name, "Swipe actions") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
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
                                                        "Choose what happens when you swipe a quote row left or right.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    SettingToggle(
                                                        title = "Swipe actions on Home",
                                                        subtitle = "Allow left/right swipes on home-page quotes",
                                                        checked = swipeHomeEnabled,
                                                        onCheckedChange = vm::setSwipeHomeEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Swipe actions on Random",
                                                        subtitle = "Allow left/right swipes on random quotes",
                                                        checked = swipeRandomEnabled,
                                                        onCheckedChange = vm::setSwipeRandomEnabled
                                                    )
                                                    SwipeActionPicker(
                                                        label = "Swipe left",
                                                        selected = swipeLeft,
                                                        onSelect = { vm.setSwipeAction(true, it) }
                                                    )
                                                    SwipeActionPicker(
                                                        label = "Swipe right",
                                                        selected = swipeRight,
                                                        onSelect = { vm.setSwipeAction(false, it) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Quote length presets") || catNameMatches) {
                            item(key = "section:Quote length presets") {
                                InSettingsSection(cat.name, "Quote length presets") {
                                    SettingsSectionHeader(text = "Quote length presets")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Quote length presets")) {
                                item(key = "content:Quote length presets") {
                                    InSettingsSection(cat.name, "Quote length presets") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(16.dp)) {
                                                    Text(
                                                        "Word counts for the Length filter presets on Browse and Search.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    val longerWords by vm.lengthLongerWords.collectAsStateWithLifecycle()
                                                    val shorterWords by vm.lengthShorterWords.collectAsStateWithLifecycle()
                                                    var longerText by remember(longerWords) { mutableStateOf(longerWords.toString()) }
                                                    var shorterText by remember(shorterWords) { mutableStateOf(shorterWords.toString()) }
                                                    OutlinedTextField(
                                                        value = longerText,
                                                        onValueChange = { input ->
                                                            longerText = input.filter { it.isDigit() }.take(4)
                                                            longerText.toIntOrNull()?.let { vm.setLengthLongerWords(it) }
                                                        },
                                                        label = { Text("“Longer” means at least (words)") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    OutlinedTextField(
                                                        value = shorterText,
                                                        onValueChange = { input ->
                                                            shorterText = input.filter { it.isDigit() }.take(4)
                                                            shorterText.toIntOrNull()?.let { vm.setLengthShorterWords(it) }
                                                        },
                                                        label = { Text("“Shorter” means at most (words)") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.fillMaxWidth()
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
            // v1.59 settings category: Your data.
            run {
                val cat = SETTINGS_CATEGORIES.first { it.name == "Your data" }
                if (categoryVisible(cat)) {
                    val catNameMatches = settingsQueryWords.isNotEmpty() &&
                        settingsQueryWords.all { cat.name.lowercase().contains(it) }
                    val catOpen = forceExpand || uiState.isExpanded("cat", cat.name)
                    item(key = "cat:${cat.name}") {
                        SettingsCategoryHeader(
                            name = cat.name,
                            expanded = catOpen,
                            onToggle = { uiState.toggleExpanded("cat", cat.name) }
                        )
                    }
                    if (catOpen) {
                        if (sectionVisible("My Quotes") || catNameMatches) {
                            item(key = "section:My Quotes") {
                                InSettingsSection(cat.name, "My Quotes") {
                                    SettingsSectionHeader(text = "My Quotes")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "My Quotes")) {
                                item(key = "content:My Quotes") {
                                    InSettingsSection(cat.name, "My Quotes") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(
                                                    Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        "Quotes you add yourself live in their own database, " +
                                                            "included in Random and Quote of the Day.",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    if (myQuotesOn) {
                                                        Button(onClick = onMyQuotesClick) {
                                                            Text("Manage My Quotes")
                                                        }
                                                    } else {
                                                        Text(
                                                            "My Quotes is turned off (Settings → Features).",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    // v1.88.36: user-created databases.
                                                    Button(onClick = onMyDatabasesClick) {
                                                        Text("My databases")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Backup & restore") || catNameMatches) {
                            item(key = "section:Backup & restore") {
                                InSettingsSection(cat.name, "Backup & restore") {
                                    SettingsSectionHeader(text = "Backup & restore")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Backup & restore")) {
                                item(key = "content:Backup & restore") {
                                    InSettingsSection(cat.name, "Backup & restore") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(
                                                    Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        "Full backup, restore, automatic backups",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Button(onClick = onBackupRestore) {
                                                        Text("Open backup & restore")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Auto backup") || catNameMatches) {
                            item(key = "section:Auto backup") {
                                InSettingsSection(cat.name, "Auto backup") {
                                    SettingsSectionHeader(text = "Auto backup")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Auto backup")) {
                                item(key = "content:Auto backup") {
                                    InSettingsSection(cat.name, "Auto backup") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(12.dp)) {
                                                    Text(
                                                        "Automatically back up all databases to a folder you choose.",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        listOf("off" to "Off", "daily" to "Daily", "weekly" to "Weekly")
                                                            .forEach { (value, label) ->
                                                                FilterChip(
                                                                    selected = autoBackupFreq == value,
                                                                    onClick = { vm.setAutoBackupFreq(value) },
                                                                    label = { Text(label) }
                                                                )
                                                            }
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Column(Modifier.weight(1f)) {
                                                            Text(
                                                                "Backup folder",
                                                                style = MaterialTheme.typography.bodyLarge
                                                            )
                                                            Text(
                                                                backupFolderName ?: "Not chosen",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        TextButton(onClick = { treeLauncher.launch(null) }) {
                                                            Text("Choose folder")
                                                        }
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        TextButton(
                                                            onClick = {
                                                                backupStatus = "Backing up…"
                                                                vm.runBackupNow { backupStatus = it }
                                                            },
                                                            enabled = autoBackupTree != null
                                                        ) {
                                                            Text("Back up now")
                                                        }
                                                        Spacer(Modifier.weight(1f))
                                                    }
                                                    val status = backupStatus ?: lastAutoBackup
                                                    if (status != null) {
                                                        Text(
                                                            text = status,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Databases") || catNameMatches) {
                            item(key = "section:Databases") {
                                InSettingsSection(cat.name, "Databases") {
                                    SettingsSectionHeader(text = "Databases")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Databases")) {
                                item(key = "content:Databases") {
                                    InSettingsSection(cat.name, "Databases") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(Modifier.padding(8.dp)) {
                                                    // v1.55: "Show collection dates" removed — dates no
                                                    // longer appear in the switcher (see the per-database
                                                    // info panel instead).
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "Database order",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Button(onClick = onDbOrderClick) {
                                                            Text("Reorder")
                                                        }
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "Duplicate quotes",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        // v1.49: the duplicate finder never runs while its
                                                        // feature toggle is off.
                                                        Button(
                                                            onClick = onDuplicatesClick,
                                                            enabled = dupMergeEnabled
                                                        ) {
                                                            Text("Find duplicates")
                                                        }
                                                    }
                                                    // v1.59: integrity check for every database.
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "Verify databases",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Button(onClick = onVerifyDatabasesClick) {
                                                            Text("Verify")
                                                        }
                                                    }
                                                    // v1.83: plugin packs (quote packs, theme packs).
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "Plugins",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Button(onClick = onPluginsClick) {
                                                            Text("Manage")
                                                        }
                                                    }
                                                }
                                            }
                                            Text(
                                                "Back up a database to a file, or share it " +
                                                    "(e.g. email it to yourself) to add it to the app later.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        backupDbs.forEach { db ->
                                            DatabaseBackupRow(
                                                db = db,
                                                vm = vm,
                                                onExport = {
                                                    exportTarget = db
                                                    exportLauncher.launch(vm.exportFileName(db.id))
                                                },
                                                onShare = {
                                                    scope.launch {
                                                        val file = vm.stagedShareFile(db.id)
                                                        if (file == null) {
                                                            dbStatus = "Couldn't prepare ${db.label} for sharing."
                                                            return@launch
                                                        }
                                                        val uri = FileProvider.getUriForFile(
                                                            context, FILE_PROVIDER_AUTHORITY, file
                                                        )
                                                        val send = android.content.Intent(
                                                            android.content.Intent.ACTION_SEND
                                                        ).apply {
                                                            type = "application/octet-stream"
                                                            putExtra(
                                                                android.content.Intent.EXTRA_STREAM, uri
                                                            )
                                                            putExtra(
                                                                android.content.Intent.EXTRA_SUBJECT,
                                                                "Quote database: ${db.label}"
                                                            )
                                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        }
                                                        context.startActivity(
                                                            android.content.Intent.createChooser(
                                                                send, "Share ${db.label}"
                                                            )
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                        if (dbStatus != null) {
                                                Text(
                                                    text = dbStatus!!,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                        }
                                        }
                                    }
                                }
                            }
                        }
                        // v1.60: download-based database updates.
                        if (sectionVisible("Database updates") || catNameMatches) {
                            item(key = "section:Database updates") {
                                InSettingsSection(cat.name, "Database updates") {
                                    SettingsSectionHeader(text = "Database updates")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Database updates")) {
                                item(key = "content:Database updates") {
                                    InSettingsSection(cat.name, "Database updates") {
                                        DatabaseUpdateSection(vm = vm)
                                    }
                                }
                            }
                        }
                        if (sectionVisible("Features") || catNameMatches) {
                            item(key = "section:Features") {
                                InSettingsSection(cat.name, "Features") {
                                    SettingsSectionHeader(text = "Features")
                                }
                            }
                            if (forceExpand || uiState.isExpanded("section", "Features")) {
                                item(key = "content:Features") {
                                    InSettingsSection(cat.name, "Features") {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                                )
                                            ) {
                                                Column(
                                                    Modifier.padding(4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                                ) {
                                                    SettingToggle(
                                                        title = "Random without repeats",
                                                        subtitle = "Random skips quotes already shown until every quote " +
                                                            "has appeared, then the cycle starts over.",
                                                        checked = randomNoRepeat,
                                                        onCheckedChange = vm::setRandomNoRepeat
                                                    )
                                                    SettingToggle(
                                                        title = "Personal notes",
                                                        subtitle = "Write private notes on quotes, listed on the Notes " +
                                                            "screen and searchable.",
                                                        checked = notesEnabled,
                                                        onCheckedChange = vm::setNotesEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Slideshow video export",
                                                        subtitle = "Export a category or the Top 100 as an MP4 " +
                                                            "slideshow, with optional narration.",
                                                        checked = slideshowExportEnabled,
                                                        onCheckedChange = vm::setSlideshowExportEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Reading goals",
                                                        subtitle = "Weekly and monthly reading goals with progress " +
                                                            "on the Stats screen.",
                                                        checked = readingGoalsEnabled,
                                                        onCheckedChange = vm::setReadingGoalsEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Nick timeline",
                                                        subtitle = "Chronological timeline of a nick's quotes on " +
                                                            "their page.",
                                                        checked = nickTimelineEnabled,
                                                        onCheckedChange = vm::setNickTimelineEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Tap gestures",
                                                        subtitle = "Double-tap a quote card or the quote body to " +
                                                            "favorite; two-finger tap the quote body to share.",
                                                        checked = gesturesEnabled,
                                                        onCheckedChange = vm::setGesturesEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Duplicate merge",
                                                        subtitle = "Cross-database duplicate finder and one-tap " +
                                                            "merge (keeps the best copy, hides the rest).",
                                                        checked = dupMergeEnabled,
                                                        onCheckedChange = vm::setDupMergeEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Select all in bulk mode",
                                                        subtitle = "Show the Select all / Deselect all button in " +
                                                            "the Search and Favorites bulk-selection bar.",
                                                        checked = bulkSelectAll,
                                                        onCheckedChange = vm::setBulkSelectAll
                                                    )
                                                    // v1.50 feature toggles (all default ON).
                                                    SettingToggle(
                                                        title = "Quote battle",
                                                        subtitle = "Head-to-head game: tap the funnier quote to " +
                                                            "climb the Elo ladder.",
                                                        checked = quoteBattleOn,
                                                        onCheckedChange = vm::setQuoteBattleEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Vacation mode",
                                                        subtitle = "Freeze your reading streak while you're away " +
                                                            "— vacation days don't count for or against it.",
                                                        checked = vacationMode,
                                                        onCheckedChange = vm::setVacationMode
                                                    )
                                                    SettingToggle(
                                                        title = "Personal ratings",
                                                        subtitle = "Rate quotes with 1–5 stars; your taste boosts " +
                                                            "“More like this”.",
                                                        checked = personalRatingsOn,
                                                        onCheckedChange = vm::setPersonalRatingsEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Pinch to zoom quote text",
                                                        subtitle = "Two-finger pinch on the quote detail screen " +
                                                            "scales the text (0.8x–2.5x).",
                                                        checked = pinchZoomOn,
                                                        onCheckedChange = vm::setPinchZoomEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Remember browse position",
                                                        subtitle = "Browse picks up where you left off in each " +
                                                            "database.",
                                                        checked = browsePositionMemory,
                                                        onCheckedChange = vm::setBrowsePositionMemory
                                                    )
                                                    SettingToggle(
                                                        title = "Shareable stats card",
                                                        subtitle = "Render your reading stats as an image and " +
                                                            "share it.",
                                                        checked = statsCardShare,
                                                        onCheckedChange = vm::setStatsCardShare
                                                    )
                                                    SettingToggle(
                                                        title = "Extra widgets",
                                                        subtitle = "Random Quote and Reading Streak home-screen " +
                                                            "widgets.",
                                                        checked = extraWidgets,
                                                        onCheckedChange = vm::setExtraWidgets
                                                    )
                                                    // v1.51 feature toggles (all default ON).
                                                    SettingToggle(
                                                        title = "Per-database themes",
                                                        subtitle = "Each quote database renders in its own " +
                                                            "site theme; All-databases mode themes each " +
                                                            "card by its source database.",
                                                        checked = perDbThemesOn,
                                                        onCheckedChange = vm::setPerDbThemesEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Classic QDB layout option",
                                                        subtitle = "Allow the Classic QDB card layout " +
                                                            "(quote number + score top bar, monospace " +
                                                            "body, info bar) per database.",
                                                        checked = classicLayoutOn,
                                                        onCheckedChange = vm::setClassicLayoutOption
                                                    )
                                                    SettingToggle(
                                                        title = "+/- vote controls",
                                                        subtitle = "Thumbs up/down on quote cards adjust " +
                                                            "your personal rating only — nothing is sent " +
                                                            "anywhere.",
                                                        checked = voteControlsOn,
                                                        onCheckedChange = vm::setPlusMinusRatingsEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Queued tags",
                                                        subtitle = "Show a “Queued” tag on quotes that were " +
                                                            "submitted but not yet approved for the archive.",
                                                        checked = queuedTagsOn,
                                                        onCheckedChange = vm::setQueuedTagsEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Approved/Queued content filter",
                                                        subtitle = "Lets each database show approved quotes, " +
                                                            "queued quotes, or both.",
                                                        checked = statusFilterOn,
                                                        onCheckedChange = vm::setStatusFilterEnabled
                                                    )
                                                    // v1.53 feature toggles (all default ON except Kid-safe).
                                                    SettingToggle(
                                                        title = "My Quotes database",
                                                        subtitle = "Your personal quote database: add, edit, " +
                                                            "and organize your own quotes alongside the " +
                                                            "archives.",
                                                        checked = myQuotesOn,
                                                        onCheckedChange = vm::setMyQuotesEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Per-database widgets",
                                                        subtitle = "The random-quote widget can pull from " +
                                                            "one database or all of them.",
                                                        checked = perDbWidgetOn,
                                                        onCheckedChange = vm::setPerDbWidgetEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Memorize mode",
                                                        subtitle = "Flashcards from your favorites or a " +
                                                            "category: reveal the text, score Got it / " +
                                                            "Missed it, see session stats.",
                                                        checked = memorizeOn,
                                                        onCheckedChange = vm::setMemorizeModeEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "On this day",
                                                        subtitle = "Resurface quotes posted on today's " +
                                                            "date in past years, plus quotes you read " +
                                                            "or favorited on this date.",
                                                        checked = onThisDayOn,
                                                        onCheckedChange = vm::setOnThisDayEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Kid-safe mask",
                                                        subtitle = "Masks profanity everywhere quotes are " +
                                                            "shown (display only — stored text is never " +
                                                            "changed). Off by default.",
                                                        checked = kidSafeOn,
                                                        onCheckedChange = vm::setKidSafeEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Site-styled share cards",
                                                        subtitle = "Offer the database's site theme as an " +
                                                            "image style when sharing a quote, next to the " +
                                                            "plain card.",
                                                        checked = siteShareCardsOn,
                                                        onCheckedChange = vm::setSiteShareCardsEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Explain this quote",
                                                        subtitle = "Long-press menu entry that asks Gemini " +
                                                            "to explain a quote's references and in-jokes " +
                                                            "(uses your stored API key).",
                                                        checked = explainOn,
                                                        onCheckedChange = vm::setExplainQuoteEnabled
                                                    )
                                                    SettingToggle(
                                                        title = "Ambient mode",
                                                        subtitle = "Fullscreen rotating quotes; tap to " +
                                                            "pause/advance, keeps the screen awake.",
                                                        checked = ambientOn,
                                                        onCheckedChange = vm::setAmbientModeEnabled
                                                    )
                                                    if (ambientOn) {
                                                        AmbientIntervalRow(
                                                            seconds = ambientInterval,
                                                            onChange = vm::setAmbientIntervalSec
                                                        )
                                                    }
                                                    SettingToggle(
                                                        title = "Report / flag",
                                                        subtitle = "Flag bad quotes from the quote " +
                                                            "menus with a reason. Flags stay on this " +
                                                            "device and are never sent anywhere.",
                                                        checked = flagQuotesOn,
                                                        onCheckedChange = vm::setFlagQuotesEnabled
                                                    )
                                                    if (flagQuotesOn) {
                                                        SettingToggle(
                                                            title = "Hide flagged quotes",
                                                            subtitle = "Keep flagged quotes out of Browse, " +
                                                                "Search, Random, Favorites, History and " +
                                                                "Top/Bottom. Off by default.",
                                                            checked = hideFlaggedOn,
                                                            onCheckedChange = vm::setHideFlaggedQuotes
                                                        )
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable { showFlaggedReview = true }
                                                                .padding(12.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(Modifier.weight(1f)) {
                                                                Text(
                                                                    "Flagged quotes",
                                                                    style = MaterialTheme.typography.bodyLarge
                                                                )
                                                                Text(
                                                                    if (flaggedQuotes.isEmpty())
                                                                        "Nothing flagged yet."
                                                                    else
                                                                        "${flaggedQuotes.size} flagged — review " +
                                                                            "reasons, unflag, or clear all.",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                            Icon(
                                                                imageVector = Icons.Filled.ChevronRight,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                    TransformModeSelector(
                                                        selected = transformMode,
                                                        onSelect = vm::setTransformMode
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
    }

    }
    }

    // v1.53 report/flag: review the flagged quotes.
    if (showFlaggedReview) {
        FlaggedQuotesDialog(
            vm = vm,
            dbLabels = remember(databases) {
                databases.associate { it.id to it.label }
            },
            onDismiss = { showFlaggedReview = false }
        )
    }
}

/**
 * v1.53 report/flag: review the flagged quotes. Each entry shows the
 * database, quote number, a preview of the text, the chosen reason plus
 * any custom note, and an Unflag button. "Clear all" removes every flag
 * after confirmation. Flags live only on this device — nothing here is
 * sent anywhere.
 */
@Composable
private fun FlaggedQuotesDialog(
    vm: MainViewModel,
    dbLabels: Map<String, String>,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var entries by remember { mutableStateOf<List<FlaggedSourcedQuote>?>(null) }
    var confirmClear by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            entries = withContext(Dispatchers.IO) { vm.flaggedQuoteDetails() }
        }
    }
    LaunchedEffect(Unit) { reload() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Flagged quotes") },
        text = {
            val list = entries
            when {
                list == null -> {
                    Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                list.isEmpty() -> {
                    Text(
                        "Nothing flagged yet. Flag a quote from the " +
                            "quote menu (⋯) on any list, or from the flag " +
                            "button on a quote's detail screen."
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        flingBehavior = rememberCappedFlingBehavior()) {
                        items(list, key = { "${it.dbId}/${it.quote.number}" }) { fq ->
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${dbLabels[fq.dbId] ?: fq.dbId} #${fq.quote.number}",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(
                                        onClick = {
                                            vm.unflagQuote(fq.dbId, fq.quote.number)
                                            reload()
                                        }
                                    ) {
                                        Text("Unflag")
                                    }
                                }
                                Text(
                                    text = fq.quote.text.take(140),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = buildString {
                                        append("Reason: ${fq.reason}")
                                        if (fq.customText.isNotBlank()) {
                                            append(" — ${fq.customText}")
                                        }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (entries?.isNotEmpty() == true) {
                TextButton(onClick = { confirmClear = true }) {
                    Text("Clear all")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all flags?") },
            text = {
                Text(
                    "This removes all ${entries?.size ?: 0} flags on this " +
                        "device. This can't be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        vm.clearFlaggedQuotes()
                        reload()
                    }
                ) {
                    Text("Clear all")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/** 0xFFRRGGBB -> "RRGGBB". */
fun Long.toRgbHex(): String =
    this.toString(16).takeLast(6).uppercase()

@Composable
private fun DatabaseBackupRow(
    db: DbInfo,
    vm: MainViewModel,
    onExport: () -> Unit,
    onShare: () -> Unit
) {
    var count by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(db.id) {
        count = try {
            vm.databaseQuoteCount(db.id)
        } catch (e: Exception) {
            null
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = db.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = count?.let { "$it quotes" } ?: "…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onExport, modifier = Modifier.weight(1f)) {
                    Text("Back up")
                }
                OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                    Text("Share / email")
                }
            }
        }
    }
}

/**
 * v1.60: download-based database updates. The user pastes a manifest URL
 * (JSON listing .db files with revision + SHA-256); "Check for updates"
 * downloads and merges anything newer, preserving user data. Downloads
 * are verified before anything is touched.
 */
@Composable
private fun DatabaseUpdateSection(vm: MainViewModel) {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf(vm.getDbUpdateUrl()) }
    var checking by remember { mutableStateOf(false) }
    var results by remember {
        mutableStateOf<List<DatabaseManager.DbUpdateOutcome>?>(null)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Pull newer quote databases without waiting for an app update. " +
                    "Downloads are verified and merged in place — your categories, " +
                    "favorites, ratings, and notes are kept.",
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Update list URL") },
                placeholder = { Text("https://example.com/quotebrowser/updates.json") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
            Button(
                onClick = {
                    vm.setDbUpdateUrl(url)
                    checking = true
                    results = null
                    scope.launch {
                        try {
                            results = vm.checkForDatabaseUpdates()
                        } finally {
                            checking = false
                        }
                    }
                },
                enabled = !checking && url.trim().isNotEmpty()
            ) {
                Text(if (checking) "Checking…" else "Check for updates")
            }
            results?.forEach { outcome ->
                val color = when (outcome.status) {
                    DatabaseManager.DbUpdateOutcome.UpdateStatus.UPDATED ->
                        MaterialTheme.colorScheme.primary
                    DatabaseManager.DbUpdateOutcome.UpdateStatus.FAILED ->
                        MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val title = outcome.label.ifEmpty { "Update" }
                Text(
                    text = "$title: ${outcome.detail}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun TextSizeSetting(
    scale: Float,
    onScaleChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Quote text size", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${(scale * 100).toInt()}% — applies to quote text everywhere",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value = scale,
            onValueChange = onScaleChange,
            valueRange = 0.8f..1.5f,
            steps = 6
        )
    }
}

/** Comfortable vs compact quote-list density. */
@Composable
private fun DensitySetting(
    density: String,
    onDensityChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text("List density", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "Compact fits more quotes on screen",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = density != "compact",
                onClick = { onDensityChange("comfortable") },
                label = { Text("Comfortable") }
            )
            FilterChip(
                selected = density == "compact",
                onClick = { onDensityChange("compact") },
                label = { Text("Compact") }
            )
        }
    }
}

/**
 * v1.88.22: "Break between quotes" — blank lines between the turns/lines
 * of a quote body ("the menu option for the carriage returns"). Styled like
 * the other Display rows (title + current choice subtitle); tapping opens a
 * single-select radio dialog with Off/Small/Medium/Large. Off renders
 * stored newlines exactly as-is; Small/Medium/Large insert 1/2/3 blank
 * lines between turns. Stored in DisplayPrefs (Small by default, preserving
 * the v1.88.21 look on update).
 */
@Composable
private fun BreakBetweenQuotesSetting(
    current: String,
    onSelect: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentLabel = DisplayPrefs.BREAK_LABELS[current] ?: "Off"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
            .padding(12.dp)
    ) {
        Text("Break between quotes", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = currentLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Break between quotes") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    flingBehavior = rememberCappedFlingBehavior()) {
                    items(DisplayPrefs.BREAK_OPTIONS) { value ->
                        val label = DisplayPrefs.BREAK_LABELS[value] ?: value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(value)
                                    showDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value == current,
                                onClick = {
                                    onSelect(value)
                                    showDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Quote-body font: Default, Serif, Monospace, or a .ttf/.otf picked through
 * the Storage Access Framework (persisted URI, loaded on demand).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuoteFontSetting(
    value: String,
    onValueChange: (String) -> Unit
) {
    val context = LocalContext.current
    val fontPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onValueChange(uri.toString())
                Toast.makeText(context, "Font applied", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(
                    context, "Couldn't load that font", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    val isCustom = value != "default" && value != "serif" &&
        value != "monospace"
    // Display name of the loaded custom font file (null when none loaded).
    val customFontName = remember(value) {
        if (!isCustom) null else runCatching {
            context.contentResolver.query(Uri.parse(value), null, null, null, null)
                ?.use { cursor ->
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
                }
        }.getOrNull()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text("Quote font", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = "Applies to quote text in lists and detail",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        // Chips wrap on narrow screens (e.g. Fold folded) instead of
        // squeezing into a vertical-text column.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = value == "default",
                onClick = { onValueChange("default") },
                label = { Text("Default") }
            )
            FilterChip(
                selected = value == "serif",
                onClick = { onValueChange("serif") },
                label = { Text("Serif") }
            )
            FilterChip(
                selected = value == "monospace",
                onClick = { onValueChange("monospace") },
                label = { Text("Monospace") }
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { fontPicker.launch(arrayOf("font/*")) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
        ) {
            Text(
                text = if (!isCustom) "Load custom font…"
                    else if (customFontName != null) "Custom font: $customFontName — tap to change"
                    else "Custom font — tap to change",
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * v1.59: section headers are tappable to collapse/expand their section
 * (the state persists in SettingsUiState) and long-pressable to pin the
 * whole section to the Pinned row. Outside SettingsScreen (no provider)
 * it renders as the plain pre-v1.59 header.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsSectionHeader(text: String) {
    val uiState = LocalSettingsUiState.current
    val location = LocalSettingsLocation.current
    val context = LocalContext.current
    val interactive = uiState != null && location.section.isNotEmpty()
    val expanded = if (interactive) uiState!!.isExpanded("section", text) else true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (interactive) Modifier.combinedClickable(
                    onClick = { uiState!!.toggleExpanded("section", text) },
                    onLongClick = {
                        val nowPinned = uiState!!.togglePin(
                            "section:$text", text, text, location.category,
                            isSection = true
                        )
                        Toast.makeText(
                            context,
                            if (nowPinned) "Section pinned — find it in the Pinned row at the top"
                            else "Section unpinned",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) else Modifier
            )
            .padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        if (interactive) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess
                else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Collapse section" else "Expand section",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Home-screen widget category filter: which category the widget's random
 * quotes come from (null = all categories). Categories are listed from the
 * current database; the widget matches by name in whichever database it
 * draws from.
 */
@Composable
private fun WidgetCategorySetting(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Widget category", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Limit the home-screen widget's quotes to one category",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selected ?: "All", maxLines = 1)
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "Choose widget category"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("All categories") },
                    onClick = { expanded = false; onSelect(null) }
                )
                categories.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { expanded = false; onSelect(name) }
                    )
                }
            }
        }
    }
}

/**
 * v1.59: long-press a toggle to pin it to the Pinned row at the top of
 * Settings; every change is recorded in the Recently-changed row. Both
 * are no-ops when rendered outside SettingsScreen (no provider) — then
 * the toggle behaves exactly as before.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val uiState = LocalSettingsUiState.current
    val location = LocalSettingsLocation.current
    val context = LocalContext.current
    // Only track/pin inside SettingsScreen's categorized sections.
    val tracked = uiState != null && location.section.isNotEmpty()
    val id = "toggle:$title"
    fun recordChange() {
        if (tracked) uiState!!.markChanged(id, title, location.section, location.category)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { recordChange(); onCheckedChange(!checked) },
                onLongClick = if (tracked) ({
                    val nowPinned = uiState!!.togglePin(
                        id, title, location.section, location.category
                    )
                    Toast.makeText(
                        context,
                        if (nowPinned) "Pinned — find it in the Pinned row at the top"
                        else "Unpinned",
                        Toast.LENGTH_SHORT
                    ).show()
                }) else null
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { recordChange(); onCheckedChange(it) }
        )
    }
}

/**
 * v1.52: single mutually-exclusive selector for the display-only text
 * transformation (Off / 1337 / Upside-down / aLtErNaTiNg), defaulting to
 * Off. Replaces v1.51's three stacking toggles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransformModeSelector(
    selected: TransformMode,
    onSelect: (TransformMode) -> Unit
) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Text display transform",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Novelty text effect for quotes " +
                "(display only, never changes saved text).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            TransformMode.entries.forEachIndexed { i, mode ->
                SegmentedButton(
                    selected = selected == mode,
                    onClick = { onSelect(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = i,
                        count = TransformMode.entries.size
                    )
                ) {
                    Text(mode.label, maxLines = 1)
                }
            }
        }
    }
}

/**
 * v1.53: ambient-mode rotation interval (5–120 seconds).
 */
@Composable
fun AmbientIntervalRow(
    seconds: Int,
    onChange: (Int) -> Unit
) {
    Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(
            text = "Ambient rotation: $seconds seconds",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "How long each quote stays on screen in ambient mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = seconds.toFloat(),
            onValueChange = { onChange(it.roundToInt().coerceIn(5, 120)) },
            valueRange = 5f..120f,
            steps = 114,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Manages the offensive-words list for the content filter: an add field
 * plus one removable row per word. Words match whole words,
 * case-insensitively.
 */
/**
 * v1.65 global content maturity ceiling: a radio list of the eight
 * maturity filter levels. Picking a ceiling caps every screen's results;
 * per-screen maturity filters AND with this and can only narrow it.
 */
@Composable
fun MaturityCeilingSetting(
    selected: MaturityLevel,
    onSelect: (MaturityLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        MaturityLevel.entries.forEach { level ->
            val isSelected = level == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(level) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelect(level) }
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        level.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        level.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun OffensiveWordsSetting(
    words: List<String>,
    onAdd: (String) -> Boolean,
    onRemove: (String) -> Unit
) {
    var newWord by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var duplicateNote by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    fun submit() {
        val added = onAdd(newWord)
        duplicateNote = !added && newWord.isNotBlank()
        if (added) newWord = ""
    }

    Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(
            text = "Quotes containing these words are hidden everywhere except your own My Quotes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newWord,
                onValueChange = { newWord = it; duplicateNote = false },
                label = { Text("Add a word") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { submit() }
                )
            )
            Spacer(Modifier.width(8.dp))
            androidx.compose.material3.FilledTonalIconButton(onClick = ::submit) {
                Icon(Icons.Filled.Add, contentDescription = "Add word")
            }
        }
        if (duplicateNote) {
            Text(
                text = "That word is already on the list.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        if (words.isEmpty()) {
            Text(
                text = "No words yet — nothing is hidden.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            words.forEach { word ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = word,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onRemove(word) }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Remove $word",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/** Dropdown for picking a quote-row swipe action. */
@Composable
private fun SwipeActionPicker(
    label: String,
    selected: com.quotebrowser.app.ui.components.SwipeAction,
    onSelect: (com.quotebrowser.app.ui.components.SwipeAction) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Box {
            TextButton(onClick = { expanded = true }) {
                Text(selected.label)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Change $label action")
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                com.quotebrowser.app.ui.components.SwipeAction.values().forEach { action ->
                    DropdownMenuItem(
                        text = { Text(action.label) },
                        onClick = {
                            expanded = false
                            onSelect(action)
                        }
                    )
                }
            }
        }
    }
}

/**
 * v1.51: one database's theme settings — enable toggle, theme picker
 * dropdown, "Site colors" toggle, layout segmented control
 * (Modern|Classic QDB|Site, default Site), and the approved/queued
 * content mode.
 */
/**
 * v1.56: one optional per-database color slot — a swatch button opening the
 * HSV picker, plus Reset (back to the site default) when a value is set.
 * [hex] is "" when unset.
 */
@Composable
private fun DbThemeColorSlot(
    label: String,
    description: String,
    hex: String,
    enabled: Boolean,
    onPick: (String) -> Unit,
    onReset: () -> Unit
) {
    var pickerOpen by remember { mutableStateOf(false) }
    val current = remember(hex) { hexToColorOrNull(hex) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (current != null) {
            TextButton(onClick = onReset, enabled = enabled) { Text("Reset") }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(current ?: MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable(enabled = enabled) { pickerOpen = true }
        )
    }
    if (pickerOpen) {
        ColorPickerDialog(
            initial = current ?: Color.Gray,
            onDismiss = { pickerOpen = false },
            onPick = { color ->
                onPick(colorToHex(color))
                pickerOpen = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DbThemeRow(
    db: DbInfo,
    config: DatabaseThemeConfig,
    customThemes: List<CustomTheme>,
    onConfigChange: (DatabaseThemeConfig) -> Unit,
    onPickBackgroundImage: (Uri) -> Unit,
    onClearBackgroundImage: () -> Unit
) {
    var themeMenuOpen by remember { mutableStateOf(false) }
    val siteThemeId = siteThemeFor(db.id)
    val siteThemeName =
        ThemePresets.find { it.id == siteThemeId }?.name ?: "Site default"
    // v1.83: plugin theme packs available as per-database palette themes.
    val pluginThemeOptions by rememberPluginThemeOptions()
    // v1.88.23: grouped into labeled sections (shared with the quick
    // theme dialog).
    val themeOptions = remember(customThemes, pluginThemeOptions, siteThemeName) {
        groupedDbThemeOptions(
            "Site default ($siteThemeName)", customThemes, pluginThemeOptions
        )
    }
    val selectedThemeName = themeOptions
        .filterIsInstance<ThemeOption.Item>()
        .find { it.id == config.themeId }?.name
        ?: "Site default ($siteThemeName)"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = db.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = config.themeEnabled,
                    onCheckedChange = { onConfigChange(config.copy(themeEnabled = it)) }
                )
            }
            ExposedDropdownMenuBox(
                expanded = themeMenuOpen,
                onExpandedChange = { if (config.themeEnabled) themeMenuOpen = it }
            ) {
                OutlinedTextField(
                    value = selectedThemeName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Theme") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeMenuOpen)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    enabled = config.themeEnabled
                )
                ExposedDropdownMenu(
                    expanded = themeMenuOpen,
                    onDismissRequest = { themeMenuOpen = false }
                ) {
                    // v1.88.23: labeled section headers (disabled) between
                    // the grouped theme options.
                    themeOptions.forEach { opt ->
                        when (opt) {
                            is ThemeOption.Header -> DropdownMenuItem(
                                text = {
                                    Text(
                                        opt.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            is ThemeOption.Item -> DropdownMenuItem(
                                text = { Text(opt.name) },
                                onClick = {
                                    onConfigChange(config.copy(themeId = opt.id))
                                    themeMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Site colors",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = config.siteColors,
                    onCheckedChange = { onConfigChange(config.copy(siteColors = it)) },
                    enabled = config.themeEnabled
                )
            }
            // v1.53: optional centered site logo in the quote header bar.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Centered header logo",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Show the site logo in the middle of the quote header bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.centeredHeaderLogo,
                    onCheckedChange = { onConfigChange(config.copy(centeredHeaderLogo = it)) },
                    enabled = config.themeEnabled
                )
            }
            // v1.61.11: optional hide-TLD in the quote header bar title.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Short header title",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Show just the site name in the header bar (\"Bash\" instead of \"Bash.org\")",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.hideTldInHeader,
                    onCheckedChange = { onConfigChange(config.copy(hideTldInHeader = it)) },
                    enabled = config.themeEnabled
                )
            }
            Text(
                text = "Layout",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                SiteLayout.entries.forEachIndexed { i, layout ->
                    SegmentedButton(
                        selected = config.layout == layout,
                        onClick = { onConfigChange(config.copy(layout = layout)) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = i,
                            count = SiteLayout.entries.size
                        ),
                        enabled = config.themeEnabled
                    ) {
                        Text(layout.label, maxLines = 1)
                    }
                }
            }
            Text(
                text = "Content",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                StatusMode.entries.forEachIndexed { i, mode ->
                    SegmentedButton(
                        selected = config.statusMode == mode,
                        onClick = { onConfigChange(config.copy(statusMode = mode)) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = i,
                            count = StatusMode.entries.size
                        ),
                        enabled = config.themeEnabled
                    ) {
                        Text(mode.label, maxLines = 1)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            // v1.56: per-database quote/page background overrides. The
            // defaults shown are the site defaults (what Reset restores);
            // blank = site default. The image is drawn behind the page,
            // dimmed, on single-database Browse/Random/detail.
            val snap = LocalDbThemeSnapshots.current
            val resolvedForDefaults = remember(snap, db.id) { resolveDbTheme(snap, db.id) }
            val defaultQuoteBgHex = remember(resolvedForDefaults, snap) {
                colorToHex(
                    quoteBackgroundFor(
                        resolvedForDefaults.paletteThemeId,
                        snap.customThemes,
                        snap.builtInOverrides
                    )
                )
            }
            val defaultPageBgHex = remember(resolvedForDefaults, snap) {
                effectivePreset(
                    resolvedForDefaults.paletteThemeId,
                    snap.builtInOverrides
                )?.background?.let { colorToHex(Color(it)) } ?: "—"
            }
            DbThemeColorSlot(
                label = "Quote background",
                description = "Card body behind the quote text. " +
                    "Default: #$defaultQuoteBgHex (site default).",
                hex = config.quoteBackgroundHex,
                enabled = config.themeEnabled,
                onPick = { onConfigChange(config.copy(quoteBackgroundHex = it)) },
                onReset = { onConfigChange(config.copy(quoteBackgroundHex = "")) }
            )
            DbThemeColorSlot(
                label = "Page background",
                description = "Page behind the quote cards. " +
                    "Default: #$defaultPageBgHex (site default).",
                hex = config.pageBackgroundHex,
                enabled = config.themeEnabled,
                onPick = { onConfigChange(config.copy(pageBackgroundHex = it)) },
                onReset = { onConfigChange(config.copy(pageBackgroundHex = "")) }
            )
            val imagePicker = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) onPickBackgroundImage(uri)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Background image",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (config.pageBackgroundImage.isNotBlank())
                            "Image set — shown dimmed behind the page."
                        else "Optional image behind the page on " +
                            "single-database Browse/Random.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (config.pageBackgroundImage.isNotBlank()) {
                    TextButton(
                        onClick = onClearBackgroundImage,
                        enabled = config.themeEnabled
                    ) { Text("Remove") }
                } else {
                    Button(
                        onClick = { imagePicker.launch(arrayOf("image/*")) },
                        enabled = config.themeEnabled
                    ) { Text("Choose") }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/**
 * v1.61: quote translation settings — engine, target language, display
 * mode, per-database auto-translate, and the translation cache.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslationSettingsCard(
    repo: TranslationRepository,
    databases: List<DbInfo>
) {
    val engine by repo.engine.collectAsStateWithLifecycle()
    val targetLang by repo.targetLang.collectAsStateWithLifecycle()
    val displayMode by repo.displayMode.collectAsStateWithLifecycle()
    val rowCount by repo.rowCount.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var clearing by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Translate quotes into your language. Each quote is " +
                    "translated once, saved on your device, and never " +
                    "translated again — even across backups.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Translation engine", style = MaterialTheme.typography.bodyLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(
                    TranslationRepository.ENGINE_AUTO to "Auto",
                    TranslationRepository.ENGINE_GEMINI to "Gemini",
                    TranslationRepository.ENGINE_FALLBACK to "Free"
                ).forEachIndexed { i, (value, label) ->
                    SegmentedButton(
                        selected = engine == value,
                        onClick = { repo.setEngine(value) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = 3)
                    ) { Text(label, maxLines = 1) }
                }
            }
            Text(
                when (engine) {
                    TranslationRepository.ENGINE_GEMINI ->
                        "Gemini only — uses your API key quota."
                    TranslationRepository.ENGINE_FALLBACK ->
                        "Free fallback only — no API key, no quota."
                    else ->
                        "Automatic — Gemini first, free fallback when " +
                            "the quota runs out."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TranslationLanguageRow(
                title = "Translate into",
                subtitle = "Quotes show in this language.",
                selectedCode = targetLang,
                includeAuto = false,
                onSelect = { repo.setTargetLang(it) }
            )

            Text("Show quotes as", style = MaterialTheme.typography.bodyLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(
                    TranslationRepository.DISPLAY_BOTH to "Both",
                    TranslationRepository.DISPLAY_TRANSLATED to "Translated",
                    TranslationRepository.DISPLAY_ORIGINAL to "Original"
                ).forEachIndexed { i, (value, label) ->
                    SegmentedButton(
                        selected = displayMode == value,
                        onClick = { repo.setDisplayMode(value) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = 3)
                    ) { Text(label, maxLines = 1) }
                }
            }
            Text(
                "Translated shows only the translation. The Translated badge " +
                    "on a card flips back to the original on tap; long-press " +
                    "the badge to show both for that quote.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Auto-translate", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Translate quotes automatically as you browse. " +
                    "Warpdrive.se is on by default (Swedish archive).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            databases.filter { it.id != DatabaseManager.ALL_DBS_ID }.forEach { db ->
                PerDbTranslationRow(repo = repo, db = db)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when {
                        rowCount < 0 -> "Counting cached translations…"
                        rowCount == 1 -> "1 translation cached"
                        else -> "$rowCount translations cached"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        clearing = true
                        scope.launch {
                            repo.clearAll()
                            clearing = false
                        }
                    },
                    enabled = rowCount > 0 && !clearing
                ) { Text(if (clearing) "Clearing…" else "Clear") }
            }
        }
    }
}

/** One per-database auto-translate row: label + switch, source language below. */
@Composable
private fun PerDbTranslationRow(
    repo: TranslationRepository,
    db: DbInfo
) {
    val autoOn by repo.autoTranslateFlow(db.id).collectAsStateWithLifecycle()
    val srcLang by repo.sourceLangFlow(db.id).collectAsStateWithLifecycle()
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                db.displayTitle(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = autoOn,
                onCheckedChange = { repo.setAutoTranslate(db.id, it) }
            )
        }
        TranslationLanguageRow(
            title = "From",
            subtitle = "",
            selectedCode = srcLang,
            includeAuto = true,
            compact = true,
            onSelect = { repo.setSourceLang(db.id, it) }
        )
    }
}

/** Title/subtitle on the left, language dropdown on the right. */
@Composable
private fun TranslationLanguageRow(
    title: String,
    subtitle: String,
    selectedCode: String,
    includeAuto: Boolean,
    onSelect: (String) -> Unit,
    compact: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = if (compact) MaterialTheme.typography.bodySmall
                else MaterialTheme.typography.bodyLarge
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(
                    TranslationRepository.languageName(selectedCode),
                    maxLines = 1
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = "Choose language"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (includeAuto) {
                    DropdownMenuItem(
                        text = { Text("Detect") },
                        onClick = { expanded = false; onSelect("auto") }
                    )
                }
                TranslationRepository.LANGUAGES.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = { expanded = false; onSelect(code) }
                    )
                }
            }
        }
    }
}

/**
 * v1.61.20: summary card for saved voice recordings — count, storage, and
 * a jump into the management screen.
 */
@Composable
fun VoiceRecordingsSettingsCard(
    onManageClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { VoiceRecordings.ensure(context) }
    val recordings by VoiceRecordings.recordings.collectAsStateWithLifecycle()
    val totalBytes = remember(recordings) { VoiceRecordings.totalBytes() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Every read-aloud is saved on your device, so replays cost " +
                    "nothing. Changing a speaker's voice generates fresh " +
                    "audio on the next play; the old version is kept as a " +
                    "backup. Included in full backups.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                if (recordings.isEmpty()) "No recordings saved yet."
                else {
                    val mb = totalBytes / (1024.0 * 1024.0)
                    val size = if (mb >= 1) String.format(java.util.Locale.US, "%.1f MB", mb)
                    else "${totalBytes / 1024} KB"
                    "${recordings.size} quote${if (recordings.size == 1) "" else "s"} · $size"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onManageClick) {
                Text("Manage voice recordings")
            }
        }
    }
}

/**
 * v1.74: toggles a mood preset's category names in the ping category set.
 * Matching is case-insensitive; stored names keep their original casing.
 */
private fun togglePingMoodNames(
    current: Set<String>,
    preset: MoodPreset
): Set<String> {
    val lower = current.map { it.lowercase() }.toSet()
    val names = preset.categories.map { it.trim() }.filter { it.isNotEmpty() }
    val byLower = names.associateBy { it.lowercase() }
    return if (names.all { it.lowercase() in lower }) {
        current.filter { it.lowercase() !in byLower }.toSet()
    } else {
        current + names.filter { it.lowercase() !in lower }
    }
}

/**
 * v1.74: ping maturity dialog — follow the app's global ceiling, or pick
 * a notification-specific one.
 */
@Composable
private fun PingMaturityDialog(
    useGlobal: Boolean,
    globalLabel: String,
    custom: MaturityLevel,
    onUseGlobal: () -> Unit,
    onCustom: (MaturityLevel) -> Unit,
    onDismiss: () -> Unit
) {
    AccentAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        },
        title = { Text("Ping maturity") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onUseGlobal() }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = useGlobal,
                        onClick = { onUseGlobal() }
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Same as app",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (useGlobal) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            "Follow the global ceiling (currently $globalLabel).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                MaturityCeilingSetting(
                    selected = custom,
                    onSelect = { onCustom(it) }
                )
            }
        }
    )
}

/**
 * v1.74: ping category picker — the union of category names across the
 * ping source databases. Empty selection = all categories.
 */
@Composable
private fun PingCategoryDialog(
    vm: MainViewModel,
    dbIds: Set<String>?,
    selected: Set<String>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var names by remember { mutableStateOf<List<String>?>(null) }
    var checked by remember { mutableStateOf(selected) }
    LaunchedEffect(dbIds) {
        names = withContext(Dispatchers.IO) {
            val ids = dbIds?.takeIf { it.isNotEmpty() } ?: vm.allPingDbIds().toSet()
            try {
                vm.pingCategoryUniverse(ids)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    val all = names
    AccentAlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(checked) }) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = { checked = emptySet() }) { Text("Clear") }
        },
        title = { Text("Ping categories") },
        text = {
            Column {
                Text(
                    "Only quotes in these categories can ping. " +
                        "Nothing selected = every category.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                if (all == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (all.isEmpty()) {
                    Text(
                        "No categories in the ping databases yet.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        all.forEach { name ->
                            val isChecked = name in checked
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        checked = if (isChecked) checked - name else checked + name
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        checked = if (it) checked + name else checked - name
                                    }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
