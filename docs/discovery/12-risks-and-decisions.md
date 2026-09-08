# 12. Riskregister, öppna frågor och beslut

## Riskregister

| ID | Risk | Sannolikhet | Impact | Mitigation | Residual | Owner |
|----|------|-------------|--------|------------|----------|-------|
| R-01 | Metadata-läckage via relay | Hög | Hög | Sealed sender, padding, minimal logs | Medel-Hög | Architecture |
| R-02 | libsignal AGPL-licens konflikt | Medel | Medel | Legal review; öppen källkod relay | Låg | Legal |
| R-03 | Spam utan telefonnummer-verifiering | Hög | Medel | Rate limits, PoW, block | Medel | Backend |
| R-04 | Användare förlorar data (ingen backup) | Hög | Hög | Krypterad backup Phase 4b; tydlig UX | Medel | Product |
| R-05 | iOS bakgrund begränsar reliability | Medel | Medel | Push-first; BGTask supplementary | Låg | Mobile |
| R-06 | TURN-kostnad vid samtal | Medel | Medel | Budget; TURN-only optional | Medel | Infra |
| R-07 | App Store rejection (crypto app) | Låg | Hög | Export compliance; privacy manifest | Låg | Mobile |
| R-08 | KMP mognad/iOS integration | Medel | Medel | Native UI; gradvis adoption | Låg | Mobile |
| R-09 | Juridisk compliance (CSAM, GDPR) | Medel | Hög | Legal counsel; report mechanism | Medel | Legal |
| R-10 | Teamstorlek vs dual-platform | Medel | Medel | KMP shared core; phased delivery | Medel | PM |
| R-11 | Signal Protocol implementation bugs | Låg | Kritisk | libsignal (ej egen); test vectors | Låg | Security |
| R-12 | Relay Sybil/registrering abuse | Hög | Medel | Rate limit; PoW | Medel | Backend |
| R-13 | Push metadata (Apple/Google) | Hög | Medel | Data-only push; transparent policy | Medel | Privacy |
| R-14 | IP-läckor vid samtal | Hög | Hög | TURN-only mode; user education | Medel | Calling |
| R-15 | Dependency supply-chain | Medel | Hög | Lockfiles, scanning, SBOM | Medel | DevSecOps |

---

## Osäkerheter (behöver verifieras)

| ID | Osäkerhet | Verifiering |
|----|-----------|-------------|
| U-01 | libsignal Swift/KMP interoperability | Spike i Phase 3 |
| U-02 | SQLCipher performance på stora DBs | Benchmark |
| U-03 | Sealed sender implementation complexity | Signal docs review |
| U-04 | EU hosting provider val (Hetzner vs OVH) | Cost/security review |
| U-05 | App Store krav för crypto-export | Legal + Apple docs |
| U-06 | Redis durability vs message loss tradeoff | Architecture decision |
| U-07 | Max queue TTL acceptable to users | Product research |

---

## Beslut som kräver godkännande

### Kritiska (blockerar implementation)

| ID | Beslut | Rekommendation | Status |
|----|--------|----------------|--------|
| D-01 | Hybrid lokal-first + stateless relay | **Godkänn** | ⏳ Väntar |
| D-02 | Signal Protocol (libsignal) | **Godkänn** | ⏳ Väntar |
| D-03 | Pseudonym identitet (ej telefonnummer) | **Godkänn** | ⏳ Väntar |
| D-04 | MVP utan samtal | **Godkänn** | ⏳ Väntar |
| D-05 | MVP single-device | **Godkänn** | ⏳ Väntar |
| D-06 | KMP + native UI | **Godkänn** | ⏳ Väntar |
| D-07 | Rust relay server | **Godkänn** | ⏳ Väntar |

### Viktiga (behöver input)

