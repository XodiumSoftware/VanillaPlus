# Inventory

Two commands for interacting with nearby containers: search for items and unload your inventory.

## Config

| Key                     | Type        | Default                 | Description                |
|-------------------------|-------------|-------------------------|----------------------------|
| `Enabled`               | `Boolean`   | `false`                 | Enable this module         |
| `SearchSuccessfulSound` | `SoundData` | `entity.player.levelup` | Sound on successful search |
| `SearchFailedSound`     | `SoundData` | `block.anvil.land`      | Sound on failed search     |
| `UnloadSuccessfulSound` | `SoundData` | `entity.player.levelup` | Sound on successful unload |
| `UnloadFailedSound`     | `SoundData` | `block.anvil.land`      | Sound on failed unload     |

## Commands & Permissions

| Command              | Aliases                                       | Permission              | Default  | Description                                                                           |
|----------------------|-----------------------------------------------|-------------------------|----------|---------------------------------------------------------------------------------------|
| `/search [material]` | `invsearch`, `searchinv`, `invs`, `sinv`, `s` | `vanillaplus.invsearch` | Everyone | Search nearby containers for an item. Omit the argument to use the item in your hand. |
| `/unload`            | `invunload`, `unloadinv`, `invu`, `uinv`, `u` | `vanillaplus.invunload` | Everyone | Unload your inventory into matching nearby containers.                                |

## Behaviour

- **Search**: Draws a particle trail from you to every nearby container that holds the specified item.
- **Unload**: Moves items from your inventory into nearby containers that already contain the same item type.
- "Nearby" is determined by the server's container search range.
