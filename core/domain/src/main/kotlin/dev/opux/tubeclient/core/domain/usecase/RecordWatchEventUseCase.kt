package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import dev.opux.tubeclient.core.domain.repository.WatchHistoryRepository

class RecordWatchEventUseCase(
    private val repository: WatchHistoryRepository,
) {
    suspend operator fun invoke(detail: VideoDetail, progressMs: Long = 0L) {
        repository.record(
            WatchHistoryEntry(
                videoId = detail.id,
                videoUrl = detail.url,
                title = detail.title,
                channelName = detail.channel.name,
                channelUrl = detail.channel.url,
                thumbnailUrl = detail.thumbnailUrl,
                durationSeconds = detail.durationSeconds,
                progressMs = progressMs,
                watchedAt = System.currentTimeMillis(),
            ),
        )
    }
}
