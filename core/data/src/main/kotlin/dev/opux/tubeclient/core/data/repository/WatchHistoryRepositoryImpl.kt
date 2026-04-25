package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDomain
import dev.opux.tubeclient.core.data.mapper.toEntity
import dev.opux.tubeclient.core.database.dao.WatchHistoryDao
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val dao: WatchHistoryDao,
) : WatchHistoryRepository {

    override fun observeRecent(limit: Int): Flow<List<WatchHistoryEntry>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun findLastForVideo(videoId: String): WatchHistoryEntry? =
        dao.findLastForVideo(videoId)?.toDomain()

    override suspend fun updateProgress(videoId: String, progressMs: Long) {
        dao.updateProgress(
            videoId = videoId,
            progressMs = progressMs,
            watchedAt = System.currentTimeMillis(),
        )
    }

    override suspend fun record(entry: WatchHistoryEntry) {
        // Keep one row per video: update timestamp+progress if it already exists,
        // otherwise insert with auto-generated id. There is no UNIQUE constraint on
        // videoId so we manage uniqueness here rather than at the schema level.
        val existing = dao.findLastForVideo(entry.videoId)
        if (existing != null) {
            dao.updateProgress(
                videoId = entry.videoId,
                progressMs = entry.progressMs,
                watchedAt = entry.watchedAt,
            )
        } else {
            dao.upsert(entry.toEntity())
        }
    }

    override suspend fun delete(videoId: String) = dao.deleteByVideoId(videoId)

    override suspend fun clearAll() = dao.clearAll()
}
