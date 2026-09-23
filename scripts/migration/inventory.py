#!/usr/bin/env python3
"""Read-only local migration/contract inventory. No database connections."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[2]
contract=json.loads((root/'docs/openapi.json').read_text())
print(json.dumps({"legacyMigrationFiles":sorted(p.name for p in (root/'backend/src/main/resources/db/migration').glob('*.sql')), "nextMigrationFiles":sorted(p.name for p in (root/'backend-next/src/main/resources/db/migration').glob('*.sql')), "legacyApiPaths":len(contract['paths']), "importImplemented":False},indent=2))
