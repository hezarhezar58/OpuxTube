package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.SearchFilter
import dev.opux.tubeclient.core.domain.model.VideoPreview

interface SearchRepository {
    suspend fun search(
        query: String,
        filter: SearchFilter = SearchFilter.VIDEOS,
        pageToken: String? = null,
    ): Result<PagedResult<VideoPreview>>
}
