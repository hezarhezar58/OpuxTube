package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDomain
import dev.opux.tubeclient.core.data.mapper.toEntity
import dev.opux.tubeclient.core.database.dao.DownloadedVideoDao
import dev.opux.tubeclient.core.domain.model.DownloadedVideo
import dev.opux.tubeclient.core.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val dao: DownloadedVideoDao,
) : DownloadRepository {

    override fun observeAll(): Flow<List<DownloadedVideo>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun find(videoId: String): DownloadedVideo? =
        dao.findByVideoId(videoId)?.toDomain()

    override suspend fun upsert(downloaded: DownloadedVideo) {
        dao.upsert(downloaded.toEntity())
    }

    override suspend fun delete(videoId: String) {
        val existing = dao.findByVideoId(videoId)
        if (existing != null) {
            runCatching { File(existing.filePath).delete() }
        }
        dao.delete(videoId)
    }

    override suspend fun clearAll() {
        // Delete files first so we don't leak storage even if the row purge crashes.
        val all = dao.observeAll().first()
        all.forEach { runCatching { File(it.filePath).delete() } }
        dao.clearAll()
    }
}
