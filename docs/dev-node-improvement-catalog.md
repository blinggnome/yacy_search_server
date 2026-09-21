# YaCy Dev Node Improvement Catalog

This is the plain-language guide to what we have built, why it matters, and
what we might contribute to YaCy. It groups related fixes into capabilities
instead of making each follow-up correction look like another feature.

**Reviewed:** September 21, 2026. **Source checkpoint:** `eaad79496`, on
`yacy-space-abuse-message`. The retained history from base `94e8ac3b5` contains
51 commits: 37 that change application code, templates or tests, and 14 for
documentation/workflow. Those numbers are commit counts, not feature counts.

The catalog was checked against the current source, tests, Git history and
[central change log](dev-node-change-log.md). "Retained" means present in this
fork, not that every feature is enabled on every node. Historical test results
are identified as such; this cataloging pass did not deploy code, probe nodes,
or rerun application tests. Upstream PR status was checked on the review date.

### Reading Guide

- [Contribution shortlist](#suggested-contribution-order)
- [Reliability](#server-and-search-reliability), [HTML metadata](#html-titles-and-descriptions), and [YouTube](#youtube-metadata-and-url-cleanup)
- [Index quality](#recrawl-and-index-quality) and [content rules](#content-rules-and-operator-controls)
- [Dead domains and remote crawling](#dead-domains-and-remote-crawling)
- [UI and local policy](#other-user-interface-and-local-policy)
- [Future ideas](#future-improvements-to-consider) and [commit/source map](#evidence-and-contribution-boundaries)

## At A Glance

| Area | What is better than our starting point | Contribution outlook |
| --- | --- | --- |
| Server reliability | Remote Solr connections no longer leak eviction threads | Already merged upstream |
| Search history | Malformed old log records no longer break the history reader; date ranges are correct | PR open, with regression tests and fleet evidence |
| Search continuity | Crawler cleanup no longer force-clears live searches; rejected rows no longer strand later results | Promising, but split the general search fix from custom cleanup machinery |
| Remote crawling | A bad or empty remote queue no longer makes providers disappear; retries and intake are bounded | Strong next candidate after deterministic scheduler tests |
| HTML metadata | Empty, malformed and overlong titles are handled better; useful descriptions can be recovered | Good candidates for small, independently tested PRs |
| YouTube records | Better titles/descriptions/authors, cleaner keywords and fewer duplicate URLs | Useful, but site-specific and dependent on external responses |
| Index quality | Better recrawls can replace weak records; empty stubs and error pages are rejected | Separate quality improvements from destructive cleanup policy |
| Content controls | Page-level soft rules, host-level Poison Pills, protective whitelist | Valuable operator tools; strengthen safety and recovery before upstreaming |
| Dead hosts/domains | Detect parked domains and certain DNS failures; clean up and blacklist | Needs stronger evidence and reversibility safeguards |
| Administration | Better crawler action reporting, blacklist undo and convenient crawl buttons | Crawl-start button PR 832 open; larger tools depend on custom features |
| Abusive-client response | Special responses and heuristic-crawl suppression for a configured client | Keep the current targeted policy local; consider general rate controls separately |

### Suggested Contribution Order

**User decision, September 21, 2026:** submit only the crawl-start button (U1)
as the next PR. Other unsubmitted features need further testing and a separate
decision before upstream publication. The list below remains a planning guide,
not authorization to publish those features. Existing PRs 809 and 831 are
unaffected by this decision.

1. Follow through on [PR 831](https://github.com/yacy/yacy_search_server/pull/831)
   rather than opening a duplicate. The thread fix is already merged.
2. Duplicate **Start New Crawl Job** button submitted as
   [PR 832](https://github.com/yacy/yacy_search_server/pull/832), ready for review.
3. Separate the general HTML parser fixes into small regression-backed patches:
   empty titles, missing descriptions, malformed tags and oversized titles.
4. Prepare the remote-crawl persistence fix with simulated failing/empty peers.
5. Isolate the search pagination/eviction correction and prove it independently
   of our custom rejection tools.
6. Revisit deletion, DNS confidence and undo before proposing host-wide cleanup.

These are recommendations, not claims that a patch is ready to merge unchanged.
Each proposed PR needs a fresh comparison against current upstream, focused
tests, and a check that it contains no unrelated fork changes or private data.

## Server And Search Reliability

### R1. Stop The Remote Solr Thread Leak

Repeated remote searches could leave HTTP clients and their connection-eviction
threads alive. Eventually a node could exhaust its ability to create threads,
making several database-backed pages fail. We close clients owned by the remote
Solr instance when that instance closes.

**Evidence:** the recorded day-after fleet check found 78/78 instances healthy,
with three eviction threads each; subsequent user reports found no recurrence.
**Upstream:** [PR 809](https://github.com/yacy/yacy_search_server/pull/809) merged
August 18, 2026. Merge is confirmed; this catalog does not claim a particular
released version contains it. [Deployment handoff](../FLEET_THREAD_EVICTOR_FIX_HANDOFF.md).

### R2. Make Search History Tolerate Damaged Records

A query containing line breaks could create a malformed log record, breaking
the local search history page. We now keep new records on one physical line,
skip malformed legacy records safely, and validate records during date seeking
as well as ordinary reading. We also fixed date-range boundaries, duplicate
timestamps and end-of-file handling. Buffered reads avoid loading a whole
selected history range into memory.

**Evidence:** 11 new regressions fail on the old implementation and pass with
the fix; 19 focused tests passed. The recorded fleet rollout verified 83
instances, resolved 27 active parser failures, and preserved existing history.
**Upstream:** PR 831 is open with both build checks passing as of the review
date. Only the repair and synthetic tests are in the PR, not real user searches.
[Notes](access-tracker-log-notes.md) and
[sanitized production evidence](access-tracker-public-rollout-evidence.md).
The history page's in-memory request counter is not the number of saved lines
in the persistent log, especially after a restart.

### R3. Keep Crawler Cleanup From Interrupting Searches

Crawler-managed blacklist writes and index cleanup used to force-clear cached
search events while browsers were still requesting results. We introduced
non-forcing blacklist/cache operations and defer crawler-triggered deletions
while searches are feeding or have recently been used. An explicit administrator
request to clear search events remains a different operation.

**Evidence:** live dev testing stopped reproducing the immediate interruption
when crawler rules removed records. **Revisit:** the deferred queue is in memory
and waits for a quiet search period. Completion reporting, restart behavior,
continuous-traffic starvation and cancellation need dedicated tests and stronger
handling. This is not proof that every possible search stall is fixed.

### R4. Continue Pagination Past Rejected Search Rows

Some index rows are discarded while results are assembled, for example because
a blacklist rejects them. We account for those discarded rows and fetch later
local Solr pages instead of leaving a visible result slot waiting indefinitely.
This addressed the normal-sort and date-sort stalls exposed by the Cyrillic
single-character searches.

**Upstream:** a promising general correctness fix. It shares commit `931489066`
with R3, so extract and test it separately where possible. It is not a revival
of our earlier search-result text-filter/count-rewriting experiment, and does
not claim that all distributed facet counts are exact.

## HTML Titles And Descriptions

These improve the metadata YaCy extracts during crawling. A stored description
is not necessarily the same as a query-dependent search-result snippet. The
crawler still does not run a browser JavaScript engine.

### M1. Recover From Empty Titles And Missing Descriptions

An empty HTML title no longer prevents useful fallback metadata from being
selected. When a page lacks a usable description, existing metadata alternatives
and a body-text fallback can provide something useful instead of a blank field.
We improved the fallback path; social metadata and headings are not inventions
of this fork. The original test case was the African Family site.

**Evidence:** parser regression tests cover empty-title/headline fallback,
body-description fallback and social metadata. **Upstream:** good small-patch
candidates. Check navigation-heavy pages so a fallback does not merely describe
the menu, cookie banner or site footer.

### M2. Recover Metadata After Malformed HTML Tags

Malformed singleton tags, such as broken metadata markup, could swallow later
useful information. The parser now recovers more gracefully and continues
collecting title/description information. This addressed the InformationLiberation
example rather than requiring a special parser for that site.

**Evidence:** a malformed-tag fixture is in `htmlParserTest`. **Upstream:** a
general parser fix worth isolating with both broken and valid HTML examples.

### M3. Salvage Oversized Titles And Limit Them To 180 Characters

Long or malformed title blocks can contain much more than a sensible title.
We salvage useful title text and limit the affected title-cleaning/fallback
paths to 180 characters, preferring a sensible sentence or word boundary.
This includes the generated-title problem observed during live crawling.

**Evidence:** oversized-title and title-limit tests exist; the Tantek examples
motivated the recovery work. **Revisit:** add multilingual and Unicode-boundary
tests, and decide whether the limit should be configurable. This is not a
database-wide operation that shortens all existing records automatically.

### M4. Give App Shells A Low-Confidence Title

When no useful title is available, a readable URL path can supply a fallback
title. This helped make the Bethesda document example less opaque, but the URL
cannot prove what a JavaScript application would display.

**Important limit:** a URL-derived title alone does not rescue a zero-content
stub from the rejection check in Q2. **Revisit before upstream:** retain the
distinction between observed page metadata and a guessed label; the rendered
page might still be an error. A browser renderer is not part of this feature.

### M5. Do Not Index Authentication Handoff Pages As Content

Recognized SAML request handoff pages are marked `noindex,nofollow`, so a login
or redirect mechanism is not mistaken for the article/product behind it.
The E*TRADE request-assertion example motivated this work.

**Revisit:** move recognition earlier if that avoids unnecessary work, and test
other handoff forms without rejecting useful pages merely discussing SAML.
This does not retrieve protected content or bypass authentication.

## YouTube Metadata And URL Cleanup

### Y1. Replace Generic Video Metadata With Useful Fields

When a supported video URL arrives with generic metadata such as `- YouTube`,
the parser can obtain a real title and author from YouTube's oEmbed endpoint.
Useful titles retain the ` - YouTube` suffix. Supported legacy URL forms also
reach this enrichment path.

**Limit:** oEmbed supplies title/author here, not the video description. The
extra request can fail and is time-bounded; it is not JavaScript rendering.
The same URL can yield different HTML to a browser and a server crawler.

### Y2. Recover Descriptions From Structured Page Source

The parser extracts an attributed description embedded in YouTube's returned
source when it is available. A bounded scanner replaced an overflow-prone
regular expression, fixing a `Crawler_p.html` failure on large responses.

**Evidence:** tests cover escaped description text and a large source without
a matching field; the formerly failing crawl returned HTTP 200 after repair.
**Upstream:** the scanner repair depends on our description extractor; it is
not a standalone defect in upstream code that lacks that extractor.

### Y3. Canonicalize Video URLs And Remove Old Variants

Supported video URL forms are normalized to a conventional watch URL using
the video ID. During applicable crawl/index cleanup, locally indexed variants
for the same video ID can be removed rather than retained as separate pages.
Live testing observed multiple noncanonical records removed for one video.

**Limit:** this happens as videos are encountered. It is not a completed sweep
of all historical records, nor does it rewrite records on other peers.
**Revisit:** expand positive/negative URL fixtures and prove that cleanup never
crosses video IDs or removes unrelated YouTube pages.

### Y4. Discard Generic Keyword Filler And Empty Video Stubs

The generic `video,sharing,camera,phone,free,upload` keyword set is dropped
instead of being stored as though it describes every video's subject. We do
not invent replacement keywords. A generic video label with no useful content
does not make a worthwhile index entry; unavailable-video text can also be
caught by the operator's raw-source rules.

**Contribution outlook for Y1-Y4:** useful, but review as a coherent optional
site-specific enhancement. Use offline fixtures for real, removed, restricted,
consent and incomplete responses; review request caching, time budgets and
integration with YaCy's ordinary crawler network policy. Keep unavailable-video
rules page-scoped unless there is independent evidence to block an entire host.

## Recrawl And Index Quality

### Q1. Improve Poor Existing Records Without Replacing Better Ones

Poor stored metadata can make an already-known URL eligible for another crawl.
After fetching/parsing, metadata quality is compared so a weaker replacement
does not overwrite a richer existing record.

**Important limit:** this compares record-level scores using fields such as
title, description and author. It is not a field-by-field merge. A strictly
higher old score can keep the old record; equal scores do not receive that
protection. **Revisit:** combine field-level quality with freshness, so preserving
a good title does not also preserve an obsolete body or miss a useful new field.
Rejection checks still take precedence; a rich old record does not exempt a
page from Poison Pill or soft-rule checking.

### Q2. Reject Truly Empty Index Stubs

A page with no useful title, no useful description and no body text is rejected
instead of becoming an empty search hit. A useful title **or** description
**or** body can keep a page eligible; all three are not required. Generic and
URL-derived titles do not count as sufficient content by themselves.

**Evidence:** metadata-quality tests cover empty shells, generic video stubs,
description-only pages and real body text. The FIFA examples exposed the need.
**Limit:** a static crawler cannot establish what a script-only page would
render. Rejection here is an index-quality decision, not proof that the URL is
permanently dead.

### Q3. Reject Soft Error Pages That Return Successful HTTP Statuses

Some servers return HTTP 200 for an error page. We look for an error-indicating
title plus confirming missing-page text in the description or body. Merely
mentioning "404" in a useful article should not be enough.

**Evidence:** tests include error pages and a legitimate article about 404s.
**Revisit:** a larger multilingual fixture set and precision measurement before
adding broader wording. A rejection can also remove an old indexed version,
so false positives matter more than for a display-only filter.

### Q4. Remove Stale Content After Bad Recrawls

Applicable recrawl failures and rejection paths can remove a previously indexed
document instead of leaving yesterday's useful-looking hit forever. Successful
fetches newly marked `noindex` are also handled. Crawl-profile exclusions remain
scope choices, not proof that a previously indexed page is bad.

**Safety review needed:** the HTTP error predicate currently covers status
codes **400 and higher**, including 403, 429 and 5xx. Those are not all permanent
failures. Before upstreaming, distinguish likely permanent removal from temporary
overload, authentication restrictions and rate limiting; consider confirmation
attempts or a grace period. Do not describe all these statuses as definitive
evidence of dead content.

### Q5. Purge Persisted Failure Markers

Removing searchable content and retaining a failure record are separate things.
We exposed the existing failure-marker purge operation through a button on
`IndexCreateParserErrors_p.html`, allowing administrators to clear that
bookkeeping without confusing it with a live search document.

**Upstream:** potentially a small UI improvement once its scope and confirmation
text are checked against current upstream behavior. It does not restore pages
deleted by cleanup or automatically recrawl them.

## Content Rules And Operator Controls

### C1. Soft Rules: Reject One Page

The **Crawler Content Rejection** tab under **Filter & Blacklists** lets an
administrator add, edit in bulk and remove plain-text rules. Matching is a
case-insensitive substring search, not a regular expression. Checks run against
raw fetched page source before parsing and against parsed metadata/body where
applicable. A match rejects that page and removes its existing indexed record.

The original invisible-list problem was corrected. Raw-source matching was an
important change: useful rejection text can exist in scripts or embedded data
that YaCy does not expose as ordinary page text.

### C2. Poison Pills: Block And Clean Up A Host

A second list is intentionally stronger. A Poison Pill match writes an
exact-host entry to `url.poison_pill.black` and schedules/removes indexed records
for that host. Poison rules are checked before soft rules. This is not the
same scope as the root-domain-plus-subdomains parked-domain action below.

**Safety review needed:** source can mention an unwanted phrase in a legitimate
comment, link, script or advertisement. A substring match is evidence of text,
not proof the entire host is unwanted. The earlier accidental YouTube block
demonstrates why host-wide action needs careful rules and recovery controls.

### C3. Protect Important Hosts With A Whitelist

The content-rule whitelist can protect a named host and its subdomains from
Poison Pill host-wide action. The rule data is stored separately from the soft
and poison lists.

**Important limit:** this is not a universal "always index this site" switch.
It does not bypass soft rules, zero-content/error checks or every other cleanup
policy. **Revisit:** make the UI explicit about that scope, and decide whether
other destructive actions need their own protections.

### C4. Show Recent Crawler Actions And Keep A Focused Audit Log

The crawler monitor shows recent rule actions, including enrichment/skip
explanations useful during testing. A separate persistent audit log retains
blacklist-related actions and their rule/match evidence, rather than filling
with routine metadata upgrades, soft rejections or empty-page skips.

The disk log is `DATA/LOG/crawler-rule-actions.log`, limited to the newest
10,000 entries. Live recent actions and audit history are different views.
The UI shows compact summaries instead of dumping raw HTML; persisted evidence
can retain more detail. The refresh path was fixed so new audit entries appear.

**Revisit:** protect potentially sensitive source excerpts and measure the cost
of trimming/reloading the log under high crawl activity. A quiet audit log is
not evidence that no pages have been rejected.

### C5. Undo Blacklist Entries And Avoid Misleading Cleanup Buttons

Audit entries offer **Remove block** for supported crawler-managed blacklist
actions. Manual **Purge + Blacklist** controls are suppressed when automatic
dead-domain cleanup already applies. Status text distinguishes cleanup targets
from blacklist rules newly added, already present or failed.

**Important limit:** removing a block does not restore deleted index records.
Some removal counts are counted before the deletion, which may be deferred
while searches are active. "Removed" can therefore describe the intended
cleanup rather than a newly confirmed completion. Add explicit queued/completed
states and cancellation before treating this as a complete recovery system.

### C6. Return To The Poison Pill Entry Form

After adding a rule, the page returns to the **Add poison pill** heading with
the entry form visible, rather than making the user scroll down again or
landing just below the form.

**Evidence:** user-tested placement and repeated additions. **Upstream:** bundle
with the content-rule interface, not as a PR against a form upstream lacks.

## Dead Domains And Remote Crawling

### D1. Recognize Parked/For-Sale Domains, Including Simple Redirects

We detect known parked-domain landing signals in URLs, headers and fetched
content. For an otherwise empty page, a simple client-side redirect target can
be examined without executing JavaScript. The follow-up fetch has bounded
size/time and bounded HTTP redirects; it is not arbitrary browser navigation.

Manual cleanup, or the opt-in automatic setting on **Dead Domains**, removes
indexed root-domain/subdomain content and writes the two native YaCy blacklist
forms to `url.domain_for_sale.black`. This keeps these rules out of the default
blacklist. Domain and subdomain rule counts are not document-removal counts.

**Evidence:** successful live tests against several parking providers, with no
false positives reported in that early testing. **Revisit:** broader negative
fixtures, confidence levels and confirmation before domain-wide deletion.
In particular, a soft-rule match on a followed landing page can participate in
the detection path; it deserves stricter scrutiny before authorizing an action
much broader than ordinary page rejection.

### D2. Handle Certain Unresolvable Hosts

Unknown-host failures can trigger cleanup and an exact-host entry in
`url.domain_abandoned.black`. Malformed names are screened. When a subdomain
fails, its registrable root is checked; if that root resolves, normal failure
handling continues rather than creating an abandoned-host blacklist entry.
This corrected the bad inference that a broken subdomain meant its healthy
parent domain should be blocked.

**Important limit:** this is DNS-based evidence, not a domain-registration
lookup. It cannot prove that a domain is unregistered. **Revisit before
upstream:** distinguish authoritative nonexistence from resolver outages and
temporary failures; require repeated confirmation, expiry/rechecks or review.

### D3. Keep Looking For Remote Crawl Work

A peer advertising crawl jobs can legitimately return an empty queue later.
We stopped treating that, or a failed remote-crawl request, as proof the peer
is offline. The loader no longer zeros its advertised queue or demotes its
active seed simply because that endpoint fails.

Candidates are shuffled when rebuilt. Failed/empty providers enter a short
cooldown (default five minutes), and each loader run has bounded attempts
(default five). A small local queue no longer blocks all remote intake
(default threshold 20). Remote-triggered queue capacity is 200; requests are
limited to available room. The remote-crawl page reports queue pressure,
cooldowns, attempts, successes, intake, rejected URLs and failed/empty feeds.

**Evidence:** dev testing, including overnight observation, found sustained
remote crawling improved; an isolated fleet handoff was prepared. That handoff
is not evidence of a completed fleet rollout. **Upstream:** strong candidate
after deterministic fairness, backoff and resource-pressure tests. Normal
remote-acceptance, peer eligibility and resource guards still apply; accepting
remote work does not guarantee every advertised queue can supply jobs.
[Operational notes](remote-crawl-notes.md) and
[handoff](../FLEET_REMOTE_CRAWL_STALL_FIX_HANDOFF.md).

## Other User Interface And Local Policy

### U1. Start A Crawl Beside The URL Box

`CrawlStartExpert.html` now also has **Start New Crawl Job** beside the URL input,
while retaining the original bottom button. When previous options are already
correct, the user can paste a URL and start without scrolling through the form.

**Evidence:** user confirmed placement and behavior on the dev node. On September
21, browser fixture checks against current upstream plus the patch verified
identical 22-field submissions from both buttons at 1440, 1024 and 390 px,
including multiple URLs, edited depth and the crawl-start flag. The button wraps
without overlap; the form's pre-existing mobile overflow is unchanged.
**Upstream:** [PR 832](https://github.com/yacy/yacy_search_server/pull/832) is open
and ready for review. Its sole commit `fdfc741d3` adds one line to this template;
no other fork work is included. CI was in progress at publication.

### U2. Targeted Abusive-Client Response Policy

For a configured matching unauthenticated User-Agent, a special rendering path
can alter result URLs, hide alternate access links, substitute an operator
warning title/body with a short suffix, and suppress heuristic result crawling.
The current mitigation narrows the alternate TLD pool to `.space`. It is not
intended to alter indexed documents or normal/authenticated search results.

**Upstream recommendation:** keep this operator-specific response policy out of
general improvement PRs. User-Agent matching is easy to evade, and a rewritten
URL is not necessarily harmless or nonexistent. Consider a separate proposal
for transparent rate limits, explicit retry responses and limits on search-led
crawl amplification instead. Normal direct index/export interfaces are not
automatically protected by a result-rendering hook.

## Engineering Improvements Around The Node

These make development and rollout safer; they are not new crawler features.

- Separate dev installation, service and ports, leaving the standard node alone
  unless standard-node work is explicitly authorized.
- Known-good file/runtime backups before changes, focused Git checkpoints, and
  restoring the known-good baseline instead of repeatedly editing over a broken
  experiment. File backups complement version control rather than replacing it.
- A central changelog, topic notes, synthetic regression fixtures and isolated
  fleet handoffs with backup, validation and rollback steps.
- Dedicated workspaces with local Git ownership, persistent work orders and
  source-graph/memory guidance to make long-running work easier to resume.
- Upstream patches separated from local features, with public evidence stripped
  of private search data, credentials and fleet identities.

## Future Improvements To Consider

Nothing in this section is implemented merely by being listed here. "Existing"
means previously requested or recorded; "new" means raised by this catalog's
source review. Priorities favor preventing irreversible mistakes first.

| Priority | Idea and origin | Useful completion test |
| --- | --- | --- |
| High | **Safer bad-recrawl policy (new):** distinguish 404/410 from temporary 403/429/5xx, with confirmation/grace where appropriate | Temporary failure preserves a useful record; confirmed permanent removal cleans it up; eventual recovery can be indexed |
| High | **DNS evidence and expiry (new refinement):** separate failed lookup from abandoned registration; repeated checks and expiring blocks | Resolver outage cannot blacklist many healthy sites; broken subdomain cannot block healthy parent; recovered host is reconsidered |
| High | **Preview/quarantine for host-wide rules (new):** show proposed scope/count and matched evidence; optional confirmation or reversible staging | An incidental phrase on one page cannot silently erase a major host without the configured safeguards |
| High | **Truthful cleanup lifecycle (new):** show queued, completed and failed actions; confirm actual counts and link them to audit records | Count is not reported as completed deletion while searches still defer the job |
| High | **Bounded, recoverable cleanup jobs (new):** coalesce duplicate jobs, define restart/retry behavior, avoid indefinite waiting and cancel pending work on undo | Continuous searches, restart, deletion failure and undo-before-execution all have deterministic outcomes |
| High | **Recovery after false blocks (new):** separate unblock from restoration, optionally enqueue a reviewed recrawl | Removing a block neither promises restored data nor allows an old pending cleanup to delete newly recovered content |
| Medium | **Metadata backfill (existing):** find historical missing/poor titles/descriptions and enrich them in bounded batches | Dry run reports candidates; controlled recrawls improve them without restarting the whole index or overwriting better fields |
| Medium | **Field-level metadata quality plus freshness (new):** preserve good fields without freezing an entire old document | A poorer new title cannot erase a good title, but verified new content and useful new fields still arrive |
| Medium | **App-shell confidence (existing):** revisit URL-slug titles and detection of rendered errors without assuming a label proves content | Metadata-only useful pages survive; blank/error shells do not gain credibility from their URL alone |
| Medium | **SAML handoff rejection earlier in processing (existing)** | Known handoffs avoid needless work while actual articles about authentication are preserved |
| Medium | **Blacklist growth (existing):** investigate efficient root/subdomain representation rather than endlessly adding pairs | Large rule sets are benchmarked; any compact representation preserves exact YaCy host/path and subdomain semantics |
| Medium | **A shared cleanup/protection policy (new):** clarify which rules honor which allowlists and what host/domain scope each uses | An administrator can predict the effect of every whitelist and deletion action from the UI and tests |
| Medium | **Repeatable parser/cleanup fixtures (new expansion):** multilingual errors, Unicode title limits, parking-provider negatives and changing video responses | Tests work offline and measure false positives, not just successful matches |
| Medium | **Remote-crawl simulations (new validation work):** empty, slow, failing and recovering peers plus local queue pressure | No destructive peer demotion, unbounded retry, provider starvation or remote-queue overflow |
| Medium | **Concurrent search/cleanup tests (new validation work):** active search, filtered rows, date sort and cleanup together | Searches finish without cache invalidation stalls; delayed cleanup eventually finishes |
| Medium | **Explicit parser/network budgets (new):** integrate enrichment and landing-page probes with ordinary fetch policy, caching, redirect/address checks and total time limits | Extra fetches cannot unexpectedly bypass crawler restrictions or monopolize workers |
| Medium | **General abuse controls (new alternative):** request budgets, clear retry responses and bounded heuristic crawl amplification | Excess requests are predictably limited without corrupting ordinary results or multiplying crawl queues |
| Lower | **Audit privacy and efficient retention (new):** structured action IDs, protected excerpts and efficient rolling retention | Useful evidence remains available without exposing sensitive source or heavy full-log rewrites |

A JavaScript-rendering browser remains outside the chosen design. It should
not quietly become a dependency while improving app-shell handling. Any future
proposal for it needs its own resource, security and operational discussion.

## What Is Not Part Of This Catalog's Retained Feature Set

- **The YaCy-to-OpenSearch bridge:** its peer selection, caching, queueing,
  hashing, prewarming, highlighting and pagination belong to the separate bridge
  project, even when they use these nodes.
- **The earlier search-result text blacklist and count-rebuilding experiments:**
  the user-provided conversation records the decision to abandon them and return
  to a fresh repo. They are not the current crawler content-rejection feature,
  which acts before indexing.
- **The earlier Amazon URL-normalization experiment:** the user requested a
  known-good restore and reported restored functionality in the conversation
  supplied for this review. No retained Amazon-normalization feature is verified here.
  A future attempt needs a fresh isolated design and backup, not assumptions
  that the old experiment is still active.
- **Unimplemented browser rendering or mass historical enrichment:** neither
  follows automatically from metadata fallback during a new crawl.
- **Stock YaCy settings:** the permission to crawl URLs containing `?`, ordinary
  redirects, heuristic crawling and the original search-event-cache checkbox
  are not new features we should claim to have invented.

The inspected retained commit history does not preserve a precise rollback
commit for the old Amazon or result-filter experiments. Their retirement is
reported in the user-provided conversation, not independently reconstructed
from Git. Neither is counted as a retained implementation here; this is not
proof that every old backup is gone.

## Evidence And Contribution Boundaries

This table accounts for all 37 retained functional commits. Related refinements
are grouped deliberately. Read the linked topic notes and source before using
these as cherry-pick instructions: several groups depend on one another.

| Catalog entries | Retained commits | Main source / supporting notes |
| --- | --- | --- |
| R1 | `e1bdf3101` | [RemoteInstance.java](../source/net/yacy/cora/federate/solr/instance/RemoteInstance.java), [thread handoff](../FLEET_THREAD_EVICTOR_FIX_HANDOFF.md) |
| R2 | `466e57146` | [AccessTracker.java](../source/net/yacy/search/query/AccessTracker.java), [history notes](access-tracker-log-notes.md) |
| R3, R4 | `931489066` | [SearchEvent.java](../source/net/yacy/search/query/SearchEvent.java), [SearchEventCache.java](../source/net/yacy/search/query/SearchEventCache.java), [Switchboard.java](../source/net/yacy/search/Switchboard.java), [parser/cleanup notes](parser-metadata-notes.md) |
| M1 | `1f98f7fe0`, `7e83c5278` | [ContentScraper.java](../source/net/yacy/document/parser/html/ContentScraper.java) |
| M2 | `ef900be9c` | [TransformerWriter.java](../source/net/yacy/document/parser/html/TransformerWriter.java) |
| M3 | `4b41c16e5`, `b2b13923b` | [ContentScraper.java](../source/net/yacy/document/parser/html/ContentScraper.java) |
| M4, M5 | `dba5d7c3e`, `8502690be` | [htmlParser.java](../source/net/yacy/document/parser/htmlParser.java), [ContentScraper.java](../source/net/yacy/document/parser/html/ContentScraper.java) |
| Y1, Y2 | `e491a4092`, `1f718132b`, `86a6a4b32`, `331590df1` | [htmlParser.java](../source/net/yacy/document/parser/htmlParser.java), [metadata notes](parser-metadata-notes.md) |
| Y3 | `049e5a61f`, `e2e3d4396` | [htmlParser.java](../source/net/yacy/document/parser/htmlParser.java), [Switchboard.java](../source/net/yacy/search/Switchboard.java) |
| Y4 | `77e90d398`, `28f1ab060` | [htmlParser.java](../source/net/yacy/document/parser/htmlParser.java), [MetadataQuality.java](../source/net/yacy/search/schema/MetadataQuality.java) |
| Q1 | `870896950` | [CrawlStacker.java](../source/net/yacy/crawler/CrawlStacker.java), [MetadataQuality.java](../source/net/yacy/search/schema/MetadataQuality.java), [Switchboard.java](../source/net/yacy/search/Switchboard.java) |
| Q2, Q3 | `83f19d2cb`, `790e7a35e` | [MetadataQuality.java](../source/net/yacy/search/schema/MetadataQuality.java), [Switchboard.java](../source/net/yacy/search/Switchboard.java) |
| Q4, Q5 | `d6012b65d`, `31369a880` | [ErrorCache.java](../source/net/yacy/search/index/ErrorCache.java), [failure-marker UI](../htroot/IndexCreateParserErrors_p.html) |
| C1 | `675658ca1`, `13c8b6c73`, `353eb1414` | [CrawlerContentRejection.java](../source/net/yacy/crawler/data/CrawlerContentRejection.java), [content-rule UI](../htroot/CrawlerContentRejection_p.html) |
| C2, C3 | `d8c8f39d4`, `caa0380a5` | [CrawlerContentRejection.java](../source/net/yacy/crawler/data/CrawlerContentRejection.java), [Switchboard.java](../source/net/yacy/search/Switchboard.java) |
| C4, C5 | `caa0380a5`, `fa4eec75d`, `1616bebda` (with D1 and R3 refinements) | [crawler monitor](../htroot/Crawler_p.html), [Switchboard.java](../source/net/yacy/search/Switchboard.java), [notes](parser-metadata-notes.md) |
| C6 | `3c108af51` | [content-rule UI](../htroot/CrawlerContentRejection_p.html) |
| D1 | `48c4ce7aa` | [Dead Domains UI](../htroot/DeadDomains_p.html), [Switchboard.java](../source/net/yacy/search/Switchboard.java) |
| D2 | `09e85887c` | [CrawlQueues.java](../source/net/yacy/crawler/data/CrawlQueues.java), [Switchboard.java](../source/net/yacy/search/Switchboard.java) |
| D3 | `6da22f99e` | [CrawlQueues.java](../source/net/yacy/crawler/data/CrawlQueues.java), [Protocol.java](../source/net/yacy/peers/Protocol.java), [remote-crawl notes](remote-crawl-notes.md) |
| U1 | `5e4c8ca1d` | [expert crawl form](../htroot/CrawlStartExpert.html) |
| U2 | `da10c19b8`, `386cadb4b` | [yacysearchitem.java](../source/net/yacy/htroot/yacysearchitem.java), [response-policy handoff](../FLEET_YACY_SPACE_RESPONSE_PATCH_HANDOFF.md) |

The thread-fix hashes `481764a3e` and `99b14e964` refer to equivalent patch
versions on other branches, not two additional features. Its upstream PR uses
`99b14e964`; the AccessTracker PR uses `3bb969ea4`, equivalent to our retained
`466e57146`. Documentation-only follow-ups are indexed in the central changelog
rather than inflated into new application capabilities here.

Existing focused test sources include
[HTML parser tests](../test/java/net/yacy/document/parser/htmlParserTest.java),
[metadata-quality tests](../test/java/net/yacy/search/schema/MetadataQualityTest.java),
[content-rule tests](../test/java/net/yacy/crawler/data/CrawlerContentRejectionTest.java),
[error-cache tests](../test/java/net/yacy/search/index/ErrorCacheTest.java), and
[AccessTracker tests](../test/java/net/yacy/search/query/AccessTrackerTest.java).
Their presence is not a claim of complete concurrency, fleet or negative-case
coverage. The suggestions above identify where that coverage should grow.

## Keeping This Useful

For each future meaningful feature or correction, update the chronological
[change log](dev-node-change-log.md) and this grouped catalog. Amend the existing
entry when refining a capability; add a new one when the user-visible behavior
is genuinely new. Keep current behavior, dated validation, upstream status and
future proposals distinct. Update contribution status after a PR changes state.
