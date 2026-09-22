# YaCy Dev Node Change Log

This is the central index for local YaCy dev-node changes made in this fork.
Topical notes can still live in focused files such as
`docs/parser-metadata-notes.md` and `docs/remote-crawl-notes.md`, but every
meaningful local feature, fix, fleet patch, or handoff should have a short entry
here.

For a plain-language overview grouped by benefit, contribution readiness and
future ideas, start with the [Dev Node Improvement Catalog](dev-node-improvement-catalog.md).

Each entry should answer:

- What behavior changed?
- Which commit or patch boundary carries it?
- Which files or settings matter when testing or rolling back?
- What verification or rollout note should a future agent look at first?

## 2026-09-22: Attribute Blacklist Matches And Offer Scoped Actions

Patch boundary: `Attribute blacklist matches and add rule actions`, following
`b41f7f185`. User verified all new functions, including edits and deletions,
on September 22, 2026. No upstream PR or fleet changes.

- Blacklist Test now attributes each source entry to its active file and purposes.
  Duplicate files/spellings remain distinct; missing sources get warnings instead
  of guessed attribution. Original source text is retained for editing/deletion.
- Edit opens the existing selected-rule editor in a new tab/window. Returning
  focus re-tests the URL. Delete requires a separate warning and consent checkbox,
  administrator authentication, POST, token and current file revision. It removes
  only the selected source spelling from the named file, reloads native blacklists
  and redirects to a fresh test. No rule removal via GET or silent stale deletion.
- Changes: `BlacklistDiagnostics.java`, `BlacklistTest_p.java`, its HTML and new
  `htroot/js/BlacklistTest.js`; diagnostic/page tests. Matching hot path and native
  editor unchanged. Bare-host source entries without `/path` have no actions.
- Attribution gate: 23 passing tests before continuing. Full gate: `ant compileTest`
  and 29 tests passing, including packaged runtime. One JAR class replaced, three
  added, 1,653 unrelated entries byte-identical.
- Local backup `backups/blacklist-rule-actions-20260922T014701Z/`; remote backup
  `/opt/yacy-dev/backups/blacklist-actions-20260922T014701Z/`. Dev-only deployment
  and live verification tracked in the corresponding work order; production
  remains untouched. Restore saved files plus remove recorded new files to roll back.
- [Behavior and limitations](blacklist-test-notes.md); catalog U3 updated.
- Deployed at 02:09:08 UTC; web-ready HTTP 200 verified. Runtime artifact hashes
  match local. Synthetic live tests passed attribution, confirmation/cancel,
  stale/invalid/replayed requests, scoped deletion with duplicate-file blocking,
  and native edit/save. Fixtures and activation entries removed; unauthenticated
  administrator-page request returns HTTP 401. Chrome intercepted-response checks
  pass new-tab behavior, consent/cancel and layout at 1280/390 px. Final template
  mobile adjustment installed without a restart; all 29 tests still pass.
- Initial installer aborted before writes when direct API shutdown triggered
  systemd auto-restart via a failing second ExecStop. The successful installer
  used service-managed stop with a temporary runtime 900-second allowance; that
  override was removed and original two-minute timeout verified restored. No
  cache/index modifications or standard-service changes.
- Acceptance checkpoint: all 29 focused tests re-passed. Auto-retest uses browser
  blur/focus events after Edit, not polling or editor-close detection. Switching
  back without closing also re-tests once, even if no change was saved; it uses
  a fresh GET rather than resubmitting the editor POST.

## 2026-09-22: Show Matching Rules In Blacklist Test

Patch boundary: `b41f7f185`, `Show matching active rules in blacklist test`;
user confirmed working, committed and pushed. No upstream PR or fleet rollout.

- `BlacklistTest_p.html` now shows every matching active host/path pattern with
  the purposes it applies to. Duplicate patterns share one sorted row. Existing
  blocked categories remain, with a separate warning for cache-only blocks.
- `Blacklist.getMatchingRules` reuses the real host/path matcher without changing
  normal crawl/search matching or the URL hash cache. Loaded normalized patterns
  are shown; the engine does not retain source blacklist filenames.
