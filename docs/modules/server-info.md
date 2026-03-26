# Server Info

Registers server links that appear in the Minecraft client's pause menu (1.21+).

## Defaults

| Field         | Type                            | Description             |
|---------------|---------------------------------|-------------------------|
| `serverLinks` | `Map<ServerLinks.Type, String>` | Map of link type to URL |

See [ServerLinks.Type](https://jd.papermc.io/paper/1.21.11/org/bukkit/ServerLinks.Type.html) for valid keys.

## Behaviour

- Links are registered at plugin startup.
- Invalid URLs are silently skipped.
