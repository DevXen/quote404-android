package com.quotebrowser.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stats for the shareable "My reading stats" card.
 *
 * @param quotesRead total quotes read (all databases).
 * @param currentStreak current consecutive reading days.
 * @param longestStreak longest streak ever.
 * @param favoritesCount total favorites across all databases.
 * @param topNick most-quoted nick, null when unknown.
 * @param topNickCount quotes by [topNick].
 * @param weeklyRead quotes read in the last 7 days.
 * @param weeklyTarget weekly reading goal; 0 = no goal set.
 */
data class StatsCardData(
    val quotesRead: Long,
    val currentStreak: Int,
    val longestStreak: Int,
    val favoritesCount: Int,
    val topNick: String?,
    val topNickCount: Int,
    val weeklyRead: Int,
    val weeklyTarget: Int
)

private const val STATS_CARD_W = 1080
private const val STATS_CARD_H = 1350
private const val STATS_MARGIN = 90

// Fixed dark palette so the PNG looks good regardless of the app theme.
private const val SC_BG_TOP = 0xFF23233A
private const val SC_BG_BOTTOM = 0xFF0E0E1A
private const val SC_ACCENT = 0xFF90CAF9
private const val SC_TEXT = 0xFFFFFFFF

/**
 * Renders a shareable reading-stats card (1080x1350 PNG): headline stat
 * grid (quotes read, streaks, favorites), top nick, and weekly-goal
 * progress. Pure android.graphics drawing, same pattern as
 * [renderQuoteCard]/[renderQuoteStrip] in QuoteCards.kt.
 */
fun renderStatsCard(context: Context, stats: StatsCardData): Bitmap {
    val bitmap = Bitmap.createBitmap(STATS_CARD_W, STATS_CARD_H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Gradient background.
    val bg = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, STATS_CARD_H.toFloat(),
            SC_BG_TOP.toInt(), SC_BG_BOTTOM.toInt(),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, STATS_CARD_W.toFloat(), STATS_CARD_H.toFloat(), bg)

    val numberFmt = NumberFormat.getInstance(Locale.getDefault())
    val margin = STATS_MARGIN.toFloat()
    val mid = STATS_CARD_W / 2f

    // v1.61.15: the Quote404 logo right on the background — large,
    // top-center — instead of a floating strip.
    val logoBottom = canvas.drawQuote404LogoOnArt(quote404WatermarkLogo(context))

    var top = logoBottom + 70f
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SC_ACCENT.toInt()
        textSize = 64f
        isFakeBoldText = true
    }
    canvas.drawText("MY READING STATS", margin, top, titlePaint)
    top += 28f
    canvas.drawRect(
        margin, top,
        margin + 120f, top + 12f,
        Paint().apply { color = SC_ACCENT.toInt() }
    )
    top += 80f
    val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SC_TEXT.toInt()
        textSize = 36f
        alpha = 180
    }
    val dateLine = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()))
    canvas.drawText(dateLine, margin, top, datePaint)
    top += 123f

    // Headline stat grid, two per row.
    fun drawStatBlock(x: Float, y: Float, number: String, label: String) {
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SC_TEXT.toInt()
            textSize = 104f
            isFakeBoldText = true
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = SC_TEXT.toInt()
            textSize = 38f
            alpha = 210
        }
        canvas.drawText(number, x, y, numPaint)
        canvas.drawText(label.uppercase(Locale.getDefault()), x, y + 62f, labelPaint)
    }
    drawStatBlock(margin, top, numberFmt.format(stats.quotesRead), "quotes read")
    drawStatBlock(mid, top, stats.currentStreak.toString(), "day streak")
    top += 190f
    drawStatBlock(margin, top, stats.longestStreak.toString(), "longest streak")
    drawStatBlock(mid, top, numberFmt.format(stats.favoritesCount), "favorites")
    top += 170f

    val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SC_ACCENT.toInt()
        textSize = 44f
        isFakeBoldText = true
    }
    val bigPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SC_TEXT.toInt()
        textSize = 56f
        isFakeBoldText = true
    }
    val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SC_TEXT.toInt()
        textSize = 40f
        alpha = 210
    }

    // Top nick.
    if (!stats.topNick.isNullOrBlank()) {
        val maxNickW = STATS_CARD_W - STATS_MARGIN * 2f
        val topNick = stats.topNick ?: ""
        var nick = topNick
        while (bigPaint.measureText(nick) > maxNickW && nick.length > 8) {
            nick = nick.dropLast(1)
        }
        if (nick.length < topNick.length) nick += "\u2026"
        canvas.drawText("TOP NICK", margin, top, sectionPaint)
        top += 80f
        canvas.drawText(nick, margin, top, bigPaint)
        top += 55f
        canvas.drawText(
            "${numberFmt.format(stats.topNickCount)} quotes",
            margin, top, bodyPaint
        )
        top += 110f
    }

    // Weekly goal progress (or plain weekly count when no goal is set).
    canvas.drawText(
        if (stats.weeklyTarget > 0) "WEEKLY GOAL" else "READ THIS WEEK",
        margin, top, sectionPaint
    )
    top += 80f
    if (stats.weeklyTarget > 0) {
        canvas.drawText(
            "${numberFmt.format(stats.weeklyRead)} / ${numberFmt.format(stats.weeklyTarget)}",
            margin, top, bigPaint
        )
        top += 48f
        val frac = (stats.weeklyRead.toFloat() / stats.weeklyTarget).coerceIn(0f, 1f)
        val barW = (STATS_CARD_W - STATS_MARGIN * 2).toFloat()
        canvas.drawRoundRect(
            margin, top,
            margin + barW, top + 24f,
            12f, 12f,
            Paint().apply { color = SC_TEXT.toInt(); alpha = 40 }
        )
        if (frac > 0f) {
            canvas.drawRoundRect(
                margin, top,
                margin + barW * frac, top + 24f,
                12f, 12f,
                Paint().apply { color = SC_ACCENT.toInt() }
            )
        }
        top += 68f
        canvas.drawText(
            if (stats.weeklyRead >= stats.weeklyTarget) "Goal reached \u2014 nice reading!"
            else "${numberFmt.format(stats.weeklyTarget - stats.weeklyRead)} to go",
            margin, top, bodyPaint
        )
    } else {
        canvas.drawText(
            "${numberFmt.format(stats.weeklyRead)} quotes",
            margin, top, bigPaint
        )
    }

    // v1.61.11: the Quote404 branding moved into the logo strip at the
    // top — the footer wordmark and centered watermark are gone.
    return bitmap
}

/**
 * Renders the [StatsCardData] card, writes the PNG to the app cache, and
 * fires the system share sheet. Call from a coroutine scope.
 */
suspend fun renderAndShareStatsCard(context: Context, data: StatsCardData) {
    try {
        val uri = withContext(Dispatchers.IO) {
            val rendered = renderStatsCard(context, data)
            val dir = File(context.cacheDir, "quote_cards").apply { mkdirs() }
            val file = File(dir, "my-reading-stats.png")
            file.outputStream().use { out ->
                rendered.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            rendered.recycle()
            FileProvider.getUriForFile(
                context, "com.quotebrowser.app.fileprovider", file
            )
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share reading stats"))
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't render card", Toast.LENGTH_SHORT).show()
    }
}
