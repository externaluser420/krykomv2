package com.veil.shared.data.mock

import com.veil.shared.domain.model.Conversation
import com.veil.shared.domain.model.ConversationId
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.Message
import com.veil.shared.domain.model.MessageStatus
import com.veil.shared.domain.port.MessageStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** In-memory store until SQLCipher integration (Phase 3). */
class InMemoryMessageStore : MessageStore {
    private val mutex = Mutex()
    private val messages = mutableListOf<Message>()
    private val conversations = mutableListOf<Conversation>()
    private val contacts = mutableListOf<Contact>()
    private val blocked = mutableSetOf<IdentityId>()

    override suspend fun saveMessage(message: Message) {
        mutex.withLock { messages.add(message) }
    }

    override suspend fun getMessages(conversationId: ConversationId): List<Message> =
        mutex.withLock {
            messages.filter { it.conversationId == conversationId }
        }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        mutex.withLock {
            val index = messages.indexOfFirst { it.id == messageId }
            if (index >= 0) {
                messages[index] = messages[index].copy(status = status)
            }
        }
    }

    override suspend fun saveConversation(conversation: Conversation) {
        mutex.withLock {
            conversations.removeIf { it.id == conversation.id }
            conversations.add(conversation)
        }
    }

    override suspend fun getConversations(): List<Conversation> =
        mutex.withLock { conversations.toList() }

    override suspend fun saveContact(contact: Contact) {
        mutex.withLock {
            contacts.removeIf { it.identityId == contact.identityId }
            contacts.add(contact)
        }
    }

    override suspend fun getContacts(): List<Contact> =
        mutex.withLock { contacts.toList() }

    override suspend fun getBlockedIdentities(): List<IdentityId> =
        mutex.withLock { blocked.toList() }

    override suspend fun clearAllData(): Result<Unit> {
        mutex.withLock {
            messages.clear()
            conversations.clear()
            contacts.clear()
            blocked.clear()
        }
        return Result.success(Unit)
    }
}
