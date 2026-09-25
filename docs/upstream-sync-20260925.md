# Custom Fork Synchronization: 2026-09-25

This is a source integration, not a deployment. No running YaCy installation,
bridge service, fleet node or index was changed.

## Revisions And Recovery

- Custom source before documentation checkpoint: `14781c7a15532c7d7794aab941ab4db15d47f3c8`.
- Reviewed documentation checkpoint: `9b630a94025531b49d0f4ae21b90f2d5267bb445`.
- Pinned upstream master: `b50b76bd552a851f737b98683f20d8b861e137ae`.
- Merge base: `94e8ac3b5f9080489eae4ed9efa1e760a983857c`.
- Candidate branch: `integration/upstream-20260925`, separate ignored worktree.
- Merge subject: `Merge upstream master b50b76bd into the custom YaCy fork`.
  Final commit/tree and publication receipts are in work order
  `20260925T163717Z-upstream-sync.md`.
- Local recovery tag: `checkpoint/pre-upstream-sync-20260925` at `9b630a940`.
- Timestamped source/documentation archives and conflict originals are under
  `backups/upstream-sync-20260925T163717Z/pre-change/` (ignored).

History is merged, not rewritten. Existing contribution branches and local
`master` are unchanged. Recovery should start from the checkpoint in a separate
worktree; do not force-reset a published branch or copy an old jar over a newer
runtime. No runtime rollback is needed for this source-only operation.

## What Was Retained

Compared ordered custom added/removed lines before/after integration, excluding
Git headers and hunk coordinates. Of 46 custom source/template/test files,
44 retain identical custom deltas. This is textual evidence, not proof of every
runtime interaction. The two differences are understood:

- `RemoteInstance.java` now exactly matches upstream, which includes our accepted
  thread-evictor fix. No duplicate implementation is added.
- `yacysearchitem.java` retains custom response handling while accepting upstream
  snapshot and obsolete authentication-debug removal. Required custom imports,
  display values, suppression guards and heuristic suppression remain.

`AGENTS.md` was an add/add conflict: both our project policies and upstream's
help/localization and scoped-test requirements are preserved. No whole-file
ours/theirs resolution was used. All remaining source changes auto-merged;
the crawler/search overlaps received independent review.

| Catalog group | Verification |
| --- | --- |
| R1 thread lifecycle | Byte-identical to upstream accepted fix |
| R2 search-history repair | AccessTracker regression tests |
| R3/R4 search continuity and rejected-row paging | Exact custom-delta retention and independent SearchEvent/Switchboard review |
| M1-M5 metadata recovery and Y1-Y4 YouTube handling | Parser regressions and exact custom-delta retention |
| Q1-Q5 recrawl quality, rejection and failure-marker purge | MetadataQuality/ErrorCache tests plus retained Switchboard hooks |
| C1-C6 soft/poison/whitelist rules, audit/undo and entry anchor | Content-rule tests; retained templates, handlers and reviewed crawler JS |
| D1/D2 dead/unresolvable domains | Retained code and reviewed crawler/deferred-cleanup integration |
| D3 remote crawl persistence | Retained custom delta and reviewed Protocol/CrawlQueues changes |
| U1 duplicate crawl button | Real-browser synthetic form checks at three widths |
| U2 local client-response policy | Reviewed renderer conflict; no real peer requests |
| U3 blacklist explanations/actions | Blacklist tests, JS syntax and editor-return scenarios |

## Validation

OpenJDK 21.0.12, Ant 1.10.14, JUnit 4.13.2, Node.js 22.17.1.
Fresh candidate application compilation passed. The focused suites use fresh
output directories first on the classpath, with `--release 17` and
`build/classes/java/main:lib/*:libt/*` dependencies.

| Gate | Result |
| --- | --- |
| `ant compile` | PASS |
| Custom/blacklist/FileUtils suite, 10 classes | 91 tests passed |
| `ant jetty12-server-test`, explicit six-class JUnit rerun | PASS; 54 tests passed |
| Additional upstream crawler/peer/auth/header/config suites, 11 classes | 40 tests passed |
| CrawlSwitchboard with isolated synthetic-host DNS | 5 tests passed on candidate and pristine upstream |
| JavaScript syntax: Crawler.js and BlacklistTest.js | PASS |
| Blacklist editor-return event harness | Six scenarios passed; stubbed DOM, not browser E2E |
| Crawl-start browser fixture, 1440/1024/390 pixels | Both buttons submit the same 22 fields; no added overlap/overflow |

Total: **190 passing candidate JUnit tests**, not the complete repository suite.
The crawl fixture blocks external requests and rejects POSTs; no real crawl was
submitted. Existing mobile overflow (710px at 390px viewport) and missing
generated `env/style.css` occur on both fixtures. Screenshots were reviewed.
Temporary browser and fixture server were stopped.

### Known Full-Suite Failure

`ant compileTest` still fails: **94 errors on the candidate, 89 on pristine
upstream**. All 89 upstream error headers are present in the candidate. The
five extras are confined to `MetadataQualityTest` and stem from the same missing
Solr classes in the upstream test classpath. That custom class compiles and
passes with the complete classpath used above. Upstream also has the stale
`HostBalancerTest` constructor call. These failures were not repaired or hidden;
there is no claim of a full-suite pass.

An initial supplementary run was stopped while `CrawlSwitchboardTest` waited on
DNS for thousands of synthetic `.test` names. Its thread dump identifies
`persistedActiveRootsProtectGraphUntilCrawlStops` via `Domains.dnsResolve`.
Rerunning with `-Djdk.net.hosts.file=<ignored localhost-only fixture>` completed
on both revisions. This changes only that JVM's resolver, not system DNS or
repository code, and is not a real-network test.

## Deployment Limits And Next Checkpoint

Upstream includes major infrastructure changes: Jetty 12, the Solr 9 bridge,
ordinary-user authentication retirement and snapshot removal. In particular,
the renderer's authentication exemption is now administrator-only, consistent
with upstream. Do not assume old elevated-search user accounts still provide
the same contract on this new base.

Before any deployment, plan a separately authorized dev canary with full runtime
dependency/configuration backups and a compatible rollback. Do not use an old
jar-only fleet patch procedure for this upgrade. Check authenticated admin
actions and rejection of unauthenticated/invalid-token actions, mixed-peer
ranking/pagination, crawler shutdown/cooldowns, deferred cleanup during/after
searches and accepted recrawl start-host tracking. Existing indexes have not
been opened by this candidate; migration compatibility remains untested.

PR 834's previously documented help/localization follow-up remains separate;
this synchronization does not update or broaden any upstream PR.

## Evidence

Ignored directory `backups/upstream-sync-20260925T163717Z/` contains the candidate,
archives, Ant/JUnit logs, full-test diagnostic comparison, custom-delta report,
independent documentation/merge reviews, browser fixtures/screenshots and test
thread dump. The exact pristine baseline is reused from
`backups/pr834-current-integration-20260925T152746Z/baseline/`; test compilation
was rerun during this task. Evidence may be absent in another checkout.
