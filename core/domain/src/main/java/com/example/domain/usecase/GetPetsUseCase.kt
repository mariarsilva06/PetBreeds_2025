package com.example.domain.usecase

import com.example.common.NetworkResult
import com.example.domain.repository.PetRepository
import com.example.model.Pet
import com.example.model.PetType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPetsUseCase
    @Inject
    constructor(
        private val repository: PetRepository,
    ) {
        operator fun invoke(petType: PetType): Flow<NetworkResult<List<Pet>>> = repository.getPets(petType)
    }
