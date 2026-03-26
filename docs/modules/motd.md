# MOTD

Sets a custom server list MOTD (the two lines shown below the server name in the multiplayer screen).

## Defaults

| Field  | Default   | Description                                                |
|--------|-----------|------------------------------------------------------------|
| `motd` | See above | Up to 2 lines of MiniMessage text shown in the server list |

All strings support [MiniMessage](https://docs.advntr.dev/minimessage/) formatting including gradients, colours, and bold/italic.

## Behaviour

- Intercepts `ServerListPingEvent` and replaces the default MOTD with the configured lines.
- Line 1 and line 2 map directly to the two MOTD lines in the vanilla server list UI.
