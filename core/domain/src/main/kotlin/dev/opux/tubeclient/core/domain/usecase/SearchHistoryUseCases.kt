package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveSearchHistoryUseCase(private val repo: SearchHistoryRepository) {
    operator fun invoke(): Flow<List<String>> = repo.observeRecent()
}

class RecordSearchQueryUseCase(private val repo: SearchHistoryRepository) {
    suspend operator fun invoke(query: String) = repo.record(query.trim())
}

class DeleteSearchQueryUseCase(private val repo: SearchHistoryRepository) {
    suspend operator fun invoke(query: String) = repo.delete(query)
}

class ClearSearchHistoryUseCase(private val repo: SearchHistoryRepository) {
    suspend operator fun invoke() = repo.clearAll()
}
