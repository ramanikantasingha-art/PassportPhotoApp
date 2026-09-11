package com.passportphoto.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.passportphoto.app.model.BackgroundChoice
import com.passportphoto.app.model.PassportSpec
import com.passportphoto.app.viewmodel.PhotoSessionViewModel

@Composable
fun EditorScreen(
    viewModel: PhotoSessionViewModel,
    onNext: () -> Unit,
    onRetake: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Preview & Enhance", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(360.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isProcessing -> CircularProgressIndicator()
                state.processedPhoto != null -> {
                    Image(
                        bitmap = state.processedPhoto!!.bitmap.asImageBitmap(),
                        contentDescription = "Processed passport photo",
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
                state.errorMessage != null -> Text(
                    state.errorMessage ?: "Error",
                    color = MaterialTheme.colorScheme.error
                )
                else -> Text("No photo yet")
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Background Color", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackgroundChoice.entries.forEach { choice ->
                FilterChip(
                    selected = state.selectedBackground == choice,
                    onClick = { viewModel.onBackgroundChanged(choice) },
                    label = { Text(choice.label) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Photo Standard", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Column {
            PassportSpec.ALL.forEach { spec ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = state.selectedSpec == spec,
                        onClick = { viewModel.onSpecChanged(spec) }
                    )
                    Text(spec.name)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                Text("Retake")
            }
            Button(
                onClick = onNext,
                enabled = state.processedPhoto != null && !state.isProcessing,
                modifier = Modifier.weight(1f)
            ) {
                Text("Continue")
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
