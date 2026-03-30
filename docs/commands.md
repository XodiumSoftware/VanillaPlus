# Commands & Permissions

All permission nodes follow the pattern `vanillaplus.<name>`.

## Book

| Command  | Aliases | Permission               | Default  | Description                 |
|----------|---------|--------------------------|----------|-----------------------------|
| `/rules` | —       | `vanillaplus.book.rules` | Everyone | Opens the server rules book |

Each configured book generates its own command and permission derived from its `cmd` field.

## Chat

| Command                       | Aliases                       | Permission            | Default  | Description                        |
|-------------------------------|-------------------------------|-----------------------|----------|------------------------------------|
| `/whisper <player> <message>` | `w`, `msg`, `tell`, `tellraw` | `vanillaplus.whisper` | Everyone | Send a private message to a player |

## Inventory

| Command              | Aliases                                       | Permission              | Default  | Description                                                |
|----------------------|-----------------------------------------------|-------------------------|----------|------------------------------------------------------------|
| `/search [material]` | `invsearch`, `searchinv`, `invs`, `sinv`, `s` | `vanillaplus.invsearch` | Everyone | Draw particle trails to nearby containers holding the item |
| `/unload`            | `invunload`, `unloadinv`, `invu`, `uinv`, `u` | `vanillaplus.invunload` | Everyone | Move inventory items into matching nearby containers       |

## Locator

| Command                        | Aliases | Permission            | Default  | Description                 |
|--------------------------------|---------|-----------------------|----------|-----------------------------|
| `/locator <color\|hex\|reset>` | `lc`    | `vanillaplus.locator` | Everyone | Set your locator bar colour |

## Player

| Command            | Aliases | Permission             | Default  | Description                        |
|--------------------|---------|------------------------|----------|------------------------------------|
| `/nickname [name]` | `nick`  | `vanillaplus.nickname` | Everyone | Set or clear your display nickname |

## Rune

| Command                       | Aliases | Permission              | Default  | Description                   |
|-------------------------------|---------|-------------------------|----------|-------------------------------|
| `/runes`                      | `r`     | `vanillaplus.rune`      | Everyone | Open the rune equipment menu  |
| `/runes give <rune>`          | —       | `vanillaplus.rune.give` | OP       | Give yourself a rune          |
| `/runes give <rune> <player>` | —       | `vanillaplus.rune.give` | OP       | Give a rune to another player |

## Scoreboard

| Command        | Aliases       | Permission                | Default  | Description                       |
|----------------|---------------|---------------------------|----------|-----------------------------------|
| `/leaderboard` | `lb`, `board` | `vanillaplus.leaderboard` | Everyone | Toggle the leaderboard scoreboard |

All permissions default to **Everyone**.
