package dev.opux.tubeclient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val channelUrl: String,
    val name: String,
    val avatarUrl: String?,
    val subscriberCount: Long,
    val subscribedAt: Long,
)
