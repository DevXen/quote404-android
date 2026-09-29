package com.quotebrowser.app.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.screens.games.GamePrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Persists which achievements have been unlocked, in the
 * "achievements_v145" SharedPreferences file. Unlocks are sticky: once
 * earned, an achievement stays unlocked.
 */
class AchievementStore private constructor(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isUnlocked(id: String): Boolean = prefs.getBoolean(KEY_UNLOCKED + id, false)

    /**
     * Marks every achievement in [states] whose progress hit its target as
     * unlocked. Returns the ids that were newly unlocked by this call.
     */
    fun sync(states: List<AchievementState>): List<String> {
        val newly = states.filter { it.unlocked && !isUnlocked(it.def.id) }
        if (newly.isEmpty()) return emptyList()
        prefs.edit().apply {
            newly.forEach { putBoolean(KEY_UNLOCKED + it.def.id, true) }
        }.apply()
        return newly.map { it.def.id }
    }

    companion object {
        const val PREFS_NAME = "achievements_v145"
        private const val KEY_UNLOCKED = "unlocked_"
        fun of(context: Context): AchievementStore = AchievementStore(context)
    }
}

/** One achievement definition: title, how it's earned, and its target. */
data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val target: Int
)

/** An achievement with the player's current progress attached. */
data class AchievementState(
    val def: AchievementDef,
    val progress: Int,
    val unlocked: Boolean = progress >= def.target
)

/**
 * Computes achievement progress from existing persisted data: the reading
 * log + streaks in [DatabaseManager], per-game best scores in [GamePrefs],
 * and favorite counts. No new tracking of its own.
 */
suspend fun evaluateAchievements(
    context: Context,
    manager: DatabaseManager
): List<AchievementState> = withContext(Dispatchers.IO) {
    val ids = try { manager.allDbIds() } catch (e: Exception) { emptyList() }
    val totalSeen = ids.sumOf {
        try { manager.seenCount(it).first() } catch (e: Exception) { 0 }
    }
    val longestStreak = try { manager.longestReadingStreakDays() } catch (e: Exception) { 0 }
    val favCount = ids.sumOf {
        try { manager.repoFor(it).favorites().first().size } catch (e: Exception) { 0 }
    }
    val gamePrefs = GamePrefs.of(context)
    val gameKeys = listOf(
        GamePrefs.FILL_BLANK, GamePrefs.ODD_ONE_OUT, GamePrefs.SORT_RATING,
        GamePrefs.GUESS_DB, GamePrefs.SPEED_ROUND, GamePrefs.TOURNAMENT
    )
    val gameBests = gameKeys.associateWith { gamePrefs.getBestScore(it) }
    val gamesPlayed = gameBests.count { it.value > 0 }
    val whoSaidBest = manager.whoSaidBestStreak.value
    val ratingGuessBest = manager.ratingGuessBestStreak.value
    val dcBest = manager.dcBestScore.value
    val dcStreak = manager.dcStreak.value

    fun ach(id: String, title: String, desc: String, target: Int, progress: Int) =
        AchievementState(AchievementDef(id, title, desc, target), progress)

    listOf(
        ach("read_1", "First Steps", "Read your first quote", 1, totalSeen),
        ach("read_100", "Avid Reader", "Read 100 quotes", 100, totalSeen),
        ach("read_1000", "Bookworm", "Read 1,000 quotes", 1000, totalSeen),
        ach("read_5000", "Archivist", "Read 5,000 quotes", 5000, totalSeen),
        ach("read_21096", "Completionist", "Read all 21,096 quotes", 21096, totalSeen),
        ach("streak_7", "Week Warrior", "Read 7 days in a row", 7, longestStreak),
        ach("streak_30", "Month of Quotes", "Read 30 days in a row", 30, longestStreak),
        ach("streak_100", "Centurion", "Read 100 days in a row", 100, longestStreak),
        ach("streak_365", "Quote-a-Day Year", "Read 365 days in a row", 365, longestStreak),
        ach("fav_1", "Saved One", "Favorite your first quote", 1, favCount),
        ach("fav_25", "Curator", "Collect 25 favorites", 25, favCount),
        ach("fav_100", "Collector", "Collect 100 favorites", 100, favCount),
        ach("fav_500", "Hoarder", "Collect 500 favorites", 500, favCount),
        ach("play_1", "First Game", "Score in a game", 1, gamesPlayed),
        ach("play_3", "Dabbler", "Score in 3 different games", 3, gamesPlayed),
        ach("play_6", "Arcade Regular", "Score in all 6 arcade games", 6, gamesPlayed),
        ach(
            "speed_10", "Speed Demon", "Score 10+ in Speed Round", 10,
            gameBests[GamePrefs.SPEED_ROUND] ?: 0
        ),
        ach(
            "tournament_8", "Champion", "Score 8+ in Tournament", 8,
            gameBests[GamePrefs.TOURNAMENT] ?: 0
        ),
        ach("whosaid_5", "Name Dropper", "Reach a 5-streak in Who Said It", 5, whoSaidBest),
        ach("whosaid_15", "People Person", "Reach a 15-streak in Who Said It", 15, whoSaidBest),
        ach("ratingguess_5", "Critic", "Reach a 5-streak in Guess the Rating", 5, ratingGuessBest),
        ach("dc_3", "Challenger", "Score 3+ in the Daily Challenge", 3, dcBest),
        ach("dc_10", "Daily Ritualist", "Score a perfect 10 in the Daily Challenge", 10, dcBest),
        ach("dc_streak_7", "Daily Ritual", "A 7-day Daily Challenge streak", 7, dcStreak),
    )
}

/** Achievements section for the stats screen: unlocked count + badge grid. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementsCard(manager: DatabaseManager) {
    val context = LocalContext.current
    var states by remember { mutableStateOf<List<AchievementState>?>(null) }
    var newlyUnlocked by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(manager) {
        val computed = evaluateAchievements(context, manager)
        val store = AchievementStore.of(context)
        newlyUnlocked = store.sync(computed).toSet()
        states = computed
    }

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
                    "Achievements",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                val s = states
                if (s != null) {
                    val unlocked = s.count { it.unlocked }
                    Text(
                        "$unlocked / ${s.size}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            val s = states
            if (s == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    s.forEach { state ->
                        AchievementBadge(
                            state = state,
                            isNew = state.def.id in newlyUnlocked,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    state: AchievementState,
    isNew: Boolean,
    modifier: Modifier = Modifier
) {
    val earned = state.unlocked
    Surface(
        modifier = modifier.width(148.dp),
        shape = RoundedCornerShape(12.dp),
        color = when {
            isNew -> MaterialTheme.colorScheme.tertiaryContainer
            earned -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = when {
            isNew -> MaterialTheme.colorScheme.onTertiaryContainer
            earned -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Column(
            Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (earned) "★" else "☆",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                state.def.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                state.def.description,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (earned) "Unlocked!"
                else "${state.progress.coerceAtMost(state.def.target)} / ${state.def.target}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isNew) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
