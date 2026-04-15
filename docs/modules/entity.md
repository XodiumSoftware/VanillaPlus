# Entity

Per-mob grief prevention and random spawn egg drops from entity deaths.

## Defaults

| Field                 | Default | Description                                           |
|-----------------------|---------|-------------------------------------------------------|
| `entityEggDropChance` | `0.001` | Chance (0–1) for a mob to drop its spawn egg on death |

## Behaviour

### Grief prevention

Grief cancellation is always active for: Blaze, Creeper, Ender Dragon, Enderman, Fireball (Ghast/WitherSkull), and Wither. To change, which types are affected, edit `Config.griefCancelTypes` in the source.

### Spawn egg drops

On entity death, there is a configurable chance (default 0.1%) that the mob drops its own spawn egg. This works for all mob types that have a corresponding spawn egg.
