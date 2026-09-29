package com.quotebrowser.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Layout
import android.text.StaticLayout
import android.graphics.Typeface
import android.text.TextPaint
import com.quotebrowser.app.data.Quote
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * v1.49: category / Top-100 slideshow video export (MP4, H.264 + optional
 * AAC narration from the built-in TTS engine).
 *
 * Each quote becomes one 720x1280 slide rendered with android.graphics
 * (no Compose needed off the main thread). Without narration a slide
 * shows for 5 seconds; with narration the slide lasts as long as its
 * spoken audio plus a 1.5-second tail.
 */

/** One slideshow slide: the quote plus the label shown under its number. */
data class SlideItem(val quote: Quote, val sourceLabel: String)

private const val SLIDE_W = 720
private const val SLIDE_H = 1280
private const val SLIDE_MS_SILENT = 5_000L
private const val SLIDE_MS_NARR_TAIL = 1_500L
private const val VIDEO_BITRATE = 4_000_000
private const val VIDEO_FPS = 30
private const val AUDIO_SAMPLE_RATE = 22_050
private const val AUDIO_BITRATE = 96_000

/**
 * v1.63: animated background choices for the narrated slideshow video.
 */
enum class VideoBackground(val label: String) {
    MIDNIGHT("Midnight"),
    AURORA("Aurora"),
    STARFIELD("Starfield"),
    WAVES("Waves"),
    EMBER("Ember");

    companion object {
        fun fromLabel(label: String): VideoBackground =
            entries.firstOrNull { it.label == label } ?: MIDNIGHT
    }
}

/** Transparent 720x1280 chrome layer: header, divider, logo watermark, footer. */
private data class SlideChrome(val bitmap: Bitmap, val logoBottom: Float)

/**
 * Transparent 720x1280 body-text layer, plus the layout used for
 * karaoke word metrics. Word boxes are in layout coordinates; the
 * layout is drawn at (BODY_X, bodyTop) on the slide.
 */
private data class SlideBody(
    val bitmap: Bitmap,
    val layout: StaticLayout,
    val bodyTop: Float
)

private const val BODY_X = 48f

/** One word of the quote body with per-line screen segments. */
private data class WordSegment(
    val x0: Float, val x1: Float,
    val top: Float, val bottom: Float, val baseline: Float,
    val textStart: Int, val textEnd: Int
)

private data class TimedWord(
    val startMs: Long, val endMs: Long,
    val segments: List<WordSegment>
)

/**
 * v1.63: draws the animated background full-bleed. [tMs] is the time
 * since the slide started. Seeded per slide so renders are deterministic.
 */
