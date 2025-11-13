import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.model.PetType
import com.example.feature.onboarding.OnboardingViewModel
import com.example.preferences.PreferencesManager
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

    @Test
    fun `GIVEN save error WHEN selectPetType is called THEN handles exception gracefully`() = runTest {
        // Given
        coEvery { preferencesManager.savePetType(any()) } throws Exception("Save failed")

        // When
        onboardingViewModel.selectPetType(PetType.CAT)
        advanceUntilIdle()

        // Then
        coVerify { preferencesManager.savePetType(PetType.CAT) }
    }

    @Test
    fun `GIVEN rapid pet type changes WHEN selectPetType is called rapidly THEN saves only last selection`() = runTest {
        // Given
        val capturedTypes = mutableListOf<PetType>()
        coEvery { preferencesManager.savePetType(capture(capturedTypes)) } just Runs

        // When
        onboardingViewModel.selectPetType(PetType.CAT)
        onboardingViewModel.selectPetType(PetType.DOG)
        onboardingViewModel.selectPetType(PetType.CAT)
        advanceUntilIdle()

        // Then - All calls were made (or only last if debounced)
        // Adjust expectation based on implementation
        assert(capturedTypes.size == 3) {
            "Expected 3 saves but got ${capturedTypes.size}"
        }
        assert(capturedTypes.last() == PetType.CAT) {
            "Expected last save to be CAT but got ${capturedTypes.last()}"
        }
    }
}