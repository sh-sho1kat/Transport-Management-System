# ADR 0001: Isolated replacement

Status: accepted by the user's implementation plan.

The source specification is retained verbatim. The later approved migration plan governs these deliberate differences:

1. Existing `/api/v1` and session/CSRF authentication remain in backend. Replacement contracts use `/api/v2`; JWT/refresh is introduced in Stage 1.
2. Replacement database is separate; no original migration is edited. Foundation V1 records application identity; business migrations start at V2, rather than copying the source specification's illustrative V1-users numbering.
3. Docker is optional later. Daily development uses IntelliJ and a private local PostgreSQL cluster.
4. Build only the current milestone. The source specification is a target, not a claim that all features exist.
5. Keep backend-next temporary; replace backend only after the cutover gates pass.

No live inventory may be writable in both services. Four target roles replace the current role model only at cutover. Historical records that cannot be truthfully represented remain in an archive.
