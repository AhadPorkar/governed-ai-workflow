Ein KI-Assistent sollte seine eigenen Aktionen nicht freigeben dürfen.

Auf dieser Idee basiert mein neuer Release Candidate: eine Governed AI Workflow Platform für nicht lieferbare Bestellungen.

Der Assistent untersucht den Fall mit vier begrenzten Lesetools. Deterministische Regeln prüfen den Vorschlag. Eine andere Person kontrolliert die genaue Aktion. Erst danach darf ein Worker ausführen, nachdem er gespeicherte Freigabe und aktuelle Fakten erneut geprüft hat.

Der wichtigste Moment der Demo ist für mich keine flüssige Antwort.

Es ist ein Worker, der nach dem Commit, aber vor der Jobbestätigung abstürzt. Nach Serverneustart wiederholt ein neuer Worker die Anfrage und erhält denselben Beleg. Eine synthetische Erstattung. Kein zweiter Effekt.

Das Paket enthält Java-21-Code, eine deutsche und englische Prüfoberfläche, einen Ollama-Tool-Adapter, ein Spring-/Camunda-/Kafka-/PostgreSQL-Overlay und reproduzierbare Tests.

Auch die Grenzen sind dokumentiert: 106 Java-Tests und 29 echte HTTP-/Prozesstests bestanden. Neun Browser-Prüfungen nutzten einen offengelegten DOM-Harness. Externer Stack und echtes Modell brauchen noch ihre Abnahme. Geschäftseffekte bleiben in einer Sandbox.

Selbst die Schwächen der Baseline sind sichtbar: 33 exakte Vorschläge bei 48 gelabelten Fällen. Ein zulässiger Betrag ist nicht automatisch der Betrag, den der Kunde verlangt hat.

Mein Schwerpunkt ist nicht Autonomie um jeden Preis, sondern nachvollziehbare, wiederanlauffähige Automatisierung mit klarer Autorität.

Code und technische Dokumentation:
https://github.com/AhadPorkar/bpmn-order-workflow

#Softwareentwicklung #Java #Prozessautomatisierung #AgenticAI #Softwarearchitektur
