package com.example.petbreeds.presentation.navigation

sealed class Routes(
    val route: String,
) {
    object Onboarding : Routes("onboarding")

    object Breeds : Routes("breeds")

    object Favorites : Routes("favorites")

    object Settings : Routes("settings")

    object Details : Routes("details/{petId}") {
        fun createRoute(petId: String) = "details/$petId"
    }
    object Profile : Routes("profile")
}
