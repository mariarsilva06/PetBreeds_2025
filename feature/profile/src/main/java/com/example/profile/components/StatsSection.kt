package com.example.profile.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PetType
import com.example.profile.R.string

@SuppressLint("DefaultLocale")
@Composable
fun StatsSection(
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
            text = stringResource(string.your_statistics),
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
                title = stringResource(string.favorites),
                value = "$favoritesCount",
                stringResource(string.breeds_subtitle, currentPetType?.name?.lowercase() ?: "pet")
            )

            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Schedule,
                title = stringResource(string.avg_lifespan),
                value = if (averageLifespan > 0) String.format("%.1f", averageLifespan) else "--",
                subtitle = stringResource(string.years)
            )
        }
    }
}
