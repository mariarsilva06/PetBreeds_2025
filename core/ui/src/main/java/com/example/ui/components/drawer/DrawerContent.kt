package com.example.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PetType
import com.example.ui.components.ComingSoonDialog

@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    currentPetType: PetType,
    onPetTypeChanged: (PetType) -> Unit,
    onCloseDrawer: () -> Unit
) {
    var showComingSoonDialog by remember { mutableStateOf(false) }
    var selectedFeatureName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Header
        DrawerHeader()

        Spacer(modifier = Modifier.height(32.dp))

        // Pet Type Switch
        PetTypeSwitcher(
            currentPetType = currentPetType,
            onPetTypeChanged = onPetTypeChanged
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Menu Items
        DrawerMenuItem(
            icon = Icons.Default.Notifications,
            title = "Breeding Notifications",
            subtitle = "Get alerts for your favorite breeds",
            onClick = {
                selectedFeatureName = "Breeding Notifications"
                showComingSoonDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(
            icon = Icons.Default.LocationOn,
            title = "Find Nearby Breeders",
            subtitle = "Locate certified breeders in your area",
            onClick = {
                selectedFeatureName = "Find Nearby Breeders"
                showComingSoonDialog = true
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Version info at bottom
        // TODO: Add from the build info
        VersionInfo(versionName = "1.0.0")
    }

    // Coming Soon Dialog
    if (showComingSoonDialog) {
        ComingSoonDialog(
            featureName = selectedFeatureName,
            onDismiss = {
                showComingSoonDialog = false
                onCloseDrawer()
            }
        )
    }
}

