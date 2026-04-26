package dev.opux.tubeclient.feature.player.presentation

import dev.opux.tubeclient.core.domain.model.VideoDetail

data class PlayerUiState(
    val isLoading: Boolean = true,
    val detail: VideoDetail? = null,
    val error: String? = null,
)

data class SkippedSegmentEvent(
    val category: String,
    val durationMs: Long,
)

internal fun String.toTurkishLabel(): String = when (this) {
    "sponsor" -> "Sponsor"
    "intro" -> "İntro"
    "outro" -> "Bitiş"
    "selfpromo" -> "Kanal reklamı"
    "interaction" -> "Etkileşim"
    "music_offtopic" -> "Konu dışı müzik"
    "preview" -> "Önizleme"
    else -> this
}
