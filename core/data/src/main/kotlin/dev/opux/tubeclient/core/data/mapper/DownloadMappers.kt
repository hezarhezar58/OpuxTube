package dev.opux.tubeclient.core.data.mapper

import dev.opux.tubeclient.core.database.entity.DownloadedVideoEntity
import dev.opux.tubeclient.core.domain.model.DownloadedVideo

fun DownloadedVideoEntity.toDomain(): DownloadedVideo = DownloadedVideo(
    videoId = videoId,
    videoUrl = videoUrl,
    title = title,
    channelName = channelName,
    thumbnailUrl = thumbnailUrl,
    durationSeconds = durationSeconds,
    filePath = filePath,
    fileSizeBytes = fileSizeBytes,
    mimeType = mimeType,
    isAudioOnly = isAudioOnly,
    downloadedAt = downloadedAt,
)

fun DownloadedVideo.toEntity(): DownloadedVideoEntity = DownloadedVideoEntity(
    videoId = videoId,
    videoUrl = videoUrl,
    title = title,
    channelName = channelName,
    thumbnailUrl = thumbnailUrl,
    durationSeconds = durationSeconds,
    filePath = filePath,
    fileSizeBytes = fileSizeBytes,
    mimeType = mimeType,
    isAudioOnly = isAudioOnly,
    downloadedAt = downloadedAt,
)
