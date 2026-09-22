# Blacklist Test Match Details

`BlacklistTest_p.html` now displays every matching active host/path rule, with
the source blacklist filename and purposes it applies to (Crawling, DHT, News,
Proxy, Search and Surftips). Copies in different files have separate rows.
Header and action tooltips explain the controls. Existing blocked/not-blocked
results remain.

## Semantics And Boundaries

- Rules are the normalized patterns loaded by YaCy, not necessarily byte-for-byte
  copies of their original file lines. The loaded engine merges active files;
  the diagnostic reads current active files on demand, mirrors loader
  normalization and intersects each source with its active purposes. It does
  not guess missing filenames or claim historical load-time provenance.
- Original source text is retained for actions. Different spellings of one
  normalized rule have separate rows; exact duplicate lines in one file share
  one row. Missing/unreadable/unsafe sources are reported, with actions disabled.
- Diagnostics use the existing split host/path matcher, including its exact,
  prefix/suffix, wildcard and regex host behavior. They do not approximate a
  blacklist entry with a full-URL regular expression.
- Path pattern flags and the URL components are the same as ordinary matching.
  Display escaping preserves percent escapes, plus signs and backslashes.
- A positive URL hash cache can still block a URL after its current rule is gone.
  If no active rule explains that cached block, an explicit warning identifies
  the affected purposes instead of inventing a rule. Testing alone does not
  clear caches, edit rules, change active-list settings or modify the index.
- Exhaustive matching is only used by the authenticated administrator test
  page. The existing crawl/search boolean matching methods are unchanged.
  Very large lists or expensive operator-supplied regexes can make this manual
  diagnostic slower than a normal first-match lookup.

## Edit And Delete

- Edit posts the exact source entry and filename to the existing `Blacklist_p`
  selected-entry editor in a new tab/window. Saving uses native YaCy behavior.
  Returning focus to the test page re-tests using GET, not a repeated POST.
  The script arms on Edit submission, records the page losing focus, and performs
  one `location.replace` when focus returns. It does not poll or inspect the
  editor; closing the tab or simply switching back both trigger the re-test,
  including when the user did not save any changes.
- Delete first shows a separate warning naming the entry and file. The user must
  check a confirmation box; Cancel is a read-only GET. Confirmed deletion removes
  this exact source spelling (including identical copies) from this file only.
  Other spellings, other entries and copies in other files remain.
- Both deletion stages require administrator authentication, POST and a valid
  transaction token, including localhost requests. The server revalidates the
  matching entry and whole-file revision; stale, inactive, forged and replayed
  selections are rejected. No deletion is performed merely by visiting a link.
- The older shared deletion helper can remove normalized variants. This tool
  instead uses a narrowly scoped exact-source deletion with atomic replacement,
  retaining other lines, line endings and permissions. Unsafe filenames,
  symlinks, stale revisions and lossy character decoding are refused. Revision
  checks detect intervening edits; this is not a new cross-process file-locking
  protocol for all existing YaCy writers.
- After deletion, native clear/reload refreshes loaded rules and caches, and a
  redirect re-tests the URL. Like other manual blacklist editing, reload may
  clear active search events. A remaining duplicate can still block the URL.
- Bare-host legacy lines without an explicit `/path` remain attributed but have
  actions disabled: the existing editor cannot safely round-trip that syntax.
  Inspect these in the main blacklist editor. No rewrite is performed silently.

## Patch And Verification

Implementation: `source/net/yacy/repository/Blacklist.java` (additive diagnostic
API), `source/net/yacy/htroot/BlacklistTest_p.java` and
`htroot/BlacklistTest_p.html`. Tests:
`test/java/net/yacy/repository/BlacklistMatchingRulesTest.java` and
`test/java/net/yacy/htroot/BlacklistTestPageTest.java`.

```sh
ant compileTest
java -cp 'build/classes/java/main:test/java:lib/*:libt/*' org.junit.runner.JUnitCore \
  net.yacy.repository.BlacklistTest \
  net.yacy.repository.BlacklistMatchingRulesTest \
  net.yacy.repository.BlacklistDiagnosticsTest \
  net.yacy.htroot.BlacklistTestPageTest
```

