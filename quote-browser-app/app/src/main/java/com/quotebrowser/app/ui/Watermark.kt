package com.quotebrowser.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.quotebrowser.app.R

/**
 * v1.61.8: Clint's Quote404 logo as a graphical watermark on every
 * exported image and video frame.
 *
 * The logo asset (quote404_logo) is his neon banner on a black
 * background. Drawn centered at ~10% opacity it reads as a classic
 * watermark: a faint glowing emblem on dark cards, a faint dark emblem
 * on light cards, without hurting the legibility of the quote text.
 */
private var cachedWatermarkLogo: Bitmap? = null

/**
 * Decodes Clint's Quote404 logo once and returns the cached bitmap, or
 * null when the resource can't be decoded (callers then skip the
 * watermark). Safe to call from any thread.
 */
@Synchronized
fun quote404WatermarkLogo(context: Context): Bitmap? {
    cachedWatermarkLogo?.let { if (!it.isRecycled) return it }
    return try {
        BitmapFactory.decodeResource(context.resources, R.drawable.quote404_logo)
            ?.also { cachedWatermarkLogo = it }
    } catch (_: Exception) {
        null
    }
}

/**
 * Draws the Quote404 logo [logo] centered on this canvas at low opacity.
 * Call as the final draw step of an export renderer so the watermark sits
 * over the finished card or frame. A null logo is a no-op.
 *
 * v1.61.15: superseded by [drawQuote404LogoOnArt] (logo right on the
 * background) and [drawQuote404LogoPlate] (slim flush plate) — the old
 * strip floated above the card and the logo still read too small. Kept
 * for other export paths.
 */
fun Canvas.drawQuote404Watermark(logo: Bitmap?) {
    if (logo == null || logo.isRecycled) return
    val w = width.toFloat()
    val h = height.toFloat()
    if (w <= 0f || h <= 0f) return
    // Banner at ~62% of the canvas width, aspect preserved.
    val targetW = w * 0.62f
    val scale = targetW / logo.width.toFloat()
    val targetH = logo.height.toFloat() * scale
    val left = (w - targetW) / 2f
    val top = (h - targetH) / 2f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        .apply { alpha = 26 }
    drawBitmap(logo, null, RectF(left, top, left + targetW, top + targetH), paint)
}

/**
 * v1.61.16: the logo plate height without drawing — callers that must
 * size their bitmap before they have a canvas (site cards, strips,
 * plain PNGs) compute the plate height up front, then pass it back as
 * [plateH] to [drawQuote404LogoPlate] so the drawn plate matches.
 */
fun quote404LogoPlateH(
    logo: Bitmap?,
    canvasW: Float,
    widthFrac: Float = 0.6f,
    // v1.74.7: was 26f — the tighter asset crop removed the dead black
    // rows, so the plate needs less padding to breathe.
    vPad: Float = 16f,
    drawTagline: Boolean = true
): Float {
    if (logo == null || logo.isRecycled || canvasW <= 0f) return 0f
    return quote404LogoBlockH(canvasW, widthFrac, drawTagline = drawTagline) + vPad * 2f
}

/**
 * v1.61.15: slim black logo plate for exports whose top is structured
 * content (site-theme cards, multi-quote strips, plain PNGs). The plate
 * sits flush against the content below it — no floating gap — and the
 * logo runs large ([widthFrac] of the canvas width) so every word stays
 * readable. v1.61.16: the plate now carries the wordmark plus the
 * tagline as vector text ([drawQuote404LogoBlock]). Pass an explicit
 * [plateH] (from [quote404LogoPlateH]) when the caller laid its bitmap
 * out around the plate; a non-positive value measures it here. Returns
 * the plate height (0 when there's no logo).
 */
