package com.quotebrowser.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Deterministic identicon avatar for a nick: a 5x5 mirrored grid seeded by
 * the nick's lowercase hash, drawn with a hue picked from the same hash.
 * No new dependencies; tones adapt to light/dark color schemes.
 */
@Composable
fun NickAvatar(
    nick: String,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val hash = nick.lowercase().hashCode()
    // 0..360 from the hash (mask the sign bit).
    val hue = ((hash and 0x7FFFFFFF) % 360).toFloat()
    val scheme = MaterialTheme.colorScheme
    val isLight = scheme.background.luminance() > 0.5f
    val cellColor = Color.hsv(
        hue = hue,
        saturation = if (isLight) 0.62f else 0.5f,
        value = if (isLight) 0.52f else 0.88f
    )
    val bgColor = scheme.surfaceVariant
    // 5 rows x 3 source columns; columns 3-4 mirror 1-0.
    val cells = remember(nick) {
        val rng = Random(hash.toLong())
        List(5 * 3) { rng.nextBoolean() }
    }
    Canvas(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    ) {
        drawRect(bgColor)
        val n = 5
        val cell = minOf(this.size.width, this.size.height) / n
        val pad = cell * 0.12f
        for (row in 0 until n) {
            for (col in 0 until n) {
                val src = if (col <= 2) col else 4 - col
                if (cells[row * 3 + src]) {
                    drawRoundRect(
                        color = cellColor,
                        topLeft = Offset(col * cell + pad, row * cell + pad),
                        size = Size(cell - pad * 2f, cell - pad * 2f),
                        cornerRadius = CornerRadius(cell * 0.18f)
                    )
                }
            }
        }
    }
}
