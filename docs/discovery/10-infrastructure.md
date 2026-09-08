# 13, 18, 26. Infrastruktur och DevSecOps

## Minimal serverinfrastruktur (MVP)

```
                    ┌─────────────────────────────────┐
                    │         CDN / WAF / DDoS        │
                    └───────────────┬─────────────────┘
                                    │
                    ┌───────────────▼─────────────────┐
                    │      Relay Cluster (Rust)       │
                    │  ┌─────────┐  ┌──────────────┐  │
                    │  │ Key Dir │  │ Message Queue│  │
                    │  │ endpoint│  │ (Redis TTL)  │  │
                    │  └─────────┘  └──────────────┘  │
                    │  ┌─────────┐  ┌──────────────┐  │
                    │  │ Device  │  │ Push Gateway │  │
                    │  │ Registry│  │ APNs + FCM   │  │
                    │  └─────────┘  └──────────────┘  │
                    └───────────────┬─────────────────┘
                                    │
                    ┌───────────────▼─────────────────┐
                    │   Minimal persistent store      │
                    │   (Redis/PostgreSQL — keys only)│
                    └─────────────────────────────────┘
```

---

## Komponentanalys

| Komponent | Behövs? | MVP | Beskrivning |
|-----------|---------|-----|-------------|
| API Gateway | Ja | Ja | nginx/envoy — TLS termination, rate limit |
| Authentication | Ja | Ja | Device certificate / signed requests (ej lösenord) |
| Authorization | Ja | Ja | Device can only fetch own messages |
| User identity DB | **Minimal** | Ja | Opaque ID + public keys only |
| Device registration | Ja | Ja | Device ID, push token, key bundle |
| Key distribution | Ja | Ja | Key directory endpoint |
| Messaging | Ja | Ja | Encrypted message relay |
| Message queues | Ja | Ja | Redis med TTL |
| WebSockets | Ja | Ja | Real-time message fetch |
| Signaling | Nej | Phase 6 | WebRTC signaling |
| Database | **Minimal** | Ja | Key directory + device registry (INTE meddelanden) |
| Cache | Ja | Ja | Redis — keys + queues |
| Object storage | Nej | Phase 4b | Bilagor |
| TURN | Nej | Phase 6 | coturn |
| Push gateway | Ja | Ja | APNs + FCM |
| Rate limiting | Ja | Ja | Per IP + per device |
| DDoS-skydd | Ja | Ja | CDN/WAF |
| Secrets management | Ja | Ja | Vault / cloud KMS |
| Encryption at rest | Ja | Ja | Redis/DB disk encryption |
| Logging | **Minimal** | Ja | Metrics only, no PII |
| Monitoring | Ja | Ja | Prometheus + Grafana |
| Alerting | Ja | Ja | PagerDuty/Opsgenie |

---

## Relay API (preliminär)

| Method | Endpoint | Syfte |
|--------|----------|-------|
| PUT | `/v1/accounts/{id}/keys` | Registrera key bundle |
| GET | `/v1/accounts/{id}/keys` | Hämta key bundle |
| PUT | `/v1/devices/{id}` | Registrera enhet + push token |
| DELETE | `/v1/devices/{id}` | Avregistrera enhet |
| POST | `/v1/messages` | Skicka krypterat meddelande |
| GET | `/v1/messages` | Hämta väntande meddelanden |
| DELETE | `/v1/messages/{id}` | ACK — radera meddelande |
| WS | `/v1/stream` | Real-time meddelanden |

Alla endpoints kräver **device-autentiserade requests** (signerade med device private key).

---

## Stateless design

| Egenskap | Implementation |
|----------|----------------|
| Ingen meddelandeinbox | Meddelanden raderas efter ACK |
| TTL | Max 30 dagar i kö |
| Ingen plaintext | Server processar aldrig klartext |
| Minimal logging | Aggregerade metrics; inga access logs med IP |
| Horisontell skalning | Stateless relay bakom load balancer |

