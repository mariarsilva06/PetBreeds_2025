package com.example.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.model.PetType
import com.example.profile.R.string

@Composable
fun PetTypeDialog(
    currentPetType: PetType?,
    onDismiss: () -> Unit,
    onSelect: (PetType) -> Unit
) {
    // TODO: Improve Dialog UI
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(string.choose_pet_type)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(string.select_preferred_pet_type))
                Spacer(modifier = Modifier.height(8.dp))

                FilterChip(
                    selected = currentPetType == PetType.CAT,
                    onClick = { onSelect(PetType.CAT) },
                    label = { Text(stringResource(string.cat)) },
                    leadingIcon = if (currentPetType == PetType.CAT) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                FilterChip(
                    selected = currentPetType == PetType.DOG,
                    onClick = { onSelect(PetType.DOG) },
                    label = { Text(stringResource(string.dog)) },
                    leadingIcon = if (currentPetType == PetType.DOG) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(string.close))
            }
        }
    )
}