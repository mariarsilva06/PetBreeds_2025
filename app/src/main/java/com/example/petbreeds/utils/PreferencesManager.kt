package com.example.petbreeds.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.petbreeds.domain.model.PetType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pet_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val PET_TYPE_KEY = stringPreferencesKey("pet_type")

    suspend fun savePetType(petType: PetType) {
        context.dataStore.edit { preferences ->
            preferences[PET_TYPE_KEY] = petType.name
        }
    }

    fun setPetType(petType: PetType) {
        // Launch in background
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            savePetType(petType)
        }
    }

    val petTypeFlow: Flow<PetType?> = context.dataStore.data.map { preferences ->
        preferences[PET_TYPE_KEY]?.let { PetType.valueOf(it) }
    }

    suspend fun clearPetType() {
        context.dataStore.edit { preferences ->
            preferences.remove(PET_TYPE_KEY)
        }
    }
}