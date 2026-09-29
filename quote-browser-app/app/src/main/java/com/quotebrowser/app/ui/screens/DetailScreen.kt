package com.quotebrowser.app.ui.screens

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.BasketEntry
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.DisplayPrefs
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.QueueStore
import com.quotebrowser.app.data.QuoteListStore
import com.quotebrowser.app.data.ScoredQuote
import com.quotebrowser.app.ui.applyDisplayBreaks
import com.quotebrowser.app.ui.buildSpokenQuoteText
import com.quotebrowser.app.ui.buildSpokenTurns
import com.quotebrowser.app.ui.dropSpeakerAnnouncements
import com.quotebrowser.app.ui.applyVoiceOrDefault
import com.quotebrowser.app.ui.applySpeakerVoice
import com.quotebrowser.app.ui.applyNarratorVoice
import com.quotebrowser.app.ui.needsPitchRateFallback
import com.quotebrowser.app.ui.GeminiTts
import com.quotebrowser.app.ui.GeminiTtsSegment
import com.quotebrowser.app.ui.GeminiTtsSettings
import com.quotebrowser.app.ui.TTS_ENGINE_GEMINI
import com.quotebrowser.app.ui.geminiVoiceMapForSpeakers
import com.quotebrowser.app.ui.CategoryViewModel
import com.quotebrowser.app.ui.DetailViewModel
import com.quotebrowser.app.ui.ReadAlongDialog
import com.quotebrowser.app.ui.components.CategoryEditDialog
import com.quotebrowser.app.ui.components.DbPageBackground
import com.quotebrowser.app.ui.components.EmptyState
import com.quotebrowser.app.ui.components.ExplainQuoteDialog
import com.quotebrowser.app.ui.components.QuoteDetailCard
import com.quotebrowser.app.ui.components.ShareOptionsDialog
import com.quotebrowser.app.ui.components.SingleQuoteExportDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

