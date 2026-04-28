# ARCHITECTURE.md

This file provides guidance when working with code in this repository.

## Project Overview

IllyriaPlus is a multi-module Paper Minecraft plugin project (1.21.11) containing two plugins:

- **IllyriaCore** — Enhances base gameplay with custom enchantments, recipes, and mechanics
- **IllyriaKingdoms** — Kingdoms/factions system for land claiming and territory management

Built with Kotlin + Gradle, targeting Java 21. Uses the Paper API's modern lifecycle/registry APIs extensively.

## Module Structure

```
IllyriaPlus/
├── settings.gradle.kts      # Defines IllyriaCore and IllyriaKingdoms subprojects
├── IllyriaCore/             # Core gameplay enhancement plugin
│   ├── build.gradle.kts
│   └── src/
└── IllyriaKingdoms/         # Kingdoms/factions plugin
    ├── build.gradle.kts
    └── src/
```

## Build & Run Commands

```bash
# Build all subprojects
./gradlew shadowJar

# Build specific subproject
./gradlew :IllyriaCore:shadowJar
./gradlew :IllyriaKingdoms:shadowJar

# Run test server for specific subproject
./gradlew :IllyriaCore:runServer
./gradlew :IllyriaKingdoms:runServer

# Build only (no shadow)
./gradlew build
```

Output JARs:

- `IllyriaCore/build/libs/IllyriaCore-*.jar`
- `IllyriaKingdoms/build/libs/IllyriaKingdoms-*.jar`

There are no automated tests in this project.

## IllyriaCore Architecture

### Entry Points

- **`IllyriaPlusBootstrap`** — `PluginBootstrap` implementation. Runs before plugin enable. Creates item tags (`vanillaplus:tools`, `vanillaplus:weapons`, `vanillaplus:tools_weapons`, `vanillaplus:blaze_rods`), registers eleven custom enchantments into Paper's registry via `RegistryEvents.ENCHANTMENT`, then tags all eleven as tradeable, non-treasure, and in-enchanting-table via `LifecycleEvents.TAGS.postFlatten`.
- **`IllyriaPlus`** — `JavaPlugin` main class. On enable: validates server version, registers all recipes, registers all mechanics. All mechanics are active by default (`enabled` defaults to `true` on `ModuleInterface`); override `enabled` to `false` in a specific mechanic to disable it at compile time.

### Module System

Every feature is an `object` implementing **`ModuleInterface`** (which extends `Listener`). Modules self-register as Bukkit event listeners and register their commands/permissions via `register()`.

Each module exposes a nested `object Config` with hardcoded default values. There is no file-based configuration — all settings are compile-time constants.

All mechanics are instantiated as `object` singletons and listed explicitly in `IllyriaPlus.onEnable()`.

### Enchantments

Custom enchantments implement **`EnchantmentInterface`** and are registered in `IllyriaPlusBootstrap` via `RegistryEvents.ENCHANTMENT`. The interface provides:

- **`key`** — a `TypedKey<Enchantment>` derived automatically from the class name (e.g. `VerdanceEnchantment` → `vanillaplus:verdance`).
- **`invoke(builder)`** — override to configure the enchantment's registry entry (description, anvil cost, level range, weight, slot group, etc.). The default implementation is a no-op pass-through.
- **`get()`** — looks up and returns the live `Enchantment` instance from the registry after bootstrap.

Event handling is done via ordinary `@EventHandler` methods in each enchantment object — there is no generic event type on the interface. Twelve enchantments are actively registered and tagged as tradeable, non-treasure, and in-enchanting-table:

