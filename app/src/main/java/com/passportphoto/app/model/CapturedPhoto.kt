package com.passportphoto.app.model

import android.graphics.Bitmap

/**
 * Represents a photo at each stage of the pipeline: raw capture, cropped,
 * and fully enhanced (background replaced + lighting corrected).
 */
data class CapturedPhoto(
    val bitmap: Bitmap,
    val orientation: PhotoOrientation
)

data class ProcessedPhoto(
    val bitmap: Bitmap,
    val orientation: PhotoOrientation,
    val backgroundChoice: BackgroundChoice
)
