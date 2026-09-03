package com.example.ai

import android.graphics.Bitmap

sealed class AIResult<out T> {
    data class Success<T>(
        val data: T,
        val engineName: String = "AI Engine Pro",
        val executionTimeMs: Long = 0
    ) : AIResult<T>()

    data class NotConfigured(
        val message: String,
        val configKeyRequired: String = "GEMINI_API_KEY",
        val documentationUrl: String = "https://ai.studio/build"
    ) : AIResult<Nothing>()

    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : AIResult<Nothing>()
}

interface AIImageEngine {
    val isConfigured: Boolean
    val statusDescription: String

    suspend fun editImage(image: Bitmap, prompt: String): Result<Bitmap>
    suspend fun editWithPrompt(source: Bitmap, prompt: String): AIResult<Bitmap>
    suspend fun removeObject(source: Bitmap, mask: Bitmap): AIResult<Bitmap>
    suspend fun replaceBackground(source: Bitmap, stylePrompt: String): AIResult<Bitmap>
    suspend fun generativeFill(source: Bitmap, mask: Bitmap, prompt: String): AIResult<Bitmap>
    suspend fun modifyClothing(source: Bitmap, stylePrompt: String): AIResult<Bitmap>
    suspend fun autoEnhance(source: Bitmap): AIResult<Bitmap>
    suspend fun generateStyleFilter(source: Bitmap, stylePrompt: String): AIResult<Bitmap>
}
