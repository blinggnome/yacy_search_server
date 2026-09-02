# Fleet Handoff: Remote Solr HTTP Client Thread Leak Fix

## Purpose

Roll out the YaCy remote Solr HTTP client leak fix to the standard YaCy fleet.
This fixes a failure mode where YaCy accumulates thousands of Apache/Solr
`Connection evic` threads, eventually failing requests with:

```text
java.lang.OutOfMemoryError: unable to create native thread
```

The upstream pull request is:

```text
https://github.com/yacy/yacy_search_server/pull/809
```

## Scope

Target standard YaCy nodes only:

- install path: `/opt/yacy`
- service: `yacy.service`
- HTTP port: `8090`
- service user/group: `yacy:yacy`

Do not touch Server2 dev paths:

- `/opt/yacy-dev`
- `yacy-dev.service`
- port `8091`

Use the Server23 pyinfra workspace:

```text
/home/programmer/Documents/Server23_and_soforth
```

## Known-Good Artifact

The artifact currently deployed and validated on both Server50 and the dev node
has this SHA256:

```text
05486b584596b2cb2b668d1c627b9dc52a229fc257d214426fb391e9176ec4bd
```

Known-good locations at the time this handoff was written:

```text
Server50: /opt/yacy/lib/yacycore.jar
Dev node: /opt/yacy-dev/lib/yacycore.jar
```

Important: the local `lib/yacycore.jar` in
`/home/programmer/Documents/yacy_dev_node_builder` may have been rebuilt while
preparing the upstream PR and should not be assumed to be the same runtime
artifact. Before fleet rollout, stage a single jar artifact intentionally and
verify its SHA256.

Recommended staging path in the Server23 workspace:

```text
pyinfra/files/yacycore-thread-leak-fix/yacycore.jar
```

Example staging command from Server50:

```bash
cd /home/programmer/Documents/Server23_and_soforth
mkdir -p pyinfra/files/yacycore-thread-leak-fix
rsync -av debian@ns3072014.ip-37-187-226.eu:/opt/yacy/lib/yacycore.jar \
  pyinfra/files/yacycore-thread-leak-fix/yacycore.jar
sha256sum pyinfra/files/yacycore-thread-leak-fix/yacycore.jar
```

The hash must match:

```text
05486b584596b2cb2b668d1c627b9dc52a229fc257d214426fb391e9176ec4bd
```

## Current Canary Evidence

Before patching, Server50 had:

```text
Threads: 38393
TasksCurrent: 38393
Connection evic threads: roughly 38248
HTTP 8090: empty reply / failing
```

After deploying the patched jar and restarting:

```text
Server50: 3 Connection evic threads, HTTP 200
Dev node: 3 Connection evic threads, HTTP 200
```

After the overnight check:

```text
Dev node: 227 total threads, 3 Connection evic, HTTP 200
Server50: 164 total threads, 3 Connection evic, HTTP 200
```

## Rollout Strategy

1. Regenerate pyinfra inventory.
2. Run read-only audit.
3. Stage the known-good jar and verify its hash.
4. Canary one unpatched standard node.
5. Verify canary with thread counts, HTTP, and a global search.
6. Roll out to the fleet in small batches or with conservative pyinfra
   concurrency.
7. Verify all nodes.
8. Re-check the fleet after several hours or the next day.

Server50 is already patched and can be skipped by idempotence if its jar hash
matches the target artifact.

If Server2 standard `/opt/yacy` is intentionally disabled because the dev node
is doing recovery work, exclude Server2 from this rollout unless the user
explicitly confirms it should be included. In all cases, never deploy the
standard fleet jar to `/opt/yacy-dev`.

## Preflight Commands

Run from:

```bash
cd /home/programmer/Documents/Server23_and_soforth
```

Regenerate inventory:

```bash
.venv/bin/python generate_pyinfra_inventory.py
```

Inspect inventory:

```bash
.venv/bin/pyinfra pyinfra/inventory.py debug-inventory --json
```

Run a read-only audit canary:

```bash
.venv/bin/pyinfra pyinfra/inventory.py pyinfra/deploys/audit.py \
  --limit server-50 --yes -vvv
```

Run a group audit when ready:

```bash
.venv/bin/pyinfra pyinfra/inventory.py pyinfra/deploys/audit.py \
  --limit yacy_nodes --yes -vvv
```

## Suggested pyinfra Deploy Shape

Create a new deploy such as:

```text
pyinfra/deploys/yacycore_thread_leak_fix.py
```

The deploy should:

1. Run only when `yacy_nodes` is in `host.groups`.
2. Verify `/opt/yacy/lib/yacycore.jar` exists.
3. Print current jar hash and current thread summary.
4. Upload the staged jar to a temporary path.
5. Verify uploaded jar hash.
6. If the current jar hash already matches the target hash, print
   `YACYCORE_THREAD_LEAK_FIX_ALREADY_MATCHES=1` and skip restart.
