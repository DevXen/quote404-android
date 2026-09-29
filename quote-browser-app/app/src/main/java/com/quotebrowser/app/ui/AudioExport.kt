package com.quotebrowser.app.ui

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import com.quotebrowser.app.data.Quote
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * v1.74.5: narrated audio export.
 *
 * Synthesizes quote text with the built-in TTS engine (the same path the
 * narrated slideshow video uses), concatenates the clips with a short
 * silence gap, and encodes to AAC inside an M4A container with the
 * framework MediaCodec/MediaMuxer — no native dependencies.
 *
 * Android has no built-in MP3 encoder, so this ships M4A/AAC, which plays
 * everywhere MP3 does. Per-nick voices arrive in v1.76; until then the
 * default TTS voice reads everything.
 */

private const val AUDIO_EXPORT_SAMPLE_RATE = 22_050
private const val AUDIO_EXPORT_BITRATE = 96_000
/** Silence between quotes in a multi-quote file. */
private const val GAP_MS = 750

/**
 * Renders [texts] (already in spoken form, e.g. "Quote number 8. …") to
 * a temp M4A file. Throws with a user-readable message on failure; the
 * caller deletes the temp file after streaming it out.
 */
@Throws(Exception::class)
fun exportNarrationToM4a(context: Context, texts: List<String>): File {
    require(texts.isNotEmpty()) { "No quotes to export." }
    // v1.74.6: strict — a broken TTS setup throws a diagnosable error
    // instead of producing a headers-only (195-byte) M4A.
    val pcmClips = synthesizeNarration(context, texts, strict = true)
    if (pcmClips.all { it.isEmpty() }) {
        throw IllegalStateException("Text-to-speech produced no audio.")
    }
    val gapSamples = (AUDIO_EXPORT_SAMPLE_RATE * GAP_MS / 1000)
    val total = pcmClips.sumOf { it.size } + gapSamples * (pcmClips.size - 1).coerceAtLeast(0)
    // v1.74.6: never encode (and report success for) less than a second
    // of audio — the v1.74.5 bug wrote a headers-only M4A from a ~empty
    // clip and called it done.
    if (total < AUDIO_EXPORT_SAMPLE_RATE) {
        throw IllegalStateException("Text-to-speech produced no usable audio.")
    }
    val mixed = ShortArray(total)
    var pos = 0
    pcmClips.forEachIndexed { i, clip ->
        clip.copyInto(mixed, pos)
        pos += clip.size
        if (i < pcmClips.lastIndex) pos += gapSamples // gap stays silent (zeros)
    }
    val out = File.createTempFile("quotes_audio_", ".m4a", context.cacheDir)
    try {
        encodePcmToM4a(mixed, out)
    } catch (e: Exception) {
        out.delete()
        throw e
    }
    return out
}

/** Spoken form for a quote, matching the narrated slideshow video. */
fun audioSpokenText(number: Int, text: String): String = "Quote number $number. $text"

/**
 * v1.74.9: exports a single quote's narration to M4A using the Gemini
 * voices already saved for the quote (per-nick voice assignments from the
 * voice-recordings manifest). Honors [announceNicks] ("X says:" /
 * "Y replies:") and [includeNumber] ("Quote number N.") options.
 *
 * Falls back to the system TTS path ([exportNarrationToM4a]) when no
 * Gemini API key is set or the quote has no saved voice assignments.
 * Throws with a user-readable message on failure.
 */
@Throws(Exception::class)
fun exportGeminiNarrationToM4a(
    context: Context,
    quote: Quote,
    dbId: String,
    announceNicks: Boolean,
    includeNumber: Boolean
): File {
    val settings = GeminiTtsSettings.get(context)
    val apiKey = settings.currentApiKey()
    // Saved per-nick voice assignments (lowercased nick -> voice name).
    val savedVoices = VoiceRecordings.recordings.value[
        VoiceRecordings.key(dbId, quote.number)
    ]?.active?.nickVoices.orEmpty()

    // Fall back to system TTS when Gemini isn't available.
    if (apiKey.isBlank() || savedVoices.isEmpty()) {
        val text = buildString {
            if (includeNumber) append("Quote number ${quote.number}. ")
            if (announceNicks) {
                // Use the spoken-turns builder for "says"/"replies".
                val turns = buildSpokenTurns(quote.text, true, true)
                append(turns.joinToString(" ") { it.text })
            } else {
                append(quote.text)
            }
        }
        return exportNarrationToM4a(context, listOf(text))
    }

    // Build Gemini segments with the saved per-nick voices.
    val turns = buildSpokenTurns(quote.text, true, announceNicks)
    val defaultVoice = settings.currentVoice()
    val segments = mutableListOf<GeminiTtsSegment>()
    if (includeNumber) {
        segments += GeminiTtsSegment(
            nick = null,
            text = "Quote number ${quote.number}.",
            voice = defaultVoice
        )
    }
    for (turn in turns) {
        val voice = turn.nick?.lowercase()?.let { savedVoices[it] } ?: defaultVoice
        segments += GeminiTtsSegment(
            nick = turn.nick,
            text = turn.text,
            voice = voice
        )
    }

    val tts = GeminiTts(context)
    // Synthesize silently (no playback), get the cache files.
    val result = kotlinx.coroutines.runBlocking {
        tts.synthesizeSilently(segments, apiKey)
    }
    if (result.files.isEmpty()) {
        throw IllegalStateException("Gemini TTS produced no audio.")
    }
    // Read PCM from the cache files (24kHz, 16-bit mono).
    // Note: ttsCacheDir is filesDir/gemini_tts (not cacheDir).
    val cacheDir = File(context.filesDir, "gemini_tts")
    val pcmClips = result.files.map { name ->
        val file = File(cacheDir, name)
        if (!file.exists() || file.length() == 0L) {
            throw IllegalStateException("Missing audio data for export.")
        }
        val bytes = file.readBytes()
        // Convert little-endian bytes to shorts.
        ShortArray(bytes.size / 2) { i ->
            ((bytes[i * 2].toInt() and 0xFF) or
                ((bytes[i * 2 + 1].toInt() and 0xFF) shl 8)).toShort()
        }
    }
    if (pcmClips.all { it.isEmpty() }) {
        throw IllegalStateException("Gemini TTS produced no audio.")
    }
    // Concatenate with silence gaps, encode to M4A (24kHz).
    return encodePcmClipsToM4a(pcmClips, sampleRate = 24_000, context)
}

