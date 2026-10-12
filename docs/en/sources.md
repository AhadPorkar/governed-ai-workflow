# Primary sources and provenance

All external references below were inspected on **12 October 2026**. Version and licensing statements describe the referenced material on that date; validate them again when publishing or deploying. These references inform the integration design, not the independently executed test results.

| Source | Purpose |
|---|---|
| [bpmn-order-workflow](https://github.com/AhadPorkar/bpmn-order-workflow) | Intended host, public README and architecture context. |
| [Host pom.xml](https://raw.githubusercontent.com/AhadPorkar/bpmn-order-workflow/main/pom.xml) | Observed Java/Spring/Camunda/Testcontainers versions. |
| [Host application](https://raw.githubusercontent.com/AhadPorkar/bpmn-order-workflow/main/src/main/java/io/github/ahadporkar/ordertocash/OrderToCashApplication.java) | Existing wildcard deployment and scheduling. |
| [Host worker](https://raw.githubusercontent.com/AhadPorkar/bpmn-order-workflow/main/src/main/java/io/github/ahadporkar/ordertocash/process/InventoryWorkers.java) | Current annotation and BPMN-error imports. |
| [Camunda agentic orchestration](https://docs.camunda.io/docs/components/agentic-orchestration/ai-agents/) | Conceptual separation of model-driven work and orchestration; not a claim to use newer native connectors. |
| [Camunda runtime licensing](https://docs.camunda.io/docs/reference/licenses/) | Runtime production-license boundary, separate from the application source license. |
| [Ollama tool calling](https://docs.ollama.com/capabilities/tool-calling) | Native chat/tool-call protocol and tool-result messages. |
| [Checkout action releases](https://github.com/actions/checkout/releases) | CI template action release selection. |
| [Setup Java releases](https://github.com/actions/setup-java/releases) | CI template action release selection. |
| [Setup Python releases](https://github.com/actions/setup-python/releases) | CI template action release selection. |

Camunda Self-Managed production use has runtime licensing requirements. Publishing application code under MIT does not grant a production license to Camunda or a chosen model. No legal compliance opinion or certification is provided. Review the exact runtime/model terms for your use case.

`PROVENANCE.json` records the attachment mismatch and the one reused JSON utility, including its original SHA-256. The utility came from the supplied RechnungFlow archive and was moved into the governance namespace; the archive itself was not modified. No private employer code or credentials were imported.

The upstream main-branch commit was not resolved or pinned. The overlay installer records the target `pom.xml` hash at application time; record `git rev-parse HEAD` yourself before applying it to create a real integration baseline.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
