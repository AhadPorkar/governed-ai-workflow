# Start here

## What this delivery is

**Governed AI Workflow Platform 1.1.0-rc.1.** An independently runnable Java 21 governance sandbox plus 77 additive files for your actual `bpmn-order-workflow` repository.

The supplied `rechnungflow.zip` contains the invoice project. It was left unchanged. This delivery is not a merged copy of the BPMN repository and does not claim a pinned upstream commit.

## Run the reference

```bash
python scripts/run_demo.py
```

Requires a Java 21 JDK and Python 3.10+. Open `http://127.0.0.1:8095`. Credentials are generated in private `.local/access.json`; do not publish that directory. Create a synthetic case as the operator; approve the exact proposal with the independent reviewer. In another terminal run:

```bash
python scripts/worker.py --once
```

To replay the actual process-crash demonstration and regenerate local evidence:

```bash
python scripts/recovery_demo.py
python scripts/test.py --repository
python scripts/evaluate.py
```

Browser verification is a separate opt-in: see `docs/en/testing.md`. Never silently substitute the DOM harness for native-browser acceptance.

## Apply the overlay safely

From this package, against a clean branch of the actual BPMN repository:

```bash
python scripts/apply_overlay.py /path/to/bpmn-order-workflow --report build/overlay-dry-run.json
python scripts/apply_overlay.py /path/to/bpmn-order-workflow --apply --report build/overlay-applied.json
```

Read `docs/en/integration.md` first. The installer refuses the invoice project, overwrite conflicts, symlink destinations and migration-version collisions. Nothing is posted or pushed automatically. Do not blindly unzip the host-layout archive over a working tree.

## Read and publish

| Material | Entry point |
|---|---|
| English technical documentation | [Overview](docs/en/overview.md) - 13 chapters. |
| Deutsche technische Dokumentation | [Uebersicht](docs/de/overview.md) - 13 Kapitel. |
| Exact file changes | [File map](FILE-CHANGES.md) and `CHANGE-MANIFEST.json`. |
| Test scope and evidence | [Verification](evidence/verification.json), plus raw logs/JSON/XML. |
| English release dossier | [Seven-page PDF](publishing/Governed_AI_Workflow_Release_EN.pdf). |
| Deutsches Release-Dossier | [Siebenseitiges PDF](publishing/Governed_AI_Workflow_Release_DE.pdf). |
| English LinkedIn document | [Eight-slide PDF](publishing/LinkedIn_Governed_AI_EN.pdf) and editable `.pptx`. |
| Deutsches LinkedIn-Dokument | [Achtseitiges PDF](publishing/LinkedIn_Governed_AI_DE.pdf) und editierbare `.pptx`. |
| Posts, long articles and CV copy | `publishing/` in English and German. |
| Publication checklist | [Read before posting](publishing/PUBLISHING-CHECKLIST.md). |

## Evidence, not certification

167 checks passed: 106 Java, 29 real HTTP/process, nine explicit Chromium DOM-harness checks and 23 repository checks. The live process-crash demonstration produced one sandbox effect across two execution attempts, returning the same receipt after restart.

The 48-case deterministic baseline achieved 33 exact proposals and 40 acceptable dispositions. No real model was evaluated and no business effects were executed by that evaluation.

Host Maven/Spring/PostgreSQL/Kafka/Camunda integration, native-browser networking/CSP, live-model evaluation and production acceptance remain gates. Business effects are synthetic ledger updates, not payments, ERP writes or modifications of the original order tables.
