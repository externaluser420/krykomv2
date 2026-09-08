package com.veil.shared.platform

import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityCreationResult
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.ProtocolMaterial
import com.veil.shared.domain.model.SIGNAL_DEVICE_ID
import com.veil.shared.domain.port.PublicKeyBundle
import com.veil.shared.domain.port.SecureKeyStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class IosSecureKeyStoreStub : SecureKeyStore {
    private val mutex = Mutex()
    private var bundle: PublicKeyBundle? = null
    private var dbPassphrase: ByteArray = Random.nextBytes(32)

    override suspend fun hasIdentity(): Boolean = mutex.withLock { bundle != null }

    override suspend fun getIdentityId(): IdentityId? = mutex.withLock { bundle?.identityId }

    override suspend fun getDeviceId(): DeviceId? = mutex.withLock { bundle?.deviceId }

    override suspend fun createIdentity(): Result<IdentityCreationResult> =
        mutex.withLock {
            if (bundle != null) {
                return Result.failure(IllegalStateException("Identity already exists"))
            }
            val identityId = IdentityId("ios-stub-${Random.nextInt(999_999)}")
            val deviceId = DeviceId("ios-device-${Random.nextInt(999_999)}")
            val keyBundle =
                PublicKeyBundle(
                    identityId = identityId,
                    deviceId = deviceId,
                    registrationId = Random.nextInt(1, 16380),
                    deviceIdNumeric = SIGNAL_DEVICE_ID,
                    identityKey = Random.nextBytes(33),
                    signedPreKeyId = 1,
                    signedPreKey = Random.nextBytes(33),
                    signedPreKeySignature = Random.nextBytes(64),
                    oneTimePreKeyId = 1,
                    oneTimePreKey = Random.nextBytes(33),
                    kyberPreKeyId = 1,
                    kyberPreKey = Random.nextBytes(1568),
                    kyberPreKeySignature = Random.nextBytes(64),
                )
            bundle = keyBundle
            dbPassphrase = Random.nextBytes(32)
            Result.success(IdentityCreationResult(identityId, deviceId, keyBundle))
        }

    override suspend fun getPublicKeyBundle(): PublicKeyBundle? = mutex.withLock { bundle }

    override suspend fun getProtocolMaterial(): ProtocolMaterial? = null

    override suspend fun clearIdentity(): Result<Unit> =
        mutex.withLock {
            bundle = null
            dbPassphrase = Random.nextBytes(32)
            Result.success(Unit)
        }

    override suspend fun getDatabasePassphrase(): ByteArray = mutex.withLock { dbPassphrase.copyOf() }
}