| Enchantment | Slot Group | Supported Items                               |
|-------------|------------|-----------------------------------------------|
| Verdance    | `MAINHAND` | Hoes                                          |
| Tether      | `MAINHAND` | Tools + Weapons (`vanillaplus:tools_weapons`) |
| Nimbus      | `SADDLE`   | Harnesses (Happy Ghast saddle slot)           |
| Earthrend   | `MAINHAND` | Pickaxes                                      |
| Embertread  | `FEET`     | Foot armor                                    |
| Inferno     | `MAINHAND` | Blaze Rods (`vanillaplus:blaze_rods`)         |
| Skysunder   | `MAINHAND` | Blaze Rods (`vanillaplus:blaze_rods`)         |
| Witherbrand | `MAINHAND` | Blaze Rods (`vanillaplus:blaze_rods`)         |
| Frostbind   | `MAINHAND` | Blaze Rods (`vanillaplus:blaze_rods`)         |
| Tempest     | `MAINHAND` | Blaze Rods (`vanillaplus:blaze_rods`)         |
| Voidpull    | `MAINHAND` | Blaze Rods (`vanillaplus:blaze_rods`)         |
| Quake       | `MAINHAND` | Blaze Rods (`vanillaplus:blaze_rods`)         |

SilkTouch and FeatherFalling exist as implementations but are not currently registered in the bootstrap.

**Spell system:** Seven Blaze Rod spell enchantments (Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Voidpull, Quake) each cost XP to cast. All seven are **compatible** with each other and can be combined on a single wand. `SpellManager.handleWandInteraction` manages the interaction: **left-click** casts the selected spell (consuming XP), **right-click** cycles through available spells (showing the selected spell name in the action bar). `XpManager` handles XP cost validation and deduction, playing `NO_XP_SOUND` when the player has insufficient XP. Creative mode bypasses XP costs. Frostbind and Voidpull tag their projectiles with a `NamespacedKey` and resolve hits in `ProjectileHitEvent`. Projectile trail effects are created via `ScheduleUtils.spawnProjectileTrail`, which schedules a per-tick particle task that self-cancels when the entity is no longer valid.

### PDCs (Persistent Data Containers)

PDC helpers in `pdcs/` expose Kotlin property delegates on entity types. `ItemPDC` provides `selectedSpell` for wand spell selection.

### Recipes

Recipe objects implement **`RecipeInterface`** and are listed in `IllyriaPlus.onEnable()`. They expose a `recipes` list and a `register()` function that returns elapsed time in ms.

### Package Structure (`org.xodium.illyriaplus` in IllyriaCore)

| Package         | Contents                                                                                                                                         |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `mechanics/`    | 14 feature mechanic singletons                                                                                                                   |
| `data/`         | `CommandData`, `BookData`, `AdjacentBlockData`                                                                                                   |
| `enchantments/` | Verdance, Tether, Nimbus, Earthrend, Embertread, Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Voidpull, Quake, SilkTouch, FeatherFalling |
| `interfaces/`   | `ModuleInterface`, `EnchantmentInterface`, `RecipeInterface`, `ItemInterface`                                                                    |
| `managers/`     | `XpManager`, `PlayerMessageManager`, `SpellManager`                                                                                              |
| `pdcs/`         | `PlayerPDC`, `ItemPDC`                                                                                                                           |
| `recipes/`      | Chainmail, DiamondRecycle, Painting, RottenFlesh, WoodLog                                                                                        |
| `utils/`        | `Utils`, `CommandUtils`, `BlockUtils`, `PlayerUtils`, `ScheduleUtils`                                                                            |

### Key Conventions

- All internal classes are `internal` visibility.
- All mechanics are `object` singletons.
- Each module defines a nested `object Config` (with nested `object` blocks for logical groupings) containing hardcoded default values. There is no file-based config system.
- MiniMessage (`Utils.MM`) is used throughout for all text formatting.
- The `@Suppress("UnstableApiUsage")` annotation is needed whenever using Paper's experimental APIs (registry events, dialogs, enchantment builders, menu types, etc.).
- ktlint is enforced (configured in `.idea/ktlint-plugin.xml`); wildcard imports are suppressed per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` only when needed.
- Recipe `NamespacedKey` naming: `{descriptive_name}_{recipe_type}` (e.g. `diamond_recycle_blasting_recipe`, `chainmail_helmet_shaped_recipe`).
