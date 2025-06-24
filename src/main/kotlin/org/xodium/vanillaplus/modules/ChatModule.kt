/*
 *  Copyright (c) 2025. Xodium.
 *  All rights reserved.
 */

package org.xodium.vanillaplus.modules

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.xodium.vanillaplus.VanillaPlus.Companion.instance
import org.xodium.vanillaplus.interfaces.ModuleInterface
import org.xodium.vanillaplus.managers.ConfigManager
import org.xodium.vanillaplus.utils.ExtUtils.mm
import org.xodium.vanillaplus.utils.FmtUtils.fireFmt
import org.xodium.vanillaplus.utils.FmtUtils.mangoFmt

class ChatModule : ModuleInterface {
    override fun enabled(): Boolean = ConfigManager.data.chatModule.enabled

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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerCommandPreprocessEvent) {
        if (!enabled()) return

        val args = event.message.split(" ")
        val command = args.first().lowercase()

        if (command !in setOf("/tell", "/w", "/msg")) return

        event.isCancelled = true
        val sender = event.player

        if (args.size < 3) return sender.sendMessage("Usage: /w <player> <message>".mangoFmt().mm())

        val target = instance.server.getPlayer(args[1])
        if (target == null) return sender.sendMessage("Player not found.".fireFmt().mm())

        val message = args.drop(2).joinToString(" ")

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