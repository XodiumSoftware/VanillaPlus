# Enchantments

VanillaPlus adds **9 custom enchantments** and **extends 2 vanilla enchantments**. All are obtainable through the enchanting table, anvil and villager trades (tradeable, non-treasure).

---

## Custom enchantments

### Earthrend

> Vein-mines connected ores of the same type.

| Property       | Value                                |
|----------------|--------------------------------------|
| **Slot**       | Mainhand (Pickaxes)                  |
| **Levels**     | I – III                              |
| **Max blocks** | Lv I → 16 · Lv II → 32 · Lv III → 48 |

Breaks all connected blocks of the same ore type in a single swing. Works with all standard ores plus Ancient Debris, Nether Quartz Ore, and Nether Gold Ore.

> **Synergy:** Combine with **Tether** to automatically collect all vein-mined drops directly into your inventory.

---

### Embertread

> Prevents damage from fire and magma blocks.

| Property   | Value        |
|------------|--------------|
| **Slot**   | Feet (Boots) |
| **Levels** | I            |

While wearing boots enchanted with Embertread, walking on magma blocks or through fire and soul fire deals no damage.

---

### Nimbus

> Boosts Happy Ghast flying speed.

| Property              | Value                                                    |
|-----------------------|----------------------------------------------------------|
| **Slot**              | Saddle (Harnesses)                                       |
| **Levels**            | I – V                                                    |
| **Speed multipliers** | I → ×1.5 · II → ×2.0 · III → ×2.5 · IV → ×3.0 · V → ×3.5 |

Applies to the harness slot of a Happy Ghast. Higher levels increase the base flying speed proportionally.

---

### Tether

> Auto-picks up block drops directly into your inventory.

| Property   | Value                      |
|------------|----------------------------|
| **Slot**   | Mainhand (Tools & Weapons) |
| **Levels** | I                          |

Broken block drops are collected directly into the player's inventory instead of being dropped as item entities. Overflow that doesn't fit is still dropped normally.

> **Synergy:** Combine with **Earthrend** to collect entire ore veins without running around to pick up drops.

---

### Verdance

> Automatically replants fully grown crops after a harvest.

| Property   | Value           |
|------------|-----------------|
| **Slot**   | Mainhand (Hoes) |
| **Levels** | I               |

When a fully grown crop is harvested with a Verdance hoe, the crop block is reset to age 0 two ticks later — effectively auto-replanting without consuming seeds.

### Inferno

> Launches a fireball on left-click with a Blaze Rod.

| Property           | Value                                                           |
|--------------------|-----------------------------------------------------------------|
| **Slot**           | Mainhand (Blaze Rods)                                           |
| **Levels**         | I                                                               |
| **Mana cost**      | 10                                                              |
| **Exclusive with** | Skysunder, Witherbrand, Frostbind, Tempest, Voidpull, Bloodpact |

Left-clicking with an enchanted Blaze Rod fires a small fireball in the direction you are looking. Fireballs deal no block damage (`yield = 0`) and leave a trail of flame and lava particles. Requires mana; displays the shared **Spellbite** mana bar on cast. Only usable in Survival or Adventure mode.

---

### Skysunder

> Calls down a lightning bolt on left-click with a Blaze Rod.

| Property           | Value                                                         |
|--------------------|---------------------------------------------------------------|
| **Slot**           | Mainhand (Blaze Rods)                                         |
| **Levels**         | I                                                             |
| **Mana cost**      | 20                                                            |
| **Range**          | 30 blocks                                                     |
| **Exclusive with** | Inferno, Witherbrand, Frostbind, Tempest, Voidpull, Bloodpact |

Left-clicking with a Skysunder Blaze Rod ray-traces up to 30 blocks in the direction you are looking. A real lightning bolt (dealing damage) strikes the first block hit, or the maximum range point if no block is found. Bursts `ELECTRIC_SPARK` particles at the strike location. Draws from the shared **Spellbite** mana pool (costs twice as much as Inferno). Only usable in Survival or Adventure mode.

---

### Voidpull

> Shoots an ender pearl that pulls the struck entity to you on left-click with a Blaze Rod.

| Property           | Value                                                          |
|--------------------|----------------------------------------------------------------|
| **Slot**           | Mainhand (Blaze Rods)                                          |
| **Levels**         | I                                                              |
| **Mana cost**      | 20                                                             |
| **Exclusive with** | Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Bloodpact |

