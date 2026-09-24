#!/usr/bin/env python3
"""Check maintained documentation links, excluding immutable reference/history documents."""
from pathlib import Path
import re
from urllib.parse import unquote
root=Path(__file__).resolve().parents[2]
files=[root/'README.md',root/'backend-next/README.md']
files += [root/'docs'/name for name in ['README.md','CURRENT_STATE.md','MIGRATION_MAP.md','API_COMPATIBILITY.md','PLAN.md','architecture.md','database.md','permissions.md','specification.md']]
files += list((root/'docs/migration').glob('*.md')) + [root/'docs/work/current-task.md',root/'docs/work/roadmap.md',root/'docs/work/handoff.md',root/'docs/decisions/0002-final-document-alignment.md',root/'docs/api/current-v1.md']
errors=[];links=0
for file in files:
    if not file.exists():errors.append(str(file)+': missing document');continue
    text=file.read_text()
    if '/api/v2' in text or 'next-v2.yaml' in text:errors.append(str(file)+': obsolete replacement API guidance')
    # Exclude fenced examples; source references and archived reports are not altered.
    text=re.sub(r'```.*?```','',text,flags=re.S)
    for target in re.findall(r'\[[^\]]*\]\(([^)]+)\)',text):
        target=target.strip('<>')
        if '://' in target or target.startswith(('#','mailto:')):continue
        target=unquote(target.split('#')[0])
        if not target:continue
        links+=1
        if not (file.parent/target).exists():errors.append(str(file.relative_to(root))+': broken link '+target)
if errors:raise SystemExit('\n'.join(errors))
print(f'Documentation checks passed: {len(files)} active documents, {links} local links; immutable references/history excluded.')
