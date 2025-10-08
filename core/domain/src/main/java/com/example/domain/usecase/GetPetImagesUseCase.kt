package com.example.domain.usecase

import com.example.domain.repository.PetRepository
import com.example.model.PetType
import javax.inject.Inject
import android.util.Log

class GetPetImagesUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String, petType: PetType): List<String> {
        return try {
            Log.d("GetPetImagesUseCase", "Fetching images for petId: $petId, petType: $petType")
            repository.getPetImages(petId, petType)
        } catch (e: Exception) {
            Log.e("GetPetImagesUseCase", "Error fetching images", e)
            emptyList()
        }
    }
}