| ID | Beslut | Alternativ | Status |
|----|--------|------------|--------|
| D-08 | Projektnamn | Veil / annat | ⏳ Väntar |
| D-09 | Open source-licens | AGPL-3.0 / MIT (client) + AGPL (server) | ⏳ Väntar |
| D-10 | Recovery-modell | Ingen / krypterad backup / seed phrase | ⏳ Väntar |
| D-11 | Hosting region | EU-only / multi-region | ⏳ Väntar |
| D-12 | Proof-of-work vid registrering | Ja / Nej | ⏳ Väntar |
| D-13 | Läskvitton default | Av / På | ⏳ Väntar |

---

## Funktionella krav (sammanfattning)

| ID | Krav |
|----|------|
| FR-01 | Användare ska kunna skapa pseudonym identitet |
| FR-02 | Användare ska kunna skicka E2EE textmeddelanden 1:1 |
| FR-03 | Meddelanden ska levereras till offline mottagare |
| FR-04 | Användare ska kunna adda kontakter via QR/ID |
| FR-05 | Användare ska kunna verifiera kontakters nycklar |
| FR-06 | Användare ska kunna blockera kontakter |
| FR-07 | Användare ska få push-notifikation vid nytt meddelande |
| FR-08 | Användare ska kunna se meddelandehistorik offline |
| FR-09 | Användare ska kunna låsa app med PIN/biometri |
| FR-10 | Användare ska kunna radera sitt konto |

## Icke-funktionella krav

| ID | Krav | Mått |
|----|------|------|
| NFR-01 | Meddelande-leveranslatens | < 2s p95 (online) |
| NFR-02 | App start | < 1s cold start |
| NFR-03 | Tillgänglighet relay | 99.9% |
| NFR-04 | Offline support | Full read; write queued |
| NFR-05 | Accessibility | WCAG 2.1 AA (where applicable) |
| NFR-06 | Supported platforms | iOS 16+, Android 8+ |
| NFR-07 | Message queue retention | Max 30 dagar |
| NFR-08 | Local DB encryption | AES-256 (SQLCipher) |

## Security requirements

| ID | Krav |
|----|------|
| SR-01 | E2EE med Signal Protocol för all meddelandeinnehåll |
| SR-02 | Forward secrecy via Double Ratchet |
| SR-03 | Privata nycklar i Secure Enclave/Keystore |
| SR-04 | TLS 1.3 + certificate pinning |
| SR-05 | Ingen plaintext på server |
| SR-06 | Message authentication (MAC) |
| SR-07 | Replay protection |
| SR-08 | Secure deletion av relay-kö efter ACK |
| SR-09 | Dependency scanning i CI |
| SR-10 | Secret scanning i CI |
| SR-11 | Signed releases |
| SR-12 | Key verification mechanism |

## Privacy requirements

| ID | Krav |
|----|------|
| PR-01 | Ingen telefonnummer/e-post som default identitet |
| PR-02 | Ingen tredjeparts-analytics |
| PR-03 | Ingen server-side meddelandeinbox |
| PR-04 | Minimal serverloggning (inga PII/IP) |
| PR-05 | Kontaktlista lagras endast lokalt |
| PR-06 | Push-notifikationer utan avsändare/innehåll |
| PR-07 | Privacy by design dokumenterad |
| PR-08 | GDPR compliance (EU hosting, data minimization) |
| PR-09 | Användare informeras om kvarstående metadata |
| PR-10 | Opt-in crash reporting |

---

## Godkännande-checklista

Innan implementation (Phase 2+) godkänns:

- [ ] D-01 Hybrid architecture
- [ ] D-02 Signal Protocol
- [ ] D-03 Pseudonym identity
- [ ] D-04 MVP scope (no calls)
- [ ] D-05 Single device MVP
- [ ] D-06 Tech stack (KMP + native)
- [ ] D-07 Rust relay
- [ ] D-08 Project name
- [ ] D-09 License
- [ ] D-10 Recovery model
- [ ] D-11 Hosting region

**Svara med godkännande (eller ändringar) på besluten ovan för att starta Phase 2.**
