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

- **`VanillaPlusBootstrap`** — `PluginBootstrap` implementation. Runs before plugin enable. Creates three item tags (`vanillaplus:tools`, `vanillaplus:weapons`, `vanillaplus:tools_weapons`), registers six custom enchantments into Paper's registry via `RegistryEvents.ENCHANTMENT`, then tags all six as tradeable, non-treasure, and in-enchanting-table via `LifecycleEvents.TAGS.postFlatten`.
- **`VanillaPlus`** — `JavaPlugin` main class. On enable: validates server version, registers the `/vanillaplus reload` command via `ConfigManager`, loads `config.json`, registers all recipes, registers all modules (only enabled ones are activated), calls `ConfigManager.prune()` then saves, snapshots each module's `enabled` state at startup, and registers a reload listener that adds a restart-required warning via `ConfigManager.addReloadWarning` if any module's `enabled` state changes.

### Module System

Every feature is an `object` implementing **`ModuleInterface`** (which extends `Listener`). Modules self-register as Bukkit event listeners and register their commands/permissions via `register()`.

Each module exposes a `val config: ModuleConfigInterface` property. The `isEnabled` check goes through this interface. Config is bound using `ModuleConfigDelegate`, which caches the decoded config and re-decodes on reload via a listener registered with `ConfigManager`.

All modules are instantiated as `object` singletons and listed explicitly in `VanillaPlus.onEnable()`.

### Configuration System

`ConfigManager` is the central config authority. It holds the raw `JsonObject` (`data`) and exposes:

- `load(fileName)` — reads `config.json` from the plugin data folder into `data`
- `save(fileName)` — writes `data` back to disk (keys are sorted)
- `merge(key, element)` — merges a single JSON element into `data` under the given key
- `decodeWith(key, serializer, default)` — decodes a module's section from `data`, merging defaults back in; also tracks the key in `registeredKeys`
- `prune()` — removes any keys from `data` that were never registered via `decodeWith` (cleans up stale module entries)
- `onReload(listener)` / `notifyReload()` — pub/sub for config reload events; `notifyReload` clears `reloadWarnings` before firing listeners
- `addReloadWarning(warning)` — queues a warning string to be shown to the reload command sender after reload completes
- `reloadCommand` / `reloadPermission` — the `/vanillaplus reload` (alias `/vp reload`) command and its permission

JSON is formatted with `CapitalizedStrategy` (custom `JsonNamingStrategy`). Config is reloaded at runtime via `/vp reload`, which runs asynchronously: `load` → `notifyReload` → `prune` → `save`, then dispatches any queued `reloadWarnings` back to the sender on the main thread.

Each module's config is accessed via a **`ModuleConfigDelegate`** (`delegates/`), a `ReadOnlyProperty<Any?, C>` that caches the decoded config and re-decodes it whenever `ConfigManager.notifyReload()` fires.

Each module config class implements **`ModuleConfigInterface`** (`interfaces/`), which requires an `enabled: Boolean` property.

### Enchantments

Custom enchantments implement **`EnchantmentInterface`** and are registered in `VanillaPlusBootstrap` via `RegistryEvents.ENCHANTMENT`. Their registry key is derived automatically from the class name (e.g. `VerdanceEnchantment` → `vanillaplus:verdance`). Six enchantments are actively registered and tagged as tradeable, non-treasure, and in-enchanting-table:

| Enchantment | Supported Items                               |
|-------------|-----------------------------------------------|
| Verdance    | Hoes                                          |
| Tether      | Tools + Weapons (`vanillaplus:tools_weapons`) |
| Nightsight  | Head armor                                    |
| Nimbus      | Harnesses (chestplates)                       |
| Earthrend   | Pickaxes                                      |
| Embertread  | Foot armor                                    |

SilkTouch and FeatherFalling exist as implementations but are not currently registered in the bootstrap.

### PDCs (Persistent Data Containers)

PDC helpers in `pdcs/` expose Kotlin property delegates on entity types.

### Recipes

Recipe objects implement **`RecipeInterface`** and are listed in `VanillaPlus.onEnable()`. They expose a `recipes` list and a `register()` function that returns elapsed time in ms.

### Package Structure (`org.xodium.vanillaplus`)

| Package         | Contents                                                                               |
|-----------------|----------------------------------------------------------------------------------------|
| `modules/`      | 15 feature module singletons                                                           |
| `data/`         | `CommandData`, `SoundData`, `BookData`, `AdjacentBlockData`                            |
| `delegates/`    | `ModuleConfigDelegate`                                                                 |
| `enchantments/` | Verdance, Tether, Nightsight, Nimbus, Earthrend, Embertread, SilkTouch, FeatherFalling |
| `interfaces/`   | `ModuleInterface`, `ModuleConfigInterface`, `EnchantmentInterface`, `RecipeInterface`  |
| `managers/`     | `ConfigManager`, `PlayerMessageManager`                                                |
| `pdcs/`         | `PlayerPDC`                                                                            |
| `recipes/`      | Chainmail, DiamondRecycle, Painting, RottenFlesh, WoodLog                              |
| `strategies/`   | `CapitalizedStrategy`                                                                  |
| `utils/`        | `Utils`, `CommandUtils`, `BlockUtils`, `MessageUtils`, `PlayerUtils`, `ScheduleUtils`  |

### Key Conventions

- All internal classes are `internal` visibility.
- All modules are `object` singletons.
- Each module defines a `@Serializable data class Config(...) : ModuleConfigInterface` nested inside it, and exposes it via `ModuleConfigDelegate`.
- MiniMessage (`Utils.MM`) is used throughout for all text formatting.
- The `@Suppress("UnstableApiUsage")` annotation is needed whenever using Paper's experimental APIs (registry events, dialogs, enchantment builders, menu types, etc.).
- ktlint is enforced (configured in `.idea/ktlint-plugin.xml`); wildcard imports are suppressed per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` only when needed.
- Recipe `NamespacedKey` naming: `{descriptive_name}_{recipe_type}` (e.g. `diamond_recycle_blasting_recipe`, `chainmail_helmet_shaped_recipe`).
