package com.quotebrowser.app.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.util.Base64
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.plugins.PluginManager
import com.quotebrowser.app.data.plugins.rememberPluginVoiceOptions
import com.quotebrowser.app.ui.screens.SettingToggle
import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** SharedPreferences file for read-aloud voice engine settings. */
const val TTS_PREFS_V145 = "tts_prefs_v145"
private const val KEY_TTS_ENGINE = "engine"
private const val KEY_GEMINI_API_KEY = "gemini_api_key"
private const val KEY_GEMINI_VOICE = "gemini_voice"
/**
 * Narrator voice for *action* lines (v1.47): the Gemini prebuilt voice used
 * for action-line turns, always distinct from every speaker voice.
 */
private const val KEY_NARRATOR_VOICE = "narrator_voice"
/** Default narrator voice: distinct from the default speaker voice (Zephyr). */
const val DEFAULT_NARRATOR_VOICE = "Aoede"
/** Installed TTS voice name for *action* lines (built-in engine); blank = automatic. */
private const val KEY_NARRATOR_VOICE_BUILTIN = "narrator_voice_builtin"
/** Lowercase cleaned nick -> installed TTS voice name (built-in engine override). */
private const val KEY_NICK_VOICE_OVERRIDES = "nick_voice_overrides"
/** Lowercase cleaned nick -> Gemini prebuilt voice name (Gemini engine override). */
private const val KEY_NICK_GEMINI_VOICE_OVERRIDES = "nick_gemini_voice_overrides"
/** Last Gemini read-aloud failure, kept visible in Settings (v1.49). */
private const val KEY_GEMINI_LAST_ERROR = "gemini_last_error"
/** Fall back to built-in TTS when Gemini read-aloud fails (v1.49, default ON). */
private const val KEY_GEMINI_FALLBACK = "gemini_fallback_builtin"
/**
 * v1.62: auto-assign a voice to speakers without a manual per-nick pick
 * (default ON).
 */
private const val KEY_AUTO_ASSIGN_SPEAKER_VOICES = "auto_assign_speaker_voices"

/** "Built-in TTS": the device's android.speech.tts engine (default). */
const val TTS_ENGINE_BUILTIN = "builtin"

/** "Gemini cloud": Google Gemini cloud TTS voices via the REST API. */
const val TTS_ENGINE_GEMINI = "gemini"

/** Gemini TTS endpoint. 16-bit PCM mono at 24 kHz comes back base64-encoded. */
private const val GEMINI_TTS_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent"

/** Max characters per Gemini TTS request; longer text is split and spoken sequentially. */
private const val GEMINI_MAX_CHARS_PER_REQUEST = 4000

/** Sample rate (Hz) of the PCM audio Gemini TTS returns. */
private const val GEMINI_SAMPLE_RATE = 24000

/**
 * Gemini's prebuilt voices (as listed by the Gemini speech API).
 */
val GeminiPrebuiltVoices = listOf(
    "Zephyr", "Kore", "Fenrir", "Charon", "Aoede", "Leda", "Callirrhoe",
    "Autonoe", "Enceladus", "Iapetus", "Umbriel", "Algieba", "Despina",
    "Erinome", "Algenib", "Rasalgethi", "Laomedeia", "Alnilam", "Schedar",
    "Gacrux", "Pulcherrima", "Achird", "Zubenelgenubi", "Vindemiatrix",
    "Sadachbia", "Sadaltager", "Puck", "Orus", "Achernar", "Sulafat"
)

/**
 * v1.87: a Gemini voice name the app can actually use — a prebuilt cloud
 * voice or a loadable voice-pack voice id (resolved to its base prebuilt
 * voice at synthesis time). A pack voice whose pack was disabled or
 * uninstalled is NOT known, so stale overrides fall back to Automatic.
 */
fun isKnownGeminiVoice(name: String): Boolean =
    name in GeminiPrebuiltVoices ||
        PluginManager.resolveGeminiVoiceName(name) != null

/**
 * Resolve a stored voice reference to the concrete prebuilt cloud voice
 * name the Gemini API accepts. Plain prebuilt names pass through; voice
 * pack ids resolve to their base voice; unknown ids fall back to Zephyr
 * (never send a pack id to the API — it would fail the request).
 */
fun geminiVoiceForSynthesis(name: String): String =
    if (name in GeminiPrebuiltVoices) name
    else PluginManager.resolveGeminiVoiceName(name) ?: "Zephyr"

/**
 * v1.63: best-effort gender tags for the prebuilt cloud voices, so
 * automatic speaker assignment can match a nick that reads female/male
 * to a same-gender voice. Untagged voices are flex picks for any nick.
 * Tags follow the voices' published descriptions; anything uncertain is
 * left UNKNOWN rather than risk a wrong tag.
 */
private val geminiVoiceGenders: Map<String, NickGender> = mapOf(
    "Zephyr" to NickGender.FEMALE,
    "Kore" to NickGender.FEMALE,
    "Fenrir" to NickGender.MALE,
    "Charon" to NickGender.MALE,
    "Aoede" to NickGender.FEMALE,
    "Leda" to NickGender.FEMALE,
    "Callirrhoe" to NickGender.FEMALE,
    "Autonoe" to NickGender.FEMALE,
    "Enceladus" to NickGender.MALE,
    "Iapetus" to NickGender.MALE,
    "Umbriel" to NickGender.MALE,
    "Algieba" to NickGender.MALE,
    "Despina" to NickGender.FEMALE,
    "Erinome" to NickGender.FEMALE,
    "Algenib" to NickGender.MALE,
    "Rasalgethi" to NickGender.MALE,
    "Laomedeia" to NickGender.FEMALE,
    "Alnilam" to NickGender.MALE,
    "Schedar" to NickGender.MALE,
    "Gacrux" to NickGender.FEMALE,
    "Pulcherrima" to NickGender.FEMALE,
    "Achird" to NickGender.MALE,
    "Zubenelgenubi" to NickGender.MALE,
    "Vindemiatrix" to NickGender.FEMALE,
    "Sadachbia" to NickGender.MALE,
    "Sadaltager" to NickGender.MALE,
    "Puck" to NickGender.MALE,
    "Orus" to NickGender.MALE,
    "Achernar" to NickGender.FEMALE
    // "Sulafat" deliberately untagged: flex pick for any gender.
)

fun geminiVoiceGender(voice: String): NickGender =
    geminiVoiceGenders[voice] ?: NickGender.UNKNOWN

/**
 * Read-aloud voice engine settings, backed by private app prefs
 * ([TTS_PREFS_V145]). The Gemini API key never leaves the device except
 * in the HTTPS request to Google's Gemini API itself.
 *
 * Process-wide singleton: every screen sees the same values.
 */
