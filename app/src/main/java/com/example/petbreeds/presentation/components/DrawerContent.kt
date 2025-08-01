package com.example.petbreeds.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petbreeds.R
import com.example.petbreeds.domain.model.PetType

@Composable
fun DrawerContent(
    currentPetType: PetType,
    onPetTypeChanged: (PetType) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
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
        VersionInfo()
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

@Composable
private fun DrawerHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(id = R.drawable.catdog2),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Pet Breeds",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Discover your perfect companion",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PetTypeSwitcher(
    currentPetType: PetType,
    onPetTypeChanged: (PetType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Explore by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PetTypeButton(
                    petType = PetType.CAT,
                    currentPetType = currentPetType,
                    onPetTypeChanged = onPetTypeChanged,
                    painter = painterResource(id = R.drawable.cat7),
                    label = "Cats",
                    modifier = Modifier.weight(6f)
                )

                PetTypeButton(
                    petType = PetType.DOG,
                    currentPetType = currentPetType,
                    onPetTypeChanged = onPetTypeChanged,
                    painter = painterResource(id = R.drawable.dog7),
                    label = "Dogs",
                    modifier = Modifier.weight(6f)
                )
            }
        }
    }
}

@Composable
private fun PetTypeButton(
    petType: PetType,
    currentPetType: PetType,
    onPetTypeChanged: (PetType) -> Unit,
    painter: Painter,
    label: String,
    modifier: Modifier = Modifier
) {
    val isSelected = petType == currentPetType
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onPetTypeChanged(petType) },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = if (isSelected) CardDefaults.cardElevation(4.dp) else CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painter,
                contentDescription = label,
                modifier = Modifier.size(25.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VersionInfo() {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pet Breeds App",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
        }

}