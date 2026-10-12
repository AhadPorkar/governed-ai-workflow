# Schnellstart: eigenständiges Quellpaket

## Voraussetzungen

Benötigt werden ein **Java-21-JDK** mit `java`, `javac` und `jar` im PATH sowie Python ab Version 3.10. Die Baseline-Demo benötigt weder Maven noch Docker, Cloudkonto, Datenbank oder kostenpflichtigen Modellzugang. Hier wurde Linux getestet. Windows-Startdateien sind enthalten; eine Windows-Ausführung wurde nicht geprüft.

Alle folgenden Befehle werden im Stammverzeichnis des entpackten **Quellpakets** ausgeführt, nicht im bestehenden BPMN-Repository. Für das Overlay gilt die gesonderte Integrationsanleitung.

```bash
python scripts/run_demo.py
```

Öffnen Sie `http://127.0.0.1:8095`. Das Skript kompiliert die Java-Quellen, erzeugt bei Bedarf lokale Demo-Identitäten und legt synthetische Daten für zwei Mandanten an. Vorhandene Bestellungen werden dabei nicht zurückgesetzt.

Die Datei `.local/access.json` enthält die Demo-Tokens. Melden Sie sich mit `operator` an. Erstellen Sie für `ORD-1001` einen Fall mit `Bitte erstatten Sie meine Zahlung.` und starten Sie die Untersuchung. Prüfen Sie den Vorschlag, melden Sie sich ab und mit `reviewer` an. Kontrollieren Sie Originalanfrage, Betrag und Nachweise, geben Sie eine Begründung ein und erteilen Sie die Freigabe.

Starten Sie in einem zweiten Terminal:

```bash
python scripts/worker.py --once
```

Aktualisieren Sie den Fall. Der Beleg ist ausdrücklich mit `sandbox: true` gekennzeichnet. Die Rolle `viewer` darf lesen, aber keine Fälle erstellen oder freigeben. `other` zeigt einen getrennten Mandanten.

## Tests und Evaluation

```bash
python scripts/test.py --repository
python scripts/evaluate.py --no-build
```

Der Testbefehl kompiliert sauber neu und führt Java-, HTTP-/Prozess- und Repository-Prüfungen aus. Die Evaluation ist ein getrenntes Experiment mit erwarteten Vorschlägen. Fehlklassifikationen werden ausgewiesen und nicht in bestandene Sicherheitstests umbenannt.

Für native Chromium-Tests auf Ihrem Rechner:

```bash
python -m pip install -r requirements-browser.txt
python -m playwright install chromium
python scripts/test.py --browser --repository
```

Bei Bedarf zeigt `CHROMIUM_PATH` auf die Browserdatei. Andernfalls verwendet Playwright seine installierte Chromium-Version. Standard ist die native Ausführung. `GOV_UI_TRANSPORT=harness` ist ausschließlich ein ausdrücklich dokumentierter Ersatz-Testaufbau für eingeschränkte Umgebungen und kein nativer Ende-zu-Ende-Nachweis.

## Echte Modellnutzung bewusst aktivieren

Starten Sie einen vertrauenswürdigen Ollama-Server und installieren Sie ein Tool-fähiges Modell mit passender Lizenz und Hardwareanforderung. In dieser Lieferung wurde kein bestimmtes Modell evaluiert.

```bash
python scripts/run_demo.py --provider ollama --model YOUR_INSTALLED_MODEL
python scripts/evaluate.py --provider ollama --model YOUR_INSTALLED_MODEL
```

Standardendpunkt ist `http://127.0.0.1:11434/api/chat`. Für andere Adressen dienen `--model-endpoint` in der Demo und `--endpoint` in der Evaluation. Entfernte Ziele verlangen HTTPS. Die Adresse ist vertrauenswürdige Administratorkonfiguration und niemals ein Modell- oder Kundenargument. Vor echten Daten sind Datenschutz- und Verarbeitungsbedingungen zu prüfen.

Fehler des Modellservers führen zur Eskalation, nicht zu einem heimlichen Wechsel auf die Baseline. Die Baseline wird sichtbar ausgewiesen und verursacht keine Modellaufrufe.

## Ablauf und Rotation von Zugangsdaten

Tokens laufen nach 24 Stunden ab. Anwendung stoppen, explizit rotieren und neu starten:

```bash
python scripts/run_demo.py --rotate-credentials
```

`.local/state` bleibt erhalten. Niemals `.local`, Tokens oder echte Anfragen committen. Für eine neue Wegwerf-Demo verwenden Sie ein frisch entpacktes Verzeichnis, statt einen unbekannten Datenbestand zu löschen. Unter Windows müssen NTFS-Berechtigungen separat geprüft werden.

Die erzeugte JAR-Datei enthält nicht selbstständig alle Webdateien. Verwenden Sie den Launcher, damit Ressourcen- und Datenpfade stimmen.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
