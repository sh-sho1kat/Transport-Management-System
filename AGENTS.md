# Development rules

- Read docs/README.md, docs/work/current-task.md and docs/work/handoff.md first. Implement only the current bounded task; update handoff with evidence and the next task.
- backend/ and frontend/ remain the running application. Replacement work belongs in backend-next/. Never change old migrations or scripts/local-db.py as part of replacement work.
- Follow docs/architecture.md: feature-owned MVC, DTO responses, concrete services, module writes via explicit APIs, shared independent, workflow coordinates cross-module transactions.
- No fake feature endpoints or hundreds of empty classes. Each feature includes migration, contract and tests. Lock inventory deterministically with PostgreSQL concurrency tests from the first booking write.
- Never sell the same live departure from both databases. Never reset/drop a developer database or fabricate historical passenger/payment data. Keep secrets and generated output out of Git.
- Local DB: `python3 scripts/next-db.py start`. Replacement checks: `cd backend-next && ./mvnw clean verify`; from root: `python3 scripts/checks/next-contract.py` and `python3 scripts/migration/inventory.py`.
- Existing checks remain documented in README.md. No Docker prerequisite. Production release and cutover gates are in docs/migration/cutover.md.
