# Verification matrix and reproduction

## What was executed in this delivery

| Layer | Executed evidence | Interpretation |
|---|---|---|
| Java application contracts | 106 named tests, zero failures in the recorded run. | Policy, authorization, concurrency, state, JSON and mock-model protocol behavior. |
| Real HTTP/process tests | 29 tests, zero failures. | Actual Java server, authenticated requests, durable restart and independent worker process. |
| Browser DOM/visual checks | 9 tests passed in explicit harness mode. | Actual JavaScript/CSS in Chromium, with real Java API calls bridged through Python. |
| Repository checks | 23 tests, zero failures. | Packaging, docs, API/BPMN structure and installer safeguards. |
| Evaluation | 48 synthetic labelled cases. | Baseline quality experiment, not a passing security suite. |

Machine-readable JSON/JUnit XML and logs are under `evidence/tests/`. Environment limitations and the failed native navigation attempt are under `evidence/environment/`. The consolidated truth is `evidence/verification.json`; no line/branch coverage percentage is claimed.

## Important tests

The Java suite covers strict request/proposal parsing, excess/missing fields, allowed tools, scoped evidence, token expiry, role boundaries, concealed tenant access, self-approval, digest mutation, stale order/policy, grant expiry, planner failures, copy-on-write rollback, atomic file restart, writer locks and corrupted state. A 32-way concurrent execution test expects one receipt. A parameter sweep checks 500 refund amounts inside one named test; those inputs are not misrepresented as 500 independent test cases.

HTTP tests exercise the actual process and protocol: content types, invalid UTF-8, oversized bodies, missing/expired/incorrect-role tokens, extra identity fields, digest mismatch, origin checks, query rejection, stable errors and rate limiting. Twelve concurrent HTTP retries yield one effect. The crash test kills a real worker with exit 73 after commit, kills/restarts the server, and verifies one receipt, one refund and a replay attempt before acknowledgement.

Ollama tests serve controlled HTTP protocol fixtures. They verify tool loops, forbidden tool arguments, output/schema bounds and budget behavior. They do not prove any live model's competence or protection from every prompt injection.

## Browser limitation, explicitly recorded

Native navigation failed with `net::ERR_BLOCKED_BY_ADMINISTRATOR`. The environment policy was not altered. Harness mode injects the unmodified application assets into Chromium, supplies an explicit storage shim, and routes fetch calls through a local Python bridge to the real Java server.

That verifies rendering, interaction, escaping, labels, mobile horizontal fit and approval/rejection/receipt flows. It does **not** verify native navigation, browser fetch/CORS behavior, CSP enforcement or actual origin/localStorage isolation. Screenshots in the release are real rendered UI from that harness, with synthetic cases.

Native browser testing remains the default on user/CI machines. Run it without the harness flag before advertising browser end-to-end acceptance.

## Not executed / mandatory gates

The host Maven build, Spring endpoint runtime, real PostgreSQL Testcontainers tests, Kafka delivery, Camunda deployment/execution, broker/worker fault injection, a live Ollama model, Windows execution, production load, accessibility audit, penetration test and external payment/ERP integration were not verified here.

A valid BPMN XML graph is not proof of Camunda compatibility. Java API review is not a Maven compilation. A correctly returned receipt from the independent demo is not proof of a real refund. No CI badge or published GitHub run has been fabricated.

## Commands

```bash
python scripts/test.py --repository
python -m pip install -r requirements-browser.txt
python -m playwright install chromium
python scripts/test.py --browser --repository
python scripts/evaluate.py --no-build
```

To reproduce only the restricted-environment harness on a POSIX shell:

```bash
GOV_UI_TRANSPORT=harness python scripts/test.py --browser --repository
```

On PowerShell set `$env:GOV_UI_TRANSPORT = "harness"` explicitly. Do not describe the resulting browser checks as native end-to-end tests. Integration commands and additional stack gates are documented separately.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
