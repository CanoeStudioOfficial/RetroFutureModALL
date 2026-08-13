# RetroFuture

Multi-module RetroFuturaGradle workspace for Minecraft 1.12.2 Forge mods.

## Included Mods

- `retrofuturebuzzybees`
- `retrofuturelushcave`
- `retrofuturemccore`
- `retrofuturenetherupdate`
- `retrofuturethewildupdate`
- `retrofuturetrailsandtales`
- `retrofuturetrickytrials`
- `retrofutureupdateaquatic`
- `retrofuturevillageandpillage`

## Build

Build every mod:

```powershell
.\gradlew.bat build
```

Build one all-in-one distribution jar (Binnie-style aggregation):

```powershell
.\gradlew.bat all
# Equivalent explicit task:
.\gradlew.bat buildAllInOne
```

The all-in-one artifact is written to `build/libs/retrofuture-all-<version>.jar`.
It contains all currently discovered modules in one installable file while
preserving each module's original Forge Mod ID, entry point, metadata, mixins,
and resource namespace. The normal module jars are still generated under each
module's `build/libs` directory.

The `all` project is only an aggregator. It has no mod source of its own; its
`jar` task depends on the individual modules' `reobfJar` tasks and copies their
compiled classes and resources into one final archive, just like the reference
Binnie project.

Build one mod:

```powershell
.\gradlew.bat :retrofuturelushcave:build
.\gradlew.bat :retrofuturethewildupdate:build
```

List discovered modules:

```powershell
.\gradlew.bat listMods
```

Artifacts are written under each module's `build/libs` directory.

## Add Another Mod

Create a new folder under `modules/<modid>` with this shape:

```text
modules/<modid>/
  gradle.properties
  tags.properties
  CHANGELOG.md
  README.md
  src/main/java/...
  src/main/resources/...
```

`settings.gradle` automatically includes any folder under `modules` that contains `gradle.properties`.

Common build behavior lives in `gradle/scripts/mod-build.gradle`. Module-specific dependencies or task tweaks can go in `modules/<modid>/extra.gradle`.
