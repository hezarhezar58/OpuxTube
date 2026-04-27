package dev.opux.tubeclient.feature.library.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.opux.tubeclient.core.domain.model.PlaylistEntry
import dev.opux.tubeclient.core.domain.usecase.ObservePlaylistEntriesUseCase
import dev.opux.tubeclient.core.domain.usecase.ObservePlaylistsUseCase
import dev.opux.tubeclient.core.domain.usecase.RemoveVideoFromPlaylistUseCase
import dev.opux.tubeclient.feature.library.navigation.PlaylistIdArg
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaylistDetailUiState(
    val playlistName: String = "",
    val entries: List<PlaylistEntry> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePlaylists: ObservePlaylistsUseCase,
    observeEntries: ObservePlaylistEntriesUseCase,
    private val removeVideo: RemoveVideoFromPlaylistUseCase,
) : ViewModel() {

    private val playlistId: Long = checkNotNull(savedStateHandle[PlaylistIdArg]) {
        "Missing $PlaylistIdArg nav argument"
    }

    val state: StateFlow<PlaylistDetailUiState> =
        combine(observePlaylists(), observeEntries(playlistId)) { playlists, entries ->
            val name = playlists.firstOrNull { it.id == playlistId }?.name ?: ""
            PlaylistDetailUiState(
                playlistName = name,
                entries = entries,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaylistDetailUiState(isLoading = true),
        )

    fun onRemove(videoId: String) {
        viewModelScope.launch { removeVideo(playlistId, videoId) }
    }
}
