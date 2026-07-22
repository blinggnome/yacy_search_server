# Remote Crawl Notes

The remote crawl loader pulls URLs advertised by other peers with a positive
`RCount`. The loader now treats those advertisements as hints rather than proof
that a peer still has work available.

Current behavior:

- Provider hashes are shuffled when the candidate list is rebuilt, avoiding the
  old deterministic end-of-list selection order.
- Providers that fail or return an empty feed are put in a short in-memory
  cooldown. The default is 300 seconds and can be overridden with
  `remoteCrawlLoader.providerCooldownMillis`.
- Each loader run tries only a bounded number of providers before yielding back
  to the scheduler. The default is 5 attempts and can be overridden with
  `remoteCrawlLoader.maxProviderAttempts`.
- Remote-crawl endpoint failures and empty feeds do not alter the peer's active
  seed state or advertised `RCount`. A failed handoff is not proof that the peer
  itself is offline: normal hello and crawl-receipt traffic can still work while
  `/yacy/urls.xml?call=remotecrawl` returns a bad or empty response.
- Failed and empty handoffs are handled by the loader cooldown map instead of
  by demoting the peer or zeroing its advertised remote URL count.
- Remote intake is allowed while the local crawl queue is small. The default
  deferral threshold is 20 local jobs and can be overridden with
  `remoteCrawlLoader.localQueueLimit`.
- The remote-triggered queue is capped at 200 entries, and each provider request
  asks only for the remaining space up to that cap.
- `RemoteCrawl_p.html` shows loader status, queue pressure, provider cooldown
  count, maximum attempts per run, successful fetch count, URL intake count,
  rejected URL count, empty feed count, and failed feed count.

This does not change the provider endpoint semantics: `/yacy/urls.xml` still
pops remote-crawl URLs from the provider's global queue. A peer may advertise
remote work and then consume or move that work locally before another peer asks
for it, so empty feeds are expected on a transient network.
