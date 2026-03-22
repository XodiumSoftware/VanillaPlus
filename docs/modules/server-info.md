# Server Info

Registers server links that appear in the Minecraft client's pause menu (1.21+).

## Config

| Key           | Type                            | Default                                                                                  | Description             |
|---------------|---------------------------------|------------------------------------------------------------------------------------------|-------------------------|
| `Enabled`     | `Boolean`                       | `false`                                                                                  | Enable this module      |
| `ServerLinks` | `Map<ServerLinks.Type, String>` | [ServerLinks.Type](https://jd.papermc.io/paper/1.21.11/org/bukkit/ServerLinks.Type.html) | Map of link type to URL |

## Behaviour

- Links are registered at plugin startup and updated on `/vp reload`.
- Invalid URLs are silently skipped.
