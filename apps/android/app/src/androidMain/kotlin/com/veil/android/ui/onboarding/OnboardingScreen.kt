package com.veil.android.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.veil.android.ui.components.VeilPrimaryButton
import com.veil.android.ui.theme.VeilAccent
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.service.AppLockService
import com.veil.shared.domain.service.IdentityService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class OnboardingStep {
    Welcome,
    CreateIdentity,
    SetPin,
}

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val identityService: IdentityService = koinInject()
    val appLockService: AppLockService = koinInject()
    val scope = rememberCoroutineScope()

    var step by remember { mutableIntStateOf(OnboardingStep.Welcome.ordinal) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var identityLabel by remember { mutableStateOf("") }

    val currentStep = OnboardingStep.entries[step]

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VeilSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VeilBrandMark()
        Spacer(modifier = Modifier.height(VeilSpacing.xxxl))

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 3 } + fadeOut())
            },
            label = "onboardingStep",
        ) { targetStep ->
            when (targetStep) {
                OnboardingStep.Welcome -> {
                    WelcomeStep(
                        onContinue = { step = OnboardingStep.CreateIdentity.ordinal },
                    )
                }
                OnboardingStep.CreateIdentity -> {
                    CreateIdentityStep(
                        loading = loading,
                        identityLabel = identityLabel,
                        onCreate = {
                            scope.launch {
                                loading = true
                                error = null
                                identityService.createAndRegisterIdentity()
                                    .onSuccess { result ->
                                        identityLabel = result.identityId.value.take(12) + "…"
                                        step = OnboardingStep.SetPin.ordinal
                                    }
                                    .onFailure {
                                        error = "Couldn't create identity. Please try again."
                                    }
                                loading = false
                            }
                        },
                    )
                }
                OnboardingStep.SetPin -> {
                    SetPinStep(
                        pin = pin,
                        onPinChange = { pin = it },
                        loading = loading,
                        onContinue = {
                            scope.launch {
                                loading = true
                                error = null
                                if (pin.length != AppLockService.PIN_LENGTH) {
                                    error = "PIN must be exactly ${AppLockService.PIN_LENGTH} digits"
                                    loading = false
                                    return@launch
                                }
                                try {
                                    appLockService
                                        .setPin(pin)
                                        .onSuccess { onComplete() }
                                        .onFailure { err -> error = err.message ?: "Couldn't set PIN" }
                                } catch (e: Exception) {
                                    error = "Couldn't save PIN. Please try again."
                                }
                                loading = false
                            }
                        },
                    )
                }
            }
        }

        error?.let {
            VeilErrorBanner(
                message = it,
                modifier = Modifier.padding(top = VeilSpacing.lg),
            )
        }
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Welcome to Veil",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.md))
        Text(
            text = "Private messaging without phone numbers. Your conversations stay between you.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.sm))
        Text(
            text = "End-to-end encrypted · No account required",
            style = MaterialTheme.typography.labelMedium,
            color = VeilAccent,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.huge))
        VeilPrimaryButton(text = "Get started", onClick = onContinue)
    }
}

@Composable
private fun CreateIdentityStep(
    loading: Boolean,
    identityLabel: String,
    onCreate: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Create your identity",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.md))
        Text(
            text = "Veil creates a unique, pseudonymous identity on this device. No personal information needed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (identityLabel.isNotEmpty()) {
            Spacer(modifier = Modifier.height(VeilSpacing.lg))
            Text(
                text = identityLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(modifier = Modifier.height(VeilSpacing.huge))
        VeilPrimaryButton(
            text = "Create identity",
            loading = loading,
            onClick = onCreate,
        )
    }
}

@Composable
private fun SetPinStep(
    pin: String,
    onPinChange: (String) -> Unit,
    loading: Boolean,
    onContinue: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Secure your app",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.md))
        Text(
            text = "Set a PIN to protect Veil when you're away from your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.xxxl))
        VeilPinInput(
            pin = pin,
            onPinChange = onPinChange,
            length = AppLockService.PIN_LENGTH,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(VeilSpacing.sm))
        Text(
            text = "${AppLockService.PIN_LENGTH} digits required",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.huge))
        VeilPrimaryButton(
            text = "Continue",
            loading = loading,
            enabled = pin.length == AppLockService.PIN_LENGTH,
            onClick = onContinue,
        )
    }
}