September 22, 2026: compilation and all 16 focused tests pass, including real
template rendering, overlapping rules, matching parity, non-matches, encoded
paths, HTML escaping, invalid URLs and cache-only presentation. Tests also pass
with the patched runtime JAR first in the classpath. Only two JAR entries change;
all 1,652 other entries retain identical contents.

Local known-good backups and ignored validation artifacts:
`backups/blacklist-match-details-20260922/`. Dev runtime backup:
`/opt/yacy-dev/backups/blacklist-match-20260922T010707Z/`.
The guarded installer targets only `yacy-dev.service`, checks the original JAR,
and backs up the JAR, both Java source files and the HTML template before stopping
the service. Restore those four saved files together while dev is stopped, then
start dev and verify port 8091. Never install a whole unrelated local build over
the runtime JAR or change the standard instance as part of this rollback.

Live verification: existing root/subdomain entries produced two rows with a
matching heading count; an unblocked synthetic URL produced no rows; malformed
input produced the existing invalid-URL message. All returned HTTP 200, without
servlet errors. First matched lookup took 1.31 s; the nonmatch took 0.61 s.
These are single observed timings, not performance guarantees. No rule was added
for that first-stage testing. User accepted the original matcher display;
checkpoint `b41f7f185` was committed and pushed before the next stage.

### Filename Attribution And Actions Follow-Up

Local backup: `backups/blacklist-rule-actions-20260922T014701Z/`.
Attribution was verified first with 23 tests, then actions added. Compilation
and all 29 tests pass against both compiled classes and the patched runtime JAR.
The JAR changes only `BlacklistTest_p.class`, adds `BlacklistDiagnostics` and
its two nested classes, and preserves 1,653 other entries exactly. New source
is `source/net/yacy/repository/BlacklistDiagnostics.java`; browser focus handling
is `htroot/js/BlacklistTest.js`. Native editor and crawl/search matchers unchanged.

Runtime backup: `/opt/yacy-dev/backups/blacklist-actions-20260922T014701Z/`.
Restore saved JAR, handler source and template together while dev is stopped;
remove newly introduced files listed in `absent-files` (diagnostic source and
JS) when rolling back. Do not replace unrelated runtime classes or production.
Live action verification and final deployment state are recorded in the work
order and central change log.

Live verification passed on dev with two temporary `.invalid` blacklist fixtures:
per-file attribution, cancellation, rejected GET/wrong token/missing consent,
stale selection rejection, confirmed scoped deletion, duplicate-file blocking,
redirect/retest, replay rejection, and native editor open/save. Both fixture
files and activation entries were removed afterward. Unauthenticated access to
the test page returns HTTP 401; dev homepage returns HTTP 200.

Chrome captured-response tests verified opening the selected native editor in
a new tab, returning to a GET retest, required consent and cancellation. These
browser requests were intercepted, not live mutations. Screenshots at 1280 and
390 pixels were inspected; the final mobile template stacks labeled fields and
has no horizontal overflow. Final JAR/template/JS hashes match local artifacts.
User accepted all new functions, including editing and deletion, on September 22,
2026. The 29 focused tests were re-run successfully for the accepted checkpoint,
`Attribute blacklist matches and add rule actions`. Upstream submission remains
a separate user decision.

### Restart Observation

This dev restart took about twelve minutes to become web-ready, not the roughly
three minutes seen previously. The existing service reached its two-minute
stop timeout. Main-thread samples then showed native `Cache.init` rebuilding
the existing approximately 1.3 GiB HTTP-cache header index and subsequent cache
body indexes at `/mnt/yacy-dev-htcache`; the new diagnostic was not executing.
Wait for HTTP readiness and verify startup progress before repeating a restart.
Review graceful-stop allowance before the next busy-node deployment. No timeout,
cache setting, cache file or index file was manually changed as part of this task.

Follow-up deployment: directly invoking `stopYACY.sh` made systemd invoke its
ExecStop again after JVM exit. That second call returned 1 because `yacy.running`
was already gone, triggering `Restart=on-failure`. The install guard detected
the new PID and refused to change files. Use `systemctl stop` for service-managed
shutdown. A temporary `/run/systemd/system/yacy-dev.service.d/` stop-time allowance
was used for the subsequent install, then removed; no permanent unit change.
