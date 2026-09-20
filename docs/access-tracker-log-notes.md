# AccessTracker Query Log Reader Investigation

Initially investigated 2026-09-20 UTC (2026-09-19 America/Denver). The original
assessment is retained below; implementation and dev validation are now complete.

## Implemented Fix And Dev Validation

- Dev commit: `466e57146` (Fix malformed query history records and date range boundaries).
- Upstream commit: `3bb969ea4`, branch `fix/access-tracker-query-log`.
- PR: https://github.com/yacy/yacy_search_server/pull/831
- Files: `source/net/yacy/search/query/AccessTracker.java` and
  `test/java/net/yacy/search/query/AccessTrackerTest.java`.
- No new settings. Existing query history path remains `DATA/LOG/queries.log`.

The writer normalizes CRLF/CR/LF in the logged copy only. Strict shared header
validation skips malformed timestamps/counts during both seeking and decoding.
The iterative date search returns correct `[from, to)` boundaries, retaining
duplicate timestamps and the final record. Buffered, bounded UTF-8 streaming
replaces the whole-range byte-array allocation. Existing history is never
rewritten by the reader, and the original query passed to search is unchanged.

All 11 AccessTracker tests fail on the original reader/writer and pass with the
fix. Combined with QueryParams/GenericFormatter, 19 tests pass on both the dev
checkout and the current upstream integration. Dev `ant compileTest` passes.
Upstream `ant compile` passes, but its full `compileTest` target omits generated
Solr bridge JARs from its test classpath and fails in untouched Solr tests.
Adding `-lib lib` does not fix that target (`includeantruntime=false`); do not
repeat this workaround. Focused tests compile/run successfully with the full
`build/classes/java/main:lib/*:libt/*` classpath, as documented in the PR.

A one-million-record synthetic log with malformed continuations (47,445,630
bytes) returned correct 3,600-record and 100,000-record ranges in 62 ms and 117 ms
respectively under a 256 MB heap. These are local observations, not benchmarks
guaranteed on every node.

On Server2 dev, authenticated synthetic Solr submissions verified the old
writer produced multiple physical lines, with empty timeline/topic statistics.
After deployment using that history intact, the history page, access page,
timeline and local search returned HTTP 200; history/timeline contained useful
synthetic history again. The patched synthetic query matched 0 documents and
its complete text was logged on one physical line. The original 5,496,957 bytes
of pre-install history were verified as an unchanged prefix of the current log.

The probe must specify `df=text_t` and `q.op=AND`, with `rows=0`. Missing `df`
caused HTTP 400 on the initial attempts. Default OR matched many documents on
the baseline query; no result rows were retrieved. Do not use that baseline
request as an ongoing load-test recipe.

## Deployment And Rollback Record

Target: Server2 `/opt/yacy-dev`, `yacy-dev.service`, port 8091. The regular
`yacy.service` was already disabled/inactive and remains so. No fleet deployment.

- Installed 2026-09-20T03:24:06Z, new JVM PID 805810.
- Runtime backup: `/opt/yacy-dev/backups/access-tracker-20260920T030516Z/`.
- Backup includes `yacycore.jar`, `AccessTracker.java`, original history before
  synthetic tests, and `queries.log.before-install` after orderly shutdown.
- Baseline JAR SHA256: `080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9`.
- Installed JAR SHA256: `cb979066b5a501d27440cab24b89c801d19068b7c793b8387a57e5685f20a42c`.
- Exactly four `AccessTracker*.class` ZIP entries changed; all 1,650 other entries
  are byte-identical. The local full build was not deployed over the live JAR.
- HTTP 8091 confirmed 200 about three minutes after restart; the first check near
  one minute was too early. This is an observed upper bound, not exact startup
  latency. Allow roughly three minutes before the next initial check and refine
  the estimate from another measured restart.

For an authorized rollback, stop only `yacy-dev.service`, restore the backed-up
JAR and matching source as `yacy-dev:yacy-dev` mode 0644, start that service, and
verify HTTP 8091 after startup. Do not restore/truncate history as a code rollback:
synthetic malformed legacy entries are retained, and the original reader would
again be vulnerable to them. Keep the backups until the patch is established.

## Git And Upstream Boundary

The original isolated commit `785947528` was built/tested on upstream
`e171a4a0e`. Pushing that history required a GitHub workflow scope not present in
the existing OAuth token. The fork's master has custom commits, so it was not
synced or reset. Only the unpublished PR commit was rebased onto shared upstream
ancestor `94e8ac3b5`, producing `3bb969ea4`, which pushed successfully.

`git merge-tree --write-tree upstream/master 3bb969ea4` produced tree
`b1147d1e0abce3c1577c0d5f15da5423a428df24`, exactly the tested `785947528` tree.
The PR therefore integrates the same tested content and contains only the
AccessTracker source and synthetic tests, with no private reports or fleet work.

Ignored local evidence: `backups/access-tracker-fix-20260920/` contains logs,
benchmark/probe sources, JARs, generated patches, the isolated Git worktree and
the PR body. Remote helpers are in `/tmp/`; no credentials were copied into them.

Remaining limitations: the existing five-digit count ceiling and chronological
log-ordering assumption are retained. Exact reconstruction of old multiline
query text, optional diagnostic UI, and the upstream test-classpath issue remain
separate work. CI/maintainer review are external to dev validation.

## Diagnosis

The provisioner's private report describes HTTP 500 from
`AccessTracker_p.html?page=2` on standard Servers48 and 50. Those remote
observations were not repeated during this investigation. Current local
`AccessTracker.java` and `AccessTracker_p.java` match the report's source hashes.
The defect was reproduced locally using synthetic query text only.

