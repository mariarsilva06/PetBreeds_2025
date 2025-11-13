package com.example.details

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ui.components.ImageCarousel
import com.example.ui.components.LoadingIndicator
import com.example.model.Pet
import com.example.feature.R.string
import com.example.feature.details.DetailsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val pet by viewModel.pet.collectAsState()
    val additionalImages by viewModel.additionalImages.collectAsState()
    val isLoadingImages by viewModel.isLoadingImages.collectAsState()
    val context = LocalContext.current


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pet?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(string.back_button_description)
                        )
                    }
                },
                actions = {
                    pet?.let { currentPet ->
                        // Share button
                        IconButton(onClick = {
                            onShareClick(currentPet, context)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(string.share_label)
                            )
                        }

                        // Favorite button
                        IconButton(onClick = viewModel::onToggleFavorite) {
                            Icon(
                                imageVector = if (currentPet.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = if (currentPet.isFavorite) {
                                    stringResource(string.remove_from_favorites)
                                } else {
                                    stringResource(string.add_to_favorites)
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
    ) {  paddingValues ->
        pet?.let { currentPet ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Section
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val mainImageUrl = currentPet.imageUrl?.trim()
                    val imagesToShow = buildList {
                        mainImageUrl?.let { add(it) }
                        if (additionalImages.size >= 2) {
                            addAll(
                                additionalImages
                                    .map { it.trim() }
                                    .filter { it != mainImageUrl }
                            )
                        }
                    }
                    if (imagesToShow.isNotEmpty()) {
                        ImageCarousel(
                            images = imagesToShow,
                            petName = currentPet.name,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoadingImages) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(string.loading_images),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Text(
                                    text = stringResource(string.no_images_available),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Content Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Breed Name
                    Text(
                        text = currentPet.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Basic Info with clean layout
                    if (currentPet.origin.isNotEmpty() &&
                        currentPet.origin.lowercase() != "unknown" &&
                        currentPet.origin != "Unknown") {
                        InfoRow(label = stringResource(string.origin_label), value = currentPet.origin)
                    }

                    if (currentPet.lifeSpan.isNotEmpty()) {
                        val lifeSpanValue = if (currentPet.lifeSpan.contains("year", ignoreCase = true)) {
                            currentPet.lifeSpan
                        } else {
                            "${currentPet.lifeSpan} years"
                        }
                        InfoRow(label = stringResource(string.lifespan_label), value = lifeSpanValue)
                    }

                    // Temperament Section
                    if (currentPet.temperament.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Text(
                            text = stringResource(string.temperament_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TemperamentChips(temperament = currentPet.temperament)
                    }

                    // Description Section
                    if (currentPet.description.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Text(
                            text = stringResource(string.description_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentPet.description,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Favorite Action Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentPet.isFavorite) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                //TODO: Go to Favorites Screen on click
                                Text(
                                    text = if (currentPet.isFavorite) stringResource(string.added_to_favorites) else stringResource(string.add_to_favorites),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (currentPet.isFavorite) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                                Text(
                                    text = if (currentPet.isFavorite) {
                                        stringResource(string.breed_in_favorites)
                                    } else {
                                        stringResource(string.save_to_favorites)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (currentPet.isFavorite) {
                                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    }
                                )
                            }

                            IconButton(
                                onClick = viewModel::onToggleFavorite
                            ) {
                                Icon(
                                    imageVector = if (currentPet.isFavorite) {
                                        Icons.Filled.Favorite
                                    } else {
                                        Icons.Outlined.FavoriteBorder
                                    },
                                    contentDescription = if (currentPet.isFavorite) {
                                        stringResource(string.remove_from_favorites)
                                    } else {
                                        stringResource(string.add_to_favorites)
                                    },
                                    tint = if (currentPet.isFavorite) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    // Bottom spacing
                    Spacer(modifier = Modifier.height(16.dp))
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
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemperamentChips(
    temperament: String,
    modifier: Modifier = Modifier
) {
    val temperaments = temperament.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        temperaments.forEach { trait ->
            AssistChip(
                onClick = {  },
                label = {
                    Text(
                        text = trait,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

fun onShareClick(pet: Pet, context: Context) {
    val shareText = buildString {
        appendLine(context.getString(string.share_pet_title, pet.name))
        appendLine()
        appendLine(context.getString(string.share_pet_origin, pet.origin))
        appendLine(context.getString(string.share_pet_lifespan, pet.lifeSpan))
        appendLine()
        appendLine(context.getString(string.share_pet_temperament_title))
        appendLine(pet.temperament)
        appendLine()
        appendLine(context.getString(string.share_pet_about_title))
        appendLine(pet.description)
        appendLine()
        appendLine(context.getString(string.share_pet_discover))
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}