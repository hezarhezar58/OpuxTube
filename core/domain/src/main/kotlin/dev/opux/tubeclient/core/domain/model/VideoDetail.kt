package dev.opux.tubeclient.core.domain.model

data class VideoDetail(
    val id: String,
    val url: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val viewCount: Long,
    val likeCount: Long,
    val uploadedAt: String?,
    val category: String?,
    val channel: ChannelSummary,
    val videoStreams: List<VideoStream>,
    val audioStreams: List<AudioStream>,
    val videoOnlyStreams: List<VideoStream>,
    val relatedVideos: List<VideoPreview>,
    val isLive: Boolean,
)

data class ChannelSummary(
    val name: String,
    val url: String,
    val avatarUrl: String?,
    val subscriberCount: Long,
    val isVerified: Boolean,
)

data class VideoStream(
    val url: String,
    val mimeType: String?,
    val resolution: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val framerate: Int,
    val codec: String?,
    val isAdaptive: Boolean,
)

data class AudioStream(
    val url: String,
    val mimeType: String?,
    val bitrate: Int,
    val averageBitrate: Int,
    val codec: String?,
    val language: String?,
)
