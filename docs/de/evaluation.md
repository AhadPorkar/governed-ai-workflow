# Evaluation: Schwächen ebenfalls veröffentlichen

## Datensatz und Kennzahlen

`evaluation/cases.json` enthält **48 manuell gelabelte synthetische Fälle** in Deutsch und Englisch. Sie umfassen Erstattung, Stornierung und Alternativen sowie Mehrdeutigkeit, Verneinung, zitierte Aktionswörter, Teilbeträge, Lieferanteneinschränkungen, bezahlte/versandte Aufträge, andere Währungen und injektionsartige Texte.

Erwartete Vorschläge und zulässige Zustände wurden vor dem Baseline-Lauf festgelegt. Es handelt sich um einen kleinen Entwicklungsdatensatz, nicht um einen unabhängigen, zurückgehaltenen Benchmark. Private Kundenkommunikation ist nicht enthalten.

**Exakter Vorschlag** verlangt die erwartete Aktion, den ganzzahligen Betrag und den Lieferanten. Der Wortlaut der Erklärung wird nicht bewertet. **Zulässiges Ergebnis** prüft den resultierenden Zustand. Eine deterministisch blockierte Anfrage kann sicher abgelehnt sein, obwohl der Planer den falschen Vorschlag machte. Keine der Kennzahlen bedeutet vollständige erfolgreiche Geschäftsbearbeitung.

## Aufgezeichnetes Baseline-Ergebnis

| Kennzahl | Ergebnis |
|---|---|
| Fälle | 48. |
| Exakte Vorschläge | **33 / 48 = 68,75 %**. |
| Zulässige Ergebnisse | **40 / 48 = 83,33 %**. |
| Modellaufrufe / beobachtete Modelltokens | 0 / 0. |
| Freigegebene oder ausgeführte Effekte | 0. |
| Modellkosten | Nicht anwendbar; kein Modellaufruf. |

Die Schlüsselwort-Baseline scheitert insbesondere an Teilbeträgen, zitierten Wörtern, differenzierter Verneinung, Synonymen und Einschränkungen wie 'nur dieser Lieferant'. Bei einer Bitte um nur 25 Euro kann sie den gesamten verbleibenden Zahlungseingang vorschlagen. Dass der Betrag innerhalb der finanziellen Obergrenze liegt, macht die Absichtserkennung **nicht korrekt**. Die unabhängige Prüfung muss das erkennen; ein Modellvergleich soll solche Fehler verbessern.

Bei unzulässigen Anfragen kann die Baseline eine Erstattung vorschlagen, die Regeln blockieren sie jedoch. Das Label erwartet eventuell Eskalation. Beide Kennzahlen bleiben sichtbar, statt diese Differenz hinter einer einzigen Sicherheitsquote zu verstecken.

## Reproduktion und Modellvergleich

```bash
python scripts/evaluate.py
python scripts/evaluate.py --provider ollama --model YOUR_INSTALLED_MODEL
```

Jeder Lauf speichert SHA-256 und Version des Datensatzes, Planer, Java-/OS-Metadaten, Erwartungen, tatsächliche Vorschläge/Zustände, Dauer, Nutzung und Fehler. Neue Ergebnisse liegen unter `build/evaluation/`, der Liefernachweis unter `evidence/evaluation/`.

Zeitmessungen stammen aus einem einzelnen synthetischen Lauf ohne kontrolliertes Aufwärmen oder isolierte Last. Die enthaltenen p50/p95 sind Beobachtungen, keine Produktionszusagen. Baseline und Modellgeschwindigkeit nur mit Angaben zu Hardware, Modell, Aufwärmen und Kontext vergleichen.

Der Adapter kann konfigurierte Preise programmgesteuert verrechnen; der Launcher erfindet keine Preise. Keine lokale API-Gebühr bedeutet nicht kostenlose Hardware oder Energie. Bei fehlgeschlagenen Aufrufen können verlässliche Nutzungszahlen fehlen.

## Vor Aussagen zur KI-Qualität

Exaktes Modell, Version und Lizenz festhalten. Einen separat zurückgehaltenen, möglichst unabhängig geprüften Datensatz ergänzen. Gleiche Regel- und Freigabegrenzen verwenden. Fehler nach Kategorie, ungültige Ausgaben, Tool-Missbrauch, Eskalation, genaue Beträge, Zeit und Ressourcen ausweisen.

Menschlichen Prüfaufwand und echte Endergebnisse erst in einer kontrollierten Studie als Aufgabenerfolg messen. Diese Lieferung bietet einen Evaluationsrahmen und eine transparente Baseline; **ein Ergebnis eines echten Modells wird nicht behauptet**.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
