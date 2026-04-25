package dev.opux.tubeclient.feature.channel.presentation

import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.model.VideoPreview

data class ChannelUiState(
    val isLoading: Boolean = true,
    val detail: ChannelDetail? = null,
    val videos: List<VideoPreview> = emptyList(),
    val isAppending: Boolean = false,
    val nextPageToken: String? = null,
    val isSubscribed: Boolean = false,
    val error: String? = null,
) {
    val canLoadMore: Boolean get() = nextPageToken != null && !isAppending && error == null
}
