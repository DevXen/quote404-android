package com.quotebrowser.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.SourcedQuote
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

/** One Daily Challenge round: a quiz game drawn deterministically per day. */
sealed interface DailyRound {
    data class WhoSaid(
        val quote: SourcedQuote,
        val options: List<String>,
        val answer: String
    ) : DailyRound

    data class HigherLower(val a: SourcedQuote, val b: SourcedQuote) : DailyRound
}

/** Five rounds per day, date-seeded; only the first attempt is scored. */
const val DAILY_CHALLENGE_ROUNDS = 5

/**
 * Daily Challenge: a date-seeded mix of "Who said it?" and
 * "Higher or lower" rounds. Exactly one scored attempt per day is
 * recorded (completion date, score, streak, best); replays after
 * completion run unscored and never touch the persisted stats.
 */
class DailyChallengeViewModel(
    private val manager: DatabaseManager
) : ViewModel() {

    val today: String = LocalDate.now().toString()

    val completedToday: StateFlow<Boolean> =
        manager.dcLastDate
            .map { it == today }
            .stateIn(
                viewModelScope, SharingStarted.WhileSubscribed(5000),
                manager.dcLastDate.value == today
            )

    val lastScore: StateFlow<Int> = manager.dcLastScore
    val bestScore: StateFlow<Int> = manager.dcBestScore
    val streak: StateFlow<Int> = manager.dcStreak

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _rounds = MutableStateFlow<List<DailyRound>?>(null)
    val rounds: StateFlow<List<DailyRound>?> = _rounds.asStateFlow()

    private val _index = MutableStateFlow(0)
    val index: StateFlow<Int> = _index.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _pickedNick = MutableStateFlow<String?>(null)
    val pickedNick: StateFlow<String?> = _pickedNick.asStateFlow()

    private val _pickedQuote = MutableStateFlow<SourcedQuote?>(null)
    val pickedQuote: StateFlow<SourcedQuote?> = _pickedQuote.asStateFlow()

    private val _finished = MutableStateFlow(false)
    val finished: StateFlow<Boolean> = _finished.asStateFlow()

    /** Whether the in-progress attempt counts toward the daily stats. */
    private val _scored = MutableStateFlow(true)
    val scored: StateFlow<Boolean> = _scored.asStateFlow()

    val currentRound: DailyRound?
        get() = _rounds.value?.getOrNull(_index.value)

    /**
     * Starts an attempt. [scored] = false for practice replays after the
     * day's attempt is already recorded.
     */
    fun start(scored: Boolean) {
        _scored.value = scored
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            try {
                _rounds.value = buildRounds(today)
                _index.value = 0
                _score.value = 0
                _finished.value = false
                resetPicks()
            } finally {
                _loading.value = false
            }
        }
    }

    private fun resetPicks() {
        _pickedNick.value = null
        _pickedQuote.value = null
    }

    private suspend fun buildRounds(date: String): List<DailyRound> {
        val seed = try {
            LocalDate.parse(date).toEpochDay()
        } catch (_: Exception) {
            0L
        }
        val rng = Random(seed)
        val top = manager.topNicks(60).map { it.first }
        val ids = manager.allDbIds()
        // Date-seeded mix: 3 "Who said it?" + 2 "Higher or lower", shuffled.
        val types = listOf("who", "hl", "who", "hl", "who").shuffled(rng)
        val out = mutableListOf<DailyRound>()
        for (t in types) {
            val round = if (t == "who") drawWhoSaid(rng, ids, top)
            else drawHigherLower(rng, ids)
            if (round != null) out.add(round)
        }
        return out
    }

    private suspend fun drawWhoSaid(
        rng: Random,
        ids: List<String>,
        top: List<String>
    ): DailyRound.WhoSaid? {
        if (top.size < 4 || ids.isEmpty()) return null
        repeat(30) {
            val id = ids[rng.nextInt(ids.size)]
            val quotes = try {
                manager.repoFor(id).randomQuotes(20)
            } catch (_: Exception) {
                emptyList()
            }
            val pick = quotes.firstOrNull { q ->
                extractNicks(q.text).isNotEmpty() &&
                    !manager.isQuoteHiddenFromLists(id, q.number, q.text)
            } ?: return@repeat
            val answer = extractNicks(pick.text).first()
            val distractors = top
                .filter { t -> !t.equals(answer, ignoreCase = true) }
                .shuffled(rng)
                .take(3)
            if (distractors.size < 3) return@repeat
            return DailyRound.WhoSaid(
                quote = SourcedQuote(id, pick),
                options = (distractors + answer).shuffled(rng),
                answer = answer
            )
        }
        return null
    }

    private suspend fun drawHigherLower(
        rng: Random,
        ids: List<String>
    ): DailyRound.HigherLower? {
        if (ids.isEmpty()) return null
        repeat(25) {
            val id = ids[rng.nextInt(ids.size)]
            val dealt = try {
                manager.repoFor(id).randomQuotes(12)
                    .filter { q ->
                        q.rating != null &&
                            !manager.isQuoteHiddenFromLists(id, q.number, q.text)
                    }
                    .shuffled(rng)
            } catch (_: Exception) {
                emptyList()
            }
            if (dealt.size >= 2) {
                return DailyRound.HigherLower(
                    SourcedQuote(id, dealt[0]),
                    SourcedQuote(id, dealt[1])
                )
            }
        }
        return null
    }

    fun answerWhoSaid(nick: String) {
        val r = currentRound as? DailyRound.WhoSaid ?: return
        if (_pickedNick.value != null) return
        _pickedNick.value = nick
        if (nick.equals(r.answer, ignoreCase = true)) _score.value += 1
    }

    fun answerHigherLower(which: SourcedQuote) {
        val r = currentRound as? DailyRound.HigherLower ?: return
        if (_pickedQuote.value != null) return
        _pickedQuote.value = which
        val other = if (which == r.a) r.b else r.a
        if ((which.quote.rating ?: 0) >= (other.quote.rating ?: 0)) {
            _score.value += 1
        }
    }

    /**
     * Advances to the next round, or finishes. The score is recorded exactly
     * once, on the first finish of a scored attempt.
     */
    fun nextRound() {
        val rounds = _rounds.value ?: return
        if (_index.value < rounds.size - 1) {
            _index.value += 1
            resetPicks()
        } else if (!_finished.value) {
            _finished.value = true
            if (_scored.value) {
                manager.recordDailyChallenge(today, _score.value)
            }
        }
    }

    fun labelFor(dbId: String): String = manager.shortLabel(dbId)
}
