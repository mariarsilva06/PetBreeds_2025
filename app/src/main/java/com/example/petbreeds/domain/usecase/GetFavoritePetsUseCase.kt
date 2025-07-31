package com.example.petbreeds.domain.usecase

import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.domain.repository.PetRepository
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
                    favoritesCount = pets.size.toFloat()
                )
            }
        }
    }
}

sealed interface FavoritePetsState {
    object Empty : FavoritePetsState
    data class Success(
        val pets: List<Pet>,
        val favoritesCount: Float
    ) : FavoritePetsState
}