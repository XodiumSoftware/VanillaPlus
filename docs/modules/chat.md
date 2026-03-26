# Chat

Replaces the default chat with a custom format, adds whispers, and shows a welcome banner on first join.

## Defaults

| Field               | Default                                                                                                                                            | Description                                       |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|
| `chatFormat`        | `<player_head> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>`                                                                   | MiniMessage format for public chat                |
| `whisperToFormat`   | `<gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>➛</gradient> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>` | Format shown to the sender of a whisper           |
| `whisperFromFormat` | `<player> <reset><gradient:#FFE259:#FFA751>➛</gradient> <gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>›</gradient> <message>` | Format shown to the receiver of a whisper         |
| `deleteCross`       | `<dark_gray>[<dark_red><b>X</b></dark_red><dark_gray>]`                                                                                            | Clickable delete button appended to chat messages |

All strings support [MiniMessage](https://docs.advntr.dev/minimessage/) formatting.

## Commands & Permissions

| Command                       | Aliases                       | Permission            | Default  | Description            |
|-------------------------------|-------------------------------|-----------------------|----------|------------------------|
| `/whisper <player> <message>` | `w`, `msg`, `tell`, `tellraw` | `vanillaplus.whisper` | Everyone | Send a private message |

## Behaviour

- Public chat messages include a **clickable player name** that opens a whisper prompt.
- The message author sees a **clickable delete button** next to their own messages.
- Coordinates included in messages are **clickable** and copy the position to clipboard.
