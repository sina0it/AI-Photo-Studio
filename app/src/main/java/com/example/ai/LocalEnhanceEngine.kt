package com.example.ai

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

object LocalEnhanceEngine {

    fun performSmartEnhance(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        // Balanced tone curve matrix: slight contrast boost (+15%), exposure lift (+8%), vibrance (+12%)
        val contrast = 1.15f
        val brightness = 10f
        val saturation = 1.18f

        val cmContrast = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val cmSat = ColorMatrix().apply { setSaturation(saturation) }

        // Slight warm tint for skin appearance
        val cmWarmth = ColorMatrix(
            floatArrayOf(
                1.04f, 0f, 0f, 0f, 2f,
                0f, 1.01f, 0f, 0f, 1f,
                0f, 0f, 0.96f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )

        val finalMatrix = ColorMatrix()
        finalMatrix.postConcat(cmContrast)
        finalMatrix.postConcat(cmSat)
        finalMatrix.postConcat(cmWarmth)

        paint.colorFilter = ColorMatrixColorFilter(finalMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)

        return output
    }
}
