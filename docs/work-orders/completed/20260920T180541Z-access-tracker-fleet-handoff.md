# Work Order: AccessTracker On-Demand Fleet Handoff

- ID: `20260920T180541Z-access-tracker-fleet-handoff`
- Created: `2026-09-20T18:05:41Z`
- Last Updated: `2026-09-20T18:15:00Z`
- Owner: YaCy dev builder
- Status: closed
- Outcome: succeeded

## Request And Definition Of Done

Create a complete node-provisioner handoff for applying the latest AccessTracker
patch when a standard node exhibits the history-page failure. Provide a bounded
patch, preflight, preservation of failure evidence/history, installation,
verification and rollback. Do not deploy to fleet nodes during this task.

## Authorized Scope And Boundaries

- Local handoff, portable patch artifact, preparation helper and documentation.
- Copy handoff/artifact to the provisioner's workspace; no sibling Git changes.
- No remote installation, restart, query submission, history deletion or index
  modification. Exclude Server2/dev from standard-node instructions.
- Request: user asked for future on-demand live-failure validation, not rollout.

## Checklist

- [x] [verified] Review implementation record, backups and existing handoffs.
- [x] [verified] Verify isolated artifact and baseline/dependency requirements.
- [x] [verified] Write complete runbook and validate offline preparation/checks.
- [x] [verified] Update changelog, copy handoff and close work order.

## Current State And Next Action

- Current state: handoff and checksummed portable artifact prepared. Exact
  baseline/fixed hashes match the prior dev deployment; 19 tests pass against
  the packaged JAR. Five local installer simulations pass; no remote changes.
- Next Action: none for builder; provisioner selects a live failing node and
  follows the handoff when ready. No rollout was launched.
- Resume check: inspect this record and bundle hashes; do not deploy remotely.

## Runbooks, Commands And Evidence

- `docs/access-tracker-log-notes.md`
- `docs/work-orders/completed/20260920T030516Z-access-tracker-fix.md`
- `backups/access-tracker-fix-20260920/yacycore.dev-before.jar`
- `backups/access-tracker-fix-20260920/yacycore.dev-patched.jar`
- `git status --short --branch`: only pre-existing AGENTS/report/handoff/JAR
  changes at start. These are excluded from this task's Git checkpoint.
- New ignored evidence: `backups/access-tracker-handoff-20260920/`.
- Portable archive SHA256:
  `39503bc6f7b36b2f5abc799ad1d14e1320c39c8cbea1ff90336ecb5fe2bc2a3a`.
- `java -cp 'backups/access-tracker-fix-20260920/yacycore.dev-patched.jar:test/java:lib/*:libt/*' org.junit.runner.JUnitCore net.yacy.search.query.AccessTrackerTest net.yacy.search.query.QueryParamsTest net.yacy.cora.date.GenericFormatterTest`: 19 passing tests.
- `python3 backups/access-tracker-handoff-20260920/verify_install_recipe.py`:
  success, unknown-JAR, unknown-source, start-failure and missing-log all pass.
- Handoff shell blocks checked with `bash -n`; embedded Python parsed with AST;
  archive/member SHA256 checks pass. Four changed class entries, 1,650 unchanged.
- Copied `FLEET_ACCESS_TRACKER_FIX_HANDOFF.md` into the provisioner's workspace
  root; SHA256 `ca00a5bf20d2a3fae1875865bb1eb372358ebdc1accb5810cd550ca9ee2c4dab`
  matches the builder copy. Archive copied to provisioner
  `pyinfra/files/access-tracker-20260920.tar.gz`; hash matches the value above.
- Git checkpoint: subject `Document on-demand AccessTracker fleet repair`;
  excludes all pre-existing changes and generated/ignored artifacts.

## Decisions And Attempts

| UTC time | Decision or attempt | Observed result / reason | Next step |
| --- | --- | --- | --- |
| 2026-09-20T18:05:41Z | Use class-only overlay, not full dev JAR | Existing dev record verified only four AccessTracker entries changed | Add baseline checks and verify dependencies |
| 2026-09-20 | Package exact tested overlay JAR with strict whole-baseline hash gate | Provisioner runbook records the same old fleet hash; any different baseline must stop | No generic overwrite or automatic dependency changes |
| 2026-09-20 | Offline command verification | 19 JUnit tests and five service-mocked install/recovery scenarios pass | Copy only handoff and non-sensitive archive |
| 2026-09-20 | Copy to provisioner | Handoff/archive copies match SHA256; no sibling Git changes | Provisioner owns future per-node live-failure validation |

## Delegation

Provisioner owns future target selection, deployment, monitoring and its own Git.

Codebase output trial: the focused graph replies were small; kept normal output.
An over-narrow file-pattern search missed readLog; qualified-name search resolved
it without broad source scanning or unnecessary re-indexing.

## Closeout

- Closed: `2026-09-20T18:15:00Z`
- Outcome: succeeded
- Verified results: portable payload and full handoff delivered; artifact checks,
  focused tests, syntax checks and local installer simulations passed.
- Limitations: live failure resolution remains a future per-node validation;
  only the pinned baseline is approved for this artifact. No remote actions.
- Follow-up / successor: provisioner after handoff, one affected node at a time.
- Reusable knowledge saved: `FLEET_ACCESS_TRACKER_FIX_HANDOFF.md`, changelog and
  `docs/access-tracker-log-notes.md`; no managed-memory write requested.
