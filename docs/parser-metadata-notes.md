# Parser Metadata Notes

## Empty JavaScript application shells

Some single-page applications return an initial HTML shell with empty metadata and no body content, then fetch the actual page title and content from JavaScript-managed APIs. For example, `https://documents.bethesda.net/en/privacy-policy` initially returns an empty title, empty social metadata, and an empty content section; the actual privacy policy is loaded later from Contentful.

The current HTML parser falls back to a readable title derived from the URL slug when no title, social metadata, or heading is available. This prevents completely empty titles, but it is not a complete solution: if the rendered application would show a 404, redirect, gated state, or different canonical content, the parser cannot know that from the initial shell alone.

Future work should revisit these pages with a better crawler-layer strategy, such as controlled JavaScript rendering, site-declared API discovery, or a generic way to detect empty app shells and mark them for richer follow-up fetches. Parser-level slug titles should be treated as a low-confidence fallback, not proof that the rendered page exists or contains useful content.

## Authentication handoff pages

Some URLs return authentication protocol handoff pages instead of content. For example, SAML endpoints may return an auto-submit HTML form containing a hidden `SAMLRequest` and a `RelayState` target URL. The current parser marks these documents as `noindex,nofollow` so they should not become searchable content.

Future crawler work should revisit whether these pages should be rejected earlier in the crawl pipeline instead of being parsed and then marked non-indexable. Ideally, SAML/auth handoffs should be tracked only as blocked or rejected crawl attempts, not as content candidates that need later cleanup.
