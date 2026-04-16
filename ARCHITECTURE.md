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

- **`VanillaPlusBootstrap`** — `PluginBootstrap` implementation. Runs before plugin enable. Creates item tags (`vanillaplus:tools`, `vanillaplus:weapons`, `vanillaplus:tools_weapons`, `vanillaplus:blaze_rods`), registers eleven custom enchantments into Paper's registry via `RegistryEvents.ENCHANTMENT`, then tags all eleven as tradeable, non-treasure, and in-enchanting-table via `LifecycleEvents.TAGS.postFlatten`.
- **`VanillaPlus`** — `JavaPlugin` main class. On enable: validates server version, registers all recipes, registers all modules. All modules are active by default (`enabled` defaults to `true` on `ModuleInterface`); override `enabled` to `false` in a specific module to disable it at compile time.

### Module System

Every feature is an `object` implementing **`ModuleInterface`** (which extends `Listener`). Modules self-register as Bukkit event listeners and register their commands/permissions via `register()`.

Each module exposes a nested `object Config` with hardcoded default values. There is no file-based configuration — all settings are compile-time constants.

All modules are instantiated as `object` singletons and listed explicitly in `VanillaPlus.onEnable()`.

### Enchantments

Custom enchantments implement **`EnchantmentInterface`** and are registered in `VanillaPlusBootstrap` via `RegistryEvents.ENCHANTMENT`. The interface provides:

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

**Mana system:** Seven Blaze Rod spell enchantments (Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Voidpull, Quake) share a single mana pool stored in `PlayerPDC.mana`. All seven are **compatible** with each other and can be combined on a single wand. `PlayerModule.handleWandInteraction` manages the interaction: **left-click** casts the selected spell, **right-click** cycles through available spells (showing the selected spell name in the action bar). `SpellManager` handles spell registration, cycling logic, and execution dispatch. `ManaManager` owns the bossbar display (`showManaBar`), regen scheduler (`startRegenTask`), and the no-mana sound (`NO_MANA_SOUND`). The bossbar uses the **Spellbite** gradient (`#832466 → #BF4299 → #832466`) with `NOTCHED_10` overlay. Frostbind and Voidpull tag their projectiles with a
`NamespacedKey` and resolve hits in `ProjectileHitEvent`. Projectile trail effects are created via `ScheduleUtils.spawnProjectileTrail`, which schedules a per-tick particle task that self-cancels when the entity is no longer valid.

### Items

Custom items implement **`ItemInterface`** and are created as `object` singletons in `potions/`. Items include potions and other consumables with magical effects, primarily the **Mana Item** which refills the player's mana pool for spell casting.

**Mana Item:**

- **Effect:** Instantly refills `PlayerPDC.mana` to 100 (max)
- **Visual:** Purple color from the Spellbite gradient (`#832466`)
- **Name:** "Potion of Arcane Restoration" with gradient styling
- **Marker:** Uses `ItemPDC.isManaPotion` PDC flag
- **Brewing:** Awkward Potion/Splash Potion/Lingering Potion + Blaze Rod in brewing stand
- **Crafting:** 8 Arrows + 1 Lingering Potion = 8 Tipped Arrows
- **Handler:** `PotionModule` listens for `PlayerItemConsumeEvent`

### PDCs (Persistent Data Containers)

PDC helpers in `pdcs/` expose Kotlin property delegates on entity types. `ItemPDC` provides `isManaPotion` for identifying mana potions, and `selectedSpell` for wand spell selection.

### Recipes

Recipe objects implement **`RecipeInterface`** and are listed in `VanillaPlus.onEnable()`. They expose a `recipes` list and a `register()` function that returns elapsed time in ms.

### Package Structure (`org.xodium.vanillaplus`)

| Package         | Contents                                                                                                                                         |
|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `potions/`      | Custom item implementations (Mana Item)                                                                                                         |
| `modules/`      | 15 feature module singletons                                                                                                                     |
| `data/`         | `CommandData`, `BookData`, `AdjacentBlockData`                                                                                                   |
| `enchantments/` | Verdance, Tether, Nimbus, Earthrend, Embertread, Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Voidpull, Quake, SilkTouch, FeatherFalling |
| `interfaces/`   | `ModuleInterface`, `EnchantmentInterface`, `RecipeInterface`, `ItemInterface`                                                                    |
| `managers/`     | `ManaManager`, `PlayerMessageManager`, `SpellManager`                                                                                            |
| `pdcs/`         | `PlayerPDC`, `ItemPDC`                                                                                                                           |
| `recipes/`      | Chainmail, DiamondRecycle, ManaItem, Painting, RottenFlesh, WoodLog                                                                               |
| `utils/`        | `Utils`, `CommandUtils`, `BlockUtils`, `PlayerUtils`, `ScheduleUtils`                                                                            |

### Key Conventions

- All internal classes are `internal` visibility.
- All modules are `object` singletons.
- Each module defines a nested `object Config` (with nested `object` blocks for logical groupings) containing hardcoded default values. There is no file-based config system.
- MiniMessage (`Utils.MM`) is used throughout for all text formatting.
- The `@Suppress("UnstableApiUsage")` annotation is needed whenever using Paper's experimental APIs (registry events, dialogs, enchantment builders, menu types, etc.).
- ktlint is enforced (configured in `.idea/ktlint-plugin.xml`); wildcard imports are suppressed per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` only when needed.
- Recipe `NamespacedKey` naming: `{descriptive_name}_{recipe_type}` (e.g. `diamond_recycle_blasting_recipe`, `chainmail_helmet_shaped_recipe`).
