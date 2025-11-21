package com.example.domain.common

import com.example.model.Pet
import com.example.model.PetType

object TestData {

    // Pet IDs
    const val TEST_PET_ID = "test_pet_id"
    const val CAT_PET_ID = "cat_id"
    const val DOG_PET_ID = "dog_id"

    const val PET_ID_1 = "1"
    const val PET_ID_2 = "2"
    const val PET_ID_3 = "3"
    const val NONEXISTENT_PET_ID = "nonexistent"

    // Pet Names
    const val CAT_NAME_PERSIAN = "Persian"
    const val CAT_NAME_MAINE_COON = "Maine Coon"
    const val CAT_NAME_SIAMESE = "Siamese"
    const val DOG_NAME_BUDDY = "Buddy"
    const val DOG_NAME_LABRADOR = "Labrador"

    // Origins
    const val ORIGIN_IRAN = "Iran"
    const val ORIGIN_USA = "USA"
    const val ORIGIN_TEST = "Test Origin"

    // Temperaments
    const val TEMPERAMENT_CALM = "Calm"
    const val TEMPERAMENT_FRIENDLY = "Friendly"
    const val TEMPERAMENT_PLAYFUL = "Friendly, Playful"

    // Descriptions
    const val DESCRIPTION_LONG_HAIRED = "Long-haired breed"
    const val DESCRIPTION_FRIENDLY_DOG = "A friendly dog"
    const val DESCRIPTION_TEST = "Test description"

    // Life Spans
    const val LIFESPAN_10_12 = "10 - 12"
    const val LIFESPAN_10_14 = "10 - 14"
    const val LIFESPAN_10_14_YEARS = "10 - 14 years"
    const val LIFESPAN_10_16 = "10 - 16"
    const val LIFESPAN_12_14 = "12 - 14"
    const val LIFESPAN_12_16 = "12 - 16"
    const val LIFESPAN_12_16_YEARS = "12 - 16 years"
    const val LIFESPAN_12_17 = "12 - 17"
    const val LIFESPAN_12 = "12"
    const val LIFESPAN_12_YEARS = "12 years"
    const val LIFESPAN_14 = "14"
    const val LIFESPAN_14_YEARS = "14 years"
    const val LIFESPAN_15_YEARS = "15 years"
    const val LIFESPAN_UNKNOWN = "unknown"

    // Image URLs
    const val IMAGE_URL_PERSIAN = "http://example.com/persian.jpg"
    const val IMAGE_URL_BUDDY = "http://example.com/buddy.jpg"
    const val IMAGE_URL_BUDDY_2 = "http://example.com/buddy2.jpg"
    const val IMAGE_URL_TEST = "https://test.com/image.jpg"
    const val IMAGE_URL_1 = "image1.jpg"
    const val IMAGE_URL_2 = "image2.jpg"
    const val IMAGE_URL_3 = "image3.jpg"
    const val IMAGE_URL_CAT_1 = "cat1.jpg"
    const val IMAGE_URL_CAT_2 = "cat2.jpg"
    const val IMAGE_URL_DOG_1 = "dog1.jpg"
    const val IMAGE_URL_DOG_2 = "dog2.jpg"

    // Error Messages
    const val ERROR_NETWORK = "Network error"

    // Search Queries
    const val SEARCH_QUERY_PERSIAN = "Persian"

    fun createMockPet(
        id: String = PET_ID_1,
        name: String = CAT_NAME_PERSIAN,
        origin: String = ORIGIN_TEST,
        temperament: String = TEMPERAMENT_PLAYFUL,
        description: String = DESCRIPTION_TEST,
        lifeSpan: String = LIFESPAN_10_14,
        imageUrl: String? = IMAGE_URL_TEST,
        additionalImages: List<String> = emptyList(),
        isFavorite: Boolean = true,
        petType: PetType = PetType.CAT
    ): Pet {
        return Pet(
            id = id,
            name = name,
            origin = origin,
            temperament = temperament,
            description = description,
            lifeSpan = lifeSpan,
            imageUrl = imageUrl,
            additionalImages = additionalImages,
            isFavorite = isFavorite,
            petType = petType
        )
    }

    fun createMockPersianCat(
        id: String = PET_ID_1,
        lifeSpan: String = LIFESPAN_12_17,
        isFavorite: Boolean = false
    ): Pet = createMockPet(
        id = id,
        name = CAT_NAME_PERSIAN,
        origin = ORIGIN_IRAN,
        temperament = TEMPERAMENT_CALM,
        description = DESCRIPTION_LONG_HAIRED,
        lifeSpan = lifeSpan,
        imageUrl = IMAGE_URL_PERSIAN,
        isFavorite = isFavorite,
        petType = PetType.CAT
    )

    fun createMockBuddyDog(
        id: String = PET_ID_1,
        lifeSpan: String = LIFESPAN_10_12,
        isFavorite: Boolean = true
    ): Pet = createMockPet(
        id = id,
        name = DOG_NAME_BUDDY,
        origin = ORIGIN_USA,
        temperament = TEMPERAMENT_FRIENDLY,
        description = DESCRIPTION_FRIENDLY_DOG,
        lifeSpan = lifeSpan,
        imageUrl = IMAGE_URL_BUDDY,
        additionalImages = listOf(IMAGE_URL_BUDDY_2),
        isFavorite = isFavorite,
        petType = PetType.DOG
    )

    fun createMockFavoriteCats(): List<Pet> = listOf(
        createMockPet(
            id = PET_ID_1,
            name = CAT_NAME_PERSIAN,
            lifeSpan = LIFESPAN_10_14_YEARS,
            isFavorite = true
        ),
        createMockPet(
            id = PET_ID_2,
            name = CAT_NAME_MAINE_COON,
            lifeSpan = LIFESPAN_12_16_YEARS,
            isFavorite = true
        )
    )

}