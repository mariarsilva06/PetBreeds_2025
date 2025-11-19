package com.example.domain.usecase

import android.util.Log
import com.example.domain.repository.PetRepository
import com.example.model.PetType
import javax.inject.Inject

class GetPetImagesUseCase
    @Inject
    constructor(
        private val repository: PetRepository,
    ) {
        suspend operator fun invoke(
            petId: String,
            petType: PetType,
        ): List<String> =
            try {
                Log.d("GetPetImagesUseCase", "Fetching images for petId: $petId, petType: $petType")
                repository.getPetImages(petId, petType)
            } catch (e: Exception) {
                Log.e("GetPetImagesUseCase", "Error fetching images", e)
                emptyList()
            }
    }