class GeminiTtsSettings private constructor(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(TTS_PREFS_V145, Context.MODE_PRIVATE)

    private val _engine = MutableStateFlow(
        prefs.getString(KEY_TTS_ENGINE, TTS_ENGINE_BUILTIN) ?: TTS_ENGINE_BUILTIN
    )
    val engine: StateFlow<String> = _engine.asStateFlow()

    private val _apiKey = MutableStateFlow(
        prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
    )
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _voice = MutableStateFlow(
        prefs.getString(KEY_GEMINI_VOICE, "Zephyr") ?: "Zephyr"
    )
    val voice: StateFlow<String> = _voice.asStateFlow()

    private val _narratorVoice = MutableStateFlow(
        prefs.getString(KEY_NARRATOR_VOICE, DEFAULT_NARRATOR_VOICE)
            ?: DEFAULT_NARRATOR_VOICE
    )
    val narratorVoice: StateFlow<String> = _narratorVoice.asStateFlow()

    private val _narratorVoiceBuiltin = MutableStateFlow(
        prefs.getString(KEY_NARRATOR_VOICE_BUILTIN, "") ?: ""
    )
    val narratorVoiceBuiltin: StateFlow<String> = _narratorVoiceBuiltin.asStateFlow()

    /**
     * Per-nick voice overrides, keyed by lowercase cleaned nick: the voice
     * picked for that nick on their page, used when Speaker voices is on.
     */
    private val _nickVoiceOverrides = MutableStateFlow(readNickMap(KEY_NICK_VOICE_OVERRIDES))
    val nickVoiceOverrides: StateFlow<Map<String, String>> =
        _nickVoiceOverrides.asStateFlow()

    private val _nickGeminiVoiceOverrides =
        MutableStateFlow(readNickMap(KEY_NICK_GEMINI_VOICE_OVERRIDES))
    val nickGeminiVoiceOverrides: StateFlow<Map<String, String>> =
        _nickGeminiVoiceOverrides.asStateFlow()

    /**
     * Last Gemini read-aloud failure, kept so Settings -> Read aloud can
     * show it. Recorded on every Gemini failure (detail screen and
     * playlists); cleared on the next successful playback.
     */
    private val _lastError =
        MutableStateFlow(prefs.getString(KEY_GEMINI_LAST_ERROR, null))
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun recordLastError(message: String) {
        prefs.edit().putString(KEY_GEMINI_LAST_ERROR, message).apply()
        _lastError.value = message
    }

    fun clearLastError() {
        prefs.edit().remove(KEY_GEMINI_LAST_ERROR).apply()
        _lastError.value = null
    }

    /**
     * When ON (default), a Gemini read-aloud failure falls back to the
     * built-in TTS engine with a toast instead of just stopping.
     */
    private val _fallbackToBuiltin =
        MutableStateFlow(prefs.getBoolean(KEY_GEMINI_FALLBACK, true))
    val fallbackToBuiltin: StateFlow<Boolean> = _fallbackToBuiltin.asStateFlow()

    fun setFallbackToBuiltin(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GEMINI_FALLBACK, enabled).apply()
        _fallbackToBuiltin.value = enabled
    }

    fun currentFallbackToBuiltin(): Boolean =
        prefs.getBoolean(KEY_GEMINI_FALLBACK, true)

    /**
     * v1.62: when ON (default), speakers without a manual per-nick voice
     * pick get a deterministic auto-assigned voice (same nick always
     * sounds the same). When OFF, only nicks with an explicit override
     * get their own voice; everyone else uses the default voice.
     */
    private val _autoAssignSpeakerVoices =
        MutableStateFlow(prefs.getBoolean(KEY_AUTO_ASSIGN_SPEAKER_VOICES, true))
    val autoAssignSpeakerVoices: StateFlow<Boolean> =
        _autoAssignSpeakerVoices.asStateFlow()

    fun setAutoAssignSpeakerVoices(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_ASSIGN_SPEAKER_VOICES, enabled).apply()
        _autoAssignSpeakerVoices.value = enabled
    }

    fun currentAutoAssignSpeakerVoices(): Boolean =
        prefs.getBoolean(KEY_AUTO_ASSIGN_SPEAKER_VOICES, true)

    /** Snapshot reads for non-Compose callers (always fresh from prefs). */
    fun currentEngine(): String =
        prefs.getString(KEY_TTS_ENGINE, TTS_ENGINE_BUILTIN) ?: TTS_ENGINE_BUILTIN

    fun currentApiKey(): String =
        prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""

    fun currentVoice(): String =
        prefs.getString(KEY_GEMINI_VOICE, "Zephyr") ?: "Zephyr"

    fun currentNickVoiceOverrides(): Map<String, String> =
        readNickMap(KEY_NICK_VOICE_OVERRIDES)

    fun currentNickGeminiVoiceOverrides(): Map<String, String> =
        readNickMap(KEY_NICK_GEMINI_VOICE_OVERRIDES)

    fun setEngine(value: String) {
        val v = if (value == TTS_ENGINE_GEMINI) TTS_ENGINE_GEMINI else TTS_ENGINE_BUILTIN
        prefs.edit().putString(KEY_TTS_ENGINE, v).apply()
        _engine.value = v
    }

    fun setApiKey(value: String) {
        val v = value.trim()
        prefs.edit().putString(KEY_GEMINI_API_KEY, v).apply()
        _apiKey.value = v
    }

    fun setVoice(value: String) {
        val v = if (value in GeminiPrebuiltVoices) value else "Zephyr"
        prefs.edit().putString(KEY_GEMINI_VOICE, v).apply()
        _voice.value = v
    }

    fun currentNarratorVoice(): String =
        prefs.getString(KEY_NARRATOR_VOICE, DEFAULT_NARRATOR_VOICE)
            ?: DEFAULT_NARRATOR_VOICE

    fun setNarratorVoice(value: String) {
        val v = if (value in GeminiPrebuiltVoices) value else DEFAULT_NARRATOR_VOICE
        prefs.edit().putString(KEY_NARRATOR_VOICE, v).apply()
        _narratorVoice.value = v
    }

    fun currentNarratorVoiceBuiltin(): String =
        prefs.getString(KEY_NARRATOR_VOICE_BUILTIN, "") ?: ""

    /**
     * Sets (or, with a blank name, clears) the installed TTS voice used for
     * *action* lines with the built-in engine.
     */
    fun setNarratorVoiceBuiltin(voiceName: String) {
        prefs.edit().putString(KEY_NARRATOR_VOICE_BUILTIN, voiceName).apply()
        _narratorVoiceBuiltin.value = voiceName
    }

    /**
     * Sets (or, with a blank [voiceName], clears) the built-in TTS voice
     * override for [nick]: the installed voice [voiceName] is used for
     * that speaker when Speaker voices is on.
     */
    fun setNickVoice(nick: String, voiceName: String) {
        val key = nick.lowercase()
        val map = readNickMap(KEY_NICK_VOICE_OVERRIDES).toMutableMap()
        val v = voiceName.trim()
        if (v.isEmpty()) map.remove(key) else map[key] = v
        writeNickMap(KEY_NICK_VOICE_OVERRIDES, map)
        _nickVoiceOverrides.value = map
    }

    fun clearNickVoice(nick: String) = setNickVoice(nick, "")

    /**
     * Sets (or, with a voice outside the prebuilt list, clears) the Gemini
     * cloud voice override for [nick].
     */
    fun setNickGeminiVoice(nick: String, voice: String) {
        val key = nick.lowercase()
        val map = readNickMap(KEY_NICK_GEMINI_VOICE_OVERRIDES).toMutableMap()
        if (voice in GeminiPrebuiltVoices) map[key] = voice else map.remove(key)
        writeNickMap(KEY_NICK_GEMINI_VOICE_OVERRIDES, map)
        _nickGeminiVoiceOverrides.value = map
    }

    fun clearNickGeminiVoice(nick: String) {
        val key = nick.lowercase()
        val map = readNickMap(KEY_NICK_GEMINI_VOICE_OVERRIDES).toMutableMap()
        if (map.remove(key) != null) {
            writeNickMap(KEY_NICK_GEMINI_VOICE_OVERRIDES, map)
            _nickGeminiVoiceOverrides.value = map
        }
    }

    private fun readNickMap(prefKey: String): Map<String, String> {
        val raw = prefs.getString(prefKey, null) ?: return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val out = mutableMapOf<String, String>()
            obj.keys().forEach { k -> out[k] = obj.optString(k, "") }
            out.filterValues { it.isNotEmpty() }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun writeNickMap(prefKey: String, map: Map<String, String>) {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        prefs.edit().putString(prefKey, obj.toString()).apply()
    }

    companion object {
        @Volatile
        private var instance: GeminiTtsSettings? = null

        fun get(context: Context): GeminiTtsSettings =
            instance ?: synchronized(this) {
                instance ?: GeminiTtsSettings(context).also { instance = it }
            }
    }
}

/** Failure from a Gemini TTS request; [message] is user-presentable. */
open class GeminiTtsException(message: String) : Exception(message)

/**
 * v1.60: quota exhaustion (HTTP 429). Unlike other chunk failures this
 * aborts the whole read immediately — every further request would fail
 * too, so firing them only burns the (already exhausted) quota.
 * The free tier for gemini-2.5-flash-preview-tts is ~15 requests/day,
 * which a multi-voice quote used to eat in a single read-aloud
 * (one request per dialogue turn).
 */
class GeminiQuotaException(message: String) : GeminiTtsException(message)

/**
 * Shown instead of raw socket errors (e.g. "Software caused connection
 * abort"); tells the user what to actually try.
 */
const val FRIENDLY_NETWORK_ERROR =
    "Couldn't reach Google's speech server (network error). Check your " +
        "connection \u2014 and try turning off any VPN, ad-blocker, or " +
        "Private DNS \u2014 then try again."

/**
 * Splits [text] into chunks of at most [maxChars] characters for sequential
 * Gemini TTS requests. Splits prefer sentence boundaries, then word
 * boundaries, so speech doesn't cut mid-word.
 */
