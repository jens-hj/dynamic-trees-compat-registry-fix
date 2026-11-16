# Changelog

## 0.1.1

- Fix joining servers whose generated tree pack contains branches, seeds, or saplings missing from the client's older pack. Pack synchronization now runs before registry validation.
- When the server pack changes, the first connection downloads it and asks you to restart Minecraft. After a full restart, reconnect to use the updated pack.

## 0.1.0

- Fix Dynamic Trees Compat 2.3 generating different wood compatibility blocks on cached and fresh startup, which can prevent joining multiplayer servers.
- Preserve generated tree caches and the original mod's pack synchronization.
