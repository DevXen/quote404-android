package com.quotebrowser.app.ui.auto

import android.content.Intent
import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Bundle
import android.service.media.MediaBrowserService
import com.quotebrowser.app.QuoteApp
import com.quotebrowser.app.data.DbInfo
import com.quotebrowser.app.ui.PlaylistPlayer
import com.quotebrowser.app.ui.PlaylistItem
import com.quotebrowser.app.ui.RADIO_STATION_ALL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * v1.63: Android Auto support. Exposes the quote radio stations (every
 * database plus the "everything shuffle" station) to the car's media
 * browser and drives the very same app-scoped [PlaylistPlayer] the phone
 * UI uses, so play/pause/skip in the car and on the phone stay in sync.
 */
class RadioBrowserService : MediaBrowserService() {

    companion object {
        private const val ROOT = "quote404_root"
        private const val STATION_ALL_LABEL = "Everything shuffle"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var player: PlaylistPlayer
    private lateinit var session: MediaSession
    private var stations: List<DbInfo> = emptyList()

    override fun onCreate() {
        super.onCreate()
        player = (applicationContext as QuoteApp).playlistPlayer
        session = MediaSession(this, "Quote404Radio").apply {
            setCallback(SessionCallback())
            setSessionToken(sessionToken)
        }
        // Keep the car's now-playing card and transport controls in sync
        // with the shared player.
        serviceScope.launch {
            combine(
                player.playing,
                player.index,
                player.items,
                player.radioMode,
                player.radioDbId
            ) { playing, index, items, radioMode, radioDbId ->
                updateSession(playing, index, items, radioMode, radioDbId)
            }.collect { }
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot = BrowserRoot(ROOT, null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowser.MediaItem>>
    ) {
        if (parentId != ROOT) {
            result.sendResult(mutableListOf())
            return
        }
        result.detach()
        serviceScope.launch {
            val dbs = runCatching {
                (applicationContext as QuoteApp).dbManager
                    .availableDatabases().first()
            }.getOrElse { emptyList() }
            stations = dbs
            val items = mutableListOf<MediaBrowser.MediaItem>()
            items += stationItem(RADIO_STATION_ALL, STATION_ALL_LABEL, "All databases, shuffled")
            dbs.forEach { db ->
                items += stationItem(db.id, db.label, "Quote404 radio")
            }
            result.sendResult(items)
        }
    }

    private fun stationItem(id: String, title: String, subtitle: String): MediaBrowser.MediaItem {
        val desc = MediaDescription.Builder()
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(subtitle)
            .build()
        return MediaBrowser.MediaItem(desc, MediaBrowser.MediaItem.FLAG_PLAYABLE)
    }

    private inner class SessionCallback : MediaSession.Callback() {
        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            if (mediaId.isNullOrEmpty()) return
            session.isActive = true
            player.startRadio(mediaId)
        }

        override fun onPlay() {
            session.isActive = true
            if (player.items.value.isEmpty()) {
                player.startRadio(player.radioDbId.value ?: RADIO_STATION_ALL)
            } else {
                player.resumePlayback()
            }
        }

        override fun onPause() {
            player.pausePlayback()
        }

        override fun onSkipToNext() {
            player.skip()
        }

        override fun onStop() {
            player.stopPlayback()
            session.isActive = false
        }
    }

    private fun updateSession(
        playing: Boolean,
        index: Int,
        items: List<PlaylistItem>,
        radioMode: Boolean,
        radioDbId: String?
    ) {
        val item = items.getOrNull(index)
        val state = when {
            item == null -> PlaybackState.STATE_STOPPED
            playing -> PlaybackState.STATE_PLAYING
            else -> PlaybackState.STATE_PAUSED
        }
        val actions = (if (playing) PlaybackState.ACTION_PAUSE else PlaybackState.ACTION_PLAY) or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_STOP or
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
        )
        val stationLabel = when {
            radioDbId == RADIO_STATION_ALL -> STATION_ALL_LABEL
            radioDbId != null -> stations.firstOrNull { it.id == radioDbId }?.label ?: radioDbId
            else -> "Quote404 Radio"
        }
        if (item != null) {
            val clean = item.rawText.replace(Regex("\\s+"), " ").trim().take(160)
            session.setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Quote #${item.number}")
                    .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, "Quote #${item.number}")
                    .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, clean)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, stationLabel)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, "Quote404 Radio")
                    .build()
            )
        } else {
            session.setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, "Quote404 Radio")
                    .putString(
                        MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE,
                        if (radioMode) stationLabel else "Pick a station to start"
                    )
                    .build()
            )
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        session.release()
        super.onDestroy()
    }

    // This service is only ever bound; onBind is handled by the framework.
    override fun onUnbind(intent: Intent?): Boolean = super.onUnbind(intent)
}
