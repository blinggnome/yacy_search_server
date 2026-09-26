# Work Order: Upgrade The Dev Runtime To The Synchronized Baseline

- ID: `20260925T175946Z-dev-runtime-upgrade`
- Created: `2026-09-25T17:59:46Z`
- Last Updated: `2026-09-26T07:00:17Z`
- Owner: dev-builder lead
- Status: closed
- Outcome: succeeded with documented validation limits and user baseline confirmation

## Request And Definition Of Done

User authorized executing the proposed dev upgrade: inspect compatibility and
storage, establish application/config/rules/data rollback, install the complete
synchronized custom build, and verify HTTP/admin/search/crawl/blacklist/index
behavior. Source checkpoint is `27152b94af573c0f2dda1fd8d94b89ecd65830e1`.

## Authorized Scope And Boundaries

- Server2 `/opt/yacy-dev`, `yacy-dev.service`, port 8091 only; necessary backup,
  build, staging, service restart and bounded synthetic validation authorized.
- No writes to standard `/opt/yacy`, standard service, fleet, or sibling Git.
- Do not inspect or manipulate individual Lucene segment files; no index reset.
- Preserve private settings and query logs in protected backups, never commits.
- A coherent recoverable backup must precede new-runtime access to live data.

## Checklist

- [x] [verified] Inspect live runtime/storage and upstream migration requirements.
- [x] [verified] Complete distribution built/uploaded; establish verified rollback.
- [x] [verified] Stage and activate dev-only runtime with compatible data.
- [x] [verified] Validate web/admin/search/crawl/blacklist/index scope; user confirms
  prior search baseline. Automated row-count limits remain explicit, not a pass.
- [x] [verified] Document and archive outcome; publication checkpoint subject
  `Record accepted dev upgrade and existing search delays`.

## Current State And Next Action

Current outcome: upgraded dev and original feeder retained. User confirms search
works as before, including slow local results on mechanical disks. No source
patch or rollback made. Deployment accepted for continued dev use; no fleet
rollout authorized. All bounded probe sessions have completed. Optional future
performance and JSON robustness work is separate. Chronology follows:

- Source synchronization is committed/published; 190 focused tests passed;
  full compileTest has documented baseline/classpath failures.
- Baseline: dev active/enabled, standard inactive/disabled; Java21.0.12.1,
  Solr9.0, dev DATA543GiB plus external cache2.3GiB,1.2TB available.
- Authenticated HTTP baseline passes all11 probes; collection25,628,999,
  webgraph0 (live counts can change before stop). External HTTP200.
- Distribution build PASS (Ant session72200); upload checksum matched. Archive
  `release/yacy_v1.942_202609251109_27152b94a.tar.gz` in evidence root.
- Cold backup ran through unit `yacy-dev-upgrade-backup-20260925T175946Z`.
  Dev shutdown completed cleanly (MainPID0/inactive); standard unchanged.
  SSH/systemd-run session18903 later completed; do not relaunch.
- Live/default Solr configuration matches new package. Existing solr_9_0 path,
  schema and luceneMatchVersion remain appropriate; no manual index migration.
  Rollback requires old runtime and old data together. Java21 already sufficient;
  no system package upgrade identified. User authorized needed prerequisites.
- Last confirmed backup18:17:36 UTC:28GiB copied, no job errors, dev inactive
  and standard inactive/disabled. Early throughput around50MiB/s; expected hours.
- Status SSH attempts at18:23/18:24 timed out during banner exchange at15/45s.
  Original systemd-run SSH session18903 still waits; no completion receipt.
  Backup runs independently. No reboot/relaunch/activation performed.
- SSH recovered18:26; at18:26:59 backup56GiB, no reported errors, dev inactive.
  I/O pressure was high (about70%); causal link to timeouts is not established.
  Reusable SSH control socket `/tmp/yacy-dev-upgrade-20260925.sock` now available.
- User confirms mechanical drives in RAID-1 and can still SSH into the server.
  Backup job runtime priorities verified: IOWeight10, CPUWeight20. Allow the
  cold copy to complete; do not substitute hard links or skip data recovery.