/**
 * v1.74.9: encodes PCM clips to M4A at the given sample rate.
 * Extracted from exportNarrationToM4a to support Gemini's 24kHz output.
 */
@Throws(Exception::class)
private fun encodePcmClipsToM4a(
    pcmClips: List<ShortArray>,
    sampleRate: Int,
    context: Context
): File {
    val gapSamples = (sampleRate * GAP_MS / 1000)
    val total = pcmClips.sumOf { it.size } + gapSamples * (pcmClips.size - 1).coerceAtLeast(0)
    if (total < sampleRate) {
        throw IllegalStateException("Text-to-speech produced no usable audio.")
    }
    val mixed = ShortArray(total)
    var pos = 0
    pcmClips.forEachIndexed { i, clip ->
        clip.copyInto(mixed, pos)
        pos += clip.size
        if (i < pcmClips.lastIndex) pos += gapSamples
    }
    val out = File.createTempFile("quotes_audio_", ".m4a", context.cacheDir)
    try {
        encodePcmToM4a(mixed, out, sampleRate)
    } catch (e: Exception) {
        out.delete()
        throw e
    }
    return out
}

/**
 * Encodes mono 16-bit [pcm] at 22050 Hz to AAC in an M4A container.
 */
private fun encodePcmToM4a(pcm: ShortArray, outFile: File, sampleRate: Int = AUDIO_EXPORT_SAMPLE_RATE) {
    val format = MediaFormat.createAudioFormat(
        MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 1
    ).apply {
        setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )
        setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_EXPORT_BITRATE)
        setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16 * 1024)
    }
    val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
    var muxer: MediaMuxer? = null
    try {
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false
        val bufferInfo = android.media.MediaCodec.BufferInfo()

        var pcmPos = 0
        var presentationUs = 0L
        var inputDone = false
        var outputDone = false
        // v1.74.6: bound the post-EOS drain so a misbehaving codec can't
        // spin here forever.
        var idleDrains = 0
        // AAC-LC frame = 1024 samples.
        val frameSamples = 1024
        while (!outputDone) {
            if (!inputDone) {
                val inIndex = codec.dequeueInputBuffer(10_000)
                if (inIndex >= 0) {
                    val inBuf = codec.getInputBuffer(inIndex)!!
                    inBuf.clear()
                    val remaining = pcm.size - pcmPos
                    if (remaining <= 0) {
                        codec.queueInputBuffer(
                            inIndex, 0, 0, presentationUs,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        inputDone = true
                    } else {
                        val n = minOf(remaining, frameSamples, inBuf.remaining() / 2)
                        // Write little-endian shorts.
                        val shortView = inBuf.order(ByteOrder.nativeOrder()).asShortBuffer()
                        shortView.put(pcm, pcmPos, n)
                        inBuf.position(n * 2)
                        inBuf.flip()
                        val durationUs = (n * 1_000_000L) / AUDIO_EXPORT_SAMPLE_RATE
                        codec.queueInputBuffer(inIndex, 0, n * 2, presentationUs, 0)
                        presentationUs += durationUs
                        pcmPos += n
                    }
                }
            }
            val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            when {
                outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    if (muxerStarted) throw IllegalStateException("Format changed twice.")
                    trackIndex = muxer!!.addTrack(codec.outputFormat)
                    muxer!!.start()
                    muxerStarted = true
                }
                outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (inputDone) {
                        // Give the encoder a beat, then keep draining.
                        Thread.sleep(5)
                        if (++idleDrains > 2000) {
                            throw IllegalStateException(
                                "Audio encoder stalled after end of stream."
                            )
                        }
                    }
                }
                outIndex >= 0 -> {
                    val outBuf = codec.getOutputBuffer(outIndex)!!
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                        // Codec config is handled by the muxer; skip it.
                        bufferInfo.size = 0
                    }
                    if (bufferInfo.size > 0 && muxerStarted) {
                        outBuf.position(bufferInfo.offset)
                        outBuf.limit(bufferInfo.offset + bufferInfo.size)
                        muxer!!.writeSampleData(trackIndex, outBuf, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outIndex, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                }
            }
        }
        if (!muxerStarted) throw IllegalStateException("Encoder produced no output.")
    } finally {
        try {
            codec.stop()
        } catch (_: Exception) {
        }
        codec.release()
        try {
            muxer?.stop()
        } catch (_: Exception) {
        }
        muxer?.release()
    }
    if (!outFile.exists() || outFile.length() == 0L) {
        outFile.delete()
        throw IllegalStateException("Audio encoding produced an empty file.")
    }
    // v1.74.6: a headers-only M4A (no audio frames) is ~195 bytes — the
    // v1.74.5 failure mode. Never report that as success.
    if (outFile.length() < 1024) {
        outFile.delete()
        throw IllegalStateException("Audio encoding produced an unusable file.")
    }
}
