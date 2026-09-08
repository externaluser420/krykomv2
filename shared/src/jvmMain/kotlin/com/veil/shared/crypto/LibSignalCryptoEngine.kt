package com.veil.shared.crypto

import com.veil.shared.domain.model.EncryptedPayload
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.domain.port.PublicKeyBundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.SessionBuilder
import org.signal.libsignal.protocol.SessionCipher
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.UsePqRatchet
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage

class LibSignalCryptoEngine(
    private val storeHolder: SignalProtocolStoreHolder,
) : CryptoMessagingEngine {
    override suspend fun establishSession(remote: PublicKeyBundle) =
        withContext(Dispatchers.Default) {
            val store = storeHolder.getStore()
            val address = SignalProtocolAddress(remote.identityId.value, remote.deviceIdNumeric)
            SessionBuilder(store, address).process(PreKeyBundleMapper.toLibSignal(remote), UsePqRatchet.YES)
        }

    override suspend fun encrypt(
        remoteIdentityId: IdentityId,
        remoteDeviceIdNumeric: Int,
        plaintext: ByteArray,
    ): EncryptedPayload =
        withContext(Dispatchers.Default) {
            val store = storeHolder.getStore()
            val cipher = SessionCipher(store, SignalProtocolAddress(remoteIdentityId.value, remoteDeviceIdNumeric))
            val encrypted = cipher.encrypt(plaintext)
            EncryptedPayload(encrypted.serialize(), encrypted.type)
        }

    override suspend fun decrypt(
        senderIdentityId: IdentityId,
        senderDeviceIdNumeric: Int,
        payload: EncryptedPayload,
    ): ByteArray =
        withContext(Dispatchers.Default) {
            val store = storeHolder.getStore()
            val cipher = SessionCipher(store, SignalProtocolAddress(senderIdentityId.value, senderDeviceIdNumeric))
            when (payload.messageType) {
                CiphertextMessage.PREKEY_TYPE ->
                    cipher.decrypt(PreKeySignalMessage(payload.bytes), UsePqRatchet.YES)
                else ->
                    cipher.decrypt(SignalMessage(payload.bytes))
            }
        }

    override suspend fun fingerprint(remote: PublicKeyBundle): String =
        withContext(Dispatchers.Default) {
            IdentityKey(remote.identityKey).fingerprint
        }

    override suspend fun resetLocalCryptoState() {
        storeHolder.reset()
    }
}
