# Verifikationsmatrix und Reproduktion

## Hier ausgeführte Prüfungen

| Ebene | Nachweis | Aussage |
|---|---|---|
| Java-Kern | 106 benannte Tests ohne Fehler im aufgezeichneten Lauf. | Regeln, Autorisierung, Parallelität, Zustand, JSON und simuliertes Modellprotokoll. |
| HTTP/Prozesse | 29 Tests ohne Fehler. | Echter Java-Server, Anfragen, dauerhafter Neustart und separater Worker. |
| Browser-DOM/Darstellung | Neun bestandene Prüfungen im expliziten Harness. | Echtes JavaScript/CSS in Chromium, API-Aufrufe über Python-Brücke. |
| Repository | 23 Tests ohne Fehler. | Paket, Dokumentation, API-/BPMN-Struktur und Installer-Schutz. |
| Evaluation | 48 synthetische gelabelte Fälle. | Qualitätsexperiment, keine bestandene Sicherheitstestsuite. |

JSON, JUnit-XML und Logs liegen unter `evidence/tests/`. Die Umgebungsgrenzen und der gescheiterte native Navigationsversuch stehen unter `evidence/environment/`. Maßgeblich ist `evidence/verification.json`. Es wird kein nicht gemessener Zeilen- oder Branch-Coverage-Prozentsatz behauptet.

## Inhalt der Tests

Java prüft unter anderem Pflicht-/Zusatzfelder, strikte Vorschläge, Tools und Nachweise, Tokenablauf, Rollen, verborgene Fremdmandanten, Selbstfreigabe, Hash-Manipulation, veraltete Versionen, Ablauf, Providerfehler, Rollback, Dateiwiederanlauf, Schreibsperren und Korruption. 32 parallele Ausführungen müssen einen einzigen Beleg ergeben. 500 Erstattungsbeträge werden innerhalb eines benannten Parametertests geprüft und nicht als 500 unabhängige Tests gezählt.

HTTP-Tests verwenden reale Prozesse und prüfen Inhaltstyp, UTF-8, Bodygröße, Tokens/Rollen, zusätzliche Identitätsfelder, Hash, Origin, Query-Verbot, Fehler und Ratenlimit. Zwölf parallele HTTP-Wiederholungen erzeugen einen Effekt. Der Absturztest beendet einen echten Worker mit Code 73, beendet/startet den Server neu und prüft einen Beleg und eine Erstattung.

Ollama-Tests nutzen einen kontrollierten HTTP-Modellserver. Sie prüfen Tool-Schleifen, unzulässige Argumente, Ausgabeschema und Budgets. Das beweist weder die Qualität eines echten Modells noch Schutz vor jeder Prompt Injection.

## Browsergrenze ausdrücklich dokumentiert

Native Navigation scheiterte mit `net::ERR_BLOCKED_BY_ADMINISTRATOR`. Die Umgebungsrichtlinie wurde nicht geändert. Im Harness werden die unveränderten Anwendungsdateien in Chromium geladen, ein ausdrücklicher Speicherersatz bereitgestellt und fetch-Aufrufe über Python an den echten Java-Server weitergegeben.

Damit werden Darstellung, Interaktion, Escaping, Beschriftung, mobile Breite und Freigabe-/Ablehnungs-/Belegabläufe geprüft. Nicht abgedeckt sind native Navigation, Browser-fetch/CORS, CSP-Durchsetzung und echte Origin-/localStorage-Isolation. Die Screenshots zeigen echte gerenderte Oberflächen mit synthetischen Fällen aus diesem Aufbau.

Native Tests bleiben auf Benutzer- und CI-Rechnern Standard. Vor einer Ende-zu-Ende-Aussage müssen sie ohne Harness bestehen.

## Nicht ausgeführt

Host-Maven-Build, Spring-Laufzeit, echte PostgreSQL-Testcontainer, Kafka, Camunda-Deployment und -Ausführung, Brokerfehler, echter Camunda-Worker-Abbruch, echtes Ollama-Modell, Windows, Produktionslast, Accessibility-Audit, Penetrationstest und externe ERP-/Zahlungsanbindung wurden hier nicht validiert.

Gültiges BPMN-XML beweist keine Camunda-Kompatibilität. Quelltextreview ersetzt keinen Maven-Build. Ein Sandbox-Beleg beweist keine echte Zahlung. CI-Badges oder veröffentlichte GitHub-Läufe wurden nicht erfunden.

## Befehle

```bash
python scripts/test.py --repository
python -m pip install -r requirements-browser.txt
python -m playwright install chromium
python scripts/test.py --browser --repository
python scripts/evaluate.py --no-build
```

Nur zur ausdrücklichen Reproduktion des eingeschränkten Testaufbaus auf POSIX:

```bash
GOV_UI_TRANSPORT=harness python scripts/test.py --browser --repository
```

In PowerShell vorher `$env:GOV_UI_TRANSPORT = "harness"` setzen. Das Ergebnis darf nicht als nativer Browser-Ende-zu-Ende-Test bezeichnet werden. Die Stack-Abnahme ist getrennt dokumentiert.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
