package com.veil.shared.crypto

import com.veil.shared.domain.port.PublicKeyBundle
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ecc.ECPublicKey
import org.signal.libsignal.protocol.kem.KEMPublicKey
import org.signal.libsignal.protocol.state.PreKeyBundle

internal object PreKeyBundleMapper {
    fun toLibSignal(bundle: PublicKeyBundle): PreKeyBundle {
        val preKeyId = bundle.oneTimePreKeyId ?: PreKeyBundle.NULL_PRE_KEY_ID
        val preKey =
            bundle.oneTimePreKey?.let {
                ECPublicKey(it)
            }
        val kyberId = bundle.kyberPreKeyId ?: PreKeyBundle.NULL_PRE_KEY_ID
        val kyberBytes =
            bundle.kyberPreKey
                ?: error("Kyber prekey required for libsignal 0.76 session establishment")
        val kyberKey = KEMPublicKey(kyberBytes)
        return PreKeyBundle(
            bundle.registrationId,
            bundle.deviceIdNumeric,
            preKeyId,
            preKey,
            bundle.signedPreKeyId,
            ECPublicKey(bundle.signedPreKey),
            bundle.signedPreKeySignature,
            IdentityKey(bundle.identityKey),
            kyberId,
            kyberKey,
            bundle.kyberPreKeySignature ?: ByteArray(0),
        )
    }
}
