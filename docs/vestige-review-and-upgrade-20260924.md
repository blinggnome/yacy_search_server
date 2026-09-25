# Vestige Review And Upgrade

Date: 2026-09-24 UTC (September 23 locally)
Status: review, live upgrade and restarted Codex connection verified.

## Conclusion

The earlier memory-workflow proposal assigns too much routine bookkeeping to
the agent. Vestige is not merely a searchable notes database: its ingestion,
retrieval and background lifecycle already perform substantial memory management.
We should use those mechanisms instead of implementing a parallel system.

There is an important division of responsibility. The agent supplies accurate,
safe facts and asks useful questions; Vestige cannot learn an unsent fact from
the conversation. Once supplied, ordinary smart ingestion decides how to
incorporate it. The Getting Started guide explicitly distinguishes tool-driven
capture from transcript recording.
[Getting Started](https://github.com/samvallad33/vestige/blob/main/docs/GETTING-STARTED.md)

## What Each Requested Document Established

| Document | Relevant finding |
| --- | --- |
| README | Vestige's purpose is continuity and avoiding rediscovery; agents write/recall while the engine handles redundant and related knowledge. The separately marketed hosted products are not required for local MCP use. |
| Getting Started | Capture comes from agent tool calls or deliberate CLI input, not automatic access to every conversation message. Data-directory selection must match the actual store. |
| Science | Novelty gating, retrieval weighting and retention are native mechanisms. The document distinguishes implemented algorithms from research-inspired approximations. Fading is not ordinary automatic deletion. |
| FAQ | A short agent protocol is still necessary. Routine decay and duplicate management do not require manual tuning. The FAQ includes legacy examples, so check live schemas rather than copying calls blindly. |
| Configuration | Defaults matter: ordinary 3.0 writes use automatic mode unless review was configured; periodic consolidation and optional output profiles are separate settings. Several defaults changed after 2.3. |
| Tool Contracts | Discover installed capabilities; inspect application outcomes rather than assuming every protocol success means an applied write. Native receipts, scopes, previews and reversible operations should be preferred over reimplementing them. |

Reviewed the six requested documents at main commit
`3c5b198712d97b1f5350a04a4f2df5ed6cb2245b`, plus the linked
[Agent Memory Protocol](https://github.com/samvallad33/vestige/blob/main/docs/AGENT-MEMORY-PROTOCOL.md)
and [Memory Hygiene](https://github.com/samvallad33/vestige/blob/main/docs/MEMORY_HYGIENE.md).
Also compared release-tagged 2.3.0 and 3.0.0 configuration/science references.
One cached raw main README differed from the live GitHub snapshot, so the review
used pinned source files. Main and installed releases are not interchangeable.

## Native Management Versus Agent Work

**Leave routine incorporation and ranking to Vestige.** Supply a concise fact,
scope, source and tags through `smart_ingest`; do not manually inspect the entire
store or create a merge plan for each discovery. The user still wants every
accurate, safe new discovery considered for capture, including small command
options; the agent should not add an importance or expected-reuse test.

**Supply new information.** Vestige cannot infer that an old rule has changed if
we never send the correction. Submit the correction with its scope and basis.
Use explicit repair only if native ingestion leaves a demonstrated problem.

**Use feedback appropriately.** The 2.3.0 Science document says retrieval itself
strengthens matches. The 3.0.0 document instead describes audit-only retrieval
and explicit promotion after usefulness is established. Do not repeatedly recall
a stale statement and treat its score as proof of truth. This is a real version
difference, not an agent preference.
[2.3 Science](https://github.com/samvallad33/vestige/blob/v2.3.0/docs/SCIENCE.md),
[3.0 Science](https://github.com/samvallad33/vestige/blob/v3.0.0/docs/SCIENCE.md)

**Distinguish fading, merging and purge.** Low relevance does not mean a fact was
physically deleted. Separately, 2.3.0 enabled automatic consolidation merging
that absorbs duplicate rows; later releases disable that destructive background
merge by default. This does not disable normal smart-ingest novelty handling.
Do not promise either universal preservation of every original row or automatic
deletion of every unused memory.
[Versioned configuration](https://github.com/samvallad33/vestige/blob/v3.0.0/docs/CONFIGURATION.md)

## Installed Configuration Findings

Before upgrade:

- npm package and native MCP binary: 2.3.0.
- Codex invokes `vestige-mcp` through the existing user-local npm installation.
- The actual open database is `/home/programmer/.local/share/core/vestige.db`.
  This differs from the Linux directory printed in the docs. Open-file inspection
  established the path; it must not be changed casually during upgrade.
- The live process had no inspected Vestige environment overrides. No optional
  `vestige.toml` or `review_mode.json` was present beside the database.
- MCP reported profile `default`, healthy state, 123 memories, ready embeddings,
  100% coverage and zero mismatched embeddings.
- Recent consolidation and dream timestamps were present. This establishes
  lifecycle activity, not a need for the agent to run it after every save.
- Vestige had no recorded backup timestamp. This does not establish that no
  other system-level backups existed.
- A sandboxed CLI stats call failed because it opens storage writable. MCP
  health was working; this was not evidence of database corruption.

Nothing found justified changing output limits, model selection, data location,
approval controls or optional hosted services. No such changes were made.

## Upgrade Record

The user explicitly requested the latest version during the review. npm's latest
package is 3.0.0, also the newest numbered stable GitHub release examined.
GitHub's generic latest-release endpoint currently selects a whitepaper tag;
it is not a reliable software-version selector for this repository.

Backup directory (private, not committed):
`/home/programmer/.local/state/vestige-upgrades/20260924T001611Z/`

- `package-2.3.0/`: complete old npm package, including native binaries.
- `codex-config.toml`: private configuration backup; never print or commit it.
- `before-rehearsal.db`: consistent pre-test SQLite backup.
- `final-before-upgrade.db`: final backup after stopping the old MCP process.

Staged the npm tarball with scripts disabled, verified npm integrity, inspected
the installer through codebase-memory, then ran it in an isolated directory.
It downloads version-pinned release binaries and checks their SHA256. It does
not edit Codex instructions/configuration. No change to the YaCy server.

Rehearsal verification:

- `vestige --data-dir <copied-store> upgrade --dry-run` passed.
- Applied migration only to the copied store before live activation.
- SQLite integrity passed, with all 123 memories preserved.
- Compared IDs, content, type, tags, source, validity, scope and supersession
  against baseline: zero missing or changed records.
- A temporary stdio MCP client verified initialization as 3.0.0, ready embeddings,
  successful lookup and session context for known workspace information.
- The actual server advertised 15 tools, including `receipt` and `project`, plus
  `memory_status` views `tools` and `stats`. This differs from prose saying 14;
  use the running inventory as the contract.

Live activation and final verification:

- Stopped the old 2.3.0 MCP process before the final database backup.
- Installed `npm install --global vestige-mcp-server@3.0.0`; native
  `vestige-mcp --version` confirms 3.0.0.
- Default-path `vestige upgrade --dry-run` found the existing
  `/home/programmer/.local/share/core/vestige.db`, not a new empty store.
- Applied `vestige --data-dir /home/programmer/.local/share/core upgrade`.
- The independent MCP probe against the live store passed initialization,
  all 15 tool definitions, known-workspace recall and session initialization.
- Health reports healthy, 123 memories, ready embeddings, 100% coverage and
  zero mismatched embeddings. SQLite integrity is OK. Comparison with the final
  backup found zero missing or changed fact records across the fields above.
- Codex configuration is byte-identical to its pre-upgrade backup. No hooks,
  model, data-directory, approval or AGENTS configuration changes were made.
- The verification process exited. Restart Codex to attach the upgraded server
  and refresh this session's stale 2.3.0 tool catalog. Fresh-session attachment
  and a new capture/recall test remain follow-up checks, not claims of this run.

Private backup directory also contains `probe.mjs`, `rehearsal-probe.json` and
`live-probe.json`. The reports contain aggregate checks, not memory contents.
The procedure is captured in the dated Codex extension note
`20260924T002913Z-vestige-3-upgrade-procedure.md`; no deliberate Vestige fact
ingestion was performed through the old session after stopping its server.

## Post-Restart Verification

At 2026-09-24T00:37:10Z, after the user restarted Codex:

- The connected MCP returned catalogVersion 3.0.0 and all 15 current tools.
- `session_start` retrieved existing project memories. Health showed all 123
  pre-existing memories, healthy state, ready embeddings and 100% coverage.
- `smart_ingest` saved the verified upgrade procedure as memory
  `2bdc27c5-2587-43e6-9475-5a4bd2cd21bf`, reporting success and an embedding.
  A subsequent `recall` returned that procedure as its first result.
- This verifies existing-memory retrieval across the restart and new capture/
  retrieval through the restarted client. The newly saved fact has not itself
  been tested across a second restart; no second restart is required now.
- No instruction rollout, memory cleanup, reinstall or server changes performed.

## Rollback Boundary

This is a paired binary/database rollback, not a binary-only downgrade after
schema migration. Stop all Vestige clients first. Preserve the current store
and any post-upgrade facts before replacing anything. Restore the exact backed-up
2.3.0 package and matching final-before-upgrade database with all writers stopped;
do not mix an old DB with new WAL/SHM files. Validate integrity and counts before
reconnecting. Restoring the old snapshot would otherwise lose later discoveries.

The original Codex configuration should remain unchanged. The backup is for
comparison/recovery, not an instruction to overwrite later unrelated settings.
No rollback has been performed.

## Revised Recommendation

1. Keep a short shared agent protocol: session start, retrieve before rediscovery,
   capture accurate/safe facts promptly, and provide correction/usefulness feedback.
2. Let native smart ingestion, relevance and lifecycle processing do their jobs.
   Drop routine manual merging, mandatory dual-writing of every fact, and a
   second permanent receipt/verification database from the earlier proposal.
3. Keep Codex managed memory for cross-session policy/wayfinding as appropriate,
   not as a manually synchronized clone of Vestige. Keep project runbooks and
   work orders for their existing purposes.
4. Inspect save results. Use native receipts or targeted read-back for errors,
   consequential corrections and diagnosis, rather than repeatedly auditing
   every normal ingestion. Preserve a simple fallback only when saving fails.
5. After restarting the client, use the installed `memory_status(view="tools")`
   contract and refresh outdated examples. Do not call every available tool just
   because it exists or enable optional companion hooks by default.
6. Run one small cross-session capture/recall test, then observe ordinary work.
   Add machinery only for a demonstrated remaining failure.

These recommendations were initially review-only. The user subsequently approved
their implementation; the common policy is now installed in global and YaCy dev
AGENTS files. See the [owner handoff](shared-memory-policy-handoff.md) and the
changelog entry for backups, scope and verification. Sibling project files were
not changed. The earlier elaborate proposal remains historical and must not be
deployed unchanged. Native projection must not target Codex-managed registry
files or another workspace without the appropriate authorization.
