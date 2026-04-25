package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.repository.SubscriptionRepository

class UnsubscribeChannelUseCase(
    private val repository: SubscriptionRepository,
) {
    suspend operator fun invoke(channelUrl: String) = repository.unsubscribe(channelUrl)
}
