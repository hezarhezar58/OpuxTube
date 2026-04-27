package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.database.dao.SearchHistoryDao
import dev.opux.tubeclient.core.database.entity.SearchHistoryEntity
import dev.opux.tubeclient.core.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepositoryImpl @Inject constructor(
    private val dao: SearchHistoryDao,
) : SearchHistoryRepository {

    override fun observeRecent(limit: Int): Flow<List<String>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.query } }

    override suspend fun record(query: String) {
        if (query.isBlank()) return
        dao.upsert(SearchHistoryEntity(query = query, lastUsedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(query: String) {
        dao.delete(query)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