private class BackgroundAnim(
    private val bg: VideoBackground,
    seed: Long
) {
    private val rng = java.util.Random(seed)
    private val basePaint = Paint()

    // Starfield state.
    private data class Star(val x: Float, val y: Float, val r: Float, val phase: Float, val speed: Float, val drift: Float)
    private val stars: List<Star> = List(130) {
        Star(
            x = rng.nextFloat() * SLIDE_W,
            y = rng.nextFloat() * SLIDE_H,
            r = 0.8f + rng.nextFloat() * 1.6f,
            phase = rng.nextFloat() * (2 * PI).toFloat(),
            speed = 0.8f + rng.nextFloat() * 2.4f,
            drift = 4f + rng.nextFloat() * 14f
        )
    }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

    // Aurora state.
    private data class Blob(val color: Int, val radius: Float, val cx0: Float, val cy0: Float, val ampX: Float, val ampY: Float, val speed: Float, val phase: Float)
    private val blobs: List<Blob> = listOf(
        Blob(Color.parseColor("#00E5FF"), 260f, 180f, 420f, 120f, 160f, 0.35f, 0f),
        Blob(Color.parseColor("#7C4DFF"), 300f, 560f, 760f, 140f, 120f, 0.27f, 2.1f),
        Blob(Color.parseColor("#FF4081"), 220f, 400f, 1080f, 110f, 140f, 0.42f, 4.2f)
    )

    // Ember state.
    private data class Ember(val x: Float, val seedY: Float, val speed: Float, val r: Float, val phase: Float, val color: Int)
    private val embers: List<Ember> = List(70) {
        Ember(
            x = rng.nextFloat() * SLIDE_W,
            seedY = rng.nextFloat() * (SLIDE_H + 80),
            speed = 30f + rng.nextFloat() * 90f,
            r = 1.2f + rng.nextFloat() * 2.6f,
            phase = rng.nextFloat() * (2 * PI).toFloat(),
            color = if (rng.nextBoolean()) Color.parseColor("#FF9800") else Color.parseColor("#FF5722")
        )
    }
    private val emberPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Waves state.
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private data class Wave(val color: Int, val alpha: Int, val width: Float, val midY: Float, val amp: Float, val freq: Float, val speed: Float, val phase: Float)
    private val waves: List<Wave> = listOf(
        Wave(Color.parseColor("#00E5FF"), 110, 4f, 420f, 46f, 0.011f, 1.6f, 0f),
        Wave(Color.parseColor("#7C4DFF"), 90, 3f, 640f, 60f, 0.008f, 1.1f, 1.7f),
        Wave(Color.parseColor("#FF4081"), 80, 3f, 860f, 52f, 0.010f, 1.35f, 3.4f),
        Wave(Color.parseColor("#2979FF"), 70, 5f, 220f, 38f, 0.013f, 1.9f, 5.1f)
    )

    // Cached midnight gradient.
    private val midnightShader = LinearGradient(
        0f, 0f, 0f, SLIDE_H.toFloat(),
        intArrayOf(Color.parseColor("#1A1F2E"), Color.parseColor("#0B0E17")),
        floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
    )
    private val midnightPaint = Paint().apply { shader = midnightShader }

    fun draw(c: Canvas, tMs: Long) {
        val t = tMs / 1000f
        when (bg) {
            VideoBackground.MIDNIGHT -> c.drawRect(0f, 0f, SLIDE_W.toFloat(), SLIDE_H.toFloat(), midnightPaint)
            VideoBackground.AURORA -> {
                basePaint.shader = null
                basePaint.color = Color.parseColor("#0A0E1A")
                c.drawRect(0f, 0f, SLIDE_W.toFloat(), SLIDE_H.toFloat(), basePaint)
                for (b in blobs) {
                    val cx = b.cx0 + b.ampX * sin(t * b.speed + b.phase)
                    val cy = b.cy0 + b.ampY * cos(t * b.speed * 0.8f + b.phase)
                    val shader = RadialGradient(
                        cx, cy, b.radius,
                        intArrayOf(0x66000000 or (b.color and 0x00FFFFFF), Color.TRANSPARENT),
                        floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
                    )
                    basePaint.shader = shader
                    c.drawCircle(cx, cy, b.radius, basePaint)
                }
                basePaint.shader = null
            }
            VideoBackground.STARFIELD -> {
                basePaint.shader = null
                basePaint.color = Color.parseColor("#05070D")
                c.drawRect(0f, 0f, SLIDE_W.toFloat(), SLIDE_H.toFloat(), basePaint)
                for (s in stars) {
                    val y = (s.y + t * s.drift) % SLIDE_H
                    val tw = 0.25f + 0.75f * abs(sin(t * s.speed + s.phase))
                    starPaint.alpha = (255 * tw).toInt()
                    c.drawCircle(s.x, y, s.r, starPaint)
                }
            }
            VideoBackground.WAVES -> {
                basePaint.shader = null
                basePaint.color = Color.parseColor("#0B1020")
                c.drawRect(0f, 0f, SLIDE_W.toFloat(), SLIDE_H.toFloat(), basePaint)
                val path = Path()
                for (w in waves) {
                    path.rewind()
                    var first = true
                    var x = 0f
                    while (x <= SLIDE_W) {
                        val y = w.midY + w.amp * sin(x * w.freq + t * w.speed + w.phase)
                        if (first) { path.moveTo(x, y); first = false } else path.lineTo(x, y)
                        x += 12f
                    }
                    wavePaint.color = w.color
                    wavePaint.alpha = w.alpha
                    wavePaint.strokeWidth = w.width
                    c.drawPath(path, wavePaint)
                }
            }
            VideoBackground.EMBER -> {
                basePaint.shader = null
                basePaint.color = Color.parseColor("#0D0805")
                c.drawRect(0f, 0f, SLIDE_W.toFloat(), SLIDE_H.toFloat(), basePaint)
                val span = SLIDE_H + 80f
                for (e in embers) {
                    var y = (e.seedY - t * e.speed) % span
                    if (y < 0) y += span
                    y -= 40f
                    val flicker = 0.35f + 0.65f * abs(sin(t * 3f + e.phase))
                    emberPaint.color = e.color
                    emberPaint.alpha = (200 * flicker).toInt()
                    c.drawCircle(e.x, y, e.r, emberPaint)
                }
            }
        }
    }
}

