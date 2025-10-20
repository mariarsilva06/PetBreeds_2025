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
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_BIO_KEY = stringPreferencesKey("user_bio")
    private val USER_PHOTO_URI_KEY = stringPreferencesKey("user_photo_uri")

    // Theme Mode
    suspend fun saveThemeMode(mode: ThemeMode) {
        try {
            context.dataStore.edit { preferences ->
                preferences[THEME_MODE_KEY] = mode.name
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            saveThemeMode(mode)
        }
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY]?.let {
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

    suspend fun saveUserName(name: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[USER_NAME_KEY] = name
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "Pet Lover"
    }

    suspend fun saveUserBio(bio: String) {
        try {
            context.dataStore.edit { preferences ->
                preferences[USER_BIO_KEY] = bio
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val userBioFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_BIO_KEY] ?: "Exploring the world of pets"
    }

    suspend fun saveUserPhotoUri(uri: String?) {
        try {
            context.dataStore.edit { preferences ->
                if (uri != null) {
                    preferences[USER_PHOTO_URI_KEY] = uri
                } else {
                    preferences.remove(USER_PHOTO_URI_KEY)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val userPhotoUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PHOTO_URI_KEY]
    }

}
