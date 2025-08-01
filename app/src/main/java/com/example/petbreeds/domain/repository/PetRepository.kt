package com.example.petbreeds.domain.repository

import com.example.petbreeds.core.data.NetworkResult
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getPets(petType: PetType): Flow<NetworkResult<List<Pet>>>
    fun getFavoritePets(petType: PetType): Flow<List<Pet>>
    suspend fun refreshPets(petType: PetType, page: Int = 0, query: String? = null): NetworkResult<Unit>
    suspend fun toggleFavorite(petId: String)
    suspend fun getPetDetails(petId: String): Pet?
}