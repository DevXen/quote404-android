package com.quotebrowser.app.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

/**
 * v1.90 sticker renderer: draws one quote onto a 512x512 transparent
 * canvas as a white rounded card (the WhatsApp/Telegram sticker size),
 * with the quote text auto-shrunk to fit and the Quote404 footer the
 * branding rules require on every exported image. Pure Canvas drawing —
 * no Compose capture — so it runs on any thread and never needs an
 * Activity.
 */
object StickerRenderer {

    const val SIZE = 512
    const val TRAY_SIZE = 96

    fun renderSticker(displayText: String, footer: String = "www.Quote404.com"): Bitmap {
        val bmp = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val pad = 26f
        val card = RectF(pad, pad, SIZE - pad, SIZE - pad)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            setShadowLayer(12f, 0f, 5f, 0x55000000)
        }
        canvas.drawRoundRect(card, 38f, 38f, cardPaint)

        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF8A8A8A.toInt()
            textSize = 21f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        canvas.drawText(footer, SIZE / 2f, SIZE - pad - 20f, footerPaint)

        val textWidth = (SIZE - 2 * (pad + 32)).toInt()
        val maxTextHeight = (SIZE - 2 * pad - 96).toInt()
        var size = 47f
        var layout: StaticLayout
        while (true) {
            val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF1B1B1B.toInt()
                textSize = size
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            }
            layout = StaticLayout.Builder
                .obtain(displayText, 0, displayText.length, tp, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.12f)
                .setIncludePad(false)
                .build()
            if (layout.height <= maxTextHeight || size <= 16f) break
            size -= 2f
        }
        canvas.save()
        canvas.translate(
            (SIZE - textWidth) / 2f,
            pad + (maxTextHeight - layout.height) / 2f + 8f
        )
        layout.draw(canvas)
        canvas.restore()
        return bmp
    }

    /** 96x96 pack tray icon: brand-teal rounded square, pack initial. */
    fun renderTray(packName: String): Bitmap {
        val bmp = Bitmap.createBitmap(TRAY_SIZE, TRAY_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF0F766E.toInt()
        }
        canvas.drawRoundRect(
            RectF(2f, 2f, TRAY_SIZE - 2f, TRAY_SIZE - 2f), 18f, 18f, bg
        )
        val initial = packName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "Q"
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 52f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val baseline = TRAY_SIZE / 2f - (tp.descent() + tp.ascent()) / 2f
        canvas.drawText(initial, TRAY_SIZE / 2f, baseline, tp)
        return bmp
    }
}
