# Getting Started

## Installation

1. Download the latest `VanillaPlus-*.jar` from [GitHub Releases](https://github.com/XodiumSoftware/VanillaPlus/releases) or [Modrinth](https://modrinth.com/plugin/vanillaplus).
2. Drop it into your server's `plugins/` folder.
3. Start (or restart) the server.
4. A `config.json` is generated in `plugins/VanillaPlus/`.

## Enabling modules

All modules are **disabled by default**. Open `config.json` and set `Enabled: true` for any module you want to activate, then reload:

```
/vp reload
```

!!! tip
You can reload the plugin at any time with `/vp reload` (requires `vanillaplus.reload`, OP by default). No restart needed for config changes.

## Example: enabling double doors

```json
{
    "OpenableModule": {
        "Enabled": true,
        "AllowDoubleDoors": true,
        "AllowKnocking": true
    }
}
```

## Config format

- The config file uses **PascalCase** JSON keys.
- Each module has its own top-level object keyed by its class name (e.g. `ChatModule`, `EntityModule`).
- Boolean flags, numbers, strings, lists, and nested objects are all supported.

See the full [Configuration reference](configuration.md) for all keys and defaults.
