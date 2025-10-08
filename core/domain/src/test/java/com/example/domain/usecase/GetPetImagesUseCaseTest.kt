package com.example.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.domain.repository.PetRepository
import com.example.model.PetType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GetPetImagesUseCaseTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var petRepository: PetRepository
    private lateinit var getPetImagesUseCase: GetPetImagesUseCase

    @Before
    fun setUp() {
        petRepository = mockk(relaxed = true)
        getPetImagesUseCase = GetPetImagesUseCase(petRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN repository returns images WHEN invoke is called THEN returns those images`() = runTest {
        // Given
        val petId = "test-pet-id"
        val expectedImages = listOf("image1.jpg", "image2.jpg", "image3.jpg")
        coEvery { petRepository.getPetImages(petId, PetType.CAT) } returns expectedImages

        // When
        val result = getPetImagesUseCase(petId, PetType.CAT)

        // Then
        assert(result == expectedImages) {
            "Expected $expectedImages but got $result"
        }
        coVerify { petRepository.getPetImages(petId, PetType.CAT) }
    }

    @Test
    fun `GIVEN repository returns empty list WHEN invoke is called THEN returns empty list`() = runTest {
        // Given
        val petId = "test-pet-id"
        coEvery { petRepository.getPetImages(petId, PetType.DOG) } returns emptyList()

        // When
        val result = getPetImagesUseCase(petId, PetType.DOG)

        // Then
        assert(result.isEmpty()) {
            "Expected empty list but got $result"
        }
        coVerify { petRepository.getPetImages(petId, PetType.DOG) }
    }

    @Test
    fun `GIVEN repository throws exception WHEN invoke is called THEN returns empty list`() = runTest {
        // Given
        val petId = "test-pet-id"
        val exception = Exception("Network error")
        coEvery { petRepository.getPetImages(petId, PetType.CAT) } throws exception

        // When
        val result = getPetImagesUseCase(petId, PetType.CAT)

        // Then
        assert(result.isEmpty()) {
            "Expected empty list on exception but got $result"
        }
        coVerify { petRepository.getPetImages(petId, PetType.CAT) }
    }

    @Test
    fun `GIVEN different pet types WHEN invoke is called THEN calls repository with correct pet type`() = runTest {
        // Given
        val catPetId = "cat-id"
        val dogPetId = "dog-id"
        val catImages = listOf("cat1.jpg", "cat2.jpg")
        val dogImages = listOf("dog1.jpg", "dog2.jpg")

        coEvery { petRepository.getPetImages(catPetId, PetType.CAT) } returns catImages
        coEvery { petRepository.getPetImages(dogPetId, PetType.DOG) } returns dogImages

        // When
        val catResult = getPetImagesUseCase(catPetId, PetType.CAT)
        val dogResult = getPetImagesUseCase(dogPetId, PetType.DOG)

        // Then
        assert(catResult == catImages)
        assert(dogResult == dogImages)
        coVerify { petRepository.getPetImages(catPetId, PetType.CAT) }
        coVerify { petRepository.getPetImages(dogPetId, PetType.DOG) }
    }
}