package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDomain
import dev.opux.tubeclient.core.domain.model.SkipSegment
import dev.opux.tubeclient.core.domain.repository.SponsorBlockRepository
import dev.opux.tubeclient.core.network.sponsorblock.SponsorBlockApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SponsorBlockRepositoryImpl @Inject constructor(
    private val api: SponsorBlockApi,
) : SponsorBlockRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    override suspend fun getSkipSegments(videoId: String): Result<List<SkipSegment>> =
        withContext(io) {
            runCatching { api.getSkipSegments(videoId).map { it.toDomain() } }
                // SponsorBlock returns 404 when no segments exist for a video. The HTTP layer
                // surfaces it as an exception; treat any failure as "no segments" so playback
                // never blocks on a missing/unreachable SB response.
                .onFailure { Timber.w(it, "SponsorBlock fetch failed for %s", videoId) }
        }
}
