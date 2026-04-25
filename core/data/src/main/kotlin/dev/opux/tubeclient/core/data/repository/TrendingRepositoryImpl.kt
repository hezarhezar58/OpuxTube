package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toPreview
import dev.opux.tubeclient.core.data.paging.PageCache
import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.repository.TrendingRepository
import dev.opux.tubeclient.core.network.newpipe.NewPipeInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrendingRepositoryImpl @Inject constructor(
    private val newPipe: NewPipeInitializer,
    private val pageCache: PageCache,
) : TrendingRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    @Volatile
    private var activeKioskUrl: String? = null

    override suspend fun getTrending(pageToken: String?): Result<PagedResult<VideoPreview>> =
        withContext(io) {
            runCatching {
                if (pageToken != null) loadMore(pageToken) else loadInitial()
            }.onFailure { Timber.e(it, "getTrending failed") }
        }

    private fun loadInitial(): PagedResult<VideoPreview> {
        val kioskList = newPipe.youtube.kioskList

        var lastError: Throwable? = null
        for (id in PREFERRED_KIOSKS) {
            try {
                val handler = kioskList.getListLinkHandlerFactoryByType(id).fromId(id)
                val extractor = kioskList.getExtractorByUrl(handler.url, null)
                extractor.fetchPage()
                val initialPage = extractor.initialPage
                val previews = initialPage.items.orEmpty()
                    .filterIsInstance<StreamInfoItem>()
                    .map { it.toPreview() }
                if (previews.isEmpty()) {
                    Timber.w("Kiosk '%s' returned empty page; trying next", id)
                    lastError = IllegalStateException("Empty kiosk: $id")
                    continue
                }
                activeKioskUrl = handler.url
                Timber.d("Trending kiosk='%s' items=%d", id, previews.size)
                return PagedResult(
                    items = previews,
                    nextPageToken = pageCache.store(initialPage.nextPage),
                )
            } catch (e: Exception) {
                Timber.w(e, "Kiosk '%s' failed; trying next", id)
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("No working kiosk")
    }

    private fun loadMore(pageToken: String): PagedResult<VideoPreview> {
        val page = pageCache.consume(pageToken) ?: return PagedResult.empty()
        val kioskUrl = activeKioskUrl ?: return PagedResult.empty()
        val extractor = newPipe.youtube.kioskList.getExtractorByUrl(kioskUrl, null)
        val morePage = extractor.getPage(page)
        val previews = morePage.items.orEmpty()
            .filterIsInstance<StreamInfoItem>()
            .map { it.toPreview() }
        return PagedResult(
            items = previews,
            nextPageToken = pageCache.store(morePage.nextPage),
        )
    }

    private companion object {
        // YouTube has retired the combined "Trending" kiosk page; NewPipeExtractor PR #1402
        // confirms it now returns an error page upstream — there's no fix coming and the kiosk
        // is being removed from the extractor. Skip it and lead with the category kiosks that
        // actually return mixed-content listings. "live" stays as a last-resort fallback.
        val PREFERRED_KIOSKS = listOf(
            "trending_music",
            "trending_gaming",
            "trending_movies_and_shows",
            "live",
        )
    }
}
