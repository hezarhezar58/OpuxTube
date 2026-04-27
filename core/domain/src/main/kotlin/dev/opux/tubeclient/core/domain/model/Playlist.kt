package dev.opux.tubeclient.core.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val itemCount: Int,
)

data class PlaylistEntry(
    val id: Long,
    val playlistId: Long,
    val videoId: String,
    val videoUrl: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val position: Int,
    val addedAt: Long,
)
