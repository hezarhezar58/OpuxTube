package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.VideoPreview

interface TrendingRepository {
    suspend fun getTrending(pageToken: String? = null): Result<PagedResult<VideoPreview>>
}
