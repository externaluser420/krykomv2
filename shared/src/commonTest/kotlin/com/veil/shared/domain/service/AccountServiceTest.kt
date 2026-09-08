package com.veil.shared.domain.service

import com.veil.shared.data.mock.InMemoryAppLockStore
import com.veil.shared.data.mock.InMemoryMessageStore
import com.veil.shared.data.mock.InMemorySecureKeyStore
import com.veil.shared.data.mock.MockRelayClient
import com.veil.shared.crypto.LibSignalCryptoEngine
import com.veil.shared.crypto.SignalProtocolStoreHolder
import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityId
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AccountServiceTest {
    @Test
    fun deleteAccount_wipesIdentityPinAndContacts() = kotlinx.coroutines.test.runTest {
        val keyStore = InMemorySecureKeyStore()
        val relay = MockRelayClient()
        val messageStore = InMemoryMessageStore()
        val storeHolder = SignalProtocolStoreHolder(keyStore)
        val crypto = LibSignalCryptoEngine(storeHolder)
        val identityService = IdentityService(keyStore, relay)
        val appLock = AppLockService(InMemoryAppLockStore())
        val messages = MessageRepository(identityService, relay, messageStore, crypto)
        val accountService = AccountService(identityService, appLock, messageStore, messages, crypto)

        identityService.createAndRegisterIdentity().getOrThrow()
        appLock.setPin("123456")
        messageStore.saveContact(
            Contact(
                identityId = IdentityId("remote"),
                deviceId = DeviceId("device-remote"),
                displayName = "Remote",
            ),
        )

        accountService.deleteAccount().getOrThrow()

        assertFalse(identityService.hasIdentity())
        assertFalse(appLock.isEnabled())
        assertTrue(messageStore.getContacts().isEmpty())
    }
}
