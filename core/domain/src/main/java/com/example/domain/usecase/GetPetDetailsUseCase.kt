package com.example.domain.usecase

import com.example.domain.repository.PetRepository
import com.example.model.Pet
import javax.inject.Inject

class GetPetDetailsUseCase
    @Inject
    constructor(
        private val repository: PetRepository,
    ) {
        suspend operator fun invoke(petId: String): Pet? = repository.getPetDetails(petId)
    }
