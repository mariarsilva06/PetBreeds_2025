package com.example.data.repository

import android.util.Log
import com.example.common.NetworkResult
import com.example.data.mapper.toCatEntities
import com.example.data.mapper.toDogEntities
import com.example.data.mapper.toDomain
import com.example.database.dao.PetDao
import com.example.domain.repository.PetRepository
import com.example.model.Pet
import com.example.model.PetType
import com.example.network.service.CatApiService
import com.example.network.service.DogApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val catApiService: CatApiService,
    private val dogApiService: DogApiService,
    private val petDao: PetDao
) : PetRepository {

    override fun getPets(petType: PetType): Flow<NetworkResult<List<Pet>>> {
        return petDao.getPetsByType(petType).map { entities ->
            NetworkResult.Success(entities.toDomain())
        }
    }

    override fun getFavoritePets(petType: PetType): Flow<List<Pet>> {
        return petDao.getFavoritePetsByType(petType).map { entities ->
            entities.toDomain()
        }
    }

    override suspend fun refreshPets(petType: PetType, page: Int, query: String?): NetworkResult<Unit> {
        return try {
            // Fetch data from API
            val pets = when (petType) {
                PetType.CAT -> {
                    val response = if (!query.isNullOrBlank()) {
                        catApiService.searchBreeds(query)
                    } else {
                        catApiService.getBreeds(limit = 20, page = page)
                    }
                    response.toCatEntities()
                }
                PetType.DOG -> {
                    val response = if (!query.isNullOrBlank()) {
                        dogApiService.searchBreeds(query)
                    } else {
                        dogApiService.getBreeds(limit = 20, page = page)
                    }
                    response.toDogEntities()
                }
            }

            // Handle different scenarios
            when {
                // Search query - replace all data
                !query.isNullOrBlank() -> {
                    val favoriteIds = petDao.getFavoritePetsByType(petType).first().map { it.id }.toSet()
                    val petsWithFavorites = pets.map { newPet ->
                        newPet.copy(isFavorite = favoriteIds.contains(newPet.id))
                    }
                    petDao.refreshPetsForFirstPage(petsWithFavorites, petType)
                }

                // First page - replace all data
                page == 0 -> {
                    val favoriteIds = petDao.getFavoritePetsByType(petType).first().map { it.id }.toSet()
                    val petsWithFavorites = pets.map { newPet ->
                        newPet.copy(isFavorite = favoriteIds.contains(newPet.id))
                    }
                    petDao.refreshPetsForFirstPage(petsWithFavorites, petType)
                }

                // Subsequent pages - append only new data
                else -> {
                    val existingPets = petDao.getPetsByType(petType).first()
                    val favoriteIds = existingPets.filter { it.isFavorite }.map { it.id }.toSet()

                    val petsWithFavorites = pets.map { newPet ->
                        newPet.copy(isFavorite = favoriteIds.contains(newPet.id))
                    }

                    petDao.appendPets(petsWithFavorites, petType)
                }
            }

            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun toggleFavorite(petId: String) {
        val pet = petDao.getPetById(petId)
        pet?.let {
            val newFavoriteStatus = !it.isFavorite
            petDao.updateFavoriteStatus(petId, newFavoriteStatus)
        }
    }

    override suspend fun getPetDetails(petId: String): Pet? {
        return petDao.getPetById(petId)?.toDomain()
    }

    override suspend fun getPetImages(petId: String, petType: PetType): List<String> {
        return try {
            Log.d("PetRepositoryImpl", "Fetching images for petId: $petId, petType: $petType")

            val cachedPet = petDao.getPetById(petId)

            if (cachedPet != null && cachedPet.additionalImages.isNotEmpty()) {
                Log.d("PetRepositoryImpl", "Found cached images: ${cachedPet.additionalImages.size}")
                return cachedPet.additionalImages
            }

            val images = when (petType) {
                PetType.CAT -> {
                    Log.d("PetRepositoryImpl", "Fetching cat images from API")
                    catApiService.getBreedImages(petId, limit = 5)
                }
                PetType.DOG -> {
                    Log.d("PetRepositoryImpl", "Fetching dog images from API")
                    dogApiService.getBreedImages(petId, limit = 5)
                }
            }.map { it.url }

            Log.d("PetRepositoryImpl", "Fetched ${images.size} images from API")

            if (cachedPet != null) {
                val updatedPet = cachedPet.copy(additionalImages = images)
                petDao.insertPet(updatedPet)
                Log.d("PetRepositoryImpl", "Updated cache with ${images.size} images")
            }

            images
        } catch (e: Exception) {
            Log.e("PetRepositoryImpl", "Error fetching images", e)
            petDao.getPetById(petId)?.additionalImages ?: emptyList()
        }
    }
}