package dev.opux.tubeclient.core.player

import dev.opux.tubeclient.core.domain.model.AudioStream
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamSelector @Inject constructor() {

    fun select(detail: VideoDetail, override: VideoStream? = null): Selection? {
        if (override != null) {
            // Honor user-picked quality. If the override is a progressive stream
            // (matches one in `videoStreams`), use it directly; otherwise treat it
            // as a video-only track and pair with the best audio.
            val isProgressive = detail.videoStreams.any { it.url == override.url }
            if (isProgressive) return Selection.Progressive(override)
            val audio = detail.audioStreams.maxByOrNull { it.bitrate }
            return when (audio) {
                null -> Selection.VideoOnly(override)
                else -> Selection.Merged(override, audio)
            }
        }
        // Prefer a progressive (combined audio+video) stream when available — single
        // HTTP source is simpler and avoids the MergingMediaSource A/V drift class of bugs.
        val progressive = detail.videoStreams
            .filter { it.height in MIN_HEIGHT..MAX_HEIGHT }
            .maxByOrNull { it.height }
            ?: detail.videoStreams.maxByOrNull { it.height }
        if (progressive != null) return Selection.Progressive(progressive)

        val videoOnly = detail.videoOnlyStreams
            .filter { it.height in MIN_HEIGHT..MAX_HEIGHT }
            .maxByOrNull { it.height.toLong() * 1_000_000L + it.bitrate.toLong() }
            ?: detail.videoOnlyStreams.maxByOrNull { it.height }

        val audio = detail.audioStreams.maxByOrNull { it.bitrate }

        return when {
            videoOnly != null && audio != null -> Selection.Merged(videoOnly, audio)
            videoOnly != null -> Selection.VideoOnly(videoOnly)
            audio != null -> Selection.AudioOnly(audio)
            else -> null
        }
    }

    sealed interface Selection {
        data class Progressive(val video: VideoStream) : Selection
        data class Merged(val video: VideoStream, val audio: AudioStream) : Selection
        data class VideoOnly(val video: VideoStream) : Selection
        data class AudioOnly(val audio: AudioStream) : Selection
    }

    private companion object {
        const val MIN_HEIGHT = 144
        const val MAX_HEIGHT = 720
    }
}
