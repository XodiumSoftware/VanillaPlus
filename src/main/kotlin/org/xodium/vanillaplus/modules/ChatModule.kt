/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault
import org.xodium.vanillaplus.VanillaPlus.Companion.PREFIX
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.data.CommandData
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.SkinUtils.faceToMM
import org.xodium.vanillaplus.utils.Utils
import java.util.concurrent.CompletableFuture

class ChatModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.chatModule.enabled

    @Suppress("UnstableApiUsage")
    override fun cmds(): CommandData? {
        return CommandData(
            listOf(
                Commands.literal("whisper")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands.argument("target", ArgumentTypes.player())
                            .suggests { ctx, builder ->
                                instance.server.onlinePlayers
                                    .map { it.name }
                                    .filter { it.lowercase().startsWith(builder.remaining.lowercase()) }
                                    .forEach(builder::suggest)
                                CompletableFuture.completedFuture(builder.build())
                            }
                            .then(
                                Commands.argument("message", StringArgumentType.greedyString())
                                    .executes { ctx ->
                                        Utils.tryCatch(ctx) {
                                            whisper(
                                                it.sender as Player,
                                                instance.server.getPlayer(
                                                    ctx.getArgument(
                                                        "target",
                                                        String::class.java
                                                    )
                                                ) ?: return@tryCatch it.sender.sendMessage(
                                                    "$PREFIX Player is not Online!".fireFmt().mm()
                                                ),
                                                ctx.getArgument("message", String::class.java)
                                            )
                                        }
                                    }
                            )
                    )
            ),
            "This command allows you to whisper to players.",
            listOf("w")
        )
    }

    override fun perms(): List<Permission> {
        return listOf(
            Permission(
                "${instance::class.simpleName}.whisper.use".lowercase(),
                "Allows use of the whisper command",
                PermissionDefault.TRUE
            )
        )
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: AsyncChatEvent) {
        if (!enabled()) return
        event.renderer { source, displayName, message, _ ->
            ConfigManager.data.chatModule.chatFormat.mm(
                Placeholder.component(
                    "player",
                    displayName
                        .clickEvent(ClickEvent.suggestCommand("/w ${source.name} "))
                        .hoverEvent(HoverEvent.showText("Click to Whisper".fireFmt().mm()))
                ),
                Placeholder.component("message", PlainTextComponentSerializer.plainText().serialize(message).mm()),
            )
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!enabled()) return

        event.joinMessage(null)

        val player = event.player
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    ConfigManager.data.chatModule.joinMessage.mm(
                        Placeholder.component("player", player.displayName())
                    )
                )
            }

        val faceLines = player.faceToMM().lines()
        var imageIndex = 1
        val welcomeText =
            Regex("<image>").replace(ConfigManager.data.chatModule.welcomeText) { "<image${imageIndex++}>" }
        val imageResolvers = faceLines.mapIndexed { i, line -> Placeholder.component("image${i + 1}", line.mm()) }
        val playerComponent = player
            .displayName()
            .clickEvent(ClickEvent.suggestCommand("/nickname ${player.name}"))
            .hoverEvent(HoverEvent.showText(Utils.cmdHover.mm()))

        player.sendMessage(
            welcomeText.mm(
                Placeholder.component("player", playerComponent),
                *imageResolvers.toTypedArray()
            )
        )
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun on(event: PlayerQuitEvent) {
        if (!enabled()) return

        event.quitMessage(null)

        val player = event.player
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    ConfigManager.data.chatModule.quitMessage.mm(
                        Placeholder.component("player", player.displayName())
                    )
                )
            }
    }

    /**
     * Handles the whisper command.
     * @param sender The player who sent the command.
     * @param target The player to whom the message is being sent.
     * @param message The message to be sent.
     */
    private fun whisper(sender: Player, target: Player, message: String) {
        sender.sendMessage(
            ConfigManager.data.chatModule.whisperToFormat.mm(
                Placeholder.component(
                    "player",
                    target.displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${target.name} "))
                        .hoverEvent(HoverEvent.showText("Click to Whisper".fireFmt().mm()))
                ),
                Placeholder.component("message", message.mm())
            )
        )

        target.sendMessage(
            ConfigManager.data.chatModule.whisperFromFormat.mm(
                Placeholder.component(
                    "player",
                    sender.displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${sender.name} "))
                        .hoverEvent(HoverEvent.showText("Click to Whisper".fireFmt().mm()))
                ),
                Placeholder.component("message", message.mm())
            )
        )
    }
}