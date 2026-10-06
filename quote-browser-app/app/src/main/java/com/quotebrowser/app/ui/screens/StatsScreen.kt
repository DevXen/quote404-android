package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.DbReadingStat
import com.quotebrowser.app.ui.StatsCardData
import com.quotebrowser.app.ui.StatsViewModel
import com.quotebrowser.app.ui.VoiceRecordings
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.components.ScreenHeader
import com.quotebrowser.app.ui.renderAndShareStatsCard
import com.quotebrowser.app.ui.vmFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.quotebrowser.app.ui.rememberCappedFlingBehavior

/** Reading stats: per-database progress, total quotes seen, day streak. */
@Composable
fun StatsScreen(
    manager: DatabaseManager,
    onNickClick: (String) -> Unit = {}
) {
    val vm: StatsViewModel = viewModel(factory = vmFactory { StatsViewModel(manager) })
    val stats by vm.stats.collectAsStateWithLifecycle()
    val totalSeen by vm.totalSeen.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    val longestStreak by vm.longestStreak.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    val readLog by vm.readLog.collectAsStateWithLifecycle()
    val todayCount by vm.todayCount.collectAsStateWithLifecycle()
    val dailyGoal by vm.dailyGoal.collectAsStateWithLifecycle()
    // v1.49 weekly/monthly reading goals.
    val weeklyCount by vm.weeklyCount.collectAsStateWithLifecycle()
    val monthlyCount by vm.monthlyCount.collectAsStateWithLifecycle()
    val weeklyGoal by vm.weeklyGoal.collectAsStateWithLifecycle()
    val monthlyGoal by vm.monthlyGoal.collectAsStateWithLifecycle()
    val readingGoalsEnabled by vm.readingGoalsEnabled.collectAsStateWithLifecycle()
    val ratingDistribution by vm.ratingDistribution.collectAsStateWithLifecycle()
    // v1.64.2: top speakers, top categories, favorites.
    val topNicks by vm.topNicks.collectAsStateWithLifecycle()
    val topCategories by vm.topCategories.collectAsStateWithLifecycle()
    val favoritesCount by vm.favoritesCount.collectAsStateWithLifecycle()
    // v1.64.2: saved voice recordings tile.
    val recordings by VoiceRecordings.recordings.collectAsStateWithLifecycle()
    val voiceRecordingCount = recordings.size
    // v1.50 vacation mode: streaks are frozen while enabled.
    val vacationOn by manager.vacationEnabled.collectAsStateWithLifecycle()
    // v1.50 shareable stats card toggle.
    val statsCardShareOn by manager.statsCardShare.collectAsStateWithLifecycle()

    var showResetConfirm by remember { mutableStateOf(false) }
    var sharingStats by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Reading stats") {
            IconButton(
                onClick = {
                    scope.launch {
                        renderAndShareStatsCard(context, totalSeen, streak, stats, readLog)
                    }
                },
                enabled = totalSeen > 0
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share stats as image")
            }
            TextButton(
                onClick = { showResetConfirm = true },
                enabled = totalSeen > 0
            ) {
                Text("Reset")
            }
        }
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = { Text("Reset reading stats?") },
                text = {
                    Text("This clears your per-quote progress and day streak. This can't be undone.")
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.reset()
                        showResetConfirm = false
                    }) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        if (loading && stats.isEmpty()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Column
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            flingBehavior = rememberCappedFlingBehavior()) {
            // v1.50: subtle banner while vacation mode is freezing streaks.
            if (vacationOn) {
                item {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("Vacation mode is on — streaks are frozen")
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            // Shareable stats card: renders the headline stats as a PNG and
            // opens the system share sheet, gated on the feature toggle.
            if (statsCardShareOn) {
                item {
                    FilledTonalButton(
                    onClick = {
                        if (sharingStats) return@FilledTonalButton
                        sharingStats = true
                        scope.launch {
                            try {
                                val data = withContext(Dispatchers.IO) {
                                    val favCount = manager.allDbIds().sumOf { id ->
                                        runCatching {
                                            manager.repoFor(id).favorites().first().size
                                        }.getOrDefault(0)
                                    }
                                    val top = runCatching {
                                        manager.topNicks(1).firstOrNull()
                                    }.getOrNull()
                                    StatsCardData(
                                        quotesRead = totalSeen.toLong(),
                                        currentStreak = streak,
                                        longestStreak = longestStreak,
                                        favoritesCount = favCount,
                                        topNick = top?.first,
                                        topNickCount = top?.second ?: 0,
                                        weeklyRead = weeklyCount,
                                        weeklyTarget = if (readingGoalsEnabled) weeklyGoal else 0
                                    )
                                }
                                renderAndShareStatsCard(context, data)
                            } finally {
                                sharingStats = false
                            }
                        }
                    },
                    enabled = !sharingStats && totalSeen > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (sharingStats) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Share, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(if (sharingStats) "Rendering\u2026" else "Share stats")
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        value = totalSeen.toString(),
                        label = "quotes read",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = streak.toString(),
                        label = "day streak",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = longestStreak.toString(),
                        label = "longest streak",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // v1.64.2: favorites and saved voice recordings.
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        value = favoritesCount.toString(),
                        label = "favorites",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        value = voiceRecordingCount.toString(),
                        label = "voice recordings",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                StreakMilestonesCard(longestStreak = longestStreak)
            }
            item {
                AchievementsCard(manager = manager)
            }
            if (dailyGoal > 0) {
                item {
                    DailyGoalCard(todayCount = todayCount, goal = dailyGoal)
                }
            }
            // v1.49: weekly/monthly reading goals with progress bars.
            if (readingGoalsEnabled && (weeklyGoal > 0 || monthlyGoal > 0)) {
                item {
                    ReadingGoalsCard(
                        weeklyCount = weeklyCount,
                        weeklyGoal = weeklyGoal,
                        monthlyCount = monthlyCount,
                        monthlyGoal = monthlyGoal
                    )
                }
            }
            item {
                ReadingHeatmapCard(readLog = readLog)
            }
            item {
                RatingHistogramCard(distribution = ratingDistribution)
            }
            // v1.64.2: top speakers and categories across read quotes; the
            // speaker rows open the nick page.
            item {
                TopListCard(
                    title = "Top speakers",
                    entries = topNicks,
                    emptyText = "Read some quotes with dialogue to see your top speakers.",
                    formatLabel = { "<$it>" },
                    onEntryClick = onNickClick
                )
            }
            item {
                TopListCard(
                    title = "Top categories",
                    entries = topCategories,
                    emptyText = "Read some categorized quotes to see your top categories."
                )
            }
            // v1.50 rate-as-you-read: mean personal star rating; hidden
            // until at least one quote is rated.
            item {
                PersonalRatingRow()
            }
            if (stats.isEmpty()) {
                item {
                    Text(
                        "Open a quote to start tracking.\nDetail views and the endless deck count.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                    )
                }
            }
            items(stats, key = { it.dbId }) { stat ->
                DbStatCard(stat)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/**
 * v1.50 rate-as-you-read: "Average personal rating" row, e.g.
 * "4.2 ★ (38 rated)". Hidden when the feature is off or nothing has
 * been rated yet.
 */
@Composable
private fun PersonalRatingRow() {
    val store = com.quotebrowser.app.ui.components.LocalPersonalRatings.current
    val enabled = com.quotebrowser.app.ui.components.LocalPersonalRatingsEnabled.current
    val count = remember(store, enabled) { if (enabled) store?.ratedCount() ?: 0 else 0 }
    if (count == 0) return
    val average = remember(store, count) { store?.averageRating() ?: 0.0 }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Average personal rating",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "%.1f ★ (%d rated)".format(average, count),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Streak milestone badges: 7, 30, 100, and 365-day streaks. */
@Composable
private fun StreakMilestonesCard(longestStreak: Int) {
    val milestones = listOf(7, 30, 100, 365)
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
                "Streak milestones",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                milestones.forEach { days ->
                    val earned = longestStreak >= days
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = if (earned) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (earned)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Column(
                            Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (earned) "★" else "☆",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "$days",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "days",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DbStatCard(stat: DbReadingStat) {
    val fraction = if (stat.total > 0) stat.seen.toFloat() / stat.total else 0f
    val pct = (fraction * 100).toInt()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stat.label,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$pct%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${stat.seen} of ${stat.total} quotes read",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Daily goal progress ring: "X / goal today". */
@Composable
private fun DailyGoalCard(todayCount: Int, goal: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val progress = (todayCount.toFloat() / goal).coerceIn(0f, 1f)
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.padding(8.dp))
            Column {
                Text(
                    text = "$todayCount / $goal today",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (todayCount >= goal) "Goal reached — nice reading!"
                    else "${goal - todayCount} more to hit your daily goal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * v1.49: weekly and monthly reading-goal progress bars ("X / goal this
 * week", "X / goal this month").
 */
@Composable
private fun ReadingGoalsCard(
    weeklyCount: Int,
    weeklyGoal: Int,
    monthlyCount: Int,
    monthlyGoal: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Reading goals",
                style = MaterialTheme.typography.titleSmall
            )
            if (weeklyGoal > 0) {
                GoalProgressRow(
                    label = "This week",
                    count = weeklyCount,
                    goal = weeklyGoal
                )
            }
            if (monthlyGoal > 0) {
                GoalProgressRow(
                    label = "This month",
                    count = monthlyCount,
                    goal = monthlyGoal
                )
            }
        }
    }
}

@Composable
private fun GoalProgressRow(label: String, count: Int, goal: Int) {
    val progress = (count.toFloat() / goal).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                "$count / $goal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (count >= goal) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            if (count >= goal) "Goal reached \u2014 nice reading!"
            else "${goal - count} more to go",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** GitHub-style reading heatmap: last ~20 weeks, day columns, month labels. */
@Composable
private fun ReadingHeatmapCard(readLog: Map<String, Int>) {
    val weeks = 20
    val today = remember { java.time.LocalDate.now() }
    // Sunday of the current week, so rows line up Sun..Sat.
    val thisSunday = today.minusDays((today.dayOfWeek.value % 7).toLong())
    val maxCount = (readLog.values.maxOrNull() ?: 0).coerceAtLeast(1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Reading activity",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                var lastMonth = -1
                for (w in 0 until weeks) {
                    val weekStart = thisSunday.minusWeeks((weeks - 1 - w).toLong())
                    val month = weekStart.monthValue
                    val showMonth = month != lastMonth
                    lastMonth = month
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(14.dp, 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (showMonth) {
                                Text(
                                    text = weekStart.month.getDisplayName(
                                        java.time.format.TextStyle.SHORT,
                                        java.util.Locale.getDefault()
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                        for (r in 0..6) {
                            val day = weekStart.plusDays(r.toLong())
                            val future = day.isAfter(today)
                            val count = if (future) -1 else readLog[day.toString()] ?: 0
                            val color = when {
                                future -> MaterialTheme.colorScheme.surface
                                count <= 0 -> MaterialTheme.colorScheme.surfaceContainerHigh
                                else -> {
                                    val frac = (count.toFloat() / maxCount).coerceIn(0.15f, 1f)
                                    MaterialTheme.colorScheme.primary.copy(alpha = frac)
                                }
                            }
                            Box(
                                modifier = Modifier.size(14.dp)
                                    .background(
                                        color = color,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Less → more",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Horizontal-bar histogram of quote ratings across all databases. */
@Composable
private fun RatingHistogramCard(distribution: List<Pair<String, Int>>) {
    val total = distribution.sumOf { it.second }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Rating distribution",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            if (total == 0) {
                Text(
                    "No rated quotes yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val max = distribution.maxOf { it.second }.coerceAtLeast(1)
                for ((label, count) in distribution) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(44.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(count.toFloat() / max)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                        Text(
                            count.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(56.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

/**
 * v1.64.2: ranked horizontal-bar list (top speakers, top categories).
 * Speaker rows open the nick page when [onEntryClick] is set.
 */
@Composable
private fun TopListCard(
    title: String,
    entries: List<Pair<String, Int>>,
    emptyText: String,
    formatLabel: (String) -> String = { it },
    onEntryClick: ((String) -> Unit)? = null
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
                title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            if (entries.isEmpty()) {
                Text(
                    emptyText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val max = entries.maxOf { it.second }.coerceAtLeast(1)
                for ((label, count) in entries) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .let { m ->
                                if (onEntryClick != null) m.clickable { onEntryClick(label) }
                                else m
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            formatLabel(label),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(count.toFloat() / max)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            count.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
