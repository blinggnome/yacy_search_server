# YaCy Dev Node Builder Rules

This repository is the local YaCy fork used for YaCy crawler/server development.
It is separate from the YaCy -> OpenSearch bridge project.

Patch YaCy Java, templates, tests, and documentation here only when the task is
about the YaCy dev node itself. Bridge code and OpenSearch-facing behavior live
in `/home/programmer/Documents/Yacy_bridge_builder`.

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

## Memory

Use Vestige for cross-session project memory. At the start of substantive YaCy
dev-node work, recall context for the current task before changing code or
server state. Save durable lessons, verified decisions, and reusable patterns
with `smart_ingest`; do not store credentials, private keys, raw logs, cookies,
or temporary command output.

## Planning

Use Sequential Thinking before acting on complex, ambiguous, architectural, or
high-risk YaCy changes, especially crawler/indexing behavior, remote dev-server
state, deployment, rollback, or failures with multiple plausible causes.

Do not use Sequential Thinking for routine status checks, simple reads, or
small mechanical edits unless unexpected complexity appears.

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

## Git Checkpoints

Keep YaCy fork commits separate from bridge commits. Check
`git status --short --branch` before and after committing.

After the user confirms a dev-node feature works, create a focused commit and
push to the appropriate GitHub remote unless the user asks not to. Do not commit
runtime databases, local backup directories, credentials, caches, generated
browser/tool state, or `.codebase-memory` artifacts unless explicitly requested.
