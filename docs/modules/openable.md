# Openable

Adds two door interactions: **double doors** open in sync, and players can **knock** on doors.

## Config

| Key                         | Type        | Default                            | Description                                                                    |
|-----------------------------|-------------|------------------------------------|--------------------------------------------------------------------------------|
| `Enabled`                   | `Boolean`   | `false`                            | Enable this module                                                             |
| `InitDelayInTicks`          | `Long`      | `1`                                | Delay before toggling the adjacent door (prevents instant double-click issues) |
| `AllowDoubleDoors`          | `Boolean`   | `true`                             | Open/close both halves of a double door together                               |
| `AllowKnocking`             | `Boolean`   | `true`                             | Enable the knocking interaction                                                |
| `AllowIronDoorByHand`       | `Boolean`   | `false`                            | Allow opening iron doors by right-clicking (no button/lever required)          |
| `KnockingRequiresEmptyHand` | `Boolean`   | `true`                             | Player must have an empty hand to knock                                        |
| `KnockingRequiresShifting`  | `Boolean`   | `true`                             | Player must be sneaking to knock                                               |
| `SoundKnock`                | `SoundData` | `entity.zombie.attack_wooden_door` | Sound played when knocking                                                     |
| `SoundProximityRadius`      | `Double`    | `10.0`                             | Radius (blocks) in which the knock sound is audible                            |

## Behaviour

### Double doors

Right-clicking one half of a side-by-side door pair opens or closes both halves simultaneously with a 1-tick delay.

### Knocking

Left-clicking a door (optionally with empty hand and/or while sneaking) plays the knock sound to all players within `SoundProximityRadius` blocks.
