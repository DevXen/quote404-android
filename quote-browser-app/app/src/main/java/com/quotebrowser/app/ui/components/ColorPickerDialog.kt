package com.quotebrowser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** "RRGGBB" for a Compose color. */
fun colorToHex(color: Color): String =
    "%06X".format(color.toArgb() and 0xFFFFFF)

private fun hsvToColor(h: Float, s: Float, v: Float): Color =
    Color(android.graphics.Color.HSVToColor(floatArrayOf(h, s, v)))

private fun colorToHsv(color: Color): FloatArray {
    val out = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), out)
    return out
}

/**
 * HSV color picker: a saturation/value pad, a hue strip, and a hex field.
 * No need to know color codes — drag and the hex fills itself in.
 */
@Composable
fun ColorPickerDialog(
    initial: Color,
    onDismiss: () -> Unit,
    onPick: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialHsv = remember(initial) { colorToHsv(initial) }
    var hue by remember { mutableStateOf(initialHsv[0]) }
    var sat by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }
    var hexText by remember { mutableStateOf(colorToHex(initial)) }

    fun currentColor(): Color = hsvToColor(hue, sat, value)

    fun syncHex() {
        hexText = colorToHex(currentColor())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text("Pick a color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SaturationValuePad(
                    hue = hue,
                    sat = sat,
                    value = value,
                    onChange = { s, v ->
                        sat = s
                        value = v
                        syncHex()
                    }
                )
                HueStrip(
                    hue = hue,
                    onChange = {
                        hue = it
                        syncHex()
                    }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(currentColor())
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp)
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { input ->
                            val clean = input.filter {
                                it.isDigit() || it.lowercaseChar() in 'a'..'f'
                            }.take(6).uppercase()
                            hexText = clean
                            if (clean.length == 6) {
                                try {
                                    val parsed = Color(("FF$clean").toLong(16))
                                    val hsv = colorToHsv(parsed)
                                    hue = hsv[0]
                                    sat = hsv[1]
                                    value = hsv[2]
                                } catch (e: Exception) {
                                    // keep the pad where it is
                                }
                            }
                        },
                        label = { Text("Hex") },
                        prefix = { Text("#") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(currentColor()) }) {
                Text("Use this color")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SaturationValuePad(
    hue: Float,
    sat: Float,
    value: Float,
    onChange: (Float, Float) -> Unit
) {
    val density = LocalDensity.current
    val hueColor = hsvToColor(hue, 1f, 1f)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(Color.White, hueColor)))
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
    ) {
        val wDp = maxWidth
        val hDp = maxHeight
        fun updateFrom(offset: Offset) {
            val wPx = with(density) { wDp.toPx() }
            val hPx = with(density) { hDp.toPx() }
            if (wPx <= 0 || hPx <= 0) return
            onChange(
                (offset.x / wPx).coerceIn(0f, 1f),
                1f - (offset.y / hPx).coerceIn(0f, 1f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .pointerInput(hue) { detectTapGestures { updateFrom(it) } }
                .pointerInput(hue) { detectDragGestures { change, _ -> updateFrom(change.position) } }
        ) {
            // Cursor dot at the current saturation/value.
            Spacer(
                modifier = Modifier
                    .size(22.dp)
                    .offset(
                        x = wDp * sat - 11.dp,
                        y = hDp * (1f - value) - 11.dp
                    )
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun HueStrip(
    hue: Float,
    onChange: (Float) -> Unit
) {
    val density = LocalDensity.current
    val hueColors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Brush.horizontalGradient(hueColors))
    ) {
        val wDp = maxWidth
        fun updateFrom(offset: Offset) {
            val wPx = with(density) { wDp.toPx() }
            if (wPx <= 0) return
            onChange((offset.x / wPx).coerceIn(0f, 1f) * 360f)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(Unit) { detectTapGestures { updateFrom(it) } }
                .pointerInput(Unit) { detectDragGestures { change, _ -> updateFrom(change.position) } }
        ) {
            Spacer(
                modifier = Modifier
                    .size(22.dp)
                    .offset(
                        x = wDp * (hue / 360f) - 11.dp,
                        y = 3.dp
                    )
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}
