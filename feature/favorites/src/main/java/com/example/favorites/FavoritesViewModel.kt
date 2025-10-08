package com.example.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.PetType
import com.example.domain.usecase.FavoritePetsState
import com.example.domain.usecase.GetFavoritePetsUseCase
import com.example.domain.usecase.ToggleFavoriteUseCase
import com.example.preferences.PreferencesManager
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

    val favoritesState: StateFlow<FavoritePetsState> = currentPetType
        .filterNotNull()
        .flatMapLatest { petType ->
            getFavoritePetsUseCase(petType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritePetsState.Empty
        )

    fun toggleFavorite(petId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(petId)
        }
    }

    fun setPetType(petType: PetType) {
        preferencesManager.setPetType(petType)
    }
}