fun chunkTextForTts(text: String, maxChars: Int = GEMINI_MAX_CHARS_PER_REQUEST): List<String> {
    val out = mutableListOf<String>()
    var remaining = text.trim()
    val separators = listOf(". ", "! ", "? ", ".\n", "!\n", "?\n", "\n")
    while (remaining.length > maxChars) {
        var cut = -1
        for (sep in separators) {
            val idx = remaining.lastIndexOf(sep, maxChars)
            if (idx > cut) cut = idx + sep.length - 1
        }
        if (cut <= 0) {
            cut = remaining.lastIndexOf(' ', maxChars).takeIf { it > 0 } ?: maxChars
        }
        val chunkEnd = (cut + 1).coerceAtMost(remaining.length)
        out += remaining.substring(0, chunkEnd).trim()
        remaining = remaining.substring(chunkEnd).trim()
    }
    if (remaining.isNotEmpty()) out += remaining
    return out
}

/**
 * Deterministic per-speaker Gemini voice assignment: each distinct speaker
 * gets their own cloud voice (greedy, so no two speakers share a voice
 * unless there are more than 26 of them). Per-nick overrides win. The same
 * nick always maps to the same voice across quotes and sessions. Narration
 * turns (null nick) are not assigned here; callers use the selected
 * default cloud voice for those. The narrator voice (used for *action*
 * lines) is reserved up front so no speaker ever shares it. v1.62: when
 * [autoAssign] is false, only explicit per-nick overrides are mapped;
 * unmapped speakers fall back to the default cloud voice at call sites.
 */
fun geminiVoiceMapForSpeakers(
    settings: GeminiTtsSettings,
    speakers: List<String?>,
    /** v1.62: when false, only explicit per-nick overrides are mapped. */
    autoAssign: Boolean = settings.currentAutoAssignSpeakerVoices()
): Map<String, String> {
    val distinct = speakers.mapNotNull { it?.takeIf { n -> n.isNotBlank() } }.distinct()
    val overrides = settings.currentNickGeminiVoiceOverrides()
    val map = LinkedHashMap<String, String>()
    // Reserve the narrator voice: *action* lines always sound different
    // from every speaker.
    val used = mutableSetOf(settings.currentNarratorVoice())
    // Pass 1: explicit per-nick overrides.
    for (s in distinct) {
        val key = s.lowercase()
        val o = overrides[key]
        // v1.87: manual overrides may also name a voice-pack voice.
        if (!o.isNullOrBlank() && isKnownGeminiVoice(o) && o !in used) {
            map[key] = o
            used += o
        }
    }
    if (!autoAssign) return map
    // Pass 2: v1.63 gender-aware deterministic assignment. A nick that
    // reads female (male) prefers unused voices tagged with that gender,
    // then untagged flex voices, then anything free — speakers sound like
    // their nicks suggest while staying distinct from each other.
    // Unknown-gender nicks keep the legacy hash order over all voices.
    val flexVoices =
        GeminiPrebuiltVoices.filter { geminiVoiceGender(it) == NickGender.UNKNOWN }
    for (s in distinct) {
        val key = s.lowercase()
        if (key in map) continue
        val want = guessNickGender(s)
        val ordered = when (want) {
            NickGender.UNKNOWN -> GeminiPrebuiltVoices
            else -> (GeminiPrebuiltVoices.filter { geminiVoiceGender(it) == want }
                    + flexVoices + GeminiPrebuiltVoices).distinct()
        }
        val start = Math.floorMod(key.hashCode(), ordered.size)
        var chosen: String? = null
        for (i in ordered.indices) {
            val v = ordered[(start + i) % ordered.size]
            if (v !in used) {
                chosen = v
                break
            }
        }
        val voice = chosen ?: ordered[start]
        map[key] = voice
        used += voice
    }
    return map
}

/**
 * v1.64.2: builds the exact Gemini segments read-aloud would synthesize for
 * [text] (shared by playback call sites and eager regeneration, so a
 * regenerated recording always matches what playing the quote produces).
 */
fun buildGeminiSegments(
    text: String,
    dialogueBreaks: Boolean,
    settings: GeminiTtsSettings,
    perNickVoices: Boolean
): List<GeminiTtsSegment> {
    if (!perNickVoices) {
        return listOf(
            GeminiTtsSegment(
                nick = null,
                text = buildSpokenQuoteText(text, dialogueBreaks),
                voice = settings.currentVoice()
            )
        )
    }
    // Per-speaker cloud voices: each turn is synthesized with that
    // speaker's own Gemini voice (or their chosen override). When 2+
    // speakers are identified by their voices, the "says"/"replies"
    // announcements are dropped; narration lines keep the selected default
    // cloud voice.
    val previewTurns = buildSpokenTurns(text, dialogueBreaks)
    val speakers = previewTurns.map { it.nick }
    val turns = if (dropSpeakerAnnouncements(speakers))
        buildSpokenTurns(text, dialogueBreaks, announceSpeakers = false)
    else previewTurns
    val voiceMap = geminiVoiceMapForSpeakers(settings, speakers)
    val narratorVoice = settings.currentNarratorVoice()
    return turns.map { turn ->
        GeminiTtsSegment(
            nick = turn.nick,
            text = turn.text,
            voice = when {
                // *Action* lines use the dedicated narrator voice, always
                // distinct from every speaker voice.
                turn.isAction -> narratorVoice
                else -> turn.nick?.lowercase()?.let { voiceMap[it] }
                    ?: settings.currentVoice()
            },
            // v1.60: labels for the multi-speaker request.
            speakerLabel = when {
                turn.isAction -> "Directions"
                turn.nick.isNullOrBlank() -> "Narrator"
                else -> null
            }
        )
    }
}

/** The Gemini cloud voice for one speaker nick (override-aware, deterministic). */
fun geminiVoiceForNick(settings: GeminiTtsSettings, nick: String?): String {
    if (nick.isNullOrBlank()) return settings.currentVoice()
    return geminiVoiceMapForSpeakers(settings, listOf(nick))[nick.lowercase()]
        ?: settings.currentVoice()
}

/**
 * True when per-speaker Gemini voices identify the speakers: at least two
 * distinct speakers whose assigned cloud voices differ, so the
 * "says"/"replies" narration can be dropped.
 */
fun geminiVoicesIdentifySpeakers(
    settings: GeminiTtsSettings,
    nicks: List<String?>
): Boolean {
    val speakers = nicks.filter { !it.isNullOrBlank() }.distinct()
    if (speakers.size < 2) return false
    return geminiVoiceMapForSpeakers(settings, speakers).values.distinct().size >= 2
}

/** One read-aloud segment: the spoken text plus the voice to synthesize it with. */
data class GeminiTtsSegment(
    /** Speaker nick, or null for narration lines. */
    val nick: String?,
    val text: String,
    val voice: String,
    /**
     * v1.60: hint for the multi-speaker label (e.g. "Narrator" for
     * narration lines, "Directions" for *action* lines). Null = derive
     * from [nick].
     */
    val speakerLabel: String? = null
)

/** v1.60: max distinct voices in one multi-speaker request (API limit). */
private const val GEMINI_MAX_SPEAKERS_PER_REQUEST = 2

/** v1.60: on-disk TTS audio cache cap (LRU by file mtime). */
private const val GEMINI_TTS_CACHE_MAX_BYTES = 50L * 1024 * 1024

/** v1.60: bump when the cache key scheme changes. */
private const val GEMINI_TTS_CACHE_VERSION = "v1"

/** v1.60: one synthesized piece of a read: exact text plus its voices. */
private data class TtsRequestChunk(
    /** Text to send: "Label: ..." lines for multi-voice, plain for single. */
    val text: String,
    /** Speaker label to voice name, in order of appearance. */
    val speakerVoices: List<Pair<String, String>>,
    /** The segments this chunk covers (for single-voice fallback). */
    val segments: List<GeminiTtsSegment>
)

/**
 * v1.60: groups segments into runs of at most [maxVoices] distinct voices,
 * so each run fits in one multi-speaker request. Blank segments are dropped.
 * A 2-person dialogue becomes ONE request instead of one per turn.
 */
private fun partitionVoiceRuns(
    segments: List<GeminiTtsSegment>,
    maxVoices: Int = GEMINI_MAX_SPEAKERS_PER_REQUEST
): List<List<GeminiTtsSegment>> {
    val runs = mutableListOf<MutableList<GeminiTtsSegment>>()
    for (seg in segments) {
        if (seg.text.isBlank()) continue
        val current = runs.lastOrNull()
        val voices = current?.map { it.voice }?.toSet() ?: emptySet()
        if (current != null && (seg.voice in voices || voices.size < maxVoices)) {
            current += seg
        } else {
            runs += mutableListOf(seg)
        }
    }
    return runs
}

/** v1.60: speaker labels must be alphanumeric for the multi-speaker API. */
private fun sanitizeSpeakerLabel(raw: String, fallback: String): String =
    raw.filter { it.isLetterOrDigit() }.take(32).ifEmpty { fallback }

