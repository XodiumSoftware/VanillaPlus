# Entity

Per-mob grief prevention and random spawn egg drops from entity deaths.

## Defaults

| Field                     | Default | Description                                           |
|---------------------------|---------|-------------------------------------------------------|
| `disableBlazeGrief`       | `true`  | Prevent Blazes from destroying blocks                 |
| `disableCreeperGrief`     | `true`  | Prevent Creeper explosions from destroying blocks     |
| `disableEnderDragonGrief` | `true`  | Prevent the Ender Dragon from destroying blocks       |
| `disableEndermanGrief`    | `true`  | Prevent Endermen from picking up blocks               |
| `disableGhastGrief`       | `true`  | Prevent Ghast fireballs from destroying blocks        |
| `disableWitherGrief`      | `true`  | Prevent the Wither from destroying blocks             |
| `entityEggDropChance`     | `0.001` | Chance (0–1) for a mob to drop its spawn egg on death |

## Behaviour

### Grief prevention

Each grief flag can be toggled independently. When disabled, the relevant block-change or explosion event is cancelled for that mob type only.

### Spawn egg drops

On entity death, there is a configurable chance (default 0.1%) that the mob drops its own spawn egg. This works for all mob types that have a corresponding spawn egg.
