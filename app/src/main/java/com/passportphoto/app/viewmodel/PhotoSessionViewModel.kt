package com.passportphoto.app.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passportphoto.app.model.BackgroundChoice
import com.passportphoto.app.model.CapturedPhoto
import com.passportphoto.app.model.LayoutOption
import com.passportphoto.app.model.PassportSpec
import com.passportphoto.app.model.ProcessedPhoto
import com.passportphoto.app.processing.CanvasCompositor
import com.passportphoto.app.processing.EnhancementPipeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PhotoSessionUiState(
    val capturedPhoto: CapturedPhoto? = null,
    val processedPhoto: ProcessedPhoto? = null,
    val selectedSpec: PassportSpec = PassportSpec.US_2X2,
    val selectedBackground: BackgroundChoice = BackgroundChoice.WHITE,
    val selectedLayout: LayoutOption = LayoutOption.FULL_12,
    val finalCanvas: Bitmap? = null,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Shared, nav-graph-scoped ViewModel that carries the photo through every screen
 * (Camera -> Editor -> Layout -> Export) so nothing needs to be re-processed
 * or passed via savedState/bundles.
 */
class PhotoSessionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoSessionUiState())
    val uiState: StateFlow<PhotoSessionUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(captured: CapturedPhoto) {
        _uiState.value = _uiState.value.copy(capturedPhoto = captured, errorMessage = null)
        runEnhancement()
    }

    fun onBackgroundChanged(choice: BackgroundChoice) {
        _uiState.value = _uiState.value.copy(selectedBackground = choice)
        runEnhancement()
    }

    fun onSpecChanged(spec: PassportSpec) {
        _uiState.value = _uiState.value.copy(selectedSpec = spec)
        runEnhancement()
    }

    fun onLayoutSelected(layout: LayoutOption) {
        _uiState.value = _uiState.value.copy(selectedLayout = layout)
        renderCanvas()
    }

    fun retake() {
        _uiState.value = PhotoSessionUiState(
            selectedSpec = _uiState.value.selectedSpec,
            selectedBackground = _uiState.value.selectedBackground,
            selectedLayout = _uiState.value.selectedLayout
        )
    }

    private fun runEnhancement() {
        val captured = _uiState.value.capturedPhoto ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            try {
                val processed = EnhancementPipeline.process(
                    captured = captured,
                    spec = _uiState.value.selectedSpec,
                    background = _uiState.value.selectedBackground
                )
                _uiState.value = _uiState.value.copy(processedPhoto = processed, isProcessing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "Enhancement failed: ${e.message}"
                )
            }
        }
    }

    fun renderCanvas() {
        val processed = _uiState.value.processedPhoto ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            try {
                val canvas = CanvasCompositor.compose(
                    processedPhoto = processed.bitmap,
                    orientation = processed.orientation,
                    spec = _uiState.value.selectedSpec,
                    layout = _uiState.value.selectedLayout
                )
                _uiState.value = _uiState.value.copy(finalCanvas = canvas, isProcessing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    errorMessage = "Canvas render failed: ${e.message}"
                )
            }
        }
    }
}
