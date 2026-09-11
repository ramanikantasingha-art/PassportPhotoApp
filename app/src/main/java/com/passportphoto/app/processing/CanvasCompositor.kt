package com.passportphoto.app.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.passportphoto.app.model.LayoutOption
import com.passportphoto.app.model.PassportSpec
import com.passportphoto.app.model.PhotoOrientation

/**
 * Composites N copies of a single processed passport photo onto a 4x6in @ 300DPI
 * canvas, arranged in a grid, with dashed cut guides. Supports the FULL_12 layout
 * (fills the whole sheet) and HALF_6 layout (fills only the top half, leaving the
 * bottom half blank).
 */
object CanvasCompositor {

    const val DPI = 300
    const val CANVAS_WIDTH_IN = 4f
    const val CANVAS_HEIGHT_IN = 6f
    val CANVAS_WIDTH_PX = (CANVAS_WIDTH_IN * DPI).toInt()   // 1200
    val CANVAS_HEIGHT_PX = (CANVAS_HEIGHT_IN * DPI).toInt() // 1800

    private const val MARGIN_IN = 0.08f // gap between photos, for cutting

    fun compose(
        processedPhoto: Bitmap,
        orientation: PhotoOrientation,
        spec: PassportSpec,
        layout: LayoutOption
    ): Bitmap {
        val canvasBitmap = Bitmap.createBitmap(
            CANVAS_WIDTH_PX, CANVAS_HEIGHT_PX, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(canvasBitmap)
        canvas.drawColor(Color.WHITE)

        val guidePaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }

        // Photo cell size in px at 300 DPI, honoring capture orientation:
        // swap width/height so a landscape capture yields a landscape passport photo.
        val mmToIn = 1f / 25.4f
        val specWidthIn = spec.photoWidthMm * mmToIn
        val specHeightIn = spec.photoHeightMm * mmToIn

        val cellWidthIn: Float
        val cellHeightIn: Float
        if (orientation == PhotoOrientation.PORTRAIT) {
            cellWidthIn = specWidthIn
            cellHeightIn = specHeightIn
        } else {
            cellWidthIn = specHeightIn
            cellHeightIn = specWidthIn
        }

        val cellWidthPx = (cellWidthIn * DPI).toInt()
        val cellHeightPx = (cellHeightIn * DPI).toInt()
        val marginPx = (MARGIN_IN * DPI).toInt()

        val scaledPhoto = Bitmap.createScaledBitmap(processedPhoto, cellWidthPx, cellHeightPx, true)

        // Figure out how many columns/rows fit, then pick a grid matching the
        // requested photo count (falls back to auto-fit if the exact 12/6 count
        // doesn't fit the chosen spec's dimensions).
        val maxCols = (CANVAS_WIDTH_PX / (cellWidthPx + marginPx)).coerceAtLeast(1)
        val usableHeightPx = when (layout) {
            LayoutOption.FULL_12 -> CANVAS_HEIGHT_PX
            LayoutOption.HALF_6 -> CANVAS_HEIGHT_PX / 2
        }
        val maxRows = (usableHeightPx / (cellHeightPx + marginPx)).coerceAtLeast(1)

        val targetCount = layout.photoCount
        var cols = maxCols
        var rows = kotlin.math.ceil(targetCount.toFloat() / cols).toInt()
        if (rows > maxRows) {
            rows = maxRows
            cols = kotlin.math.ceil(targetCount.toFloat() / rows).toInt().coerceAtMost(maxCols)
        }

        val totalGridWidthPx = cols * cellWidthPx + (cols - 1) * marginPx
        val totalGridHeightPx = rows * cellHeightPx + (rows - 1) * marginPx

        val startX = (CANVAS_WIDTH_PX - totalGridWidthPx) / 2
        val startY = when (layout) {
            LayoutOption.FULL_12 -> (CANVAS_HEIGHT_PX - totalGridHeightPx) / 2
            LayoutOption.HALF_6 -> (usableHeightPx - totalGridHeightPx) / 2
        }

        var placed = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (placed >= targetCount) break
                val left = startX + c * (cellWidthPx + marginPx)
                val top = startY + r * (cellHeightPx + marginPx)

                canvas.drawBitmap(scaledPhoto, left.toFloat(), top.toFloat(), null)
                canvas.drawRect(
                    left.toFloat(), top.toFloat(),
                    (left + cellWidthPx).toFloat(), (top + cellHeightPx).toFloat(),
                    guidePaint
                )
                placed++
            }
        }

        return canvasBitmap
    }
}