- Handler/template rendering preserves percent escapes, plus signs and regex
  backslashes, escapes HTML, and provides column tooltips and wrapping rule text.
- `ant compileTest` and 16 focused JUnit tests pass. Packaged runtime tests pass;
  exactly two JAR entries changed with all 1,652 others byte-identical.
- Dev-only install completed at 01:11:12 UTC. Runtime JAR, Java sources and HTML
  backed up together under
  `/opt/yacy-dev/backups/blacklist-match-20260922T010707Z/`. Standard instance
  remains inactive/disabled and untouched. HTTP 200 verified after startup;
  authenticated tests returned two overlapping rules, no match and invalid input
  correctly, without servlet errors. Deployed source/template hashes match local.
- Restart observation: existing two-minute stop timeout was reached; startup
  rebuilt the large existing HTTP cache before opening the web port, about
  twelve minutes after start. No second restart or manual cache/index repair.
  Review graceful-stop allowance before the next busy-node deployment.
- No blacklist edits, activation changes, cache clears or index operations.
  Local backups/evidence: `backups/blacklist-match-details-20260922/`.
- Chrome captured-response checks at 1280/390 px show both rules and purposes,
  working tooltips and wrapping long patterns; existing mobile form overflow is
  unchanged. Temporary browser and fixture server stopped after verification.
- Behavior, tests and rollback: [blacklist test notes](blacklist-test-notes.md).
  Added catalog entry U3; work order
  `docs/work-orders/completed/20260922T005313Z-blacklist-match-details.md`.

## 2026-09-21: Crawl-Start Button Submitted Upstream

PR: https://github.com/yacy/yacy_search_server/pull/832
Original dev commit: `5e4c8ca1d`. Isolated PR commit: `fdfc741d3`.
Local documentation checkpoint: `Record crawl-start button upstream PR`.

- Added button remains the same tested one-line change to
  `htroot/CrawlStartExpert.html`; no new dev-runtime changes or deployments.
- PR is open and ready for review. GitHub read-back confirms one commit, one
  file, +1/-0 and the exact reviewed public description. Initial CI status:
  `build` and `build-and-release` in progress, not yet claimed passing.
- Current upstream integration was tested at `de973ca4444912ecfe8682dedc9e14842f8a4d57`.
  To avoid requiring additional workflow-token scope, publication uses shared
  ancestor `94e8ac3b5`; merge-tree equals tested integration tree
  `994f5af4f59b0bf10e515aba84a71172fc5a0764`. Fork master was not reset or synced.
- Chrome/YaCy-template fixture at 1440, 1024 and 390 px: both submit controls
  send identical 22-field data including URLs, depth and `crawlingstart=1`.
  No overlap or added horizontal overflow. Existing narrow-screen overflow
  remains outside scope. No actual crawls were submitted during these checks.
- Fixture used repository assets, without generated/custom `env/style.css`.
  No Java compilation gate was needed for this template-only patch. Existing
  live dev testing is distinguished from today's synthetic fixture checks.
- Ignored evidence, screenshots, public body and worktrees:
  `backups/crawl-start-button-pr-20260921/`. Temporary test services stopped.
- User decision recorded in the catalog: other unsubmitted improvements need
  more testing and a separate publication decision. None are included in PR 832.
- Work order: `docs/work-orders/completed/20260921T230257Z-crawl-start-button-pr.md`.

## 2026-09-21: Human-Readable Improvement Catalog And Contribution Review

Documentation only; application checkpoint remains `eaad79496`. Checkpoint
subject: `Catalog dev node improvements and contribution priorities`.

- Added `docs/dev-node-improvement-catalog.md`, grouping all 37 retained
  functional commits since `94e8ac3b5` into user-facing capabilities, with a
  source/commit appendix and separate retired/out-of-scope experiments.
- Cross-checked current source, existing tests, topic notes and Git history.
  PR 809 is merged; PR 831 remains open with passing build checks as of review.
- Separated existing future plans from new ideas, including safer error/DNS
  cleanup, truthful deferred-deletion counts, cancellation/recovery, metadata
  freshness, concurrency tests and upstream patch isolation.
