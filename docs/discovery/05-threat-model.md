# 14. Threat Model

## Metodologi

STRIDE-inspirerad analys med fokus på messaging-app hot. Varje hot bedöms enligt:

**Threat → Impact → Likelihood → Mitigation → Residual risk**

Skala: Impact/Likelihood = Low / Medium / High  
Residual risk = Low / Medium / High (efter mitigation)

---

## Systemdiagram (för hotanalys)

```
┌─────────────┐     TLS 1.3      ┌──────────────┐     APNs/FCM    ┌─────────────┐
│  Avsändare  │ ───────────────> │ Stateless    │ ─────────────> │  Mottagare  │
│  (iOS/And)  │ <─────────────── │ Relay        │               │  (iOS/And)  │
└─────────────┘                  └──────────────┘               └─────────────┘
       │                                │                              │
       │ Secure Enclave/Keystore        │ Ephemeral queue              │ Local encrypted DB
       │ Local encrypted DB             │ Key directory                │ Secure Enclave/Keystore
       └────────────────────────────────┴──────────────────────────────┘
```

**Trust boundaries:**
- TB1: Enhet (OS + app)
- TB2: Relay (operatör)
- TB3: Push-infrastruktur (Apple/Google)
- TB4: Nätverk (ISP)

---

## Hot #1: Komprometterad relay-server

| | |
|--|--|
| **Threat** | Angripare får full kontroll över relay |
| **Impact** | **High** — metadata-exposure, meddelandekö-läcka, DoS, fake push |
| **Likelihood** | Medium |
| **Mitigation** | E2EE (Signal Protocol); minimal queue TTL; sealed sender; TLS pinning; encrypted push tokens; forward secrecy; ACK-delete; infrastruktur-hardening; HSM för server keys |
| **Residual risk** | **Medium** — metadata fortfarande synlig |

---

## Hot #2: Skadlig serveradministratör

| | |
|--|--|
| **Threat** | Insider loggar metadata, korrelerar användare |
| **Impact** | **High** — integritetsbrott |
| **Likelihood** | Medium |
| **Mitigation** | Minimal logging policy; teknisk enforcement (ej access till ciphertext keys); audit logs för admin; open source server; sealed sender; dokumenterad privacy policy |
| **Residual risk** | **Medium** |

---

## Hot #3: Databasläcka (relay)

| | |
|--|--|
| **Threat** | Persistent DB med nycklar/köer läcker |
| **Impact** | **High** om plaintext; **Medium** om bara ciphertext |
| **Likelihood** | Medium |
| **Mitigation** | Ingen persistent meddelandeinbox; TTL queues; key directory endast publika nycklar; encryption at rest; minimal data |
| **Residual risk** | **Low-Medium** |

---

## Hot #4: Läckta serverköer

| | |
|--|--|
| **Threat** | Okrypterade eller kvarliggande meddelanden i kö |
| **Impact** | **High** |
| **Likelihood** | Low (med E2EE) |
| **Mitigation** | Signal Protocol; auto-delete efter ACK; max TTL; padding |
| **Residual risk** | **Low** |

---

## Hot #5: Stulen telefon

| | |
|--|--|
| **Threat** | Fysisk åtkomst till upplåst enhet |
| **Impact** | **High** — full läsning av meddelanden |
| **Likelihood** | Medium |
| **Mitigation** | App-lås (PIN/biometri); OS-kryptering; kort session timeout; Data Protection (iOS); encrypted DB; remote wipe (Phase 4, multi-device) |
| **Residual risk** | **Medium** (upplåst enhet) |

---

## Hot #6: Malware på användarens telefon

| | |
|--|--|
| **Threat** | Keylogger/skärmdump stjäl data |
| **Impact** | **High** |
| **Likelihood** | Low-Medium |
| **Mitigation** | Secure Enclave/Keystore (nycklar ej extraherbara); screenshot prevention (valfritt); jailbreak/root detection (inform, ej block); OS-säkerhet |
| **Residual risk** | **Medium** |

---

## Hot #7: Man-in-the-middle

| | |
|--|--|
| **Threat** | Nätverksangripare interceptar trafik |
| **Impact** | **High** utan E2EE; **Low** med E2EE |
| **Likelihood** | Medium |
| **Mitigation** | TLS 1.3; certificate pinning; Signal Protocol; key verification |
| **Residual risk** | **Low** |

---

