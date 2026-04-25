package dev.opux.tubeclient.feature.home.presentation

import dev.opux.tubeclient.core.domain.model.VideoPreview

data class HomeUiState(
    val items: List<VideoPreview> = emptyList(),
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val error: String? = null,
    val nextPageToken: String? = null,
) {
    val canLoadMore: Boolean get() = nextPageToken != null && !isAppending && error == null
    val isInitialLoad: Boolean get() = isLoading && items.isEmpty()
}
