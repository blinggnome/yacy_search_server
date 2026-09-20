# Work Order: <Title>

- ID: `<YYYYMMDDTHHMMSSZ-short-slug>`
- Created: `<UTC timestamp>`
- Last Updated: `<UTC timestamp>`
- Owner: `<workspace lead or assigned worker>`
- Status: active
- Outcome: not yet determined

## Request And Definition Of Done

<User request and measurable completion criteria.>

## Authorized Scope And Boundaries

- Allowed files, targets and actions: <explicit scope>
- Exclusions: <services, repositories and prohibited operations>
- Authorization and changes: <request reference; this grants no new permission>

## Checklist

- [ ] [pending] <Step and required validation>
- [ ] [pending] <Step and required validation>

Use pending/running/blocked/verified/skipped. Check only verified steps.

## Current State And Next Action

- Current state: <verified facts, not assumptions>
- Next Action: <one precise step; none when closed>
- Resume check: <state, artifacts and scope to revalidate before retrying>

## Runbooks, Commands And Evidence

- Current runbooks: <paths>
- Exact non-secret command and scope: <invocation, or not applicable>
- Run/job IDs, service/build identity: <references, or not applicable>
- Verification and result paths: <observed evidence>
- Git checkpoint: <commit, or explicitly uncommitted/not applicable>

Ignored evidence may be absent in another checkout. No secrets or raw logs.

## Decisions And Attempts

| UTC time | Decision or attempt | Observed result / reason | Next step |
| --- | --- | --- | --- |
| <time> | <attempt> | <evidence or uncertainty> | <action> |

## Delegation

<Worker, scope, constraints, completion criteria and result pointer; or none.>

## Closeout

- Closed: <UTC timestamp, or not closed>
- Outcome: <succeeded/partial/failed/cancelled/superseded/launch-only>
- Verified results: <what actually completed>
- Limitations: <unresolved items and still-running jobs>
- Follow-up / successor: <owner and pointer, or none>
- Reusable knowledge saved: <runbook/memory references or pending permitted write>

Move closed files to completed/ and update both indexes. Do not archive merely
blocked work or equate launch success with completion.
