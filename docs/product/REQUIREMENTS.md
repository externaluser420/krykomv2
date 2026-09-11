# Product Requirements — Veil Messenger v1

**Status:** Approved  
**Date:** 2026-09-02

## Vision

Privacy-first encrypted communication for iOS and Android.

## Functional requirements

| ID | Requirement | Priority |
|----|-------------|----------|
| FR-01 | Pseudonymous identity creation (no phone number) | P0 |
| FR-02 | E2EE 1:1 text messaging | P0 |
| FR-03 | E2EE group chat | P0 |
| FR-04 | 1:1 encrypted voice calls | P0 |
| FR-05 | Encrypted group voice calls | P0 |
| FR-06 | Offline message delivery | P0 |
| FR-07 | Push notification on new message (no content in notification) | P0 |
| FR-08 | Contact add via QR / identity ID | P0 |
| FR-09 | Key verification (safety numbers) | P1 |
| FR-10 | Block contact | P1 |
| FR-11 | App lock (PIN / biometric) | P1 |
| FR-12 | Delete account | P1 |
| FR-13 | Delivery status (sent / delivered) | P1 |

## Non-functional requirements

| ID | Requirement |
|----|-------------|
| NFR-01 | Message delivery latency < 2s p95 (online) |
| NFR-02 | iOS 16+, Android 8+ (API 26) |
| NFR-03 | Relay availability 99.9% (production) |
| NFR-04 | Local DB encrypted (AES-256 SQLCipher) |
| NFR-05 | No third-party analytics |
| NFR-06 | GDPR-first, EU hosting (production) |

## Out of scope v1

- Multi-device
- Video calls
- Carrier SMS
- Desktop clients
- Federation
- Cloud provider wiring (interfaces only until decided)

## Security requirements

See [docs/discovery/12-risks-and-decisions.md](../discovery/12-risks-and-decisions.md) SR-01 through SR-12.

## Architecture

- ADR-001: Hybrid local-first + stateless relay
- ADR-002: Product scope (this document)
- ADR-003: Mock-first infrastructure
- ADR-004: Single device
