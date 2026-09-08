# Phase 4 — Secure Messaging

**Date:** 2026-09-02  
**Branch:** `cursor/phase4-messaging-db74`

## Delivered

| Task | Status | Implementation |
|------|--------|----------------|
| T-041 libsignal sessions | ✅ | `LibSignalCryptoEngine` + `SignalProtocolStoreHolder` |
| T-042 X3DH / PreKey bundles | ✅ | `PreKeyBundleMapper` (Kyber 1024, libsignal 0.76) |
| T-043 Message repository | ✅ | `MessageRepository` — encrypt, send, sync, decrypt |
| T-044 Contact management | ✅ | `ContactService` + local store |
| T-045 Mock relay routing | ✅ | `MockRelayClient` routes by `recipientDeviceId` |
| T-046 Android chat UI | ✅ | Contacts, add contact, chat screen |
| T-047 E2EE integration test | ✅ | `MessagingIntegrationTest` (Alice ↔ Bob) |
| T-048 Http relay client | ⏸ | Deferred — mock remains default (ADR-003) |

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│  ContactsScreen / ChatScreen (Android Compose)           │
├──────────────────────────────────────────────────────────┤
│  ContactService          MessageRepository               │
├──────────────────────────────────────────────────────────┤
│  CryptoMessagingEngine   RelayClient                     │
│  (LibSignalCryptoEngine) (MockRelayClient / future HTTP) │
├──────────────────────────────────────────────────────────┤
│  SignalProtocolStoreHolder ← SecureKeyStore              │
│  MessageStore (SQLCipher / in-memory)                    │
└──────────────────────────────────────────────────────────┘
```

## Message flow (1:1 E2EE)

1. Sender fetches recipient **PublicKeyBundle** from relay key directory.
2. `CryptoMessagingEngine.establishSession()` runs X3DH via libsignal `SessionBuilder`.
3. Plaintext encrypted with `SessionCipher` → `RelayEnvelope` (ciphertext only).
4. Relay queues envelope for recipient device id (opaque string, e.g. `device-<uuid>`).
5. Recipient `syncIncoming()` fetches, decrypts, persists locally, acks.

**Signal device id** is always `1` (ADR-004 single-device policy). Relay routing uses the opaque `DeviceId` string.

## libsignal 0.76 notes

- `PreKeyBundle` requires **Kyber prekeys** — generated in `LibSignalKeyGenerator`.
- Use `UsePqRatchet.YES` in `SessionBuilder.process()` and first-message decrypt.
- First message ciphertext type: `PREKEY_TYPE` (3); follow-ups: `WHISPER_TYPE` (2).
- Constants duplicated in `Messaging.kt` to keep libsignal out of `commonMain`.

## Android UI

After onboarding + app lock:

- **Contacts** — shows local identity/device id for sharing, sync button, contact list.
- **Add contact** — identity id + device id (must be registered on same relay).
- **Chat** — E2EE send/receive with local history.

### Mock relay limitation

`MockRelayClient` is in-memory **per app process**. Two physical devices need the Rust relay (`server/relay`) and `HttpRelayClient` (Phase 5 wiring). E2EE correctness is verified by JVM integration tests.

## Tests

```bash
./gradlew :shared:jvmTest
# MessagingIntegrationTest — full Alice→Bob roundtrip via mock relay
```

## Next (Phase 5)

- Sender Keys for E2EE group chat
- `HttpRelayClient` (Ktor) → production relay
- Push notification hooks (NoOp → APNs/FCM)
