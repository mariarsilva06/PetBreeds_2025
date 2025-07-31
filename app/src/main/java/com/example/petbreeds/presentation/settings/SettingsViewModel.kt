package com.example.petbreeds.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.utils.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val currentPetType = preferencesManager.petTypeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetType.CAT
    )

    fun setPetType(petType: PetType) {
        preferencesManager.setPetType(petType)
    }
} 