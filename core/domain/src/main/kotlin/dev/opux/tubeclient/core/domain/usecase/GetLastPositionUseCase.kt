package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository

class GetLastPositionUseCase(
    private val repository: WatchHistoryRepository,
) {
    suspend operator fun invoke(videoId: String): Long =
        repository.findLastForVideo(videoId)?.progressMs ?: 0L
}
