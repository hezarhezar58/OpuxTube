package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.repository.TrendingRepository

class GetTrendingUseCase(private val repository: TrendingRepository) {
    suspend operator fun invoke(pageToken: String? = null): Result<PagedResult<VideoPreview>> =
        repository.getTrending(pageToken)
}