/**
 * v1.60: assigns each distinct voice in [run] a unique speaker label.
 * Prefers the segment's [GeminiTtsSegment.speakerLabel] hint ("Narrator",
 * "Directions"), then the nick, then a generic fallback.
 */
private fun assignSpeakerLabels(run: List<GeminiTtsSegment>): Map<String, String> {
    val labels = LinkedHashMap<String, String>()
    var generic = 0
    for (seg in run) {
        if (seg.voice in labels) continue
        generic++
        val base = sanitizeSpeakerLabel(
            seg.speakerLabel?.takeIf { it.isNotBlank() }
                ?: seg.nick?.takeIf { it.isNotBlank() }
                ?: "",
            "Voice$generic"
        )
        var label = base
        var n = 2
        while (labels.values.contains(label)) {
            label = "$base$n"
            n++
        }
        labels[seg.voice] = label
    }
    return labels
}

/**
 * v1.60: splits a voice run into request chunks of at most [maxChars].
 * Single-voice runs use plain text (legacy voiceConfig path); multi-voice
 * runs use "Label: text" lines (multiSpeakerVoiceConfig path). Lines are
 * never split mid-segment.
 */
private fun chunkVoiceRun(
    run: List<GeminiTtsSegment>,
    maxChars: Int = GEMINI_MAX_CHARS_PER_REQUEST
): List<TtsRequestChunk> {
    val nonEmpty = run.filter { it.text.isNotBlank() }
    if (nonEmpty.isEmpty()) return emptyList()
    val labels = assignSpeakerLabels(nonEmpty)
    val voices = nonEmpty.map { it.voice }.distinct()
    if (voices.size <= 1) {
        val voice = voices.first()
        val plain = nonEmpty.joinToString("\n") { it.text.trim() }.trim()
        if (plain.isEmpty()) return emptyList()
        return chunkTextForTts(plain, maxChars).map { piece ->
            TtsRequestChunk(piece, listOf("Voice" to voice), nonEmpty)
        }
    }
    val out = mutableListOf<TtsRequestChunk>()
    val current = StringBuilder()
    var currentSegs = mutableListOf<GeminiTtsSegment>()
    val usedVoices = LinkedHashSet<String>()
    fun flush() {
        if (currentSegs.isNotEmpty()) {
            val sv = usedVoices.map { v -> labels.getValue(v) to v }
            out += TtsRequestChunk(
                current.toString().trimEnd(),
                sv,
                currentSegs.toList()
            )
            current.clear()
            currentSegs = mutableListOf()
            usedVoices.clear()
        }
    }
    for (seg in nonEmpty) {
        val line = "${labels.getValue(seg.voice)}: ${seg.text.trim()}\n"
        if (line.length > maxChars) {
            // Pathological single segment: send it alone via the
            // single-voice path rather than an unlabeled multi-speaker line.
            flush()
            for (piece in chunkTextForTts(seg.text.trim(), maxChars)) {
                out += TtsRequestChunk(
                    piece,
                    listOf(labels.getValue(seg.voice) to seg.voice),
                    listOf(seg)
                )
            }
            continue
        }
        if (current.length + line.length > maxChars && currentSegs.isNotEmpty()) flush()
        current.append(line)
        currentSegs += seg
        usedVoices += seg.voice
    }
    flush()
    return out
}

/**
 * Gemini cloud TTS player: sends text to the Gemini TTS REST API
 * (authenticated with the user's own API key) and streams the returned
 * 16-bit PCM mono 24 kHz audio through an [AudioTrack].
 *
 * All network and audio work happens on an internal IO scope; [onDone] and
 * [onError] are always invoked on the main thread. Call [stop] to halt
 * playback immediately (used by the read-aloud sleep timer); [release]
 * frees the player for good.
 */
class GeminiTts(context: Context) {
    private val appContext = context.applicationContext
    init { VoiceRecordings.ensure(appContext) }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    /**
     * v1.74.8: cache files written by a synthesis that hasn't been filed
     * in the voice-recordings manifest yet. The LRU prune must not evict
     * these mid-synthesis (the manifest only protects them after filing).
     * Guarded by [lock]; cleared when the synthesis ends.
     */
    private val inProgressTtsFiles = mutableSetOf<String>()

    @Volatile
    private var stopped = false
    private var currentJob: Job? = null
    private var track: AudioTrack? = null

    /** True while a speak job is actively running. */
    fun isPlaying(): Boolean = currentJob?.isActive == true

    /**
     * Speaks [text] with the Gemini cloud [voice]. Long text is split into
     * ~4000-char chunks and spoken with sequential API calls into one
     * continuous stream. Any in-progress playback is stopped first.
     *
     * [onError] also reports whether any audio already played
     * ([playedAny]): callers must not re-read the whole text with a
     * fallback voice when part of it was already heard.
     */
    fun speak(
        text: String,
        apiKey: String,
        voice: String,
        onDone: () -> Unit,
        onError: (message: String, playedAny: Boolean) -> Unit,
        /** v1.61.20: when set, the synthesis is filed in the voice-recordings manifest. */
        quoteDbId: String? = null,
        quoteNumber: Int? = null
    ) {
        speakSegments(
            listOf(GeminiTtsSegment(null, text, voice)),
            apiKey,
            onDone,
            onError,
            quoteDbId = quoteDbId,
            quoteNumber = quoteNumber
        )
    }

