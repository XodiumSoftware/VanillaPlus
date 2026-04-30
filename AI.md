# IllyriaPlus — Claude Code Context

## Project at a Glance

- **Name:** IllyriaPlus
- **Type:** Multi-module Minecraft Paper plugin project (server-side only)
- **MC Version:** 1.21.11
- **Language:** Kotlin (JVM 21)
- **Build Tool:** Gradle with Kotlin DSL
- **Modules:**
    - **IllyriaCore** — Core gameplay enhancements (enchantments, recipes, mechanics)
    - **IllyriaKingdoms** — Kingdoms/factions system for land claiming

## APIs & Tools

| Category            | Technology                               | Purpose                            |
|---------------------|------------------------------------------|------------------------------------|
| **Core API**        | [Paper API](https://papermc.io/) 1.21.11 | Minecraft server plugin API        |
| **Language**        | Kotlin 2.3.20                            | JVM language                       |
| **Build Tool**      | Gradle (Kotlin DSL)                      | Build automation                   |
| **Gradle Plugins**  | Shadow 9.4.1                             | Fat JAR creation                   |
|                     | run-paper 3.0.2                          | Local test server                  |
|                     | resource-factory 1.3.1                   | `paper-plugin.yml` generation      |
|                     | foojay-resolver 1.0.0                    | Auto-download JVM toolchains       |
| **Text Formatting** | MiniMessage                              | Adventure API component-based text |
| **Docs**            | Dokka                                    | Kotlin API documentation           |
| **Code Style**      | ktlint                                   | Kotlin linting (IDE plugin)        |

### Paper API Resources

- **Documentation**: https://docs.papermc.io/paper/dev/
- **JavaDoc**: https://jd.papermc.io/paper/1.21.11/ (matches project version)

### Paper API Notes

- Uses modern lifecycle/registry APIs (experimental — requires `@Suppress("UnstableApiUsage")`)
- Custom enchantments use `RegistryEvents.ENCHANTMENT`
- Item tags created via `LifecycleEvents.TAGS.postFlatten`
- Plugin bootstrapper pattern for early registry access

### Dokka

Documentation is generated with Dokka from KDoc comments in the source code.

- Run `./gradlew :IllyriaCore:dokkaGenerateHtml` to generate IllyriaCore documentation
- Run `./gradlew :IllyriaKingdoms:dokkaGenerateHtml` to generate IllyriaKingdoms documentation
- Output goes to `{module}/docs/` directory (published to GitHub Pages automatically)
- Auto-deployed via GitHub Actions on pushes to main
- Key files to document: interfaces, managers, and public APIs
- Use KDoc format: `/** ... */` with Markdown support

## Quick Commands

```bash
# Build all plugins
./gradlew shadowJar

# Build specific plugin
./gradlew :IllyriaCore:shadowJar
./gradlew :IllyriaKingdoms:shadowJar

# Run local test server (auto-downloads Paper 1.21.11)
./gradlew :IllyriaCore:runServer
./gradlew :IllyriaKingdoms:runServer

# Generate Dokka documentation for specific module
./gradlew :IllyriaCore:dokkaGenerateHtml
./gradlew :IllyriaKingdoms:dokkaGenerateHtml

# Run linting
./gradlew ktlintCheck

# Fix linting issues
./gradlew ktlintFormat
```

## Module Structure

```
IllyriaPlus/
├── settings.gradle.kts       # Subproject definitions
├── IllyriaCore/              # Core gameplay plugin
│   ├── build.gradle.kts
│   └── src/
│       ├── IllyriaPlus.kt
│       ├── IllyriaPlusBootstrap.kt
│       ├── mechanics/        # 14 feature mechanics
│       ├── enchantments/     # 14 enchantment implementations
│       ├── interfaces/       # ModuleInterface, EnchantmentInterface, RecipeInterface
│       ├── managers/         # XpManager, PlayerMessageManager, SpellManager
│       ├── pdcs/             # PlayerPDC, ItemPDC
│       ├── recipes/          # Recipe implementations
│       ├── data/             # Data classes
│       └── utils/            # Utility functions
└── IllyriaKingdoms/          # Kingdoms/factions plugin
    ├── build.gradle.kts
    └── src/
        └── org/xodium/illyriaplus/
            └── IllyriaKingdoms.kt
```

## IllyriaCore Architecture

### Entry Points

1. **IllyriaPlusBootstrap** (`PluginBootstrap`) — Runs before plugin enable. Registers 11 custom enchantments into Paper's registry and creates item tags.
2. **IllyriaPlus** (`JavaPlugin`) — Main class. Validates server version, registers recipes, and enables all mechanics.

### Module System

- All features are Kotlin `object` singletons implementing `ModuleInterface` (extends Bukkit `Listener`)
- Mechanics self-register in `IllyriaPlus.onEnable()` via `register()`
- Each mechanic has a nested `object Config` with **hardcoded defaults** — no file-based config system
- To disable a mechanic at compile time, override `enabled` to `false` in the mechanic object

### Enchantments

Custom enchantments implement `EnchantmentInterface` with:

- Auto-generated `TypedKey<Enchantment>` from class name (e.g., `VerdanceEnchantment` → `vanillaplus:verdance`)
- `invoke(builder)` to configure registry entry (description, cost, levels, weight, slots)
- `get()` to retrieve live `Enchantment` instance from registry

**Active Enchantments (11):**
| Name | Slot | Items |
|------|------|-------|
| Verdance | MAINHAND | Hoes |
| Tether | MAINHAND | Tools + Weapons |
| Nimbus | SADDLE | Happy Ghast harnesses |
| Earthrend | MAINHAND | Pickaxes |
| Embertread | FEET | Foot armor |
| Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Voidpull, Quake | MAINHAND | Blaze Rods (spell wands) |

**Spell System:**

- 7 Blaze Rod spell enchantments (Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Voidpull, Quake) cost XP to cast
- All spells are **compatible** — can combine multiple on one wand
- **Left-click:** Cast selected spell (consumes XP, free in Creative)
- **Right-click:** Cycle spells (shows in action bar)
- `XpManager` handles XP cost validation and plays `NO_XP_SOUND` on insufficient XP

### Key Conventions

- All internal classes use `internal` visibility
- All mechanics are `object` singletons
- Use MiniMessage (`Utils.MM`) for all text formatting
- Add `@Suppress("UnstableApiUsage")` when using Paper's experimental APIs
- ktlint is enforced; suppress wildcard imports per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` if needed
- Recipe `NamespacedKey` naming: `{descriptive_name}_{recipe_type}`
- **Import types instead of using fully qualified paths** — e.g., `import org.bukkit.inventory.meta.PotionMeta` instead of `org.bukkit.inventory.meta.PotionMeta`
- **Use `it` for single-parameter lambdas** — e.g., `list.forEach { it.doSomething() }` instead of `list.forEach { item -> item.doSomething() }`
- **Use `ItemStack.of()` instead of `ItemStack()` constructor** — Paper's modern API for creating item stacks
- **Don't create intermediate `const val` for override properties** — assign directly to the override, e.g., `override val key: String = "vanillaplus:mana_potion"` instead of creating a `const val KEY` and then `override val key = KEY`
- **Don't add KDoc to implemented overrides** — the base interface/class already has documentation; let it inherit naturally
- **Use data class builders** — e.g., `potion(PotionData(color = X, displayName = Y))` instead of lambda receivers for simpler configuration
- **Use explicit named factory functions** — prefer `potion()` and `splash()` over `invoke()` operator for clarity
- **Alphabetical order for static collections** — `SPELL_MAP` and similar static maps/lists should be sorted alphabetically by key

### Code Structure (in interfaces, classes, objects)

Order members from top to bottom:

1. **`const val`** — compile-time constants
2. **`val`** — read-only properties (overrides first)
3. **`var`** — mutable properties (overrides first)
4. **`fun`** — functions (overrides first)
5. **`object Config`** — nested config object (at bottom for mechanics)

Within each group:

- **`override`** members go above regular members
- **`@EventHandler`** functions go above regular `public` functions
- **`@EventHandler`** functions should always be named `on(event: <EventType>)` — Kotlin allows multiple `@EventHandler fun on(...)` as long as parameter types differ
- **`@EventHandler`** functions should not have KDoc comments (the event type is self-documenting)
- **`public`** members go above **`private`** members

## Testing

- No automated tests in this project
- Test by running `./gradlew :IllyriaCore:runServer` or `./gradlew :IllyriaKingdoms:runServer` and manually verifying in-game

## Important Notes

- No file-based configuration — all settings are compile-time constants in mechanic `Config` objects
- Enchantments must be registered in `IllyriaPlusBootstrap` AND tagged as tradeable/non-treasure/enchanting-table
- Spell enchantments use `XpManager` to consume XP on cast
- Project uses Paper's modern lifecycle/registry APIs extensively

## Claude Code Workflow

### Task Management

**When creating tasks:**

- Number tasks in the name (e.g., "1. Add Verdance enchantment", "2. Update mana system")
- This makes it easy to reference specific tasks in conversation

**After completing each task:**

- Ask the user if they want to git commit the changes or adjust before committing

**When all tasks in a worktree are complete:**

- Ask the user if they want to git publish (push) the changes or adjust before publishing

### After Making Edits

**Always update documentation when code changes:**

1. **ARCHITECTURE.md** — Update if you:
    - Add/remove enchantments, mechanics, recipes, or managers
    - Change the mechanic system or interfaces
    - Modify the mana system or spell mechanics
    - Change project structure or conventions

2. **GUIDE.md** — Update if you:
    - Add new plugins or modules
    - Change build commands or installation steps
    - Add/remove major features

3. **KDoc comments** — Add/update if you:
    - Add new public APIs (interfaces, managers, utils)
    - Change existing function signatures or behavior
    - Add complex logic that needs explanation
    - **Run `./gradlew :IllyriaCore:dokkaGenerateHtml`** or `./gradlew :IllyriaKingdoms:dokkaGenerateHtml` to regenerate docs after changes

**Rule of thumb:** If a code change would confuse someone reading the docs, update the docs.

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

- **kotlin.yml** — Builds shadow JARs for both IllyriaCore and IllyriaKingdoms on push/PR, uploads artifacts, creates nightly release
- **enforce_pr_title.yml** — Validates PR titles follow conventional commits

## Adding a New Module (Subproject)

To create a new plugin module (e.g., `IllyriaCustom`):

1. Add to `settings.gradle.kts`: `include("IllyriaCustom")`
2. Create directory `IllyriaCustom/`
3. Create `IllyriaCustom/build.gradle.kts` (copy from IllyriaCore and adjust names)
4. Create `IllyriaCustom/src/` source directory
5. Update CI workflow to build and upload the new module

## Adding Components to IllyriaCore

### Adding an Enchantment

1. Create new file in `IllyriaCore/src/enchantments/YournameEnchantment.kt`
2. Implement `EnchantmentInterface` as an `object`
3. In `invoke(builder)`, configure: `description()`, `supportedItems()`, `anvilCost()`, `maxLevel()`, `weight()`, `slotGroup()`
4. In `IllyriaPlusBootstrap.kt`:
    - Add `YournameEnchantment` to the `ENCHANTMENTS` list
    - Add it to the tags (tradeable, non-treasure, enchanting-table)
    - Add supported items to appropriate `ItemTag` if needed
5. If it's a spell (Blaze Rod enchantment):
    - Register in `SpellManager.SPELL_MAP` (alphabetical order)
    - Store cost in `PlayerPDC` constants
    - Add to enchantment compatibility group
    - Implement `@EventHandler` for `PlayerInteractEvent` or projectile logic
6. Update `ARCHITECTURE.md` enchantment table
7. Add KDoc comments to explain the enchantment's behavior
8. Run `./gradlew dokkaHtmlMultiModule` to regenerate documentation

### Adding a Mechanic

1. Create new file in `IllyriaCore/src/mechanics/YourMechanic.kt`
2. Implement `ModuleInterface` as an `object`
3. Override `Config` object with settings as compile-time constants
4. Implement `@EventHandler` methods for events
5. Register commands/permissions in `register()` if needed
6. In `IllyriaPlus.kt`, add `YourMechanic` to the mechanic list in `onEnable()`
7. Add KDoc comments explaining the mechanic's purpose and features
8. Update `ARCHITECTURE.md` mechanic count

### Adding a Recipe

1. Create new file in `IllyriaCore/src/recipes/YourRecipe.kt`
2. Implement `RecipeInterface` as an `object`
3. Define `recipes` list for crafting/smelting recipes, or `potions` list for brewing recipes
4. Use naming pattern `{descriptive_name}_{recipe_type}` for `NamespacedKey`
5. In `IllyriaPlus.kt`, add `YourRecipe` to the recipe list in `onEnable()`
6. Add KDoc comments describing the recipe

### Adding a PDC (Persistent Data Container)

1. For player data: edit `IllyriaCore/src/pdcs/PlayerPDC.kt`
2. For item data: edit `IllyriaCore/src/pdcs/ItemPDC.kt`
3. Add a new property delegate using `by` with `namespacedKey()`
4. Use primitive types or custom serializers for complex data
5. Access via `player.mana`, `item.customData`, etc. directly in code
6. Document the new PDC field in `ARCHITECTURE.md` if significant

### Adding an Interface

1. Create new file in `IllyriaCore/src/interfaces/YourInterface.kt`
2. Design interface with common methods for the feature category
3. Keep interfaces minimal and focused on one responsibility
4. Document the interface purpose in `ARCHITECTURE.md`

### Adding Data Classes

1. Create new file in `IllyriaCore/src/data/YourData.kt`
2. Define `data class` with properties for structured data
3. Keep data classes immutable (`val` properties)
4. Add appropriate helper methods or companion object factory functions
5. Document if used across multiple mechanics

### Adding Utilities

1. For general: edit `IllyriaCore/src/utils/Utils.kt` or create `IllyriaCore/src/utils/YourUtils.kt`
2. Keep utility functions `internal` visibility
3. Prefer extension functions on existing types
4. Use `Utils.MM` for MiniMessage formatting
5. Add Javadoc comments for complex utilities

## Memory System

This project uses Claude Code's persistent memory in `.claude/memory/`. These files persist across sessions and different PCs. Review `MEMORY.md` for existing context about the user and project.
