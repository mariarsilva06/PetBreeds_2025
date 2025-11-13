package com.example.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.model.PetType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.example.resources.R
import com.example.onboarding.R.string
@Composable
fun OnboardingScreen(
    onPetTypeSelected: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var animationStarted by remember { mutableStateOf(false) }
    var selectedPetType by remember { mutableStateOf<PetType?>(null) }

    // Start animations
    LaunchedEffect(Unit) {
        delay(300)
        animationStarted = true
    }

    LaunchedEffect(selectedPetType) {
        selectedPetType?.let { petType ->
            try {
                delay(1200)

                withContext(Dispatchers.Main) {
                    viewModel.selectPetType(petType)
                    delay(200)
                    onPetTypeSelected()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onPetTypeSelected()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Background decoration circles
        BackgroundDecorations()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo with animation
            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(800)) + scaleIn(animationSpec = tween(800))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.catdog2),
                    contentDescription = stringResource(string.pet_breeds_app_logo),
                    modifier = Modifier
                        .size(85.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 200)) +
                        slideInVertically(animationSpec = tween(800, delayMillis = 200))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(string.onboarding_welcome_title_part1),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = stringResource(string.onboarding_welcome_title_part2),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle with animation
            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 400))
            ) {
                Text(
                    text = stringResource(string.onboarding_welcome_subtitle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Question text
            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 600))
            ) {
                Text(
                    text = stringResource(string.onboarding_are_you_a),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Pet type selection cards
            AnimatedVisibility(
                visible = animationStarted,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 800)) +
                        slideInVertically(animationSpec = tween(800, delayMillis = 800))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        TypeCard(
                            painter = painterResource(id = R.drawable.cat7),
                            label = stringResource(string.cat_person),
                            petType = PetType.CAT,
                            isSelected = selectedPetType == PetType.CAT,
                            onClick = { selectedPetType = PetType.CAT }
                        )
                    }
                    // Add some space between the cards
                    Spacer(modifier = Modifier.width(16.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        TypeCard(
                            painter = painterResource(id = R.drawable.dog7),
                            label = stringResource(string.dog_person),
                            petType = PetType.DOG,
                            isSelected = selectedPetType == PetType.DOG,
                            onClick = { selectedPetType = PetType.DOG }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

