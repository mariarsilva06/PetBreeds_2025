package com.example.domain.usecase

import com.example.common.NetworkResult
import com.example.domain.common.BaseUseCaseTest
import com.example.domain.common.TestData
import com.example.model.PetType
import com.example.domain.repository.PetRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RefreshPetsUseCaseTest : BaseUseCaseTest(){

    @Mock
    private lateinit var repository: PetRepository

    private lateinit var useCase: RefreshPetsUseCase

    @Before
    override fun setUp() {
        super.setUp()
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
        val searchQuery = TestData.SEARCH_QUERY_PERSIAN
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
        val errorMessage = TestData.ERROR_NETWORK
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