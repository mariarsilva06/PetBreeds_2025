package com.example.petbreeds.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Pet
import com.example.model.PetType
import com.example.petbreeds.domain.usecase.FavoritePetsState
import com.example.petbreeds.domain.usecase.GetFavoritePetsUseCase
import com.example.petbreeds.domain.usecase.ToggleFavoriteUseCase
import com.example.petbreeds.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritePetsUseCase: GetFavoritePetsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val currentPetType = preferencesManager.petTypeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetType.CAT
    )

    private val _favoritePets = MutableStateFlow<List<Pet>>(emptyList())
    val favoritesState: StateFlow<FavoritePetsState> = _favoritePets
        .map { list ->
            if (list.isEmpty()) FavoritePetsState.Empty
            else FavoritePetsState.Success(
                pets = list,
                favoritesCount = list.size.toFloat(),
                averageLifespan = calculateAverageLifespan(list)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FavoritePetsState.Empty)

    init {
        currentPetType
            .filterNotNull()
            .flatMapLatest { getFavoritePetsUseCase(it) }
            .onEach { state ->
                if (state is FavoritePetsState.Success) {
                    _favoritePets.value = state.pets
                } else if (state is FavoritePetsState.Empty) {
                    _favoritePets.value = emptyList()
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleFavorite(petId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(petId)
            _favoritePets.update { it.filterNot { pet -> pet.id == petId } }
        }
    }

    fun setPetType(petType: PetType) {
        preferencesManager.setPetType(petType)
    }

    private fun calculateAverageLifespan(pets: List<Pet>): Float {
        if (pets.isEmpty()) return 0f
        
        val totalLifespan = pets.sumOf { pet ->
            val lifespan = pet.lifeSpan.replace(" years", "").trim()
            try {
                // Handle ranges like "9 - 12" by taking the average
                if (lifespan.contains("-")) {
                    val parts = lifespan.split("-").map { it.trim().toInt() }
                    (parts[0] + parts[1]) / 2
                } else {
                    lifespan.toInt()
                }
            } catch (e: NumberFormatException) {
                0
            }
        }
        
        return totalLifespan.toFloat() / pets.size
    }
}
