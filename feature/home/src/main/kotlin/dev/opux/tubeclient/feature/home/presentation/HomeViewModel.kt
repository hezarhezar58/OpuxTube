package dev.opux.tubeclient.feature.home.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.opux.tubeclient.core.domain.usecase.GetTrendingUseCase
import dev.opux.tubeclient.feature.home.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val getTrending: GetTrendingUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(isLoading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init { loadInitial() }

    fun loadInitial() {
        loadJob?.cancel()
        _state.value = HomeUiState(isLoading = true)
        loadJob = viewModelScope.launch {
            getTrending(pageToken = null)
                .onSuccess { page ->
                    _state.value = HomeUiState(
                        items = page.items,
                        isLoading = false,
                        nextPageToken = page.nextPageToken,
                    )
                }
                .onFailure { t ->
                    _state.value = HomeUiState(
                        isLoading = false,
                        error = t.message ?: appContext.getString(R.string.home_load_failed),
                    )
                }
        }
    }

    fun loadMore() {
        val current = _state.value
        if (!current.canLoadMore) return
        val token = current.nextPageToken ?: return
        loadJob?.cancel()
        _state.update { it.copy(isAppending = true) }
        loadJob = viewModelScope.launch {
            getTrending(pageToken = token)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            items = it.items + page.items,
                            isAppending = false,
                            nextPageToken = page.nextPageToken,
                        )
                    }
                }
                .onFailure { t ->
                    _state.update {
                        it.copy(
                            isAppending = false,
                            error = t.message ?: appContext.getString(R.string.home_load_more_failed),
                        )
                    }
                }
        }
    }

    fun retry() = loadInitial()

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