fun Canvas.drawQuote404LogoPlate(
    logo: Bitmap?,
    widthFrac: Float = 0.6f,
    // v1.74.7: was 26f — see quote404LogoPlateH.
    vPad: Float = 16f,
    plateH: Float = -1f,
    drawTagline: Boolean = true
): Float {
    if (logo == null || logo.isRecycled) return 0f
    val w = width.toFloat()
    if (w <= 0f) return 0f
    val blockH = quote404LogoBlockH(w, widthFrac, drawTagline = drawTagline)
    val h = if (plateH > 0f) plateH else blockH + vPad * 2f
    if (h <= 0f) return 0f
    drawRect(0f, 0f, w, h, Paint().apply { color = 0xFF000000.toInt() })
    drawQuote404LogoBlock(logo, (h - blockH) / 2f, widthFrac, drawTagline = drawTagline)
    return h
}

/**
 * v1.61.15: draws Clint's Quote404 logo directly on the export's
 * background — centered and large ([widthFrac] of the canvas width) —
 * instead of a black strip floating above the card.
 *
 * v1.61.16: draws the logo block ([drawQuote404LogoBlock] — wordmark
 * plus vector tagline) at [top], with a soft scrim band behind it so
 * the neon banner melts into the art instead of showing a hard box
 * edge. The band feathers out above and below the block, so the logo
 * can sit anywhere on the card — e.g. just above the quote text.
 * Returns the y just below the block ([top] when there's no logo).
 */
fun Canvas.drawQuote404LogoOnArt(
    logo: Bitmap?,
    top: Float = 48f,
    widthFrac: Float = 0.68f
): Float {
    val w = width.toFloat()
    if (logo == null || logo.isRecycled || w <= 0f) return top
    val blockH = quote404LogoBlockH(w, widthFrac)
    // Soft scrim band: near-black just behind the block, feathering out
    // at both edges so the banner's black background melts into the art.
    // v1.61.17: the band hugs the logo now (Clint: the old one ballooned
    // way out around it) — just a short feather above and below.
    val bandTop = (top - 28f).coerceAtLeast(0f)
    val bandBottom = top + blockH + 28f
    val span = (bandBottom - bandTop).coerceAtLeast(1f)
    val scrim = Paint().apply {
        shader = LinearGradient(
            0f, bandTop, 0f, bandBottom,
            intArrayOf(
                android.graphics.Color.argb(0, 0, 0, 0),
                android.graphics.Color.argb(215, 0, 0, 0),
                android.graphics.Color.argb(215, 0, 0, 0),
                android.graphics.Color.argb(0, 0, 0, 0)
            ),
            floatArrayOf(
                0f,
                (top - bandTop) / span,
                (top + blockH - bandTop) / span,
                1f
            ),
            Shader.TileMode.CLAMP
        )
    }
    drawRect(0f, bandTop, w, bandBottom, scrim)
    return drawQuote404LogoBlock(logo, top, widthFrac)
}

/**
 * v1.61.16: Clint's logo asset geometry. quote404_logo.jpg is 1672x941.
 * v1.61.17: the old 555/941 cut sliced straight through the Q's tail —
 * the full emblem is included now. The tagline is drawn as crisp
 * letter-spaced vector text instead of a baked-in bitmap.
 * v1.74.7: the asset's rows above ~230 are empty black background — the
 * crop now starts at [LOGO_CROP_TOP_FRACTION] so the plate isn't mostly
 * dead black (Clint: "way too much black area on top"). The baked-in
 * tagline runs ~rows 555-595: [LOGO_WORDMARK_FRACTION] (600/941) keeps it
 * for the baked-tagline path, while [LOGO_WORDMARK_NO_TAGLINE_FRACTION]
 * (535/941) cuts just below the wordmark for the vector-tagline path so
 * the two taglines never double up.
 */
private const val LOGO_CROP_TOP_FRACTION = 230f / 941f
private const val LOGO_WORDMARK_FRACTION = 600f / 941f
private const val LOGO_WORDMARK_NO_TAGLINE_FRACTION = 535f / 941f
private const val LOGO_ASPECT = 1672f / 941f

/** v1.61.16: the tagline drawn under the wordmark on every export. */
const val QUOTE404_TAGLINE = "Funny quotes from everywhere."

/**
 * v1.61.16: the height of the logo block (wordmark + tagline) at
 * [widthFrac] of a [canvasW]-wide canvas, without drawing.
 */
