# 6. Teknikval och plattformsstrategi

## Jämförelse: mobilramverk

### Alternativ 1: Full native (Swift + Kotlin separata codebases)

| Kriterium | Bedömning |
|-----------|-----------|
| Säkerhet | ⭐⭐⭐⭐⭐ Bäst — direkt OS API |
| Prestanda | ⭐⭐⭐⭐⭐ |
| Native API (Keychain, Keystore, CallKit) | ⭐⭐⭐⭐⭐ |
| Krypto/WebRTC | ⭐⭐⭐⭐⭐ libsignal native bindings |
| Bakgrund/Push | ⭐⭐⭐⭐⭐ |
| Underhållbarhet | ⭐⭐ — dubbel kodbas |
| Utvecklingshastighet | ⭐⭐ — långsammast |
| Testbarhet | ⭐⭐⭐⭐ |
| Kompetens | Medel — kräver iOS + Android devs |

### Alternativ 2: Flutter

| Kriterium | Bedömning |
|-----------|-----------|
| Säkerhet | ⭐⭐⭐ FFI till native crypto |
| Prestanda | ⭐⭐⭐⭐ |
| Native API | ⭐⭐⭐ Via platform channels |
| Krypto/WebRTC | ⭐⭐⭐ flutter_webrtc, libsignal via FFI |
| Bakgrund/Push | ⭐⭐⭐ Begränsat |
| Underhållbarhet | ⭐⭐⭐⭐ En codebase |
| Utvecklingshastighet | ⭐⭐⭐⭐⭐ |
| Testbarhet | ⭐⭐⭐⭐ |
| Risk | Google dependency; FFI-komplexitet för crypto |

### Alternativ 3: React Native

| Kriterium | Bedömning |
|-----------|-----------|
| Säkerhet | ⭐⭐⭐ Bridge-säkerhetsproblem historiskt |
| Prestanda | ⭐⭐⭐ |
| Native API | ⭐⭐⭐ Modules |
| Krypto/WebRTC | ⭐⭐⭐ |
| Bakgrund/Push | ⭐⭐⭐ |
| Underhållbarhet | ⭐⭐⭐ |
| Utvecklingshastighet | ⭐⭐⭐⭐ |
| Risk | Hermes/bridge; mindre lämplig för crypto-first |

### Alternativ 4: Kotlin Multiplatform (KMP) + Native UI

| Kriterium | Bedömning |
|-----------|-----------|
| Säkerhet | ⭐⭐⭐⭐⭐ Delad kärna; native UI/secure storage |
| Prestanda | ⭐⭐⭐⭐⭐ |
| Native API | ⭐⭐⭐⭐⭐ UI-lager fullt native |
| Krypto/WebRTC | ⭐⭐⭐⭐⭐ libsignal JVM/Native i shared |
| Bakgrund/Push | ⭐⭐⭐⭐⭐ Native push i UI-lager |
| Underhållbarhet | ⭐⭐⭐⭐ Delad affärslogik |
| Utvecklingshastighet | ⭐⭐⭐⭐ |
| Testbarhet | ⭐⭐⭐⭐⭐ Shared tests i JVM |
| Kompetens | Växande KMP-community |

---

## Rekommendation

### **Kotlin Multiplatform (shared core) + Native UI (SwiftUI + Jetpack Compose)**

**Motivering:**

1. **Säkerhetskritisk kod skrivs en gång** — protokoll, crypto wrapper, sync, message model
2. **Plattformsintegration behålls** — Secure Enclave, Keystore, CallKit, Telecom via native UI-lager
3. **libsignal** har mogna Java/Kotlin och Swift bindings — shared wrapper möjlig
4. **Testbarhet** — shared module testas på JVM utan emulator
5. **Signal, WhatsApp** använder i princip native — vi matchar den standarden i UI-lager
6. **Undviker Flutter/RN bridge-risk** för kryptografisk kod

### Arkitektur (lager)

