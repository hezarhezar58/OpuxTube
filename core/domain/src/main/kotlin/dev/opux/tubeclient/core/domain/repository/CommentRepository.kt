package dev.opux.tubeclient.core.domain.repository

import dev.opux.tubeclient.core.domain.model.Comment

interface CommentRepository {
    suspend fun getComments(videoUrl: String): Result<List<Comment>>
}
