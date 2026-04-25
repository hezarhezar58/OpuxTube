package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow

class GetSubscriptionsUseCase(
    private val repository: SubscriptionRepository,
) {
    operator fun invoke(): Flow<List<Subscription>> = repository.observeAll()
}
