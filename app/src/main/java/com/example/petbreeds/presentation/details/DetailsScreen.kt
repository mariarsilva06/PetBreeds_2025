package com.example.petbreeds.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petbreeds.presentation.components.ImageCarousel
import com.example.petbreeds.presentation.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val pet by viewModel.pet.collectAsState()
    val showFullImage = remember { mutableStateOf(false) }
    val additionalImages by viewModel.additionalImages.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    pet?.let { currentPet ->
                        IconButton(onClick = viewModel::onToggleFavorite) {
                            Icon(
                                imageVector = if (currentPet.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = if (currentPet.isFavorite) {
                                    "Remove from favorites"
                                } else {
                                    "Add to favorites"
                                },
                                tint = if (currentPet.isFavorite) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        pet?.let { currentPet ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    val allImages = buildList {
                        currentPet.imageUrl?.let { add(it) }
                        addAll(additionalImages)
                    }

                    if (allImages.isNotEmpty()) {
                        ImageCarousel(
                            images = allImages,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(Color.White)
                        )
                    }
                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = currentPet.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    InfoRow(label = "Origin", value = currentPet.origin)

                    if (currentPet.lifeSpan.isNotEmpty()) {
                        InfoRow(label = "Life Span", value = currentPet.lifeSpan)
                    }

                    if (currentPet.temperament.isNotEmpty()) {
                        Text(
                            text = "Temperament",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TemperamentChips(temperament = currentPet.temperament)
                    }

                    if (currentPet.description.isNotEmpty()) {
                        HorizontalDivider()
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentPet.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp
                        )
                    }
                }

            }
            if (showFullImage.value) {
                Dialog(onDismissRequest = { showFullImage.value = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AsyncImage(
                            model = currentPet.imageUrl,
                            contentDescription = currentPet.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                        IconButton(
                            onClick = { showFullImage.value = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close preview",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        } ?: LoadingIndicator()
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemperamentChips(
    temperament: String,
    modifier: Modifier = Modifier
) {
    val temperaments = temperament.split(",").map { it.trim() }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        temperaments.forEach { trait ->
            AssistChip(
                onClick = { },
                label = { Text(trait) }
            )
        }
    }
}