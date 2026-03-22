# Entity

Per-mob grief prevention and random spawn egg drops from entity deaths.

## Config

| Key                       | Type      | Default | Description                                           |
|---------------------------|-----------|---------|-------------------------------------------------------|
| `Enabled`                 | `Boolean` | `false` | Enable this module                                    |
| `DisableBlazeGrief`       | `Boolean` | `true`  | Prevent Blazes from destroying blocks                 |
| `DisableCreeperGrief`     | `Boolean` | `true`  | Prevent Creeper explosions from destroying blocks     |
| `DisableEnderDragonGrief` | `Boolean` | `true`  | Prevent the Ender Dragon from destroying blocks       |
| `DisableEndermanGrief`    | `Boolean` | `true`  | Prevent Endermen from picking up blocks               |
| `DisableGhastGrief`       | `Boolean` | `true`  | Prevent Ghast fireballs from destroying blocks        |
| `DisableWitherGrief`      | `Boolean` | `true`  | Prevent the Wither from destroying blocks             |
| `EntityEggDropChance`     | `Double`  | `0.001` | Chance (0–1) for a mob to drop its spawn egg on death |

## Behaviour

### Grief prevention

Each grief flag can be toggled independently. When disabled, the relevant block-change or explosion event is cancelled for that mob type only.

### Spawn egg drops

On entity death, there is a configurable chance (default 0.1%) that the mob drops its own spawn egg. This works for all mob types that have a corresponding spawn egg.
