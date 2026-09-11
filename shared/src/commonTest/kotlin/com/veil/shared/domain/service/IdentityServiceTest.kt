package com.veil.shared.domain.service

import com.veil.shared.data.mock.InMemorySecureKeyStore
import com.veil.shared.data.mock.MockRelayClient
import com.veil.shared.domain.model.IdentityAlreadyExistsException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentityServiceTest {
    @Test
    fun createAndRegisterIdentity_succeedsOnce() = runTest {
        val relay = MockRelayClient()
        val keyStore = InMemorySecureKeyStore()
        val service = IdentityService(keyStore, relay)

        assertFalse(service.hasIdentity())
        val result = service.createAndRegisterIdentity()
        assertTrue(result.isSuccess)
        assertTrue(service.hasIdentity())

        val second = service.createAndRegisterIdentity()
        assertTrue(second.isFailure)
        assertTrue(second.exceptionOrNull() is IdentityAlreadyExistsException)
    }

    @Test
    fun getIdentityState_afterCreate_returnsIdentity() = runTest {
        val service = IdentityService(InMemorySecureKeyStore(), MockRelayClient())
        service.createAndRegisterIdentity().getOrThrow()
        val state = service.getIdentityState().getOrThrow()
        assertEquals(state.identityId, state.publicKeyBundle.identityId)
    }
}
