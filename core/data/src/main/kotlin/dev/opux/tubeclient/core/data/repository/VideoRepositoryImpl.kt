package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDetail
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.repository.VideoRepository
import dev.opux.tubeclient.core.domain.util.VideoIdExtractor
import dev.opux.tubeclient.core.network.newpipe.NewPipeInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.stream.StreamInfo
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val newPipe: NewPipeInitializer,
) : VideoRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    override suspend fun getVideoDetails(videoUrl: String): Result<VideoDetail> =
        withContext(io) {
            runCatching {
                val resolved = resolveUrl(videoUrl)
                val service = newPipe.youtube
                val linkHandler = service.streamLHFactory.fromUrl(resolved)
                val info = StreamInfo.getInfo(service, linkHandler.url)
                info.toDetail()
            }.onFailure { Timber.e(it, "getVideoDetails failed for %s", videoUrl) }
        }

    private fun resolveUrl(input: String): String =
        if (input.startsWith("http", ignoreCase = true)) {
            input
        } else {
            val id = VideoIdExtractor.extractId(input) ?: input
            VideoIdExtractor.buildWatchUrl(id)
        }
}
