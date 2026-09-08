package com.veil.android.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.veil.android.ui.components.VeilBrandMark
import com.veil.android.ui.components.VeilErrorBanner
import com.veil.android.ui.components.VeilPinInput
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.service.AppLockService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AppLockScreen(onUnlocked: () -> Unit) {
    val appLockService: AppLockService = koinInject()
    val scope = rememberCoroutineScope()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var verifying by remember { mutableStateOf(false) }

    fun tryUnlock(currentPin: String) {
        if (currentPin.length != AppLockService.PIN_LENGTH || verifying) return
        scope.launch {
            verifying = true
            error = null
            try {
                if (appLockService.verifyPin(currentPin)) {
                    onUnlocked()
                } else {
                    error = "Incorrect PIN"
                    pin = ""
                }
            } catch (e: Exception) {
                error = "Could not verify PIN. Please try again."
                pin = ""
            } finally {
                verifying = false
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(VeilSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        VeilBrandMark()
        Spacer(modifier = Modifier.height(VeilSpacing.xxxl))
        Text(
            text = "Veil is locked",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.sm))
        Text(
            text = "Enter your ${AppLockService.PIN_LENGTH}-digit PIN",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.xxxl))
        VeilPinInput(
            pin = pin,
            onPinChange = { newPin ->
                if (verifying) return@VeilPinInput
                pin = newPin
                error = null
                if (newPin.length == AppLockService.PIN_LENGTH) {
                    tryUnlock(newPin)
                }
            },
            length = AppLockService.PIN_LENGTH,
            enabled = !verifying,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            VeilErrorBanner(
                message = it,
                modifier = Modifier.padding(top = VeilSpacing.lg),
            )
        }
    }
}
