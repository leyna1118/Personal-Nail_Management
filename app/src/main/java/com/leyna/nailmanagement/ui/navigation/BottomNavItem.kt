package com.leyna.nailmanagement.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.leyna.nailmanagement.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector
) {
    // Route and label constants
    private object Routes {
        const val GEL = "gel"
        const val NAIL = "nail"
        const val SETTINGS = "settings"
    }

    data object Gel : BottomNavItem(
        route = Routes.GEL,
        labelResId = R.string.nav_gel,
        icon = Icons.Default.Star
    )

    data object Nail : BottomNavItem(
        route = Routes.NAIL,
        labelResId = R.string.nav_nail,
        icon = Icons.Default.Face
    )

    data object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        labelResId = R.string.nav_settings,
        icon = Icons.Default.Settings
    )
}
