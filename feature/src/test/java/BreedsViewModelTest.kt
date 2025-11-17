import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.breeds.BreedsUiState
import com.example.breeds.BreedsViewModel
import com.example.common.NetworkResult
import com.example.domain.usecase.GetPetsUseCase
import com.example.domain.usecase.RefreshPetsUseCase
import com.example.domain.usecase.ToggleFavoriteUseCase
import com.example.model.Pet
import com.example.model.PetType
import com.example.preferences.PreferencesManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.*

@ExperimentalCoroutinesApi
class BreedsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var getPetsUseCase: GetPetsUseCase
    private lateinit var refreshPetsUseCase: RefreshPetsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var breedsViewModel: BreedsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getPetsUseCase = mockk(relaxed = true)
        refreshPetsUseCase = mockk(relaxed = true)
        toggleFavoriteUseCase = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)

        // Mock PreferencesManager to return CAT immediately
        every { preferencesManager.petTypeFlow } returns flowOf(PetType.CAT)
        every { preferencesManager.setPetType(any()) } just Runs

        // Mock refresh use case to return success by default
        coEvery { refreshPetsUseCase(any(), any(), any()) } returns NetworkResult.Success(Unit)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN successful pets fetch WHEN viewmodel is initialized THEN updates pets state with success`() = runTest {
        // Given
        val mockPets = listOf(
            createMockPet("1", "Persian", PetType.CAT),
            createMockPet("2", "Maine Coon", PetType.CAT)
        )
        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Success(mockPets))

        // When - Initialize ViewModel and wait for all emissions
        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // Force collection to ensure all states are processed
        val job = launch {
            breedsViewModel.petsState.collect { state ->
                // This will collect Loading first, then Success
            }
        }

        advanceUntilIdle()
        job.cancel()

        // Then
        val currentState = breedsViewModel.petsState.value
        assert(currentState is BreedsUiState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }
        assert((currentState as BreedsUiState.Success).pets.size == 2) {
            "Expected 2 pets but got ${currentState.pets.size}"
        }
        verify { getPetsUseCase(PetType.CAT) }
    }

    @Test
    fun `GIVEN error during pets fetch WHEN viewmodel is initialized THEN updates pets state with error`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Error(errorMessage))

        // When
        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // Force collection
        val job = launch {
            breedsViewModel.petsState.collect { }
        }

        advanceUntilIdle()
        job.cancel()

        // Then
        val currentState = breedsViewModel.petsState.value
        assert(currentState is BreedsUiState.Error) {
            "Expected Error state but got ${currentState::class.simpleName}"
        }
        assert((currentState as BreedsUiState.Error).message == errorMessage) {
            "Expected error message '$errorMessage' but got '${currentState.message}'"
        }
    }

    @Test
    fun `GIVEN loading state WHEN pets are being fetched THEN updates pets state with loading`() = runTest {
        // Given
        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Loading())

        // When
        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch {
            breedsViewModel.petsState.collect { }
        }

        advanceUntilIdle()
        job.cancel()

        // Then
        val currentState = breedsViewModel.petsState.value
        assert(currentState is BreedsUiState.Loading) {
            "Expected Loading state but got ${currentState::class.simpleName}"
        }
    }

    @Test
    fun `GIVEN search query WHEN onSearchQueryChanged is called THEN updates search query and filters pets`() = runTest {
        // Given
        val mockPets = listOf(
            createMockPet("1", "Persian", PetType.CAT),
            createMockPet("2", "Maine Coon", PetType.CAT)
        )
        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Success(mockPets))

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // Wait for initial state
        val initJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        initJob.cancel()

        // When
        breedsViewModel.onSearchQueryChanged("Persian")

        val searchJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        searchJob.cancel()

        // Then
        assert(breedsViewModel.searchQuery.value == "Persian") {
            "Expected search query 'Persian' but got '${breedsViewModel.searchQuery.value}'"
        }

        val currentState = breedsViewModel.petsState.value
        assert(currentState is BreedsUiState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }

        val filteredPets = (currentState as BreedsUiState.Success).pets
        assert(filteredPets.size == 1) {
            "Expected 1 filtered pet but got ${filteredPets.size}"
        }
        assert(filteredPets.first().name == "Persian") {
            "Expected filtered pet name 'Persian' but got '${filteredPets.first().name}'"
        }
    }

    @Test
    fun `GIVEN empty search query WHEN onSearchQueryChanged is called THEN shows all pets`() = runTest {
        // Given
        val mockPets = listOf(
            createMockPet("1", "Persian", PetType.CAT),
            createMockPet("2", "Maine Coon", PetType.CAT)
        )
        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Success(mockPets))

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // Wait for initial state
        val initJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        initJob.cancel()

        // When
        breedsViewModel.onSearchQueryChanged("")

        val searchJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        searchJob.cancel()

        // Then
        assert(breedsViewModel.searchQuery.value == "") {
            "Expected empty search query but got '${breedsViewModel.searchQuery.value}'"
        }

        val currentState = breedsViewModel.petsState.value
        assert(currentState is BreedsUiState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }
        assert((currentState as BreedsUiState.Success).pets.size == 2) {
            "Expected 2 pets but got ${currentState.pets.size}"
        }
    }

    @Test
    fun `GIVEN pet id WHEN onToggleFavorite is called THEN calls toggle favorite use case`() = runTest {
        // Given
        val petId = "test-pet-id"
        coEvery { toggleFavoriteUseCase(petId) } just Runs

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // When
        breedsViewModel.onToggleFavorite(petId)
        advanceUntilIdle()

        // Then
        coVerify { toggleFavoriteUseCase(petId) }
    }

    @Test
    fun `GIVEN refresh request WHEN onRefresh is called THEN calls refresh use case and updates refreshing state`() = runTest {
        // Given
        coEvery { refreshPetsUseCase(PetType.CAT, 0, null) } returns NetworkResult.Success(Unit)

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        job.cancel()

        // When
        breedsViewModel.onRefresh()
        advanceUntilIdle()

        // Then
        coVerify { refreshPetsUseCase(PetType.CAT, 0, null) }
        assert(!breedsViewModel.isRefreshing.value) {
            "Expected refreshing to be false after completion but got ${breedsViewModel.isRefreshing.value}"
        }
    }

    @Test
    fun `GIVEN lifespan range WHEN onLifeSpanRangeChanged is called THEN updates lifespan range and filters pets`() = runTest {
        // Given
        val mockPets = listOf(
            createMockPet("1", "Persian", PetType.CAT, lifeSpan = "10 - 15"),
            createMockPet("2", "Maine Coon", PetType.CAT, lifeSpan = "20 - 25")
        )
        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Success(mockPets))

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // Wait for initial state
        val initJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        initJob.cancel()

        val newRange = 10f..15f

        // When
        breedsViewModel.onLifeSpanRangeChanged(newRange)

        val rangeJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        rangeJob.cancel()

        // Then
        assert(breedsViewModel.lifeSpanRange.value == newRange) {
            "Expected lifespan range $newRange but got ${breedsViewModel.lifeSpanRange.value}"
        }

        val currentState = breedsViewModel.petsState.value
        assert(currentState is BreedsUiState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }

        val filteredPets = (currentState as BreedsUiState.Success).pets
        assert(filteredPets.size == 1) {
            "Expected 1 filtered pet but got ${filteredPets.size}"
        }
        assert(filteredPets.first().name == "Persian") {
            "Expected filtered pet 'Persian' but got '${filteredPets.first().name}'"
        }
    }

    @Test
    fun `GIVEN new pet type WHEN setPetType is called THEN calls preferences manager to save pet type`() = runTest {
        // Given
        val newPetType = PetType.DOG
        every { preferencesManager.setPetType(newPetType) } just Runs

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        // When
        breedsViewModel.setPetType(newPetType)

        // Then
        verify { preferencesManager.setPetType(newPetType) }
    }

    @Test
    fun `GIVEN pagination request WHEN loadNextPage is called THEN calls refresh use case with next page`() = runTest {
        // Given
        coEvery { refreshPetsUseCase(PetType.CAT, 1, null) } returns NetworkResult.Success(Unit)

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        job.cancel()

        // When
        breedsViewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        coVerify { refreshPetsUseCase(PetType.CAT, 1, null) }
        assert(!breedsViewModel.isLoadingMore.value) {
            "Expected loading more to be false after completion but got ${breedsViewModel.isLoadingMore.value}"
        }
    }

    @Test
    fun `GIVEN loading more in progress WHEN loadNextPage is called THEN does not make additional request`() = runTest {
        // Given
        coEvery { refreshPetsUseCase(any(), any(), any()) } coAnswers {
            delay(1000) // Simulate long running operation
            NetworkResult.Success(Unit)
        }

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        job.cancel()

        // When
        breedsViewModel.loadNextPage()
        breedsViewModel.loadNextPage() // Second call while first is in progress
        advanceUntilIdle()

        // Then - Should only call once for pagination (plus once for initial load)
        coVerify(exactly = 2) { refreshPetsUseCase(any(), any(), any()) }
    }

    @Test
    fun `GIVEN search query is active WHEN onRefresh is called THEN calls refresh use case with search query`() = runTest {
        // Given
        val searchQuery = "Persian"
        coEvery { refreshPetsUseCase(PetType.CAT, 0, searchQuery) } returns NetworkResult.Success(Unit)

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        job.cancel()

        breedsViewModel.onSearchQueryChanged(searchQuery)
        advanceUntilIdle()

        // When
        breedsViewModel.onRefresh()
        advanceUntilIdle()

        // Then
        coVerify { refreshPetsUseCase(PetType.CAT, 0, searchQuery) }
    }

    @Test
    fun `GIVEN search query is active WHEN loadNextPage is called THEN does not paginate`() = runTest {
        // Given
        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        job.cancel()

        // Set search query to block pagination
        breedsViewModel.onSearchQueryChanged("Persian")
        advanceUntilIdle()

        // When
        breedsViewModel.loadNextPage()
        advanceUntilIdle()

        // Then - Should only have initial load, no pagination
        coVerify(exactly = 1) { refreshPetsUseCase(any(), any(), any()) }
    }

    @Test
    fun `GIVEN lifespan filter is active WHEN loadNextPage is called THEN does not paginate`() = runTest {
        // Given
        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        job.cancel()

        // Set lifespan filter to block pagination
        breedsViewModel.onLifeSpanRangeChanged(5f..10f)
        advanceUntilIdle()

        // When
        breedsViewModel.loadNextPage()
        advanceUntilIdle()

        // Then - Should only have initial load, no pagination
        coVerify(exactly = 1) { refreshPetsUseCase(any(), any(), any()) }
    }

    @Test
    fun `GIVEN pet type changes WHEN setPetType is called THEN triggers reload with new pet type`() = runTest {
        // Given
        val mockCatPets = listOf(createMockPet("1", "Persian", PetType.CAT))
        val mockDogPets = listOf(createMockPet("2", "Labrador", PetType.DOG))

        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Success(mockCatPets))
        every { getPetsUseCase(PetType.DOG) } returns flowOf(NetworkResult.Success(mockDogPets))

        // Use real MutableStateFlow to simulate pet type changes
        val petTypeFlow = MutableStateFlow(PetType.CAT)
        every { preferencesManager.petTypeFlow } returns petTypeFlow

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()

        // Verify initial state has cats
        val initialState = breedsViewModel.petsState.value as BreedsUiState.Success
        assert(initialState.pets.first().petType == PetType.CAT) {
            "Expected initial pet type to be CAT but got ${initialState.pets.first().petType}"
        }

        // Clear previous invocations to count only new calls
        clearMocks(refreshPetsUseCase, answers = false)
        coEvery { refreshPetsUseCase(any(), any(), any()) } returns NetworkResult.Success(Unit)

        // When - Change pet type
        breedsViewModel.setPetType(PetType.DOG)
        petTypeFlow.value = PetType.DOG // Emit new value to trigger collection
        advanceUntilIdle()

        job.cancel()

        // Then
        verify { preferencesManager.setPetType(PetType.DOG) }
        coVerify { refreshPetsUseCase(PetType.DOG, 0, null) }
    }

    @Test
    fun `GIVEN search and lifespan filters WHEN both are applied THEN filters by both criteria`() = runTest {
        // Given
        val mockPets = listOf(
            createMockPet("1", "Persian", PetType.CAT, lifeSpan = "10 - 15"),
            createMockPet("2", "Persian Longhair", PetType.CAT, lifeSpan = "20 - 25"),
            createMockPet("3", "Maine Coon", PetType.CAT, lifeSpan = "12 - 18")
        )
        every { getPetsUseCase(PetType.CAT) } returns flowOf(NetworkResult.Success(mockPets))

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val initJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        initJob.cancel()

        // When - Apply both filters
        breedsViewModel.onSearchQueryChanged("Persian")
        breedsViewModel.onLifeSpanRangeChanged(10f..15f)

        val filterJob = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        filterJob.cancel()

        // Then - Only "Persian" with lifespan 10-15 should match
        val currentState = breedsViewModel.petsState.value as BreedsUiState.Success
        assert(currentState.pets.size == 1) {
            "Expected 1 pet matching both filters but got ${currentState.pets.size}"
        }
        assert(currentState.pets.first().name == "Persian") {
            "Expected filtered pet 'Persian' but got '${currentState.pets.first().name}'"
        }
    }

    @Test
    fun `GIVEN pagination error WHEN loadNextPage is called THEN stops loading but maintains current page`() = runTest {
        // Given
        coEvery { refreshPetsUseCase(PetType.CAT, 1, null) } returns NetworkResult.Error("Pagination failed")

        breedsViewModel = BreedsViewModel(
            getPetsUseCase,
            refreshPetsUseCase,
            toggleFavoriteUseCase,
            preferencesManager
        )

        val job = launch { breedsViewModel.petsState.collect { } }
        advanceUntilIdle()
        job.cancel()

        // When
        breedsViewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        assert(!breedsViewModel.isLoadingMore.value) {
            "Expected loading more to be false after error"
        }
        coVerify { refreshPetsUseCase(PetType.CAT, 1, null) }
    }

    private fun createMockPet(
        id: String,
        name: String,
        petType: PetType,
        lifeSpan: String = "10 - 15",
        isFavorite: Boolean = false
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
            petType = petType
        )
    }
}