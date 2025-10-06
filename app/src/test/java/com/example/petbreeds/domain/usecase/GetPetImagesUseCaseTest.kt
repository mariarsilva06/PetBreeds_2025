package com.example.petbreeds.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.network.dto.ImageResponse
import com.example.petbreeds.data.api.service.CatApiService
import com.example.petbreeds.data.api.service.DogApiService
import com.example.database.dao.PetDao
import com.example.database.entity.PetEntity
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

    private lateinit var catApiService: CatApiService
    private lateinit var dogApiService: DogApiService
    private lateinit var petDao: PetDao
    private lateinit var getPetImagesUseCase: GetPetImagesUseCase

    @Before
    fun setUp() {
        catApiService = mockk(relaxed = true)
        dogApiService = mockk(relaxed = true)
        petDao = mockk(relaxed = true)

        getPetImagesUseCase = GetPetImagesUseCase(catApiService, dogApiService, petDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN cached images exist WHEN invoke is called THEN returns cached images without API call`() = runTest {
        // Given
        val petId = "test-pet-id"
        val cachedImages = listOf("cached1.jpg", "cached2.jpg")
        val mockPetEntity = mockk<PetEntity> {
            every { additionalImages } returns cachedImages
        }
        coEvery { petDao.getPetById(petId) } returns mockPetEntity

        // When
        val result = getPetImagesUseCase(petId, PetType.CAT)

        // Then
        assert(result == cachedImages)
        coVerify { petDao.getPetById(petId) }
        coVerify(exactly = 0) { catApiService.getBreedImages(any(), any()) }
    }

    @Test
    fun `GIVEN no cached images for cat WHEN invoke is called THEN fetches from cat API and caches result`() = runTest {
        // Given
        val petId = "test-pet-id"
        val apiImages = listOf(
            ImageResponse("1", "image1.jpg", 400, 300),
            ImageResponse("2", "image2.jpg", 400, 300)
        )
        val expectedUrls = listOf("image1.jpg", "image2.jpg")
        val mockPetEntity = mockk<PetEntity> {
            every { additionalImages } returns emptyList()
            every { copy(additionalImages = any()) } returns this
        }

        coEvery { petDao.getPetById(petId) } returns mockPetEntity
        coEvery { catApiService.getBreedImages(petId, 5) } returns apiImages
        coEvery { petDao.insertPet(any()) } just Runs

        // When
        val result = getPetImagesUseCase(petId, PetType.CAT)

        // Then
        assert(result == expectedUrls) {
            "Expected $expectedUrls but got $result"
        }
        coVerify { catApiService.getBreedImages(petId, 5) }
        coVerify { petDao.insertPet(any()) }
    }

    @Test
    fun `GIVEN no cached images for dog WHEN invoke is called THEN fetches from dog API and caches result`() = runTest {
        // Given
        val petId = "test-pet-id"
        val apiImages = listOf(
            ImageResponse("1", "dogimage1.jpg", 400, 300)
        )
        val expectedUrls = listOf("dogimage1.jpg")
        val mockPetEntity = mockk<PetEntity> {
            every { additionalImages } returns emptyList()
            every { copy(additionalImages = any()) } returns this
        }

        coEvery { petDao.getPetById(petId) } returns mockPetEntity
        coEvery { dogApiService.getBreedImages(petId, 5) } returns apiImages
        coEvery { petDao.insertPet(any()) } just Runs

        // When
        val result = getPetImagesUseCase(petId, PetType.DOG)

        // Then
        assert(result == expectedUrls) {
            "Expected $expectedUrls but got $result"
        }
        coVerify { dogApiService.getBreedImages(petId, 5) }
        coVerify { petDao.insertPet(any()) }
    }

    @Test
    fun `GIVEN API error WHEN invoke is called THEN returns empty list`() = runTest {
        // Given
        val petId = "test-pet-id"
        val mockPetEntity = mockk<PetEntity> {
            every { additionalImages } returns emptyList()
        }

        coEvery { petDao.getPetById(petId) } returns mockPetEntity
        coEvery { catApiService.getBreedImages(petId, 5) } throws Exception("Network error")

        // When
        val result = getPetImagesUseCase(petId, PetType.CAT)

        // Then
        assert(result.isEmpty())
        coVerify { catApiService.getBreedImages(petId, 5) }
    }

    @Test
    fun `GIVEN pet not found in database WHEN invoke is called THEN fetches from API without caching`() = runTest {
        // Given
        val petId = "non-existent-pet"
        val apiImages = listOf(ImageResponse("1", "image1.jpg", 400, 300))
        val expectedUrls = listOf("image1.jpg")

        coEvery { petDao.getPetById(petId) } returns null
        coEvery { catApiService.getBreedImages(petId, 5) } returns apiImages

        // When
        val result = getPetImagesUseCase(petId, PetType.CAT)

        // Then
        assert(result == expectedUrls)
        coVerify { catApiService.getBreedImages(petId, 5) }
        coVerify(exactly = 0) { petDao.insertPet(any()) }
    }
}