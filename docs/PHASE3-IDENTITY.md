# Phase 3 — Local Data & Identity

**Date:** 2026-09-02  
**Branch:** `cursor/phase3-identity-db74`

## Delivered

| Task | Status | Implementation |
|------|--------|----------------|
| T-031 SQLCipher | ✅ | `SqlCipherMessageStore` + Room on Android |
| T-032 libsignal keygen | ✅ | `LibSignalKeyGenerator` (libsignal 0.76 API) |
| T-033 Device registration | ✅ | `IdentityService.createAndRegisterIdentity()` |
| T-034 App lock | ✅ | `AppLockService` + Android onboarding PIN |
| T-035 iOS Keychain | ⏸ Stub | `IosSecureKeyStoreStub` — Keychain on macOS Phase 3b |
| T-036 Android Keystore | ✅ | `AndroidSecureKeyStore` + EncryptedSharedPreferences |
| T-037 Crypto tests | ✅ | `JvmSecureKeyStoreTest`, `IdentityServiceTest` |

## Architecture

```
┌─────────────────────────────────────────┐
│  IdentityService                        │
│  AppLockService                         │
├─────────────────────────────────────────┤
│  SecureKeyStore (expect/actual)         │
│  MessageStore   (SQLCipher on Android)  │
│  AppLockStore                           │
├─────────────────────────────────────────┤
│  Android: Keystore + libsignal + Room   │
│  JVM:     libsignal (tests)             │
│  iOS:     stub (Keychain pending)       │
└─────────────────────────────────────────┘
```

## libsignal 0.76 note

`KeyHelper.generateSignedPreKey()` removed in 0.76. We use:
- `IdentityKeyPair.generate()`
- `ECKeyPair.generate()` + `SignedPreKeyRecord` with `privateKey.calculateSignature()`
- `PreKeyRecord` for one-time prekeys

## Android onboarding flow

1. Create identity (libsignal keys + relay mock registration)
2. Set PIN (EncryptedSharedPreferences)
3. Home screen

## Tests

```bash
./gradlew :shared:jvmTest   # 4 tests pass
cargo test -p veil-relay    # relay unchanged
```

## Next — Phase 4: Secure Messaging

- libsignal Double Ratchet sessions
- X3DH first message
- HTTP relay client (still mock option)
- Message encrypt/decrypt roundtrip
