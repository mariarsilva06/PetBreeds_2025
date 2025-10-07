package com.example.favorites

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.model.PetType
import com.example.domain.usecase.FavoritePetsState
import com.example.ui.components.DrawerContent
import com.example.ui.components.PetCard
import com.example.ui.components.TopBar
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val favoritesState by viewModel.favoritesState.collectAsState()
    val currentPetType by viewModel.currentPetType.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp)
            ) {
                DrawerContent(
                    currentPetType = currentPetType ?: PetType.CAT,
                    onPetTypeChanged = { petType ->
                        viewModel.setPetType(petType)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onCloseDrawer = {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(
                title = "Favorite Breeds",
                subtitle = if (currentPetType == PetType.CAT) "Exploring Cats" else "Dogs",
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                when (val currentFavoritesState = favoritesState) {
                    is FavoritePetsState.Empty -> {
                        EmptyFavoritesState(
                            petType = currentPetType?.name?.lowercase() ?: "pet"
                        )
                    }

                    is FavoritePetsState.Success -> {
                        Column {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp, bottom = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Average Lifespan",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Based on ${currentFavoritesState.pets.size} favorite breeds",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                            alpha = 0.9f
                                        ),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier.size(width = 70.dp, height = 60.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = String.format(
                                                    "%.1f",
                                                    currentFavoritesState.averageLifespan
                                                ),
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }

                            }

                            // Favorites Grid
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(
                                    items = currentFavoritesState.pets,
                                    key = { it.id }
                                ) { pet ->
                                    var visible by remember(pet.id) { mutableStateOf(true) }

                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        modifier = Modifier.animateItemPlacement()
                                    ) {
                                        PetCard(
                                            pet = pet,
                                            onCardClick = { onNavigateToDetails(pet.id) },
                                            onFavoriteClick = {
                                                visible = false
                                                viewModel.toggleFavorite(pet.id)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
    @Composable
    fun EmptyFavoritesState(
        petType: String,
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No favorite $petType breeds yet",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start exploring breeds and tap the heart icon to add them to your favorites",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
