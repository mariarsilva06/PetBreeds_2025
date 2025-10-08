package com.example.domain.usecase

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

class GetPetDetailsUseCaseTest {

    @Mock
    private lateinit var repository: PetRepository

    private lateinit var useCase: GetPetDetailsUseCase

    private val mockPet = Pet(
        id = "1",
        name = "Persian",
        origin = "Iran",
        temperament = "Calm",
        description = "Long-haired breed",
        lifeSpan = "12 - 17",
        imageUrl = "http://example.com/persian.jpg",
        petType = PetType.CAT
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetPetDetailsUseCase(repository)
    }

    @Test
    fun `should return pet details from repository`() = runTest {
        // Given
        val petId = "1"
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
        val petId = "nonexistent"
        whenever(repository.getPetDetails(petId)).thenReturn(null)

        // When
        val result = useCase(petId)

        // Then
        verify(repository).getPetDetails(petId)
        assertThat(result).isNull()
    }

}