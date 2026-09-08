# 6. MVP-definition, Roadmap och TODO

## MVP — Minimal användbar och säker produkt

### Scope (inkluderat)

| # | Funktion | Acceptance criteria |
|---|----------|---------------------|
| M1 | **Identitetsskapande** | Användare skapar pseudonym identitet lokalt; publik nyckel registreras på relay |
| M2 | **Säker nyckellagring** | Privata nycklar i Secure Enclave/Keystore; aldrig i plaintext |
| M3 | **Kontaktupptäckt** | Dela identitet via QR-kod eller kopiera ID; manuell add |
| M4 | **E2EE 1:1 text** | Meddelanden krypterade med Signal Protocol; server kan ej läsa |
| M5 | **E2EE gruppchatt** | Sender Keys; server kan ej läsa |
| M6 | **1:1 ljudsamtal** | WebRTC + DTLS-SRTP |
| M7 | **Gruppsamtal (ljud)** | SFU; utan video |
| M8 | **Offline-leverans** | Meddelanden till offline mottagare köas på relay; levereras vid reconnect |
| M9 | **Push-notifikationer** | Data push väcker app; **ingen** avsändare/innehåll i notification |
| M10 | **Lokal krypterad lagring** | All historik i SQLCipher DB |
| M11 | **Nyckelverifiering** | Safety numbers / QR compare |
| M12 | **Blockera kontakt** | Lokal blocklist |
| M13 | **App-lås** | PIN/biometri |
| M14 | **En enhet** | ADR-004 — ingen multi-device |

### Scope (exkluderat från v1)

- Multi-device
- Video (1:1 eller grupp)
- Carrier SMS
- Cloud API wiring i build-fas (mock-first — ADR-003)
- Bilagor (senare fas)
- Federation
- Desktop-klient

### MVP-användarflöde

```
Installera → Skapa identitet → Visa/dela QR → Skanna/vaddera kontakt
→ Verifiera nycklar (valfritt) → Skicka E2EE-meddelande → Ta emot offline
→ Push notifierar → Läs meddelande lokalt
```

---

## Roadmap

### Phase 0 — Discovery ✓
Produktförståelse, krav, arkitektur, threat model, data inventory.

### Phase 1 — Architecture
ADR:er, API-design relay, protokollspec, wireframes.

### Phase 2 — Foundation
Monorepo, Git, CI/CD, linting, teststruktur, dev containers.

### Phase 3 — Local Data and Identity
Krypterad DB, identitetsnycklar, enhetsregistrering, app-lås.

### Phase 4 — Secure Messaging
libsignal integration, X3DH, Double Ratchet, send/receive, offline queue.

### Phase 4b — Multi-device & Attachments
Sesame/multi-device, krypterade bilagor, media-hantering.

### Phase 5 — Minimal Infrastructure
Relay server, push gateway, deployment, staging/prod.

### Phase 6 — Calling
WebRTC, signaling, TURN, CallKit, Android Telecom, 1:1 ljud.

### Phase 7 — Privacy Hardening
Sealed sender, padding, logging audit, metadata minimization.

### Phase 8 — Security Hardening
Pen test, dependency audit, fuzzing, incident response plan.

### Phase 9 — Performance and Scale
Load test relay, latency optimization, chaos testing.

### Phase 10 — Beta
TestFlight, Play Internal Testing, feedback loop.

### Phase 11 — Production
App Store/Play release, monitoring, on-call.

---

## Prioriterad TODO-lista

### Phase 0 — Discovery

| ID | Priority | Status | Description | Dependencies | Acceptance criteria | Complexity | Sec | Priv | Data |
|----|----------|--------|-------------|--------------|---------------------|------------|-----|------|------|
| T-001 | 🔴 | ✅ Done | Discovery-rapport komplett | — | Alla 31 deliverables dokumenterade | M | H | H | — |
| T-002 | 🔴 | ⏳ Pending | Godkänn arkitekturbeslut (ADR-001) | T-001 | Stakeholder sign-off | S | H | H | — |
| T-003 | 🔴 | ⏳ Pending | Besvara identitetsfrågor (Q-01, Q-08) | T-001 | Dokumenterade beslut | S | H | H | — |

