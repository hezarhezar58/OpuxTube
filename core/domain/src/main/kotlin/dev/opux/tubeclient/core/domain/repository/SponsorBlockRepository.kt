package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.SkipSegment

interface SponsorBlockRepository {
    suspend fun getSkipSegments(videoId: String): Result<List<SkipSegment>>
}
