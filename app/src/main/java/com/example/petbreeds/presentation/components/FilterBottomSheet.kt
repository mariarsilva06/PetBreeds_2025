package com.example.petbreeds.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentRange: ClosedFloatingPointRange<Float>,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    var tempRange by remember { mutableStateOf(currentRange) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Filter by Lifespan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Lifespan Range: ${tempRange.start.toInt()}-${tempRange.endInclusive.toInt()} years",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            RangeSlider(
                value = tempRange,
                onValueChange = { tempRange = it },
                valueRange = 0f..30f,
                steps = 29,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        tempRange = 0f..30f
                        onRangeChange(tempRange)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        onRangeChange(tempRange)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}