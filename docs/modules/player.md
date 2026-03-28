# Player

The most feature-rich module. Handles nicknames, skull drops, XP bottles, custom join/quit/death messages, enchantment event routing, and tab list formatting.

## Defaults

| Field                                           | Default | Description                                       |
|-------------------------------------------------|---------|---------------------------------------------------|
| `skullDropChance`                               | `0.01`  | Chance (0–1) a player drops their head on death   |
| `xpCostToBottle`                                | `11`    | XP levels consumed when bottling XP               |
| `SilkTouchEnchantment.allowSpawnerSilk`         | `true`  | Allow collecting spawners with Silk Touch         |
| `SilkTouchEnchantment.allowBuddingAmethystSilk` | `true`  | Allow collecting budding amethyst with Silk Touch |

## Commands & Permissions

| Command            | Aliases | Permission             | Default  | Description                                                      |
|--------------------|---------|------------------------|----------|------------------------------------------------------------------|
| `/nickname [name]` | `nick`  | `vanillaplus.nickname` | Everyone | Set or remove your display nickname. Omit the argument to clear. |

## Behaviour

### Skull drops

Players have a configurable chance to drop their player head when killed by another player.

### XP bottles

Shift-clicking an **enchanting table** while holding a **glass bottle** in your off-hand converts XP levels into an XP bottle. Costs `XpCostToBottle` levels per bottle.

### Vanilla enchantment extensions

Handled through this module's event handlers (requires [Enchantments](../enchantments.md) to be registered):

- **Silk Touch** — collecting spawners and budding amethyst (toggleable)
- **Feather Falling** — prevents farmland trampling
- **Earthrend** — vein mining
- **Tether** — auto-pickup drops
- **Verdance** — auto-replant crops
- **Embertread** — magma/campfire protection

### Messages

All join, quit, death, kick, spawn-set, advancement, and bed-enter messages are defined in `Config.PlayerMessages` and related nested objects. All strings support [MiniMessage](https://docs.advntr.dev/minimessage/) and the following placeholders:

| Placeholder      | Available in            |
|------------------|-------------------------|
| `<player>`       | Join, quit, death, kick |
| `<killer>`       | PvP death               |
| `<cause>`        | Non-PvP death           |
| `<reason>`       | Kick                    |
| `<notification>` | Spawn set               |
| `<nickname>`     | Nickname updated        |
