# Enchantments

VanillaPlus adds **7 custom enchantments** and **extends 2 vanilla enchantments**. All are obtainable through the enchanting table, anvil and villager trades (tradeable, non-treasure).

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

| Property      | Value                 |
|---------------|-----------------------|
| **Slot**      | Mainhand (Blaze Rods) |
| **Levels**    | I                     |
| **Mana cost** | 10                    |

Left-clicking with an enchanted Blaze Rod fires a small fireball in the direction you are looking. Fireballs deal no block damage (`yield = 0`) and leave a trail of flame and lava particles. Requires mana; displays the shared **Spellbite** mana bar on cast. Only usable in Survival or Adventure mode.

---

### Frostbind

> Launches an ice bolt on left-click with Packed Ice that freezes nearby targets on impact.

| Property          | Value                 |
|-------------------|-----------------------|
| **Slot**          | Mainhand (Blaze Rods) |
| **Levels**        | I                     |
| **Mana cost**     | 10                    |
| **Freeze radius** | 3 blocks              |

Left-clicking with a Frostbind Blaze Rod fires a snowball in the direction you are looking. On impact, all living entities within 3 blocks (excluding the caster) are fully frozen for the maximum freeze duration. Leaves a snowflake particle trail and bursts on hit. Draws from the same shared mana pool as Inferno; displays the **Spellbite** mana bar on cast. Only usable in Survival or Adventure mode.

---

## Extended vanilla enchantments

These enchantments already exist in vanilla Minecraft. VanillaPlus adds extra behaviour on top of their normal effects. The extended behaviour is togglable in [PlayerModule config](modules/player.md).

### Silk Touch (extended)

In addition to its normal behaviour, Silk Touch on a pickaxe allows collecting:

- **Spawners** — drops as a Spawner item + the mob's spawn egg (configurable)
- **Budding Amethyst** — drops as a Budding Amethyst block (configurable)

### Feather Falling (extended)

In addition to its normal fall-damage reduction, Feather Falling on boots **prevents farmland trampling** while worn.
