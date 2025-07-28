package org.xodium.vanillaplus.modules

import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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
import org.xodium.vanillaplus.utils.ExtUtils.clickRunCmd
import org.xodium.vanillaplus.utils.ExtUtils.face
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.pt
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt
import java.util.concurrent.CompletableFuture

internal class ChatModule : ModuleInterface<ChatModule.Config> {
    override val config: Config = Config()

    override fun enabled(): Boolean = config.enabled

    override fun cmds(): List<CommandData> {
        return listOf(
            CommandData(
                Commands
                    .literal("whisper")
                    .requires { it.sender.hasPermission(perms()[0]) }
                    .then(
                        Commands
                            .argument("target", StringArgumentType.string())
                            .suggests { ctx, builder ->
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
                                            val sender = ctx.source.sender as Player
                                            val targetName = ctx.getArgument("target", String::class.java)
                                            val target =
                                                instance.server.getPlayer(targetName)
                                                    ?: return@tryCatch sender.sendMessage(
                                                        "$PREFIX Player is not Online!".fireFmt().mm(),
                                                    )
                                            val message = ctx.getArgument("message", String::class.java)
                                            whisper(sender, target, message)
                                        }
                                    },
                            ),
                    ),
                "This command allows you to whisper to players.",
                listOf("w"),
            ),
        )
    }

    override fun perms(): List<Permission> =
        listOf(
            Permission(
                "${instance::class.simpleName}.whisper".lowercase(),
                "Allows use of the whisper command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: AsyncChatEvent) {
        if (!config.enabled) return
        event.renderer { source, displayName, message, _ ->
            config.chatFormat.mm(
                Placeholder.component(
                    "player",
                    displayName
                        .clickEvent(ClickEvent.suggestCommand("/w ${source.name} "))
                        .hoverEvent(HoverEvent.showText("Click to Whisper".fireFmt().mm())),
                ),
                Placeholder.component("message", message.pt().mm()),
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!config.enabled) return

        event.joinMessage(null)

        val player = event.player
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    config.joinMessage.mm(
                        Placeholder.component("player", player.displayName()),
                    ),
                )
            }

        var imageIndex = 1
        player.sendMessage(
            Regex("<image>").replace(config.welcomeText.joinToString("\n")) { "<image${imageIndex++}>" }.mm(
                Placeholder.component(
                    "player",
                    player
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/nickname ${player.name}"))
                        .hoverEvent(HoverEvent.showText("Click Me!".fireFmt().mm())),
                ),
                *player
                    .face()
                    .lines()
                    .mapIndexed { i, line -> Placeholder.component("image${i + 1}", line.mm()) }
                    .toTypedArray(),
            ),
        )
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun on(event: PlayerQuitEvent) {
        if (!config.enabled) return

        event.quitMessage(null)

        val player = event.player
        instance.server.onlinePlayers
            .filter { it.uniqueId != player.uniqueId }
            .forEach {
                it.sendMessage(
                    config.quitMessage.mm(
                        Placeholder.component("player", player.displayName()),
                    ),
                )
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
            config.whisperToFormat.mm(
                Placeholder.component(
                    "player",
                    target
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${target.name} "))
                        .hoverEvent(HoverEvent.showText("Click to Whisper".fireFmt().mm())),
                ),
                Placeholder.component("message", message.mm()),
            ),
        )

        target.sendMessage(
            config.whisperFromFormat.mm(
                Placeholder.component(
                    "player",
                    sender
                        .displayName()
                        .clickEvent(ClickEvent.suggestCommand("/w ${sender.name} "))
                        .hoverEvent(HoverEvent.showText("Click to Whisper".fireFmt().mm())),
                ),
                Placeholder.component("message", message.mm()),
            ),
        )
    }

    data class Config(
        override var enabled: Boolean = true,
        var chatFormat: String = "<player> <reset>${"›".mangoFmt(true)} <message>",
        var welcomeText: List<String> =
            listOf(
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true),
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)} ${"Welcome".fireFmt()} <player>",
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)} ${"Check out".fireFmt()}<gray>: ${
                    "/rules".clickRunCmd("Click Me!".fireFmt()).skylineFmt()
                } <gray>🟅 ${
                    "/guide".clickRunCmd("Click Me!".fireFmt()).skylineFmt()
                }",
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)}",
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true),
            ),
        var joinMessage: String = "<green>➕<reset> ${"›".mangoFmt(true)} <player>",
        var quitMessage: String = "<red>➖<reset> ${"›".mangoFmt(true)} <player>",
        var whisperToFormat: String =
            "${"You".skylineFmt()} ${"➛".mangoFmt(true)} <player> <reset>${"›".mangoFmt(true)} <message>",
        var whisperFromFormat: String =
            "<player> <reset>${"➛".mangoFmt(true)} ${"You".skylineFmt()} ${"›".mangoFmt(true)} <message>",
    ) : ModuleInterface.Config
}
