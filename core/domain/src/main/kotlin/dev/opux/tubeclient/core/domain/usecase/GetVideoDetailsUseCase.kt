package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.repository.VideoRepository

class GetVideoDetailsUseCase(private val repository: VideoRepository) {
    suspend operator fun invoke(videoUrl: String): Result<VideoDetail> =
        repository.getVideoDetails(videoUrl)
}
