# AccessTracker History-Page Fix: On-Demand Fleet Handoff

Prepared 2026-09-20 UTC by the YaCy dev builder for the node provisioner in
`/home/programmer/Documents/Server23_and_soforth`.

## Scope And Decision

Apply this patch to **one affected standard node at a time**, when its Local
Search Log page fails with the confirmed AccessTracker query-history parsing
error. Preserve the failing history and prove that the same page works with
that history after installation. This is not an instruction to roll out across
the fleet now. No fleet node was modified when preparing this handoff.

Targets: explicitly selected `/opt/yacy`, `yacy.service`, HTTP 8090 instances.
Exclude Server2 (both standard and dev) and all `/opt/yacy-dev` installs. The
Server2 dev instance already has this fix and is not a rollout target.
Do not modify the index, blacklists, settings, heaps, dependencies, or services
other than the selected standard YaCy service. Do not reboot the server.
Coordinate the brief service interruption with other maintenance on that node.
The provisioner owns target selection, deployment records and its workspace's Git.

## What Is Fixed

Raw CR/LF characters in a search query could create extra physical lines in
`DATA/LOG/queries.log`. A continuation line could then reach an unprotected
numeric conversion in `AccessTracker.readLog()`, causing the history page's
optional top-word statistics to abort the whole page. Other malformed-line
positions could instead produce empty history. The patch:

- Normalizes CRLF, CR and LF only in the logged copy, not the executed query.
- Validates timestamps/counts and skips malformed legacy lines during both
  date seeking and reading, without editing existing history.
- Corrects `[from, to)` date boundaries, duplicate timestamps and EOF handling.
- Uses bounded buffered UTF-8 reading instead of allocating the whole range.

No template, settings, database schema, crawler or search-policy changes.
This is not a repair for arbitrary servlet errors, OOM, disk-full or Solr faults.

Source commit: `466e57146` in `blinggnome/yacy_search_server`.
Isolated upstream commit: `3bb969ea4`; PR:
https://github.com/yacy/yacy_search_server/pull/831
The PR's CI checks passed at the original dev closeout; check GitHub for later
review/merge status rather than treating this dated handoff as current status.

## Artifact And Compatibility Gate

Portable archive, delivered beside this handoff in the provisioner workspace:

```text
pyinfra/files/access-tracker-20260920.tar.gz
SHA256 39503bc6f7b36b2f5abc799ad1d14e1320c39c8cbea1ff90336ecb5fe2bc2a3a
```

Authoring copy:
`/home/programmer/Documents/yacy_dev_node_builder/backups/access-tracker-handoff-20260920/access-tracker-20260920.tar.gz`.

Archive contents: `yacycore.jar`, `AccessTracker.java`, `AccessTrackerTest.java`,
`0001-Fix-malformed-query-history-records-and-date-range-b.patch`, `SHA256SUMS`.
It contains no query logs, passwords, settings, databases or private reports.

| File/role | SHA256 |
| --- | --- |
| Required existing `yacycore.jar` | `080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9` |
| Patched `yacycore.jar` | `cb979066b5a501d27440cab24b89c801d19068b7c793b8387a57e5685f20a42c` |
| Original source, when present | `935180c3cef4691a2f5c55a3335ff2a8de2afd9a3f6f4cd5cdb4b8dd01ac9e91` |
| Patched source | `22212dcd9acaa622e2662c97165fa1d1479db15f0f0468fc0ae8fce20d534ee1` |

The supplied JAR is the **tested class-only overlay on the known fleet JAR**,
not a replacement from the latest full dev build. ZIP comparison verified that
only these four entries changed and all **1,650 other entries are byte-identical**:

```text
net/yacy/search/query/AccessTracker.class
net/yacy/search/query/AccessTracker$1.class
net/yacy/search/query/AccessTracker$Location.class
net/yacy/search/query/AccessTracker$QueryEvent.class
```

