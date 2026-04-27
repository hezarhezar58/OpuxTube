package dev.opux.tubeclient.core.domain.usecase

import dev.opux.tubeclient.core.domain.model.Comment
import dev.opux.tubeclient.core.domain.repository.CommentRepository

class GetCommentsUseCase(private val repository: CommentRepository) {
    suspend operator fun invoke(videoUrl: String): Result<List<Comment>> =
        repository.getComments(videoUrl)
}

class GetCommentRepliesUseCase(private val repository: CommentRepository) {
    suspend operator fun invoke(token: String): Result<List<Comment>> =
        repository.getReplies(token)
}
