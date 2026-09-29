package com.quotebrowser.app.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * Shareable quote collections: a favorites folder or a category exported as
 * a single importable JSON file ("*.qbcollection.json"), and the parser for
 * importing one back into My Quotes.
 */

data class CollectionQuote(
    val number: Int,
    val text: String,
    val rating: Int?
)

data class QuoteCollection(
    val name: String,
    /** "favorites-folder" or "category". */
    val source: String,
    val quotes: List<CollectionQuote>
)

private const val COLLECTION_TYPE = "quotebrowser-collection"
private const val COLLECTION_VERSION = 1

/** Serializes a collection to its JSON file format. */
fun buildCollectionJson(collection: QuoteCollection): String {
    val quotesArr = org.json.JSONArray()
    for (q in collection.quotes) {
        val o = org.json.JSONObject()
        o.put("number", q.number)
        o.put("text", q.text)
        if (q.rating != null) o.put("rating", q.rating)
        quotesArr.put(o)
    }
    return org.json.JSONObject()
        .put("type", COLLECTION_TYPE)
        .put("version", COLLECTION_VERSION)
        .put("name", collection.name)
        .put("source", collection.source)
        .put("quotes", quotesArr)
        .toString(2)
}

/**
 * Parses a collection file. Returns null when the text isn't a valid
 * collection (wrong type/version, missing fields).
 */
fun parseCollection(text: String): QuoteCollection? {
    return try {
        val root = org.json.JSONObject(text)
        if (root.optString("type") != COLLECTION_TYPE) return null
        if (root.optInt("version", -1) != COLLECTION_VERSION) return null
        val name = root.optString("name").ifEmpty { return null }
        val source = root.optString("source").ifEmpty { "unknown" }
        val arr = root.optJSONArray("quotes") ?: return null
        val quotes = mutableListOf<CollectionQuote>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            val qtext = o.optString("text")
            if (qtext.isEmpty()) continue
            quotes.add(
                CollectionQuote(
                    number = o.optInt("number", 0),
                    text = qtext,
                    rating = if (o.has("rating")) o.optInt("rating") else null
                )
            )
        }
        if (quotes.isEmpty()) return null
        QuoteCollection(name, source, quotes)
    } catch (e: Exception) {
        null
    }
}

/**
 * Stages a collection in the share cache and opens the system share sheet.
 */
fun shareCollection(context: Context, collection: QuoteCollection) {
    try {
        val dir = File(context.cacheDir, "collections").apply { mkdirs() }
        val safeName = collection.name
            .replace(Regex("[^A-Za-z0-9 _-]"), "")
            .trim().ifEmpty { "collection" }
            .replace(' ', '_')
        val file = File(dir, "$safeName.qbcollection.json")
        file.writeText(buildCollectionJson(collection))
        val uri = FileProvider.getUriForFile(
            context, "com.quotebrowser.app.fileprovider", file
        )
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "QuoteBrowser collection: ${collection.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Share collection"))
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't share collection", Toast.LENGTH_SHORT).show()
    }
}
