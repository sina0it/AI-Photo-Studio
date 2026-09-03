package com.example.editor

import android.graphics.ColorMatrix

object FilterMatrixFactory {

    fun getFilterMatrix(type: FilterType, intensity: Float = 1.0f): ColorMatrix {
        val baseMatrix = when (type) {
            FilterType.NONE -> ColorMatrix()

            FilterType.CINEMATIC -> {
                // Rich teal & orange look: boosted contrast, slightly crushed blues, warm highlights
                ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0f, 0f, 10f,
                        0f, 1.05f, 0f, 0f, 5f,
                        0f, 0f, 0.9f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.PORTRAIT -> {
                // Soft flattering skin tones, gentle highlights, mild pink-peach warmth
                ColorMatrix(
                    floatArrayOf(
                        1.08f, 0f, 0f, 0f, 8f,
                        0f, 1.02f, 0f, 0f, 4f,
                        0f, 0f, 0.98f, 0f, 2f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.VINTAGE -> {
                // Nostalgic muted sepia-wash with lifted blacks
                ColorMatrix(
                    floatArrayOf(
                        0.9f, 0.2f, 0.1f, 0f, 30f,
                        0.1f, 0.8f, 0.1f, 0f, 20f,
                        0.1f, 0.1f, 0.6f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.BW -> {
                // High contrast dramatic monochrome
                ColorMatrix().apply {
                    setSaturation(0f)
                    val contrast = 1.25f
                    val cm = ColorMatrix(
                        floatArrayOf(
                            contrast, 0f, 0f, 0f, -10f,
                            0f, contrast, 0f, 0f, -10f,
                            0f, 0f, contrast, 0f, -10f,
                            0f, 0f, 0f, 1f, 0f
                        )
                    )
                    postConcat(cm)
                }
            }

            FilterType.WARM -> {
                // Golden hour glow
                ColorMatrix(
                    floatArrayOf(
                        1.15f, 0f, 0f, 0f, 20f,
                        0f, 1.05f, 0f, 0f, 10f,
                        0f, 0f, 0.85f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.COOL -> {
                // Nordic cold blue tint
                ColorMatrix(
                    floatArrayOf(
                        0.9f, 0f, 0f, 0f, -5f,
                        0f, 1.0f, 0f, 0f, 0f,
                        0f, 0f, 1.2f, 0f, 20f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.FASHION -> {
                // Vogue editorial: crisp blacks, punchy reds, elegant vibrance
                ColorMatrix(
                    floatArrayOf(
                        1.25f, 0f, 0f, 0f, 5f,
                        0f, 1.15f, 0f, 0f, 0f,
                        0f, 0f, 1.1f, 0f, 5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.NIGHT -> {
                // Cyberpunk neon: deep shadows, vibrant magenta & electric cyan
                ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0.2f, 0f, -10f,
                        0f, 1.0f, 0f, 0f, -5f,
                        0.1f, 0f, 1.3f, 0f, 15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.TRAVEL -> {
                // Travel blogger pop: vibrant skies and greens
                ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, 5f,
                        0f, 1.12f, 0f, 0f, 8f,
                        0f, 0f, 1.15f, 0f, 12f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.NATURE -> {
                // Emerald greenery enhancement
                ColorMatrix(
                    floatArrayOf(
                        0.95f, 0f, 0f, 0f, 0f,
                        0f, 1.25f, 0f, 0f, 15f,
                        0f, 0f, 0.95f, 0f, -5f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.HDR -> {
                // High dynamic range punch: high local contrast & micro-detail
                ColorMatrix(
                    floatArrayOf(
                        1.3f, 0f, 0f, 0f, -15f,
                        0f, 1.3f, 0f, 0f, -15f,
                        0f, 0f, 1.3f, 0f, -15f,
                        0f, 0f, 0f, 1f, 0f
                    )
                ).apply {
                    val sat = ColorMatrix().apply { setSaturation(1.25f) }
                    postConcat(sat)
                }
            }

            FilterType.FILM -> {
                // Analog 35mm film emulation: faded blacks, warm greens, subtle grain tone
                ColorMatrix(
                    floatArrayOf(
                        1.05f, 0.05f, 0f, 0f, 15f,
                        0f, 1.02f, 0.05f, 0f, 12f,
                        0.05f, 0f, 0.92f, 0f, 18f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            FilterType.RETRO -> {
                // 90s Polaroid retro glow
                ColorMatrix(
                    floatArrayOf(
                        1.1f, 0.1f, 0f, 0f, 25f,
                        0f, 1.0f, 0.1f, 0f, 15f,
                        0f, 0.1f, 0.8f, 0f, 35f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
        }

        if (intensity >= 0.99f || type == FilterType.NONE) {
            return baseMatrix
        }

        // Interpolate between Identity and baseMatrix according to intensity
        val identity = ColorMatrix()
        val identityArray = identity.array
        val targetArray = baseMatrix.array
        val blended = FloatArray(20)
        for (i in 0 until 20) {
            blended[i] = identityArray[i] + (targetArray[i] - identityArray[i]) * intensity
        }
        return ColorMatrix(blended)
    }

    fun getAdjustmentsMatrix(adj: AdjustmentsState): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Brightness (-100..100) -> -100f..100f offset
        // 2. Exposure (-100..100) -> multiplicative scaling
        val exposureScale = 1.0f + (adj.exposure / 100f) * 0.8f
        val brightnessOffset = (adj.brightness * 1.2f)

        // 3. Contrast (-100..100) -> 0.2f..2.0f
        val contrastFactor = if (adj.contrast >= 0f) {
            1.0f + (adj.contrast / 100f) * 1.0f
        } else {
            1.0f + (adj.contrast / 100f) * 0.7f
        }
        val contrastOffset = (-0.5f * contrastFactor + 0.5f) * 255f

        val cmBrightnessContrast = ColorMatrix(
            floatArrayOf(
                contrastFactor * exposureScale, 0f, 0f, 0f, contrastOffset + brightnessOffset,
                0f, contrastFactor * exposureScale, 0f, 0f, contrastOffset + brightnessOffset,
                0f, 0f, contrastFactor * exposureScale, 0f, contrastOffset + brightnessOffset,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(cmBrightnessContrast)

        // 4. Saturation (-100..100) -> 0f..2.0f
        if (adj.saturation != 0f) {
            val satFactor = 1.0f + (adj.saturation / 100f)
            val cmSat = ColorMatrix().apply { setSaturation(satFactor.coerceAtLeast(0f)) }
            matrix.postConcat(cmSat)
        }

        // 5. Temperature / Warmth (-100..100)
        // Warmth: boost red, reduce blue
        if (adj.temperature != 0f) {
            val rShift = (adj.temperature / 100f) * 25f
            val bShift = -(adj.temperature / 100f) * 25f
            val cmTemp = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, rShift,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, bShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmTemp)
        }

        // 6. Tint (-100..100) -> Green vs Magenta
        if (adj.tint != 0f) {
            val gShift = -(adj.tint / 100f) * 20f
            val rbShift = (adj.tint / 100f) * 12f
            val cmTint = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, rbShift,
                    0f, 1f, 0f, 0f, gShift,
                    0f, 0f, 1f, 0f, rbShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmTint)
        }

        // 7. Highlights (-100..100) & Shadows (-100..100)
        if (adj.highlights != 0f || adj.shadows != 0f) {
            val hlShift = (adj.highlights / 100f) * 18f
            val shShift = (adj.shadows / 100f) * 22f
            val cmTones = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, hlShift + shShift,
                    0f, 1f, 0f, 0f, hlShift + shShift,
                    0f, 0f, 1f, 0f, hlShift + shShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmTones)
        }

        // 8. Fade (0..100) -> Lifts the floor (blacks become dark charcoal) and softens high contrast
        if (adj.fade > 0f) {
            val fadeRatio = (adj.fade / 100f)
            val floorLift = fadeRatio * 42f
            val contrastSoft = 1.0f - (fadeRatio * 0.22f)
            val cmFade = ColorMatrix(
                floatArrayOf(
                    contrastSoft, 0f, 0f, 0f, floorLift,
                    0f, contrastSoft, 0f, 0f, floorLift,
                    0f, 0f, contrastSoft, 0f, floorLift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmFade)
        }

        return matrix
    }

    fun getBeautyMatrix(beauty: BeautyState): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Skin tone warmth & peach shift (-100..100)
        if (beauty.skinTone != 0f) {
            val warmShift = (beauty.skinTone / 100f) * 16f
            val peachShift = (beauty.skinTone / 100f) * 8f
            val cmTone = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, warmShift,
                    0f, 1f, 0f, 0f, peachShift,
                    0f, 0f, 1f, 0f, -warmShift * 0.5f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmTone)
        }

        // 2. Facial brightness (-100..100)
        if (beauty.brightness != 0f) {
            val bShift = (beauty.brightness / 100f) * 35f
            val cmBright = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, bShift,
                    0f, 1f, 0f, 0f, bShift,
                    0f, 0f, 1f, 0f, bShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmBright)
        }

        // 3. Teeth Whitening (0..100): desaturates yellow tones and lifts whites
        if (beauty.teeth > 0f) {
            val factor = (beauty.teeth / 100f) * 15f
            val cmTeeth = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, factor * 0.5f,
                    0f, 1f, 0f, 0f, factor * 0.5f,
                    0f, 0f, 1.05f, 0f, factor * 1.2f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmTeeth)
        }

        // 4. Eye Brightness (0..100): boosts high-contrast clarity
        if (beauty.eyeBrightness > 0f) {
            val eyeFactor = 1f + (beauty.eyeBrightness / 100f) * 0.2f
            val cmEyes = ColorMatrix(
                floatArrayOf(
                    eyeFactor, 0f, 0f, 0f, 2f,
                    0f, eyeFactor, 0f, 0f, 2f,
                    0f, 0f, eyeFactor, 0f, 4f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(cmEyes)
        }

        return matrix
    }
}
