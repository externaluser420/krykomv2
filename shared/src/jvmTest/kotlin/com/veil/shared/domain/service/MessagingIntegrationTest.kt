package com.veil.shared.domain.service

import com.veil.shared.crypto.LibSignalCryptoEngine
import com.veil.shared.crypto.SignalProtocolStoreHolder
import com.veil.shared.data.mock.InMemoryMessageStore
import com.veil.shared.data.mock.MockRelayClient
import com.veil.shared.domain.model.Contact
import com.veil.shared.platform.JvmSecureKeyStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessagingIntegrationTest {
    @Test
    fun e2eeMessageRoundTrip_viaMockRelay() = runTest {
        val relay = MockRelayClient()

        val aliceKeys = JvmSecureKeyStore()
        val bobKeys = JvmSecureKeyStore()
        val aliceIdentity = IdentityService(aliceKeys, relay)
        val bobIdentity = IdentityService(bobKeys, relay)

        val alice = aliceIdentity.createAndRegisterIdentity().getOrThrow()
        val bob = bobIdentity.createAndRegisterIdentity().getOrThrow()

        val aliceCrypto = LibSignalCryptoEngine(SignalProtocolStoreHolder(aliceKeys))
        val bobCrypto = LibSignalCryptoEngine(SignalProtocolStoreHolder(bobKeys))

        val aliceRepo =
            MessageRepository(
                aliceIdentity,
                relay,
                InMemoryMessageStore(),
                aliceCrypto,
            )
        val bobRepo =
            MessageRepository(
                bobIdentity,
                relay,
                InMemoryMessageStore(),
                bobCrypto,
            )

        val bobContact =
            Contact(
                identityId = bob.identityId,
                deviceId = bob.deviceId,
                displayName = "Bob",
            )
        val aliceContact =
            Contact(
                identityId = alice.identityId,
                deviceId = alice.deviceId,
                displayName = "Alice",
            )

        val sent = aliceRepo.sendTextMessage(bobContact, "Hello Bob!")
        assertTrue(sent.isSuccess, sent.exceptionOrNull()?.message)

        val received = bobRepo.syncIncoming()
        assertTrue(received.isSuccess)
        assertEquals(1, received.getOrThrow().size)
        assertEquals("Hello Bob!", received.getOrThrow().first().body)

        val reply = bobRepo.sendTextMessage(aliceContact, "Hi Alice!")
        assertTrue(reply.isSuccess)

        val aliceIncoming = aliceRepo.syncIncoming()
        assertTrue(aliceIncoming.isSuccess)
        assertEquals("Hi Alice!", aliceIncoming.getOrThrow().first().body)
    }
}
