package com.example.petbreeds.domain.usecase

import com.example.model.Pet
import com.example.petbreeds.domain.repository.PetRepository
import javax.inject.Inject

class GetPetDetailsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String): Pet? {
        return repository.getPetDetails(petId)
    }
}