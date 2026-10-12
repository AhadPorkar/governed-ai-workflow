# HTTP API and contracts

The machine-readable OpenAPI 3.1 document is `openapi/governance.openapi.json` in the independent source package. The host overlay stores it at `docs/governed-ai/governance.openapi.json`.

The local base URL is `http://127.0.0.1:8095/api`. In the Spring adapter it is `http://localhost:8081/governance/api`. JSON bodies are limited to 16 KiB. Query parameters are not supported. Authenticated requests are limited to 120 per actor per minute, per process; this is not a distributed or anonymous-edge rate limiter.

## Endpoints

| Method and path relative to `/api` | Required role | Contract |
|---|---|---|
| `GET /health` | Public | Version, mode, planner; not dependency readiness. |
| `GET /session` | Authenticated | Server-derived identity, tenant, roles and planner. |
| `GET /dashboard` | READ | Current tenant's orders/cases/policy and audit validity. |
| `GET /cases/{id}` | READ | Case, evidence, audit; receipt after execution. |
| `POST /cases` | OPERATOR | Exact fields `orderId`, `request`; `Idempotency-Key` required. |
| `POST /cases/{id}/plan` | OPERATOR or WORKER | Empty JSON object; durable lease and bounded investigation. |
| `POST /cases/{id}/decisions` | REVIEWER | Exact fields `proposalDigest`, `approve`, `reason`. |
| `GET /jobs` | WORKER | Current tenant's ready execution jobs. |
| `POST /jobs/{id}/run` | WORKER | Empty JSON object; persisted grant always revalidated. |
| `POST /jobs/{id}/ack` | WORKER | Exact field `receiptId`; must match the committed receipt. |

Send `Authorization: Bearer <token>` and `Content-Type: application/json` for POST requests. The browser never sends a role or tenant selector. Adding an identity, tenant, approval or action field to an endpoint body is rejected rather than ignored.

## Create and plan

```http
POST /api/cases
Authorization: Bearer YOUR_OPERATOR_TOKEN
Idempotency-Key: example-order-1001-01
Content-Type: application/json

{"orderId":"ORD-1001","request":"Please refund my unavailable order."}
```

A successful create returns `201`. Reusing the same key for the same actor/tenant and exact request returns the existing case. Reusing it with a different payload returns `409 IDEMPOTENCY_CONFLICT`. Keys follow `[A-Za-z0-9][A-Za-z0-9._-]{0,99}`. Requests are nonblank and at most 4,000 Java string characters.

Plan the returned case ID with `{}`. A successful actionable proposal is not approved; its state is `PENDING_APPROVAL`. Evidence includes tool names and result hashes. Proposed money is integer cents, never a floating-point business amount.

```json
{
  "action": "REFUND",
  "amountCents": 12900,
  "supplierId": "",
  "explanation": "Propose returning the captured balance, subject to review.",
  "citations": ["POLICY-ORDER-EXCEPTION@2026-10-12.1"]
}
```

## Decide without changing the action

```json
{
  "proposalDigest": "COPY_THE_EXACT_64_CHARACTER_DIGEST_FROM_THE_CASE",
  "approve": true,
  "reason": "Verified the customer request, captured balance and exact action."
}
```

The displayed digest must be copied verbatim, not recomputed by the client. A decision does not accept a replacement amount. To change a proposal, obtain a new revision and a new approval through the supported lifecycle. A rationale is required for approval and rejection, at most 600 characters.

Approval expires 15 minutes after proposal creation, not 15 minutes after the reviewer clicks. Execution after expiry fails even when a timer has not yet updated the visible case status.

## Errors and repeat requests

Responses use a stable `type`, `title` and `status` shape. Expect `401` for missing/expired credentials, `403` for insufficient role or self-approval, concealed `404` for another tenant's case, `409` for stale state/grant/digest, `413` for oversized bodies, `415` for the wrong POST content type, and `429` for the actor limit. Transport-specific error titles can differ between JDK and Spring boundaries; assert invariant status/behavior, not undocumented prose.

Execution replay returns the existing receipt and records a replay audit event. Repeating a human decision is a state conflict, not a silently successful approval. The ready-job queue and committed effect are deliberately separate so acknowledgement loss is recoverable.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
