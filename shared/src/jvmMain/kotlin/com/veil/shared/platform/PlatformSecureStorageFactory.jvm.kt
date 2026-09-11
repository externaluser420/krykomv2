package com.veil.shared.platform

import com.veil.shared.crypto.LibSignalCryptoEngine
import com.veil.shared.crypto.SignalProtocolStoreHolder
import com.veil.shared.data.mock.InMemoryAppLockStore
import com.veil.shared.data.mock.InMemoryMessageStore
import com.veil.shared.domain.port.AppLockStore
import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.SecureKeyStore

actual class PlatformSecureStorageFactory actual constructor(
    @Suppress("UNUSED_PARAMETER") platformContext: Any?,
) {
    private val keyStore = JvmSecureKeyStore()

    actual fun createSecureKeyStore(): SecureKeyStore = keyStore

    actual fun createAppLockStore(): AppLockStore = InMemoryAppLockStore()

    actual fun createMessageStore(): MessageStore = InMemoryMessageStore()

    actual fun createCryptoMessagingEngine(): CryptoMessagingEngine =
        LibSignalCryptoEngine(SignalProtocolStoreHolder(keyStore))
}
