package org.xodium.vanillaplus.modules

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.serialization.Serializable
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.CommandUtils.executesCatching
import org.xodium.vanillaplus.utils.PlayerUtils.face
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
                                    .executesCatching { ctx ->
                                        if (ctx.source.sender !is Player) {
                                            instance.logger.warning(
                                                "Command can only be executed by a Player!",
                                            )
                                        }

                                        val sender = ctx.source.sender as Player
                                        val targetResolver =
                                            ctx.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                                        val target =
                                            targetResolver.resolve(ctx.source).singleOrNull()
                                                ?: return@executesCatching sender.sendMessage(
                                                    MM.deserialize(config.chatModule.i18n.playerIsNotOnline),
                                                )
                                        val message = ctx.getArgument("message", String().javaClass)

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) = playerJoin(event)

    @EventHandler
    fun on(event: PlayerSetSpawnEvent) = playerSetSpawn(event)

    /**
     * Handles asynchronous chat events.
     * @param event The [AsyncChatEvent] to be processed.
     */
    private fun asyncChat(event: AsyncChatEvent) {
        event.renderer(ChatRenderer.defaultRenderer())
        event.renderer { player, displayName, message, audience ->
            var base =
                MM.deserialize(
                    config.chatModule.chatFormat,
                    Placeholder.component("player_head", MM.deserialize("<head:${player.uniqueId}>")),
                    Placeholder.component(
                        "player",
                        displayName
                            .clickEvent(ClickEvent.suggestCommand("/w ${player.name} "))
                            .hoverEvent(
                                HoverEvent.showText(MM.deserialize(config.chatModule.i18n.clickToWhisper)),
                            ),
                    ),
                    Placeholder.component("message", message),
                )

            if (audience == player) base = base.appendSpace().append(createDeleteCross(event.signedMessage()))
            base
        }
    }

    /**
     * Handles player join events.
     * @param event The [PlayerJoinEvent] to be processed.
     */
    private fun playerJoin(event: PlayerJoinEvent) {
        val player = event.player

        var imageIndex = 0

        player.sendMessage(
            MM.deserialize(
                Regex("<image>").replace(config.chatModule.welcomeText.joinToString("\n")) { "<image${++imageIndex}>" },
                Placeholder.component("player", player.displayName()),
                *player
                    .face()
                    .lines()
                    .mapIndexed { i, line -> Placeholder.component("image${i + 1}", MM.deserialize(line)) }
                    .toTypedArray(),
            ),
        )
    }

    /**
     * Handles player set spawn events.
     * @param event The [PlayerSetSpawnEvent] to be processed.
     */
    private fun playerSetSpawn(event: PlayerSetSpawnEvent) {
        event.notification =
            MM.deserialize(
                config.chatModule.i18n.playerSetSpawn,
                Placeholder.component("notification", event.notification ?: return),
            )
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
                config.chatModule.whisperToFormat,
                Placeholder.component(
                    "player",
                    target
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${target.name} "))
                        .hoverEvent(HoverEvent.showText(MM.deserialize(config.chatModule.i18n.clickToWhisper))),
                ),
                Placeholder.component("message", MM.deserialize(message)),
            ),
        )

        target.sendMessage(
            MM.deserialize(
                config.chatModule.whisperFromFormat,
                Placeholder.component(
                    "player",
                    sender
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${sender.name} "))
                        .hoverEvent(HoverEvent.showText(MM.deserialize(config.chatModule.i18n.clickToWhisper))),
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
            .deserialize(config.chatModule.deleteCross)
            .hoverEvent(MM.deserialize(config.chatModule.i18n.deleteMessage))
            .clickEvent(ClickEvent.callback { instance.server.deleteMessage(signedMessage) })

    /** Represents the config of the module. */
    @Serializable
    data class Config(
        var enabled: Boolean = true,
        var chatFormat: String = "<player_head> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>",
        var welcomeText: List<String> =
            listOf(
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gradient:#CB2D3E:#EF473A>Welcome</gradient> <player> <click:suggest_command:'/nickname '><hover:show_text:'<gradient:#FFE259:#FFA751>Set your nickname!</gradient>'><white><sprite:items:item/name_tag></white></hover></click> <click:suggest_command:'/locator '><hover:show_text:'<gradient:#FFE259:#FFA751>Change your locator color!</gradient>'><white><sprite:items:item/compass_00></white></hover></click>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gradient:#CB2D3E:#EF473A>Check out</gradient><gray>:</gray>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gray>✦</gray> <click:run_command:'/rules'><gradient:#13547a:#80d0c7>/rules</gradient></click:run_command>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient> <gray>✦</gray> <click:open_url:'https://illyria.fandom.com'><gradient:#13547a:#80d0c7>wiki</gradient></click:open_url>",
                "<image><gradient:#FFE259:#FFA751>⯈</gradient>",
                "<gradient:#FFA751:#FFE259>]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[</gradient>",
            ),
        var whisperToFormat: String =
            "<gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>➛</gradient> <player> <reset><gradient:#FFE259:#FFA751>›</gradient> <message>",
        var whisperFromFormat: String =
            "<player> <reset><gradient:#FFE259:#FFA751>➛</gradient> <gradient:#1488CC:#2B32B2>You</gradient> <gradient:#FFE259:#FFA751>›</gradient> <message>",
        var deleteCross: String = "<dark_gray>[<dark_red><b>X</b></dark_red><dark_gray>]",
        var i18n: I18n = I18n(),
    ) {
        /** Represents the internationalization strings for the module. */
        @Serializable
        data class I18n(
            var clickMe: String = "<gradient:#FFE259:#FFA751>Click me!</gradient>",
            var clickToWhisper: String = "<gradient:#FFE259:#FFA751>Click to Whisper</gradient>",
            var playerIsNotOnline: String =
                "${instance.prefix} <gradient:#CB2D3E:#EF473A>Player is not Online!</gradient>",
            var deleteMessage: String = "<gradient:#FFE259:#FFA751>Click to delete your message</gradient>",
            var clickToClipboard: String = "<gradient:#FFE259:#FFA751>Click to copy position to clipboard</gradient>",
            var playerSetSpawn: String =
                "<gradient:#CB2D3E:#EF473A>❗</gradient> <gradient:#FFE259:#FFA751>›</gradient> <notification>",
        )
    }
}
