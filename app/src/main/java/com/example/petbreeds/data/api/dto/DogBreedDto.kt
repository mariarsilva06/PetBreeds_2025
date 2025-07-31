package com.example.petbreeds.data.api.dto

import com.google.gson.annotations.SerializedName

data class DogBreedDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("origin")
    val origin: String?,
    @SerializedName("temperament")
    val temperament: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("life_span")
    val lifeSpan: String?,
    @SerializedName("image")
    val image: ImageDto?
)

