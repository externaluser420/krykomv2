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
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.veil.android.ui.components.VeilAvatar
import com.veil.android.ui.components.VeilPrimaryButton
import com.veil.android.ui.components.VeilSettingsRow
import com.veil.android.ui.components.VeilSettingsSectionHeader
import com.veil.android.ui.theme.VeilAccent
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.service.ContactService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactProfileScreen(
    contact: Contact,
    onBack: () -> Unit,
    onOpenChat: (Contact) -> Unit,
) {
    val contactService: ContactService = koinInject()
    val scope = rememberCoroutineScope()

    var safetyNumber by remember { mutableStateOf<String?>(null) }
    var loadingSafetyNumber by remember { mutableStateOf(false) }

    val displayName = contact.displayName ?: contact.identityId.value.take(8)

    LaunchedEffect(contact) {
        loadingSafetyNumber = true
        contactService.safetyNumber(contact).onSuccess { safetyNumber = it }
        loadingSafetyNumber = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
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
                    .verticalScroll(rememberScrollState()),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(VeilSpacing.xxxl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VeilAvatar(name = displayName, size = 96.dp, verified = contact.verified)
                Spacer(modifier = Modifier.height(VeilSpacing.lg))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineMedium,
                )
                if (contact.verified) {
                    Text(
                        text = "Verified contact",
                        style = MaterialTheme.typography.labelMedium,
                        color = VeilAccent,
                        modifier = Modifier.padding(top = VeilSpacing.xs),
                    )
                }
            }

            VeilPrimaryButton(
                text = "Send message",
                onClick = { onOpenChat(contact) },
                modifier = Modifier.padding(horizontal = VeilSpacing.screenHorizontal),
            )

            Spacer(modifier = Modifier.height(VeilSpacing.xxxl))

            VeilSettingsSectionHeader(title = "Identity")
            VeilSettingsRow(
                title = "Identity ID",
                subtitle = contact.identityId.value,
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = VeilSpacing.screenHorizontal),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            VeilSettingsRow(
                title = "Device ID",
                subtitle = contact.deviceId.value,
            )

            Spacer(modifier = Modifier.height(VeilSpacing.lg))

            VeilSettingsSectionHeader(title = "Security")
            VeilSettingsRow(
                title = "Safety number",
                subtitle =
                    when {
                        loadingSafetyNumber -> "Loading…"
                        safetyNumber != null -> safetyNumber
                        else -> "Unavailable"
                    },
            )

            Spacer(modifier = Modifier.height(VeilSpacing.xxxl))

            Text(
                text = "Compare safety numbers in person to verify this contact's identity.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VeilSpacing.screenHorizontal),
            )
        }
    }
}
