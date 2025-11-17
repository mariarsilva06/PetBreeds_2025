package com.example.petbreeds.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.feature.breeds.BreedsScreen
import com.example.details.DetailsScreen
import com.example.feature.favorites.FavoritesScreen
import com.example.feature.onboarding.OnboardingScreen
import com.example.feature.splash.SplashScreen
import com.example.preferences.PreferencesManager
import com.example.feature.profile.ProfileScreen


@Composable
fun PetBreedsNavigation(
    navController: NavHostController,
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val isFirstLaunch by preferencesManager.isFirstLaunchFlow.collectAsState(initial = null)
    val petType by preferencesManager.petTypeFlow.collectAsState(initial = null)

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route, // Always start with splash
        modifier = modifier
    ) {

        composable(Routes.Splash.route) {
            val isLoaded = isFirstLaunch != null

            SplashScreen(
                onSplashFinished = {
                    if (isLoaded) {
                        if (isFirstLaunch == true || petType == null) {
                            navController.navigate(Routes.Onboarding.route) {
                                popUpTo(Routes.Splash.route) { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate(Routes.Breeds.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                }
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
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
                }
            )
        }



        composable(Routes.Favorites.route) {
            FavoritesScreen(
                onNavigateToDetails = { petId ->
                    navController.navigate(Routes.Details.createRoute(petId))
                },
                onNavigateToProfile = {
                    navController.navigate(Routes.Profile.route)
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

        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }


    }
}