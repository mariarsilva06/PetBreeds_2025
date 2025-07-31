package com.example.petbreeds.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.petbreeds.presentation.breeds.BreedsScreen
import com.example.petbreeds.presentation.details.DetailsScreen
import com.example.petbreeds.presentation.favorites.FavoritesScreen
import com.example.petbreeds.presentation.onboarding.OnboardingScreen
import com.example.petbreeds.presentation.settings.SettingsScreen
import com.example.petbreeds.utils.PreferencesManager

@Composable
fun PetBreedsNavigation(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val petType by preferencesManager.petTypeFlow.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = if (petType != null) Routes.Breeds.route else Routes.Onboarding.route,
        modifier = modifier
    ) {
        composable(Routes.Onboarding.route) {
            OnboardingScreen(
                onPetTypeSelected = {
                    navController.navigate(Routes.Breeds.route) {
                        popUpTo(Routes.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Breeds.route) {
            BreedsScreen(
                onNavigateToDetails = { petId ->
                    navController.navigate(Routes.Details.createRoute(petId))
                }
            )
        }

        composable(Routes.Favorites.route) {
            FavoritesScreen(
                onNavigateToDetails = { petId ->
                    navController.navigate(Routes.Details.createRoute(petId))
                }
            )
        }

        composable(Routes.Settings.route) {
            SettingsScreen()
        }

        composable(
            route = Routes.Details.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) {
            DetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}