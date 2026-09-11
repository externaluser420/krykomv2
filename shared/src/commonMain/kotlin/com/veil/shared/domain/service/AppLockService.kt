package com.veil.shared.domain.service

import com.veil.shared.domain.port.AppLockStore

/** App-level PIN lock (biometric gate wired on platform UI — Phase 3 Android). */
class AppLockService(
    private val appLockStore: AppLockStore,
) {
    suspend fun isEnabled(): Boolean = appLockStore.isLockEnabled()

    suspend fun setPin(pin: String): Result<Unit> {
        require(pin.length in PIN_MIN..PIN_MAX) { "PIN must be $PIN_MIN–$PIN_MAX digits" }
        require(pin.all { it.isDigit() }) { "PIN must be numeric" }
        return appLockStore.setPinHash(hashPin(pin))
    }

    suspend fun verifyPin(pin: String): Boolean {
        if (!appLockStore.isLockEnabled()) return true
        val stored = appLockStore.getPinHash() ?: return false
        return stored == hashPin(pin)
    }

    suspend fun clearPin(): Result<Unit> = appLockStore.clearPin()

    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> =
        appLockStore.setBiometricEnabled(enabled)

    suspend fun isBiometricEnabled(): Boolean = appLockStore.isBiometricEnabled()

    companion object {
        const val PIN_MIN = 4
        const val PIN_MAX = 8
        /** Fixed PIN length used in the UI (lock + onboarding). */
        const val PIN_LENGTH = 6

        /** Simple hash for Phase 3 — platform store holds hash in EncryptedSharedPreferences. */
        internal fun hashPin(pin: String): String {
            var hash = 0x811C9DC5.toInt()
            for (c in pin) {
                hash = hash xor c.code
                hash *= 0x01000193
            }
            return hash.toUInt().toString(16).padStart(8, '0')
        }
    }
}
