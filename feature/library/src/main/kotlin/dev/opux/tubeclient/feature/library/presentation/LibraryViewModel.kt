package dev.opux.tubeclient.feature.library.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.opux.tubeclient.core.domain.model.SponsorBlockCategory
import dev.opux.tubeclient.core.domain.preferences.SponsorBlockPreferences
import dev.opux.tubeclient.core.domain.usecase.ClearWatchHistoryUseCase
import dev.opux.tubeclient.core.domain.usecase.GetSubscriptionsUseCase
import dev.opux.tubeclient.core.domain.usecase.GetWatchHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    getHistory: GetWatchHistoryUseCase,
    getSubscriptions: GetSubscriptionsUseCase,
    private val clearAll: ClearWatchHistoryUseCase,
    private val sponsorBlockPreferences: SponsorBlockPreferences,
) : ViewModel() {

    val state: StateFlow<LibraryUiState> =
        combine(
            getHistory(),
            getSubscriptions(),
            sponsorBlockPreferences.enabledCategories,
        ) { history, subs, enabled ->
            LibraryUiState(
                history = history,
                subscriptions = subs,
                sponsorBlockEnabled = enabled,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState(isLoading = true),
        )

    fun onClearHistory() {
        viewModelScope.launch { clearAll() }
    }

    fun onToggleCategory(category: SponsorBlockCategory, enabled: Boolean) {
        viewModelScope.launch {
            sponsorBlockPreferences.setCategoryEnabled(category, enabled)
        }
    }
}
