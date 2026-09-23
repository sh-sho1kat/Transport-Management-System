# Migration tooling

`inventory.py` is a read-only source/contract inventory, not a database migration. Export/import tools are intentionally deferred until mappings and reconciliation rules are implemented. See docs/migration/data-mapping.md. Never run target migrations against the legacy database.
