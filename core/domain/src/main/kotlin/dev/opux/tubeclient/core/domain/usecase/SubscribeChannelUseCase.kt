package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.repository.SubscriptionRepository

class SubscribeChannelUseCase(
    private val repository: SubscriptionRepository,
) {
    suspend operator fun invoke(channel: ChannelDetail) = repository.subscribe(channel)
}
