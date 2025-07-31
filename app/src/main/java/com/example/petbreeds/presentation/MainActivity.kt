package com.example.petbreeds.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.petbreeds.core.ui.theme.PetBreedsTheme
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.presentation.navigation.BottomNavItem
import com.example.petbreeds.presentation.navigation.PetBreedsNavigation
import com.example.petbreeds.presentation.navigation.Routes
import com.example.petbreeds.utils.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val currentPetType by preferencesManager.petTypeFlow.collectAsState(initial = PetType.CAT)

            PetBreedsTheme(petType = currentPetType ?: PetType.CAT) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Check if we should show bottom bar
                val showBottomBar = currentDestination?.hierarchy?.any { destination ->
                    destination.route == Routes.Breeds.route ||
                            destination.route == Routes.Favorites.route ||
                            destination.route == Routes.Settings.route
                } == true

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar {
                                val items = listOf(
                                    BottomNavItem.Breeds,
                                    BottomNavItem.Favorites,
                                    BottomNavItem.Settings
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
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    PetBreedsNavigation(
                        navController = navController,
                        preferencesManager = preferencesManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}