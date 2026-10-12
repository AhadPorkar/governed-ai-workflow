# Primärquellen und Herkunft

Die folgenden Quellen wurden am **12. Oktober 2026** eingesehen. Versions- und Lizenzangaben sind vor Veröffentlichung oder Betrieb erneut zu prüfen. Die Quellen begründen Integrationsentscheidungen, nicht die separat ausgeführten Testergebnisse.

| Quelle | Verwendung |
|---|---|
| [bpmn-order-workflow](https://github.com/AhadPorkar/bpmn-order-workflow) | Gewünschter Host und Architekturkontext. |
| [Host pom.xml](https://raw.githubusercontent.com/AhadPorkar/bpmn-order-workflow/main/pom.xml) | Beobachtete Java-/Spring-/Camunda-/Testcontainers-Versionen. |
| [Hauptanwendung](https://raw.githubusercontent.com/AhadPorkar/bpmn-order-workflow/main/src/main/java/io/github/ahadporkar/ordertocash/OrderToCashApplication.java) | Wildcard-Deployment und Scheduling. |
| [Bestehender Worker](https://raw.githubusercontent.com/AhadPorkar/bpmn-order-workflow/main/src/main/java/io/github/ahadporkar/ordertocash/process/InventoryWorkers.java) | Annotationen und BPMN-Fehler-API. |
| [Camunda Agentic Orchestration](https://docs.camunda.io/docs/components/agentic-orchestration/ai-agents/) | Konzeptuelle Trennung von Modellarbeit und Orchestrierung. |
| [Camunda-Laufzeitlizenzen](https://docs.camunda.io/docs/reference/licenses/) | Produktionslizenzgrenze unabhängig von der Quellcodelizenz. |
| [Ollama Tool Calling](https://docs.ollama.com/capabilities/tool-calling) | Natives Chat-/Tool-Protokoll. |
| [Checkout-Releases](https://github.com/actions/checkout/releases) | Aktionsversionen der CI-Vorlage. |
| [Setup-Java-Releases](https://github.com/actions/setup-java/releases) | Aktionsversionen der CI-Vorlage. |
| [Setup-Python-Releases](https://github.com/actions/setup-python/releases) | Aktionsversionen der CI-Vorlage. |

Camunda Self-Managed unterliegt im Produktionsbetrieb Laufzeitlizenzanforderungen. MIT-lizenzierter Anwendungscode erteilt keine Produktionsrechte für Camunda oder ein gewähltes Modell. Eine Rechtsberatung oder Konformitätszertifizierung ist nicht Teil der Lieferung.

`PROVENANCE.json` dokumentiert die Archivabweichung und die einzige wiederverwendete JSON-Hilfsklasse samt ursprünglichem SHA-256. Die Klasse stammt aus RechnungFlow und erhielt einen neuen Namespace; das Archiv selbst blieb unverändert. Private Arbeitgeberquellen oder Zugangsdaten wurden nicht übernommen.

Ein Upstream-Commit wurde nicht bestimmt oder fixiert. Der Installer protokolliert bei Anwendung den Hash der Ziel-`pom.xml`. Halten Sie vorher mit `git rev-parse HEAD` den tatsächlichen Integrationsstand fest.


---
[Überblick](overview.md) | [Schnellstart](quickstart.md) | [Architektur](architecture.md) | [Integration](integration.md) | [API](api.md) | [Sicherheit](security.md) | [Betrieb](operations.md) | [Tests](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Entscheidungen](decisions.md) | [Quellen](sources.md)
