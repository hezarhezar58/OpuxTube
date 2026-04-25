package dev.opux.tubeclient.feature.library.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import dev.opux.tubeclient.feature.library.presentation.LibraryScreen

const val LibraryRoute = "library"

fun NavGraphBuilder.libraryDestination(
    onHistoryClick: (WatchHistoryEntry) -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
) {
    composable(route = LibraryRoute) {
        LibraryScreen(
            onHistoryClick = onHistoryClick,
            onSubscriptionClick = onSubscriptionClick,
        )
    }
}
