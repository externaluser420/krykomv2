# ADR-004: Single Device Policy

## Status

**Accepted** — 2026-09-02

## Context

Multi-device sync (Signal Sesame) adds significant cryptographic and UX complexity. Stakeholder opted out.

## Decision

- Each identity is bound to **one registered device** at a time.
- Registering a new device **revokes** the previous device on the relay.
- No cross-device message sync.
- Device migration requires **encrypted backup import** (future) or new identity.

## Security implications

- Smaller attack surface (no multi-device key fan-out)
- Stolen device = single point of compromise
- Simpler key management (one device key pair per identity)

## Privacy implications

- Less metadata (no multi-device routing)

## Operational implications

- Users must understand: new phone = migrate or lose history
- Support burden for device migration

## References

- Signal Sesame (NOT used in v1)
- Recovery strategy in Phase 4b
