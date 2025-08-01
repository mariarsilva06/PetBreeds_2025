package com.example.petbreeds.presentation.onboarding

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petbreeds.R
import com.example.petbreeds.domain.model.PetType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
                    contentDescription = "Pet Breeds App Logo",
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
                        text = "Welcome to",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Pet Breeds",
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
                    text = "Discover your perfect companion",
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
                    text = "Are you a...",
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
                            label = "Cat Person",
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
                            label = "Dog Person",
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

@Composable
fun TypeCard(
    painter: Painter,
    label: String,
    petType: PetType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 16.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_elevation"
    )

    Card(
        modifier = Modifier
            .widthIn(min = 140.dp, max = 160.dp)
            .aspectRatio(1f)
            .scale(scale)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val emojiScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "emoji_scale"
            )

            Icon(
                painter = painter,
                contentDescription = label,
                modifier = Modifier.size(48.dp).scale(emojiScale),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Selection indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BackgroundDecorations() {
    // Floating decoration circles
    Box(modifier = Modifier.fillMaxSize()) {
        // Top left circle
        Card(
            modifier = Modifier
                .size(100.dp)
                .offset((-30).dp, 50.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {}

        // Top right circle
        Card(
            modifier = Modifier
                .size(60.dp)
                .offset(20.dp, 120.dp)
                .align(Alignment.TopEnd),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            )
        ) {}

        // Bottom left circle
        Card(
            modifier = Modifier
                .size(80.dp)
                .offset((-20).dp, (-40).dp)
                .align(Alignment.BottomStart),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
            )
        ) {}
    }
}