- Corrected the historical HTTP-error description below: the current predicate
  covers status >=400, not only definitive permanent failures. No policy change
  was made. The catalog explicitly flags this for review.
- No application edits, test reruns, deployments, node probes or sibling Git
  changes. Validation and final checkpoint are recorded in the catalog work
  order under `docs/work-orders/`.

## 2026-09-21: Sanitized AccessTracker Production Evidence Published

Documentation/publication only; source patch and PR code unchanged. Checkpoint
subject: `Record sanitized AccessTracker production validation`.

- Published aggregate results on upstream PR 831 with explicit user approval:
  https://github.com/yacy/yacy_search_server/pull/831#issuecomment-5762715488
- 83 instances verified, 82 updated, 27 active parser failures resolved; about
  8.14 GB of original history preserved across updated instances. The earlier
  canary was already patched. Results are bounded post-deployment checks.
- Public payload: `docs/access-tracker-public-rollout-evidence.md`; only that
  reviewed file was submitted. No node identities, addresses, private paths,
  credentials, runtime hashes, raw queries/logs or private attachments included.
- Counts cross-checked against the provisioner report; privacy checks passed;
  GitHub read-back matches the approved local file exactly. No fleet or sibling
  workspace modifications. PR code still contains only source fix and tests.
- Details: `docs/access-tracker-log-notes.md` and completed work order
  `docs/work-orders/completed/20260921T150427Z-access-tracker-public-evidence.md`.

## 2026-09-20: On-Demand AccessTracker Fleet Repair Handoff

Documentation/artifact boundary only; Java fix remains `466e57146` / PR 831.
No runtime changes or fleet deployment during handoff preparation.

- `FLEET_ACCESS_TRACKER_FIX_HANDOFF.md` provides per-affected-node diagnosis,
  strict baseline checks, backup, atomic installation, authenticated application
  verification, query-history preservation, follow-up and rollback commands.
- Portable ignored archive: `backups/access-tracker-handoff-20260920/`.
  Contains tested JAR, source, synthetic tests, isolated patch and checksums;
  no settings, credentials, private reports or query history.
- Reverified exactly four changed AccessTracker class entries with 1,650 other
  entries unchanged. All 19 focused tests pass against the packaged JAR.
- Requires the known old JAR hash; unknown/newer baselines are a hard stop.
  Server2 and all dev installs are excluded. Preserve all existing fleet fixes.
- Explains that post-restart in-memory request counters are not saved-log totals.
- Provisioner owns future deployment and its own Git; this is not a bulk rollout.
- Work order: `docs/work-orders/completed/20260920T180541Z-access-tracker-fleet-handoff.md`.

## 2026-09-20: Robust AccessTracker Query History And Upstream Submission

Dev commit: `466e57146`. Upstream commit: `3bb969ea4`.
PR: https://github.com/yacy/yacy_search_server/pull/831

- `AccessTracker.java` now keeps logged queries on one line, validates record
  headers during seeking/reading, skips malformed legacy lines, and corrects
  inclusive/exclusive boundaries, duplicate timestamps and EOF handling.
- Bounded buffered UTF-8 reads replace allocation of the entire selected range.
- Added `AccessTrackerTest.java`: 11 regressions fail on baseline and pass after
  the fix. 19 focused tests pass including QueryParams/GenericFormatter.
- Dev `ant compileTest` and upstream application compile pass. The upstream
  full test target has an unrelated missing Solr bridge classpath; focused tests
  pass using the complete classpath. See PR for commands and synthetic examples.
- Deployed only Server2 dev 8091, preserving 1,650 unrelated JAR entries and the
  original query history. Live history/timeline/local search return HTTP 200, and
  a new synthetic multiline query is persisted as a single physical line.
- Log path remains `DATA/LOG/queries.log`; no new settings or fleet rollout.
- Details, hashes, backups, performance observations, limitations and rollback:
  `docs/access-tracker-log-notes.md`.
- Work order: `docs/work-orders/completed/20260920T030516Z-access-tracker-fix.md`.
- PR ready for review and mergeable; GitHub `build` and `build-and-release`
  checks both passed. Documentation checkpoint: `03eb06d4b`.

