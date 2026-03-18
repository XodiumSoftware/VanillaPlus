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
- **`VanillaPlus`** — `JavaPlugin` main class. On enable: validates server version, registers the `/vanillaplus reload` command via `ConfigManager`, loads `config.json`, then registers all recipes and modules.

### Module System

Every feature is an `object` implementing **`ModuleInterface`** (which extends `Listener`). Modules self-register as Bukkit event listeners and register their commands/permissions via `register()`.

Each module exposes a `val config: ModuleConfigInterface` property. The `isEnabled` check goes through this interface. Config is bound using `ModuleConfigDelegate`, which caches the decoded config and re-decodes on reload via a listener registered with `ConfigManager`.

All modules are instantiated as `object` singletons and listed explicitly in `VanillaPlus.onEnable()`.

### Configuration System

`ConfigManager` is the central config authority. It holds the raw `JsonObject` (`data`) and exposes:
- `load(fileName)` — reads `config.json` from the plugin data folder into `data`
- `save(fileName)` — writes `data` back to disk
- `decodeWith(key, serializer, default)` — decodes a module's section from `data`, merging defaults back in
- `onReload(listener)` / `notifyReload()` — pub/sub for config reload events
- `reloadCommand` / `reloadPermission` — the `/vanillaplus reload` command and its permission

JSON is formatted with `CapitalizedStrategy` (custom `JsonNamingStrategy`). Config is reloaded at runtime via `/vp reload`, which calls `load` → `notifyReload` → `save` asynchronously.

Each module's config is accessed via a **`ModuleConfigDelegate`** (`delegates/`), a `ReadOnlyProperty<Any?, C>` that caches the decoded config and re-decodes it whenever `ConfigManager.notifyReload()` fires.

Each module config class implements **`ModuleConfigInterface`** (`interfaces/`), which requires an `enabled: Boolean` property.

### Enchantments

Custom enchantments implement **`EnchantmentInterface`** and are registered in `VanillaPlusBootstrap` via `RegistryEvents.ENCHANTMENT`. Their registry key is derived automatically from the class name (e.g. `ReplantEnchantment` → `vanillaplus:replant`). All custom enchantments are tagged as tradeable, non-treasure, and in-enchanting-table.

### Dialogs

Custom dialogs implement **`DialogInterface`** and are registered in `VanillaPlusBootstrap` via `RegistryEvents.DIALOG`. Key derivation follows the same CamelCase → snake_case convention as enchantments.

- **Registered dialogs** (e.g. `FaqDialog`): implement `DialogInterface`, registered in `VanillaPlusBootstrap`, shown via `player.showDialog(...)`.
- **On-the-fly dialogs** (e.g. `MannequinDialog`): created per-entity as extension functions, not registered in the bootstrap.

All dialog API requires `@Suppress("UnstableApiUsage")`.

### Menus

Custom inventory UIs live in `menus/`. `MannequinEquipmentMenu` is an `object` that builds a `MenuType.GENERIC_9X1` inventory view for editing mannequin equipment. It tracks open views in a `WeakHashMap<InventoryView, Mannequin>` and handles slot clicks (shift-click and cursor placement). Menu interaction events are routed through `MannequinModule`.

### PDCs (Persistent Data Containers)

PDC helpers in `pdcs/` expose Kotlin property delegates on entity types. For example, `MannequinPDC` adds a `Mannequin.owner: UUID` property backed by NBT.

### Recipes

Recipe objects implement **`RecipeInterface`** and are listed in `VanillaPlus.onEnable()`. They expose a `recipes` list and a `register()` function that returns elapsed time in ms.

### Package Structure (`org.xodium.vanillaplus`)

| Package | Contents |
|---|---|
| `modules/` | 17 feature module singletons |
| `data/` | `CommandData`, `SoundData`, `BookData`, `AdjacentBlockData` |
| `dialogs/` | `MannequinDialog` (on-the-fly) |
| `delegates/` | `ModuleConfigDelegate` |
| `enchantments/` | Replant, Pickup, NightVision, Nimbus, VeinMine, SilkTouch, FeatherFalling |
| `interfaces/` | `ModuleInterface`, `ModuleConfigInterface`, `EnchantmentInterface`, `DialogInterface`, `RecipeInterface` |
| `managers/` | `ConfigManager`, `PlayerMessageManager` |
| `menus/` | `MannequinEquipmentMenu` |
| `pdcs/` | `MannequinPDC`, `PlayerPDC` |
| `recipes/` | Chainmail, DiamondRecycle, Painting, RottenFlesh, WoodLog |
| `strategies/` | `CapitalizedStrategy` |
| `utils/` | `Utils`, `CommandUtils`, `BlockUtils`, `MessageUtils`, `PlayerUtils`, `ScheduleUtils` |

### Key Conventions

- All internal classes are `internal` visibility.
- All modules are `object` singletons.
- Each module defines a `@Serializable data class Config(...) : ModuleConfigInterface` nested inside it, and exposes it via `ModuleConfigDelegate`.
- MiniMessage (`Utils.MM`) is used throughout for all text formatting.
- The `@Suppress("UnstableApiUsage")` annotation is needed whenever using Paper's experimental APIs (registry events, dialogs, enchantment builders, menu types, etc.).
- ktlint is enforced (configured in `.idea/ktlint-plugin.xml`); wildcard imports are suppressed per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` only when needed.
- Recipe `NamespacedKey` naming: `{descriptive_name}_{recipe_type}` (e.g. `diamond_recycle_blasting_recipe`, `chainmail_helmet_shaped_recipe`).
