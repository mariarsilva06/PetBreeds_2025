package com.example.petbreeds.presentation.breeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petbreeds.core.data.NetworkResult
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.domain.usecase.GetPetsUseCase
import com.example.petbreeds.domain.usecase.RefreshPetsUseCase
import com.example.petbreeds.domain.usecase.ToggleFavoriteUseCase
import com.example.petbreeds.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedsViewModel @Inject constructor(
    private val getPetsUseCase: GetPetsUseCase,
    private val refreshPetsUseCase: RefreshPetsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    private val _isLoadingMore = MutableStateFlow(false)

    val currentPetType = preferencesManager.petTypeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetType.CAT
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val petsState = combine(
        currentPetType.filterNotNull(),
        _searchQuery
    ) { petType, searchQuery ->
        Pair(petType, searchQuery)
    }.flatMapLatest { (petType, searchQuery) ->
        getPetsUseCase(petType).map { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val filteredPets = filterPets(result.data ?: emptyList(), searchQuery)
                    BreedsUiState.Success(filteredPets)
                }
                is NetworkResult.Error -> {
                    BreedsUiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Loading -> {
                    BreedsUiState.Success(emptyList())
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BreedsUiState.Loading
    )

    val isLoadingMore = _isLoadingMore.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onToggleFavorite(petId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(petId)
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            currentPetType.value?.let { petType ->
                _isRefreshing.value = true
                _currentPage.value = 0
                refreshPetsUseCase(petType, 0)
                _isRefreshing.value = false
            }
        }
    }

    // Initial load when pet type changes
    init {
        viewModelScope.launch {
            currentPetType.filterNotNull().collect { petType ->
                _currentPage.value = 0
                // Load initial data if no cached data exists
                refreshPetsUseCase(petType, 0)
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            currentPetType.value?.let { petType ->
                if (!_isLoadingMore.value) {
                    _isLoadingMore.value = true
                    val nextPage = _currentPage.value + 1
                    try {
                        val result = refreshPetsUseCase(petType, nextPage)
                        if (result is NetworkResult.Success) {
                            _currentPage.value = nextPage
                        }
                    } catch (e: Exception) {
                        // Handle error silently for pagination
                    } finally {
                        _isLoadingMore.value = false
                    }
                }
            }
        }
    }

    private fun filterPets(pets: List<Pet>, query: String): List<Pet> {
        return if (query.isEmpty()) {
            pets
        } else {
            pets.filter { pet ->
                pet.name.contains(query, ignoreCase = true)
            }
        }
    }
}

sealed interface BreedsUiState {
    object Loading : BreedsUiState
    data class Success(val pets: List<Pet>) : BreedsUiState
    data class Error(val message: String) : BreedsUiState
}
