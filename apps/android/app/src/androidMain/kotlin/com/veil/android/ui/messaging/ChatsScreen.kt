package com.veil.android.ui.messaging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.veil.android.ui.components.ConversationPreview
import androidx.compose.material3.rememberTopAppBarState
import com.veil.android.ui.components.VeilConversationRow
import com.veil.android.ui.components.VeilEmptyState
import com.veil.android.ui.components.VeilListDivider
import com.veil.android.ui.components.VeilLoadingScreen
import com.veil.android.ui.components.VeilSearchBar
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.service.ContactService
import com.veil.shared.domain.service.MessageRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onAddContact: () -> Unit,
    onOpenChat: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contactService: ContactService = koinInject()
    val messageRepository: MessageRepository = koinInject()
    val scope = rememberCoroutineScope()

    var conversations by remember { mutableStateOf<List<ConversationPreview>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf<String?>(null) }

    fun loadConversations(showLoading: Boolean = false) {
        scope.launch {
            if (showLoading) isLoading = true
            try {
                val contacts = contactService.listContacts()
                val previews =
                    contacts.map { contact ->
                        val messages = messageRepository.loadChatMessages(contact)
                        val last = messages.lastOrNull()
                        ConversationPreview(
                            contact = contact,
                            lastMessage = last?.body,
                            lastMessageTime = last?.sentAtEpochMs,
                            unreadCount = 0,
                        )
                    }.sortedByDescending { it.lastMessageTime ?: 0L }
                conversations = previews
            } catch (_: Exception) {
                syncStatus = "Couldn't load conversations"
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun syncMessages(onDone: () -> Unit = {}) {
        scope.launch {
            messageRepository.syncIncoming()
                .onSuccess { incoming ->
                    syncStatus =
                        if (incoming.isEmpty()) {
                            "Up to date"
                        } else {
                            "${incoming.size} new message${if (incoming.size > 1) "s" else ""}"
                        }
                    loadConversations()
                }
                .onFailure {
                    syncStatus = "Sync failed"
                }
            onDone()
        }
    }

    LaunchedEffect(Unit) {
        syncMessages()
        loadConversations(showLoading = true)
    }

    val filteredConversations =
        if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter { preview ->
                val name = preview.contact.displayName ?: preview.contact.identityId.value
                name.contains(searchQuery, ignoreCase = true) ||
                    preview.lastMessage?.contains(searchQuery, ignoreCase = true) == true
            }
        }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chats",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            isRefreshing = true
                            syncMessages { isRefreshing = false }
                        },
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = "Sync messages")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddContact,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Edit, contentDescription = "New chat")
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
                placeholder = "Search conversations",
                onClear = { searchQuery = "" },
                modifier = Modifier.padding(horizontal = VeilSpacing.screenHorizontal, vertical = VeilSpacing.sm),
            )

            syncStatus?.let { status ->
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = VeilSpacing.screenHorizontal, vertical = VeilSpacing.xs),
                )
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        VeilLoadingScreen(message = "Loading conversations…")
                    }
                }
                filteredConversations.isEmpty() && searchQuery.isBlank() -> {
                    VeilEmptyState(
                        title = "No conversations yet",
                        description = "Add a contact to start a private, encrypted conversation.",
                        actionLabel = "Add contact",
                        onAction = onAddContact,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                filteredConversations.isEmpty() -> {
                    VeilEmptyState(
                        title = "No results",
                        description = "No conversations match \"$searchQuery\".",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            syncMessages { isRefreshing = false }
                        },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = filteredConversations,
                                key = { "${it.contact.identityId.value}:${it.contact.deviceId.value}" },
                            ) { preview ->
                                VeilConversationRow(
                                    preview = preview,
                                    onClick = { onOpenChat(preview.contact) },
                                )
                                VeilListDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
