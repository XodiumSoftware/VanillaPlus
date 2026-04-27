package org.xodium.vanillaplus.mechanics

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
internal object ChatMechanic : ModuleInterface {
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
                                                    MM.deserialize(Config.ChatMessages.playerIsNotOnline),
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
                    Config.CHAT_FORMAT,
                    Placeholder.component("player_head", MM.deserialize("<head:${player.uniqueId}>")),
                    Placeholder.component(
                        "player",
                        displayName
                            .clickEvent(ClickEvent.suggestCommand("/w ${player.name} "))
                            .hoverEvent(
                                HoverEvent.showText(MM.deserialize(Config.ChatMessages.CLICK_TO_WHISPER)),
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
                Config.WHISPER_TO_FORMAT,
                Placeholder.component(
                    "player",
                    target
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${target.name} "))
                        .hoverEvent(HoverEvent.showText(MM.deserialize(Config.ChatMessages.CLICK_TO_WHISPER))),
                ),
                Placeholder.component("message", MM.deserialize(message)),
            ),
        )

        target.sendMessage(
            MM.deserialize(
                Config.WHISPER_FROM_FORMAT,
                Placeholder.component(
                    "player",
                    sender
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${sender.name} "))
                        .hoverEvent(HoverEvent.showText(MM.deserialize(Config.ChatMessages.CLICK_TO_WHISPER))),
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
            .deserialize(Config.DELETE_SYMBOL)
            .hoverEvent(MM.deserialize(Config.ChatMessages.CLICK_TO_DELETE))
            .clickEvent(ClickEvent.callback { instance.server.deleteMessage(signedMessage) })

    /** Represents the config of the module. */
    object Config {
        const val CHAT_FORMAT: String = "<player_head> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>"
        const val WHISPER_TO_FORMAT: String =
            "<gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>➛</gradient> " +
                "<player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>"
        const val WHISPER_FROM_FORMAT: String =
            "<player> <reset><gradient:#FFE259:#FFA751>➛</gradient> " +
                "<gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>›</gradient> <message>"
        const val DELETE_SYMBOL: String = "<dark_gray>[<dark_red><b>X</b></dark_red><dark_gray>]"

        /** Represents the chat message strings for the module. */
        object ChatMessages {
            const val CLICK_TO_WHISPER: String = "<gradient:#FFE259:#FFA751>Click to Whisper</gradient>"
            val playerIsNotOnline: String =
                "${instance.prefix} <gradient:#CB2D3E:#EF473A>Player is not Online!</gradient>"
            const val CLICK_TO_DELETE: String = "<gradient:#FFE259:#FFA751>Click to delete your message</gradient>"
        }
    }
}
