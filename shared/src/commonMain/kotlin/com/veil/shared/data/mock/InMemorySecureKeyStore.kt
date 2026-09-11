package com.veil.shared.data.mock

import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityCreationResult
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.ProtocolMaterial
import com.veil.shared.domain.model.SIGNAL_DEVICE_ID
import com.veil.shared.domain.port.AppLockStore
import com.veil.shared.domain.port.PublicKeyBundle
import com.veil.shared.domain.port.SecureKeyStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/** In-memory secure store for tests — NOT production crypto. */
class InMemorySecureKeyStore : SecureKeyStore {
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
        mutex.withLock {
            if (identityId != null) {
                return Result.failure(IllegalStateException("Identity already exists"))
            }
            val newIdentityId = IdentityId("test-identity-${Random.nextInt(999_999)}")
            val newDeviceId = DeviceId("test-device-${Random.nextInt(999_999)}")
            val keyBundle =
                PublicKeyBundle(
                    identityId = newIdentityId,
                    deviceId = newDeviceId,
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
            identityId = newIdentityId
            deviceId = newDeviceId
            bundle = keyBundle
            protocolMaterial = null
            dbPassphrase = Random.nextBytes(32)
            Result.success(IdentityCreationResult(newIdentityId, newDeviceId, keyBundle))
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
}

class InMemoryAppLockStore : AppLockStore {
    private val mutex = Mutex()
    private var pinHash: String? = null
    private var biometric = false

    override suspend fun isLockEnabled(): Boolean = mutex.withLock { pinHash != null }

    override suspend fun getPinHash(): String? = mutex.withLock { pinHash }

    override suspend fun setPinHash(hash: String): Result<Unit> =
        mutex.withLock {
            pinHash = hash
            Result.success(Unit)
        }

    override suspend fun clearPin(): Result<Unit> =
        mutex.withLock {
            pinHash = null
            biometric = false
            Result.success(Unit)
        }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> =
        mutex.withLock {
            biometric = enabled
            Result.success(Unit)
        }

    override suspend fun isBiometricEnabled(): Boolean = mutex.withLock { biometric }
}
