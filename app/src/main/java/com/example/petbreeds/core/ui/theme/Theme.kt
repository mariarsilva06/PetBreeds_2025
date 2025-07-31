package com.example.petbreeds.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.petbreeds.domain.model.PetType

private val CatLightColorScheme = lightColorScheme(
    primary = CatPrimary,
    onPrimary = White,
    primaryContainer = CatPrimary,
    secondary = CatSecondary,
    background = CatBackground,
    surface = White,
    error = Favorite,
    onBackground = Black,
    onSurface = Black,
)

private val CatDarkColorScheme = darkColorScheme(
    primary = CatPrimaryDark,
    onPrimary = White,
    primaryContainer = CatPrimaryDark,
    secondary = CatSecondary,
    background = Black,
    surface = Color(0xFF1E1E1E),
    error = Favorite,
    onBackground = White,
    onSurface = White
)

private val DogLightColorScheme = lightColorScheme(
    primary = DogPrimary,
    onPrimary = White,
    primaryContainer = DogPrimary,
    secondary = DogSecondary,
    background = DogBackground,
    surface = White,
    error = Favorite,
    onBackground = Black,
    onSurface = Black
)

private val DogDarkColorScheme = darkColorScheme(
    primary = DogPrimaryDark,
    onPrimary = White,
    primaryContainer = DogPrimaryDark,
    secondary = DogSecondary,
    background = Black,
    surface = Color(0xFF1E1E1E),
    error = Favorite,
    onBackground = White,
    onSurface = White
)

@Composable
fun PetBreedsTheme(
    petType: PetType = PetType.CAT,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme && petType == PetType.CAT -> CatDarkColorScheme
        darkTheme && petType == PetType.DOG -> DogDarkColorScheme
        !darkTheme && petType == PetType.CAT -> CatLightColorScheme
        else -> DogLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}