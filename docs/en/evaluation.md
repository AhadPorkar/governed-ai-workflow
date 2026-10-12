# Evaluation: publish the weaknesses too

## Dataset and metrics

`evaluation/cases.json` contains **48 manually labelled synthetic cases** in English and German. The dataset includes ordinary refund/cancellation/alternative requests, ambiguity, negation, quoted words, partial amounts, specific-supplier constraints, paid/shipped orders, unsupported currency and injection-like requests.

The labels were written as expected proposals and acceptable dispositions before running the baseline. This is a small development dataset, not an independent held-out benchmark. It contains no private customer conversations.

**Exact proposal match** requires the expected action, integer amount and supplier identifier. Explanation wording is not scored. **Acceptable disposition** checks whether the resulting state belongs to the label's accepted set. A policy-blocked request may be safely rejected even when the model proposed the wrong thing. Neither metric is end-to-end business-task completion.

## Recorded baseline result

| Metric | Result |
|---|---|
| Cases | 48. |
| Exact proposals | **33 / 48 = 68.75%**. |
| Acceptable dispositions | **40 / 48 = 83.33%**. |
| Model calls / observed model tokens | 0 / 0. |
| Effects approved or executed | 0. |
| Model-cost estimate | Not applicable; no model was called. |

The keyword baseline commonly fails on partial refund amounts, quoted mentions of an action, nuanced negation, synonyms and restrictions such as 'only this supplier'. For example, a request for only EUR 25 may cause it to propose the remaining captured balance. That is **not** an acceptable intent inference merely because the amount is below the balance limit. An independent reviewer must catch it; a real-model comparison should improve it.

The lower exact-match result for policy-inadmissible requests is also meaningful: the baseline may propose a refund and be blocked, whereas the label expects escalation. Publish both metrics rather than hiding the difference behind a single safety score.

## Reproduce or compare

```bash
python scripts/evaluate.py
python scripts/evaluate.py --provider ollama --model YOUR_INSTALLED_MODEL
```

Each run records dataset SHA-256, dataset version, provider, Java/OS metadata, per-case expectation and actual proposal/state, latency, usage and failure. Results go to `build/evaluation/`; delivery snapshots are in `evidence/evaluation/`.

Latency here is a single-process synthetic measurement without controlled warmup or load isolation. The JSON includes p50/p95 as observed values, not production performance commitments. Do not compare a keyword baseline's speed with a real model without reporting hardware, model parameters, warmup and context lengths.

The adapter can calculate a provider-price estimate when rates are supplied programmatically; the shipped launcher does not invent rates. Local-model API cost is not the same as zero infrastructure or electricity cost. Failed provider calls may lack reliable token accounting.

## Before making AI-quality claims

Choose and record the exact model/version and license. Add a separately held-out dataset, ideally reviewed independently. Run the same policy and approval boundary for both planners. Report category-level failures, unsupported/invalid output, tool misuse, abstention, exact intent/amount match, latency and resource usage.

Measure human-review burden and successful final outcomes in a controlled user study before calling it task success. This delivery supplies an evaluation harness and a transparent baseline; **no real-model result is claimed**.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
