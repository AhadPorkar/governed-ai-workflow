# File changes / Dateiaenderungen

**Release: 1.1.0-rc.1.**

The uploaded RechnungFlow archive is unchanged. This is an independent runnable package plus an additive overlay for the actual `bpmn-order-workflow` repository. No upstream commit has been resolved; it is not a merged checkout.

Das hochgeladene RechnungFlow-Archiv bleibt unveraendert. Geliefert werden ein eigenstaendig startbares Paket und ein additives Overlay fuer das tatsaechliche BPMN-Repository, kein zusammengefuehrter Checkout.

## Host changes / Aenderungen am Host

**77 added files. Zero modified existing files. Zero deletions.**

The safe installer is `scripts/apply_overlay.py`; it defaults to a dry run and checks the target project, overwrite conflicts, symlinks and Flyway V100 collisions. `integration/governance-overlay.patch` is an alternative new-files-only Git patch. It does not implement the installer's project and migration guards. Prefer the installer.

Every row below is an addition. SHA-256 values and byte lengths are in `CHANGE-MANIFEST.json`. `PACKAGE-MANIFEST.json` inventories the complete independent package, including tests, evidence, bilingual documents and publishing materials that are not all copied to the host.

| Source in delivery | Destination in actual host repository |
|---|---|
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Actor.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Actor.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/ApiRouter.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/ApiRouter.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Audit.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Audit.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Data.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Data.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/DemoFixtures.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/DemoFixtures.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/DeterministicPlanner.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/DeterministicPlanner.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/FileStateStore.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/FileStateStore.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/GovernanceEngine.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/GovernanceEngine.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/JdbcStateStore.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/JdbcStateStore.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Json.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Json.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/MemoryStore.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/MemoryStore.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/OllamaPlanner.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/OllamaPlanner.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Planner.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Planner.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Policy.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Policy.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Problem.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Problem.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/Proposal.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/Proposal.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/ReadTools.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/ReadTools.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/StateStore.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/StateStore.java` |
| `src/main/java/io/github/ahadporkar/ordertocash/governance/TokenAuth.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/TokenAuth.java` |
| `integration/src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceConfiguration.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceConfiguration.java` |
| `integration/src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceController.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceController.java` |
| `integration/src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceHttpBoundary.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceHttpBoundary.java` |
| `integration/src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceOutboxRelay.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceOutboxRelay.java` |
| `integration/src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceWorkers.java` | `src/main/java/io/github/ahadporkar/ordertocash/governance/spring/GovernanceWorkers.java` |
| `integration/src/main/resources/application-governance.yml` | `src/main/resources/application-governance.yml` |
| `integration/src/main/resources/bpmn/governed-order-exception.bpmn` | `src/main/resources/bpmn/governed-order-exception.bpmn` |
| `integration/src/main/resources/db/migration/V100__governed_ai_workflow.sql` | `src/main/resources/db/migration/V100__governed_ai_workflow.sql` |
| `integration/src/test/java/io/github/ahadporkar/ordertocash/governance/CoreReferenceTest.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/CoreReferenceTest.java` |
| `integration/src/test/java/io/github/ahadporkar/ordertocash/governance/PostgresGovernanceIT.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/PostgresGovernanceIT.java` |
| `tests/java/io/github/ahadporkar/ordertocash/governance/tests/AllTests.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/tests/AllTests.java` |
| `tests/java/io/github/ahadporkar/ordertocash/governance/tests/CoreTests.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/tests/CoreTests.java` |
| `tests/java/io/github/ahadporkar/ordertocash/governance/tests/ModelTests.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/tests/ModelTests.java` |
| `tests/java/io/github/ahadporkar/ordertocash/governance/tests/SecurityTests.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/tests/SecurityTests.java` |
| `tests/java/io/github/ahadporkar/ordertocash/governance/tests/StorageTests.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/tests/StorageTests.java` |
| `tests/java/io/github/ahadporkar/ordertocash/governance/tests/TestSupport.java` | `src/test/java/io/github/ahadporkar/ordertocash/governance/tests/TestSupport.java` |
| `web/app.js` | `src/main/resources/static/governance/app.js` |
| `web/icon.svg` | `src/main/resources/static/governance/icon.svg` |
| `web/index.html` | `src/main/resources/static/governance/index.html` |
| `web/styles.css` | `src/main/resources/static/governance/styles.css` |
| `docs/assets/login-de.png` | `docs/governed-ai/assets/login-de.png` |
| `docs/assets/login-en.png` | `docs/governed-ai/assets/login-en.png` |
| `docs/assets/workspace-de-mobile.png` | `docs/governed-ai/assets/workspace-de-mobile.png` |
| `docs/assets/workspace-de.png` | `docs/governed-ai/assets/workspace-de.png` |
| `docs/assets/workspace-en.png` | `docs/governed-ai/assets/workspace-en.png` |
| `docs/de/api.md` | `docs/governed-ai/de/api.md` |
| `docs/de/architecture.md` | `docs/governed-ai/de/architecture.md` |
| `docs/de/decisions.md` | `docs/governed-ai/de/decisions.md` |
| `docs/de/demo.md` | `docs/governed-ai/de/demo.md` |
| `docs/de/evaluation.md` | `docs/governed-ai/de/evaluation.md` |
| `docs/de/integration.md` | `docs/governed-ai/de/integration.md` |
| `docs/de/operations.md` | `docs/governed-ai/de/operations.md` |
| `docs/de/overview.md` | `docs/governed-ai/de/overview.md` |
| `docs/de/quickstart.md` | `docs/governed-ai/de/quickstart.md` |
| `docs/de/release.md` | `docs/governed-ai/de/release.md` |
| `docs/de/security.md` | `docs/governed-ai/de/security.md` |
| `docs/de/sources.md` | `docs/governed-ai/de/sources.md` |
| `docs/de/testing.md` | `docs/governed-ai/de/testing.md` |
| `docs/en/api.md` | `docs/governed-ai/en/api.md` |
| `docs/en/architecture.md` | `docs/governed-ai/en/architecture.md` |
| `docs/en/decisions.md` | `docs/governed-ai/en/decisions.md` |
| `docs/en/demo.md` | `docs/governed-ai/en/demo.md` |
| `docs/en/evaluation.md` | `docs/governed-ai/en/evaluation.md` |
| `docs/en/integration.md` | `docs/governed-ai/en/integration.md` |
| `docs/en/operations.md` | `docs/governed-ai/en/operations.md` |
| `docs/en/overview.md` | `docs/governed-ai/en/overview.md` |
| `docs/en/quickstart.md` | `docs/governed-ai/en/quickstart.md` |
| `docs/en/release.md` | `docs/governed-ai/en/release.md` |
| `docs/en/security.md` | `docs/governed-ai/en/security.md` |
| `docs/en/sources.md` | `docs/governed-ai/en/sources.md` |
| `docs/en/testing.md` | `docs/governed-ai/en/testing.md` |
| `PROVENANCE.json` | `docs/governed-ai/package/PROVENANCE.json` |
| `CHANGELOG.md` | `docs/governed-ai/package/CHANGELOG.md` |
| `LICENSE` | `docs/governed-ai/package/LICENSE` |
| `NOTICE.md` | `docs/governed-ai/package/NOTICE.md` |
| `scripts/stack_acceptance.py` | `scripts/governance-acceptance.py` |
| `scripts/credentials.py` | `scripts/governance-credentials.py` |
| `openapi/governance.openapi.json` | `docs/governed-ai/governance.openapi.json` |

## Manual integration decisions

The original stock-failure branch, original order tables, existing README, Maven build configuration and existing security configuration are not edited. README additions are supplied in `integration/README-addition.en.md` and `.de.md` for deliberate insertion. Run the host compile and stack acceptance gates before publishing a final integrated release.
