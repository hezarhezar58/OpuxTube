package dev.opux.tubeclient.feature.library.presentation

import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry

data class LibraryUiState(
    val history: List<WatchHistoryEntry> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val isLoading: Boolean = true,
)
