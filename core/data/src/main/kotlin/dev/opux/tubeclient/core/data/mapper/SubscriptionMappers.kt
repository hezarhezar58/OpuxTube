package dev.opux.tubeclient.core.data.mapper

import dev.opux.tubeclient.core.database.entity.SubscriptionEntity
import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.model.Subscription

fun SubscriptionEntity.toDomain(): Subscription = Subscription(
    channelUrl = channelUrl,
    name = name,
    avatarUrl = avatarUrl,
    subscriberCount = subscriberCount,
    subscribedAt = subscribedAt,
)

fun ChannelDetail.toSubscriptionEntity(now: Long = System.currentTimeMillis()): SubscriptionEntity =
    SubscriptionEntity(
        channelUrl = url,
        name = name,
        avatarUrl = avatarUrl,
        subscriberCount = subscriberCount,
        subscribedAt = now,
    )
