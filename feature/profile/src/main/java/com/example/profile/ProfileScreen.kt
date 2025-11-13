package com.example.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.preferences.ThemeMode
import com.example.profile.R.string
import com.example.profile.components.AboutCard
import com.example.profile.components.EditTextDialog
import com.example.profile.components.PreferencesSection
import com.example.profile.components.ProfileHeader
import com.example.profile.components.StatsSection

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
                title = { Text(stringResource(string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(string.navigate_back)
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
            title = stringResource(string.edit_name),
            currentValue = userName,
            placeholder = stringResource(string.enter_your_name),
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
            title = stringResource(string.edit_bio),
            currentValue = userBio,
            placeholder = stringResource(string.tell_us_about_yourself),
            onDismiss = { showEditBioDialog = false },
            onConfirm = { newBio ->
                viewModel.updateUserBio(newBio)
                showEditBioDialog = false
            },
            singleLine = false
        )
    }
}

