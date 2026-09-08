package com.veil.shared.crypto

import com.veil.shared.domain.model.DeviceId
import com.veil.shared.domain.model.IdentityCreationResult
import com.veil.shared.domain.model.IdentityId
import com.veil.shared.domain.model.ProtocolMaterial
import com.veil.shared.domain.model.SIGNAL_DEVICE_ID
import com.veil.shared.domain.port.PublicKeyBundle
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.ecc.ECKeyPair
import org.signal.libsignal.protocol.kem.KEMKeyPair
import org.signal.libsignal.protocol.kem.KEMKeyType
import org.signal.libsignal.protocol.state.KyberPreKeyRecord
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.signal.libsignal.protocol.util.KeyHelper
import java.util.UUID

/** libsignal 0.76 key generation including Kyber prekeys. */
internal object LibSignalKeyGenerator {
    data class GeneratedIdentity(
        val result: IdentityCreationResult,
        val material: ProtocolMaterial,
        val signedPreKey: SignedPreKeyRecord,
        val oneTimePreKey: PreKeyRecord,
        val kyberPreKey: KyberPreKeyRecord,
    )

    fun generateIdentity(): GeneratedIdentity {
        val identityKeyPair = IdentityKeyPair.generate()
        val registrationId = KeyHelper.generateRegistrationId(false)
        val signedPreKey = generateSignedPreKey(identityKeyPair, 1)
        val oneTimePreKey = PreKeyRecord(1, ECKeyPair.generate())
        val kyberKeyPair = KEMKeyPair.generate(KEMKeyType.KYBER_1024)
        val kyberSignature =
            identityKeyPair.privateKey.calculateSignature(
                kyberKeyPair.publicKey.serialize(),
            )
        val kyberPreKey = KyberPreKeyRecord(1, System.currentTimeMillis(), kyberKeyPair, kyberSignature)

        val identityId = IdentityId(UUID.randomUUID().toString())
        val deviceId = DeviceId("device-${UUID.randomUUID()}")

        val bundle =
            PublicKeyBundle(
                identityId = identityId,
                deviceId = deviceId,
                registrationId = registrationId,
                deviceIdNumeric = SIGNAL_DEVICE_ID,
                identityKey = identityKeyPair.publicKey.serialize(),
                signedPreKeyId = signedPreKey.id,
                signedPreKey = signedPreKey.keyPair.publicKey.serialize(),
                signedPreKeySignature = signedPreKey.signature,
                oneTimePreKeyId = oneTimePreKey.id,
                oneTimePreKey = oneTimePreKey.keyPair.publicKey.serialize(),
                kyberPreKeyId = kyberPreKey.id,
                kyberPreKey = kyberPreKey.keyPair.publicKey.serialize(),
                kyberPreKeySignature = kyberPreKey.signature,
            )

        val material =
            ProtocolMaterial(
                identityKeyPairSerialized = identityKeyPair.serialize(),
                registrationId = registrationId,
                signedPreKeySerialized = signedPreKey.serialize(),
                oneTimePreKeySerialized = oneTimePreKey.serialize(),
                kyberPreKeySerialized = kyberPreKey.serialize(),
            )

        return GeneratedIdentity(
            result = IdentityCreationResult(identityId, deviceId, bundle),
            material = material,
            signedPreKey = signedPreKey,
            oneTimePreKey = oneTimePreKey,
            kyberPreKey = kyberPreKey,
        )
    }

    fun generateSignedPreKey(identityKeyPair: IdentityKeyPair, id: Int): SignedPreKeyRecord {
        val signedPreKeyPair = ECKeyPair.generate()
        val timestamp = System.currentTimeMillis()
        val signature =
            identityKeyPair.privateKey.calculateSignature(
                signedPreKeyPair.publicKey.serialize(),
            )
        return SignedPreKeyRecord(id, timestamp, signedPreKeyPair, signature)
    }
}
