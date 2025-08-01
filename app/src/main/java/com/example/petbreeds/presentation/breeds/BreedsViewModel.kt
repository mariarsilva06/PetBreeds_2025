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

    private val _lifeSpanRange = MutableStateFlow(0f..30f)
    val lifeSpanRange = _lifeSpanRange.asStateFlow()

    val isLoadingMore = _isLoadingMore.asStateFlow()

    val currentPetType = preferencesManager.petTypeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null // Changed from PetType.CAT to null
    )

    val petsState = combine(
        currentPetType.filterNotNull(),
        _searchQuery,
        _lifeSpanRange
    ) { petType, searchQuery, range ->
        Triple(petType, searchQuery, range)
    }.flatMapLatest { (petType, searchQuery, lifeSpanRange) ->
        getPetsUseCase(petType).map { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val filteredPets = filterPets(result.data ?: emptyList(), searchQuery, lifeSpanRange)
                    BreedsUiState.Success(filteredPets)
                }
                is NetworkResult.Error -> {
                    BreedsUiState.Error(result.message ?: "Unknown error")
                }
                is NetworkResult.Loading -> {
                    BreedsUiState.Loading
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BreedsUiState.Loading
    )

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
                refreshPetsUseCase(petType, 0, _searchQuery.value.takeIf { it.isNotBlank() })
                _isRefreshing.value = false
            }
        }
    }

    // Initial load when pet type changes - only if pet type is set
    init {
        viewModelScope.launch {
            try {
                currentPetType.filterNotNull().collect { petType ->
                    _currentPage.value = 0
                    refreshPetsUseCase(petType, 0)
                }
            } catch (e: Exception) {
                // Handle initialization errors
                e.printStackTrace()
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
                        val result = refreshPetsUseCase(petType, nextPage, null)
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

    private fun filterPets(
        pets: List<Pet>,
        query: String,
        lifeSpanRange: ClosedFloatingPointRange<Float>
    ): List<Pet> {
        return pets.filter { pet ->
            val matchesQuery = query.isEmpty() || pet.name.contains(query, ignoreCase = true)

            val lifeSpanValue = extractLifeSpan(pet.lifeSpan)
            val matchesLifeSpan = lifeSpanValue?.let { it in lifeSpanRange } ?: true

            matchesQuery && matchesLifeSpan
        }
    }

    fun onLifeSpanRangeChanged(range: ClosedFloatingPointRange<Float>) {
        _lifeSpanRange.value = range
    }

    fun setPetType(petType: PetType) {
        preferencesManager.setPetType(petType)
    }

    private fun extractLifeSpan(lifeSpan: String): Float? {
        // Extract numbers from strings like "10 - 12 years" or "10 years"
        val regex = Regex("(\\d+)")
        val numbers = regex.findAll(lifeSpan).mapNotNull { it.value.toFloatOrNull() }.toList()
        return if (numbers.isNotEmpty()) {
            numbers.average().toFloat()
        } else {
            null
        }
    }
}

sealed interface BreedsUiState {
    object Loading : BreedsUiState
    data class Success(val pets: List<Pet>) : BreedsUiState
    data class Error(val message: String) : BreedsUiState
}