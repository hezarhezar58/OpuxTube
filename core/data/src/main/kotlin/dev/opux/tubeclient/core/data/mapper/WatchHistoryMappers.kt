package dev.opux.tubeclient.core.data.mapper

import dev.opux.tubeclient.core.database.entity.WatchHistoryEntity
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry

fun WatchHistoryEntity.toDomain(): WatchHistoryEntry = WatchHistoryEntry(
    videoId = videoId,
    videoUrl = videoUrl,
    title = title,
    channelName = channelName,
    channelUrl = channelUrl,
    thumbnailUrl = thumbnailUrl,
    durationSeconds = durationSeconds,
    progressMs = progressMs,
    watchedAt = watchedAt,
)

fun WatchHistoryEntry.toEntity(existingId: Long = 0L): WatchHistoryEntity = WatchHistoryEntity(
    id = existingId,
    videoId = videoId,
    videoUrl = videoUrl,
    title = title,
    channelName = channelName,
    channelUrl = channelUrl,
    thumbnailUrl = thumbnailUrl,
    durationSeconds = durationSeconds,
    progressMs = progressMs,
    watchedAt = watchedAt,
)
