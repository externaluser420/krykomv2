package com.veil.shared.domain.service

import com.veil.shared.domain.model.Contact
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.port.MessageStore
import com.veil.shared.domain.port.RelayClient

class ContactService(
    private val messageStore: MessageStore,
    private val relayClient: RelayClient,
    private val cryptoEngine: com.veil.shared.domain.port.CryptoMessagingEngine,
) {
    suspend fun addContact(
        identityId: IdentityId,
        deviceId: DeviceId,
        displayName: String?,
    ): Result<Contact> {
        val bundle =
            relayClient.fetchKeyBundle(identityId, deviceId)
                ?: return Result.failure(IllegalStateException("Identity not found on relay"))
        val contact =
            Contact(
                identityId = identityId,
                deviceId = deviceId,
                displayName = displayName ?: identityId.value.take(8),
                verified = false,
            )
        messageStore.saveContact(contact)
        return Result.success(contact)
    }

    suspend fun listContacts(): List<Contact> = messageStore.getContacts()

    suspend fun safetyNumber(contact: Contact): Result<String> {
        val bundle =
            relayClient.fetchKeyBundle(contact.identityId, contact.deviceId)
                ?: return Result.failure(IllegalStateException("Key bundle not found"))
        return Result.success(cryptoEngine.fingerprint(bundle))
    }
}
