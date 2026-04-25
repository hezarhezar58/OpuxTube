package dev.opux.tubeclient.feature.player.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.feature.player.presentation.PlayerScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

const val PlayerVideoUrlArg = "videoUrl"
const val PlayerRoute = "player/{$PlayerVideoUrlArg}"

fun NavGraphBuilder.playerDestination(
    onBack: () -> Unit,
    onRelatedClick: (VideoPreview) -> Unit,
    onChannelClick: (String) -> Unit,
) {
    composable(
        route = PlayerRoute,
        arguments = listOf(
            navArgument(PlayerVideoUrlArg) { type = NavType.StringType },
        ),
    ) {
        PlayerScreen(
            onBack = onBack,
            onRelatedClick = onRelatedClick,
            onChannelClick = onChannelClick,
        )
    }
}

fun NavController.navigateToPlayer(videoUrl: String) {
    val encoded = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.name())
    navigate("player/$encoded")
}
