package com.leyna.nailmanagement.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    // Route and label constants
    private object Routes {
        const val GEL = "gel"
        const val NAIL = "nail"
        const val SETTINGS = "settings"
    }

    private object Labels {
        const val GEL = "Gel"
        const val NAIL = "Nail"
        const val SETTINGS = "Settings"
    }

    data object Gel : BottomNavItem(
        route = Routes.GEL,
        label = Labels.GEL,
        icon = Icons.Default.Star
    )

    data object Nail : BottomNavItem(
        route = Routes.NAIL,
        label = Labels.NAIL,
        icon = Icons.Default.Face
    )

    data object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        label = Labels.SETTINGS,
        icon = Icons.Default.Settings
    )
}