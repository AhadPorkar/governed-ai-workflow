An AI assistant should not be able to approve its own actions.

I have built a release-candidate reference around that idea: a Governed AI Workflow Platform for unavailable-order exceptions.

The assistant investigates through four scoped, read-only tools. Deterministic rules validate its proposal. A different person reviews the exact action. Only then may a worker execute it, after checking the saved approval and current facts again.

The demonstration I care about most is not a fluent answer.

It is a worker that crashes after committing an effect but before acknowledging the job. After a server restart, a fresh worker retries and receives the same receipt. One synthetic refund. No second effect.

The package includes Java 21 code, an English/German review console, an Ollama tool-calling adapter, a Spring/Camunda/Kafka/PostgreSQL integration overlay, and reproducible tests.

I am publishing the boundaries too: 106 Java tests and 29 real HTTP/process tests passed; nine browser checks used an explicitly disclosed DOM harness. The external stack and a live model still require acceptance. Business effects remain in a sandbox ledger.

Even the baseline's weaknesses are visible: 33 exact proposals from 48 labelled cases. A safe-looking amount is not necessarily the amount the customer asked for.

My focus is not autonomous action at any cost. It is inspectable, recoverable automation with explicit authority.

Code and implementation notes:
https://github.com/AhadPorkar/bpmn-order-workflow

#SoftwareEngineering #Java #WorkflowAutomation #AgenticAI #SoftwareArchitecture
