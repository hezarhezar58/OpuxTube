package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDomain
import dev.opux.tubeclient.core.data.mapper.toSubscriptionEntity
import dev.opux.tubeclient.core.database.dao.SubscriptionDao
import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val dao: SubscriptionDao,
) : SubscriptionRepository {

    override fun observeAll(): Flow<List<Subscription>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeIsSubscribed(channelUrl: String): Flow<Boolean> =
        dao.observeAll()
            .map { list -> list.any { it.channelUrl == channelUrl } }
            .distinctUntilChanged()

    override suspend fun subscribe(channel: ChannelDetail) {
        dao.upsert(channel.toSubscriptionEntity())
    }

    override suspend fun unsubscribe(channelUrl: String) {
        dao.delete(channelUrl)
    }
}
