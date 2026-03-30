# Rune Module

> Adds a gem-based slot system that lets players permanently boost their stats.

## How it works

Rare **gem items** drop from the three vanilla bosses. Players open `/rune` to manage up to **5 rune slots** displayed as a hopper inventory. Placing a gem into a slot equips it; removing it returns the gem to the player's inventory. Modifiers take effect immediately on close and are restored on `login`.

| Detail           | Value                                |
|------------------|--------------------------------------|
| **Command**      | `/runes` (alias: `r`)                |
| **Permission**   | `vanillaplus.rune` (default: true)   |
| **Slots**        | 5                                    |
| **Drop sources** | Elder Guardian, Wither, Ender Dragon |

## Gems

| Gem         | Material       | Effect per slot          |
|-------------|----------------|--------------------------|
| Health Rune | Amethyst Shard | +2 max health (+1 heart) |

## Config

```kotlin
object Config {
    var runeDropChance: Double = 0.10  // 10 % chance per boss kill
}
```
