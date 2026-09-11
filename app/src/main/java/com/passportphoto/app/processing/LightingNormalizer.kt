package com.passportphoto.app.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

/**
 * Lightweight brightness/contrast normalization using Android's native ColorMatrix
 * (no external CV library required, keeps the build simple). This auto-levels
 * exposure based on the image's mean luminance and applies a mild contrast boost —
 * good enough for typical passport-photo compliance without heavy dependencies.
 *
 * For more advanced local-contrast enhancement (CLAHE-style), OpenCV can be added
 * later as an optional module — see README.
 */
object LightingNormalizer {

    fun normalize(bitmap: Bitmap, contrastBoost: Float = 1.15f, manualBrightness: Float = 0f): Bitmap {
        val meanLuminance = estimateMeanLuminance(bitmap)
        // Target mid-gray luminance ~ 140/255; nudge brightness toward it.
        val targetLuminance = 140f
        val autoBrightnessOffset = (targetLuminance - meanLuminance).coerceIn(-40f, 40f)
        val totalBrightness = autoBrightnessOffset + manualBrightness

        val colorMatrix = ColorMatrix().apply {
            val scale = contrastBoost
            val translate = (-.5f * scale + .5f) * 255f + totalBrightness
            set(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }

        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(colorMatrix) }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return output
    }

    private fun estimateMeanLuminance(bitmap: Bitmap): Float {
        // Sample a grid of pixels rather than every pixel, for speed.
        val sampleStepX = (bitmap.width / 50).coerceAtLeast(1)
        val sampleStepY = (bitmap.height / 50).coerceAtLeast(1)
        var sum = 0L
        var count = 0
        var y = 0
        while (y < bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                sum += (0.299 * r + 0.587 * g + 0.114 * b).toLong()
                count++
                x += sampleStepX
            }
            y += sampleStepY
        }
        return if (count == 0) 128f else sum.toFloat() / count
    }
}
