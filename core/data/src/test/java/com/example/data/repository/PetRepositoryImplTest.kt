package com.example.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.common.NetworkResult
import com.example.database.dao.PetDao
import com.example.database.entity.PetEntity
import com.example.model.PetType
import com.example.network.dto.CatBreedDto
import com.example.network.dto.ImageDto
import com.example.network.dto.ImageResponse
import com.example.network.service.CatApiService
import com.example.network.service.DogApiService
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class PetRepositoryImplTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var catApiService: CatApiService
    private lateinit var dogApiService: DogApiService
    private lateinit var petDao: PetDao
    private lateinit var petRepository: PetRepositoryImpl

    @Before
    fun setUp() {
        catApiService = mockk(relaxed = true)
        dogApiService = mockk(relaxed = true)
        petDao = mockk(relaxed = true)

        petRepository = PetRepositoryImpl(catApiService, dogApiService, petDao)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN pets in database WHEN getPets is called THEN returns pets from local storage`() =
        runTest {
            // Given
            val mockPetEntities =
                listOf(
                    createMockPetEntity("1", "Persian", PetType.CAT),
                    createMockPetEntity("2", "Maine Coon", PetType.CAT),
                )
            every { petDao.getPetsByType(PetType.CAT) } returns flowOf(mockPetEntities)

            // When
            val result = petRepository.getPets(PetType.CAT).first()

            // Then
            assert(result is NetworkResult.Success)
            val pets = (result as NetworkResult.Success).data
            assert(pets?.size == 2)
            assert(pets?.first()?.name == "Persian")
            verify { petDao.getPetsByType(PetType.CAT) }
        }

    @Test
    fun `GIVEN favorite pets in database WHEN getFavoritePets is called THEN returns only favorite pets`() =
        runTest {
            // Given
            val mockFavoritePetEntities =
                listOf(
                    createMockPetEntity("1", "Persian", PetType.CAT, isFavorite = true),
                )
            every { petDao.getFavoritePetsByType(PetType.CAT) } returns flowOf(mockFavoritePetEntities)

            // When
            val result = petRepository.getFavoritePets(PetType.CAT).first()

            // Then
            assert(result.size == 1)
            assert(result.first().isFavorite)
            assert(result.first().name == "Persian")
            verify { petDao.getFavoritePetsByType(PetType.CAT) }
        }

    @Test
    fun `GIVEN successful API response WHEN refreshPets is called for first page THEN replaces all pets in database`() =
        runTest {
            // Given
            val mockApiResponse =
                listOf(
                    CatBreedDto(
                        "1",
                        "Persian",
                        "Iran",
                        "Calm",
                        "Description",
                        "10-15",
                        ImageDto("image.jpg"),
                    ),
                )
            val mockFavorites = emptyList<PetEntity>()

            coEvery { catApiService.getBreeds(20, 0) } returns mockApiResponse
            coEvery { petDao.getFavoritePetsByType(PetType.CAT) } returns flowOf(mockFavorites)
            coEvery { petDao.refreshPetsForFirstPage(any(), PetType.CAT) } just Runs

            // When
            val result = petRepository.refreshPets(PetType.CAT, 0)

            // Then
            assert(result is NetworkResult.Success)
            coVerify { catApiService.getBreeds(20, 0) }
            coVerify { petDao.refreshPetsForFirstPage(any(), PetType.CAT) }
        }

    @Test
    fun `GIVEN successful API response WHEN refreshPets is called with search query THEN searches and replaces pets`() =
        runTest {
            // Given
            val searchQuery = "Persian"
            val mockApiResponse =
                listOf(
                    CatBreedDto(
                        "1",
                        "Persian",
                        "Iran",
                        "Calm",
                        "Description",
                        "10-15",
                        ImageDto("image.jpg"),
                    ),
                )
            val mockFavorites = emptyList<PetEntity>()

            coEvery { catApiService.searchBreeds(searchQuery) } returns mockApiResponse
            coEvery { petDao.getFavoritePetsByType(PetType.CAT) } returns flowOf(mockFavorites)
            coEvery { petDao.refreshPetsForFirstPage(any(), PetType.CAT) } just Runs

            // When
            val result = petRepository.refreshPets(PetType.CAT, 0, searchQuery)

            // Then
            assert(result is NetworkResult.Success)
            coVerify { catApiService.searchBreeds(searchQuery) }
            coVerify { petDao.refreshPetsForFirstPage(any(), PetType.CAT) }
        }

    @Test
    fun `GIVEN API error WHEN refreshPets is called THEN returns error result`() =
        runTest {
            // Given
            val errorMessage = "Network error"
            coEvery { catApiService.getBreeds(any(), any()) } throws Exception(errorMessage)

            // When
            val result = petRepository.refreshPets(PetType.CAT, 0)

            // Then
            assert(result is NetworkResult.Error)
            assert((result as NetworkResult.Error).message == errorMessage)
        }

    @Test
    fun `GIVEN pet exists WHEN toggleFavorite is called THEN updates favorite status in database`() =
        runTest {
            // Given
            val petId = "test-pet-id"
            val mockPet = createMockPetEntity(petId, "Persian", PetType.CAT, isFavorite = false)

            coEvery { petDao.getPetById(petId) } returns mockPet
            coEvery { petDao.updateFavoriteStatus(petId, true) } just Runs

            // When
            petRepository.toggleFavorite(petId)

            // Then
            coVerify { petDao.getPetById(petId) }
            coVerify { petDao.updateFavoriteStatus(petId, true) }
        }

    @Test
    fun `GIVEN pet does not exist WHEN toggleFavorite is called THEN does not update database`() =
        runTest {
            // Given
            val petId = "non-existent-pet"
            coEvery { petDao.getPetById(petId) } returns null

            // When
            petRepository.toggleFavorite(petId)

            // Then
            coVerify { petDao.getPetById(petId) }
            coVerify(exactly = 0) { petDao.updateFavoriteStatus(any(), any()) }
        }

    @Test
    fun `GIVEN pet exists WHEN getPetDetails is called THEN returns pet from database`() =
        runTest {
            // Given
            val petId = "test-pet-id"
            val mockPetEntity = createMockPetEntity(petId, "Persian", PetType.CAT)
            coEvery { petDao.getPetById(petId) } returns mockPetEntity

            // When
            val result = petRepository.getPetDetails(petId)

            // Then
            assert(result != null)
            assert(result?.id == petId)
            assert(result?.name == "Persian")
            coVerify { petDao.getPetById(petId) }
        }

    @Test
    fun `GIVEN subsequent page request WHEN refreshPets is called THEN appends pets to existing data`() =
        runTest {
            // Given
            val mockApiResponse =
                listOf(
                    CatBreedDto(
                        "3",
                        "Siamese",
                        "Thailand",
                        "Active",
                        "Description",
                        "12-18",
                        ImageDto("image3.jpg"),
                    ),
                )
            val mockExistingPets =
                listOf(
                    createMockPetEntity("1", "Persian", PetType.CAT, isFavorite = true),
                )

            coEvery { catApiService.getBreeds(20, 1) } returns mockApiResponse
            coEvery { petDao.getPetsByType(PetType.CAT) } returns flowOf(mockExistingPets)
            coEvery { petDao.appendPets(any(), PetType.CAT) } just Runs

            // When
            val result = petRepository.refreshPets(PetType.CAT, 1) // Page 1 (not first page)

            // Then
            assert(result is NetworkResult.Success)
            coVerify { catApiService.getBreeds(20, 1) }
            coVerify { petDao.appendPets(any(), PetType.CAT) }
        }

    @Test
    fun `GIVEN cached images exist WHEN getPetImages is called THEN returns cached images without API call`() =
        runTest {
            // Given
            val petId = "test-pet-id"
            val cachedImages = listOf("cached1.jpg", "cached2.jpg", "cached3.jpg")
            val mockPetEntity =
                createMockPetEntity(
                    petId,
                    "Persian",
                    PetType.CAT,
                    additionalImages = cachedImages,
                )

            coEvery { petDao.getPetById(petId) } returns mockPetEntity

            // When
            val result = petRepository.getPetImages(petId, PetType.CAT)

            // Then
            assert(result == cachedImages) {
                "Expected cached images $cachedImages but got $result"
            }
            coVerify { petDao.getPetById(petId) }
            coVerify(exactly = 0) { catApiService.getBreedImages(any(), any()) }
            coVerify(exactly = 0) { dogApiService.getBreedImages(any(), any()) }
        }

    @Test
    fun `GIVEN no cached images for cat WHEN getPetImages is called THEN fetches from cat API and updates cache`() =
        runTest {
            // Given
            val petId = "test-cat-id"
            val apiImages =
                listOf(
                    ImageResponse("1", "cat-image1.jpg", 400, 300),
                    ImageResponse("2", "cat-image2.jpg", 400, 300),
                    ImageResponse("3", "cat-image3.jpg", 400, 300),
                )
            val expectedUrls = listOf("cat-image1.jpg", "cat-image2.jpg", "cat-image3.jpg")
            val mockPetEntity =
                createMockPetEntity(
                    petId,
                    "Persian",
                    PetType.CAT,
                    additionalImages = emptyList(),
                )

            coEvery { petDao.getPetById(petId) } returns mockPetEntity
            coEvery { catApiService.getBreedImages(petId, 5) } returns apiImages
            coEvery { petDao.insertPet(any()) } just Runs

            // When
            val result = petRepository.getPetImages(petId, PetType.CAT)

            // Then
            assert(result == expectedUrls) {
                "Expected $expectedUrls but got $result"
            }
            coVerify { petDao.getPetById(petId) }
            coVerify { catApiService.getBreedImages(petId, 5) }
            coVerify {
                petDao.insertPet(
                    match {
                        it.id == petId &&
                            it.additionalImages == expectedUrls
                    },
                )
            }
        }

    @Test
    fun `GIVEN no cached images for dog WHEN getPetImages is called THEN fetches from dog API and updates cache`() =
        runTest {
            // Given
            val petId = "test-dog-id"
            val apiImages =
                listOf(
                    ImageResponse("1", "dog-image1.jpg", 400, 300),
                    ImageResponse("2", "dog-image2.jpg", 400, 300),
                )
            val expectedUrls = listOf("dog-image1.jpg", "dog-image2.jpg")
            val mockPetEntity =
                createMockPetEntity(
                    petId,
                    "Golden Retriever",
                    PetType.DOG,
                    additionalImages = emptyList(),
                )

            coEvery { petDao.getPetById(petId) } returns mockPetEntity
            coEvery { dogApiService.getBreedImages(petId, 5) } returns apiImages
            coEvery { petDao.insertPet(any()) } just Runs

            // When
            val result = petRepository.getPetImages(petId, PetType.DOG)

            // Then
            assert(result == expectedUrls) {
                "Expected $expectedUrls but got $result"
            }
            coVerify { petDao.getPetById(petId) }
            coVerify { dogApiService.getBreedImages(petId, 5) }
            coVerify {
                petDao.insertPet(
                    match {
                        it.id == petId &&
                            it.additionalImages == expectedUrls
                    },
                )
            }
        }

    @Test
    fun `GIVEN API error without cached data WHEN getPetImages is called THEN returns empty list`() =
        runTest {
            // Given
            val petId = "test-pet-id"
            val mockPetEntity =
                createMockPetEntity(
                    petId,
                    "Persian",
                    PetType.CAT,
                    additionalImages = emptyList(),
                )

            coEvery { petDao.getPetById(petId) } returns mockPetEntity
            coEvery { catApiService.getBreedImages(petId, 5) } throws Exception("Network error")

            // When
            val result = petRepository.getPetImages(petId, PetType.CAT)

            // Then
            assert(result.isEmpty()) {
                "Expected empty list on error without cache but got $result"
            }
            coVerify { catApiService.getBreedImages(petId, 5) }
        }

    @Test
    fun `GIVEN pet not found in database WHEN getPetImages is called THEN fetches from API without caching`() =
        runTest {
            // Given
            val petId = "non-existent-pet"
            val apiImages =
                listOf(
                    ImageResponse("1", "image1.jpg", 400, 300),
                    ImageResponse("2", "image2.jpg", 400, 300),
                )
            val expectedUrls = listOf("image1.jpg", "image2.jpg")

            coEvery { petDao.getPetById(petId) } returns null
            coEvery { catApiService.getBreedImages(petId, 5) } returns apiImages

            // When
            val result = petRepository.getPetImages(petId, PetType.CAT)

            // Then
            assert(result == expectedUrls) {
                "Expected $expectedUrls but got $result"
            }
            coVerify { petDao.getPetById(petId) }
            coVerify { catApiService.getBreedImages(petId, 5) }
            coVerify(exactly = 0) { petDao.insertPet(any()) }
        }

    @Test
    fun `GIVEN pet not in database and API error WHEN getPetImages is called THEN returns empty list`() =
        runTest {
            // Given
            val petId = "non-existent-pet"

            coEvery { petDao.getPetById(petId) } returns null
            coEvery { catApiService.getBreedImages(petId, 5) } throws Exception("Network error")

            // When
            val result = petRepository.getPetImages(petId, PetType.CAT)

            // Then
            assert(result.isEmpty()) {
                "Expected empty list when pet not found and API fails but got $result"
            }
            coVerify { petDao.getPetById(petId) }
            coVerify { catApiService.getBreedImages(petId, 5) }
        }

    private fun createMockPetEntity(
        id: String,
        name: String,
        petType: PetType,
        isFavorite: Boolean = false,
        additionalImages: List<String> = emptyList(),
    ): PetEntity =
        PetEntity(
            id = id,
            name = name,
            origin = "Test Origin",
            temperament = "Friendly, Playful",
            description = "Test description",
            lifeSpan = "10 - 15",
            imageUrl = "https://test.com/image.jpg",
            additionalImages = additionalImages,
            isFavorite = isFavorite,
            petType = petType,
        )
}
