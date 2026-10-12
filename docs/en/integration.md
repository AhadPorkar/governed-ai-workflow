# Integration overlay for bpmn-order-workflow

## Baseline and compatibility

The publicly inspected baseline uses Java 21, Spring Boot 3.5.16, Camunda 8.8.40, PostgreSQL, Kafka and Testcontainers 2.0.5. These are observed repository versions, not a claim that they are the newest versions. The application already deploys `classpath*:bpmn/*.bpmn` and enables scheduling. Worker imports follow `io.camunda.client.annotation.JobWorker` in that baseline. See the dated primary-source list.

The new planner is a custom external loop, not a Camunda 8.10 native AI connector transplanted into an 8.8 application.

## Apply deliberately

Use a clean branch of your actual BPMN repository. From the independent source package:

```bash
python scripts/apply_overlay.py /path/to/bpmn-order-workflow --report build/overlay-dry-run.json
python scripts/apply_overlay.py /path/to/bpmn-order-workflow --apply --report build/overlay-applied.json
```

On Windows, quote a path such as `"D:\Projects\bpmn-order-workflow"`.

The installer verifies the root Maven artifact ID, rejects RechnungFlow, rejects symlink destinations, refuses overwriting different files, and detects a competing Flyway `V100__...` migration. It preflights all changes and removes files newly created by this invocation if copying fails. It does not claim resilience to hostile concurrent filesystem mutation or atomic deployment across the entire checkout. Do not run it against a working tree being modified concurrently.

The change manifest describes every delivered source-to-target mapping. Existing files are modified **zero** times and deleted **zero** times. Review `git status` and `git diff --stat` afterward. README additions are supplied separately for deliberate insertion; they are not auto-applied.

## Added application surface

| Addition | Location / purpose |
|---|---|
| Core | `src/main/java/.../governance/`, no third-party dependencies. |
| Spring adapter | `.../governance/spring/`, opt-in beans and bounded HTTP transport. |
| BPMN | `src/main/resources/bpmn/governed-order-exception.bpmn`. |
| SQL | `V100__governed_ai_workflow.sql`, new independent `governance_state` table. |
| UI | `src/main/resources/static/governance/`. |
| Configuration | `application-governance.yml`, profile and explicit enable switch. |
| Tests | Core JUnit bridge and `PostgresGovernanceIT`; database tests require Docker. |
| Documentation | `docs/governed-ai/`, including EN/DE guides. |

No extra Maven dependencies are intentionally required beyond the inspected host baseline. **The full host Maven compile is still a mandatory gate.** Do not infer that source-level review proves external API compatibility.

## Configure and run a disposable stack

Inside the host checkout, generate identities outside version control:

```bash
python scripts/governance-credentials.py /private/path/governance-demo
```

Set these environment variables using your shell:

```text
SPRING_PROFILES_ACTIVE=governance
GOVERNANCE_ENABLED=true
GOVERNANCE_SEED_DEMO=true
GOVERNANCE_AUTH_FILE=/private/path/governance-demo/auth.json
GOVERNANCE_ALLOWED_ORIGIN=http://localhost:8081
GOVERNANCE_PLANNER=baseline
```

Start the host's documented PostgreSQL/Kafka/Camunda environment, then run its Maven wrapper (`./mvnw spring-boot:run`, or `mvnw.cmd spring-boot:run` on Windows). Open `http://localhost:8081/governance/index.html`.

The default feature switch is off. The SQL migration and existing application's wildcard BPMN deployment still run as part of their normal host lifecycle when resources are present; disabled means no governance beans/relay/workers, not that these resource files vanish.

For Ollama use `GOVERNANCE_PLANNER=ollama`, `GOVERNANCE_OLLAMA_MODEL`, and optionally `GOVERNANCE_OLLAMA_ENDPOINT`. Never enable real-data ingestion merely by changing that flag.

## Acceptance commands

```bash
./mvnw verify
python scripts/governance-acceptance.py --access /private/path/governance-demo/access.json --confirm-synthetic
```

The smoke check deliberately does not call `/plan` or `/run`; it waits for automatic progression. It requires a fresh synthetic `ORD-1001` and fails on an already-refunded seed order. Inspect the Camunda instance and Kafka records independently. Fault-inject a Camunda worker between effect commit and job completion, then verify the same receipt. Run the PostgreSQL concurrency/rollback tests with Docker. These steps were not run in the delivery environment.

Do not run the standalone worker against the Spring/Camunda instance during this acceptance check. It would mask broken orchestration by draining the same logical jobs.

## Deliberate remaining work

The original stock-failure branch is **not rewired**. A future trusted boundary must map an actual order snapshot, verified captured payments and inventory facts into governance; a `paymentId` alone is not proof of a captured amount. Real ERP/payment writes, OIDC, tenant-scoped database design, multi-replica relay claiming, rate limits shared across replicas, and operational monitoring remain release gates.

Existing host APIs remain under their existing security model. The added governance filter does not secure the entire application. Camunda access is privileged control-plane access and must be protected separately.


---
[Overview](overview.md) | [Quickstart](quickstart.md) | [Architecture](architecture.md) | [Integration](integration.md) | [API](api.md) | [Security](security.md) | [Operations](operations.md) | [Testing](testing.md) | [Evaluation](evaluation.md) | [Release](release.md) | [Demo](demo.md) | [Decisions](decisions.md) | [Sources](sources.md)
