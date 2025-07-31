package com.example.petbreeds.domain.model

data class Pet(
    val id: String,
    val name: String,
    val origin: String,
    val temperament: String,
    val description: String,
    val lifeSpan: String,
    val imageUrl: String?,
    val additionalImages: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val petType: PetType
)