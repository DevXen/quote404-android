package com.quotebrowser.app.ui

import com.quotebrowser.app.data.Quote
import java.io.ByteArrayOutputStream
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Quotes per chapter file inside the EPUB. */
private const val EPUB_CHAPTER_SIZE = 100

private fun xmlEscape(s: String): String = buildString(s.length) {
    for (c in s) {
        when (c) {
            '&' -> append("&amp;")
            '<' -> append("&lt;")
            '>' -> append("&gt;")
            '"' -> append("&quot;")
            else -> append(c)
        }
    }
}

private fun quoteToXhtml(q: Quote): String = buildString {
    append("<section class=\"quote\">\n")
    append("<h2>#${q.number}</h2>\n")
    if (q.rating != 0) append("<p class=\"rating\">Rating: ${q.rating}</p>\n")
    append("<p class=\"text\">")
    val escaped = xmlEscape(q.text)
    append(escaped.replace("\n", "<br/>\n"))
    append("</p>\n</section>\n")
}

/**
 * Renders [quotes] as an EPUB 3 e-book: one spine of chapters with
 * [EPUB_CHAPTER_SIZE] quotes each. Returns the raw .epub bytes.
 */
fun renderQuotesToEpub(quotes: List<Quote>, title: String): ByteArray {
    val safeTitle = xmlEscape(title.ifBlank { "Quotes" })
    val chapterCount = (quotes.size + EPUB_CHAPTER_SIZE - 1) / EPUB_CHAPTER_SIZE
    val chapters = (0 until chapterCount).map { i ->
        val slice = quotes.subList(
            i * EPUB_CHAPTER_SIZE,
            minOf((i + 1) * EPUB_CHAPTER_SIZE, quotes.size)
        )
        "chap${i + 1}.xhtml" to buildString {
            append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            append("<!DOCTYPE html>\n")
            append("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n")
            append("<title>$safeTitle — Part ${i + 1}</title>\n")
            append("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>\n")
            append("</head>\n<body>\n<h1>$safeTitle — Part ${i + 1}</h1>\n")
            for (q in slice) append(quoteToXhtml(q))
            append("</body>\n</html>\n")
        }
    }
    val css = """
        body { font-family: serif; line-height: 1.5; margin: 1em; }
        h1 { font-size: 1.4em; }
        h2 { font-size: 1.1em; margin-bottom: 0.2em; }
        .quote { margin: 0 0 1.6em 0; padding-bottom: 1em; border-bottom: 1px solid #ccc; }
        .rating { color: #666; font-size: 0.85em; margin: 0 0 0.4em 0; }
        .text { white-space: pre-wrap; margin: 0; }
    """.trimIndent()

    val toc = buildString {
        append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        append("<!DOCTYPE html>\n")
        append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">\n")
        append("<head><title>Contents</title></head>\n<body>\n<nav epub:type=\"toc\">\n<ol>\n")
        for ((i, name) in chapters.withIndex()) {
            append("<li><a href=\"$name\">Part ${i + 1}</a></li>\n")
        }
        append("</ol>\n</nav>\n</body>\n</html>\n")
    }
    // NCX for EPUB 2 readers; registered in the OPF manifest and spine below.
    val uid = "quotebrowser-${System.currentTimeMillis()}"
    val ncx = buildString {
        append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        append("<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n")
        append("<head>\n<meta name=\"dtb:uid\" content=\"$uid\"/>\n</head>\n")
        append("<docTitle><text>$safeTitle</text></docTitle>\n")
        append("<navMap>\n")
        for ((i, name) in chapters.withIndex()) {
            append("<navPoint id=\"navPoint-${i + 1}\" playOrder=\"${i + 1}\">\n")
            append("<navLabel><text>Part ${i + 1}</text></navLabel>\n")
            append("<content src=\"$name\"/>\n")
            append("</navPoint>\n")
        }
        append("</navMap>\n</ncx>\n")
    }
    val opfUid = uid
    val opf = buildString {
        append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        append("<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"uid\" version=\"3.0\">\n")
        append("<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n")
        append("<dc:title>$safeTitle</dc:title>\n")
        append("<dc:language>en</dc:language>\n")
        append("<dc:identifier id=\"uid\">$opfUid</dc:identifier>\n")
        append("<meta property=\"dcterms:modified\">${java.time.Instant.now()}</meta>\n")
        append("</metadata>\n<manifest>\n")
        append("<item id=\"nav\" href=\"toc.xhtml\" media-type=\"application/xhtml+xml\" properties=\"nav\"/>\n")
        append("<item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n")
        append("<item id=\"css\" href=\"style.css\" media-type=\"text/css\"/>\n")
        for ((name, _) in chapters) {
            val id = name.removeSuffix(".xhtml")
            append("<item id=\"$id\" href=\"$name\" media-type=\"application/xhtml+xml\"/>\n")
        }
        append("</manifest>\n<spine toc=\"ncx\">\n")
        for ((name, _) in chapters) {
            append("<itemref idref=\"${name.removeSuffix(".xhtml")}\"/>\n")
        }
        append("</spine>\n</package>\n")
    }
    val container = """<?xml version="1.0" encoding="utf-8"?>
<container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
  <rootfiles>
    <rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/>
  </rootfiles>
</container>
"""

    val out = ByteArrayOutputStream()
    ZipOutputStream(out).use { zos ->
        // The mimetype entry must be first and stored (uncompressed).
        val mimeBytes = "application/epub+zip".toByteArray(Charsets.US_ASCII)
        val mimeEntry = ZipEntry("mimetype").apply {
            method = ZipEntry.STORED
            size = mimeBytes.size.toLong()
            crc = CRC32().apply { update(mimeBytes) }.value
        }
        zos.putNextEntry(mimeEntry)
        zos.write(mimeBytes)
        zos.closeEntry()

        fun add(name: String, bytes: ByteArray) {
            zos.putNextEntry(ZipEntry(name))
            zos.write(bytes)
            zos.closeEntry()
        }
        add("META-INF/container.xml", container.toByteArray(Charsets.UTF_8))
        add("OEBPS/content.opf", opf.toByteArray(Charsets.UTF_8))
        add("OEBPS/toc.xhtml", toc.toByteArray(Charsets.UTF_8))
        add("OEBPS/toc.ncx", ncx.toByteArray(Charsets.UTF_8))
        add("OEBPS/style.css", css.toByteArray(Charsets.UTF_8))
        for ((name, html) in chapters) {
            add("OEBPS/$name", html.toByteArray(Charsets.UTF_8))
        }
    }
    return out.toByteArray()
}