## 2026-09-20: AccessTracker Query Log Failure Investigation

Patch boundary: investigation/documentation only, uncommitted. No YaCy source
or runtime change, deployment, production log modification or upstream PR.

- Added `docs/access-tracker-log-notes.md` with verified source paths, synthetic
  reproduction, additional date-range defects, and a proposed isolated patch.
- Current source reproduces the reported multiline-log NumberFormatException.
  Other malformed-line positions return empty history during date seeking.
- Clean-log fixtures also expose omitted final records and incorrect interval
  boundaries. The proposed fix must cover seeking as well as line parsing.
- Compiled the unmodified reader with Java 11 target and ran an isolated local
  synthetic harness; full build and HTTP/canary validation remain future work.
- Private report and raw query data are excluded from these documents/tests.
- Work order: `docs/work-orders/completed/20260920T025230Z-access-tracker-review.md`.

## 2026-09-20: Persistent Work Orders And Durable Workflow Instructions

Patch boundary: documentation only; left uncommitted for this workspace's
agent to review and commit. No application or remote-node changes.

Files:

- `AGENTS.md`
- `docs/work-orders/README.md`, `TEMPLATE.md`, `active/README.md`,
  `completed/README.md`
- `docs/dev-node-change-log.md`

Behavior:

- Work orders, current task state and closed outcomes stay in this workspace.
- Adopted the codebase-only output trial for one or two dev-builder sessions,
  retaining complete cached responses and reverting if usefulness declines.
- Added concrete local-memory/Vestige lookup, verified breadcrumb capture,
  documented user corrections and resume/closeout requirements.
- Preserved dev/standard-node isolation, build/rollback guidance and local Git
  ownership. No fleet-specific identity, helpers or deployment rules imported.

Verification scope: documentation links, whitespace, scope and preservation
of existing operational sections; no live deployment or Java test required.
The workspace-local agent should review the uncommitted documentation diff.

## 2026-09-01: Central Changelog Backfill

Commit: `666480fc2`

Files:

- `docs/dev-node-change-log.md`
- `AGENTS.md`
- `FLEET_THREAD_EVICTOR_FIX_HANDOFF.md`

Behavior:

- Added this central change index so future work starts from documentation
  before falling back to Git archaeology.
- Updated local agent instructions so future meaningful changes include a
  changelog entry before commit.
- Promoted the remote Solr thread-evictor fleet handoff into tracked project
  documentation.

Verification:

- Backfilled from reachable Git history, existing topic notes, and existing
  fleet handoff files.

## 2026-09-01: Remote Crawl Fleet Handoff

Commit: `47f014305`

Files:

- `FLEET_REMOTE_CRAWL_STALL_FIX_HANDOFF.md`

Behavior:

- Created a Server23 provisioner handoff for the remote-crawl stall fix.
- Identified the isolated patch boundary as commit `6da22f99e`.
- Documented that the current `yacy-space-abuse-message` branch and local jar
  hash `080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9`
  already include the remote-crawl fix, so the provisioner should verify before
  redeploying and must not roll back the active yacy.space mitigation.

Verification:

- Handoff copied to `/home/programmer/Documents/Server23_and_soforth`.
- Local and copied file SHA256 matched.

## 2026-08-17: Cross-Workspace Git Ownership

Commit: `6119b9c12`

Files:

- `AGENTS.md`

Behavior:

- Documented that adjacent project workspaces may exchange handoffs and status
  notes, but Git ownership stays local to the workspace.
- Prevents a provisioner or bridge agent from staging, committing, pushing, or
  cleaning this repo without explicit instruction, and vice versa.

Verification:

- Documentation-only change.

## 2026-08-16: yacy.space Response Patch Handoff

Commits: `386cadb4b`, `94dd20366`

Files:

- `source/net/yacy/htroot/yacysearchitem.java`
- `FLEET_YACY_SPACE_RESPONSE_PATCH_HANDOFF.md`

Behavior:

- For unauthenticated search result rendering from the configured yacy.space
  user agent, result titles and snippets are replaced with a configurable
  network-impact message plus a per-result suffix.
