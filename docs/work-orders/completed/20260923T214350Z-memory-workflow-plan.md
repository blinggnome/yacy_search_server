# Work Order: Durable Cross-Session Memory Workflow Plan

- ID: `20260923T214350Z-memory-workflow-plan`
- Created: `2026-09-23T21:43:50Z`
- Last Updated: `2026-09-23T21:53:41Z`
- Owner: YaCy dev builder
- Status: closed
- Outcome: succeeded (planning only)

## Request And Definition Of Done

Use Sequential Thinking to design a durable memory workflow and proposed
AGENTS.md sections for current and future sessions. After a provisional
conclusion, explicitly review insufficiencies and omissions, revise the plan,
and return a concrete plan with verification and adoption criteria. Do not
claim that a finite review proves no further improvement is possible.

## Authorized Scope And Boundaries

- Planning, read-only inspection of relevant instructions/tool contracts and
  documentation, and workspace-local plan/work-order artifacts.
- No implementation of the proposed memory rules, server changes, memory
  cleanup, global/sibling configuration edits, or sibling Git operations.
- Preserve existing AGENTS.md/changelog edits, local reports and test JARs.
- Accuracy and safety are the user's capture criteria; do not reintroduce an
  importance or expected-reuse gate. Respect actual session/tool constraints.

## Checklist

- [x] [verified] Verify current instructions, store roles and tool behavior.
- [x] [verified] Develop initial workflow using Sequential Thinking.
- [x] [verified] Challenge sufficiency and omissions; revise with evidence.
- [x] [verified] Write proposed sections, rollout and acceptance tests; report.

## Current State And Next Action

- Vestige session_start succeeds and retrieves the older September 20 policy;
  today's project wording and dated Codex notes contain newer clarifications.
- Sequential Thinking completed twelve steps:
  provisional conclusion, explicit sufficiency and omission challenges,
  counterexamples, revisions, and a second review.
- Verified CLI version 0.155.1 and existing SessionStart configuration. It
  already reminds about retrieval on startup/resume/clear/compact, but not
  capture verification. Duplicate code-graph reminders are present; no edit.
- Official instruction-loading and hook docs support versioned bootstrap,
  profile/override checks and trusted lifecycle reminders. Hook coverage in
  this exact tool path remains an implementation-stage canary, not established.
- Tool schemas distinguish smart merge, per-item batch outcomes, retention,
  explicit GC preview, and reversible supersession. No maintenance was run.
- Proposal: [Reliable memory workflow](../../memory-workflow-plan.md), including
  exact shared AGENTS text, store roles, delivery states, fallback, ownership,
  rollout, rollback and twenty behavioral acceptance scenarios.
- Next Action: user reviews the proposal; implementation is a separate task.
- Resume: read this record and the user's latest request; remain plan-only.

## Runbooks, Commands And Evidence

- AGENTS.md; docs/dev-node-change-log.md; Codex managed-memory extension notes
  dated 20260923T154027Z and 20260923T213138Z; global Codex AGENTS.md.
- Vestige session_start/recall/status and installed tool schemas; Sequential
  Thinking for design reviews. No memory write/delete or service operation.
- Git checkpoint: planning artifacts left uncommitted, alongside the protected
  pre-existing dirty files. No staging, commit, push or sibling Git operation.
- Read back the full proposal; git diff --check reports no whitespace issues.
  The untracked-file no-index check also emitted no whitespace diagnostics
  (exit 1 denotes that the new file differs from /dev/null).

## Decisions And Attempts

- Distinguish stored facts, project instructions, current task state and code
  graph. A successful memory lookup is not proof of current truth or authority.
- Inspect real maintenance contracts; do not assume aging implies automatic
  safe deletion or that project instructions can override runtime constraints.
- Acknowledged saves and local extension-note writes are separate from verified
  retrieval and managed-registry indexing. Do not claim automatic synchronization.
- Hooks provide reminders/observability, not proof that every discovery was
  noticed. Never auto-ingest transcripts or execute remembered commands.
- Keep one shared contract with scoped project adapters; local owners manage
  sibling Git. Tests must exercise behavior, not only policy recitation.

## Delegation

None. The iterative reviews were performed with Sequential Thinking.

## Closeout

- Closed: `2026-09-23T21:53:41Z`
- Outcome: succeeded; requested plan and iterative reviews completed
- Verified results: current configuration/tool contracts inspected, official
  references checked, twelve-step review completed, proposal read back
- Limitations: proposed rules/hooks are not installed; fresh-session and
  multi-workspace behavioral tests are planned, not passed
- Follow-up: user review of the proposed workflow
- Reusable knowledge: plan and work-order records; no memory writes in this task
