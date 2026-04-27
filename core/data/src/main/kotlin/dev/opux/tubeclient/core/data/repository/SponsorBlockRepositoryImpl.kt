package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.data.mapper.toDomain
import dev.opux.tubeclient.core.domain.model.SkipSegment
import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.preferences.SponsorBlockPreferences
import dev.opux.tubeclient.core.domain.repository.SponsorBlockRepository
import dev.opux.tubeclient.core.network.sponsorblock.SponsorBlockApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SponsorBlockRepositoryImpl @Inject constructor(
    private val api: SponsorBlockApi,
    private val preferences: SponsorBlockPreferences,
) : SponsorBlockRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    override suspend fun getSkipSegments(videoId: String): Result<List<SkipSegment>> =
        withContext(io) {
            val enabled: Set<SponsorBlockCategory> = preferences.enabledCategories.first()
            if (enabled.isEmpty()) return@withContext Result.success(emptyList())
            runCatching {
                api.getSkipSegments(videoId, categories = enabled.toCategoryQuery())
                    .map { it.toDomain() }
            }
                // SponsorBlock returns 404 when no segments exist for a video. The HTTP layer
                // surfaces it as an exception; treat any failure as "no segments" so playback
                // never blocks on a missing/unreachable SB response.
                .onFailure { Timber.w(it, "SponsorBlock fetch failed for %s", videoId) }
        }

    private fun Set<SponsorBlockCategory>.toCategoryQuery(): String =
        joinToString(prefix = "[", postfix = "]") { "\"${it.apiKey}\"" }
}
