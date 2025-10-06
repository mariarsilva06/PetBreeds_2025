package com.example.domain.usecase

import com.example.database.dao.PetDao
import com.example.model.PetType
import javax.inject.Inject
import android.util.Log
import com.example.network.service.CatApiService
import com.example.network.service.DogApiService

class GetPetImagesUseCase @Inject constructor(
    private val catApiService: CatApiService,
    private val dogApiService: DogApiService,
    private val petDao: PetDao
) {
    suspend operator fun invoke(petId: String, petType: PetType): List<String> {
        return try {
            Log.d("GetPetImagesUseCase", "Fetching images for petId: $petId, petType: $petType")

            // First check if we have cached images
            val cachedPet = petDao.getPetById(petId)
            if (cachedPet != null && cachedPet.additionalImages.isNotEmpty()) {
                Log.d("GetPetImagesUseCase", "Found cached images: ${cachedPet.additionalImages.size}")
                return cachedPet.additionalImages
            }

            // Fetch from API
            val images = when (petType) {
                PetType.CAT -> {
                    Log.d("GetPetImagesUseCase", "Fetching cat images from API")
                    catApiService.getBreedImages(petId, limit = 5)
                }
                PetType.DOG -> {
                    Log.d("GetPetImagesUseCase", "Fetching dog images from API")
                    dogApiService.getBreedImages(petId, limit = 5)
                }
            }.map { it.url }

            Log.d("GetPetImagesUseCase", "Fetched ${images.size} images from API")

            // Update cache with new images
            cachedPet?.let { pet ->
                val updatedPet = pet.copy(additionalImages = images)
                petDao.insertPet(updatedPet)
                Log.d("GetPetImagesUseCase", "Updated cache with ${images.size} images")
            }

            images
        } catch (e: Exception) {
            Log.e("GetPetImagesUseCase", "Error fetching images", e)
            emptyList()
        }
    }
}