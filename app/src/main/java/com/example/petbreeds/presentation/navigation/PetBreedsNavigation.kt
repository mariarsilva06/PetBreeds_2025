package com.example.petbreeds.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.petbreeds.presentation.breeds.BreedsScreen
import com.example.petbreeds.presentation.details.DetailsScreen
import com.example.petbreeds.presentation.favorites.FavoritesScreen
import com.example.petbreeds.presentation.onboarding.OnboardingScreen
import com.example.petbreeds.presentation.splash.SplashScreen
import com.example.petbreeds.utils.PreferencesManager
import kotlinx.coroutines.delay

@Composable
fun PetBreedsNavigation(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val isFirstLaunch by preferencesManager.isFirstLaunchFlow.collectAsState(initial = true)
    val petType by preferencesManager.petTypeFlow.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route, // Always start with splash
        modifier = modifier
    ) {
        composable(Routes.Splash.route) {

            LaunchedEffect(isFirstLaunch, petType) {
                delay(2000)
                if (isFirstLaunch || petType == null) {
                    navController.navigate(Routes.Onboarding.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Routes.Breeds.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            }

            SplashScreen(
                onSplashFinished = { }
            )
        }

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