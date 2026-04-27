package dev.opux.tubeclient.core.domain.model

data class Comment(
    val id: String,
    val text: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val likeCount: Int,
    val replyCount: Int,
    val uploadedAt: String?,
    val isPinned: Boolean,
    val isHeartedByUploader: Boolean,
)
