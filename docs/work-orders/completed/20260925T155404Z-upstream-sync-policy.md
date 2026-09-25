# Work Order: Standing Upstream Synchronization Policy

- ID: `20260925T155404Z-upstream-sync-policy`
- Created: `2026-09-25T15:54:04Z`
- Last Updated: `2026-09-25T16:02:58Z`
- Owner: YaCy dev builder
- Status: completed
- Outcome: succeeded

## Request And Definition Of Done

Install the approved upstream-alignment strategy in local AGENTS.md so future
sessions initiate it without repeated user reminders. Use Sequential Thinking
and a durable work order. Complete when backups, policy, consistency checks,
changelog and memory capture are verified, with operational limits explicit.

## Authorized Scope And Boundaries

- Local AGENTS.md, project changelog, this work order and work-order indexes;
  ignored exact backups and policy-check evidence; authorized memory recording.
- No actual upstream synchronization, branch creation/rename/rewrite, source
  edits, PR changes, deployment, service operations, sibling/global instructions
  or staging of unrelated work. Installing policy is not executing first sync.
- Preserve all pre-existing uncommitted memory-policy/documentation/JAR changes.
- Future routine agent-driven checks/integration should not need repeated
  reminders; runtime approvals and explicit deployment/destructive gates remain.

## Checklist

- [x] [verified] Read current instructions, prior audit and dirty state.
- [x] [verified] Back up AGENTS.md and changelog; compare AGENTS backup bytewise.
- [x] [verified] Step through triggers, branch roles, validation and failure cases.
- [x] [verified] Add focused standing policy and changelog entry.
- [x] [verified] Verify preserved content and representative workflow scenarios.
- [x] [verified] Save approved policy to memory and close with remaining limits.

## Current State And Next Action

- Existing AGENTS.md already has backup, local-Git ownership, work-order,
  memory, continuity-stop and dev/fleet-separation rules; preserve them.
- Current custom branch is yacy-space-abuse-message. Local master already has
  custom commits; never silently reset it into an upstream mirror.
- PR 834 testing covers one isolated contribution, not full dev-branch parity.
- Backups: backups/upstream-sync-policy-20260925T155404Z/AGENTS.before.md and
  dev-node-change-log.before.md. AGENTS cmp passed. No code/Git operations begun.
- Six Sequential Thinking steps complete; policy and changelog installed.
- Original AGENTS and previous changelog entries are byte-for-byte preserved;
  policy-checks.json records passing structural/content checks; diff --check passes.
- Seventeen scenario paths reviewed in ignored scenario-review.md. These are
  instruction checks, not a simulated merge/deployment or compliance guarantee.
- Independent review found read-only scope and promotion/push approval
  ambiguities. Both corrected; exact fast-forward target and nonblocking
  minimal-PR clarification also added. Independent re-review passed.
- Final text preservation and 14 policy-marker checks pass. Saved policy was
  read back in full from Vestige, confirming the new approval/scope boundaries.
- Next Action: none for policy installation. At the next applicable development
  checkpoint, create/resume the first full sync work order; preserve current
  dirty work and obtain the required promotion/deployment approvals.
- Resume: read this work order/index, latest user request and current file diff;
  do not infer that a sync was launched or repeat any operation without evidence.

## Runbooks, Commands And Evidence

- docs/blacklist-pr-audit-20260924.md;
  docs/pr834-current-upstream-validation-20260925.md; AGENTS.md.
- Read-only baseline: git status --short --branch; cat AGENTS.md.
- Verified backup: cp --preserve=timestamps AGENTS.md
  backups/upstream-sync-policy-20260925T155404Z/AGENTS.before.md; cmp originals.
- Evidence: backups/upstream-sync-policy-20260925T155404Z/.
- Git checkpoint: documentation edits only; no source commit/push in this task.

## Decisions And Attempts

- Agent-driven workflow while sessions are active, not an unattended timer or
  automatic deployment. Sync state/deferrals belong in work orders, not a new
  competing tracking database or daily memory snapshots.
- Use upstream/master as the clean reference; preserve existing custom branch
  history and verify branch roles rather than assuming master is pristine.
- Shared-branch promotion/push is an explicit approval gate unless already
  authorized for a specific sync work order. Routine candidate work is automatic;
  this avoids inventing unattended publication authority from strategy approval.

## Delegation

Singer (01a0d220-7fa4-7381-87e1-de51ef08e266) reviews the new Git workflow for
contradictions/data-loss/default-authorization issues. Read-only except ignored
policy-review.md; no Git/source/services/PR/memory/sibling work. Lead owns edits.

## Closeout

- Closed: 2026-09-25T16:02:58Z
- Outcome: succeeded
- Verified results: exact pre-edit backups; appended policy preserves all prior
  AGENTS content; previous changelog entries preserved; 14 content checks,
  17 scenario reviews, independent review and git diff --check pass.
- Limitations: first complete custom-branch synchronization is not performed;
  no application tests or services needed for documentation. Policy adherence
  is not guaranteed by text checks. Changes remain local/uncommitted.
- Follow-up / successor: first full synchronization under the installed workflow;
  missing PR 834 help/localization remains a separate existing follow-up.
- Reusable knowledge saved: native smart_ingest merged policy into Vestige
  23955329-f12e-4702-aafe-3f3a263039f9; get read-back verified complete new text.
- Graph output trial: not applicable; documentation-only workflow, no code
  discovery or graph changes required.
