package org.xodium.vanillaplus.modules

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
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.utils.ExtUtils.clickOpenUrl
import org.xodium.vanillaplus.utils.ExtUtils.clickRunCmd
import org.xodium.vanillaplus.utils.ExtUtils.clickSuggestCmd
import org.xodium.vanillaplus.utils.ExtUtils.face
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.ExtUtils.prefix
import org.xodium.vanillaplus.utils.ExtUtils.tryCatch
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt
import org.xodium.vanillaplus.utils.FmtUtils.skylineFmt
import java.util.concurrent.CompletableFuture

/** Represents a module handling chat mechanics within the system. */
internal class ChatModule : ModuleInterface<ChatModule.Config> {
    override val config: Config = Config()

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
                                            if (it.sender !is Player) instance.logger.warning("Command can only be executed by a Player!")
                                            val sender = it.sender as Player
                                            val targetName = ctx.getArgument("target", String::class.java)
                                            val target =
                                                instance.server.getPlayer(targetName)
                                                    ?: return@tryCatch sender.sendMessage(
                                                        config.i18n.playerIsNotOnline.mm(),
                                                    )
                                            val message = ctx.getArgument("message", String::class.java)
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
                "${instance::class.simpleName}.whisper".lowercase(),
                "Allows use of the whisper command",
                PermissionDefault.TRUE,
            ),
        )

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun on(event: AsyncChatEvent) {
        if (!config.enabled) return

        event.renderer(ChatRenderer.defaultRenderer())

        event.renderer { player, displayName, message, audience ->
            var base =
                config.chatFormat.mm(
                    Placeholder.component("player_head", "<head:${player.uniqueId}>".mm()),
                    Placeholder.component(
                        "player",
                        displayName
                            .clickEvent(ClickEvent.suggestCommand("/w ${player.name} "))
                            .hoverEvent(HoverEvent.showText(config.i18n.clickToWhisper.mm())),
                    ),
                    Placeholder.component("message", message),
                )
            if (audience == player) base = base.appendSpace().append(createDeleteCross(event.signedMessage()))
            base
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun on(event: PlayerJoinEvent) {
        if (!config.enabled) return

        val player = event.player
        var imageIndex = 0
        player.sendMessage(
            Regex("<image>")
                .replace(config.welcomeText.joinToString("\n")) { "<image${++imageIndex}>" }
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
        if (!enabled()) return

        event.notification =
            config.i18n.playerSetSpawn.mm(Placeholder.component("notification", event.notification ?: return))
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
                        .hoverEvent(HoverEvent.showText(config.i18n.clickToWhisper.mm())),
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
                        .hoverEvent(HoverEvent.showText(config.i18n.clickToWhisper.mm())),
                ),
                Placeholder.component("message", message.mm()),
            ),
        )
    }

    /**
     * Creates to delete cross-component for message deletion.
     * @param signedMessage The signed message to be deleted.
     * @return A [Component] representing the delete cross with hover text and click action.
     */
    private fun createDeleteCross(signedMessage: SignedMessage): Component =
        config.deleteCross
            .mm()
            .hoverEvent(config.i18n.deleteMessage.mm())
            .clickEvent(ClickEvent.callback { instance.server.deleteMessage(signedMessage) })

    data class Config(
        var chatFormat: String = "<player_head> <player> <reset>${"›".mangoFmt(true)} <message>",
        var welcomeText: List<String> =
            listOf(
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true),
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)} ${"Welcome".fireFmt()} <player> ${
                    "<white><sprite:item/name_tag></white>".clickSuggestCmd(
                        "/nickname ",
                        "Set your nickname!".mangoFmt(),
                    )
                }",
                "<image>${"⯈".mangoFmt(true)}",
                "<image>${"⯈".mangoFmt(true)} ${"Check out".fireFmt()}<gray>:",
                "<image>${"⯈".mangoFmt(true)} ${
                    "<white><sprite:item/writable_book></white>".clickRunCmd(
                        "/rules",
                        "View the server /rules".mangoFmt(),
                    )
                }",
                "<image>${"⯈".mangoFmt(true)} ${
                    "<white><sprite:item/light></white>".clickOpenUrl(
                        "https://illyria.fandom.com",
                        "Visit the wiki!".mangoFmt(),
                    )
                }",
                "<image>${"⯈".mangoFmt(true)}",
                "]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[=]|[".mangoFmt(true),
            ),
        var whisperToFormat: String =
            "${"You".skylineFmt()} ${"➛".mangoFmt(true)} <player> <reset>${"›".mangoFmt(true)} <message>",
        var whisperFromFormat: String =
            "<player> <reset>${"➛".mangoFmt(true)} ${"You".skylineFmt()} ${"›".mangoFmt(true)} <message>",
        var deleteCross: String = "<dark_gray>[<dark_red><b>X</b></dark_red><dark_gray>]",
        var i18n: I18n = I18n(),
    ) : ModuleInterface.Config {
        data class I18n(
            var clickMe: String = "Click me!".mangoFmt(),
            var clickToWhisper: String = "Click to Whisper".mangoFmt(),
            var playerIsNotOnline: String = "${instance.prefix} Player is not Online!".fireFmt(),
            var deleteMessage: String = "Click to delete your message".mangoFmt(),
            var clickToClipboard: String = "Click to copy position to clipboard".mangoFmt(),
            var playerSetSpawn: String = "${"❗".fireFmt()} ${"›".mangoFmt(true)} <notification>",
        )
    }
}
