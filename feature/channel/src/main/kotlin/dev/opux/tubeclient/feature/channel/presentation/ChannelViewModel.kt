package dev.opux.tubeclient.feature.channel.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.opux.tubeclient.core.domain.usecase.GetChannelDetailsUseCase
import dev.opux.tubeclient.core.domain.usecase.GetChannelVideosUseCase
import dev.opux.tubeclient.core.domain.usecase.IsSubscribedUseCase
import dev.opux.tubeclient.core.domain.usecase.SubscribeChannelUseCase
import dev.opux.tubeclient.core.domain.usecase.UnsubscribeChannelUseCase
import dev.opux.tubeclient.feature.channel.navigation.ChannelUrlArg
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getDetails: GetChannelDetailsUseCase,
    private val getVideos: GetChannelVideosUseCase,
    private val isSubscribed: IsSubscribedUseCase,
    private val subscribeChannel: SubscribeChannelUseCase,
    private val unsubscribeChannel: UnsubscribeChannelUseCase,
) : ViewModel() {

    private val channelUrl: String = run {
        val raw: String = checkNotNull(savedStateHandle[ChannelUrlArg]) {
            "Missing $ChannelUrlArg nav argument"
        }
        URLDecoder.decode(raw, StandardCharsets.UTF_8.name())
    }

    private val _state = MutableStateFlow(ChannelUiState())
    val state: StateFlow<ChannelUiState> = _state.asStateFlow()

    private var loadMoreJob: Job? = null

    init {
        load()
        observeSubscriptionStatus()
    }

    private fun observeSubscriptionStatus() {
        viewModelScope.launch {
            isSubscribed(channelUrl).collect { sub ->
                _state.update { it.copy(isSubscribed = sub) }
            }
        }
    }

    private fun load() {
        _state.value = ChannelUiState(isLoading = true)
        viewModelScope.launch {
            val detailDeferred = async { getDetails(channelUrl) }
            val videosDeferred = async { getVideos(channelUrl, pageToken = null) }
            val detailResult = detailDeferred.await()
            val videosResult = videosDeferred.await()

            val detail = detailResult.getOrNull()
            val page = videosResult.getOrNull()
            val firstError = detailResult.exceptionOrNull() ?: videosResult.exceptionOrNull()

            _state.update {
                it.copy(
                    isLoading = false,
                    detail = detail,
                    videos = page?.items ?: emptyList(),
                    nextPageToken = page?.nextPageToken,
                    error = if (detail == null) firstError?.message else null,
                )
            }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (!current.canLoadMore) return
        val token = current.nextPageToken ?: return
        loadMoreJob?.cancel()
        _state.update { it.copy(isAppending = true) }
        loadMoreJob = viewModelScope.launch {
            getVideos(channelUrl, pageToken = token)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            videos = it.videos + page.items,
                            isAppending = false,
                            nextPageToken = page.nextPageToken,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isAppending = false,
                            error = t.message ?: "Daha fazla yüklenemedi",
                        )
                    }
                }
        }
    }

    fun toggleSubscription() {
        val detail = _state.value.detail ?: return
        viewModelScope.launch {
            if (_state.value.isSubscribed) {
                unsubscribeChannel(detail.url)
            } else {
                subscribeChannel(detail)
            }
        }
    }

    fun retry() = load()
}