This preserves the known baseline's evictor, remote-crawl and response patches.
The same original whole-JAR hash was documented on Servers48/50 and in the
provisioner's response-patch runbook. Those are historical observations: check
the selected node now. **Different old hash: stop**, even if AccessTracker itself
looks identical. Do not downgrade a newer/custom build or overwrite later fixes.
Ask the dev builder to prepare and test an overlay for that exact baseline.
If the installed hash already equals the patched hash, do not reinstall/restart;
verify the page and investigate any remaining failure separately.

The new classes target Java 11 (class-file major version 55); retain the node's
existing supported Java runtime. `BoundedInputStream.builder()` requires a
compatible Commons IO already on YaCy's runtime classpath. Approved dependency
files checked locally for this handoff are:

```text
commons-io-2.19.0.jar 824268919b4b62f9f40f08c54381de5993b078f58667e332d17348ae019d72b9
commons-io-2.22.0.jar 2b9a7b1f726fb86216dbd2c8321eabe0221dbd5b1be81c18e1cb53811b104758
```

Confirm the actual service JVM/classpath and Commons IO JAR(s). If another or
shadowing version is present, stop for compatibility verification; do not fix
that by replacing dependencies as part of this deployment.

## 1. Capture A Live Failure Before Restarting

Use the provisioner's existing inventory and local admin authentication method.
Do not obtain credentials from this handoff or put passwords in arguments,
transcripts, Git, screenshots or artifacts. Capture HTTP bodies/log evidence
only in root-only node backups or ignored private results directories.

For manual diagnostics, SSH to the selected node and enter `sudo -i` first.
Create a private observation directory (separate from the install backup):

```bash
OBS=/root/access-tracker-observation-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -m 0700 "$OBS"
cd "$OBS"
```

From that root shell, record:

```bash
date -u
hostname -f
systemctl show yacy.service -p ActiveState -p SubState -p MainPID -p User -p ExecStart
sha256sum /opt/yacy/lib/yacycore.jar
sha256sum /opt/yacy/lib/commons-io-*.jar
df -h /opt/yacy
du -h /opt/yacy/DATA/LOG/queries.log
```

Require the service to be active and the service target to be the selected
standard installation. Check RAM/thread health if there are broader symptoms.
Have sufficient free space for a full query-log backup plus the candidate and
old JARs, even if reflinks are not supported. Stop if disk space is insufficient.

Authenticate and fetch `http://127.0.0.1:8090/AccessTracker_p.html?page=2`.
Record HTTP status, elapsed time and whether the body is an error, a login page
or the actual Local Search Log. A 200 login/error page is not a passing test.
Compare with page 1 and a read-only Solr count request:

```text
/AccessTracker_p.html?page=1
/solr/collection1/select?q=*:*&rows=0&wt=json
```

For a manual check, `curl --digest --user "$ADMIN_USER"` prompts for the password
without placing it in the command. For example, in the root-only case directory:

```bash
curl --silent --show-error --digest --user "$ADMIN_USER" --max-time 60 \
  --output before-page2.html --write-out 'HTTP %{http_code}; %{time_total}s\n' \
  'http://127.0.0.1:8090/AccessTracker_p.html?page=2'
```

Correlate the request time with `/opt/yacy/DATA/LOG/yacy00.log` and rotated YaCy
logs if necessary. The diagnostic signature is `NumberFormatException` in
`AccessTracker.readLog`, called by `AccessTracker_p.respond`; old builds show
lines 272/274. A generic `YaCyDefaultServlet.handleTemplate` stack alone does not
confirm the cause. Do not publish surrounding query text or exception tokens.
Servers48/50 were previously affected, but their present state is not assumed.

Reference: provisioner `docs/yacy-access-tracker-log-errors.md`. Its diagnosis
remains useful, but its statement that no fix has been built is superseded here.
Do not clear or edit the history, restart first, or deliberately inject malformed
queries into an unpatched production node to manufacture a failure.

## 2. Stage And Verify

On the provisioner workstation, select one inventory-resolved SSH destination
and verify the supplied archive before copying it. Replace `TARGET` deliberately;
do not expand this command to the whole inventory.

