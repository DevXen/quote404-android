package com.quotebrowser.app.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.quotebrowser.app.ui.theme.scaledTextStyle
import java.io.File

/**
 * Quote-body font override, applied to quote text in lists and detail.
 * Null = the default font.
 */
val LocalQuoteFontFamily = compositionLocalOf<FontFamily?> { null }

/**
 * Resolves the stored quote-font preference to a [FontFamily]: "default"
 * (null), "serif", "monospace", or a persisted SAF content URI copied into
 * the app cache. Anything unresolvable falls back to the default.
 */
fun resolveQuoteFontFamily(context: Context, value: String): FontFamily? {
    when (value) {
        "serif" -> return FontFamily.Serif
        "monospace" -> return FontFamily.Monospace
        "default" -> return null
    }
    val uri = try {
        Uri.parse(value)
    } catch (_: Exception) {
        return null
    }
    if (uri.scheme != "content") return null
    return try {
        val cacheFile = File(context.cacheDir, "quote_font_custom")
        context.contentResolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        FontFamily(Font(cacheFile))
    } catch (_: Exception) {
        null
    }
}

/**
 * Quote text style: the base style with the user's text scale and the
 * chosen quote-body font applied.
 */
@Composable
fun quoteTextStyle(base: TextStyle): TextStyle {
    val scaled = scaledTextStyle(base)
    val font = LocalQuoteFontFamily.current
    return remember(scaled, font) {
        if (font == null) scaled else scaled.copy(fontFamily = font)
    }
}
