package com.passportphoto.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.passportphoto.app.ui.screens.CameraScreen
import com.passportphoto.app.ui.screens.EditorScreen
import com.passportphoto.app.ui.screens.ExportScreen
import com.passportphoto.app.ui.screens.LayoutScreen
import com.passportphoto.app.viewmodel.PhotoSessionViewModel

object Routes {
    const val CAMERA = "camera"
    const val EDITOR = "editor"
    const val LAYOUT = "layout"
    const val EXPORT = "export"
}

@Composable
fun PassportPhotoNavGraph(navController: NavHostController = rememberNavController()) {
    // Scoped to the whole graph so the same instance (and its in-progress photo)
    // survives navigation between screens.
    val sharedViewModel: PhotoSessionViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.CAMERA) {
        composable(Routes.CAMERA) {
            CameraScreen(
                viewModel = sharedViewModel,
                onPhotoReady = { navController.navigate(Routes.EDITOR) }
            )
        }
        composable(Routes.EDITOR) {
            EditorScreen(
                viewModel = sharedViewModel,
                onNext = { navController.navigate(Routes.LAYOUT) },
                onRetake = {
                    sharedViewModel.retake()
                    navController.popBackStack(Routes.CAMERA, inclusive = false)
                }
            )
        }
        composable(Routes.LAYOUT) {
            LayoutScreen(
                viewModel = sharedViewModel,
                onNext = { navController.navigate(Routes.EXPORT) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.EXPORT) {
            ExportScreen(
                viewModel = sharedViewModel,
                onStartOver = {
                    sharedViewModel.retake()
                    navController.popBackStack(Routes.CAMERA, inclusive = false)
                }
            )
        }
    }
}
