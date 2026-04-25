package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.VideoPreview

interface ChannelRepository {
    suspend fun getChannelDetail(channelUrl: String): Result<ChannelDetail>
    suspend fun getChannelVideos(
        channelUrl: String,
        pageToken: String? = null,
    ): Result<PagedResult<VideoPreview>>
}
