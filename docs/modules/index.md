# Modules

VanillaPlus is built around independent modules. Each module is self-contained, has its own config section in `config.json`, and can be enabled or disabled without affecting other modules.

## All modules

| Module                                     | Summary                                                                         | Commands             |
|--------------------------------------------|---------------------------------------------------------------------------------|----------------------|
| [Chiseled Bookshelf](chiseledbookshelf.md) | Action-bar display when interacting with chiseled bookshelves                   | —                    |
| [Chat](chat.md)                            | Custom chat format, whispers, welcome text                                      | `/whisper`           |
| [Horde](horde.md)                          | Spawn a ranked medieval enemy formation with boss bar                           | `/horde`             |
| [Dimensions](dimensions.md)                | Nether portal grief prevention                                                  | —                    |
| [Entity](entity.md)                        | Per-mob grief prevention, spawn egg drops                                       | —                    |
| [Inventory](inventory.md)                  | Search and unload nearby containers                                             | `/search`, `/unload` |
| [Locator](locator.md)                      | Personalise locator bar colour                                                  | `/locator`           |
| [Map](map.md)                              | Xaero's World Map & Minimap integration                                         | —                    |
| [MOTD](motd.md)                            | Custom server list MOTD                                                         | —                    |
| [Openable](openable.md)                    | Double doors, knocking sounds                                                   | —                    |
| [Player](player.md)                        | Nicknames, skull drops, XP bottles, death/join/quit messages, enchantment hooks | `/nickname`          |
| [Scoreboard](scoreboard.md)                | Leaderboard scoreboard toggle                                                   | `/leaderboard`       |
| [Server Info](server-info.md)              | Server links (website, Discord, bug tracker)                                    | —                    |
| [Sit](sit.md)                              | Sit on stairs and slabs                                                         | —                    |
| [Tameable](tameable.md)                    | Transfer pet ownership via lead                                                 | —                    |

## Common config pattern

Every module shares the same base config structure:

```json
{
    "FooModule": {
        "Enabled": false
    }
}
```

Set `"Enabled": true` and add any module-specific keys to activate it.
