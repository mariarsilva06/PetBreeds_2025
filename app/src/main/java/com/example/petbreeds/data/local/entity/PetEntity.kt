package com.example.petbreeds.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.model.PetType
import com.example.petbreeds.data.local.database.Converters

@Entity(tableName = "pets")
@TypeConverters(Converters::class)
data class PetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val origin: String,
    val temperament: String,
    val description: String,
    val lifeSpan: String,
    val imageUrl: String?,
    val additionalImages: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val petType: PetType,
    val lastUpdated: Long = System.currentTimeMillis()
)