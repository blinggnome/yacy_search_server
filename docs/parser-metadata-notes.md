# Parser Metadata Notes

## Empty JavaScript application shells

Some single-page applications return an initial HTML shell with empty metadata and no body content, then fetch the actual page title and content from JavaScript-managed APIs. For example, `https://documents.bethesda.net/en/privacy-policy` initially returns an empty title, empty social metadata, and an empty content section; the actual privacy policy is loaded later from Contentful.

The current HTML parser falls back to a readable title derived from the URL slug when no title, social metadata, or heading is available. This prevents completely empty titles, but it is not a complete solution: if the rendered application would show a 404, redirect, gated state, or different canonical content, the parser cannot know that from the initial shell alone.

Future work should revisit these pages with a better crawler-layer strategy, such as controlled JavaScript rendering, site-declared API discovery, or a generic way to detect empty app shells and mark them for richer follow-up fetches. Parser-level slug titles should be treated as a low-confidence fallback, not proof that the rendered page exists or contains useful content.

Zero-content documents are now rejected before indexing when the fetched page has no usable body text, no usable description, and no title beyond a URL-derived slug. This prevents empty JavaScript shells and soft-404 stubs from becoming searchable records. Pages that provide useful metadata, such as a real title plus description, remain indexable even when the visible body depends on JavaScript.

Parsed soft-error pages are also rejected when their title indicates a 404/not-found page and the description or body confirms missing-page content. This catches sites that return `200 OK` while serving an error page, without rejecting ordinary articles that merely discuss HTTP 404 errors.

Operators can also configure crawler content rejection rules from the Filter & Blacklists admin area. Soft rules are plain-text, case-insensitive substring matches against fetched source and parsed title, description, and body text. A matching document is rejected before indexing and any existing indexed record for that URL is removed.

Poison Pill rules are managed separately on the same page. They use the same plain-text matching behavior, but a match treats the host as compromised or broadly unwanted: the crawler rejects the page, adds the host to `url.poison_pill.black`, removes indexed records for that host, and records the removed-record count in Recent Crawler Rule Actions. Use Poison Pills for host-wide indicators such as domain hijack text or other signatures where keeping any content from that host is undesirable. Crawler actions that skip a page because of a rejection rule or write to a blacklist are also appended to `DATA/LOG/crawler-rule-actions.log` with the URL, matched rule, match location, and a short excerpt so overnight false positives can be audited after restart; routine metadata enrichment and "existing metadata is richer" skips stay out of the disk log. The on-disk audit log keeps the newest 10000 entries. Audit-log undo buttons remove the blacklist rule(s) written by false-positive automatic actions, but they do not restore already deleted index records; allowed content must be reacquired by future crawls. The crawler content rejection whitelist protects configured hosts and their subdomains from poison-pill host-wide purge and blacklist actions; it does not bypass soft rules, HTTP error cleanup, noindex cleanup, or other normal crawler decisions.

Parked-domain detections can be reviewed from the crawler monitor and manually purged by domain. The cleanup action targets indexed records for the root domain and discovered subdomains, then adds two YaCy-native blacklist entries to `url.domain_for_sale.black`: one for the root domain and one for subdomains, for example `example.com/.*` and `*.example.com/.*`. The cleanup status reports targeted host names separately from newly added, already-present, or failed blacklist rules. The Filter & Blacklists > Dead Domains page can enable automatic cleanup so detected dead domains are purged and blacklisted during crawl instead of waiting for the manual crawler-monitor button. Future work should revisit whether these can be safely collapsed into one rule without triggering YaCy blacklist escaping or matching problems. If parked-domain cleanup produces many thousands of rules, measure blacklist lookup cost and consider a more compact representation or a dedicated domain-ban list.

Crawler load failures caused by DNS unknown-host errors are treated as abandoned-host signals. The cleanup is intentionally exact-host only: if `subdomain.example.org` fails, only `subdomain.example.org/.*` is added to `url.domain_abandoned.black`, and indexed records for that host are removed. Do not collapse these failures to the registrable root domain, because an abandoned subdomain does not prove the parent domain is abandoned. For subdomain failures, the crawler first attempts to resolve the registrable root domain; if the root still resolves, the subdomain is not blacklisted and the ordinary crawl failure path continues. Malformed or suspicious hosts are discarded instead of blacklisted; for example, hosts whose registrable domain would itself start with `www.` are treated as bad URL data, not as real abandoned domains. This depends on resolver behavior: an unknown-host result is strong evidence that a host is gone, but transient DNS outages can still produce the same Java exception.

## YouTube metadata enrichment

Some YouTube pages return generic parser metadata such as `- YouTube` with little or no description, while the raw response can still contain useful structured fields. The parser enriches these records with YouTube oEmbed data when the fetched metadata is generic, appends the ` - YouTube` title suffix, captures the author when available, and drops the generic `video, sharing, camera, phone, free, upload` keyword set for YouTube video records.

On 2026-07-03, starting a crawl for `https://www.youtube.com/watch?v=3geZ5EVZg7E` exposed a parser failure where `Crawler_p.html` returned a generic `javax.servlet.ServletException` page. The underlying YaCy log showed `java.lang.StackOverflowError` in Java regex matching immediately after the YouTube response was cached. The cause was the parser's regex-based extraction of `attributedDescription.content` from the full raw YouTube source; on a large response without a simple match, the regex could recurse until the servlet request failed.

The fix replaced that regex with a bounded string scanner that locates the `attributedDescription` object, reads a JSON string value after the `content` field, and reuses JSON decoding only for that extracted value. Regression tests cover normal escaped description text and a large source without a matching `content` field. Verification used `ant compileTest`, `htmlParserTest`, a dev-instance restart, and an authenticated `Crawler_p.html` crawl POST for the failing YouTube URL; after the fix, the request returned `HTTP 200`, the crawl started, and the URL indexed without a fresh `StackOverflowError`.

## Authentication handoff pages

Some URLs return authentication protocol handoff pages instead of content. For example, SAML endpoints may return an auto-submit HTML form containing a hidden `SAMLRequest` and a `RelayState` target URL. The current parser marks these documents as `noindex,nofollow` so they should not become searchable content.

Future crawler work should revisit whether these pages should be rejected earlier in the crawl pipeline instead of being parsed and then marked non-indexable. Ideally, SAML/auth handoffs should be tracked only as blocked or rejected crawl attempts, not as content candidates that need later cleanup.

## Recrawl cleanup

When a recrawl gets a definitive bad HTTP response (`4xx` or `5xx`), YaCy now removes any previously indexed fulltext/webgraph document for that URL before writing the failure marker. Redirects and network-level failures without an HTTP status do not trigger removal.

When a recrawl successfully fetches a page but the parsed document declares `noindex`, the old indexed document is also removed before the failure is recorded. This covers parser-detected authentication handoff pages such as SAML request forms.

Crawl-profile scope filters are intentionally not treated as proof that an existing document is bad. For example, a media-excluding crawl or a temporary URL/content regex should not delete a valid record that was accepted by an earlier crawl.

## Future metadata enrichment

The parser now has fallbacks for pages with missing, malformed, or low-value titles and descriptions, and generated titles are capped to 180 characters. Those improvements apply when a URL is crawled or recrawled; they do not automatically rewrite older records already stored in the index.

Future work should add a maintenance plan to find existing records with missing, generic, malformed, or overlong titles/descriptions, recrawl or reparse them with the current metadata rules, and update the stored record only when the new metadata is clearly richer. The same pass should preserve good existing metadata, remove records that recrawl as definitive errors, and report how many records were enriched, unchanged, rejected, or deleted.
