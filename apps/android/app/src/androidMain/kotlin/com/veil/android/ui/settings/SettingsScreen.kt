package com.veil.android.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.veil.android.ui.components.VeilAvatar
import com.veil.android.ui.components.VeilSettingsRow
import com.veil.android.ui.components.VeilSettingsSectionHeader
import com.veil.android.ui.theme.LocalVeilThemeMode
import com.veil.android.ui.theme.LocalVeilThemeModeController
import com.veil.android.ui.theme.VeilSpacing
import com.veil.android.ui.theme.VeilThemeMode
import com.veil.shared.domain.service.AccountService
import com.veil.shared.domain.service.IdentityService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onAccountDeleted: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val accountService: AccountService = koinInject()
    val identityService: IdentityService = koinInject()
    val scope = rememberCoroutineScope()
    val themeMode = LocalVeilThemeMode.current
    val setThemeMode = LocalVeilThemeModeController.current

    var identityId by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var deleting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        identityService.getIdentityState().onSuccess { state ->
            identityId = state.identityId.value
            deviceId = state.deviceId.value
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = {
                Column {
                    Text(
                        "This will permanently delete your identity and all local data from this device. This cannot be undone.",
                    )
                    deleteError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = VeilSpacing.sm),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            deleting = true
                            deleteError = null
                            accountService.deleteAccount()
                                .onSuccess {
                                    showDeleteDialog = false
                                    onAccountDeleted()
                                }
                                .onFailure {
                                    deleteError = "Could not delete account. Please try again."
                                    deleting = false
                                }
                        }
                    },
                    enabled = !deleting,
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                    )
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
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
        ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(VeilSpacing.xxxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            VeilAvatar(name = "You", size = 72.dp)
            Spacer(modifier = Modifier.height(VeilSpacing.md))
            Text(
                text = "Your identity",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = identityId.take(16) + if (identityId.length > 16) "…" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = VeilSpacing.xs),
            )
        }

        VeilSettingsSectionHeader(title = "Account")
        VeilSettingsRow(
            title = "Identity ID",
            subtitle = identityId.ifEmpty { "Loading…" },
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = VeilSpacing.screenHorizontal),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        VeilSettingsRow(
            title = "Device ID",
            subtitle = deviceId.ifEmpty { "Loading…" },
        )

        Spacer(modifier = Modifier.height(VeilSpacing.lg))

        VeilSettingsSectionHeader(title = "Privacy & Security")
        VeilSettingsRow(
            title = "Security",
            subtitle = "PIN lock and app protection",
            onClick = onNavigateToSecurity,
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = VeilSpacing.screenHorizontal),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        VeilSettingsRow(
            title = "Biometric unlock",
            subtitle = "Coming soon",
        )

        Spacer(modifier = Modifier.height(VeilSpacing.lg))

        VeilSettingsSectionHeader(title = "Appearance")
        VeilSettingsRow(
            title = "Theme",
            subtitle =
                when (themeMode) {
                    VeilThemeMode.SYSTEM -> "System default"
                    VeilThemeMode.LIGHT -> "Light"
                    VeilThemeMode.DARK -> "Dark"
                },
            onClick = onNavigateToAppearance,
        )

        Spacer(modifier = Modifier.height(VeilSpacing.lg))

        VeilSettingsSectionHeader(title = "Data")
        VeilSettingsRow(
            title = "Delete account",
            subtitle = "Remove identity and all local data",
            onClick = { showDeleteDialog = true },
        )

        Spacer(modifier = Modifier.height(VeilSpacing.lg))

        VeilSettingsSectionHeader(title = "About")
        VeilSettingsRow(
            title = "Veil Messenger",
            subtitle = "Version 0.3.0 · End-to-end encrypted",
        )

        Spacer(modifier = Modifier.height(VeilSpacing.huge))

        Text(
            text = "Your messages are encrypted on this device.\nVeil cannot read your conversations.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VeilSpacing.screenHorizontal),
        )

        Spacer(modifier = Modifier.height(VeilSpacing.xxxl))
        }
    }
}
