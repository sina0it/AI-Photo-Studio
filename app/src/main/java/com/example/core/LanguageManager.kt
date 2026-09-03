package com.example.core

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class LanguageItem(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean
)

class LanguageManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_studio_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_LANGUAGE = "pref_selected_language"
        const val PREF_FIRST_LAUNCH = "pref_is_first_launch"
        const val PREF_THEME = "pref_theme_mode" // "system", "light", "dark"
        const val PREF_EXPORT_QUALITY = "pref_export_quality" // 50..100

        val SUPPORTED_LANGUAGES = listOf(
            LanguageItem("fa", "Persian", "فارسی", isRtl = true),
            LanguageItem("en", "English", "English", isRtl = false),
            LanguageItem("ar", "Arabic", "العربية", isRtl = true),
            LanguageItem("zh", "Chinese", "中文", isRtl = false),
            LanguageItem("ru", "Russian", "Русский", isRtl = false)
        )
    }

    private val _currentLanguage = MutableStateFlow(getSavedLanguage())
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(PREF_THEME, "dark") ?: "dark")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    val isFirstLaunch: Boolean
        get() = prefs.getBoolean(PREF_FIRST_LAUNCH, true)

    fun completeFirstLaunch() {
        prefs.edit().putBoolean(PREF_FIRST_LAUNCH, false).apply()
    }

    fun getSavedLanguage(): String {
        return prefs.getString(PREF_LANGUAGE, "en") ?: "en"
    }

    fun setLanguage(langCode: String) {
        prefs.edit().putString(PREF_LANGUAGE, langCode).apply()
        _currentLanguage.value = langCode
        updateAppLocale(context, langCode)
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(PREF_THEME, mode).apply()
        _themeMode.value = mode
    }

    fun isRtl(): Boolean {
        val lang = _currentLanguage.value
        return lang == "fa" || lang == "ar"
    }

    fun getLayoutDirection(): LayoutDirection {
        return if (isRtl()) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    fun updateAppLocale(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }
}
