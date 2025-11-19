package com.example.network.service

import com.example.network.dto.CatBreedDto
import com.example.network.dto.ImageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CatApiService {
    @GET("breeds")
    suspend fun getBreeds(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 0,
    ): List<CatBreedDto>

    @GET("breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String,
    ): List<CatBreedDto>

    @GET("images/search")
    suspend fun getBreedImages(
        @Query("breed_id") breedId: String,
        @Query("limit") limit: Int = 5,
    ): List<ImageResponse>
}
