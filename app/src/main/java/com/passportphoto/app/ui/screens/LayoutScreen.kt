package com.passportphoto.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.passportphoto.app.model.LayoutOption
import com.passportphoto.app.viewmodel.PhotoSessionViewModel

@Composable
fun LayoutScreen(
    viewModel: PhotoSessionViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.selectedLayout, state.processedPhoto) {
        if (state.processedPhoto != null) {
            viewModel.renderCanvas()
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Choose Layout", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        LayoutOption.entries.forEach { option ->
            val selected = state.selectedLayout == option
            Card(
                onClick = { viewModel.onLayoutSelected(option) },
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(option.label, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "4x6in canvas @ 300 DPI, orientation matches your capture",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    RadioButton(selected = selected, onClick = { viewModel.onLayoutSelected(option) })
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Live Preview", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isProcessing -> CircularProgressIndicator()
                state.finalCanvas != null -> Image(
                    bitmap = state.finalCanvas!!.asImageBitmap(),
                    contentDescription = "4x6 canvas preview",
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                )
                else -> Text("Preparing preview…")
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                enabled = state.finalCanvas != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
            }
        }
    }
}
