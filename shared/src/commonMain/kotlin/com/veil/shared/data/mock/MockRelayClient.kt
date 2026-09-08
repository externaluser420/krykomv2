package com.veil.shared.data.mock

import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.port.PublicKeyBundle
import com.veil.shared.domain.port.RelayEnvelope
import com.veil.shared.domain.port.RelayClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory relay for local development (ADR-003).
 * Routes envelopes to explicit recipient device.
 */
class MockRelayClient : RelayClient {
    private val mutex = Mutex()
    private val keyDirectory = mutableMapOf<Pair<IdentityId, DeviceId>, PublicKeyBundle>()
    private val messageQueues = mutableMapOf<DeviceId, MutableList<RelayEnvelope>>()
    private val registeredDevices = mutableSetOf<DeviceId>()

    override suspend fun registerDevice(
        deviceId: DeviceId,
        identityId: IdentityId,
        keyBundle: PublicKeyBundle,
    ) {
        mutex.withLock {
            registeredDevices.add(deviceId)
            keyDirectory[identityId to deviceId] = keyBundle
        }
    }

    override suspend fun unregisterDevice(deviceId: DeviceId) {
        mutex.withLock {
            registeredDevices.remove(deviceId)
            messageQueues.remove(deviceId)
            keyDirectory.entries.removeIf { it.key.second == deviceId }
        }
    }

    override suspend fun fetchKeyBundle(
        identityId: IdentityId,
        deviceId: DeviceId,
    ): PublicKeyBundle? =
        mutex.withLock {
            keyDirectory[identityId to deviceId]
        }

    override suspend fun sendMessage(envelope: RelayEnvelope): Result<Unit> {
        mutex.withLock {
            if (envelope.recipientDeviceId !in registeredDevices) {
                return Result.failure(IllegalStateException("Recipient device not registered"))
            }
            messageQueues.getOrPut(envelope.recipientDeviceId) { mutableListOf() }.add(envelope)
        }
        return Result.success(Unit)
    }

    override suspend fun fetchPendingMessages(deviceId: DeviceId): List<RelayEnvelope> =
        mutex.withLock {
            messageQueues[deviceId]?.toList() ?: emptyList()
        }

    override suspend fun acknowledgeMessage(messageId: String): Result<Unit> {
        mutex.withLock {
            messageQueues.values.forEach { queue ->
                queue.removeIf { it.id == messageId }
            }
        }
        return Result.success(Unit)
    }
}