- Existing result URL blackhole behavior remains active, with the TLD pool
  narrowed to `space`.
- Matching requests keep heuristic search-result crawling suppressed.
- Normal users, authenticated users, stored index records, direct Solr APIs,
  exports, and blacklists are not intentionally changed.

Config:

```ini
search.result.blackhole.enabled=true
search.result.blackhole.userAgent=yacy.space-remote-fetcher/1.0
search.result.blackhole.tldPool=space
search.result.blackhole.message.enabled=true
search.result.blackhole.title=You are destroying the Yacy Network Please Stop
search.result.blackhole.message=<configured warning body>
```

Verification:

- `ant compileTest` passed for the tested handoff build.
- Server2 dev node returned HTTP 200 after deployment.
- Normal User-Agent checks remained normal.
- Matching yacy.space User-Agent checks returned warning title/body and `.space`
  rewritten result URLs.
- Fleet rollout later reported no node error behavior.

## 2026-08-13: Remote Solr HTTP Client Thread Leak Fix

Commits: `481764a3e`, `99b14e964`, `e1bdf3101`

Files:

- `source/net/yacy/cora/federate/solr/instance/RemoteInstance.java`
- `FLEET_THREAD_EVICTOR_FIX_HANDOFF.md`

Behavior:

- Closed owned remote Solr HTTP clients so repeated remote Solr use does not
  accumulate thousands of `Connection evic` threads.
- The failure mode this addressed was eventually visible as
  `java.lang.OutOfMemoryError: unable to create native thread`.

Verification:

- Upstream PR: `https://github.com/yacy/yacy_search_server/pull/809`
- Tested on Server50 and the Server2 dev node.
- Known-good handoff artifact hash:
  `05486b584596b2cb2b668d1c627b9dc52a229fc257d214426fb391e9176ec4bd`.
- Fleet day-after sweep reported 78/78 checked instances OK, patched jar hash
  OK, exactly 3 connection-evictor threads per checked JVM, and no recurrence
  of runaway thread behavior.

## 2026-08-12: Dev Node Agent Instructions

Commit: `822cdb127`

Files:

- `AGENTS.md`

Behavior:

- Added local working rules for the YaCy dev-node builder workspace.
- Captured dev/testbed isolation, backup, build, Git, memory, and code graph
  expectations.

Verification:

- Documentation-only change.

## 2026-08-12: Configurable Result Blackhole Links

Commit: `da10c19b8`

Files:

- `source/net/yacy/htroot/yacysearchitem.java`

Behavior:

- Added a configurable search result rendering path that rewrites visible result
  URLs for matching unauthenticated User-Agent traffic.
- Added helpers to choose a configured TLD, replace the final TLD in rendered
  URLs, and hide cache/proxy/index-browser/snapshot links for matched requests.
- Added a guard to avoid heuristic search-result crawling for matched requests.

Config:

```ini
search.result.blackhole.enabled=<true|false>
search.result.blackhole.userAgent=<substring>
search.result.blackhole.tldPool=<comma-separated TLDs>
```

Verification:

- Later reused by the yacy.space response-message branch and fleet handoff.

## 2026-07-22: Remote Crawl Provider Handling

Commit: `6da22f99e`

Files:

- `docs/remote-crawl-notes.md`
- `htroot/RemoteCrawl_p.html`
- `source/net/yacy/crawler/data/CrawlQueues.java`
- `source/net/yacy/htroot/RemoteCrawl_p.java`
- `source/net/yacy/peers/Protocol.java`

Behavior:

- Treated remote-crawl advertisements as hints rather than proof that a peer
  still has work available.
- Removed destructive failure handling that set provider `RCOUNT` to `0` or
  marked interface departure after a bad remote-crawl endpoint response.
- Added in-memory cooldown for providers that fail or return empty feeds.
- Replaced recursive provider retry with bounded attempts per loader run.
- Allowed remote intake while the local crawl queue is small instead of
  deferring whenever any local crawl work exists.
- Added `RemoteCrawl_p.html` loader status and counters.

Config:

```ini
remoteCrawlLoader.localQueueLimit=20
remoteCrawlLoader.maxProviderAttempts=5
remoteCrawlLoader.providerCooldownMillis=300000
```

