package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.repository.ChannelRepository

class GetChannelVideosUseCase(
    private val repository: ChannelRepository,
) {
    suspend operator fun invoke(
        channelUrl: String,
        pageToken: String? = null,
    ): Result<PagedResult<VideoPreview>> =
        repository.getChannelVideos(channelUrl, pageToken)
}