fun quote404LogoBlockH(
    canvasW: Float,
    widthFrac: Float = 0.68f,
    taglineSize: Float = 40f,
    drawTagline: Boolean = true
): Float {
    if (canvasW <= 0f) return 0f
    // v1.74.7: the crop starts at LOGO_CROP_TOP_FRACTION (no dead black
    // rows) and, on the vector-tagline path, ends below the wordmark so
    // the baked-in tagline isn't drawn twice.
    val cropBottom =
        if (drawTagline) LOGO_WORDMARK_NO_TAGLINE_FRACTION else LOGO_WORDMARK_FRACTION
    val wordmarkH =
        canvasW * widthFrac * (cropBottom - LOGO_CROP_TOP_FRACTION) / LOGO_ASPECT
    return if (drawTagline) wordmarkH + 28f + taglineSize * 1.45f else wordmarkH
}

/**
 * v1.61.16: draws the Quote404 wordmark (the banner cropped above its
 * baked-in tagline) centered at [top], with the tagline as crisp
 * letter-spaced vector text right under it — readable at any export
 * size. Returns the y below the tagline ([top] when there's no logo).
 * v1.61.19: [drawTagline] = false skips the vector tagline for exports
 * where the wordmark crop already shows the tagline baked into the logo.
 */
fun Canvas.drawQuote404LogoBlock(
    logo: Bitmap?,
    top: Float,
    widthFrac: Float = 0.68f,
    taglineSize: Float = 40f,
    drawTagline: Boolean = true
): Float {
    val w = width.toFloat()
    if (logo == null || logo.isRecycled || w <= 0f) return top
    val targetW = w * widthFrac
    val scale = targetW / logo.width.toFloat()
    // v1.74.7: crop starts at LOGO_CROP_TOP_FRACTION — the asset's rows
    // above ~230 are empty black — and on the vector-tagline path ends
    // below the wordmark (535/941) so the baked-in tagline isn't doubled.
    // The full Q emblem (tail included) stays inside the crop.
    val topRow = (logo.height * LOGO_CROP_TOP_FRACTION).toInt().coerceAtLeast(0)
    val cropBottom =
        if (drawTagline) LOGO_WORDMARK_NO_TAGLINE_FRACTION else LOGO_WORDMARK_FRACTION
    val botRow =
        (logo.height * cropBottom).toInt().coerceAtMost(logo.height).coerceAtLeast(topRow + 1)
    val wordmarkH = (botRow - topRow) * scale
    val left = (w - targetW) / 2f
    drawBitmap(
        logo,
        android.graphics.Rect(0, topRow, logo.width, botRow),
        RectF(left, top, left + targetW, top + wordmarkH),
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    )
    if (!drawTagline) return top + wordmarkH
    val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF9FB6C9.toInt()
        textSize = taglineSize
        letterSpacing = 0.14f
        textAlign = Paint.Align.CENTER
    }
    val taglineTop = top + wordmarkH + 28f
    drawText(
        QUOTE404_TAGLINE, w / 2f,
        taglineTop + taglineSize,
        taglinePaint
    )
    return taglineTop + taglineSize * 1.45f
}

/** v1.88.32: height of the slim Quote404 site footer under exports. */
fun quote404FooterH(): Float = 68f

/**
 * v1.88.32: slim black footer strip under exported/shared card images —
 * the same black background as the top logo plate, with
 * "www.Quote404.com" small in the bottom-right corner in white (the
 * neon header look), so the export promotes the site without touching
 * the card itself. [top] is the strip's top edge, [width] the canvas
 * width; [height] defaults to [quote404FooterH].
 */
fun Canvas.drawQuote404Footer(
    top: Float,
    width: Float,
    height: Float = quote404FooterH()
) {
    if (width <= 0f || height <= 0f) return
    drawRect(
        0f, top, width, top + height,
        Paint().apply { color = 0xFF000000.toInt() }
    )
    val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 34f
        alpha = 230
    }
    val label = "www.Quote404.com"
    drawText(
        label,
        width - 36f - labelPaint.measureText(label),
        top + height - 20f,
        labelPaint
    )
}
