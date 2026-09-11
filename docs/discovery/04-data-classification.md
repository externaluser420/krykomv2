# 5. Dataklassificering — Data Inventory

## Klassificeringsnyckel

- **E2EE:** Kan krypteras end-to-end (server ser ej plaintext)
- **Lokal only:** Kan lagras enbart på enheten
- **Server:** Kräver server-side hantering
- **Metadata:** Avslöjar information även utan innehåll

---

## Fullständig inventory

### Meddelandeinnehåll

| Attribut | Värde |
|----------|-------|
| **Skapas** | Avsändarens enhet |
| **Lagras** | Lokal DB (plaintext efter dekryptering); relay (ciphertext, TTL) |
| **Retention** | Lokalt: tills användaren raderar; Relay: max 30 dagar eller tills ACK |
| **Kan läsa** | Avsändare, mottagare; **inte** relay (E2EE) |
| **E2EE** | Ja |
| **Lokal only** | Ja (efter mottagning) |
| **Server** | Tillfällig ciphertext-kö |
| **Metadata** | Storlek, timing |
| **Vid förlust** | Borta permanent om ej levererat/backup |
| **Vid manipulation** | MAC/autentisering detekterar; meddelande avvisas |

### Bilagor

| Attribut | Värde |
|----------|-------|
| **Skapas** | Avsändarens enhet |
| **Lagras** | Lokal DB + filsystem (krypterat); relay (krypterat blob, TTL) |
| **Retention** | Som meddelanden |
| **Kan läsa** | Parter i chatten |
| **E2EE** | Ja (som del av meddelande-payload) |
| **Lokal only** | Ja (efter nedladdning) |
| **Server** | Tillfällig blob-lagring |
| **Metadata** | Storlek, MIME (om ej padding) |
| **Vid förlust** | Som meddelanden |
| **Vid manipulation** | Hash/MAC i krypterat meddelande |

### Kontaktlistor

| Attribut | Värde |
|----------|-------|
| **Skapas** | Användarens enhet |
| **Lagras** | **Endast lokalt** |
| **Retention** | Tills användaren raderar |
| **Kan läsa** | Endast användaren |
| **E2EE** | N/A (lokal kryptering at rest) |
| **Lokal only** | **Ja** |
| **Server** | **Nej** |
| **Metadata** | Relay ser "A skickar till B" oavsett |
| **Vid förlust** | Återställs via backup eller manuell re-add |
| **Vid manipulation** | Lokal integritet via DB-kryptering |

### Användaridentitet

| Attribut | Värde |
|----------|-------|
| **Skapas** | Enheten vid onboarding |
| **Lagras** | Lokalt (nycklar); relay (publik nyckel + opaque ID) |
| **Retention** | Permanent (lokal); tills avregistrering (server) |
| **Kan läsa** | Användaren; relay ser publik identitet |
| **E2EE** | Identitetsnyckel är publik/privat-par |
| **Lokal only** | Privat nyckel ja; publik nej |
| **Server** | Publik nyckel + registreringsmetadata |
| **Metadata** | Registreringstid, IP vid registrering |
| **Vid förlust** | Identitet förlorad utan backup |
| **Vid manipulation** | Signaturer skyddar; server kan ej förfalska privata nycklar |

### Enhetsidentitet

| Attribut | Värde |
|----------|-------|
| **Skapas** | Enheten |
| **Lagras** | Lokalt + relay (device ID, pubkey, push hash) |
| **Retention** | Tills enhet avregistreras |
| **Kan läsa** | Användaren; relay (device metadata) |
| **E2EE** | Device keys i Signal Protocol |
| **Lokal only** | Privata enhetsnycklar ja |
| **Server** | Device registration record |
| **Metadata** | Enhetstyp, registreringstid |
| **Vid förlust** | Enhet kan avregistreras remote (multi-device, Phase 4) |
| **Vid manipulation** | Signaturbaserad registrering |

### Publika nycklar

| Attribut | Värde |
|----------|-------|
| **Skapas** | Enheten |
| **Lagras** | Relay (key directory); lokal cache |
| **Retention** | Tills rotation |
| **Kan läsa** | Alla (publika per definition) |
| **E2EE** | N/A |
| **Lokal only** | Cache ja; authoritative på relay |
| **Server** | **Ja** — key directory |
| **Metadata** | Koppling identitet↔nyckel |
| **Vid förlust** | Kan ej dekryptera nya meddelanden till den nyckeln |
| **Vid manipulation** | Key verification (safety numbers) detekterar |

### Privata nycklar

