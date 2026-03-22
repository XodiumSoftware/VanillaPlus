# Sit

Allows players to sit on **stairs** and **slabs** by right-clicking them.

## Config

| Key         | Type      | Default | Description                   |
|-------------|-----------|---------|-------------------------------|
| `Enabled`   | `Boolean` | `false` | Enable this module            |
| `UseStairs` | `Boolean` | `true`  | Allow sitting on stair blocks |
| `UseSlabs`  | `Boolean` | `true`  | Allow sitting on slab blocks  |

## Behaviour

- Right-clicking a stair or slab in **survival mode** with nothing in hand mounts the player on an invisible, damage-immune ArmorStand.
- The ArmorStand is automatically removed when:
    - The player dismounts (they are repositioned above the block).
    - The player disconnects.
    - The player takes damage.
    - The block is broken.