Verification:

- Server2 dev node testing showed remote crawl activity became more persistent.
- Handoff for fleet validation is in
  `FLEET_REMOTE_CRAWL_STALL_FIX_HANDOFF.md`.

## 2026-07-09: Crawl Start Expert Submit Button

Commit: `5e4c8ca1d`

Files:

- `htroot/CrawlStartExpert.html`

Behavior:

- Added a duplicate `Start New Crawl Job` button beside the URL input area on
  `CrawlStartExpert.html`.
- Preserves the bottom submit button while making repeated single-URL crawls
  faster when the previous crawl options are already set.

Verification:

- User tested on the dev instance and confirmed the placement and behavior.

## 2026-07-06: Poison Pill Form Anchor

Commit: `3c108af51`

Files:

- `htroot/CrawlerContentRejection_p.html`

Behavior:

- After adding a poison pill entry on `CrawlerContentRejection_p.html`, the
  confirmation returns near the Add Poison Pill form instead of the top of the
  page.
- Adjusted the target so the form itself is in the viewport.

Verification:

- User tested and confirmed the landing behavior.

## 2026-07-05: Search Event Stability During Crawler Cleanup

Commit: `931489066`

Files:

- `docs/parser-metadata-notes.md`
- `source/net/yacy/data/ListManager.java`
- `source/net/yacy/htroot/Crawler_p.java`
- `source/net/yacy/peers/Protocol.java`
- `source/net/yacy/repository/BlacklistHelper.java`
- `source/net/yacy/search/Switchboard.java`
- `source/net/yacy/search/query/SearchEvent.java`
- `source/net/yacy/search/query/SearchEventCache.java`

Behavior:

- Prevented crawler cleanup and crawler-managed blacklist writes from
  force-clearing active search events.
- Deferred crawler-triggered Solr deletions while live search events are still
  feeding or have recently been touched.
- Updated search result materialization so Solr rows rejected by blacklists or
  domain checks are counted as evictions, and later local Solr pages are fetched
  until a visible result slot is available or Solr is exhausted.

Verification:

- Addressed browser-side result rendering stalls seen when crawler cleanup
  removed records during active search rendering.
- Subsequent Cyrillic single-character tests no longer stalled on blacklisted or
  rejected local Solr rows.

## 2026-07-03: Crawler Rule Action Audit Log

Commits: `caa0380a5`, `fa4eec75d`, `1616bebda`

Files:

- `docs/parser-metadata-notes.md`
- `htroot/Crawler_p.html`
- `htroot/api/status_p.xml`
- `htroot/js/Crawler.js`
- `source/net/yacy/crawler/data/CrawlerContentRejection.java`
- `source/net/yacy/htroot/CrawlerContentRejection_p.java`
- `source/net/yacy/htroot/Crawler_p.java`
- `source/net/yacy/htroot/api/status_p.java`
- `source/net/yacy/search/Switchboard.java`
- `test/java/net/yacy/crawler/data/CrawlerContentRejectionTest.java`

Behavior:

- Added crawler rule action reporting to the crawler monitor and status API.
- Added on-disk audit logging for crawler actions that write crawler-managed
  blacklist entries.
- Trimmed the audit log to avoid routine noise such as metadata enrichment,
  zero-content skips without blacklist writes, and richer-existing-record skips.
- Limited the on-disk audit log to the newest 10000 blacklist-action entries.
- Web display uses compact summaries instead of raw source excerpts.

Log file:

```text
DATA/LOG/crawler-rule-actions.log
```

Verification:

- User confirmed poison pill and automatic blacklist events appeared correctly
  after the audit refresh issue was fixed.

## 2026-07-03: YouTube Description Parser Overflow Fix

Commits: `331590df1`, `5202aea53`

Files:

- `docs/parser-metadata-notes.md`
- `source/net/yacy/document/parser/htmlParser.java`
- `test/java/net/yacy/document/parser/htmlParserTest.java`

Behavior:

- Replaced regex extraction of YouTube `attributedDescription.content` with a
  bounded scanner to prevent stack overflow on large YouTube source responses.
