# YaCy Dev Node Builder Rules

This repository is the local YaCy fork used for YaCy crawler/server development.
It is separate from the YaCy -> OpenSearch bridge project.

Patch YaCy Java, templates, tests, and documentation here only when the task is
about the YaCy dev node itself. Bridge code and OpenSearch-facing behavior live
in `/home/programmer/Documents/Yacy_bridge_builder`.

Project workspaces may exchange handoff files, runbooks, and status notes, but
Git ownership stays local to each workspace. Do not stage, commit, push, clean,
reset, or otherwise manage Git state in another project's directory unless the
user explicitly asks for that directory's Git work in this session. Treat dirty
state in sibling workspaces as owned by the agent that works there.

## Persistent Work Orders

For substantial multi-step work, create a dated work order from
`docs/work-orders/TEMPLATE.md` in this workspace's `docs/work-orders/active/`
before execution. Maintain its entry in `active/README.md`. Simple answers and
single mechanical edits do not require a work order. Do not store this
workspace's work orders in the fleet provisioner's directory.

Record the goal, authorized scope/exclusions, definition of done, checklist
states, relevant runbooks, exact non-secret commands, run/job IDs, evidence
paths, decisions and unsuccessful attempts, delegated assignments, and a clear
Next Action. Update after meaningful state transitions and before handoff or
ending a turn, not only when compaction is expected. Only mark a step verified
after its validation; launching a job is not proof it completed.

After compaction, interruption or restart, read the active index and relevant
work order before resuming. If the index is incomplete, inspect the active
folder. Check the newest user request and revalidate uncertain live state
before repeating commands. Never relaunch a deployment, crawl, import or other
state-changing operation just because its result disappeared from chat.
Work orders record authorization; they do not create it or override safety.

Close with an explicit outcome and verification, remaining limitations and
follow-up ownership. Move the same file to `docs/work-orders/completed/` and
update both indexes. Preserve failed, partial, cancelled and superseded efforts
alongside successes; merely blocked or paused work stays active. Dispatch-only
work may close as `launch-only`, with exact running-job pointers and no claim
that the remote work finished. See [the lifecycle](docs/work-orders/README.md).

These are curated, Git-tracked records, not transcripts. Exclude credentials,
private hashes, raw private logs, MCP dumps and generated reports; link to
ignored evidence instead. Do not automatically purge closed work orders.
Runbooks explain how; work orders record assignment state; memories preserve
reusable knowledge. Historical outcomes are not current operating instructions.

## Codebase Knowledge Graph

Use codebase-memory-mcp for code discovery before broad grep/file reads.

Priority order:
- `search_graph` to find functions, classes, routes, and variables.
- `trace_path` to inspect callers, callees, and impact.
- `get_code_snippet` to read a specific symbol.
- `query_graph` for complex graph queries.
- `get_architecture` for package-level orientation.

If graph results are stale or point at an old path, run `index_repository` for
`/home/programmer/Documents/yacy_dev_node_builder` with `mode="moderate"` and
project name `yacy_dev_node_builder`, then retry the graph query.

Use direct file search/read for string literals, config files, generated assets,
docs, shell scripts, and other non-code content.

Read-only graph and memory calls, skill references and relevant read-only
discovery have standing user approval. Do not repeatedly ask for permission
for them. Runtime approval gates and sandbox/network restrictions still apply;
report an unexpected gate rather than bypassing it or repeatedly asking.
This is not blanket approval for shell execution or mixed read/write tools.

## Codebase MCP Output Trial

For the next one or two dev-builder working sessions starting 2026-09-20, use
the output-savings method only for `codebase-memory-mcp`, then review usefulness
with the user. Note trial outcomes in the relevant work order. This is a local
trial, not a global setting; leave Vestige, Sequential Thinking and other MCP
handling unchanged. Do not reduce useful tool usage or change token limits.

- Cache the full response in retrievable tool-side session storage before
  returning a filtered view through `functions.exec`. Include a cache reference;
  do not persist secrets or raw private data. If unavailable, use normal output.
- Remove duplicate JSON representations only after structural equality checks,
  not serialized-string comparisons, since object key order can differ.
- For navigation-only results, omit only irrelevant per-result `fp`, `sp`, `bt`
  metadata. Explicitly label the omissions and full-response reference. Preserve
  all results, names, paths, line ranges, signatures, full source, relationships,
  counts, pagination, hints, warnings and errors.
