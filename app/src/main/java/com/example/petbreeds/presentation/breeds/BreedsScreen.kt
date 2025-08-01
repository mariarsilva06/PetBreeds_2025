package com.example.petbreeds.presentation.breeds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.presentation.components.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedsScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: BreedsViewModel = hiltViewModel()
) {
    val uiState by viewModel.petsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val currentPetType by viewModel.currentPetType.collectAsState()
    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isRefreshing)
    var showFilters by remember { mutableStateOf(false) }
    val lifeSpanRange by viewModel.lifeSpanRange.collectAsState()

    // Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .filter { lastVisibleItemIndex ->
                val currentState = uiState
                currentState is BreedsUiState.Success &&
                        lastVisibleItemIndex != null &&
                        lastVisibleItemIndex >= currentState.pets.size - 3 &&
                        !isLoadingMore &&
                        !isRefreshing &&
                        searchQuery.isEmpty() &&
                        lifeSpanRange.start == 0f && lifeSpanRange.endInclusive == 30f
            }
            .collect {
                viewModel.loadNextPage()
            }
    }

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
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = viewModel::onRefresh
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TopBar(
                        title = "Pet Breeds",
                        subtitle = if (currentPetType == PetType.CAT) "Exploring Cats" else "Dogs",
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = viewModel::onSearchQueryChanged,
                            placeholder = "Search ${currentPetType?.name?.lowercase() ?: "pet"} breeds...",
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        IconButton(onClick = { showFilters = true }, modifier = Modifier.padding(start = 8.dp)) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val currentUiState = uiState
                when (currentUiState) {
                    is BreedsUiState.Loading -> LoadingIndicator()

                    is BreedsUiState.Success -> {
                        if (currentUiState.pets.isEmpty() && !isRefreshing) {
                            EmptyState(
                                message = if (searchQuery.isEmpty()) {
                                    "No breeds available"
                                } else {
                                    "No breeds found for \"$searchQuery\""
                                }
                            )
                        } else {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(end = 16.dp, start = 16.dp, bottom = 16.dp)
                            ) {
                                items(
                                    items = currentUiState.pets,
                                    key = { it.id }
                                ) { pet ->
                                    PetCard(
                                        pet = pet,
                                        onCardClick = { onNavigateToDetails(pet.id) },
                                        onFavoriteClick = { viewModel.onToggleFavorite(pet.id) }
                                    )
                                }

                                if (isLoadingMore) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is BreedsUiState.Error -> {
                        ErrorMessage(
                            message = currentUiState.message,
                            onRetry = viewModel::onRefresh
                        )
                    }
                }
            }
        }
    }

    if (showFilters) {
        FilterBottomSheet(
            currentRange = lifeSpanRange,
            onRangeChange = viewModel::onLifeSpanRangeChanged,
            onDismiss = { showFilters = false }
        )
    }
}