package dev.opux.tubeclient.core.data.mapper

import android.text.Html
import dev.opux.tubeclient.core.domain.model.AudioStream
import dev.opux.tubeclient.core.domain.model.ChannelSummary
import dev.opux.tubeclient.core.domain.model.SubtitleTrack
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.domain.model.VideoStream
import dev.opux.tubeclient.core.domain.util.VideoIdExtractor
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.AudioStream as NpAudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.SubtitlesStream as NpSubtitlesStream
import org.schabi.newpipe.extractor.stream.VideoStream as NpVideoStream

fun List<Image>?.bestThumbnailUrl(): String? {
    if (this.isNullOrEmpty()) return null
    return maxByOrNull {
        val w = if (it.width > 0) it.width else 0
        val h = if (it.height > 0) it.height else 0
        w.toLong() * h.toLong()
    }?.url
}

fun StreamInfoItem.toPreview(): VideoPreview {
    val videoId = VideoIdExtractor.extractId(url) ?: url
    val safeDuration = duration.coerceAtLeast(0)
    return VideoPreview(
        id = videoId,
        url = url,
        title = name.orEmpty(),
        thumbnailUrl = thumbnails.bestThumbnailUrl(),
        durationSeconds = safeDuration,
        viewCount = viewCount.coerceAtLeast(0),
        uploadedAt = textualUploadDate,
        channelName = uploaderName.orEmpty(),
        channelUrl = uploaderUrl.orEmpty(),
        channelAvatarUrl = uploaderAvatars.bestThumbnailUrl(),
        // NewPipe occasionally mis-tags finalized videos as LIVE_STREAM in YouTube kiosk responses;
        // a positive duration means playback is finalized and cannot still be live.
        isLive = (streamType == StreamType.LIVE_STREAM || streamType == StreamType.AUDIO_LIVE_STREAM) &&
            safeDuration <= 0L,
        isShort = isShortFormContent,
    )
}

fun StreamInfo.toDetail(): VideoDetail {
    val videoId = VideoIdExtractor.extractId(url) ?: id ?: url
    val safeDuration = duration.coerceAtLeast(0)
    return VideoDetail(
        id = videoId,
        url = url,
        title = name.orEmpty(),
        description = description.toPlainText(),
        thumbnailUrl = thumbnails.bestThumbnailUrl(),
        durationSeconds = safeDuration,
        viewCount = viewCount.coerceAtLeast(0),
        likeCount = likeCount.coerceAtLeast(0),
        uploadedAt = textualUploadDate,
        category = category.takeUnless { it.isNullOrBlank() },
        channel = ChannelSummary(
            name = uploaderName.orEmpty(),
            url = uploaderUrl.orEmpty(),
            avatarUrl = uploaderAvatars.bestThumbnailUrl(),
            subscriberCount = uploaderSubscriberCount.coerceAtLeast(0),
            isVerified = isUploaderVerified,
        ),
        videoStreams = videoStreams.orEmpty().map { it.toDomain() },
        audioStreams = audioStreams.orEmpty().map { it.toDomain() },
        videoOnlyStreams = videoOnlyStreams.orEmpty().map { it.toDomain() },
        subtitles = subtitles.orEmpty().map { it.toDomain() },
        relatedVideos = relatedItems.orEmpty()
            .filterIsInstance<StreamInfoItem>()
            .map { it.toPreview() },
        isLive = (streamType == StreamType.LIVE_STREAM || streamType == StreamType.AUDIO_LIVE_STREAM) &&
            safeDuration <= 0L,
    )
}

private fun NpVideoStream.toDomain(): VideoStream {
    val res = getResolution()
    val height = res?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
    // NewPipe still exposes the public `isVideoOnly` field but flagged it deprecated in
    // 0.26+; the matching getter `isVideoOnly()` is the supported accessor. Force the
    // method form so we don't lean on the deprecated field path.
    @Suppress("DEPRECATION")
    val videoOnly = isVideoOnly
    return VideoStream(
        url = content,
        mimeType = format?.mimeType,
        resolution = res ?: "",
        width = width,
        height = height,
        bitrate = bitrate,
        framerate = fps,
        codec = codec.takeUnless { it.isNullOrBlank() },
        isAdaptive = !videoOnly,
    )
}

private fun Description?.toPlainText(): String {
    if (this == null) return ""
    val raw = content.orEmpty()
    return when (type) {
        Description.HTML -> Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        else -> raw
    }
}

private fun NpSubtitlesStream.toDomain(): SubtitleTrack {
    val mime = format?.mimeType ?: when (extension?.lowercase()) {
        "vtt" -> "text/vtt"
        "ttml" -> "application/ttml+xml"
        "srv3", "srt" -> "application/x-subrip"
        else -> "text/vtt"
    }
    return SubtitleTrack(
        url = content,
        languageTag = languageTag.orEmpty(),
        displayName = displayLanguageName.orEmpty().ifEmpty { languageTag.orEmpty() },
        mimeType = mime,
        isAutoGenerated = isAutoGenerated,
    )
}

private fun NpAudioStream.toDomain(): AudioStream = AudioStream(
    url = content,
    mimeType = format?.mimeType,
    bitrate = bitrate,
    averageBitrate = averageBitrate,
    codec = codec.takeUnless { it.isNullOrBlank() },
    language = audioLocale?.toLanguageTag(),
)
