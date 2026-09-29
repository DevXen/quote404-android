package com.quotebrowser.app.ui

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * v1.53 "Explain this quote": long-press a quote for a plain-English
 * breakdown of its obscure references and in-jokes. Reuses the Gemini
 * integration and the user's stored API key (same prefs as the Gemini
 * read-aloud engine). Client-side only: one generateContent call, no
 * streaming.
 */
private const val GEMINI_EXPLAIN_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"
private const val GEMINI_API_KEY_PREF = "gemini_api_key"

sealed interface ExplainResult {
    data class Ok(val text: String) : ExplainResult
    /** No API key set (or blank): tells the user what it needs. */
    data class NeedKey(val message: String) : ExplainResult
    data class Error(val message: String) : ExplainResult
}

/** Reads the stored Gemini API key (shared with the read-aloud engine). */
fun geminiApiKeyOrNull(context: Context): String? {
    val prefs = context.getSharedPreferences(TTS_PREFS_V145, Context.MODE_PRIVATE)
    val key = prefs.getString(GEMINI_API_KEY_PREF, "")?.trim().orEmpty()
    return key.ifBlank { null }
}

/**
 * Asks Gemini to explain a quote. Returns [ExplainResult.NeedKey] when no
 * API key is stored, [ExplainResult.Error] when offline or the request
 * fails.
 */
