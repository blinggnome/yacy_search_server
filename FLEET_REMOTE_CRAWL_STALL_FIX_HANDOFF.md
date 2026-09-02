# Fleet Remote Crawl Stall Fix Handoff

Date: 2026-09-01
Authoring workspace: `/home/programmer/Documents/yacy_dev_node_builder`
Target operations workspace: `/home/programmer/Documents/Server23_and_soforth`

## Purpose

Prepare the previously tested remote-crawl loader stabilization for controlled
fleet validation and rollout from the Server23 node provisioner workspace.

This fix addresses the behavior where YaCy nodes configured to accept remote
crawls could stop persistently looking for remote crawl queues after transient
provider failures, empty feeds, or small amounts of local crawl work.

## Patch Boundary

Isolated source commit:

```text
6da22f99efa7233f37f884588405848c72654ea8
Stabilize remote crawl provider handling
```

Files changed by that commit:

```text
docs/remote-crawl-notes.md
htroot/RemoteCrawl_p.html
source/net/yacy/crawler/data/CrawlQueues.java
source/net/yacy/htroot/RemoteCrawl_p.java
source/net/yacy/peers/Protocol.java
```

Current branch containment check from the dev-builder workspace:

```text
master contains 6da22f99e
yacy-space-abuse-message contains 6da22f99e
origin/master contains 6da22f99e
origin/yacy-space-abuse-message contains 6da22f99e
```

Important operational caveat: the yacy.space response branch already contains
this remote-crawl commit. The current local `lib/yacycore.jar` hash is:

```text
080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9
```

If the fleet is already running the yacy.space response jar built from this
branch lineage, the remote-crawl fix may already be installed fleet-wide. The
provisioner should verify the deployed artifact provenance or jar hash before
doing another rollout. Do not install an older "remote-crawl only" jar over the
active yacy.space response mitigation.

## Behavior Before The Fix

The previous loader treated provider advertisements too destructively:

- `Protocol.queryRemoteCrawlURLs()` set the provider seed `RCOUNT` to `0` and
  called `interfaceDeparture()` when a remote crawl request failed.
- A malformed, unreachable, or empty `/yacy/urls.xml` remote-crawl response
  could remove a peer from the active provider list even when the peer itself
  was still alive.
- `remoteCrawlLoaderJob()` recursively retried providers and could give up or
  churn through candidates in a brittle way.
- Any local crawl queue activity above `0` deferred remote intake, so small
  local crawl activity could suppress remote-crawl fetching for long periods.
- `RemoteCrawl_p.html` did not expose enough loader status to distinguish no
  providers, provider cooldowns, empty feeds, queue pressure, and real failures.

## Behavior After The Fix

The new loader treats remote-crawl advertisements as hints:

- Failed or empty provider responses are handled by an in-memory cooldown map,
  not by demoting the peer or clearing its advertised remote-crawl count.
- Provider candidates are rebuilt from peers advertising positive remote URL
  counts and shuffled before use.
- Each loader run tries a bounded number of providers, default `5`, before
  yielding back to the scheduler.
- Remote intake is allowed while the local crawl queue remains small, default
  limit `20`.
- The remote-triggered queue is capped at `200`; each provider request asks only
  for the remaining queue capacity, up to `60` URLs.
- `Protocol.queryRemoteCrawlURLs()` now decrements `RCOUNT` only after a
  non-empty feed is successfully parsed.
- `RemoteCrawl_p.html` exposes loader status, queue pressure, provider
  cooldown count, provider attempts, successes, fetched URL count, rejected URL
  count, empty feeds, and failed feeds.

Default tunables added by the patch:

```ini
remoteCrawlLoader.localQueueLimit=20
remoteCrawlLoader.maxProviderAttempts=5
remoteCrawlLoader.providerCooldownMillis=300000
```

No config changes are required to use the defaults.

## Recommended Provisioner Flow

1. Confirm whether the currently deployed fleet jar already contains
   `6da22f99e`.

   If the fleet jar hash is the same active yacy.space response artifact from
   the dev-builder branch lineage, treat this as a validation pass first.

2. If deployment is still needed, build from a branch that includes both:

   - the current active yacy.space response mitigation
   - commit `6da22f99efa7233f37f884588405848c72654ea8`

   Do not deploy a jar that drops either active fix.

3. Build in the dev-builder repo:

   ```bash
   cd /home/programmer/Documents/yacy_dev_node_builder
   ant compileTest
   sha256sum lib/yacycore.jar
   ```

4. Copy the verified `lib/yacycore.jar` into the Server23 provisioner artifact
   area and let the Server23 workspace own all Git, canary, and fleet actions.

5. Canary first on a small set of standard nodes that accept remote crawls.
   Exclude `/opt/yacy-dev` and `yacy-dev.service`; fleet rollout targets
   standard `/opt/yacy` installs only unless explicitly directed otherwise.

6. Before replacing jars on each canary, back up:

   ```text
   /opt/yacy/lib/yacycore.jar
   ```

7. Restart the standard YaCy service and verify HTTP health before moving to
   the next batch.

## Validation Signals

Use canaries with remote crawl enabled and inspect `RemoteCrawl_p.html`.

Healthy signs:

- HTTP returns 200 after restart.
- `Remote Crawl Loader Status` appears on the remote crawl admin page.
- `Last status` changes over time rather than remaining stale for hours.
- `run(s)` increases as the scheduler fires.
- Provider candidates and cooldowns vary instead of permanently emptying after
  one bad provider.
- `successful fetch(es)` and `URL(s) queued` increase over time on active
  network peers.
- Empty and failed feeds can occur, but they should not remove the peer from
  the active seed list merely because the remote-crawl endpoint was empty or
  transiently bad.
- The node continues normal search/crawl operation.

Regression signs:

- `Peers offering remote crawl URLs` empties for hours despite known peers with
  remote queues.
- One bad provider appears to suppress all future remote-crawl loading.
- Search or crawl pages begin throwing servlet errors.
- Remote-triggered queue remains permanently full at `200`.
- JVM thread counts, disk usage, or HTTP health show unrelated regressions.

## Rollback

For any canary failure:

1. Restore the timestamped backup of `/opt/yacy/lib/yacycore.jar`.
2. Restart the standard YaCy service.
3. Verify HTTP health.
4. Stop rollout and report the canary host, previous jar hash, candidate jar
   hash, service logs, and the `RemoteCrawl_p.html` status values.

## Notes For Future Upstreaming

This patch is a good upstream candidate because it makes remote crawl provider
handling less destructive and adds operational visibility without changing the
provider endpoint semantics. If upstreaming separately, use commit `6da22f99e`
as the starting patch boundary and avoid including local fleet-only response
mitigations.
