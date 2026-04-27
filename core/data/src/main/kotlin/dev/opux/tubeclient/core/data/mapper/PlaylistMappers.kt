package dev.opux.tubeclient.core.data.mapper

import dev.opux.tubeclient.core.database.entity.PlaylistEntity
import dev.opux.tubeclient.core.database.entity.PlaylistEntryEntity
import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.PlaylistEntry

fun PlaylistEntity.toDomain(itemCount: Int): Playlist = Playlist(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    itemCount = itemCount,
)

fun PlaylistEntryEntity.toDomain(): PlaylistEntry = PlaylistEntry(
    id = id,
    playlistId = playlistId,
    videoId = videoId,
    videoUrl = videoUrl,
    title = title,
    channelName = channelName,
    thumbnailUrl = thumbnailUrl,
    durationSeconds = durationSeconds,
    position = position,
    addedAt = addedAt,
)