/**
 * Renders the slide chrome (header, divider, Quote404 logo watermark,
 * footer) as a transparent bitmap. Caller must recycle it.
 */
private fun renderSlideChrome(
    item: SlideItem,
    index: Int,
    total: Int,
    watermark: Bitmap? = null
): SlideChrome {
    val bmp = Bitmap.createBitmap(SLIDE_W, SLIDE_H, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 34f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9AA3B2")
        textSize = 26f
    }
    val linePaint = Paint().apply {
        color = Color.parseColor("#2A3350")
        strokeWidth = 2f
    }
    c.drawText("Quote404", BODY_X, 84f, headerPaint)
    c.drawText("${index + 1} / $total", SLIDE_W - 48f - subPaint.measureText("${index + 1} / $total"), 80f, subPaint)
    c.drawText("#${item.quote.number} · ${item.sourceLabel}", BODY_X, 128f, subPaint)
    c.drawLine(BODY_X, 158f, SLIDE_W - 48f, 158f, linePaint)

    if (watermark != null && !watermark.isRecycled) {
        val targetW = 560f
        val scale = targetW / watermark.width
        val targetH = watermark.height * scale
        val left = (SLIDE_W - targetW) / 2f
        val top = (SLIDE_H - targetH) / 2f
        val alphaPaint = Paint().apply { alpha = 40 }
        c.drawBitmap(
            watermark,
            null,
            RectF(left, top, left + targetW, top + targetH),
            alphaPaint
        )
    }

    c.drawLine(BODY_X, SLIDE_H - 96f, SLIDE_W - 48f, SLIDE_H - 96f, linePaint)
    val footerY = SLIDE_H - 52f
    c.drawText(
        "Quote404",
        (SLIDE_W - headerPaint.measureText("Quote404")) / 2f,
        footerY,
        subPaint
    )
    return SlideChrome(bmp, 200f)
}

/**
 * Renders the quote body text as a transparent bitmap and returns the
 * layout used, for karaoke word metrics. Caller must recycle it.
 */
