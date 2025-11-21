package com.example.domain.usecase

import com.example.domain.common.BaseUseCaseTest
import com.example.domain.common.TestData
import com.example.model.Pet
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

class GetPetDetailsUseCaseTest : BaseUseCaseTest() {

    @Mock
    private lateinit var repository: PetRepository

    private lateinit var useCase: GetPetDetailsUseCase

    private val mockPet = TestData.createMockBuddyDog()

    @Before
    override fun setUp() {
        super.setUp()
        useCase = GetPetDetailsUseCase(repository)
    }

    @Test
    fun `should return pet details from repository`() = runTest {
        // Given
        val petId = TestData.PET_ID_1
        whenever(repository.getPetDetails(petId)).thenReturn(mockPet)

        // When
        val result = useCase(petId)

        // Then
        verify(repository).getPetDetails(petId)
        assertThat(result).isEqualTo(mockPet)
    }

    @Test
    fun `should return null when pet not found`() = runTest {
        // Given
        val petId = TestData.NONEXISTENT_PET_ID
        whenever(repository.getPetDetails(petId)).thenReturn(null)

        // When
        val result = useCase(petId)

        // Then
        verify(repository).getPetDetails(petId)
        assertThat(result).isNull()
    }

}