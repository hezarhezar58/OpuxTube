package dev.opux.tubeclient.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.feature.search.presentation.SearchScreen

const val SearchRoute = "search"

fun NavGraphBuilder.searchDestination(
    onVideoClick: (VideoPreview) -> Unit,
) {
    composable(route = SearchRoute) {
        SearchScreen(onVideoClick = onVideoClick)
    }
}
