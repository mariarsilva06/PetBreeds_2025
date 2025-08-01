package com.example.petbreeds.data.mapper

import com.example.petbreeds.data.api.dto.CatBreedDto
import com.example.petbreeds.data.api.dto.DogBreedDto
import com.example.petbreeds.data.local.entity.PetEntity
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType

// DTO to Entity
fun CatBreedDto.toEntity(
    isFavorite: Boolean = false,
    additionalImages: List<String> = emptyList()
): PetEntity {
    return PetEntity(
        id = id,
        name = name,
        origin = origin ?: "Unknown",
        temperament = temperament ?: "",
        description = description ?: "",
        lifeSpan = lifeSpan ?: "",
        imageUrl = image?.url,
        additionalImages = additionalImages,
        isFavorite = isFavorite,
        petType = PetType.CAT
    )
}

fun DogBreedDto.toEntity(
    isFavorite: Boolean = false,
    additionalImages: List<String> = emptyList()
): PetEntity {
    return PetEntity(
        id = id,
        name = name,
        origin = origin ?: "Unknown",
        temperament = temperament ?: "",
        description = description ?: "",
        lifeSpan = lifeSpan ?: "",
        imageUrl = image?.url,
        additionalImages = additionalImages,
        isFavorite = isFavorite,
        petType = PetType.DOG
    )
}

// Entity to Domain
fun PetEntity.toDomain(): Pet {
    return Pet(
        id = id,
        name = name,
        origin = origin,
        temperament = temperament,
        description = description,
        lifeSpan = lifeSpan,
        imageUrl = imageUrl,
        additionalImages = additionalImages,
        isFavorite = isFavorite,
        petType = petType
    )
}

// List extensions
fun List<PetEntity>.toDomain(): List<Pet> = map { it.toDomain() }
fun List<CatBreedDto>.toCatEntities(): List<PetEntity> = map { it.toEntity() }
fun List<DogBreedDto>.toDogEntities(): List<PetEntity> = map { it.toEntity() }