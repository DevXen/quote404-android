package com.quotebrowser.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotebrowser.app.data.DatabaseManager
import com.quotebrowser.app.data.Quote
import com.quotebrowser.app.data.StickerPack
import com.quotebrowser.app.data.StickerPackStore
import com.quotebrowser.app.ui.QuoteCleanOptions
import com.quotebrowser.app.ui.applyCleanMode
import com.quotebrowser.app.ui.applyKidSafeMask
import com.quotebrowser.app.ui.cleanDisplayText
import com.quotebrowser.app.ui.repoFor
import com.quotebrowser.app.ui.rememberCappedFlingBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * v1.90 sticker packs: build a pack from the current database's
 * favorites (up to [StickerPackStore.MAX_STICKERS] — WhatsApp's cap),
 * rendered as 512x512 PNG stickers with the same Clean mode / Kid-safe
 * transforms the quote display uses. Packs live on-device; Share exports
 * a ZIP (stickers + tray icon + pack.json) for messaging-app importers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickerPacksScreen(
    manager: DatabaseManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dbId by manager.currentDbId.collectAsStateWithLifecycle()
    val cleanMode by manager.cleanModeEnabled.collectAsStateWithLifecycle()
    val kidSafe by manager.kidSafeEnabled.collectAsStateWithLifecycle()
    val dbLabel = remember(dbId) { manager.shortLabel(dbId) }

    var favorites by remember { mutableStateOf<List<Quote>>(emptyList()) }
    LaunchedEffect(dbId) {
        favorites = runCatching {
            manager.repoFor(dbId).favorites().first()
        }.getOrDefault(emptyList())
    }

    var packs by remember {
        mutableStateOf(StickerPackStore.listPacks(context))
    }
    var packName by remember(dbLabel) {
        mutableStateOf("$dbLabel favorites")
    }
    var creating by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Sticker packs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            flingBehavior = rememberCappedFlingBehavior(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "Make a sticker pack from your $dbLabel favorites. " +
                        "Each quote becomes a 512x512 sticker image; share " +
                        "the pack ZIP into a sticker-importer app to use " +
                        "them in WhatsApp, Telegram, and friends.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                OutlinedTextField(
                    value = packName,
                    onValueChange = { packName = it },
                    label = { Text("Pack name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(
                    onClick = {
                        if (creating) return@Button
                        creating = true
                        message = null
                        val source = favorites
                        val name = packName.trim()
                            .ifEmpty { "$dbLabel favorites" }
                        scope.launch {
                            try {
                                val pack = withContext(Dispatchers.IO) {
                                    val texts = source
                                        .take(StickerPackStore.MAX_STICKERS)
                                        .map { q ->
                                            var t = q.text
                                            if (cleanMode) t = applyCleanMode(t)
                                            if (kidSafe) t = applyKidSafeMask(t)
                                            cleanDisplayText(
                                                t,
                                                irc = true,
                                                dialogue = true,
                                                opts = QuoteCleanOptions()
                                            )
                                        }
                                    StickerPackStore.createPack(
                                        context, name, dbLabel, texts
                                    )
                                }
                                packs = StickerPackStore.listPacks(context)
                                message = "Created \"${pack.name}\" — " +
                                    "${pack.count} stickers."
                            } catch (e: Exception) {
                                message = "Couldn't create the pack: " +
                                    (e.message ?: "unknown error")
                            } finally {
                                creating = false
                            }
                        }
                    },
                    enabled = !creating && favorites.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            creating -> "Rendering stickers…"
                            favorites.isEmpty() ->
                                "No favorites in $dbLabel yet"
                            else -> "Create pack from " +
                                "${minOf(favorites.size, StickerPackStore.MAX_STICKERS)} " +
                                "favorites"
                        }
                    )
                }
                if (favorites.size > StickerPackStore.MAX_STICKERS) {
                    Text(
                        "Only the first ${StickerPackStore.MAX_STICKERS} " +
                            "favorites are used (WhatsApp's per-pack cap).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                message?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            if (packs.isNotEmpty()) {
                item {
                    Text(
                        "Your packs",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(packs, key = { it.id }) { pack ->
                    PackRow(
                        pack = pack,
                        onShare = { StickerPackStore.sharePack(context, pack) },
                        onDelete = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    StickerPackStore.deletePack(context, pack)
                                }
                                packs = StickerPackStore.listPacks(context)
                            }
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PackRow(
    pack: StickerPack,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val date = remember(pack.createdAt) {
        SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(pack.createdAt))
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(pack.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${pack.count} stickers · ${pack.dbLabel} · $date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) { Text("Delete") }
                TextButton(onClick = onShare) { Text("Share ZIP") }
            }
        }
    }
}
