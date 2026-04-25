package dev.opux.tubeclient.core.domain.model

data class PagedResult<T>(
    val items: List<T>,
    val nextPageToken: String?,
) {
    val hasMore: Boolean get() = nextPageToken != null

    companion object {
        fun <T> empty(): PagedResult<T> = PagedResult(emptyList(), null)
    }
}