```bash
TARGET='debian@REPLACE_WITH_SELECTED_HOST'
sha256sum pyinfra/files/access-tracker-20260920.tar.gz
scp pyinfra/files/access-tracker-20260920.tar.gz \
  "$TARGET:access-tracker-20260920.tar.gz"
ssh "$TARGET"
sudo -i
```

On the target in the root shell (adapt the upload path only if its SSH account
has a different home directory):

```bash
set -euo pipefail
archive=/home/debian/access-tracker-20260920.tar.gz
test "$(sha256sum "$archive" | cut -d ' ' -f1)" = \
  39503bc6f7b36b2f5abc799ad1d14e1320c39c8cbea1ff90336ecb5fe2bc2a3a
stage=$(mktemp -d /root/access-tracker-stage.XXXXXXXX)
tar -xzf "$archive" -C "$stage"
(cd "$stage" && sha256sum -c SHA256SUMS)
printf 'Verified staging directory: %s\n' "$stage"
```

Keep this shell's `stage` variable for the next step. Do not use the existing
`yacy_space_response_patch.py` unchanged: it points to the older JAR and writes
unrelated settings. For later automation, translate these explicit guards and
checks into a dedicated provisioner-owned pyinfra operation with one-host scope.

## 3. Back Up And Install

Run the following block only after diagnosis, scope and dependency checks pass.
It backs up the JAR/source, stops YaCy, preserves the final flushed history,
installs atomically and starts only the selected service. It makes no config
changes. A synchronous failure attempts restoration/start of the old runtime.
Post-start HTTP failure still needs the manual rollback below.

```bash
bash -s -- "$stage" <<'INSTALL'
set -Eeuo pipefail
stage=$1
root=/opt/yacy
service=yacy.service
old=080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9
new=cb979066b5a501d27440cab24b89c801d19068b7c793b8387a57e5685f20a42c
source=source/net/yacy/search/query/AccessTracker.java
test "$(id -u)" = 0
systemctl is-active --quiet "$service"
test "$(sha256sum "$root/lib/yacycore.jar" | cut -d ' ' -f1)" = "$old"
test "$(sha256sum "$stage/yacycore.jar" | cut -d ' ' -f1)" = "$new"
(cd "$stage" && sha256sum -c SHA256SUMS)
if test -e "$root/$source"; then
  test "$(sha256sum "$root/$source" | cut -d ' ' -f1)" = \
    935180c3cef4691a2f5c55a3335ff2a8de2afd9a3f6f4cd5cdb4b8dd01ac9e91
fi
case_dir="$root/backups/access-tracker-$(date -u +%Y%m%dT%H%M%SZ)"
mkdir -p "$root/backups"
mkdir -m 0700 "$case_dir"
cp -a "$root/lib/yacycore.jar" "$case_dir/yacycore.jar"
if test -e "$root/$source"; then
  cp -a "$root/$source" "$case_dir/AccessTracker.java"
fi
sha256sum "$case_dir/yacycore.jar"
test "$(sha256sum "$case_dir/yacycore.jar" | cut -d ' ' -f1)" = "$old"
printf 'CASE_DIR=%s\n' "$case_dir"
recover() {
  trap - ERR
  set +e
  systemctl stop "$service" || { printf 'Cannot stop service; manual recovery required.\n' >&2; return; }
  test "$(systemctl show "$service" -p MainPID --value)" = 0 || {
    printf 'JVM still running; runtime files left alone.\n' >&2; return;
  }
  cp -a "$case_dir/yacycore.jar" "$root/lib/yacycore.jar.access-tracker-restore" &&
    mv -f "$root/lib/yacycore.jar.access-tracker-restore" "$root/lib/yacycore.jar" || {
      printf 'JAR restoration failed; service left stopped.\n' >&2; return;
    }
  if test -f "$case_dir/AccessTracker.java"; then
    cp -a "$case_dir/AccessTracker.java" "$root/$source" || {
      printf 'Source restoration failed; service left stopped.\n' >&2; return;
    }
  fi
  systemctl start "$service"
  printf 'Installation failed. Recovery attempted; verify service and old hash. Backup: %s\n' "$case_dir" >&2
}
trap 'recover; exit 1' ERR
systemctl stop "$service"
test "$(systemctl show "$service" -p MainPID --value)" = 0
test "$(sha256sum "$root/lib/yacycore.jar" | cut -d ' ' -f1)" = "$old"
cp -a --reflink=auto "$root/DATA/LOG/queries.log" "$case_dir/queries.log.before-install"
cmp -s "$root/DATA/LOG/queries.log" "$case_dir/queries.log.before-install"
sha256sum "$case_dir/queries.log.before-install" > "$case_dir/history-before.sha256"
install -m 0644 "$stage/yacycore.jar" "$root/lib/yacycore.jar.access-tracker-new"
chown --reference="$case_dir/yacycore.jar" "$root/lib/yacycore.jar.access-tracker-new"
mv -f "$root/lib/yacycore.jar.access-tracker-new" "$root/lib/yacycore.jar"
if test -f "$case_dir/AccessTracker.java"; then
  install -m 0644 "$stage/AccessTracker.java" "$root/$source"
  chown --reference="$case_dir/AccessTracker.java" "$root/$source"
fi
test "$(sha256sum "$root/lib/yacycore.jar" | cut -d ' ' -f1)" = "$new"
systemctl start "$service"
date -u +%FT%TZ > "$case_dir/runtime-installed"
trap - ERR
systemctl show "$service" -p ActiveState -p MainPID
printf 'Installed; HTTP/application verification still required. CASE_DIR=%s\n' "$case_dir"
INSTALL
```

