package com.veil.shared.platform

import com.veil.shared.domain.port.AppLockStore
import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.SecureKeyStore

expect class PlatformSecureStorageFactory(platformContext: Any?) {
    fun createSecureKeyStore(): SecureKeyStore

    fun createAppLockStore(): AppLockStore

    fun createMessageStore(): MessageStore

    fun createCryptoMessagingEngine(): CryptoMessagingEngine
}
