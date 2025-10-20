package com.example.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.model.PetType
import com.example.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentPetType by viewModel.currentPetType.collectAsState()
    val currentThemeMode by viewModel.currentThemeMode.collectAsState()
    val favoritesCount by viewModel.favoritesCount.collectAsState()
    val averageLifespan by viewModel.averageLifespan.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // TODO: Make ProfileHeader editable (photo, name, bio)
            ProfileHeader()
            Spacer(modifier = Modifier.height(24.dp))

            StatsSection(
                favoritesCount = favoritesCount,
                averageLifespan = averageLifespan,
                currentPetType = currentPetType
            )
            Spacer(modifier = Modifier.height(24.dp))

            PreferencesSection(
                currentPetType = currentPetType,
                currentThemeMode = currentThemeMode,
                onPetTypeChanged = viewModel::updatePetType,
                onThemeModeChanged = viewModel::updateThemeMode
            )
            Spacer(modifier = Modifier.height(24.dp))

            AboutCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileHeader() {
    // TODO: Add click to edit profile photo
    // TODO: Add click to edit name and bio
    // TODO: Show user join date
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                // TODO: Add camera icon overlay for photo change
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Pet Lover", // TODO: Make this editable
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Exploring the world of pets", // TODO: Make this editable bio
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatsSection(
    favoritesCount: Int,
    averageLifespan: Float,
    currentPetType: PetType?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Your Statistics",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                title = "Favorites",
                value = "$favoritesCount",
                subtitle = "${currentPetType?.name?.lowercase() ?: "pet"} breeds"
            )

            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                title = "Avg Lifespan",
                value = if (averageLifespan > 0) String.format("%.1f", averageLifespan) else "--",
                subtitle = "years"
            )
        }
        // TODO: Add row for total breeds viewed
        // TODO: Add row for days streak using app
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PreferencesSection(
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
            text = "Preferences",
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
                // Pet Type Preference
                PreferenceItem(
                    icon = Icons.Default.Pets,
                    title = "Pet Type",
                    value = if (currentPetType == PetType.CAT) "Cat" else "Dog",
                    onClick = { showPetTypeDialog = true }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Theme Preference
                PreferenceItem(
                    icon = Icons.Default.Brightness6,
                    title = "App Theme",
                    value = when (currentThemeMode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "System Default"
                    },
                    onClick = { showThemeDialog = true }
                )

                // TODO: Add notification preferences
            }
        }
    }

    // Pet Type Dialog
    if (showPetTypeDialog) {
        // TODO: Improve Dialog UI
        AlertDialog(
            onDismissRequest = { showPetTypeDialog = false },
            title = { Text("Choose Pet Type") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select your preferred pet type:")
                    Spacer(modifier = Modifier.height(8.dp))

                    FilterChip(
                        selected = currentPetType == PetType.CAT,
                        onClick = {
                            onPetTypeChanged(PetType.CAT)
                            showPetTypeDialog = false
                        },
                        label = { Text("Cat") },
                        leadingIcon = if (currentPetType == PetType.CAT) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FilterChip(
                        selected = currentPetType == PetType.DOG,
                        onClick = {
                            onPetTypeChanged(PetType.DOG)
                            showPetTypeDialog = false
                        },
                        label = { Text("Dog") },
                        leadingIcon = if (currentPetType == PetType.DOG) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPetTypeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        // TODO: Improve Dialog UI
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select your preferred theme:")
                    Spacer(modifier = Modifier.height(8.dp))

                    ThemeMode.values().forEach { mode ->
                        FilterChip(
                            selected = currentThemeMode == mode,
                            onClick = {
                                onThemeModeChanged(mode)
                                showThemeDialog = false
                            },
                            label = {
                                Text(
                                    when (mode) {
                                        ThemeMode.LIGHT -> "Light Mode"
                                        ThemeMode.DARK -> "Dark Mode"
                                        ThemeMode.SYSTEM -> "System Default"
                                    }
                                )
                            },
                            leadingIcon = {
                                val icon = when (mode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.Brightness4
                                }
                                Icon(icon, contentDescription = null, Modifier.size(18.dp))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun PreferenceItem(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
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
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Edit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutCard() {
    // TODO: Add Rate App button
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AboutRow(
                    icon = Icons.Default.Info,
                    label = "App Version",
                    value = "1.0.2"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                AboutRow(
                    icon = Icons.Default.Code,
                    label = "Developed with",
                    value = "Jetpack Compose"
                )
            }
        }
    }
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}