private fun renderSlideBody(item: SlideItem): SlideBody {
    val bmp = Bitmap.createBitmap(SLIDE_W, SLIDE_H, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 44f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    // v1.88.21: "mind breaks" — same paragraph separation as the on-screen
    // cards. StaticLayout renders the blank lines as vertical breathing
    // room; karaoke word spans are computed against this laid-out text.
    val bodyPara = paragraphizeBody(item.quote.text)
    val layout = StaticLayout.Builder
        .obtain(bodyPara, 0, bodyPara.length, textPaint, SLIDE_W - 96)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(0f, 1.25f)
        .setIncludePad(true)
        .build()
    val availTop = 200f + 40f
    val availBottom = SLIDE_H - 130f
    val bodyTop = availTop + max(0f, (availBottom - availTop - layout.height) / 2f)
    c.save()
    c.translate(BODY_X, bodyTop)
    layout.draw(c)
    c.restore()
    return SlideBody(bmp, layout, bodyTop)
}

/**
 * Estimates per-word timings for karaoke highlighting from the narration
 * audio length. Words are weighted by length (longer words take longer
 * to say); timings cover the quote-body portion of the spoken text.
 */
private fun timeKaraokeWords(
    spoken: String,
    audioMs: Long,
    body: SlideBody
): List<TimedWord> {
    val bodyText = body.layout.text.toString()
    if (bodyText.isBlank() || audioMs <= 0) return emptyList()
    val spokenWords = spoken.split(Regex("\\s+")).filter { it.isNotEmpty() }
    // Spoken text is "Quote number N. <body>"; the body starts at word 3.
    val prefixWords = 3
    if (spokenWords.size < prefixWords) return emptyList()
    // Char spans of each body word, in body-text order.
    val spans = mutableListOf<Pair<Int, Int>>()
    var i = 0
    while (i < bodyText.length) {
        while (i < bodyText.length && bodyText[i].isWhitespace()) i++
        if (i >= bodyText.length) break
        val s = i
        while (i < bodyText.length && !bodyText[i].isWhitespace()) i++
        spans.add(s to i)
    }
    val totalWeight = spokenWords.sumOf { (it.length + 1).toLong() }.toDouble()
    if (totalWeight <= 0) return emptyList()
    var acc = spokenWords.take(prefixWords).sumOf { (it.length + 1).toLong() }.toDouble()
    val layout = body.layout
    return spans.mapIndexedNotNull { j, (s, e) ->
        val spokenWord = spokenWords.getOrNull(prefixWords + j) ?: return@mapIndexedNotNull null
        val w = (spokenWord.length + 1).toDouble()
        val startMs = (acc / totalWeight * audioMs).toLong()
        val endMs = ((acc + w) / totalWeight * audioMs).toLong()
        acc += w
        val line0 = layout.getLineForOffset(s)
        val line1 = layout.getLineForOffset(max(s, e - 1))
        val segments = (line0..line1).map { line ->
            val segS = if (line == line0) s else layout.getLineStart(line)
            val segE = if (line == line1) e else layout.getLineEnd(line)
            val x0 = layout.getPrimaryHorizontal(segS)
            val x1 = layout.getPrimaryHorizontal(segE)
            WordSegment(
                min(x0, x1), max(x0, x1),
                layout.getLineTop(line).toFloat(),
                layout.getLineBottom(line).toFloat(),
                layout.getLineBaseline(line).toFloat(),
                segS, segE
            )
        }
        TimedWord(startMs, endMs, segments)
    }
}

private val karaokeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#FFC400")
    alpha = 70
}
private val karaokeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#FFC400")
    textSize = 44f
    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
}

/**
 * Draws the karaoke highlight for the word active at [tMs]: a soft amber
 * rounded rect behind the word plus the word redrawn in amber.
 */
private fun drawKaraokeWord(
    c: Canvas,
    body: SlideBody,
    bodyText: String,
    word: TimedWord
) {
    for (seg in word.segments) {
        val left = BODY_X + seg.x0 - 4f
        val top = body.bodyTop + seg.top - 2f
        val right = BODY_X + seg.x1 + 4f
        val bottom = body.bodyTop + seg.bottom + 2f
        c.drawRoundRect(RectF(left, top, right, bottom), 10f, 10f, karaokeFillPaint)
        c.drawText(
            bodyText.substring(seg.textStart, seg.textEnd),
            BODY_X + seg.x0,
            body.bodyTop + seg.baseline,
            karaokeTextPaint
        )
    }
}

/**
 * Renders the slideshow to an MP4 file in the app cache directory.
 *
 * @param narrate when true, each slide is narrated with the built-in TTS
 *   engine (AAC audio track); otherwise the video is silent.
 * @param background v1.63: the animated background preset for the video.
 * @param karaoke v1.63: when true (and narrating), each quote word is
 *   highlighted in amber as it is read.
 * @throws Exception with a user-readable message when encoding fails.
 */
