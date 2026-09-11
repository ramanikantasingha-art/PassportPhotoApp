package com.passportphoto.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.passportphoto.app.camera.CameraController
import com.passportphoto.app.model.PhotoOrientation
import com.passportphoto.app.viewmodel.PhotoSessionViewModel

@Composable
fun CameraScreen(
    viewModel: PhotoSessionViewModel,
    onPhotoReady: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission is required to take passport photos.")
                Spacer(Modifier.height(12.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
        return
    }

    val cameraController = remember { CameraController(context, lifecycleOwner) }
    var detectedOrientation by remember { mutableStateOf<PhotoOrientation?>(null) }
    var isCapturing by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    cameraController.bindPreview(previewView)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Live orientation badge — approximates from device configuration;
        // the authoritative value comes from EXIF after capture.
        val configOrientation = LocalContext.current.resources.configuration.orientation
        val liveOrientation = if (configOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
            "Portrait"
        } else {
            "Landscape"
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
            color = Color.Black.copy(alpha = 0.55f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = "Orientation: $liveOrientation",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            if (isCapturing) {
                CircularProgressIndicator(color = Color.White)
            } else {
                IconButton(
                    onClick = {
                        isCapturing = true
                        cameraController.capturePhoto(
                            onSuccess = { captured ->
                                isCapturing = false
                                viewModel.onPhotoCaptured(captured)
                                onPhotoReady()
                            },
                            onError = {
                                isCapturing = false
                            }
                        )
                    },
                    modifier = Modifier
                        .size(76.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Camera,
                        contentDescription = "Capture",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { cameraController.shutdown() }
    }
}
