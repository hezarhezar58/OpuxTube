package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow

class IsSubscribedUseCase(
    private val repository: SubscriptionRepository,
) {
    operator fun invoke(channelUrl: String): Flow<Boolean> =
        repository.observeIsSubscribed(channelUrl)
}