    /**
     * Speaks [segments] in order through one continuous audio stream: each
     * segment is synthesized with its own cloud voice (used for per-speaker
     * voices). Any in-progress playback is stopped first. See [speak] for
     * the [onError] [playedAny] contract.
     */
    fun speakSegments(
        segments: List<GeminiTtsSegment>,
        apiKey: String,
        onDone: () -> Unit,
        onError: (message: String, playedAny: Boolean) -> Unit,
        /** v1.61.20: when both are set, the finished synthesis is filed in the voice-recordings manifest. */
        quoteDbId: String? = null,
        quoteNumber: Int? = null
    ) {
        stop()
        if (apiKey.isBlank()) {
            postError(
                onError,
                "No Gemini API key set. Add your key in Settings \u2192 Read aloud " +
                    "(get one free in Google AI Studio).",
                playedAny = false
            )
            return
        }
        stopped = false
        currentJob = scope.launch {
            // Whether any audio reached the speaker before a failure.
            // Callers use this to avoid re-reading already-heard text.
            var playedAnyAudio = false
            // v1.74.8: hoisted out of the try so the finally below can
            // release the in-progress prune protection for these files.
            val manifestFiles = mutableListOf<String>()
            try {
                // v1.60: voice runs — a 2-person dialogue becomes ONE
                // multi-speaker request instead of one per turn, and repeats
                // are served from the disk cache without touching the quota.
                // (The free tier is ~3 requests/minute and ~15/day; the old
                // per-turn requests exhausted that in a single read-aloud.)
                val runs = partitionVoiceRuns(segments)
                if (runs.isEmpty()) {
                    // Never silently "succeed": an empty segment list (e.g.
                    // every turn had blank text after multi-voice parsing)
                    // used to play nothing and report done. Report it as
                    // the failure it is so the UI can say so.
                    val who = segments.mapNotNull { it.nick }.distinct()
                        .takeIf { it.isNotEmpty() }?.joinToString(", ")
                    postError(
                        onError,
                        "Gemini read-aloud had nothing to speak" +
                            (if (who != null) " (speakers: $who)" else "") +
                            ". Every quote segment was empty after parsing — " +
                            "try turning Speaker voices off for this quote.",
                        playedAny = false
                    )
                    return@launch
                }
                val audioTrack = createTrack()
                synchronized(lock) { track = audioTrack }
                audioTrack.play()
                var totalFrames = 0L
                // v1.61.20: file the finished synthesis in the
                // voice-recordings manifest (per-quote management).
                // (manifestFiles is hoisted above the try so the finally
                // can release its prune protection.)
                val manifestVoices =
                    segments.map { it.voice }.distinct().joinToString(" + ")
                // v1.65: per-nick voices for the manifest, so a later
                // re-record can keep other nicks' existing audio.
                val manifestNickVoices = segments
                    .mapNotNull { seg -> seg.nick?.lowercase()?.let { it to seg.voice } }
                    .toMap()
                // Per-chunk resilience: one bad chunk (bad voice name, a
                // rejected segment) no longer kills the whole quote — the
                // failure is recorded and the rest still plays. Surviving
                // failures are reported at the end.
                val failures = mutableListOf<String>()
                var quotaMessage: String? = null
                // Once the multi-speaker API rejects a chunk, every later
                // chunk would be rejected too — fall back directly.
                var multiVoiceRejected = false

                /** Synthesizes one chunk and streams it; throws on failure. */
                fun synthChunk(chunk: TtsRequestChunk) {
                    val pcm = cachedTts(chunk, apiKey)
                    if (quoteDbId != null) manifestFiles += cacheFileFor(chunk).name
                    writeFully(audioTrack, pcm)
                    totalFrames += pcm.size / 2
                    if (pcm.isNotEmpty()) playedAnyAudio = true
                }

                /**
                 * v1.60: single-voice fallback for one chunk's segments,
                 * used when the multi-speaker request is rejected. Returns
                 * null on success or a failure description; rethrows
                 * GeminiQuotaException so quota still aborts everything.
                 */
                fun fallbackChunk(chunk: TtsRequestChunk): String? {
                    for (seg in chunk.segments) {
                        if (stopped) return "stopped"
                        for (piece in chunkTextForTts(seg.text.trim())) {
                            if (stopped) return "stopped"
                            val fbChunk = TtsRequestChunk(
                                piece,
                                listOf("Voice" to seg.voice),
                                listOf(seg)
                            )
                            val pcm = try {
                                cachedTts(fbChunk, apiKey)
                            } catch (e: GeminiQuotaException) {
                                throw e
                            } catch (e: GeminiTtsException) {
                                return "voice \"${seg.voice}\": " +
                                    (e.message ?: "request failed")
                            }
                            if (quoteDbId != null) manifestFiles += cacheFileFor(fbChunk).name
                            writeFully(audioTrack, pcm)
                            totalFrames += pcm.size / 2
                            if (pcm.isNotEmpty()) playedAnyAudio = true
                        }
                    }
                    return null
                }

                runLoop@ for ((runIndex, run) in runs.withIndex()) {
                    if (stopped) break
                    val chunks = chunkVoiceRun(run)
                    for ((chunkIndex, chunk) in chunks.withIndex()) {
                        if (stopped) break
                        val label = "run ${runIndex + 1}, chunk ${chunkIndex + 1} of ${chunks.size}"
                        // A previously rejected multi-speaker API goes
                        // straight to the single-voice fallback.
                        if (multiVoiceRejected && chunk.speakerVoices.size > 1) {
                            val fb = try {
                                fallbackChunk(chunk)
                            } catch (qe: GeminiQuotaException) {
                                quotaMessage = qe.message
                                break@runLoop
                            }
                            if (fb != null && fb != "stopped") {
                                failures += "$label (fallback): $fb"
                            }
                            continue
                        }
                        try {
                            synthChunk(chunk)
                        } catch (e: GeminiQuotaException) {
                            // Usually the per-minute limit during rapid
                            // reads — wait ~25s and retry once; a second
                            // 429 means the daily quota is really gone, so
                            // stop instead of burning more requests.
                            var recovered = false
                            var waited = 0
                            while (waited < 25 && !stopped) {
                                Thread.sleep(1000)
                                waited++
                            }
                            if (!stopped) {
                                try {
                                    synthChunk(chunk)
                                    recovered = true
                                } catch (e2: GeminiQuotaException) {
                                    quotaMessage = e2.message
                                } catch (e2: GeminiTtsException) {
                                    failures += "$label (retry): " +
                                        (e2.message ?: "request failed")
                                }
                            }
                            if (!recovered && quotaMessage != null) break@runLoop
                        } catch (e: GeminiTtsException) {
                            if (chunk.speakerVoices.size > 1 && !stopped) {
                                multiVoiceRejected = true
                                val fb = try {
                                    fallbackChunk(chunk)
                                } catch (qe: GeminiQuotaException) {
                                    quotaMessage = qe.message
                                    break@runLoop
                                }
                                if (fb != null && fb != "stopped") {
                                    failures += "$label (fallback): $fb"
                                }
                            } else {
                                val v = chunk.speakerVoices.firstOrNull()?.second
                                failures += "$label: voice \"$v\": " +
                                    (e.message ?: "request failed")
                            }
                        }
                    }
                }
                if (!stopped) {
                    waitUntilPlayed(audioTrack, totalFrames)
                    when {
                        quotaMessage != null -> postError(
                            onError,
                            quotaMessage!!,
                            playedAny = playedAnyAudio
                        )
                        failures.isNotEmpty() -> postError(
                            onError,
                            "Gemini read-aloud finished with ${failures.size} failed " +
                                "chunk(s):\n" + failures.joinToString("\n"),
                            // Part of the quote already played: the caller
                            // must not replay the whole thing.
                            playedAny = playedAnyAudio
                        )
                        else -> {
                            // v1.61.20: file it — a changed voice assignment
                            // yields new cache files, so the previous
                            // generation is retired into alternates.
                            if (quoteDbId != null && quoteNumber != null &&
                                manifestFiles.isNotEmpty()
                            ) {
                                VoiceRecordings.record(
                                    quoteDbId, quoteNumber,
                                    manifestFiles.distinct(), manifestVoices,
                                    manifestNickVoices
                                )
                            }
                            postDone(onDone)
                        }
                    }
                }
            } catch (e: CancellationException) {
                // stop() was called; stay silent.
            } catch (e: GeminiTtsException) {
                postError(
                    onError, e.message ?: "Gemini TTS failed.",
                    playedAny = playedAnyAudio
                )
            } catch (e: Exception) {
                postError(
                    onError,
                    "Gemini TTS error: ${e.message ?: e.javaClass.simpleName}",
                    playedAny = playedAnyAudio
                )
            } finally {
                releaseTrack()
                // v1.74.8: the synthesis is over — filed files are now
                // protected by the manifest; unfiled ones may be pruned.
                synchronized(lock) { inProgressTtsFiles.removeAll(manifestFiles) }
            }
        }
    }

    /**
     * v1.64.2: result of a silent (no-playback) synthesis: the cache files
     * to file in the voice-recordings manifest.
     */
    data class SilentSynthesis(
        /** Cache file names, in synthesis order. */
        val files: List<String>,
        /** Distinct voices used, joined for the manifest. */
        val voices: String,
        /**
         * v1.65: the voice each nick was synthesized with (lowercased
         * nick to voice name), filed in the recordings manifest.
         */
        val nickVoices: Map<String, String>
    )

