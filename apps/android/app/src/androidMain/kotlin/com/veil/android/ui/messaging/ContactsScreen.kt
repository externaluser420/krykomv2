package com.veil.android.ui.messaging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.veil.android.ui.components.VeilContactRow
import com.veil.android.ui.components.VeilEmptyState
import com.veil.android.ui.components.VeilListDivider
import com.veil.android.ui.components.VeilLoadingScreen
import com.veil.android.ui.components.VeilSearchBar
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.service.ContactService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onAddContact: () -> Unit,
    onOpenContact: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contactService: ContactService = koinInject()
    val scope = rememberCoroutineScope()

    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshContacts() {
        scope.launch {
            isLoading = true
            contacts = contactService.listContacts()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshContacts()
    }

    val filteredContacts =
        if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter { contact ->
                val name = contact.displayName ?: contact.identityId.value
                name.contains(searchQuery, ignoreCase = true) ||
                    contact.identityId.value.contains(searchQuery, ignoreCase = true)
            }
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add contact")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            VeilSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search contacts",
                onClear = { searchQuery = "" },
                modifier = Modifier.padding(horizontal = VeilSpacing.screenHorizontal, vertical = VeilSpacing.sm),
            )

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        VeilLoadingScreen(message = "Loading contacts…")
                    }
                }
                filteredContacts.isEmpty() && searchQuery.isBlank() -> {
                    VeilEmptyState(
                        title = "No contacts yet",
                        description = "Add someone by their identity to start messaging securely.",
                        actionLabel = "Add contact",
                        onAction = onAddContact,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                filteredContacts.isEmpty() -> {
                    VeilEmptyState(
                        title = "No results",
                        description = "No contacts match \"$searchQuery\".",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = "${filteredContacts.size} contact${if (filteredContacts.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier =
                                    Modifier.padding(
                                        horizontal = VeilSpacing.screenHorizontal,
                                        vertical = VeilSpacing.sm,
                                    ),
                            )
                        }
                        items(
                            items = filteredContacts,
                            key = { "${it.identityId.value}:${it.deviceId.value}" },
                        ) { contact ->
                            VeilContactRow(
                                contact = contact,
                                onClick = { onOpenContact(contact) },
                            )
                            VeilListDivider()
                        }
                    }
                }
            }
        }
    }
}
