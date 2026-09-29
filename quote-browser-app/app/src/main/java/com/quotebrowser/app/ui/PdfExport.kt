package com.quotebrowser.app.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.quotebrowser.app.data.Quote
import java.io.ByteArrayOutputStream

/**
 * Renders quotes to a PDF document (byte array) using the framework
 * PdfDocument. A4 pages, one title page header, quotes flowed with
 * word-wrapped body text; a new page starts whenever the next quote
 * would not fit.
 */
fun renderQuotesToPdf(quotes: List<Quote>, title: String): ByteArray {
    val pageW = 595
    val pageH = 842
    val margin = 48
    val contentW = pageW - margin * 2

    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = 0xFF111111.toInt()
    }
    val headerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = 0xFF333333.toInt()
    }
    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f
        typeface = Typeface.DEFAULT
        color = 0xFF111111.toInt()
    }
    val rulePaint = Paint().apply {
        color = 0xFFCCCCCC.toInt()
        strokeWidth = 1f
    }

    fun layoutOf(text: String, paint: TextPaint): StaticLayout =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, contentW)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setIncludePad(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, contentW, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
        }

    val doc = PdfDocument()
    var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, 1).create())
    var canvas = page.canvas
    var y = margin.toFloat()

    fun newPage() {
        doc.finishPage(page)
        page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, 1).create())
        canvas = page.canvas
        y = margin.toFloat()
    }

    // Title on the first page.
    val titleLayout = layoutOf(title, titlePaint)
    canvas.save()
    canvas.translate(margin.toFloat(), y)
    titleLayout.draw(canvas)
    canvas.restore()
    y += titleLayout.height + 24f

    for (q in quotes) {
        val meta = buildString {
            append("#${q.number}")
            q.rating?.let { append(" · rating $it") }
        }
        val headerLayout = layoutOf(meta, headerPaint)
        // v1.88.21: "mind breaks" — same paragraph separation as the cards.
        val bodyLayout = layoutOf(paragraphizeBody(q.text), bodyPaint)
        val blockH = headerLayout.height + 6f + bodyLayout.height + 22f

        if (y + blockH > pageH - margin) newPage()

        canvas.save()
        canvas.translate(margin.toFloat(), y)
        headerLayout.draw(canvas)
        canvas.restore()
        y += headerLayout.height + 6f

        canvas.save()
        canvas.translate(margin.toFloat(), y)
        bodyLayout.draw(canvas)
        canvas.restore()
        y += bodyLayout.height + 10f

        canvas.drawLine(margin.toFloat(), y, (pageW - margin).toFloat(), y, rulePaint)
        y += 12f
    }

    doc.finishPage(page)
    val out = ByteArrayOutputStream()
    doc.writeTo(out)
    doc.close()
    return out.toByteArray()
}
