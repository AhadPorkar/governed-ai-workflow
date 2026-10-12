# Governed AI Workflow Platform
## AI proposes. Policy authorizes. Workers execute.

**Ahad Porkar | 1.1.0-rc.1 | 12 October 2026 | English / Deutsch**

A portfolio reference implementation for one demanding question: how can a probabilistic assistant investigate an order exception without being allowed to approve its own actions?

The customer cannot receive an order. They send a free-text request. A planner reads a bounded, case-scoped snapshot and proposes a refund, cancellation, alternative supplier, or escalation. A deterministic policy rejects inadmissible proposals. A different human reviews the precise action. A worker rechecks the persisted grant and current facts before committing an effect.

This is not a universal agent builder. It is a narrow, explainable implementation with a polished operator console and reproducible failure evidence.

## Delivery boundary

The uploaded archive is `rechnungflow.zip`, containing the separate RechnungFlow invoice project. It was not changed. The requested target is `AhadPorkar/bpmn-order-workflow`; its public main branch was inspected, but could not be cloned and no immutable commit was resolved in this environment.

Accordingly, this delivery contains an **independently runnable module plus an additive integration overlay**, not a merged copy of the original repository. The overlay creates files and refuses overwrite conflicts. No existing order table, payment integration, out-of-stock branch, README, or Maven configuration is silently replaced.

The independently runnable application and its tests use a **sandbox ledger**. A refund changes synthetic captured/refunded balances; it does not contact a payment provider. Supplier availability is a per-order synthetic snapshot, not a global inventory reservation system.

## What is implemented

| Capability | Implementation |
|---|---|
| Investigation | Transparent keyword baseline, plus a native Ollama tool-calling adapter. |
| Authority | Versioned policy, strict proposal schema, integer EUR cents, immutable snapshot binding. |
| Human control | Independent reviewer, exact proposal digest, rationale, expiry, separate credentials. |
| Execution | Worker-only command, durable receipt, replay-safe sandbox effects, separate acknowledgement. |
| Recovery | Atomic file persistence, real process-kill test, concurrent retries, corruption failure. |
| Operator experience | English/German console, queue, evidence, audit, model usage, receipts, mobile layout. |
| Integration reference | Spring MVC boundary, PostgreSQL aggregate store, Kafka outbox relay, Camunda workers and BPMN. |
| Evidence | Java contracts, HTTP/process tests, explicit browser DOM harness, 48 labelled synthetic cases. |

The integration reference is **authored but not stack-validated here**. Maven, Docker, Camunda, Kafka, PostgreSQL and a live model runtime were unavailable. Browser navigation was blocked by environment policy; visual/DOM checks used an explicit harness instead of claiming native end-to-end coverage.

## The demonstration

Approve a precise refund, terminate the worker after the effect is committed but before acknowledgement, restart the server, and retry. The same receipt is returned and the synthetic order is refunded only once. This demonstrates a transactional idempotency boundary, not globally exactly-once distributed execution.

For a technical review, start with the architecture and test evidence. For a portfolio presentation, use the demo script and release dossier. Keep the release-candidate and sandbox labels visible.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
