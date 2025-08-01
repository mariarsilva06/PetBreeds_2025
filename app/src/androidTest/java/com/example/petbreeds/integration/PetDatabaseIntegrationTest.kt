package com.example.petbreeds.integration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.petbreeds.data.local.dao.PetDao
import com.example.petbreeds.data.local.database.PetDatabase
import com.example.petbreeds.data.local.entity.PetEntity
import com.example.petbreeds.domain.model.PetType
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PetDatabaseIntegrationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PetDatabase
    private lateinit var petDao: PetDao

    private val testPets = listOf(
        PetEntity(
            id = "1",
            name = "Persian",
            origin = "Iran",
            temperament = "Calm, Gentle",
            description = "A long-haired breed",
            lifeSpan = "12 - 17",
            imageUrl = "http://example.com/persian.jpg",
            petType = PetType.CAT
        ),
        PetEntity(
            id = "2",
            name = "Siamese",
            origin = "Thailand",
            temperament = "Active, Vocal",
            description = "A short-haired breed",
            lifeSpan = "10 - 15",
            imageUrl = "http://example.com/siamese.jpg",
            isFavorite = true,
            petType = PetType.CAT
        ),
        PetEntity(
            id = "3",
            name = "Labrador",
            origin = "Canada",
            temperament = "Friendly",
            description = "Popular family dog",
            lifeSpan = "10 - 12",
            imageUrl = "http://example.com/labrador.jpg",
            petType = PetType.DOG
        )
    )

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PetDatabase::class.java
        ).allowMainThreadQueries().build()
        petDao = database.petDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrievePets() = runTest {
        // Insert pets
        petDao.insertPets(testPets)

        // Retrieve cats
        val cats = petDao.getPetsByType(PetType.CAT).first()
        assertThat(cats).hasSize(2)
        assertThat(cats.map { it.name }).containsExactly("Persian", "Siamese")

        // Retrieve dogs
        val dogs = petDao.getPetsByType(PetType.DOG).first()
        assertThat(dogs).hasSize(1)
        assertThat(dogs.first().name).isEqualTo("Labrador")
    }

    @Test
    fun getFavoritePets() = runTest {
        // Insert pets
        petDao.insertPets(testPets)

        // Get favorites
        val favoriteCats = petDao.getFavoritePetsByType(PetType.CAT).first()
        assertThat(favoriteCats).hasSize(1)
        assertThat(favoriteCats.first().name).isEqualTo("Siamese")

        val favoriteDogs = petDao.getFavoritePetsByType(PetType.DOG).first()
        assertThat(favoriteDogs).isEmpty()
    }

    @Test
    fun updateFavoriteStatus() = runTest {
        // Insert pets
        petDao.insertPets(testPets)

        // Update favorite status
        petDao.updateFavoriteStatus("1", true)

        // Verify update
        val pet = petDao.getPetById("1")
        assertThat(pet?.isFavorite).isTrue()

        // Verify in favorites list
        val favorites = petDao.getFavoritePetsByType(PetType.CAT).first()
        assertThat(favorites).hasSize(2)
        assertThat(favorites.map { it.name }).containsExactly("Persian", "Siamese")
    }

    @Test
    fun refreshPetsForFirstPage() = runTest {
        // Insert initial pets
        petDao.insertPets(testPets)

        // New pets for refresh
        val newPets = listOf(
            PetEntity(
                id = "4",
                name = "Maine Coon",
                origin = "USA",
                temperament = "Gentle",
                description = "Large breed",
                lifeSpan = "12 - 15",
                imageUrl = "http://example.com/mainecoon.jpg",
                petType = PetType.CAT
            )
        )

        // Refresh (should replace all cats)
        petDao.refreshPetsForFirstPage(newPets, PetType.CAT)

        // Verify cats were replaced
        val cats = petDao.getPetsByType(PetType.CAT).first()
        assertThat(cats).hasSize(1)
        assertThat(cats.first().name).isEqualTo("Maine Coon")

        // Verify dogs were not affected
        val dogs = petDao.getPetsByType(PetType.DOG).first()
        assertThat(dogs).hasSize(1)
        assertThat(dogs.first().name).isEqualTo("Labrador")
    }

    @Test
    fun appendPets() = runTest {
        // Insert initial pets
        val initialPets = listOf(testPets.first()) // Just Persian
        petDao.insertPets(initialPets)

        // Append more pets
        val additionalPets = listOf(testPets[1]) // Just Siamese
        petDao.appendPets(additionalPets, PetType.CAT)

        // Verify both pets exist
        val cats = petDao.getPetsByType(PetType.CAT).first()
        assertThat(cats).hasSize(2)
        assertThat(cats.map { it.name }).containsExactly("Persian", "Siamese")
    }

    @Test
    fun appendPetsIgnoresDuplicates() = runTest {
        // Insert initial pets
        petDao.insertPets(testPets)

        // Try to append duplicate pets
        val duplicatePets = listOf(testPets.first()) // Persian again
        petDao.appendPets(duplicatePets, PetType.CAT)

        // Verify no duplicates
        val cats = petDao.getPetsByType(PetType.CAT).first()
        assertThat(cats).hasSize(2) // Still just 2, not 3
        assertThat(cats.map { it.name }).containsExactly("Persian", "Siamese")
    }

    @Test
    fun getPetCountByType() = runTest {
        // Insert pets
        petDao.insertPets(testPets)

        // Check counts
        val catCount = petDao.getPetCountByType(PetType.CAT)
        val dogCount = petDao.getPetCountByType(PetType.DOG)

        assertThat(catCount).isEqualTo(2)
        assertThat(dogCount).isEqualTo(1)
    }

    @Test
    fun getPetIdsByType() = runTest {
        // Insert pets
        petDao.insertPets(testPets)

        // Get IDs
        val catIds = petDao.getPetIdsByType(PetType.CAT)
        val dogIds = petDao.getPetIdsByType(PetType.DOG)

        assertThat(catIds).containsExactly("1", "2")
        assertThat(dogIds).containsExactly("3")
    }

    @Test
    fun deleteAllPetsByType() = runTest {
        // Insert pets
        petDao.insertPets(testPets)

        // Delete all cats
        petDao.deleteAllPetsByType(PetType.CAT)

        // Verify cats are gone
        val cats = petDao.getPetsByType(PetType.CAT).first()
        assertThat(cats).isEmpty()

        // Verify dogs remain
        val dogs = petDao.getPetsByType(PetType.DOG).first()
        assertThat(dogs).hasSize(1)
    }
}
