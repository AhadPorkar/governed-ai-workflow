# Governed AI Workflow Platform
## KI schlägt vor. Regeln autorisieren. Worker führen aus.

**Ahad Porkar | 1.1.0-rc.1 | 12. Oktober 2026 | Deutsch / English**

Eine Portfolio-Referenzimplementierung für eine konkrete Frage: Wie kann ein probabilistischer Assistent eine Bestellausnahme untersuchen, ohne seine eigenen Aktionen freigeben zu dürfen?

Eine Bestellung ist nicht lieferbar. Der Kunde beschreibt sein Anliegen in freiem Text. Ein Planer liest einen begrenzten, fallbezogenen Datenstand und schlägt Erstattung, Stornierung, Lieferantenwechsel oder Eskalation vor. Deterministische Regeln lehnen unzulässige Vorschläge ab. Eine andere Person prüft die genaue Aktion. Vor dem Commit kontrolliert ein Worker die gespeicherte Freigabe und die aktuellen Fakten erneut.

Dies ist kein universeller Agentenbaukasten. Der enge Umfang ermöglicht nachvollziehbare Kontrollgrenzen, reproduzierbare Fehlerfälle und eine zweisprachige Bedienoberfläche.

## Abgrenzung der Lieferung

Das hochgeladene Archiv `rechnungflow.zip` enthält das separate Rechnungsprojekt RechnungFlow. Es wurde nicht verändert. Gewünschtes Ziel ist `AhadPorkar/bpmn-order-workflow`. Dessen öffentlicher Hauptzweig wurde eingesehen, konnte hier aber nicht geklont werden. Ein unveränderlicher Commit wurde nicht bestimmt.

Die Lieferung besteht deshalb aus einem **eigenständig ausführbaren Modul und einem additiven Integrations-Overlay**, nicht aus einer zusammengeführten Kopie des Ziel-Repositories. Bestehende Tabellen, Zahlungsanbindungen, Bestandszweige, README-Dateien und Maven-Konfigurationen werden nicht stillschweigend ersetzt.

Alle Geschäftseffekte der ausgeführten Demo liegen in einem **Sandbox-Buchungsbestand**. Eine Erstattung ändert synthetische Beträge, ruft aber keinen Zahlungsdienst auf. Lieferantenbestände sind synthetische, bestellbezogene Momentaufnahmen und keine globale Lagerreservierung.

## Implementierte Funktionen

| Bereich | Umsetzung |
|---|---|
| Untersuchung | Transparente Schlüsselwort-Baseline sowie nativer Ollama-Adapter mit Tool-Aufrufen. |
| Autorität | Versionierte Regeln, striktes Vorschlagsschema, ganzzahlige Eurocent und Datenstandsbindung. |
| Menschliche Kontrolle | Unabhängige Freigabe, genauer Vorschlags-Hash, Begründung und Ablaufzeit. |
| Ausführung | Nur Worker, dauerhafter Beleg, wiederholsichere Sandbox-Effekte und separate Bestätigung. |
| Wiederanlauf | Atomare Dateispeicherung, echter Prozessabbruch, konkurrierende Wiederholungen und Integritätsprüfung. |
| Oberfläche | Deutsch/Englisch, Fallliste, Nachweise, Audit, Modellnutzung, Belege und mobiles Layout. |
| Referenzintegration | Spring MVC, PostgreSQL, Kafka-Outbox, Camunda-Worker und BPMN. |
| Nachweise | Java-Vertragstests, HTTP-/Prozesstests, expliziter Browser-Testaufbau und 48 gelabelte Fälle. |

Die Referenzintegration wurde hier **nicht als kompletter Stack validiert**. Maven, Docker, Camunda, Kafka, PostgreSQL und eine echte Modelllaufzeit standen nicht zur Verfügung. Native Browsernavigation war durch die Umgebung blockiert; DOM- und Darstellungstests verwendeten deshalb einen offengelegten Testaufbau.

## Der entscheidende Nachweis

Nach einer Freigabe wird der Worker nach dem Commit, aber vor seiner Bestätigung beendet. Nach Serverneustart und Wiederholung erscheint derselbe Beleg; die synthetische Erstattung findet nur einmal statt. Das zeigt eine transaktionale Idempotenzgrenze, nicht eine globale Exactly-once-Garantie.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
