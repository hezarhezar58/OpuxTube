package dev.opux.tubeclient.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.opux.tubeclient.feature.channel.navigation.channelDestination
import dev.opux.tubeclient.feature.channel.navigation.navigateToChannel
import dev.opux.tubeclient.feature.home.navigation.HomeRoute
import dev.opux.tubeclient.feature.home.navigation.homeDestination
import dev.opux.tubeclient.feature.library.navigation.libraryDestination
import dev.opux.tubeclient.feature.player.navigation.navigateToPlayer
import dev.opux.tubeclient.feature.player.navigation.playerDestination
import dev.opux.tubeclient.feature.search.navigation.searchDestination

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OpuxNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in TopLevelDestination.routes

    Scaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        bottomBar = {
            if (showBottomBar) {
                OpuxBottomBar(navController = navController, currentRoute = currentRoute)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            homeDestination(onVideoClick = { navController.navigateToPlayer(it.url) })
            searchDestination(onVideoClick = { navController.navigateToPlayer(it.url) })
            libraryDestination(
                onHistoryClick = { entry -> navController.navigateToPlayer(entry.videoUrl) },
                onSubscriptionClick = { sub -> navController.navigateToChannel(sub.channelUrl) },
            )
            channelDestination(
                onBack = { navController.popBackStack() },
                onVideoClick = { navController.navigateToPlayer(it.url) },
            )
            playerDestination(
                onBack = { navController.popBackStack() },
                onRelatedClick = { navController.navigateToPlayer(it.url) },
                onChannelClick = { url -> navController.navigateToChannel(url) },
            )
        }
    }
}

@Composable
private fun OpuxBottomBar(navController: NavController, currentRoute: String?) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        TopLevelDestination.entries.forEach { dest ->
            val selected = dest.route == currentRoute
            NavigationBarItem(
                modifier = Modifier.testTag("nav_${dest.name.lowercase()}"),
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(dest.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.label,
                    )
                },
                label = { Text(text = dest.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            )
        }
    }
}
