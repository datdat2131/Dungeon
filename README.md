# CursedRelics 0.1.0 — Paper 1.21.4

Plugin prototype for Paper 1.21.4, built with Java 21 and Maven.

## Build on GitHub Actions

1. Create a GitHub repository.
2. Upload **the contents of this ZIP** to the repository root. `pom.xml` must appear at the same level as `src/` and `.github/`; do not leave everything nested inside another `CursedRelics-0.1.0/` directory.
3. Commit/push the files. The workflow in `.github/workflows/build.yml` runs automatically on push, and can also be started from **Actions → Build CursedRelics → Run workflow**.
4. Open the completed workflow run and download the `CursedRelics-Paper-1.21.4` artifact. It contains `CursedRelics-0.1.0.jar`.

If the build fails, open the failed job and read the **first actual error** in `Compile and package` (or earlier). A red workflow by itself does not tell us whether the problem is repository layout, dependency resolution, or Java compilation. Share that log to diagnose it.

## Build locally

Requirements: JDK 21 and Maven. From the directory containing `pom.xml`, run:

```bash
mvn --batch-mode clean package
```

The JAR is written to `target/CursedRelics-0.1.0.jar`.

## Commands

- `/dungeon join` — create an arena and start the boss encounter
- `/dungeon leave` — leave the instance
- `/dungeon party invite <player>`
- `/dungeon party accept`
- `/dungeon relics`
- `/cr admin reload` — reloads `config.yml`

## Prototype limitations

This is an early prototype, not a production-ready dungeon system. Arenas are built in the current world at generated coordinates. Some relic and curse effects are descriptive only; party management and cleanup are minimal. Test on a backup or dedicated server first. GitHub Actions builds the JAR; it does not host or run a Minecraft server.
