#!/usr/bin/env python3
"""Launch an isolated offline test client using an existing Modrinth runtime."""
import json
import os
from pathlib import Path
import subprocess
import sys

meta = Path.home() / '.local/share/ModrinthApp/meta'
version = '1.21.1-21.1.266'
data = json.loads((meta / 'versions' / version / (version + '.json')).read_text())
directory = Path(sys.argv[1]).resolve()
mode = sys.argv[2] if len(sys.argv) > 2 else 'snapshot'

def allowed(rules):
    result = not rules
    for rule in rules:
        if rule.get('features'):
            continue
        platform = rule.get('os', {})
        if platform.get('name', 'linux') != 'linux' or platform.get('arch', 'x86_64') != 'x86_64':
            continue
        result = rule['action'] == 'allow'
    return result

classpath = []
for lib in data['libraries']:
    if not allowed(lib.get('rules', [])) or not lib.get('include_in_classpath', True):
        continue
    artifact = lib.get('downloads', {}).get('artifact')
    if artifact:
        path = meta / 'libraries' / artifact['path']
        if not path.exists():
            raise SystemExit('Missing runtime library: ' + str(path))
        classpath.append(str(path))
classpath.append(str(meta / 'versions' / version / (version + '.jar')))
variables = {
    'auth_player_name': 'RegistryAudit', 'version_name': version,
    'game_directory': str(directory), 'assets_root': str(meta / 'assets'),
    'assets_index_name': data['assets'], 'auth_uuid': '00000000000000000000000000000001',
    'auth_access_token': '0', 'clientid': '0', 'auth_xuid': '0', 'user_type': 'legacy',
    'version_type': 'release', 'natives_directory': str(meta / 'natives' / version),
    'launcher_name': 'registry-audit', 'launcher_version': '1',
    'classpath': os.pathsep.join(classpath), 'classpath_separator': os.pathsep,
    'library_directory': str(meta / 'libraries'),
}

def arguments(kind):
    result = []
    for value in data['arguments'][kind]:
        if isinstance(value, dict):
            if not allowed(value.get('rules', [])):
                continue
            value = value['value']
        for arg in value if isinstance(value, list) else [value]:
            for name, replacement in variables.items():
                arg = arg.replace('${' + name + '}', replacement)
            result.append(arg)
    return result

# All test launches are muted before the process starts.
(directory / 'options.txt').write_text(
    'soundCategory_master:0.0\nsoundCategory_music:0.0\nfullscreen:false\n'
    'renderDistance:2\nsimulationDistance:2\nonboardAccessibility:false\n'
    'narrator:0\npauseOnLostFocus:false\ntutorialStep:none\n')
command = [os.environ['JDK21'] + '/bin/java', '-Xmx6G',
           '-Dregistry.audit.cacheFixture=' + os.environ.get('AUDIT_CACHE_FIXTURE', 'false'),
           '-Dregistry.audit.mode=' + mode, '-Dregistry.audit.server=127.0.0.1:25579',
           *arguments('jvm'), data['mainClass'], *arguments('game')]
raise SystemExit(subprocess.call(command, cwd=directory))
