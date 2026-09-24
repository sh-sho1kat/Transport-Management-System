#!/usr/bin/env python3
"""Increment 0 contract guard; expand alongside implemented API stages."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[2]
contract=json.loads((root/'docs/api/next.yaml').read_text()) # JSON is a valid YAML subset.
assert contract['openapi']=='3.0.3'
assert set(contract['paths'])=={'/actuator/health'}, 'Update contract checks and endpoint tests when implementing new endpoints'
assert set(contract['paths']['/actuator/health'])=={'get'}
assert set(contract['paths']['/actuator/health']['get']['responses'])=={'200','503'}
assert set(contract['components']['schemas']['ApiErrorResponse']['required'])=={'code','message','timestamp','status','path','traceId'}
print('Replacement Increment 0 contract structure passed; run Maven tests for HTTP behavior.')
