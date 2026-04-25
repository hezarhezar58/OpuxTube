package dev.opux.tubeclient.core.domain.model

data class VideoPreview(
    val id: String,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val viewCount: Long,
    val uploadedAt: String?,
    val channelName: String,
    val channelUrl: String,
    val channelAvatarUrl: String?,
    val isLive: Boolean,
    val isShort: Boolean,
)
