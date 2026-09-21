# Work Order: Upstream Crawl Start Button

- ID: `20260921T230257Z-crawl-start-button-pr`
- Created: `2026-09-21T23:02:57Z`
- Last Updated: `2026-09-21T23:19:00Z`
- Owner: YaCy dev builder
- Status: closed
- Outcome: succeeded

## Request And Definition Of Done

Submit only the already-tested expert crawl-start button upstream. Confirm the
current upstream comparison, identical form submission from both buttons and
reasonable layout, then publish a documented PR. All other catalog features
remain outside this contribution pending further testing.

## Authorized Scope And Boundaries

- Read source, docs, Git and GitHub metadata; prepare isolated upstream-based
  branch/worktree, focused commit, push to user fork and open PR to YaCy.
- Update main-fork catalog, changelog and work order separately from PR code.
- No deployments, real crawl submissions, fleet operations, sibling Git changes
  or unrelated source modifications. Preserve the main checkout's dirty paths.
- Existing local button commit: `5e4c8ca1d`; main branch starts at `6d659529c`.

## Checklist

- [x] [verified] Inspect original one-line template patch and historical testing.
- [x] [verified] Check current upstream, contribution template, auth and duplicates.
- [x] [verified] Prepare isolated branch and validate buttons/layout safely.
- [x] [verified] Publish narrow PR and verify public file/commit/body scope.
- [x] [verified] Update documentation and archive the verified record.

## Current State And Next Action

- Current state: main branch `yacy-space-abuse-message`; dirty AGENTS.md, two
  reports/handoffs and two dependency JARs predate task and are excluded.
- Next Action: await maintainer review/CI on PR 832. Other unsubmitted catalog
  features await further testing and separate user approval to publish.
- Resume check: inspect worktrees, branch and PR state before any repeat push
  or PR creation. Never submit a real crawl merely to verify a button.

## Runbooks, Commands And Evidence

- `docs/dev-node-improvement-catalog.md` U1;
  `git show 5e4c8ca1d -- htroot/CrawlStartExpert.html`.
- `docs/dev-node-change-log.md`, work-order lifecycle, local Playwright skill.
- Evidence/backups: `backups/crawl-start-button-pr-20260921/`; main-template and
  documentation backups made. Synthetic renderer uses YaCy TemplateEngine;
  fixture serves upstream assets on loopback and rejects all crawl POSTs.
- CLI artifact folder `.playwright-cli/` is task-generated; move into the ignored
  evidence directory at closeout. No real query/node credentials were used.
- Browser verification passed with actual template scripts and native submit
  events: 22 identical fields from top/bottom buttons at 1440, 1024 and 390 px,
  including two synthetic URLs, depth 3 and crawlingstart=1. No overlap.
- Before/after scroll widths: 1440/1440, 1024/1024, 710/710 respectively. Existing
  mobile overflow is unchanged; generated/custom env/style.css is absent from
  both fixtures (repository assets otherwise loaded). No claim of mobile redesign.
- Reviewed desktop and mobile screenshots; local fixture rejects POSTs and
  browser routing blocks non-loopback requests. No real crawls or live-node probe.
- Public body: ignored `backups/crawl-start-button-pr-20260921/pr-body.md`.
  Contains only purpose, one-line scope and synthetic verification; no private
  identities, paths, logs, credentials or unrelated features.
- Temporary test browser and loopback server have been stopped. CLI artifacts
  moved into ignored backups; no test services left running.
- Git checkpoints: original button `5e4c8ca1d`, isolated PR `fdfc741d3`;
  local documentation subject `Record crawl-start button upstream PR`.
  No remote runtime touched.
- Public PR https://github.com/yacy/yacy_search_server/pull/832 is open and
  non-draft. GitHub file list/diff and sole commit verified; body exactly matches
  reviewed local body. Initial `build` and `build-and-release` checks in progress.

## Decisions And Attempts

| UTC time | Decision or attempt | Observed result / reason | Next step |
| --- | --- | --- | --- |
| 2026-09-21T23:02:57Z | Isolate from upstream, not current feature branch | User explicitly wants only this small feature published | Compare original patch with current upstream |
| 2026-09-21 | Graph text lookup for template literals returned no results | HTML template inspected directly as known non-code template | Check form fields and CSS |
| 2026-09-21 | Git metadata/helper validation | Current upstream `de973ca4444912ecfe8682dedc9e14842f8a4d57`; no duplicate PR or template; CONTRIBUTING read; token lacks workflow scope | Use previously validated shared-ancestor publication approach without resetting fork |
| 2026-09-21 | Two isolated worktrees prepared | PR `fdfc741d3` on shared ancestor `94e8ac3b5`; upstream validation `faf16b96a` on current upstream | Merge tree equals validation tree `994f5af4f59b0bf10e515aba84a71172fc5a0764`; exactly one template line added |
| 2026-09-21 | Playwright wrapper attempted registry refresh | Restricted DNS prevented npx download; existing cached CLI is available | Use cached CLI; no dependency installation needed |
| 2026-09-21 | Browser route setup | CLI requires a function, and URL construction in route callback stalled navigation | Replaced with simple loopback URL-prefix routing; fixture now loads successfully |
| 2026-09-21 | Published isolated PR, then verified GitHub scope/body | PR 832 contains only one added template line and one commit; no unrelated or private data | Marked ready for review; CI still in progress at first check |

## Delegation

Git helper `01a0c636-4a3a-7b83-a08e-826574dcb7ac`: discovery and isolated Git prep
completed. Worktrees under task backup folder: `pr-worktree` (branch
`fix/crawl-start-button`) and `upstream-validation` (detached tested integration).
Git helper completed branch push, draft creation, exact public diff/body checks
and ready-for-review transition for PR 832. Only the authorized branch was
pushed. Main checkout unchanged by helper. Lead owns fixture/testing and docs.
Codebase output trial: cache full graph results before labeled filtering;
preserve source/errors and use normal output if caching unavailable.

## Closeout

- Closed: `2026-09-21T23:19:00Z`
- Outcome: succeeded
- Verified results: ready PR 832, exact single-file/line/commit scope, equivalent
  current-upstream integration, same form submissions and unchanged overflow at
  three viewports. Fixture services stopped; pre-existing dirty paths preserved.
- Limitations: browser fixture is not a new live crawl test; custom/generated
  skin absent; existing mobile overflow unchanged; CI still in progress at first
  read. No claim of upstream acceptance or merge.
- Follow-up / successor: maintainer review/CI for PR 832; user chooses later
  feature tests. Do not publish other catalog items merely from their ranking.
- Reusable knowledge saved: catalog and central changelog; no managed-memory
  write requested. This record contains no private fleet identities or secrets.
