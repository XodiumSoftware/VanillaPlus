# ARCHITECTURE.md

This file provides guidance when working with code in this repository.

## Project Overview

VanillaPlus is a Paper Minecraft plugin (1.21.11) that enhances base gameplay. Built with Kotlin + Gradle, targeting Java 21. Uses the Paper API's modern lifecycle/registry APIs extensively.

## Build & Run Commands

```bash
# Build the shadow JAR (output: build/libs/)
./gradlew shadowJar

# Run a local Paper test server (auto-downloads MC 1.21.11)
./gradlew runServer

# Build only (no shadow)
./gradlew build
```

There are no automated tests in this project.

## Architecture

### Entry Points

- **`VanillaPlusBootstrap`** — `PluginBootstrap` implementation. Runs before plugin enable. Registers custom enchantments, dialogs, and item tags into Paper's registries using lifecycle events.
- **`VanillaPlus`** — `JavaPlugin` main class. On enable: validates server version, registers the `/vanillaplus reload` command, loads `config.json`, then registers all recipes and modules.

### Module System

Every feature is an `object` implementing **`ModuleInterface`** (which extends `Listener`). Modules self-register as Bukkit event listeners and register their commands/permissions via `register()`. The `isEnabled` property reflects the matching `Config.enabled` field in `ConfigData` using Kotlin reflection — the convention is that `FooModule`'s config property in `ConfigData` must be named `fooModule`.

All modules are instantiated as `object` singletons and listed explicitly in `VanillaPlus.onEnable()`.

### Configuration System

`ConfigData` is a `@Serializable` data class backed by `config.json` (in the plugin data folder). It holds one `Config` nested data class per module. JSON is formatted with `CapitalizedStrategy` (custom naming strategy). Config can be reloaded at runtime with `/vp reload`. The `load()` extension function writes defaults on first run and merges unknown keys gracefully (`ignoreUnknownKeys = true`).

### Enchantments

Custom enchantments implement **`EnchantmentInterface`** and are registered in `VanillaPlusBootstrap` via `RegistryEvents.ENCHANTMENT`. Their registry key is derived automatically from the class name (e.g. `ReplantEnchantment` → `vanillaplus:replant`). All custom enchantments are tagged as tradeable, non-treasure, and in-enchanting-table.

### Dialogs

Custom dialogs implement **`DialogInterface`** and are registered in `VanillaPlusBootstrap` via `RegistryEvents.DIALOG`. Key derivation follows the same CamelCase → snake_case convention as enchantments.

### PDCs (Persistent Data Containers)

PDC helpers in `pdcs/` expose Kotlin property delegates on entity types. For example, `MannequinPDC` adds a `Mannequin.owner: UUID` property backed by NBT.

### Recipes

Recipe objects implement **`RecipeInterface`** and are listed in `VanillaPlus.onEnable()`. They expose a `recipes` list and a `register()` function that returns elapsed time in ms.

### Key Conventions

- All internal classes are `internal` visibility.
- All modules are `object` singletons.
- Each module defines a `@Serializable data class Config(...)` nested inside it.
- MiniMessage (`Utils.MM`) is used throughout for all text formatting.
- The `@Suppress("UnstableApiUsage")` annotation is needed whenever using Paper's experimental APIs (registry events, dialogs, enchantment builders, etc.).
- ktlint is enforced (configured in `.idea/ktlint-plugin.xml`); wildcard imports are suppressed per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` only when needed.
