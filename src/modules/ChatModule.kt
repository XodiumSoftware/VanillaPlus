package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.Utils.MM
import org.xodium.vanillaplus.utils.Utils.prefix

/** Represents a module handling chat mechanics within the system. */
internal object ChatModule : ModuleInterface {
    override val cmds =
        listOf(
            CommandData(
                Commands
                    .literal("whisper")
                    .requires { it.sender.hasPermission(perms[0]) }
                    .then(
                        Commands
                            .argument("target", ArgumentTypes.player())
                            .then(
                                Commands
                                    .argument("message", StringArgumentType.greedyString())
                                    .executesCatching {
                                        if (it.source.sender !is Player) {
                                            instance.logger.warning(
                                                "Command can only be executed by a Player!",
                                            )
                                        }

                                        val sender = it.source.sender as Player
                                        val targetResolver =
                                            it.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                                        val target =
                                            targetResolver.resolve(it.source).singleOrNull()
                                                ?: return@executesCatching sender.sendMessage(
                                                    MM.deserialize(Config.I18n.playerIsNotOnline),
                                                )
                                        val message = it.getArgument("message", String().javaClass)

                                        whisper(sender, target, message)
                                    },
                            ),
                    ),
                "This command allows you to whisper to players",
                listOf("w", "msg", "tell", "tellraw"),
            ),
        )

    override val perms =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.whisper".lowercase(),
                "Allows use of the whisper command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: AsyncChatEvent) = asyncChat(event)

    /**
     * Handles asynchronous chat events.
     * @param event The [AsyncChatEvent] to be processed.
     */
    private fun asyncChat(event: AsyncChatEvent) {
        event.renderer(ChatRenderer.defaultRenderer())
        event.renderer { player, displayName, message, audience ->
            var base =
                MM.deserialize(
                    Config.chatFormat,
                    Placeholder.component("player_head", MM.deserialize("<head:${player.uniqueId}>")),
                    Placeholder.component(
                        "player",
                        displayName
                            .clickEvent(ClickEvent.suggestCommand("/w ${player.name} "))
                            .hoverEvent(
                                HoverEvent.showText(MM.deserialize(Config.I18n.clickToWhisper)),
                            ),
                    ),
                    Placeholder.component("message", message),
                )

            if (audience == player) base = base.appendSpace().append(createDeleteCross(event.signedMessage()))
            base
        }
    }

    /**
     * Handles the whisper command.
     * @param sender The player who sent the command.
     * @param target The player to whom the message is being sent.
     * @param message The message to be sent.
     */
    private fun whisper(
        sender: Player,
        target: Player,
        message: String,
    ) {
        sender.sendMessage(
            MM.deserialize(
                Config.whisperToFormat,
                Placeholder.component(
                    "player",
                    target
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${target.name} "))
                        .hoverEvent(HoverEvent.showText(MM.deserialize(Config.I18n.clickToWhisper))),
                ),
                Placeholder.component("message", MM.deserialize(message)),
            ),
        )

        target.sendMessage(
            MM.deserialize(
                Config.whisperFromFormat,
                Placeholder.component(
                    "player",
                    sender
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${sender.name} "))
                        .hoverEvent(HoverEvent.showText(MM.deserialize(Config.I18n.clickToWhisper))),
                ),
                Placeholder.component("message", MM.deserialize(message)),
            ),
        )
    }

    /**
     * Creates to delete cross-component for message deletion.
     * @param signedMessage The signed message to be deleted.
     * @return A [net.kyori.adventure.text.Component] representing the delete cross with hover text and click action.
     */
    private fun createDeleteCross(signedMessage: SignedMessage): Component =
        MM
            .deserialize(Config.deleteCross)
            .hoverEvent(MM.deserialize(Config.I18n.deleteMessage))
            .clickEvent(ClickEvent.callback { instance.server.deleteMessage(signedMessage) })

    /** Represents the config of the module. */
    object Config {
        var chatFormat: String = "<player_head> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>"
        var whisperToFormat: String =
            "<gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>➛</gradient> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>"
        var whisperFromFormat: String =
            "<player> <reset><gradient:#FFE259:#FFA751>➛</gradient> <gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>›</gradient> <message>"
        var deleteCross: String = "<dark_gray>[<dark_red><b>X</b></dark_red><dark_gray>]"

        /** Represents the internationalization strings for the module. */
        object I18n {
            var clickMe: String = "<gradient:#FFE259:#FFA751>Click me!</gradient>"
            var clickToWhisper: String = "<gradient:#FFE259:#FFA751>Click to Whisper</gradient>"
            var playerIsNotOnline: String =
                "${instance.prefix} <gradient:#CB2D3E:#EF473A>Player is not Online!</gradient>"
            var deleteMessage: String = "<gradient:#FFE259:#FFA751>Click to delete your message</gradient>"
            var clickToClipboard: String = "<gradient:#FFE259:#FFA751>Click to copy position to clipboard</gradient>"
        }
    }
}
