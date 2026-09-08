package com.veil.shared.platform

import com.veil.shared.crypto.LibSignalKeyGenerator
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityCreationResult
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.ProtocolMaterial
import com.veil.shared.domain.port.PublicKeyBundle
import com.veil.shared.domain.port.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.random.Random

/** JVM libsignal keystore for unit tests. */
class JvmSecureKeyStore : SecureKeyStore {
    private val mutex = Mutex()
    private var identityId: IdentityId? = null
    private var deviceId: DeviceId? = null
    private var bundle: PublicKeyBundle? = null
    private var protocolMaterial: ProtocolMaterial? = null
    private var dbPassphrase: ByteArray = Random.nextBytes(32)

    override suspend fun hasIdentity(): Boolean = mutex.withLock { identityId != null }

    override suspend fun getIdentityId(): IdentityId? = mutex.withLock { identityId }

    override suspend fun getDeviceId(): DeviceId? = mutex.withLock { deviceId }

    override suspend fun createIdentity(): Result<IdentityCreationResult> =
        withContext(Dispatchers.Default) {
            mutex.withLock {
                if (identityId != null) {
                    return@withContext Result.failure(IllegalStateException("Identity already exists"))
                }
                val generated = LibSignalKeyGenerator.generateIdentity()
                identityId = generated.result.identityId
                deviceId = generated.result.deviceId
                bundle = generated.result.publicKeyBundle
                protocolMaterial = generated.material
                dbPassphrase = Random.nextBytes(32)
                Result.success(generated.result)
            }
        }

    override suspend fun getPublicKeyBundle(): PublicKeyBundle? = mutex.withLock { bundle }

    override suspend fun getProtocolMaterial(): ProtocolMaterial? = mutex.withLock { protocolMaterial }

    override suspend fun clearIdentity(): Result<Unit> =
        mutex.withLock {
            identityId = null
            deviceId = null
            bundle = null
            protocolMaterial = null
            dbPassphrase = Random.nextBytes(32)
            Result.success(Unit)
        }

    override suspend fun getDatabasePassphrase(): ByteArray = mutex.withLock { dbPassphrase.copyOf() }

    internal fun identityKeyPublicBase64(): String? =
        bundle?.identityKey?.let { java.util.Base64.getEncoder().encodeToString(it) }
}
