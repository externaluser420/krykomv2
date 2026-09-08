# 3.1 Produktförståelse

## Problemet appen löser

Användare behöver en kommunikationskanal som:

- Skyddar **innehåll** (meddelanden, bilagor, samtal) mot avlyssning
- Minimerar **metadata** som tredje part kan samla in
- Fungerar **tillförlitligt på mobil** (offline, push, bakgrund)
- Inte kräver att användaren offrar telefonnummer eller e-post som primär identitet
- Är **modern och enkel** — inte bara för tekniker

Befintliga alternativ (WhatsApp, Telegram, iMessage) kompromissar integritet på olika sätt. Signal är närmast men har designval (telefonnummer, central relay) som denna app kan förbättra eller differentiera på.

## Målgrupp

| Segment | Behov |
|---------|-------|
| **Integritetsmedvetna privatpersoner** | E2EE, minimal metadata, enkel UX |
| **Journalister, aktivister, advokater** | Forward secrecy, nyckelverifiering, ingen central profil |
| **Tekniskt medvetna early adopters** | Transparens, open source, verifierbar säkerhet |
| **Organisationer (senare)** | Säker intern kommunikation, eventuellt self-hosted relay |

**Inte primär målgrupp i MVP:** Massmarknad som kräver telefonnummer-upptäckt, social graph, bots, channels.

## Kärnfunktioner (MVP+)

| Funktion | Prioritet | MVP |
|----------|-----------|-----|
| 1:1 textmeddelanden, E2EE | Kritisk | Ja |
| Bilagor (bilder, filer) | Hög | Nej (Phase 4b) |
| Offline-leverans | Kritisk | Ja |
| Push-notifikationer | Kritisk | Ja |
| Pseudonym identitet | Kritisk | Ja |
| Nyckelverifiering | Hög | Ja |
| Lokal krypterad lagring | Kritisk | Ja |
| Meddelandestatus (skickat/levererat/läst) | Medel | Levererat ja, läst valfritt |
| Gruppchatt | Medel | Nej (Phase 5) |
| Multi-device | Hög | Nej (Phase 4) |
| Ljudsamtal | Hög | Nej (Phase 6) |
| Videosamtal | Låg | Nej (Phase 7+) |
| Backup/återställning | Hög | Begränsad i MVP |
| Blockera/rapportera | Medel | Block ja, rapportera begränsat |

## Funktioner som kan vänta

- Gruppchatt och gruppsamtal
- Videosamtal
- Stories/status
- Bots och integrationer
- Federation mellan relay-operatörer
- Desktop-klienter
- In-app browser / link previews (metadata-risk)
- Kontaktsynk från telefonbok
- Telefonnummer-baserad upptäckt

## Mest säkerhetskritiska delar

1. **Nyckelhantering** — identitetsnycklar, sessionsnycklar, lagring i Secure Enclave/Keystore
2. **E2EE-protokoll** — Signal Protocol-implementering, aldrig egen krypto
3. **Relay-säkerhet** — vad servern kan läsa, TTL, kryptering at rest för köer
4. **Push-pipeline** — metadata i APNs/FCM
5. **Enhetsbyte/recovery** — risk för central svag punkt
6. **Samtal (senare)** — IP-läckor, TURN-loggar, signaling-metadata
7. **Supply chain** — dependencies, CI/CD, signering

## Mest tekniskt komplexa delar

1. **Multi-device sync** (Signal Sesame / PQXDH)
2. **Offline-first med konflikthantering**
3. **WebRTC + NAT traversal + bakgrund på iOS**
4. **Metadata-minimering utan att offra tillförlitlighet**
5. **Abuse prevention utan central användardatabas**
6. **Recovery utan att skapa krypteringsbakdörr**

## Delar som kräver central infrastruktur

| Del | Varför |
|-----|--------|
| Offline meddelandeleverans | Mottagare offline → någon måste buffra krypterat payload |
| Push-notifikationer | APNs/FCM kräver server-initierad push |
| Publik nyckelkatalog | Upptäcka mottagares enhetsnycklar utan P2P |
| Signaling (samtal) | ICE-kandidater, session setup |
| TURN (samtal) | ~80% samtal behöver relay p.g.a. NAT |
| Rate limiting / DDoS-skydd | Edge-infra oavsett arkitektur |
| (Valfritt) TURN för meddelanden | Ej nödvändigt om relay används |

## Delar som kan fungera lokalt eller P2P

| Del | Lokalt | P2P | Kommentar |
|-----|--------|-----|-----------|
| Meddelandeinnehåll (plaintext) | Ja | — | Endast på enheten efter dekryptering |
| Meddelandehistorik | Ja | — | Lokal krypterad DB |
| Privata nycklar | Ja | — | Secure Enclave / Keystore |
| Kontaktlista | Ja | — | Användaren väljer kontakter manuellt |
| Nyckelverifiering | Ja (QR) | Ja (QR/NFC) | Offline QR-scanning |
| Direktmeddelande (online) | — | Teoretiskt | Opålitligt på mobil; relay rekommenderas |
| Samtal (online) | — | Delvis | WebRTC P2P när NAT tillåter; annars TURN |

## Viktig distinktion

**"Utan central databas"** betyder i praktiken:

- **INTE:** ingen datalagring någonstans
- **UTAN:** ingen persistent central databas med användarprofiler, klartext, meddelandeinbox eller social graph

**Lokal databas på telefonen är fortfarande en databas** — SQLCipher-krypterad SQLite — och är arkitekturens primära lagringslager för meddelanden och kontakter.
