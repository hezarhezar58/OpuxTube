package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun observeAll(): Flow<List<Subscription>>
    fun observeIsSubscribed(channelUrl: String): Flow<Boolean>
    suspend fun subscribe(channel: ChannelDetail)
    suspend fun unsubscribe(channelUrl: String)
}
