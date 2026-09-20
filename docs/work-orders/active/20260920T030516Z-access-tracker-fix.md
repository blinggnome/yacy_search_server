# Work Order: AccessTracker Fix, Dev Validation And Upstream PR

- ID: `20260920T030516Z-access-tracker-fix`
- Created: `2026-09-20T03:05:16Z`
- Last Updated: `2026-09-20T03:24:11Z`
- Owner: YaCy dev builder
- Status: active
- Outcome: not yet determined

## Request And Definition Of Done

Implement the reviewed query-log fix, reproduce known failures with synthetic
fixtures, pass regression/build checks, deploy and validate on Server2 dev,
then open an isolated, documented upstream PR with synthetic failure examples.

## Authorized Scope And Boundaries

- User explicitly authorized local edits/tests, dev deployment/testing, and
  upstream PR creation after successful validation in the current request.
- Target only `/opt/yacy-dev`, `yacy-dev.service`, port 8091 on Server2.
- Preserve existing dev features and production logs; no standard-node/fleet
  deployment, no changes in sibling Git workspaces.
- Synthetic fixtures only in public tests/PR. Never stage private report,
  credentials, backups, dependencies, raw user queries, or generated artifacts.
- Preserve existing dirty instructions/documentation. Stage specific files.

## Checklist

- [x] [verified] Verify current upstream, reader design, test/build/deploy prerequisites.
- [x] [verified] Back up and implement narrow reader/writer changes with regression tests.
- [x] [verified] Demonstrate failing baseline and passing fix, compile and large-log checks.
- [x] [verified] Inspect dev state, back up runtime, deploy and test synthetic failure via HTTP.
- [x] [verified] Isolate upstream commit, document evidence, create PR and verify its scope.
- [ ] [running] Update changelog/topic docs, commit local checkpoints, close work order.

## Current State And Next Action

- Prior assessment: `../completed/20260920T025230Z-access-tracker-review.md`.
- Git branch `yacy-space-abuse-message`; prior dirty docs and private/untracked
  files remain. GitHub authentication is available.
- Current tests: 11 AccessTracker regressions fail on baseline; 19 tests pass on
  the fixed code including QueryParams/GenericFormatter. `ant compileTest` passed.
- Next Action: commit/push local documentation and code checkpoints, then close
  this work order. Dev tests/deployment and PR creation are complete.
- Resume check: inspect Git/worktrees and recorded remote backup/deploy state;
  do not redeploy or inject fixtures solely because tool output was lost.

## Runbooks, Commands And Evidence

- `docs/access-tracker-log-notes.md`, `AGENTS.md`, `docs/work-orders/README.md`.
- Evidence: ignored `backups/access-tracker-fix-20260920/` includes source/JAR
  backups, baseline-test.log, fixed-tests.log, compile-test.log and benchmark.
- `ant compileTest` required sandbox escalation for existing ~/.ivy2 cache.
- Dev baseline: active/enabled, MainPID 689646, HTTP200; regular yacy inactive
  and disabled. Baseline JAR SHA256 `080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9`.
- Remote backup `/opt/yacy-dev/backups/access-tracker-20260920T030516Z/` contains
  pre-change JAR/source and `queries.log.before-synthetic`. Do not overwrite.
- Probe `/tmp/yacy-access-tracker-http-probe.py`; secrets read locally on server,
  never printed. Initial query lacked `df`, yielding400 twice; not deployed yet.
- Correction: dev deployment completed03:24:06UTC; prior line describes the
  pre-deploy state. Final probe is `/tmp/dev_http_probe.py`, with `df=text_t`
  and `q.op=AND`. The default-OR baseline query matched7266593 records but returned
  zero rows; AND added to keep subsequent synthetic matching empty.
- Baseline live submission produced multiline records; timeline returned empty
  events and history topics disappeared. Source tests separately reproduce500's
  underlying NumberFormatException deterministically.
- Dev installed JAR SHA256 `cb979066b5a501d27440cab24b89c801d19068b7c793b8387a57e5685f20a42c`.
  Exactly4 AccessTracker class entries differ;1650 other ZIP entries unchanged.
- Dev new PID805810. Installation marker in backup directory records completion;
  do not rerun `/tmp/deploy-dev.sh`. Regular service remains disabled/inactive.
- Dev final checks: external8091 HTTP200; authenticated history and timeline200
  with synthetic history present; local search200; patched Solr query0 hits;
  one new physical log record contains all submitted query parts. Before-install
  history5496957 bytes is an unchanged prefix of current5497120-byte history.
- Initial readiness check near1minute failed; successful web check near3minutes.
  Startup delay was not a patch failure; use a longer initial delay for next run.
- Isolated worktree `backups/access-tracker-fix-20260920/upstream`, branch
  `fix/access-tracker-query-log`, based on upstream `e171a4a0e`.
- Git checkpoints: source/tests applied to worktree, uncommitted. Upstream
  application compiled; full `compileTest` fails in untouched Solr tests because
  its test classpath omits generated bridge JARs. `ant -lib lib compileTest`
  also fails (`includeantruntime=false`). Stop adjusting build configuration:
  explicitly compiled focused tests with `build/classes/java/main:lib/*:libt/*`
  and19 tests pass on current upstream. No unrelated build changes in PR.
- GitHub rejected the initial push for missing workflow scope. No PR was
  created on that attempt. Fork master has custom commits and was preserved.
- PR commit rebased onto shared ancestor `94e8ac3b5` as `3bb969ea4`; initial tested
  commit `785947528` was based on upstream `e171a4a0e`. `git merge-tree --write-tree
  upstream/master HEAD` returns `b1147d1e0abce3c1577c0d5f15da5423a428df24`, identical
  to tested785947528's tree. Only two source/test files differ in PR.
- Push succeeded. PR https://github.com/yacy/yacy_search_server/pull/831 created;
  API verified one commit, exactly two intended files and mergeable state.
  Description matches prepared synthetic examples (only extra terminal newline).
  PR marked ready for review. Local code commit `466e57146` created.

## Decisions And Attempts

| UTC time | Decision or attempt | Observed result / reason | Next step |
| --- | --- | --- | --- |
| 2026-09-20T03:05:16Z | Begin authorized fix/deploy/PR workflow | Prior investigation reproducible; scope explicitly authorized | Implement and validate |
| 2026-09-20 | Implement and test | 11 baseline failures, 19 passing fixed tests; full compile passed | Dev validation |
| 2026-09-20 | Large synthetic log under256MB heap | 1M rows/47MB; 3600 results62ms, 100000 results117ms, exact counts | Keep bounded buffered reads |
| 2026-09-20 | Dev synthetic API request | 400 twice: explicit Solr missing df error; reassessed and corrected to df=text_t | Retry corrected request once |
| 2026-09-20T03:24:06Z | Deploy AccessTracker-only JAR | Checksummed backup preserved; service restarted, newPID805810 | Delayed HTTP readiness and synthetic retest |

## Delegation

None. Apply local codebase MCP output trial; cache complete replies, omit only
verified duplicate JSON/navigation-only metadata, and preserve all source.

## Closeout

- Closed: not closed
- Outcome: pending
- Verified results: pending
- Limitations: pending
- Follow-up / successor: none yet
- Reusable knowledge saved: topic notes/work-order updates during work
