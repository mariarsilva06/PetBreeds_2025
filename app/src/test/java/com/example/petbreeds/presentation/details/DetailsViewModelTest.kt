package com.example.petbreeds.presentation.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.model.Pet
import com.example.model.PetType
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class DetailsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var getPetDetailsUseCase: GetPetDetailsUseCase
    private lateinit var getPetImagesUseCase: GetPetImagesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var detailsViewModel: DetailsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val testPetId = "test-pet-id"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getPetDetailsUseCase = mockk(relaxed = true)
        getPetImagesUseCase = mockk(relaxed = true)
        toggleFavoriteUseCase = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        // Mock SavedStateHandle to return test pet ID
        every { savedStateHandle.get<String>("petId") } returns testPetId
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN valid pet id WHEN viewmodel is initialized THEN loads pet details and images`() = runTest {
        // Given
        val mockPet = createMockPet(testPetId, "Persian", PetType.CAT)
        val mockImages = listOf("image1.jpg", "image2.jpg")

        coEvery { getPetDetailsUseCase(testPetId) } returns mockPet
        coEvery { getPetImagesUseCase(testPetId, PetType.CAT) } returns mockImages

        // When
        detailsViewModel = DetailsViewModel(
            getPetDetailsUseCase,
            getPetImagesUseCase,
            toggleFavoriteUseCase,
            savedStateHandle
        )

        // Force collection to process all states
        val job = launch {
            detailsViewModel.pet.collect { }
        }
        val imagesJob = launch {
            detailsViewModel.additionalImages.collect { }
        }

        advanceUntilIdle()
        job.cancel()
        imagesJob.cancel()

        // Then
        assert(detailsViewModel.pet.value == mockPet)
        assert(detailsViewModel.additionalImages.value == mockImages)
        assert(!detailsViewModel.isLoadingImages.value)
        coVerify { getPetDetailsUseCase(testPetId) }
        coVerify { getPetImagesUseCase(testPetId, PetType.CAT) }
    }

    @Test
    fun `GIVEN pet not found WHEN viewmodel is initialized THEN pet remains null`() = runTest {
        // Given
        coEvery { getPetDetailsUseCase(testPetId) } returns null

        // When
        detailsViewModel = DetailsViewModel(
            getPetDetailsUseCase,
            getPetImagesUseCase,
            toggleFavoriteUseCase,
            savedStateHandle
        )

        val job = launch { detailsViewModel.pet.collect { } }
        advanceUntilIdle()
        job.cancel()

        // Then
        assert(detailsViewModel.pet.value == null)
        coVerify { getPetDetailsUseCase(testPetId) }
        coVerify(exactly = 0) { getPetImagesUseCase(any(), any()) }
    }

    @Test
    fun `GIVEN error loading images WHEN viewmodel is initialized THEN falls back to cached images`() = runTest {
        // Given
        val cachedImages = listOf("cached1.jpg", "cached2.jpg")
        val mockPet = createMockPet(testPetId, "Persian", PetType.CAT, additionalImages = cachedImages)

        coEvery { getPetDetailsUseCase(testPetId) } returns mockPet
        coEvery { getPetImagesUseCase(testPetId, PetType.CAT) } throws Exception("Network error")

        // When
        detailsViewModel = DetailsViewModel(
            getPetDetailsUseCase,
            getPetImagesUseCase,
            toggleFavoriteUseCase,
            savedStateHandle
        )

        val job = launch { detailsViewModel.pet.collect { } }
        val imagesJob = launch { detailsViewModel.additionalImages.collect { } }
        advanceUntilIdle()
        job.cancel()
        imagesJob.cancel()

        // Then
        assert(detailsViewModel.pet.value == mockPet)
        assert(detailsViewModel.additionalImages.value == cachedImages)
        assert(!detailsViewModel.isLoadingImages.value)
    }

    @Test
    fun `GIVEN favorite pet WHEN onToggleFavorite is called THEN toggles favorite status and reloads pet`() = runTest {
        // Given
        val initialPet = createMockPet(testPetId, "Persian", PetType.CAT, isFavorite = true)
        val updatedPet = createMockPet(testPetId, "Persian", PetType.CAT, isFavorite = false)

        coEvery { getPetDetailsUseCase(testPetId) } returnsMany listOf(initialPet, updatedPet)
        coEvery { getPetImagesUseCase(testPetId, PetType.CAT) } returns emptyList()
        coEvery { toggleFavoriteUseCase(testPetId) } just Runs

        detailsViewModel = DetailsViewModel(
            getPetDetailsUseCase,
            getPetImagesUseCase,
            toggleFavoriteUseCase,
            savedStateHandle
        )

        val job = launch { detailsViewModel.pet.collect { } }
        advanceUntilIdle()
        job.cancel()

        // When
        detailsViewModel.onToggleFavorite()

        val job2 = launch { detailsViewModel.pet.collect { } }
        advanceUntilIdle()
        job2.cancel()

        // Then
        assert(detailsViewModel.pet.value?.isFavorite == false)
        coVerify { toggleFavoriteUseCase(testPetId) }
        coVerify(exactly = 2) { getPetDetailsUseCase(testPetId) }
    }

    @Test
    fun `GIVEN loading images WHEN images are being fetched THEN isLoadingImages is true during fetch`() = runTest {
        // Given
        val mockPet = createMockPet(testPetId, "Persian", PetType.CAT)
        val loadingStates = mutableListOf<Boolean>()

        coEvery { getPetDetailsUseCase(testPetId) } returns mockPet
        coEvery { getPetImagesUseCase(testPetId, PetType.CAT) } coAnswers {
            kotlinx.coroutines.delay(100) // Simulate loading
            emptyList()
        }

        // When
        detailsViewModel = DetailsViewModel(
            getPetDetailsUseCase,
            getPetImagesUseCase,
            toggleFavoriteUseCase,
            savedStateHandle
        )

        // Collect loading states
        val loadingJob = launch {
            detailsViewModel.isLoadingImages.collect { loading ->
                loadingStates.add(loading)
            }
        }

        val petJob = launch { detailsViewModel.pet.collect { } }

        // Let some time pass to capture the loading state
        advanceTimeBy(50) // Capture loading=true
        advanceUntilIdle() // Complete everything

        loadingJob.cancel()
        petJob.cancel()

        // Then - Should have seen both false (initial), true (during loading), false (after)
        assert(loadingStates.contains(true)) {
            "Expected to see loading=true at some point, but got states: $loadingStates"
        }
        assert(!detailsViewModel.isLoadingImages.value) {
            "Expected final loading state to be false"
        }
    }

    private fun createMockPet(
        id: String,
        name: String,
        petType: PetType,
        isFavorite: Boolean = false,
        additionalImages: List<String> = emptyList()
    ): Pet {
        return Pet(
            id = id,
            name = name,
            origin = "Test Origin",
            temperament = "Friendly, Playful",
            description = "Test description",
            lifeSpan = "10 - 15",
            imageUrl = "https://test.com/image.jpg",
            additionalImages = additionalImages,
            isFavorite = isFavorite,
            petType = petType
        )
    }
}
