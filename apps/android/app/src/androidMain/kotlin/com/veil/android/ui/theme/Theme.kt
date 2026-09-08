package com.veil.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors =
    darkColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF6B8AFE),
        secondary = androidx.compose.ui.graphics.Color(0xFF9AA8C7),
    )

private val LightColors =
    lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF3D5AFE),
        secondary = androidx.compose.ui.graphics.Color(0xFF5C6BC0),
    )

@Composable
fun VeilTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
