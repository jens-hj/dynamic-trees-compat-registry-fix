#!/usr/bin/env python3
"""Compare actual registry snapshots; nonzero status means the registries differ."""
import json
from pathlib import Path
import sys

left = json.loads(Path(sys.argv[1]).read_text())
right = json.loads(Path(sys.argv[2]).read_text())
result = {}
for registry in ['blocks', 'items']:
    a, b = set(left[registry]), set(right[registry])
    result[registry] = {'left_count': len(a), 'right_count': len(b),
                        'left_only': sorted(a - b), 'right_only': sorted(b - a)}
print(json.dumps(result, indent=2))
raise SystemExit(any(entry['left_only'] or entry['right_only'] for entry in result.values()))
