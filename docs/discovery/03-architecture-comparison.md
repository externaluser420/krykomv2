# 4. Arkitekturjämförelse — Databasfri och decentraliserad

## Sammanfattning i ett stycke

Appen **kan inte** byggas helt utan serverinfrastruktur på iOS/Android och leverera tillförlitlig offline-messaging med push. Appen **kan** byggas **utan en traditionell central databas** (persistent användarprofiler, meddelandeinbox, kontakter) genom en **hybrid lokal-first + stateless relay**-modell liknande Signal. Ren P2P är **inte realistisk** som primär transport för mobilmeddelanden. Federation är **möjlig långsiktigt** men **inte lämplig för MVP**.

---

## 4.1 Lokal-first-arkitektur

### Beskrivning

All användardata (meddelanden, kontakter, nycklar utom publika) lagras primärt i **lokal krypterad databas** på enheten. Servern är inte source of truth för konversationer.

### Komponenter

| Komponent | Implementation |
|-----------|----------------|
| Lokal DB | SQLCipher (SQLite) — Room (Android), GRDB/SQLCipher (iOS) |
| Nycklar | Secure Enclave (iOS), Android Keystore (StrongBox om tillgänglig) |
| Sync | Client-driven: hämta från relay, spara lokalt, bekräfta mottagning |
| Konflikter | Meddelanden är append-only med deterministiska ID; redigering = nytt meddelande |

### Analys per punkt

| Aspekt | Analys |
|--------|--------|
| **Offline-first** | Fullständigt stöd — skriv lokalt, skicka när online |
| **Synkronisering** | Client pull/push via relay; server håller ej canonical history |
| **Meddelandeköer** | Utgående kö lokalt; inkommande kö på relay (TTL) |
| **Återställning** | Kräver krypterad backup ELLER acceptera dataförlust |
| **Byte av enhet** | Ny enhet = ny enhetsnyckel; identitetsnyckel via backup/seed |
| **Förlorad telefon** | Data borta om ingen backup; forward secrecy skyddar historik |
| **Flera enheter** | Kräver multi-device protokoll (Sesame) — Phase 4 |
| **Push** | Server triggar push baserat på enhetsregistrering, inte meddelandeinnehåll |
| **Serverns roll** | Relay, key directory, push — inte datalager |

### Viktigt

> En lokal krypterad databas **är en databas**. Skillnaden är **var** data lagras och **vem** som kontrollerar den. Användaren äger sin data lokalt; operatören lagrar inte meddelandeinbox.

---

## 4.2 Peer-to-peer

### Beskrivning

Enheter ansluter direkt via WebRTC DataChannels, Bluetooth/NFC, eller liknande utan central meddelandeserver.

### Teknisk analys

| Teknik | Roll |
|--------|------|
| **ICE** | Samlar anslutningsvägar |
| **STUN** | Upptäcker publik IP/port |
| **TURN** | Relä när direktanslutning misslyckas |
| **WebRTC DataChannel** | Krypterad datakanal (DTLS-SRTP) |

### Varför ren P2P är svår för mobilmeddelanden

1. **Offline-mottagare** — P2P kräver att båda är online samtidigt; ingen naturlig kö
2. **iOS bakgrund** — iOS suspenderar appar aggressivt; inkommande P2P kräver push + wakeup
3. **NAT/CGNAT** — de flesta mobila nätverk blockerar inkommande anslutningar
4. **Batteri** — persistent lyssnande drainar batteri
5. **Nätverksbyte** — Wi-Fi ↔ cellular bryter anslutningar
6. **IP-läckor** — direktanslutning avslöjar IP-adresser för båda parter
7. **Signaling** — P2P kräver ändå en signaling-server för ICE-utbyte
8. **App Store** — bakgrundsnätverk begränsat; VoIP-push missbrukas inte för meddelanden

### Realistisk P2P-användning

| Scenario | P2P lämpligt? |
|----------|---------------|
| Primär meddelandetransport | **Nej** |
| Online filöverföring (stora bilagor) | **Delvis** (Phase 5+) |
| Samtal (WebRTC media) | **Ja**, med TURN-fallback |
| Nyckelverifiering (QR, NFC) | **Ja**, offline |
| LAN-synk (samma Wi-Fi) | **Experimentellt** |

