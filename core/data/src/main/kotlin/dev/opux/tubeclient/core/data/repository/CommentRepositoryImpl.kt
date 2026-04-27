package dev.opux.tubeclient.core.data.repository

import dev.opux.tubeclient.core.domain.model.Comment
import dev.opux.tubeclient.core.domain.repository.CommentRepository
import dev.opux.tubeclient.core.network.newpipe.NewPipeInitializer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.comments.CommentsInfo
import org.schabi.newpipe.extractor.comments.CommentsInfoItem
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val newPipe: NewPipeInitializer,
) : CommentRepository {

    private val io: CoroutineDispatcher = Dispatchers.IO

    // Reply pagination needs the original CommentsInfo — NewPipe stores per-service
    // session state on it. We keep one entry per token (one token per top-level item
    // that exposes a replies Page) so the "Yanıtları gör" call can resolve later.
    private data class ReplyContext(val info: CommentsInfo, val page: Page)

    private val replyContexts = ConcurrentHashMap<String, ReplyContext>()

    override suspend fun getComments(videoUrl: String): Result<List<Comment>> =
        withContext(io) {
            runCatching {
                val service = newPipe.youtube
                val info = CommentsInfo.getInfo(service, videoUrl)
                if (info.isCommentsDisabled) {
                    emptyList()
                } else {
                    info.relatedItems.map { item -> item.toDomain(info) }
                }
            }.onFailure { Timber.w(it, "getComments failed for %s", videoUrl) }
        }

    override suspend fun getReplies(token: String): Result<List<Comment>> =
        withContext(io) {
            runCatching {
                val ctx = replyContexts[token]
                    ?: error("Reply context for token $token is no longer cached")
                val service = newPipe.youtube
                val page = CommentsInfo.getMoreItems(service, ctx.info, ctx.page)
                page.items.orEmpty().map { item -> item.toDomain(ctx.info) }
            }.onFailure { Timber.w(it, "getReplies failed for token %s", token) }
        }

    private fun CommentsInfoItem.toDomain(parentInfo: CommentsInfo): Comment {
        val token = replies?.let { page ->
            val newToken = UUID.randomUUID().toString()
            replyContexts[newToken] = ReplyContext(parentInfo, page)
            newToken
        }
        return Comment(
            id = commentId.orEmpty().ifEmpty { "${uploaderName}_${textualUploadDate}_${likeCount}" },
            text = commentText?.content.orEmpty(),
            authorName = uploaderName.orEmpty(),
            authorAvatarUrl = uploaderAvatars?.firstOrNull()?.url,
            likeCount = likeCount.coerceAtLeast(0),
            replyCount = replyCount.coerceAtLeast(0),
            uploadedAt = textualUploadDate,
            isPinned = isPinned,
            isHeartedByUploader = isHeartedByUploader,
            repliesToken = token,
        )
    }
}
