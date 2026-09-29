package com.quotebrowser.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.quotebrowser.app.data.DatabaseManager
import java.io.File
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * v1.78 "My Quote DNA": a fun taste profile computed on-device from the
 * user's favorites and votes. No network, no Room entities — it just reads
 * the existing meta database (favorites) and the persisted local ratings.
 */

data class DnaDbStat(val dbId: String, val label: String, val count: Int)

data class QuoteDnaData(
    val totalFavorites: Int,
    val upVotes: Int,
    val downVotes: Int,
    val topDbs: List<DnaDbStat>,
    val funTitle: String,
    val funSubtitle: String
) {
    val isEmpty: Boolean get() = totalFavorites == 0 && upVotes == 0 && downVotes == 0
}

/**
 * Computes the user's Quote DNA from favorites (per-database counts) and
 * local +/- votes. The fun title is playful and small: a handful of cases
 * keyed off the most-favorited database, plus a spread check for mixed
 * taste.
 */
suspend fun computeQuoteDna(manager: DatabaseManager): QuoteDnaData =
    withContext(Dispatchers.IO) {
        val favorites = try {
            manager.metaDb.metaDao().allFavoriteEntries().first()
        } catch (_: Exception) {
            emptyList()
        }
        val labels = try {
            manager.availableDatabases().first().associate { it.id to it.label }
        } catch (_: Exception) {
            emptyMap()
        }
        val counts = favorites.groupingBy { it.dbId }.eachCount()
        val topDbs = counts.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { (dbId, count) ->
                DnaDbStat(dbId, labels[dbId] ?: dbId, count)
            }

        val ratings = manager.localRatings.value
        val upVotes = ratings.count { it.value > 0 }
        val downVotes = ratings.count { it.value < 0 }

        val totalFavorites = favorites.size
        val topDbId = topDbs.firstOrNull()?.dbId.orEmpty()
        val topShare = if (totalFavorites > 0)
            topDbs.firstOrNull()?.count?.toFloat()?.div(totalFavorites) ?: 0f
        else 0f

        val (title, subtitle) = when {
            totalFavorites == 0 && upVotes == 0 && downVotes == 0 ->
                "Undiscovered" to "Pin some quotes and your DNA will appear here."
            topDbId.contains("bash") ->
                "Certified Bash Goblin" to "Bash.org runs through your veins."
            topDbId.contains("qdb") ->
                "QDB Scholar" to "A connoisseur of the classic quote databases."
            topDbId.contains("irc") ->
                "IRC Native" to "You were raised in the chatrooms."
            topDbId.contains("crapshack") ->
                "CrapShack Regular" to "Proud resident of the internet's basement."
            topDbId.contains("xkcd") ->
                "Nerd Sniped" to "Technically correct — the best kind of funny."
            topDbId == DatabaseManager.MY_QUOTES_ID ->
                "Quote Smith" to "You don't just read quotes, you forge them."
            topShare in 0f..0.49f && topDbs.size >= 2 ->
                "Quote Omnivore" to "No single database can contain your taste."
            else ->
                "Quote Connoisseur" to "Refined taste, dangerously quotable."
        }

        QuoteDnaData(
            totalFavorites = totalFavorites,
            upVotes = upVotes,
            downVotes = downVotes,
            topDbs = topDbs,
            funTitle = title,
            funSubtitle = subtitle
        )
    }

private const val DNA_CARD_W = 1080
private const val DNA_CARD_H = 1420
private const val DNA_MARGIN = 90

private const val DNA_BG_TOP = 0xFF23233A
private const val DNA_BG_BOTTOM = 0xFF0E0E1A
private const val DNA_ACCENT = 0xFF90CAF9
private const val DNA_TEXT = 0xFFFFFFFF

/**
 * Renders the shareable "My Quote DNA" card (1080x1420 PNG): logo art up
 * top, the fun title, top-3 database bars, and the headline stats. Same
 * android.graphics pattern as [renderStatsCard]; the Quote404 logo art is
 * drawn large at the top so every export visibly carries Clint's logo.
 */
