# Release-Dossier: 1.1.0-rc.1

**Governed AI Workflow Platform**
**Ahad Porkar | 12. Oktober 2026**

> KI darf Unsicherheit untersuchen. Daraus entsteht keine Berechtigung zum Handeln.

## Die Geschichte dieses Release Candidates

Der Kandidat erweitert das Order-to-Cash-Portfolio um eine kontrollierte Ausnahmebearbeitung: fallbezogene Lesetools, begrenzte Planung, deterministische Regeln, unabhängige Freigabe, dauerhafte Ausführungsbelege und nachvollziehbaren Wiederanlauf. Die zweisprachige Oberfläche macht Kontrollgrenzen sichtbar, statt nur einen gelungenen Chat zu zeigen.

Die wichtigste Demonstration ist absichtlich praktisch: Worker in der Lücke zwischen Commit und Bestätigung beenden und nachweisen, dass die synthetische Erstattung nicht doppelt erfolgt. Eine plausible Antwort ist noch kein verantwortbarer Geschäftsvorgang.

## Lieferumfang

Enthalten sind Java-Kern, Offline-Build, lokaler Server/Worker, EN-/DE-Oberfläche, automatisierte Tests, gelabelte Evaluation, OpenAPI, additives Spring-/PostgreSQL-/Kafka-/Camunda-Overlay, BPMN, SQL-Migration, geschützter Installer, Herkunftsnachweis, zweisprachige technische Dokumentation, Release-PDFs und LinkedIn-Entwürfe.

Das Dateimanifest enthält Quell-/Zielpfade und Hashes. Bestehende Dateien werden nicht geändert oder gelöscht. Das RechnungFlow-Archiv bleibt unangetastet. Das Ziel-Repository wurde öffentlich eingesehen, hier aber nicht geklont oder zusammengeführt.

## Vorzeigbare Nachweise

106 Java-Vertragstests und 29 echte HTTP-/Prozesstests bestanden im aufgezeichneten Lauf. Neun Browser-Prüfungen bestanden im offengelegten DOM-Harness, da native Navigation blockiert war. Zusätzliche Repository-Prüfungen und Gesamtsummen stehen im finalen Bericht. 32 parallele direkte und zwölf parallele HTTP-Wiederholungen werden separat geprüft.

Die Wiederanlauf-Demo umfasst einen echten Worker-Abbruch, Serverneustart und einen einzigen Beleg. Die Baseline-Evaluation zeigt 33 exakte Vorschläge und 40 zulässige Ergebnisse bei 48 Fällen, keine behauptete Modellüberlegenheit.

## Voraussetzungen für ein finales integriertes Release

- [ ] Overlay auf dokumentierten sauberen Host-Commit anwenden und prüfen.
- [ ] Maven-Build sowie PostgreSQL-Parallelitäts- und Rollbacktests bestehen.
- [ ] BPMN deployen und Kafka-/Camunda-Fortschritt samt Fehlerwiederholung belegen.
- [ ] Native Browser-, Tastatur-/Accessibility- und gegebenenfalls Windows-Prüfungen abschließen.
- [ ] Echtes Modell auf separatem Datensatz evaluieren und Fehler/Kostenannahmen veröffentlichen.
- [ ] Sandbox-Umfang bewusst beibehalten oder echte Konnektoren samt Abgleich implementieren.
- [ ] Demo-Identitäten vor öffentlicher Bereitstellung ersetzen und alle Host-APIs prüfen.
- [ ] Lizenzen, Datenschutz/Aufbewahrung, Härtung, Sicherung und Lastverhalten bewerten.

Erst nach belastbaren Nachweisen darf ein finales integriertes Release daraus werden. Ein Portfolio-Sandbox-Kandidat kann vorher ehrlich veröffentlicht werden, ohne Produktionszertifizierung anzudeuten.

## Vorschlag für den Release-Titel

`Governed AI Workflow 1.1.0-rc.1 - genaue Freigaben, dauerhafte Belege, getesteter Wiederanlauf`

Die README-Ergänzungen und Screenshots können verwendet werden. Modul- und Host-Integrationsstatus getrennt lassen. LinkedIn-Texte sind Entwürfe; vor dem Posten einen tatsächlich veröffentlichten Release-Permalink einsetzen. Diese Lieferung hat nichts in Ihren Konten veröffentlicht.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
