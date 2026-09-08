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
cp local.properties.example local.properties
# Edit sdk.dir to your Android SDK path (Android Studio sets this automatically)
./gradlew :apps:android:app:assembleDebug
```

### Android Studio + emulator (recommended for UI work)

**Important:** Do **not** click "New Project". Open the existing repo:

1. Android Studio → **File → Open**
2. Select the **repository root** folder (the one containing `settings.gradle.kts`)
3. Wait for Gradle sync to finish
4. Create/start an emulator:
   - **Device Manager** (phone icon in toolbar) → **Create Device** → Pixel 7 → API 35 → Finish → **Run ▶**
   - Or from terminal: `bash scripts/create-android-emulator.sh`
5. Start the relay server in a separate terminal:
   ```bash
   cargo run -p veil-relay
   ```
6. Run the app: select run configuration **`app`** and press **Run ▶**

The emulator reaches your computer's localhost relay at `http://10.0.2.2:8080` (already configured in `build.gradle.kts`).

**Troubleshooting**

| Problem | Fix |
|---------|-----|
| "SDK location not found" | Create `local.properties` with `sdk.dir=...` (see `local.properties.example`) |
| Gradle sync fails | Use JDK 17+, open repo **root** not `apps/android/app` |
| Emulator won't start | Enable virtualization (BIOS/Hyper-V/WHPX), install HAXM/WHPX |
| App can't connect to relay | Start `cargo run -p veil-relay` before sending messages |

### Keep Desktop folder in sync with GitHub

If you work in the cloud (Cursor Agent) but also have a local copy on Desktop
(e.g. `~/Desktop/krykom2`), install auto-sync **once on your computer**:

```bash
cd ~/Desktop/krykom2          # or wherever your local clone lives
bash scripts/install-desktop-sync.sh
```

This will:
1. Clone/update `~/Desktop/krykom2` from GitHub if needed
2. Pull the latest branch every 2 minutes automatically

Manual sync anytime:

```bash
bash scripts/sync-desktop-repo.sh once
```

Custom path or branch:

```bash
VEIL_DESKTOP_REPO=~/Desktop/krykomv2 VEIL_SYNC_BRANCH=main bash scripts/install-desktop-sync.sh
```

Remove auto-sync:

```bash
bash scripts/uninstall-desktop-sync.sh
```

**Note:** GitHub pushes from the cloud agent do not push to your Desktop directly —
your Mac/PC must pull. Auto-sync handles that in the background.

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
