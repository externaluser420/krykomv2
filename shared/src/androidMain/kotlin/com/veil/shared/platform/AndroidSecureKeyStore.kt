package com.veil.shared.platform

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.veil.shared.crypto.LibSignalKeyGenerator
import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityCreationResult
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.ProtocolMaterial
import com.veil.shared.domain.port.PublicKeyBundle
import com.veil.shared.domain.port.SecureKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.random.Random

class AndroidSecureKeyStore(
    private val context: Context,
) : SecureKeyStore {
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

    override suspend fun hasIdentity(): Boolean =
        withContext(Dispatchers.IO) {
            mutex.withLock { prefs.contains(KEY_IDENTITY_KEYPAIR) }
        }

    override suspend fun getIdentityId(): IdentityId? =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                prefs.getString(KEY_IDENTITY_ID, null)?.let { IdentityId(it) }
            }
        }

    override suspend fun getDeviceId(): DeviceId? =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                prefs.getString(KEY_DEVICE_ID, null)?.let { DeviceId(it) }
            }
        }

    override suspend fun createIdentity(): Result<IdentityCreationResult> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (prefs.contains(KEY_IDENTITY_KEYPAIR)) {
                    return@withContext Result.failure(IllegalStateException("Identity already exists"))
                }
                val generated = LibSignalKeyGenerator.generateIdentity()
                val dbPassphrase = Random.nextBytes(32)

                prefs.edit()
                    .putString(KEY_IDENTITY_ID, generated.result.identityId.value)
                    .putString(KEY_DEVICE_ID, generated.result.deviceId.value)
                    .putString(KEY_IDENTITY_KEYPAIR, encode(generated.material.identityKeyPairSerialized))
                    .putInt(KEY_REGISTRATION_ID, generated.material.registrationId)
                    .putString(KEY_SIGNED_PREKEY, encode(generated.material.signedPreKeySerialized))
                    .putString(KEY_ONETIME_PREKEY, encode(generated.material.oneTimePreKeySerialized))
                    .putString(KEY_KYBER_PREKEY, encode(generated.material.kyberPreKeySerialized))
                    .putString(KEY_DB_PASSPHRASE, encode(dbPassphrase))
                    .commit()

                Result.success(generated.result)
            }
        }

    override suspend fun getPublicKeyBundle(): PublicKeyBundle? =
        withContext(Dispatchers.IO) {
            mutex.withLock { loadProtocolMaterial()?.let { rebuildBundle(it) } }
        }

    override suspend fun getProtocolMaterial(): ProtocolMaterial? =
        withContext(Dispatchers.IO) {
            mutex.withLock { loadProtocolMaterial() }
        }

    override suspend fun clearIdentity(): Result<Unit> =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                prefs.edit().clear().apply()
                Result.success(Unit)
            }
        }

    override suspend fun getDatabasePassphrase(): ByteArray =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val stored = prefs.getString(KEY_DB_PASSPHRASE, null)?.let { decode(it) }
                if (stored != null && stored.size == 32) return@withContext stored
                val fresh = Random.nextBytes(32)
                prefs.edit().putString(KEY_DB_PASSPHRASE, encode(fresh)).apply()
                fresh
            }
        }

    private fun loadProtocolMaterial(): ProtocolMaterial? {
        val identity = prefs.getString(KEY_IDENTITY_KEYPAIR, null)?.let { decode(it) } ?: return null
        val regId = prefs.getInt(KEY_REGISTRATION_ID, -1)
        if (regId < 0) return null
        val signed = prefs.getString(KEY_SIGNED_PREKEY, null)?.let { decode(it) } ?: return null
        val oneTime = prefs.getString(KEY_ONETIME_PREKEY, null)?.let { decode(it) } ?: return null
        val kyber = prefs.getString(KEY_KYBER_PREKEY, null)?.let { decode(it) } ?: return null
        return ProtocolMaterial(identity, regId, signed, oneTime, kyber)
    }

    private fun rebuildBundle(material: ProtocolMaterial): PublicKeyBundle? {
        val identityId = prefs.getString(KEY_IDENTITY_ID, null)?.let { IdentityId(it) } ?: return null
        val deviceId = prefs.getString(KEY_DEVICE_ID, null)?.let { DeviceId(it) } ?: return null
        return try {
            val pair = org.signal.libsignal.protocol.IdentityKeyPair(material.identityKeyPairSerialized)
            val signed = org.signal.libsignal.protocol.state.SignedPreKeyRecord(material.signedPreKeySerialized)
            val oneTime = org.signal.libsignal.protocol.state.PreKeyRecord(material.oneTimePreKeySerialized)
            val kyber = org.signal.libsignal.protocol.state.KyberPreKeyRecord(material.kyberPreKeySerialized)
            PublicKeyBundle(
                identityId = identityId,
                deviceId = deviceId,
                registrationId = material.registrationId,
                identityKey = pair.publicKey.serialize(),
                signedPreKeyId = signed.id,
                signedPreKey = signed.keyPair.publicKey.serialize(),
                signedPreKeySignature = signed.signature,
                oneTimePreKeyId = oneTime.id,
                oneTimePreKey = oneTime.keyPair.publicKey.serialize(),
                kyberPreKeyId = kyber.id,
                kyberPreKey = kyber.keyPair.publicKey.serialize(),
                kyberPreKeySignature = kyber.signature,
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    companion object {
        private const val PREFS_NAME = "veil_secure_keys"
        private const val KEY_IDENTITY_ID = "identity_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_IDENTITY_KEYPAIR = "identity_keypair"
        private const val KEY_REGISTRATION_ID = "registration_id"
        private const val KEY_SIGNED_PREKEY = "signed_prekey"
        private const val KEY_ONETIME_PREKEY = "onetime_prekey"
        private const val KEY_KYBER_PREKEY = "kyber_prekey"
        private const val KEY_DB_PASSPHRASE = "db_passphrase"
    }
}
