package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.VideoDetail

interface VideoRepository {
    suspend fun getVideoDetails(videoUrl: String): Result<VideoDetail>
}
