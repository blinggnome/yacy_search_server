# PR 834: Current Upstream Validation

Tested 2026-09-25 in fresh detached checkouts. No source patches, branch
rewrites, live-node operations or blacklist changes were needed.

## Exact Revisions

| Revision | Commit |
| --- | --- |
| Upstream master | `b50b76bd552a851f737b98683f20d8b861e137ae` |
| PR head | `f1eb2d107877abba585892bfc2fe2f926cefccae` |
| GitHub merge tested | `5cae18c88c8e618dca7d59d18f1c13fe2c8c9d97` |
| Merged tree | `407121c5a8d026c06c866a59f1da74577cb4e97a` |

The merge parents match the listed upstream/PR commits. Its complete eight-file
feature patch matches the original accepted contribution byte-for-byte. The
delta from the prior integration consists only of the four intervening upstream
commits: sorted `FileUtils.saveMap` output and escaped `ServletResource`
directory-listing links, their tests, and the two merge commits.

## Results

Linux; OpenJDK 21.0.12; Ant 1.10.14; JUnit 4.13.2; Node.js 22.17.1.
Explicit focused Java compilation used `--release 17` and a fresh output
directory placed first on the test runtime classpath.

| Check | Integration | Pristine upstream |
| --- | --- | --- |
| `ant compile` | PASS | PASS |
| Blacklist-focused JUnit | 29 passed | PR suite not applicable |
| ServletResource/FileUtils JUnit | 12 passed | 12 passed |
| Full `ant compileTest` | 89 errors | Same 89 errors |

All six focused classes passed in one integration run: **41 tests**.
`node --check htroot/js/BlacklistTest.js` passed. The audit's six additional
stubbed-DOM editor-return scenarios also passed against this integration; they
are not browser end-to-end tests.

Both full test compilations fail on existing Solr test-classpath errors and a
stale `HostBalancerTest`/`CrawlProfile` constructor. All 432 diagnostic lines
from first error through error count match exactly after normalization of only
checkout path prefixes. No source/build/test repairs were applied to conceal
these failures. Application builds are separate successful gates.

## Conclusion And Limits

No integration regression was found by these checks. Master and the PR merge
were rechecked before publication and still matched the tested objects. This
result applies to those pinned revisions, not arbitrary future upstream code.
It is local validation, not a new GitHub CI run or live-node/browser deployment.

The [audit's missing help/localization finding](blacklist-pr-audit-20260924.md)
remains outstanding. Passing integration tests does not complete that work.

## Evidence

Ignored evidence directory: `backups/pr834-current-integration-20260925T152746Z/`.
It contains detached `integration`/`baseline` checkouts, four Ant logs,
two focused JUnit logs, fresh class output, `compile-comparison.json`, independent
`boundary-review.md`, and the sanitized reviewer report `pr-comment.md` with
reproduction commands. Both validation checkouts remained source-clean.

The [upstream validation comment](https://github.com/yacy/yacy_search_server/pull/834#issuecomment-5835080395)
was posted at 2026-09-25T15:36:08Z and read back byte-for-byte equal to the
reviewed candidate. It contains only public revisions, repository-relative
commands and aggregate results, not private paths, fleet information or logs.
Publication receipt is also recorded in the completed work order
`20260925T152746Z-pr834-current-integration.md`.
