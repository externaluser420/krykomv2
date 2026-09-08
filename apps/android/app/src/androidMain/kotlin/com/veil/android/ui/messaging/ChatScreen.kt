package com.veil.android.ui.messaging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.veil.android.ui.components.VeilAvatar
import com.veil.android.ui.components.VeilChatComposer
import com.veil.android.ui.components.VeilEmptyState
import com.veil.android.ui.components.VeilErrorBanner
import com.veil.android.ui.components.VeilMessageBubble
import com.veil.android.ui.theme.VeilAccent
import com.veil.android.ui.theme.VeilSpacing
import com.veil.shared.domain.model.ChatMessage
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.service.MessageRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: Contact,
    onBack: () -> Unit,
    onOpenProfile: ((Contact) -> Unit)? = null,
) {
    val messageRepository: MessageRepository = koinInject()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    val displayName = contact.displayName ?: contact.identityId.value.take(8)

    fun reloadMessages() {
        scope.launch {
            messageRepository.syncIncoming()
            messages = messageRepository.loadChatMessages(contact)
            isLoading = false
        }
    }

    LaunchedEffect(contact) {
        isLoading = true
        reloadMessages()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "End-to-end encrypted",
                            style = MaterialTheme.typography.labelSmall,
                            color = VeilAccent,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (onOpenProfile != null) {
                        IconButton(onClick = { onOpenProfile(contact) }) {
                            VeilAvatar(name = displayName, size = 36.dp)
                        }
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(modifier = Modifier.imePadding()) {
                error?.let {
                    VeilErrorBanner(
                        message = it,
                        onRetry = { error = null },
                    )
                }
                VeilChatComposer(
                    value = draft,
                    onValueChange = { draft = it },
                    sending = sending,
                    onSend = {
                        val text = draft.trim()
                        if (text.isEmpty() || sending) return@VeilChatComposer
                        scope.launch {
                            sending = true
                            error = null
                            messageRepository
                                .sendTextMessage(contact, text)
                                .onSuccess {
                                    draft = ""
                                    messages = messageRepository.loadChatMessages(contact)
                                }
                                .onFailure { err ->
                                    error = "Message couldn't be sent. Please try again."
                                }
                            sending = false
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when {
                isLoading -> {
                    // Subtle loading — show empty chat area
                }
                messages.isEmpty() -> {
                    VeilEmptyState(
                        title = "No messages yet",
                        description = "Send a message to start your encrypted conversation with $displayName.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = VeilSpacing.sm),
                        reverseLayout = false,
                    ) {
                        items(messages, key = { it.id }) { message ->
                            VeilMessageBubble(
                                message = message,
                                modifier = Modifier.padding(vertical = VeilSpacing.xxs),
                            )
                        }
                    }
                }
            }
        }
    }
}