    /**
     * v1.64.2: synthesizes [segments] WITHOUT playing anything. Warms the
     * disk cache with the same chunking, quota retry-once, and multi-voice
     * fallback as playback, then returns the cache files so the caller can
     * file them via [VoiceRecordings.record]. Throws [GeminiTtsException]
     * on failure. Never touches the AudioTrack or the shared stop flag, so
     * it is safe to run while something else is playing. [onProgress] may
     * be invoked off the main thread.
     */
    suspend fun synthesizeSilently(
        segments: List<GeminiTtsSegment>,
        apiKey: String,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
    ): SilentSynthesis = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) throw GeminiTtsException(
            "No Gemini API key set. Add your key in Settings \u2192 Read aloud " +
                "(get one free in Google AI Studio)."
        )
        val runs = partitionVoiceRuns(segments)
        if (runs.isEmpty()) throw GeminiTtsException(
            "Nothing to synthesize: every segment was empty."
        )
        val chunksByRun = runs.map { chunkVoiceRun(it) }
        val total = chunksByRun.sumOf { it.size }.coerceAtLeast(1)
        var done = 0
        val files = mutableListOf<String>()
        val failures = mutableListOf<String>()
        var quotaMessage: String? = null
        var multiVoiceRejected = false

        /** Synthesizes one chunk into the cache; throws on failure. */
        fun synthChunk(chunk: TtsRequestChunk) {
            val pcm = cachedTts(chunk, apiKey)
            if (pcm.isEmpty()) throw GeminiTtsException("Empty audio returned.")
            files += cacheFileFor(chunk).name
            onProgress(++done, total)
        }

        /**
         * Single-voice fallback for one chunk's segments (mirrors
         * playback). Returns null on success or a failure description;
         * rethrows [GeminiQuotaException] so quota still aborts everything.
         */
        fun fallbackChunk(chunk: TtsRequestChunk): String? {
            for (seg in chunk.segments) {
                for (piece in chunkTextForTts(seg.text.trim())) {
                    val fbChunk = TtsRequestChunk(
                        piece,
                        listOf("Voice" to seg.voice),
                        listOf(seg)
                    )
                    try {
                        val pcm = cachedTts(fbChunk, apiKey)
                        if (pcm.isEmpty()) throw GeminiTtsException(
                            "Empty audio returned."
                        )
                    } catch (e: GeminiQuotaException) {
                        throw e
                    } catch (e: GeminiTtsException) {
                        return "voice \"${seg.voice}\": " +
                            (e.message ?: "request failed")
                    }
                    files += cacheFileFor(fbChunk).name
                    onProgress(++done, total)
                }
            }
            return null
        }

        runLoop@ for ((runIndex, run) in chunksByRun.withIndex()) {
            for ((chunkIndex, chunk) in run.withIndex()) {
                // Cooperative cancellation between chunks (the blocking
                // network call itself is not interruptible).
                ensureActive()
                val label =
                    "run ${runIndex + 1}, chunk ${chunkIndex + 1} of ${run.size}"
                if (multiVoiceRejected && chunk.speakerVoices.size > 1) {
                    val fb = try {
                        fallbackChunk(chunk)
                    } catch (qe: GeminiQuotaException) {
                        quotaMessage = qe.message
                        break@runLoop
                    }
                    if (fb != null) failures += "$label (fallback): $fb"
                    continue
                }
                try {
                    synthChunk(chunk)
                } catch (e: GeminiQuotaException) {
                    // Mirror playback: wait ~25s and retry once; a second
                    // 429 means the daily quota is really gone.
                    delay(25000)
                    try {
                        synthChunk(chunk)
                    } catch (e2: GeminiQuotaException) {
                        quotaMessage = e2.message
                        break@runLoop
                    } catch (e2: GeminiTtsException) {
                        failures += "$label (retry): " +
                            (e2.message ?: "request failed")
                    }
                } catch (e: GeminiTtsException) {
                    if (chunk.speakerVoices.size > 1) {
                        multiVoiceRejected = true
                        val fb = try {
                            fallbackChunk(chunk)
                        } catch (qe: GeminiQuotaException) {
                            quotaMessage = qe.message
                            break@runLoop
                        }
                        if (fb != null) failures += "$label (fallback): $fb"
                    } else {
                        failures += "$label: voice " +
                            "\"${chunk.speakerVoices.firstOrNull()?.second}\": " +
                            (e.message ?: "request failed")
                    }
                }
            }
        }
        try {
            when {
                quotaMessage != null -> throw GeminiQuotaException(quotaMessage!!)
                failures.isNotEmpty() -> throw GeminiTtsException(
                    "Synthesis finished with ${failures.size} failed chunk(s):\n" +
                        failures.joinToString("\n")
                )
                files.isEmpty() -> throw GeminiTtsException(
                    "Synthesis produced no audio."
                )
            }
        } finally {
            // v1.74.8: the synthesis is over — filed files are protected
            // by the manifest once the caller records them; unfiled ones
            // may be pruned.
            synchronized(lock) { inProgressTtsFiles.removeAll(files.toSet()) }
        }
        SilentSynthesis(
            files = files.distinct(),
            voices = segments.map { it.voice }.distinct().joinToString(" + "),
            nickVoices = segments
                .mapNotNull { seg -> seg.nick?.lowercase()?.let { it to seg.voice } }
                .toMap()
        )
    }

    /**
     * Halts playback immediately: cancels the in-flight request and stops
     * the audio track. No [onDone]/[onError] callback fires after this.
     */
    fun stop() {
        stopped = true
        currentJob?.cancel()
        currentJob = null
        synchronized(lock) {
            try {
                track?.pause()
                track?.flush()
            } catch (e: Exception) {
                // Best effort; the track is released in the job's finally.
            }
        }
    }

    /** Stops playback and tears down the internal scope. */
    fun release() {
        stop()
        scope.coroutineContext[Job]?.cancel()
        releaseTrack()
    }

    private fun postDone(onDone: () -> Unit) {
        mainHandler.post { if (!stopped) onDone() }
    }

    private fun postError(
        onError: (message: String, playedAny: Boolean) -> Unit,
        message: String,
        playedAny: Boolean
    ) {
        mainHandler.post { if (!stopped) onError(message, playedAny) }
    }

    private fun createTrack(): AudioTrack {
        val channelConfig = AudioFormat.CHANNEL_OUT_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT
        val minBuf = AudioTrack.getMinBufferSize(GEMINI_SAMPLE_RATE, channelConfig, encoding)
        val bufSize = (minBuf * 4).coerceAtLeast(64 * 1024)
        return AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setSampleRate(GEMINI_SAMPLE_RATE)
                .setChannelMask(channelConfig)
                .setEncoding(encoding)
                .build(),
            bufSize,
            AudioTrack.MODE_STREAM,
            android.media.AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }

    /**
     * "Test connection" probe for Settings: synthesizes a short "Hello"
     * with [apiKey]/[voice]. Returns null on success, or the
     * user-presentable error message on failure. Switches to IO itself.
     */
    suspend fun testConnection(apiKey: String, voice: String): String? =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext "No Gemini API key set. Add your key in Settings " +
                    "\u2192 Read aloud (get one free in Google AI Studio)."
            }
            try {
                requestChunk("Hello", apiKey, voice)
                null
            } catch (e: GeminiTtsException) {
                e.message
            } catch (e: Exception) {
                FRIENDLY_NETWORK_ERROR
            }
        }

    /** One TTS request; returns the raw 16-bit PCM mono 24 kHz bytes. */
    private fun requestChunk(chunk: String, apiKey: String, voice: String): ByteArray =
        requestSingleVoice(chunk, apiKey, voice)

    /** v1.60: single-voice request body (legacy voiceConfig path). */
    private fun requestSingleVoice(text: String, apiKey: String, voice: String): ByteArray {
        // v1.87: voice-pack ids resolve to their base prebuilt voice here —
        // the API only accepts real prebuilt voice names.
        val concreteVoice = geminiVoiceForSynthesis(voice)
        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", text))
                    )
                )
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("responseModalities", JSONArray().put("AUDIO"))
                    .put(
                        "speechConfig",
                        JSONObject().put(
                            "voiceConfig",
                            JSONObject().put(
                                "prebuiltVoiceConfig",
                                JSONObject().put("voiceName", concreteVoice)
                            )
                        )
                    )
            )
        return try {
            executeChunk(openTtsConnection(apiKey), body)
        } catch (e: java.net.SocketException) {
            // Stale pooled keep-alive socket ("Software caused connection
            // abort"): retry once on a fresh connection that opts out of
            // keep-alive. ConnectException is a SocketException subclass,
            // so it retries too.
            executeChunk(openTtsConnection(apiKey, closeConnection = true), body)
        }
    }

    /**
     * v1.60: multi-speaker request: the whole dialogue in ONE call.
     * [speakerVoices] is speaker label to prebuilt voice name; the
     * transcript must prefix each turn with its label ("Joe: ...").
     * Labels are control markers, not spoken aloud.
     */
    private fun requestMultiVoice(
        transcript: String,
        speakerVoices: List<Pair<String, String>>,
        apiKey: String
    ): ByteArray {
        val configs = JSONArray()
        for ((label, voice) in speakerVoices) {
            // v1.87: resolve voice-pack ids to real prebuilt voice names.
            val concreteVoice = geminiVoiceForSynthesis(voice)
            configs.put(
                JSONObject()
                    .put("speaker", label)
                    .put(
                        "voiceConfig",
                        JSONObject().put(
                            "prebuiltVoiceConfig",
                            JSONObject().put("voiceName", concreteVoice)
                        )
                    )
            )
        }
        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", transcript))
                    )
                )
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("responseModalities", JSONArray().put("AUDIO"))
                    .put(
                        "speechConfig",
                        JSONObject().put(
                            "multiSpeakerVoiceConfig",
                            JSONObject().put("speakerVoiceConfigs", configs)
                        )
                    )
            )
        return try {
            executeChunk(openTtsConnection(apiKey), body)
        } catch (e: java.net.SocketException) {
            executeChunk(openTtsConnection(apiKey, closeConnection = true), body)
        }
    }

    /**
     * v1.61.18: the TTS audio cache lives in filesDir (persistent — the
     * system never clears it) instead of cacheDir. Files from the old
     * cacheDir location are moved over once, so nothing is re-fetched.
     * The cache is included in full backups (see BackupEngine).
     */
    private val ttsCacheDir: File by lazy {
        val dir = File(appContext.filesDir, "gemini_tts").apply { mkdirs() }
        try {
            val legacy = File(appContext.cacheDir, "gemini_tts")
            if (legacy.isDirectory) {
                legacy.listFiles()?.forEach { f ->
                    if (f.isFile) {
                        val dest = File(dir, f.name)
                        if (!dest.exists()) f.copyTo(dest)
                    }
                }
                legacy.deleteRecursively()
            }
        } catch (e: Exception) {
            // Migration is best-effort; synthesis just re-fetches.
        }
        dir
    }

    /**
     * v1.60: cache-aware synthesis. Replays, playlist repeats, and the
     * Settings test button hit the disk cache instead of the API — cached
     * audio never counts against the free quota.
     */
    /** v1.61.20: the on-disk cache file for a chunk (extracted so the manifest can name it). */
    private fun cacheFileFor(chunk: TtsRequestChunk): File {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val raw = "$GEMINI_TTS_CACHE_VERSION|$GEMINI_TTS_URL|" +
            chunk.speakerVoices.joinToString(",") { (l, v) -> "$l=$v" } +
            "|" + chunk.text
        val key = md.digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return File(ttsCacheDir, key)
    }

    private fun cachedTts(
        chunk: TtsRequestChunk,
        apiKey: String
    ): ByteArray {
        val dir = ttsCacheDir
        val file = cacheFileFor(chunk)
        if (file.exists()) {
            try {
                val cached = file.readBytes()
                if (cached.isNotEmpty()) return cached
            } catch (e: Exception) {
                // Fall through to the network request.
            }
        }
        val pcm = if (chunk.speakerVoices.size == 1) {
            requestSingleVoice(chunk.text, apiKey, chunk.speakerVoices[0].second)
        } else {
            requestMultiVoice(chunk.text, chunk.speakerVoices, apiKey)
        }
        try {
            if (pcm.isNotEmpty()) {
                file.writeBytes(pcm)
                // v1.74.8: protect it from the LRU prune until the
                // synthesis files it in the manifest (or ends).
                synchronized(lock) { inProgressTtsFiles += file.name }
                pruneTtsCache(dir)
            }
        } catch (e: Exception) {
            // Cache is best-effort; playback already has the audio.
        }
        return pcm
    }

    /** v1.60: LRU eviction for the TTS audio cache (best-effort). */
    private fun pruneTtsCache(dir: File) {
        try {
            // v1.61.20: never evict files the voice-recordings manifest
            // still points at (managed recordings, incl. alternates).
            // v1.74.8: also never evict files a synthesis is still
            // writing — they aren't in the manifest yet.
            val protected = VoiceRecordings.referencedFiles() +
                synchronized(lock) { inProgressTtsFiles.toSet() }
            val files = (dir.listFiles() ?: return).filter { it.name !in protected }
            var total = files.sumOf { it.length() }
            if (total <= GEMINI_TTS_CACHE_MAX_BYTES) return
            for (f in files.sortedBy { it.lastModified() }) {
                total -= f.length()
                f.delete()
                if (total <= GEMINI_TTS_CACHE_MAX_BYTES * 4 / 5) break
            }
        } catch (e: Exception) {
            // Best-effort.
        }
    }

    private fun openTtsConnection(
        apiKey: String,
        closeConnection: Boolean = false
    ): HttpURLConnection =
        (URL(GEMINI_TTS_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("x-goog-api-key", apiKey)
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (closeConnection) setRequestProperty("Connection", "close")
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 60_000
        }

    private fun executeChunk(conn: HttpURLConnection, body: JSONObject): ByteArray {
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                // v1.60: 429 aborts the whole read (see GeminiQuotaException).
                val msg = friendlyHttpError(code, readBody(conn))
                if (code == 429) throw GeminiQuotaException(msg)
                throw GeminiTtsException(msg)
            }
            val responseText = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            return extractAudio(responseText)
        } catch (e: GeminiTtsException) {
            throw e
        } catch (e: java.net.SocketException) {
            // Rethrown for the one retry in requestChunk; anything else
            // becomes the friendly message below.
            throw e
        } catch (e: java.io.IOException) {
            throw GeminiTtsException(FRIENDLY_NETWORK_ERROR)
        } finally {
            conn.disconnect()
        }
    }

    private fun readBody(conn: HttpURLConnection): String = try {
        (conn.errorStream ?: conn.inputStream)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: ""
    } catch (e: Exception) {
        ""
    }

    private fun friendlyHttpError(code: Int, body: String): String {
        val apiMessage = try {
            JSONObject(body).optJSONObject("error")?.optString("message")
        } catch (e: Exception) {
            null
        }.orEmpty()
        val detail = apiMessage.ifBlank { body.take(200) }.ifBlank { "no details" }
        return when (code) {
            400, 401, 403 -> "Gemini rejected the API key (HTTP $code): $detail " +
                "Check the key in Settings \u2192 Read aloud."
            429 -> "Gemini rate limit hit (HTTP 429): $detail " +
                "The free tier allows about 15 TTS requests per day. " +
                "Replaying cached audio doesn't use quota — try again " +
                "tomorrow, or switch to the built-in voice."
            else -> "Gemini TTS failed (HTTP $code): $detail"
        }
    }

    /** Concatenates every part's inlineData audio (base64 PCM) in order. */
    private fun extractAudio(responseText: String): ByteArray {
        val out = mutableListOf<Byte>()
        try {
            val root = JSONObject(responseText)
            val candidates = root.optJSONArray("candidates") ?: JSONArray()
            for (ci in 0 until candidates.length()) {
                val parts = candidates.optJSONObject(ci)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts") ?: continue
                for (pi in 0 until parts.length()) {
                    val data = parts.optJSONObject(pi)
                        ?.optJSONObject("inlineData")
                        ?.optString("data")
                    if (!data.isNullOrEmpty()) {
                        out += Base64.decode(data, Base64.DEFAULT).toList()
                    }
                }
            }
        } catch (e: Exception) {
            throw GeminiTtsException("Couldn't parse the Gemini response: ${e.message}")
        }
        if (out.isEmpty()) {
            throw GeminiTtsException("Gemini returned no audio. Try a shorter quote.")
        }
        return out.toByteArray()
    }

    private fun writeFully(audioTrack: AudioTrack, pcm: ByteArray) {
        var offset = 0
        while (offset < pcm.size) {
            if (stopped) return
            val written = audioTrack.write(pcm, offset, pcm.size - offset)
            if (written < 0) throw GeminiTtsException("Audio playback failed (write error $written).")
            if (written == 0) Thread.sleep(10)
            offset += written
        }
    }

    /** Waits until the streamed frames have actually been played. */
    private fun waitUntilPlayed(audioTrack: AudioTrack, totalFrames: Long) {
        if (totalFrames <= 0) return
        val expectedMs = totalFrames * 1000L / GEMINI_SAMPLE_RATE
        val deadline = System.currentTimeMillis() + expectedMs + 15_000L
        while (!stopped) {
            val played = try {
                audioTrack.playbackHeadPosition.toLong() and 0xFFFFFFFFL
            } catch (e: Exception) {
                return
            }
            if (played >= totalFrames) return
            if (System.currentTimeMillis() > deadline) return
            Thread.sleep(100)
        }
    }

    private fun releaseTrack() {
        synchronized(lock) {
            try {
                track?.release()
            } catch (e: Exception) {
                // Best effort.
            }
            track = null
        }
    }
}

