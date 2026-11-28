package com.quotebrowser.app.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.quotebrowser.app.ui.StickerRenderer
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * v1.90 sticker packs: named sets of 512x512 PNG stickers rendered from
 * quotes, stored under filesDir/sticker_packs/<id>/ with a pack.json
 * manifest (name, source database, created date, sticker count, and a
 * reverse-domain identifier for importer apps). Sharing exports the
 * whole pack as a ZIP (stickers + tray.png + pack.json) that Telegram /
 * WhatsApp sticker-importer apps can consume.
 */
data class StickerPack(
    val id: String,
    val name: String,
    val dbLabel: String,
    val createdAt: Long,
    val count: Int
)

object StickerPackStore {

    /** WhatsApp's per-pack sticker cap; Telegram allows more, 30 is safe. */
    const val MAX_STICKERS = 30

    private fun root(context: Context): File =
        File(context.filesDir, "sticker_packs").apply { mkdirs() }

    private fun packDir(context: Context, id: String): File =
        File(root(context), id)

    fun listPacks(context: Context): List<StickerPack> {
        val dirs = root(context).listFiles { f -> f.isDirectory } ?: return emptyList()
        return dirs.mapNotNull { dir ->
            try {
                val json = JSONObject(File(dir, "pack.json").readText())
                StickerPack(
                    id = dir.name,
                    name = json.optString("name", dir.name),
                    dbLabel = json.optString("db", ""),
                    createdAt = json.optLong("created", dir.lastModified()),
                    count = json.optInt("count", 0)
                )
            } catch (e: Exception) {
                null
            }
        }.sortedByDescending { it.createdAt }
    }

    fun createPack(
        context: Context,
        name: String,
        dbLabel: String,
        stickerTexts: List<String>
    ): StickerPack {
        val texts = stickerTexts.take(MAX_STICKERS)
        require(texts.isNotEmpty()) { "No quotes to make stickers from" }
        val now = System.currentTimeMillis()
        val slug = name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "pack" }
        val id = "$slug-$now"
        val dir = packDir(context, id).apply { mkdirs() }
        texts.forEachIndexed { i, text ->
            val bmp = StickerRenderer.renderSticker(text)
            FileOutputStream(File(dir, "sticker_%02d.png".format(i + 1))).use { out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bmp.recycle()
        }
        val tray = StickerRenderer.renderTray(name)
        FileOutputStream(File(dir, "tray.png")).use { out ->
            tray.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        tray.recycle()
        val json = JSONObject()
            .put("identifier", "com.quotebrowser.app.stickers.$slug")
            .put("name", name)
            .put("db", dbLabel)
            .put("created", now)
            .put("count", texts.size)
            .put("publisher", "Quote404")
        File(dir, "pack.json").writeText(json.toString(2))
        return StickerPack(id, name, dbLabel, now, texts.size)
    }

    fun deletePack(context: Context, pack: StickerPack) {
        packDir(context, pack.id).deleteRecursively()
    }

    fun buildZip(context: Context, pack: StickerPack): File {
        val dir = packDir(context, pack.id)
        val zipDir = File(context.cacheDir, "sticker_zips").apply { mkdirs() }
        val zipFile = File(zipDir, "${pack.id}.zip")
        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            dir.listFiles()
                ?.sortedBy { it.name }
                ?.forEach { f ->
                    zip.putNextEntry(ZipEntry(f.name))
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
        }
        return zipFile
    }

    fun sharePack(context: Context, pack: StickerPack) {
        try {
            val zipFile = buildZip(context, pack)
            val uri = FileProvider.getUriForFile(
                context, "com.quotebrowser.app.fileprovider", zipFile
            )
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Quote404 sticker pack: ${pack.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "Share sticker pack"))
        } catch (e: Exception) {
            Toast.makeText(context, "Couldn't share sticker pack", Toast.LENGTH_SHORT)
                .show()
        }
    }
}
