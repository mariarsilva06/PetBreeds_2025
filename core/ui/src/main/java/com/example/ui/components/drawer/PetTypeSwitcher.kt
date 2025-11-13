package com.example.ui.components.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.PetType
import com.example.resources.R

@Composable
fun PetTypeSwitcher(
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