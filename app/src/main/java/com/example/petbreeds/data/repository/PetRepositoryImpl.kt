package com.example.petbreeds.data.repository

import com.example.petbreeds.core.data.NetworkResult
import com.example.petbreeds.data.api.service.CatApiService
import com.example.petbreeds.data.api.service.DogApiService
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.data.mapper.toCatEntities
import com.example.petbreeds.data.mapper.toDogEntities
import com.example.petbreeds.data.mapper.toDomain
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.domain.repository.PetRepository
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
}