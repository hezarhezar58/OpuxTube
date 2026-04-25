package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.repository.ChannelRepository

class GetChannelDetailsUseCase(
    private val repository: ChannelRepository,
) {
    suspend operator fun invoke(channelUrl: String): Result<ChannelDetail> =
        repository.getChannelDetail(channelUrl)
}
