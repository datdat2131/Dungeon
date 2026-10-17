# CursedRelics 0.1.0 — Flat GitHub project

Paper 1.21.4 plugin prototype; Java 21.

## This ZIP uses the same flat layout as the example

All `.java` files, `pom.xml`, `plugin.yml`, `config.yml`, `dungeons.yml` and `relics.yml` are in the repository root. Java package declarations remain intact; Maven compiles these root-level source files using the configured `sourceDirectory`.

## Upload to GitHub

1. Create a repository.
2. Extract this ZIP.
3. Upload the CONTENTS of the extracted folder to the repository root. Do not upload an extra parent folder.
4. Ensure `.github/workflows/build.yml` is included (GitHub's upload interface may hide dot-folders; if so, create the workflow in GitHub or upload it using Git).
5. Commit to `main` or `master`, then open Actions.

The workflow checks for required root files, runs `mvn clean package` on Java 21, then uploads `CursedRelics-Paper-1.21.4` as an artifact.

## Build locally

Requires JDK 21 and Maven 3.9+:

```bash
mvn clean package
```

Output: `target/CursedRelics-0.1.0.jar`.

## Commands

- `/dungeon help`
- `/dungeon join`
- `/dungeon leave`
- `/dungeon party invite <player>`
- `/dungeon party accept`
- `/dungeon relics`
- `/cr admin reload`

## Prototype limitations

This is not a production-ready dungeon system. The arena is a simple generated test room; it does not clone templates or implement multi-stage rooms. Several relic/curse effects are descriptions only, inventory snapshots and crash-safe recovery are not implemented, and party management is minimal. Test on a backup server.

The workflow and file layout are prepared, but a successful compilation cannot be claimed until GitHub Actions actually completes the Maven build.
