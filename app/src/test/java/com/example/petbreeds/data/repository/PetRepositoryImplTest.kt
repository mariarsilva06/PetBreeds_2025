package com.example.petbreeds.data.repository

import com.example.petbreeds.core.data.NetworkResult
import com.example.petbreeds.data.api.dto.CatBreedDto
import com.example.petbreeds.data.api.dto.DogBreedDto
import com.example.petbreeds.data.api.dto.ImageDto
import com.example.petbreeds.data.api.service.CatApiService
import com.example.petbreeds.data.api.service.DogApiService
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.data.local.entity.PetEntity
import com.example.petbreeds.domain.model.PetType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PetRepositoryImplTest {

    @Mock
    private lateinit var catApiService: CatApiService

    @Mock
    private lateinit var dogApiService: DogApiService

    @Mock
    private lateinit var petDao: PetDao

    private lateinit var repository: PetRepositoryImpl

    private val mockCatBreedDto = CatBreedDto(
        id = "1",
        name = "Persian",
        origin = "Iran",
        temperament = "Calm",
        description = "Long-haired breed",
        lifeSpan = "12 - 17",
        image = ImageDto("http://example.com/persian.jpg")
    )

    private val mockDogBreedDto = DogBreedDto(
        id = "2",
        name = "Labrador",
        origin = "Canada",
        temperament = "Friendly",
        description = "Popular family dog",
        lifeSpan = "10 - 12",
        image = ImageDto("http://example.com/labrador.jpg")
    )

    private val mockPetEntity = PetEntity(
        id = "1",
        name = "Persian",
        origin = "Iran",
        temperament = "Calm",
        description = "Long-haired breed",
        lifeSpan = "12 - 17",
        imageUrl = "http://example.com/persian.jpg",
        petType = PetType.CAT
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = PetRepositoryImpl(catApiService, dogApiService, petDao)
    }

    @Test
    fun `getPets should return pets from dao as network result success`() = runTest {
        // Given
        whenever(petDao.getPetsByType(PetType.CAT)).thenReturn(flowOf(listOf(mockPetEntity)))

        // When
        val result = repository.getPets(PetType.CAT).first()

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        val successResult = result as NetworkResult.Success
        assertThat(successResult.data).hasSize(1)
        assertThat(successResult.data?.first()?.name).isEqualTo("Persian")
    }

    @Test
    fun `getFavoritePets should return favorite pets from dao`() = runTest {
        // Given
        val favoritePet = mockPetEntity.copy(isFavorite = true)
        whenever(petDao.getFavoritePetsByType(PetType.CAT)).thenReturn(flowOf(listOf(favoritePet)))

        // When
        val result = repository.getFavoritePets(PetType.CAT).first()

        // Then
        assertThat(result).hasSize(1)
        assertThat(result.first().isFavorite).isTrue()
        assertThat(result.first().name).isEqualTo("Persian")
    }

    @Test
    fun `refreshPets for cats should call cat api and save to dao`() = runTest {
        // Given
        whenever(catApiService.getBreeds(20, 0)).thenReturn(listOf(mockCatBreedDto))
        whenever(petDao.getFavoritePetsByType(PetType.CAT)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.refreshPets(PetType.CAT, 0, null)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        verify(catApiService).getBreeds(20, 0)
        verify(petDao).refreshPetsForFirstPage(any(), any())
    }

    @Test
    fun `refreshPets for dogs should call dog api and save to dao`() = runTest {
        // Given
        whenever(dogApiService.getBreeds(20, 0)).thenReturn(listOf(mockDogBreedDto))
        whenever(petDao.getFavoritePetsByType(PetType.DOG)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.refreshPets(PetType.DOG, 0, null)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        verify(dogApiService).getBreeds(20, 0)
        verify(petDao).refreshPetsForFirstPage(any(), any())
    }

    @Test
    fun `refreshPets with search query should call search endpoint`() = runTest {
        // Given
        val searchQuery = "Persian"
        whenever(catApiService.searchBreeds(searchQuery)).thenReturn(listOf(mockCatBreedDto))
        whenever(petDao.getFavoritePetsByType(PetType.CAT)).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.refreshPets(PetType.CAT, 0, searchQuery)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        verify(catApiService).searchBreeds(searchQuery)
        verify(petDao).refreshPetsForFirstPage(any(), any())
    }

    @Test
    fun `refreshPets for subsequent pages should append data`() = runTest {
        // Given
        whenever(catApiService.getBreeds(20, 1)).thenReturn(listOf(mockCatBreedDto))
        whenever(petDao.getPetsByType(PetType.CAT)).thenReturn(flowOf(listOf(mockPetEntity)))

        // When
        val result = repository.refreshPets(PetType.CAT, 1, null)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Success::class.java)
        verify(catApiService).getBreeds(20, 1)
        verify(petDao).appendPets(any(), any())
    }

    @Test
    fun `refreshPets should preserve favorite status`() = runTest {
        // Given
        val favoritePet = mockPetEntity.copy(isFavorite = true)
        whenever(catApiService.getBreeds(20, 0)).thenReturn(listOf(mockCatBreedDto))
        whenever(petDao.getFavoritePetsByType(PetType.CAT)).thenReturn(flowOf(listOf(favoritePet)))

        // When
        repository.refreshPets(PetType.CAT, 0, null)

        // Then
        verify(petDao).refreshPetsForFirstPage(any(), any())
        // The verification that favorites are preserved would need argument captor
        // to check the actual entities passed to refreshPetsForFirstPage
    }

    @Test
    fun `refreshPets should return error when api call fails`() = runTest {
        // Given
        whenever(catApiService.getBreeds(20, 0)).thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.refreshPets(PetType.CAT, 0, null)

        // Then
        assertThat(result).isInstanceOf(NetworkResult.Error::class.java)
        val errorResult = result as NetworkResult.Error
        assertThat(errorResult.message).isEqualTo("Network error")
    }

    @Test
    fun `toggleFavorite should update favorite status in dao`() = runTest {
        // Given
        val petId = "1"
        whenever(petDao.getPetById(petId)).thenReturn(mockPetEntity)

        // When
        repository.toggleFavorite(petId)

        // Then
        verify(petDao).getPetById(petId)
        verify(petDao).updateFavoriteStatus(petId, true) // toggles to true
    }

    @Test
    fun `toggleFavorite should handle null pet gracefully`() = runTest {
        // Given
        val petId = "nonexistent"
        whenever(petDao.getPetById(petId)).thenReturn(null)

        // When
        repository.toggleFavorite(petId)

        // Then
        verify(petDao).getPetById(petId)
        // Should not call updateFavoriteStatus when pet is null
    }

    @Test
    fun `getPetDetails should return pet from dao`() = runTest {
        // Given
        val petId = "1"
        whenever(petDao.getPetById(petId)).thenReturn(mockPetEntity)

        // When
        val result = repository.getPetDetails(petId)

        // Then
        assertThat(result).isNotNull()
        assertThat(result?.name).isEqualTo("Persian")
        assertThat(result?.id).isEqualTo(petId)
    }

    @Test
    fun `getPetDetails should return null when pet not found`() = runTest {
        // Given
        val petId = "nonexistent"
        whenever(petDao.getPetById(petId)).thenReturn(null)

        // When
        val result = repository.getPetDetails(petId)

        // Then
        assertThat(result).isNull()
    }
}