# Enchantments

VanillaPlus adds **8 custom enchantments** and **extends 2 vanilla enchantments**. All are obtainable through the enchanting table, anvil and villager trades (tradeable, non-treasure).

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

| Property           | Value                 |
|--------------------|-----------------------|
| **Slot**           | Mainhand (Blaze Rods) |
| **Levels**         | I                     |
| **Mana cost**      | 10                    |
| **Exclusive with** | Skysunder             |

Left-clicking with an enchanted Blaze Rod fires a small fireball in the direction you are looking. Fireballs deal no block damage (`yield = 0`) and leave a trail of flame and lava particles. Requires mana; displays the shared **Spellbite** mana bar on cast. Only usable in Survival or Adventure mode.

---

### Skysunder

> Calls down a lightning bolt on left-click with a Blaze Rod.

| Property           | Value                 |
|--------------------|-----------------------|
| **Slot**           | Mainhand (Blaze Rods) |
| **Levels**         | I                     |
| **Mana cost**      | 20                    |
| **Range**          | 30 blocks             |
| **Exclusive with** | Inferno               |

Left-clicking with a Skysunder Blaze Rod ray-traces up to 30 blocks in the direction you are looking. A real lightning bolt (dealing damage) strikes the first block hit, or the maximum range point if no block is found. Bursts `ELECTRIC_SPARK` particles at the strike location. Draws from the shared **Spellbite** mana pool (costs twice as much as Inferno). Only usable in Survival or Adventure mode.

---

### Witherbrand

> Launches a wither skull on left-click with a Blaze Rod.

| Property           | Value                 |
|--------------------|-----------------------|
| **Slot**           | Mainhand (Blaze Rods) |
| **Levels**         | I                     |
| **Mana cost**      | 15                    |
| **Exclusive with** | Inferno, Skysunder    |

Left-clicking with a Witherbrand Blaze Rod fires an uncharged wither skull in the direction you are looking. The skull applies the Wither effect on hit and leaves a trail of soul and ash particles. Requires mana; draws from the shared **Spellbite** mana pool. Only usable in Survival or Adventure mode.

---

## Extended vanilla enchantments

These enchantments already exist in vanilla Minecraft. VanillaPlus adds extra behaviour on top of their normal effects. The extended behaviour is togglable in [PlayerModule config](modules/player.md).

### Silk Touch (extended)

In addition to its normal behaviour, Silk Touch on a pickaxe allows collecting:

- **Spawners** — drops as a Spawner item + the mob's spawn egg (configurable)
- **Budding Amethyst** — drops as a Budding Amethyst block (configurable)

### Feather Falling (extended)

In addition to its normal fall-damage reduction, Feather Falling on boots **prevents farmland trampling** while worn.
