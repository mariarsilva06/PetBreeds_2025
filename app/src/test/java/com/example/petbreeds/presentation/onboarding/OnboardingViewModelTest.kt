package com.example.petbreeds.presentation.onboarding

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.model.PetType
import com.example.petbreeds.utils.PreferencesManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class OnboardingViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var onboardingViewModel: OnboardingViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        preferencesManager = mockk(relaxed = true)
        onboardingViewModel = OnboardingViewModel(preferencesManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `GIVEN cat pet type WHEN selectPetType is called THEN saves cat preference`() = runTest {
        // Given
        coEvery { preferencesManager.savePetType(PetType.CAT) } just Runs

        // When
        onboardingViewModel.selectPetType(PetType.CAT)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.savePetType(PetType.CAT) }
    }

    @Test
    fun `GIVEN dog pet type WHEN selectPetType is called THEN saves dog preference`() = runTest {
        // Given
        coEvery { preferencesManager.savePetType(PetType.DOG) } just Runs

        // When
        onboardingViewModel.selectPetType(PetType.DOG)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.savePetType(PetType.DOG) }
    }

    @Test
    fun `GIVEN multiple pet type calls WHEN selectPetType is called multiple times THEN saves each preference`() = runTest {
        // Given
        coEvery { preferencesManager.savePetType(any()) } just Runs

        // When
        onboardingViewModel.selectPetType(PetType.CAT)
        onboardingViewModel.selectPetType(PetType.DOG)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.savePetType(PetType.CAT) }
        coVerify { preferencesManager.savePetType(PetType.DOG) }
    }
}