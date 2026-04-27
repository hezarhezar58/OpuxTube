package dev.opux.tubeclient.core.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import dev.opux.tubeclient.core.player.model.PlaybackState
import dev.opux.tubeclient.core.player.service.OpuxPlaybackService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoMediaPlayerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val streamSelector: StreamSelector,
) : MediaPlayerController {

    private val sessionToken = SessionToken(
        context,
        ComponentName(context, OpuxPlaybackService::class.java),
    )

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playerFlow = MutableStateFlow<Player?>(null)
    override val playerFlow: StateFlow<Player?> = _playerFlow.asStateFlow()

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            _state.update {
                it.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING,
                    isReady = playbackState == Player.STATE_READY,
                )
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying, error = null) }
        }

        override fun onPlayerError(error: PlaybackException) {
            Timber.e(error, "Controller player error")
            _state.update { it.copy(error = error.localizedMessage ?: "Oynatma hatası") }
        }
    }

    init {
        connect()
    }

    private fun connect() {
        // Bring up the playback service so its SessionToken can resolve.
        context.startService(Intent(context, OpuxPlaybackService::class.java))

        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener({
            val ctrl = runCatching { future.get() }.getOrNull()
            if (ctrl != null) {
                mediaController = ctrl
                ctrl.addListener(playerListener)
                _playerFlow.value = ctrl
                Timber.d("MediaController connected")
            } else {
                Timber.e("Failed to build MediaController")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    override fun play(
        detail: VideoDetail,
        startPositionMs: Long,
        qualityOverride: VideoStream?,
    ) {
        val selection = streamSelector.select(detail, qualityOverride) ?: run {
            _state.update { it.copy(error = "Oynatılabilir akış bulunamadı") }
            return
        }
        val ctrl = mediaController
        if (ctrl == null) {
            // Queue: replay this once the connection succeeds.
            controllerFuture?.addListener(
                { play(detail, startPositionMs, qualityOverride) },
                ContextCompat.getMainExecutor(context),
            )
            return
        }
        val (videoUrl, audioUrl) = when (selection) {
            is StreamSelector.Selection.Progressive -> selection.video.url to null
            is StreamSelector.Selection.Merged -> selection.video.url to selection.audio.url
            is StreamSelector.Selection.VideoOnly -> selection.video.url to null
            is StreamSelector.Selection.AudioOnly -> selection.audio.url to null
        }
        Timber.d("Sending playStreams: video=%s audio=%s", videoUrl, audioUrl)
        val subtitleEntries = if (qualityOverride != null && qualityOverride.url.startsWith("file://")) {
            // Local downloaded playback: skip remote subtitles, the file is muxed standalone.
            emptyArray()
        } else {
            detail.subtitles
                .filter { it.url.isNotBlank() }
                // Prefer non-auto subtitles per language to keep the menu lean.
                .groupBy { it.languageTag.ifBlank { it.displayName } }
                .mapNotNull { (_, group) ->
                    group.firstOrNull { !it.isAutoGenerated } ?: group.firstOrNull()
                }
                .map { "${it.url}|${it.languageTag}|${it.displayName}|${it.mimeType}" }
                .toTypedArray()
        }
        val args = Bundle().apply {
            putString(OpuxPlaybackService.ARG_VIDEO_URL, videoUrl)
            putString(OpuxPlaybackService.ARG_AUDIO_URL, audioUrl)
            putLong(OpuxPlaybackService.ARG_START_MS, startPositionMs)
            putString(OpuxPlaybackService.ARG_TITLE, detail.title)
            putString(OpuxPlaybackService.ARG_ARTIST, detail.channel.name)
            putString(OpuxPlaybackService.ARG_ARTWORK_URL, detail.thumbnailUrl)
            putStringArray(OpuxPlaybackService.ARG_SUBTITLES, subtitleEntries)
        }
        ctrl.sendCustomCommand(
            SessionCommand(OpuxPlaybackService.CMD_PLAY_STREAMS, Bundle.EMPTY),
            args,
        )
    }

    override fun pause() { mediaController?.pause() }

    override fun resume() { mediaController?.play() }

    override fun seekTo(positionMs: Long) { mediaController?.seekTo(positionMs) }

    override fun setPlaybackSpeed(speed: Float) {
        mediaController?.setPlaybackSpeed(speed.coerceIn(0.25f, 4.0f))
    }

    override fun stop() {
        mediaController?.stop()
        mediaController?.clearMediaItems()
        _state.value = PlaybackState()
    }

    override fun release() {
        mediaController?.let {
            it.removeListener(playerListener)
            it.release()
        }
        mediaController = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        _playerFlow.value = null
    }
}
