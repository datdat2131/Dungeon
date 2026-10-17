# CursedRelics 0.1.0 — Paper 1.21.4

Prototype plugin for Java 21 and Paper 1.21.4. This is a source project, not a precompiled JAR.

## Features in this prototype
- `/dungeon join` creates a simple arena region and spawns The Hollow King.
- `/dungeon leave` exits the current run.
- Basic party invitations: `/dungeon party invite <player>` and `/dungeon party accept`.
- Boss defeat grants a prototype Cursed Loot item and opens a three-choice Relic Draft GUI.
- `/dungeon relics` lists relic IDs selected in the current run.
- `/cr admin reload` reloads `config.yml`.
- YAML files for basic dungeon and relic definitions.

## Build
Requires JDK 21 and Maven 3.9+ with internet access to resolve Paper API.

```bash
mvn clean package
```
The resulting jar will be `target/CursedRelics-0.1.0.jar`.

Copy that JAR into the server's `plugins/` directory and restart a Paper 1.21.4 server.

## Commands
- `/dungeon help`
- `/dungeon join`
- `/dungeon leave`
- `/dungeon party invite <player>`
- `/dungeon party accept`
- `/dungeon relics`
- `/cr admin reload`

## Important prototype limitations
- The arena is generated as a simple boxed test room in the world the leader is standing in, at a distant coordinate. Use a dedicated test world/server and ensure the coordinates around X=1000+ are clear.
- This MVP does not clone a real dungeon template or implement multi-stage rooms.
- Cursed item effects and relic effects are prototype descriptions; several are not yet wired into combat. The draft GUI, selection tracking and basic boss reward loop are implemented.
- The config `keep-inventory` setting is reserved for a future inventory snapshot implementation; the prototype does not yet snapshot/restore player inventories.
- Party invites are intentionally minimal; persistent parties, offline membership, shared loot voting, anti-exploit transaction recovery and crash-safe instance recovery are not implemented.
- Test on a backup server before use. This is a starter prototype, not production-ready software.