suspend fun explainQuote(context: Context, quoteText: String): ExplainResult =
    withContext(Dispatchers.IO) {
        val apiKey = geminiApiKeyOrNull(context)
        if (apiKey == null) {
            return@withContext ExplainResult.NeedKey(
                "Explaining quotes needs a Gemini API key. Add yours in " +
                    "Settings → Read aloud → Gemini engine, then try again."
            )
        }
        val prompt = "Explain this internet chat quote in plain English. " +
            "Identify the speakers, decode any obscure references, slang, " +
            "in-jokes, or technical terms, and say what makes it funny. " +
            "Keep it short — a few sentences, no preamble.\n\nQuote:\n$quoteText"
        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
            )
            .put(
                "generationConfig",
                JSONObject().put("maxOutputTokens", 300)
            )
        try {
            val conn = (URL(GEMINI_EXPLAIN_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("x-goog-api-key", apiKey)
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 15_000
                readTimeout = 30_000
            }
            try {
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                if (code != HttpURLConnection.HTTP_OK) {
                    val err = try {
                        (conn.errorStream ?: conn.inputStream)?.use {
                            it.readBytes().toString(Charsets.UTF_8)
                        }
                    } catch (e: Exception) {
                        null
                    }
                    return@withContext ExplainResult.Error(
                        "Gemini said no (HTTP $code)." +
                            (err?.take(160)?.let { " $it" } ?: "") +
                            " Check your API key in Settings → Read aloud."
                    )
                }
                val responseText = conn.inputStream.use {
                    it.readBytes().toString(Charsets.UTF_8)
                }
                val text = extractExplainText(responseText)
                if (text.isBlank()) {
                    ExplainResult.Error("Gemini returned an empty explanation. Try again.")
                } else {
                    ExplainResult.Ok(text.trim())
                }
            } catch (e: java.net.SocketException) {
                ExplainResult.Error(
                    "Couldn't reach Gemini — check your connection and try again."
                )
            } catch (e: java.io.IOException) {
                ExplainResult.Error(
                    "Couldn't reach Gemini — you're probably offline. Try again later."
                )
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            ExplainResult.Error("Explanation failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }

/** Pulls candidates[0].content.parts[*].text out of a generateContent reply. */
private fun extractExplainText(responseText: String): String {
    return try {
        val root = JSONObject(responseText)
        val candidates = root.optJSONArray("candidates") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until candidates.length()) {
            val parts = candidates.optJSONObject(i)
                ?.optJSONObject("content")
                ?.optJSONArray("parts") ?: continue
            for (j in 0 until parts.length()) {
                val t = parts.optJSONObject(j)?.optString("text").orEmpty()
                if (t.isNotBlank()) {
                    if (sb.isNotEmpty()) sb.append("\n")
                    sb.append(t)
                }
            }
            if (sb.isNotEmpty()) break
        }
        sb.toString()
    } catch (e: Exception) {
        ""
    }
}

/**
 * v1.78: shared Gemini generateContent call behind explain/roast/rhyme.
 * Same API key lookup, HTTP pattern, and result type as [explainQuote];
 * `maxTokens` caps the reply and `verb` personalizes the error text
 * (e.g. "roast", "explanation").
 */
private suspend fun geminiGenerate(
    context: Context,
    prompt: String,
    maxTokens: Int,
    verb: String
): ExplainResult = withContext(Dispatchers.IO) {
    val apiKey = geminiApiKeyOrNull(context)
    if (apiKey == null) {
        return@withContext ExplainResult.NeedKey(
            "Gemini AI features need an API key. Add yours in " +
                "Settings → Read aloud → Gemini engine, then try again."
        )
    }
    val body = JSONObject()
        .put(
            "contents",
            JSONArray().put(
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", prompt))
                )
            )
        )
        .put(
            "generationConfig",
            JSONObject().put("maxOutputTokens", maxTokens)
        )
    try {
        val conn = (URL(GEMINI_EXPLAIN_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("x-goog-api-key", apiKey)
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 30_000
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                val err = try {
                    (conn.errorStream ?: conn.inputStream)?.use {
                        it.readBytes().toString(Charsets.UTF_8)
                    }
                } catch (e: Exception) {
                    null
                }
                return@withContext ExplainResult.Error(
                    "Gemini said no (HTTP $code)." +
                        (err?.take(160)?.let { " $it" } ?: "") +
                        " Check your API key in Settings → Read aloud."
                )
            }
            val responseText = conn.inputStream.use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            val text = extractExplainText(responseText)
            if (text.isBlank()) {
                ExplainResult.Error("Gemini returned an empty $verb. Try again.")
            } else {
                ExplainResult.Ok(text.trim())
            }
        } catch (e: java.net.SocketException) {
            ExplainResult.Error(
                "Couldn't reach Gemini — check your connection and try again."
            )
        } catch (e: java.io.IOException) {
            ExplainResult.Error(
                "Couldn't reach Gemini — you're probably offline. Try again later."
            )
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        ExplainResult.Error("$verb failed: ${e.message ?: e.javaClass.simpleName}")
    }
}

/**
 * v1.78: "Roast this quote". A savage-but-playful roast of the quote and
 * whoever said it — dark-humor-friendly, punchy, a few sentences. It
 * roasts the quote and the speaker's choices, never anyone's identity,
 * background, or protected traits. Results are NOT cached: every open
 * asks Gemini for a fresh roast (see ExplainQuoteDialog).
 */
suspend fun roastQuote(context: Context, quoteText: String): ExplainResult =
    geminiGenerate(
        context = context,
        prompt = "Roast this internet chat quote. Be savage but playful: " +
            "mock the quote, the logic, and whoever said it, with " +
            "dark-humor-friendly punch. Keep it fun — roast the quote and " +
            "the speaker's choices, never anyone's identity, background, " +
            "or protected traits. A few sentences, no preamble, " +
            "no moralizing.\n\nQuote:\n$quoteText",
        maxTokens = 300,
        verb = "roast"
    )

/**
 * v1.78: "Rhyme this quote". Rewrites the quote as rhyming couplets — a
 * short song verse that keeps the original meaning, the speakers'
 * voices, and what makes it funny. NOT cached: fresh verse every open
 * (see ExplainQuoteDialog).
 */
suspend fun rhymeQuote(context: Context, quoteText: String): ExplainResult =
    geminiGenerate(
        context = context,
        prompt = "Rewrite this internet chat quote as rhyming couplets — " +
            "a short song verse that keeps the original meaning, the " +
            "speakers' voices, and what makes it funny. A few lines, " +
            "no preamble, no explanation, just the verse.\n\nQuote:\n$quoteText",
        maxTokens = 300,
        verb = "rhyme"
    )
