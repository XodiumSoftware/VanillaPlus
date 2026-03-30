# Book

Registers configurable in-game book commands. Each book opens as a readable written book in the player's client.

## Defaults

| Field   | Type             | Description                                                          |
|---------|------------------|----------------------------------------------------------------------|
| `books` | `List<BookData>` | List of book definitions (see below). Defaults to one `/rules` book. |

### BookData fields

| Field        | Type                 | Default                              | Description                                       |
|--------------|----------------------|--------------------------------------|---------------------------------------------------|
| `cmd`        | `String`             | —                                    | Command name used to open the book (e.g. `rules`) |
| `permission` | `PermissionDefault`  | `TRUE`                               | Who can run the command by default                |
| `title`      | `String`             | Capitalised `cmd` with fire gradient | MiniMessage title shown in the book UI            |
| `author`     | `String`             | `VanillaPlus`                        | Author shown in the book UI                       |
| `pages`      | `List<List<String>>` | —                                    | Pages as a list of line lists (MiniMessage)       |

## Commands

| Command  | Permission               | Default | Description               |
|----------|--------------------------|---------|---------------------------|
| `/<cmd>` | `vanillaplus.book.<cmd>` | All     | Opens the configured book |

Each entry in `books` produces its own command and permission, both derived from the `cmd` field.

## Behaviour

- One command and permission are registered per book entry at plugin startup.
- Pages are rendered with MiniMessage; each inner list is joined with newlines to form a page.
- The title and author fields also support MiniMessage formatting.
- Adding or modifying books requires editing `BookModule.Config.books` in source.
