package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toPreview
import dev.opux.tubeclient.core.data.paging.PageCache
import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.SearchFilter
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.repository.SearchRepository
import dev.opux.tubeclient.core.network.newpipe.NewPipeInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val newPipe: NewPipeInitializer,
    private val pageCache: PageCache,
) : SearchRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    override suspend fun search(
        query: String,
        filter: SearchFilter,
        pageToken: String?,
    ): Result<PagedResult<VideoPreview>> = withContext(io) {
        runCatching {
            val service = newPipe.youtube
            val contentFilters = listOf(filter.toNewPipeFilter())
            val handler = service.searchQHFactory.fromQuery(query, contentFilters, "")

            val (items, nextPage) = if (pageToken == null) {
                val info = SearchInfo.getInfo(service, handler)
                info.relatedItems.orEmpty() to info.nextPage
            } else {
                val page = pageCache.consume(pageToken)
                    ?: return@runCatching PagedResult.empty<VideoPreview>()
                val more = SearchInfo.getMoreItems(service, handler, page)
                more.items.orEmpty() to more.nextPage
            }

            val previews = items.filterIsInstance<StreamInfoItem>().map { it.toPreview() }
            PagedResult(items = previews, nextPageToken = pageCache.store(nextPage))
        }.onFailure { Timber.e(it, "search failed for %s", query) }
    }

    private fun SearchFilter.toNewPipeFilter(): String = when (this) {
        SearchFilter.ALL -> YoutubeSearchQueryHandlerFactory.ALL
        SearchFilter.VIDEOS -> YoutubeSearchQueryHandlerFactory.VIDEOS
        SearchFilter.CHANNELS -> YoutubeSearchQueryHandlerFactory.CHANNELS
        SearchFilter.PLAYLISTS -> YoutubeSearchQueryHandlerFactory.PLAYLISTS
    }
}
