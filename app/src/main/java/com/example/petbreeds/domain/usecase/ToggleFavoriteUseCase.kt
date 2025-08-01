package com.example.petbreeds.domain.usecase

import com.example.petbreeds.domain.repository.PetRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: PetRepository
) {
    suspend operator fun invoke(petId: String) {
        repository.toggleFavorite(petId)
    }
}