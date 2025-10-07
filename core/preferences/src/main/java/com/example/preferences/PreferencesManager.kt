package com.example.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.PetType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val IS_FIRST_LAUNCH_KEY = booleanPreferencesKey("is_first_launch")

    suspend fun savePetType(petType: PetType) {
        try {
            context.dataStore.edit { preferences ->
                preferences[PET_TYPE_KEY] = petType.name
                // Mark that the user has completed onboarding
                preferences[IS_FIRST_LAUNCH_KEY] = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Retry once if failed
            try {
                context.dataStore.edit { preferences ->
                    preferences[PET_TYPE_KEY] = petType.name
                    preferences[IS_FIRST_LAUNCH_KEY] = false
                }
            } catch (retryException: Exception) {
                retryException.printStackTrace()
            }
        }
    }

    fun setPetType(petType: PetType) {
        // Launch in background
        CoroutineScope(Dispatchers.IO).launch {
            savePetType(petType)
        }
    }

    val petTypeFlow: Flow<PetType?> = context.dataStore.data.map { preferences ->
        preferences[PET_TYPE_KEY]?.let { PetType.valueOf(it) }
    }

    val isFirstLaunchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH_KEY] ?: true // Default to true for first launch
    }

}