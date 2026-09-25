# Work Order: Apply Shared Memory Policy

- ID: `20260924T004853Z-shared-memory-policy`
- Created: `2026-09-24T00:48:53Z`
- Last Updated: `2026-09-24T00:54:01Z`
- Owner: YaCy dev builder
- Status: closed
- Outcome: succeeded

## Request And Definition Of Done

The user approved the proposed simplified AGENTS memory policy and requested
backups of each existing AGENTS.md before changing it. Back up and update the
global and current project instructions, preserve unrelated guidance, and
provide owner-mediated wording for sibling workspaces. Verify exact backups,
policy consistency, retained safeguards and documentation before closeout.

## Authorized Scope And Boundaries

- Targets: /home/programmer/.codex/AGENTS.md and this workspace's AGENTS.md.
- Project documentation, work-order lifecycle and authorized memory capture.
- No sibling AGENTS edits or Git operations; deliver a local owner handoff.
- No runtime settings, MCP upgrades, memory cleanup, YaCy deployment or services.
- Preserve existing uncommitted work and all unrelated instruction sections.

## Checklist

- [x] [verified] Retrieve review decisions and inspect current instructions.
- [x] [verified] Create and compare timestamped pre-edit backups for both files.
- [x] [verified] Apply matching memory policy and retain project-specific routing.
- [x] [verified] Verify focused diffs, unchanged safeguards and current schemas.
- [x] [verified] Document rollout/rollback, record policy in memory, and close.

## Current State And Next Action

- Existing global/project instructions read. Current project threshold already
  says accurate and safe; shared wording still emphasizes stable/reusable facts.
- Existing dirty files recorded by git status; do not discard or commit them.
- Backups global-AGENTS.before.md and project-AGENTS.before.md were compared
  byte-for-byte with the originals before editing; both comparisons passed.
- Project and global files now contain the same shared policy. Global installed
  candidate was byte-compared; its original matched the backup before installation.
- Validation passed: matching shared blocks, unique markers, required rules,
  unchanged project text outside Memory, unchanged global text outside Memory
  except Sequential Thinking heading renamed Planning with body unchanged.
- Owner handoff and changelog complete; git diff --check passes. The installed
  policy is saved in Vestige and returned as the first result of targeted recall.
  Dated extension note was installed and byte-compared successfully.
- Next Action: none for these two files. Sibling owners can use
  docs/shared-memory-policy-handoff.md within their authorized scope.
- Resume: inspect target files, backups and checklist before repeating writes.

## Runbooks, Commands And Evidence

- docs/vestige-review-and-upgrade-20260924.md; current AGENTS instructions.
- Local evidence: backups/memory-policy-20260924T004853Z/ (ignored).
- Validation: cmp pre-edit copies; compare shared blocks and non-memory text;
  git diff --check. No Java changes or application tests expected.
- Global installation: cp backups/memory-policy-20260924T004853Z/global-AGENTS.after.md
  /home/programmer/.codex/AGENTS.md, after an unchanged-original cmp check;
  installed file then compared byte-for-byte with the reviewed candidate.
- Git checkpoint: no commit/push requested; existing dirty work is preserved.

## Decisions And Attempts

- The common policy replaces overlapping memory prose, not other MCP guidance.
- Keep native ingestion/retention separate from agent capture and feedback.
- Keep restrictions honest: standing capture request does not bypass runtime
  permissions; failed saves get a documented fallback, not a false success claim.

## Delegation

Sibling owners receive a local handoff; no work dispatched in their directories.

## Closeout

- Closed: 2026-09-24T00:54:01Z
- Outcome: succeeded
- Verified results: two byte-verified pre-edit backups, identical installed
  memory blocks, unchanged unrelated project instructions and global body
  (except the Planning heading), whitespace validation and policy recall pass.
- Limitations: future-session adherence cannot be guaranteed by text alone.
- Follow-up / successor: sibling owners have a local handoff, not a dispatched
  assignment; their files/Git were not changed. Observe policy in ordinary work.
- Reusable knowledge saved: Vestige c227ba64-d278-4d83-b125-31dfb694c6e9 and
  managed extension note 20260924T005147Z-shared-native-memory-policy.md.
