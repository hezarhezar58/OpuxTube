package dev.opux.tubeclient.core.data.mapper

import dev.opux.tubeclient.core.domain.model.ChannelDetail
import org.schabi.newpipe.extractor.channel.ChannelInfo

fun ChannelInfo.toDetail(): ChannelDetail = ChannelDetail(
    url = url,
    name = name.orEmpty(),
    avatarUrl = avatars.bestThumbnailUrl(),
    bannerUrl = banners.bestThumbnailUrl(),
    description = description.orEmpty(),
    subscriberCount = subscriberCount.coerceAtLeast(0),
    isVerified = isVerified,
)
