# Commands & Permissions

All permission nodes follow the pattern `vanillaplus.<name>`.

## Admin

| Command                            | Permission           | Default | Description                                        |
|------------------------------------|----------------------|---------|----------------------------------------------------|
| `/vanillaplus reload` `/vp reload` | `vanillaplus.reload` | OP      | Reload `config.json` without restarting the server |

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

## Scoreboard

| Command        | Aliases       | Permission                | Default  | Description                       |
|----------------|---------------|---------------------------|----------|-----------------------------------|
| `/leaderboard` | `lb`, `board` | `vanillaplus.leaderboard` | Everyone | Toggle the leaderboard scoreboard |

---

## Permission defaults summary

| Default      | Permissions           |
|--------------|-----------------------|
| **OP only**  | `vanillaplus.reload`  |
| **Everyone** | All other permissions |