Record the printed `CASE_DIR`, old/new hashes, timestamps and old/new JVM PID
in the provisioner's ignored per-node results. Source synchronization is optional
when the installation has no source tree; never install test classes into YaCy.

## 4. Verify The Same Failing Page And Preserved History

Allow the node's known startup delay before polling. The dev observation was
about three minutes; use 185 seconds for an initial wait if no better per-node
measurement exists, then check HTTP every 15 seconds with a bounded deadline.
Example readiness check (does not prove the protected history page is fixed):

```bash
sleep 185
ready=false
for attempt in $(seq 1 28); do
  code=$(curl --silent --output /dev/null --write-out '%{http_code}' \
    --max-time 10 http://127.0.0.1:8090/ || true)
  if test "$code" = 200; then ready=true; break; fi
  sleep 15
done
test "$ready" = true
```

If this deadline expires, stop and inspect service/log state before rollback;
do not loop restarts. Service `active` alone is not readiness.

Repeat the **same authenticated page-2 request** that failed before installation.
Require HTTP 200, the actual `Local Search Log` heading/table, and no `Ops!`,
`ServletException` or login response. Recheck page 1, the Solr count endpoint and
normal local search. Use an ordinary local-only smoke query (`resource=local`),
not a global network broadcast. Fetch the external node URL as well.
Check only newly written YaCy logs for fresh AccessTracker exceptions or linkage
errors such as `NoSuchMethodError`; pre-existing errors are expected in old logs.

Verify history preservation without printing any query text. Replace the backup
path with the exact recorded one; do not guess the latest backup directory:

```bash
CASE_DIR=/opt/yacy/backups/access-tracker-REPLACE_WITH_RECORDED_TIMESTAMP
python3 - "$CASE_DIR/queries.log.before-install" /opt/yacy/DATA/LOG/queries.log <<'PY'
import json, sys
from pathlib import Path
old, current = map(Path, sys.argv[1:])
size = old.stat().st_size
same = True
with old.open('rb') as before, current.open('rb') as after:
    while True:
        block = before.read(1024 * 1024)
        if not block:
            break
        if after.read(len(block)) != block:
            same = False
            break
print(json.dumps({'original_bytes': size, 'current_bytes': current.stat().st_size,
                  'original_history_is_unchanged_prefix': same}))
sys.exit(0 if same else 1)
PY
sha256sum /opt/yacy/lib/yacycore.jar
```

