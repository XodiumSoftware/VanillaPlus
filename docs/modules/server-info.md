# Server Info

Registers server links that appear in the Minecraft client's pause menu (1.21+).

## Config

| Key           | Type                | Default   | Description             |
|---------------|---------------------|-----------|-------------------------|
| `Enabled`     | `Boolean`           | `false`   | Enable this module      |
| `ServerLinks` | `Map<Type, String>` | See above | Map of link type to URL |

### Supported link types

| Type         | Where it shows     |
|--------------|--------------------|
| `WEBSITE`    | Official site      |
| `REPORT_BUG` | Bug report link    |
| `STATUS`     | Server status page |
| `COMMUNITY`  | Discord or forum   |

## Behaviour

- Links are registered at plugin startup and updated on `/vp reload`.
- Invalid URLs are silently skipped.