## Hot #8: Account takeover

| | |
|--|--|
| **Threat** | Angripare tar över identitet |
| **Impact** | **High** |
| **Likelihood** | Low (ingen central login) |
| **Mitigation** | Ingen lösenord-on-server; identitet = nyckelpar; key verification; SIM swap irrelevant (ingen telefonnummer-bindning) |
| **Residual risk** | **Low** |

---

## Hot #9: SIM swap

| | |
|--|--|
| **Threat** | Telefonnummer kapas |
| **Impact** | **None** om ingen telefonnummer-identitet |
| **Likelihood** | N/A |
| **Mitigation** | Pseudonym identitet (default) |
| **Residual risk** | **Low** |

---

## Hot #10: Phishing

| | |
|--|--|
| **Threat** | Användare delar backup/seed eller godkänner falsk nyckel |
| **Impact** | **High** |
| **Likelihood** | Medium |
| **Mitigation** | UX: tydlig key verification; varningar vid nyckeländring; ingen seed i klartext i UI |
| **Residual risk** | **Medium** |

---

## Hot #11: Replay attacks

| | |
|--|--|
| **Threat** | Gammalt meddelande skickas igen |
| **Impact** | Medium |
| **Likelihood** | Medium |
| **Mitigation** | Signal Protocol (message counters, MAC); server-side dedup (message ID) |
| **Residual risk** | **Low** |

---

## Hot #12: Message injection

| | |
|--|--|
| **Threat** | Angripare injicerar meddelanden via relay |
| **Impact** | **High** |
| **Likelihood** | Medium |
| **Mitigation** | Autentiserade meddelanden (Signal MAC); sender certificate; rate limiting |
| **Residual risk** | **Low** |

---

## Hot #13: Spam

| | |
|--|--|
| **Threat** | Massutskick av meddelanden |
| **Impact** | Medium |
| **Likelihood** | **High** |
| **Mitigation** | Rate limiting per device/IP; proof-of-work (valfritt); blocklist (lokal); captcha vid registrering (tradeoff) |
| **Residual risk** | **Medium** |

---

## Hot #14: Abuse / harassment

| | |
|--|--|
| **Threat** | Målriktad trakasseri |
| **Impact** | Medium-High |
| **Likelihood** | Medium |
| **Mitigation** | Block; report (E2EE till moderering); rate limits |
| **Residual risk** | **Medium** |

---

## Hot #15: DDoS

| | |
|--|--|
| **Threat** | Överbelastning av relay |
| **Impact** | **High** — tjänsten nere |
| **Likelihood** | Medium |
| **Mitigation** | CDN/edge; rate limiting; anycast; autoscaling; WAF |
| **Residual risk** | **Medium** |

---

## Hot #16: Metadata analysis

| | |
|--|--|
| **Threat** | Relay/ISP korrelerar kommunikationsmönster |
| **Impact** | **High** (integritet) |
| **Likelihood** | **High** |
| **Mitigation** | Sealed sender; message padding; minimal logging; (lång sikt) mix networks / onion routing research |
| **Residual risk** | **High** — svår att eliminera helt |

---

## Hot #17: Traffic analysis

| | |
|--|--|
| **Threat** | Timing/storleksanalys avslöjar aktivitet |
| **Impact** | Medium-High |
| **Likelihood** | Medium |
| **Mitigation** | Padding; constant-rate dummy traffic (forskningsstadium); batch delivery |
| **Residual risk** | **Medium-High** |

---

## Hot #18: IP leakage (samtal)

| | |
|--|--|
| **Threat** | WebRTC avslöjar IP-adresser |
| **Impact** | **High** |
| **Likelihood** | **High** vid samtal |
| **Mitigation** | TURN-only mode (tradeoff: latency); VPN integration (Phase 7); inform users |
| **Residual risk** | **Medium-High** |

---

## Hot #19: Komprometterad push-infrastruktur

| | |
|--|--|
| **Threat** | Apple/Google eller MITM av push |
| **Impact** | Medium |
| **Likelihood** | Low |
| **Mitigation** | Data-only push (ingen avsändare/innehåll); encrypted payload; minimal metadata |
| **Residual risk** | **Medium** |

---

## Hot #20: Supply-chain attacks

