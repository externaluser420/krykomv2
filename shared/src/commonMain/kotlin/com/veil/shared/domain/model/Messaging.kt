package com.veil.shared.domain.model

/** Serialized libsignal state required to rebuild [SignalProtocolStore]. */
data class ProtocolMaterial(
    val identityKeyPairSerialized: ByteArray,
    val registrationId: Int,
    val signedPreKeySerialized: ByteArray,
    val oneTimePreKeySerialized: ByteArray,
    val kyberPreKeySerialized: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProtocolMaterial) return false
        return registrationId == other.registrationId &&
            identityKeyPairSerialized.contentEquals(other.identityKeyPairSerialized)
    }

    override fun hashCode(): Int = registrationId
}

/** libsignal CiphertextMessage types — duplicated to avoid libsignal in commonMain. */
const val CIPHERTEXT_PREKEY_TYPE = 3
const val CIPHERTEXT_WHISPER_TYPE = 2

/** Primary Signal device id — always 1 for single-device policy (ADR-004). */
const val SIGNAL_DEVICE_ID = 1

/** Encrypted payload from libsignal SessionCipher. */
data class EncryptedPayload(
    val bytes: ByteArray,
    val messageType: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedPayload) return false
        return messageType == other.messageType && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = messageType + bytes.contentHashCode()
}

/** Decrypted message for UI. */
data class ChatMessage(
    val id: String,
    val conversationId: ConversationId,
    val senderId: IdentityId,
    val body: String,
    val sentAtEpochMs: Long,
    val status: MessageStatus,
    val isOutgoing: Boolean,
)

fun directConversationId(local: IdentityId, remote: IdentityId): ConversationId {
    val parts = listOf(local.value, remote.value).sorted()
    return ConversationId("${parts[0]}_${parts[1]}")
}
