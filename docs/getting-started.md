# Getting Started

## Installation

1. Download the latest `VanillaPlus-*.jar` from [GitHub Releases](https://github.com/XodiumSoftware/VanillaPlus/releases) or [Modrinth](https://modrinth.com/plugin/vanillaplus).
2. Drop it into your server's `plugins/` folder.
3. Start (or restart) the server.

All modules are **enabled by default** and active immediately after install. No configuration file is required.

## Customizing behaviour

Settings are hardcoded defaults defined in each module's `object Config`. To change a value, edit the source and rebuild:

```
./gradlew shadowJar
```

See the [Module reference](modules/index.md) for each module's available defaults.
