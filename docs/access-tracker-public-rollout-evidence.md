## Production rollout validation (2026-09-20)

We have now tested this fix against existing production query histories, including
nodes actively exhibiting the reported Local Search Log failure.

| Check | Result |
| --- | --- |
| YaCy instances verified | 83 |
| Updated during this rollout | 82; one previously patched canary was already healthy and was not restarted |
| Confirmed history-page parser failures before the update | 27 instances returned authenticated HTTP 500 |
| Those same failing pages after the update | All 27 returned HTTP 200 |
| Local Search Log after rollout | All 83 instances passed authenticated checks |
| Other application checks | Access-tracker page 1, local search and Solr checks passed on all 83 |
| Existing query history preserved | Approximately 8.14 GB across the 82 updated instances, verified unchanged; the earlier canary was checked separately |

The failing endpoint was `AccessTracker_p.html?page=2`. Before/after checks used
authenticated requests to the actual YaCy servlet, rather than treating an
unauthenticated login page or a successful root-page response as proof of repair.
The existing query histories were not deleted, truncated or rewritten to clear
the error. Only the patch and optional matching source were installed, followed
by a YaCy service restart; no operating-system reboot, index cleanup, settings,
blacklist or dependency changes were part of this rollout.

No relevant parser or linkage errors were found in the retained post-start
application logs examined by the rollout verifier. This was a bounded check of
available rotated logs, not a claim about unlimited historical log coverage.

These results provide real-world confirmation beyond the synthetic regression
tests: the failing pages recovered while their original histories remained
intact. They describe post-deployment application checks, not an overnight soak
test or a fleet-wide external routing/DNS validation.

This summary intentionally contains only aggregate results. No node identifiers,
hostnames, IP addresses, local filesystem paths, credentials, runtime fingerprints,
raw HTTP bodies, query text or private report attachments are included.
