package com.example.petbreeds.data.mapper

import com.example.petbreeds.data.api.dto.CatBreedDto
import com.example.petbreeds.data.api.dto.DogBreedDto
import com.example.petbreeds.data.api.dto.ImageDto
import com.example.petbreeds.data.local.entity.PetEntity
import com.example.model.PetType
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PetMappersTest {

    @Test
    fun `CatBreedDto should map to PetEntity correctly`() {
        // Given
        val catBreedDto = CatBreedDto(
            id = "1",
            name = "Persian",
            origin = "Iran",
            temperament = "Calm, Gentle",
            description = "A long-haired breed of cat",
            lifeSpan = "12 - 17",
            image = ImageDto("http://example.com/persian.jpg")
        )
        val additionalImages = listOf("http://example.com/persian2.jpg")

        // When
        val result = catBreedDto.toEntity(isFavorite = true, additionalImages = additionalImages)

        // Then
        assertThat(result.id).isEqualTo("1")
        assertThat(result.name).isEqualTo("Persian")
        assertThat(result.origin).isEqualTo("Iran")
        assertThat(result.temperament).isEqualTo("Calm, Gentle")
        assertThat(result.description).isEqualTo("A long-haired breed of cat")
        assertThat(result.lifeSpan).isEqualTo("12 - 17")
        assertThat(result.imageUrl).isEqualTo("http://example.com/persian.jpg")
        assertThat(result.additionalImages).isEqualTo(additionalImages)
        assertThat(result.isFavorite).isTrue()
        assertThat(result.petType).isEqualTo(PetType.CAT)
    }

    @Test
    fun `CatBreedDto with null values should map to PetEntity with defaults`() {
        // Given
        val catBreedDto = CatBreedDto(
            id = "1",
            name = "Persian",
            origin = null,
            temperament = null,
            description = null,
            lifeSpan = null,
            image = null
        )

        // When
        val result = catBreedDto.toEntity()

        // Then
        assertThat(result.id).isEqualTo("1")
        assertThat(result.name).isEqualTo("Persian")
        assertThat(result.origin).isEqualTo("Unknown")
        assertThat(result.temperament).isEmpty()
        assertThat(result.description).isEmpty()
        assertThat(result.lifeSpan).isEmpty()
        assertThat(result.imageUrl).isNull()
        assertThat(result.additionalImages).isEmpty()
        assertThat(result.isFavorite).isFalse()
        assertThat(result.petType).isEqualTo(PetType.CAT)
    }

    @Test
    fun `DogBreedDto should map to PetEntity correctly`() {
        // Given
        val dogBreedDto = DogBreedDto(
            id = "2",
            name = "Labrador",
            origin = "Canada",
            temperament = "Friendly, Active",
            description = "A popular family dog",
            lifeSpan = "10 - 12",
            image = ImageDto("http://example.com/labrador.jpg")
        )
        val additionalImages = listOf("http://example.com/labrador2.jpg")

        // When
        val result = dogBreedDto.toEntity(isFavorite = true, additionalImages = additionalImages)

        // Then
        assertThat(result.id).isEqualTo("2")
        assertThat(result.name).isEqualTo("Labrador")
        assertThat(result.origin).isEqualTo("Canada")
        assertThat(result.temperament).isEqualTo("Friendly, Active")
        assertThat(result.description).isEqualTo("A popular family dog")
        assertThat(result.lifeSpan).isEqualTo("10 - 12")
        assertThat(result.imageUrl).isEqualTo("http://example.com/labrador.jpg")
        assertThat(result.additionalImages).isEqualTo(additionalImages)
        assertThat(result.isFavorite).isTrue()
        assertThat(result.petType).isEqualTo(PetType.DOG)
    }

    @Test
    fun `DogBreedDto with null values should map to PetEntity with defaults`() {
        // Given
        val dogBreedDto = DogBreedDto(
            id = "2",
            name = "Labrador",
            origin = null,
            temperament = null,
            description = null,
            lifeSpan = null,
            image = null
        )

        // When
        val result = dogBreedDto.toEntity()

        // Then
        assertThat(result.id).isEqualTo("2")
        assertThat(result.name).isEqualTo("Labrador")
        assertThat(result.origin).isEqualTo("Unknown")
        assertThat(result.temperament).isEmpty()
        assertThat(result.description).isEmpty()
        assertThat(result.lifeSpan).isEmpty()
        assertThat(result.imageUrl).isNull()
        assertThat(result.additionalImages).isEmpty()
        assertThat(result.isFavorite).isFalse()
        assertThat(result.petType).isEqualTo(PetType.DOG)
    }

    @Test
    fun `PetEntity should map to Pet domain model correctly`() {
        // Given
        val petEntity = PetEntity(
            id = "1",
            name = "Persian",
            origin = "Iran",
            temperament = "Calm, Gentle",
            description = "A long-haired breed of cat",
            lifeSpan = "12 - 17",
            imageUrl = "http://example.com/persian.jpg",
            additionalImages = listOf("http://example.com/persian2.jpg"),
            isFavorite = true,
            petType = PetType.CAT
        )

        // When
        val result = petEntity.toDomain()

        // Then
        assertThat(result.id).isEqualTo("1")
        assertThat(result.name).isEqualTo("Persian")
        assertThat(result.origin).isEqualTo("Iran")
        assertThat(result.temperament).isEqualTo("Calm, Gentle")
        assertThat(result.description).isEqualTo("A long-haired breed of cat")
        assertThat(result.lifeSpan).isEqualTo("12 - 17")
        assertThat(result.imageUrl).isEqualTo("http://example.com/persian.jpg")
        assertThat(result.additionalImages).containsExactly("http://example.com/persian2.jpg")
        assertThat(result.isFavorite).isTrue()
        assertThat(result.petType).isEqualTo(PetType.CAT)
    }

    @Test
    fun `List of PetEntity should map to List of Pet correctly`() {
        // Given
        val petEntities = listOf(
            PetEntity(
                id = "1",
                name = "Persian",
                origin = "Iran",
                temperament = "Calm",
                description = "Long-haired breed",
                lifeSpan = "12 - 17",
                imageUrl = "http://example.com/persian.jpg",
                petType = PetType.CAT
            ),
            PetEntity(
                id = "2",
                name = "Labrador",
                origin = "Canada",
                temperament = "Friendly",
                description = "Family dog",
                lifeSpan = "10 - 12",
                imageUrl = "http://example.com/labrador.jpg",
                petType = PetType.DOG
            )
        )

        // When
        val result = petEntities.toDomain()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Persian")
        assertThat(result[0].petType).isEqualTo(PetType.CAT)
        assertThat(result[1].name).isEqualTo("Labrador")
        assertThat(result[1].petType).isEqualTo(PetType.DOG)
    }

    @Test
    fun `List of CatBreedDto should map to List of PetEntity correctly`() {
        // Given
        val catBreedDtos = listOf(
            CatBreedDto(
                id = "1",
                name = "Persian",
                origin = "Iran",
                temperament = "Calm",
                description = "Long-haired breed",
                lifeSpan = "12 - 17",
                image = ImageDto("http://example.com/persian.jpg")
            ),
            CatBreedDto(
                id = "2",
                name = "Siamese",
                origin = "Thailand",
                temperament = "Active",
                description = "Short-haired breed",
                lifeSpan = "10 - 15",
                image = ImageDto("http://example.com/siamese.jpg")
            )
        )

        // When
        val result = catBreedDtos.toCatEntities()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Persian")
        assertThat(result[0].petType).isEqualTo(PetType.CAT)
        assertThat(result[1].name).isEqualTo("Siamese")
        assertThat(result[1].petType).isEqualTo(PetType.CAT)
    }

    @Test
    fun `List of DogBreedDto should map to List of PetEntity correctly`() {
        // Given
        val dogBreedDtos = listOf(
            DogBreedDto(
                id = "1",
                name = "Labrador",
                origin = "Canada",
                temperament = "Friendly",
                description = "Family dog",
                lifeSpan = "10 - 12",
                image = ImageDto("http://example.com/labrador.jpg")
            ),
            DogBreedDto(
                id = "2",
                name = "Golden Retriever",
                origin = "Scotland",
                temperament = "Intelligent",
                description = "Popular breed",
                lifeSpan = "10 - 12",
                image = ImageDto("http://example.com/golden.jpg")
            )
        )

        // When
        val result = dogBreedDtos.toDogEntities()

        // Then
        assertThat(result).hasSize(2)
        assertThat(result[0].name).isEqualTo("Labrador")
        assertThat(result[0].petType).isEqualTo(PetType.DOG)
        assertThat(result[1].name).isEqualTo("Golden Retriever")
        assertThat(result[1].petType).isEqualTo(PetType.DOG)
    }

    @Test
    fun `Empty lists should map correctly`() {
        // Given & When & Then
        assertThat(emptyList<PetEntity>().toDomain()).isEmpty()
        assertThat(emptyList<CatBreedDto>().toCatEntities()).isEmpty()
        assertThat(emptyList<DogBreedDto>().toDogEntities()).isEmpty()
    }
}