### Phase 1 — Architecture

| ID | Priority | Status | Description | Dependencies | Acceptance criteria | Complexity | Sec | Priv | Data |
|----|----------|--------|-------------|--------------|---------------------|------------|-----|------|------|
| T-010 | 🔴 | Pending | Skriv ADR-001: Hybrid architecture | T-002 | ADR godkänd | M | H | H | H |
| T-011 | 🔴 | Pending | Skriv ADR-002: Signal Protocol | T-002 | Kryptoval dokumenterat | M | H | H | — |
| T-012 | 🔴 | Pending | Skriv ADR-003: Identity model | T-003 | Identitetsflöde specificerat | M | H | H | H |
| T-013 | 🟠 | Pending | Relay API spec (OpenAPI) | T-010 | Endpoints dokumenterade | L | H | H | H |
| T-014 | 🟠 | Pending | Wireframes / UX flows | T-003 | Onboarding + chat flows | M | M | M | — |
| T-015 | 🟠 | Pending | ADR-004: Push strategy | T-010 | APNs/FCM minimal metadata | M | M | H | M |

### Phase 2 — Foundation

| ID | Priority | Status | Description | Dependencies | Acceptance criteria | Complexity | Sec | Priv | Data |
|----|----------|--------|-------------|--------------|---------------------|------------|-----|------|------|
| T-020 | 🔴 | Pending | Skapa monorepo struktur | T-002 | Repo initierat, .gitignore | S | M | L | — |
| T-021 | 🔴 | Pending | CI: lint + build + test | T-020 | GitHub Actions grönt | M | H | L | — |
| T-022 | 🔴 | Pending | Secret scanning (gitleaks) | T-020 | CI blockerar secrets | S | H | L | — |
| T-023 | 🟠 | Pending | Dependency scanning | T-020 | Dependabot aktiv | S | H | L | — |
| T-024 | 🟠 | Pending | Dev container / README | T-020 | Ny dev kan bygga | M | L | L | — |
| T-025 | 🟡 | Pending | ADR template + process | T-020 | docs/adr/ på plats | S | L | L | — |

### Phase 3 — Local Data and Identity

| ID | Priority | Status | Description | Dependencies | Acceptance criteria | Complexity | Sec | Priv | Data |
|----|----------|--------|-------------|--------------|---------------------|------------|-----|------|------|
| T-030 | 🔴 | Pending | KMP shared module setup | T-020 | KMP kompilerar | M | M | L | — |
| T-031 | 🔴 | Pending | SQLCipher integration (iOS + Android) | T-030 | Encrypted DB read/write | H | H | H | H |
| T-032 | 🔴 | Pending | Identity key generation (libsignal) | T-030, T-011 | Keypair i secure storage | H | H | H | H |
| T-033 | 🔴 | Pending | Device registration flow | T-032, T-013 | Device registrerad på relay | H | H | H | H |
| T-034 | 🟠 | Pending | App lock (PIN/biometric) | T-030 | App kräver auth | M | H | M | L |
| T-035 | 🟠 | Pending | iOS: Keychain + Secure Enclave | T-032 | Nycklar ej extraherbara | H | H | H | H |
| T-036 | 🟠 | Pending | Android: Keystore + StrongBox | T-032 | Nycklar ej extraherbara | H | H | H | H |
| T-037 | 🟡 | Pending | Unit tests: crypto keygen | T-032 | Test vectors pass | M | H | L | — |

### Phase 4 — Secure Messaging

