package com.example.petbreeds.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.feature.breeds.BreedsScreen
import com.example.feature.details.DetailsScreen
import com.example.feature.favorites.FavoritesScreen
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.profile.ProfileScreen
import com.example.preferences.PreferencesManager

@Composable
fun PetBreedsNavigation(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier,
) {
    val isFirstLaunch by preferencesManager.isFirstLaunchFlow.collectAsState(initial = null)
    val petType by preferencesManager.petTypeFlow.collectAsState(initial = null)

    val startDestination = remember(isFirstLaunch, petType) {
        if (isFirstLaunch == null) {
            Routes.Onboarding.route
        } else if (isFirstLaunch == true || petType == null) {
            Routes.Onboarding.route
        } else {
            Routes.Breeds.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onPetTypeSelected = {
                    navController.navigate(Routes.Breeds.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.Breeds.route) {
            BreedsScreen(
                onNavigateToDetails = { petId ->
                    navController.navigate(Routes.Details.createRoute(petId))
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
                },
            )
        }

        composable(Routes.Favorites.route) {
            FavoritesScreen(
                onNavigateToDetails = { petId ->
                    navController.navigate(Routes.Details.createRoute(petId))
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
                },
            )
        }

        composable(
            route = Routes.Details.route,
            arguments =
                listOf(
                    navArgument("petId") { type = NavType.StringType },
                ),
        ) {
            DetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
