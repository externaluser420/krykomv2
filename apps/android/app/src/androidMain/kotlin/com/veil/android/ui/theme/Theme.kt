package com.veil.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class VeilThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

val LocalVeilThemeMode = compositionLocalOf { VeilThemeMode.SYSTEM }
val LocalVeilThemeModeController =
    compositionLocalOf<(VeilThemeMode) -> Unit> { {} }

private val DarkColorScheme =
    darkColorScheme(
        primary = VeilPrimary,
        onPrimary = VeilOnPrimary,
        primaryContainer = VeilPrimaryContainer,
        onPrimaryContainer = VeilDarkTextPrimary,
        secondary = VeilSecondary,
        onSecondary = VeilDarkTextPrimary,
        tertiary = VeilAccent,
        background = VeilDarkBackground,
        onBackground = VeilDarkTextPrimary,
        surface = VeilDarkSurface,
        onSurface = VeilDarkTextPrimary,
        surfaceVariant = VeilDarkSurfaceVariant,
        onSurfaceVariant = VeilDarkTextSecondary,
        outline = VeilDarkBorder,
        outlineVariant = VeilDarkDivider,
        error = VeilError,
        onError = VeilOnPrimary,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = VeilPrimary,
        onPrimary = VeilOnPrimary,
        primaryContainer = Color(0xFFD6E4FF),
        onPrimaryContainer = Color(0xFF0D2B5E),
        secondary = VeilSecondary,
        onSecondary = VeilOnPrimary,
        tertiary = VeilAccent,
        background = VeilLightBackground,
        onBackground = VeilLightTextPrimary,
        surface = VeilLightSurface,
        onSurface = VeilLightTextPrimary,
        surfaceVariant = VeilLightSurfaceVariant,
        onSurfaceVariant = VeilLightTextSecondary,
        outline = VeilLightBorder,
        outlineVariant = VeilLightDivider,
        error = VeilError,
        onError = VeilOnPrimary,
    )

@Composable
fun VeilTheme(content: @Composable () -> Unit) {
    val themePrefs = rememberVeilThemePreferences()
    var currentMode by remember { mutableStateOf(themePrefs.load()) }
    val isDark =
        when (currentMode) {
            VeilThemeMode.SYSTEM -> isSystemInDarkTheme()
            VeilThemeMode.LIGHT -> false
            VeilThemeMode.DARK -> true
        }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalVeilThemeMode provides currentMode,
        LocalVeilThemeModeController provides { mode ->
            currentMode = mode
            themePrefs.save(mode)
        },
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = VeilTypography,
            shapes = VeilShapes,
            content = content,
        )
    }
}

@Composable
fun isVeilDarkTheme(): Boolean {
    val mode = LocalVeilThemeMode.current
    return when (mode) {
        VeilThemeMode.SYSTEM -> isSystemInDarkTheme()
        VeilThemeMode.LIGHT -> false
        VeilThemeMode.DARK -> true
    }
}
