package com.example.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

