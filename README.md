# Veil Messenger

Privacy-first, end-to-end encrypted communication for iOS and Android.

**Version:** 0.3.0 — Phase 4 (Secure messaging)  
**Status:** Phase 4 complete — Phase 5 (group chat + relay wiring) next

## Features (v1 scope)

| Feature | Status |
|---------|--------|
| E2EE 1:1 messaging | ✅ Phase 4 |
| Identity + local keys | ✅ Phase 3 |
| App lock (PIN) | ✅ Phase 3 |
| SQLCipher local DB | ✅ Android Phase 3 |
| E2EE group chat | Planned (Phase 5) |
| 1:1 voice calls | Planned (Phase 6) |
| Group voice calls | Planned (Phase 6) |
| Multi-device | **Excluded** (ADR-004) |
| Video calls | **Excluded** |
| Cloud APIs (APNs/FCM/TURN) | **Deferred** — interfaces ready (ADR-003) |

## Architecture

- **Hybrid local-first + stateless relay** ([ADR-001](docs/adr/001-hybrid-architecture.md))
- **Signal Protocol** (libsignal) — Phase 4 ✅
- **KMP shared core** + native UI (SwiftUI / Jetpack Compose)
- **Rust relay** server
- **Single device** per identity ([ADR-004](docs/adr/004-single-device-policy.md))

## Repository structure

```
veil-messenger/
├── apps/
│   ├── android/          # Jetpack Compose app
│   └── ios/              # SwiftUI app (Xcode on macOS)
├── shared/               # KMP — models, ports, mock services
├── server/relay/         # Rust stateless relay
├── docs/                 # Discovery, ADRs, API spec
├── infra/                # Docker, Terraform (future)
└── .github/workflows/    # CI
```

## Quick start

### Relay server (in-memory, local)

```bash
cp .env.example .env
cargo run -p veil-relay
# → http://localhost:8080/health
```

### Kotlin shared module tests

```bash
./gradlew :shared:jvmTest
```

### Android app (requires Android SDK)

```bash
cp apps/android/local.properties.example apps/android/local.properties
# Edit sdk.dir
./gradlew :apps:android:app:assembleDebug
```

### Docker (optional)

```bash
docker compose --profile relay up --build
```

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `VEIL_USE_MOCK_SERVICES` | `true` | Use in-memory mocks (no cloud) |
| `VEIL_RELAY_BASE_URL` | `http://localhost:8080` | Relay URL |
| `VEIL_RELAY_PORT` | `8080` | Relay listen port |

## Documentation

| Document | Description |
|----------|-------------|
| [Product Requirements](docs/product/REQUIREMENTS.md) | Approved v1 scope |
| [Discovery Report](docs/discovery/EXECUTIVE-SUMMARY.md) | Phase 0 analysis |
| [Relay API](docs/api/relay-openapi.yaml) | OpenAPI contract |
| [ADRs](docs/adr/) | Architecture decisions |
| [Contributing](CONTRIBUTING.md) | Dev guidelines |

## Roadmap

| Phase | Status |
|-------|--------|
| 0 — Discovery | ✅ |
| 2 — Foundation | ✅ |
| 3 — Local data & identity | ✅ |
| 4 — Secure messaging | ✅ |
| 5 — Group chat + infra | **Next** |
| 6 — Voice + group calls | |

## License

TBD — libsignal is AGPL-3.0; license decision pending (ADR pending).

## Security

See [security/SECURITY.md](security/SECURITY.md).
