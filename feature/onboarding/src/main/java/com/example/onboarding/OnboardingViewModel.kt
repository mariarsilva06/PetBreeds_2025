package com.example.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.PetType
import com.example.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    fun selectPetType(petType: PetType) {
        viewModelScope.launch {
            try {
                preferencesManager.savePetType(petType)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save pet type: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "OnboardingViewModel"
    }
}