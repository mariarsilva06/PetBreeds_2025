package com.example.petbreeds.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.model.PetType
import com.example.petbreeds.presentation.navigation.BottomNavItem
import com.example.petbreeds.presentation.navigation.PetBreedsNavigation
import com.example.petbreeds.presentation.navigation.Routes
import com.example.preferences.PreferencesManager
import com.example.preferences.ThemeMode
import com.example.ui.theme.PetBreedsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferencesManager: PreferencesManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        setContent {
            val navController = rememberNavController()
            val currentPetType by preferencesManager.petTypeFlow.collectAsState(initial = null)
            val isFirstLaunch by preferencesManager.isFirstLaunchFlow.collectAsState(initial = true)
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val themeMode by preferencesManager.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
            var navigationReady by remember { mutableStateOf(false) }

            val darkTheme =
                when (themeMode) {
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }

            LaunchedEffect(navController.currentBackStackEntryAsState().value) {
                val route = navController.currentBackStackEntry?.destination?.route
                if (route == Routes.Breeds.route || route == Routes.Favorites.route || route == Routes.Settings.route) {
                    navigationReady = true
                }
            }

            // Only dismiss splash when both are ready
            LaunchedEffect(currentPetType, navigationReady) {
                if (currentPetType != null && navigationReady) {
                    keepSplashScreen = false
                }
            }

            // Use a default theme during loading, but don't load data
            PetBreedsTheme(
                petType = currentPetType ?: PetType.CAT,
                darkTheme = darkTheme,
            ) {
                val showBottomBar =
                    currentDestination?.hierarchy?.any { destination ->
                        destination.route == Routes.Breeds.route ||
                            destination.route == Routes.Favorites.route ||
                            destination.route == Routes.Settings.route
                    } == true

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                val items =
                                    listOf(
                                        BottomNavItem.Breeds,
                                        BottomNavItem.Favorites,
                                    )

                                items.forEach { item ->
                                    NavigationBarItem(
                                        icon = { Icon(item.icon, contentDescription = item.label) },
                                        label = { Text(item.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    },
                ) { innerPadding ->
                    PetBreedsNavigation(
                        navController = navController,
                        preferencesManager = preferencesManager,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