| ID | Priority | Status | Description | Dependencies | Acceptance criteria | Complexity | Sec | Priv | Data |
|----|----------|--------|-------------|--------------|---------------------|------------|-----|------|------|
| T-040 | 🔴 | Pending | libsignal session setup (X3DH) | T-032 | Session etablerad | H | H | H | H |
| T-041 | 🔴 | Pending | Double Ratchet send/receive | T-040 | E2EE roundtrip fungerar | H | H | H | H |
| T-042 | 🔴 | Pending | Message encrypt → relay → decrypt | T-041, T-050 | End-to-end fungerar | H | H | H | H |
| T-043 | 🟠 | Pending | Offline outgoing queue (local) | T-041 | Meddelanden köas offline | M | M | L | H |
| T-044 | 🟠 | Pending | Contact management (QR) | T-033 | QR add contact fungerar | M | M | M | H |
| T-045 | 🟠 | Pending | Key verification UI | T-032 | Safety numbers match | M | H | H | — |
| T-046 | 🟠 | Pending | Delivery receipts | T-042 | Avsändare ser "delivered" | M | M | M | M |
| T-047 | 🟡 | Pending | Block contact | T-044 | Blocked users döljs | S | M | M | L |
| T-048 | 🟡 | Pending | Integration tests: messaging | T-042 | CI test pass | M | H | M | — |

### Phase 5 — Minimal Infrastructure

| ID | Priority | Status | Description | Dependencies | Acceptance criteria | Complexity | Sec | Priv | Data |
|----|----------|--------|-------------|--------------|---------------------|------------|-----|------|------|
| T-050 | 🔴 | Pending | Relay server MVP | T-013 | Message + key endpoints | H | H | H | H |
| T-051 | 🔴 | Pending | Ephemeral message queue (TTL) | T-050 | Auto-delete efter ACK/TTL | M | H | H | H |
| T-052 | 🔴 | Pending | Push gateway (APNs + FCM) | T-050, T-015 | Push väcker app | H | M | H | M |
| T-053 | 🟠 | Pending | Rate limiting | T-050 | Spam begränsat | M | H | M | M |
| T-054 | 🟠 | Pending | TLS + cert pinning (client) | T-050 | MITM detekteras | M | H | L | — |
| T-055 | 🟠 | Pending | Deploy staging (EU) | T-050 | Staging relay live | M | H | H | H |
| T-056 | 🟡 | Pending | Load test relay (baseline) | T-050 | 1000 concurrent connections | M | M | L | — |

### Phase 6 — Calling (post-MVP)

| ID | Priority | Status | Description | Dependencies | Acceptance criteria | Complexity | Sec | Priv | Data |
|----|----------|--------|-------------|--------------|---------------------|------------|-----|------|------|
| T-060 | 🟠 | Pending | Signaling server | T-050 | ICE exchange fungerar | H | H | H | H |
| T-061 | 🟠 | Pending | TURN server deployment | T-060 | NAT traversal >80% | H | H | H | H |
| T-062 | 🟠 | Pending | WebRTC 1:1 audio | T-060 | Samtal fungerar | H | H | H | H |
| T-063 | 🟠 | Pending | iOS CallKit integration | T-062 | Native call UI | H | M | M | — |
| T-064 | 🟠 | Pending | Android Telecom integration | T-062 | Native call UI | H | M | M | — |
| T-065 | 🟡 | Pending | ADR: IP leakage mitigation | T-061 | TURN-only option dokumenterad | M | H | H | — |

---

## Första konkreta implementationsteg (efter godkännande)

1. **T-002** — Du godkänner ADR-001 (hybrid architecture)
2. **T-003** — Du besvarar identitetsfrågor
3. **T-020** — Skapa monorepo med dokumentation, CI, tomma moduler
4. **T-030–T-032** — KMP + lokal krypterad DB + nyckelgenerering
5. **T-050** — Minimal relay (key directory + message queue)
6. **T-040–T-042** — Första E2EE-meddelande end-to-end

**Inget steg ovan innebär "skumma" placeholders — varje steg har tydliga acceptance criteria.**
