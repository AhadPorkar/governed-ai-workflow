# Security model and limitations

## Threat model

Customer text, model output, browser input and transport retries are untrusted. Application code, administrator configuration, the identity file, the database and Camunda control plane are trusted in this reference deployment. A compromised administrator or database is outside its claimed protection boundary.

| Threat | Implemented control | Remaining boundary |
|---|---|---|
| Prompt injection asks for a refund tool. | Model only receives four scoped read tools; executable tool names are rejected. | Model may still misunderstand intent; semantic evaluation and human review remain necessary. |
| Cross-tenant access. | Tenant derived from authenticated actor; reads and mutations recheck ownership. | Static identities are demo-only; production identity lifecycle is not implemented. |
| Browser or model sets `approved=true`. | Decision route rejects extra fields; worker reads persisted grant. | A privileged control-plane administrator remains powerful. |
| Creator approves own case. | Reviewer role plus different actor ID required. | Role administration and real personnel identity must be governed externally. |
| Amount/order/policy changes after approval. | Digest and current versions/expiry checked again at commit. | Rewriting the full trusted store defeats application-layer checks. |
| Duplicate delivery or lost acknowledgement. | Atomic ledger effect and stable receipt; retries reuse it. | A remote payment system requires independent idempotency and reconciliation. |
| HTML in customer/model text. | Escaped rendering, no HTML injection API; no remote assets; CSP headers. | Harness tested escaping; native browser CSP enforcement still needs acceptance. |
| Model selects an arbitrary URL. | Tool arguments empty; endpoint fixed by trusted configuration. | HTTPS administrator-configured endpoints still require an egress allowlist in production. |
| Resource exhaustion. | Body, parser, tool, context, round, time and token limits. | Local server is not slow-client/Internet hardened; no perimeter DDoS protection. |

## Model budgets

The Ollama adapter allows at most six model rounds, eight tool executions and 6,000 observed prompt-plus-output tokens. Each response is bounded to 65,536 bytes. Requests are capped at 48,000 serialized characters, with a default 45-second total deadline, a maximum 20-second request timeout, 768 generated-token hint and 8,192 context hint.

Observed token counts are checked after responses: this is not a prepaid token guarantee. Failed attempts may have consumed resources without returning billable usage. Counts absent from a provider response cause rejection rather than fabricated zero usage. Remote redirects are not followed. All provider failures result in escalation, not implicit fallback or execution.

## Approval is a precise grant

All consequential actions require a separate human, including low-value refunds. The grant binds the exact proposal and its evidence context. Explanation text is part of the digest; even a rationale change invalidates the grant. Canonical JSON is a project-local deterministic encoding, **not an RFC 8785 compliance claim**.

The policy is a Java implementation with a version identifier. It does not interpret a natural-language policy document as executable authority. Changing policy rules requires a deliberate version change and regression tests.

## Data protection and secrets

Only synthetic orders should be used in this release. Requests, rationale, state and audit details are persisted locally; storage encryption, deletion schedules, purpose limitation and a production privacy review are not implemented. Do not describe the project as GDPR-certified, EU-AI-Act compliant, penetration-tested or independently security-audited.

Tokens are cryptographically random; only SHA-256 hashes and expiry are loaded by the server. The generated convenience `access.json` contains plaintext demo tokens and must remain private. UI credentials live in JavaScript memory, not localStorage. Static token configuration is not OAuth/OIDC and does not implement SSO, MFA or account recovery. Rotate through an explicit restart.

The hash chain detects accidental/in-place audit inconsistency, not malicious full-chain rewriting. For stronger guarantees, add append-only access boundaries and external anchoring, then test restoration and verification.

## Reporting and production gate

Do not publish exploit payloads containing real credentials or customer data. Follow the repository owner's current private contact route; no unverified security email address has been invented here. Supply a minimal synthetic reproduction, affected commit and expected versus actual behavior.

Before public hosting: implement OIDC and least-privilege access, validate the entire host application's endpoints, enforce TLS and egress policy, add shared rate limiting and timeouts, apply retention/encryption requirements, review runtime/model licenses, and obtain a real deployment security assessment.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
