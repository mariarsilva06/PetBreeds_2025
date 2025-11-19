package com.example.data.mapper

import com.example.database.entity.PetEntity
import com.example.model.Pet
import com.example.model.PetType
import com.example.network.dto.CatBreedDto
import com.example.network.dto.DogBreedDto

// DTO to Entity
fun CatBreedDto.toEntity(
    isFavorite: Boolean = false,
    additionalImages: List<String> = emptyList(),
): PetEntity =
    PetEntity(
        id = id,
        name = name,
        origin = origin ?: "Unknown",
        temperament = temperament ?: "",
        description = description ?: "",
        lifeSpan = lifeSpan ?: "",
        imageUrl = image?.url,
        additionalImages = additionalImages,
        isFavorite = isFavorite,
        petType = PetType.CAT,
    )

fun DogBreedDto.toEntity(
    isFavorite: Boolean = false,
    additionalImages: List<String> = emptyList(),
): PetEntity =
    PetEntity(
        id = id,
        name = name,
        origin = origin ?: "Unknown",
        temperament = temperament ?: "",
        description = description ?: "",
        lifeSpan = lifeSpan ?: "",
        imageUrl = image?.url,
        additionalImages = additionalImages,
        isFavorite = isFavorite,
        petType = PetType.DOG,
    )

// Entity to Domain
fun PetEntity.toDomain(): Pet =
    Pet(
        id = id,
        name = name,
        origin = origin,
        temperament = temperament,
        description = description,
        lifeSpan = lifeSpan,
        imageUrl = imageUrl,
        additionalImages = additionalImages,
        isFavorite = isFavorite,
        petType = petType,
    )

// List extensions
fun List<PetEntity>.toDomain(): List<Pet> = map { it.toDomain() }

fun List<CatBreedDto>.toCatEntities(): List<PetEntity> = map { it.toEntity() }

fun List<DogBreedDto>.toDogEntities(): List<PetEntity> = map { it.toEntity() }
