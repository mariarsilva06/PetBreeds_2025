package com.example.petbreeds.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.domain.usecase.FavoritePetsState
import com.example.petbreeds.domain.usecase.GetFavoritePetsUseCase
import com.example.petbreeds.domain.usecase.ToggleFavoriteUseCase
import com.example.petbreeds.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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
                favoritesCount = list.size.toFloat()
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
}
