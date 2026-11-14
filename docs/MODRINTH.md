# Dynamic Trees Compat Registry Fix

Fixes a Dynamic Trees: Universal Compat **2.3** startup inconsistency that can prevent joining multiplayer servers with errors about unknown blocks or items.

The original mod can detect different wood types depending on whether its cache is used. Every Compat and Supplementaries may then generate different furniture and signs on the client and server. This addon makes both startup paths use the same wood-resource scan.

### Requirements

- Minecraft **1.21.1**, NeoForge **21.1.266 or newer within 1.21.1**.
- [Dynamic Trees: Universal Compat **2.3**](https://modrinth.com/mod/dynamic-trees-universal-compat) and its dependencies.
- Install this addon on **both client and server**, alongside the original mod, then restart both.

Generated-tree caching and the original mod's server pack synchronization remain enabled. Startup performs an extra resource scan when it would otherwise reuse cached wood-finder inputs.

Back up existing worlds and test a copy before installation: furniture or items generated only by the inconsistent cached path may no longer exist. This addon does not migrate existing blocks or fix unrelated mod/configuration mismatches.

Independent addon by jens-hj. Dynamic Trees: Universal Compat is by Gabou; no original mod code or assets are redistributed.