---

## 4.3 Federerad arkitektur

### Beskrivning

Flera oberoende servrar (som Matrix, XMPP, email) som relay-operatörer kan hosta noder; användare på olika servrar kan kommunicera.

### Analys

| Aspekt | Bedömning |
|--------|-----------|
| **Serverägarskap** | Användare/org kan self-host |
| **Trust model** | Användaren litar på sin hemnod + E2EE skyddar innehåll |
| **Identitet** | `@user:server.domain` — server ser medlemskap |
| **Moderering** | Per-server policies |
| **Abuse** | Sybil-attacker, spam över federation |
| **Metadata** | Hemnod ser all lokal trafik |
| **Interoperabilitet** | Kräver standardiserat protokoll |
| **Skalbarhet** | Horisontell per nod |
| **Komplexitet** | **Hög** — federation-protokoll, server discovery, trust |

### Referensprojekt

- **Matrix/Element** — federerad, E2EE (Olm/Megolm), komplex
- **XMPP** — federerad, E2EE via OMEMO, fragmenterat ekosystem
- **ActivityPub** — ej designat för E2EE messaging

### Rekommendation

Designa relay-API så att **federation kan läggas till** (Phase 8+), men implementera **single-operator relay i MVP**.

---

## 4.4 Stateless relay

### Beskrivning

Server vidarebefordrar krypterade meddelanden utan att kunna läsa innehåll. Minimal persistent state.

### Server-state (det som MÅSTE finnas)

| Data | Persistent? | TTL |
|------|-------------|-----|
| Enhetsregistrering (device ID, push token hash, pubkey) | Ja | Tills avregistrering |
| Publika nycklar (key bundle) | Ja | Tills rotation |
| Meddelandekö (ciphertext) | **Nej** — tillfällig | 7–30 dagar (konfigurerbart) |
| Rate limit counters | Kort | Minuter |
| (Valfritt) Opaque account ID hash | Ja | Minimal |

### Flöde

```
Avsändare                         Relay                         Mottagare
   │                                │                               │
   │── encrypt(msg, session_key) ──>│                               │
   │── POST /v1/messages ──────────>│── store ciphertext (TTL) ────>│
   │                                │── push (silent/data) ────────>│
   │                                │                               │── fetch & decrypt
   │                                │<── ACK (delete from queue) ───│
```

### Om relay komprometteras

| Vad angriparen får | Vad angriparen INTE får |
|--------------------|-------------------------|
| Metadata (vem→vem, när, storlek) | Meddelandeinnehåll (plaintext) |
| Krypterade payloads (tills levererade) | Privata nycklar |
| IP-adresser | Historik efter leverans+ACK |
| Publika nycklar | Past message keys (forward secrecy) |

### Mitigation

- TLS 1.3 + certificate pinning
- Minimize queue retention
- Padding av meddelanden (mot storleksanalys)
- Sealed sender (Signal) — dölj avsändare från relay
- Minimal logging, inga access logs med IP om möjligt

---

## 4.5 Hybridarkitektur (REKOMMENDERAD)

### Vad lagras var

| Data | Lokalt | Relay (tillfälligt) | Relay (persistent) | Aldrig centralt |
|------|--------|---------------------|--------------------|-----------------|
| Meddelandeinnehåll (plaintext) | ✓ | | | ✓ |
| Meddelandeinnehåll (ciphertext) | ✓ (efter mottagning) | ✓ (kö) | | |
| Kontaktlista | ✓ | | | ✓ |
| Privata nycklar | ✓ | | | ✓ |
| Publika nycklar | ✓ (cache) | | ✓ (directory) | |
| Push token | ✓ | | ✓ (hash) | |
| Lässtatus | ✓ | | | ✓ |
| Samtalsmetadata | ✓ | ✓ (signaling) | | |
| Blocklist | ✓ | | (valfritt sync) | |

### Nyckeldistribution

