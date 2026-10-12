# Betrieb und Wiederanlauf

## Betriebsarten

`baseline` ruft kein Modell auf und ermöglicht reproduzierbare Abläufe. `ollama` nutzt den konfigurierten HTTP-Endpunkt; Fehler eskalieren den Fall. Ein Moduswechsel plant bestehende Vorschläge nicht rückwirkend neu.

Der lokale Dateispeicher erlaubt genau einen schreibenden Server. Mehrere Worker-Clients sind möglich, mehrere Server mit demselben Datenverzeichnis nicht. PostgreSQL muss vor Mehrprozessbetrieb seine Parallelitätstests bestehen; ein einzelnes gesperrtes Aggregat ist keine skalierbare Produktionsarchitektur.

| Steuerung | Eigenständiges Paket | Spring-Overlay |
|---|---|---|
| Port | `--port`, Standard 8095. | Bisheriger Host-Port, beobachtet 8081. |
| Planer | `--provider baseline` oder `ollama`. | `GOVERNANCE_PLANNER`. |
| Modell | `--model`. | `GOVERNANCE_OLLAMA_MODEL`. |
| Endpunkt | `--model-endpoint`. | `GOVERNANCE_OLLAMA_ENDPOINT`. |
| Identitäten | `.local/auth.json`. | `GOVERNANCE_AUTH_FILE`. |
| Zustand | `.local/state`. | `governance_state`. |
| Aktivierung | Bewusster lokaler Start. | `GOVERNANCE_ENABLED=false` als Standard. |
| Testdaten | Anlage ohne Zurücksetzen. | `GOVERNANCE_SEED_DEMO=false` als Standard. |
| Origin | Gleicher lokaler Port. | `GOVERNANCE_ALLOWED_ORIGIN`. |
| Ereignisse | Outbox bleibt lokal liegen. | `GOVERNANCE_EVENTS_TOPIC`, Standard `governance.events`. |

## Abbruch zwischen Commit und Bestätigung

Synthetischen Fall erstellen, untersuchen und freigeben. Dann:

```bash
python scripts/worker.py --once --crash-after-commit
```

Der Java-Worker endet absichtlich mit Code 73 nach dem Commit. Die Python-Hülle meldet den Fehlercode. Server ohne Löschen des Zustands und ohne Tokenrotation neu starten, anschließend:

```bash
python scripts/worker.py --once
```

Derselbe Beleg wird zurückgegeben und bestätigt. Kontrollieren Sie `EXECUTION_REPLAYED`, genau ein `EFFECT_COMMITTED` und den unveränderten Erstattungsbetrag. `python scripts/recovery_demo.py` automatisiert diesen Versuch in einem isolierten temporären Server und schreibt den Nachweis nach `build/demo/`.

Dies ist ein tatsächlicher Absturz des eigenständigen Worker-Prozesses, kein Nachweis eines bereits durchgeführten Camunda-Worker-Abbruchs.

## Fehlerdiagnose

| Symptom | Sichere Reaktion |
|---|---|
| Anmeldung nach einem Tag abgelehnt. | Ablauf prüfen, bewusst rotieren und neu starten. |
| `PLANNING_IN_PROGRESS`. | Andere Planung besitzt eine 120-Sekunden-Lease; keine unkontrollierte Wiederholungsschleife. |
| `APPROVAL_EXPIRED`, `ORDER_CHANGED`, `POLICY_CHANGED`. | Hash nicht manipulieren; neuen Vorschlag und Freigabe einholen. Für beendete BPMN neuen Fall erstellen. |
| `BLOCKED`. | Stabile Regelgründe und Fakten prüfen, nicht nur Modelltext. |
| `ESCALATED`. | Unklarheit oder Providerfehler manuell klären; kein automatischer wirksamer Ersatz. |
| Integritätsfehler beim Start. | Dateien sichern und untersuchen; nicht löschen, um den Fehler zu umgehen. |
| Outbox in lokaler Demo offen. | Erwartet, da kein Broker-Relay aktiv ist. |
| Outbox im Spring-Stack offen. | Kafka, Topics, Berechtigungen, Camunda und Logs prüfen; Wiederholungen sind möglich. |
| Freigabe ohne Fortschritt. | Relay, Korrelation und Worker prüfen; nicht durch Standalone-Worker verdecken. |

Veraltete Freigaben werden als fehlgeschlagen markiert und nicht endlos erneut ausgeführt. Fall-, Ereignis- und Beleg-IDs dienen zur Untersuchung. Tokens und vollständige Modellprompts gehören nicht in Logs.

## Sicherung und Grenzen

Für eine lokale Sicherung den Server stoppen und das gesamte Datenverzeichnis geschützt kopieren. Wiederherstellung isoliert testen, Original behalten, passende Identitäten verwenden und Audit sowie Belegwiederholung prüfen. Die Existenz einer Sicherungsdatei belegt keine funktionierende Wiederherstellung.

Der Dateispeicher ist auf 64 MiB, der Kern auf 10.000 Fälle begrenzt. Dies sind Schutzgrenzen, keine Leistungszusagen. Audit und Outbox werden nicht automatisch gekürzt. PostgreSQL serialisiert das gesamte Aggregat und schreibt JSON neu; vor Normalisierung oder anderen Sperrstrategien sind Lastmessungen notwendig.

`GET /health` zeigt einen lebenden HTTP-Adapter, keine vollständige Datenbank-/Broker-/Modellbereitschaft. Warteschlangenalter, Lease-Alter, abgelaufene Freigaben, Outbox-Verzug, Wiederholungen und Modellfehler sollten später überwacht werden. Entsprechende Produktionsalarme werden hier nicht als implementiert dargestellt.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