7. Otherwise:
   - backup `/opt/yacy/lib/yacycore.jar` to a timestamped file
   - install new jar as `yacy:yacy` mode `0644`
   - restart `yacy.service`
   - wait for local HTTP 200 on `http://127.0.0.1:8090/`
   - print post-restart thread summary

Use a timestamped backup name like:

```text
/opt/yacy/lib/yacycore.jar.pre-thread-leak-fix-YYYYmmdd-HHMMSS
```

Use this target hash in the deploy:

```text
05486b584596b2cb2b668d1c627b9dc52a229fc257d214426fb391e9176ec4bd
```

The core remote shell logic should be equivalent to:

```bash
set -eu
target_hash="05486b584596b2cb2b668d1c627b9dc52a229fc257d214426fb391e9176ec4bd"
jar="/opt/yacy/lib/yacycore.jar"
staged="/tmp/yacycore-thread-leak-fix.jar"

current_hash="$(sha256sum "$jar" | awk '{print $1}')"
echo "YACYCORE_CURRENT_HASH=$current_hash"

if [ "$current_hash" = "$target_hash" ]; then
  echo "YACYCORE_THREAD_LEAK_FIX_ALREADY_MATCHES=1"
  exit 0
fi

uploaded_hash="$(sha256sum "$staged" | awk '{print $1}')"
if [ "$uploaded_hash" != "$target_hash" ]; then
  echo "YACYCORE_UPLOADED_HASH_MISMATCH=$uploaded_hash"
  exit 1
fi

ts="$(date +%Y%m%d-%H%M%S)"
cp -a "$jar" "$jar.pre-thread-leak-fix-$ts"
install -o yacy -g yacy -m 0644 "$staged" "$jar"
systemctl restart yacy.service

for i in $(seq 1 60); do
  if curl -fsS --max-time 10 -o /dev/null http://127.0.0.1:8090/; then
    echo "YACY_HTTP_READY=1"
    break
  fi
  sleep 5
done

curl -fsS --max-time 10 -o /dev/null http://127.0.0.1:8090/

pid="$(systemctl show yacy.service -p MainPID --value)"
echo "YACY_MAINPID=$pid"
grep -E '^(Threads|VmRSS|VmSize):' "/proc/$pid/status"
evictors="$(
  for t in /proc/$pid/task/*/comm; do
    cat "$t" 2>/dev/null
  done | grep -c '^Connection evic$' || true
)"
echo "YACY_CONNECTION_EVICTORS=$evictors"
```

## Verification Commands

Per host, verify service and web readiness:

```bash
systemctl show yacy.service \
  --property=MainPID,TasksCurrent,TasksMax,ActiveState,SubState,ExecMainStartTimestamp \
  --no-pager
curl -sS --max-time 10 -o /dev/null -w 'YACY_HTTP=%{http_code} %{time_total}\n' \
  http://127.0.0.1:8090/
```

Per host, verify thread counts:

```bash
pid="$(systemctl show yacy.service -p MainPID --value)"
grep -E '^(Threads|VmRSS|VmSize):' "/proc/$pid/status"
for t in /proc/$pid/task/*/comm; do
  cat "$t" 2>/dev/null
done | sort | uniq -c | sort -nr | head -20
```

Expected result after restart and normal activity:

```text
Connection evic should stay low, around 3.
It must not climb into hundreds or thousands.
```

Trigger a global search path after the node is ready:

```bash
curl -sS --max-time 35 -o /tmp/yacy-global-search-smoke.html \
  -w 'SEARCH_HTTP=%{http_code} %{time_total}\n' \
  'http://127.0.0.1:8090/yacysearch.html?query=yacy&Enter=&verify=ifexist&contentdom=text&resource=global&maximumRecords=10'
```

Then re-check `Connection evic` threads.

## Rollback

If a node fails immediately after deploy:

1. Identify the most recent backup:

   ```bash
   ls -lt /opt/yacy/lib/yacycore.jar.pre-thread-leak-fix-*
   ```

2. Restore it:

   ```bash
   backup="/opt/yacy/lib/yacycore.jar.pre-thread-leak-fix-YYYYmmdd-HHMMSS"
   install -o yacy -g yacy -m 0644 "$backup" /opt/yacy/lib/yacycore.jar
   systemctl restart yacy.service
   ```

3. Wait for HTTP 200 on port 8090.

Do not delete backup jars until the fleet has been stable for at least an
overnight soak.

## Notes for the Rollout Agent

- This rollout requires a YaCy JVM restart on each node to load the new jar.
- Avoid restarting the entire fleet at once. Use a canary, then conservative
  batches.
- Large nodes can take several minutes to bind HTTP after restart while opening
  index and HTCache files. If logs are progressing, wait and re-check; do not
  send repeated restarts.
- Use network-capable execution for pyinfra, SSH, rsync, and curl work.
- Do not commit generated inventory, result JSON, staged jar payloads, backup
  files, or credentials.
- Record final result JSON/logs under `pyinfra/results/` if a helper script
  writes them; that directory is ignored by Git.
