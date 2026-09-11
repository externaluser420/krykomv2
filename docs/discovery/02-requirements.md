# 3.2 Frågor, antaganden och beslut

## Frågor som måste besvaras

### Produkt och identitet

| ID | Fråga | Påverkan |
|----|-------|----------|
| Q-01 | Ska identitet vara **strikt pseudonym** (genererad nyckel/ID) eller **valfri** koppling till telefon/e-post? | Upptäckt, recovery, metadata |
| Q-02 | Ska användare kunna **byta användarnamn/display name** fritt? | Serverdata, impersonation |
| Q-03 | Ska appen stödja **organisationskonton** eller endast privat? | Federation, hosting |
| Q-04 | Vilken **jurisdiktion** (EU, US, global)? | GDPR, lagring, kryptoexport |
| Q-05 | Ska appen vara **open source** (full, eller bara klient)? | Trust, granskning, konkurrens |

### Recovery och multi-device

| ID | Fråga | Påverkan |
|----|-------|----------|
| Q-06 | Vad händer vid **förlorad telefon** utan backup — accepterar vi permanent identitetsförlust? | UX vs säkerhet |
| Q-07 | Ska **krypterad backup** finnas (lokal fil, iCloud/Google Drive, användarvald)? | Nyckelhantering |
| Q-08 | Ska **multi-device** ingå i MVP eller Phase 4? | Komplexitet, timeline |
| Q-09 | Recovery via **24-word seed phrase** (som krypto-plånböcker)? | UX, säkerhet |

### Abuse och drift

| ID | Fråga | Påverkan |
|----|-------|----------|
| Q-10 | Hur aggressiv **rate limiting** vs integritet? | IP-loggning |
| Q-11 | Ska **CAPTCHA / proof-of-work** användas vid registrering? | Bot-skydd |
| Q-12 | Ska relay vara **single operator** eller **federation-ready** från start? | Arkitektur |
| Q-13 | Budget för **TURN-trafik** (samtal kan bli dyrt)? | Phase 6 |

### Teknik

| ID | Fråga | Påverkan |
|----|-------|----------|
| Q-14 | **KMP + native UI** vs **full dual-native** (separata codebases)? | Team, hastighet |
| Q-15 | Relay-språk: **Rust** vs **Go**? | Prestanda, säkerhet, hiring |
| Q-16 | Ska vi använda **libsignal direkt** eller wrapper? | Underhåll |

## Tillfälliga antaganden (tills besvarat)

| ID | Antagande |
|----|-----------|
| A-01 | Pseudonym nyckelbaserad identitet som default; ingen telefonnummer-krav |
| A-02 | Single relay-operatör i MVP; federation designas in men implementeras senare |
| A-03 | EU-baserad infrastruktur (GDPR-first) |
| A-04 | Full open source (klient + relay) för transparens |
| A-05 | Recovery via valfri krypterad backup (användarinitierad); ingen server-side backup |
| A-06 | Multi-device i Phase 4, inte MVP |
| A-07 | Ljudsamtal i Phase 6 |
| A-08 | Svenska + engelska i UI; fler språk senare |
| A-09 | Min ålder 13+ (COPPA/GDPR barn-data undviks i MVP) |
| A-10 | libsignal som kryptografisk grund |

## Beslut som kräver godkännande

| ID | Beslut | Rekommendation |
|----|--------|----------------|
| D-01 | Arkitektur: hybrid lokal-first + stateless relay | **Godkänn** |
| D-02 | Ingen traditionell central DB | **Godkänn** |
| D-03 | Signal Protocol (libsignal) | **Godkänn** |
| D-04 | MVP utan samtal | **Godkänn** |
| D-05 | MVP en enhet per identitet | **Godkänn** |
| D-06 | KMP shared core + native UI | **Godkänn** (alternativ: dual-native) |
| D-07 | Projektnamn | **Behöver input** |
| D-08 | Identitetsmodell (Q-01) | **Behöver input** |
| D-09 | Open source-licens (AGPL vs MIT) | **Behöver input** |

## Risker med att gå vidare utan mer information

| Risk | Konsekvens |
|------|------------|
| Fel identitetsmodell | Ombyggnad av registrering, discovery, recovery |
| Oklar juridik | Fel region för hosting, compliance-problem |
| Oklar OSS-strategi | Fel licensval, svårt att ändra senare |
| Recovery oklar | Användare förlorar data eller vi bygger osäker backup |

## Krav påverkade av centraliserad vs decentraliserad arkitektur

| Krav | Central DB | Stateless relay | Ren P2P | Federerad |
|------|------------|-----------------|---------|-----------|
| Offline-leverans | Enkelt | Enkelt | Svårt/omöjligt | Medel |
| Push | Enkelt | Enkelt | Svårt | Medel |
| Metadata-minimering | Svårare | Bättre | Bäst (online) | Varierar |
| Abuse prevention | Enkelt | Medel | Svårt | Medel |
| Skalbarhet | Välkänd | Välkänd | Dålig på mobil | Medel |
| Multi-device | Enkelt | Medel (Signal) | Svårt | Medel |
| Tillförlitlighet | Hög | Hög | Låg på mobil | Varierar |
| Driftkostnad | Medel-hög | Låg-medel | Låg (men dålig UX) | Hög |
| App Store-kompatibilitet | Ja | Ja | Problem | Ja |