| Attribut | Värde |
|----------|-------|
| **Skapas** | Enheten (Secure Enclave/Keystore) |
| **Lagras** | **Endast lokalt** i hårdvarusäker lagring |
| **Retention** | Tills enhet wipe |
| **Kan läsa** | Endast enheten (OS-skyddad) |
| **E2EE** | Grund för all E2EE |
| **Lokal only** | **Ja — MÅSTE** |
| **Server** | **Aldrig** |
| **Metadata** | Ingen |
| **Vid förlust** | Identitet/data oåterkallelig |
| **Vid manipulation** | Hårdvaru-skydd |

### Sessionsnycklar

| Attribut | Värde |
|----------|-------|
| **Skapas** | Double Ratchet under konversation |
| **Lagras** | Endast lokalt (ratchet state) |
| **Retention** | Under session + begränsad historik för out-of-order |
| **Kan läsa** | Endast lokala parter |
| **E2EE** | Ja |
| **Lokal only** | **Ja** |
| **Server** | **Nej** |
| **Metadata** | Ingen direkt |
| **Vid förlust** | Kan ej dekryptera gamla meddelanden (by design, forward secrecy) |
| **Vid manipulation** | Ratchet state corruption = session reset |

### Push-token

| Attribut | Värde |
|----------|-------|
| **Skapas** | APNs/FCM |
| **Lagras** | Lokalt; relay (hash eller krypterat) |
| **Retention** | Tills app avinstalleras/enhet byts |
| **Kan läsa** | Apple/Google; relay (hash); **inte** plaintext till tredje part |
| **E2EE** | Kan krypteras för relay |
| **Lokal only** | Nej — måste delas med relay för push |
| **Server** | Ja (device registration) |
| **Metadata** | Apple/Google vet att enhet får push från vår app |
| **Vid förlust** | Push slutar fungera; förnyas automatiskt |
| **Vid manipulation** | Push till fel enhet (risk vid serverkompromiss) |

### IP-adresser

| Attribut | Värde |
|----------|-------|
| **Skapas** | Nätverksanslutning |
| **Lagras** | Relay (kort TTL i minnet); **inte** persistent om möjligt |
| **Retention** | **Minimal** — undvik loggning |
| **Kan läsa** | Relay-operatör, ISP, angripare (MITM) |
| **E2EE** | Nej |
| **Lokal only** | N/A |
| **Server** | Ser vid anslutning |
| **Metadata** | **Ja — betydande integritetsrisk** |
| **Vid förlust** | N/A |
| **Vid manipulation** | VPN/Tor kan dölja (med tradeoffs) |

### Tidsstämplar

| Attribut | Värde |
|----------|-------|
| **Skapas** | Avsändare (lokal) + relay (server time vid mottagning) |
| **Lagras** | Lokalt; relay (delivery timestamp) |
| **Retention** | Lokalt permanent; server till leverans |
| **Kan läsa** | Parter; relay |
| **E2EE** | Klientsidig timestamp kan E2EE:as i payload |
| **Lokal only** | Klientsida ja |
| **Server** | Server-side delivery time |
| **Metadata** | **Ja** — trafikanalys |
| **Vid förlust** | Kosmetisk |
| **Vid manipulation** | Lokal visning påverkas |

### Leveransstatus

| Attribut | Värde |
|----------|-------|
| **Skapas** | Relay (delivered) + mottagare (ACK) |
| **Lagras** | Lokalt; kort på relay |
| **Retention** | Lokalt; relay: tills ACK |
| **Kan läsa** | Avsändare (via relay notification) |
| **E2EE** | Status kan E2EE:as (Signal-style delivery receipts) |
| **Lokal only** | Delvis |
| **Server** | Delivery metadata |
| **Metadata** | Ja |
| **Vid förlust** | Status okänd |
| **Vid manipulation** | Fake delivery möjlig utan autentisering |

### Lässtatus

| Attribut | Värde |
|----------|-------|
| **Skapas** | Mottagare |
| **Lagras** | **Endast lokalt** (rekommendation) eller E2EE receipt |
| **Retention** | Lokalt |
| **Kan läsa** | Avsändare (om delat) |
| **E2EE** | Ja (read receipts som E2EE-meddelanden) |
| **Lokal only** | **Ja** (default av) |
| **Server** | **Nej** (om E2EE receipt) |
| **Metadata** | Om aktiverat: avsändare vet att mottagare läst |
| **Vid förlust** | Kosmetisk |
| **Vid manipulation** | Fake read receipt |

### Samtalsmetadata

