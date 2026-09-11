package com.passportphoto.app.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.passportphoto.app.processing.ExportUtils
import com.passportphoto.app.viewmodel.PhotoSessionViewModel

@Composable
fun ExportScreen(
    viewModel: PhotoSessionViewModel,
    onStartOver: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSaving by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Export", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            state.finalCanvas?.let { canvas ->
                Image(
                    bitmap = canvas.asImageBitmap(),
                    contentDescription = "Final 4x6 canvas",
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                )
            } ?: Text("No canvas to export yet")
        }

        Spacer(Modifier.height(20.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val canvas = state.finalCanvas ?: return@OutlinedButton
                    isSaving = true
                    val saved = ExportUtils.saveToGallery(
                        context, canvas, "passport_sheet_${System.currentTimeMillis()}",
                        Bitmap.CompressFormat.JPEG
                    )
                    isSaving = false
                    Toast.makeText(
                        context,
                        if (saved) "Saved to Pictures/PassportPhotos" else "Save failed",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                enabled = state.finalCanvas != null && !isSaving,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isSaving) "Saving…" else "Save JPEG")
            }

            OutlinedButton(
                onClick = {
                    val canvas = state.finalCanvas ?: return@OutlinedButton
                    ExportUtils.saveToGallery(
                        context, canvas, "passport_sheet_${System.currentTimeMillis()}",
                        Bitmap.CompressFormat.PNG
                    )
                    Toast.makeText(context, "Saved as PNG", Toast.LENGTH_SHORT).show()
                },
                enabled = state.finalCanvas != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save PNG")
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val canvas = state.finalCanvas ?: return@Button
                ExportUtils.printBitmap(context, canvas, "Passport Photo Sheet")
            },
            enabled = state.finalCanvas != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Print (Wireless Printer)")
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onStartOver, modifier = Modifier.fillMaxWidth()) {
            Text("Start Over")
        }
    }
}