---

## Serverdata vid fel

| Fel | Konsekvens | Recovery |
|-----|------------|----------|
| Relay nere | Meddelanden ej levererade; lokal kö hos avsändare | Auto-retry; TTL-meddelanden väntar |
| Redis crash | Väntande meddelanden förloras | Acceptabelt — avsändare retry; forward secrecy |
| Key directory förlust | Nycklar måste re-registreras | Enheter re-registrerar vid connect |
| Push gateway nere | Ingen push; polling vid app open | Degraded, not broken |

---

## Abuse prevention (utan central användardatabas)

| Mekanism | Beskrivning |
|----------|-------------|
| Rate limiting | Per IP, per device ID, per account hash |
| Proof-of-work | Valfritt vid account creation (Hashcash-style) |
| Registration cost | Ingen gratis oändlig registrering |
| Block | Lokal (klient) |
| Report | E2EE report till moderering (Phase 8) |
| IP block | Temporär, automatisk |

**Tradeoff:** Utan telefonnummer-verifiering ökar spam-risk. Mitigation: rate limits + proof-of-work.

---

## Miljöer

| Miljö | Syfte | Data |
|-------|-------|------|
| **Local** | Utveckling | Mock relay, test keys |
| **Staging** | Integration test | Test APNs/FCM; separata keys |
| **Production** | Live | Riktiga push; EU region |

---

## DevSecOps / CI/CD (plan)

| Pipeline | Verktyg |
|----------|---------|
| CI | GitHub Actions |
| Lint (Kotlin) | ktlint, detekt |
| Lint (Swift) | SwiftLint |
| Lint (Rust) | clippy |
| Format | ktfmt, swift-format, rustfmt |
| Unit tests | JUnit (KMP), XCTest, cargo test |
| SAST | CodeQL |
| Dependency scan | Dependabot, cargo-audit |
| Secret scan | gitleaks |
| SBOM | syft / cyclonedx |
| Mobile build | Fastlane (iOS + Android) |
| Signering | Match (iOS), Play App Signing |
| Container | Docker → GHCR |
| Deploy | ArgoCD / Terraform |
| Reproducible builds | Locked dependencies |

### Release pipeline

```
PR → CI (lint, test, SAST, scan) → Merge → Staging deploy
→ Integration tests → Manual approval → Production deploy
→ TestFlight/Play Internal → Beta → Production release
```

### Secrets

- **Aldrig** i repo
- GitHub Environments med protection rules
- OIDC för cloud deploy (ej long-lived tokens)
- APNs key (.p8) och FCM service account i vault

---

## Observability

| Typ | Vad | PII |
|-----|-----|-----|
| Metrics | Request rate, latency, queue depth, push success | Nej |
| Logs | Error traces (sanitized) | Nej |
| Traces | Distributed tracing (relay) | Nej |
| Alerts | Error rate, latency p99, queue backlog | Nej |
| Crash | Opt-in, anonymiserad (client) | Nej |

**Ingen användar-level tracking. Ingen analytics.**

---

## Incident response (plan)

1. Detektera (alert)
2. Isolera (block IP, drain node)
3. Utvärdera (metadata exposure? key compromise?)
4. Rotera (TLS certs, server keys)
5. Kommunicera (transparency report)
6. Post-mortem (blameless)

---

## Infrastrukturkrav sammanfattning

| Resurs | MVP | Production (10k users) |
|--------|-----|------------------------|
| Relay nodes | 1 | 2–3 (HA) |
| Redis | 1 | 3 (cluster) |
| DB (keys only) | 1 small | 2 (HA) |
| Bandbredd | ~100 Mbps | ~1 Gbps |
| Region | EU (1) | EU (2+) |
| Uppskattad kostnad | ~€50/mo | ~€200–500/mo |

(TURN tillkommer betydligt i Phase 6.)
