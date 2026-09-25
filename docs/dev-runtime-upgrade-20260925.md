# Dev Runtime Upgrade: 2026-09-25

Status: new runtime installed and running; search validation remains unresolved.
Do not describe this as a fully verified upgrade or fleet-ready build. See the
[active work order](work-orders/active/20260925T175946Z-dev-runtime-upgrade.md)
before resuming. No search-code changes or rollback have been performed.

## Scope And Build

User authorized deploying the synchronized custom source to Server2 dev only,
including necessary prerequisites. Source `27152b94a` contains merge `975a92f51`
of upstream `b50b76bd`. Standard `/opt/yacy` and the fleet are excluded.

The installed dev baseline uses Java 21.0.12.1 and Solr 9.0.0. The candidate uses
Solr 9.10.1/Lucene 9.12.3, the relocated private Jetty 10 client bridge, and public
Jetty 12.0.37. Java already satisfies the minimum; no OS package upgrade needed.
Do not mix old Solr/Jetty jars with the new package: the startup script loads
all jars in `lib/`.

Build command: `ant -Drelease=backups/dev-runtime-upgrade-20260925T175946Z/release dist`.
Archive: `yacy_v1.942_202609251109_27152b94a.tar.gz`.
SHA256: `9a3c31321760ced21b4173b4081e28b679bfa3c4e6673f4160fcfa54d3bea485`.
Local and uploaded hashes matched. The generated version-properties file is
part of the package. Root `help/` is omitted by this upstream packaging target,
but the current static Help endpoint does not load those Markdown files.

## Data And Recovery Boundary

Preflight found approximately 543 GiB in dev DATA, including 509 GiB in INDEX,
plus 2.3 GiB external HTTP cache. About 1.2 TB was free on the shared ext4 filesystem.
The user confirms mechanical drives in RAID-1. Early cold-copy throughput was
about 50 MiB/s; allow hours for this dataset. Temporary backup-job resource
weights IOWeight=10 and CPUWeight=20 favor other services during the copy.
The standard service was already inactive/disabled; preserve that state.

A read-only memory check during the backup found 31 GiB total, 3.6 GiB used,
28 GiB buffer/cache and 27 GiB available. Both YaCy services were stopped, but
a separate `opensearch.service` still used about 1.7 GiB resident memory.
Four sampled vmstat intervals showed no swap traffic; memory-pressure averages
were zero while I/O pressure was substantial. Low free RAM alone was not memory
exhaustion: the backup occupied reclaimable filesystem cache and write buffers.
No cache clearing, swap changes or changes to OpenSearch were made.

Root-only remote staging/backup directory:
`/opt/yacy-dev-upgrade-20260925T175946Z`.
Cold backup covers the full installation, `/mnt/yacy-dev-htcache`,
`/var/lib/yacy-dev`, and the original dev systemd unit. Settings, blacklist rules
and private logs remain in protected server backups, never Git.

Use `systemctl stop yacy-dev.service`, not direct `stopYACY.sh`, to avoid the
previous double-stop/restart race. Temporary runtime drop-in
`/run/systemd/system/yacy-dev.service.d/90-upgrade-stop.conf` permits graceful
shutdown without the existing two-minute forced-kill deadline. Remove that
exact drop-in after activation/recovery and daemon-reload. Boot enablement is
not changed. Graceful stop completed 2026-09-25T18:07:38Z.

The live Solr defaults match the candidate. Keep `solr_9_0`, schema 1.4 and
luceneMatchVersion 9.0 unchanged; jar versions alone do not require renaming or
reindexing. Existing-index opening is a live validation gate, not proven by
fresh-fixture tests. Older software may not read newer writes: rollback must
restore the old application/configuration and corresponding cold data together.
Never repair individual Lucene segment files or use the obsolete destructive
`bin/checkindex.sh` tool for this upgrade.

Backup success requires the unit to finish successfully, the `backup-complete`
marker, and empty rsync dry-run comparison receipts for install/cache/home.
Copy integrity is provided by rsync; metadata comparison is not a separate
byte-by-byte checksum audit of the full index. Retain the backup after success.