```
┌─────────────────────────────────────────────────┐
│  iOS (Swift/SwiftUI)  │  Android (Kotlin/Compose) │
│  - UI, Navigation     │  - UI, Navigation          │
│  - Push (APNs)        │  - Push (FCM)              │
│  - CallKit            │  - Telecom                 │
│  - Keychain bridge    │  - Keystore bridge         │
├─────────────────────────────────────────────────┤
│           KMP Shared Core (Kotlin)              │
│  - Protocol models    - Crypto service (libsignal)│
│  - Message store IF   - Sync engine              │
│  - Contact model      - Network client           │
│  - Relay API client   - Session manager          │
└─────────────────────────────────────────────────┘
```

---

## iOS-strategi

| Område | Val |
|--------|-----|
| Språk | Swift 6 |
| UI | SwiftUI |
| Min iOS | iOS 16+ (Security, SwiftUI mognad) |
| Nyckellagring | Keychain (kSecAttrAccessibleWhenUnlockedThisDeviceOnly) + Secure Enclave för identitetsnyckel |
| Lokal DB | GRDB + SQLCipher (via SQLCipher CocoaPod/SPM) |
| Push | APNs (data/silent push för meddelanden); **PushKit endast för VoIP** (Phase 6) |
| Bakgrund | BGAppRefreshTask (begränsat); push-wakeup primär strategi |
| Calls | CallKit (Phase 6) |
| Data Protection | NSFileProtectionComplete |
| Privacy | Privacy Manifest (PrivacyInfo.xcprivacy); App Tracking Transparency (ej relevant — ingen tracking) |
| App Store | Privacy nutrition labels; inga tredjeparts-SDK trackers |

### iOS-begränsningar att respektera

- Bakgrundsexekvering är **begränsad** — förlita dig på push, inte polling
- PushKit VoIP **måste** visa CallKit (Apple policy) — använd **inte** PushKit för meddelanden
- Secure Enclave: EC P-256; begränsad storage
- iCloud backup: markera känsliga filer `NSURLIsExcludedFromBackupKey`

---

## Android-strategi

| Område | Val |
|--------|-----|
| Språk | Kotlin |
| UI | Jetpack Compose |
| Min Android | API 26+ (Android 8.0) |
| Nyckellagring | Android Keystore (StrongBox om tillgänglig) |
| Lokal DB | Room + SQLCipher |
| Push | FCM (data messages, high priority) |
| Bakgrund | Foreground service för samtal (Phase 6); WorkManager för sync |
| Calls | Android Telecom / ConnectionService (Phase 6) |
| Security | android:allowBackup="false" för app data; Network Security Config med pinning |
| Play | Data safety form; inga AD_ID permissions |

### Android-begränsningar att respektera

- Doze mode och battery optimization — FCM high priority + user education
- Keystore keys kan ogiltigförklaras vid factory reset
- Background execution limits (API 26+)
- Play Store kräver target SDK inom 1 år av latest

---

## Backend / Relay

| Område | Rekommendation |
|--------|----------------|
| Språk | **Rust** (axum + tokio) — minne-säkerhet, prestanda, låg attackyta |
| Alternativ | Go (enklare hiring, bra concurrency) |
| Protokoll | HTTP/2 + WebSocket (real-time fetch) |
| Lagring | Redis (ephemeral queues, TTL) + minimal PostgreSQL (device registry only) ELLER ren Redis med persistence off |
| Push | Egen push service (APNs HTTP/2 + FCM v1 API) |
| TURN | coturn (Phase 6) |
| Infra | Docker → Kubernetes (Hetzner/OVH EU) |
| IaC | Terraform |

**Notering om "databas":** Device registry och key directory kräver **minimal persistent storage**. Detta är **inte** en traditionell meddelandedatabas — det är en key-value directory med publika nycklar och device tokens. Redis + optional SQLite/PostgreSQL för durability.

---

## Kryptografibibliotek

| Bibliotek | Användning |
|-----------|------------|
| **libsignal** (Signal Foundation) | E2EE: X3DH, Double Ratchet, group (senare) |
| SQLCipher | Lokal DB-kryptering |
| ring / boring (via libsignal) | Primitiver — använd **inte** direkt |

**Aldrig:** Egen implementation av AES-GCM, ECDH, ratcheting.

---

## Beslut som dokumenteras i ADR

- ADR-002: Signal Protocol via libsignal
- ADR-005: KMP + Native UI
- ADR-006: Rust relay server

**Detta beslut ska inte ändras utan ny ADR och konsekvensanalys.**