/** One engine choice row in the settings card. */
@Composable
private fun EngineChoiceRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.padding(start = 8.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * "Read aloud" settings card for SettingsScreen: the voice engine selector
 * (Built-in TTS vs Gemini cloud), the Speaker voices toggle, the
 * password-style Gemini API key field, and the Gemini voice dropdown
 * (enabled only for the Gemini engine).
 */
/**
 * "Test connection" for the Gemini section: synthesizes a short "Hello"
 * with the saved key/voice and reports success or the friendly error
 * inline, so the key and network can be verified without playing a quote.
 */
@Composable
private fun TestConnectionRow(
    enabled: Boolean,
    apiKey: String,
    voice: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var testing by remember { mutableStateOf(false) }
    var succeeded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    Column {
        OutlinedButton(
            onClick = {
                testing = true
                succeeded = false
                errorMessage = null
                scope.launch {
                    val player = GeminiTts(context)
                    try {
                        val err = player.testConnection(apiKey.trim(), voice)
                        if (err == null) succeeded = true else errorMessage = err
                    } finally {
                        player.release()
                        testing = false
                    }
                }
            },
            enabled = enabled && !testing
        ) {
            Text(if (testing) "Testing\u2026" else "Test connection")
        }
        when {
            testing -> Text(
                "Contacting Google's speech server\u2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            succeeded -> Text(
                "\u2713 Connection OK \u2014 your key and voice work.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            errorMessage != null -> Text(
                errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
        // v1.60: the free tier is tiny (~3 requests/minute, ~15/day), so say
        // so — and note that replays are cached and don't count.
        Text(
            "Free API keys: about 3 requests/minute, 15/day. Replaying a " +
                "quote uses its cached audio and doesn't count.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsEngineSettingsCard(
    speakerVoices: Boolean,
    onSpeakerVoicesChange: (Boolean) -> Unit,
    onManageSpeakerVoices: () -> Unit = {}
) {
    val context = LocalContext.current
    val settings = remember { GeminiTtsSettings.get(context) }
    val engine by settings.engine.collectAsStateWithLifecycle()
    val apiKey by settings.apiKey.collectAsStateWithLifecycle()
    val voice by settings.voice.collectAsStateWithLifecycle()
    val lastError by settings.lastError.collectAsStateWithLifecycle()
    val fallbackToBuiltin by settings.fallbackToBuiltin.collectAsStateWithLifecycle()
    val autoAssignSpeakerVoices by settings.autoAssignSpeakerVoices.collectAsStateWithLifecycle()
    val isGemini = engine == TTS_ENGINE_GEMINI

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Voice engine", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(4.dp))
            EngineChoiceRow(
                selected = !isGemini,
                title = "Built-in TTS",
                subtitle = "Your device's text-to-speech voices (default)",
                onClick = { settings.setEngine(TTS_ENGINE_BUILTIN) }
            )
            EngineChoiceRow(
                selected = isGemini,
                title = "Gemini cloud",
                subtitle = "Google Gemini cloud voices \u2014 needs an API key",
                onClick = { settings.setEngine(TTS_ENGINE_GEMINI) }
            )
            Spacer(Modifier.height(8.dp))
            SettingToggle(
                title = "Speaker voices",
                subtitle = "Each speaker gets their own voice in multi-speaker quotes; " +
                    "\"says/replies\" narration is skipped when the voices identify the speaker.",
                checked = speakerVoices,
                onCheckedChange = onSpeakerVoicesChange
            )
            Spacer(Modifier.height(4.dp))
            // v1.63: per-nick voice assignment, also reachable from each
            // nick's page — no need to hunt the page down first.
            TextButton(onClick = onManageSpeakerVoices) {
                Text("Manage speaker voices")
            }
            Spacer(Modifier.height(4.dp))
            SettingToggle(
                title = "Auto-assign speaker voices",
                subtitle = "Applies when Speaker voices is on: speakers without " +
                    "a manual voice pick get their own voice automatically " +
                    "\u2014 the same nick always sounds the same. Turn off and " +
                    "unpicked speakers use the default voice instead.",
                checked = autoAssignSpeakerVoices,
                onCheckedChange = settings::setAutoAssignSpeakerVoices
            )
            Spacer(Modifier.height(8.dp))
            var keyVisible by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = apiKey,
                onValueChange = settings::setApiKey,
                label = { Text("Gemini API key") },
                placeholder = { Text("Paste key from Google AI Studio") },
                singleLine = true,
                visualTransformation = if (keyVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { keyVisible = !keyVisible }) {
                        Icon(
                            if (keyVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (keyVisible) "Hide API key" else "Show API key"
                        )
                    }
                },
                supportingText = {
                    Text(
                        "Stays on this device \u2014 it is only ever sent to Google's " +
                            "Gemini API. Get a free key in Google AI Studio."
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            var voiceExpanded by remember { mutableStateOf(false) }
            // v1.87: voice-pack voices appear alongside the prebuilt ones.
            val pluginGeminiVoices by rememberPluginVoiceOptions("gemini")
            val geminiVoiceChoices: List<Pair<String, String>> =
                GeminiPrebuiltVoices.map { it to it } + pluginGeminiVoices
            ExposedDropdownMenuBox(
                expanded = voiceExpanded,
                onExpandedChange = { if (isGemini) voiceExpanded = it }
            ) {
                OutlinedTextField(
                    value = if (voice.startsWith(PluginManager.DB_ID_PREFIX))
                        PluginManager.pluginVoiceDisplayName(voice)
                    else voice,
                    onValueChange = {},
                    readOnly = true,
                    enabled = isGemini,
                    label = { Text("Gemini voice") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = voiceExpanded,
                    onDismissRequest = { voiceExpanded = false }
                ) {
                    geminiVoiceChoices.forEach { (id, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { settings.setVoice(id); voiceExpanded = false }
                        )
                    }
                }
            }
            if (isGemini && apiKey.isBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Add your API key above to use Gemini cloud voices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(8.dp))
            TestConnectionRow(
                enabled = isGemini,
                apiKey = apiKey,
                voice = voice
            )
            Spacer(Modifier.height(8.dp))
            // v1.49: optional fallback to the built-in engine when Gemini
            // read-aloud fails (default ON), with a toast at fallback time.
            SettingToggle(
                title = "Fall back to built-in TTS",
                subtitle = "If Gemini read-aloud fails, read with the device's " +
                    "text-to-speech instead (with a toast).",
                checked = fallbackToBuiltin,
                onCheckedChange = settings::setFallbackToBuiltin
            )
            // v1.49: the last Gemini read-aloud failure stays visible here.
            // v1.55: the error text is long-press selectable, plus a Copy
            // button next to Clear so the error can be pasted elsewhere.
            val err = lastError
            if (err != null) {
                val clipboard = LocalClipboardManager.current
                Spacer(Modifier.height(4.dp))
                Text(
                    "Last error",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
                SelectionContainer {
                    Text(
                        err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row {
                    TextButton(onClick = {
                        clipboard.setText(AnnotatedString(err))
                        android.widget.Toast.makeText(
                            context,
                            "Error copied to clipboard",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text("Copy")
                    }
                    TextButton(onClick = { settings.clearLastError() }) {
                        Text("Clear")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            NarratorVoicePicker(settings = settings, isGemini = isGemini)
            Spacer(Modifier.height(8.dp))
            // v1.49: shortcut to the system TTS settings so more built-in
            // voices (e.g. other languages, higher quality) can be installed.
            // (The literal action string is used because the
            // TextToSpeech.Engine constant isn't in this compile classpath.)
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(
                            android.content.Intent(
                                "com.android.settings.TTS_SETTINGS"
                            )
                        )
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(
                            context,
                            "Couldn't open TTS settings",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Install more voices")
            }
        }
    }
}

/** Installed TTS voices for the narrator picker (built-in engine). */
@Composable
private fun rememberNarratorInstalledVoices(): List<android.speech.tts.Voice> {
    val context = LocalContext.current
    var voices by remember { mutableStateOf<List<android.speech.tts.Voice>>(emptyList()) }
    DisposableEffect(context) {
        var tts: android.speech.tts.TextToSpeech? = null
        tts = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                tts?.let { voices = installedVoices(it) }
            }
        }
        onDispose { tts?.shutdown() }
    }
    return voices
}

/**
 * "Narrator voice" picker (v1.47): the voice that reads *action* lines,
 * always distinct from every speaker voice. Gemini engine -> a cloud
 * voice; built-in engine -> an installed device voice (or Automatic).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NarratorVoicePicker(settings: GeminiTtsSettings, isGemini: Boolean) {
    val narratorVoice by settings.narratorVoice.collectAsStateWithLifecycle()
    val narratorBuiltin by settings.narratorVoiceBuiltin.collectAsStateWithLifecycle()
    val installed = rememberNarratorInstalledVoices()
    var expanded by remember { mutableStateOf(false) }
    // v1.87: voice-pack voices join the built-in and Gemini lists.
    val pluginGeminiVoices by rememberPluginVoiceOptions("gemini")
    val pluginBuiltinVoices by rememberPluginVoiceOptions("builtin")
    val options: List<Pair<String, String>>
    val current: String
    val label: String
    val onPick: (String) -> Unit
    if (isGemini) {
        options = GeminiPrebuiltVoices.map { it to it } + pluginGeminiVoices
        current = if (narratorVoice.startsWith(PluginManager.DB_ID_PREFIX))
            PluginManager.pluginVoiceDisplayName(narratorVoice)
        else narratorVoice
        label = "Narrator voice (Gemini)"
        onPick = { settings.setNarratorVoice(it) }
    } else {
        options = listOf("Automatic" to "Automatic") +
            installed.map { it.name to it.name } + pluginBuiltinVoices
        current = if (narratorBuiltin.startsWith(PluginManager.DB_ID_PREFIX))
            PluginManager.pluginVoiceDisplayName(narratorBuiltin)
        else narratorBuiltin.ifBlank { "Automatic" }
        label = "Narrator voice (built-in)"
        onPick = { settings.setNarratorVoiceBuiltin(if (it == "Automatic") "" else it) }
    }
    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = current,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
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
                options.forEach { (id, display) ->
                    DropdownMenuItem(
                        text = { Text(display) },
                        onClick = { onPick(id); expanded = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Reads *action* lines in read-aloud; always different from the speaker voices.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
