package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository

class UpdateWatchProgressUseCase(
    private val repository: WatchHistoryRepository,
) {
    suspend operator fun invoke(videoId: String, progressMs: Long) {
        repository.updateProgress(videoId = videoId, progressMs = progressMs)
    }
}