1. Identitetsnyckelpar genereras lokalt vid onboarding
2. Publik nyckel + signed prekey bundle registreras på relay
3. X3DH vid första meddelande till ny kontakt
4. Double Ratchet för pågående session

### Offline-återhämtning

1. Mottagare kommer online
2. Client pollar relay (eller push väcker appen)
3. Hämtar alla väntande ciphertexts
4. Dekrypterar, sparar lokalt, skickar ACK
5. Relay raderar levererade meddelanden

---

## 4.6 Slutsats

| Fråga | Svar |
|-------|------|
| Helt utan central databas? | **Nej** — key directory och meddelandekö kräver server-side storage |
| Utan traditionell central DB? | **Ja** — detta är rekommenderad modell |
| Minimal serverinfrastruktur | Key relay + message relay + push gateway + (senare) signaling + TURN |
| Bäst balans integritet/säkerhet/UX | **Hybrid lokal-first + stateless relay** |
| Funktioner att offra utan central DB | Centraliserad moderering, global sök, telefonbok-upptäckt, server-side backup |
| Risker med full decentralisering | Dålig mobil-upplevelse, Sybil, komplex recovery, fragmenterad abuse-hantering |
| **MVP-arkitektur** | Hybrid (lokal-first + stateless relay) |
| **Långsiktig arkitektur** | Hybrid + valfri federerad relay + förbättrad metadata-minimering (sealed sender, mix networks research) |

---

## MVP — fem alternativ jämförda

### 1. Med central databas (traditionell)

| | |
|--|--|
| **Funktionalitet** | Full: enkel multi-device, server-side search, backup |
| **Begränsningar** | Server ser metadata; större attackyta |
| **Kostnad** | Hög (DB, backup, drift) |
| **Säkerhet** | E2EE möjligt men server har metadata + risk för misconfiguration |
| **Integritet** | **Låg-medel** |
| **Komplexitet** | Medel |
| **MVP?** | **Nej** — motsäger projektmål |

### 2. Utan central databas (endast lokalt)

| | |
|--|--|
| **Funktionalitet** | Bara online P2P; ingen offline-leverans |
| **Begränsningar** | Omöjlig push; omöjlig offline |
| **Kostnad** | Minimal |
| **Säkerhet** | Bra för online; omöjlig att använda |
| **Integritet** | Hög men oanvändbar |
| **Komplexitet** | Låg men **orealistisk** |
| **MVP?** | **Nej** |

### 3. Minimal stateless relay

| | |
|--|--|
| **Funktionalitet** | E2EE messaging, offline, push, key directory |
| **Begränsningar** | Metadata kvar; abuse svårare |
| **Kostnad** | Låg-medel |
| **Säkerhet** | **Hög** (med libsignal) |
| **Integritet** | **Hög** (bäst praktisk balans) |
| **Komplexitet** | Medel |
| **MVP?** | **Ja — REKOMMENDERAD** |

### 4. Federerad modell

| | |
|--|--|
| **Funktionalitet** | Self-host, decentraliserat ägande |
| **Begränsningar** | Komplex; lång utvecklingstid |
| **Kostnad** | Varierar per operatör |
| **Säkerhet** | Beror på nod; E2EE skyddar innehåll |
| **Integritet** | Medel-hög |
| **Komplexitet** | **Hög** |
| **MVP?** | **Nej** — Phase 8+ |

### 5. Hybridmodell

| | |
|--|--|
| **Funktionalitet** | Samma som relay + lokal-first optimering |
| **Begränsningar** | Samma som relay |
| **Kostnad** | Låg-medel |
| **Säkerhet** | **Hög** |
| **Integritet** | **Hög** |
| **Komplexitet** | Medel |
| **MVP?** | **Ja** — samma som #3 med explicit lokal-first design |

### Samtal i MVP?

**Nej.** Ljudsamtal kräver WebRTC, signaling, TURN, CallKit/Telecom, bakgrundshantering — minst 4–8 veckors extra arbete med hög säkerhetsrisk. Rekommenderas **Phase 6** efter messaging är stabilt och granskat.
