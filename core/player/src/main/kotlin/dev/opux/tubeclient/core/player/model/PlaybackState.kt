package dev.opux.tubeclient.core.player.model

data class PlaybackState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val error: String? = null,
)
