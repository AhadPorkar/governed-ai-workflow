# AI can investigate an exception. It should not inherit authority to act.

## Why I built a governed workflow reference

An unavailable order looks like a small business problem. It quickly becomes a systems problem: the customer may ask for money back, a replacement, or simply clarification. Inventory can change while a proposal is being reviewed. A worker can disappear after a commit. A message can arrive twice.

An assistant that produces a convincing answer does not solve those problems by itself. I wanted a portfolio reference that makes the boundaries visible: where uncertainty is useful, where rules are authoritative, who approves an action, and what happens after a failure.

The result is a release candidate for one scenario, not a general autonomous-agent platform.

## Four responsibilities, not one powerful agent

The investigation layer reads a case-scoped order snapshot, inventory, approved alternatives and policy. Its tools are read-only. It cannot select a different tenant, ask for arbitrary SQL or call a refund endpoint.

The policy layer validates a strict proposal. Amounts are integer Euro cents. A refund cannot exceed the remaining captured balance. Cancellation requires no outstanding captured payment and an appropriate fulfillment state. A supplier must be approved, in stock and no more expensive under the reference policy.

The review layer belongs to a different identity from the case creator. The reviewer sees the original request, exact action, supporting evidence and expiry, then records a reason. A generic 'yes' is not enough: the approval binds the precise proposal and its context.

The execution layer rechecks that stored approval, order version, policy version and expiry. The model and browser cannot turn a process variable into authorization.

## The difficult boundary is after the commit

The demonstration deliberately terminates a worker after the sandbox effect is committed but before the job is acknowledged. The server is restarted. A fresh worker retries.

The expected result is not a second refund. It is the same receipt.

The application commits the synthetic order update, receipt, audit record and outbox event together. A repeated execution reads that receipt. Job acknowledgement is separate, so its loss does not imply that the business effect should be performed again.

This is a transactional idempotency guarantee for the sandbox ledger. It is not globally exactly-once execution across a real payment provider. A real connector would need a provider idempotency key, a durable command record and reconciliation when the remote outcome is uncertain.

## Why the baseline is allowed to look imperfect

The project includes a native Ollama tool-calling adapter, but the reproducible local demonstration defaults to a transparent keyword baseline. That is intentional: anyone can examine the control flow without purchasing model access or pretending that a stub is an LLM.

A 48-case English/German dataset includes ambiguity, negation, partial amounts, quoted intent, restricted suppliers and policy-inadmissible requests. The baseline produced 33 exact proposals and 40 acceptable dispositions.

One important failure is a partial-refund request. Returning the remaining captured balance may satisfy the financial limit while still violating what the customer requested. Policy admissibility is not semantic correctness. This is precisely why the original request and exact amount stay visible to an independent reviewer.

The evaluation has not been turned into a marketing accuracy claim. It is a small development dataset, not an independent benchmark or a live-model result.

## Evidence has a scope

The recorded delivery contains 106 passing Java contracts and 29 passing tests against real HTTP/server/worker processes. Those cover roles, tenant isolation, approval tampering, stale facts, expired grants, concurrent retries and durable recovery. Separate repository checks validate packaging and structural contracts.

Nine UI checks run the actual JavaScript and CSS in Chromium. Native navigation was blocked in the execution environment, so an explicitly documented DOM harness bridged requests to the real Java server. That validates meaningful interactions and rendering, but not native browser networking or CSP enforcement.

The Spring/Camunda/Kafka/PostgreSQL overlay and its acceptance tests are included, but the external stack was not run in that environment. No real model quality result, production rollout, security certification or real customer refund is claimed.

## The engineering story I want the repository to tell

The interface matters because a reviewer should be able to understand the system, not just admire its architecture diagram. The English/German console exposes the queue, proposal, evidence, audit, usage and receipt. It shows the sandbox and planner mode openly.

The design decisions matter just as much. The single transactional aggregate makes the reference implementation inspectable but limits throughput. The hash-linked audit detects local inconsistency but is not tamperproof against a privileged full rewrite. Static expiring identities are useful for a local demonstration but are not production OIDC.

Those are not details to hide until an interview. They are the substance of the interview.

The next gates are a pinned host integration, real PostgreSQL/Camunda failure tests, native browser acceptance, a held-out model evaluation and deliberate decisions about identity, data protection and real connectors.

My goal is a system where AI can help with uncertainty while permission, evidence and recovery remain explicit.

**Repository:** https://github.com/AhadPorkar/bpmn-order-workflow  
**Author:** Ahad Porkar  
**Status:** 1.1.0-rc.1, synthetic-data portfolio reference.
