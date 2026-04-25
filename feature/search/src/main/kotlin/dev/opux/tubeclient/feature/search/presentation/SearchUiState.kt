package dev.opux.tubeclient.feature.search.presentation

import dev.opux.tubeclient.core.domain.model.VideoPreview

data class SearchUiState(
    val query: String = "",
    val items: List<VideoPreview> = emptyList(),
    val isLoading: Boolean = false,
    val isAppending: Boolean = false,
    val error: String? = null,
    val nextPageToken: String? = null,
    val hasSearched: Boolean = false,
) {
    val canLoadMore: Boolean get() = nextPageToken != null && !isAppending && error == null
    val isEmptyResult: Boolean get() = hasSearched && !isLoading && error == null && items.isEmpty()
}
