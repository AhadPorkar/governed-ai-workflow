# KI darf einen Ausnahmefall untersuchen. Daraus entsteht keine Handlungsbefugnis.

## Warum ich diese Workflow-Referenz entwickelt habe

Eine nicht lieferbare Bestellung wirkt wie ein kleines fachliches Problem. Schnell wird daraus ein Systemproblem: Der Kunde wünscht vielleicht eine Erstattung, eine Alternative oder nur eine Erklärung. Während der Prüfung kann sich der Bestand ändern. Ein Worker kann nach dem Commit ausfallen. Eine Nachricht kann doppelt eintreffen.

Ein Assistent mit einer überzeugenden Antwort löst diese Probleme nicht automatisch. Ich wollte eine Portfolio-Referenz, die Grenzen sichtbar macht: Wo hilft der Umgang mit Unsicherheit? Wo entscheiden Regeln? Wer gibt frei? Was geschieht nach einem Fehler?

Das Ergebnis ist ein Release Candidate für ein enges Szenario, keine allgemeine Plattform für autonome Agenten.

## Vier Verantwortungen statt eines mächtigen Agenten

Die Untersuchung liest bestellbezogene Daten, Bestand, zugelassene Alternativen und Regeln. Alle Tools sind lesend. Der Planer kann weder den Mandanten wechseln noch beliebiges SQL ausführen oder einen Erstattungsendpunkt aufrufen.

Die Regelebene prüft einen strikt strukturierten Vorschlag. Beträge sind ganzzahlige Eurocent. Eine Erstattung darf den verbleibenden Zahlungseingang nicht übersteigen. Stornierung verlangt einen passenden Lieferzustand und keinen ausstehenden gebuchten Betrag. Ein alternativer Lieferant muss zugelassen, lieferfähig und nach der Referenzregel nicht teurer sein.

Die Freigabe liegt bei einer anderen Identität als der des Erstellers. Die prüfende Person sieht Originalanfrage, genaue Aktion, Nachweise und Frist und dokumentiert eine Begründung. Ein allgemeines Ja genügt nicht: Die Freigabe bindet den konkreten Vorschlag und seinen Kontext.

Die Ausführung kontrolliert gespeicherte Freigabe, Bestellversion, Regelversion und Ablaufzeit erneut. Weder Modell noch Browser können aus einer Prozessvariable eine Berechtigung machen.

## Der kritische Moment liegt nach dem Commit

In der Demo wird der Worker absichtlich nach dem Commit des Sandbox-Effekts beendet, aber vor seiner Jobbestätigung. Der Server startet neu. Ein neuer Worker wiederholt die Anfrage.

Das erwartete Ergebnis ist keine zweite Erstattung, sondern derselbe Beleg.

Synthetische Bestelländerung, Beleg, Audit und Outbox werden gemeinsam gespeichert. Eine Wiederholung liest den vorhandenen Beleg. Die getrennte Jobbestätigung darf verloren gehen, ohne eine erneute Geschäftswirkung auszulösen.

Diese Garantie gilt für die transaktionale Sandbox-Grenze. Sie ist keine globale Exactly-once-Zusage für einen echten Zahlungsdienst. Ein realer Konnektor braucht einen eigenen Idempotenzschlüssel, einen dauerhaften Auftragszustand und einen Abgleich unklarer entfernter Ergebnisse.

## Warum die Baseline unvollkommen sein darf

Ein nativer Ollama-Adapter mit Tool-Aufrufen ist implementiert. Die reproduzierbare lokale Demo startet jedoch mit einer transparenten Schlüsselwort-Baseline. So lässt sich der Kontrollfluss ohne gekauften Modellzugang untersuchen, ohne eine Attrappe als Sprachmodell auszugeben.

Der deutsch-englische Datensatz umfasst 48 Fälle: Mehrdeutigkeit, Verneinung, Teilbeträge, zitierte Absichten, eingeschränkte Lieferanten und unzulässige Anforderungen. Die Baseline erzeugte 33 exakte Vorschläge und 40 zulässige Ergebnisse.

Ein wichtiger Fehler betrifft Teilrückzahlungen. Der gesamte verbleibende Zahlungseingang kann innerhalb der finanziellen Grenze liegen und trotzdem der Kundenanfrage widersprechen. Regelkonformität ist nicht dasselbe wie semantische Richtigkeit. Deshalb bleiben Originaltext und genauer Betrag für eine unabhängige Person sichtbar.

Daraus wird keine werbliche Genauigkeitsbehauptung. Es ist ein kleiner Entwicklungsdatensatz, kein unabhängiger Benchmark und kein Ergebnis eines echten Modells.

## Jeder Nachweis hat einen Geltungsbereich

Die aufgezeichnete Lieferung enthält 106 bestandene Java-Vertragstests und 29 bestandene Tests mit echten HTTP-/Server-/Worker-Prozessen. Geprüft werden Rollen, Mandantentrennung, Freigabemanipulation, veraltete Fakten, Ablauf, Parallelität und Wiederanlauf. Repository-Prüfungen kontrollieren Paket und Strukturverträge.

Neun UI-Prüfungen verwenden das echte JavaScript und CSS in Chromium. Weil native Navigation in der Ausführungsumgebung blockiert war, leitete ein dokumentierter DOM-Harness API-Anfragen an den echten Java-Server weiter. Das prüft relevante Interaktion und Darstellung, aber keine native Netzwerk- oder CSP-Durchsetzung.

Das Spring-/Camunda-/Kafka-/PostgreSQL-Overlay samt Abnahmetests ist enthalten; der externe Stack wurde hier jedoch nicht ausgeführt. Echte Modellqualität, Produktionseinsatz, Sicherheitszertifizierung oder Kundenerstattung werden nicht behauptet.

## Welche Geschichte das Repository erzählen soll

Die Oberfläche ist wichtig, weil eine prüfende Person das System verstehen muss, nicht nur sein Architekturdiagramm. Die deutsche und englische Konsole zeigt Warteschlange, Vorschlag, Nachweise, Audit, Nutzung und Beleg. Sandbox und Planermodus bleiben sichtbar.

Genauso wichtig sind bewusste Architekturkompromisse. Ein einzelnes transaktionales Aggregat ist gut prüfbar, begrenzt aber den Durchsatz. Die Audit-Hash-Kette erkennt lokale Inkonsistenzen, ist jedoch gegen eine privilegierte vollständige Neuschreibung nicht manipulationssicher. Statische ablaufende Identitäten passen zur lokalen Demo, ersetzen aber kein produktives OIDC.

Diese Punkte sollen nicht erst im Vorstellungsgespräch auftauchen. Sie sind der eigentliche Inhalt des Gesprächs.

Die nächsten Abnahmen sind eine dokumentierte Host-Integration, reale PostgreSQL-/Camunda-Fehlertests, native Browserprüfung, ein separater Modellevaluationsdatensatz sowie bewusste Entscheidungen zu Identität, Datenschutz und echten Konnektoren.

Mein Ziel ist ein System, in dem KI bei Unsicherheit hilft, während Berechtigung, Nachweise und Wiederanlauf explizit bleiben.

**Repository:** https://github.com/AhadPorkar/bpmn-order-workflow  
**Autor:** Ahad Porkar  
**Status:** 1.1.0-rc.1, Portfolio-Referenz mit synthetischen Daten.
