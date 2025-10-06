package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.model.PetType


private val CatLightColorScheme = lightColorScheme(
    primary = CatPrimary,
    onPrimary = White,
    primaryContainer = CatCard,
    onPrimaryContainer = CatPrimary,
    secondary = CatSecondary,
    onSecondary = White,
    secondaryContainer = CatCard,
    onSecondaryContainer = CatSecondary,
    tertiary = CatSecondary,
    onTertiary = White,
    background = CatBackground,
    onBackground = DarkGray,
    surface = CatSurface,
    onSurface = DarkGray,
    surfaceVariant = CatCard,
    onSurfaceVariant = MediumGray,
    surfaceContainer = CatCard,
    surfaceContainerHighest = LightGray,
    outline = MediumGray,
    error = ErrorRed,
    onError = White
)

private val CatDarkColorScheme = darkColorScheme(
    primary = CatPrimary,
    onPrimary = White,
    primaryContainer = CatPrimaryDark,
    onPrimaryContainer = White,
    secondary = CatSecondary,
    onSecondary = White,
    secondaryContainer = Color(0xFF2A2A3E),
    onSecondaryContainer = CatSecondary,
    tertiary = CatSecondary,
    onTertiary = White,
    background = Color(0xFF0F0F23),
    onBackground = White,
    surface = Color(0xFF1A1A2E),
    onSurface = White,
    surfaceVariant = Color(0xFF2A2A3E),
    onSurfaceVariant = Color(0xFFB0B0B0),
    surfaceContainer = Color(0xFF2A2A3E),
    surfaceContainerHighest = Color(0xFF3A3A4E),
    outline = Color(0xFF5A5A6E),
    error = ErrorRed,
    onError = White
)

private val DogLightColorScheme = lightColorScheme(
    primary = DogPrimary,
    onPrimary = White,
    primaryContainer = DogCard,
    onPrimaryContainer = DogPrimary,
    secondary = DogSecondary,
    onSecondary = White,
    secondaryContainer = DogCard,
    onSecondaryContainer = DogPrimary,
    tertiary = DogSecondary,
    onTertiary = White,
    background = DogBackground,
    onBackground = DarkGray,
    surface = DogSurface,
    onSurface = DarkGray,
    surfaceVariant = DogCard,
    onSurfaceVariant = MediumGray,
    surfaceContainer = DogCard,
    surfaceContainerHighest = LightGray,
    outline = MediumGray,
    error = ErrorRed,
    onError = White
)

private val DogDarkColorScheme = darkColorScheme(
    primary = DogPrimary,
    onPrimary = White,
    primaryContainer = DogPrimaryDark,
    onPrimaryContainer = White,
    secondary = DogSecondary,
    onSecondary = White,
    secondaryContainer = Color(0xFF1A2E2E),
    onSecondaryContainer = DogSecondary,
    tertiary = DogSecondary,
    onTertiary = White,
    background = Color(0xFF0A1F1F),
    onBackground = White,
    surface = Color(0xFF152A2A),
    onSurface = White,
    surfaceVariant = Color(0xFF1A2E2E),
    onSurfaceVariant = Color(0xFFB0B0B0),
    surfaceContainer = Color(0xFF1A2E2E),
    surfaceContainerHighest = Color(0xFF2A3E3E),
    outline = Color(0xFF4A5E5E),
    error = ErrorRed,
    onError = White
)

@Composable
fun PetBreedsTheme(
    petType: PetType = PetType.CAT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = try {
        when {
            darkTheme && petType == PetType.CAT -> CatDarkColorScheme
            darkTheme && petType == PetType.DOG -> DogDarkColorScheme
            !darkTheme && petType == PetType.CAT -> CatLightColorScheme
            else -> DogLightColorScheme
        }
    } catch (e: Exception) {
        // Fallback to cat light scheme if there's any issue
        e.printStackTrace()
        CatLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}