package com.veil.shared.platform

import android.content.Context
import com.veil.shared.crypto.LibSignalCryptoEngine
import com.veil.shared.crypto.SignalProtocolStoreHolder
import com.veil.shared.data.local.SqlCipherMessageStore
import com.veil.shared.domain.port.AppLockStore
import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.SecureKeyStore

actual class PlatformSecureStorageFactory actual constructor(
    platformContext: Any?,
) {
    private val context: Context =
        requireNotNull(platformContext as? Context) {
            "Android PlatformSecureStorageFactory requires Android Context"
        }

    private val secureKeyStore: SecureKeyStore by lazy { AndroidSecureKeyStore(context) }
    private val storeHolder: SignalProtocolStoreHolder by lazy { SignalProtocolStoreHolder(secureKeyStore) }

    actual fun createSecureKeyStore(): SecureKeyStore = secureKeyStore

    actual fun createAppLockStore(): AppLockStore = AndroidAppLockStore(context)

    actual fun createMessageStore(): MessageStore = SqlCipherMessageStore(context, secureKeyStore)

    actual fun createCryptoMessagingEngine(): CryptoMessagingEngine =
        LibSignalCryptoEngine(storeHolder)
}
