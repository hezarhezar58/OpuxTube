package dev.opux.tubeclient.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.opux.tubeclient.core.domain.usecase.GetLastPositionUseCase
import dev.opux.tubeclient.core.domain.usecase.GetVideoDetailsUseCase
import dev.opux.tubeclient.core.domain.usecase.RecordWatchEventUseCase
import dev.opux.tubeclient.core.domain.usecase.UpdateWatchProgressUseCase
import dev.opux.tubeclient.core.player.MediaPlayerController
import dev.opux.tubeclient.core.player.model.PlaybackState
import dev.opux.tubeclient.feature.player.navigation.PlayerVideoUrlArg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDetails: GetVideoDetailsUseCase,
    private val controller: MediaPlayerController,
    private val recordWatch: RecordWatchEventUseCase,
    private val updateProgress: UpdateWatchProgressUseCase,
    private val getLastPosition: GetLastPositionUseCase,
) : ViewModel() {

    private val videoUrl: String = run {
        val raw: String = checkNotNull(savedStateHandle[PlayerVideoUrlArg]) {
            "Missing $PlayerVideoUrlArg nav argument"
        }
        URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
    }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = controller.state
    val playerFlow: StateFlow<Player?> = controller.playerFlow

    private var progressJob: Job? = null

    init { loadAndPlay() }

    private fun loadAndPlay() {
        progressJob?.cancel()
        _uiState.value = PlayerUiState(isLoading = true)
        viewModelScope.launch {
            getDetails(videoUrl)
                .onSuccess { detail ->
                    _uiState.value = PlayerUiState(isLoading = false, detail = detail)
                    val resumeAt = resolveResumePosition(detail.id, detail.durationSeconds)
                    controller.play(detail, startPositionMs = resumeAt)
                    recordWatch(detail, progressMs = resumeAt)
                    startProgressTicker(detail.id)
                }
                .onFailure { t ->
                    _uiState.value = PlayerUiState(
                        isLoading = false,
                        error = t.message ?: "Video yüklenemedi",
                    )
                }
        }
    }

    private suspend fun resolveResumePosition(videoId: String, durationSeconds: Long): Long {
        val saved = getLastPosition(videoId)
        val durationMs = durationSeconds * 1000L
        val tail = (durationMs - END_OF_VIDEO_THRESHOLD_MS).coerceAtLeast(0L)
        return when {
            saved < MIN_PROGRESS_MS_TO_PERSIST -> 0L
            durationMs > 0L && saved >= tail -> 0L
            else -> saved
        }
    }

    private fun startProgressTicker(videoId: String) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(PROGRESS_FLUSH_INTERVAL_MS)
                val pos = controller.playerFlow.value?.currentPosition ?: 0L
                if (pos >= MIN_PROGRESS_MS_TO_PERSIST) {
                    updateProgress(videoId, pos)
                }
            }
        }
    }

    fun retry() = loadAndPlay()

    fun togglePlayPause() {
        if (controller.state.value.isPlaying) controller.pause() else controller.resume()
    }

    override fun onCleared() {
        super.onCleared()
        // We deliberately do NOT call controller.stop() — the playback service owns the player
        // and keeps audio running so the user can navigate back / let the screen die without
        // killing the music. The user explicitly stops via the system notification or by
        // swiping the app from recents.
        val pos = controller.playerFlow.value?.currentPosition ?: 0L
        val detail = _uiState.value.detail
        progressJob?.cancel()
        if (detail != null && pos >= MIN_PROGRESS_MS_TO_PERSIST) {
            val flushScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            flushScope.launch(NonCancellable) {
                runCatching { updateProgress(detail.id, pos) }
                flushScope.cancel()
            }
        }
    }

    private companion object {
        const val PROGRESS_FLUSH_INTERVAL_MS = 10_000L
        const val MIN_PROGRESS_MS_TO_PERSIST = 10_000L
        const val END_OF_VIDEO_THRESHOLD_MS = 10_000L
    }
}
