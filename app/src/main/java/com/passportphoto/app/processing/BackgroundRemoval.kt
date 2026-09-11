package com.passportphoto.app.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.google.mlkit.vision.segmentation.Segmentation
import com.passportphoto.app.model.BackgroundChoice
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.nio.ByteBuffer

/**
 * Uses ML Kit Selfie Segmentation to separate subject from background, then
 * composites the subject over a solid passport-compliant background color.
 */
object BackgroundRemoval {

    private val options = SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
        .enableRawSizeMask()
        .build()

    suspend fun replaceBackground(
        bitmap: Bitmap,
        background: BackgroundChoice
    ): Bitmap = suspendCancellableCoroutine { cont ->
        val segmenter = Segmentation.getClient(options)
        val input = InputImage.fromBitmap(bitmap, 0)

        segmenter.process(input)
            .addOnSuccessListener { mask ->
                try {
                    val maskBuffer = mask.buffer
                    val maskWidth = mask.width
                    val maskHeight = mask.height

                    val output = Bitmap.createBitmap(
                        bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(output)
                    canvas.drawColor(background.colorArgb)

                    val masked = applyConfidenceMask(
                        bitmap, maskBuffer, maskWidth, maskHeight
                    )
                    canvas.drawBitmap(masked, 0f, 0f, null)

                    cont.resume(output)
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
            .addOnFailureListener { e -> cont.resumeWithException(e) }
    }

    /**
     * Builds an ARGB bitmap where each pixel's alpha channel is set from the
     * segmentation confidence mask (foreground = opaque, background = transparent),
     * scaled from the mask's resolution to the source bitmap's resolution.
     */
    private fun applyConfidenceMask(
        source: Bitmap,
        maskBuffer: ByteBuffer,
        maskWidth: Int,
        maskHeight: Int
    ): Bitmap {
        maskBuffer.rewind()
        val confidences = FloatArray(maskWidth * maskHeight)
        for (i in confidences.indices) {
            confidences[i] = maskBuffer.getFloat()
        }

        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(source.width * source.height)
        source.getPixels(srcPixels, 0, source.width, 0, 0, source.width, source.height)

        val outPixels = IntArray(source.width * source.height)
        for (y in 0 until source.height) {
            val maskY = (y.toFloat() / source.height * maskHeight).toInt().coerceIn(0, maskHeight - 1)
            for (x in 0 until source.width) {
                val maskX = (x.toFloat() / source.width * maskWidth).toInt().coerceIn(0, maskWidth - 1)
                val confidence = confidences[maskY * maskWidth + maskX]
                val alpha = (confidence * 255).toInt().coerceIn(0, 255)
                val srcPixel = srcPixels[y * source.width + x]
                val rgb = srcPixel and 0x00FFFFFF
                outPixels[y * source.width + x] = (alpha shl 24) or rgb
            }
        }
        result.setPixels(outPixels, 0, source.width, 0, 0, source.width, source.height)
        return result
    }
}