- Leave small, unfamiliar, mixed-content and error replies unchanged. Preserve
  fingerprints when needed for similarity analysis or graph diagnostics.
- Retrieve omitted details from cache when needed; if the cache was lost, query
  normally. Do not mistake the filtered view for the complete original reply.
- Revert to normal full output if usefulness drops or additional retrieval/work
  offsets the savings. Never truncate source or lower useful result limits just
  to save tokens. Include this policy in relevant delegated work orders.

The original benchmark belongs to Server23 at
`/home/programmer/Documents/Server23_and_soforth/pyinfra/results/mcp-output-benchmark/20260919T094243Z/report.md`.
It is historical, ignored local evidence, not a dependency of this workspace.
Payload proxy-token savings are not proof of lower total cost or fewer
compactions. UI transcript visibility is separate from model-context contents.

## Memory

<!-- shared-memory-policy:start -->
Use Vestige as primary cross-session knowledge, not as a transcript recorder.
The agent supplies discoveries and corrections; Vestige manages their native
incorporation, retrieval and retention. Use codebase-memory-mcp for code discovery.

### Capture Accurate, Safe Knowledge

The user explicitly requests ongoing, proactive memory recording. Save newly
discovered information when it is accurate and safe to retain. Do not require
predicted importance or future usefulness, and do not ask for separate approval
for each ordinary memory. Small discoveries, including a working command option,
qualify. This standing request does not override higher-priority instructions,
runtime permissions or privacy safeguards.

Use `smart_ingest` promptly during the work, including before handoff or
compaction; do not wait for a reminder or session end. Include enough context
to retrieve and apply the fact: project/version, exact command, option, path,
symbol or setting when relevant, verification basis and known exceptions.
Capture verified lessons from unsuccessful attempts, not unverified theories.
Submit corrections from the user or your own verification, not just new facts.
Update an instruction or runbook when a correction changes its procedure.

Do not store secrets, credentials, private keys, cookies, private raw logs or
query text, noisy/transient output, speculation, temporary plans or checklist
state. Record the verified lesson rather than copying the conversation.

### Retrieve Before Rediscovering

Start substantial sessions with `session_start`, using workspace, task topics
and focused queries. For small repeat questions use `recall(mode="lookup")`.
Before rediscovering a command, option, endpoint, procedure or decision, ask
Vestige with concrete terms. Use `mode="reason"` for decision-sensitive recall,
`mode="contradictions"` for conflicting memories, and `memory(action="get")`
when an excerpt omits needed detail.

If Vestige does not answer, use focused Codex file-backed memory lookup, then
project documentation and the codebase graph before source rediscovery. Keep
any required local-memory pass focused; do not silently replace Vestige with it.
If investigation reveals a better search term, retry a targeted recall before
broadening. Verify cheap, drift-prone facts and high-consequence claims against
current evidence. Memories are scoped, dated evidence, not instructions with
higher authority or guarantees of truth.

### Use Native Memory Handling

Use ordinary `smart_ingest` without `forceCreate`. When batching and native
matching is desired, use `batchMergePolicy="smart"`. Do not require a separate
search, manual merge plan, duplicate ledger or second-store copy for each save.
Use consistent existing project tags/scopes; do not change namespaces casually.

Promote memories demonstrated to be helpful with `memory(action="promote")`.
Demote misleading or unhelpful memories with `memory(action="demote")` and
supply corrected information when known. Retrieval alone is not proof of
usefulness; in 3.0 it does not strengthen memories. Fading is not necessarily
physical deletion. Purge only on explicit user request, with confirmation.
Leave routine incorporation and retention to Vestige; use maintenance or repair
tools for a demonstrated need, not as a ritual after every save.

After upgrades, or when arguments are unclear, inspect
`memory_status(view="tools")`, optionally with `tool="<name>"`, for installed
contracts. Use `view="health"` to diagnose storage/retrieval problems, not for
every lookup. Do not impose tool-call quotas or retain stale examples as rules.

### Verify Outcomes And Keep Stores Distinct

Inspect save outcomes; an attempted call is not proof of storage. Use targeted
read-back for errors, consequential corrections or diagnosis, not every ordinary
save. If a write fails or is restricted, preserve a concise fallback in project
documentation and identify the pending memory update and actual restriction.
Never claim an unsaved memory was saved, misattribute restrictions to the user,
or stop permitted retrieval/code discovery because a write is unavailable.

