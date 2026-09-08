# Discovery — Säker kommunikationsapp

**Status:** Phase 0 — Discovery (väntar på godkännande)  
**Datum:** 2026-09-02  
**Version:** 0.1.0-draft

## Syfte

Denna mapp innehåller Discovery-rapporten för ett nytt kommunikationsprojekt från grunden: en integritetsfokuserad, E2EE-kapabel messenger för iOS och Android.

**Ingen applikationskod har skrivits ännu.** Endast analys, krav, arkitektur och planering.

## Dokumentindex

| Dokument | Innehåll |
|----------|----------|
| [01-product-understanding.md](./01-product-understanding.md) | Produktförståelse, målgrupp, kärnfunktioner |
| [02-requirements.md](./02-requirements.md) | Funktionella, icke-funktionella, säkerhets- och integritetskrav |
| [03-architecture-comparison.md](./03-architecture-comparison.md) | Centraliserad, lokal-first, P2P, federerad, relay, hybrid |
| [04-data-classification.md](./04-data-classification.md) | Data inventory och klassificering |
| [05-threat-model.md](./05-threat-model.md) | Threat model med mitigations |
| [06-mvp-and-roadmap.md](./06-mvp-and-roadmap.md) | MVP, roadmap, prioriterad TODO |
| [07-tech-stack.md](./07-tech-stack.md) | Teknikval, iOS/Android-strategi |
| [08-crypto-and-messaging.md](./08-crypto-and-messaging.md) | Kryptografi, messaging, offline, metadata |
| [09-calling-architecture.md](./09-calling-architecture.md) | Ljud/video-samtal (ej MVP) |
| [10-infrastructure.md](./10-infrastructure.md) | Relay, push, TURN, DevSecOps |
| [11-repository-and-cicd-plan.md](./11-repository-and-cicd-plan.md) | Repo-struktur, CI/CD (plan, ej implementerad) |
| [12-risks-and-decisions.md](./12-risks-and-decisions.md) | Riskregister, öppna frågor, beslut som kräver godkännande |
| [EXECUTIVE-SUMMARY.md](./EXECUTIVE-SUMMARY.md) | Sammanfattning och rekommendationer |

## Rekommenderat projektnamn (förslag)

**Codename:** `veil` (intern) / **`Veil Messenger`** (produkt)

Alternativ: `nexus-comm`, `quietline`, `enclave-chat`

Namnet är inte låst — kräver produktbeslut.

## Nästa steg efter godkännande

1. Godkänn arkitekturbeslut (ADR-001)
2. Godkänn MVP-scope och identitetsmodell
3. Skapa repository-struktur (Phase 2)
4. Initiera Git, CI/CD, linting
5. Börja Phase 3 — Local Data and Identity
