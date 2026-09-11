package com.passportphoto.app.processing

import com.passportphoto.app.camera.OrientationResolver
import com.passportphoto.app.model.BackgroundChoice
import com.passportphoto.app.model.CapturedPhoto
import com.passportphoto.app.model.PassportSpec
import com.passportphoto.app.model.ProcessedPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrates the full enhancement pipeline off the main thread:
 * 1. Face detection
 * 2. Auto-crop to passport spec (orientation-aware aspect ratio)
 * 3. Background segmentation + replacement
 * 4. Lighting/contrast normalization
 */
object EnhancementPipeline {

    suspend fun process(
        captured: CapturedPhoto,
        spec: PassportSpec,
        background: BackgroundChoice
    ): ProcessedPhoto = withContext(Dispatchers.Default) {
        val targetAspect = OrientationResolver.aspectRatioFor(
            captured.orientation, spec.photoWidthMm, spec.photoHeightMm
        )

        val faceBox = try {
            FaceCropUtils.detectFaceBounds(captured.bitmap)
        } catch (e: Exception) {
            null
        }

        val cropped = FaceCropUtils.cropToSpec(captured.bitmap, faceBox, spec, targetAspect)
        val bgReplaced = BackgroundRemoval.replaceBackground(cropped, background)
        val normalized = LightingNormalizer.normalize(bgReplaced)

        ProcessedPhoto(
            bitmap = normalized,
            orientation = captured.orientation,
            backgroundChoice = background
        )
    }
}
