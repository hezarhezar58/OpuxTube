package dev.opux.tubeclient.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import dev.opux.tubeclient.feature.home.navigation.HomeRoute
import dev.opux.tubeclient.feature.library.navigation.LibraryRoute
import dev.opux.tubeclient.feature.search.navigation.SearchRoute

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(HomeRoute, "Ana Sayfa", Icons.Filled.Home, Icons.Outlined.Home),
    Search(SearchRoute, "Ara", Icons.Filled.Search, Icons.Outlined.Search),
    Library(LibraryRoute, "Kitaplık", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    ;

    companion object {
        val routes: Set<String> = entries.map { it.route }.toSet()
    }
}
