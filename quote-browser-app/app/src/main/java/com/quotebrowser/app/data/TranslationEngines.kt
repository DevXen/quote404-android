package com.quotebrowser.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * v1.61: quote translation engines.
 *
 * Primary: the user's on-device Gemini API key, via the `generateContent`
 * text endpoint. Quotes are translated in batches (10–20 per request) so a
 * whole database costs a handful of requests against the free text quota
 * (~250/day, far roomier than the TTS quota).
 *
 * Fallback: Google's keyless web translation endpoint. No API key, no
 * credits, no quota that matters here — so translations keep working when
 * the Gemini key is missing/quota-exhausted, or on a copy of the app handed
 * to someone without a key. Each quote is translated once and cached in
 * [TranslationDatabase], so the endpoint is hit at most once per quote.
 */
object TranslationEngines {

    /** Thrown when Gemini reports quota exhaustion (HTTP 429). */
    class GeminiQuotaException(message: String) : Exception(message)

    class TranslationException(message: String) : Exception(message)

    private const val GEMINI_TEXT_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"

    /** Max quotes per Gemini request; keeps prompts small and parseable. */
    const val GEMINI_BATCH_SIZE = 15

    /**
     * Translates [texts] (parallel to the returned list) via Gemini in one
     * request. Each input is numbered in the prompt and each output line is
     * numbered the same way, so results map back even if a line is dropped.
     * Throws [GeminiQuotaException] on 429, [TranslationException] otherwise.
     */
    suspend fun geminiBatch(
        texts: List<String>,
        sourceLang: String,
        targetLang: String,
        apiKey: String
    ): List<String> = withContext(Dispatchers.IO) {
        if (texts.isEmpty()) return@withContext emptyList()
        val sourceName = languageName(sourceLang)
        val targetName = languageName(targetLang)
        val numbered = texts.mapIndexed { i, t -> "${i + 1}. $t" }.joinToString("\n")
        val prompt = buildString {
            append("Translate each of the following ${texts.size} texts from ")
            if (sourceLang == "auto") append("their original language (auto-detect each one)")
            else append("$sourceName")
            append(" to $targetName. ")
            append("These are IRC chat quotes: preserve nicknames in <angle brackets>, ")
            append("timestamps, and line breaks exactly. Do not explain or comment. ")
            append("Return ONLY the translations, one per line, each starting with its ")
            append("number exactly like \"1. \", \"2. \", in the same order:\n")
            append(numbered)
        }
        val body = JSONObject()
            .put("contents", JSONArray().put(JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))))
            .put("generationConfig", JSONObject().put("temperature", 0.2).put("maxOutputTokens", 8192))
        val conn = (URL(GEMINI_TEXT_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("x-goog-api-key", apiKey)
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 90_000
        }
        try {
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code == 429) {
                throw GeminiQuotaException(
                    "Gemini translation quota reached. Translations will continue " +
                        "with the free fallback engine, or try again later."
                )
            }
            if (code != HttpURLConnection.HTTP_OK) {
                val err = readBody(conn).take(300)
                throw TranslationException("Gemini translation failed (HTTP $code): $err")
            }
            val responseText = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
            parseNumberedResponse(responseText, texts.size)
        } catch (e: GeminiQuotaException) {
            throw e
        } catch (e: TranslationException) {
            throw e
        } catch (e: java.io.IOException) {
            throw TranslationException("Translation network error: ${e.message}")
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Parses Gemini's numbered lines back into per-quote translations.
     * Missing numbers yield "" (caller skips caching those).
     */
    private fun parseNumberedResponse(responseText: String, expected: Int): List<String> {
        val text = try {
            JSONObject(responseText)
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } catch (e: Exception) {
            throw TranslationException("Gemini returned an unreadable translation response.")
        }
        val byNumber = mutableMapOf<Int, String>()
        // Numbered lines may wrap; accumulate continuation lines onto the
        // current number so multi-line quotes survive.
        var current = -1
        val lineRe = Regex("""^(\d{1,3})\.\s?(.*)$""")
        for (raw in text.lines()) {
            val line = raw.trimEnd()
            if (line.isBlank()) continue
            val m = lineRe.matchEntire(line.trimStart())
            if (m != null) {
                val n = m.groupValues[1].toIntOrNull() ?: continue
                if (n in 1..expected) {
                    current = n
                    byNumber[n] = m.groupValues[2]
                } else {
                    current = -1
                }
            } else if (current > 0) {
                byNumber[current] = (byNumber[current] ?: "") + "\n" + line
            }
        }
        return (1..expected).map { (byNumber[it] ?: "").trim() }
    }

    /**
     * Keyless fallback: one text per request via Google's public web
     * translation endpoint. Returns "" on failure (caller skips caching).
     */
    suspend fun fallbackSingle(
        text: String,
        sourceLang: String,
        targetLang: String
    ): String = withContext(Dispatchers.IO) {
        try {
            val sl = if (sourceLang == "auto") "auto" else sourceLang
            val q = URLEncoder.encode(text, "UTF-8")
            val url =
                "https://translate.googleapis.com/translate_a/single?client=gtx" +
                    "&sl=$sl&tl=${targetLang}&dt=t&q=$q"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 16) QuoteBrowser/1.61"
                )
                connectTimeout = 15_000
                readTimeout = 30_000
            }
            try {
                if (conn.responseCode != HttpURLConnection.HTTP_OK) return@withContext ""
                val body = conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
                // Response: [[["translated","source",...],["more",...]],...]
                val outer = JSONArray(body)
                val sentences = outer.getJSONArray(0)
                buildString {
                    for (i in 0 until sentences.length()) {
                        append(sentences.getJSONArray(i).optString(0))
                    }
                }
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun readBody(conn: HttpURLConnection): String = try {
        (conn.errorStream ?: conn.inputStream)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: ""
    } catch (e: Exception) {
        ""
    }

    /** Human name for a language code, for prompts. */
    fun languageName(code: String): String = when (code.lowercase()) {
        "en" -> "English"
        "sv" -> "Swedish"
        "es" -> "Spanish"
        "fr" -> "French"
        "de" -> "German"
        "it" -> "Italian"
        "pt" -> "Portuguese"
        "nl" -> "Dutch"
        "pl" -> "Polish"
        "ru" -> "Russian"
        "ja" -> "Japanese"
        "zh" -> "Chinese"
        "ko" -> "Korean"
        "auto" -> "auto-detected language"
        else -> code
    }

    /** Languages offered for the translation target setting. */
    val TARGET_LANGUAGES: List<Pair<String, String>> = listOf(
        "en" to "English",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "it" to "Italian",
        "pt" to "Portuguese",
        "nl" to "Dutch",
        "sv" to "Swedish",
        "pl" to "Polish",
        "ru" to "Russian",
        "ja" to "Japanese",
        "zh" to "Chinese",
        "ko" to "Korean"
    )
}
