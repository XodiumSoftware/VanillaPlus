# Configuration

VanillaPlus has **no file-based configuration**. All settings are hardcoded defaults defined as `object Config` singletons inside each module's source file.

To change a value, edit the relevant `Config` object in the module's `.kt` file and rebuild the plugin.

## MiniMessage formatting

String values that are displayed as in-game text support [MiniMessage](https://docs.advntr.dev/minimessage/) syntax:

| Syntax                                      | Effect                         |
|---------------------------------------------|--------------------------------|
| `<red>text</red>`                           | Colour                         |
| `<b>text</b>`                               | Bold                           |
| `<gradient:#FF0000:#0000FF>text</gradient>` | Gradient                       |
| `<reset>`                                   | Reset formatting               |
| `<player>`, `<killer>`, etc.                | Placeholders (module-specific) |
