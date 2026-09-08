# 7. Repository- och CI/CD-plan (ej implementerad — väntar godkännande)

## Projektnamn (förslag)

**Repository:** `veil-messenger`  
**Codename:** Veil  
**Beskrivning:** Privacy-first E2EE messenger

---

## Monorepo vs multi-repo

**Rekommendation: Monorepo**

| Fördel | Beskrivning |
|--------|-------------|
| En version | Klient + relay synkas |
| Delade kontrakt | API specs, protobuf |
| Enklare CI | En pipeline med paths-filter |
| Atomic changes | Protocol change = client + server i samma PR |

---

## Föreslagen struktur

```
veil-messenger/
├── README.md
├── LICENSE
├── .gitignore
├── .env.example
├── .editorconfig
├── docs/
│   ├── discovery/          # Phase 0 (denna rapport)
│   ├── adr/                # Architecture Decision Records
│   ├── api/                # OpenAPI specs
│   ├── security/           # Threat model, security policy
│   └── privacy/            # Privacy policy draft, data handling
├── apps/
│   ├── android/            # Jetpack Compose app
│   │   ├── app/
│   │   └── build.gradle.kts
│   └── ios/                # SwiftUI app
│       ├── Veil/
│       └── Veil.xcodeproj
├── shared/                 # KMP shared core
│   ├── core/
│   │   ├── crypto/
│   │   ├── protocol/
│   │   ├── storage/
│   │   ├── network/
│   │   └── models/
│   └── build.gradle.kts
├── server/
│   ├── relay/              # Rust relay server
│   │   ├── src/
│   │   ├── Cargo.toml
│   │   └── Dockerfile
│   └── push/               # Push gateway (kan vara modul i relay)
├── infra/
│   ├── terraform/          # EU infrastructure
│   ├── docker/
│   └── k8s/
├── proto/                  # Protobuf/gRPC definitions (om applicable)
├── tools/
│   ├── scripts/
│   └── loadtest/
├── .github/
│   ├── workflows/
│   │   ├── ci.yml
│   │   ├── android-release.yml
│   │   ├── ios-release.yml
│   │   ├── relay-deploy.yml
│   │   └── security-scan.yml
│   ├── dependabot.yml
│   └── CODEOWNERS
├── security/
│   └── SECURITY.md
└── CONTRIBUTING.md
```

---

## Konfigurationsstruktur

| Fil | Syfte |
|-----|-------|
| `.env.example` | Template för dev secrets (ej riktiga värden) |
| `apps/android/local.properties.example` | SDK paths |
| `apps/ios/Config/Staging.xcconfig` | Staging relay URL |
| `apps/ios/Config/Production.xcconfig` | Prod relay URL |
| `server/relay/config.example.toml` | Relay config |

---

## Miljöhantering

| Miljö | Relay URL | Push | Keys |
|-------|-----------|------|------|
| local | `localhost:8080` | Mock | Test keys |
| staging | `staging.relay.veil.example` | Sandbox APNs | Staging |
| production | `relay.veil.example` | Production APNs/FCM | Production |

---

## Lokal utvecklingsmiljö

| Komponent | Krav |
|-----------|------|
| Android | Android Studio, SDK 26+ |
| iOS | Xcode 16+, macOS |
| KMP | JDK 17 |
| Relay | Rust 1.80+, Docker |
| Redis | Docker compose |

```bash
# Planerat (efter implementation)
docker compose up -d redis
cargo run -p relay
# Android/iOS pekar på localhost relay
```

---

## Teststruktur

```
shared/core/src/commonTest/     # KMP unit tests
server/relay/tests/               # Rust integration tests
apps/android/app/src/test/        # Android unit
apps/android/app/src/androidTest/ # Android instrumented
apps/ios/VeilTests/               # iOS unit
tools/loadtest/                   # k6 load tests
```

---

## Kodstandarder

| Språk | Lint | Format |
|-------|------|--------|
| Kotlin | detekt, ktlint | ktfmt |
| Swift | SwiftLint | swift-format |
| Rust | clippy | rustfmt |

---

## GitHub-strategi

| Aspekt | Policy |
|--------|--------|
| Branching | Trunk-based med `main` + `cursor/*` feature branches |
| PR | Required review; CI must pass |
| Protected branches | `main` — no direct push |
| Secrets | GitHub Environments (staging, production) |
| Releases | Tags `v*.*.*`; GitHub Releases med SBOM |

---

## CI/CD pipelines (plan)

### `ci.yml` (varje PR)
- KMP build + test
- Android build + lint + test
- iOS build + lint + test (macOS runner)
- Rust relay build + test + clippy
- CodeQL
- gitleaks

### `security-scan.yml` (weekly)
- cargo-audit
- dependency review
- SBOM generation

### `relay-deploy.yml` (main → staging, tag → prod)
- Docker build
- Push to registry
- Deploy via Terraform/ArgoCD

### `android-release.yml` / `ios-release.yml`
- Fastlane beta/production
- Signed artifacts

---

## GitHub-koppling

**Status:** Ingen remote konfigurerad ännu.

När autentisering finns:
```bash
git remote add origin git@github.com:<org>/veil-messenger.git
git push -u origin main
```

Om push misslyckas p.g.a. auth rapporteras exakt vad som saknas.

---

## Decentraliserad nodstruktur (lång sikt)

```
                    ┌──────── User A's relay (optional self-host)
                    │
Central relay (MVP) ─┼──────── User B's relay (optional self-host)
                    │
                    └── Federation protocol (Phase 8)
```

Design relay-API för framtida federation men implementera single-operator i MVP.
