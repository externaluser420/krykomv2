package com.veil.android.ui.lock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    var shakeError by remember { mutableStateOf(false) }

    val errorAlpha by animateFloatAsState(
        targetValue = if (shakeError) 1f else 1f,
        animationSpec = tween(300),
        label = "errorAlpha",
    )

    LaunchedEffect(pin) {
        if (pin.length >= AppLockService.PIN_MIN) {
            scope.launch {
                if (appLockService.verifyPin(pin)) {
                    onUnlocked()
                } else {
                    error = "Incorrect PIN"
                    shakeError = !shakeError
                    pin = ""
                }
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(VeilSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        VeilBrandMark()
        Spacer(modifier = Modifier.height(VeilSpacing.xxxl))
        Text(
            text = "Veil is locked",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.sm))
        Text(
            text = "Enter your PIN to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(VeilSpacing.xxxl))
        VeilPinInput(
            pin = pin,
            onPinChange = {
                pin = it
                error = null
            },
            length = 6,
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            VeilErrorBanner(
                message = it,
                modifier =
                    Modifier
                        .padding(top = VeilSpacing.lg)
                        .alpha(errorAlpha),
            )
        }
    }
}
