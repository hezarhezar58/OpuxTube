package dev.opux.tubeclient.feature.player.presentation

import android.content.Context
import dev.opux.tubeclient.core.domain.model.Comment
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import dev.opux.tubeclient.feature.player.R

data class PlayerUiState(
    val isLoading: Boolean = true,
    val detail: VideoDetail? = null,
    val error: String? = null,
    val qualityOverride: VideoStream? = null,
    val playbackSpeed: Float = 1.0f,
    /** When non-null, the wall-clock instant (epoch ms) at which playback should pause. */
    val sleepTimerEndAtMs: Long? = null,
)

data class CommentsUiState(
    val isLoading: Boolean = false,
    val items: List<Comment> = emptyList(),
    val disabled: Boolean = false,
    val error: String? = null,
)

sealed interface RepliesState {
    data object Loading : RepliesState
    data class Loaded(val items: List<Comment>) : RepliesState
    data class Failed(val message: String) : RepliesState
}

data class QualityOption(
    val label: String,
    val stream: VideoStream?,
)

data class SkippedSegmentEvent(
    val category: String,
    val durationMs: Long,
)

internal fun resolveSegmentLabel(context: Context, category: String): String = when (category) {
    "sponsor" -> context.getString(R.string.player_segment_sponsor)
    "intro" -> context.getString(R.string.player_segment_intro)
    "outro" -> context.getString(R.string.player_segment_outro)
    "selfpromo" -> context.getString(R.string.player_segment_selfpromo)
    "interaction" -> context.getString(R.string.player_segment_interaction)
    "music_offtopic" -> context.getString(R.string.player_segment_music_offtopic)
    "preview" -> context.getString(R.string.player_segment_preview)
    else -> category
}
