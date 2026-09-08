# Executive Summary — Discovery

## Vad vi rekommenderar att bygga

En **hybrid, lokal-first, E2EE-kommunikationsapp** med:

- **Pseudonym identitet** (ingen telefonnummer-krav som default)
- **Signal Protocol** (libsignal) för meddelandekryptering
- **Lokal krypterad lagring** som primär datakälla (SQLCipher / Room / Core Data)
- **Minimal stateless relay-infrastruktur** för offline-leverans, nyckelkatalog och push — **ingen traditionell central användardatabas med profiler, kontakter eller meddelandeinnehåll**
- **Native plattformsintegration** (SwiftUI + Jetpack Compose) med **delad protokoll-/kryptokärna** (Kotlin Multiplatform)
- **Ljudsamtal i Phase 6**, inte i MVP
- **Ingen analytics**, minimal serverloggning, privacy by design

## Kan appen byggas utan central databas?

**Delvis ja — men inte helt utan serverinfrastruktur.**

| Påstående | Svar |
|-----------|------|
| Utan traditionell central DB (PostgreSQL med användarprofiler, meddelanden, kontakter) | **Ja, realistiskt** |
| Utan någon server alls | **Nej, inte för mobil messaging med offline-stöd** |
| Utan lokal databas på telefonen | **Nej** — lokal krypterad DB är obligatorisk |
| Ren P2P utan relay | **Nej** — inte tillförlitligt på iOS/Android |

**Minimal serverinfrastruktur som krävs:**

1. **Key directory relay** — publika nycklar + enhetsregistrering (krypterad/obfuskerad metadata)
2. **Message relay** — krypterade meddelanden i tillfällig kö (TTL-baserad, ej persistent inbox)
3. **Push gateway** — APNs/FCM med minimal metadata (silent/data push)
4. **Signaling server** — WebRTC signaling (Phase 6)
5. **TURN server** — NAT traversal för samtal (Phase 6)
6. **Rate limiting / abuse layer** — IP-baserad, hash-baserad, utan användarprofiler

Servern **kan inte läsa meddelandeinnehåll** om Signal Protocol implementeras korrekt. Servern **kommer fortfarande se metadata**: ungefärlig kommunikationsgraf, tidsstämplar, IP-adresser, enhets-token-hash.

## Arkitekturrekommendation

| Fas | Arkitektur |
|-----|------------|
| **MVP** | Hybrid: lokal-first + minimal stateless relay (Signal-liknande) |
| **Lång sikt** | Samma grund + valfri federation av relay-noder (ej från dag 1) |

**Inte rekommenderat för MVP:** ren P2P, full decentralisering (DHT/blockchain), federerad Matrix-liknande modell.

## MVP (första version)

- 1:1 textmeddelanden, E2EE
- Pseudonym identitet (genererad nyckelpar-identitet)
- QR/kod-baserad kontaktupptäckt
- Offline-leverans via relay
- Push-notifikationer (kategoriserade, minimal metadata)
- Lokal krypterad lagring
- Nyckelverifiering (safety numbers / QR)
- En enhet (multi-device = Phase 4)
- **Ej samtal i MVP**

## Teknikval (rekommendation)

| Lager | Val |
|-------|-----|
| iOS UI | Swift, SwiftUI |
| Android UI | Kotlin, Jetpack Compose |
| Delad kärna | Kotlin Multiplatform (KMP) — protokoll, crypto wrapper, sync |
| Krypto | libsignal (Signal Protocol) |
| Lokal DB | SQLCipher via Room (Android) / GRDB+SQLCipher eller equivalent (iOS) |
| Relay | Rust (axum/tokio) eller Go — stateless, minimal |
| Infra | Kubernetes eller managed containers, Terraform |

## Beslut som kräver ditt godkännande

1. **Identitetsmodell** — pseudonym nyckelbaserad vs valfritt telefonnummer/e-post?
2. **Projektnamn och open-source-strategi** — full OSS, source-available, eller closed?
3. **Juridisk entity och datalagring** — EU-only infra? GDPR-first?
4. **Recovery-modell** — hur hanteras förlorad enhet utan central backup?
5. **Abuse/spam-strategi** — hur aggressiv rate limiting vs integritet?
6. **MVP exkluderar samtal** — godkänns detta?
7. **Teknikstack** — KMP + native UI vs full dual-native?
8. **Relay-hosting** — egen infra vs moln (AWS/GCP/Hetzner)?

## Byggordning

```
Phase 0 Discovery ✓ (denna rapport)
    ↓ [godkännande]
Phase 2 Foundation (repo, CI, tooling)
    ↓
Phase 3 Local Data & Identity
    ↓
Phase 4 Secure Messaging
    ↓
Phase 5 Minimal Infrastructure (relay, push)
    ↓
Phase 6 Calling
    ↓
Phase 7–11 Privacy, Hardening, Scale, Beta, Production
```

## Risker (topp 5)

1. **Metadata-läckage** — server ser vem som pratar med vem (även utan innehåll)
2. **Recovery utan central backup** — förlorad enhet = förlorad identitet om ingen backup
3. **Push-notifikationer** — Apple/Google ser att en enhet får notiser
4. **Abuse utan central kontroll** — svårare att stoppa spam utan att samla data
5. **Multi-device komplexitet** — Signal Protocol Sesame/multi-device är icke-trivial
