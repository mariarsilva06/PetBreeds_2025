package com.example.petbreeds.data.local.dao

import androidx.room.*
import com.example.petbreeds.data.local.entity.PetEntity
import com.example.petbreeds.domain.model.PetType
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE petType = :petType ORDER BY name ASC")
    fun getPetsByType(petType: PetType): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE petType = :petType AND isFavorite = 1 ORDER BY name ASC")
    fun getFavoritePetsByType(petType: PetType): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: String): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPets(pets: List<PetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Query("UPDATE pets SET isFavorite = :isFavorite WHERE id = :petId")
    suspend fun updateFavoriteStatus(petId: String, isFavorite: Boolean)

    @Query("DELETE FROM pets WHERE petType = :petType")
    suspend fun deleteAllPetsByType(petType: PetType)

    @Query("SELECT COUNT(*) FROM pets WHERE petType = :petType")
    suspend fun getPetCountByType(petType: PetType): Int

    @Query("SELECT id FROM pets WHERE petType = :petType")
    suspend fun getPetIdsByType(petType: PetType): List<String>

    @Transaction
    suspend fun refreshPetsForFirstPage(pets: List<PetEntity>, petType: PetType) {
        deleteAllPetsByType(petType)
        insertPets(pets)
    }

    @Transaction
    suspend fun appendPets(pets: List<PetEntity>, petType: PetType) {
        // Get existing IDs to avoid duplicates
        val existingIds = getPetIdsByType(petType).toSet()
        val newPets = pets.filter { !existingIds.contains(it.id) }
        if (newPets.isNotEmpty()) {
            insertPets(newPets)
        }
    }
}