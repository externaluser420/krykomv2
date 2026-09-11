package com.veil.android.ui.messaging

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.veil.android.ui.components.VeilErrorBanner
import com.veil.android.ui.components.VeilPrimaryButton
import com.veil.android.ui.components.VeilTextField
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.service.ContactService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
) {
    val contactService: ContactService = koinInject()
    val scope = rememberCoroutineScope()

    var identityId by remember { mutableStateOf("") }
    var deviceId by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add contact") },
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
            Text(
                text = "Add a contact",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Enter their identity and device ID from their Veil app to connect securely.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = VeilSpacing.sm),
            )

            Spacer(modifier = Modifier.height(VeilSpacing.xxxl))

            VeilTextField(
                value = identityId,
                onValueChange = { identityId = it.trim() },
                label = "Identity ID",
                placeholder = "Paste identity ID",
            )
            Spacer(modifier = Modifier.height(VeilSpacing.lg))
            VeilTextField(
                value = deviceId,
                onValueChange = { deviceId = it.trim() },
                label = "Device ID",
                placeholder = "Paste device ID",
            )
            Spacer(modifier = Modifier.height(VeilSpacing.lg))
            VeilTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Display name",
                placeholder = "Optional",
            )

            Spacer(modifier = Modifier.height(VeilSpacing.xxxl))

            VeilPrimaryButton(
                text = "Add contact",
                loading = loading,
                enabled = identityId.isNotBlank() && deviceId.isNotBlank(),
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        if (identityId.isBlank() || deviceId.isBlank()) {
                            error = "Identity and device ID are required"
                            loading = false
                            return@launch
                        }
                        contactService
                            .addContact(
                                identityId = IdentityId(identityId),
                                deviceId = DeviceId(deviceId),
                                displayName = displayName.ifBlank { null },
                            )
                            .onSuccess { onAdded() }
                            .onFailure {
                                error = "Couldn't find this identity. Check the IDs and try again."
                            }
                        loading = false
                    }
                },
            )

            error?.let {
                VeilErrorBanner(
                    message = it,
                    modifier = Modifier.padding(top = VeilSpacing.lg),
                )
            }

            Spacer(modifier = Modifier.height(VeilSpacing.xxxl))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(VeilSpacing.lg))
            Text(
                text = "Your contact can find their IDs in Settings → Identity on their Veil app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
