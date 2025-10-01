package com.example.petbreeds.domain.usecase

import com.example.petbreeds.core.data.NetworkResult
import com.example.model.PetType
import com.example.petbreeds.domain.repository.PetRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RefreshPetsUseCaseTest {

    @Mock
    private lateinit var repository: PetRepository

    private lateinit var useCase: RefreshPetsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = RefreshPetsUseCase(repository)
    }

    @Test
    fun `should delegate to repository with correct parameters`() = runTest {
        // Given
        whenever(repository.refreshPets(PetType.CAT, 0, null)).thenReturn(
            NetworkResult.Success(Unit)
        )

        // When
        val result = useCase(PetType.CAT, 0, null)

        // Then
        verify(repository).refreshPets(PetType.CAT, 0, null)
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
    }

    @Test
    fun `should pass search query to repository`() = runTest {
        // Given
        val searchQuery = "Persian"
        whenever(repository.refreshPets(PetType.CAT, 0, searchQuery)).thenReturn(
            NetworkResult.Success(Unit)
        )

        // When
        val result = useCase(PetType.CAT, 0, searchQuery)

        // Then
        verify(repository).refreshPets(PetType.CAT, 0, searchQuery)
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
    }

    @Test
    fun `should handle repository error`() = runTest {
        // Given
        val errorMessage = "Network error"
        whenever(repository.refreshPets(PetType.CAT, 0, null)).thenReturn(
            NetworkResult.Error(errorMessage)
        )

        // When
        val result = useCase(PetType.CAT, 0, null)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        val errorResult = result as NetworkResult.Error
        assertThat(errorResult.message).isEqualTo(errorMessage)
    }
}