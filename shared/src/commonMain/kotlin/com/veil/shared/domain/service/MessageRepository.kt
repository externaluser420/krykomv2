package com.veil.shared.domain.service

import com.veil.shared.domain.model.ChatMessage
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.model.Conversation
import com.veil.shared.domain.model.ConversationType
import com.veil.shared.domain.model.EncryptedPayload
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.Message
import com.veil.shared.domain.model.MessageStatus
import com.veil.shared.domain.port.RelayEnvelope
import com.veil.shared.domain.model.SIGNAL_DEVICE_ID
import com.veil.shared.domain.model.directConversationId
import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.PublicKeyBundle
import com.veil.shared.domain.port.RelayClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.veil.shared.domain.model.CIPHERTEXT_PREKEY_TYPE
import com.veil.shared.domain.model.CIPHERTEXT_WHISPER_TYPE
import kotlin.random.Random

class MessageRepository(
    private val identityService: IdentityService,
    private val relayClient: RelayClient,
    private val messageStore: MessageStore,
    private val cryptoEngine: CryptoMessagingEngine,
) {
    private val sessionMutex = Mutex()
    private val establishedSessions = mutableSetOf<String>()

    suspend fun sendTextMessage(
        contact: Contact,
        text: String,
    ): Result<ChatMessage> {
        val local = identityService.getIdentityState().getOrElse { return Result.failure(it) }
        val remoteBundle =
            relayClient.fetchKeyBundle(contact.identityId, contact.deviceId)
                ?: return Result.failure(IllegalStateException("Remote key bundle not found"))

        ensureSession(contact.identityId, remoteBundle)

        val conversationId = directConversationId(local.identityId, contact.identityId)
        val encrypted =
            cryptoEngine.encrypt(
                remoteIdentityId = contact.identityId,
                remoteDeviceIdNumeric = SIGNAL_DEVICE_ID,
                plaintext = text.encodeToByteArray(),
            )

        val messageId = "msg-${Random.nextLong().toULong().toString(16)}"
        val now = System.currentTimeMillis()
        val envelope =
            RelayEnvelope(
                id = messageId,
                recipientDeviceId = contact.deviceId,
                recipientIdentityId = contact.identityId,
                senderIdentityId = local.identityId,
                senderDeviceId = local.deviceId,
                conversationId = conversationId,
                ciphertext = encrypted.bytes,
                messageType = encrypted.messageType,
                sentAtEpochMs = now,
            )

        relayClient.sendMessage(envelope).getOrElse { return Result.failure(it) }

        messageStore.saveMessage(
            Message(
                id = messageId,
                conversationId = conversationId,
                senderId = local.identityId,
                ciphertext = encrypted.bytes,
                sentAtEpochMs = now,
                status = MessageStatus.SENT,
                localPlaintext = text,
            ),
        )
        ensureConversation(conversationId, local.identityId, contact.identityId)

        return Result.success(
            ChatMessage(
                id = messageId,
                conversationId = conversationId,
                senderId = local.identityId,
                body = text,
                sentAtEpochMs = now,
                status = MessageStatus.SENT,
                isOutgoing = true,
            ),
        )
    }

    suspend fun syncIncoming(): Result<List<ChatMessage>> {
        val local = identityService.getIdentityState().getOrElse { return Result.failure(it) }
        val pending = relayClient.fetchPendingMessages(local.deviceId)
        val decoded = mutableListOf<ChatMessage>()

        for (envelope in pending) {
            try {
                val plaintext =
                    cryptoEngine.decrypt(
                        senderIdentityId = envelope.senderIdentityId,
                        senderDeviceIdNumeric = SIGNAL_DEVICE_ID,
                        payload = EncryptedPayload(envelope.ciphertext, envelope.messageType),
                    )
                val body = plaintext.decodeToString()
                decoded.add(
                    ChatMessage(
                        id = envelope.id,
                        conversationId = envelope.conversationId,
                        senderId = envelope.senderIdentityId,
                        body = body,
                        sentAtEpochMs = envelope.sentAtEpochMs,
                        status = MessageStatus.DELIVERED,
                        isOutgoing = false,
                    ),
                )
                messageStore.saveMessage(
                    Message(
                        id = envelope.id,
                        conversationId = envelope.conversationId,
                        senderId = envelope.senderIdentityId,
                        ciphertext = envelope.ciphertext,
                        sentAtEpochMs = envelope.sentAtEpochMs,
                        status = MessageStatus.DELIVERED,
                    ),
                )
                relayClient.acknowledgeMessage(envelope.id)
            } catch (_: Exception) {
                // Skip messages that cannot be decrypted.
            }
        }
        return Result.success(decoded)
    }

    suspend fun loadChatMessages(contact: Contact): List<ChatMessage> {
        val local = identityService.getIdentityState().getOrNull() ?: return emptyList()
        val conversationId = directConversationId(local.identityId, contact.identityId)
        return messageStore.getMessages(conversationId).mapNotNull { msg -> toChatMessage(msg, local.identityId) }
    }

    private suspend fun toChatMessage(msg: Message, localIdentityId: IdentityId): ChatMessage? {
        val isOutgoing = msg.senderId == localIdentityId
        val body =
            if (isOutgoing) {
                msg.localPlaintext ?: return null
            } else {
                try {
                    cryptoEngine.decrypt(
                        senderIdentityId = msg.senderId,
                        senderDeviceIdNumeric = SIGNAL_DEVICE_ID,
                        payload =
                            EncryptedPayload(
                                msg.ciphertext,
                                CIPHERTEXT_WHISPER_TYPE,
                            ),
                    ).decodeToString()
                } catch (_: Exception) {
                    try {
                        cryptoEngine.decrypt(
                            senderIdentityId = msg.senderId,
                            senderDeviceIdNumeric = SIGNAL_DEVICE_ID,
                            payload = EncryptedPayload(msg.ciphertext, CIPHERTEXT_PREKEY_TYPE),
                        ).decodeToString()
                    } catch (_: Exception) {
                        return null
                    }
                }
            }
        return ChatMessage(
            id = msg.id,
            conversationId = msg.conversationId,
            senderId = msg.senderId,
            body = body,
            sentAtEpochMs = msg.sentAtEpochMs,
            status = msg.status,
            isOutgoing = isOutgoing,
        )
    }

    private suspend fun ensureSession(remoteIdentityId: IdentityId, bundle: PublicKeyBundle) {
        val key = remoteIdentityId.value
        sessionMutex.withLock {
            if (key !in establishedSessions) {
                cryptoEngine.establishSession(bundle)
                establishedSessions.add(key)
            }
        }
    }

    suspend fun clearSessionCache() {
        sessionMutex.withLock { establishedSessions.clear() }
    }

    private suspend fun ensureConversation(
        conversationId: com.veil.shared.domain.model.ConversationId,
        local: IdentityId,
        remote: IdentityId,
    ) {
        if (messageStore.getConversations().none { it.id == conversationId }) {
            messageStore.saveConversation(
                Conversation(
                    id = conversationId,
                    type = ConversationType.DIRECT,
                    participantIds = listOf(local, remote),
                ),
            )
        }
    }
}
