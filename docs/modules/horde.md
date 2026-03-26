# Horde

Spawns a structured medieval horde formation at a player's location, featuring ranked enemy unit types, mounted elite commanders, and a boss bar for the Warlord.

## Commands & Permissions

| Command  | Permission          | Default | Description                               |
|----------|---------------------|---------|-------------------------------------------|
| `/horde` | `vanillaplus.horde` | OP      | Spawns a horde formation at your location |

## Behaviour

### Formation layout

Calling `/horde` spawns five rows of enemies centered on the player, extending away from them along the Z axis:

| Row | Z offset | Unit        | Count | Notes                             |
|-----|----------|-------------|-------|-----------------------------------|
| 1   | +0       | Goblin      | 6     | Front line, spaced 4 apart        |
| 2   | +8       | Orc         | 4     | Second line, spaced 5 apart       |
| 3   | +18      | Troll       | 2     | Heavy support, spaced 10 apart    |
| 4   | +28      | Dark Knight | 2     | Mounted skeletons, spaced 8 apart |
| 5   | +36      | Warlord     | 1     | Boss, mounted zombie at the rear  |

### Unit stats

| Unit        | Base type | Health                  | Scale | Armour                                                 | Weapon                            |
|-------------|-----------|-------------------------|-------|--------------------------------------------------------|-----------------------------------|
| Goblin      | Zombie    | 25                      | 0.75× | Full iron                                              | Bow or Crossbow (random)          |
| Orc         | Zombie    | 40                      | 1.5×  | Full iron + skull shield                               | Iron Spear                        |
| Troll       | Zombie    | 100                     | 3.0×  | Full chainmail                                         | Mace                              |
| Dark Knight | Skeleton  | 50                      | 1.0×  | Full netherite + skull shield                          | Netherite Sword or Spear (random) |
| Warlord     | Zombie    | 300 max (spawns at 150) | 1.25× | Netherite + enchanted pieces + glinting pumpkin helmet | Mace (Density IV, Fire Aspect II) |

All units are persistent and drop no equipment.

### Mounts

- **Dark Knight** rides a black netherite-armored Horse (25 HP).
- **Warlord** rides a white netherite-armored Horse (150 HP, 1.25× scale).

### Boss bar

When the Warlord spawns, a red boss bar labelled **Warlord** is shown to all online players. The bar tracks the Warlord's current health in real time and is removed when the Warlord dies. Players who join mid-fight are automatically added to the boss bar.

### Formation movement

Once spawned, the entire formation marches together. Every 10 ticks (0.5 s) the Warlord's pathfinder is pointed at the nearest player, and every other unit's pathfinder is pointed at their fixed offset position relative to the Warlord. This keeps the rows intact as the formation advances. Units still engage in melee combat when a player enters their attack range — the formation movement does not suppress vanilla combat AI.

The movement task is cancelled automatically when the Warlord dies.

### Shield

Orcs, Dark Knights, and the Warlord carry a custom shield: black base colour with a red skull banner pattern.
