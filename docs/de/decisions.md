# Architekturentscheidungen

## ADR-001: Vorschlag und Autorität trennen

**Entscheidung:** Der Planer liefert einen nicht vertrauenswürdigen Vorschlag; Regeln, Freigabe und Ausführung sind eigene Operationen. **Grund:** Sprachliche Sicherheit ist keine Berechtigung. **Abwägung:** Mehr Zustand und langsamerer Ablauf als direkter Tool-Zugriff. **Folge:** Jede wirksame Aktion benötigt eine gespeicherte, unabhängig geprüfte Freigabe.

## ADR-002: Enger Anwendungsfall

**Entscheidung:** Nur Erstattung nicht lieferbarer Bestellungen, unbezahlte Stornierung, zulässiger Lieferantenwechsel und Eskalation. **Grund:** Aussagekräftige Fehler-/Sicherheitstests sind wertvoller als ein großer Tool-Katalog. **Abwägung:** Kein generischer Workflow-Builder, Retourenprozess, Steuer-/Währungsmodul oder komplexe Mehrpositionslogistik. **Folge:** Andere Fälle werden eskaliert oder blockiert.

## ADR-003: Zuerst synthetische Geschäftseffekte

**Entscheidung:** Unabhängiger Sandbox-Buchungsbestand. **Grund:** Echte Zahlungs-/Auftragsfakten lagen nicht als verifizierte Integrationsquelle vor. Ein erfundener externer Erfolg wäre irreführend. **Abwägung:** Keine echte ERP-/Zahlungswirkung. **Folge:** `sandbox=true`; echte Konnektoren brauchen Idempotenz und Ergebnisabgleich.

## ADR-004: Transaktionales Aggregat als Referenz

**Entscheidung:** Memory-, atomarer Datei- und PostgreSQL-Einzelaggregat-Adapter. **Grund:** Effekt, Beleg, Audit und Outbox liegen in einer nachvollziehbaren Transaktion. **Abwägung:** Serialisierung, vollständiges Neuschreiben und begrenzte Kapazität. **Folge:** Vor Durchsatzzusagen messen und gegebenenfalls neu entwerfen; kein Event-Sourcing-Produkt behaupten.

## ADR-005: Baseline und begrenzter Ollama-Adapter

**Entscheidung:** Reproduzierbare Tests nutzen Baseline und Protokollfixtures; echte Modellnutzung ist explizit. **Grund:** Tests ohne Cloud-Zugang bei vorhandener echter Tool-Integration. **Abwägung:** Schwache Baseline-Semantik und offene Modellevaluation. **Folge:** Fehler veröffentlichen und Planermodus anzeigen.

## ADR-006: Freigabe an genauen Kontext binden

**Entscheidung:** Hash über Mandant, Fall, Auftrag, Versionen, Originalanfrage, Vorschlag und Ablaufzeit. **Grund:** Ein allgemeines Ja kann gegen geänderte Fakten missbraucht werden. **Abwägung:** Veraltete Vorschläge brauchen neue Prüfung. **Folge:** Kein nachträgliches Bearbeiten eines freigegebenen Betrags; erneute Faktenprüfung vor Ausführung.

## ADR-007: Mindestens-einmal-Transport, idempotenter Effekt

**Entscheidung:** Wiederholte Jobs/Ereignisse zulassen und für abgeschlossene Fälle denselben Beleg liefern. **Grund:** Abstürze erzeugen unklare Bestätigungen. **Abwägung:** Deduplizierung und zusätzliche Audit-Einträge. **Folge:** Keine verteilte Exactly-once-Behauptung; Verbraucher und echte Konnektoren benötigen eigene Grenzen.

## ADR-008: Verifikationslücken offenlegen

**Entscheidung:** Nützliches Modul mit ehrlicher Kandidatenmatrix statt erfundener Stack-/Modellergebnisse liefern. **Grund:** Falsches Quellarchiv, hier nicht ausführbare externe Abhängigkeiten und blockierte native Navigation. **Abwägung:** Eigentümer muss Integrationsabnahme nachholen. **Folge:** Baseline-, Harness- und offene Abnahmetests bleiben getrennte Kategorien.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
