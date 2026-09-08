# Veil — iOS App

**Status:** Foundation phase — requires macOS + Xcode for builds.

## Requirements

- macOS 14+
- Xcode 16+
- iOS 16+ deployment target

## Structure (to be created on macOS)

```
apps/ios/
├── Veil/
│   ├── VeilApp.swift          # SwiftUI entry
│   ├── ContentView.swift      # Placeholder UI
│   ├── Services/              # Bridges to KMP shared framework
│   └── Resources/
├── Veil.xcodeproj
└── Config/
    ├── Debug.xcconfig
    └── Release.xcconfig
```

## KMP integration

The `shared` module produces an `shared` XCFramework via:

```bash
./gradlew :shared:assembleSharedXCFramework
```

Link the framework in Xcode. Swift UI calls Kotlin shared code through the KMP bridge.

## Configuration

| Key | Debug | Release |
|-----|-------|---------|
| `VEIL_RELAY_BASE_URL` | `http://localhost:8080` | TBD |
| `VEIL_USE_MOCK_SERVICES` | `true` | `false` (when ready) |

## Platform services (Phase 3+)

- Keychain + Secure Enclave — identity keys
- APNs — push (wired later, ADR-003)
- CallKit — group/1:1 voice (Phase 6)
- SQLCipher via GRDB — local encrypted storage

## Build

Open `Veil.xcodeproj` in Xcode. iOS project scaffolding will be generated in Phase 3 on macOS CI runner.
