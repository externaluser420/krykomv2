package com.veil.shared.domain.port

import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.model.Conversation
import com.veil.shared.domain.model.ConversationId
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.EncryptedPayload
import com.veil.shared.domain.model.IdentityCreationResult
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.ProtocolMaterial
import com.veil.shared.domain.model.SIGNAL_DEVICE_ID
import com.veil.shared.domain.model.Message
import com.veil.shared.domain.model.MessageStatus

/** Public key bundle for X3DH session establishment (Signal Protocol). */
data class PublicKeyBundle(
    val identityId: IdentityId,
    val deviceId: DeviceId,
    val registrationId: Int,
    val deviceIdNumeric: Int = SIGNAL_DEVICE_ID,
    val identityKey: ByteArray,
    val signedPreKeyId: Int,
    val signedPreKey: ByteArray,
    val signedPreKeySignature: ByteArray,
    val oneTimePreKeyId: Int?,
    val oneTimePreKey: ByteArray?,
    val kyberPreKeyId: Int?,
    val kyberPreKey: ByteArray?,
    val kyberPreKeySignature: ByteArray?,
)

/** Envelope sent to relay — server sees ciphertext only. */
data class RelayEnvelope(
    val id: String,
    val recipientDeviceId: DeviceId,
    val recipientIdentityId: IdentityId,
    val senderIdentityId: IdentityId,
    val senderDeviceId: DeviceId,
    val conversationId: ConversationId,
    val ciphertext: ByteArray,
    val messageType: Int,
    val sentAtEpochMs: Long,
)

/** Stateless relay: key directory + encrypted message queue (ADR-001). */
interface RelayClient {
    suspend fun registerDevice(deviceId: DeviceId, identityId: IdentityId, keyBundle: PublicKeyBundle)

    suspend fun unregisterDevice(deviceId: DeviceId)

    suspend fun fetchKeyBundle(identityId: IdentityId, deviceId: DeviceId): PublicKeyBundle?

    suspend fun sendMessage(envelope: RelayEnvelope): Result<Unit>

    suspend fun fetchPendingMessages(deviceId: DeviceId): List<RelayEnvelope>

    suspend fun acknowledgeMessage(messageId: String): Result<Unit>
}

/** libsignal encrypt/decrypt — platform implementation (JVM/Android). */
interface CryptoMessagingEngine {
    suspend fun establishSession(remote: PublicKeyBundle)

    suspend fun encrypt(
        remoteIdentityId: IdentityId,
        remoteDeviceIdNumeric: Int,
        plaintext: ByteArray,
    ): EncryptedPayload

    suspend fun decrypt(
        senderIdentityId: IdentityId,
        senderDeviceIdNumeric: Int,
        payload: EncryptedPayload,
    ): ByteArray

    suspend fun fingerprint(remote: PublicKeyBundle): String

    /** Clears in-memory crypto sessions after account deletion. */
    suspend fun resetLocalCryptoState()
}

/** Push gateway abstraction — NoOp in dev; APNs/FCM wired later (ADR-003). */
interface PushService {
    suspend fun registerToken(deviceId: DeviceId, tokenHash: String): Result<Unit>

    suspend fun notifyNewMessage(deviceId: DeviceId)
}

/** TURN/STUN config for WebRTC — mock in dev (ADR-003). */
data class IceServerConfig(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null,
)

interface TurnService {
    suspend fun getIceServers(): List<IceServerConfig>
}

/** WebRTC signaling — local/mock until production infra (ADR-003). */
interface SignalingService {
    suspend fun connect(sessionId: String)

    suspend fun sendSignal(sessionId: String, payload: ByteArray)

    suspend fun receiveSignals(sessionId: String): kotlinx.coroutines.flow.Flow<ByteArray>

    suspend fun disconnect(sessionId: String)
}

/** Local encrypted storage — SQLCipher in Phase 3. */
interface MessageStore {
    suspend fun saveMessage(message: Message)

    suspend fun getMessages(conversationId: ConversationId): List<Message>

    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)

    suspend fun saveConversation(conversation: Conversation)

    suspend fun getConversations(): List<Conversation>

    suspend fun saveContact(contact: Contact)

    suspend fun getContacts(): List<Contact>

    suspend fun getBlockedIdentities(): List<IdentityId>

    /** Removes all local messages, contacts, and conversations. */
    suspend fun clearAllData(): Result<Unit>

    /** Opens the encrypted database before first UI access (Android SQLCipher). */
    suspend fun ensureReady(): Result<Unit> = Result.success(Unit)
}

/** Secure identity and device key storage — Secure Enclave / Keystore (Phase 3). */
interface SecureKeyStore {
    suspend fun hasIdentity(): Boolean

    suspend fun getIdentityId(): IdentityId?

    suspend fun getDeviceId(): DeviceId?

    /** Creates identity + device keys locally. Fails if identity exists (ADR-004). */
    suspend fun createIdentity(): Result<IdentityCreationResult>

    suspend fun getPublicKeyBundle(): PublicKeyBundle?

    /** Removes all local keys and identity metadata. */
    suspend fun clearIdentity(): Result<Unit>

    /** Serialized libsignal material for session store rebuild. */
    suspend fun getProtocolMaterial(): ProtocolMaterial?

    /** 32-byte passphrase for SQLCipher — derived/stored via platform secure storage. */
    suspend fun getDatabasePassphrase(): ByteArray
}

/** PIN / biometric preferences — encrypted on platform. */
interface AppLockStore {
    suspend fun isLockEnabled(): Boolean

    suspend fun getPinHash(): String?

    suspend fun setPinHash(hash: String): Result<Unit>

    suspend fun clearPin(): Result<Unit>

    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit>

    suspend fun isBiometricEnabled(): Boolean
}