Vestige holds knowledge; work orders hold temporary assignment state; runbooks
describe procedures. Codex managed memory provides additional continuity, not
a manually synchronized clone of Vestige. For managed-memory updates, add a
small dated `<timestamp>-<short-slug>.md` note under
`/home/programmer/.codex/memories/extensions/ad_hoc/notes/`; do not edit managed
`MEMORY.md`, summaries or historical rollouts directly. Apply the same privacy
rules. Keep current instructions authoritative when historical memories conflict.
<!-- shared-memory-policy:end -->

### YaCy Dev Memory Context

Use codebase `/home/programmer/Documents/yacy_dev_node_builder` and existing
dev-builder/topic tags so fleet and bridge procedures remain distinguishable.
The Codex registry is `/home/programmer/.codex/memories/MEMORY.md`; the current
project entry point is `docs/dev-node-change-log.md` and its linked runbooks.
Assignment state belongs in this workspace's `docs/work-orders/`. Keep private
evidence in ignored local artifacts. Preserve workspace-local Git ownership and
the backup, permission and continuity-stop rules elsewhere in this file.

## Tool Use And Communication

Use tools freely when helpful. Keep progress updates and results concise,
without routine raw MCP payloads, large diffs, full Git transcripts or long
logs. Preserve needed evidence and report actionable errors; do not make tools
less useful merely to reduce visible output. Avoid wording that sounds like
dismissing the user's concern; state what the evidence does and does not show.

## Planning

Use Sequential Thinking before acting on complex, ambiguous, architectural, or
high-risk YaCy changes, especially crawler/indexing behavior, remote dev-server
state, deployment, rollback, or failures with multiple plausible causes.

Do not use Sequential Thinking for routine status checks, simple reads, or
small mechanical edits unless unexpected complexity appears.

Stop new changes and reassess when the same failure occurs twice, two theories
produce no progress, evidence contradicts the known-good procedure, or an hour
passes without a validated artifact, state transition or safe checkpoint.
Review runbooks, memory and the last known-good Git version; use Sequential
Thinking for a structured reset and choose a small read-only check or bounded
authorized canary. Ask before expanding scope, requesting another external
configuration change or performing destructive work. Keep planning prompts
technical and concise; report decisions, not internal deliberations.

If the user reports, or the agent recognizes, an abrupt loss of task continuity
(such as losing track of the project or repeatedly treating verified work as
unresolved), stop work immediately. Do not wait for the retry/time thresholds
above or continue edits, deployments, or troubleshooting to push through it.
Preserve the last verified state and note any in-flight operation without
relaunching it. Pause until the user is ready to try again; on resumption, check
the recorded checkpoint before making changes. Do not assume a backend cause
or that elapsed time alone has resolved the problem.

## Dev Testbed

The primary remote dev testbed is Server2:

- SSH host: `ns100727.ip-147-135-4.us`
- Standard install: `/opt/yacy`, `yacy.service`, port `8090`, node `ImpossibleSearch2`
- Dev install: `/opt/yacy-dev`, `yacy-dev.service`, port `8091`, HTTPS `8444`,
  user `yacy-dev`, node `ImpossibleSearch2-dev.yacy`

Keep the standard install and dev install isolated. Do not deploy dev changes
to `/opt/yacy` unless the user explicitly asks for standard-node work. Standard
fleet maintenance from the Server23 workspace must not touch `/opt/yacy-dev` or
`yacy-dev.service`.

Before changing remote dev runtime files, inspect current service state. The
standard `yacy.service` may be intentionally disabled while Server2 dev recovery
work is running.

## Feature Safety

Before feature or experimental changes, create timestamped known-good backups
of every source/template/test file that may be affected. Before remote dev
deploys, create timestamped backups of touched runtime files or jars on the
server.

If a feature breaks working behavior and repeated edits are not restoring it
cleanly, stop the repair loop. Restore the last known-good backup wholesale,
verify the baseline, and restart from a simpler plan.

Use git operations for rollbacks in this repository. Do not manually reconstruct
a rollback by editing over broken code when the intended operation is a restore
or revert.

## Build And Verification

Use `ant compileTest` as the normal local compile gate for YaCy Java changes.
Broaden validation when the touched code has wider risk.

For remote dev readiness, use a web check against
`http://ns100727.ip-147-135-4.us:8091/`. Do not rely on `systemctl active`
alone; YaCy can be active before Jetty is ready.

