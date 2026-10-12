# Operations and recovery runbook

## Operating modes

`baseline` makes no model requests and is appropriate for deterministic demonstrations. `ollama` performs real HTTP calls to the configured runtime; a failed call escalates the case. Mode changes do not retroactively re-plan an already-created proposal.

The local file store supports one server writer. Run any number of HTTP worker clients, but never start a second server against the same state directory. Use the PostgreSQL adapter for a multi-process reference deployment only after its concurrency tests have passed; its single-row locking is intentionally not production-scale architecture.

## Useful controls

| Control | Independent package | Spring overlay |
|---|---|---|
| HTTP port | `--port`, default 8095. | Existing host port, observed default 8081. |
| Planner | `--provider baseline\|ollama`. | `GOVERNANCE_PLANNER`. |
| Model | `--model`. | `GOVERNANCE_OLLAMA_MODEL`. |
| Endpoint | `--model-endpoint`. | `GOVERNANCE_OLLAMA_ENDPOINT`. |
| Auth | `.local/auth.json`; launcher rotation. | `GOVERNANCE_AUTH_FILE`; restart after rotation. |
| State | `.local/state`. | New `governance_state` table. |
| Feature enable | Local launcher is explicit. | `GOVERNANCE_ENABLED=false` by default. |
| Synthetic seed | Local launcher `--seed`, no reset. | `GOVERNANCE_SEED_DEMO=false` by default. |
| Allowed origin | Localhost/127.0.0.1 same configured port. | Explicit `GOVERNANCE_ALLOWED_ORIGIN`. |
| Event destination | Outbox retained locally; no broker. | `GOVERNANCE_EVENTS_TOPIC`, default `governance.events`. |

## Crash after commit, before acknowledgement

Create, plan and approve a synthetic case, then run:

```bash
python scripts/worker.py --once --crash-after-commit
```

The Java worker exits with code 73 after the API has committed its receipt. The Python wrapper reports the nonzero exit; that is intentional fault injection. Stop and restart the server **without rotating identities or deleting state**, then run:

```bash
python scripts/worker.py --once
```

The worker receives the same receipt and acknowledges it. Inspect `EXECUTION_REPLAYED`, one `EFFECT_COMMITTED`, and the unchanged refunded balance. The automated `python scripts/recovery_demo.py` performs this experiment in an isolated temporary server and writes a result under `build/demo/`.

This is an actual standalone worker-process crash, not a claim that the Camunda worker-kill acceptance test has run.

## Triage

| Symptom | Inspect / safe response |
|---|---|
| `AUTHENTICATION_REQUIRED` after a day. | Token expiry. Stop, explicitly rotate and restart; never lengthen expiry silently. |
| `PLANNING_IN_PROGRESS`. | Another planner owns a 120-second lease. Do not start an uncontrolled retry storm. Retry after the lease only when needed. |
| `APPROVAL_EXPIRED`, `ORDER_CHANGED`, `POLICY_CHANGED`. | Do not edit the digest. Obtain a new proposal and approval; for a terminated BPMN lifecycle, create a new case. |
| `BLOCKED`. | Inspect stable policy reasons, not only the model explanation. No execution job should be created. |
| `ESCALATED`. | Investigate ambiguity/provider failure manually. No automatic consequential fallback exists. |
| File-store startup integrity error. | Preserve the directory; inspect backup and evidence. Do not delete data to force startup. |
| Outbox pending in standalone mode. | Expected: the local demo has no broker relay. |
| Outbox pending in Spring mode. | Check broker availability, credentials, topic, Camunda endpoint and logs. Events may be redelivered. |
| Approved case does not progress in stack. | Verify outbox/message correlation and active workers. Do not run the standalone worker to mask the issue. |

The local queue endpoint lists `READY` jobs. A failed stale grant is marked failed and is not retried indefinitely. Correlation/event IDs, case IDs and receipt IDs are the useful investigation keys; do not log bearer tokens or full model prompts.

## Backup, restore and operational limits

For the local demonstration, stop the server and copy the complete state directory to protected storage. Restore into an isolated directory, preserve the original, start with matching identities, then inspect audit validity and a replayed receipt. A backup file existing is not evidence that restoration works.

The file store caps state at 64 MiB and the engine caps cases at 10,000. These are safety bounds, not capacity targets. Audit and outbox entries are not automatically pruned. The PostgreSQL adapter serializes transactions through a single aggregate and rewrites JSON; measure its limitations before considering normalization or per-case locks.

`GET /health` reports that the HTTP adapter is alive. It is not comprehensive database/broker/model readiness or an SLO. Queue age, lease age, expired grants, outbox lag, replay frequency and model failure rates should become monitored signals in a production design. Such dashboards and alerting are not claimed as implemented here.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
