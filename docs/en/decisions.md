# Architecture decisions

## ADR-001: proposals and authority are separate

**Decision:** the planner returns an untrusted proposal; policy, review and execution are separate application operations. **Reason:** natural-language confidence cannot grant business permission. **Trade-off:** more state and a slower user journey than direct tool execution. **Consequence:** all consequential actions require a persisted, independently reviewed grant.

## ADR-002: one narrow scenario

**Decision:** only unavailable-order refund, unpaid cancellation, approved supplier substitution and escalation. **Reason:** meaningful recovery/security tests are more valuable than a broad tool catalogue. **Trade-off:** no generic workflow builder, returns process, tax calculation, FX or multi-line order fulfillment. **Consequence:** out-of-scope situations escalate or are blocked.

## ADR-003: synthetic business effects first

**Decision:** commit effects to an independent sandbox ledger. **Reason:** existing order/payment facts were not supplied as a verified integration source, and a fake external 'refund success' would misrepresent the implementation. **Trade-off:** no real ERP/payment integration. **Consequence:** receipts explicitly mark `sandbox=true`; real connectors require their own idempotency/reconciliation protocol.

## ADR-004: transactional aggregate as the reference persistence model

**Decision:** implement memory, atomic file and PostgreSQL single-row aggregate adapters. **Reason:** one auditable transaction holds the effect, receipt, audit and outbox. **Trade-off:** serialization, whole-aggregate rewriting and bounded capacity. **Consequence:** benchmark and redesign before claiming production throughput; do not advertise an event-sourcing platform.

## ADR-005: local baseline plus bounded native Ollama adapter

**Decision:** deterministic tests use a transparent keyword baseline and protocol fixtures; real model usage is explicit. **Reason:** reproducibility without cloud credentials, while retaining actual tool-call integration code. **Trade-off:** baseline semantics are weak and real-model evaluation remains open. **Consequence:** publish failures and label provider mode in the UI.

## ADR-006: approval binds exact immutable context

**Decision:** digest the tenant, case, order, versions, request hash, proposal and expiry. **Reason:** a generic 'approved' flag can be reused against changed facts. **Trade-off:** stale proposals need a new review. **Consequence:** the UI never edits an approved amount; execution validates current facts again.

## ADR-007: at-least-once transport, idempotent effect

**Decision:** accept repeated jobs/events and return a stable receipt for a committed case. **Reason:** crashes leave uncertain acknowledgements. **Trade-off:** extra deduplication and audit noise. **Consequence:** no distributed exactly-once claim; outbox consumers and real connectors must implement their own boundaries.

## ADR-008: disclose integration and browser verification gaps

**Decision:** ship the useful module with an explicit release-candidate matrix rather than inventing stack or model results. **Reason:** the uploaded archive is a different project, external dependencies cannot be executed here, and native navigation is blocked. **Trade-off:** final integrated release requires owner-run gates. **Consequence:** baseline tests, harness checks and unexecuted acceptance tests remain separate evidence categories.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
