# Openable

Adds two door interactions: **double doors** open in sync, and players can **knock** on doors.

## Defaults

| Field                       | Default                            | Description                                                                    |
|-----------------------------|------------------------------------|--------------------------------------------------------------------------------|
| `initDelayInTicks`          | `1`                                | Delay before toggling the adjacent door (prevents instant double-click issues) |
| `allowDoubleDoors`          | `true`                             | Open/close both halves of a double door together                               |
| `allowKnocking`             | `true`                             | Enable the knocking interaction                                                |
| `knockingRequiresEmptyHand` | `true`                             | Player must have an empty hand to knock                                        |
| `knockingRequiresShifting`  | `true`                             | Player must be sneaking to knock                                               |
| `soundKnock`                | `entity.zombie.attack_wooden_door` | Sound played when knocking                                                     |
| `soundProximityRadius`      | `10.0`                             | Radius (blocks) in which the knock sound is audible                            |

## Behaviour

### Double doors

Right-clicking one half of a side-by-side door pair opens or closes both halves simultaneously with a 1-tick delay.

### Knocking

Left-clicking a door (optionally with empty hand and/or while sneaking) plays the knock sound to all players within `SoundProximityRadius` blocks.
