package com.example.petbreeds.domain.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.petbreeds.domain.model.Pet
import com.example.petbreeds.domain.model.PetType
import com.example.petbreeds.domain.repository.PetRepository
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
class GetFavoritePetsUseCaseTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var petRepository: PetRepository
    private lateinit var getFavoritePetsUseCase: GetFavoritePetsUseCase

    @Before
    fun setUp() {
        petRepository = mockk(relaxed = true)
        getFavoritePetsUseCase = GetFavoritePetsUseCase(petRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN no favorite pets WHEN invoke is called THEN returns empty state`() = runTest {
        // Given
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(emptyList())

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        assert(result is FavoritePetsState.Empty) {
            "Expected Empty state but got ${result::class.simpleName}"
        }
        verify { petRepository.getFavoritePets(PetType.CAT) }
    }

    @Test
    fun `GIVEN favorite pets exist WHEN invoke is called THEN returns success state with correct data`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "10 - 14"),
            createMockPet("2", "Maine Coon", lifeSpan = "12 - 16")
        )
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(favoritePets)

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        assert(result is FavoritePetsState.Success) {
            "Expected Success state but got ${result::class.simpleName}"
        }

        val successResult = result as FavoritePetsState.Success
        assert(successResult.pets.size == 2) {
            "Expected 2 pets but got ${successResult.pets.size}"
        }
        assert(successResult.favoritesCount == 2f) {
            "Expected favorites count 2.0 but got ${successResult.favoritesCount}"
        }
        assert(successResult.averageLifespan == 13f) {
            "Expected average lifespan 13.0 but got ${successResult.averageLifespan}"
        }
    }

    @Test
    fun `GIVEN pets with range lifespan WHEN invoke is called THEN calculates correct average from ranges`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "10 - 14"), // Average: 12
            createMockPet("2", "Maine Coon", lifeSpan = "12 - 16") // Average: 14
        )
        // Total average: (12 + 14) / 2 = 13
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(favoritePets)

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        val successResult = result as FavoritePetsState.Success
        assert(successResult.averageLifespan == 13f) {
            "Expected average lifespan 13.0 but got ${successResult.averageLifespan}"
        }
    }

    @Test
    fun `GIVEN pets with single number lifespan WHEN invoke is called THEN calculates correct average from numbers`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "12"),
            createMockPet("2", "Maine Coon", lifeSpan = "14")
        )
        // Average: (12 + 14) / 2 = 13
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(favoritePets)

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        val successResult = result as FavoritePetsState.Success
        assert(successResult.averageLifespan == 13f) {
            "Expected average lifespan 13.0 but got ${successResult.averageLifespan}"
        }
    }

    @Test
    fun `GIVEN pets with years suffix WHEN invoke is called THEN calculates correct average removing years suffix`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "12 years"),
            createMockPet("2", "Maine Coon", lifeSpan = "14 years")
        )
        // Average: (12 + 14) / 2 = 13
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(favoritePets)

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        val successResult = result as FavoritePetsState.Success
        assert(successResult.averageLifespan == 13f) {
            "Expected average lifespan 13.0 but got ${successResult.averageLifespan}"
        }
    }

    @Test
    fun `GIVEN pets with mixed lifespan formats WHEN invoke is called THEN calculates correct average from mixed formats`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "12"), // 12
            createMockPet("2", "Maine Coon", lifeSpan = "10 - 16"), // 13
            createMockPet("3", "Siamese", lifeSpan = "15 years") // 15
        )
        // Average: (12 + 13 + 15) / 3 = 40/3 = 13.33...
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(favoritePets)

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        val successResult = result as FavoritePetsState.Success
        val expectedAverage = 40f / 3f // 13.333...
        assert(kotlin.math.abs(successResult.averageLifespan - expectedAverage) < 0.01f) {
            "Expected average lifespan $expectedAverage but got ${successResult.averageLifespan}"
        }
    }

    @Test
    fun `GIVEN pets with invalid lifespan format WHEN invoke is called THEN ignores invalid values in calculation`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "12"), // Valid: 12
            createMockPet("2", "Maine Coon", lifeSpan = "unknown"), // Invalid: 0
            createMockPet("3", "Siamese", lifeSpan = "14") // Valid: 14
        )
        // Average: (12 + 0 + 14) / 3 = 26/3 = 8.67...
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(favoritePets)

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        val successResult = result as FavoritePetsState.Success
        val expectedAverage = 26f / 3f // 8.666...
        assert(kotlin.math.abs(successResult.averageLifespan - expectedAverage) < 0.01f) {
            "Expected average lifespan $expectedAverage but got ${successResult.averageLifespan}"
        }
    }

    @Test
    fun `GIVEN single favorite pet WHEN invoke is called THEN returns correct single pet data`() = runTest {
        // Given
        val singlePet = listOf(
            createMockPet("1", "Persian", lifeSpan = "10 - 14")
        )
        every { petRepository.getFavoritePets(PetType.CAT) } returns flowOf(singlePet)

        // When
        val result = getFavoritePetsUseCase(PetType.CAT).first()

        // Then
        val successResult = result as FavoritePetsState.Success
        assert(successResult.pets.size == 1) {
            "Expected 1 pet but got ${successResult.pets.size}"
        }
        assert(successResult.favoritesCount == 1f) {
            "Expected favorites count 1.0 but got ${successResult.favoritesCount}"
        }
        assert(successResult.averageLifespan == 12f) {
            "Expected average lifespan 12.0 but got ${successResult.averageLifespan}"
        }
    }

    @Test
    fun `GIVEN dog pet type WHEN invoke is called THEN calls repository with correct pet type`() = runTest {
        // Given
        val dogPets = listOf(
            createMockPet("1", "Labrador", lifeSpan = "10 - 12", petType = PetType.DOG)
        )
        every { petRepository.getFavoritePets(PetType.DOG) } returns flowOf(dogPets)

        // When
        val result = getFavoritePetsUseCase(PetType.DOG).first()

        // Then
        assert(result is FavoritePetsState.Success) {
            "Expected Success state but got ${result::class.simpleName}"
        }
        verify { petRepository.getFavoritePets(PetType.DOG) }
    }

    private fun createMockPet(
        id: String,
        name: String,
        lifeSpan: String,
        petType: PetType = PetType.CAT
    ): Pet {
        return Pet(
            id = id,
            name = name,
            origin = "Test Origin",
            temperament = "Friendly, Playful",
            description = "Test description",
            lifeSpan = lifeSpan,
            imageUrl = "https://test.com/image.jpg",
            additionalImages = emptyList(),
            isFavorite = true,
            petType = petType
        )
    }
}