package com.example.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.usecase.GetFavoritePetsUseCase
import com.example.domain.usecase.FavoritePetsState
import com.example.model.PetType
import com.example.preferences.PreferencesManager
import com.example.preferences.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    val userName: StateFlow<String> = preferencesManager.userNameFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Pet Lover"
        )

    val userBio: StateFlow<String> = preferencesManager.userBioFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Exploring the world of pets"
        )

    val userPhotoUri: StateFlow<String?> = preferencesManager.userPhotoUriFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        ) //todo

    @OptIn(ExperimentalCoroutinesApi::class)
    private val favoritesStateFlow: StateFlow<FavoritePetsState> = currentPetType
        .filterNotNull()
        .flatMapLatest { petType ->
            getFavoritePetsUseCase(petType)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritePetsState.Empty
        )

    val favoritesCount: StateFlow<Int> = favoritesStateFlow
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

    val averageLifespan: StateFlow<Float> = favoritesStateFlow
        .map { state ->
            when (state) {
                is FavoritePetsState.Success -> state.averageLifespan
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
    fun updateUserName(name: String) {
        viewModelScope.launch {
            preferencesManager.saveUserName(name)
        }
    }

    fun updateUserBio(bio: String) {
        viewModelScope.launch {
            preferencesManager.saveUserBio(bio)
        }
    }

    fun updateUserPhotoUri(uri: String?) {
        viewModelScope.launch {
            preferencesManager.saveUserPhotoUri(uri)
        }
    } //todo

    // TODO: Implement streak counter (days in a row using the app)
}