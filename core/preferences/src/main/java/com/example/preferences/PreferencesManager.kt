package com.example.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.common.di.ApplicationScope
import com.example.model.PetType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pet_preferences")

@Singleton
class PreferencesManager
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        @param:ApplicationScope private val coroutineScope: CoroutineScope,
    ) {
        private val petTypeKey = stringPreferencesKey("pet_type")
        private val isFirstLaunchKey = booleanPreferencesKey("is_first_launch")
        private val themeModeKey = stringPreferencesKey("theme_mode")
        private val userNameKey = stringPreferencesKey("user_name")
        private val userBioKey = stringPreferencesKey("user_bio")
        private val userPhotoUriKey = stringPreferencesKey("user_photo_uri")

        // Theme Mode
        suspend fun saveThemeMode(mode: ThemeMode) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[themeModeKey] = mode.name
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val themeModeFlow: Flow<ThemeMode> =
            context.dataStore.data.map { preferences ->
                preferences[themeModeKey]?.let {
                    try {
                        ThemeMode.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        ThemeMode.SYSTEM
                    }
                } ?: ThemeMode.SYSTEM
            }

        suspend fun savePetType(petType: PetType) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[petTypeKey] = petType.name
                    // Mark that the user has completed onboarding
                    preferences[isFirstLaunchKey] = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Retry once if failed
                try {
                    context.dataStore.edit { preferences ->
                        preferences[petTypeKey] = petType.name
                        preferences[isFirstLaunchKey] = false
                    }
                } catch (retryException: Exception) {
                    retryException.printStackTrace()
                }
            }
        }

        fun setPetType(petType: PetType) {
            coroutineScope.launch { savePetType(petType) }
        }

        val petTypeFlow: Flow<PetType?> =
            context.dataStore.data.map { preferences ->
                preferences[petTypeKey]?.let { PetType.valueOf(it) }
            }

        val isFirstLaunchFlow: Flow<Boolean> =
            context.dataStore.data.map { preferences ->
                preferences[isFirstLaunchKey] ?: true // Default to true for first launch
            }

        suspend fun saveUserName(name: String) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[userNameKey] = name
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val userNameFlow: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[userNameKey] ?: "Pet Lover"
            }

        suspend fun saveUserBio(bio: String) {
            try {
                context.dataStore.edit { preferences ->
                    preferences[userBioKey] = bio
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val userBioFlow: Flow<String> =
            context.dataStore.data.map { preferences ->
                preferences[userBioKey] ?: "Exploring the world of pets"
            }

        suspend fun saveUserPhotoUri(uri: String?) {
            try {
                context.dataStore.edit { preferences ->
                    if (uri != null) {
                        preferences[userPhotoUriKey] = uri
                    } else {
                        preferences.remove(userPhotoUriKey)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val userPhotoUriFlow: Flow<String?> =
            context.dataStore.data.map { preferences ->
                preferences[userPhotoUriKey]
            }
    }
