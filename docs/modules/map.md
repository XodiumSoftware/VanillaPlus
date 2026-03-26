# Map

Integrates with [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map) and [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) to share server-side player tracking data with the client-side maps.

## Defaults

| Field      | Default | Description                                                                               |
|------------|---------|-------------------------------------------------------------------------------------------|
| `serverId` | `12345` | A unique integer ID for this server, used by Xaero's maps to separate map data per server |

## Behaviour

- Sends Xaero handshake and level ID packets when a player registers the Xaero plugin channel.
- Broadcasts player position updates to all map-capable clients (~150 ms throttle).
- Hides players from the map while they are **sneaking**.
- Clears and resends tracking data on world change.
- Cleans up tracking data on player disconnect.

## Requirements

Players must have **Xaero's Minimap** or **Xaero's World Map** installed on their client for this to have any effect.
