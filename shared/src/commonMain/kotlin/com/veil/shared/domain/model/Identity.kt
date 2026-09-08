package com.veil.shared.domain.model

import com.veil.shared.domain.port.PublicKeyBundle

/** Result of first-time identity creation on device. */
data class IdentityCreationResult(
    val identityId: IdentityId,
    val deviceId: DeviceId,
    val publicKeyBundle: PublicKeyBundle,
)

/** Current local identity state (single device — ADR-004). */
data class IdentityState(
    val identityId: IdentityId,
    val deviceId: DeviceId,
    val publicKeyBundle: PublicKeyBundle,
)

class IdentityAlreadyExistsException : IllegalStateException("Identity already exists on this device")

class IdentityNotFoundException : IllegalStateException("No identity on this device")
