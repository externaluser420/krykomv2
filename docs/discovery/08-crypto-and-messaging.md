# 8–10, 22–24. Krypto, Messaging, Metadata och Offline

## 9. Kryptografisk strategi

### Principer

1. **Aldrig egen krypto** — använd libsignal (Signal Protocol)
2. **Forward secrecy** — Double Ratchet roterar nycklar per meddelande
3. **Post-compromise security** — ratchet återhämtar efter key compromise
4. **Deniability** — Signal Protocol ger cryptographic deniability
5. **Minimal trust i server** — server ser aldrig plaintext

### Protokollstack

| Lager | Protokoll | Syfte |
|-------|-----------|-------|
| Registrering | Key bundle upload | Publika nycklar till relay |
| Initial session | **X3DH** (Extended Triple Diffie-Hellman) | Första meddelande till ny kontakt |
| Pågående | **Double Ratchet** | Alla efterföljande meddelanden |
| Multi-device (Phase 4) | **Sesame** / PQXDH | Synka across enheter |
| Grupp (Phase 5) | **Sender Keys** | Effektiv grupp-E2EE |

### Nyckeltyper

| Nyckel | Lagring | Rotation |
|--------|---------|----------|
| Identity Key Pair (IK) | Secure Enclave/Keystore | Sällan; vid compromise |
| Signed PreKey (SPK) | Lokal + relay | Veckovis |
| One-Time PreKeys (OPK) | Lokal + relay | Engångs; fyll på |
| Session keys | Ratchet state (lokal) | Per meddelande |
| Device keys | Lokal + relay | Per enhet |

### Key verification

- **Safety numbers** — SHA-256 hash av identitetsnycklar, visat som siffror
- **QR-kod** — innehåller publik identitetsnyckel + fingerprint
- Vid nyckeländring: **prominent varning** i UI

### Recovery (utan central svag punkt)

| Metod | Säkerhet | UX |
|-------|----------|-----|
| **Ingen recovery** | Bäst | Sämst — förlorad enhet = förlorad identitet |
| **Krypterad backup (användarvald fil)** | Bra | Medel |
| **24-word seed phrase** | Bra om korrekt implementerad | Dålig |
| **Server-side backup** | **Dålig** — central svag punkt | Bra |

**Rekommendation:** Valfri **krypterad backup** (AES-256-GCM, PBKDF2/Argon2id från användarens passphrase). Backup lagras **aldrig** på vår server.

### Varför libsignal?

| Kriterium | libsignal |
|-----------|-----------|
| Säkerhet | Granskad av experter; används av Signal, WhatsApp |
| Mognad | 10+ år produktion |
| Oberoende granskning | Flera formella analyser |
| Licens | AGPL-3.0 (kräver att relay också öppen källkod om distribuerad) |
| Plattform | Swift, Java/Kotlin, TypeScript, Rust |
| Underhåll | Signal Foundation, aktivt |
| Dokumentation | spec.signal.org |

---

## 19. Messaging Architecture

### Meddelandeformat (logiskt)

```
EncryptedMessage {
  message_id: UUID (deterministisk eller random)
  sender_device_id: opaque
  recipient_device_id: opaque
  timestamp: client-generated (i ciphertext)
  ciphertext: Signal Protocol encrypted payload
  padding: random bytes (metadata mot storleksanalys)
}
```

### Leveransflöde

```
1. Avsändare skapar meddelande lokalt (status: pending)
2. Hämtar mottagares key bundle från relay (om ej cachad)
3. X3DH (första gången) → etablerar session
4. Krypterar med Double Ratchet
5. Skickar ciphertext till relay
6. Relay: sparar i TTL-kö, triggar push
7. Mottagare: push → fetch → dekryptera → spara lokalt → ACK
8. Relay: raderar från kö
9. Avsändare: får delivery receipt (E2EE)
```

### Offline-stöd

| Scenario | Beteende |
|----------|----------|
| Avsändare offline | Meddelande sparas i lokal utgående kö; skickas vid reconnect |
| Mottagare offline | Relay behåller ciphertext (TTL 30 dagar); push vid registrering |
| Båda offline | Meddelande skickas när avsändare online; köas för mottagare |
| Relay nere | Lokal kö; exponential backoff retry |
| Lång offline (>TTL) | Meddelande förloras; avsändare informeras |

### Synkronisering (single device MVP)

- **Ingen server-side history** — mottagare har historik lokalt
- Avsändare har historik lokalt
- Multi-device (Phase 4): varje enhet synkar via relay encrypted sync messages

### Konflikthantering

- Meddelanden är **append-only** (inga redigeringar i MVP)
- Deterministiskt message_id för dedup
- Server-side dedup: reject duplicate message_id

---

## 10. Anonymitet, pseudonymitet och metadata

### Vad vi INTE kan kalla anonymt

| Observation | Vem ser det |
|-------------|-------------|
| Appen installerad | Apple/Google |
| Push-notifikation skickad | Apple/Google |
| Anslutning till relay | ISP, relay-operatör |
| Kommunikationsgraf | Relay-operatör |
| IP-adress | Relay, ISP |
| Ungefärlig tid | Relay |

### Vad vi KAN minimera

| Data | Strategi |
|------|----------|
| Telefonnummer | **Använd inte** som identitet |
| E-post | **Använd inte** som identitet |
| Meddelandeinnehåll | E2EE |
| Avsändare (mot relay) | Sealed sender (Phase 7) |
| Payload-storlek | Padding |
| Kontaktlista | Lokal only |
| Analytics | **Ingen** |
| Crash data | Opt-in, anonymiserad |
| Persistent IP logs | **Inga** |

### Plattformsbegränsningar

| Begränsning | Konsekvens |
|-------------|------------|
| APNs/FCM krävs | Apple/Google vet att enheten använder appen |
| App Store review | Metadata till Apple (app-kategori, etc.) |
| iOS background | Kan inte hålla persistent P2P-lyssnare |
| Android Doze | FCM high priority behövs |
| VoIP | IP-läckor via ICE-kandidater |
| Device identifiers | Använd egna opaque IDs; aldrig IDFA/GAID |

**Korrekt term: pseudonymitet med stark E2EE och metadata-minimering.**

---

## 23. Offline- och synkroniseringsstrategi

### Lokal-first principer

1. **Skriv alltid lokalt först** — UI uppdateras omedelbart
2. **Synka i bakgrund** — WorkManager (Android), BGTask (iOS)
3. **Idempotent operations** — samma meddelande kan skickas flera gånger
4. **Optimistic UI** — visa meddelande som skickat; markera failed vid error

### Enhetsbyte och återställning (MVP)

| Scenario | MVP-beteende |
|----------|--------------|
| Ny telefon, ingen backup | Ny identitet; förlorar historik |
| Ny telefon, med backup | Importera krypterad backup |
| Förlorad telefon | Historik borta (forward secrecy); kontakter kan re-add |
| App reinstall | Ny enhetsregistrering; samma identitet om backup finns |

### Multi-device (Phase 4)

- Varje enhet har eget device key pair
- Identitetsnyckel delas via krypterad provisioning
- Meddelanden fan-out till alla enheter
- Read/delete synkas via sync messages

---

## 25. Enhetsbytes- och återställningsstrategi

Se ovan. ADR-007 (post-godkännande) dokumenterar exakt backup-format.

**Kritiskt beslut (kräver godkännande):** Accepterar vi att MVP **inte har backup** och användare **förlorar all data** vid enhetsförlust?

Rekommendation: Inkludera **krypterad export/import** i Phase 4b (snabbt efter MVP messaging).
