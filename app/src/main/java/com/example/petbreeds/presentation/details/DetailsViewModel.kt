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

    init {
        loadPetDetails()
    }

    private fun loadPetDetails() {
        viewModelScope.launch {
            val petDetails = getPetDetailsUseCase(petId)
            _pet.value = petDetails

            petDetails?.let { pet ->
                if (pet.additionalImages.isNotEmpty()) {
                    _additionalImages.value = pet.additionalImages
                } else {
                    val images = getPetImagesUseCase(petId, pet.petType)
                    _additionalImages.value = images
                }
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            toggleFavoriteUseCase(petId)
            loadPetDetails()
        }
    }
}