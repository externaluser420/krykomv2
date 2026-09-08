# ADR-005: Identity and Key Storage (Phase 3)

## Status

**Accepted** — 2026-09-02

## Context

Phase 3 requires local identity creation, secure key storage, encrypted database, and single-device policy (ADR-004).

## Decision

1. **Identity keys** generated with **libsignal** (`IdentityKeyPair.generate()`, X3DH prekeys via `KeyHelper`).
2. **Android private key storage**: EncryptedSharedPreferences with AES256-GCM MasterKey (Android Keystore-backed).
3. **Database encryption**: SQLCipher via Room `SupportOpenHelperFactory`; passphrase from SecureKeyStore (32 random bytes).
4. **Pseudonymous IDs**: UUID v4 for `IdentityId` and `DeviceId` — no phone/email.
5. **App lock**: PIN hash in separate EncryptedSharedPreferences; biometric flag stored (UI gate in Phase 3 Android).
6. **JVM tests**: `libsignal-client` with in-memory store for cryptographic verification.
7. **iOS**: stub until Keychain implementation on macOS (Phase 3b).

## Alternatives considered

| Alternative | Rejected because |
|-------------|------------------|
| Custom EC keygen | Never roll own crypto |
| Plain SharedPreferences | No hardware-backed protection |
| Server-side identity | Violates privacy architecture |

## Security implications

- Private keys never leave device unencrypted
- Single device: `createIdentity()` fails if identity exists
- DB passphrase tied to identity creation lifecycle

## References

- libsignal 0.76.1
- ADR-004 Single device
- Signal Protocol specifications
