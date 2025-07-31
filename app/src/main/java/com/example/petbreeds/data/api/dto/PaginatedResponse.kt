package com.example.petbreeds.data.api.dto

data class PaginatedResponse<T>(
    val data: List<T>,
    val currentPage: Int,
    val totalPages: Int,
    val hasMore: Boolean
)