fun exportSlideshowVideo(
    context: Context,
    items: List<SlideItem>,
    narrate: Boolean,
    background: VideoBackground = VideoBackground.MIDNIGHT,
    karaoke: Boolean = true,
    onProgress: (done: Int, total: Int) -> Unit = { _, _ -> }
): File {
    require(items.isNotEmpty()) { "No quotes to export." }

    // 1. Narration first: its per-slide audio length sets each slide's
    //    on-screen duration.
    val spokenTexts = items.map { "Quote number ${it.quote.number}. ${it.quote.text}" }
    val pcmPerSlide: List<ShortArray> =
        if (narrate) synthesizeNarration(context, spokenTexts)
        else List(items.size) { ShortArray(0) }
    val durationsMs = items.indices.map { i ->
        val pcm = pcmPerSlide.getOrElse(i) { ShortArray(0) }
        if (narrate && pcm.isNotEmpty())
            (pcm.size * 1000L / AUDIO_SAMPLE_RATE) + SLIDE_MS_NARR_TAIL
        else SLIDE_MS_SILENT
    }

    val out = File(context.cacheDir, "slideshow_${System.currentTimeMillis()}.mp4")
    val coord = MuxCoordinator(out)
    if (!narrate) coord.expectNoAudio()

    // 2. Video encoder (H.264, surface input).
    val vFormat = MediaFormat.createVideoFormat(
        MediaFormat.MIMETYPE_VIDEO_AVC, SLIDE_W, SLIDE_H
    )
    vFormat.setInteger(
        MediaFormat.KEY_COLOR_FORMAT,
        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
    )
    vFormat.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BITRATE)
    vFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FPS)
    vFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
    val vEnc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    vEnc.configure(vFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    val surface = vEnc.createInputSurface()
    vEnc.start()

    var videoError: Exception? = null
    val dst = Rect(0, 0, SLIDE_W, SLIDE_H)
    // v1.61.8: Clint's Quote404 logo watermark for the exported video.
    val watermark = quote404WatermarkLogo(context)
    val videoThread = Thread {
        try {
            // v1.63: one reusable full-slide bitmap; each frame composites
            // the animated background, the chrome layer, the body text,
            // and the karaoke highlight at 30fps.
            val frameBmp = Bitmap.createBitmap(SLIDE_W, SLIDE_H, Bitmap.Config.ARGB_8888)
            val frameCanvas = Canvas(frameBmp)
            val frameStep = 1000L / VIDEO_FPS
            try {
                for ((i, item) in items.withIndex()) {
                    val chrome = renderSlideChrome(item, i, items.size, watermark)
                    val body = renderSlideBody(item)
                    val pcm = pcmPerSlide.getOrElse(i) { ShortArray(0) }
                    val audioMs =
                        if (narrate && pcm.isNotEmpty()) pcm.size * 1000L / AUDIO_SAMPLE_RATE
                        else 0L
                    val words =
                        if (karaoke && audioMs > 0) timeKaraokeWords(spokenTexts[i], audioMs, body)
                        else emptyList()
                    // v1.88.21: karaoke word spans index into the laid-out
                    // (paragraphized) body text — never the raw quote text.
                    val bodyText = body.layout.text.toString()
                    val anim = BackgroundAnim(background, 0x9E3779B9L + i)
                    try {
                        var elapsed = 0L
                        val dur = durationsMs[i]
                        while (elapsed < dur) {
                            anim.draw(frameCanvas, elapsed)
                            frameCanvas.drawBitmap(chrome.bitmap, 0f, 0f, null)
                            frameCanvas.drawBitmap(body.bitmap, 0f, 0f, null)
                            if (words.isNotEmpty()) {
                                val active = words.firstOrNull {
                                    elapsed >= it.startMs && elapsed < it.endMs
                                }
                                if (active != null) {
                                    drawKaraokeWord(frameCanvas, body, bodyText, active)
                                }
                            }
                            val canvas = surface.lockCanvas(null)
                            try {
                                canvas.drawBitmap(frameBmp, null, dst, null)
                            } finally {
                                surface.unlockCanvasAndPost(canvas)
                            }
                            drainEncoder(vEnc, coord, 0, false)
                            elapsed += frameStep
                            onProgress(i + 1, items.size)
                        }
                    } finally {
                        chrome.bitmap.recycle()
                        body.bitmap.recycle()
                    }
                }
            } finally {
                frameBmp.recycle()
            }
            vEnc.signalEndOfInputStream()
            drainEncoder(vEnc, coord, 0, true)
        } catch (e: Exception) {
            videoError = e
        } finally {
            try {
                vEnc.stop()
            } catch (_: Exception) {
            }
            try {
                vEnc.release()
            } catch (_: Exception) {
            }
            try {
                surface.release()
            } catch (_: Exception) {
            }
        }
    }

    // 3. Audio encoder (AAC) when narrating: each slide's PCM followed by
    //    silence padding so the audio matches the slide durations exactly.
    var audioError: Exception? = null
    val audioThread: Thread? = if (narrate) Thread {
        try {
            val aFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC, AUDIO_SAMPLE_RATE, 1
            )
            aFormat.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BITRATE)
            aFormat.setInteger(
                MediaFormat.KEY_AAC_PROFILE,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC
            )
            aFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            val aEnc = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            aEnc.configure(aFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            aEnc.start()
            try {
                var ptsUs = 0L
                val frameSamples = 1024
                for (i in items.indices) {
                    val pcm = pcmPerSlide.getOrElse(i) { ShortArray(0) }
                    val totalSamples =
                        (durationsMs[i] * AUDIO_SAMPLE_RATE / 1000).toInt()
                    var pos = 0
                    while (pos < totalSamples) {
                        val n = min(frameSamples, totalSamples - pos)
                        val inIdx = aEnc.dequeueInputBuffer(10_000)
                        if (inIdx < 0) continue
                        val inBuf = aEnc.getInputBuffer(inIdx)!!
                        inBuf.clear()
                        for (k in 0 until n) {
                            val s =
                                if (pos + k < pcm.size) pcm[pos + k] else 0.toShort()
                            inBuf.putShort(s)
                        }
                        aEnc.queueInputBuffer(inIdx, 0, n * 2, ptsUs, 0)
                        ptsUs += n * 1_000_000L / AUDIO_SAMPLE_RATE
                        pos += n
                        drainEncoder(aEnc, coord, 1, false)
                    }
                }
                val inIdx = aEnc.dequeueInputBuffer(10_000)
                if (inIdx >= 0) {
                    aEnc.queueInputBuffer(
                        inIdx, 0, 0, ptsUs,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                }
                drainEncoder(aEnc, coord, 1, true)
            } finally {
                try {
                    aEnc.stop()
                } catch (_: Exception) {
                }
                try {
                    aEnc.release()
                } catch (_: Exception) {
                }
            }
        } catch (e: Exception) {
            audioError = e
        }
    } else null

    videoThread.start()
    audioThread?.start()
    videoThread.join()
    audioThread?.join()
    coord.finish()

    videoError?.let { throw it }
    // A narration failure degrades to a silent video rather than failing
    // the whole export; the caller reports it when nothing else went wrong.
    if (narrate && audioError != null && !out.exists()) {
        throw audioError as Exception
    }
    if (!out.exists() || out.length() == 0L) {
        throw IllegalStateException("Video encoder produced no output")
    }
    return out
}

