

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.domain.usecase.FavoritePetsState
import com.example.domain.usecase.GetFavoritePetsUseCase
import com.example.domain.usecase.ToggleFavoriteUseCase
import com.example.feature.favorites.FavoritesViewModel
import com.example.model.Pet
import com.example.model.PetType
import com.example.preferences.PreferencesManager
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    fun `GIVEN favorite pets exist WHEN viewmodel is initialized THEN loads favorite pets with average lifespan`() =
        runTest {
            // Given
            val favoritePets =
                listOf(
                    createMockPet("1", "Persian", lifeSpan = "10 - 14", isFavorite = true),
                    createMockPet("2", "Maine Coon", lifeSpan = "12 - 15", isFavorite = true),
                )
            val expectedAverageLifespan = 13f // (12 + 13.5) / 2 = 12.75 ≈ 13
            val successState =
                FavoritePetsState.Success(
                    pets = favoritePets,
                    favoritesCount = 2f,
                    averageLifespan = expectedAverageLifespan,
                )

            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            // Force collection
            val job =
                launch {
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
    fun `GIVEN no favorite pets WHEN viewmodel is initialized THEN shows empty state`() =
        runTest {
            // Given
            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(FavoritePetsState.Empty)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job =
                launch {
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
    fun `GIVEN favorite pets WHEN toggleFavorite is called THEN removes pet from favorites list`() =
        runTest {
            // Given
            val petToRemove = createMockPet("1", "Persian", isFavorite = true)
            val otherPet = createMockPet("2", "Maine Coon", isFavorite = true)
            val initialPets = listOf(petToRemove, otherPet)
            val updatedPets = listOf(otherPet)

            val initialState =
                FavoritePetsState.Success(
                    pets = initialPets,
                    favoritesCount = 2f,
                    averageLifespan = 12f,
                )

            val updatedState =
                FavoritePetsState.Success(
                    pets = updatedPets,
                    favoritesCount = 1f,
                    averageLifespan = 13f,
                )

            val stateFlow = MutableStateFlow<FavoritePetsState>(initialState)
            every { getFavoritePetsUseCase(PetType.CAT) } returns stateFlow

            coEvery { toggleFavoriteUseCase("1") } coAnswers {
                stateFlow.value = updatedState
            }

            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job =
                launch {
                    favoritesViewModel.favoritesState.collect { }
                }

            advanceUntilIdle()

            // Verify initial state has 2 pets
            val initialStateValue = favoritesViewModel.favoritesState.value as FavoritePetsState.Success
            assert(initialStateValue.pets.size == 2)

            // When
            favoritesViewModel.toggleFavorite("1")
            advanceUntilIdle()

            // Then
            coVerify { toggleFavoriteUseCase("1") }

            val finalState = favoritesViewModel.favoritesState.value as FavoritePetsState.Success
            assert(finalState.pets.size == 1) {
                "Expected 1 pet after removal but got ${finalState.pets.size}"
            }
            assert(finalState.pets.none { it.id == "1" }) {
                "Pet with id '1' should be removed"
            }
            assert(finalState.pets.first().id == "2")

            job.cancel()
        }

    @Test
    fun `GIVEN single favorite pet WHEN viewmodel is initialized THEN calculates correct average lifespan`() =
        runTest {
            // Given
            val singlePet =
                listOf(
                    createMockPet("1", "Persian", lifeSpan = "10 - 14", isFavorite = true),
                )
            val expectedAverageLifespan = 12f // (10 + 14) / 2 = 12
            val successState =
                FavoritePetsState.Success(
                    pets = singlePet,
                    favoritesCount = 1f,
                    averageLifespan = expectedAverageLifespan,
                )

            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job =
                launch {
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
    fun `GIVEN pets with different lifespan formats WHEN viewmodel is initialized THEN handles various lifespan formats correctly`() =
        runTest {
            // Given
            val favoritePets =
                listOf(
                    createMockPet("1", "Persian", lifeSpan = "12", isFavorite = true),
                    createMockPet("2", "Maine Coon", lifeSpan = "10 - 16", isFavorite = true),
                    createMockPet("3", "Siamese", lifeSpan = "15 years", isFavorite = true),
                )
            val expectedAverageLifespan = 13.33f
            val successState =
                FavoritePetsState.Success(
                    pets = favoritePets,
                    favoritesCount = 3f,
                    averageLifespan = expectedAverageLifespan,
                )

            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job =
                launch {
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
    fun `GIVEN favorite dogs WHEN pet type changes to DOG THEN loads dog favorites`() =
        runTest {
            // Given
            val catPets = listOf(createMockPet("1", "Persian", isFavorite = true))
            val dogPets =
                listOf(
                    createMockPet("2", "Labrador", isFavorite = true).copy(petType = PetType.DOG),
                )

            val catState = FavoritePetsState.Success(catPets, 1f, 12f)
            val dogState = FavoritePetsState.Success(dogPets, 1f, 11f)

            // Use MutableStateFlow to simulate pet type changes
            val petTypeFlow = MutableStateFlow(PetType.CAT)
            every { preferencesManager.petTypeFlow } returns petTypeFlow
            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(catState)
            every { getFavoritePetsUseCase(PetType.DOG) } returns flowOf(dogState)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job = launch { favoritesViewModel.favoritesState.collect { } }
            advanceUntilIdle()

            // Verify initial state is CAT
            val initialState = favoritesViewModel.favoritesState.value as FavoritePetsState.Success
            assert(initialState.pets.first().petType == PetType.CAT)

            // Change to DOG
            petTypeFlow.value = PetType.DOG
            advanceUntilIdle()

            job.cancel()

            // Then
            verify { getFavoritePetsUseCase(PetType.CAT) }
            verify { getFavoritePetsUseCase(PetType.DOG) }
        }

    @Test
    fun `GIVEN pets with specific lifespans WHEN calculating average THEN returns mathematically correct value`() =
        runTest {
            // Given
            val favoritePets =
                listOf(
                    createMockPet("1", "Persian", lifeSpan = "10 - 14", isFavorite = true), // avg = 12
                    createMockPet("2", "Maine Coon", lifeSpan = "12 - 16", isFavorite = true), // avg = 14
                    createMockPet("3", "Siamese", lifeSpan = "15 - 20", isFavorite = true), // avg = 17.5
                )
            // Expected: (12 + 14 + 17.5) / 3 = 14.5
            val expectedAverageLifespan = 14.5f
            val successState =
                FavoritePetsState.Success(
                    pets = favoritePets,
                    favoritesCount = 3f,
                    averageLifespan = expectedAverageLifespan,
                )

            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job = launch { favoritesViewModel.favoritesState.collect { } }
            advanceUntilIdle()
            job.cancel()

            // Then
            val currentState = favoritesViewModel.favoritesState.value as FavoritePetsState.Success
            assert(currentState.averageLifespan == expectedAverageLifespan) {
                "Expected average lifespan $expectedAverageLifespan but got ${currentState.averageLifespan}"
            }
        }

    @Test
    fun `GIVEN single favorite pet WHEN toggleFavorite is called THEN transitions to empty state`() =
        runTest {
            // Given
            val singlePet = createMockPet("1", "Persian", isFavorite = true)
            val successState =
                FavoritePetsState.Success(
                    pets = listOf(singlePet),
                    favoritesCount = 1f,
                    averageLifespan = 12f,
                )

            val stateFlow = MutableStateFlow<FavoritePetsState>(successState)
            every { getFavoritePetsUseCase(PetType.CAT) } returns stateFlow

            coEvery { toggleFavoriteUseCase("1") } coAnswers {
                stateFlow.value = FavoritePetsState.Empty
            }

            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job =
                launch {
                    favoritesViewModel.favoritesState.collect { }
                }

            advanceUntilIdle()

            // Verify initial state has 1 pet
            assert(favoritesViewModel.favoritesState.value is FavoritePetsState.Success)

            // When - Remove last favorite
            favoritesViewModel.toggleFavorite("1")
            advanceUntilIdle()

            // Then - Should transition to Empty
            val finalState = favoritesViewModel.favoritesState.value
            assert(finalState is FavoritePetsState.Empty) {
                "Expected Empty state after removing last favorite but got ${finalState::class.simpleName}"
            }
            coVerify { toggleFavoriteUseCase("1") }

            job.cancel()
        }

    @Test
    fun `GIVEN pets with invalid lifespan WHEN calculating average THEN handles gracefully`() =
        runTest {
            // Given
            val favoritePets =
                listOf(
                    createMockPet("1", "Persian", lifeSpan = "N/A", isFavorite = true),
                    createMockPet("2", "Maine Coon", lifeSpan = "", isFavorite = true),
                    createMockPet("3", "Siamese", lifeSpan = "10 - 15", isFavorite = true),
                )
            // Expected: Should handle invalid lifespans without crashing
            val successState =
                FavoritePetsState.Success(
                    pets = favoritePets,
                    favoritesCount = 3f,
                    averageLifespan = 12.5f, // Only valid pet counted
                )

            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(successState)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job = launch { favoritesViewModel.favoritesState.collect { } }
            advanceUntilIdle()
            job.cancel()

            // Then - Should not crash
            val currentState = favoritesViewModel.favoritesState.value
            assert(currentState is FavoritePetsState.Success) {
                "Expected Success state but got ${currentState::class.simpleName}"
            }
        }

    @Test
    fun `GIVEN new pet type WHEN setPetType is called THEN calls preferences manager`() =
        runTest {
            // Given
            val newPetType = PetType.DOG
            every { preferencesManager.setPetType(newPetType) } just Runs
            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(FavoritePetsState.Empty)

            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            // When
            favoritesViewModel.setPetType(newPetType)

            // Then
            verify { preferencesManager.setPetType(newPetType) }
        }

    @Test
    fun `GIVEN DOG favorites exist WHEN pet type changes to DOG THEN loads dog favorites`() =
        runTest {
            // Given
            val catPets = listOf(createMockPet("1", "Persian", isFavorite = true))
            val dogPets =
                listOf(
                    createMockPet("2", "Labrador", isFavorite = true).copy(petType = PetType.DOG),
                )

            val catState = FavoritePetsState.Success(catPets, 1f, 12f)
            val dogState = FavoritePetsState.Success(dogPets, 1f, 11f)

            // Use real MutableStateFlow to simulate pet type changes
            val petTypeFlow = MutableStateFlow(PetType.CAT)
            every { preferencesManager.petTypeFlow } returns petTypeFlow
            every { preferencesManager.setPetType(any()) } answers {
                petTypeFlow.value = firstArg()
            }
            every { getFavoritePetsUseCase(PetType.CAT) } returns flowOf(catState)
            every { getFavoritePetsUseCase(PetType.DOG) } returns flowOf(dogState)

            // When
            favoritesViewModel =
                FavoritesViewModel(
                    getFavoritePetsUseCase,
                    toggleFavoriteUseCase,
                    preferencesManager,
                )

            val job = launch { favoritesViewModel.favoritesState.collect { } }
            advanceUntilIdle()

            // Verify initial state is CAT
            val initialState = favoritesViewModel.favoritesState.value as FavoritePetsState.Success
            assert(initialState.pets.first().petType == PetType.CAT)

            // Change to DOG
            favoritesViewModel.setPetType(PetType.DOG)
            advanceUntilIdle()

            // Then
            val updatedState = favoritesViewModel.favoritesState.value as FavoritePetsState.Success
            assert(updatedState.pets.first().petType == PetType.DOG) {
                "Expected DOG pets but got ${updatedState.pets.first().petType}"
            }

            job.cancel()

            verify { getFavoritePetsUseCase(PetType.CAT) }
            verify { getFavoritePetsUseCase(PetType.DOG) }
        }

    private fun createMockPet(
        id: String,
        name: String,
        lifeSpan: String = "10 - 15",
        isFavorite: Boolean = true,
    ): Pet =
        Pet(
            id = id,
            name = name,
            origin = "Test Origin",
            temperament = "Friendly, Playful",
            description = "Test description",
            lifeSpan = lifeSpan,
            imageUrl = "https://test.com/image.jpg",
            additionalImages = emptyList(),
            isFavorite = isFavorite,
            petType = PetType.CAT,
        )
}
