package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDetail
import dev.opux.tubeclient.core.data.mapper.toPreview
import dev.opux.tubeclient.core.data.paging.PageCache
import dev.opux.tubeclient.core.domain.model.ChannelDetail
import dev.opux.tubeclient.core.domain.model.PagedResult
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.repository.ChannelRepository
import dev.opux.tubeclient.core.network.newpipe.NewPipeInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val newPipe: NewPipeInitializer,
    private val pageCache: PageCache,
) : ChannelRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    @Volatile
    private var activeTabHandler: ListLinkHandler? = null

    override suspend fun getChannelDetail(channelUrl: String): Result<ChannelDetail> =
        withContext(io) {
            runCatching {
                val service = newPipe.youtube
                val handler = service.channelLHFactory.fromUrl(channelUrl)
                ChannelInfo.getInfo(service, handler.url).toDetail()
            }.onFailure { Timber.e(it, "getChannelDetail failed for %s", channelUrl) }
        }

    override suspend fun getChannelVideos(
        channelUrl: String,
        pageToken: String?,
    ): Result<PagedResult<VideoPreview>> = withContext(io) {
        runCatching {
            val service = newPipe.youtube
            if (pageToken == null) {
                val channelHandler = service.channelLHFactory.fromUrl(channelUrl)
                val info = ChannelInfo.getInfo(service, channelHandler.url)
                val videosTab = info.tabs.firstOrNull { handler ->
                    handler.contentFilters.contains(ChannelTabs.VIDEOS)
                } ?: info.tabs.firstOrNull()
                if (videosTab == null) return@runCatching PagedResult.empty<VideoPreview>()

                val tabInfo = ChannelTabInfo.getInfo(service, videosTab)
                activeTabHandler = videosTab
                val previews = tabInfo.relatedItems.orEmpty()
                    .filterIsInstance<StreamInfoItem>()
                    .map { it.toPreview() }
                PagedResult(
                    items = previews,
                    nextPageToken = pageCache.store(tabInfo.nextPage),
                )
            } else {
                val page = pageCache.consume(pageToken)
                    ?: return@runCatching PagedResult.empty<VideoPreview>()
                val handler = activeTabHandler
                    ?: return@runCatching PagedResult.empty<VideoPreview>()
                val morePage = ChannelTabInfo.getMoreItems(service, handler, page)
                val previews = morePage.items.orEmpty()
                    .filterIsInstance<StreamInfoItem>()
                    .map { it.toPreview() }
                PagedResult(
                    items = previews,
                    nextPageToken = pageCache.store(morePage.nextPage),
                )
            }
        }.onFailure { Timber.e(it, "getChannelVideos failed for %s", channelUrl) }
    }
}
