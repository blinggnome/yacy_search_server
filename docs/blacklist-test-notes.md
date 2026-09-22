# Blacklist Test Match Details

`BlacklistTest_p.html` now displays every matching active host/path rule, with
the blacklist purposes it applies to (Crawling, DHT, News, Proxy, Search and
Surftips). Identical rules are grouped in one row, sorted for comparison. Header
tooltips explain the columns. Existing blocked/not-blocked results remain.

## Semantics And Boundaries

- Rules are the normalized patterns loaded by YaCy, not necessarily byte-for-byte
  copies of their original file lines. The loaded engine merges active files
  and does not retain source-file provenance; this page does not guess filenames.
- Diagnostics use the existing split host/path matcher, including its exact,
  prefix/suffix, wildcard and regex host behavior. They do not approximate a
  blacklist entry with a full-URL regular expression.
- Path pattern flags and the URL components are the same as ordinary matching.
  Display escaping preserves percent escapes, plus signs and backslashes.
- A positive URL hash cache can still block a URL after its current rule is gone.
  If no active rule explains that cached block, an explicit warning identifies
  the affected purposes instead of inventing a rule. This tool does not clear
  caches, edit rules, change active-list settings or modify the index.
- Exhaustive matching is only used by the authenticated administrator test
  page. The existing crawl/search boolean matching methods are unchanged.
  Very large lists or expensive operator-supplied regexes can make this manual
  diagnostic slower than a normal first-match lookup.

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
for testing. User acceptance remains pending.

### Restart Observation

This dev restart took about twelve minutes to become web-ready, not the roughly
three minutes seen previously. The existing service reached its two-minute
stop timeout. Main-thread samples then showed native `Cache.init` rebuilding
the existing approximately 1.3 GiB HTTP-cache header index and subsequent cache
body indexes at `/mnt/yacy-dev-htcache`; the new diagnostic was not executing.
Wait for HTTP readiness and verify startup progress before repeating a restart.
Review graceful-stop allowance before the next busy-node deployment. No timeout,
cache setting, cache file or index file was manually changed as part of this task.
