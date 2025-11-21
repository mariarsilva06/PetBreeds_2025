package com.example.domain.usecase

import com.example.common.NetworkResult
import com.example.domain.common.BaseUseCaseTest
import com.example.domain.common.TestData

import com.example.model.Pet
import com.example.model.PetType
import com.example.domain.repository.PetRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetPetsUseCaseTest : BaseUseCaseTest(){

    @Mock
    private lateinit var repository: PetRepository

    private lateinit var useCase: GetPetsUseCase

    private val mockPets = listOf(TestData.createMockPersianCat())

    @Before
    override fun setUp() {
        super.setUp()
        useCase = GetPetsUseCase(repository)
    }

    @Test
    fun `should return pets from repository`() = runTest {
        // Given
        whenever(repository.getPets(PetType.CAT)).thenReturn(
            flowOf(NetworkResult.Success(mockPets))
        )

        // When
        val result = useCase(PetType.CAT).first()

        // Then
        verify(repository).getPets(PetType.CAT)
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        val successResult = result as NetworkResult.Success
        assertThat(successResult.data).isEqualTo(mockPets)
    }
}