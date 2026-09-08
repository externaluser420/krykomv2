# ADR-001: Hybrid Local-First Architecture with Stateless Relay

## Status

**Proposed** — väntar på godkännande

## Date

2026-09-02

## Context

Vi bygger en integritetsfokuserad kommunikationsapp från grunden. En central arkitekturfråga är om appen kan fungera utan en traditionell central databas, och vilken minimal infrastruktur som krävs för tillförlitlig mobil messaging med offline-stöd och push-notifikationer.

Krav:
- E2EE meddelanden
- Offline-leverans
- Push-notifikationer
- Minimal metadata
- iOS + Android
- Ingen central användarprofil-databas

## Decision

Vi väljer en **hybrid lokal-first arkitektur med stateless relay**:

1. **All användardata** (meddelandeinnehåll, kontakter, historik) lagras **primärt lokalt** i krypterad databas på enheten
2. **Relay-server** hanterar:
   - Publik nyckelkatalog (key directory)
   - Tillfällig meddelandekö (krypterad, TTL-baserad)
   - Enhetsregistrering (opaque ID, push token hash)
   - Push-triggers
3. **Ingen persistent central meddelandedatabas** — meddelanden raderas efter leverans+ACK
4. **Signal Protocol** (libsignal) för E2EE
5. **Federation** designas in i API men implementeras inte i MVP

## Alternatives Considered

| Alternativ | För | Emot |
|------------|-----|------|
| **Traditionell central DB** | Enkel multi-device, backup, sök | Motsäger integritetsmål; metadata-risk |
| **Ren P2P** | Bästa integritet online | Omöjlig offline/push på mobil |
| **Federerad (Matrix-liknande)** | Decentraliserat ägande | För komplex för MVP |
| **Blockchain/DHT** | Decentraliserad discovery | Opålitlig, långsam, over-engineered |
| **Hybrid (valt)** | Bästa praktiska balans | Metadata kvarstår på relay |

## Consequences

### Positive
- Server kan inte läsa meddelandeinnehåll
- Ingen central inbox att läcka
- Offline-first naturligt
- Skalbart (stateless relay)
- Välkänd modell (Signal-bevisad)

### Negative
- Metadata (kommunikationsgraf) synlig för relay-operatör
- Multi-device kräver extra protokoll (Phase 4)
- Abuse prevention svårare utan central profil
- Recovery kräver lokal backup

## Security Implications

- E2EE skyddar innehåll; relay compromise exponerar metadata + krypterade köer
- Forward secrecy via Double Ratchet
- Key directory är publik data — integritet via signaturer
- Kräver TLS pinning, minimal logging, ACK-delete

## Scalability Implications

- Relay är horisontellt skalbart (stateless)
- Redis/queue kan bli bottleneck — sharding möjligt
- Key directory växer O(användare) — acceptabelt

## Privacy Implications

- Pseudonymitet, inte anonymitet
- Relay ser: A↔B relationer, timing, payload-storlek
- Förbättras i Phase 7: sealed sender, padding
- Ingen telefonnummer/e-post som default

## Operational Implications

- Relay + Redis + minimal DB att drifta
- EU-hosting rekommenderat
- TURN tillkommer i Phase 6 (samtal)
- Monitoring utan PII

## Failure Modes

| Failure | Impact | Mitigation |
|---------|--------|------------|
| Relay down | Ingen leverans | Client retry queue |
| Redis crash | Väntande meddelanden förloras | Sender retry; TTL |
| Key directory loss | Enheter re-registrerar | Persistent backup av keys |
| Push failure | Delayed delivery | Poll on app open |

## Recommendation

**Godkänn** denna arkitektur för MVP och långsiktig grund.

## Reasoning

Ren decentralisering (P2P, federation) offrar för mycket tillförlitlighet på mobil utan att eliminera metadata. Traditionell central DB offrar för lite integritet. Hybrid modellen är den enda realistiska som uppfyller E2EE + offline + push + minimal server trust.

## References

- [Signal Server Architecture](https://signal.org/docs/)
- [Signal Protocol Specifications](https://signal.org/docs/)
- Discovery: [03-architecture-comparison.md](../discovery/03-architecture-comparison.md)
- Discovery: [05-threat-model.md](../discovery/05-threat-model.md)
