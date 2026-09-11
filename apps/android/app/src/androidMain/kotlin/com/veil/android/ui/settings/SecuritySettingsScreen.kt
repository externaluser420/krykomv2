package com.veil.android.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.veil.android.ui.components.VeilPinInput
import com.veil.android.ui.components.VeilPrimaryButton
import com.veil.android.ui.components.VeilSettingsRow
import com.veil.android.ui.components.VeilSettingsSectionHeader
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.service.AppLockService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(onBack: () -> Unit) {
    val appLockService: AppLockService = koinInject()
    val scope = rememberCoroutineScope()

    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun pinValid(value: String) = value.length == AppLockService.PIN_LENGTH && value.all { it.isDigit() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security") },
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
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(VeilSpacing.screenHorizontal),
        ) {
            VeilSettingsSectionHeader(title = "Change PIN")
            Text(
                text = "Enter your current PIN, then choose a new ${AppLockService.PIN_LENGTH}-digit PIN.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = VeilSpacing.lg),
            )
            VeilPinInput(
                pin = currentPin,
                onPinChange = { if (it.length <= AppLockService.PIN_LENGTH) currentPin = it },
                length = AppLockService.PIN_LENGTH,
                enabled = !loading,
            )
            Text(
                text = "New PIN",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = VeilSpacing.lg, bottom = VeilSpacing.sm),
            )
            VeilPinInput(
                pin = newPin,
                onPinChange = { if (it.length <= AppLockService.PIN_LENGTH) newPin = it },
                length = AppLockService.PIN_LENGTH,
                enabled = !loading,
            )
            Text(
                text = "Confirm new PIN",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = VeilSpacing.lg, bottom = VeilSpacing.sm),
            )
            VeilPinInput(
                pin = confirmPin,
                onPinChange = { if (it.length <= AppLockService.PIN_LENGTH) confirmPin = it },
                length = AppLockService.PIN_LENGTH,
                enabled = !loading,
            )
            VeilPrimaryButton(
                text = "Update PIN",
                loading = loading,
                enabled = pinValid(currentPin) && pinValid(newPin) && pinValid(confirmPin),
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        message = null
                        if (newPin != confirmPin) {
                            error = "PINs don't match"
                            loading = false
                            return@launch
                        }
                        if (!appLockService.verifyPin(currentPin)) {
                            error = "Current PIN is incorrect"
                            loading = false
                            return@launch
                        }
                        appLockService.setPin(newPin)
                            .onSuccess {
                                message = "PIN updated successfully"
                                currentPin = ""
                                newPin = ""
                                confirmPin = ""
                            }
                            .onFailure { err -> error = err.message ?: "Couldn't update PIN" }
                        loading = false
                    }
                },
                modifier = Modifier.padding(top = VeilSpacing.lg),
            )

            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = VeilSpacing.sm),
                )
            }
            error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = VeilSpacing.sm),
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = VeilSpacing.xxl),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            VeilSettingsSectionHeader(title = "App lock")
            VeilSettingsRow(
                title = "PIN protection",
                subtitle = "Required on app launch and when returning from background",
            )
        }
    }
}
