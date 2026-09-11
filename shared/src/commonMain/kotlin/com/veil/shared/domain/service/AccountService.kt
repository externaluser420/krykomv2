package com.veil.shared.domain.service

import com.veil.shared.domain.port.CryptoMessagingEngine
import com.veil.shared.domain.port.MessageStore

/** Wipes all local account state so the user can re-register cleanly. */
class AccountService(
    private val identityService: IdentityService,
    private val appLockService: AppLockService,
    private val messageStore: MessageStore,
    private val messageRepository: MessageRepository,
    private val cryptoEngine: CryptoMessagingEngine,
) {
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            messageStore.clearAllData().getOrElse { return Result.failure(it) }
            messageRepository.clearSessionCache()
            cryptoEngine.resetLocalCryptoState()
            appLockService.clearPin().getOrElse { return Result.failure(it) }
            identityService.deleteIdentity().getOrElse { return Result.failure(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
