package com.veil.android.ui.messaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.service.ContactService
import com.veil.shared.domain.service.IdentityService
import com.veil.shared.domain.service.MessageRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onAddContact: () -> Unit,
    onOpenChat: (Contact) -> Unit,
) {
    val identityService: IdentityService = koinInject()
    val contactService: ContactService = koinInject()
    val messageRepository: MessageRepository = koinInject()
    val scope = rememberCoroutineScope()

    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var identityLabel by remember { mutableStateOf("") }
    var deviceLabel by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }

    fun refreshContacts() {
        scope.launch {
            contactService.listContacts().let { contacts = it }
        }
    }

    LaunchedEffect(Unit) {
        identityService.registerExistingIdentityWithRelay()
            .onSuccess { state ->
                identityLabel = state.identityId.value
                deviceLabel = state.deviceId.value
            }
        refreshContacts()
        messageRepository.syncIncoming().onSuccess { incoming ->
            if (incoming.isNotEmpty()) {
                status = "${incoming.size} new message(s)"
                refreshContacts()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Veil") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                messageRepository.syncIncoming().onSuccess { incoming ->
                                    status =
                                        if (incoming.isEmpty()) {
                                            "No new messages"
                                        } else {
                                            "${incoming.size} new message(s)"
                                        }
                                    refreshContacts()
                                }
                            }
                        },
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContact) {
                Icon(Icons.Default.Add, contentDescription = "Add contact")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Your identity", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = identityLabel,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Device: $deviceLabel",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            status?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            if (contacts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "No contacts yet. Add someone by their identity and device ID.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    items(contacts, key = { "${it.identityId.value}:${it.deviceId.value}" }) { contact ->
                        ContactRow(contact = contact, onClick = { onOpenChat(contact) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onClick: () -> Unit) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = contact.displayName ?: contact.identityId.value.take(8),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = contact.identityId.value.take(16) + "…",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