fun renderQuoteDnaCard(context: Context, data: QuoteDnaData): Bitmap {
    val bitmap = Bitmap.createBitmap(DNA_CARD_W, DNA_CARD_H, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bg = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, DNA_CARD_H.toFloat(),
            DNA_BG_TOP.toInt(), DNA_BG_BOTTOM.toInt(),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, DNA_CARD_W.toFloat(), DNA_CARD_H.toFloat(), bg)

    val numberFmt = NumberFormat.getInstance(Locale.getDefault())
    val margin = DNA_MARGIN.toFloat()

    // v1.61.15: the Quote404 logo right on the background — large,
    // top-center — instead of a floating strip.
    val logoBottom = canvas.drawQuote404LogoOnArt(quote404WatermarkLogo(context))

    var top = logoBottom + 70f
    val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_ACCENT.toInt()
        textSize = 52f
        isFakeBoldText = true
    }
    canvas.drawText("MY QUOTE DNA", margin, top, sectionPaint)
    top += 92f
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_TEXT.toInt()
        textSize = 76f
        isFakeBoldText = true
    }
    var title = data.funTitle
    val maxTitleW = DNA_CARD_W - DNA_MARGIN * 2f
    while (titlePaint.measureText(title) > maxTitleW && title.length > 8) {
        title = title.dropLast(1)
    }
    if (title.length < data.funTitle.length) title += "\u2026"
    canvas.drawText(title, margin, top, titlePaint)
    top += 62f
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_TEXT.toInt()
        textSize = 40f
        alpha = 200
    }
    canvas.drawText(data.funSubtitle, margin, top, subtitlePaint)
    top += 110f

    // Top databases as bars.
    val dbLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_TEXT.toInt()
        textSize = 44f
    }
    val dbCountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_ACCENT.toInt()
        textSize = 44f
        isFakeBoldText = true
    }
    val barTrackPaint = Paint().apply { color = DNA_TEXT.toInt(); alpha = 40 }
    val barFillPaint = Paint().apply { color = DNA_ACCENT.toInt() }
    val maxCount = data.topDbs.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    val barW = (DNA_CARD_W - DNA_MARGIN * 2).toFloat()
    for (stat in data.topDbs) {
        var label = stat.label
        val countStr = numberFmt.format(stat.count)
        while (dbLabelPaint.measureText("$label  $countStr") > maxTitleW && label.length > 8) {
            label = label.dropLast(1)
        }
        if (label.length < stat.label.length) label += "\u2026"
        canvas.drawText(label, margin, top, dbLabelPaint)
        val countW = dbCountPaint.measureText(countStr)
        canvas.drawText(countStr, margin + maxTitleW - countW, top, dbCountPaint)
        top += 28f
        canvas.drawRoundRect(
            margin, top, margin + barW, top + 22f,
            11f, 11f, barTrackPaint
        )
        val frac = stat.count.toFloat() / maxCount
        if (frac > 0f) {
            canvas.drawRoundRect(
                margin, top, margin + barW * frac, top + 22f,
                11f, 11f, barFillPaint
            )
        }
        top += 78f
    }
    top += 30f

    // Headline stats: favorites / upvotes / downvotes.
    val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_TEXT.toInt()
        textSize = 96f
        isFakeBoldText = true
    }
    val statLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_TEXT.toInt()
        textSize = 36f
        alpha = 200
    }
    val colW = (DNA_CARD_W - DNA_MARGIN * 2f) / 3f
    fun drawStat(col: Int, number: String, label: String) {
        val x = margin + col * colW
        canvas.drawText(number, x, top, numPaint)
        canvas.drawText(label.uppercase(Locale.getDefault()), x, top + 58f, statLabelPaint)
    }
    drawStat(0, numberFmt.format(data.totalFavorites), "favorites")
    drawStat(1, numberFmt.format(data.upVotes), "upvotes")
    drawStat(2, numberFmt.format(data.downVotes), "downvotes")
    top += 190f

    // Footer.
    val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DNA_TEXT.toInt()
        textSize = 36f
        alpha = 160
    }
    val dateLine = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()))
    canvas.drawText("Quote404 \u00b7 $dateLine", margin, DNA_CARD_H - 90f, footerPaint)

    return bitmap
}

/**
 * Renders the DNA card, writes the PNG to the app cache, and fires the
 * system share sheet. Same pattern as [renderAndShareStatsCard]; call from
 * a coroutine scope.
 */
suspend fun renderAndShareQuoteDnaCard(context: Context, data: QuoteDnaData) {
    try {
        val uri = withContext(Dispatchers.IO) {
            val rendered = renderQuoteDnaCard(context, data)
            val dir = File(context.cacheDir, "quote_cards").apply { mkdirs() }
            val file = File(dir, "my-quote-dna.png")
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
        context.startActivity(Intent.createChooser(send, "Share my Quote DNA"))
    } catch (_: Exception) {
        Toast.makeText(context, "Couldn't render card", Toast.LENGTH_SHORT).show()
    }
}

/**
 * "My Quote DNA" dialog: the fun title, a preview of the shareable card,
 * and a Share button. Empty state (no favorites/votes yet) shows a
 * friendly nudge instead of the preview.
 */
@Composable
fun QuoteDnaDialog(
    manager: DatabaseManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = remember { kotlinx.coroutines.CoroutineScope(Dispatchers.Main) }
    var data by remember { mutableStateOf<QuoteDnaData?>(null) }
    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var sharing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val dna = computeQuoteDna(manager)
        data = dna
        if (!dna.isEmpty) {
            cardBitmap = withContext(Dispatchers.Default) {
                renderQuoteDnaCard(context, dna)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("My Quote DNA") },
        text = {
            val dna = data
            when {
                dna == null -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Reading your taste buds…")
                }
                dna.isEmpty -> Text(
                    "No DNA yet — pin some quotes as favorites (or vote on a few) " +
                        "and your taste profile will grow here."
                )
                else -> Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        dna.funTitle,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        dna.funSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    val bmp = cardBitmap
                    if (bmp != null && !bmp.isRecycled) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "My Quote DNA card preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val dna = data
                if (dna != null && !dna.isEmpty) {
                    TextButton(
                        onClick = {
                            if (!sharing) {
                                sharing = true
                                scope.launch {
                                    renderAndShareQuoteDnaCard(context, dna)
                                    sharing = false
                                }
                            }
                        },
                        enabled = !sharing
                    ) { Text(if (sharing) "Sharing…" else "Share") }
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}