- Documented the `Crawler_p.html` servlet error and the test/verification path.

Verification:

- `ant compileTest` and `htmlParserTest` covered escaped description text and a
  large source without a matching content field.
- Dev crawl POST for the failing YouTube URL returned HTTP 200 and no fresh
  `StackOverflowError`.

## 2026-07-01 to 2026-07-02: Poison Pill And Abandoned Domain Rules

Commits: `d8c8f39d4`, `09e85887c`

Files:

- `docs/parser-metadata-notes.md`
- `htroot/CrawlerContentRejection_p.html`
- `source/net/yacy/crawler/data/CrawlerContentRejection.java`
- `source/net/yacy/htroot/CrawlerContentRejection_p.java`
- `source/net/yacy/search/Switchboard.java`
- `source/net/yacy/crawler/data/CrawlQueues.java`
- `test/java/net/yacy/crawler/data/CrawlerContentRejectionTest.java`

Behavior:

- Split crawler content rejection into soft rules and Poison Pill rules.
- Soft rules reject a single fetched page and remove any existing indexed record
  for that URL.
- Poison Pill rules treat a matched host as compromised or broadly unwanted:
  blacklist the host in `url.poison_pill.black` and remove indexed records for
  that host.
- Added abandoned-host cleanup for DNS unknown-host failures, using exact-host
  blacklist rules in `url.domain_abandoned.black`.
- Added guardrails so a failed subdomain does not automatically blacklist a
  working parent domain, and malformed host data is discarded instead of
  blacklisted.

Verification:

- User tested Poison Pill host cleanup in the wild and observed noncanonical
  records being removed.
- User tested abandoned-domain guardrails after false positives such as
  `onlinegrad.baylor.edu` and malformed `www.black`.

## 2026-06-29: Parked Or For-Sale Domain Cleanup

Commit: `48c4ce7aa`

Files:

- `.gitignore`
- `docs/parser-metadata-notes.md`
- `htroot/Crawler_p.html`
- `htroot/DeadDomains_p.html`
- `htroot/api/status_p.xml`
- `htroot/env/templates/submenuBlacklist.template`
- `htroot/js/Crawler.js`
- `source/net/yacy/document/parser/htmlParser.java`
- `source/net/yacy/htroot/Crawler_p.java`
- `source/net/yacy/htroot/DeadDomains_p.java`
- `source/net/yacy/htroot/ViewFile.java`
- `source/net/yacy/htroot/api/status_p.java`
- `source/net/yacy/search/Switchboard.java`

Behavior:

- Added parked/dead-domain detection and cleanup tooling.
- Manual cleanup purges indexed records for the root domain and discovered
  subdomains, then writes root and subdomain YaCy blacklist entries to
  `url.domain_for_sale.black`.
- Added a Filter & Blacklists > Dead Domains page with an automatic cleanup
  option so detected parked domains can be purged and blacklisted during crawl.
- Cleanup status now distinguishes targeted hosts from newly added,
  already-present, and failed blacklist rules.

Verification:

- User tested automatic and manual cleanup on domain-for-sale examples and
  confirmed accurate detections with no early false positives.

## 2026-06-28: Crawler Content Rejection Soft Rules

Commits: `675658ca1`, `13c8b6c73`, `353eb1414`

Files:

- `docs/parser-metadata-notes.md`
- `htroot/CrawlerContentRejection_p.html`
- `htroot/env/templates/submenuBlacklist.template`
- `source/net/yacy/crawler/data/CrawlerContentRejection.java`
- `source/net/yacy/htroot/CrawlerContentRejection_p.java`
- `source/net/yacy/search/Switchboard.java`
- `source/net/yacy/search/schema/MetadataQuality.java`
- `test/java/net/yacy/crawler/data/CrawlerContentRejectionTest.java`
- `test/java/net/yacy/search/schema/MetadataQualityTest.java`

Behavior:

- Added a Filter & Blacklists admin tab for crawler content rejection rules.
- Soft rules are plain-text, case-insensitive substring matches.
- Rules are checked against raw fetched source before parsing, and against
  parsed title, description, and body text when available.
