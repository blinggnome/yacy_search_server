# Work Order: Blacklist Test Matching Rules

- ID: `20260922T005313Z-blacklist-match-details`
- Created: `2026-09-22T00:53:13Z`
- Last Updated: `2026-09-22T01:26:06Z`
- Owner: YaCy dev builder
- Status: closed
- Outcome: succeeded

## Request And Definition Of Done

Show which blacklist rule(s) a tested URL matches on BlacklistTest_p.html.
Use the real engine semantics, preserve current category/blocking results, and
show all applicable matches safely. Back up, implement focused tests, compile
and validate on dev only. Document behavior and invite user verification.

## Authorized Scope And Boundaries

- Blacklist test handler/template and minimum matcher diagnostic support/tests.
- Local source backup and dev runtime backup before any deployment. Server2 dev
  only (/opt/yacy-dev, yacy-dev.service, 8091); inspect live service state first.
- Do not alter blacklist contents, activation settings, matching decisions,
  production /opt/yacy, fleet state or sibling workspace Git.
- No new upstream PR for this feature. User wants further dev testing first.
- Preserve pre-existing dirty AGENTS.md, two reports/handoffs and dependency JARs.

## Checklist

- [x] [verified] Inspect matching semantics, cache and blacklist test behavior.
- [x] [verified] Back up and implement minimal diagnostic UI/API with tests.
- [x] [verified] Compile and verify matches, non-matches, normalization and escaping.
- [x] [verified] Back up/deploy dev only and verify HTTP/UI without rule changes.
- [x] [verified] Update docs/catalog and close with user test instructions.

## Current State And Next Action

- Main branch: yacy-space-abuse-message. New matching-rules UI is live on dev.
- Next Action: user tests BlacklistTest_p.html on port 8091; after confirmation,
  create focused local-workspace commit and push, excluding pre-existing files.
- Final verification: HTTP 200; authenticated overlapping-rule test returned two
  rules/count 2 in 1.31 s; no-match test returned no rows/unblocked in 0.61 s;
  invalid URL returned its existing message in 0.011 s. No servlet errors.
  Dev runtime JAR/source/template hashes match the tested local files.
- Chrome displayed the captured live response with actual dev assets at 1280
  and 390 px: both rows, purpose labels and header tooltips present, table fits
  its fieldset and synthetic long rule wraps. Mobile page width is 404 px both
  with and without the new table; pre-existing input overflow is unchanged.
  Screenshots inspected. This was captured-response visual QA, not an additional
  authenticated browser form submission; actual controller tested over HTTP.
- Final `ant compileTest` and all 16 focused JUnit tests pass, both against local
  classes and the patched runtime JAR. ZIP comparison proves only Blacklist.class
  and BlacklistTest_p.class changed; all 1,652 other entries are unchanged.
- Deployment payload: `/tmp/yacy-blacklist-match-20260922T010707Z/` on Server2.
  Guarded installer creates `/opt/yacy-dev/backups/blacklist-match-20260922T010707Z/`
  before stopping dev, checks baseline hashes, restores originals on install error.
  Installer session 20103 completed successfully; dev restarted at
  `2026-09-22T01:11:12Z`, PID 545487. Standard remains inactive/disabled.
  An immediate browser navigation was refused before readiness; do not treat
  that as failure. Next HTTP check no earlier than 01:14:17Z.
- The subsequent HTTP checks still found a closed port. Read-only service/log
  check at 01:15 shows the same active JVM with CPU use and ongoing native RWI
  index merges; no patch/classloading error in the sampled startup output.
  Paused page tests, reassessed, and extended startup wait rather than restarting
  or changing code. No index files were inspected or modified.
- Main-thread-only `jcmd Thread.print` at about six minutes shows active reads
  in `HeapReader.initIndexReadFromHeap -> Cache.init -> Switchboard.<init>`:
  startup is rebuilding the crawl-cache index, not executing the new diagnostic.
  Do not restart or remove cache files to rush feature validation.
- Configured HTTP cache is `/mnt/yacy-dev-htcache`, not DATA/HTCACHE. Live
  configuration has a 1 GiB content limit; its existing header heap is about
  1.3 GiB on a 4 GiB tmpfs with headroom. Header scan finished near 01:20;
  startup then advanced to cache-body index rebuilds (Cache.init line 129).
  File-offset check was limited to the HTTP-cache descriptor, not Lucene files.
  A first `lsof` sample placed flags after the filename; corrected by inspecting
  the already identified descriptor. No cache/index edits or second restart.
- Temporary loopback fixture server session 59792 and Playwright session
  blacklist-details are stopped. Browser artifacts retained in ignored backups.
- Startup eventually returned HTTP 200 about twelve minutes after service start.
  Journal confirms existing two-minute stop timeout was reached. Review graceful
  shutdown allowance before future busy-node deployments, separately from this
  feature. No manual cache/index repair and no further service restart occurred.
- `ant compileTest` passed. Initial 14-test run exposed fixture expectations:
  a bare `.*` host uses matchable-host semantics, not catch-all regex semantics;
  HTML encodes backslashes as entities. Tests now use decoded DOM text and add
  explicit coverage of encoded paths/plus signs and the original host behavior.
- Live read-only baseline: dev active/HTTP 200, standard inactive/disabled.
  Runtime JAR still matches documented AccessTracker deployment baseline.
- Resume check: read this record before repeating build or deployment; inspect
  current remote service/artifact state before any writes.

## Runbooks, Commands And Evidence

- docs/dev-node-change-log.md; docs/parser-metadata-notes.md;
  docs/access-tracker-log-notes.md for recent isolated JAR deployment method.
- Evidence/backup directory: backups/blacklist-match-details-20260922/.
  Original source, handler, template, changelog and catalog backed up before edits.
- Additive diagnostics reuse the existing matcher; hot-path boolean methods are
  unchanged. Rules are normalized loaded patterns, not source-file provenance.
  Explicit cache-only warning prevents inventing a rule for stale cached blocks.
- Git checkpoint: not yet created; await tested feature/user confirmation.
- One sandboxed compile retry could not write the existing Ivy cache; the
  normal escalated compile completed successfully. DNS-limited scp likewise
  succeeded with escalated network access. No dependency/config changes.

## Decisions And Attempts

| UTC time | Decision or attempt | Observed result / reason | Next step |
| --- | --- | --- | --- |
| 2026-09-22T00:53:13Z | Inspect engine before designing output | YaCy uses separate host/path semantics and per-purpose loaded lists | Reuse actual matching logic rather than approximate full-URL regex |

## Delegation

No delegated work. Graph-first discovery with full-response session caches for
filtered graph replies was useful for targeted symbols; no source was truncated
by filtering. Broad/verbose results were less useful; keep future queries scoped.

## Closeout

- Closed: `2026-09-22T01:26:06Z`
- Outcome: succeeded; implemented, backed up, deployed and verified on dev only.
- Verified results: compile gate, 16 focused tests, two-entry JAR comparison,
  authenticated live cases, source/runtime hashes, screenshots and isolation.
- Limitations: no filename provenance in loaded maps; cache-only warning cannot
  reconstruct a removed rule; exhaustive manual diagnostics cost more than a
  first-match lookup. Browser form authentication was exercised by HTTP probes,
  not a fresh interactive login. User acceptance remains pending.
- Follow-up / successor: user tests, then focused commit/push. No upstream
  publication authorized. Standard instance remains inactive/disabled.
- Reusable knowledge saved: docs/blacklist-test-notes.md, central changelog and
  catalog U3. No managed-memory write requested or performed.
