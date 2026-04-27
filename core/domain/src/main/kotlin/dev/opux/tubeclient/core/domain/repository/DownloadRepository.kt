package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.DownloadedVideo
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun observeAll(): Flow<List<DownloadedVideo>>
    suspend fun find(videoId: String): DownloadedVideo?
    suspend fun upsert(downloaded: DownloadedVideo)
    suspend fun delete(videoId: String)
    suspend fun clearAll()
}
