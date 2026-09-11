# ADR-002: Product Scope — Approved Requirements

## Status

**Accepted** — 2026-09-02

## Context

Stakeholder approved product scope after Discovery (Phase 0). This ADR locks functional requirements before implementation.

## Decision

### In scope (v1)

| Feature | Description |
|---------|-------------|
| **E2EE 1:1 messaging** | In-app encrypted text messages (NOT carrier SMS) |
| **E2EE group chat** | Encrypted group messaging (Sender Keys) |
| **1:1 voice calls** | Audio only, WebRTC + E2EE |
| **Group voice calls** | Audio conference, SFU-based |
| **Pseudonymous identity** | No phone number required |
| **Offline delivery** | Via stateless relay |
| **Push notifications** | Minimal metadata (wired later) |
| **Local encrypted storage** | SQLCipher on device |
| **Key verification** | Safety numbers / QR |
| **Single device** | One active device per identity |

### Explicitly out of scope (v1)

| Feature | Reason |
|---------|--------|
| **Multi-device** | Stakeholder decision — reduces complexity ~30–50% |
| **Video calls** | Stakeholder decision |
| **Carrier SMS** | Not E2EE-capable; different product |
| **Cloud API wiring** | Deferred — build interfaces + mocks first |
| **Federation** | Phase 8+ |
| **Analytics / tracking** | Privacy requirement |

## Infrastructure strategy

Build all components with **interface abstractions**. Use **mock/local implementations** during development. Cloud providers (AWS, Hetzner, APNs, FCM, TURN) connected in a future phase.

## Architecture unchanged

Hybrid local-first + stateless relay (ADR-001). Signal Protocol (libsignal) for E2EE.

## Consequences

### Positive
- Clear scope boundary
- Faster path to MVP without multi-device
- Mock-first enables offline development

### Negative
- Users cannot use tablet + phone simultaneously
- Device loss without backup = identity loss
- Group calls require SFU infrastructure (higher cost at scale)

## References

- ADR-001: Hybrid architecture
- ADR-003: Mock-first infrastructure
- ADR-004: Single device policy
