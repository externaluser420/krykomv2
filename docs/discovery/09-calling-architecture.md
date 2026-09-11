# 11. Samtalsarkitektur (Phase 6 — ej MVP)

## Scope

| Funktion | Phase |
|----------|-------|
| 1:1 ljudsamtal | Phase 6 |
| 1:1 videosamtal | Phase 7+ |
| Gruppsamtal | Phase 8+ |

**Samtal ingår INTE i MVP.** Analysen nedan säkerställer att messaging-arkitekturen inte blockerar framtida samtal.

---

## Arkitekturöversikt

```
┌──────────┐                              ┌──────────┐
│  Caller  │                              │  Callee  │
└────┬─────┘                              └────┬─────┘
     │                                         │
     │  1. Call request (E2EE via messaging)   │
     │────────────────────────────────────────>│
     │                                         │
     │  2. Signaling (SDP/ICE via relay)       │
     │<───────────────────────────────────────>│
     │                                         │
     │  3. Media (WebRTC SRTP)                 │
     │<══════════ P2P eller TURN ═════════════>│
     │                                         │
```

---

## Komponenter

| Komponent | Roll | Central DB? |
|-----------|------|-------------|
| **Call signaling server** | ICE/SDP-utbyte | Nej — transient |
| **STUN server** | NAT discovery | Nej — stateless |
| **TURN server** | Media relay | Nej — session state (kort) |
| **Messaging channel** | Samtalshistorik, call invite | Befintlig relay |
| **CallKit / Telecom** | Native call UI | Lokal |

---

## WebRTC stack

| Lager | Teknik |
|-------|--------|
| Media | WebRTC (libwebrtc) |
| Kryptering | DTLS-SRTP (inbyggt i WebRTC) |
| NAT | ICE (STUN + TURN) |
| Signaling | Egen signaling over WebSocket |
| iOS | GoogleWebRTC / LiveKit SDK (utvärdera) |
| Android | Stream WebRTC / org.webrtc |

### Kryptering av samtal

- **DTLS-SRTP** ger E2EE för media (per session)
- **Insertable Streams** (E2EE overlay) — valfritt extra lager (Signal-style)
- Signaling (SDP) innehåller fingerprint för MITM-skydd

---

## NAT traversal

| Metod | Användning |
|-------|------------|
| **STUN** | Upptäck public IP/port |
| **TURN** | Relay media när P2P misslyckas (~80% av samtal) |
| **ICE** | Aggregerar kandidater, väljer bästa väg |

### IP-läckor

| Risk | Allvarlighet | Mitigation |
|------|--------------|------------|
| Host ICE candidate avslöjar lokal IP | Medium | mDNS obfuscation; TURN-only mode |
| STUN avslöjar public IP | **High** | TURN-only mode (tradeoff: latency, kostnad) |
| TURN server loggar IP | **High** | Minimal logging; EU hosting; short retention |
| Signaling server ser IP | Medium | Minimal retention |

**Rekommendation:** Erbjud **"Maximal integritet"**-läge med TURN-only (ingen P2P). Default: ICE med P2P-försök + TURN fallback (bättre kvalitet, sämre integritet).

---

## Push och inkommande samtal

### iOS

- **PushKit (VoIP push)** — väcker appen för inkommande samtal
- **MÅSTE** rapportera till CallKit inom ~seconds (Apple policy)
- VoIP push får **inte** användas för meddelanden (ban-risk)

### Android

- **FCM high priority** + **Foreground Service** under samtal
- **Telecom API** — visa native call UI
- **Full-screen intent** för inkommande samtal

---

## Samtalsmetadata

| Metadata | Vem ser | Kan minimeras? |
|----------|---------|----------------|
| Vem ringde vem | Signaling server | Delvis (E2EE invite via messaging) |
| Samtalsstart/slut | Signaling, TURN | Minimal logging |
| Duration | Lokal (valfritt) | Ja — default av |
| IP-adresser | TURN, signaling | TURN-only; VPN |
| Samtalskvalitet metrics | Operatör | Opt-in |

**Rekommendation:** Ingen server-side samtalslogg. Lokal samtalshistorik **default av**.

---

## Nätverksbyte och återanslutning

- ICE restart vid nätverksbyte (Wi-Fi ↔ cellular)
- TURN-hållbar session under kort avbrott
- UX: "Ansluter..." → "Återansluten" / "Samtal avslutat"

---

## Batteri och prestanda

- WebRTC är CPU/intensivt — acceptabelt under samtal
- Persistent TURN under samtal (ej utanför)
- Adaptiv bitrate för dåliga nätverk

---

## Missed calls

- Call invite skickas via messaging (E2EE)
- Timeout (30–60s) → "Missed call" meddelande lokalt
- Push till callee via VoIP push (iOS) / FCM (Android)

---

## Vad fungerar utan central databas?

| Funktion | Utan central DB |
|----------|-----------------|
| Samtaletablering | **Ja** — signaling + messaging |
| Samtalsmedia | **Ja** — P2P/TURN |
| Samtalshistorik | **Ja** — lokal |
| Missed call notification | **Ja** — via messaging + push |
| Kontogrupp-samtal | **Svårt** — SFU behövs (Phase 8) |

---

## Infrastrukturkrav (Phase 6)

| Tjänst | Uppskattad kostnad |
|--------|-------------------|
| Signaling (del av relay) | Ingår |
| STUN (coturn) | Låg |
| TURN (coturn) | **Medium-hög** (bandbredd) |
| **Totalt** | TURN-dominerad; budgetera €500–2000/mo vid 10k användare |

---

## Beslut

- Samtal **efter** messaging MVP är stabil
- WebRTC med DTLS-SRTP
- TURN-only integritetsläge erbjuds
- Ingen server-side samtalslogg
