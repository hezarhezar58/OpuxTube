package dev.opux.tubeclient.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    fun observeRecent(limit: Int = 20): Flow<List<String>>
    suspend fun record(query: String)
    suspend fun delete(query: String)
    suspend fun clearAll()
}
