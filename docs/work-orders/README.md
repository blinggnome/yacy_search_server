# Work Orders

This directory belongs to the YaCy dev builder. Runbooks explain how; work
orders preserve assignment state; Vestige stores reusable knowledge.

- [Active index](active/README.md)
- [Completed archive](completed/README.md)
- [Template](TEMPLATE.md)

## Create And Update

1. Before substantial multi-step execution, create
   `active/YYYYMMDDTHHMMSSZ-short-slug.md` from the template. Use UTC and keep
   the same filename when archiving. Simple answers/mechanical edits are exempt.
2. Record the user request, authorized scope, exclusions and definition of done.
   Link current runbooks rather than copying full procedures into the record.
3. Add an active-index entry. Keep independent tasks separate; link dependencies
   and successor work orders instead of silently combining unrelated work.
4. Update Last Updated, checklist and Next Action after meaningful progress,
   failures, decisions, changed authorization or delegation, and before ending
   a turn or handing off. Do not wait for a compaction warning.
5. Record non-secret commands, run IDs, service/build identities, evidence paths,
   actual verification, relevant commits and failed approaches with reasons.
   Give delegates bounded assignments, constraints and completion criteria.

Step states: `pending`, `running`, `blocked`, `verified`, `skipped`. Check boxes
only for verified steps; explain skipped steps. A launch acknowledgment does
not prove a job finished. Keep worker assignments and evidence pointers here,
not just in the chat transcript.

## Resume

Read the active index and relevant work order. If the index is incomplete,
inspect the active folder rather than assuming there is nothing underway.
Compare with the newest user instruction. Verify current process/service state
where necessary before following Next Action. Reuse recorded IDs for inspection;
never repeat a crawl, deployment, import or destructive command because its
prior output disappeared. A work order documents scope, not new authorization.

## Close And Archive

Status is `active`, `blocked`, `paused` or `closed`. Blocked/paused work remains
active unless explicitly closed or superseded. On closure, record one outcome:
`succeeded`, `partial`, `failed`, `cancelled`, `superseded`, or `launch-only`.
Include completion time, verification, limitations and follow-up ownership.

Move the same file to `completed/`, remove the active-index row and add a row
to the completed index with outcome and short result. Keep relative links valid
after moving. Retain unsuccessful attempts as well as successes; "completed"
means closed, not necessarily successful. Do not automatically delete archives.

For an assignment to dispatch and review later, use `launch-only` with exact
running-job references and say completion is unverified. Do not create polling
loops merely to close paperwork. If verification is part of the assignment,
keep it active until verified or explicitly close with an incomplete outcome.

## Privacy, Git And Knowledge

These are curated, trackable project documents. Review before committing.
Never copy passwords, tokens, keys, admin hashes, raw private logs/query text,
MCP dumps or generated reports into them. Link ignored evidence, noting that
it may not exist on another checkout. Do not move evidence/credential folders
into docs for portability. Git ownership remains local to this workspace.

Closed records are historical evidence, not procedures to replay. Preserve
original outcomes; add dated corrections or linked successors. Do not backfill
old records as if contemporaneously maintained; label sources and uncertainty.
Update current runbooks and reusable Vestige notes separately. Transient task
state belongs here, not in long-term memory.