- At18:44:32 UTC backup102GiB and progressing, no job errors. Reused SSH
  connection responsive. No new runtime installed or service restarted.
- At18:48 backup112GiB. User asked about high memory: live18:51 check shows
  31GiB total,3.6GiB used,28GiB buff/cache,27GiB available. Both YaCy services
  inactive/MainPID0; separate opensearch.service remains active (~1.7GiB RSS).
  Four vmstat interval samples show no swap-in/out; memory PSI averages zero,
  I/O PSI substantial (~68-74%). Evidence points to disk pressure, not memory
  exhaustion. OpenSearch and memory/kernel settings left unchanged.
- At19:58:48 backup323GiB, progressing without reported job errors. Both YaCy
  services still inactive/MainPID0; original backup job950077 remains running.
  No activation performed. Continue spaced read-only checks; no relaunch.
- Backup completed21:14:17Z: unit inactive/MainPID0/ExecMainStatus0/Result success;
  full backup546GiB and all three metadata-comparison receipts empty. Original
  SSH session18903 completed successfully. No runtime activation yet.
- Activated21:16:11Z; web HTTP200 after185-second first-check delay. All11
  authenticated page/API probes passed; index25,631,634 and webgraph0. Public
  admin HTTP401; stylesheetHTTP200. Installed jar hash matches release.
- Synthetic blacklist attribution/edit/delete protections all passed; temporary
  fixture lists removed. Startup sample182 JVM threads,2 evictors,2561 indexed
  log messages, no severe errors. Routine crawler network/parser warnings remain.
- Restored three server-local bin helpers unchanged from backup. Existing
  searchprofiling helper also preserved (identical to current source, omitted
  by dist). Feeder Requires=yacy-dev.service caused it to stop with shutdown;
  restored its previous running state at21:23:39, no new errors/restarts observed.
  Standard remains inactive/disabled. Temporary stop-timeout drop-in removed.
- Browser homepage renders correctly. Local search validation is intermittent:
  first ALL-mode request0 rows, TEXT page1 rendered10, TEXT page2 rendered0;
  repeating original ALL request later rendered9. Counts remain populated.
  Two-row JSON query yielded a leading comma when its first result was absent;
  ifexist variant yielded0 items. No search edits or rollback yet.
- Direct Solr probe returned10 IDs in0.374s (QTime246ms). Search diagnostics
  show10s/1s result-slot timeouts while feeds run and RWI HeapReader read/lock
  waits; later sample18GiB available, no interval swap traffic,33-43% I/O wait.
  Repeating page2 still returned0 rows. Cause and regression status unresolved.
- Git freshness rechecked: HEAD/origin27152b94a equal; upstream remainsb50b76bd.
  No new source changes. Temporary Playwright browser closed; no probe sessions
  remain running. Dev and its original feeder remain active.
- User chose to keep the upgraded dev runtime running and investigate (Sept26).
  At06:56:31 the same dev and feeder PIDs remain active with zero restarts;
  standard remains inactive. HTTP200 in0.003s,14GiB available RAM, substantially
  lower disk PSI (about3-4%) than immediately after startup.
- User subsequently confirmed search appears to work as before the update and
  explained the known local-result delay on mechanical drives/large database.
- Bounded follow-up session69307 completed: all7 HTTP200/no ServletException;
  directSolr10 IDs1.34s/full docs0.114s; HTML page1 counts4then6, page2 counts0
  both times; JSON1of2 requested, validJSON. Do not turn these partial counts
  into a claim all search tests passed. Evidence is preserved in probe-results.json.
- Next Action: normal dev work may resume. Optional local search latency and
  absent-slot JSON robustness need separate scoped investigation, not an assumed
  regression repair. Retain cold backup; no rollback or fleet rollout planned.
- Resume: read this order; check recorded jobs and live service state before
  repeating any backup, installation or crawl. Do not repeat by assumption.

## Runbooks, Commands And Evidence

