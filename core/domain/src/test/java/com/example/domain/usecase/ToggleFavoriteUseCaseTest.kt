package com.example.domain.usecase

import com.example.domain.repository.PetRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify

class ToggleFavoriteUseCaseTest {
    @Mock
    private lateinit var repository: PetRepository

    private lateinit var useCase: ToggleFavoriteUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = ToggleFavoriteUseCase(repository)
    }

    @Test
    fun `should delegate to repository`() =
        runTest {
            // Given
            val petId = "test_pet_id"

            // When
            useCase(petId)

            // Then
            verify(repository).toggleFavorite(petId)
        }
}
