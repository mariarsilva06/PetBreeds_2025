package com.example.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetFavoritePetsUseCase
import com.example.domain.usecase.FavoritePetsState
import com.example.model.Pet
import com.example.model.PetType
import com.example.preferences.PreferencesManager
import com.example.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getFavoritePetsUseCase: GetFavoritePetsUseCase,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val currentPetType: StateFlow<PetType?> = preferencesManager.petTypeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val currentThemeMode: StateFlow<ThemeMode> = preferencesManager.themeModeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val favoritesCount: StateFlow<Int> = currentPetType
        .filterNotNull()
        .flatMapLatest { petType ->
            getFavoritePetsUseCase(petType)
        }
        .map { state ->
            when (state) {
                is FavoritePetsState.Success -> state.pets.size
                else -> 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val averageLifespan: StateFlow<Float> = currentPetType
        .filterNotNull()
        .flatMapLatest { petType ->
            getFavoritePetsUseCase(petType)
        }
        .map { state ->
            when (state) {
                is FavoritePetsState.Success -> calculateAverageLifespan(state.pets)
                else -> 0f
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    // TODO: Add user authentication and profile sync
    // TODO: Implement profile photo upload/change functionality
    // TODO: Add ability to edit user name and bio
    // TODO: Add email/phone verification
    // TODO: Add achievement badges system (e.g., "Favorited 10 breeds", "Explorer")
    // TODO: Implement account deletion option with confirmation
    // TODO: Add feedback/bug report form
    // TODO: Implement app rating prompt

    fun updatePetType(petType: PetType) {
        viewModelScope.launch {
            preferencesManager.savePetType(petType)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.saveThemeMode(mode)
        }
    }

    private fun calculateAverageLifespan(pets: List<Pet>): Float {
        if (pets.isEmpty()) return 0f

        val totalLifespan = pets.sumOf { pet ->
            val lifespan = pet.lifeSpan.replace(" years", "").trim()
            try {
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

    // TODO: Implement streak counter (days in a row using the app)
}