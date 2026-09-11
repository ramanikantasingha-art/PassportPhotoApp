package com.passportphoto.app.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import com.passportphoto.app.model.CapturedPhoto
import com.passportphoto.app.model.PhotoOrientation
import java.io.File

/**
 * Reads EXIF rotation from a captured JPEG file, rotates the decoded bitmap so its
 * width/height reflect what the user actually saw in the viewfinder, and classifies
 * the result as PORTRAIT or LANDSCAPE. This is the single source of truth the rest
 * of the pipeline (crop, canvas layout) relies on for "smart orientation matching".
 */
object OrientationResolver {

    fun resolve(file: File, rawBitmap: Bitmap): CapturedPhoto {
        val rotationDegrees = readExifRotationDegrees(file)
        val normalized = rotateBitmap(rawBitmap, rotationDegrees)

        val orientation = if (normalized.width < normalized.height) {
            PhotoOrientation.PORTRAIT
        } else {
            PhotoOrientation.LANDSCAPE
        }

        return CapturedPhoto(bitmap = normalized, orientation = orientation)
    }

    private fun readExifRotationDegrees(file: File): Int {
        return try {
            val exif = ExifInterface(file.absolutePath)
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * Given the orientation the photo was captured in, returns the aspect ratio
     * (width / height) the passport photo cell should use. For non-square specs
     * (e.g. 35x45mm) we swap width/height when the capture was landscape, so the
     * final passport photo "matches" how the user held the phone.
     */
    fun aspectRatioFor(orientation: PhotoOrientation, baseWidthMm: Float, baseHeightMm: Float): Float {
        return if (orientation == PhotoOrientation.PORTRAIT) {
            baseWidthMm / baseHeightMm
        } else {
            baseHeightMm / baseWidthMm
        }
    }
}
