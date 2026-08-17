# Fleet Handoff: yacy.space Response Patch

Date: 2026-08-17
Development workspace: `/home/programmer/Documents/yacy_dev_node_builder`
Dev target tested: Server2 dev node, `/opt/yacy-dev`, `yacy-dev.service`, port `8091`

## Branch and Commits

- Branch: `yacy-space-abuse-message`
- Base: `master` at `822cdb127`
- Existing URL blackhole behavior was already present on `master` from `da10c19b8`.
- Preserved Solr HTTP-client evictor fix by cherry-picking `99b14e964` as local commit `e1bdf3101`.
- New response-message implementation commit: `386cadb4b`.

Changed source files relative to `master`:

- `source/net/yacy/cora/federate/solr/instance/RemoteInstance.java`
- `source/net/yacy/htroot/yacysearchitem.java`

## Behavior

The response patch affects only unauthenticated search result rendering when:

```ini
search.result.blackhole.enabled=true
search.result.blackhole.userAgent=yacy.space-remote-fetcher/1.0
```

For matching requests:

- Visible result URLs are rewritten through the existing result blackhole path.
- The active TLD pool is narrowed to `space`.
- Visible result titles are replaced with the configured warning title plus an 8-character per-result suffix.
- Visible result descriptions/snippets are replaced with the configured warning body plus the same suffix used in that result title.
- Heuristic search-result crawling remains suppressed by the existing `!blackholeResult` guard.

Normal User-Agents, authenticated users, admins, stored index records, direct Solr APIs, exported data, and blacklists are not intentionally changed.

Recommended fleet config:

```ini
search.result.blackhole.enabled=true
search.result.blackhole.userAgent=yacy.space-remote-fetcher/1.0
search.result.blackhole.tldPool=space
search.result.blackhole.message.enabled=true
search.result.blackhole.title=You are destroying the Yacy Network Please Stop
search.result.blackhole.message=We hope you are doing this by accident, and that is the reason for the friendly invitation to join the forum and find out what you have done. Hundreds of nodes that do Heuristic crawling have had their hard drives filled up, and their crawl queues are in the millions, so they are out of RAM and crash. Many people who run casual nodes at home/work have quit the project because of the load you are putting on all of our systems. Please stop. This project has been around over 20 years, and you are in danger of destroying it. Please come talk in the forum. Thank you. There is no contact information, so this was the only way to reach you before contacting Digital Ocean and reporting your millions of searches a day as a denial of service attack. By the way, you hit every dual addressed node twice with every query because you are targeting both the IPv4 and V6 addresses as if they were two machines.
```

Disable switch:

```ini
search.result.blackhole.enabled=false
```

## Build and Dev Deployment

Local checks:

- `git diff --check`: passed.
- `ant compile`: passed after running outside the restricted sandbox so Ivy could write `~/.ivy2`.
- `ant compileTest`: passed.

Built artifact:

```text
/home/programmer/Documents/yacy_dev_node_builder/lib/yacycore.jar
SHA256: 080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9
```

Dev deployment:

- Deployed jar: `/opt/yacy-dev/lib/yacycore.jar`
- Deployed jar SHA256: `080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9`
- Previous jar SHA256: `05486b584596b2cb2b668d1c627b9dc52a229fc257d214426fb391e9176ec4bd`
- Backup directory: `/opt/yacy-dev/backups/yacy-space-abuse-message-20260817-053906`
- Backup files:
  - `yacycore.jar.before`
  - `yacy.conf.before`

Post-restart status:

- `yacy-dev.service`: active.
- HTTP check: `http://ns100727.ip-147-135-4.us:8091/` returned successfully.
- Dev home page local fetch size: `13119` bytes.
- `Connection evic` threads on the actual `yacy-dev.service` MainPID: `3`.
- Total threads on the actual dev Java process at check time: `140`.

## Functional Test Summary

All functional checks used `resource=local` to avoid adding network fan-out while the dev node had a large crawl queue.

HTML search, query `yacy`, `maximumRecords=5`:

- Normal User-Agent:
  - warning title count: `0`
  - warning body count: `0`
  - result example remained normal: `https://hub.docker.com/u/yacy`
- Matching User-Agent:
  - warning title present.
  - warning body present.
  - rewritten URL example: `https://hub.docker.space/u/yacy`
  - example suffix matched between title and body: `1du5op2l`

JSON search, query `video`, `maximumRecords=5`:

- Normal User-Agent:
  - warning title count: `0`
  - warning body count: `0`
- Matching User-Agent:
  - warning title count: `5`
  - warning body count: `5`
  - `.space` URL examples included:
    - `https://tv.newsday.space/`
    - `https://indieweb.space/video`
    - `https://roanoke.space/video/`
  - first five result suffix checks all matched title to description:
    - `tgez3u15`: true
    - `1j37i7hf`: true
    - `1q36r1o8`: true
    - `gkprlm1i`: true
    - `16pre7as`: true

RSS/OpenSearch search, query `video`, `maximumRecords=5`:

- Normal User-Agent:
  - warning title count: `0`
  - warning body count: `0`
- Matching User-Agent:
  - warning title count: `5`
  - warning body count: `5`
  - `.space` URL examples were present.

Authenticated/admin same-User-Agent behavior was not live-tested with credentials in this run, but the code path keeps the pre-existing `!authenticated && isResultBlackholeUserAgent(...)` match boundary.

Direct Solr/API output was not separately tested. The implementation remains scoped to `yacysearchitem` response rendering.

## Fleet Rollout Notes

Fleet rollout should be handled from `/home/programmer/Documents/Server23_and_soforth`.

Targets:

- Standard YaCy nodes only.
- Standard root: `/opt/yacy`
- Standard service: `yacy.service`
- Standard port: `8090`

Exclusions:

- Do not touch `/opt/yacy-dev`.
- Do not touch `yacy-dev.service`.
- Do not overwrite the Server2 dev test backup.

Suggested rollout:

1. Stage exactly the built jar above and verify SHA256.
2. Canary on one standard node.
3. Verify HTTP 200, local search, matching User-Agent mutation, and `Connection evic` count after a search.
4. Roll out in conservative batches.
5. Keep timestamped backups of every replaced `yacycore.jar` and changed `yacy.conf`.

Rollback:

1. Set `search.result.blackhole.enabled=false` to disable the mitigation without replacing the jar.
2. If jar rollback is needed on Server2 dev, restore:
   `/opt/yacy-dev/backups/yacy-space-abuse-message-20260817-053906/yacycore.jar.before`
   to `/opt/yacy-dev/lib/yacycore.jar`.
3. Restore `/opt/yacy-dev/backups/yacy-space-abuse-message-20260817-053906/yacy.conf.before` if config rollback is needed.
4. Restart the affected service and verify HTTP readiness with a web check.
