#!/usr/bin/env python3
"""Find affected saved references in region chunks, playerdata and saved data.

Read-only preflight. level.dat is excluded because its registry snapshot contains
unused registrations; world loading is a separate required migration check.
"""
import gzip
import json
from pathlib import Path
import re
import struct
import sys
import zlib

root = Path(sys.argv[1])
pattern = re.compile(rb'(?:everycomp|supplementaries):[a-z0-9_./-]*(?:sappy_maple|hollow_ancient|hollow_blossom)[a-z0-9_./-]*')
hits = []
errors = []
chunks = 0

def inspect(data, source):
    found = sorted({match.decode() for match in pattern.findall(data)})
    if found:
        hits.append({'source': source, 'ids': found})

def decompress(data, kind):
    if kind == 1:
        return gzip.decompress(data)
    if kind == 2:
        return zlib.decompress(data)
    if kind == 3:
        return data
    raise ValueError('Unsupported chunk compression: ' + str(kind))

for path in sorted(root.rglob('*')):
    if not path.is_file():
        continue
    try:
        if path.suffix == '.mca':
            if path.stat().st_size == 0:
                continue
            with path.open('rb') as f:
                header = f.read(4096)
                if len(header) != 4096:
                    raise ValueError('Truncated region header')
                for index in range(1024):
                    offset = int.from_bytes(header[index * 4:index * 4 + 3], 'big') * 4096
                    if not offset:
                        continue
                    f.seek(offset)
                    length = struct.unpack('>I', f.read(4))[0]
                    kind = f.read(1)[0]
                    if kind & 128:
                        _, rx, rz = path.stem.split('.')
                        x, z = int(rx) * 32 + index % 32, int(rz) * 32 + index // 32
                        payload = (path.parent / f'c.{x}.{z}.mcc').read_bytes()
                    else:
                        payload = f.read(length - 1)
                    inspect(decompress(payload, kind & 127), f'{path.relative_to(root)}:chunk-{index}')
                    chunks += 1
        elif path.suffix == '.dat' and path.name != 'level.dat':
            data = path.read_bytes()
            if data[:2] == b'\x1f\x8b':
                data = gzip.decompress(data)
            inspect(data, str(path.relative_to(root)))
    except Exception as e:
        errors.append({'source': str(path.relative_to(root)), 'error': str(e)})

print(json.dumps({'chunks_checked': chunks, 'affected_references': hits, 'errors': errors}, indent=2))
raise SystemExit(1 if hits or errors else 0)
