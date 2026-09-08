package com.veil.shared.data.mock

import com.veil.shared.domain.port.IceServerConfig
import com.veil.shared.domain.port.TurnService

/** STUN-only config for local WebRTC development. TURN wired in production phase. */
class MockTurnService(
    private val stunUrl: String = "stun:stun.l.google.com:19302",
) : TurnService {
    override suspend fun getIceServers(): List<IceServerConfig> =
        listOf(IceServerConfig(urls = listOf(stunUrl)))
}
