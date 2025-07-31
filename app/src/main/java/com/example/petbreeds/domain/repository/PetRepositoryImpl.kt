package com.example.petbreeds.data.repository

import com.example.petbreeds.core.data.NetworkResult
import com.example.petbreeds.data.api.service.CatApiService
import com.example.petbreeds.data.api.service.DogApiService
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.data.mapper.*
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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

    override suspend fun refreshPets(petType: PetType, page: Int): NetworkResult<Unit> {
        return try {
            val pets = when (petType) {
                PetType.CAT -> {
                    val response = catApiService.getBreeds(limit = 20, page = page)
                    response.toCatEntities()
                }
                PetType.DOG -> {
                    val response = dogApiService.getBreeds(limit = 20, page = page)
                    response.toDogEntities()
                }
            }

            // Always preserve favorites, even for the first page
            val existingPets = petDao.getPetsByType(petType).first()
            val petsWithFavorites = pets.map { newPet ->
                val existingPet = existingPets.firstOrNull { existing -> existing.id == newPet.id }
                newPet.copy(isFavorite = existingPet?.isFavorite ?: false)
            }

            if (page == 0) {
                // First page - refresh all but preserve favorites
                petDao.refreshPets(petsWithFavorites, petType)
            } else {
                // Subsequent pages - append
                petDao.insertPets(petsWithFavorites)
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