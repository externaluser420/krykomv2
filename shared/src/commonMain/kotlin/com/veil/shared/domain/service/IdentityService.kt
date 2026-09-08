package com.veil.shared.domain.service

import com.veil.shared.domain.model.IdentityAlreadyExistsException
import com.veil.shared.domain.model.IdentityCreationResult
import com.veil.shared.domain.model.IdentityNotFoundException
import com.veil.shared.domain.model.IdentityState
import com.veil.shared.domain.port.RelayClient
import com.veil.shared.domain.port.SecureKeyStore

/**
 * Manages pseudonymous identity lifecycle (single device — ADR-004).
 * Creates keys locally; registers public material with relay.
 */
class IdentityService(
    private val secureKeyStore: SecureKeyStore,
    private val relayClient: RelayClient,
) {
    suspend fun hasIdentity(): Boolean = secureKeyStore.hasIdentity()

    suspend fun getIdentityState(): Result<IdentityState> {
        if (!secureKeyStore.hasIdentity()) {
            return Result.failure(IdentityNotFoundException())
        }
        val identityId =
            secureKeyStore.getIdentityId()
                ?: return Result.failure(IdentityNotFoundException())
        val deviceId =
            secureKeyStore.getDeviceId()
                ?: return Result.failure(IdentityNotFoundException())
        val bundle =
            secureKeyStore.getPublicKeyBundle()
                ?: return Result.failure(IdentityNotFoundException())
        return Result.success(IdentityState(identityId, deviceId, bundle))
    }

    /**
     * Creates identity + device keys and registers with relay.
     * Fails if identity already exists (single device policy).
     */
    suspend fun createAndRegisterIdentity(): Result<IdentityCreationResult> {
        if (secureKeyStore.hasIdentity()) {
            return Result.failure(IdentityAlreadyExistsException())
        }
        val created =
            secureKeyStore.createIdentity().getOrElse {
                return Result.failure(it)
            }
        relayClient.registerDevice(
            deviceId = created.deviceId,
            identityId = created.identityId,
            keyBundle = created.publicKeyBundle,
        )
        return Result.success(created)
    }

    /** Re-registers existing identity with relay (e.g. after reinstall with backup — future). */
    suspend fun registerExistingIdentityWithRelay(): Result<IdentityState> {
        val state = getIdentityState().getOrElse { return Result.failure(it) }
        relayClient.registerDevice(
            deviceId = state.deviceId,
            identityId = state.identityId,
            keyBundle = state.publicKeyBundle,
        )
        return Result.success(state)
    }

    suspend fun deleteIdentity(): Result<Unit> {
        val deviceId = secureKeyStore.getDeviceId()
        if (deviceId != null) {
            relayClient.unregisterDevice(deviceId)
        }
        return secureKeyStore.clearIdentity()
    }
}
