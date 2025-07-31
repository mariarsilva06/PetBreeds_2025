package com.example.petbreeds.domain.usecase

import com.example.petbreeds.data.api.service.CatApiService
import com.example.petbreeds.data.api.service.DogApiService
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.domain.model.PetType
import javax.inject.Inject

class GetPetImagesUseCase @Inject constructor(
    private val catApiService: CatApiService,
    private val dogApiService: DogApiService,
    private val petDao: PetDao
) {
    suspend operator fun invoke(petId: String, petType: PetType): List<String> {
        return try {
            // First check if we have cached images
            val cachedPet = petDao.getPetById(petId)
            if (cachedPet != null && cachedPet.additionalImages.isNotEmpty()) {
                return cachedPet.additionalImages
            }

            // Fetch from API
            val images = when (petType) {
                PetType.CAT -> catApiService.getBreedImages(petId, limit = 5)
                PetType.DOG -> dogApiService.getBreedImages(petId, limit = 5)
            }.map { it.url }

            // Update cache
            cachedPet?.let { pet ->
                petDao.insertPets(listOf(pet.copy(additionalImages = images)))
            }

            images
        } catch (e: Exception) {
            emptyList()
        }
    }
}