- A matching document is rejected before indexing and any existing indexed
  record for that URL is removed.
- Fixed rule list rendering after the initial UI showed add/delete state but no
  visible list entries.

Verification:

- User confirmed the list rendered correctly.
- YouTube removed-video examples confirmed raw-source matching was required and
  then worked as intended.

## 2026-06-28: YouTube Metadata Normalization

Commits: `e491a4092`, `77e90d398`, `1f718132b`, `86a6a4b32`,
`049e5a61f`, `e2e3d4396`, `28f1ab060`

Files:

- `source/net/yacy/document/parser/htmlParser.java`
- `source/net/yacy/search/Switchboard.java`
- `source/net/yacy/search/schema/MetadataQuality.java`
- `test/java/net/yacy/search/schema/MetadataQualityTest.java`

Behavior:

- Detected generic YouTube metadata stubs such as `- YouTube`.
- Enriched generic YouTube watch metadata from YouTube oEmbed and structured
  source fields when available.
- Restored useful YouTube titles with the ` - YouTube` suffix.
- Captured author metadata when available.
- Dropped generic YouTube keyword filler:
  `video,sharing,camera,phone,free,upload`.
- Canonicalized YouTube watch URLs by video ID and removed noncanonical index
  records for the same video ID.

Verification:

- User observed successful title/description enrichment and noncanonical
  YouTube record removal in live dev testing.

## 2026-06-28: Parser Metadata, Recrawl, And Error Cleanup

Commits: `1f98f7fe0`, `7e83c5278`, `870896950`, `ef900be9c`,
`4b41c16e5`, `dba5d7c3e`, `fe5bd5990`, `8502690be`, `74ee31521`,
`d6012b65d`, `31369a880`, `83f19d2cb`, `790e7a35e`, `b2b13923b`,
`6afad9b59`

Files:

- `docs/parser-metadata-notes.md`
- `htroot/IndexCreateParserErrors_p.html`
- `source/net/yacy/crawler/CrawlStacker.java`
- `source/net/yacy/document/parser/html/ContentScraper.java`
- `source/net/yacy/document/parser/html/TransformerWriter.java`
- `source/net/yacy/htroot/IndexCreateParserErrors_p.java`
- `source/net/yacy/search/Switchboard.java`
- `source/net/yacy/search/index/ErrorCache.java`
- `source/net/yacy/search/schema/MetadataQuality.java`
- `test/java/net/yacy/document/parser/htmlParserTest.java`
- `test/java/net/yacy/search/index/ErrorCacheTest.java`
- `test/java/net/yacy/search/schema/MetadataQualityTest.java`

Behavior:

- Ignored empty HTML titles and added description/title fallbacks for malformed
  or low-value metadata.
- Recovered metadata after malformed singleton tags.
- Salvaged oversized titles and capped generated HTML titles at 180 characters.
- Derived low-confidence titles for empty app shells when no better metadata is
  available.
- Marked SAML/authentication handoff pages as `noindex,nofollow`.
- Preferred richer metadata on recrawl rather than overwriting useful existing
  records with weaker new data.
- Removed stale index records after eligible bad recrawls with HTTP status >=400.
  This includes temporary failures, not just definitively gone content; see the
  improvement catalog's safety-review recommendation before upstreaming it.
- Added a purge action for failure markers.
- Rejected zero-content stubs and parsed soft-error pages before indexing.

Verification:

- Topic notes live in `docs/parser-metadata-notes.md`.
- Regression tests were added around metadata quality, parser behavior, and
  error-cache cleanup.
- User confirmed improved parsing on examples including
  `https://africanfamily.org/`, YouTube watch pages, SAML handoffs, and
  JavaScript app-shell examples.

## Future Documentation Rule

For every future meaningful YaCy dev-node change:

- Update this file before the final commit.
- Keep or add topical docs when the feature has operational nuance.
- Update the grouped improvement catalog when behavior or contribution status
  changes; keep future ideas separate from implemented capabilities.
- Include config keys, blacklist names, data files, or runtime paths when they
  matter.
- Include the exact commit hash once known when the entry will be used as a
  rollout, rollback, or upstream patch boundary.
