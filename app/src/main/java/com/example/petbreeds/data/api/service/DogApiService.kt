package com.example.petbreeds.data.api.service

import com.example.petbreeds.data.api.dto.DogBreedDto
import com.example.petbreeds.data.api.dto.ImageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface DogApiService {
    @GET("breeds")
    suspend fun getBreeds(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 0
    ): List<DogBreedDto>

    @GET("breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String
    ): List<DogBreedDto>

    @GET("images/search")
    suspend fun getBreedImages(
        @Query("breed_id") breedId: String,
        @Query("limit") limit: Int = 5
    ): List<ImageResponse>
}