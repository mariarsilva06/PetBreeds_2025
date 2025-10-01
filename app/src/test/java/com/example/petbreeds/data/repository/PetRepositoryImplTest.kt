package com.example.petbreeds.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.petbreeds.core.data.NetworkResult
import com.example.petbreeds.data.api.dto.CatBreedDto
import com.example.petbreeds.data.api.dto.ImageDto
import com.example.petbreeds.data.api.service.CatApiService
import com.example.petbreeds.data.api.service.DogApiService
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.data.local.entity.PetEntity
import com.example.model.PetType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
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
    fun `GIVEN pets in database WHEN getPets is called THEN returns pets from local storage`() = runTest {
        // Given
        val mockPetEntities = listOf(
            createMockPetEntity("1", "Persian", PetType.CAT),
            createMockPetEntity("2", "Maine Coon", PetType.CAT)
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
    fun `GIVEN favorite pets in database WHEN getFavoritePets is called THEN returns only favorite pets`() = runTest {
        // Given
        val mockFavoritePetEntities = listOf(
            createMockPetEntity("1", "Persian", PetType.CAT, isFavorite = true)
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
    fun `GIVEN successful API response WHEN refreshPets is called for first page THEN replaces all pets in database`() = runTest {
        // Given
        val mockApiResponse = listOf(
            CatBreedDto("1", "Persian", "Iran", "Calm", "Description", "10-15", ImageDto("image.jpg"))
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
    fun `GIVEN successful API response WHEN refreshPets is called with search query THEN searches and replaces pets`() = runTest {
        // Given
        val searchQuery = "Persian"
        val mockApiResponse = listOf(
            CatBreedDto("1", "Persian", "Iran", "Calm", "Description", "10-15", ImageDto("image.jpg"))
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
    fun `GIVEN API error WHEN refreshPets is called THEN returns error result`() = runTest {
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
    fun `GIVEN pet exists WHEN toggleFavorite is called THEN updates favorite status in database`() = runTest {
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
    fun `GIVEN pet does not exist WHEN toggleFavorite is called THEN does not update database`() = runTest {
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
    fun `GIVEN pet exists WHEN getPetDetails is called THEN returns pet from database`() = runTest {
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
    fun `GIVEN subsequent page request WHEN refreshPets is called THEN appends pets to existing data`() = runTest {
        // Given
        val mockApiResponse = listOf(
            CatBreedDto("3", "Siamese", "Thailand", "Active", "Description", "12-18", ImageDto("image3.jpg"))
        )
        val mockExistingPets = listOf(
            createMockPetEntity("1", "Persian", PetType.CAT, isFavorite = true)
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

    private fun createMockPetEntity(
        id: String,
        name: String,
        petType: PetType,
        isFavorite: Boolean = false
    ): PetEntity {
        return PetEntity(
            id = id,
            name = name,
            origin = "Test Origin",
            temperament = "Friendly, Playful",
            description = "Test description",
            lifeSpan = "10 - 15",
            imageUrl = "https://test.com/image.jpg",
            additionalImages = emptyList(),
            isFavorite = isFavorite,
            petType = petType
        )
    }
}