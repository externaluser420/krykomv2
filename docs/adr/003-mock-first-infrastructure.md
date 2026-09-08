# ADR-003: Mock-First Infrastructure

## Status

**Accepted** — 2026-09-02

## Context

Cloud provider (AWS vs Hetzner) and external APIs (APNs, FCM, TURN) are not yet chosen. Development must proceed without blocking on provider decisions.

## Decision

1. Define **service interfaces** in shared core and server:
   - `RelayClient` — messaging, key directory
   - `PushService` — notification delivery
   - `TurnService` — ICE/TURN configuration
   - `SignalingService` — WebRTC signaling

2. Ship **mock/local implementations** as default:
   - `MockRelayClient` — in-memory message queue
   - `NoOpPushService` — logs only
   - `LocalSignalingService` — in-process or local WebSocket
   - `MockTurnService` — STUN-only / localhost for dev

3. **Real implementations** added later via dependency injection without changing domain logic.

4. Configuration via environment variables / build flavors — no hardcoded provider URLs.

## Alternatives considered

| Alternative | Rejected because |
|-------------|------------------|
| Wire AWS now | Provider undecided |
| Skip interfaces | Expensive refactor later |
| Full local stack day 1 | TURN/push still need real services for prod |

## Consequences

### Positive
- Parallel client/server development
- No cloud costs during build phase
- Provider-agnostic architecture

### Negative
- Integration testing against mocks may miss provider quirks
- Second phase needed to wire production services

## Failure modes

| Failure | Mitigation |
|---------|------------|
| Mock behavior diverges from prod | Contract tests + OpenAPI spec |
| Forgot to swap impl in prod | Build flavors enforce real impl in release |
