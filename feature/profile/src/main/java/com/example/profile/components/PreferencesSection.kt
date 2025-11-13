package com.example.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PetType
import com.example.preferences.ThemeMode
import com.example.profile.R.string

@Composable
fun PreferencesSection(
    currentPetType: PetType?,
    currentThemeMode: ThemeMode,
    onPetTypeChanged: (PetType) -> Unit,
    onThemeModeChanged: (ThemeMode) -> Unit
) {
    var showPetTypeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(string.preferences),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                PreferenceItem(
                    icon = Icons.Default.Pets,
                    title = stringResource(string.pet_type),
                    value = if (currentPetType == PetType.CAT) stringResource(string.cat) else stringResource(string.dog),
                    onClick = { showPetTypeDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                PreferenceItem(
                    icon = Icons.Default.Brightness6,
                    title = stringResource(string.app_theme),
                    value = when (currentThemeMode) {
                        ThemeMode.LIGHT -> stringResource(string.light)
                        ThemeMode.DARK -> stringResource(string.dark)
                        ThemeMode.SYSTEM -> stringResource(string.system_default)
                    },
                    onClick = { showThemeDialog = true }
                )
            }
        }
    }

    if (showPetTypeDialog) {
        PetTypeDialog(
            currentPetType = currentPetType,
            onDismiss = { showPetTypeDialog = false },
            onSelect = { petType ->
                onPetTypeChanged(petType)
                showPetTypeDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            currentThemeMode = currentThemeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                onThemeModeChanged(mode)
                showThemeDialog = false
            }
        )
    }
}