# Vorführskript

## Einstieg

Konsole mit sichtbarer Sandbox- und Baseline-Kennzeichnung zeigen. Erläutern: 'Diese Referenz untersucht KI-Unterstützung ohne Delegation geschäftlicher Autorität. Die deterministische Baseline macht den Ablauf reproduzierbar. Ein Ollama-Adapter ist implementiert, aber ein Qualitätsergebnis eines echten Modells wird hier nicht behauptet.'

## 1. Eine Anfrage ist keine Erlaubnis

Als Operator `ORD-1001` auswählen und `Mein Headset ist nicht lieferbar. Bitte erstatten Sie meine Zahlung.` eingeben. Nach Untersuchung Originaltext, Regelreferenz, Lesetools, Betrag und Frist zeigen. Es gibt keinen Ausführungsknopf, und der Ersteller darf seinen Vorschlag nicht selbst freigeben.

Mit `ORD-1003` eine Erstattung nach Versand anfragen und die Regelblockierung zeigen. Für `ORD-1004` eine unklare Anfrage verwenden und die Eskalation zeigen. Ein selbstsicherer Modelltext würde die Autorisierung nicht ersetzen.

## 2. Die genaue Aktion prüfen

Abmelden und mit separatem Reviewer-Token anmelden. Originalanfrage, Betrag, Bestellung und Nachweise vergleichen. Sinnvolle Begründung eingeben und freigeben. Zustandswechsel und Audit zeigen.

Erklären, dass der Hash Vorschlag, Bestellversion, Regelstand und Ablaufzeit bindet. Geänderte Fakten machen die Freigabe ungültig, statt nur die Anzeige zu verändern.

## 3. Am kritischen Punkt abbrechen

In anderem Terminal `python scripts/worker.py --once --crash-after-commit` ausführen. Den absichtlichen Fehler zeigen. Server ohne Zustandslöschung neu starten, dann `python scripts/worker.py --once` ausführen und aktualisieren.

Einen Beleg, einen Commit, das Wiederholungsereignis und die Bestätigung zeigen. Klarstellen: Dies ist ein Sandbox-Datenbankeffekt, keine Exactly-once-Zusage für einen entfernten Zahlungsdienst.

## 4. Nachweise statt Behauptungen

`evidence/verification.json` und die benannten Tests öffnen. Das unvollkommene Baseline-Ergebnis von 33/48 exakten Vorschlägen zeigen. Modellprotokolltests, echte Modellevaluation und echte Camunda-Abnahme voneinander unterscheiden.

Zwischen Deutsch und Englisch wechseln und die mobile Ansicht zeigen. Zum Abschluss den Architekturkompromiss erklären: Das einzelne Aggregat macht die Transaktion gut prüfbar; Skalierung braucht ein anderes Speichermodell und neue Tests.

## Aufzeichnung

Nur synthetische Daten verwenden. Tokens und `.local/access.json` ausblenden. Keine erfundenen CI-Badges oder echten Kundenerstattungen behaupten. Screenshots nicht so beschneiden, dass Sandbox-, Baseline- oder Release-Candidate-Hinweise verschwinden. Vor Veröffentlichung echten Release-Link einsetzen.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