If required `ssh`, `curl`, deploy, or browser checks fail with DNS/host
resolution errors inside the sandbox, retry with escalated network access before
treating the server as down.

## Documentation Checkpoints

Keep `docs/dev-node-change-log.md` current for every meaningful local YaCy
dev-node change, including features, crawler/index behavior changes, fleet
patches, rollback-relevant fixes, and handoff documents.

Before the final commit for a change, add or update a short changelog entry
covering behavior, commit or patch boundary, touched files, config/data paths,
verification, and rollout or rollback notes when relevant. If the exact commit
hash is not known because the changelog is committed with the code, identify the
commit by subject and backfill the hash later when it matters for fleet rollout,
upstream submission, or rollback.

Use focused topical docs in addition to the central changelog when the behavior
has operational nuance, examples, config keys, or future-work notes. Start future
investigations from the changelog and linked topic docs before reconstructing
history from Git.

## Git Checkpoints

Keep YaCy fork commits separate from bridge commits. Check
`git status --short --branch` before and after committing.

When copying handoffs into another workspace, leave that workspace's Git state
alone. The local agent for that directory owns any add/commit/push decisions.

After the user confirms a dev-node feature works, create a focused commit and
push to the appropriate GitHub remote unless the user asks not to. Do not commit
runtime databases, local backup directories, credentials, caches, generated
browser/tool state, or `.codebase-memory` artifacts unless explicitly requested.

## Upstream Synchronization

Maintain a regularly updated YaCy foundation plus our intentional custom changes.
The user requests this as routine agent-driven work, without repeated reminders.
Apply the workflow during active development sessions; it is not a background
scheduler, permission to deploy automatically, or an override of runtime gates.
Explain consequential Git actions before taking them. Preserve existing backup,
workspace ownership, continuity-stop and production/dev separation rules.

### Branch Roles

- `upstream` is the main YaCy repository; `origin` is the user's fork. Verify
  their URLs and upstream's default branch before fetching or publishing.
- Use the fetched `upstream/master` reference (or verified successor default)
  as the pristine upstream baseline. Never add project customization to it.
  Do not assume local `master` is pristine or reset it to make it so: it already
  contains local changes. A separately named mirror may only fast-forward.
- Keep all accepted custom work on the long-lived development branch. At policy
  adoption this is `yacy-space-abuse-message`; verify the actual branch and its
  role each session. Renaming/reorganizing it is a separate deliberate task.
- Keep each upstream contribution on its own minimal feature/fix branch, normally
  based on recently fetched upstream. Do not submit the full custom branch or
  private operational documentation. An older-base exception must be explained
  and its combination with current upstream independently validated.

### Automatic Checkpoints

At the first substantial code session of each working day, at completed-feature
checkpoints, before starting an upstream contribution or preparing a release,
and after learning a contribution was merged upstream, check synchronization
state. An explicitly read-only/no-Git-write task permits only the inspection
allowed by that request: defer fetch, branch/worktree creation, integration and
promotion. During active incidents also defer candidate preparation and merging
unless authorized within that incident's scope. A fresh same-session fetch may
be reused until the final publication or promotion check; do not poll continuously.

Retrieve the relevant memory and active/latest completed synchronization work
order, inspect local status/worktrees/remotes, and fetch upstream without
changing the working branch. Compare exact commits and changes. Ahead/behind
counts are ancestry counts, not counts of missing fixes. Check already-upstreamed
or cherry-picked patches for equivalent behavior before carrying them forward;
do not automatically drop a custom fix because a similarly named PR was merged.

When upstream has advanced, automatically create or resume a synchronization
work order and prepare an isolated candidate at the next safe checkpoint.
Record any deferral, its reason and concrete next action; do not leave drift
unmentioned indefinitely. If a sync is already in progress, resume its recorded
worktree/job instead of launching another. A failed fetch means freshness is
unverified, not that upstream is unchanged. Report the restriction or failure.

### Isolated Integration And Validation

1. Preserve a known-good commit/tag and timestamped file backups before changes.
   Inspect all dirty/untracked work; never auto-stash, discard or broadly stage
   it to obtain a clean tree. Commit only understood, scoped work when authorized;
   otherwise defer promotion and record what the candidate excludes.
