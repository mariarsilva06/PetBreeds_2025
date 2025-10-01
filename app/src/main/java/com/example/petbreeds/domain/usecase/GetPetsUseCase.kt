package com.example.petbreeds.domain.usecase

import com.example.petbreeds.core.data.NetworkResult
import com.example.model.Pet
import com.example.model.PetType
import com.example.petbreeds.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(petType: PetType): Flow<NetworkResult<List<Pet>>> {
        return repository.getPets(petType)
    }
}