// ---------------------------------------------------------------------------
// v1.49 video mux helpers: MediaMuxer coordination across the encoder
// threads, TTS WAV synthesis, and encoder draining.
// ---------------------------------------------------------------------------

/** One muxer sample buffered until all track formats are known. */
private data class MuxSample(
    val track: Int,
    val data: ByteArray,
    val info: MediaCodec.BufferInfo
)

/**
 * Coordinates MediaMuxer across the video and audio encoder threads:
 * buffers samples until both track formats are known, then starts the
 * muxer and writes through.
 */
private class MuxCoordinator(out: File) {
    private val muxer = MediaMuxer(out.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private val lock = Any()
    private var videoTrack = -1
    private var audioTrack = -1
    private var audioExpected = true
    private var started = false
    private var stopped = false
    private val pending = ArrayDeque<MuxSample>()

    fun expectNoAudio() {
        synchronized(lock) {
            audioExpected = false
            maybeStart()
        }
    }

    fun onFormat(track: Int, format: MediaFormat) {
        synchronized(lock) {
            if (track == 0) videoTrack = muxer.addTrack(format)
            else audioTrack = muxer.addTrack(format)
            maybeStart()
        }
    }

    private fun maybeStart() {
        if (!started && videoTrack >= 0 && (!audioExpected || audioTrack >= 0)) {
            muxer.start()
            started = true
            while (pending.isNotEmpty()) {
                val s = pending.removeFirst()
                writeNow(s.track, s.data, s.info)
            }
        }
    }

    private fun writeNow(track: Int, data: ByteArray, info: MediaCodec.BufferInfo) {
        val idx = if (track == 0) videoTrack else audioTrack
        muxer.writeSampleData(idx, ByteBuffer.wrap(data), info)
    }

    fun writeSample(track: Int, buffer: ByteBuffer, info: MediaCodec.BufferInfo) {
        val data = ByteArray(info.size)
        val dup = buffer.duplicate()
        dup.position(info.offset)
        dup.limit(info.offset + info.size)
        dup.get(data)
        val copy = MediaCodec.BufferInfo().apply {
            set(0, data.size, info.presentationTimeUs, info.flags)
        }
        synchronized(lock) {
            if (started) writeNow(track, data, copy)
            else pending.add(MuxSample(track, data, copy))
        }
    }

    fun finish() {
        synchronized(lock) {
            if (stopped) return
            if (started) {
                try {
                    muxer.stop()
                } catch (_: Exception) {
                }
            }
            stopped = true
            try {
                muxer.release()
            } catch (_: Exception) {
            }
        }
    }
}

/** Drains an encoder into the mux coordinator. */
private fun drainEncoder(
    enc: MediaCodec,
    coord: MuxCoordinator,
    track: Int,
    endOfStream: Boolean
) {
    val info = MediaCodec.BufferInfo()
    while (true) {
        val idx = enc.dequeueOutputBuffer(info, 10_000)
        when {
            idx == MediaCodec.INFO_TRY_AGAIN_LATER -> if (endOfStream) break else return
            idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED ->
                coord.onFormat(track, enc.outputFormat)
            idx >= 0 -> {
                if (info.size > 0) {
                    coord.writeSample(track, enc.getOutputBuffer(idx)!!, info)
                }
                enc.releaseOutputBuffer(idx, false)
                if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
            }
        }
    }
}

private fun le32(b: ByteArray, off: Int): Int =
    (b[off].toInt() and 0xFF) or
        ((b[off + 1].toInt() and 0xFF) shl 8) or
        ((b[off + 2].toInt() and 0xFF) shl 16) or
        ((b[off + 3].toInt() and 0xFF) shl 24)

private fun le16s(b: ByteArray, off: Int): Short =
    ((b[off].toInt() and 0xFF) or ((b[off + 1].toInt() and 0xFF) shl 8)).toShort()

/**
 * Reads a WAV file (as written by TextToSpeech.synthesizeToFile) and
 * converts it to mono 22050 Hz 16-bit PCM.
 */
private fun wavToMono22050(file: File): ShortArray {
    val bytes = file.readBytes()
    if (bytes.size < 44) return ShortArray(0)
    // Walk RIFF chunks to find "fmt " and "data".
    var fmtOff = -1
    var dataOff = -1
    var dataLen = 0
    var pos = 12
    while (pos + 8 <= bytes.size) {
        val id = String(bytes, pos, 4, Charsets.US_ASCII)
        val len = le32(bytes, pos + 4)
        when (id) {
            "fmt " -> fmtOff = pos + 8
            "data" -> {
                dataOff = pos + 8
                dataLen = len
            }
        }
        if (fmtOff >= 0 && dataOff >= 0) break
        pos += 8 + len + (len and 1)
    }
    if (fmtOff < 0 || dataOff < 0) return ShortArray(0)
    val channels = le16s(bytes, fmtOff + 2).toInt()
    val sampleRate = le32(bytes, fmtOff + 4)
    val bits = le16s(bytes, fmtOff + 14).toInt()
    if (channels <= 0 || sampleRate <= 0 || (bits != 8 && bits != 16)) return ShortArray(0)
    val bytesPerSample = bits / 8
    val frameCount = min(
        dataLen / (bytesPerSample * channels),
        (bytes.size - dataOff) / (bytesPerSample * channels)
    )
    if (frameCount <= 0) return ShortArray(0)
    // Decode to mono float.
    val mono = FloatArray(frameCount)
    for (f in 0 until frameCount) {
        var sum = 0.0
        for (ch in 0 until channels) {
            val o = dataOff + (f * channels + ch) * bytesPerSample
            val v = if (bits == 16) le16s(bytes, o) / 32768.0
            else (bytes[o].toInt() and 0xFF) / 255.0 * 2 - 1
            sum += v
        }
        mono[f] = (sum / channels).toFloat()
    }
    // Resample to 22050 Hz with linear interpolation.
    val target = 22050.0
    if (sampleRate == 22050) {
        return ShortArray(mono.size) {
            (mono[it] * 32767).toInt().coerceIn(-32768, 32767).toShort()
        }
    }
    val ratio = sampleRate / target
    val outLen = (mono.size / ratio).toInt()
    return ShortArray(outLen) { i ->
        val srcPos = i * ratio
        val i0 = srcPos.toInt().coerceIn(0, mono.size - 1)
        val i1 = (i0 + 1).coerceIn(0, mono.size - 1)
        val frac = (srcPos - i0).toFloat()
        val v = mono[i0] * (1 - frac) + mono[i1] * frac
        (v * 32767).toInt().coerceIn(-32768, 32767).toShort()
    }
}

/**
 * Synthesizes each text to 16-bit mono 22050 Hz PCM with the built-in
 * TTS engine. A slide that fails to synthesize degrades to silence.
 * v1.74.5: internal so the audio export can reuse it.
 *
 * v1.74.6: [strict] mode for the audio export — instead of degrading to
 * silence, every failure throws with a diagnosable message (engine
 * rejected the request, timed out, or produced no usable audio), so a
 * broken TTS setup surfaces as an error instead of a tiny empty file.
 */
internal fun synthesizeNarration(
    context: Context,
    texts: List<String>,
    strict: Boolean = false
): List<ShortArray> {
    val initLatch = CountDownLatch(1)
    val initStatus = intArrayOf(TextToSpeech.ERROR)
    val tts = TextToSpeech(context.applicationContext) { status ->
        initStatus[0] = status
        initLatch.countDown()
    }
    try {
        if (!initLatch.await(15, TimeUnit.SECONDS) || initStatus[0] != TextToSpeech.SUCCESS) {
            throw IllegalStateException("Text-to-speech engine unavailable")
        }
        if (strict) {
            // v1.74.6: missing voice data makes the engine "succeed" with
            // no audio — catch it here with an actionable message.
            val langStatus = try {
                tts.isLanguageAvailable(java.util.Locale.getDefault())
            } catch (_: Exception) {
                TextToSpeech.LANG_AVAILABLE
            }
            if (langStatus == TextToSpeech.LANG_MISSING_DATA) {
                throw IllegalStateException(
                    "Text-to-speech has no voice data for " +
                        "${java.util.Locale.getDefault().displayName} — " +
                        "install it in system Settings > Language."
                )
            }
            if (langStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
                throw IllegalStateException(
                    "Text-to-speech doesn't support " +
                        java.util.Locale.getDefault().displayName + "."
                )
            }
        }
        return texts.map { text ->
            val id = UUID.randomUUID().toString()
            val done = CountDownLatch(1)
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String) {}
                override fun onDone(utteranceId: String) {
                    done.countDown()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String) {
                    done.countDown()
                }

                override fun onError(utteranceId: String, errorCode: Int) {
                    done.countDown()
                }
            })
            val wav = File(context.cacheDir, "narr_$id.wav")
            try {
                val rc = tts.synthesizeToFile(text, null, wav, id)
                if (strict && rc != TextToSpeech.SUCCESS) {
                    throw IllegalStateException(
                        "Text-to-speech couldn't start synthesis (error $rc)."
                    )
                }
                val finished = done.await(180, TimeUnit.SECONDS)
                if (strict && !finished) {
                    throw IllegalStateException("Text-to-speech timed out.")
                }
                val wavBytes = if (wav.exists()) wav.length() else 0L
                val pcm = if (wav.exists()) wavToMono22050(wav) else ShortArray(0)
                if (strict && pcm.size < MIN_STRICT_TTS_SAMPLES) {
                    // ~empty synthesis: the v1.74.5 bug produced a
                    // headers-only M4A from this instead of failing.
                    throw IllegalStateException(
                        "Text-to-speech produced no audio " +
                            "(${wavBytes} byte wav, ${pcm.size} samples)."
                    )
                }
                pcm
            } catch (e: Exception) {
                if (strict) throw e
                ShortArray(0)
            } finally {
                wav.delete()
            }
        }
    } finally {
        try {
            tts.shutdown()
        } catch (_: Exception) {
        }
    }
}

/** v1.74.6: strict-mode floor — a quarter second of 22050 Hz audio. */
private const val MIN_STRICT_TTS_SAMPLES = 5512
