package dev.opux.tubeclient.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.feature.home.presentation.HomeScreen

const val HomeRoute = "home"

fun NavGraphBuilder.homeDestination(
    onVideoClick: (VideoPreview) -> Unit,
) {
    composable(route = HomeRoute) {
        HomeScreen(onVideoClick = onVideoClick)
    }
}
