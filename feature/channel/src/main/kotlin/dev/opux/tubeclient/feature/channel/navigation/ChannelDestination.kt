package dev.opux.tubeclient.feature.channel.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.feature.channel.presentation.ChannelScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

const val ChannelUrlArg = "channelUrl"
const val ChannelRoute = "channel/{$ChannelUrlArg}"

fun NavGraphBuilder.channelDestination(
    onBack: () -> Unit,
    onVideoClick: (VideoPreview) -> Unit,
) {
    composable(
        route = ChannelRoute,
        arguments = listOf(
            navArgument(ChannelUrlArg) { type = NavType.StringType },
        ),
    ) {
        ChannelScreen(onBack = onBack, onVideoClick = onVideoClick)
    }
}

fun NavController.navigateToChannel(channelUrl: String) {
    val encoded = URLEncoder.encode(channelUrl, StandardCharsets.UTF_8.name())
    navigate("channel/$encoded")
}
