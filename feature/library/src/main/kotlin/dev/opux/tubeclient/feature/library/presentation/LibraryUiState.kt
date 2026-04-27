package dev.opux.tubeclient.feature.library.presentation

import dev.opux.tubeclient.core.domain.model.DownloadStatus
import dev.opux.tubeclient.core.domain.model.DownloadedVideo
import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry

data class LibraryUiState(
    val history: List<WatchHistoryEntry> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val downloads: List<DownloadedVideo> = emptyList(),
    val downloadStatuses: Map<String, DownloadStatus> = emptyMap(),
    val sponsorBlockEnabled: Set<SponsorBlockCategory> = SponsorBlockCategory.entries.toSet(),
    val isLoading: Boolean = true,
)
