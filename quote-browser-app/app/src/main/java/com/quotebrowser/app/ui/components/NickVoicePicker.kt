package com.quotebrowser.app.ui.components

import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.plugins.PluginManager
import com.quotebrowser.app.data.plugins.rememberPluginVoiceOptions
import com.quotebrowser.app.ui.GeminiPrebuiltVoices
import com.quotebrowser.app.ui.GeminiTts
import com.quotebrowser.app.ui.GeminiTtsSettings
import com.quotebrowser.app.ui.TTS_ENGINE_GEMINI
import com.quotebrowser.app.ui.VoiceRecordings
import com.quotebrowser.app.ui.cleanSpokenNick
import com.quotebrowser.app.ui.geminiVoiceForNick
import com.quotebrowser.app.ui.installedVoices
import com.quotebrowser.app.ui.isKnownGeminiVoice
import com.quotebrowser.app.ui.voiceForNick

/**
 * v1.63: shared per-nick read-aloud voice picker. Used on the nick
 * deep-dive page and on the Speaker voices settings screen, so voices can
 * be assigned from Settings → Read aloud without hunting for a nick's page.
 *
 * Pick a specific voice for this speaker, used when Speaker voices is on in
 * Settings → Read aloud. The override is keyed on the cleaned nick (the
 * same form read-aloud uses), so it applies across databases.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NickVoicePicker(
    nick: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = remember { GeminiTtsSettings.get(context) }
    val engine by settings.engine.collectAsStateWithLifecycle()
    val isGemini = engine == TTS_ENGINE_GEMINI
    // Read-aloud cleans nicks before speaking (strips <>, {..} style
    // decorations), so key the override on the cleaned form.
    val overrideKey = remember(nick) { cleanSpokenNick(nick) }
    val geminiOverrides by settings.nickGeminiVoiceOverrides.collectAsStateWithLifecycle()
    val builtinOverrides by settings.nickVoiceOverrides.collectAsStateWithLifecycle()
    val autoAssign by settings.autoAssignSpeakerVoices.collectAsStateWithLifecycle()
    val (ttsEngine, installed) = rememberInstalledTts()

    // v1.87: voice-pack voices join both engine lists.
    val pluginGeminiVoices by rememberPluginVoiceOptions("gemini")
    val pluginBuiltinVoices by rememberPluginVoiceOptions("builtin")
    /** Friendly label for a stored voice value (pack ids get "Name (Pack)"). */
    fun voiceLabel(v: String): String =
        if (v.startsWith(PluginManager.DB_ID_PREFIX)) PluginManager.pluginVoiceDisplayName(v)
        else v

    val options: List<String>
    val current: String
    val onPick: (String) -> Unit
    // v1.62: show the effective auto-assigned voice next to "Automatic"
    // so the assignment is visible instead of a black box.
    val effectiveAuto: String? = when {
        isGemini -> {
            val override = geminiOverrides[overrideKey.lowercase()]
            if (!override.isNullOrBlank() && isKnownGeminiVoice(override)) null
            else geminiVoiceForNick(settings, overrideKey)
        }
        else -> {
            val names = installed.map { it.name }.distinct().sorted()
            val override = builtinOverrides[overrideKey.lowercase()]
            if (!override.isNullOrBlank() && override in names) null
            else ttsEngine?.let {
                voiceForNick(it, overrideKey, null, autoAssign)?.name
                    ?: "default voice"
            }
        }
    }
    val automaticLabel = if (effectiveAuto != null) "Automatic ($effectiveAuto)"
    else "Automatic"
    if (isGemini) {
        val override = geminiOverrides[overrideKey.lowercase()]
        options = listOf("Automatic") + GeminiPrebuiltVoices +
            pluginGeminiVoices.map { it.first }
        current =
            if (!override.isNullOrBlank() && isKnownGeminiVoice(override)) voiceLabel(override)
            else automaticLabel
        onPick = { v ->
            if (v == "Automatic") settings.clearNickGeminiVoice(overrideKey)
            else settings.setNickGeminiVoice(overrideKey, v)
        }
    } else {
        val names = installed.map { it.name }.distinct().sorted()
        val override = builtinOverrides[overrideKey.lowercase()]
        options = listOf("Automatic") + names +
            pluginBuiltinVoices.map { it.first }
        current =
            if (!override.isNullOrBlank() && override in names) override
            else if (!override.isNullOrBlank() &&
                PluginManager.resolveBuiltinVoicePitchRate(override) != null
            ) voiceLabel(override)
            else automaticLabel
        onPick = { v ->
            if (v == "Automatic") settings.clearNickVoice(overrideKey)
            else settings.setNickVoice(overrideKey, v)
        }
    }

    // v1.61.21: voices that already have saved recordings somewhere get a
    // mark (· saved). Picking one replays that audio for quotes previously
    // read with it; anything new is synthesized once and then saved.
    val recordings by VoiceRecordings.recordings.collectAsStateWithLifecycle()
    val voicesWithAudio = remember(recordings) {
        recordings.values
            .flatMap { r ->
                listOf(r.active.voices) + r.alternates.map { it.voices }
            }
            .flatMap { it.split("+") }
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "\u2014" }
            .toSet()
    }

    var expanded by remember(nick, isGemini) { mutableStateOf(false) }

    // v1.64.2: voice previews — hear a voice before assigning it. Gemini
    // previews synthesize a short sample through the cloud engine; built-in
    // previews use the installed TTS engine directly.
    val geminiTts = remember { GeminiTts(context) }
    DisposableEffect(geminiTts) { onDispose { geminiTts.release() } }
    var previewing by remember { mutableStateOf<String?>(null) }
    fun toast(msg: String) =
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    /** The concrete voice a row (or "Automatic") would preview as. */
    fun voiceForPreview(v: String): String? = when {
        v == "Automatic" -> effectiveAuto
            ?: if (isGemini) settings.currentVoice() else null
        else -> v
    }

    fun stopPreview() {
        previewing = null
        geminiTts.stop()
        ttsEngine?.stop()
    }

    fun previewVoice(rowLabel: String) {
        val voice = voiceForPreview(rowLabel)
        if (previewing == rowLabel) {
            stopPreview()
            return
        }
        stopPreview()
        if (voice.isNullOrBlank()) {
            toast("No voice available to preview")
            return
        }
        previewing = rowLabel
        if (isGemini) {
            val key = settings.currentApiKey()
            if (key.isBlank()) {
                previewing = null
                toast("Add a Gemini API key in Settings → Read aloud to preview voices")
                return
            }
            geminiTts.speak(
                text = "Hi! This is how I sound reading your quotes.",
                apiKey = key,
                voice = voice,
                onDone = { previewing = null },
                onError = { msg, _ ->
                    previewing = null
                    toast("Preview failed: ${msg.take(100)}")
                }
            )
        } else {
            val t = ttsEngine
            if (t == null) {
                previewing = null
                toast("Text-to-speech engine not ready yet")
                return
            }
            installed.find { it.name == voice }?.let { t.voice = it }
            // v1.87: voice-pack presets apply their pitch/rate to the
            // default voice; anything else resets to neutral so a
            // previous preview's preset can't leak into this one.
            val preset = PluginManager.resolveBuiltinVoicePitchRate(voice)
            t.setPitch(preset?.first ?: 1.0f)
            t.setSpeechRate(preset?.second ?: 1.0f)
            t.speak(
                "Hi! This is how I sound reading your quotes.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "voice-preview"
            )
            // Built-in TTS has no completion callback wired here; clear the
            // indicator shortly after the sample should have finished.
            android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed({ if (previewing == rowLabel) previewing = null }, 6000)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Read-aloud voice", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Voice used for <$nick> when Speaker voices is on " +
                    "(Settings \u2192 Read aloud). Tap \u25b6 on any voice " +
                    "to hear a sample. Voices marked \u00b7 saved " +
                    "already have recorded audio on some quote — picking " +
                    "one replays it wherever it was used; new reads are " +
                    "generated once, then saved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = current,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(if (isGemini) "Gemini voice" else "TTS voice") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { v ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isGemini && v in voicesWithAudio) "$v \u00b7 saved audio"
                                    else voiceLabel(v)
                                )
                            },
                            // v1.64.2: per-voice preview — hear it without
                            // picking it. Tapping the row still assigns.
                            trailingIcon = {
                                IconButton(
                                    onClick = { previewVoice(v) }
                                ) {
                                    Icon(
                                        if (previewing == v) Icons.Filled.Stop
                                        else Icons.Filled.PlayArrow,
                                        contentDescription = "Preview voice $v"
                                    )
                                }
                            },
                            onClick = { onPick(v); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Installed TTS voices plus the engine instance that provided them (for
 * resolving auto-assigned voices), loaded once from a throwaway engine
 * instance that is shut down when the screen leaves composition.
 */
@Composable
internal fun rememberInstalledTts(): Pair<TextToSpeech?, List<Voice>> {
    val context = LocalContext.current
    var voices by remember { mutableStateOf<List<Voice>>(emptyList()) }
    var engine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine = tts
                tts?.let { voices = installedVoices(it) }
            }
        }
        onDispose {
            tts?.shutdown()
            engine = null
        }
    }
    return engine to voices
}
