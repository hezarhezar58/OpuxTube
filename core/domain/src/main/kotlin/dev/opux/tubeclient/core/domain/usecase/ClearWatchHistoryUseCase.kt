package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository

class ClearWatchHistoryUseCase(
    private val repository: WatchHistoryRepository,
) {
    suspend operator fun invoke() = repository.clearAll()
}
