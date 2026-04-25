package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    fun observeRecent(limit: Int = 200): Flow<List<WatchHistoryEntry>>
    suspend fun findLastForVideo(videoId: String): WatchHistoryEntry?
    suspend fun record(entry: WatchHistoryEntry)
    suspend fun updateProgress(videoId: String, progressMs: Long)
    suspend fun delete(videoId: String)
    suspend fun clearAll()
}
