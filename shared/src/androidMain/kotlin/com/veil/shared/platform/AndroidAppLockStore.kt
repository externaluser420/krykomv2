package com.veil.shared.platform

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.veil.shared.domain.port.AppLockStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Encrypted PIN hash storage for app lock. */
class AndroidAppLockStore(
    private val context: Context,
) : AppLockStore {
    private val mutex = Mutex()

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun isLockEnabled(): Boolean =
        withContext(Dispatchers.IO) {
            mutex.withLock { prefs.contains(KEY_PIN_HASH) }
        }

    override suspend fun getPinHash(): String? =
        withContext(Dispatchers.IO) {
            mutex.withLock { prefs.getString(KEY_PIN_HASH, null) }
        }

    override suspend fun setPinHash(hash: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                try {
                    prefs.edit().putString(KEY_PIN_HASH, hash).commit()
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

    override suspend fun clearPin(): Result<Unit> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                prefs.edit()
                    .remove(KEY_PIN_HASH)
                    .remove(KEY_BIOMETRIC)
                    .apply()
                Result.success(Unit)
            }
        }

    override suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
                Result.success(Unit)
            }
        }

    override suspend fun isBiometricEnabled(): Boolean =
        withContext(Dispatchers.IO) {
            mutex.withLock { prefs.getBoolean(KEY_BIOMETRIC, false) }
        }

    companion object {
        private const val PREFS_NAME = "veil_app_lock"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC = "biometric_enabled"
    }
}