- `docs/upstream-sync-20260925.md`; `AGENTS.md` dev and deployment safeguards.
- Ignored evidence: `backups/dev-runtime-upgrade-20260925T175946Z/`.
- Sept26 investigation evidence: `backups/search-delay-20260926T065629Z/`.
- Remote root-only staging/backup: `/opt/yacy-dev-upgrade-20260925T175946Z`.
  Source archive SHA256 `9a3c31321760ced21b4173b4081e28b679bfa3c4e6673f4160fcfa54d3bea485`.
  `cold-backup.sh` uses systemctl stop, full rsync backup and metadata dry-run
  verification; original install, external `/mnt/yacy-dev-htcache`,
  `/var/lib/yacy-dev` and unit preserved. No individual segment inspection.
  Temporary runtime drop-in `90-upgrade-stop.conf` permitted graceful stop and
  has now been removed. No change to boot enablement.
- Backup started18:06 UTC. No forced JVM kill or cache/index cleanup performed.
- Additional isolated bridge guard/test session86391 completed PASS (4tests),
  `bridge-test.log`; local `test/DATA`, never remote index/rollback data.
- Reviewed activation script and rollback notes staged on remote. Activation
  requires backup-complete/inactive service/checksums and does not run itself.
- Package123MiB copy timings under backup load: sparse rsync2.36s, regular
  rsync1.76s, cp2.61s. Not a representative full-index benchmark; no method change.
- Baseline: `baseline-http.json`, credential-safe `probe.py`; protected credential
  stays remote, never printed. Probe reached all requested admin pages.

## Decisions And Attempts

| UTC time | Decision or attempt | Observed result / reason | Next step |
| --- | --- | --- | --- |
| 2026-09-25 17:59 | Begin authorized dev-only upgrade | Jetty12/Solr9/auth changes require full-runtime review | Inspect before writes |
| 2026-09-25 18:06 | Start guarded cold backup | Baseline/auth/package hash verified; graceful stop then full backup | Wait for verified backup marker |
| 2026-09-25 18:25 | Two SSH banner timeouts | Cause unconfirmed; original job not relaunched; runtime untouched | Bounded read-only access check after reassessment |
| 2026-09-25 18:27 | SSH recovered; confirm backup | 56GiB copied, no errors; high I/O pressure | Lower temporary job priority, continue waiting |
| 2026-09-25 21:14 | Cold backup completed | Unit exit0; all comparison receipts empty | Guarded activation |
| 2026-09-25 21:16 | Activate full dev runtime | Jar/runtime hashes and config/rules checks passed | HTTP and behavior validation |
| 2026-09-25 21:23 | Preserve helpers and restore dependent feeder | Original helper bytes restored, single feeder active | Finish browser search validation |
| 2026-09-25 21:33 | Search verification discrepancy | Direct Solr fast; result timeouts and RWI waits; page2 repeat still empty | Record incomplete gate; user choice for investigation/rollback |
| 2026-09-26 06:56 | Keep upgraded dev; bounded overnight recheck | Same services,0 restarts,lower disk pressure,HTTP200 | Finish existing probes |
| 2026-09-26 07:00 | User confirms prior slow-search baseline | HTTP transport/fullSolr checks pass; partial YaCy rows remain documented | Accept dev deployment with limits; no speculative search patch |

## Delegation

Singer reviewed upstream Git/docs/build migration boundaries read-only; lead
owned the remote runtime. Sept26 review found upstream RWI storage changes but
did not establish an upgrade-caused regression; `oneResult` was unchanged
between the pinned checkpoints. Lead completed live probes and documentation.
Final documentation-only commit/push is delegated to Singer in this workspace;
unrelated untracked files and sibling Git state remain excluded.

## Closeout

- Closed: 2026-09-26T07:00:17Z
- Outcome: succeeded (dev deployment accepted with known validation limits)
- Verified results: cold backup, runtime activation, index opening, 11 HTTP/API
  probes, authentication, blacklist actions, resumed indexing and helper recovery
- Limitations: detailed local latency cause and missing-slot JSON robustness
  remain unproven; no full search-validation pass or fleet readiness claimed.
  Overnight service continuity verified, not an exhaustive soak or rollback test.
- Follow-up / successor: dev-builder lead for separately scoped performance/JSON
  work if requested; retain protected complete backup
- Reusable knowledge saved: source-sync, backup/storage and memory-diagnosis
  records in Vestige; current status remains here rather than in memory
