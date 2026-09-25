# Blacklist PR 834 Audit

Reviewed 2026-09-24 in response to concerns about continuity during submission.
This is a review record, not an upstream approval or a guarantee of no defects.

## Finding: Missing Help And Localization

Medium priority, contribution completeness: [PR 834](https://github.com/yacy/yacy_search_server/pull/834)
does not update the matching help and localization files. Upstream's
`AGENTS.md` at `de973ca44`, lines 5-10, requires these with user-visible HTML
and servlet changes.

- `help/BlacklistTest_p.md:48` still describes only the old POST test form and
  has an empty parameter table. Document GET testing, matching rules and source
  attribution, native editing, two-stage deletion, access/token/consent/revision
  requirements, blacklist reload and cache side effects.
- `locales/source-master.lng.xlf:751` still lists the old page strings only.
  The added confirmation, warnings, labels and controls need the corresponding
  localization changes following upstream's conventions.

No runtime correctness defect was identified in the reviewed code. The omission
is real, but there is no evidence establishing what caused it. Do not attribute
it to an unverified platform/backend malfunction.

## Published Integrity

- Published head: `f1eb2d107877abba585892bfc2fe2f926cefccae`, one commit,
  eight files, +912/-29. All eight blobs match the user-accepted feature at
  `e5632532a`. No unrelated dev-node features were included.
- Scope: template, local JavaScript, blacklist-test handler, additive matcher
  diagnostics, source-file diagnostics/deletion helper, and three test files.
- No new external package, CDN, font or image dependency. Pencil/trash icons use
  the existing bundled Bootstrap Glyphicons. No suspect secrets or private
  operational content found in the audited diff, body and commit metadata.
- PR description matches retained submission text and accurately distinguishes
  earlier dev/browser verification from upstream integration testing. Its
  limitations concerning file provenance, bare-host editing and concurrent
  writers are stated. The help/localization omission prevents calling the
  contribution fully complete under upstream instructions.
- PR was open, ready, mergeable and without reviews/comments at the audit.
  Both published build checks were successful, not evidence of maintainer
  approval or full-suite success.

## Fresh Verification

Reused clean isolated checkouts, without changing the main branch or PR:

- `3bc444475`: original upstream-plus-feature integration.
- `de973ca44`: original pristine upstream baseline.

Application `ant compile` passed. Recompiled all three changed Java classes and
four focused test classes using `javac --release 17`, then put the fresh output
first on the JUnit classpath: all 29 tests passed. Covered matching-engine
parity, filenames/purposes, source spellings, exact deletion, revision guards,
unsafe paths/symlinks, template escaping, confirmation and POST/token checks.

`node --check htroot/js/BlacklistTest.js` passed. Six additional stubbed-DOM
event checks passed: ordinary focus is inert; edit requires leaving and
returning; return navigates once to the GET retest; either edit form arms it;
empty retest does not navigate. This is not a fresh browser end-to-end test.

Full `ant compileTest` still fails. Fresh runs on both baseline and integration
produce the same 89 normalized error headers, with no patch-only diagnostic.
Failures concern missing Solr test-classpath classes and the stale
`HostBalancerTest` constructor. No unrelated test-suite repairs were attempted.
Initial sandbox compilation failed because the dependency cache was read-only;
authorized cache access resolved that environmental obstacle.

## Current-Upstream Limit

Direct GitHub ref reads found master had advanced to
`b50b76bd552a851f737b98683f20d8b861e137ae` and the current PR merge was
`5cae18c88c8e618dca7d59d18f1c13fe2c8c9d97`. The four intervening commits touch
`ServletResource`, `FileUtils` and their tests, not the eight feature files.
The current synthetic merge retains the same feature blobs.

Both green checks actually tested merge `8998d0b5580a2ae370b8c16ce4a4c23d74e7f1cb`,
whose tree equals the freshly tested local integration. Neither those checks
nor this audit's local tests validate the entire newest merged tree. PR metadata
alone did not establish current master; verify refs and CI checkout commits.

## Evidence And Next Step

Ignored evidence: `backups/blacklist-pr-audit-20260924T063351Z/` contains
`publication-audit.md`, application and baseline/integration compile logs,
`focused-tests.log`, `compile-comparison.json`, fresh classes and
`check-editor-return.cjs`. Original live testing is documented in
[blacklist-test notes](blacklist-test-notes.md) and the completed submission
work order; it was not repeated on a live node during this read-only audit.

Recommended follow-up: complete help/localization in a focused PR update and
rerun focused/application validation against the then-current upstream merge.
No source, PR, live blacklist, service, Git commit or deployment was changed by
this review. Existing unrelated local changes were preserved. Findings and the
CI/ref-verification lesson were saved to Vestige.
