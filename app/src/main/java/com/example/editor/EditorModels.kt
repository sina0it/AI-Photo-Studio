package com.example.editor

import android.graphics.Color as AndroidColor

enum class FilterType(val id: String, val displayNameRes: Int) {
    NONE("none", com.example.R.string.filter_none),
    CINEMATIC("cinematic", com.example.R.string.filter_cinematic),
    PORTRAIT("portrait", com.example.R.string.filter_portrait),
    VINTAGE("vintage", com.example.R.string.filter_vintage),
    BW("bw", com.example.R.string.filter_bw),
    WARM("warm", com.example.R.string.filter_warm),
    COOL("cool", com.example.R.string.filter_cool),
    FASHION("fashion", com.example.R.string.filter_fashion),
    NIGHT("night", com.example.R.string.filter_night),
    TRAVEL("travel", com.example.R.string.filter_travel),
    NATURE("nature", com.example.R.string.filter_nature),
    HDR("hdr", com.example.R.string.filter_hdr),
    FILM("film", com.example.R.string.filter_film),
    RETRO("retro", com.example.R.string.filter_retro)
}

data class AdjustmentsState(
    val brightness: Float = 0f,      // -100..100
    val contrast: Float = 0f,        // -100..100
    val saturation: Float = 0f,      // -100..100
    val exposure: Float = 0f,        // -100..100
    val highlights: Float = 0f,      // -100..100
    val shadows: Float = 0f,         // -100..100
    val temperature: Float = 0f,     // -100..100
    val tint: Float = 0f,            // -100..100
    val sharpness: Float = 0f,       // 0..100
    val blur: Float = 0f,            // 0..100
    val vignette: Float = 0f,        // 0..100
    val fade: Float = 0f,            // 0..100
    val grain: Float = 0f            // 0..100
) {
    fun isDefault(): Boolean =
        brightness == 0f && contrast == 0f && saturation == 0f &&
                exposure == 0f && highlights == 0f && shadows == 0f &&
                temperature == 0f && tint == 0f && sharpness == 0f &&
                blur == 0f && vignette == 0f && fade == 0f && grain == 0f
}

data class TransformState(
    val rotation: Int = 0,             // 0, 90, 180, 270
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    val straightenAngle: Float = 0f,   // -45..45
    val cropAspect: CropAspect = CropAspect.ORIGINAL
)

enum class CropAspect(val ratio: Float?, val label: String) {
    ORIGINAL(null, "Original"),
    FREE(null, "Free"),
    SQUARE(1f, "1:1"),
    PORTRAIT_4_5(4f / 5f, "4:5"),
    PHOTO_3_4(3f / 4f, "3:4"),
    LANDSCAPE_4_3(4f / 3f, "4:3"),
    LANDSCAPE_16_9(16f / 9f, "16:9"),
    STORY_9_16(9f / 16f, "9:16")
}

data class BeautyState(
    val smoothing: Float = 0f,        // 0..100
    val brightness: Float = 0f,       // -100..100
    val skinTone: Float = 0f,         // -100..100
    val teeth: Float = 0f,            // 0..100
    val eyeBrightness: Float = 0f,    // 0..100
    val faceLighting: Float = 0f,     // 0..100
    val blemish: Float = 0f,          // 0..100
    val foundation: Float = 0f,
    val concealer: Float = 0f,
    val blush: Float = 0f,
    val lipstick: Float = 0f,
    val eyeliner: Float = 0f,
    val eyeshadow: Float = 0f,
    val eyelashes: Float = 0f,
    val eyebrows: Float = 0f,
    val hairColor: Float = 0f,
    val faceShape: Float = 0f,
    val jaw: Float = 0f,
    val cheeks: Float = 0f,
    val nose: Float = 0f,
    val chin: Float = 0f
) {
    fun isDefault(): Boolean =
        smoothing == 0f && brightness == 0f && skinTone == 0f &&
                teeth == 0f && eyeBrightness == 0f && faceLighting == 0f &&
                blemish == 0f && foundation == 0f && concealer == 0f &&
                blush == 0f && lipstick == 0f && eyeliner == 0f &&
                eyeshadow == 0f && eyelashes == 0f && eyebrows == 0f &&
                hairColor == 0f && faceShape == 0f && jaw == 0f &&
                cheeks == 0f && nose == 0f && chin == 0f
}

data class BodyState(
    val slim: Float = 0f,
    val enlarge: Float = 0f,
    val waist: Float = 0f,
    val shoulders: Float = 0f,
    val arms: Float = 0f,
    val legs: Float = 0f,
    val hips: Float = 0f,
    val chest: Float = 0f,
    val height: Float = 0f,
    val proportions: Float = 0f
) {
    fun isDefault(): Boolean =
        slim == 0f && enlarge == 0f && waist == 0f &&
                shoulders == 0f && arms == 0f && legs == 0f &&
                hips == 0f && chest == 0f && height == 0f && proportions == 0f
}

data class BackgroundState(
    val mode: BackgroundMode = BackgroundMode.NONE,
    val blurRadius: Float = 0f,
    val color: Int = AndroidColor.TRANSPARENT,
    val isGradient: Boolean = false,
    val gradientColorEnd: Int = AndroidColor.TRANSPARENT
)

enum class BackgroundMode {
    NONE, BLUR, COLOR, REPLACE, AI_GEN
}

data class TextOverlay(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val color: Int = AndroidColor.WHITE,
    val sizeSp: Float = 28f,
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val fontFamily: String = "sans-serif", // "sans-serif", "serif", "monospace", "cursive"
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val alignment: Int = 1, // 0 = Left, 1 = Center, 2 = Right
    val opacity: Float = 1.0f,
    val hasShadow: Boolean = true,
    val shadowColor: Int = AndroidColor.BLACK,
    val hasBackground: Boolean = false,
    val backgroundColor: Int = AndroidColor.argb(160, 0, 0, 0),
    val rotationAngle: Float = 0f,
    val scale: Float = 1.0f
)

data class StickerOverlay(
    val id: Long = System.currentTimeMillis(),
    val emojiOrIcon: String,
    val sizeDp: Float = 48f,
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f
)

data class EditorStateSnapshot(
    val adjustments: AdjustmentsState = AdjustmentsState(),
    val transform: TransformState = TransformState(),
    val filterType: FilterType = FilterType.NONE,
    val filterIntensity: Float = 0.8f,
    val beauty: BeautyState = BeautyState(),
    val body: BodyState = BodyState(),
    val background: BackgroundState = BackgroundState(),
    val textOverlays: List<TextOverlay> = emptyList(),
    val stickerOverlays: List<StickerOverlay> = emptyList()
)
