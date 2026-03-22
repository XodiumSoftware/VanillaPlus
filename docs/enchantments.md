# Enchantments

VanillaPlus adds **6 custom enchantments** and **extends 2 vanilla enchantments**. All are obtainable through the enchanting table, anvil and villager trades (tradeable, non-treasure).

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

!!! tip "Synergy"
Combine with **Tether** to automatically collect all vein-mined drops directly into your inventory.

---

### Embertread

> Prevents damage from magma blocks and campfires.

| Property   | Value        |
|------------|--------------|
| **Slot**   | Feet (Boots) |
| **Levels** | I            |

While wearing boots enchanted with Embertread, walking on magma blocks and standing next to campfires deal no damage.

---

### Nightsight

> Grants permanent Night Vision while worn.

| Property   | Value          |
|------------|----------------|
| **Slot**   | Head (Helmets) |
| **Levels** | I              |

Applying or removing the helmet immediately adds or removes the Night Vision effect. The effect has no visible particles.

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

!!! tip "Synergy"
Combine with **Earthrend** to collect entire ore veins without running around to pick up drops.

---

### Verdance

> Automatically replants fully grown crops after a harvest.

| Property   | Value           |
|------------|-----------------|
| **Slot**   | Mainhand (Hoes) |
| **Levels** | I               |

When a fully grown crop is harvested with a Verdance hoe, the crop block is reset to age 0 two ticks later — effectively auto-replanting without consuming seeds.

---

## Extended vanilla enchantments

These enchantments already exist in vanilla Minecraft. VanillaPlus adds extra behaviour on top of their normal effects. The extended behaviour is togglable in [PlayerModule config](modules/player.md).

### Silk Touch (extended)

In addition to its normal behaviour, Silk Touch on a pickaxe allows collecting:

- **Spawners** — drops as a Spawner item + the mob's spawn egg (configurable)
- **Budding Amethyst** — drops as a Budding Amethyst block (configurable)

### Feather Falling (extended)

In addition to its normal fall-damage reduction, Feather Falling on boots **prevents farmland trampling** while worn.
