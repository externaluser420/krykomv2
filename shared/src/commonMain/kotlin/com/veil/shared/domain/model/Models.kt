package com.veil.shared.domain.model

import kotlinx.serialization.Serializable

/** Opaque pseudonymous identity — not tied to phone number or email. */
@Serializable
data class IdentityId(val value: String)

@Serializable
data class DeviceId(val value: String)

@Serializable
enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
}

@Serializable
data class Message(
    val id: String,
    val conversationId: ConversationId,
    val senderId: IdentityId,
    val ciphertext: ByteArray,
    val sentAtEpochMs: Long,
    val status: MessageStatus,
    /** Plaintext for outgoing messages we authored (local only, never sent to relay). */
    val localPlaintext: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

@Serializable
data class ConversationId(val value: String)

@Serializable
enum class ConversationType {
    DIRECT,
    GROUP,
}

@Serializable
data class Conversation(
    val id: ConversationId,
    val type: ConversationType,
    val participantIds: List<IdentityId>,
    val title: String? = null,
)

@Serializable
data class Contact(
    val identityId: IdentityId,
    val deviceId: DeviceId,
    val displayName: String?,
    val verified: Boolean = false,
)

@Serializable
enum class CallType {
    DIRECT,
    GROUP,
}

@Serializable
enum class CallState {
    IDLE,
    RINGING,
    CONNECTING,
    ACTIVE,
    ENDED,
    FAILED,
}

@Serializable
data class CallSession(
    val id: String,
    val type: CallType,
    val participantIds: List<IdentityId>,
    val state: CallState,
)
