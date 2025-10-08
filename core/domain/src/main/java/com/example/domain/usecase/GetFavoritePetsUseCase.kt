package com.example.domain.usecase

import com.example.domain.repository.PetRepository
import com.example.model.PetType
import com.example.model.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetFavoritePetsUseCase @Inject constructor(
    private val repository: PetRepository
) {
    operator fun invoke(petType: PetType): Flow<FavoritePetsState> {
        return repository.getFavoritePets(petType).map { pets ->
            if (pets.isEmpty()) {
                FavoritePetsState.Empty
            } else {
                FavoritePetsState.Success(
                    pets = pets,
                    favoritesCount = pets.size.toFloat(),
                    averageLifespan = calculateAverageLifespan(pets)
                )
            }
        }
    }
    private fun calculateAverageLifespan(pets: List<Pet>): Float {
        if (pets.isEmpty()) return 0f

        val totalLifespan = pets.sumOf { pet ->
            val lifespan = pet.lifeSpan.replace(" years", "").trim()
            try {
                // Handle ranges like "9 - 12" by taking the average
                if (lifespan.contains("-")) {
                    val parts = lifespan.split("-").map { it.trim().toDouble() }
                    (parts[0] + parts[1]) / 2.0
                } else {
                    lifespan.toDouble()
                }
            } catch (e: NumberFormatException) {
                0.0
            }
        }

        return (totalLifespan / pets.size).toFloat()
    }
}

sealed interface FavoritePetsState {
    object Empty : FavoritePetsState
    data class Success(
        val pets: List<Pet>,
        val favoritesCount: Float,
        val averageLifespan: Float
    ) : FavoritePetsState
}