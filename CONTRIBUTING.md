# Contributing to Veil Messenger

## Principles

1. **Security first** — never implement custom cryptography
2. **Privacy by design** — minimize data collection and server storage
3. **Test before merge** — CI must pass
4. **Small commits** — logical, reviewable changes

## Commit format

```
type: description

Types: feat, fix, docs, security, test, ci, chore, refactor
```

Examples:
- `feat: add mock relay client`
- `security: harden key storage`
- `docs: update ADR-002 scope`

## Branch naming

- `cursor/<description>-db74` for agent branches
- `feat/<description>` for feature work

## Code standards

| Language | Lint | Format |
|----------|------|--------|
| Kotlin | detekt, ktlint | ktfmt (via ktlint) |
| Rust | clippy | rustfmt |
| Swift | SwiftLint (iOS, macOS) | swift-format |

## Before submitting

- [ ] No secrets in code or commits
- [ ] Tests pass locally
- [ ] ADR updated for architectural changes
- [ ] Privacy/security impact considered

## Local development

```bash
# Relay (in-memory)
cargo run -p veil-relay

# Kotlin shared tests
./gradlew :shared:jvmTest

# Android (requires SDK)
./gradlew :apps:android:app:assembleDebug
```

See [README.md](../README.md) for full setup.
