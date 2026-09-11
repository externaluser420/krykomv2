package com.veil.shared.data.mock

import com.veil.shared.domain.model.ConversationId
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.port.RelayEnvelope
import com.veil.shared.domain.model.SIGNAL_DEVICE_ID
import com.veil.shared.domain.port.PublicKeyBundle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockRelayClientTest {
    @Test
    fun sendAndFetchMessage_deliversToRegisteredDevice() = runTest {
        val relay = MockRelayClient()
        val deviceA = DeviceId("device-a")
        val deviceB = DeviceId("device-b")
        val identityA = IdentityId("identity-a")
        val identityB = IdentityId("identity-b")
        val bundle = testKeyBundle(identityA, deviceA)

        relay.registerDevice(deviceA, identityA, bundle)
        relay.registerDevice(deviceB, identityB, testKeyBundle(identityB, deviceB))

        val envelope =
            RelayEnvelope(
                id = "msg-1",
                recipientDeviceId = deviceB,
                recipientIdentityId = identityB,
                senderIdentityId = identityA,
                senderDeviceId = deviceA,
                conversationId = ConversationId("conv-1"),
                ciphertext = byteArrayOf(9, 8, 7),
                messageType = 2,
                sentAtEpochMs = 1_000L,
            )

        assertTrue(relay.sendMessage(envelope).isSuccess)
        val pending = relay.fetchPendingMessages(deviceB)
        assertEquals(1, pending.size)
        assertEquals("msg-1", pending.first().id)

        assertTrue(relay.acknowledgeMessage("msg-1").isSuccess)
        assertEquals(0, relay.fetchPendingMessages(deviceB).size)
    }

    private fun testKeyBundle(identityId: IdentityId, deviceId: DeviceId) =
        PublicKeyBundle(
            identityId = identityId,
            deviceId = deviceId,
            registrationId = 42,
            deviceIdNumeric = SIGNAL_DEVICE_ID,
            identityKey = byteArrayOf(1),
            signedPreKeyId = 1,
            signedPreKey = byteArrayOf(2),
            signedPreKeySignature = byteArrayOf(3),
            oneTimePreKeyId = 1,
            oneTimePreKey = byteArrayOf(4),
            kyberPreKeyId = 1,
            kyberPreKey = byteArrayOf(5),
            kyberPreKeySignature = byteArrayOf(6),
        )
}
