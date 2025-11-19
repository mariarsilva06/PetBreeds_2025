package com.example.network.dto

import com.google.gson.annotations.SerializedName

data class CatBreedDto(
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
    val image: ImageDto?,
)

data class ImageDto(
    @SerializedName("url")
    val url: String,
)
