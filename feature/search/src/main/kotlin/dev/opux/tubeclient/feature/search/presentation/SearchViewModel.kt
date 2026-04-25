package dev.opux.tubeclient.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.opux.tubeclient.core.domain.model.SearchFilter
import dev.opux.tubeclient.core.domain.usecase.SearchVideosUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchVideos: SearchVideosUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var loadMoreJob: Job? = null

    init {
        viewModelScope.launch {
            _query
                .debounce(DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { raw ->
                    val trimmed = raw.trim()
                    if (trimmed.isBlank()) {
                        _state.value = SearchUiState(query = raw)
                    } else {
                        performSearch(trimmed)
                    }
                }
        }
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun clearQuery() {
        _query.value = ""
    }

    private suspend fun performSearch(q: String) {
        _state.value = SearchUiState(query = q, isLoading = true, hasSearched = true)
        searchVideos(query = q, filter = SearchFilter.VIDEOS, pageToken = null)
            .onSuccess { page ->
                _state.value = SearchUiState(
                    query = q,
                    items = page.items,
                    isLoading = false,
                    nextPageToken = page.nextPageToken,
                    hasSearched = true,
                )
            }
            .onFailure { t ->
                _state.value = SearchUiState(
                    query = q,
                    isLoading = false,
                    error = t.message ?: "Arama başarısız",
                    hasSearched = true,
                )
            }
    }

    fun loadMore() {
        val current = _state.value
        if (!current.canLoadMore) return
        val token = current.nextPageToken ?: return
        loadMoreJob?.cancel()
        _state.update { it.copy(isAppending = true) }
        loadMoreJob = viewModelScope.launch {
            searchVideos(query = current.query, filter = SearchFilter.VIDEOS, pageToken = token)
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
                            error = t.message ?: "Daha fazla yüklenemedi",
                        )
                    }
                }
        }
    }

    fun retry() {
        val q = _query.value.trim()
        if (q.isNotBlank()) {
            viewModelScope.launch { performSearch(q) }
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 400L
    }
}
