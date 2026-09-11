package com.veil.shared.platform

import com.veil.shared.data.mock.InMemoryAppLockStore
import com.veil.shared.data.mock.InMemoryMessageStore
import com.veil.shared.data.mock.InMemorySecureKeyStore
import com.veil.shared.domain.port.AppLockStore
import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.SecureKeyStore

actual class PlatformSecureStorageFactory actual constructor(
    @Suppress("UNUSED_PARAMETER") platformContext: Any?,
) {
    actual fun createSecureKeyStore(): SecureKeyStore = IosSecureKeyStoreStub()

    actual fun createAppLockStore(): AppLockStore = InMemoryAppLockStore()

    actual fun createMessageStore(): MessageStore = InMemoryMessageStore()

    actual fun createCryptoMessagingEngine(): CryptoMessagingEngine =
        object : CryptoMessagingEngine {
            override suspend fun establishSession(remote: com.veil.shared.domain.port.PublicKeyBundle) =
                error("iOS crypto not implemented")

            override suspend fun encrypt(
                remoteIdentityId: com.veil.shared.domain.model.IdentityId,
                remoteDeviceIdNumeric: Int,
                plaintext: ByteArray,
            ) = error("iOS crypto not implemented")

            override suspend fun decrypt(
                senderIdentityId: com.veil.shared.domain.model.IdentityId,
                senderDeviceIdNumeric: Int,
                payload: com.veil.shared.domain.model.EncryptedPayload,
            ) = error("iOS crypto not implemented")

            override suspend fun fingerprint(remote: com.veil.shared.domain.port.PublicKeyBundle) = "ios-stub"
        }
}
