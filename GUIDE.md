# Installation

## Table of Contents

- [Prerequisites](#prerequisites)
- [Download Nightly Build](#download-nightly-build)
- [Build from Source](#build-from-source)
- [Installation](#installation-1)
- [Configuration](#configuration)
- [Features](#features)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- [Paper](https://papermc.io/) Minecraft server 1.21.11
- Java 21 or newer

## Download Nightly Build

Download pre-built JARs from GitHub releases.

### Setup

1. Download the latest release:
   ```bash
   curl -L -o IllyriaPlus.jar https://github.com/XodiumSoftware/IllyriaPlus/releases/download/nightly/IllyriaPlus.jar
   ```

2. Place the JAR in your server's `plugins/` directory

## Build from Source

Build the plugins using Gradle.

### Prerequisites

- [JDK 21](https://adoptium.net/) or newer
- [Git](https://git-scm.com/)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/XodiumSoftware/IllyriaPlus.git
   cd IllyriaPlus
   ```

2. Build the plugin:
   ```bash
   ./gradlew shadowJar
   ```

3. The output JAR is at:
   ```
   build/libs/IllyriaPlus-*.jar
   ```

### Run a Test Server

To quickly test the plugin:

```bash
./gradlew runServer
```

This automatically downloads Paper 1.21.11 and starts a local test server with the plugin.

## Installation

1. Place the JAR in your server's `plugins/` directory
2. Start or restart the server
3. The plugin will enable automatically

## Configuration

IllyriaPlus plugins use **compile-time configuration** — there are no config files to edit. All settings are hardcoded in the source code.

To customize a plugin, you must:

1. Fork the repository
2. Edit the `Config` objects in each mechanic
3. Rebuild the plugin

### Mechanic Configuration Location

Each mechanic has a nested `Config` object, for example:

```kotlin
// In src/mechanics/PlayerMechanic.kt
object Config {
    const val ENABLED = true
    // ... other settings
}
```

## Features

Enhances base gameplay with custom enchantments, items, and mechanics.

### Enchantments

Eleven custom enchantments are available:

| Enchantment          | Slot      | Description                     |
|----------------------|-----------|---------------------------------|
| Verdance             | Main Hand | Enhances hoe abilities          |
| Tether               | Main Hand | Applies to tools and weapons    |
| Nimbus               | Saddle    | Happy Ghast harness enhancement |
| Earthrend            | Main Hand | Pickaxe special ability         |
| Embertread           | Feet      | Foot armor enhancement          |
| **Blaze Rod Spells** |           |                                 |
| Inferno              | Main Hand | Fire spell                      |
| Skysunder            | Main Hand | Lightning spell                 |
| Witherbrand          | Main Hand | Wither spell                    |
| Frostbind            | Main Hand | Ice spell                       |
| Tempest              | Main Hand | Wind spell                      |
| Voidpull             | Main Hand | Teleportation spell             |
| Quake                | Main Hand | Earth spell                     |

#### Mana System

Blaze Rod spells share a single mana pool:

- **Maximum**: 100 mana
- **Regeneration**: Automatic over time
- **Display**: Boss bar shows current mana
- **Refill**: Use "Potion of Arcane Restoration"

#### Wand Mechanics

Blaze rods with spell enchantments function as wands:

- **Left-click**: Cast selected spell
- **Right-click**: Cycle through available spells
- **Compatible**: All seven spells can be on one wand

#### Custom Items

- **Potion of Arcane Restoration**: Refills mana to maximum
    - Brew: Awkward Potion + Blaze Rod
    - Color: Purple (`#832466`)

#### Recipes

Custom crafting and brewing recipes:

- Chainmail armor crafting
- Diamond recycle (smelting)
- Mana potion brewing
- Custom paintings
- Rotten flesh to leather
- Log crafting improvements

## Troubleshooting

### "Plugin disabled itself"

- Verify server version is Paper 1.21.11
- Check console for version mismatch errors
- Update your server or use a compatible plugin version

### "Enchantments not showing"

- Enchantments are registered during server startup
- Check console for bootstrap errors
- Ensure you're using Paper (not Spigot or Bukkit)

### Build fails

- Verify Java 21 is installed and active:
  ```bash
  java -version
  ```
- Make sure `JAVA_HOME` is set correctly
- Try cleaning the build:
  ```bash
  ./gradlew clean
  ./gradlew shadowJar
  ```

### Ktlint errors

The project uses ktlint for code style. Fix formatting:

```bash
./gradlew ktlintFormat
```

### Mana bar not appearing

- Must have a Blaze Rod with spell enchantments in main hand
- Mana bar appears only when holding a valid spell wand

---

<p align="right"><a href="#readme-top">▲</a></p>
