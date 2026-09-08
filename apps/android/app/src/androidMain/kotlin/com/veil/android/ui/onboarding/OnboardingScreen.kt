package com.veil.android.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.veil.shared.domain.service.AppLockService
import com.veil.shared.domain.service.IdentityService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val identityService: IdentityService = koinInject()
    val appLockService: AppLockService = koinInject()
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var identityCreated by remember { mutableStateOf(false) }
    var identityLabel by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Welcome to Veil", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Create a private identity on this device. No phone number required.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (!identityCreated) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = null
                            identityService.createAndRegisterIdentity()
                                .onSuccess { result ->
                                    identityCreated = true
                                    identityLabel = result.identityId.value.take(12) + "…"
                                }
                                .onFailure { error = it.message ?: "Failed to create identity" }
                            loading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Create identity")
                }
            }
        } else {
            Text(
                text = "Identity: $identityLabel",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) pin = it },
                label = { Text("Set app PIN (4–8 digits)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        if (pin.length < AppLockService.PIN_MIN) {
                            error = "PIN too short"
                            loading = false
                            return@launch
                        }
                        appLockService.setPin(pin)
                            .onSuccess { onComplete() }
                            .onFailure { error = it.message }
                        loading = false
                    }
                },
                enabled = pin.length >= AppLockService.PIN_MIN && !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue")
            }
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
