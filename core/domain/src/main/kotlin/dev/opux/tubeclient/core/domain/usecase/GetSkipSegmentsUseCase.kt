package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.SkipSegment
import dev.opux.tubeclient.core.domain.repository.SponsorBlockRepository

class GetSkipSegmentsUseCase(private val repository: SponsorBlockRepository) {
    suspend operator fun invoke(videoId: String): Result<List<SkipSegment>> =
        repository.getSkipSegments(videoId)
}
