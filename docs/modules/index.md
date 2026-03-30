# Modules

VanillaPlus is built around independent modules. Each module is self-contained and active by default.

## All modules

| Module                                     | Summary                                                                         | Commands             |
|--------------------------------------------|---------------------------------------------------------------------------------|----------------------|
| [Book](book.md)                            | Configurable in-game book commands (e.g. `/rules`)                              | `/<cmd>`             |
| [Chiseled Bookshelf](chiseledbookshelf.md) | Action-bar display when interacting with chiseled bookshelves                   | —                    |
| [Chat](chat.md)                            | Custom chat format, whispers, welcome text                                      | `/whisper`           |
| [Dimensions](dimensions.md)                | Nether portal grief prevention                                                  | —                    |
| [Entity](entity.md)                        | Per-mob grief prevention, spawn egg drops                                       | —                    |
| [Inventory](inventory.md)                  | Search and unload nearby containers                                             | `/search`, `/unload` |
| [Locator](locator.md)                      | Personalise locator bar colour                                                  | `/locator`           |
| [MOTD](motd.md)                            | Custom server list MOTD                                                         | —                    |
| [Openable](openable.md)                    | Double doors, knocking sounds                                                   | —                    |
| [Player](player.md)                        | Nicknames, skull drops, XP bottles, death/join/quit messages, enchantment hooks | `/nickname`          |
| [Rune](rune.md)                            | Gem slots that increase max health; gems drop from bosses                       | `/runes`             |
| [Scoreboard](scoreboard.md)                | Leaderboard scoreboard toggle                                                   | `/leaderboard`       |
| [Server Info](server-info.md)              | Server links (website, Discord, bug tracker)                                    | —                    |
| [Sit](sit.md)                              | Sit on stairs and slabs                                                         | —                    |
| [Tameable](tameable.md)                    | Transfer pet ownership via lead                                                 | —                    |

## Configuration

Modules have no file-based configuration. Settings are hardcoded in each module's `object Config` in source. To change a value, edit the relevant `.kt` file and rebuild.
