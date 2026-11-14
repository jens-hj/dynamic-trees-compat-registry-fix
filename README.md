# Dynamic Trees Compat Registry Fix

A small NeoForge addon for Minecraft 1.21.1 and **Dynamic Trees: Universal Compat 2.3**.

Dynamic Trees Compat's cached startup registers wood finders from its broad tree-discovery results. Fresh startup instead scans mod resources. These paths can register different wood types, causing Every Compat and Supplementaries to generate blocks on a server that its clients do not recognize.

This addon makes the cached path perform the same resource scan as fresh startup. Generated-tree caching and the original mod's pack synchronization remain enabled. The tradeoff is one resource scan per launch.

## Installation

Install this addon alongside Dynamic Trees Compat 2.3 on **both client and server**, then fully restart both. Do not replace the original mod. Other DTC versions are deliberately rejected until tested.

Back up existing worlds before installation. If a world already contains furniture generated only by the old cached registration path, those entries may disappear with the corrected registry. Test a copy first; this addon does not migrate such blocks or items.

## Development

Use the Nix environment (Java 21 toolchain):

```sh
nix develop -c ./gradlew build testmodJar
```

`testmodJar` is a separate test-only auditor; never distribute it as the addon. It writes sorted block and item registry snapshots at load completion. Its default snapshot mode exits immediately; `-Dregistry.audit.mode=connect` keeps the client running and automatically connects to an isolated test server at `127.0.0.1:25579`.

Full Engineering tests require production launches: its Sinytra Connector build cannot resolve the clean Minecraft artifact in a Gradle development launch. `scripts/production-client.py` uses an existing Modrinth 1.21.1/NeoForge 21.1.266 runtime with an offline test identity, no account credentials, and an isolated game directory. Run it through `scripts/headless.py` inside `nix develop` to prevent desktop windows; it writes muted options before launch.

Compare fresh and cached server/client snapshots, then verify multiplayer joining and repeat startup. Preserve the generated tree pack between runs; remove only the generation cache when explicitly testing a fresh scan.

The test-only `-Dregistry.audit.cacheFixture=true` option makes an existing generation snapshot eligible even when temporary launcher paths change its input fingerprint. This exercises the real cached registration branch without changing the cached candidates or pack fingerprint. The production-client script accepts `AUDIT_CACHE_FIXTURE=true` for this scenario. Always confirm the cached-path log message; a second launch alone does not prove a cache hit.

Server mode `worldcheck` loads the world and stops gracefully after the server-started event. `scripts/audit-world.py` scans saved chunks, entities, playerdata, and saved data for the three affected wood variants; use this as a preflight alongside a world-loading test.

## Attribution

Independent compatibility addon for [Dynamic Trees: Universal Compat](https://modrinth.com/mod/dynamic-trees-universal-compat) by Gabou. No upstream code or assets are bundled. This addon's original code is MIT-licensed; upstream licensing is unchanged.
