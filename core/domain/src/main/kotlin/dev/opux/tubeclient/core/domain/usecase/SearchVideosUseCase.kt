package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.SearchFilter
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.repository.SearchRepository

class SearchVideosUseCase(private val repository: SearchRepository) {
    suspend operator fun invoke(
        query: String,
        filter: SearchFilter = SearchFilter.VIDEOS,
        pageToken: String? = null,
    ): Result<PagedResult<VideoPreview>> {
        if (query.isBlank()) return Result.success(PagedResult.empty())
        return repository.search(query.trim(), filter, pageToken)
    }
}