@Composable
fun DetailScreen(
    vm: DetailViewModel,
    catVm: CategoryViewModel,
    manager: DatabaseManager,
    showUrl: Boolean = false,
    sourceLabel: String? = null,
    dbId: String,
    basket: List<BasketEntry>,
    onToggleBasket: (String, Int) -> Unit,
    onNickClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    /**
     * v1.56: invoked when following an external database switch lands on a
     * quote number missing from the newly selected database. The default
     * keeps the full-screen "doesn't exist" state (legit for a bad
     * deep link); AppNav passes a bounce-to-list + snackbar handler.
     */
    onFollowMissed: ((dbId: String, number: Int) -> Unit)? = null,
    /**
     * v1.66: user quote lists ("My Collections"). When null, the
     * "Add to list" button is hidden.
     */
    quoteListStore: QuoteListStore? = null
) {
    val quote by vm.quote.collectAsStateWithLifecycle()
    val loaded by vm.loaded.collectAsStateWithLifecycle()
    val isFavorite by vm.isFavorite.collectAsStateWithLifecycle(initialValue = false)
    val allCategories by catVm.categories.collectAsStateWithLifecycle()
    val quoteCategories by vm.categories.collectAsStateWithLifecycle()
    var showShareDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    // v1.66: inline "Explain this quote" dialog + "Add to list" picker.
    var showExplain by remember { mutableStateOf(false) }
    // v1.78: karaoke-style "Read along" dialog target.
    var readAlongQuote by remember { mutableStateOf<com.quotebrowser.app.data.Quote?>(null) }
    var showAddToList by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // v1.88.22: "Break between quotes" controls blank lines between
    // quote-body turns (paragraphizeBody reads it inside cleanDisplayText).
    // Collected as state so the detail view recomposes when it changes.
    DisplayPrefs.init(context)
    @Suppress("UNUSED_VARIABLE")
    val breakSetting by DisplayPrefs.breakBetweenQuotes.collectAsStateWithLifecycle()
    // "Up Next" queue store for the "Add to Up Next" action below.
    val queueStore = remember { QueueStore(context) }
    var speaking by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }
    val ircBreaks by vm.ircLineBreaks.collectAsStateWithLifecycle()
    val dialogueBreaks by vm.dialogueLineBreaks.collectAsStateWithLifecycle()
    val perNickVoices by vm.ttsPerNickVoices.collectAsStateWithLifecycle()
    val myRating by vm.myRating.collectAsStateWithLifecycle()
    val note by vm.note.collectAsStateWithLifecycle()
    // v1.50 pinch-to-zoom: feature toggle + persisted quote-body scale.
    val pinchZoomEnabled by vm.pinchZoomEnabled.collectAsStateWithLifecycle()
    val detailTextScale by vm.detailTextScale.collectAsStateWithLifecycle()
    var showNoteEditor by remember { mutableStateOf(false) }
    var confirmDeleteNote by remember { mutableStateOf(false) }
    // v1.53 report/flag.
    val flaggedQuotes by vm.flaggedQuotes.collectAsStateWithLifecycle()
    // Swipe direction for the slide animation: -1 = swiped left (next),
    // 1 = swiped right (previous).
    var slideDir by remember { mutableStateOf(0) }

    // Text-to-speech for the "Read aloud" button. Created once per detail
    // screen with the system default engine/voice and shut down when leaving.
    val tts = remember {
        ttsReady = false
        TextToSpeech(context, { status ->
            ttsReady = status == TextToSpeech.SUCCESS
        })
    }
    // Gemini cloud voice engine: used instead of the built-in TTS when the
    // user picked it in Settings -> Read aloud.
    val geminiSettings = remember { GeminiTtsSettings.get(context) }
    val geminiTts = remember { GeminiTts(context) }
    var geminiError by remember { mutableStateOf<String?>(null) }
    DisposableEffect(geminiTts) {
        onDispose { geminiTts.release() }
    }
    DisposableEffect(tts) {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                speaking = true
            }

            /** Only the last turn of a quote clears the speaking flag. */
            private fun isLastTurn(utteranceId: String?): Boolean {
                // "detail:<turn>:<turnCount>".
                val parts = utteranceId?.split(":")
                if (parts == null || parts.size != 3 || parts[0] != "detail") return true
                val turn = parts[1].toIntOrNull()
                val count = parts[2].toIntOrNull()
                return turn == null || count == null || turn >= count - 1
            }

            override fun onDone(utteranceId: String?) {
                if (isLastTurn(utteranceId)) speaking = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (isLastTurn(utteranceId)) speaking = false
            }
        })
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    /**
     * Built-in TTS read-aloud for this quote. Also the Gemini fallback
     * target: when Gemini read-aloud fails and the fallback toggle is on,
     * the quote is read this way instead of just stopping.
     */
    fun speakWithBuiltin(text: String) {
        if (speaking) {
            tts.stop()
            speaking = false
            return
        }
        if (!ttsReady) return
        tts.language = Locale.getDefault()
        if (perNickVoices) {
            // Each speaker turn gets that nick's stable installed voice (or
            // their chosen override); narration keeps the default voice.
            // When the quote has 2+ speakers the voices identify who is
            // talking — falling back to a per-speaker pitch/rate variation
            // when the engine can't give everyone a distinct voice — so the
            // "says"/"replies" announcements are dropped.
            val previewTurns = buildSpokenTurns(text, dialogueBreaks)
            if (previewTurns.isEmpty()) return
            val overrides = geminiSettings.currentNickVoiceOverrides()
            val overrideFor: (String) -> String? = { n -> overrides[n.lowercase()] }
            val speakers = previewTurns.map { it.nick }
            val fallback = needsPitchRateFallback(
                tts, speakers, overrideFor,
                geminiSettings.currentAutoAssignSpeakerVoices()
            )
            val turns = if (dropSpeakerAnnouncements(speakers))
                buildSpokenTurns(text, dialogueBreaks, announceSpeakers = false)
            else previewTurns
            val narratorBuiltin = geminiSettings.currentNarratorVoiceBuiltin()
            turns.forEachIndexed { ti, turn ->
                if (turn.isAction) {
                    // *Action* lines are read by the narrator voice, never
                    // by a speaker's voice.
                    applyNarratorVoice(tts, narratorBuiltin)
                } else {
                    applySpeakerVoice(
                        tts, turn.nick, turn.nick?.let(overrideFor), fallback,
                        geminiSettings.currentAutoAssignSpeakerVoices()
                    )
                }
                tts.speak(
                    turn.text,
                    if (ti == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                    null,
                    "detail:$ti:${turns.size}"
                )
            }
        } else {
            applyVoiceOrDefault(tts, null)
            tts.setPitch(1.0f)
            tts.setSpeechRate(1.0f)
            tts.speak(
                buildSpokenQuoteText(text, dialogueBreaks),
                TextToSpeech.QUEUE_FLUSH,
                null,
                "detail:0:1"
            )
        }
    }

    fun toggleSpeech(text: String) {
        if (geminiSettings.currentEngine() == TTS_ENGINE_GEMINI) {
            // Gemini cloud path: stop the tap when already speaking;
            // otherwise stream the spoken text through the Gemini TTS API.
            if (speaking || geminiTts.isPlaying()) {
                geminiTts.stop()
                speaking = false
                return
            }
            val key = geminiSettings.currentApiKey()
            if (key.isBlank()) {
                geminiError = "No Gemini API key set. Add your key in " +
                    "Settings \u2192 Read aloud (get one free in Google AI Studio)."
                geminiSettings.recordLastError(geminiError!!)
                return
            }
            speaking = true
            val onDone: () -> Unit = {
                speaking = false
                geminiSettings.clearLastError()
            }
            val onError: (String, Boolean) -> Unit = { msg, playedAny ->
                speaking = false
                // v1.49: every Gemini failure is persisted for the
                // Settings -> Read aloud "Last error" line.
                geminiSettings.recordLastError(msg)
                if (playedAny) {
                    // Part of the quote was already heard: surface what
                    // failed, but never re-read the whole quote with the
                    // built-in voice.
                    android.widget.Toast.makeText(
                        context, msg, android.widget.Toast.LENGTH_LONG
                    ).show()
                } else if (geminiSettings.currentFallbackToBuiltin()) {
                    android.widget.Toast.makeText(
                        context,
                        "Gemini read-aloud failed \u2014 using built-in voice.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    speakWithBuiltin(text)
                } else {
                    geminiError = msg
                }
            }
            if (perNickVoices) {
                // Per-speaker cloud voices: each turn is synthesized with
                // that speaker's own Gemini voice (or their chosen
                // override). When 2+ speakers are identified by their
                // voices, the "says"/"replies" announcements are dropped;
                // narration lines keep the selected default cloud voice.
                val previewTurns = buildSpokenTurns(text, dialogueBreaks)
                val speakers = previewTurns.map { it.nick }
                val turns = if (dropSpeakerAnnouncements(speakers))
                    buildSpokenTurns(text, dialogueBreaks, announceSpeakers = false)
                else previewTurns
                val voiceMap = geminiVoiceMapForSpeakers(geminiSettings, speakers)
                val narratorVoice = geminiSettings.currentNarratorVoice()
                val segments = turns.map { turn ->
                    GeminiTtsSegment(
                        nick = turn.nick,
                        text = turn.text,
                        voice = when {
                            // *Action* lines use the dedicated narrator
                            // voice, always distinct from speaker voices.
                            turn.isAction -> narratorVoice
                            else -> turn.nick?.lowercase()?.let { voiceMap[it] }
                                ?: geminiSettings.currentVoice()
                        },
                        // v1.60: labels for the multi-speaker request.
                        speakerLabel = when {
                            turn.isAction -> "Directions"
                            turn.nick.isNullOrBlank() -> "Narrator"
                            else -> null
                        }
                    )
                }
                geminiTts.speakSegments(
                    segments, key, onDone, onError,
                    quoteDbId = dbId, quoteNumber = quote?.number
                )
            } else {
                geminiTts.speak(
                    buildSpokenQuoteText(text, dialogueBreaks),
                    key,
                    geminiSettings.currentVoice(),
                    onDone = onDone,
                    onError = onError,
                    quoteDbId = dbId,
                    quoteNumber = quote?.number
                )
            }
            return
        }
        speakWithBuiltin(text)
    }

    // v1.56: a database switch that lands on a missing quote number must
    // never dead-end on the full-screen error — bounce to the list with a
    // snackbar instead (AppNav supplies the handler).
    LaunchedEffect(Unit) {
        vm.followMissed.collect { (missedDbId, missedNumber) ->
            onFollowMissed?.invoke(missedDbId, missedNumber)
        }
    }

    // The screen is reused when swiping between quotes and for the
    // wide-screen detail pane, so reset to the top on every new quote.
    val scrollState = rememberScrollState()
    LaunchedEffect(quote?.number, dbId) {
        scrollState.scrollTo(0)
    }

    // v1.59: swipe with a boundary toast. The swipe engine lives in the
    // ViewModel and stays silent when there is no next/previous quote (end
    // of the database, empty history), so snapshot the position first and
    // toast when it didn't move. A real move updates the position flows
    // within milliseconds, so the delay only fires at a true boundary —
    // never a full-screen error.
    fun attemptSwipe(move: () -> Unit, emptyMessage: String) {
        val before = vm.positionDbId.value to (vm.quote.value?.number ?: -1)
        move()
        scope.launch {
            delay(500)
            val now = vm.positionDbId.value to (vm.quote.value?.number ?: -1)
            if (now == before) {
                Toast.makeText(context, emptyMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Horizontal swipes page through quotes: left = next (per the swipe
    // source/order settings), right = back to the previous quote. This uses
    // the nested-scroll-aware draggable() on the outer Box (horizontal
    // only), so vertical drags pass straight through to the inner Column's
    // verticalScroll instead of getting stuck in a raw pointer-input
    // detector.
    var swipeDragTotal by remember { mutableFloatStateOf(0f) }
    // v1.56: per-database page background — the on-screen quote's own
    // source database paints its page color (or custom color/image).
    DbPageBackground(
        dbId = dbId,
        manager = manager,
        modifier = modifier.fillMaxSize()
    ) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .draggable(
                state = rememberDraggableState { dx -> swipeDragTotal += dx },
                orientation = Orientation.Horizontal,
                onDragStopped = {
                    when {
                        swipeDragTotal < -120 -> {
                            slideDir = -1
                            attemptSwipe(vm::swipeNext, "No next quote")
                        }
                        swipeDragTotal > 120 -> {
                            slideDir = 1
                            attemptSwipe(vm::swipePrevious, "No previous quote")
                        }
                    }
                    swipeDragTotal = 0f
                }
            )
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
        when {
            quote == null && !loaded -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 64.dp)
            )
            quote == null -> MissingQuoteScreen()
            else -> {
                val q = quote!!
                AnimatedContent(
                    targetState = q,
                    transitionSpec = {
                        val dir = slideDir
                        (slideInHorizontally { w -> if (dir < 0) w else -w } + fadeIn()) togetherWith
                            (slideOutHorizontally { w -> if (dir < 0) -w else w } + fadeOut())
                    },
                    label = "quote-swipe"
                ) { aq ->
                // AnimatedContent stacks multiple children on top of each other
                // (it is designed for single-child content), so the card and
                // the note section must share one Column — otherwise the note
                // card overlaps and covers the quote header.
                Column {
                QuoteDetailCard(
                    quote = aq,
                    isFavorite = isFavorite,
                    onToggleFavorite = vm::toggleFavorite,
                    onCopy = { showShareDialog = true },
                    onShare = { showShareDialog = true },
                    onExport = { showExportDialog = true },
                    showUrl = showUrl,
                    categories = quoteCategories,
                    onEditCategories = { showCategoryDialog = true },
                    sourceLabel = sourceLabel,
                    sourceDbId = dbId,
                    inBasket = BasketEntry(dbId, aq.number) in basket,
                    onToggleBasket = { onToggleBasket(dbId, aq.number) },
                    isSpeaking = speaking,
                    onNickClick = onNickClick,
                    myRating = myRating,
                    onRate = vm::setMyRating,
                    onReadAloud = {
                        toggleSpeech(aq.text)
                    },
                    // v1.78: karaoke-style read-along with word highlighting.
                    onReadAlong = { readAlongQuote = aq },
                    // v1.66: inline Explain + Add to list.
                    onExplain = { showExplain = true },
                    onAddToList = if (quoteListStore != null) {
                        { showAddToList = true }
                    } else {
                        null
                    },
                    pinchZoomEnabled = pinchZoomEnabled,
                    detailTextScale = detailTextScale,
                    onDetailTextScaleChange = vm::setDetailTextScale,
                    // v1.53 report/flag.
                    isFlagged = "${vm.positionDbId.value}/${aq.number}" in flaggedQuotes,
                    flagInfo = flaggedQuotes["${vm.positionDbId.value}/${aq.number}"],
                    onFlagQuote = { reason, custom ->
                        vm.flagQuote(vm.positionDbId.value, aq.number, reason, custom)
                    }
                )
                // v1.49: the personal-notes feature toggle gates this section.
                val detailNotesEnabled by vm.notesEnabled.collectAsStateWithLifecycle()
                if (detailNotesEnabled) {
                    NoteSection(
                        note = note,
                        onEdit = { showNoteEditor = true },
                        onDelete = { confirmDeleteNote = true }
                    )
                }
                // "Add to Up Next": queues the displayed quote for later
                // reading (Toast matches this screen's confirmation style).
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        val added = queueStore.add(dbId, aq.number)
                        Toast.makeText(
                            context,
                            if (added) "Added to Up Next" else "Already in Up Next",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text("Add to Up Next")
                    }
                }
                } // Column: quote card + note section (single AnimatedContent child)
                if (showNoteEditor) {
                    NoteEditDialog(
                        initial = note ?: "",
                        onDismiss = { showNoteEditor = false },
                        onSave = { text -> vm.setNote(text) }
                    )
                }
                if (confirmDeleteNote) {
                    AlertDialog(
                        onDismissRequest = { confirmDeleteNote = false },
                        title = { Text("Delete note?") },
                        text = { Text("This removes your personal note for quote #${aq.number}.") },
                        confirmButton = {
                            TextButton(onClick = {
                                vm.setNote("")
                                confirmDeleteNote = false
                            }) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmDeleteNote = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
                if (showShareDialog) {
                    ShareOptionsDialog(
                        quote = aq,
                        onDismiss = { showShareDialog = false },
                        categories = quoteCategories,
                        ircLineBreaks = ircBreaks,
                        dialogueLineBreaks = dialogueBreaks,
                        dbId = dbId
                    )
                }
                // v1.66: inline "Explain this quote" (Gemini pipeline, same
                // dialog as the long-press sheet's row).
                if (showExplain) {
                    ExplainQuoteDialog(
                        quote = aq,
                        onDismiss = { showExplain = false },
                        // v1.75: pass dbId so the explanation is cached.
                        dbId = dbId
                    )
                }
                // v1.78: karaoke-style read-along.
                readAlongQuote?.let { rq ->
                    ReadAlongDialog(
                        dbId = dbId,
                        number = rq.number,
                        displayText = rq.text,
                        onDismiss = { readAlongQuote = null }
                    )
                }
                // v1.66: add this quote to a user list ("My Collections").
                val listStore = quoteListStore
                if (showAddToList && listStore != null) {
                    AddToListDialog(
                        listStore = listStore,
                        dbId = dbId,
                        number = aq.number,
                        onDismiss = { showAddToList = false }
                    )
                }
                if (showExportDialog) {
                    SingleQuoteExportDialog(
                        quote = aq,
                        onDismiss = { showExportDialog = false },
                        dbId = dbId
                    )
                }
                if (showCategoryDialog) {
                    CategoryEditDialog(
                        allCategories = allCategories,
                        selectedIds = quoteCategories.map { it.id }.toSet(),
                        onCreateCategory = { name -> catVm.addCategory(name) },
                        onDismiss = { showCategoryDialog = false },
                        onSave = { ids -> vm.setCategories(ids) }
                    )
                }
                // v1.72.4: the inline "More like this" section is gone too —
                // tapping a quote just shows the quote, nothing else.
                if (geminiError != null) {
                    AlertDialog(
                        onDismissRequest = { geminiError = null },
                        title = { Text("Gemini read-aloud") },
                        text = { Text(geminiError!!) },
                        confirmButton = {
                            TextButton(onClick = { geminiError = null }) {
                                Text("OK")
                            }
                        }
                    )
                }
                }
            }
        }
    }
    } // Box
    } // DbPageBackground
}

/** Shown when a quote number doesn't exist in the current database. */
@Composable
fun MissingQuoteScreen() {
    EmptyState("That quote doesn't exist in this database.")
}

/** Personal note section on the detail screen: add/edit/delete. */
@Composable
private fun NoteSection(
    note: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "My note",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (note != null) {
                    TextButton(onClick = onEdit) { Text("Edit") }
                    TextButton(onClick = onDelete) { Text("Delete") }
                }
            }
            if (note == null) {
                TextButton(onClick = onEdit) { Text("Add a note") }
            } else {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** Add/edit dialog for a personal note. */
@Composable
private fun NoteEditDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My note") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(text); onDismiss() },
                enabled = text.trim().isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

