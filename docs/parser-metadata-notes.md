# Parser Metadata Notes

## Empty JavaScript application shells

Some single-page applications return an initial HTML shell with empty metadata and no body content, then fetch the actual page title and content from JavaScript-managed APIs. For example, `https://documents.bethesda.net/en/privacy-policy` initially returns an empty title, empty social metadata, and an empty content section; the actual privacy policy is loaded later from Contentful.

The current HTML parser falls back to a readable title derived from the URL slug when no title, social metadata, or heading is available. This prevents completely empty titles, but it is not a complete solution: if the rendered application would show a 404, redirect, gated state, or different canonical content, the parser cannot know that from the initial shell alone.

Future work should revisit these pages with a better crawler-layer strategy, such as controlled JavaScript rendering, site-declared API discovery, or a generic way to detect empty app shells and mark them for richer follow-up fetches. Parser-level slug titles should be treated as a low-confidence fallback, not proof that the rendered page exists or contains useful content.

Zero-content documents are now rejected before indexing when the fetched page has no usable body text, no usable description, and no title beyond a URL-derived slug. This prevents empty JavaScript shells and soft-404 stubs from becoming searchable records. Pages that provide useful metadata, such as a real title plus description, remain indexable even when the visible body depends on JavaScript.

Parsed soft-error pages are also rejected when their title indicates a 404/not-found page and the description or body confirms missing-page content. This catches sites that return `200 OK` while serving an error page, without rejecting ordinary articles that merely discuss HTTP 404 errors.

Operators can also configure crawler content rejection rules from the Filter & Blacklists admin area. These rules are plain-text, case-insensitive substring matches against parsed title, description, and body text. A matching document is rejected before indexing and any existing indexed record for that URL is removed.

Parked-domain detections can be reviewed from the crawler monitor and manually purged by domain. The cleanup action targets indexed records for the root domain and discovered subdomains, then adds two YaCy-native blacklist entries to `url.domain_for_sale.black`: one for the root domain and one for subdomains, for example `example.com/.*` and `*.example.com/.*`. The cleanup status reports targeted host names separately from newly added, already-present, or failed blacklist rules. The Filter & Blacklists > Dead Domains page can enable automatic cleanup so detected dead domains are purged and blacklisted during crawl instead of waiting for the manual crawler-monitor button. Future work should revisit whether these can be safely collapsed into one rule without triggering YaCy blacklist escaping or matching problems. If parked-domain cleanup produces many thousands of rules, measure blacklist lookup cost and consider a more compact representation or a dedicated domain-ban list.

## Authentication handoff pages

Some URLs return authentication protocol handoff pages instead of content. For example, SAML endpoints may return an auto-submit HTML form containing a hidden `SAMLRequest` and a `RelayState` target URL. The current parser marks these documents as `noindex,nofollow` so they should not become searchable content.

Future crawler work should revisit whether these pages should be rejected earlier in the crawl pipeline instead of being parsed and then marked non-indexable. Ideally, SAML/auth handoffs should be tracked only as blocked or rejected crawl attempts, not as content candidates that need later cleanup.

## Recrawl cleanup

When a recrawl gets a definitive bad HTTP response (`4xx` or `5xx`), YaCy now removes any previously indexed fulltext/webgraph document for that URL before writing the failure marker. Redirects and network-level failures without an HTTP status do not trigger removal.

When a recrawl successfully fetches a page but the parsed document declares `noindex`, the old indexed document is also removed before the failure is recorded. This covers parser-detected authentication handoff pages such as SAML request forms.

Crawl-profile scope filters are intentionally not treated as proof that an existing document is bad. For example, a media-excluding crawl or a temporary URL/content regex should not delete a valid record that was accepted by an earlier crawl.