If an external log-rotation task intervenes, stop and account for that separately;
do not call a mismatched prefix a successful preservation check or rewrite files
to make it pass. Do not restore old history on a code rollback either.

The history page's displayed entry/request counters reset at restart because
they are held in memory. A small counter is **not** evidence of history deletion:
the table is not reloaded from `queries.log`; its seven-day topic statistics and
the timeline use the persisted file separately.

Optional additional reader check: authenticate to
`/api/timeline_p.xml?data=queries&head=10` and confirm valid XML/events for a known
recent interval. This existing endpoint also flushes buffered query history;
record that side effect. Empty topics alone are not failure if only `sq` queries
exist (topics use `qs`). Do not send synthetic multiline searches to a live
failure node by default: the preserved failure is the preferred test case.

Recheck after subsequent normal traffic and again later that day/next day.
Only label the deployment **verified** after the before/after application checks
and prefix/hash checks pass. A recovered HTTP root page alone is insufficient.

## 5. Rollback And Stop Conditions

Stop expanding deployment for unknown baseline/source/dependencies, failed
backup, changed history, missing artifact checksums, unresolved page failures,
linkage errors, or regressions in search/crawling/thread health. Preserve private
evidence and report to the dev builder; do not repeatedly edit or restart.

For an authorized rollback, use the exact recorded backup on the same node:

```bash
set -euo pipefail
CASE_DIR=/opt/yacy/backups/access-tracker-REPLACE_WITH_RECORDED_TIMESTAMP
test "$(sha256sum "$CASE_DIR/yacycore.jar" | cut -d ' ' -f1)" = \
  080eb3072a9618d9ccf258a5b2f78b049b6f5c36fe2e0d073ec14fde453009f9
systemctl stop yacy.service
test "$(systemctl show yacy.service -p MainPID --value)" = 0
cp -a "$CASE_DIR/yacycore.jar" /opt/yacy/lib/yacycore.jar.access-tracker-restore
mv -f /opt/yacy/lib/yacycore.jar.access-tracker-restore /opt/yacy/lib/yacycore.jar
if test -f "$CASE_DIR/AccessTracker.java"; then
  cp -a "$CASE_DIR/AccessTracker.java" /opt/yacy/source/net/yacy/search/query/AccessTracker.java
fi
systemctl start yacy.service
sha256sum /opt/yacy/lib/yacycore.jar
```

Repeat the bounded HTTP and standard service checks. The original history-page
failure may return because the malformed legacy record remains; rollback is not
a history repair. **Never restore/truncate `queries.log` or any index files**
as part of this code rollback. Retain backups through the observation period.

## Evidence And Future Baselines

Report: node ID, UTC time, baseline/candidate/installed hashes, backup directory,
before/after page status and body validation, diagnostic stack method (no query
text), history prefix result, service/PID, dependency verification, ordinary
search/Solr checks, later follow-up, and whether rollback was needed.

Prior tests: 11 AccessTracker regressions fail on baseline and pass fixed;
19 focused tests passed on dev/upstream integration and were rerun successfully
against this packaged runtime JAR during handoff preparation. Dev live tests
preserved 5,496,957 original log bytes. Upstream application compile/CI passed;
its full `compileTest` has a separately documented Solr bridge test-classpath
issue. Do not change that build configuration to deploy this artifact.

Handoff verification also checked all shell/Python command syntax and all
archive member hashes. The documented installer passed isolated local tests
with simulated service control for success, unknown JAR, unknown source,
startup failure and missing history. These tests do not replace live canary
verification; no standard-node installation has been attempted by this author.

For a different future baseline, use the included source/test patch in an
isolated builder branch and rerun tests. Preserve all of that node's existing
patches; do not cherry-pick the whole dev branch. The dev-builder Git belongs to
its agent. Ask for a compatible artifact rather than modifying that workspace.
No claim is made that this snapshot handles arbitrary out-of-order history or
reconstructs the exact text of old multiline queries; original files are retained.

Builder details: `docs/access-tracker-log-notes.md` and
`docs/work-orders/completed/20260920T030516Z-access-tracker-fix.md`.
