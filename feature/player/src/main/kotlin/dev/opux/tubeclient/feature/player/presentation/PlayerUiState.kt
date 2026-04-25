package dev.opux.tubeclient.feature.player.presentation

import dev.opux.tubeclient.core.domain.model.VideoDetail

data class PlayerUiState(
    val isLoading: Boolean = true,
    val detail: VideoDetail? = null,
    val error: String? = null,
)
