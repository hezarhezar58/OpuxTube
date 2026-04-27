package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDomain
import dev.opux.tubeclient.core.database.dao.PlaylistDao
import dev.opux.tubeclient.core.database.entity.PlaylistEntity
import dev.opux.tubeclient.core.database.entity.PlaylistEntryEntity
import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.PlaylistEntry
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val dao: PlaylistDao,
) : PlaylistRepository {

    override fun observePlaylists(): Flow<List<Playlist>> =
        combine(dao.observePlaylists(), dao.observeEntryCounts()) { playlists, counts ->
            val countMap: Map<Long, Int> = counts.associate { it.playlistId to it.count }
            playlists.map { it.toDomain(itemCount = countMap[it.id] ?: 0) }
        }

    override fun observeEntries(playlistId: Long): Flow<List<PlaylistEntry>> =
        dao.observeEntries(playlistId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun createPlaylist(name: String): Long {
        val now = System.currentTimeMillis()
        return dao.insertPlaylist(
            PlaylistEntity(name = name, createdAt = now, updatedAt = now),
        )
    }

    override suspend fun deletePlaylist(playlistId: Long) = dao.deletePlaylist(playlistId)

    override suspend fun addVideo(playlistId: Long, video: VideoDetail) {
        val now = System.currentTimeMillis()
        val nextPosition = dao.maxPosition(playlistId) + 1
        dao.addVideo(
            playlistId = playlistId,
            entry = PlaylistEntryEntity(
                playlistId = playlistId,
                videoId = video.id,
                videoUrl = video.url,
                title = video.title,
                channelName = video.channel.name,
                thumbnailUrl = video.thumbnailUrl,
                durationSeconds = video.durationSeconds,
                position = nextPosition,
                addedAt = now,
            ),
        )
        dao.touchPlaylist(playlistId, now)
    }

    override suspend fun removeVideo(playlistId: Long, videoId: String) {
        dao.removeVideo(playlistId, videoId)
        dao.touchPlaylist(playlistId, System.currentTimeMillis())
    }
}
