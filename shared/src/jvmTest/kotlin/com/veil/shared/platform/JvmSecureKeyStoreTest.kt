package com.veil.shared.platform

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JvmSecureKeyStoreTest {
    @Test
    fun createIdentity_generatesLibsignalKeys() = runTest {
        val store = JvmSecureKeyStore()
        val result = store.createIdentity()
        assertTrue(result.isSuccess)

        val publicKeyBase64 = store.identityKeyPublicBase64()
        assertNotNull(publicKeyBase64)
        assertTrue(publicKeyBase64.isNotBlank())
        assertTrue(store.hasIdentity())
        assertNotNull(store.getPublicKeyBundle())
    }
}
