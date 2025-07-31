package com.example.petbreeds.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector


sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Breeds : BottomNavItem(
        route = Routes.Breeds.route,
        icon = Icons.Default.Pets,
        label = "Breeds"
    )

    object Favorites : BottomNavItem(
        route = Routes.Favorites.route,
        icon = Icons.Default.Favorite,
        label = "Favorites"
    )

    object Settings : BottomNavItem(
        route = Routes.Settings.route,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
}