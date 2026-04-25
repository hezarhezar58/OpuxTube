package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow

class GetWatchHistoryUseCase(
    private val repository: WatchHistoryRepository,
) {
    operator fun invoke(limit: Int = 200): Flow<List<WatchHistoryEntry>> =
        repository.observeRecent(limit)
}