| Attribut | Värde |
|----------|-------|
| **Skapas** | Signaling server + enheter |
| **Lagras** | Signaling (kort); lokalt (samtalslogg — valfritt av) |
| **Retention** | Minimal på server |
| **Kan läsa** | Relay; parter |
| **E2EE** | Signaling kan delvis E2EE:as |
| **Lokal only** | Samtalslogg valfritt lokal |
| **Server** | Signaling |
| **Metadata** | **Hög** — vem ringde vem, duration, IP |
| **Vid förlust** | Samtal kan ej etableras |
| **Vid manipulation** | Call hijacking (mitigated by DTLS-SRTP) |

### Signaling-data

| Attribut | Värde |
|----------|-------|
| **Skapas** | WebRTC ICE/SDP |
| **Lagras** | Signaling server (transient) |
| **Retention** | Sekunder–minuter |
| **Kan läsa** | Signaling server |
| **E2EE** | Delvis (insertable streams / SDES deprecated) |
| **Lokal only** | Nej |
| **Server** | **Ja** |
| **Metadata** | IP-kandidater (**läcka**) |
| **Vid förlust** | Samtal misslyckas |
| **Vid manipulation** | MITM (mitigated by DTLS fingerprint verification) |

### Blocklistor

| Attribut | Värde |
|----------|-------|
| **Skapas** | Användaren |
| **Lagras** | **Endast lokalt** |
| **Retention** | Tills avblockering |
| **Kan läsa** | Endast användaren |
| **E2EE** | N/A |
| **Lokal only** | **Ja** |
| **Server** | **Nej** |
| **Metadata** | Ingen (men blockerade kan fortfarande skicka till relay) |
| **Vid förlust** | Blocklist försvinner |
| **Vid manipulation** | Lokal |

### Abuse-data

| Attribut | Värde |
|----------|-------|
| **Skapas** | Relay (rate limits), användare (rapporter) |
| **Lagras** | Relay (hash/IP counters); **minimera** |
| **Retention** | Kort (timmar–dagar) |
| **Kan läsa** | Operatör |
| **E2EE** | Rapporter kan E2EE:as till moderering (Phase 8) |
| **Lokal only** | Delvis |
| **Server** | Rate limit state |
| **Metadata** | IP, hash av identitet |
| **Vid förlust** | Rate limit reset |
| **Vid manipulation** | DoS av rate limiter |

### Loggar (server)

| Attribut | Värde |
|----------|-------|
| **Skapas** | Relay |
| **Lagras** | **Minimal** — aggregat metrics, ej per-user |
| **Retention** | **Minimal** |
| **Kan läsa** | Operatör |
| **E2EE** | N/A |
| **Lokal only** | N/A |
| **Server** | Ja (operational) |
| **Metadata** | Risk om för detaljerade |
| **Rekommendation** | **Structured metrics utan PII; inga access logs med IP** |

### Crash-data

| Attribut | Värde |
|----------|-------|
| **Skapas** | Klient (om aktiverat) |
| **Lagras** | **Default: endast lokalt**; opt-in till Sentry/Crashlytics |
| **Retention** | 30 dagar om opt-in |
| **Kan läsa** | Utvecklare (om opt-in) |
| **Rekommendation** | **Opt-in, anonymiserad, ingen PII** |

### Analytics-data

| Attribut | Värde |
|----------|-------|
| **Skapas** | — |
| **Lagras** | **INGEN** i MVP |
| **Rekommendation** | **Ingen tredjeparts-analytics**; privacy-first |

### Backup-data

| Attribut | Värde |
|----------|-------|
| **Skapas** | Användaren (valfritt) |
| **Lagras** | Användarens valda plats (iCloud Drive, fil, etc.) |
| **Retention** | Användarstyrt |
| **Kan läsa** | Den med backup-lösenordet |
| **E2EE** | **Ja — backup MÅSTE vara krypterad** |
| **Lokal only** | Användarens molntjänst |
| **Server** | **Aldrig** |
| **Metadata** | Molntjänst ser att fil existerar |
| **Vid förlust** | Data oåterkallelig |
| **Vid manipulation** | Krypterad integritetsskydd |

---

## Metadata som ALLTID kvarstår (realistisk bild)

Även med bästa möjliga design kan följande observeras:

1. **Relay-operatören:** kommunikationsgraf (A↔B), meddelandefrekvens, payload-storlek, tidsstämplar
2. **Apple/Google (push):** att en enhet kör appen och får notiser
3. **ISP/nätverksoperatör:** att enheten ansluter till relay-IP
4. **Vid samtal:** IP-adresser (särskilt via TURN), samtalsduration
5. **App Store/Google Play:** att appen är installerad (ej synligt för oss men för plattformen)

**Detta är pseudonymitet och integritetsförbättring — inte anonymitet.**
