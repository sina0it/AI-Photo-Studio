package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val StudioDarkColorScheme = darkColorScheme(
    primary = SophisticatedIceBlue,
    onPrimary = SophisticatedNavyOnPrimary,
    primaryContainer = SophisticatedSurfaceVariantDark,
    onPrimaryContainer = SophisticatedIceBlue,
    secondary = SophisticatedSecondaryAccent,
    onSecondary = SophisticatedNavyOnPrimary,
    secondaryContainer = SophisticatedSurfaceDark,
    onSecondaryContainer = SophisticatedIceBlue,
    tertiary = SophisticatedAmber,
    onTertiary = SophisticatedNavyOnPrimary,
    background = SophisticatedBackgroundDark,
    onBackground = SophisticatedTextPrimaryDark,
    surface = SophisticatedSurfaceDark,
    onSurface = SophisticatedTextPrimaryDark,
    surfaceVariant = SophisticatedSurfaceVariantDark,
    onSurfaceVariant = SophisticatedTextSecondaryDark,
    outline = SophisticatedBorderDark,
    outlineVariant = SophisticatedBorderVariant,
    error = SophisticatedRose,
    onError = Color(0xFF690005)
)

private val StudioLightColorScheme = lightColorScheme(
    primary = StudioVioletPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF4C1D95),
    secondary = StudioCyanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF155E75),
    tertiary = StudioAmberTertiary,
    onTertiary = Color.White,
    background = StudioBackgroundLight,
    onBackground = StudioTextPrimaryLight,
    surface = StudioSurfaceLight,
    onSurface = StudioTextPrimaryLight,
    surfaceVariant = StudioSurfaceVariantLight,
    onSurfaceVariant = StudioTextSecondaryLight,
    outline = StudioBorderLight,
    error = StudioRose,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Photography apps need consistent color accuracy
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StudioDarkColorScheme
        else -> StudioLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
