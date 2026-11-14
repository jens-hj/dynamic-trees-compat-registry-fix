# Validation for 0.1.0

Tested on Minecraft 1.21.1, NeoForge 21.1.266, Java 21, and the Engineering 1.3.0 modpack with Dynamic Trees Compat 2.3. Tests use isolated production client/server launches, not the user's running game or hosted server.

## Registry regression

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

## Existing-world preflight

A read-only audit copy of the hosted world was scanned: 32,205 region chunk records, plus playerdata and saved-data files. No affected block/item references or scan errors were found. An isolated patched server loaded a copy of that world, reached the server-started event, and shut down successfully.

This is not a general migration guarantee for other worlds. Back up and inspect worlds that may already contain the removed compatibility variants. The scan excludes `level.dat` registry snapshots, which list unused entries too; actual world loading is checked separately.

## Packaging

The release contains only two addon classes, its Mixin configuration, NeoForge metadata, the original icon, and MIT license. It contains no upstream classes or assets and no test classes. Unsupported DTC versions are rejected by the exact `2.3` dependency constraint.

The build provides its own non-publishing `-PmodrinthDryRun` guard: Minotaur 2.10 does not implement that flag itself.
