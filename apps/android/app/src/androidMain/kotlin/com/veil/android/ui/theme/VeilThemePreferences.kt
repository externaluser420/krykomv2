package com.veil.android.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PREFS_NAME = "veil_theme_prefs"
private const val KEY_THEME_MODE = "theme_mode"

class VeilThemePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): VeilThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, VeilThemeMode.DARK.name) ?: VeilThemeMode.DARK.name
        return VeilThemeMode.entries.firstOrNull { it.name == name } ?: VeilThemeMode.DARK
    }

    fun save(mode: VeilThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}

@Composable
fun rememberVeilThemePreferences(): VeilThemePreferences {
    val context = LocalContext.current
    return remember { VeilThemePreferences(context) }
}
