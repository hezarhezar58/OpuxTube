package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.domain.model.Comment
import dev.opux.tubeclient.core.domain.repository.CommentRepository
import dev.opux.tubeclient.core.network.newpipe.NewPipeInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.comments.CommentsInfo
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val newPipe: NewPipeInitializer,
) : CommentRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    override suspend fun getComments(videoUrl: String): Result<List<Comment>> =
        withContext(io) {
            runCatching {
                val service = newPipe.youtube
                val info = CommentsInfo.getInfo(service, videoUrl)
                if (info.isCommentsDisabled) emptyList() else info.relatedItems.map { it.toDomain() }
            }.onFailure { Timber.w(it, "getComments failed for %s", videoUrl) }
        }

    private fun CommentsInfoItem.toDomain(): Comment = Comment(
        id = commentId.orEmpty().ifEmpty { "${uploaderName}_${textualUploadDate}_${likeCount}" },
        text = commentText?.content.orEmpty(),
        authorName = uploaderName.orEmpty(),
        authorAvatarUrl = uploaderAvatars?.firstOrNull()?.url,
        likeCount = likeCount.coerceAtLeast(0),
        replyCount = replyCount.coerceAtLeast(0),
        uploadedAt = textualUploadDate,
        isPinned = isPinned,
        isHeartedByUploader = isHeartedByUploader,
    )
}
