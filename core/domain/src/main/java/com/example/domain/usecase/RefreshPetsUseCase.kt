package com.example.domain.usecase

import com.example.common.NetworkResult
import com.example.model.PetType
import com.example.domain.repository.PetRepository
import javax.inject.Inject

class RefreshPetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petType: PetType, page: Int = 0, query: String? = null): NetworkResult<Unit> {
        return repository.refreshPets(petType, page, query)
    }
}