| | |
|--|--|
| **Threat** | Skadlig dependency i build |
| **Impact** | **High** |
| **Likelihood** | Medium |
| **Mitigation** | Lockfiles; dependency scanning (Dependabot/Snyk); SBOM; reproducible builds; minimerade deps |
| **Residual risk** | **Medium** |

---

## Hot #21: Skadliga dependencies

| | |
|--|--|
| **Threat** | Sårbarhet i libsignal, WebRTC, etc. |
| **Impact** | **High** |
| **Likelihood** | Medium |
| **Mitigation** | Etablerade bibliotek; snabb patch-process; vulnerability scanning |
| **Residual risk** | **Medium** |

---

## Hot #22: Komprometterad utvecklardator

| | |
|--|--|
| **Threat** | Ins attackerad build pipeline lokalt |
| **Impact** | **High** |
| **Likelihood** | Low |
| **Mitigation** | Code review; signed commits; CI-only releases; 2FA |
| **Residual risk** | **Low-Medium** |

---

## Hot #23: Komprometterad CI/CD

| | |
|--|--|
| **Threat** | GitHub Actions secrets läcker; artifact tampering |
| **Impact** | **High** |
| **Likelihood** | Low-Medium |
| **Mitigation** | Minimal secrets; OIDC; signed builds; protected branches; SLSA |
| **Residual risk** | **Low-Medium** |

---

## Hot #24: Reverse engineering

| | |
|--|--|
| **Threat** | Angripare analyserar app-binary |
| **Impact** | Low-Medium (protokoll är öppet anyway) |
| **Likelihood** | **High** |
| **Mitigation** | Obfuskering (minimal nytta); open source (transparency); server-side rate limits |
| **Residual risk** | **Low** |

---

## Hot #25: Binary tampering

| | |
|--|--|
| **Threat** | Fake app distribueras |
| **Impact** | **High** |
| **Likelihood** | Low |
| **Mitigation** | App Store/Play only; code signing; certificate pinning |
| **Residual risk** | **Low** |

---

## Hot #26: Förlorad enhet

| | |
|--|--|
| **Threat** | Enhet borttappad, ej upplåst |
| **Impact** | Medium (OS-kryptering skyddar) |
| **Likelihood** | Medium |
| **Mitigation** | OS full-disk encryption; app lock; forward secrecy |
| **Residual risk** | **Low-Medium** |

---

## Hot #27: Byte av enhet

| | |
|--|--|
| **Threat** | Migrering misslyckas; data förloras |
| **Impact** | Medium |
| **Likelihood** | Medium |
| **Mitigation** | Krypterad backup; tydlig UX; multi-device (Phase 4) |
| **Residual risk** | **Medium** |

---

## Hot #28: Korrupt lokal lagring

| | |
|--|--|
| **Threat** | DB corruption |
| **Impact** | Medium |
| **Likelihood** | Low |
| **Mitigation** | WAL mode; backup; integrity checks |
| **Residual risk** | **Low** |

---

## Hot #29: Synkroniseringskonflikter

| | |
|--|--|
| **Threat** | Multi-device sync konflikt |
| **Impact** | Medium |
| **Likelihood** | Medium (Phase 4) |
| **Mitigation** | Append-only messages; deterministic IDs; Signal Sesame |
| **Residual risk** | **Low-Medium** |

---

## Hot #30: Falska/komprometterade noder (federation)

| | |
|--|--|
| **Threat** | Sybil-attacker i federerad modell |
| **Impact** | **High** |
| **Likelihood** | Medium (om federation) |
| **Mitigation** | Trust-on-first-use; key verification; reputation (Phase 8) |
| **Residual risk** | **Medium** |

---

## Hot #31: Brist på central abuse-kontroll

| | |
|--|--|
| **Threat** | Spam/CSAM/abuse svår att hantera |
| **Impact** | **High** (legal/reputation) |
| **Likelihood** | **High** |
| **Mitigation** | Rate limits; report mechanism; legal compliance process; hash-matching (NCMEC) utan att läsa E2EE-innehåll (client-side, Phase 8) |
| **Residual risk** | **Medium-High** |

---

## Prioriterade hot (topp 10 att adressera först)

1. Metadata analysis (#16)
2. Komprometterad relay (#1)
3. Message injection (#12)
4. Spam (#13)
5. Stulen telefon (#5)
6. Supply-chain (#20)
7. Replay attacks (#11)
8. Abuse utan central kontroll (#31)
9. Phishing/key verification (#10)
10. DDoS (#15)
