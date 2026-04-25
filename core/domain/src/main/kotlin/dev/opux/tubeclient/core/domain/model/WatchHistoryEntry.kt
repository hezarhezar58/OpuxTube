package dev.opux.tubeclient.core.domain.model

data class WatchHistoryEntry(
    val videoId: String,
    val videoUrl: String,
    val title: String,
    val channelName: String,
    val channelUrl: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val progressMs: Long,
    val watchedAt: Long,
)
