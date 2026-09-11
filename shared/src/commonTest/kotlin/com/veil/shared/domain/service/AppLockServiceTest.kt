package com.veil.shared.domain.service

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLockServiceTest {
    @Test
    fun pinVerification_works() = kotlinx.coroutines.test.runTest {
        val store = com.veil.shared.data.mock.InMemoryAppLockStore()
        val service = AppLockService(store)

        assertFalse(service.isEnabled())
        service.setPin("1234")
        assertTrue(service.isEnabled())
        assertTrue(service.verifyPin("1234"))
        assertFalse(service.verifyPin("0000"))
    }
}
