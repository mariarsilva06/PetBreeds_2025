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

    @Query("UPDATE pets SET isFavorite = :isFavorite WHERE id = :petId")
    suspend fun updateFavoriteStatus(petId: String, isFavorite: Boolean)

    @Query("DELETE FROM pets WHERE petType = :petType")
    suspend fun deleteAllPetsByType(petType: PetType)

    @Transaction
    suspend fun refreshPets(pets: List<PetEntity>, petType: PetType) {
        deleteAllPetsByType(petType)
        insertPets(pets)
    }
}