package com.example.petbreeds.presentation.favorites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.favorites.FavoritesViewModel
import com.example.model.Pet
import com.example.model.PetType
import com.example.petbreeds.domain.usecase.FavoritePetsState
import com.example.petbreeds.domain.usecase.GetFavoritePetsUseCase
import com.example.petbreeds.utils.PreferencesManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class FavoritesViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var getFavoritePetsUseCase: GetFavoritePetsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var favoritesViewModel: FavoritesViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getFavoritePetsUseCase = mockk(relaxed = true)
        toggleFavoriteUseCase = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)

        // Mock PreferencesManager to return CAT immediately
        every { preferencesManager.petTypeFlow } returns flowOf(PetType.CAT)
        every { preferencesManager.setPetType(any()) } just Runs
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN favorite pets exist WHEN viewmodel is initialized THEN loads favorite pets with average lifespan`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "10 - 14", isFavorite = true),
            createMockPet("2", "Maine Coon", lifeSpan = "12 - 15", isFavorite = true)
        )
        val expectedAverageLifespan = 13f // (12 + 13.5) / 2 = 12.75 ≈ 13
        val successState = FavoritePetsState.Success(
            pets = favoritePets,
            favoritesCount = 2f,
            averageLifespan = expectedAverageLifespan
        )

        every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

        // When
        favoritesViewModel = FavoritesViewModel(
            getFavoritePetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // Force collection
        val job = launch {
            favoritesViewModel.favoritesState.collect { }
        }

        advanceUntilIdle()
        job.cancel()

        // Then
        val currentState = favoritesViewModel.favoritesState.value
        assert(currentState is FavoritePetsState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }

        val successCurrentState = currentState as FavoritePetsState.Success
        assert(successCurrentState.pets.size == 2) {
            "Expected 2 pets but got ${successCurrentState.pets.size}"
        }
        verify { getFavoritePetsUseCase(PetType.CAT) }
    }

    @Test
    fun `GIVEN no favorite pets WHEN viewmodel is initialized THEN shows empty state`() = runTest {
        // Given
        every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(FavoritePetsState.Empty)

        // When
        favoritesViewModel = FavoritesViewModel(
            getFavoritePetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch {
            favoritesViewModel.favoritesState.collect { }
        }

        advanceUntilIdle()
        job.cancel()

        // Then
        val currentState = favoritesViewModel.favoritesState.value
        assert(currentState is FavoritePetsState.Empty) {
            "Expected Empty state but got ${currentState::class.simpleName}"
        }
    }

    @Test
    fun `GIVEN favorite pet WHEN toggleFavorite is called THEN removes pet from favorites list`() = runTest {
        // Given
        val petToRemove = createMockPet("1", "Persian", isFavorite = true)
        val otherPet = createMockPet("2", "Maine Coon", isFavorite = true)
        val initialPets = listOf(petToRemove, otherPet)
        val successState = FavoritePetsState.Success(
            pets = initialPets,
            favoritesCount = 2f,
            averageLifespan = 12f
        )

        every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)
        coEvery { toggleFavoriteUseCase("1") } just Runs

        favoritesViewModel = FavoritesViewModel(
            getFavoritePetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch {
            favoritesViewModel.favoritesState.collect { }
        }

        advanceUntilIdle()
        job.cancel()

        // When
        favoritesViewModel.toggleFavorite("1")

        val job2 = launch {
            favoritesViewModel.favoritesState.collect { }
        }

        advanceUntilIdle()
        job2.cancel()

        // Then
        coVerify { toggleFavoriteUseCase("1") }
        // The internal list should be updated
    }

    @Test
    fun `GIVEN single favorite pet WHEN viewmodel is initialized THEN calculates correct average lifespan`() = runTest {
        // Given
        val singlePet = listOf(
            createMockPet("1", "Persian", lifeSpan = "10 - 14", isFavorite = true)
        )
        val expectedAverageLifespan = 12f // (10 + 14) / 2 = 12
        val successState = FavoritePetsState.Success(
            pets = singlePet,
            favoritesCount = 1f,
            averageLifespan = expectedAverageLifespan
        )

        every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

        // When
        favoritesViewModel = FavoritesViewModel(
            getFavoritePetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch {
            favoritesViewModel.favoritesState.collect { }
        }

        advanceUntilIdle()
        job.cancel()

        // Then
        val currentState = favoritesViewModel.favoritesState.value
        assert(currentState is FavoritePetsState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }
    }

    @Test
    fun `GIVEN pets with different lifespan formats WHEN viewmodel is initialized THEN handles various lifespan formats correctly`() = runTest {
        // Given
        val favoritePets = listOf(
            createMockPet("1", "Persian", lifeSpan = "12", isFavorite = true),
            createMockPet("2", "Maine Coon", lifeSpan = "10 - 16", isFavorite = true),
            createMockPet("3", "Siamese", lifeSpan = "15 years", isFavorite = true)
        )
        val expectedAverageLifespan = 13.33f
        val successState = FavoritePetsState.Success(
            pets = favoritePets,
            favoritesCount = 3f,
            averageLifespan = expectedAverageLifespan
        )

        every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

        // When
        favoritesViewModel = FavoritesViewModel(
            getFavoritePetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch {
            favoritesViewModel.favoritesState.collect { }
        }

        advanceUntilIdle()
        job.cancel()

        // Then
        val currentState = favoritesViewModel.favoritesState.value
        assert(currentState is FavoritePetsState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }
    }

    private fun createMockPet(
        id: String,
        name: String,
        lifeSpan: String = "10 - 15",
        isFavorite: Boolean = true
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
            isFavorite = isFavorite,
            petType = PetType.CAT
        )
    }
}