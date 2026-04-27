package dev.opux.tubeclient.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import dev.opux.tubeclient.R
import dev.opux.tubeclient.feature.home.navigation.HomeRoute
import dev.opux.tubeclient.feature.library.navigation.LibraryRoute
import dev.opux.tubeclient.feature.search.navigation.SearchRoute

enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(HomeRoute, R.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home),
    Search(SearchRoute, R.string.tab_search, Icons.Filled.Search, Icons.Outlined.Search),
    Library(LibraryRoute, R.string.tab_library, Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
    ;

    companion object {
        val routes: Set<String> = entries.map { it.route }.toSet()
    }
}
