# Enchantments

VanillaPlus adds **12 custom enchantments** and **extends 2 vanilla enchantments**. All are obtainable through the enchanting table, anvil and villager trades (tradeable, non-treasure).

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

---

## Spell System (Blaze Rod Spells)

Multiple spell enchantments can now be combined on a single **Blaze Rod** (wand). Players can cycle through available spells and cast them using the wand.

### Controls

| Action          | Effect                                                       |
|-----------------|--------------------------------------------------------------|
| **Left-click**  | Cast the currently selected spell                            |
| **Right-click** | Cycle to the next spell (shows selected spell in action bar) |

### Spell List

#### Inferno

> Launches a fireball.

| Property      | Value     |
|---------------|-----------|
| **Slot**      | Blaze Rod |
| **Mana cost** | 10        |

Fires a small fireball in the direction you are looking. Fireballs deal no block damage (`yield = 0`) and leave a trail of flame and lava particles.

---

#### Skysunder

> Calls down a lightning bolt.

| Property      | Value     |
|---------------|-----------|
| **Slot**      | Blaze Rod |
| **Mana cost** | 20        |
| **Range**     | 30 blocks |

Ray-traces up to 30 blocks and strikes the target with lightning. Bursts `ELECTRIC_SPARK` particles at the strike location.

---

#### Voidpull

> Shoots an enderpearl that pulls entities to you.

| Property      | Value     |
|---------------|-----------|
| **Slot**      | Blaze Rod |
| **Mana cost** | 20        |

Fires an ender pearl (no gravity) that teleports struck entities directly in front of you. Portal particles burst at both origin and arrival locations.

---

#### Quake

> Creates a shockwave that damages and knocks back nearby entities.

| Property      | Value     |
|---------------|-----------|
| **Slot**      | Blaze Rod |
| **Mana cost** | 20        |
| **Radius**    | 4 blocks  |
| **Damage**    | 6 HP      |

Creates an expanding shockwave from your position. Nearby living entities take damage and are knocked back with a slight upward arc.

---

#### Frostbind

> Launches a freezing snowball.

| Property      | Value     |
|---------------|-----------|
| **Slot**      | Blaze Rod |
| **Mana cost** | 15        |

Fires a snowball (no gravity) that freezes struck entities for several seconds. Trails snowflake and snowball particles.

---

#### Tempest

> Launches a burst of wind charges.

| Property      | Value     |
|---------------|-----------|
| **Slot**      | Blaze Rod |
| **Mana cost** | 25        |

Fires three wind charges in a horizontal spread. Each charge trails gust and cloud particles and knocks back on explosion.

---

#### Witherbrand

> Launches a wither skull.

| Property      | Value     |
|---------------|-----------|
| **Slot**      | Blaze Rod |
| **Mana cost** | 15        |

Fires an uncharged wither skull that applies the Wither effect on hit. Leaves a trail of soul and ash particles.

---

## Mana Item

To support extended spellcasting, VanillaPlus adds **Mana Items** that instantly restore your mana pool. Available in four variants:

### Potion of Arcane Restoration

> Instantly refills your mana to maximum.

| Property       | Value                            |
|----------------|----------------------------------|
| **Effect**     | Refills mana to 100              |
| **Color**      | Purple (`#832466`)               |
| **Brewing**    | Awkward Potion + Blaze Rod       |

### Splash Potion of Arcane Restoration

| Property       | Value                            |
|----------------|----------------------------------|
| **Effect**     | Refills mana to 100 (area)       |
| **Brewing**    | Splash Potion + Blaze Rod        |

### Lingering Potion of Arcane Restoration

| Property       | Value                            |
|----------------|----------------------------------|
| **Effect**     | Refills mana to 100 (area)       |
| **Brewing**    | Lingering Potion + Blaze Rod     |

### Arrow of Arcane Restoration

| Property       | Value                                         |
|----------------|-----------------------------------------------|
| **Effect**     | Refills mana to 100 (on hit)                  |
| **Crafting**   | 8 Arrows + 1 Lingering Potion = 8 Tipped Arrows |

When consumed (or hit by a tipped arrow), your mana bar is immediately filled to maximum and displayed. This allows you to continue casting spells without waiting for natural regeneration.

> **Tip:** Carry multiple mana items during extended combat or mining sessions with spell wands.

---

## Extended vanilla enchantments

These enchantments already exist in vanilla Minecraft. VanillaPlus adds extra behaviour on top of their normal effects. The extended behaviour is togglable in [PlayerModule config](modules/player.md).

### Silk Touch (extended)

In addition to its normal behaviour, Silk Touch on a pickaxe allows collecting:

- **Spawners** — drops as a Spawner item + the mob's spawn egg (configurable)
- **Budding Amethyst** — drops as a Budding Amethyst block (configurable)

### Feather Falling (extended)

In addition to its normal fall-damage reduction, Feather Falling on boots **prevents farmland trampling** while worn.
