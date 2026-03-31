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
- **`VanillaPlus`** — `JavaPlugin` main class. On enable: validates server version, registers all recipes, then registers all modules. All modules are active by default (`enabled` defaults to `true` on `ModuleInterface`); override `enabled` to `false` in a specific module to disable it at compile time.

### Module System

Every feature is an `object` implementing **`ModuleInterface`** (which extends `Listener`). Modules self-register as Bukkit event listeners and register their commands/permissions via `register()`.

Each module exposes a nested `object Config` with hardcoded default values. There is no file-based configuration — all settings are compile-time constants. `ModuleInterface` provides `val enabled: Boolean get() = true`; modules that should be inactive override this to `false`.

All modules are instantiated as `object` singletons and listed explicitly in `VanillaPlus.onEnable()`.

### Enchantments

Custom enchantments implement **`EnchantmentInterface<T : Event>`** (generic on event type) and are registered in `VanillaPlusBootstrap` via `RegistryEvents.ENCHANTMENT`. Their registry key is derived automatically from the class name (e.g. `VerdanceEnchantment` → `vanillaplus:verdance`). Seven enchantments are actively registered and tagged as tradeable, non-treasure, and in-enchanting-table:

| Enchantment | Supported Items                               |
|-------------|-----------------------------------------------|
| Verdance    | Hoes                                          |
| Tether      | Tools + Weapons (`vanillaplus:tools_weapons`) |
| Nimbus      | Harnesses (chestplates)                       |
| Earthrend   | Pickaxes                                      |
| Embertread  | Foot armor                                    |
| Inferno     | Blaze Rods                                    |

SilkTouch and FeatherFalling exist as implementations but are not currently registered in the bootstrap.

### Rune System

Each rune type is an `internal object` in `runes/` implementing **`RuneInterface`** (`id`, `item`, `modifiers(player, count)`). `RuneInterface` provides a default `modifierKey: NamespacedKey` derived from `id` (e.g. `CrimsoniteRune` → `vanillaplus:rune_crimsoniterune_modifier`), so each rune automatically gets a unique attribute modifier key with no boilerplate.

`RuneModule` owns a `RUNES: List<RuneInterface>` registry. Bosses (Elder Guardian, Wither, Warden, Ender Dragon) have a configurable chance to drop rune items. Players open `/runes` to view their 5 rune slots (`MenuType.HOPPER`). Placing a gem equips it; removing it returns it to inventory. On close, slot state is saved to `PlayerPDC.runeSlots` (comma-separated string) and each rune's `modifiers()` is called with its equipped count. Modifiers are restored on `PlayerJoinEvent`. Item PDC tags and `isRune()` checks use `ItemStack.persistentDataContainer` directly — no `itemMeta` indirection.

`RuneMenu` in `menus/` handles menu creation and tracks open views via a `WeakHashMap<InventoryView, Unit>`. `RuneModule` reads that map in its `InventoryCloseEvent`, `InventoryClickEvent`, and `InventoryDragEvent` handlers to scope behaviour to rune menus only.

**Adding a new rune:** create `runes/FooRune.kt : RuneInterface`, implement `item` and `modifiers()`, and add it to `RuneModule.runes`.

### PDCs (Persistent Data Containers)

PDC helpers in `pdcs/` expose Kotlin property delegates on entity types.

### Recipes

Recipe objects implement **`RecipeInterface`** and are listed in `VanillaPlus.onEnable()`. They expose a `recipes` list and a `register()` function that returns elapsed time in ms.

### Package Structure (`org.xodium.vanillaplus`)

| Package         | Contents                                                                                                          |
|-----------------|-------------------------------------------------------------------------------------------------------------------|
| `modules/`      | 15 feature module singletons                                                                                      |
| `data/`         | `CommandData`, `BookData`, `AdjacentBlockData`                                                                    |
| `enchantments/` | Verdance, Tether, Nimbus, Earthrend, Embertread, SilkTouch, FeatherFalling                                        |
| `interfaces/`   | `ModuleInterface`, `EnchantmentInterface`, `RecipeInterface`, `RuneInterface`                                     |
| `runes/`        | `CrimsoniteRune`, `ZephyriteRune`, `FerriteRune`, `ObsiditeRune`, `AureliteRune`, `VigoriteRune`, `GalvaniteRune` |
| `menus/`        | `RuneMenu`                                                                                                        |
| `pdcs/`         | `PlayerPDC`                                                                                                       |
| `recipes/`      | Chainmail, DiamondRecycle, Painting, RottenFlesh, WoodLog                                                         |
| `utils/`        | `Utils`, `CommandUtils`, `BlockUtils`, `PlayerUtils`, `ScheduleUtils`                                             |

### Key Conventions

- All internal classes are `internal` visibility.
- All modules are `object` singletons.
- Each module defines a nested `object Config` (with nested `object` blocks for logical groupings) containing hardcoded default values. There is no file-based config system.
- MiniMessage (`Utils.MM`) is used throughout for all text formatting.
- The `@Suppress("UnstableApiUsage")` annotation is needed whenever using Paper's experimental APIs (registry events, dialogs, enchantment builders, menu types, etc.).
- ktlint is enforced (configured in `.idea/ktlint-plugin.xml`); wildcard imports are suppressed per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` only when needed.
- Recipe `NamespacedKey` naming: `{descriptive_name}_{recipe_type}` (e.g. `diamond_recycle_blasting_recipe`, `chainmail_helmet_shaped_recipe`).
