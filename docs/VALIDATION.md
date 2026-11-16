# Validation

## 0.1.1: stale generated-pack recovery

Tested with isolated production Minecraft 1.21.1 / NeoForge 21.1.266 launches, Java 21, and Engineering fixtures. The server binds only to `127.0.0.1:25579`; the client uses an offline test identity, a separate game directory, Xvfb, and muted audio. No live player connection or world modification is involved.

The server fixture uses the hosted server's generated tree definitions and pack fingerprint `4a65e4d40fda1239e1adf177`. The initial client uses its older authoritative pack `b2c05e297728e8d7cf61d0ac`, missing oak, fancy oak, and the two enchanted Terralith mushroom families reported in the incident.

1. **Baseline:** 0.1.0 on both sides reproduces the unknown-registry-keys disconnect, including `dynamictreescompat:minecraft/oak_branch`.
2. **Transfer:** upgrade only the isolated server to 0.1.1. The still-0.1.0 client receives the new authoritative pack, stages it for restart, and shows DTC's explicit restart message instead of registry rejection.
3. **Reconnect:** upgrade the isolated client to 0.1.1 and fully relaunch it. The staged pack is activated with the server's fingerprint. Every registered ID matches: **15,865 blocks and 18,852 items**, with no client-only or server-only entries.

Use `scripts/check-pack-sync.py build/audit` to check baseline, transfer, and reconnect logs plus the saved `reconnect-result.json`. The reconnect auditor requires entering the world and staying connected for 100 ticks. It also checks that the restart prompt does not repeat. Use `scripts/compare-registries.py` on the server/client snapshots to compare all IDs.

The fix moves the existing DTC task immediately before configuration starts. It neither duplicates the task nor changes its payloads, transfer validation, acknowledgement, or restart behavior; every other queued task retains its relative order.

Build and the non-publishing Modrinth preview pass. The production jar contains three addon classes and no test classes or upstream code/assets.

## 0.1.0

Tested on Minecraft 1.21.1, NeoForge 21.1.266, Java 21, and the Engineering 1.3.0 modpack with Dynamic Trees Compat 2.3. Tests use isolated production client/server launches, not the user's running game or hosted server.

### Registry regression

| Scenario | Blocks | Items |
| --- | ---: | ---: |
| Original mod, fresh client/server | 15,853 | 18,850 |
| Original mod, cached server | 15,934 | 18,934 |
| Fixed client, fresh | 15,853 | 18,850 |
| Fixed client, cached | 15,853 | 18,850 |
| Fixed server, fresh | 15,853 | 18,850 |
| Fixed server, cached | 15,853 | 18,850 |

Comparisons check every registered block and item ID, not just counts. All fixed snapshots match exactly. The 81 blocks and 84 items added by the original cached path are all Every Compat/Supplementaries variants of `sappy_maple`, `hollow_ancient`, and `hollow_blossom`.

The cached scenarios use a separate test-only fixture to keep an existing snapshot eligible despite launcher-dependent input fingerprints. It changes only the input-fingerprint eligibility check: candidate data, pack fingerprint, the original cache loader, and registration code remain in use. Logs confirm cached loading; patched logs additionally confirm the replacement resource scan. Neither the fixture nor the auditor is packaged in the released addon.

### Existing-world preflight

A read-only audit copy of the hosted world was scanned: 32,205 region chunk records, plus playerdata and saved-data files. No affected block/item references or scan errors were found. An isolated patched server loaded a copy of that world, reached the server-started event, and shut down successfully.

This is not a general migration guarantee for other worlds. Back up and inspect worlds that may already contain the removed compatibility variants. The scan excludes `level.dat` registry snapshots, which list unused entries too; actual world loading is checked separately.

### Multiplayer

A patched cached client joined a patched fresh-cache dedicated server running the hosted-world copy. It completed the original DTC configuration handshake, entered the world, remained connected for 100 client ticks, and exited normally. The test used an offline test identity and a loopback-only server; no live hosting account or player session was used.

### Packaging

The release contains only two addon classes, its Mixin configuration, NeoForge metadata, the original icon, and MIT license. It contains no upstream classes or assets and no test classes. Unsupported DTC versions are rejected by the exact `2.3` dependency constraint.

The build provides its own non-publishing `-PmodrinthDryRun` guard: Minotaur 2.10 does not implement that flag itself.