The 546 GiB cold backup completed at 21:14:17 UTC, about 3 hours 7 minutes after
graceful shutdown. The unit exited successfully and all three comparison
receipts were empty. Activation completed at 21:16:11 UTC. The installed
`yacycore.jar` SHA256 is
`5d99c1570d9ede418a00f297db64499c1b4b4195c179d900613e8f58853d786c`.
Runtime checksum comparison passed; config and blacklist lists matched the
backup before startup. The temporary stop-timeout override was removed after
startup, restoring the original two-minute unit value without another restart.

Full directory replacement also displaced server-local `bin/` helpers absent
from the distribution. Restored unchanged and compared against the backup:
`interleave_yacy_recrawl_batches.py`, `start_yacy_file_crawl.py`, and
`yacy_recrawl_low_watermark_feeder.py`. The omitted `searchprofiling` helper was
also preserved after verifying it matches current source exactly. Future full
deployments must inventory and preserve local operational helpers explicitly.

The existing enabled `yacy-recrawl-feeder.service` has
`Requires=yacy-dev.service`; it stopped automatically with YaCy. Its prior
running state was restored at 21:23:39 UTC with the original helper and settings.
No second feeder was launched. Subsequent checks found no restart or new feeder
errors. Standard `yacy.service` remained inactive/disabled; OpenSearch unchanged.

## Verification

- Full distribution build passed; archive hash matched after upload.
- Existing source integration passed 190 focused tests; known full compileTest
  baseline/classpath failures remain documented in the synchronization report.
- Dependency-isolation guard and four embedded Solr startup/update/query/reopen
  tests passed. Their disposable repository-local `test/DATA` fixture is never
  pointed at a server index or its backup.
- Pre-upgrade authenticated 11-page/API probe passed; collection 25,628,999,
  webgraph 0. These are live pre-stop observations, not an exact frozen count.
- After the 185-second initial delay, external dev HTTP returned 200. All 11
  authenticated probes passed without ServletException; index 25,631,634,
  webgraph 0. Index counts are live and naturally change during crawling.
- Unauthenticated admin request returned 401; generated stylesheet returned 200.
- Synthetic duplicate-file attribution, native selected editing, confirmed
  file-scoped deletion, consent/token/GET/stale-selection/replay guards passed.
  Both fixture lists and their activation were removed afterward.
- Post-start sample: 182 JVM threads, 2 connection evictors, 2,561 indexing log
  messages, no severe log entries. Routine network/parser warnings were present.
- Browser homepage rendered correctly. No JavaScript errors observed in the
  sampled search page, but search-row validation failed intermittently as below.

## Unresolved Search Validation

Synthetic local-only `yacy` searches with `maximumRecords=10&verify=false`:

- Without `contentdom`, first response had counts/pagination but no result rows.
- With `contentdom=text`, first page rendered ten rows.
- With `contentdom=text&startRecord=10`, second page had no rows, including a
  repeat. A later repeat of the original unspecified-content-domain URL returned
  nine rows. Changing parameters alone therefore does not establish the cause.
- A two-result JSON request contained a leading comma when its first result
  was absent. The `verify=ifexist` variant parsed but had zero items.

Server diagnostics show result-slot timeouts (10 seconds for item zero, one
second for later local items) while feeds were still running. Sampled search
threads waited on disk-backed RWI `HeapReader` reads/locks. Direct authenticated
Solr `text_t:yacy` queries returned 1,593 matches; ten IDs returned in 0.374 seconds
(Solr QTime 246 ms). Later host samples showed 18 GiB available RAM, no interval
swap traffic and appreciable I/O wait. No severe search-time exceptions appeared.

This is evidence of a YaCy result-materialization delay, not proof of memory
exhaustion, index corruption, startup-only behavior or an upgrade regression.
The upstream merge changed HeapReader substantially; old/new live comparative
testing has not established causation. JSON templates were unchanged by the
merge, but that alone does not prove the live JSON symptom is pre-existing.
Do not repair this by changing templates, extending timeouts or clearing data
without a focused diagnosis. The user has been asked whether to continue
investigation on the upgraded dev runtime or restore the coherent old backup.

No overnight soak, fleet deployment or successful rollback rehearsal claimed.
The ordinary full compileTest failures remain as documented, not a full-suite
pass. Search validation must be resolved before declaring this assignment done.

Ignored local evidence: `backups/dev-runtime-upgrade-20260925T175946Z/`.
Remote helper scripts and markers live beneath the protected upgrade directory.
Read the work order for exact running-job identities before taking any action.
