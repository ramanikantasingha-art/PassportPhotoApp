package com.passportphoto.app.processing

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.passportphoto.app.model.PassportSpec
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Detects the face in a captured bitmap and auto-crops/centers the image to
 * match the target PassportSpec's head-height and eye-line ratios.
 */
object FaceCropUtils {

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .build()

    suspend fun detectFaceBounds(bitmap: Bitmap): Rect? = suspendCancellableCoroutine { cont ->
        val detector = FaceDetection.getClient(detectorOptions)
        val input = InputImage.fromBitmap(bitmap, 0)
        detector.process(input)
            .addOnSuccessListener { faces ->
                val box = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                    ?.boundingBox
                cont.resume(box)
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    /**
     * Crops [bitmap] around [faceBox] so the resulting image obeys [spec]'s
     * head-height ratio. Falls back to a center-crop at the target aspect ratio
     * if no face was detected.
     */
    fun cropToSpec(
        bitmap: Bitmap,
        faceBox: Rect?,
        spec: PassportSpec,
        targetAspectRatio: Float
    ): Bitmap {
        if (faceBox == null) {
            return centerCropToAspect(bitmap, targetAspectRatio)
        }

        val headHeightPx = faceBox.height().toFloat()
        // frame height such that headHeightPx / frameHeight == spec.headHeightRatio
        val frameHeight = (headHeightPx / spec.headHeightRatio).toInt()
        val frameWidth = (frameHeight * targetAspectRatio).toInt()

        // Position the frame so the face's vertical center sits at the eye-line target.
        val desiredTop = (faceBox.centerY() - frameHeight * spec.eyeLineRatioFromTop).toInt()
        val desiredLeft = (faceBox.centerX() - frameWidth / 2f).toInt()

        val left = desiredLeft.coerceIn(0, (bitmap.width - frameWidth).coerceAtLeast(0))
        val top = desiredTop.coerceIn(0, (bitmap.height - frameHeight).coerceAtLeast(0))

        val safeWidth = frameWidth.coerceAtMost(bitmap.width - left).coerceAtLeast(1)
        val safeHeight = frameHeight.coerceAtMost(bitmap.height - top).coerceAtLeast(1)

        return Bitmap.createBitmap(bitmap, left, top, safeWidth, safeHeight)
    }

    private fun centerCropToAspect(bitmap: Bitmap, targetAspect: Float): Bitmap {
        val srcAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        return if (srcAspect > targetAspect) {
            // source too wide -> crop width
            val newWidth = (bitmap.height * targetAspect).toInt()
            val left = (bitmap.width - newWidth) / 2
            Bitmap.createBitmap(bitmap, left.coerceAtLeast(0), 0, newWidth, bitmap.height)
        } else {
            // source too tall -> crop height
            val newHeight = (bitmap.width / targetAspect).toInt()
            val top = (bitmap.height - newHeight) / 2
            Bitmap.createBitmap(bitmap, 0, top.coerceAtLeast(0), bitmap.width, newHeight)
        }
    }
}
