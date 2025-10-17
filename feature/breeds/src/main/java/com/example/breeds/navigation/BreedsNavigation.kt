package com.example.breeds.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.breeds.BreedsScreen

const val BREEDS_ROUTE = "breeds"

fun NavGraphBuilder.breedsScreen(
    onNavigateToDetails: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    composable(route = BREEDS_ROUTE) {
        BreedsScreen(
            onNavigateToDetails = onNavigateToDetails
        )
    }
}