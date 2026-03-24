# Configuration

VanillaPlus uses a single `config.json` file located in `plugins/VanillaPlus/`. All keys use **PascalCase**.

## Reloading

```
/vp reload
```

Reloads the file at runtime. No server restart required. Requires the `vanillaplus.reload` permission (OP by default).

## Structure

Each module has its own top-level object in `config.json`, keyed by the module's class name:

```json
{
    "ChatModule": {
        "Enabled": true,
        ...
    },
    "EntityModule": {
        "Enabled": true,
        ...
    },
    "OpenableModule": {
        "Enabled": false,
        ...
    }
}
```

Modules that are absent from the file use their built-in defaults and remain **disabled**.

## Full default config

```json
{
    "ChatModule": {
        "Enabled": false,
        "ChatFormat": "<player_head> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>",
        "WhisperToFormat": "<gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>➛</gradient> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>",
        "WhisperFromFormat": "<player> <reset><gradient:#FFE259:#FFA751>➛</gradient> <gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>›</gradient> <message>",
        "DeleteCross": "<dark_gray>[<dark_red><b>X</b></dark_red><dark_gray>]"
    },
    "ChiseledBookshelfModule": {
        "Enabled": false
    },
    "DimensionModule": {
        "Enabled": false,
        "PortalSearchRadius": 128
    },
    "EntityModule": {
        "Enabled": false,
        "DisableBlazeGrief": true,
        "DisableCreeperGrief": true,
        "DisableEnderDragonGrief": true,
        "DisableEndermanGrief": true,
        "DisableGhastGrief": true,
        "DisableWitherGrief": true,
        "EntityEggDropChance": 0.001
    },
    "InventoryModule": {
        "Enabled": false
    },
    "LocatorModule": {
        "Enabled": false
    },
    "MapModule": {
        "Enabled": false,
        "ServerId": 12345
    },
    "MotdModule": {
        "Enabled": false,
        "Motd": [
            "<gradient:#CB2D3E:#EF473A><b>Ultimate Private SMP</b></gradient>",
            "<gradient:#FFE259:#FFA751><b>➤ WELCOME BACK LADS!</b></gradient>"
        ]
    },
    "OpenableModule": {
        "Enabled": false,
        "InitDelayInTicks": 1,
        "AllowDoubleDoors": true,
        "AllowKnocking": true,
        "AllowIronDoorByHand": false,
        "KnockingRequiresEmptyHand": true,
        "KnockingRequiresShifting": true,
        "SoundProximityRadius": 10.0
    },
    "PlayerModule": {
        "Enabled": false,
        "SkullDropChance": 0.01,
        "XpCostToBottle": 11,
        "SilkTouch": {
            "AllowSpawnerSilk": true,
            "AllowBuddingAmethystSilk": true
        }
    },
    "ScoreBoardModule": {
        "Enabled": false
    },
    "ServerInfoModule": {
        "Enabled": false,
        "ServerLinks": {
            "WEBSITE": "https://xodium.org/",
            "REPORT_BUG": "https://github.com/XodiumSoftware/VanillaPlus/issues",
            "STATUS": "https://modrinth.com/server/illyria",
            "COMMUNITY": "https://discord.gg/jusYH9aYUh"
        }
    },
    "SitModule": {
        "Enabled": false,
        "UseStairs": true,
        "UseSlabs": true
    },
    "TameableModule": {
        "Enabled": false
    }
}
```

## MiniMessage formatting

String values in the config that are displayed as in-game text support [MiniMessage](https://docs.advntr.dev/minimessage/) syntax:

| Syntax                                      | Effect                         |
|---------------------------------------------|--------------------------------|
| `<red>text</red>`                           | Colour                         |
| `<b>text</b>`                               | Bold                           |
| `<gradient:#FF0000:#0000FF>text</gradient>` | Gradient                       |
| `<reset>`                                   | Reset formatting               |
| `<player>`, `<killer>`, etc.                | Placeholders (module-specific) |
