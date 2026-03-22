# Dimensions

Prevents Nether portal grief by blocking new portal creation from the Nether side unless a corresponding Overworld portal already exists.

## Config

| Key                         | Type      | Default   | Description                                            |
|-----------------------------|-----------|-----------|--------------------------------------------------------|
| `Enabled`                   | `Boolean` | `false`   | Enable this module                                     |
| `PortalSearchRadius`        | `Int`     | `128`     | Block radius searched for an existing Overworld portal |
| `I18n.PortalCreationDenied` | `String`  | See above | Message shown when portal creation is denied           |

## Behaviour

- When a player or entity enters a Nether portal from the Nether, the plugin checks for an existing Overworld portal within `PortalSearchRadius` blocks.
- If none is found, portal creation is **cancelled**, and the player is teleported to their spawn/respawn point instead.
- This prevents griefers from accidentally or intentionally generating new portal frames in the Overworld.
