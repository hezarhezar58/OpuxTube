package dev.opux.tubeclient.feature.library.presentation

import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry

data class LibraryUiState(
    val history: List<WatchHistoryEntry> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val sponsorBlockEnabled: Set<SponsorBlockCategory> = SponsorBlockCategory.entries.toSet(),
    val isLoading: Boolean = true,
)