Relevant source:

- `source/net/yacy/search/query/AccessTracker.java`: `addToDump` (line 175),
  `dumpLog` (196), `readLog` (242), `binarySearch` (304), `seekLB` (320),
  `readDate` (336).
- `source/net/yacy/htroot/AccessTracker_p.java`: optional seven-day top-word
  statistics in `respond` (270-289). The in-memory request table is built first,
  but a later exception aborts the response.
- `source/net/yacy/htroot/api/timeline_p.java`: another `readLog` caller, found
  through the code graph. This consumer has not been exercised end to end here.

The writer appends query text unchanged and then adds a record newline. CR/LF
inside a query therefore split one record into multiple physical lines.
The reader checks length and a space at offset 14, not a valid timestamp.
Its integer conversion precedes the exception handler, and its one-character
fast path subtracts 48 without validating the character.

The date-seeking helpers have an independent failure path: they assume the
line found at a byte offset starts with a timestamp. A malformed continuation
can instead cause an IOException and an empty history result before record
parsing even begins. Moving integer parsing into a catch is insufficient.

## Synthetic Verification

Compiled the current, unmodified `AccessTracker.java` with `javac --release 11`
into an isolated temporary directory, linking existing local dependencies.
No application files or real logs were written. Full `ant compileTest` and
HTTP checks were not run because this was an assessment without a patch.

| Fixture | Observed current behavior |
| --- | --- |
| One query containing CRLF, LF and CR | One buffered record contains four physical lines |
| Nonnumeric continuation inserted at each of 63 positions in a 64-record file | 52 uncaught NumberFormatExceptions; 11 empty results after date-seek IOExceptions |
| One-character count `x` | Accepted as count 72 |
| Malformed first line followed by 64 valid records | Empty history |
| February 30 timestamp away from seek positions | Accepted by the line parser |
| 64 valid records, requested range covering the entire file | 63 returned; final record omitted |
| One valid record, requested range covering the entire file | Zero returned |
| Valid minute-spaced records; range 00:10:30 inclusive to 00:20:30 exclusive | Records 10 through 19 returned, rather than 11 through 20 |

The interval bug is independent of malformed text: the seek algorithm finds
the preceding record rather than the documented lower bound and does not
return EOF when the upper bound lies beyond the last record.

Ignored local harness:
`backups/access-tracker-review-20260920/AccessTrackerProbe.java`.
Temporary harness, classes and fixtures:
`/tmp/yacy-access-tracker-review-20260920/` (not durable or portable).

Reproduction from the repository root:

```sh
javac --release 11 -cp 'build/classes/java/main:lib/*' -sourcepath /tmp/yacy-access-tracker-review-20260920 -d /tmp/yacy-access-tracker-review-20260920/classes source/net/yacy/search/query/AccessTracker.java backups/access-tracker-review-20260920/AccessTrackerProbe.java
java -cp '/tmp/yacy-access-tracker-review-20260920/classes:build/classes/java/main:lib/*' AccessTrackerProbe /tmp/yacy-access-tracker-review-20260920/fixtures
```

These commands assume local dependencies and previously compiled support
classes exist. The probe demonstrates current failures; it is not a passing
regression suite or production build procedure.

## Recommended Patch Boundary

1. Normalize CR/LF only in the copy written to query history. Leave the actual
   search query unchanged and preserve Unicode and ordinary query text.
2. Share strict header validation between record reading and date seeking.
   Reject malformed dates/counts without aborting reading of valid later rows.
   Remove the unsafe character shortcut and avoid broad Throwable catches.
3. Correct the documented `[from, to)` boundaries, including EOF, one-record
   files, equal timestamps, and intervals between records. Retain efficient
   reading for large logs; do not silently replace seeking with an unbounded
   whole-file scan on every page request.
4. Check whether a narrow fallback is still needed for optional page statistics
   after the reader is robust. Do not conceal unrelated exceptions with a broad
   page-level catch. The current reader already catches IOExceptions, so any
   diagnostic UI would need deliberate error reporting rather than assuming
   the caller can distinguish a read failure from genuinely empty history.

The five-character count limit and the writer's long-valued count require a
compatibility decision. Check all writer callers for negative sentinel values
before adopting unsigned-only validation. Avoid broadening the first patch
into unrelated logging/concurrency changes. Historical multiline records
cannot always be reconstructed unambiguously; the fix must preserve the file
and recover valid records without claiming exact reconstruction.

Add public regression tests using synthetic fixtures for the confirmed cases,
plus empty files, truncated tails, malformed rows around interval boundaries,
duplicate timestamps, valid UTF-8 and integer limits. Check ordering assumptions:
buffered searches carry their original start timestamps, so append order is not
automatically proof of strict chronological order.

## Validation And Upstream Plan

- Back up affected files, implement locally, run `ant compileTest` and focused
  reader/writer tests. Exercise the page handler with malformed persisted
  history and check the timeline consumer.
- Test large synthetic history to ensure recovery does not create unacceptable
  page latency or memory use. Validate unchanged search query semantics.
- Agree the dev/canary scope before deployment; preserve runtime backups and
  production history. No log truncation as a substitute for fixing the reader.
- For the eventual upstream submission, refresh upstream and use a separate
  branch/worktree based on it. Include only the AccessTracker fix and public
  regression tests, excluding local crawler/fleet features and private reports.
  Current upstream state was not checked during this local assessment.

No commit, deployment, fleet change or pull request was made in the initial review.
See the [closed investigation work order](work-orders/completed/20260920T025230Z-access-tracker-review.md).
