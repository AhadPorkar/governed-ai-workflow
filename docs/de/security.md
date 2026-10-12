# Sicherheitsmodell und Grenzen

## Bedrohungsmodell

Kundentext, Modellausgabe, Browserdaten und Transportwiederholungen sind nicht vertrauenswürdig. Anwendungscode, Administratorkonfiguration, Identitätsdatei, Datenbank und Camunda-Kontrollzugriff gelten in dieser Referenz als vertrauenswürdig. Ein kompromittierter Administrator oder Datenbankserver liegt außerhalb der zugesicherten Schutzgrenze.

| Risiko | Implementierte Kontrolle | Verbleibende Grenze |
|---|---|---|
| Prompt Injection verlangt eine Erstattung. | Nur vier Lesetools; ausführende Tools nicht vorhanden. | Semantische Fehlinterpretation bleibt möglich. |
| Fremder Mandant. | Mandant aus authentifizierter Identität, erneute Eigentumsprüfung. | Statische Identitäten sind kein produktives IAM. |
| Browser setzt `approved=true`. | Strikter HTTP-Vertrag; Worker liest gespeicherte Freigabe. | Privilegierter Kontrollzugriff bleibt mächtig. |
| Selbstfreigabe. | Reviewer-Rolle und andere Identität erforderlich. | Reale Rollenzuweisung muss organisatorisch gesichert sein. |
| Veränderter Betrag, Auftrag oder Regelstand. | Exakter Hash, Versionen und Ablaufzeit vor Commit. | Vollständige Manipulation des vertrauenswürdigen Speichers ist nicht abgedeckt. |
| Verlorene Jobbestätigung. | Atomarer Effekt und dauerhafter Beleg. | Externe APIs brauchen eigene Idempotenz und Abgleich. |
| HTML im Kundentext. | Escaping, keine HTML-Injektion, lokale Assets, CSP-Header. | Native CSP-Durchsetzung noch nicht abgenommen. |
| Beliebige Modell-Zieladresse. | Leere Tool-Argumente und administrativ gesetzter Endpunkt. | Produktiv zusätzliche Egress-Freigabeliste notwendig. |
| Ressourcenverbrauch. | Grenzen für Body, Parser, Tools, Kontext, Runden, Zeit und Tokens. | Kein vollständiger Internet-/DDoS-/Slow-Client-Schutz. |

## Modellbudgets

Der Ollama-Adapter begrenzt auf sechs Modellrunden, acht Tool-Ausführungen und 6.000 beobachtete Eingabe-/Ausgabetokens. Eine Antwort darf höchstens 65.536 Byte enthalten. Anfragen sind auf 48.000 serialisierte Zeichen begrenzt. Standard sind 45 Sekunden Gesamtbudget, maximal 20 Sekunden je Anfrage, ein Generierungshinweis von 768 Tokens und ein Kontexthinweis von 8.192 Tokens.

Tokenzahlen werden nach der Antwort geprüft. Dies garantiert kein im Voraus reserviertes Kostenbudget. Fehlgeschlagene Anfragen können Ressourcen verbraucht haben, ohne abrechenbare Nutzungsdaten zurückzugeben. Fehlende Zähler werden nicht als erfundene Nullwerte behandelt. Redirects werden nicht verfolgt. Providerfehler eskalieren, statt unbemerkt einen anderen Modus zu verwenden.

## Genaue Freigabe statt allgemeinem Ja

Alle wirksamen Aktionen brauchen eine unabhängige Person, auch geringe Erstattungsbeträge. Der Hash bindet den kompletten Vorschlag samt Begründung. Die kanonische JSON-Kodierung ist projektspezifisch; es wird keine RFC-8785-Konformität behauptet.

Regeln sind versionierter Java-Code. Natürlichsprachliche Richtlinien werden nicht als ausführbare Autorität interpretiert. Regeländerungen brauchen eine neue Version und Regressionstests.

## Daten und Zugangsdaten

Diese Version soll nur synthetische Daten verarbeiten. Anfragen, Begründungen, Zustand und Audit werden gespeichert. Verschlüsselung ruhender Daten, Löschfristen, Zweckbindung und produktive Datenschutzprüfung sind nicht umgesetzt. Begriffe wie DSGVO-zertifiziert, EU-AI-Act-konform, penetration-getestet oder unabhängig sicherheitsauditiert sind für diese Lieferung unzutreffend.

Tokens sind kryptografisch zufällig; der Server lädt Hashes und Ablaufzeiten. Die Hilfsdatei `access.json` enthält Klartext-Demo-Tokens und bleibt privat. Die Oberfläche hält Zugangsdaten nur im JavaScript-Speicher, nicht in localStorage. OAuth/OIDC, SSO, MFA und Kontowiederherstellung sind nicht enthalten. Rotation erfolgt mit Neustart.

Die Audit-Hash-Kette erkennt lokale Inkonsistenzen, aber keine vollständige böswillige Neuschreibung. Stärkere Nachweise erfordern separat getestete Append-only-Grenzen und externe Verankerung.

## Meldung und Produktionsvoraussetzungen

Probleme mit synthetischer Reproduktion über den verifizierten privaten Kontaktweg des Repository-Inhabers melden; es wurde keine unbestätigte Sicherheits-E-Mail erfunden. Keine echten Tokens oder Kundendaten in öffentliche Issues aufnehmen.

Vor öffentlichem Hosting sind OIDC, geringstmögliche Berechtigungen, Prüfung aller Host-APIs, TLS, Egress-Kontrollen, gemeinsame Ratenlimits, Zeitlimits, Aufbewahrung/Verschlüsselung, Lizenzprüfung und eine reale Sicherheitsbewertung erforderlich.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
