package dev.opux.tubeclient.core.player

import androidx.media3.common.Player
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import dev.opux.tubeclient.core.player.model.PlaybackState
import kotlinx.coroutines.flow.StateFlow

interface MediaPlayerController {
    /** Emits the active [Player] once the controller has connected to the playback service. */
    val playerFlow: StateFlow<Player?>

    val state: StateFlow<PlaybackState>

    fun play(
        detail: VideoDetail,
        startPositionMs: Long = 0L,
        qualityOverride: VideoStream? = null,
    )
    fun pause()
    fun resume()
    fun seekTo(positionMs: Long)
    fun setPlaybackSpeed(speed: Float)
    fun stop()
    fun release()
}
