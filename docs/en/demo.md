# Demonstration script

## Opening

Show the console with the sandbox and baseline labels visible. Say: 'This reference explores how to use AI assistance without delegating business authority to the model. Today the deterministic baseline makes the control flow reproducible; a real Ollama adapter is available, but no real-model quality result is being claimed.'

## Act 1: a request is not permission

Sign in as operator. Select `ORD-1001`, submit `My headset cannot be delivered. Please refund the captured payment.`, and investigate. Show the customer text, policy citation, tool evidence, amount and expiry. Point out that there is no execute button and the operator cannot approve their own proposal.

Use a second case with `ORD-1003` and a refund request to show the shipped-order policy block. Use an unclear request on `ORD-1004` to show escalation. Explain why a model's confident explanation would not change these authorization requirements.

## Act 2: review the exact action

Sign out, then use the separate reviewer token. Open the first case. Compare the original request against the exact amount, order and evidence. Enter a meaningful reason and approve. Show the state transition and the audit event.

Explain that the digest binds the exact proposal, current order version, current policy and expiry. Changing facts does not merely change a screen; it invalidates the grant.

## Act 3: crash at the difficult moment

Run `python scripts/worker.py --once --crash-after-commit` in another terminal. Show the intentional worker failure. Restart the server without changing its state directory. Run `python scripts/worker.py --once` and refresh.

Show one receipt, one committed effect, the replay event and acknowledgement. Explain that this is a sandbox database effect, not an assertion of exactly-once behavior across a remote payment provider.

## Act 4: demonstrate evidence, not claims

Open `evidence/verification.json` and the named tests. Show the baseline's imperfect 33/48 exact proposal result. Explain the distinction between mock protocol tests, real-model evaluation and real Camunda integration acceptance.

Switch the interface to German and show the responsive view. End with the architecture trade-off: the current single-aggregate persistence makes the transactional boundary easy to inspect; scaling it requires a different storage design and new tests.

## Recording checklist

Use only synthetic data. Hide tokens and `.local/access.json`. Do not show a fake CI badge or imply a real provider refunded a customer. Crop screenshots only for readability, not to remove the sandbox/baseline or release-candidate labels. Replace the publication link with a real release URL after you publish.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
