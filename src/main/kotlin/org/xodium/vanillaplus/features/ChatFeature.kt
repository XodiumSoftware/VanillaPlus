package org.xodium.vanillaplus.features

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.event.player.AsyncChatEvent
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
import org.xodium.vanillaplus.interfaces.FeatureInterface
import org.xodium.vanillaplus.utils.ExtUtils.face
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import java.util.concurrent.CompletableFuture

/** Represents a feature handling chat mechanics within the system. */
internal object ChatFeature : FeatureInterface {
    override fun cmds(): List<CommandData> {
        return listOf(
            CommandData(
                Commands
                    .literal("whisper")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands
                            .argument("target", StringArgumentType.string())
                            .suggests { _, builder ->
                                instance.server.onlinePlayers
                                    .map { it.name }
                                    .filter { it.lowercase().startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }.then(
                                Commands
                                    .argument("message", StringArgumentType.greedyString())
                                    .executes { ctx ->
                                        ctx.tryCatch {
                                            if (it.sender !is Player) {
                                                instance.logger.warning(
                                                    "Command can only be executed by a Player!",
                                                )
                                            }

                                            val sender = it.sender as Player
                                            val targetName = ctx.getArgument("target", String().javaClass)
                                            val target =
                                                instance.server
                                                    .getPlayer(targetName)
                                                    ?: return@tryCatch sender.sendMessage(
                                                        config.chatFeature.i18n.playerIsNotOnline
                                                            .mm(),
                                                    )
                                            val message = ctx.getArgument("message", String().javaClass)

                                            whisper(sender, target, message)
                                        }
                                    },
                            ),
                    ),
                "This command allows you to whisper to players",
                listOf("w", "msg", "tell", "tellraw"),
            ),
        )
    }

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance.javaClass.simpleName}.whisper".lowercase(),
                "Allows use of the whisper command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: AsyncChatEvent) {
        event.renderer(ChatRenderer.defaultRenderer())
        event.renderer { player, displayName, message, audience ->
            var base =
                config.chatFeature.chatFormat.mm(
                    Placeholder.component("player_head", "<head:${player.uniqueId}>".mm()),
                    Placeholder.component(
                        "player",
                        displayName
                            .clickEvent(ClickEvent.suggestCommand("/w ${player.name} "))
                            .hoverEvent(
                                HoverEvent.showText(
                                    config.chatFeature.i18n.clickToWhisper
                                        .mm(),
                                ),
                            ),
                    ),
                    Placeholder.component("message", message),
                )
            if (audience == player) base = base.appendSpace().append(createDeleteCross(event.signedMessage()))
            base
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        val player = event.player

        var imageIndex = 0

        player.sendMessage(
            Regex("<image>")
                .replace(config.chatFeature.welcomeText.joinToString("\n")) { "<image${++imageIndex}>" }
                .mm(
                    Placeholder.component("player", player.displayName()),
                    *player
                        .face()
                        .lines()
                        .mapIndexed { i, line -> Placeholder.component("image${i + 1}", line.mm()) }
                        .toTypedArray(),
                ),
        )
    }

    @EventHandler
    fun on(event: PlayerSetSpawnEvent) {
        event.notification =
            config.chatFeature.i18n.playerSetSpawn.mm(
                Placeholder.component(
                    "notification",
                    event.notification ?: return,
                ),
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
            config.chatFeature.whisperToFormat.mm(
                Placeholder.component(
                    "player",
                    target
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${target.name} "))
                        .hoverEvent(
                            HoverEvent.showText(
                                config.chatFeature.i18n.clickToWhisper
                                    .mm(),
                            ),
                        ),
                ),
                Placeholder.component("message", message.mm()),
            ),
        )

        target.sendMessage(
            config.chatFeature.whisperFromFormat.mm(
                Placeholder.component(
                    "player",
                    sender
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${sender.name} "))
                        .hoverEvent(
                            HoverEvent.showText(
                                config.chatFeature.i18n.clickToWhisper
                                    .mm(),
                            ),
                        ),
                ),
                Placeholder.component("message", message.mm()),
            ),
        )
    }

    /**
     * Creates to delete cross-component for message deletion.
     * @param signedMessage The signed message to be deleted.
     * @return A [net.kyori.adventure.text.Component] representing the delete cross with hover text and click action.
     */
    private fun createDeleteCross(signedMessage: SignedMessage): Component =
        config.chatFeature.deleteCross
            .mm()
            .hoverEvent(
                config.chatFeature.i18n.deleteMessage
                    .mm(),
            ).clickEvent(
                ClickEvent.callback {
                    instance.server
                        .deleteMessage(signedMessage)
                },
            )
}
