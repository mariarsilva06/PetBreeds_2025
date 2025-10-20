package com.example.profile

import android.annotation.SuppressLint
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
    val userName by viewModel.userName.collectAsState()
    val userBio by viewModel.userBio.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditBioDialog by remember { mutableStateOf(false) }

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
            ProfileHeader(
                userName = userName,
                userBio = userBio,
                onEditNameClick = { showEditNameDialog = true },
                onEditBioClick = { showEditBioDialog = true }
            )
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
    // Edit Name Dialog
    if (showEditNameDialog) {
        EditTextDialog(
            title = "Edit Name",
            currentValue = userName,
            placeholder = "Enter your name",
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                viewModel.updateUserName(newName)
                showEditNameDialog = false
            }
        )
    }
    // Edit Bio Dialog
    if (showEditBioDialog) {
        EditTextDialog(
            title = "Edit Bio",
            currentValue = userBio,
            placeholder = "Tell us about yourself",
            onDismiss = { showEditBioDialog = false },
            onConfirm = { newBio ->
                viewModel.updateUserBio(newBio)
                showEditBioDialog = false
            },
            singleLine = false
        )
    }
}

@Composable
private fun ProfileHeader(
    userName: String,
    userBio: String,
    onEditNameClick: () -> Unit,
    onEditBioClick: () -> Unit
) {
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
            // Profile Photo (TODO: Add photo change functionality)
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name with Edit Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(
                    onClick = onEditNameClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit name",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Bio with Edit Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userBio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                IconButton(
                    onClick = onEditBioClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit bio",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditTextDialog(
    title: String,
    currentValue: String,
    placeholder: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    singleLine: Boolean = true
) {
    var textValue by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = { Text(placeholder) },
                singleLine = singleLine,
                modifier = Modifier.fillMaxWidth(),
                maxLines = if (singleLine) 1 else 3
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textValue.isNotBlank()) {
                        onConfirm(textValue.trim())
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@SuppressLint("DefaultLocale")
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

@Composable
private fun PetTypeDialog(
    currentPetType: PetType?,
    onDismiss: () -> Unit,
    onSelect: (PetType) -> Unit
) {
    // TODO: Improve Dialog UI
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Pet Type") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select your preferred pet type:")
                Spacer(modifier = Modifier.height(8.dp))

                FilterChip(
                    selected = currentPetType == PetType.CAT,
                    onClick = { onSelect(PetType.CAT) },
                    label = { Text("Cat") },
                    leadingIcon = if (currentPetType == PetType.CAT) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                FilterChip(
                    selected = currentPetType == PetType.DOG,
                    onClick = { onSelect(PetType.DOG) },
                    label = { Text("Dog") },
                    leadingIcon = if (currentPetType == PetType.DOG) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun ThemeModeDialog(
    currentThemeMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    // TODO: Improve Dialog UI
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select your preferred theme:")
                Spacer(modifier = Modifier.height(8.dp))

                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = currentThemeMode == mode,
                        onClick = { onSelect(mode) },
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
                            Icon(icon, null, Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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