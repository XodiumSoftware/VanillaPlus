# Chat

Replaces the default chat with a custom format, adds whispers, and shows a welcome banner on first join.

## Config

| Key                 | Type      | Default   | Description                                       |
|---------------------|-----------|-----------|---------------------------------------------------|
| `Enabled`           | `Boolean` | `false`   | Enable this module                                |
| `ChatFormat`        | `String`  | See above | MiniMessage format for public chat                |
| `WhisperToFormat`   | `String`  | See above | Format shown to the sender of a whisper           |
| `WhisperFromFormat` | `String`  | See above | Format shown to the receiver of a whisper         |
| `DeleteCross`       | `String`  | See above | Clickable delete button appended to chat messages |

All strings support [MiniMessage](https://docs.advntr.dev/minimessage/) formatting.

## Commands & Permissions

| Command                       | Aliases                       | Permission            | Default  | Description            |
|-------------------------------|-------------------------------|-----------------------|----------|------------------------|
| `/whisper <player> <message>` | `w`, `msg`, `tell`, `tellraw` | `vanillaplus.whisper` | Everyone | Send a private message |

## Behaviour

- Public chat messages include a **clickable player name** that opens a whisper prompt.
- The message author sees a **clickable delete button** next to their own messages.
- Coordinates included in messages are **clickable** and copy the position to clipboard.
