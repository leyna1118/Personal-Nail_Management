package com.leyna.nailmanagement.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Gel : BottomNavItem(
        route = "gel",
        label = "Gel",
        icon = Icons.Default.Star
    )

    data object Nail : BottomNavItem(
        route = "nail",
        label = "Nail",
        icon = Icons.Default.Face
    )
}