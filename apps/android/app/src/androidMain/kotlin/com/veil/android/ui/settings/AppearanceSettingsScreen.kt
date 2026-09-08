package com.veil.android.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.veil.android.ui.components.VeilSettingsSectionHeader
import com.veil.android.ui.theme.LocalVeilThemeMode
import com.veil.android.ui.theme.LocalVeilThemeModeController
import com.veil.android.ui.theme.VeilSpacing
import com.veil.android.ui.theme.VeilThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(onBack: () -> Unit) {
    val themeMode = LocalVeilThemeMode.current
    val setThemeMode = LocalVeilThemeModeController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appearance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            VeilSettingsSectionHeader(title = "Theme")
            ThemeOption(
                label = "System default",
                description = "Follow device settings",
                selected = themeMode == VeilThemeMode.SYSTEM,
                onClick = { setThemeMode(VeilThemeMode.SYSTEM) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = VeilSpacing.screenHorizontal),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            ThemeOption(
                label = "Light",
                description = "Bright and clean",
                selected = themeMode == VeilThemeMode.LIGHT,
                onClick = { setThemeMode(VeilThemeMode.LIGHT) },
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = VeilSpacing.screenHorizontal),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            ThemeOption(
                label = "Dark",
                description = "Easy on the eyes",
                selected = themeMode == VeilThemeMode.DARK,
                onClick = { setThemeMode(VeilThemeMode.DARK) },
            )
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = VeilSpacing.screenHorizontal, vertical = VeilSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
