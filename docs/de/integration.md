# Integrations-Overlay für bpmn-order-workflow

## Beobachtete Ausgangsbasis

Der eingesehene Repository-Stand verwendet Java 21, Spring Boot 3.5.16, Camunda 8.8.40, PostgreSQL, Kafka und Testcontainers 2.0.5. Das sind beobachtete Versionen, keine Aussage über die jeweils neueste Ausgabe. Die Hauptanwendung aktiviert bereits Scheduling und deployt `classpath*:bpmn/*.bpmn`. Worker-Annotationen folgen `io.camunda.client.annotation.JobWorker`.

Der neue Planer ist eine eigene externe Tool-Schleife. Es wird nicht behauptet, native KI-Konnektoren aus Camunda 8.10 in die 8.8-Anwendung eingebaut zu haben. Die Primärquellen sind im Quellenverzeichnis verlinkt.

## Bewusst anwenden

Erstellen Sie einen sauberen Branch des tatsächlichen BPMN-Repositories. Im eigenständigen Quellpaket:

```bash
python scripts/apply_overlay.py /path/to/bpmn-order-workflow --report build/overlay-dry-run.json
python scripts/apply_overlay.py /path/to/bpmn-order-workflow --apply --report build/overlay-applied.json
```

Unter Windows den Pfad beispielsweise als `"D:\Projects\bpmn-order-workflow"` angeben.

Der Installer prüft die Maven-Artefakt-ID, lehnt RechnungFlow und symbolische Zielverknüpfungen ab und verweigert das Überschreiben abweichender Dateien. Eine bereits belegte Flyway-Version `V100` wird erkannt. Alle Ziele werden vorab geprüft; bei Kopierfehlern werden nur neu erzeugte Dateien dieses Aufrufs entfernt. Das Verfahren ist keine atomare Bereitstellung des gesamten Repositories und kein Schutz gegen feindliche gleichzeitige Dateisystemänderungen.

Das Manifest enthält jede Quelle-Ziel-Zuordnung. Bestehende Dateien werden **nullmal verändert** und **nullmal gelöscht**. Danach `git status` und `git diff --stat` prüfen. README-Ergänzungen liegen separat zur bewussten Übernahme vor.

## Neue Bestandteile

| Bestandteil | Ziel |
|---|---|
| Kern | `src/main/java/.../governance/`, ohne externe Kernabhängigkeiten. |
| Spring-Adapter | `.../governance/spring/`, nur bei expliziter Aktivierung. |
| BPMN | `src/main/resources/bpmn/governed-order-exception.bpmn`. |
| SQL | `V100__governed_ai_workflow.sql`, unabhängige Tabelle `governance_state`. |
| Oberfläche | `src/main/resources/static/governance/`. |
| Konfiguration | `application-governance.yml`. |
| Tests | JUnit-Brücke des Kerns und `PostgresGovernanceIT` mit Docker. |
| Dokumentation | `docs/governed-ai/` in Deutsch und Englisch. |

Es werden keine zusätzlichen Maven-Abhängigkeiten gegenüber dem beobachteten Host geplant. Der **vollständige Maven-Build bleibt dennoch ein verbindlicher Abnahmeschritt**. Quelltextprüfung beweist keine Laufzeit- oder API-Kompatibilität.

## Wegwerf-Stack konfigurieren

Im Host-Checkout Demo-Identitäten außerhalb der Versionsverwaltung erzeugen:

```bash
python scripts/governance-credentials.py /private/path/governance-demo
```

Folgende Umgebungsvariablen mit der jeweiligen Shell setzen:

```text
SPRING_PROFILES_ACTIVE=governance
GOVERNANCE_ENABLED=true
GOVERNANCE_SEED_DEMO=true
GOVERNANCE_AUTH_FILE=/private/path/governance-demo/auth.json
GOVERNANCE_ALLOWED_ORIGIN=http://localhost:8081
GOVERNANCE_PLANNER=baseline
```

Starten Sie die im Host dokumentierte PostgreSQL-/Kafka-/Camunda-Umgebung und dann `./mvnw spring-boot:run`, unter Windows `mvnw.cmd spring-boot:run`. Die Oberfläche liegt unter `http://localhost:8081/governance/index.html`.

Standard ist deaktiviert. SQL-Migration und vorhandenes Wildcard-BPMN-Deployment verarbeiten eingefügte Ressourcen trotzdem im normalen Host-Lebenszyklus. Deaktiviert bedeutet keine Governance-Beans, Worker und Relays, nicht das Verschwinden der Ressourcen.

Für Ollama dienen `GOVERNANCE_PLANNER=ollama`, `GOVERNANCE_OLLAMA_MODEL` und optional `GOVERNANCE_OLLAMA_ENDPOINT`. Dies aktiviert keine genehmigte Verarbeitung echter Daten.

## Abnahme

```bash
./mvnw verify
python scripts/governance-acceptance.py --access /private/path/governance-demo/access.json --confirm-synthetic
```

Der Smoke-Test ruft absichtlich weder `/plan` noch `/run` auf. Er wartet auf automatische Verarbeitung und benötigt eine frische synthetische `ORD-1001`. Ein bereits erstatteter Datensatz führt zum Fehler. Prozessinstanz und Kafka-Ereignisse separat kontrollieren. Danach echten Camunda-Worker zwischen Commit und Jobbestätigung abbrechen, Wiederanlauf prüfen und PostgreSQL-Parallelitäts-/Rollbacktests ausführen. Diese Abnahme wurde hier nicht ausgeführt.

Währenddessen keinen eigenständigen Worker gegen den Spring-Stack starten; er könnte eine defekte Orchestrierung verdecken.

## Bewusst offene Integration

Der bisherige Zweig für fehlenden Bestand wird **nicht umverdrahtet**. Eine zukünftige vertrauenswürdige Grenze muss echte Auftragsdaten, belegte Zahlungseingänge und Bestände übernehmen. Eine `paymentId` allein belegt keinen gebuchten Betrag.

Echte ERP-/Zahlungsschreibzugriffe, OIDC, skalierbare Mandantenspeicherung, Outbox-Verteilung über mehrere Instanzen, gemeinsame Ratenlimits und Monitoring bleiben offen. Bestehende Host-APIs behalten ihr bisheriges Sicherheitsmodell. Der neue Filter sichert nicht automatisch die gesamte Anwendung. Camunda-Zugriff bleibt separat zu schützender Kontrollzugriff.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
