# Work Order: Vestige Design, Configuration And Tool Review

- ID: `20260924T001052Z-vestige-design-review`
- Created: `2026-09-24T00:10:52Z`
- Last Updated: `2026-09-24T00:37:10Z`
- Owner: YaCy dev builder
- Status: closed
- Outcome: succeeded

## Request And Definition Of Done

Review upstream README, GETTING-STARTED, SCIENCE, FAQ, CONFIGURATION and
TOOL-CONTRACTS. Use Sequential Thinking throughout to examine questions as
they arise. Compare documented design to installed version/configuration and
actual tool contracts. Explain automatic versus agent-owned responsibilities,
correct the earlier proposal where necessary, and recommend next steps. The user
subsequently authorized upgrading to the latest Vestige release; include a
consistent backup, isolated migration rehearsal, runtime verification and rollback.

## Authorized Scope And Boundaries

- Read-only external documentation and local configuration/version/health
  checks; project-local work-order and findings documentation.
- Upgrade authorization: user-local Vestige package and necessary migration/
  restart work only. Preserve unrelated Codex settings and shared memories.
- No memory cleanup, manual merging/purge, remote-server changes, sibling Git
  work, or implementation of the previous memory-workflow proposal.
- Do not expose credentials, private memory contents or raw sensitive logs.
- Preserve pre-existing dirty files and distinguish version-specific evidence.

## Checklist

- [x] [verified] Read the six requested sources with Sequential Thinking review.
- [x] [verified] Verify installed version, configuration and lifecycle behavior.
- [x] [verified] Compare actual tools and usage with versioned contracts.
- [x] [verified] Back up and stage the latest package; rehearse migration on a copy.
- [x] [verified] Upgrade, verify memory preservation and document rollback.
- [x] [verified] Document findings, revise recommendations and report limits.

## Current State And Next Action

- Vestige session_start works; health reports 123 memories and full embedding
  coverage. No useful configuration-specific memory was returned.
- Upstream pages opened. README assigns merging, contradiction detection and
  fading to Vestige, while agents supply and retrieve memories.
- Installed npm/binary version 2.3.0. npm latest is 3.0.0, also the latest
  numbered stable GitHub release. GitHub releases/latest points to a whitepaper,
  so do not use that endpoint as a software-version selector.
- Live process PID 806134 uses the npm native binary, with no relevant environment
  overrides. Open-file inspection locates the DB at ~/.local/share/core/vestige.db,
  not the Linux path printed in current documentation. No vestige.toml or review
  mode file was found alongside the database; session output is profile default.
- Health: embedding ready, 100% coverage, zero mismatches. Consolidation and dream
  timestamps exist. No Vestige-recorded backup timestamp; other backups unverified.
- Current main docs pinned to 3c5b198712d97b1f5350a04a4f2df5ed6cb2245b.
  Installed-release Science auto-strengthens recall; current main says audit-only.
  v2.3 auto consolidation merge defaults on; later versions default off.
- Backup directory: ~/.local/state/vestige-upgrades/20260924T001611Z/ (private).
  Contains package-2.3.0/, codex-config.toml and before-rehearsal.db.
- Staging: /tmp/vestige-upgrade-20260924T001611Z/. Official npm package integrity
  verified; installer verified release-binary SHA256. No client edits by installer.
- Rehearsal: upgrade --dry-run passed, applied migration to a separate copied DB,
  then probe.mjs verified MCP initialization, 15 advertised tools, embeddings,
  recall and session_start. SQLite integrity OK; 123 memories; zero missing or
  changed fact records (content/type/tags/source/validity/scope/supersession).
- Live installation and migration completed. Final backup is
  final-before-upgrade.db. Installed native version is 3.0.0; default storage
  resolves to the existing database. Codex config is byte-identical to backup.
- Live MCP probe passed: healthy, 123 memories, 100% embedding coverage, zero
  mismatches, known-workspace recall and session_start. SQLite integrity passed;
  comparison with final backup found zero missing or changed fact records.
- Probe process exited. No old MCP process remains. No YaCy server was touched.
- Next Action: user restarts Codex; next session confirms attachment to 3.0.0
  and uses its actual schemas. Review simpler policy recommendations before
  implementing any shared instruction changes; do not replay this upgrade.
- Resume: inspect recorded paths/processes before continuing; do not replay
  installation or migration without checking whether it already completed.

## Runbooks, Commands And Evidence

- Prior proposal: docs/memory-workflow-plan.md, not installed policy.
- Source repository: https://github.com/samvallad33/vestige
- Local configuration: relevant Vestige sections of ~/.codex/config.toml.
- Temporary upstream document cache: /tmp/vestige-review-20260924/.
- Tools: Vestige session_start/status, installed schemas, Sequential Thinking.
- Rehearsal evidence: /tmp/vestige-upgrade-20260924T001611Z/rehearsal-probe.json
  (aggregate metadata only; no memory content).
- Preserved probe script and both rehearsal/live aggregate reports in the
  private backup directory. See docs/vestige-review-and-upgrade-20260924.md.
- Applied commands: npm install --global vestige-mcp-server@3.0.0;
  vestige --data-dir /home/programmer/.local/share/core upgrade.
- Git checkpoint: uncommitted review artifacts; no commit or push requested.

## Decisions And Attempts

- Compare current main with installed release before calling differences bugs.
- A successful retrieval may change internal access metadata; no deliberate
  memory ingestion, edit or purge is included in this work.
- Sandbox `vestige stats` cannot open the DB writable; use MCP health for the
  initial read-only check. Do not interpret that sandbox error as DB damage.
- A cached raw main README conflicted with the live main snapshot. Pinned all
  six source files before relying on them. TOOL-CONTRACTS does not exist at v2.3.0;
  actual installed tool schemas govern current calls.
- Moderate graph indexing excludes npm bin/scripts; full indexing answered the
  installer inspection. No direct source fallback needed.
- Immutable read-only SQLite access validates the quiescent backup without
  requiring WAL sidecars in its protected directory. Never use immutable mode
  against the live changing database.

## Delegation

None.

## Closeout

- Closed: 2026-09-24T00:29:13Z
- Outcome: succeeded
- Verified results: six-source review and Sequential Thinking completed;
  staged and live 3.0.0 verification passed with all 123 fact records preserved.
- Limitations: Codex client restart and cross-session capture/recall test are
  still needed; no automatic claim that revised agent behavior is implemented.
- Follow-up / successor: user restart and review of simplified recommendations.
- Reusable knowledge saved: review/runbook and dated Codex extension note
  20260924T002913Z-vestige-3-upgrade-procedure.md under the user's standing
  capture request. No deliberate Vestige fact writes or proposed policy installed.

## Post-Closeout Verification

2026-09-24T00:37:10Z: user restarted Codex. The connected MCP advertises
catalogVersion 3.0.0 and 15 tools. Session initialization retrieved existing
memories; health confirmed the original 123 memories and 100% embedding coverage.
Under the standing capture request, smart_ingest saved the upgrade procedure as
2bdc27c5-2587-43e6-9475-5a4bd2cd21bf with an embedding. Immediate recall returned
it as the first result. This resolves the client-attachment follow-up; a second
restart of the newly saved fact was not tested and is not needed for this check.
No reinstall, memory cleanup, instruction changes, deployment or Git operation.
Next Action: ordinary work may resume; shared policy changes remain a separate
decision. Historical pre-restart limitations above are retained as dated evidence.
