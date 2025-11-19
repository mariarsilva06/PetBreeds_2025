package com.example.feature.favorites

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.usecase.FavoritePetsState
import com.example.feature.R.string
import com.example.model.PetType
import com.example.ui.components.DrawerContent
import com.example.ui.components.PetCard
import com.example.ui.components.TopBar
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    onNavigateToDetails: (String) -> Unit,
    onNavigateToProfile: () -> Unit = {},
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val favoritesState by viewModel.favoritesState.collectAsState()
    val currentPetType by viewModel.currentPetType.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
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
                    },
                )
            }
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            TopBar(
                title = stringResource(string.favorite_breeds_title),
                subtitle =
                    if (currentPetType ==
                        PetType.CAT
                    ) {
                        stringResource(string.exploring_cats_subtitle)
                    } else {
                        stringResource(string.exploring_dogs_subtitle)
                    },
                onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                },
                onProfileClick = onNavigateToProfile,
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
            ) {
                when (val currentFavoritesState = favoritesState) {
                    is FavoritePetsState.Empty -> {
                        EmptyFavoritesState(
                            petType = currentPetType?.name?.lowercase() ?: "pet",
                        )
                    }

                    is FavoritePetsState.Success -> {
                        Column {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp, bottom = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(string.average_lifespan),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = stringResource(string.based_on_favorite_breeds, currentFavoritesState.pets.size),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color =
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                                alpha = 0.9f,
                                            ),
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }

                                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                    Card(
                                        colors =
                                            CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                            ),
                                        modifier = Modifier.size(width = 70.dp, height = 60.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text =
                                                    String.format(
                                                        "%.1f",
                                                        currentFavoritesState.averageLifespan,
                                                    ),
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                            )
                                        }
                                    }
                                }
                            }

                            // Favorites Grid
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp),
                            ) {
                                items(
                                    items = currentFavoritesState.pets,
                                    key = { it.id },
                                ) { pet ->
                                    var visible by remember(pet.id) { mutableStateOf(true) }

                                    AnimatedVisibility(
                                        visible = visible,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                        modifier = Modifier.animateItemPlacement(),
                                    ) {
                                        PetCard(
                                            pet = pet,
                                            onCardClick = { onNavigateToDetails(pet.id) },
                                            onFavoriteClick = {
                                                visible = false
                                                viewModel.toggleFavorite(pet.id)
                                            },
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(string.no_favorite_breeds_yet, petType),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(string.start_exploring_and_favoriting),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
