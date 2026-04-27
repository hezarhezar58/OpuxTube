package dev.opux.tubeclient.feature.library.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.opux.tubeclient.core.domain.model.Playlist
import dev.opux.tubeclient.core.domain.model.PlaylistEntry
import dev.opux.tubeclient.core.domain.model.Subscription
import dev.opux.tubeclient.core.domain.model.WatchHistoryEntry
import dev.opux.tubeclient.feature.library.presentation.LibraryScreen
import dev.opux.tubeclient.feature.library.presentation.PlaylistDetailScreen

const val LibraryRoute = "library"
const val PlaylistDetailRoute = "playlist/{playlistId}"
const val PlaylistIdArg = "playlistId"

fun NavController.navigateToPlaylist(playlistId: Long) {
    navigate("playlist/$playlistId")
}

fun NavGraphBuilder.libraryDestination(
    onHistoryClick: (WatchHistoryEntry) -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
) {
    composable(route = LibraryRoute) {
        LibraryScreen(
            onHistoryClick = onHistoryClick,
            onSubscriptionClick = onSubscriptionClick,
            onPlaylistClick = onPlaylistClick,
        )
    }
}

fun NavGraphBuilder.playlistDetailDestination(
    onBack: () -> Unit,
    onEntryClick: (PlaylistEntry) -> Unit,
) {
    composable(
        route = PlaylistDetailRoute,
        arguments = listOf(navArgument(PlaylistIdArg) { type = NavType.LongType }),
    ) {
        PlaylistDetailScreen(onBack = onBack, onEntryClick = onEntryClick)
    }
}
