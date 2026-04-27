package dev.opux.tubeclient.feature.player.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.opux.tubeclient.core.domain.model.DownloadStatus
import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.SkipSegment
import dev.opux.tubeclient.core.domain.model.VideoDetail
import dev.opux.tubeclient.core.domain.model.VideoStream
import dev.opux.tubeclient.core.domain.repository.DownloadActions
import dev.opux.tubeclient.core.domain.usecase.AddVideoToPlaylistUseCase
import dev.opux.tubeclient.core.domain.usecase.CreatePlaylistUseCase
import dev.opux.tubeclient.core.domain.usecase.FindDownloadUseCase
import dev.opux.tubeclient.core.domain.usecase.GetLastPositionUseCase
import dev.opux.tubeclient.core.domain.usecase.GetSkipSegmentsUseCase
import dev.opux.tubeclient.core.domain.usecase.GetVideoDetailsUseCase
import dev.opux.tubeclient.core.domain.usecase.ObservePlaylistsUseCase
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private val getSkipSegments: GetSkipSegmentsUseCase,
    observePlaylists: ObservePlaylistsUseCase,
    private val addVideoToPlaylist: AddVideoToPlaylistUseCase,
    private val createPlaylist: CreatePlaylistUseCase,
    private val findDownload: FindDownloadUseCase,
    private val downloadActions: DownloadActions,
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

    private val _skipEvents = MutableSharedFlow<SkippedSegmentEvent>(
        replay = 0,
        extraBufferCapacity = 4,
    )
    val skipEvents: SharedFlow<SkippedSegmentEvent> = _skipEvents.asSharedFlow()

    private val _playlistAddedEvents = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 4,
    )
    val playlistAddedEvents: SharedFlow<String> = _playlistAddedEvents.asSharedFlow()

    val playlists: StateFlow<List<Playlist>> = observePlaylists().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val downloadStatus: StateFlow<DownloadStatus?> =
        combine(_uiState, downloadActions.statuses) { ui, statuses ->
            ui.detail?.id?.let { statuses[it] }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private var progressJob: Job? = null
    private var skipperJob: Job? = null

    private var segments: List<SkipSegment> = emptyList()
    private val recentlySkippedAt: MutableMap<String, Long> = mutableMapOf()

    init { loadAndPlay() }

    private fun loadAndPlay() {
        progressJob?.cancel()
        skipperJob?.cancel()
        segments = emptyList()
        recentlySkippedAt.clear()
        _uiState.value = PlayerUiState(isLoading = true)
        viewModelScope.launch {
            getDetails(videoUrl)
                .onSuccess { detail ->
                    _uiState.value = PlayerUiState(isLoading = false, detail = detail)
                    val resumeAt = resolveResumePosition(detail.id, detail.durationSeconds)
                    val localOverride = findDownload(detail.id)?.let { downloaded ->
                        VideoStream(
                            url = "file://${downloaded.filePath}",
                            mimeType = downloaded.mimeType,
                            resolution = "",
                            width = 0,
                            height = 0,
                            bitrate = 0,
                            framerate = 0,
                            codec = null,
                            isAdaptive = false,
                        )
                    }
                    controller.play(
                        detail = detail,
                        startPositionMs = resumeAt,
                        qualityOverride = localOverride,
                    )
                    recordWatch(detail, progressMs = resumeAt)
                    startProgressTicker(detail.id)
                    fetchAndApplySkipSegments(detail.id)
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

    private fun fetchAndApplySkipSegments(videoId: String) {
        viewModelScope.launch {
            getSkipSegments(videoId)
                .onSuccess { fetched ->
                    if (fetched.isEmpty()) return@onSuccess
                    segments = fetched.sortedBy { it.startMs }
                    startSkipperLoop()
                }
                .onFailure { Timber.w(it, "SB fetch failed for $videoId") }
        }
    }

    private fun startSkipperLoop() {
        skipperJob?.cancel()
        if (segments.isEmpty()) return
        skipperJob = viewModelScope.launch {
            while (isActive) {
                delay(SKIP_POLL_INTERVAL_MS)
                val player = controller.playerFlow.value ?: continue
                if (!player.isPlaying) continue
                val pos = player.currentPosition
                val now = System.currentTimeMillis()
                val seg = segments.firstOrNull { candidate ->
                    pos in candidate.startMs until candidate.endMs &&
                        (recentlySkippedAt[candidate.uuid]?.let { ts -> now - ts > SKIP_COOLDOWN_MS } ?: true)
                } ?: continue
                controller.seekTo(seg.endMs)
                recentlySkippedAt[seg.uuid] = now
                _skipEvents.tryEmit(SkippedSegmentEvent(seg.category, seg.durationMs))
            }
        }
    }

    fun retry() = loadAndPlay()

    fun onDownload() {
        val detail = _uiState.value.detail ?: return
        val stream = pickDownloadStream(detail) ?: run {
            Timber.w("No progressive stream available for download")
            return
        }
        downloadActions.enqueue(detail, stream)
    }

    private fun pickDownloadStream(detail: VideoDetail): VideoStream? {
        // Prefer a progressive stream (audio+video in one file) at 720p or below — these
        // download to a single playable mp4. Fall back to the highest progressive available.
        return detail.videoStreams
            .filter { it.height in 1..720 }
            .maxByOrNull { it.height }
            ?: detail.videoStreams.maxByOrNull { it.height }
    }

    fun onSelectQuality(stream: VideoStream?) {
        val detail = _uiState.value.detail ?: return
        val current = controller.playerFlow.value?.currentPosition ?: 0L
        _uiState.value = _uiState.value.copy(qualityOverride = stream)
        controller.play(detail, startPositionMs = current, qualityOverride = stream)
    }

    fun addCurrentVideoToPlaylist(playlistId: Long) {
        val detail = _uiState.value.detail ?: return
        val playlistName = playlists.value.firstOrNull { it.id == playlistId }?.name ?: ""
        viewModelScope.launch {
            runCatching { addVideoToPlaylist(playlistId, detail) }
                .onSuccess { _playlistAddedEvents.tryEmit(playlistName) }
                .onFailure { Timber.w(it, "Add to playlist failed") }
        }
    }

    fun createPlaylistAndAddCurrent(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val detail = _uiState.value.detail ?: return
        viewModelScope.launch {
            runCatching {
                val id = createPlaylist(trimmed)
                addVideoToPlaylist(id, detail)
            }
                .onSuccess { _playlistAddedEvents.tryEmit(trimmed) }
                .onFailure { Timber.w(it, "Create+add playlist failed") }
        }
    }

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
        skipperJob?.cancel()
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
        const val SKIP_POLL_INTERVAL_MS = 250L
        const val SKIP_COOLDOWN_MS = 2_000L
    }
}
