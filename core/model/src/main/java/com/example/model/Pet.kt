package com.example.model

data class Pet(
    val id: String,
    val name: String,
    val origin: String,
    val temperament: String,    // "temperament": "Affectionate, Social, Intelligent, Playful, Active",
    val description: String,
    val lifeSpan: String,       //     "life_span": "9 - 12",
    val imageUrl: String?,
    val additionalImages: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val petType: PetType
)