package com.example.petbreeds.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.usecase.GetPetDetailsUseCase
import com.example.petbreeds.domain.usecase.GetPetImagesUseCase
import com.example.petbreeds.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.example.petbreeds.domain.usecase.GetFavoritePetsUseCase
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val getPetDetailsUseCase: GetPetDetailsUseCase,
    private val getPetImagesUseCase: GetPetImagesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = checkNotNull(savedStateHandle["petId"])

    private val _pet = MutableStateFlow<Pet?>(null)
    val pet = _pet.asStateFlow()

    private val _additionalImages = MutableStateFlow<List<String>>(emptyList())
    val additionalImages = _additionalImages.asStateFlow()

    private val _isLoadingImages = MutableStateFlow(false)
    val isLoadingImages = _isLoadingImages.asStateFlow()

    private val _averageFavoriteLifeSpan = MutableStateFlow<Double?>(null)
    val averageFavoriteLifeSpan = _averageFavoriteLifeSpan.asStateFlow()

    init {
        loadPetDetails()
    }

    private fun loadPetDetails() {
        viewModelScope.launch {
            Log.d("DetailsViewModel", "Loading pet details for ID: $petId")

            val petDetails = getPetDetailsUseCase(petId)
            _pet.value = petDetails

            Log.d("DetailsViewModel", "Pet details loaded: ${petDetails?.name}")

            petDetails?.let { pet ->
                // Always try to fetch images, even if we have cached ones
                _isLoadingImages.value = true

                try {
                    val images = getPetImagesUseCase(petId, pet.petType)
                    Log.d("DetailsViewModel", "Fetched ${images.size} additional images")
                    _additionalImages.value = images
                } catch (e: Exception) {
                    Log.e("DetailsViewModel", "Error loading images", e)
                    // Fallback to cached images if available
                    _additionalImages.value = pet.additionalImages
                } finally {
                    _isLoadingImages.value = false
                }
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(petId)
            val updatedPet = getPetDetailsUseCase(petId)
            _pet.value = updatedPet
        }
    }

}