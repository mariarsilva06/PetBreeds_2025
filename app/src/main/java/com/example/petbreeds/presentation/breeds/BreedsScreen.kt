package com.example.petbreeds.presentation.breeds

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petbreeds.presentation.components.*
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

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

    // Load more when reaching the end
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .filter { lastVisibleItemIndex ->
                val currentState = uiState
                currentState is BreedsUiState.Success &&
                        lastVisibleItemIndex != null &&
                        lastVisibleItemIndex >= currentState.pets.size - 5 &&
                        !isLoadingMore
            }
            .collect {
                viewModel.loadNextPage()
            }
    }

    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = viewModel::onRefresh
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search ${currentPetType?.name?.lowercase()} breeds..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            val currentUiState = uiState
            when (currentUiState) {
                is BreedsUiState.Loading -> {
                    LoadingIndicator()
                }
                is BreedsUiState.Success -> {
                    if (currentUiState.pets.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isEmpty()) {
                                "No breeds available"
                            } else {
                                "No breeds found for \"$searchQuery\""
                            }
                        )
                    } else {
                        Box {
                            LazyColumn(
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
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
                            }

                            if (isLoadingMore) {
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

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}