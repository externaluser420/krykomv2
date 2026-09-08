# Phase 2 — Foundation Complete

**Date:** 2026-09-02  
**Branch:** `cursor/project-foundation-db74`

## What was built

### Documentation
- [ADR-002](adr/002-product-scope.md) — Approved product scope
- [ADR-003](adr/003-mock-first-infrastructure.md) — Mock-first, cloud APIs deferred
- [ADR-004](adr/004-single-device-policy.md) — No multi-device
- [Product Requirements](product/REQUIREMENTS.md)
- [Relay OpenAPI](api/relay-openapi.yaml)

### Code structure
| Component | Path | Status |
|-----------|------|--------|
| KMP shared core | `shared/` | Models, ports, mock services, DI |
| Android app shell | `apps/android/app/` | Compose placeholder |
| iOS guide | `apps/ios/README.md` | Xcode setup on macOS |
| Rust relay | `server/relay/` | In-memory API skeleton |
| Docker | `infra/docker/`, `docker-compose.yml` | Relay image |
| CI | `.github/workflows/ci.yml` | Rust + Kotlin tests |

### Service interfaces (ready for future API wiring)
- `RelayClient` — messaging + key directory
- `PushService` — NoOp (APNs/FCM later)
- `TurnService` — Mock STUN (TURN later)
- `SignalingService` — defined, not implemented
- `MessageStore` — InMemory (SQLCipher Phase 3)
- `SecureKeyStore` — defined (Keystore Phase 3)

## Verified

```bash
cargo clippy -p veil-relay -- -D warnings  # pass
cargo test -p veil-relay                   # pass
./gradlew :shared:jvmTest                  # pass (1 test)
```

## Next phase

**Phase 3 — Local Data & Identity**
- SQLCipher integration
- libsignal key generation
- Secure Enclave / Keystore
- Device registration flow
