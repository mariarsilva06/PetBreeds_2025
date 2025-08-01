package com.example.petbreeds.domain.usecase

import com.example.petbreeds.core.data.NetworkResult
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.domain.repository.PetRepository
import javax.inject.Inject

class RefreshPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petType: PetType, page: Int = 0, query: String? = null): NetworkResult<Unit> {
        return repository.refreshPets(petType, page, query)
    }
}