Left-clicking with a Voidpull Blaze Rod fires an ender pearl (no gravity) in the direction you are looking. While in flight the pearl trails portal and reverse-portal particles. When the pearl strikes an entity, that entity is teleported directly in front of the player. Portal particles burst at both the origin and arrival location. Requires mana; draws from the shared **Spellbite** mana pool. Only usable in Survival or Adventure mode.

---

### Frostbind

> Launches a freezing snowball on left-click with a Blaze Rod.

| Property           | Value                                                         |
|--------------------|---------------------------------------------------------------|
| **Slot**           | Mainhand (Blaze Rods)                                         |
| **Levels**         | I                                                             |
| **Mana cost**      | 15                                                            |
| **Exclusive with** | Inferno, Skysunder, Witherbrand, Tempest, Voidpull, Bloodpact |

Left-clicking with a Frostbind Blaze Rod fires a snowball (no gravity) in the direction you are looking. On hit, the struck entity is frozen solid for several seconds (set to full freeze ticks, decaying naturally outside powder snow). The snowball trails snowflake and snowball particles in flight. Bursts snowflake particles at the impact location. Requires mana; draws from the shared **Spellbite** mana pool. Only usable in Survival or Adventure mode.

---

### Tempest

> Launches a burst of wind charges on left-click with a Blaze Rod.

| Property           | Value                                                           |
|--------------------|-----------------------------------------------------------------|
| **Slot**           | Mainhand (Blaze Rods)                                           |
| **Levels**         | I                                                               |
| **Mana cost**      | 25                                                              |
| **Exclusive with** | Inferno, Skysunder, Witherbrand, Frostbind, Voidpull, Bloodpact |

Left-clicking with a Tempest Blaze Rod fires three wind charges in a horizontal spread. Each charge trails gust and cloud particles and knocks back anything it hits on explosion. Requires mana; the highest mana cost of all Blaze Rod spells. Only usable in Survival or Adventure mode.

---

### Witherbrand

> Launches a wither skull on left-click with a Blaze Rod.

| Property           | Value                                                       |
|--------------------|-------------------------------------------------------------|
| **Slot**           | Mainhand (Blaze Rods)                                       |
| **Levels**         | I                                                           |
| **Mana cost**      | 15                                                          |
| **Exclusive with** | Inferno, Skysunder, Frostbind, Tempest, Voidpull, Bloodpact |

Left-clicking with a Witherbrand Blaze Rod fires an uncharged wither skull in the direction you are looking. The skull applies the Wither effect on hit and leaves a trail of soul and ash particles. Requires mana; draws from the shared **Spellbite** mana pool. Only usable in Survival or Adventure mode.

---

### Bloodpact

> Sacrifice health to restore mana on left-click with a Blaze Rod.

| Property           | Value                                                         |
|--------------------|---------------------------------------------------------------|
| **Slot**           | Mainhand (Blaze Rods)                                         |
| **Levels**         | I                                                             |
| **Health cost**    | 2 hearts (4 HP)                                               |
| **Mana gain**      | 40                                                            |
| **Exclusive with** | Inferno, Skysunder, Witherbrand, Frostbind, Tempest, Voidpull |

Left-clicking with a Bloodpact Blaze Rod drains 2 hearts and immediately restores 40 mana. Blocked if the player's health would drop to zero or below, or if mana is already full — both play the no-mana sound as feedback. Damage indicator and crimson spore particles burst from the player on a successful cast. Unlike other Blaze Rod spells, Bloodpact produces mana rather than consuming it, making it a dedicated mana recovery tool at the cost of survivability. Only usable in Survival or Adventure mode.

---

## Extended vanilla enchantments

These enchantments already exist in vanilla Minecraft. VanillaPlus adds extra behaviour on top of their normal effects. The extended behaviour is togglable in [PlayerModule config](modules/player.md).

### Silk Touch (extended)

In addition to its normal behaviour, Silk Touch on a pickaxe allows collecting:

- **Spawners** — drops as a Spawner item + the mob's spawn egg (configurable)
- **Budding Amethyst** — drops as a Budding Amethyst block (configurable)

### Feather Falling (extended)

In addition to its normal fall-damage reduction, Feather Falling on boots **prevents farmland trampling** while worn.
