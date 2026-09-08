package com.veil.shared.data.mock

import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.port.PushService

/** Logs push intents; APNs/FCM wired in future phase (ADR-003). */
class NoOpPushService : PushService {
    override suspend fun registerToken(deviceId: DeviceId, tokenHash: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun notifyNewMessage(deviceId: DeviceId) {
        // Local dev: no-op. Replace with APNs/FCM implementation later.
    }
}
