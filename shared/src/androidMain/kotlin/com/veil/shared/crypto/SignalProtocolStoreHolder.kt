package com.veil.shared.crypto

import com.veil.shared.domain.model.ProtocolMaterial
import com.veil.shared.domain.port.SecureKeyStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.state.KyberPreKeyRecord
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore

/** Builds and caches libsignal protocol store from secure storage. */
class SignalProtocolStoreHolder(
    private val secureKeyStore: SecureKeyStore,
) {
    private val mutex = Mutex()
    private var store: InMemorySignalProtocolStore? = null

    suspend fun getStore(): InMemorySignalProtocolStore =
        mutex.withLock {
            store ?: buildStore().also { store = it }
        }

    suspend fun reset() {
        mutex.withLock { store = null }
    }

    private suspend fun buildStore(): InMemorySignalProtocolStore {
        val material =
            secureKeyStore.getProtocolMaterial()
                ?: error("No protocol material — create identity first")
        val identityKeyPair = IdentityKeyPair(material.identityKeyPairSerialized)
        val protocolStore = InMemorySignalProtocolStore(identityKeyPair, material.registrationId)
        val signed = SignedPreKeyRecord(material.signedPreKeySerialized)
        val oneTime = PreKeyRecord(material.oneTimePreKeySerialized)
        val kyber = KyberPreKeyRecord(material.kyberPreKeySerialized)
        protocolStore.storeSignedPreKey(signed.id, signed)
        protocolStore.storePreKey(oneTime.id, oneTime)
        protocolStore.storeKyberPreKey(kyber.id, kyber)
        return protocolStore
    }
}
