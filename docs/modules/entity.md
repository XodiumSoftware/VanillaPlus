# Entity

Per-mob grief prevention, random spawn egg drops from entity deaths, and horse armor enchanting via anvil.

## Defaults

| Field                 | Default | Description                                           |
|-----------------------|---------|-------------------------------------------------------|
| `entityEggDropChance` | `0.001` | Chance (0–1) for a mob to drop its spawn egg on death |

## Behaviour

### Grief prevention

Grief cancellation is always active for: Blaze, Creeper, Ender Dragon, Enderman, Fireball (Ghast/WitherSkull), and Wither. To change which types are affected, edit `Config.griefCancelTypes` in the source.

### Spawn egg drops

On entity death, there is a configurable chance (default 0.1%) that the mob drops its own spawn egg. This works for all mob types that have a corresponding spawn egg.

### Horse armor enchanting

Horse armor (all variants including netherite) can be enchanted via an anvil using enchanted books — something not possible in vanilla survival. The following enchantments are supported:

| Enchantment           | Effect                       |
|-----------------------|------------------------------|
| Protection            | Reduces all damage           |
| Fire Protection       | Reduces fire damage          |
| Blast Protection      | Reduces explosion damage     |
| Projectile Protection | Reduces projectile damage    |
| Thorns                | Reflects damage to attackers |
| Mending               | Repairs with XP              |
| Unbreaking            | Increases durability         |
| Binding Curse         | Prevents removal             |
| Vanishing Curse       | Destroys on death            |

Anvil cost equals the sum of all applied enchantment levels. Conflicting enchantments and books with lower levels than already present are skipped. To change the allowed set, edit `Config.horseArmorEnchants` in the source.