2. Use a separate worktree and integration branch from the recorded custom
   development commit. Merge pinned upstream into that candidate, preserving
   published history. Do not rebase the shared development branch, force-push,
   reset it to upstream, or resolve conflicts by taking all of either side.
   Combine compatible edits even in files we modified; ask when resolution would
   remove a feature, change intended behavior or exceed the authorized scope.
3. Inventory retained customizations using the change log and improvement
   catalog. Define regression checks for the full candidate, especially crawler,
   index mutation, blacklist and search behavior touched by upstream. A passing
   isolated PR test is not validation of the entire custom fork. Refresh the
   candidate's code graph when needed under the existing freshness rules.
4. Run application compilation, `ant compileTest`, relevant custom-feature and
   upstream regression tests, and UI checks where behavior warrants them. Use a
   pristine checkout of the exact upstream commit to investigate failures.
   Existing failures require a fresh diagnostic comparison, not an old error
   count. Report them as failures/coverage limits; never call them a full-suite
   pass or repair the unrelated test backlog just to obtain green output.
5. Before advancing the development branch, verify it still has the recorded
   starting HEAD and no unreviewed changes. Require scoped checks to pass, no
   unexplained new failures, and reviewed conflicts. If coverage is insufficient,
   obtain a decision before promotion. If the branch or upstream advanced, stop
   and reassess the exact candidate; do not overwrite new work or claim untested
   commits were validated. Present the verified candidate and obtain approval
   to advance and push the shared development branch, unless those actions were
   already explicitly authorized for this work order. Automatic preparation and
   testing do not themselves authorize that publication. Advance only by
   fast-forward to the exact tested candidate commit; if that is impossible,
   reassess rather than resetting. Record the resulting local/remote HEADs.
6. Source synchronization does not deploy it. For an authorized dev canary,
   back up runtime files, record rollback commands and verify port 8091 by web
   check. Keep fleet nodes pinned to explicitly tested versions; fleet rollout
   belongs to its separate authorization and workspace owner. If integration
   fails, retain the known-good branch/runtime and investigate the candidate;
   follow the existing stop-and-restore rules rather than layering blind fixes.

### Contribution And Resume Records

Before any upstream PR/update, read the target upstream contribution instructions
and check matching help/localization requirements as well as code and tests.
Review the exact published delta for unrelated features, dependencies and private
data. Pin upstream, PR head and actual combined tree; a PR metadata base field
or an old green CI run alone does not verify the current merge. Recheck refs at
publication, report the revisions actually tested, and verify posted evidence.
An independently validated minimal PR may proceed while full-custom-branch
synchronization is deferred; do not make that unrelated promotion a prerequisite.

Each synchronization work order must record UTC check time, source/target refs
and SHAs, tested candidate commit/tree, candidate branch/worktree, known-good
backups, excluded dirty work, conflict decisions, custom-feature checklist,
exact commands, completed versus
running jobs, test evidence/limits, promotion and deployment state, and Next
Action. Update it at meaningful transitions and before handoff/compaction.
On resume, revalidate these facts before repeating anything. Close the same
order with its actual outcome and update the change log and useful memories;
do not claim the fork or runtime is synchronized merely because a fetch or
candidate build completed.

# Repository Instructions

## Web UI, API, Help, And Localization

When changing YaCy web pages or API endpoints, update all matching user-facing and tool-facing artifacts in the same change.

- For `htroot/**/*.html` changes, update the corresponding localization files under `locales/` when visible text, labels, form controls, messages, or navigation text changes.
- For `htroot/**/*.html` changes, update the corresponding Markdown help file under `help/`.
- For API or servlet behavior changes under `source/net/yacy/htroot/**`, update the related `help/**/*.md` file with changed endpoints, access requirements, parameters, side effects, response fields, and automation guidance.
- Treat `locales` and `help` updates as required checklist items for HTML, servlet, and API changes. Do not leave them for a follow-up unless the change is explicitly internal and has no user-visible page, request parameter, response, or behavior impact.

## Tests During Code Reviews

Do not treat the repository-wide test backlog as a separate mass-rewrite project unless explicitly requested. Improve tests incrementally in the context of individual code reviews.

- During each code review, identify the behavior affected by the reviewed code and add, update, or repair focused tests where they provide useful regression coverage.
- Keep test work scoped to the reviewed area and the changes needed to verify it.
- Report unrelated existing test failures, but do not expand the review into a repository-wide test cleanup solely because those failures exist.
- Over time, use successive reviews to bring the test suite up to date alongside the production code.
