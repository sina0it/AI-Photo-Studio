package com.example.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.Random
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ImageProcessor {

    suspend fun loadOptimizedBitmap(
        context: Context,
        uri: Uri,
        maxDimension: Int = 2048
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            var input: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()

            var sampleSize = 1
            val maxSide = max(options.outWidth, options.outHeight)
            while (maxSide / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            input = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(input, null, decodeOptions)
            input?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun renderPreview(
        source: Bitmap,
        state: EditorStateSnapshot
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height

        // 1. Transform matrix (rotation, flip, straighten angle)
        val matrix = Matrix()
        val totalRotation = (state.transform.rotation + state.transform.straightenAngle)
        if (totalRotation != 0f) {
            matrix.postRotate(totalRotation, width / 2f, height / 2f)
        }
        val sx = if (state.transform.flipH) -1f else 1f
        val sy = if (state.transform.flipV) -1f else 1f
        if (sx != 1f || sy != 1f) {
            matrix.postScale(sx, sy, width / 2f, height / 2f)
        }

        val transformed = if (!matrix.isIdentity) {
            Bitmap.createBitmap(source, 0, 0, width, height, matrix, true)
        } else {
            source.copy(Bitmap.Config.ARGB_8888, true)
        }

        // 2. Adjustments & Filters & Beauty Color Matrices
        val adjMatrix = FilterMatrixFactory.getAdjustmentsMatrix(state.adjustments)
        val filterMatrix = FilterMatrixFactory.getFilterMatrix(state.filterType, state.filterIntensity)
        val beautyMatrix = FilterMatrixFactory.getBeautyMatrix(state.beauty)

        val totalColorMatrix = ColorMatrix()
        totalColorMatrix.postConcat(adjMatrix)
        totalColorMatrix.postConcat(filterMatrix)
        totalColorMatrix.postConcat(beautyMatrix)

        var output = Bitmap.createBitmap(transformed.width, transformed.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = ColorMatrixColorFilter(totalColorMatrix)

        canvas.drawBitmap(transformed, 0f, 0f, paint)

        // 3. Blur (0..100)
        if (state.adjustments.blur > 0f) {
            output = applyFastBlur(output, (state.adjustments.blur * 0.25f).coerceIn(1f, 25f))
        }

        // 4. Skin Smoothing (0..100)
        if (state.beauty.smoothing > 0f) {
            output = applySkinSmoothing(output, state.beauty.smoothing / 100f)
        }

        // 5. Sharpness (0..100)
        if (state.adjustments.sharpness > 0f) {
            output = applySharpen(output, state.adjustments.sharpness / 100f)
        }

        val finalCanvas = Canvas(output)

        // 6. Face / Center Studio Lighting (0..100)
        if (state.beauty.faceLighting > 0f) {
            val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val cx = output.width / 2f
            val cy = output.height * 0.45f
            val radius = max(cx, cy) * 0.85f
            val alpha = (state.beauty.faceLighting / 100f * 95).toInt().coerceIn(0, 255)
            val lightGradient = RadialGradient(
                cx, cy, radius,
                intArrayOf(Color.argb(alpha, 255, 248, 235), Color.TRANSPARENT),
                floatArrayOf(0f, 1.0f),
                Shader.TileMode.CLAMP
            )
            lightPaint.shader = lightGradient
            finalCanvas.drawRect(0f, 0f, output.width.toFloat(), output.height.toFloat(), lightPaint)
        }

        // 7. Vignette (0..100)
        if (state.adjustments.vignette > 0f) {
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val cx = output.width / 2f
            val cy = output.height / 2f
            val radius = max(cx, cy) * 1.2f
            val alpha = (state.adjustments.vignette / 100f * 220).toInt().coerceIn(0, 255)
            val gradient = RadialGradient(
                cx, cy, radius,
                intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, Color.argb(alpha, 0, 0, 0)),
                floatArrayOf(0f, 0.40f, 1.0f),
                Shader.TileMode.CLAMP
            )
            vignettePaint.shader = gradient
            finalCanvas.drawRect(0f, 0f, output.width.toFloat(), output.height.toFloat(), vignettePaint)
        }

        // 8. Film Grain (0..100)
        if (state.adjustments.grain > 0f) {
            applyGrainOverlay(finalCanvas, output.width, output.height, state.adjustments.grain / 100f)
        }

        // 9. Overlays (Text & Stickers)
        drawOverlays(finalCanvas, output.width.toFloat(), output.height.toFloat(), state.textOverlays, state.stickerOverlays)

        output
    }

    suspend fun cropBitmap(
        source: Bitmap,
        leftRatio: Float,
        topRatio: Float,
        rightRatio: Float,
        bottomRatio: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        val w = source.width
        val h = source.height

        val left = (leftRatio * w).roundToInt().coerceIn(0, w - 1)
        val top = (topRatio * h).roundToInt().coerceIn(0, h - 1)
        val right = (rightRatio * w).roundToInt().coerceIn(left + 1, w)
        val bottom = (bottomRatio * h).roundToInt().coerceIn(top + 1, h)

        val cropWidth = max(1, right - left)
        val cropHeight = max(1, bottom - top)

        Bitmap.createBitmap(source, left, top, cropWidth, cropHeight)
    }

    suspend fun scaleToResolution(source: Bitmap, targetDimension: Int): Bitmap = withContext(Dispatchers.Default) {
        if (targetDimension <= 0) return@withContext source
        val currentMax = max(source.width, source.height)
        if (currentMax <= targetDimension) return@withContext source

        val scale = targetDimension.toFloat() / currentMax
        val targetW = max(1, (source.width * scale).roundToInt())
        val targetH = max(1, (source.height * scale).roundToInt())
        Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun applyFastBlur(source: Bitmap, radius: Float): Bitmap {
        val downscaleFactor = 4
        val smallW = max(1, source.width / downscaleFactor)
        val smallH = max(1, source.height / downscaleFactor)

        val small = Bitmap.createScaledBitmap(source, smallW, smallH, true)
        val blurred = Bitmap.createScaledBitmap(small, source.width, source.height, true)
        return blurred
    }

    private fun applySkinSmoothing(source: Bitmap, amount: Float): Bitmap {
        // Fast bilateral-like skin softening: blend smooth downsampled copy with source
        val blurred = applyFastBlur(source, 6f)
        val blended = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(blended)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawBitmap(source, 0f, 0f, paint)
        paint.alpha = (amount * 175).roundToInt().coerceIn(0, 255)
        canvas.drawBitmap(blurred, 0f, 0f, paint)
        return blended
    }

    private fun applySharpen(source: Bitmap, intensity: Float): Bitmap {
        // High-pass sharpening blend
        val blurred = applyFastBlur(source, 2f)
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawBitmap(source, 0f, 0f, paint)

        // Overlay with slight contrast enhancement
        val sharpenMatrix = ColorMatrix(
            floatArrayOf(
                1f + intensity * 0.6f, 0f, 0f, 0f, -intensity * 12f,
                0f, 1f + intensity * 0.6f, 0f, 0f, -intensity * 12f,
                0f, 0f, 1f + intensity * 0.6f, 0f, -intensity * 12f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(sharpenMatrix)
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    private fun applyGrainOverlay(canvas: Canvas, w: Int, h: Int, grainStrength: Float) {
        val grainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = 1.2f
        }
        val random = Random(42)
        val pointCount = (w * h * 0.0003f * grainStrength).roundToInt().coerceIn(300, 8000)
        val alpha = (grainStrength * 140).roundToInt().coerceIn(10, 200)
        grainPaint.alpha = alpha

        val points = FloatArray(pointCount * 2)
        for (i in 0 until pointCount) {
            points[i * 2] = random.nextFloat() * w
            points[i * 2 + 1] = random.nextFloat() * h
        }
        canvas.drawPoints(points, grainPaint)
    }

    private fun drawOverlays(
        canvas: Canvas,
        w: Float,
        h: Float,
        texts: List<TextOverlay>,
        stickers: List<StickerOverlay>
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        for (text in texts) {
            val style = when {
                text.isBold && text.isItalic -> Typeface.BOLD_ITALIC
                text.isBold -> Typeface.BOLD
                text.isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            textPaint.typeface = Typeface.create(text.fontFamily, style)
            textPaint.color = text.color
            textPaint.alpha = (text.opacity * 255).roundToInt().coerceIn(0, 255)
            textPaint.textSize = text.sizeSp * (w / 420f).coerceAtLeast(1f) * text.scale

            textPaint.textAlign = when (text.alignment) {
                0 -> Paint.Align.LEFT
                2 -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }

            if (text.hasShadow) {
                textPaint.setShadowLayer(8f, 2f, 2f, text.shadowColor)
            } else {
                textPaint.clearShadowLayer()
            }

            val xPos = text.xRatio * w
            val yPos = text.yRatio * h

            canvas.save()
            canvas.translate(xPos, yPos)
            if (text.rotationAngle != 0f) {
                canvas.rotate(text.rotationAngle)
            }

            if (text.hasBackground) {
                val bounds = android.graphics.Rect()
                textPaint.getTextBounds(text.text, 0, text.text.length, bounds)
                val padX = 16f * (w / 420f).coerceAtLeast(1f)
                val padY = 8f * (w / 420f).coerceAtLeast(1f)
                val bgRect = when (text.alignment) {
                    0 -> RectF(0f - padX, bounds.top - padY, bounds.width() + padX, bounds.bottom + padY)
                    2 -> RectF(-bounds.width() - padX, bounds.top - padY, padX, bounds.bottom + padY)
                    else -> RectF(-bounds.width() / 2f - padX, bounds.top - padY, bounds.width() / 2f + padX, bounds.bottom + padY)
                }
                bgPaint.color = text.backgroundColor
                canvas.drawRoundRect(bgRect, 12f, 12f, bgPaint)
            }

            canvas.drawText(text.text, 0f, 0f, textPaint)
            canvas.restore()
        }

        val stickerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }
        for (sticker in stickers) {
            stickerPaint.textSize = sticker.sizeDp * (w / 350f).coerceAtLeast(1f)
            canvas.drawText(sticker.emojiOrIcon, sticker.xRatio * w, sticker.yRatio * h, stickerPaint)
        }
    }
}
