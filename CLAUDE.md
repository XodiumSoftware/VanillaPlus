# VanillaPlus — Claude Code Context

## Project at a Glance

- **Name:** VanillaPlus
- **Type:** Minecraft Paper plugin (server-side only)
- **MC Version:** 1.21.11
- **Language:** Kotlin (JVM 21)
- **Build Tool:** Gradle with Kotlin DSL
- **Output:** Shadow JAR to `build/libs/`

## Quick Commands

```bash
# Build the plugin JAR
./gradlew shadowJar

# Run a local test server (auto-downloads Paper 1.21.11)
./gradlew runServer
```

## Architecture Overview

### Entry Points

1. **VanillaPlusBootstrap** (`PluginBootstrap`) — Runs before plugin enable. Registers 11 custom enchantments into Paper's registry and creates item tags.
2. **VanillaPlus** (`JavaPlugin`) — Main class. Validates server version, registers recipes, and enables all modules.

### Module System

- All features are Kotlin `object` singletons implementing `ModuleInterface` (extends Bukkit `Listener`)
- Modules self-register in `VanillaPlus.onEnable()` via `register()`
- Each module has a nested `object Config` with **hardcoded defaults** — no file-based config system
- To disable a module at compile time, override `enabled` to `false` in the module object

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

**Mana System:**
- 7 Blaze Rod spell enchantments share one mana pool stored in `PlayerPDC.mana`
- All spells are **compatible** — can combine multiple on one wand
- **Left-click:** Cast selected spell
- **Right-click:** Cycle spells (shows in action bar)
- Bossbar display uses Spellbite gradient (`#832466 → #BF4299`)

### Project Structure

```
src/
├── modules/          # 14 feature modules (all `object` singletons)
├── enchantments/       # 14 enchantment implementations
├── interfaces/         # ModuleInterface, EnchantmentInterface, RecipeInterface
├── managers/           # ManaManager, PlayerMessageManager, SpellManager
├── pdcs/               # PlayerPDC, ItemPDC (Kotlin property delegates)
├── recipes/            # Recipe implementations
├── data/               # Data classes (CommandData, BookData, etc.)
└── utils/              # Utils, CommandUtils, BlockUtils, PlayerUtils, ScheduleUtils
```

### Key Conventions

- All internal classes use `internal` visibility
- All modules are `object` singletons
- Use MiniMessage (`Utils.MM`) for all text formatting
- Add `@Suppress("UnstableApiUsage")` when using Paper's experimental APIs
- ktlint is enforced; suppress wildcard imports per-file with `@file:Suppress("ktlint:standard:no-wildcard-imports")` if needed
- Recipe `NamespacedKey` naming: `{descriptive_name}_{recipe_type}`

## Testing

- No automated tests in this project
- Test by running `./gradlew runServer` and manually verifying in-game

## Important Notes

- No file-based configuration — all settings are compile-time constants in module `Config` objects
- Enchantments must be registered in `VanillaPlusBootstrap` AND tagged as tradeable/non-treasure/enchanting-table
- Spell enchantments use `PlayerPDC.mana` for shared mana pool
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
   - Add/remove enchantments, modules, recipes, or managers
   - Change the module system or interfaces
   - Modify the mana system or spell mechanics
   - Change project structure or conventions

2. **docs/** — Update if you:
   - Add/remove features that affect user-facing behavior
   - Change enchantment functionality
   - Modify command usage or module behavior
   - MkDocs source files are in `docs/`; run `mkdocs serve` to preview locally
   - **When adding/removing docs pages or changing the structure, also update `mkdocs.yml`** (nav section)

**Rule of thumb:** If a code change would confuse someone reading the docs, update the docs.

## CI/CD

GitHub Actions workflows in `.github/workflows/`:

- **build.yml** — Builds shadow JAR on push/PR, uploads artifact
- **docs.yml** — Deploys MkDocs to GitHub Pages on pushes to main
- **enforce_pr_title.yml** — Validates PR titles follow conventional commits

## Adding a New Enchantment

To add a new enchantment, follow these steps:

1. Create new file in `src/enchantments/YournameEnchantment.kt`
2. Implement `EnchantmentInterface` as an `object`
3. In `invoke(builder)`, configure: `description()`, `supportedItems()`, `anvilCost()`, `maxLevel()`, `weight()`, `slotGroup()`
4. In `VanillaPlusBootstrap.kt`:
   - Add `YournameEnchantment` to the `ENCHANTMENTS` list
   - Add it to the tags (tradeable, non-treasure, enchanting-table)
   - Add supported items to appropriate `ItemTag` if needed
5. If it's a spell (Blaze Rod enchantment):
   - Register in `SpellManager`
   - Store cost in `PlayerPDC` constants
   - Add to enchantment compatibility group
   - Implement `@EventHandler` for `PlayerInteractEvent` or projectile logic
6. Update `ARCHITECTURE.md` enchantment table
7. Update `docs/enchantments.md` user documentation
8. Update `mkdocs.yml` if needed

## Adding Other Components

### Adding a Module

1. Create new file in `src/modules/YourModule.kt`
2. Implement `ModuleInterface` as an `object`
3. Override `Config` object with settings as compile-time constants
4. Implement `@EventHandler` methods for events
5. Register commands/permissions in `register()` if needed
6. In `VanillaPlus.kt`, add `YourModule` to the module list in `onEnable()`
7. Add docs page at `docs/modules/yourmodule.md` and update `mkdocs.yml` nav
8. Update `ARCHITECTURE.md` module count

### Adding a Recipe

1. Create new file in `src/recipes/YourRecipe.kt`
2. Implement `RecipeInterface` as an `object`
3. Define `recipes` list with recipe definitions
4. Use naming pattern `{descriptive_name}_{recipe_type}` for `NamespacedKey`
5. In `VanillaPlus.kt`, add `YourRecipe` to the recipe list in `onEnable()`
6. Update `docs/recipes.md` with the new recipe

### Adding a PDC (Persistent Data Container)

1. For player data: edit `src/pdcs/PlayerPDC.kt`
2. For item data: edit `src/pdcs/ItemPDC.kt`
3. Add a new property delegate using `by` with `namespacedKey()`
4. Use primitive types or custom serializers for complex data
5. Access via `player.mana`, `item.customData`, etc. directly in code
6. Document the new PDC field in `ARCHITECTURE.md` if significant

### Adding an Interface

1. Create new file in `src/interfaces/YourInterface.kt`
2. Design interface with common methods for the feature category
3. Keep interfaces minimal and focused on one responsibility
4. Document the interface purpose in `ARCHITECTURE.md`

### Adding Data Classes

1. Create new file in `src/data/YourData.kt`
2. Define `data class` with properties for structured data
3. Keep data classes immutable (`val` properties)
4. Add appropriate helper methods or companion object factory functions
5. Document if used across multiple modules

### Adding Utilities

1. For general: edit `src/utils/Utils.kt` or create `src/utils/YourUtils.kt`
2. Keep utility functions `internal` visibility
3. Prefer extension functions on existing types
4. Use `Utils.MM` for MiniMessage formatting
5. Add Javadoc comments for complex utilities

## Memory System

This project uses Claude Code's persistent memory in `.claude/memory/`. These files persist across sessions and different PCs. Review `MEMORY.md` for existing context about the user and project.
