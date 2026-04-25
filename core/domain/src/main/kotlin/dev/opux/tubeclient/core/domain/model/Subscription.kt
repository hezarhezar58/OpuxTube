package dev.opux.tubeclient.core.domain.model

data class Subscription(
    val channelUrl: String,
    val name: String,
    val avatarUrl: String?,
    val subscriberCount: Long,
    val subscribedAt: Long,
)
