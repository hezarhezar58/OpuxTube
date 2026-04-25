package dev.opux.tubeclient.core.domain.model

data class ChannelDetail(
    val url: String,
    val name: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val description: String,
    val subscriberCount: Long,
    val isVerified: Boolean,
)
