#!/usr/bin/env python3
"""Verify the three production runs described in docs/VALIDATION.md.

Usage: nix develop -c python scripts/check-pack-sync.py build/audit
Run against a server pack containing IDs absent from the initial client pack.
Logs and the reconnect result must come from the same regression run.
"""
import json
from pathlib import Path
import sys

root = Path(sys.argv[1])
baseline = (root / 'baseline-client.log').read_text()
server = (root / 'fixed-server.log').read_text()
sync = (root / 'sync-client.log').read_text()
reconnect = (root / 'reconnect-client.log').read_text()
result = json.loads((root / 'reconnect-result.json').read_text())

def require(condition, message):
    if not condition:
        raise SystemExit('FAIL: ' + message)

require('registries with unknown keys' in baseline, 'old fix must reproduce registry rejection')
require('dynamictreescompat:minecraft/oak_branch' in baseline, 'fixture must exercise the reported missing tree IDs')
require('moved generated-pack synchronization before registry validation' in server, 'new injection must execute')
require("Dynamic Trees Compat synchronized the server's generated pack. Restart Minecraft, then reconnect." in sync,
        'first fixed connection must stage the pack and request a restart')
require('registries with unknown keys' not in sync, 'pack transfer must precede registry rejection')
require(result.get('passed') is True, 'restarted client must join and remain connected for 100 ticks')
require('registries with unknown keys' not in reconnect, 'restarted client must have matching tree registries')
require('Restart Minecraft, then reconnect.' not in reconnect, 'matching pack must not cause a restart loop')
print('PASS: reproduced missing tree IDs; transferred pack before registry validation; restarted client joined.')
